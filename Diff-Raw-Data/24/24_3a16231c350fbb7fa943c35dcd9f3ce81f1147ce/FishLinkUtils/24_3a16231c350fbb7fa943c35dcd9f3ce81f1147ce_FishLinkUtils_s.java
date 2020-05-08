 package org.freshwaterlife.fishlink;
 
 import at.jku.xlwrap.common.XLWrapException;
 import at.jku.xlwrap.spreadsheet.Cell;
 import at.jku.xlwrap.spreadsheet.Sheet;
 import at.jku.xlwrap.spreadsheet.Workbook;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 import java.util.Date;
 
 /**
  * Static utility functions shared by several classes.
  * 
  * @author Christian
  */
 public class FishLinkUtils {
 
     /**
      * Converts a Letter Column name to the zero based column index.
      * 
      * For example "A" becomes 0, "Z" becomes 25 and "AA" becomes 26.
      * @param alpha Column name as Letters
      * @return zero-based numerical index
      */
     public static final int alphaToIndex(String alpha) {
 	char[] letters = alpha.toUpperCase().toCharArray();
 	int index = 0;
 	for (int i = 0; i < letters.length; i++)
             index += ((letters[letters.length-i-1]) - 64) * (Math.pow(26, i)); // A is 64
 	return --index;
     }
 
     /**
      * Converts zero based column name to a Letter Column index the 
      * 
      * For example 0 becomes "A", 25 becomes "Z" and 26 becomes "AA".
      * @param index zero-based numerical index
      * @return alpha Column name as letters
      */
     public static final String indexToAlpha(int index){
         String reply = "";
         if (index < 26){
            char first = (char)( index + 65);
            reply = first + "";
         } else {
            char last = (char)(index - ((index / 26) * 26) + 65);
            String rest = indexToAlpha((index / 26)-1);
            reply = rest + last;
         }
         if (index != alphaToIndex(reply)){
             System.err.println("Error converting " + index);
             System.err.println("Answer was "+ reply);
             System.err.println("Which comes out as "+ alphaToIndex(reply));
             throw new AssertionError("Error in indexToAlpha");
         }
         return reply;
     }
  
     /**
      * Gets the String in the specified cell from a Sheet.
      * 
      * Given a sheet, column and row indexes, this function will look up the String Value in that sheet.
      * <p>Support data requests beyond the end of the sheet by returning a blank.
      * <p>Catches and wraps any Exception thrown.
      * 
      * @param sheet xlwrap.spreadsheet.Sheet
      * @param column Zero based Column index
      * @param row Zero based Row index
      * @return The context of that cell as a String or a blank if beyond the current sheet boundaries.
      * @throws FishLinkException Wraps exceptions thrown by XLWrap.
      */
     public static String getTextZeroBased(Sheet sheet, int column, int row) throws FishLinkException {
         if (column >= sheet.getColumns()){
             return "";
         }
         if (row >= sheet.getRows()){
             return "";
         }
         try {
             Cell cell = sheet.getCell(column, row);
             return cell.getText();
         } catch (XLWrapException ex) {
             throw new FishLinkException("Unable to cell column: " + column + " row:" + row + " from " + 
                     sheet.getSheetInfo(), ex);
         } catch (XLWrapEOFException ex) {
             throw new FishLinkException("Unable to cell column: " + column + " row:" + row + " from " + 
                     sheet.getSheetInfo(), ex);
         }
     }
 
     /**
      * Reports any message in a standard way.
      * 
      * Current implementation is just to System.out but logging can be added here!
     * @param messages Any text to be output.
      */
     public static void report (String message){
         System.out.println(message);
     }
 
 }
