 package net.minecraft.src.WirelessRedstonePipes;
 
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.RedstoneEther;
 import net.minecraft.src.buildcraft.api.EntityPassiveItem;
 import net.minecraft.src.buildcraft.api.Orientations;
 import net.minecraft.src.buildcraft.core.Utils;
 import net.minecraft.src.buildcraft.transport.TileGoldenPipe;
 
 /**
  * @author sifldoer
  * 
  */
 public class TileWirelessGoldenPipe extends TileGoldenPipe implements
 		IWirelessPipe {
 
 	public Object currentFreq;
 
 	/**
 	 * 
 	 */
 	TileWirelessGoldenPipe()
 	{
 		super();
 		currentFreq = 0;
 	}
 
 	/**
 	 * @param item
 	 * @param orientation
 	 */
 	public void entityEntering(EntityPassiveItem item, Orientations orientation) {
 		if (isPowered())
 			item.speed = Utils.pipeNormalSpeed * 20F;
 
 		super.entityEntering(item, orientation);
 	}
 
 	@Override
 	public String getInvName() {
 		return "Wireless Golden Pipe";
 	}
 
 	@Override
 	public Object getFreq() {
 		return currentFreq;
 	}
 
 	@Override
 	public void setFreq(int frequency) {
 		if (frequency < 0)
 			frequency = 0;
 		if (frequency > 9999)
 			frequency = 9999;
 
 		currentFreq = frequency;
 		updateEntity();
 	}
 
 	@Override
 	public boolean isPowered() {
 		return RedstoneEther.getInstance().getFreqState(currentFreq);
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
 			return false;
 		}
 		return entityplayer.getDistanceSq((double) xCoord + 0.5D,
 				(double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64D;
 	}
 }
