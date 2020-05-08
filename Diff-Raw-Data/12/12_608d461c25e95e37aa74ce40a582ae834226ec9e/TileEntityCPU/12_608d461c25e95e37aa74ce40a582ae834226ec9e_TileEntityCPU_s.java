 package com.dmillerw.brainFuckBlocks.tileentity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 import com.dmillerw.brainFuckBlocks.interfaces.IBrainfuckSymbol;
 import com.dmillerw.brainFuckBlocks.interfaces.IConnection;
 import com.dmillerw.brainFuckBlocks.interfaces.IRotatable;
 import com.dmillerw.brainFuckBlocks.interfaces.ISyncedTile;
 import com.dmillerw.brainFuckBlocks.util.Position;
 
 public class TileEntityCPU extends TileEntity implements IRotatable, ISyncedTile, IConnection {
 
 	private ForgeDirection rotation;
 	private ForgeDirection outputSide;
 	
 	private List<IBrainfuckSymbol> instructions = new ArrayList<IBrainfuckSymbol>();
	private List<Position> instructionPositions = new ArrayList<Position>();
 	
 	@Override
 	public ForgeDirection getRotation() {
 		if (rotation == null) {
 			return ForgeDirection.NORTH;
 		}
 		
 		return rotation;
 	}
 
 	//TODO Finish
 	public void updateInstructions() {
 		if (this.worldObj.isRemote) {
 			return;
 		}
 		
 		int currXOffset = xCoord + outputSide.offsetX;
 		int currYOffset = yCoord + outputSide.offsetY;
 		int currZOffset = zCoord + outputSide.offsetZ;
 		
 		boolean keepSearching = true;
 		int blocksFound = 0;
 		
 		while (keepSearching) {
 			if (worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset) instanceof IConnection) {
 				blocksFound++;
 				System.out.println("IConnection found @ "+currXOffset+":"+currYOffset+":"+currZOffset);
 				IConnection connection = (IConnection) worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset);
 				
				if (instructionPositions.contains(connection.getPosition())) {
 					keepSearching = false;
 					break;
 				} else {
					instructionPositions.add(connection.getPosition());
 				}
 				
 				if (worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset) instanceof IBrainfuckSymbol) {
 					IBrainfuckSymbol symbol = (IBrainfuckSymbol) worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset);
 					System.out.println(symbol.getSymbol());
 				}
 				
 				if (worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset) instanceof TileEntityCPU) {
 					keepSearching = false;
 					break;
 				}
 				
 				currXOffset = currXOffset + connection.getOutput().offsetX;
 				currYOffset = currYOffset + connection.getOutput().offsetY;
 				currZOffset = currZOffset + connection.getOutput().offsetZ;
 				System.out.println("Will look for next block @ "+currXOffset+":"+currYOffset+":"+currZOffset);
 			} else {
 				keepSearching = false;
 				System.out.println("Blocks found: "+blocksFound);
 			}
 		}
 	}
 	
 	@Override
 	public void setRotation(ForgeDirection rot) {
 		rotation = rot;
 		outputSide = rot.getRotation(ForgeDirection.UP);
 	}
 
 	@Override
 	public void rotate() {
 		setRotation(getRotation().getRotation(ForgeDirection.UP));
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		nbt.setByte("rotation", (byte) rotation.ordinal());
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		rotation = ForgeDirection.getOrientation(nbt.getByte("rotation"));
 	}
 	
 	public Packet getDescriptionPacket() {
 		NBTTagCompound nbt = new NBTTagCompound();
 		writeToNBT(nbt);
         return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
     }
 	
 	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
 		if (pkt.xPosition == this.xCoord && pkt.yPosition == this.yCoord && pkt.zPosition == this.zCoord) {
 			readFromNBT(pkt.customParam1);
 		}
     }
 	
 	@Override
 	public int[] getPayload() {
 		return null;
 	}
 
 	@Override
 	public void handlePayload(int[] payload) {
 		
 	}
 
 	@Override
 	public ForgeDirection getOutput() {
 		return outputSide;
 	}
 
 	@Override
 	public Position getPosition() {
 		return new Position(xCoord, yCoord, zCoord);
 	}
 	
 }
