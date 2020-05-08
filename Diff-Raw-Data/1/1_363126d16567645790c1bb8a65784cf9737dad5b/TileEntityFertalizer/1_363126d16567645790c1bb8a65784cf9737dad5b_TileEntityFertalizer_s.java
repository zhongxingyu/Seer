 package com.cane.tileentity;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 
 public abstract class TileEntityFertalizer extends TileEntityMachine
 {
 	public int work;
 	public int totalTime;
 	
 	public ItemStack itemNeeded;
 	
 	@Override
 	public int getTexture(int side)
 	{
 		int i = 0;
 		
 		if(side == 0)i = 16 * 3;
 		else if(side == 1)i = 16 * 1;
 		else if(side == face)i = (16 * 4) + getFaceTexture();
 		else i = 16 * 2;
 		
 		return i;
 	}
 	
 	public abstract int getFaceTexture();
 	
 	@Override
 	public void readFromNBT(NBTTagCompound tag)
 	{
 		super.readFromNBT(tag);
 		
 		this.work = tag.getInteger("work");
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound tag)
 	{
 		super.writeToNBT(tag);
 		
 		tag.setInteger("work", this.work);
 	}
 
 	@Override
 	public void updateEntity()
 	{
 		super.updateEntity();
 		
 		if(work > 0)
 		{
 			work--;
 			
 			if(work <= 0)
 			{
 				if(worldObj.isRemote)
 				{
 					fertalize();
 				}
 				getNewItems();
 			}
 		}
 		else
 		{
 			getNewItems();
 		}
 	}
 	
 	public void getNewItems()
 	{
 		for(int i = 0; i < items.length; i++)
 		{
 			if(itemExist(items[i]) && items[i].isItemEqual(itemNeeded))
 			{
 				decrStackSize(i, 1);
 				work = totalTime;
 			}
 		}
 	}
 	
 	public int getProgress()
 	{
 		if(work <= 0)
 		{
 			return 0;
 		}
 		return 52 - (work * 52 / totalTime);
 	}
 	
 	public abstract void fertalize();
 }
