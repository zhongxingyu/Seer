 package com.gamelion.xls2xml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 
 import com.gamelion.xls2xml.Xls2xml.LangSheetInfo;
 
 public class XlsReader {
 	
 	public void read( File inputFile, int keysStartRow, int keysColumn, ArrayList<LangSheetInfo> lngsInfo ) {
 		InputStream in = null;
 		try {
 			
 			in = new FileInputStream(inputFile);
 			
 			HSSFWorkbook workbook = new HSSFWorkbook( in );
 			HSSFSheet sheet = workbook.getSheetAt(0);
 			ReadSheet(sheet, keysStartRow, keysColumn, lngsInfo);
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if ( in != null ) {
 				try {
 					in.close();
 				} catch (IOException e2) {
 					// TODO Auto-generated catch block
 					e2.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	
 	private void ReadSheet( HSSFSheet sheet, int keysStartRow, int keysColumn, ArrayList<LangSheetInfo> lngsInfo ) {
 
		for ( int i = keysStartRow; i <= sheet.getLastRowNum(); i++ ) {
 			HSSFRow row = sheet.getRow(i);
 			HSSFCell keyCell = row.getCell( keysColumn );
 			String keyVal = getCellValue( keyCell );
 			
 			if ( keyVal.isEmpty() ) {
 				break;
 			}
 			
 			addSentences(row, keyVal, lngsInfo);
 		}
 	}
 	
 	
 	void addSentences( HSSFRow row, String keyVal, ArrayList<LangSheetInfo> lngsInfo ) {
 		for ( LangSheetInfo li : lngsInfo ) {
 			HSSFCell cell = row.getCell( li.column );
 			String value = getCellValue(cell);
 			
 			Bank.add(keyVal, value, li.lang);
 		}
 	}
 	
 	
 	private String getCellValue( HSSFCell cell ) {
 		String val = null;
 		
 		switch ( cell.getCellType() ) {
 		case Cell.CELL_TYPE_BLANK:
 			val = "";
 			break;
 		case Cell.CELL_TYPE_BOOLEAN:
 			val = String.valueOf( cell.getBooleanCellValue() );
 			break;
 		case Cell.CELL_TYPE_ERROR:
 			System.err.println("Error at cell: column " + cell.getColumnIndex() + "   row " + cell.getRowIndex());
 			System.exit( -1 );
 			break;
 		case Cell.CELL_TYPE_FORMULA:
 			val = cell.getCellFormula();
 			break;
 		case Cell.CELL_TYPE_NUMERIC:
 			val = String.valueOf(cell.getNumericCellValue());
 			break;
 		case Cell.CELL_TYPE_STRING:
 			val = cell.getStringCellValue();
 			break;
 		default:
 			break;
 		}
 		
 		return val;
 	}
 }
