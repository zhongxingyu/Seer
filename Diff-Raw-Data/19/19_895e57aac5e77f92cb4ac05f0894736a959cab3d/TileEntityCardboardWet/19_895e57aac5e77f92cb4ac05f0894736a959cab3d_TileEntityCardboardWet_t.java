 package sobiohazardous.minestrappolation.extradecor.tileentity;
 
 import sobiohazardous.minestrappolation.extradecor.ExtraDecor;
 import sobiohazardous.minestrappolation.extraores.block.BlockCopper;
 import sobiohazardous.minestrappolation.util.BlockFunctions;
 import net.minecraft.block.Block;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class TileEntityCardboardWet extends TileEntity
 {
 	private long ticks = 0;
 	/**
      * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
      * ticks and creates a new spawn inside its implementation.
      */
     public void updateEntity()
     {
     	ticks++;
 
     	if(isDry(worldObj))
     	{ 	
     		if(ticks == 1000)
     		{
             	this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, ExtraDecor.cardboardBlock.blockID);
 
     		}
     	}
     
     }
     
     public boolean isDry(World world)
     {
     	if(!BlockFunctions.isWaterTouchingAllSides(world, this.xCoord, this.yCoord, this.zCoord))
     	{
     		return true;
     	}
     	return false;
     }
 }
