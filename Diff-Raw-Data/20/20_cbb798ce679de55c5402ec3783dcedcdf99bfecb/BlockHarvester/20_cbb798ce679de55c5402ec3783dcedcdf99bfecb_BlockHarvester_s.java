 package lightProcessing.block;
 
 import java.util.List;
 import java.util.Random;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import lib.BlockTab;
 import lib.Reference;
 import lightProcessing.LightProcessing;
 import lightProcessing.tile.TileEntityAbsComp;
 import lightProcessing.tile.TileEntityHarvester;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockHarvester extends BlockContainer{
 
 	public BlockHarvester (int par1, Material material){
 		super(par1, Material.rock);
 		this.setUnlocalizedName("Harvester");
 		this.setHardness(10);
 		this.setCreativeTab(BlockTab.blockTab);
 		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 	}
 	
 	
 	public static void Essence(World par1World, int par2, int par3, int par4){
 		
 		TileEntityHarvester te = (TileEntityHarvester) par1World.getBlockTileEntity(par2, par3, par4);
   		 float Light = par1World.getLightBrightness(par2, par3 + 1, par4);
   		 te.Spawn(Light);
 	}
 	
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister)
 	{
 		this.blockIcon = par1IconRegister.registerIcon(Reference.MOD_ID + ":" + this.getUnlocalizedName2() + "Top");
 	}
 	
 	
 	 @Override
 	 public boolean hasTileEntity(int metadata){
 		 return true;
 	 }
 	 
 	   @Override
 	   public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
 	   {
 	   	 if(par5EntityPlayer.getCurrentItemOrArmor(0) == null){
 	   		 
 	   		 TileEntityHarvester te = (TileEntityHarvester) par1World.getBlockTileEntity(par2, par3, par4);
 	   		 float Light = par1World.getLightBrightness(par2, par3 + 1, par4);
 	   		 te.Spawn(Light);
 	   		 
 	   	 }
 	   	 return false;
 
 	   	 }
 	 
 	 @Override
 	 public boolean renderAsNormalBlock()
 		{
 			return false;
 		}
 		
 		@Override
 		public boolean isOpaqueCube()
 		{
 			return false;
 		}
 		
 		@Override
 		public int getRenderType()
 		{
 			return -1;
 		}
 	
 		@Override
 		public void randomDisplayTick(World world, int x, int y, int z, Random random)
 		   {
 		           super.randomDisplayTick(world, x, y, z, random);
 		           	float f1 = (float)x + 0.5F;
 					float f2 = (float)y + 0.5F;
 					float f3 = (float)z + 0.5F; 
 					float f4 = random.nextFloat() * 0.6F -0.3F;
 					float f5 = random.nextFloat() * -0.6F - -0.3F;
 		           world.spawnParticle("portal", (double)f1 +  f4, (double)f2, (double)f3 + f5, 0.0D, 0.0D, 0.0D);
 		           world.spawnParticle("portal", (double)f1 +  f4, (double)f2, (double)f3 + f5, 0.0D, 0.0D, 0.0D);
 		           world.spawnParticle("portal", (double)f1 +  f4, (double)f2, (double)f3 + f5, 0.0D, 0.0D, 0.0D);
 		           world.spawnParticle("portal", (double)f1 +  f4, (double)f2, (double)f3 + f5, 0.0D, 0.0D, 0.0D);
 		           world.spawnParticle("portal", (double)f1 +  f4, (double)f2, (double)f3 + f5, 0.0D, 0.0D, 0.0D);
 		           world.spawnParticle("portal", (double)f1 +  f4, (double)f2, (double)f3 + f5, 0.0D, 0.0D, 0.0D);
 		           world.spawnParticle("instantSpell", (double)f1 +  f4, (double)f2, (double)f3 + f5, 0.0D, 0.0D, 0.0D);
 			   
 		   }
 		
 		 public void addCollisionBoxesToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity)
 		    {
 			 	this.setBlockBounds(0.0F, 0.815F, 0.0F, 1.0F, 0.8715F, 1.0F);
 		        super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
 		        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.06F);
 		        super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
 		        this.setBlockBounds(0.94F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 		        super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
 		        this.setBlockBounds(0.0F, 0.0F, 0.94F, 1.0F, 1.0F, 1.0F);
 		        super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
 		        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.06F, 1.0F);
 		        super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
 		        this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.06F, 1.0F, 1.0F);
 		        super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
 		    }
 		
 		 @Override
 			public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
 		    {
 		        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 		    }
 		
 	public void onBlockAdded(World par1World, int x, int y, int z){
 		super.onBlockAdded(par1World, x, y, z);
 		par1World.markBlockForUpdate(x, y, z);
 	}
  
 
         @Override
         public TileEntity createNewTileEntity(World world) {
                 return new TileEntityHarvester();
         }
 
 }
