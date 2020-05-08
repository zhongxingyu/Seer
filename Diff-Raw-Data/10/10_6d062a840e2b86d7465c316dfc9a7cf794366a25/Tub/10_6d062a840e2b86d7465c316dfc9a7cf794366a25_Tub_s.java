 /**
  * Copyright (c) Beliar, 2012
  * https://github.com/Beliaar/Butchery
  *
  * Butchery is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license located in
  * https://github.com/Beliaar/Butchery/wiki/License
  */
 package butchery.common.blocks;
 
 import java.util.Random;
 
 import net.minecraft.src.BlockContainer;
 import net.minecraft.src.EntityItem;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import butchery.common.Butchery;
 
 public class Tub extends BlockContainer {
 
 	private Class<TileEntityTub> TubEntity;
 
 	public Tub(int tubID, Class<TileEntityTub> argTubEntity) {
 		super(tubID, Material.wood);
 		this.TubEntity = argTubEntity;
 		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.6875F, 1.0F);
 	}
 
 	@Override
 	public boolean hasTileEntity(int metadata) {
 		return true;
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World var1) {
 		try {
 			return (TileEntity) TubEntity.newInstance();
 		} catch (Exception error) {
 			throw new RuntimeException(error);
 		}
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
 
 	@Override
 	public boolean onBlockActivated(World world, int par2, int par3, int par4,
 			EntityPlayer player, int par6, float par7, float par8, float par9) {
 		if (player.isSneaking()) {
 			return false;
 		}
 		if (!world.isRemote) {
 			ItemStack currentItem = player.inventory.getCurrentItem();
 
 			if (currentItem == null) {
 				openGui(world, par2, par3, par4, player);
 			} else {
 				TileEntityTub entity = (TileEntityTub) world
 						.getBlockTileEntity(par2, par3, par4);
 				if (!entity.processActivate(player, world, currentItem)) {
 					openGui(world, par2, par3, par4, player);
 				} else {
 					return true;
 				}
 			}
 		}
 		return true;
 	}
 
 	private void openGui(World world, int par2, int par3, int par4,
 			EntityPlayer player) {
 		player.openGui(Butchery.instance, 0, world, par2, par3, par4);
 	}
 
 	@Override
 	public void fillWithRain(World world, int par2, int par3, int par4) {
 		TileEntityTub entity = (TileEntityTub) world.getBlockTileEntity(par2,
 				par3, par4);
 		entity.fillWithRain(world, par2, par3, par4);
 	}
 
 	private void dropItems(World world, int x, int y, int z) {
 		Random rand = new Random();
 
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if (!(tileEntity instanceof IInventory)) {
 			return;
 		}
 		IInventory inventory = (IInventory) tileEntity;
 
 		for (int i = 0; i < inventory.getSizeInventory(); i++) {
 			ItemStack item = inventory.getStackInSlot(i);
 
 			if (item != null && item.stackSize > 0) {
 				float rx = rand.nextFloat() * 0.8F + 0.1F;
 				float ry = rand.nextFloat() * 0.8F + 0.1F;
 				float rz = rand.nextFloat() * 0.8F + 0.1F;
 
 				EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z
 						+ rz, new ItemStack(item.itemID, item.stackSize,
 						item.getItemDamage()));
 
 				if (item.hasTagCompound()) {
 					entityItem.item.setTagCompound((NBTTagCompound) item
 							.getTagCompound().copy());
 				}
 
 				float factor = 0.05F;
 				entityItem.motionX = rand.nextGaussian() * factor;
 				entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
 				entityItem.motionZ = rand.nextGaussian() * factor;
 				world.spawnEntityInWorld(entityItem);
 				item.stackSize = 0;
 			}
 		}
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
 		dropItems(world, x, y, z);
 		super.breakBlock(world, x, y, z, par5, par6);
 	}
 
 }
