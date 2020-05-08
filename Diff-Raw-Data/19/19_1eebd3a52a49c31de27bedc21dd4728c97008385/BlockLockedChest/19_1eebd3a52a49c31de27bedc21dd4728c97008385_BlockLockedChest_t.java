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
 
 import java.util.Iterator;
 import java.util.Random;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.passive.EntityOcelot;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import ru.ulmc.extender.Reference;
 import ru.ulmc.extender.UltimateExtender;
 import ru.ulmc.extender.gui.GuiLockedChest;
 import ru.ulmc.extender.item.ItemPicklock;
 import ru.ulmc.extender.tileentity.TileEntityLockedChest;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockLockedChest extends BlockContainer implements UlmcBlock {
 
 	private int chestType;
 	private String name;
 	private Random random = new Random();
 
 	public BlockLockedChest(int i, String aName) {
 		super(i, Material.iron);
 		setHardness(1.0F);
 		setResistance(2000.0F);
 		setUnlocalizedName(aName);
 		name = aName;
 		setCreativeTab(CreativeTabs.tabDecorations);
 		setTextureName(Reference.RES_NAME + aName);
 		setTickRandomly(true);
 		chestType = 100;
 		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
 	}
 
 	/**
 	 * Is this block (a) opaque and (b) a full 1m cube? This determines whether
 	 * or not to render the shared face of two adjacent blocks and also whether
 	 * the player can attach torches, redstone wire, etc to this block.
 	 */
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	/**
 	 * If this block doesn't render as an ordinary block it will return False
 	 * (examples: signs, buttons, stairs, etc)
 	 */
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	/**
 	 * The type of render function that is called for this block
 	 */
 	public int getRenderType() {
 		return 22;
 	}
 
 	public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int par5) {
 		TileEntityLockedChest te = (TileEntityLockedChest) blockAccess.getBlockTileEntity(x, y, z);
 		UltimateExtender.logger.info("isProvidingWeakPower.isPowered(): " + te.isPowered());
 		return (te.isPowered() > 0) ? 15 : 0;
 	}
 
 	/**
 	 * Returns true if the block is emitting direct/strong redstone power on the
 	 * specified side. Args: World, X, Y, Z, side. Note that the side is
 	 * reversed - eg it is 1 (up) when checking the bottom of the block.
 	 */
 	public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int par5) {
 		TileEntityLockedChest te = (TileEntityLockedChest) blockAccess.getBlockTileEntity(x, y, z);
 		UltimateExtender.logger.info("isProvidingStrongPower.isPowered(): " + te.isPowered());
 		if (te.isPowered() == 0) {
 			return 0;
 		} else {
 			int j1 = te.isPowered() & 7;
 			return j1 == 5 && par5 == 1 ? 15 : (j1 == 4 && par5 == 2 ? 15 : (j1 == 3 && par5 == 3 ? 15 : (j1 == 2
 					&& par5 == 4 ? 15 : (j1 == 1 && par5 == 5 ? 15 : 0))));
 		}
 	}
 
 	public void updateTick(World par1World, int x, int y, int z, Random par5Random) {
 		if (!par1World.isRemote) {
 			TileEntityLockedChest te = (TileEntityLockedChest) par1World.getBlockTileEntity(x, y, z);
 
 			if (te.isPowered() != 0) {				
 				te.setProvidingPower(false);
 				par1World.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
 			}
 		}
 	}
	@Override
 	public int tickRate(World par1World) {
 		return 20;
 	}
 
 	/**
 	 * Can this block provide power. Only wire currently seems to have this
 	 * change based on its state.
 	 */
 	@Override
 	public boolean canProvidePower() {
 		return true;
 	}
 
 	/**
 	 * Called when the block is placed in the world.
 	 */
 	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase,
 			ItemStack par6ItemStack) {
 		byte b0 = 0;
 		int l = MathHelper.floor_double((double) (par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
 
 		if (l == 0) {
 			b0 = 2;
 		} else if (l == 1) {
 			b0 = 5;
 		} else if (l == 2) {
 			b0 = 3;
 		} else if (l == 3) {
 			b0 = 4;
 		}
 
 		par1World.setBlockMetadataWithNotify(par2, par3, par4, b0, 2);
 		
 		if (par5EntityLivingBase instanceof EntityPlayer) {
 			TileEntityLockedChest chestTE = (TileEntityLockedChest) par1World.getBlockTileEntity(par2, par3, par4);
 			
 			chestTE.setOwnerName(((EntityPlayer)par5EntityLivingBase).username);
 		}
 	}
 
 	
 	/*
 	 * Called upon block activation (right click on the block.)
 	 */
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
 			float par8, float par9) {
 		if(player.isUsingItem()) {
 			return false;
 		} 
 		
 		if (!world.isRemote) {
 			
 			TileEntityLockedChest lockedChestTE = (TileEntityLockedChest) world.getBlockTileEntity(x, y, z);
 			ItemStack hold = player.inventory.getCurrentItem();
 			boolean isAllowToOpen = false;
 			boolean updateEntity = false;
 			String failChatMessage = "It's locked!";
 			
 			if (player.capabilities.isCreativeMode) {
 				isAllowToOpen = true;
 			} else if(isOcelotBlockingChest(world, x, y, z)) {
 				isAllowToOpen = false;
 			} else if (player.username != null && player.username.equals(lockedChestTE.getOwnerName())) {
 				isAllowToOpen = true;
 			} else if(!lockedChestTE.isChestProtected()) {
 				isAllowToOpen = true;
 				if(hold != null && (hold.getItem() instanceof ItemPicklock)) {
 					updateEntity = true;
 					lockedChestTE.enforceChest(player.username);
 				}
 			} else {
 				if(hold == null) {
 					isAllowToOpen = false;
 				} else {
 					if((hold.getItem() instanceof ItemPicklock)) {
 						
 						// Cooldown timeout		
 						ItemPicklock picklock = (ItemPicklock)hold.getItem();
 						hold = picklock.onItemRightClick(hold, world, player);
 						
 						int picklockingStatus = lockedChestTE.tryToEnforceChest(hold, player);
 						if(picklockingStatus == TileEntityLockedChest.PICKLOCKING_SUCCESSED) {
 							updateEntity = true;
 							isAllowToOpen = true;
 						} else if(picklockingStatus == TileEntityLockedChest.PICKLOCKING_KEY_DAMAGED) {
 							//updateEntity = true; if some effects will realised
 							failChatMessage = "Key was damaged!";
 						} else if(picklockingStatus == TileEntityLockedChest.PICKLOCKING_PROTECTOR) {
 							//updateEntity = true; if some effects will realised
 							failChatMessage = "Protector Tratata";
 						} else {
 							failChatMessage = "picklocking failed!";
 						}
 						
 					} else if(lockedChestTE.isKeyAndCipherMatches(hold)) {
 						isAllowToOpen = true;
 					}
 				}				
 			}
 			
 			if (isAllowToOpen) {
 				player.openGui(UltimateExtender.instance, GuiLockedChest.GUI_ID, world, x, y, z);
 				
 			} else {
 				if(updateEntity) {
 					UltimateExtender.markSomeBlockForUpdate(player.worldObj, x, y, z);
 				}
 				player.addChatMessage(failChatMessage);
 			}
 			return true;
 		}
 		return true;
 	}
	@Override
 	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z) {
 		TileEntityLockedChest lockedChestTE = (TileEntityLockedChest) world.getBlockTileEntity(x, y, z);
 		if (player.username.equals(lockedChestTE.getOwnerName()) || player.capabilities.isCreativeMode) {
 			this.dropBlockAsItem(world, x, y, z, 1, 0);
 			return world.setBlockToAir(x, y, z);
 		} else {
 			return false;
 		}
 	}
	@Override
 	public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
     {
 		TileEntityLockedChest chestTE = (TileEntityLockedChest)par1World.getBlockTileEntity(par2, par3, par4);
 
         if (chestTE != null)
         {
             for (int j1 = 0; j1 < chestTE.getSizeInventory(); ++j1)
             {
                 ItemStack itemstack = chestTE.getStackInSlot(j1);
 
                 if (itemstack != null)
                 {
                     float f = this.random.nextFloat() * 0.8F + 0.1F;
                     float f1 = this.random.nextFloat() * 0.8F + 0.1F;
                     EntityItem entityitem;
 
                     for (float f2 = this.random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; par1World.spawnEntityInWorld(entityitem))
                     {
                         int k1 = this.random.nextInt(21) + 10;
 
                         if (k1 > itemstack.stackSize)
                         {
                             k1 = itemstack.stackSize;
                         }
 
                         itemstack.stackSize -= k1;
                         entityitem = new EntityItem(par1World, (double)((float)par2 + f), (double)((float)par3 + f1), (double)((float)par4 + f2), new ItemStack(itemstack.itemID, k1, itemstack.getItemDamage()));
                         float f3 = 0.05F;
                         entityitem.motionX = (double)((float)this.random.nextGaussian() * f3);
                         entityitem.motionY = (double)((float)this.random.nextGaussian() * f3 + 0.2F);
                         entityitem.motionZ = (double)((float)this.random.nextGaussian() * f3);
 
                         if (itemstack.hasTagCompound())
                         {
                             entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                         }
                     }
                 }
             }
 
             par1World.func_96440_m(par2, par3, par4, par5);
         }
 
         super.breakBlock(par1World, par2, par3, par4, par5, par6);
     }
 	
 	/**
 	 * Returns a new instance of a block's tile entity class. Called on placing
 	 * the block.
 	 */
	@Override
 	public TileEntity createNewTileEntity(World par1World) {
 		return new TileEntityLockedChest(this.chestType, this);
 	}
 
 
 	/**
 	 * Looks for a sitting ocelot within certain bounds. Such an ocelot is
 	 * considered to be blocking access to the chest.
 	 */
 	@SuppressWarnings("rawtypes")
 	public static boolean isOcelotBlockingChest(World par0World, int par1, int par2, int par3) {
 		Iterator iterator = par0World.getEntitiesWithinAABB(
 				EntityOcelot.class,
 				AxisAlignedBB.getAABBPool().getAABB((double) par1, (double) (par2 + 1), (double) par3, (double) (par1 + 1),
 						(double) (par2 + 2), (double) (par3 + 1))).iterator();
 		EntityOcelot entityocelot;
 
 		do {
 			if (!iterator.hasNext()) {
 				return false;
 			}
 
 			EntityOcelot entityocelot1 = (EntityOcelot) iterator.next();
 			entityocelot = (EntityOcelot) entityocelot1;
 		} while (!entityocelot.isSitting());
 
 		return true;
 	}
 
 	@SideOnly(Side.CLIENT)
 	/**
 	 * When this method is called, your block should register all the icons it needs with the given IconRegister. This
 	 * is the only chance you get to register icons.
 	 */
	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.blockIcon = par1IconRegister.registerIcon("planks_oak");
 	}
 
 	public int getChestType() {
 		return chestType;
 	}
 
 	public void setChestType(int chestType) {
 		this.chestType = chestType;
 	}
 
 	@Override
 	public String getSystemName() {
 		return name;
 	}
 
 	
 
 }
