 package com.qzx.au.core;
 
 import net.minecraft.item.ItemStack;
 
 public class SidedSlotInfo {
 	public static final int BLACK_COLOR		= 0x00000;
 	public static final int RED_COLOR		= 0xff0000;
 	public static final int GREEN_COLOR		= 0x228b22;
 	public static final int YELLOW_COLOR	= 0xffff00;
 	public static final int BLUE_COLOR		= 0x0000ff;
 	public static final int PURPLE_COLOR	= 0x9370db;
 	public static final int ORANGE_COLOR	= 0xffa500;
 
 	public static final int DEFAULT_INPUT_COLOR = SidedSlotInfo.BLUE_COLOR;
 	public static final int DEFAULT_OUTPUT_COLOR = SidedSlotInfo.ORANGE_COLOR;
 
 	private String name;
 	private int color;
 	private byte firstIndex;
 	private byte lastIndex;
 	private boolean canInsert;
 	private boolean canExtract;
 
 	public SidedSlotInfo(String name, int color, byte firstIndex, int nrSlots, boolean canInsert, boolean canExtract){
 		this.name = name;
 		this.color = color;
 		this.firstIndex = firstIndex;
		this.lastIndex = (byte)(firstIndex + nrSlots - 1);
 		this.canInsert = canInsert;
 		this.canExtract = canExtract;
 	}
 
 	public String getName(){
 		return this.name;
 	}
 	public int getColor(){
 		return this.color;
 	}
 	public byte getFirstIndex(){
 		return this.firstIndex;
 	}
 	public byte getLastIndex(){
 		return this.lastIndex;
 	}
 	public boolean canInsert(){
 		return this.canInsert;
 	}
 	public boolean canExtract(){
 		return this.canExtract;
 	}
 
 	// override this for input slots
 	public boolean isItemValid(ItemStack itemstack){
 		return false;
 	}
 }
