 package com.dmillerw.brainFuckBlocks.tileentity;
 
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
 
 public class TileEntityBrainFuckCode extends TileEntity implements IRotatable, ISyncedTile, IConnection, IBrainfuckSymbol {
 
 	private ForgeDirection rotation;
 	private ForgeDirection outputSide;
 	
 	private int type;
 	
 	private boolean active = false;
 	
 	public TileEntityBrainFuckCode(int type) {
 		this.type = type;
 	}
 	
 	@Override
 	public ForgeDirection getRotation() {
 		if (rotation == null) {
 			return ForgeDirection.NORTH;
 		}
 		
 		return rotation;
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
 		nbt.setInteger("type", type);
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		setRotation(ForgeDirection.getOrientation(nbt.getByte("rotation")));
 		type = nbt.getInteger("type");
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
 
 	public int isActive() {
 		if (active) {
 			return 0;
 		}
 		
 		return 1;
 	}
 
 	@Override
 	public int interpret(char[] array, int pointerPos) {
 		return 0;
 	}
 
 	public String getSymbol() {
 		return "Type: "+type;
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
