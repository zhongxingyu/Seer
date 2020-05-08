 /**
  * Cell property types
  */
 package com.pace.base.mdb;
 
 /**
  * @author Alan Farkas
  *
  */
 public enum CellPropertyType {
 	
 	Dirty(1,false),
	Empty(1,true);
 	
 	private int bitCount = 0;		// Number of bits in property type
 	private Object defaultValue;	// Default property value (if cell does not exist)
 	
 	CellPropertyType(int bitCount, Object defaultValue) {
 		this.bitCount = bitCount;
 		this.defaultValue = defaultValue;
 	}
 	
 	
 	/**
 	 * @return the bitCount
 	 */
 	public int getBitCount() {
 		return bitCount;
 	}
 
 	/**
 	 * @return the defaultValue
 	 */
 	public Object getDefaultValue() {
 		return defaultValue;
 	}
 
 	/**
 	 * @return the property type's offset index
 	 */
 	public int getOffSet() {
 		int offSet = 0;
 		for (CellPropertyType propertyType : CellPropertyType.values()) {
 			if (propertyType.equals(this)) break;
 			offSet += propertyType.getBitCount();
 		}
 		return offSet;
 	}
 	
 	/**
 	 * @return the total number of bits across all property types
 	 */
 	public static int getTotBitCount() {
 		int totBitCount = 0;
 		for (CellPropertyType propertyType : CellPropertyType.values()) {
 			totBitCount += propertyType.getBitCount();
 		}
 		return totBitCount;
 	}
 
 }
 
