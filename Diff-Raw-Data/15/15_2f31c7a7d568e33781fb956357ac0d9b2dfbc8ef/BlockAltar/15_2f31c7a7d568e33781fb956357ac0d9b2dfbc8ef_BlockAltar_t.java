 package mods.nordwest.blocks;
 
 import java.util.List;
 import java.util.Random;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.nordwest.common.CustomBlocks;
 import mods.nordwest.common.NordWest;
 import mods.nordwest.tileentity.TileEntityAltar;
 import mods.nordwest.utils.EffectsLibrary;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class BlockAltar extends BaseBlock {
 	@SideOnly(Side.CLIENT)
 	private Icon[] iconArray;
 
 	public BlockAltar(int par1) {
 		super(par1, Material.rock);
         setHardness(2500.0F);
         setResistance(20.0F);
         setCreativeTab(CreativeTabs.tabDecorations);
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.iconArray = new Icon[3];
 
 		this.iconArray[0] = par1IconRegister.registerIcon("nordwest:" + this.getUnlocalizedName2() + ".side");
 		this.iconArray[1] = par1IconRegister.registerIcon("nordwest:" + this.getUnlocalizedName2() + ".top");
 		this.iconArray[2] = par1IconRegister.registerIcon("nordwest:" + this.getUnlocalizedName2() + ".buttom");
 	}
 	
 	   /*public float hardness(EntityPlayer player, Block block, World world, int x, int y, int z)
 	   {
 		   	any tile = block(x, y, z);
 	   		if ((tile != null) && ((tile instanceof TileOwned)) && (.equals(((TileOwned)tile).owner))) return 0.02F;
 	   
 	   			return 1.0E-004F;
 	   }*/
 	
 	@Override
 	public void onBlockAdded(World world, int x, int y, int z) {
 		Random random = new Random();
 		if (world.provider.dimensionId == 1) {
 			int dropChance = random.nextInt(10);
 			world.createExplosion((Entity)null, x, y, z, 2.0F, false);
 			if (dropChance <= 7) {
 			world.destroyBlock(x, y, z, true);
 			} else {
 			world.destroyBlock(x, y, z, false);
 			}
 		} else {
 			boolean onBlockActivated;
 		}
 	}
 	
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int a, float par1, float par2, float par3){
 		ItemStack itemstack = player.inventory.getCurrentItem();
 		if (itemstack != null){
			if (itemstack.itemID == Item.flintAndSteel.itemID){
                 /*TileEntity tileEntity = world.getBlockTileEntity(x, y, z);*/
                 if (/*tileEntity == null || */player.isSneaking()) {
                 return false;
                 }
 				EffectsLibrary.smokeCloud(player, x, y, z, 48);
 				EffectsLibrary.playSoundOnEntity(player, "fire.ignite");
 				itemstack.damageItem(3, player);
 				player.openGui(NordWest.instance, 0, world, 0, 0, 0);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public Icon getIcon(int par1, int par2) {
 		if (par1 == 0)
 			return this.iconArray[2];
 		if (par1 == 1)
 			return this.iconArray[1];
 		return this.iconArray[0];
 	}
 	
 	@Override
 	public void randomDisplayTick(World par1World, int x, int y, int z, Random random) {
 		super.randomDisplayTick(par1World, x, y, z, random);
 		if (testBlock(par1World, x, y, z)) {
 			for (int i = x - 2; i < x + 3; i++) {
 				for (int j = y; j < y + 4; j++) {
 					for (int k = z - 2; k < z + 3; k++) {
 						if (random.nextInt(16) == 0) {
 							par1World.spawnParticle("portal", i + random.nextDouble(), j + random.nextDouble(), k
 									+ random.nextDouble(), 0, 0, 0);
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	private boolean testBlock(World world, int x, int y, int z) {
 		int id = world.getBlockId(x, y, z);
 		if (id != CustomBlocks.blockhome.blockID) {
 			return false;
 		} else {
 			boolean test = true;
 			for (int i = 0; i < 4; i++) {
 				test &= world.getBlockId(x + 2, y + i, z + 2) == Block.obsidian.blockID;
 				test &= world.getBlockId(x - 2, y + i, z + 2) == Block.obsidian.blockID;
 				test &= world.getBlockId(x + 2, y + i, z - 2) == Block.obsidian.blockID;
 				test &= world.getBlockId(x - 2, y + i, z - 2) == Block.obsidian.blockID;
 			}
 			return test;
 		}
 	}
 	
     /*@Override
     public TileEntity createTileEntity(World world, int meta) {
            return new TileEntityAltar();
     }
     
     @Override
     public boolean hasTileEntity(int metadata) {
         return true;
     }*/
 }
