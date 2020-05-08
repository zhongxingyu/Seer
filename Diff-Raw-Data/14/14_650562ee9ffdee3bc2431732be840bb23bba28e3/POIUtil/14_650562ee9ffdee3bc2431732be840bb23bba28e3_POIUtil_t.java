 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package schoolalyzer.util;
 
 import java.awt.Color;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 
 /**
  *
  * @author uli
  */
 public class POIUtil {
 
     private static final char[] upperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
 
     /**
      * Generates a column value (A, B, ..., AA, BB).
      * @param n The column index (zero-based)
      * @return The column name as a string
      * @throws IllegalArgumentException 
      */
     public static String generateColumnName(int n) {
         if (n < 0) {
             throw new IllegalArgumentException("Column indices mustn't be negative");
         }
         StringBuilder sb = new StringBuilder();
         int len = upperLetters.length;
         if (n < len) {
             sb.append(upperLetters[n]);
         } else if (n < (len * len)) { //only two letters
             sb.append(upperLetters[(n / len) - 1]);
             sb.append(upperLetters[n % len]);
         } else {
             throw new IllegalArgumentException("Column names with more than 2 letters are not supported at the moment");
         }
         return sb.toString();
     }
 
     /**
      * Searches a char array for a specific character
      * @param c The char to search for
      * @param array The array to search in
      * @return The index (0-based) of the char in the array (first occurrence) or -1 if not found
      */
     private static int findInArray(char c, char[] array) {
         for (int i = 0; i < array.length; i++) {
             if (c == array[i]) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Parses the column number from column names(A, B, ..., AA, BB).
      * @param colName The name of the column (automatically converted to uppercase)
      * @return The 0-based index
      * @throws IllegalArgumentException If the column name could't be parsed
      */
     public static int getColumnNumber(String colName) {
         colName = colName.toUpperCase();
         if (colName.length() == 1) {
             //Search the letter array for the column name letter
             int index = findInArray(colName.charAt(0), upperLetters);
             if (index == -1) {
                 throw new IllegalArgumentException("The column name contains illegal characters!");
             }
             return index;
         } else if (colName.length() == 2) {
             char c1 = colName.charAt(0);
             char c2 = colName.charAt(1);
             int c1Index = findInArray(c1, upperLetters) + 1; //+1 needed because of the upperLetter.length col names before the two-letter ones begin
             int c2Index = findInArray(c2, upperLetters);
             if (c1Index == -1 || c2Index == -1) {
                 throw new IllegalArgumentException("The column name contains illegal characters: " + colName);
             }
             return upperLetters.length * c1Index + c2Index;
         } else {
             throw new IllegalArgumentException("Given column name " + colName + " can't be parsed (only lengths 1 and 2 supported)");
         }
     }
 
     /**
      * Loads a Workbook from a file
      * @param file The file to load the workbook from
      * @return The workbook loaded from the file
      * @throws IOException
      * @throws InvalidFormatException
      */
     public static Workbook loadWorkbook(File file) throws IOException, InvalidFormatException {
         InputStream in = new BufferedInputStream(new FileInputStream(file));
         Workbook workbook = WorkbookFactory.create(in);
         in.close();
         return workbook;
     }
 
     /**
      * Wrapper for getStringValueSafe(Cell) automatically getting the cell in a safe way
      * @see getStringValueSafe(Cell)
      */
     public static String getStringCellValueSafe(Sheet sheet, int rowIndex, int columnIndex) {
         return getStringCellValueSafe(getCellSafe(sheet, rowIndex, columnIndex));
     }
 
     /**
      * Returns a cell value as a String no matter of the cell type
      * @param cell The cell to get the value from
      * @return The cell value as a String
      */
     public static String getStringCellValueSafe(Cell cell) {
         //Check if the cell is null
         if (cell == null) {
             throw new IllegalArgumentException("Cell argument is null!");
         }
         //Check the cell type
         if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
             return ((Boolean) cell.getBooleanCellValue()).toString();
         } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
             return "";
         } else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
             return ((Byte) cell.getErrorCellValue()).toString();
         } else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
             return cell.getCellFormula();
         } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
             return ((Double) cell.getNumericCellValue()).toString();
         } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
             return cell.getStringCellValue();
         }
         throw new IllegalStateException("The cell has an unknown type!");
     }
 
     /**
      * Checks a cell for emptiness
      * @param cell The cell to be checked
      * @return True if the cell is empty, false otherwise
      */
     public static boolean isEmpty(Cell cell) {
         if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
             return true;
         } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
             return cell.getStringCellValue().isEmpty();
         }
         return false;
     }
 
     /**
      * Checks a cell for emptiness. Automatically gets the cell in a safe way.
      * @param sheet The sheet of the cell
      * @param row The row of the cell
      * @param columnIndex The column of the cell
      * @return True if the cell does not exist or is empty, false otherwise
      */
     public static boolean isEmpty(Sheet sheet, int rowIndex, int columnIndex) {
         Cell cell = getCellSafe(sheet, rowIndex, columnIndex);
         if (cell == null) { //Return true if the cell does not exist
             return true;
         }
         return isEmpty(cell);
     }
 
     /**
      * @return The cell or null if it doesn't exist.
      */
     public static Cell getCellSafe(Sheet sheet, int rowIndex, int columnIndex) {
         if (sheet == null) {
             throw new IllegalArgumentException("The sheet argument is null!");
         }
         Row row = sheet.getRow(rowIndex);
         if (row == null) { //undefined row
             return null;
         }
         return row.getCell(columnIndex);
     }
 
     /**
      * Gets or creates a cell and the corresponding row in a given sheet
      * @return The cell from the sheet which is ensured to exist
      */
     public static Cell getOrCreateCell(Sheet sheet, int rowIndex, int colIndex) {
         Row row = sheet.getRow(rowIndex);
         if (row == null) {
             row = sheet.createRow(rowIndex);
         }
         Cell cell = row.getCell(colIndex);
         if (cell == null) {
             cell = row.createCell(colIndex);
         }
         return cell;
     }
 
     /**
      * Sets a cell value while ensuring the cell/row/column is created if it doesn't exist
      */
     public static void setCellValueSafe(Sheet sheet, int rowIndex, int colIndex, String value) {
         Cell cell = getOrCreateCell(sheet, rowIndex, colIndex);
         cell.setCellValue(value);
     }
 
     /**
      * Sets a cell value while ensuring the cell/row/column is created if it doesn't exist
      */
     public static void setCellValueSafe(Sheet sheet, int rowIndex, int colIndex, double value) {
         Cell cell = getOrCreateCell(sheet, rowIndex, colIndex);
         cell.setCellValue(value);
     }
 
     public static Color getVisibleForegroundColor(Color backgroundColor) {
         if ((backgroundColor.getRed() + backgroundColor.getGreen() + backgroundColor.getBlue()) / 127.0 > 127) {
             return Color.WHITE;
         }
         return Color.BLACK;
     }
 
     public static Color getForegroundColor(CellStyle cellStyle) {
         short[] foregroundTriplet = ((HSSFColor) cellStyle.getFillForegroundColorColor()).getTriplet();
         return new Color(foregroundTriplet[0], foregroundTriplet[1], foregroundTriplet[2]);
     }
 
     public static Color getBackgroundColor(CellStyle cellStyle) {
         if (cellStyle.getFillPattern() != CellStyle.SOLID_FOREGROUND) {
             return Color.WHITE;
         }
         short[] backgroundTriplet = ((HSSFColor) cellStyle.getFillBackgroundColorColor()).getTriplet();
         return new Color((int) backgroundTriplet[0], (int) backgroundTriplet[1], (int) backgroundTriplet[2]);
     }
 }
