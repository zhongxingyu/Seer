 package slimevoid.projectbench.core.lib;
 
 import slimevoidlib.util.SlimevoidHelper;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockLib {
 
 	private static final String BLOCK_PREFIX = "project.";
 	public static final String BLOCK_PROJECT_BENCH = BLOCK_PREFIX + "bench";
 	public static final String BLOCK_PROJECT_CHEST = BLOCK_PREFIX + "chest";
 	public static final int BLOCK_BASE_MAX = 1;
 	public static final int BLOCK_PROJECT_BENCH_ID = 0;
 	public static final int BLOCK_PROJECT_CHEST_ID = 1;
 	public static final String BLOCK_PROJECT_BASE = BLOCK_PREFIX + "base";
 
 	public static void notifyBlock(World world, int x, int y, int z, int blockID) {
 		Block block = Block.blocksList[world.getBlockId(x, y, z)];
 		if (block != null) {
 			block.onNeighborBlockChange(world, x, y, z, blockID);
 		}
 	}
 
 	public static void updateIndirectNeighbors(World world, int x, int y, int z, int blockID) {
 		if (world.isRemote || FMLCommonHandler.instance().getSide() == Side.CLIENT)
 			return;
 		for (int inDirX = -3; inDirX <= 3; inDirX++) {
 			for (int inDirY = -3; inDirY <= 3; inDirY++) {
 				for (int inDirZ = -3; inDirZ <= 3; inDirZ++) {
 					int updateDirection = inDirX >= 0 ? inDirX : -inDirX;
 					updateDirection += inDirY >= 0 ? inDirY : -inDirY;
 					updateDirection += inDirZ >= 0 ? inDirZ : -inDirZ;
 					if (updateDirection <= 3) {
 						notifyBlock(world, x + inDirX, y + inDirY, z + inDirZ, blockID);
 					}
 				}
 
 			}
 
 		}
 
 	}
 
 	public static void markBlockDirty(World world, int x, int y, int z) {
 		if (world.blockExists(x, y, z)) {
 			world.getChunkFromBlockCoords(x, z).setChunkModified();
 		}
 	}
 
 	public static void dropItem(World world, int x, int y, int z, ItemStack itemstack) {
 		if (world.isRemote) {
 			return;
 		} else {
 			double d = 0.69999999999999996D;
 			double xx = (double) world.rand.nextFloat() * d + (1.0D - d) * 0.5D;
 			double yy = (double) world.rand.nextFloat() * d + (1.0D - d) * 0.5D;
 			double zz = (double) world.rand.nextFloat() * d + (1.0D - d) * 0.5D;
 			EntityItem item = new EntityItem(
 					world,
 					(double) x + xx,
 					(double) y + yy,
 					(double) z + zz,
 					itemstack);
 			item.age = 10;
 			world.spawnEntityInWorld(item);
 			return;
 		}
 	}
 
 	public static Object getTileEntity(IBlockAccess world, int x, int y, int z, Class tileEntityClass) {
 		TileEntity tileentity = SlimevoidHelper.getBlockTileEntity(world, x, y, z);
 		if (!tileEntityClass.isInstance(tileentity)) {
 			return null;
 		} else {
 			return tileentity;
 		}
 	}
 
 	public static Icon[][] registerIcons(IconRegister iconRegister, Icon[][] iconList) {
 		iconList[BLOCK_PROJECT_BENCH_ID][0] = iconRegister.registerIcon(IconLib.PROJECT_BENCH_BOTTOM);
 		iconList[BLOCK_PROJECT_BENCH_ID][1] = iconRegister.registerIcon(IconLib.PROJECT_BENCH_TOP);
		iconList[BLOCK_PROJECT_BENCH_ID][2] = iconRegister.registerIcon(IconLib.PROJECT_BENCH_SIDE);
		iconList[BLOCK_PROJECT_BENCH_ID][3] = iconRegister.registerIcon(IconLib.PROJECT_BENCH_FRONT);
 		iconList[BLOCK_PROJECT_BENCH_ID][4] = iconRegister.registerIcon(IconLib.PROJECT_BENCH_SIDE);
 		iconList[BLOCK_PROJECT_BENCH_ID][5] = iconRegister.registerIcon(IconLib.PROJECT_BENCH_SIDE);
 		return iconList;
 	}
 }
