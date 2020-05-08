 package com.tnes;
 
 public class PPUHelper {
 	public static short[] PATTERN_TABLE_BIT_MASK = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };
 	public static short[] PATTERN_TABLE_BYTE1_BIT_SHIFT = { 7, 6, 5, 4, 3, 2, 1, 0 };
 	public static short[] PATTERN_TABLE_BYTE2_BIT_SHIFT = { 6, 5, 4, 3, 2, 1, 0, -1 };
 	public static short[] ATTRIBUTE_TABLE_BIT_MASK = { 0x03, 0x0C, 0x30, 0xC0 };
 	public static short[] ATTRIBUTE_TABLE_BIT_SHIFT = { -2, 0, 2, 4 };
 
 	public static int getTileIndex(int scanline, int scanlineCycle) {
 		return ((int) (Math.floor(scanline / 8) * 32) + (int) Math.floor(scanlineCycle / 8));
 	}
 
 	public static int getPatternTableByte1Index(int patternTableIndex, int scanline) {
 		return (patternTableIndex * 16) + (scanline % 8);
 	}
 
 	public static int getPatternTableByte2Index(int patternTableIndex, int scanline) {
 		return ((patternTableIndex * 16) + 8) + (scanline % 8);
 	}
 
 	public static int getAttributeTableIndex(int tileIndex) {
 		int result = 0;
 		result += (Math.floor(tileIndex / 128) * 8);
 		result += (Math.floor((tileIndex % 32) / 4));
 		return result;
 	}
 
 	public static int getAttributeTableSquare(int tileIndex) {
 		int square = 0;
 		if ((tileIndex % 128) >= 64)
 			square = 2;
 
 		square += ((tileIndex % 4) >= 2) ? 1 : 0;
 		return square;
 	}
 }
