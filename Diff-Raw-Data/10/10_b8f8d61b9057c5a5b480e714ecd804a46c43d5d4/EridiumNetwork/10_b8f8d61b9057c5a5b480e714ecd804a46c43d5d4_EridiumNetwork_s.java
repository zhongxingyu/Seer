 package com.isocraft.api.eridiumnet;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import com.isocraft.block.ModBlocks;
 
 public class EridiumNetwork {
 
 	public static int range = 17;
 
 	public EridiumNetwork() {
 
 	}
 
 	public void tickNetwork() {
 
 	}
 
	public static IEridiumNetMajorNode traceConnection(World world, IEridiumNetConnectionNode tile, int x, int y, int z, ForgeDirection dir) {	
 		for (int i = 2; i <= range; ++i) {
 			if (dir == ForgeDirection.NORTH) {				
 				if (world.getTileEntity(x, y, z - i) instanceof IEridiumNetMajorNode && world.getTileEntity(x, y, z - i) instanceof ISidedConnectable && SidedConnectableHelper.canConnect(dir, ((ISidedConnectable) world.getTileEntity(x, y, z - i))) && !(world.getBlock(x, y, z - i).equals(ModBlocks.EridiumNetHub))) {
 					tile.setConnectionsEntityLength(i);
 					return (IEridiumNetMajorNode) world.getTileEntity(x, y, z - i);
 				}
 				else if (world.getBlock(x, y, z - i).getMaterial() != Material.glass && !world.isAirBlock(x, y, z - i)){
 					tile.setOverrideConnectionEntityLength(i + 1);
 					tile.setConnectionsEntityLength(0);
 					return null;
 				}
 			}
 			else if (dir == ForgeDirection.SOUTH) {
 				if (world.getTileEntity(x, y, z + i) instanceof IEridiumNetMajorNode && world.getTileEntity(x, y, z + i) instanceof ISidedConnectable && SidedConnectableHelper.canConnect(dir, ((ISidedConnectable) world.getTileEntity(x, y, z + i))) && !(world.getBlock(x, y, z + i).equals(ModBlocks.EridiumNetHub))) {
 					tile.setConnectionsEntityLength(i);
 					return (IEridiumNetMajorNode) world.getTileEntity(x, y, z + i);
 				}
 				else if (world.getBlock(x, y, z + i).getMaterial() != Material.glass && !world.isAirBlock(x, y, z + i)){
 					tile.setOverrideConnectionEntityLength(i + 1);
 					tile.setConnectionsEntityLength(0);
 					return null;
 				}
 			}
 			else if (dir == ForgeDirection.EAST) {
 				if (world.getTileEntity(x + i, y, z) instanceof IEridiumNetMajorNode && world.getTileEntity(x + i, y, z) instanceof ISidedConnectable && SidedConnectableHelper.canConnect(dir, ((ISidedConnectable) world.getTileEntity(x + i, y, z))) && !(world.getBlock(x + i, y, z).equals(ModBlocks.EridiumNetHub))) {
 					tile.setConnectionsEntityLength(i);
 					return (IEridiumNetMajorNode) world.getTileEntity(x + i, y, z);
 				}
 				else if (world.getBlock(x + i, y, z).getMaterial() != Material.glass && !world.isAirBlock(x + i, y, z)){
 					tile.setOverrideConnectionEntityLength(i + 1);
 					tile.setConnectionsEntityLength(0);
 					return null;
 				}
 			}
 			else if (dir == ForgeDirection.WEST) {
 				if (world.getTileEntity(x - i, y, z) instanceof IEridiumNetMajorNode && world.getTileEntity(x - i, y, z) instanceof ISidedConnectable && SidedConnectableHelper.canConnect(dir, ((ISidedConnectable) world.getTileEntity(x - i, y, z))) && !(world.getBlock(x - i, y, z).equals(ModBlocks.EridiumNetHub))) {
 					tile.setConnectionsEntityLength(i);
 					return (IEridiumNetMajorNode) world.getTileEntity(x - i, y, z);
 				}
 				else if (world.getBlock(x - i, y, z).getMaterial() != Material.glass && !world.isAirBlock(x - i, y, z)){
 					tile.setOverrideConnectionEntityLength(i + 1);
 					tile.setConnectionsEntityLength(0);
 					return null;
 				}
 			}
 			else if (dir == ForgeDirection.UP) {
 				if (world.getTileEntity(x, y + i, z) instanceof IEridiumNetMajorNode && world.getTileEntity(x, y + i, z) instanceof ISidedConnectable && SidedConnectableHelper.canConnect(dir, ((ISidedConnectable) world.getTileEntity(x, y + i, z))) && !(world.getBlock(x, y + i, z).equals(ModBlocks.EridiumNetHub))) {
 					tile.setConnectionsEntityLength(i);
 					return (IEridiumNetMajorNode) world.getTileEntity(x, y + i, z);
 				}
 				else if (world.getBlock(x, y + i, z).getMaterial() != Material.glass && !world.isAirBlock(x, y + i, z)){
 					tile.setOverrideConnectionEntityLength(i + 1);
 					tile.setConnectionsEntityLength(0);
 					return null;
 				}
 			}
 			else if (dir == ForgeDirection.DOWN) {
 				if (world.getTileEntity(x, y - i, z) instanceof IEridiumNetMajorNode && world.getTileEntity(x, y - i, z) instanceof ISidedConnectable && SidedConnectableHelper.canConnect(dir, ((ISidedConnectable) world.getTileEntity(x, y - i, z))) && !(world.getBlock(x, y - i, z).equals(ModBlocks.EridiumNetHub))) {
 					tile.setConnectionsEntityLength(i);
 					return (IEridiumNetMajorNode) world.getTileEntity(x, y - i, z);
 				}
 				else if (world.getBlock(x, y - i, z).getMaterial() != Material.glass && !world.isAirBlock(x, y - i, z)){
 					tile.setOverrideConnectionEntityLength(i + 1);
 					tile.setConnectionsEntityLength(0);
 					return null;
 				}
 			}
 			else{
 				return null;
 			}
 		}
 		tile.setConnectionsEntityLength(0);
 		return null;
 	}
 
 	public void saveNetwork(NBTTagCompound nbt) {
 
 	}
 
 	public EridiumNetwork readNetwork(NBTTagCompound nbt) {
 		return new EridiumNetwork();
 	}
 }
