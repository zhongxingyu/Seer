 /**
  * Copyright (C) 2014 Kolmogorov Alexey
  * 
  * This file part of ulmc.ru ModPack
  * 
  * ulmc.ru ModPack is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ulmc.ru ModPack is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see [http://www.gnu.org/licenses/].
  * 
  */
 package ru.ulmc.extender.block;
 
 import java.util.Random;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import ru.ulmc.extender.UltimateExtender;
 import ru.ulmc.extender.gui.GuiBones;
 import ru.ulmc.extender.tileentity.TileEntityBones;
 
 public class BlockBones extends BasicFallingBlock {
 
 	private final Random random = new Random();
 
 	public BlockBones(int i, String name) {
 		super(i, Material.ground, name);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
 		setHardness(1.0F);
 		setResistance(2.0F);
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float what,
 			float these, float are) {
 
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if (tileEntity == null || player.isSneaking()) {
 			return false;
 		}
 		player.openGui(UltimateExtender.instance, GuiBones.GUI_ID, world, x, y, z);
 		return true;
 	}
 
 	@Override
 	public int getRenderType() {
 		return -1;
 	}
 
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	private boolean canPlaceStandingBlockOn(World par1World, int par2, int par3, int par4) {
 		if (par1World.doesBlockHaveSolidTopSurface(par2, par3, par4)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
 		return canPlaceStandingBlockOn(par1World, par2, par3 - 1, par4);
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
 		dropItems(world, x, y, z);
 		super.breakBlock(world, x, y, z, par5, par6);
 	}
 
 	@Override
 	protected void tryToFall(World world, int x, int y, int z) {
 		if (canFallBelow(world, x, y - 1, z) && y >= 0) {
 			byte b0 = 32;
 
 			if (world.checkChunksExist(x - b0, y - b0, z - b0, x + b0, y + b0, z + b0)) {
 				int originY = y;
 				TileEntityBones te = (TileEntityBones) world.getBlockTileEntity(x, y, z);
 				TileEntityBones teb = new TileEntityBones(te.getInventory());
 				int metadata = world.getBlockMetadata(x, y, z);
 				
 				while (canFallBelow(world, x, y - 1, z) && y > 0) {
 					--y;
 				}
 
 				if (y > 0) {
 					world.setBlock(x, y, z, this.blockID);
 					world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
 					world.setBlockTileEntity(x, y, z, teb);
 					if (!canPlaceStandingBlockOn(world, x, y - 1, z)) {
 						world.setBlockToAir(x, y, z);
 					}
 				}
 
 				world.removeBlockTileEntity(x, originY, z);
 				world.setBlockToAir(x, originY, z);
 
 			}
 		}
 	}
 
 	private void dropItems(World world, int x, int y, int z) {
 
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if (!(tileEntity instanceof IInventory)) {
 			return;
 		}
 		IInventory inventory = (IInventory) tileEntity;
 
 		for (int i = 0; i < inventory.getSizeInventory(); i++) {
 			ItemStack item = inventory.getStackInSlot(i);
 
 			if (item != null && item.stackSize > 0) {
 				float rx = random.nextFloat() * 0.8F + 0.1F;
 				float ry = random.nextFloat() * 0.8F + 0.1F;
 				float rz = random.nextFloat() * 0.8F + 0.1F;
 
 				EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz, new ItemStack(item.itemID,
 						item.stackSize, item.getItemDamage()));
 
 				if (item.hasTagCompound()) {
 					entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
 				}
 
 				float factor = 0.05F;
 				entityItem.motionX = random.nextGaussian() * factor;
 				entityItem.motionY = random.nextGaussian() * factor + 0.2F;
 				entityItem.motionZ = random.nextGaussian() * factor;
 				world.spawnEntityInWorld(entityItem);
 				item.stackSize = 0;
 			}
 		}
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityBones();
 	}
 }
