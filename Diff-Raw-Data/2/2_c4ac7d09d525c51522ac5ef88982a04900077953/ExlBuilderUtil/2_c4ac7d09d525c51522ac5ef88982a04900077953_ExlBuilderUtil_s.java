 package com.xuechong.utils.exl.process.builder;
 
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.util.CellRangeAddress;
 
 public class ExlBuilderUtil {
 	/**
 	 * merg cal<br>
 	 * cal the num of merg cloumns
 	 * 
 	 * @param contentStr
 	 * @param row
 	 * @return
 	 * @author xuechong
 	 */
 	static CellRangeAddress createMergRegion(String contentStr, Integer row) {
 		int width = contentStr.length() / 2 + 1;
 		return new CellRangeAddress(row, row, 0, width > 6 ? width : 6);
 	}
 	
 	/**
 	 * expend the column width
 	 * @param dataLength
 	 * @param columnIndex
 	 * @param sheet
 	 * @author xuechong
 	 */
 	static void expendColumnWidth(int dataLength,int columnIndex,Sheet sheet){
 		int expectLength = (dataLength<<9);
 		if( dataLength > 4
 				&&sheet.getColumnWidth(columnIndex) < expectLength){
			sheet.setColumnWidth(columnIndex,expectLength);
 		}
 	}
 }
