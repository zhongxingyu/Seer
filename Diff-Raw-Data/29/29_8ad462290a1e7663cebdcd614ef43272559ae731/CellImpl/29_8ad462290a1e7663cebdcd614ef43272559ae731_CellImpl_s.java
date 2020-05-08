 /*******************************************************************************
  * Copyright 2011 kawasima
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package net.unit8.axebomber.parser;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Color;
 import org.apache.poi.ss.usermodel.DateUtil;
 import org.apache.poi.xssf.usermodel.XSSFColor;
 
 public class CellImpl extends Cell {
 	private final org.apache.poi.ss.usermodel.Cell cell;
 	private Pattern rgbExp = Pattern.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");
 	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

 	public CellImpl(org.apache.poi.ss.usermodel.Cell cell) {
 		this.cell = cell;
 		setColumnIndex(cell.getColumnIndex());
 		setRowIndex(cell.getRowIndex());
 	}
 
 	public Boolean isNotBlank() {
 		return cell != null && (cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK);
 	}
 
 	public String toString() {
 		switch(cell.getCellType()) {
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
 			return cell.getStringCellValue();
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
 			if (DateUtil.isCellInternalDateFormatted(cell)) {
 				Date d = DateUtil.getJavaDate(cell.getNumericCellValue());
 				return dateFormat.format(d);
 			} else {
 				BigDecimal d = new BigDecimal(cell.getNumericCellValue());
 				return d.toString();
 			}
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
 			return String.valueOf(cell.getBooleanCellValue());
 		}
 		return "";
 	}
 
 	@Override
 	public int to_i() {
 		switch (cell.getCellType()) {
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
 			try {
 				return Integer.valueOf(cell.getStringCellValue());
 			} catch (NumberFormatException e) {
 				return 0;
 			}
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
 			return (int) cell.getNumericCellValue();
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
 			return (cell.getBooleanCellValue()) ? 1 : 0;
 		}
 		return 0;
 	}
 
 	@Override
 	public void setValue(Object value) {
 		if(value instanceof Cell) {
 			value = ((Cell)value).toString();
 		}
 		switch(cell.getCellType()) {
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK:
 			if(value == null) {
 				// do nothing
 			} else if(value instanceof Number) {
 				cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
 				cell.setCellValue(((Number)value).doubleValue());
 			} else {
 				cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
 				cell.setCellValue(value.toString());
 			}
 			break;
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
 			if(value == null) {
 				cell.setCellValue("");
 			} else {
 				cell.setCellValue(value.toString());
 			}
 			break;
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
 			if(value == null) {
 				cell.setCellValue(0);
 			} else {
 				cell.setCellValue(((Number)value).doubleValue());
 			}
 			break;
 		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
 			if(value==null) {
 				cell.setCellValue(false);
 			} else if(value instanceof String) {
 				Boolean b = Boolean.valueOf(value.toString());
 				cell.setCellValue(b);
 			}
 			break;
 		default:
 			throw new IllegalArgumentException(value.toString());
 		}
 	}
 
 	public java.awt.Color getColor() {
 		java.awt.Color awtColor = null;
 		CellStyle style = cell.getCellStyle();
		Color color = style.getFillBackgroundColorColor();
 		if (color instanceof HSSFColor) {
 			short[] rgb = ((HSSFColor) color).getTriplet();
 			awtColor = new java.awt.Color(rgb[0], rgb[1], rgb[2]);
 		} else if (color instanceof XSSFColor) {
 			byte[] rgb = ((XSSFColor) color).getRgb();
 			awtColor = new java.awt.Color(rgb[0], rgb[1], rgb[2], rgb[3]);
 		}
 		return awtColor;
 	}
 	public void setColor(Object color) {
 		if(color instanceof String) {
 			Matcher m = rgbExp.matcher(color.toString());
 			if(m.matches()) {
 				m.find(1);
 			}
 
 		}
 	}
 
 	@Override
 	public org.apache.poi.ss.usermodel.Cell getSubstance() {
 		return this.cell;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if(object instanceof String) {
 			return this.toString().equals((String)object);
 		} else if (object instanceof CellImpl) {
 			return this.cell.equals(((CellImpl)object).getSubstance());
 		}
 		return false;
 	}
 }
