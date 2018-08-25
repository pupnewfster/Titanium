/*
 * This file is part of Titanium
 * Copyright (C) 2018, Horizon Studio <contact@hrznstudio.com>, All rights reserved.
 *
 * This means no, you cannot steal this code. This is licensed for sole use by Horizon Studio and its subsidiaries, you MUST be granted specific written permission by Horizon Studio to use this code, thinking you have permission IS NOT PERMISSION!
 */
package com.hrznstudio.titanium.base.block.tile.progress;

import com.hrznstudio.titanium.base.api.IFactory;
import com.hrznstudio.titanium.base.api.client.IGuiAddon;
import com.hrznstudio.titanium.base.api.client.IGuiAddonProvider;
import com.hrznstudio.titanium.base.block.tile.TileBase;
import com.hrznstudio.titanium.cassandra.client.gui.addon.ProgressBarGuiAddon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class PosProgressBar implements INBTSerializable<NBTTagCompound>, IGuiAddonProvider {

    private int posX;
    private int posY;
    private int progress;
    private int maxProgress;
    private int progressIncrease;
    private Predicate<TileEntity> canIncrease;
    private Predicate<TileEntity> canReset;
    private int tickingTime;
    private Runnable onFinishWork;
    private Runnable onTickWork;
    private TileBase tileBase;

    public PosProgressBar(int posX, int posY, int maxProgress) {
        this.posX = posX;
        this.posY = posY;
        this.progress = 0;
        this.maxProgress = maxProgress;
        this.progressIncrease = 1;
        this.canIncrease = tileEntity -> false;
        this.canReset = tileEntity -> true;
        this.tickingTime = 1;
        this.onFinishWork = () -> {
        };
        this.onTickWork = () -> {
        };
    }

    /**
     * Sets a runnable to be executed when the bar is completed
     *
     * @param runnable The runnable
     * @return Self
     */
    public PosProgressBar setOnFinishWork(Runnable runnable) {
        this.onFinishWork = runnable;
        return this;
    }

    /**
     * Sets a runnable to be executed every time the bar ticks
     *
     * @param runnable The runnable
     * @return Self
     */
    public PosProgressBar setOnTickWork(Runnable runnable) {
        this.onTickWork = runnable;
        return this;
    }

    /**
     * Sets the tile where this bar is running
     *
     * @param tileBase The tile
     * @return Self
     */
    public PosProgressBar setTile(TileBase tileBase) {
        this.tileBase = tileBase;
        return this;
    }

    /**
     * Gets the tile where this bar is running
     *
     * @return The tile
     */
    public TileBase getTileBase() {
        return tileBase;
    }

    /**
     * Gets if the bar can reset
     *
     * @return True if the bar can be reseted
     */
    public Predicate<TileEntity> getCanReset() {
        return canReset;
    }

    /**
     * Sets if the the bar can be reseted when the progress is completed
     *
     * @param canReset A Predicate
     * @return Self
     */
    public PosProgressBar setCanReset(Predicate<TileEntity> canReset) {
        this.canReset = canReset;
        return this;
    }

    /**
     * Ticks the bar so it can increase if possible, managed by {@link MultiProgressBarHandler#update()}
     */
    public void tickBar() {
        if (tileBase != null && tileBase.getWorld().getTotalWorldTime() % tickingTime == 0) {
            this.progress += progressIncrease;
            tileBase.markForUpdate();
            this.onTickWork.run();
        }
        if (progress > maxProgress) {
            this.progress = 0;
            this.onFinishWork.run();
        }
    }

    /**
     * Gets where the bar is located in the X
     * @return the x position
     */
    public int getPosX() {
        return posX;
    }

    /**
     * Gets where the bar is located in the X
     * @return the y position
     */
    public int getPosY() {
        return posY;
    }

    /**
     * Gets if the progress can be increased
     * @return A predicate
     */
    public Predicate<TileEntity> getCanIncrease() {
        return canIncrease;
    }

    /**
     * Sets a predicate to check if the bar can be increased
     * @param canIncrease A predicate
     * @return Self
     */
    public PosProgressBar setCanIncrease(Predicate<TileEntity> canIncrease) {
        this.canIncrease = canIncrease;
        return this;
    }

    /**
     * Gets the current progress
     * @return The progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the progress bar progress
     * @param progress The progress to set
     */
    public void setProgress(int progress) {
        this.progress = progress;
        if (tileBase != null) tileBase.markForUpdate();
    }

    /**
     * Gets the max progress of the bar
     * @return The bas progress
     */
    public int getMaxProgress() {
        return maxProgress;
    }

    /**
     * Sets the max progress of the bar
     *
     * @param maxProgress The max progress
     * @return Self
     */
    public PosProgressBar setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        return this;
    }

    /**
     * Gets how often the bar ticks
     * @return The tick the bar tries to increase
     */
    public int getTickingTime() {
        return tickingTime;
    }

    /**
     * Sets how often the bar ticks
     * @param tickingTime The ticking time
     * @return Self
     */
    public PosProgressBar setTickingTime(int tickingTime) {
        this.tickingTime = tickingTime;
        return this;
    }

    /**
     * Gets how much the bar increases when it can increase progress
     * @return The amount it increases
     */
    public int getProgressIncrease() {
        return progressIncrease;
    }

    /**
     * Sets how much the bar will increase when it can increase
     * @param progressIncrease The increase amount
     * @return Self
     */
    public PosProgressBar setProgressIncrease(int progressIncrease) {
        this.progressIncrease = progressIncrease;
        return this;
    }

    /**
     * Gets the Gui Addons that it will be added to the machine GUI
     * @return A list of GUI addon factories
     */
    @Override
    public List<IFactory<? extends IGuiAddon>> getGuiAddons() {
        return Collections.singletonList(() -> new ProgressBarGuiAddon(posX, posY, this));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("Tick", progress);
        compound.setInteger("MaxProgress", maxProgress);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        progress = nbt.getInteger("Tick");
        maxProgress = nbt.getInteger("MaxProgress");
    }
}
