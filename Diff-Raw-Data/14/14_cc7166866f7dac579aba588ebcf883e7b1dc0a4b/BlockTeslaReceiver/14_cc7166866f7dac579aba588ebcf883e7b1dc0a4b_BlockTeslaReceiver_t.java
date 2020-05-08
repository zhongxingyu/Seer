 package steamcraft.blocks;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IIconRegister;
 import net.minecraft.item.Item;
 import net.minecraft.util.IIcon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 import steamcraft.HandlerRegistry;
 import steamcraft.Steamcraft;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockTeslaReceiver extends Block {
 	private IIcon sideBlock;
 
 	public BlockTeslaReceiver() {
 		super(Steamcraft.staticcircuit);
         setTickRandomly(true);
         setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
         setLightOpacity(0);
 	}
 
 	@Override
	public void breakBlock(World world, int i, int j, int k, Block par5, int par6) {
 		world.notifyBlocksOfNeighborChange(i, j, k, this);
		super.breakBlock(world, i, j, k, par5, par6);
 	}
 
 	@Override
 	public boolean canProvidePower() {
 		return true;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int l) {
 		if (l == 1 || (l == 0)) {
 			return blockIcon;
 		}
 		return sideBlock;
 	}
 
 	@Override
 	public Item getItemDropped(int i, Random random, int j) {
 		return Item.getItemFromBlock(getIdle());
 	}
 
 	@Override
 	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
 		return side != ForgeDirection.DOWN && side != ForgeDirection.UP;
 	}
 
 	@Override
 	public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int i, int j, int k, int l) {
 		if (par1IBlockAccess.getBlockMetadata(i, j, k) == 2 && l == 0)
 			return 15;
 		return 0;
 	}
 
 	@Override
 	public void onBlockAdded(World world, int i, int j, int k) {
 		for (int a = -9; a <= 9; a++) {
 			for (int b = -9; b <= 9; b++) {
 				for (int c = -9; c <= 9; c++) {
 					if (world.getBlock(i + a, j + b, k + c) == BlockTeslaCoil.getTeslaIdle() || world.getBlock(i + a, j + b, k + c) == BlockTeslaCoil.getTeslaActive()) {
 						world.notifyBlocksOfNeighborChange(i + a, j + b, k + c, this);
 						world.scheduleBlockUpdate(i + a, j + b, k + c, this, tickRate(world));
 					}
 				}
 			}
 		}
 		world.notifyBlocksOfNeighborChange(i, j, k, this);
 		world.scheduleBlockUpdate(i, j, k, this, tickRate(world));
 	}
 
 	@Override
 	public int quantityDropped(Random random) {
 		return 1;
 	}
 
 	@Override
     @SideOnly(Side.CLIENT)
 	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
 		int l = world.getBlockMetadata(i, j, k);
 		if (l == 1) {
 			return;
 		}
 		double d = i + 0.5F + (random.nextFloat() - 0.5F) * 0.20000000000000001D;
 		double d1 = j + 0.2F + (random.nextFloat() - 0.5F) * 0.20000000000000001D;
 		double d2 = k + 0.5F + (random.nextFloat() - 0.5F) * 0.20000000000000001D;
 		double d4 = 0.5D;
 		world.spawnParticle("reddust", d - d4, d1, d2, -1.0D, 0.7D, 1.0D);
 		world.spawnParticle("reddust", d + d4, d1, d2, -1.0D, 0.7D, 1.0D);
 		world.spawnParticle("reddust", d, d1, d2 - d4, -1.0D, 0.7D, 1.0D);
 		world.spawnParticle("reddust", d, d1, d2 + d4, -1.0D, 0.7D, 1.0D);
 		world.spawnParticle("reddust", d, d1 + 1.1D, d2, -1.0D, 0.7D, 1.0D);
 		world.spawnParticle("reddust", d, d1 - 0.2D, d2, -1.0D, 0.7D, 1.0D);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerBlockIcons(IIconRegister par1IconRegister) {
 		super.registerBlockIcons(par1IconRegister);
 		sideBlock = par1IconRegister.registerIcon("steamcraft:receiverside");
 	}
 
     @Override
     public boolean renderAsNormalBlock(){
         return false;
     }
 
     @Override
     public boolean isOpaqueCube(){
         return false;
     }
 
 	@Override
     @SideOnly(Side.CLIENT)
 	public boolean shouldSideBeRendered(IBlockAccess iblockaccess, int i, int j, int k, int l) {
 		if (l == 1) {
 			return true;
 		}
 		return super.shouldSideBeRendered(iblockaccess, i, j, k, l);
 	}
 
 	@Override
 	public int tickRate(World world) {
 		return 2;
 	}
 
 	@Override
 	public void updateTick(World world, int i, int j, int k, Random random) {
 		world.notifyBlocksOfNeighborChange(i, j, k, this);
 		world.markBlockForUpdate(i, j, k);
 		if (world.getBlockMetadata(i, j, k) == 2) {
 			world.setBlock(i, j, k, getActive(), 2, 2);
 		} else {
 			world.setBlock(i, j, k, getIdle(), 1, 2);
 		}
 		world.scheduleBlockUpdate(i, j, k, this, tickRate(world));
 	}
 
 	public static Block getActive() {
 		return HandlerRegistry.getBlock("steamcraft:receiverOn").get();
 	}
 
 	public static Block getIdle() {
 		return HandlerRegistry.getBlock("steamcraft:receiver").get();
 	}
 }
