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
 import com.dmillerw.brainfuckInterpreter.BrainfuckEngine;
 
 public class TileEntityCPU extends TileEntity implements IRotatable, ISyncedTile, IConnection {
 
 	private ForgeDirection rotation;
 	private ForgeDirection outputSide;
 	
 	private BrainfuckEngine engine;
 	
 	private List<String> instructionPositions;
 	
 	public TileEntityCPU() {
 		engine = new BrainfuckEngine(30000);
 	}
 	
 	@Override
 	public ForgeDirection getRotation() {
 		if (rotation == null) {
 			return ForgeDirection.NORTH;
 		}
 		
 		return rotation;
 	}
 
 	public void updateInstructions() {
 		if (this.worldObj.isRemote) {
 			return;
 		}
 		
 		instructionPositions = new ArrayList<String>();
 		
 		engine.clear();
 		
 		int currXOffset = xCoord + outputSide.offsetX;
 		int currYOffset = yCoord + outputSide.offsetY;
 		int currZOffset = zCoord + outputSide.offsetZ;
 		
 		boolean keepSearching = true;
 		
 		while (keepSearching) {
 			if (worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset) instanceof IConnection) {
 				IConnection connection = (IConnection) worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset);
 				
 				if (instructionPositions.contains(connection.getPosition().toString())) {
 					keepSearching = false;
 					break;
 				} else {
 					instructionPositions.add(connection.getPosition().toString());
 				}
 				
 				if (worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset) instanceof IBrainfuckSymbol) {
 					IBrainfuckSymbol symbol = (IBrainfuckSymbol) worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset);
					System.out.println(symbol.getSymbol());
 					engine.interpret(symbol.getSymbol());
 				}
 				
 				if (worldObj.getBlockTileEntity(currXOffset, currYOffset, currZOffset) instanceof TileEntityCPU) {
 					keepSearching = false;
 					break;
 				}
 				
 				currXOffset = currXOffset + connection.getOutput().offsetX;
 				currYOffset = currYOffset + connection.getOutput().offsetY;
 				currZOffset = currZOffset + connection.getOutput().offsetZ;
 			} else {
 				keepSearching = false;
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
