 /*******************************************************************************
  * Copyright (c) 2012 Mast3rPlan.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     Mast3rPlan
  ******************************************************************************/
 package net.brokenpineapple.robotcraft.world.blocks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.brokenpineapple.robotcraft.RobotCraft;
 import net.minecraft.src.EntityItem;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.TileEntity;
 
 public class CauldronTileEntity extends TileEntity {
 
 	private long catalystTime = 0;
 	
 	@Override
 	public void updateEntity() {
 		if(super.getBlockMetadata() == 1) {
 			boolean containsCatalyst = false;
 			EntityItem catalystStack = null;
 			
 			List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, super.getBlockType().getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord));
 			for(EntityItem item : items) {
 				if(item.item.itemID == Item.blazeRod.shiftedIndex) {
 					containsCatalyst = true;
 					catalystStack = item;
 				}
 				item.age = 0;
 			}
 			
 			if(containsCatalyst) {
 				if(!worldObj.isRemote) {
 					catalystTime++;
 					if(catalystTime > 80) {
 						catalystTime = 0;
 						if(--catalystStack.item.stackSize == 0) {
 							catalystStack.setDead();
 						}
 						worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "random.fizz", 0.5F, 2.6F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.8F);
 						
 						boolean success = true;
 						
 						if(!this.absorbItems(items, Item.sugar, 6)) success = false;
 						if(!this.absorbItems(items, Item.ingotIron, 4)) success = false;
 						if(!this.absorbItems(items, RobotCraft.enderDustItem, 2)) success = false;
 						
 						if(success) {
							EntityItem itemStack = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, new ItemStack(Item.appleRed));
 							itemStack.motionY = 0.3;
 							worldObj.spawnEntityInWorld(itemStack);
 						}
 						
 					}
 				} else {
 					if(worldObj.rand.nextInt(12) == 0)
 						worldObj.playSound(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "liquid.lavapop", 0.5F, 0.4F + worldObj.rand.nextFloat() * 0.2F);
 					worldObj.playSound(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "liquid.lavapop", worldObj.rand.nextFloat() * 0.25f, 0.4F + worldObj.rand.nextFloat() * 0.2F);
 					worldObj.spawnParticle(
 						"smoke", 
 						xCoord + 0.2 + worldObj.rand.nextDouble() * 0.6, 
 						yCoord + 0.4, 
 						zCoord + 0.2 + worldObj.rand.nextDouble() * 0.6, 
 						0.0D, 0.0D, 0.0D
 					);
 				}
 			}
 		}
 	}
 	
 	private boolean absorbItems(List<EntityItem> items, Item target, int amount) {
 		List<EntityItem> targets = new ArrayList<EntityItem>();
 		
 		for(EntityItem itemStack : items) {
 			if(itemStack.item.itemID == target.shiftedIndex) {
 				targets.add(itemStack);
 			}
 		}
 		
 		return this.absorbItems(targets, amount);
 	}
 	
 	private boolean absorbItems(List<EntityItem> targets, int amount) {
 		int absorbedItems = 0;
 		for(EntityItem itemStack : targets) {
 			while(absorbedItems < amount && itemStack.item.stackSize > 0) {
 				itemStack.item.stackSize--;
 				absorbedItems++;
 			}
 			if(itemStack.item.stackSize == 0) {
 				itemStack.setDead();
 			}
 		}
 		return absorbedItems == amount;
 	}
 	
 }
