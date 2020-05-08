 // histed 120801: created
 
 import org.apache.poi.ss.usermodel.*;
 import org.apache.poi.openxml4j.exceptions.*;
 import java.io.*;
 
 public class frm_jcReadXls {
  
 	protected java.io.FileInputStream jFH;
 		
 	protected double[][] outNumArray;
 	protected String[][] outStringArray;
 	protected int[][] outTypeArray;
 	protected boolean isReadDone = false;
 	protected int sheetNum;
 
 	public frm_jcReadXls(String name, int inSheetNum)
 			throws RuntimeException {
 		try {
 			jFH = new FileInputStream(name);	
 		} catch (FileNotFoundException exc) {
			System.out.println("File not found");
 		}
 		
 		sheetNum = inSheetNum;
 		try {
 			subReadAllCellsIntoMem();
 		} catch (IOException exc) {
 			System.out.println("POI error: IO error reading cells into memory");
 		} catch (InvalidFormatException exc) {
 			System.out.println("POI error: Invalid file format");
 		} 
 		
 	}
 	
 	protected void subReadAllCellsIntoMem() 
 				throws IOException, InvalidFormatException, RuntimeException {
 		Workbook wb;
 		int nSheets, nRows, nCols;
 		int tColN; 
 		int tRowN;
 		Sheet tSheet;
 		
 		if (isReadDone) {
 			throw new RuntimeException("Tried to read twice");
 		}
 		
 		
 		wb = WorkbookFactory.create(jFH);
 		nSheets = wb.getNumberOfSheets();	
 		if (sheetNum > nSheets) {
 			throw new RuntimeException("Sheet numbers are 0-origin");
 		}
 		
 		tSheet = wb.getSheetAt(sheetNum-1);
 		nRows = tSheet.getLastRowNum()+1;
 		
 		// first count the max num of rows and columns - have to iterate over all defined cells.
 		nCols = 0;
 		nRows = 0;
 		for (Row tRow : tSheet) {
 			tRowN = tRow.getRowNum();
 			for (Cell tCell : tRow) {
 				if (tCell.getCellType() == Cell.CELL_TYPE_BLANK) {
 					continue; // don't count blank cells in size
 				}
 				tColN = tCell.getColumnIndex();
 
 				
 				nRows = Math.max(nRows,tRowN+1);
 				nCols = Math.max(nCols,tColN+1);
 				
 			}
 		}
 		
 		//System.out.printf("%d %d\n", nRows, nCols);
 		
 		// init arrays and read cells
 		outNumArray = new double[nRows][nCols];
 		outStringArray = new String[nRows][nCols];
 		outTypeArray = new int[nRows][nCols];
 		// Fill num array with NaN  (our missing value)
 		for (double[] tRow: outNumArray) {
 		    java.util.Arrays.fill(tRow, Double.NaN);
 		}
 
 		for (Row tRow : tSheet) {
 			tRowN = tRow.getRowNum();
 			
 			for (Cell tCell : tRow) {
 				tColN = tCell.getColumnIndex();
 				
 				switch (tCell.getCellType()) {
 					case Cell.CELL_TYPE_BLANK:
 						continue; // completely skip blank cells
 						
 					case Cell.CELL_TYPE_NUMERIC:
 					case Cell.CELL_TYPE_BOOLEAN:
 						outNumArray[tRowN][tColN] = tCell.getNumericCellValue();
 						break;
 					
 					case Cell.CELL_TYPE_STRING:
 						outStringArray[tRowN][tColN] = tCell.getStringCellValue();
 						break;
 
 				}
 				outTypeArray[tRowN][tColN] = tCell.getCellType();
 
 			
 		}
 		
 		isReadDone = true;
 	}	
 }
 
 	public Object[] getCellContents() 
 			throws RuntimeException {
 		
 		Object[] outArray;
 		
 		if (!isReadDone) {
 			throw new RuntimeException("Call jcReadAllCellsIntoMem first");
 		}
 	
 		outArray = new Object[3];
 		outArray[0] = outNumArray;
 		outArray[1] = outStringArray;
 		outArray[2] = outTypeArray;
 		
 		return outArray;
 	}
 
 }
