 /**
  * Cell property super class
  */
 package com.pace.base.mdb;
 
 /**
  * @author Alan Farkas
  *
  */
 public abstract class CellProperty {
 
 	protected CellPropertyType type = null;
 	protected boolean[] propertyBits = null;
 	
	public boolean[] getPropertyBits() {
 		return propertyBits;
 	}
 	
	public void setPropertyBits(boolean[] propertyBits) {
 		this.propertyBits = propertyBits;
 	}
 
 	public abstract Object getValue();
 	public abstract void setValue(Object value); 
 	public abstract boolean isValidValue(Object value);
 	
 	public static CellProperty getCellPropertyFactory(CellPropertyType propertyType) {
 
 		CellProperty cellProperty = null;
 
 		switch(propertyType) {
 		case Dirty:
 			cellProperty = new CellPropertyBoolean(propertyType);
 			break;
 		case Empty:
 			cellProperty = new CellPropertyReverseBoolean(propertyType);
 			break;
 		default: 
 			String logMsg = "Illegal property type: " + propertyType.toString()
 					+ "  passed to cell property function";
 			throw new IllegalArgumentException(logMsg);
 		}
 
 		return cellProperty;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public CellPropertyType getType() {
 		return type;
 	}
 
 }
