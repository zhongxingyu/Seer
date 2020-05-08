 /**
  * Data Cache Block
  */
 package com.pace.base.mdb;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author Alan Farkas
  *
  */
 public class DataBlock {
 	double[][] cellValues = null; 			// Cell value array - [Measure][Time]
 	DataBlockProperties properties = null;	// Data block properties
 	
 	Logger logger =  Logger.getLogger(DataBlock.class);
 	
 
 	public DataBlock(int rows, int cols, int cellPropsBitCount) {
 		cellValues = new double[rows][cols];
 		properties = new DataBlockProperties(rows, cols, cellPropsBitCount);
 	}
 
 	
 	/**
 	 * Get the value of a data cache cell
 	 * 
 	 * @param cellAddress - Data cache cell address
 	 * @return Cell value
 	 */
 	public double getCellValue(DataCacheCellAddress cellAddress) {
 		return 	cellValues[cellAddress.getCoordX()][cellAddress.getCoordY()];	
 	}
 
 	/**
 	 * Set the value of a data cache cell
 	 * 
 	 * @param cellAddress - Data cache cell address
 	 * @param value - Value to set
 	 */
 	public void setCellValue(DataCacheCellAddress cellAddress, double value) {	
 		cellValues[cellAddress.getCoordX()][cellAddress.getCoordY()] = value;	
 	}
 
 
 	/**
 	 * Get cell property value for selected property type and cell address
 	 * 
 	 * @param propertyType Cell property type
 	 * @param cellAddress Cell address
 	 * 
 	 * @return Object
 	 */
 	public Object getCellProperty(CellPropertyType propertyType, DataCacheCellAddress cellAddress) {		
		return properties.getCellProperty(propertyType, cellAddress);
 	}
 
 	/**
 	 * Set cell property value for selected cell address
 	 * 
 	 * @param propertyType Cell property type
 	 * @param cellAddress Cell address
 	 * @param value Cell property value
 	 */
 	public void setCellProperty(CellPropertyType propertyType, DataCacheCellAddress cellAddress, Object value) {
 		
 		// Create new cell property
 		CellProperty cellProperty = CellProperty.getCellPropertyFactory(propertyType);
 
 		// Validate cell property value
 		if (!cellProperty.isValidValue(value)) {
 			String errMsg = "Data Block setCellProperty error - invalid property value";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 
 		// Set cell value
 		cellProperty.setValue(value);
 		properties.setCellProperty(cellAddress, cellProperty);
 	}
 
 	/**
 	 * Set cell property for selected property type across all data block cells
 	 * 
 	 * @param cellProperty Cell property
 	 */
 	private void setAllCellsProperty(CellProperty cellProperty) {
 		
 		int rowCount = cellValues.length, colCount = cellValues[0].length;
 		DataCacheCellAddress cellAddress = new DataCacheCellAddress();
 		for (int row = 0; row < rowCount; row++) {
 			cellAddress.setCoordX(row);
 			for (int col = 0; col < colCount; col++) {
 				cellAddress.setCoordY(col);
 				properties.setCellProperty(cellAddress, cellProperty);
 			}
 		}
 	}
 
 	/**
 	 * @return the isDirty
 	 */
 	public boolean isDirty() {
 		return properties.isDirty();
 	}
 	
 	/**
 	 * @param isDirty the isDirty to set
 	 */
 	public void setDirty(boolean isDirty) {
 		
 		// Set block level dirty flag
 		properties.setDirty(isDirty);
 		
 		// Clear dirty flag on each individual cell, if block level 
 		// dirty flag is being set to false
 		if (!isDirty) {
 			CellProperty cellProperty = CellProperty
 					.getCellPropertyFactory(CellPropertyType.Dirty);
 			cellProperty.setValue(isDirty);
 			setAllCellsProperty(cellProperty);
 		}
 		
 	}
 
 	/**
 	 * Set dirty flag
 	 */
 	public void setDirty() {
 		setDirty(true);
 	}
 	
 	/**
 	 * Clear dirty flag
 	 */
 	public void clearDirty() {
 		setDirty(false);
 	}
 	
 	/*
 	 *	Represent the DataCache as a 2-dimensional array of data cells
 	 *
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 
 		int rowCount = cellValues.length;
 		int colCount = cellValues[0].length;
 		StringBuffer stringBuffer = new StringBuffer("\n");	
 		String format = "%#11.2f\t";
 
 		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++ ) {
 			for (int colIndex = 0; colIndex < colCount; colIndex++ ) {
 				stringBuffer.append(String.format(format, cellValues[rowIndex][colIndex]));
 			}
 			stringBuffer.append("\n");
 		}
 		return stringBuffer.toString();
 	}
 
 }
