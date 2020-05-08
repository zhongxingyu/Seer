 package TombStone;
 
 import java.util.Random;
 import java.util.logging.Level;
 
 import cpw.mods.fml.common.FMLLog;
 
 import TombStone.client.ClientProxy;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.stats.StatList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.Explosion;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class TombStoneBlock extends BlockContainer {
 	Icon[] icons;
 	private int quantityDropped = 0;
 	private boolean explosionRecover = false;
 	private TombStoneTileEntity tempEntity;
 
 	public TombStoneBlock (int id) {
 		super(id, Material.rock);
 		setHardness(2.0F);
 		setResistance(5000.0F);	//Set well above an normal material so that it's immune to explosions
 		setStepSound(Block.soundStoneFootstep);
 		setUnlocalizedName("Tombstone Block");
 		setCreativeTab(CreativeTabs.tabBlock);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int metadata)
 	{
 		//We use textures 0-5 for this block
 		return icons[side];
 	}
 	
 
 	
 	@Override
 	public void registerIcons(net.minecraft.client.renderer.texture.IconRegister iconRegister){
 		icons = new Icon[6];
 		for(int i = 0; i < icons.length; i++){
 			iconRegister.registerIcon("TombStone:block"+i); // assumed to be .png and in [src]/mods/[mod ID]/blocks/
 		}
 		
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int idk, float what, float these, float are) {
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if (tileEntity == null ) { // isSneaking check no longer valid (now part of default Minecraft behavior
 			return false;
 		}
 		
 		player.openGui(TombStone.instance, 0, world, x, y, z);
 		return true;
 	}
 	
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
 		dropItems(world, x, y, z);
 		
 		TombStoneTileEntity tileEntity = (TombStoneTileEntity) world.getBlockTileEntity(x, y, z);
 		if(tileEntity.isCrafted())
 		{
 			//TODO - Do something
 			quantityDropped = 1;
 		}
 			
 		super.breakBlock(world, x, y, z, par5, par6);
 		
 		for(int i=0; i<TombStone.instance.tombList.size(); i++)
 		{
 			TombStoneTileEntity item = TombStone.instance.tombList.get(i);
 			if(item.xCoord == x && item.yCoord == y && item.zCoord == z)
 			{
 				TombStone.instance.tombList.remove(i);
 				break;
 			}
 		}
 	}
 	
 	@Override
 	public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6)
 	{
 		super.harvestBlock(par1World, par2EntityPlayer, par3, par4, par5, par6);
 
 		//Reset the drop quantity		
 		if(quantityDropped == 1)
 			quantityDropped = 0;
 	}
 
 	@Override
     public int quantityDropped(Random par1Random)
     {
         return quantityDropped;
     }
 	
 	private void dropItems(World world, int x, int y, int z){
 		if(explosionRecover)
 			return;
 		
 		Random rand = new Random();
 		
 		//Capture the TombStoneTileEntity based on the position of the block
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if (!(tileEntity instanceof IInventory)) {
 			return;
 		}
 		
 		//And get the inventory space of the tile entity
 		IInventory inventory = (IInventory) tileEntity;
 		
 		//Loop through all slots in the tombstone
 		for (int i = 0; i < inventory.getSizeInventory(); i++) {
 			ItemStack item = inventory.getStackInSlot(i);
 			
 			//Only do something if the slot is valid
 			if (item != null && item.stackSize > 0) {
 				//Calculate a random velocity for the item
 				float rx = rand.nextFloat() * 0.8F + 0.1F;
 				float ry = rand.nextFloat() * 0.8F + 0.1F;
 				float rz = rand.nextFloat() * 0.8F + 0.1F;
 				
 				//Create the item
 				EntityItem entityItem = new EntityItem(world,
 				                x + rx, y + ry, z + rz,
 				                new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
 				
 				//Copy any NBT tags associated with the item
 				if (item.hasTagCompound()) {
 					// func_92014_d() appears to be getEntityItem()
 					entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
 				}
 				
 				//Assign the velocity
 				float factor = 0.05F;
 				entityItem.motionX = rand.nextGaussian() * factor;
 				entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
 				entityItem.motionZ = rand.nextGaussian() * factor;
 				
 				//Spawn the item
 				world.spawnEntityInWorld(entityItem);
 				
 				//Set the local slot to empty
 				item.stackSize = 0;
 			}
 		}
 	}
 	@Override
 	public TileEntity createTileEntity(World world, int metadata) {
 		return new TombStoneTileEntity();
 	}
 	
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TombStoneTileEntity();
 	}
 	
     public boolean renderAsNormalBlock()
     {
         return false;
     }
     
     public int getRenderType()
     {
         return -1;
     }
     
     public boolean isOpaqueCube()
     {
         return false;
     }
 
     public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
     {
         return false;
     }
     
     public boolean canDropFromExplosion(Explosion par1Explosion)
     {
 		//DEBUG//
 		FMLLog.log(Level.WARNING, "[TombStone] TombStoneBlock.canDropFromExplosion(): " + par1Explosion.exploder.getEntityName());
 		
 		explosionRecover = true;
 
         return true;
     }
     
     public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
     {
     	super.dropBlockAsItemWithChance(par1World, par2, par3, par4, par5, par6, par7);
     	
     	//If we're trying to recover from an explosion, backup the tile entity
     	if(explosionRecover)
     		tempEntity = (TombStoneTileEntity) par1World.getBlockTileEntity(par2, par3, par4);    	
     }
     
    public void onBlockDestroyedByExplosion(World par1World, int par2, int par3, int par4, Explosion expl)
     {
 		//DEBUG//
 		FMLLog.log(Level.WARNING, "[TombStone] TombStoneBlock.onBlockDestroyedByExplosion(): " + par2 + "," + par3 + "," + par4);
    	
 		//We get here because the tombstone is placed between doExplosionA() and doExplosionB() because that's when the player dies
 		//This creates a problem because doExplosionA() is the one that checks the explosion resistance for blocks ...
 		//Ergo the location where the tombstone is about to exist has already been marked for demolition
 		
 		//If destroyed by explosion place it right back
 		par1World.setBlock(par2, par3, par4, TombStone.instance.tombStoneBlockId, 0, 1 | 2);
 		TombStoneTileEntity blockTileEntity = (TombStoneTileEntity) par1World.getBlockTileEntity(par2, par3, par4);
 		blockTileEntity.setOwner(tempEntity.getOwner());
 		blockTileEntity.setDeathText(tempEntity.getDeathText());
 		blockTileEntity.setIsCrafted(tempEntity.isCrafted());
 		for(int i=0; i<tempEntity.getSizeInventory(); i++)
 		{
 			ItemStack playerItem = tempEntity.getStackInSlot(i);
 			blockTileEntity.setInventorySlotContents(i, playerItem);
 		}
 
 		tempEntity = null;
 		explosionRecover = false;
     }
 }
