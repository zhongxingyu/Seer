 package voidrunner101.SoulCraft.common.blocks;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 import voidrunner101.SoulCraft.common.mod_SoulCraft;
 import voidrunner101.SoulCraft.common.core.DefaultProps;
 import voidrunner101.SoulCraft.common.proxy.CommonProxy;
 import voidrunner101.SoulCraft.common.tileentity.TileCompressor;
 
 public class Compressor extends SCBlock {
 
 	TileEntity tile;
 	
 	public Compressor(int ID, Material material) {
 		super(ID, 0, material);
 		setBlockName("compressor");
 	}
 	
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	public int getRenderType() {
 		return DefaultProps.CompressorRenderID;
 	}
 	
 	@Override
     public void addCollidingBlockToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity) {
     	float var8 = 0.0625F;
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, var8, 1.0F);
         super.addCollidingBlockToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(0.0F, 0.0F, 0.0F, var8, 1.0F, 1.0F);
         super.addCollidingBlockToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, var8);
         super.addCollidingBlockToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(1.0F - var8, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         super.addCollidingBlockToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(0.0F, 0.0F, 1.0F - var8, 1.0F, 1.0F, 1.0F);
         super.addCollidingBlockToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBoundsForItemRender();
     }
     
     public void setBlockBoundsForItemRender() {
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
     }
 	
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
 		switch(side) {
 		case 0: return 6;
 		case 1: return 6;
 		default: return 5;
 		}
 	}
 	
//	@Override
//	public void onBlockAdded(World world, int x, int y, int z) {
//		tile.reset();
//	}
	
 	@Override
 	public boolean isCollidable()
 	{
 	        return true;
 	}
 	
 	@Override
 	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
 		TileCompressor tile = (TileCompressor)(world.getBlockTileEntity(x, y, z));
 		if(tile != null && entity != null && entity instanceof EntityItem){
 			if(tile.insertItemInSlot(((EntityItem) entity).getEntityItem(), 0)) {
 				CommonProxy.proxy.removeEntity(entity);
 			}
 		} else if(entity instanceof EntityLiving) {
 			entity.attackEntityFrom(mod_SoulCraft.damageCompressor, 3);
 		}
 	}
 	
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
 		TileCompressor tile = (TileCompressor)(world.getBlockTileEntity(x, y, z));
 		if(tile == null || CommonProxy.proxy.isRenderWorld(world)) {return true;}
 		ItemStack cont = tile.getStackInSlot(0);
 		if(cont != null) {
 			player.addChatMessage("This Compressor contains " + cont.stackSize + " " + cont.getItem().getItemDisplayName(cont));
 		} else {
 			player.addChatMessage("This Compressor does not contain anything.");
 		}
 		return true;
     }
 	
 	@Override
 	public TileEntity createTileEntity(World world, int metadata) {
 		return new TileCompressor();
 	}
 	
 	@Override
     public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
             dropItems(world, x, y, z);
             super.breakBlock(world, x, y, z, par5, par6);
     }
 
     private void dropItems(World world, int x, int y, int z){
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
 
                             EntityItem entityItem = new EntityItem(world,
                                             x + rx, y + ry, z + rz,
                                             new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
 
                             if (item.hasTagCompound()) {
                                     entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
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
 }
