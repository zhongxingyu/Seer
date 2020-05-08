 package com.birdsnesttech.travelingchest.blocks;
 
 import java.util.Random;
 
 import com.birdsnesttech.travelingchest.TravelingChest;
 import com.birdsnesttech.travelingchest.lib.Config;
 import com.birdsnesttech.travelingchest.tileentity.TETravelingChest;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockChest;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class BlockTravelingChest extends BlockChest {
 	
 	private static final Random random = new Random();
 
 	protected BlockTravelingChest(int par1, int par2) {
 		super(par1, par2);
 	}
 	
 	@Override
 	public TileEntity createNewTileEntity(World world)
 	{
 		return new TETravelingChest();
 	}
 	
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
 	{
 		if (par1World.isRemote) {
 			return true;
 		} else {
 			IInventory iinventory = this.getInventory(par1World, par2, par3, par4);
 
 			if (iinventory != null) {
 				par5EntityPlayer.openGui(TravelingChest.instance, 0, par1World, par2, par3, par4);
 			}
 
 			return true;
 		}
 	}
 
 	/**
 	 * Called when the block is placed in the world.
 	 */
 	@Override
 	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack)
 	{
 		byte b0 = 0;
 		int l1 = MathHelper.floor_double(par5EntityLivingBase.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
 
 		if (l1 == 0) {
 			b0 = 2;
 		}
 
 		if (l1 == 1) {
 			b0 = 5;
 		}
 
 		if (l1 == 2) {
 			b0 = 3;
 		}
 
 		if (l1 == 3) {
 			b0 = 4;
 		}
 
 		par1World.setBlockMetadataWithNotify(par2, par3, par4, b0, 3);
 		
 		if (par6ItemStack.hasDisplayName()) {
 			((TETravelingChest)par1World.getBlockTileEntity(par2, par3, par4)).setChestGuiName(par6ItemStack.getDisplayName());
 		}
 		
 		NBTTagCompound nbt = par6ItemStack.getTagCompound();
 		if (nbt != null) {
 			NBTTagList nbttaglist = nbt.getTagList("Items");
 			TETravelingChest te = (TETravelingChest)par1World.getBlockTileEntity(par2, par3, par4);
 			te.contents = new ItemStack[te.getSizeInventory()];
 			
 			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
 				NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
 				int j = nbttagcompound1.getByte("Slot") & 255;
 				if (j >= 0 && j < te.contents.length) {
 					te.contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void breakBlock(World world, int i, int j, int k, int par5, int par6)
 	{
 		TETravelingChest te = (TETravelingChest)world.getBlockTileEntity(i, j, k);
 
 		if (te != null) {
 			
 			int doBreak = random.nextInt(1000);
 			if( doBreak <= Config.breakChance ) {		
 			for (int j1 = 0; j1 < te.getSizeInventory(); ++j1) {
                 ItemStack itemstack = te.getStackInSlot(j1);
 
                 if (itemstack != null) {
                     float f = random.nextFloat() * 0.8F + 0.1F;
                     float f1 = random.nextFloat() * 0.8F + 0.1F;
                     EntityItem entityitem;
 
                     for (float f2 = random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world.spawnEntityInWorld(entityitem)) {
                         int k1 = random.nextInt(21) + 10;
 
                         if (k1 > itemstack.stackSize) {
                             k1 = itemstack.stackSize;
                         }
 
                         itemstack.stackSize -= k1;
                         entityitem = new EntityItem(world, (double)((float)i + f), (double)((float)j + f1), (double)((float)k + f2), new ItemStack(itemstack.itemID, k1, itemstack.getItemDamage()));
                         float f3 = 0.05F;
                         entityitem.motionX = (double)((float)random.nextGaussian() * f3);
                         entityitem.motionY = (double)((float)random.nextGaussian() * f3 + 0.2F);
                         entityitem.motionZ = (double)((float)random.nextGaussian() * f3);
 
                         if (itemstack.hasTagCompound()) {
                             entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                         }
                     }
                 }
 			}
 			
 			} else {
                 
 			ItemStack is = new ItemStack(Block.blocksList[par5], 1);
 			NBTTagCompound nbt = new NBTTagCompound();
 			NBTTagList nbttaglist = new NBTTagList();
 
 	        for (int s = 0; s < te.contents.length; ++s) {
 	            if (te.contents[s] != null) {
 	                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
 	                nbttagcompound1.setByte("Slot", (byte)s);
 	                te.contents[s].writeToNBT(nbttagcompound1);
 	                nbttaglist.appendTag(nbttagcompound1);
 	            }
 	        }
 
        		nbt.setTag("Items", nbttaglist);
 			is.setTagCompound(nbt);
			EntityItem ei = new EntityItem(world,i+0.5F,j+0.5F,k+0.5F,is);
 			world.spawnEntityInWorld(ei);
 
 			for(int s = 0; s < te.getSizeInventory(); ++s) {
 				te.setInventorySlotContents(s, null);
 			}
 		}
 			world.removeBlockTileEntity(i, j, k);
 		}
 	}
 
 	@Override
 	public void dropBlockAsItemWithChance(World par1World, int par2, int par3,
 			int par4, int par5, float par6, int par7) {
 		super.dropBlockAsItemWithChance(par1World, par2, par3, par4, par5, 0.0F, par7);
 	}
 
 	@Override
 	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
 		return true;
 	}
 
 	@Override
 	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
 		//super.onBlockAdded(par1World, par2, par3, par4);
 	}
 
 	@Override
 	public int getRenderType() {
 		// TODO Auto-generated method stub
 		return ModBlocks.travelChestRenderID;
 	}
 }
