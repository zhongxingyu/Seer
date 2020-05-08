 package com.jpii.navalbattle.pavo.grid;
 
 public class GridedEntityTileOrientation {
 	public static final byte ORIENTATION_LEFTTORIGHT = 0x0A;
 	public static final byte ORIENTATION_TOPTOBOTTOM = 0x1A;
 	public static final byte ORIENTATION_RIGHTTOLEFT = 0x2A;
 	public static final byte ORIENTATION_BOTTOMTOTOP = 0x3A;
 	
 	private short[] INDICES = new short[4];
 	
 	public GridedEntityTileOrientation() {
 	}
 	
 	public void setLeftToRightImage(short storeId) {
 		INDICES[0] = storeId;
 	}
 	
 	public void setTopToBottomImage(short storeId) {
 		INDICES[1] = storeId;
 	}
 	
 	public void setRightToLeftImage(short storeId) {
 		INDICES[2] = storeId;
 	}
 	
 	public void setBottomToTopmage(short storeId) {
 		INDICES[3] = storeId;
 	}
 	
 	public short memCall(int offset) {
 		if (offset > 3 || offset < 0)
 			return 0;
 		return INDICES[offset];
 	}
}
