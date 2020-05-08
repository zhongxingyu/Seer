 package steamcraft.blocks;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.world.World;
 import steamcraft.HandlerRegistry;
 
 public class BlockTeslaCoil extends BlockRedstoneAccess {
 	private boolean torchActive;
 
 	public BlockTeslaCoil(boolean flag) {
 		super(flag);
 		torchActive = flag;
 	}
 
 	@Override
	public void breakBlock(World world, int i, int j, int k, Block par5, int par6) {
 		int l2 = world.getBlockMetadata(i, j, k);
 		int f = 0;
 		int f1 = 0;
 		int f2 = 0;
 		if (l2 == 3) {
 			f = 0;
 			f1 = 1;
 			f2 = 0;
 		} else if (l2 == 2) {
 			f = -1;
 			f1 = 0;
 			f2 = 0;
 		} else if (l2 == 1) {
 			f = 1;
 			f1 = 0;
 			f2 = 0;
 		} else if (l2 == 4) {
 			f = 0;
 			f1 = -1;
 			f2 = 0;
 		} else {
 			f = 0;
 			f1 = 0;
 			f2 = 1;
 		}
 		int nn = 0;
 		while (nn <= 8) {
 			nn = nn + 1;
 			int t1 = (i + f * nn);
 			int t2 = (k + f1 * nn);
 			int t3 = (j + f2 * nn);
 			if ((world.getBlock(t1, t3, t2) == BlockTeslaReceiver.getIdle() || world.getBlock(t1, t3, t2) == BlockTeslaReceiver.getActive()) && nn >= 1) {
 				world.setBlockMetadataWithNotify(t1, t3, t2, 1, 2);
 				world.notifyBlocksOfNeighborChange(t1, t3, t2, this);
 			}
 			if ((world.getBlock(t1, t3, t2) == getWirelessLampIdle() || world.getBlock(t1, t3, t2) == getWirelessLampActive()) && nn >= 1) {
 				world.notifyBlocksOfNeighborChange(t1, t3, t2, this);
 			}
 		}
 		if (torchActive) {
             world.notifyBlocksOfNeighborChange(i, j, k, this);
 		}
 	}
 
 	@Override
 	public boolean canProvidePower() {
 		return false;
 	}
 
 	@Override
 	public Item getItemDropped(int i, Random random, int j) {
 		return Item.getItemFromBlock(getTeslaIdle());
 	}
 
 	@Override
 	public void onBlockAdded(World world, int i, int j, int k) {
 		super.onBlockAdded(world, i, j, k);
 		world.notifyBlocksOfNeighborChange(i, j, k, this);
 		world.scheduleBlockUpdate(i, j, k, this, tickRate(world));
 	}
 
 	@Override
 	public void onNeighborBlockChange(World world, int i, int j, int k, Block l) {
 		super.onNeighborBlockChange(world, i, j, k, l);
 		world.scheduleBlockUpdate(i, j, k, this, tickRate(world));
 	}
 
 	@Override
 	public int tickRate(World world) {
 		return 1;
 	}
 
 	@Override
 	public void updateTick(World world, int i, int j, int k, Random random) {
 		boolean flag = this.hasIndirectPower(world, i, j, k);
         updateToggleTimes(world);
 		byte nnum = 1;
 		if (torchActive) {
 			nnum = 2;
 		} else {
 			nnum = 1;
 		}
 		int l2 = world.getBlockMetadata(i, j, k);
 		int f = 0;
 		int f1 = 0;
 		int f2 = 0;
 		if (l2 == 3) {
 			f = 0;
 			f1 = 1;
 			f2 = 0;
 		} else if (l2 == 2) {
 			f = -1;
 			f1 = 0;
 			f2 = 0;
 		} else if (l2 == 1) {
 			f = 1;
 			f1 = 0;
 			f2 = 0;
 		} else if (l2 == 4) {
 			f = 0;
 			f1 = -1;
 			f2 = 0;
 		} else {
 			f = 0;
 			f1 = 0;
 			f2 = 1;
 		}
 		int nn = 0;
 		while (nn <= 8) {
 			nn = nn + 1;
 			int t1 = (i + f * nn);
 			int t2 = (k + f1 * nn);
 			int t3 = (j + f2 * nn);
 			if ((world.getBlock(t1, t3, t2) == BlockTeslaReceiver.getIdle() || world.getBlock(t1, t3, t2) == BlockTeslaReceiver.getActive()) && nn >= 1) {
 				world.setBlockMetadataWithNotify(t1, t3, t2, nnum, 2);
 				world.notifyBlocksOfNeighborChange(t1, t3, t2, this);
 			}
 			if ((world.getBlock(t1, t3, t2) == getWirelessLampIdle() || world.getBlock(t1, t3, t2) == getWirelessLampActive()) && nn >= 1) {
 				world.notifyBlocksOfNeighborChange(t1, t3, t2, this);
 			}
 		}
 		if (!torchActive) {
 			if (flag) {
 				world.setBlock(i, j, k, getTeslaActive(), world.getBlockMetadata(i, j, k), 2);
 			}
 		} else if (!flag && !checkBurnout(world, i, j, k, false)) {
 			world.setBlock(i, j, k, getTeslaIdle(), world.getBlockMetadata(i, j, k), 2);
 		}
 		world.scheduleBlockUpdate(i, j, k, this, tickRate(world));
 	}
 
 	public static Block getTeslaActive() {
 		return HandlerRegistry.getBlock("steamcraft:teslaCoilOn").get();
 	}
 
 	public static Block getTeslaIdle() {
 		return HandlerRegistry.getBlock("steamcraft:teslaCoil").get();
 	}
 
 	public static Block getWirelessLampActive() {
 		return HandlerRegistry.getBlock("steamcraft:wirelessLampOn").get();
 	}
 
 	public static Block getWirelessLampIdle() {
 		return HandlerRegistry.getBlock("steamcraft:wirelessLamp").get();
 	}
 }
