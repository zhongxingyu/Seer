 package com.qzx.au.core;
 
 import net.minecraft.nbt.NBTTagCompound;
 
 public class SidedBlockInfo {
 	private SidedSlotInfo[] sidedSlotInfo; // details of each slot range
 	private byte[] sidedSlots; // [6] current mapping of slots to sides (a value of -1 is a closed side)
 	private boolean[] sidedCanToggle; // [6] player can remap side
 	private boolean[] sidedIsVisible; // [6] display port on side of block
 
 	public SidedBlockInfo(SidedSlotInfo[] slotInfo, byte[] slots, boolean[] toggle, boolean[] visible){
 		this.sidedSlotInfo = slotInfo;
 		this.sidedSlots = slots;			if(slots.length != 6) Debug.error("SidedBlockInfo needs slots[6]");
 		this.sidedCanToggle = toggle;		if(toggle.length != 6) Debug.error("SidedBlockInfo needs toggle[6]");
 		this.sidedIsVisible = visible;		if(visible.length != 6) Debug.error("SidedBlockInfo needs visible[6]");
 	}
 
 	//////////
 
 	public SidedSlotInfo[] getAllSlotInfo(){
 		return this.sidedSlotInfo;
 	}
 
 	public SidedSlotInfo getSlotInfoFromSide(byte side){
 		if(this.sidedSlots[side] == -1) return null;
 		return this.sidedSlotInfo[this.sidedSlots[side]];
 	}
 
 	public SidedSlotInfo getSlotInfoFromIndex(byte slotIndex){
 		for(byte i = 0; i < 6; i++)
 			if(this.sidedSlots[i] > -1){
 				SidedSlotInfo info = this.sidedSlotInfo[i];
 				if(slotIndex >= info.getFirstIndex() && slotIndex <= info.getLastIndex())
 					return info;
 			}
 		return null;
 	}
 
 	//////////
 
 	public byte getSlot(byte side){
 		return this.sidedSlots[side];
 	}
 
 	public void setSlot(byte side, byte slot){
 		if(this.sidedCanToggle[side])
 			this.sidedSlots[side] = slot;
 		else
 			Debug.error("SidedBlockInfo can't setSlot for side "+side);
 	}
 
 	//////////
 
 	public int[] getAccessibleSlotsFromSide(byte side){
 		SidedSlotInfo info = this.getSlotInfoFromSide(side);
		if(info == null) return null;
 		byte first = info.getFirstIndex();
 		int[] slots = new int[1+info.getLastIndex()-first];
 		for(int i = 0; i < slots.length; i++) slots[i] = first+i;
 		return slots; // return slot indices accessible from side
 	}
 
 	public boolean canInsert(byte slotIndex, byte side){
 		SidedSlotInfo info = this.getSlotInfoFromSide(side);
 		if(info == null) return false;
 		return info.canInsert() && slotIndex >= info.getFirstIndex() && slotIndex <= info.getLastIndex();
 	}
 
 	public boolean canExtract(byte slotIndex, byte side){
 		SidedSlotInfo info = this.getSlotInfoFromSide(side);
 		if(info == null) return false;
 		return info.canExtract() && slotIndex >= info.getFirstIndex() && slotIndex <= info.getLastIndex();
 	}
 
 	//////////
 
 	public boolean canToggle(byte side){
 		return this.sidedCanToggle[side];
 	}
 
 	public void setSide(byte side, byte slot){
 		if(this.sidedCanToggle[side])
 			this.sidedSlots[side] = slot;
 	}
 
 	public byte toggleSide(byte side){
 		if(this.sidedCanToggle[side] && this.sidedSlotInfo.length > 1){
 			byte slot = (byte)(this.sidedSlots[side] + 1);
 			this.sidedSlots[side] = (slot >= this.sidedSlotInfo.length ? -1 : slot);
 		}
 		return this.sidedSlots[side];
 	}
 
 	//////////
 
 	public boolean isVisible(byte side){
 		return this.sidedIsVisible[side];
 	}
 
 	//////////
 
 	public void readFromNBT(NBTTagCompound nbt){
 		for(byte side = 0; side < 6; side++)
 			if(this.sidedCanToggle[side])
 				this.sidedSlots[side] = (byte)(nbt.getByte("sidedSlot"+side) - 1);
 	}
 
 	public void writeToNBT(NBTTagCompound nbt){
 		for(byte side = 0; side < 6; side++)
 			if(this.sidedCanToggle[side])
 				nbt.setByte("sidedSlot"+side, (byte)(this.sidedSlots[side] + 1));
 	}
 }
