 package ch.epfl.data.distribdb.app;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 /**
  * TablePrinter
  *
  * Nicely formats final result
  *
  * @author Amer C <amer.chamseddine@epfl.ch>
  */
 public class TablePrinter {
 
 	/**
 	 * Here you can set the number of tuples that appear in a single page 
 	 * when the output is paginated due to its big size
 	 */
 	public static final int PRINT_PAGE_SIZE = 10;
 
 	/**
 	 * Prints all the records in the given result set to the given output stream
 	 * and uses the input stream to interact with the user should the output be
 	 * paginated (in case it is too big)
 	 *  
 	 * @param InputStream
 	 * @param OutputStream
 	 * @param ResultSet
 	 * @throws SQLException
 	 * @throws InterruptedException
 	 */
 	public static void printTableData(InputStream is, OutputStream os, ResultSet result) throws SQLException, InterruptedException {
 
 		PrintStream printStream = new PrintStream(os);
 		Scanner scanner = new Scanner(is);
 		
 		ResultSetMetaData meta = result.getMetaData();
 
 		Map<Integer, Integer> colWidth = new HashMap<Integer, Integer>();
 		List<Map<Integer, String>> pageData = new LinkedList<Map<Integer,String>>();
 		
 		while(true) {
 			boolean more = fetchDataPage(result, meta, pageData);
 			optimizeWidth(pageData, meta, colWidth);
 			printStream.print(tblHeader(meta, colWidth));
 			for(Map<Integer, String> i : pageData) {
 				printStream.print("|");
 				for(int j = 1; j <= meta.getColumnCount(); j++) {
 					printStream.print(" " + PrinterUtils.padLeft(i.get(j), colWidth.get(j)) + " ");
 					printStream.print("|");
 				}
 				printStream.print("\n");
 			}
 			printStream.print(hrLine(meta, colWidth));
 			if(more) {
 				printStream.println("Press 'enter' to display next " + PRINT_PAGE_SIZE + " results, or type 'c' then 'enter' to break");
 				if(!scanner.nextLine().toLowerCase().startsWith("c"))
 					continue;
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * Internal function that fetches the next page from the 
 	 * given result set and puts it in the given pageData argument
 	 * 
 	 * @param ResultSet
 	 * @param ResultSetMetaData this should be the meta data of this 
 	 * result set; it is used to know the columns in the result set
 	 * @param pageData Will be filled with the fetched records 
 	 * @return boolean Will return true if there is more records in the reult set
 	 * @throws SQLException
 	 */
 	private static boolean fetchDataPage(ResultSet result, ResultSetMetaData meta, List<Map<Integer, String>> pageData) throws SQLException {
 
 		pageData.clear();
 		for(int i = 0; i < PRINT_PAGE_SIZE; i++) {
 			if(!result.next())
 				return false;
 			Map<Integer, String> record = new HashMap<Integer, String>();
 			for(int j = 1; j <= meta.getColumnCount(); j++) {
 				record.put(j, result.getString(j));
 			}
 			pageData.add(record);
 		}
 		return !result.isLast();
 	}
 	
 	/**
 	 * Internal function used to dynamically adapt the 
 	 * width of columns according to the current page data
 	 * because the output is formatted in a fixed-width fashion
 	 * It checks the records in the given pageData and fills 
 	 * the given colWidth accordingly 
 	 *  
 	 * @param pageData
 	 * @param ResultSetMetaData
 	 * @param colWidth
 	 * @throws SQLException
 	 */
 	private static void optimizeWidth(List<Map<Integer, String>> pageData, ResultSetMetaData meta, Map<Integer, Integer> colWidth) throws SQLException {
 
 		colWidth.clear();
 		for(int j = 1; j <= meta.getColumnCount(); j++) {
 			colWidth.put(j, meta.getColumnLabel(j).length());
 		}
 		for(Map<Integer, String> i : pageData) {
 			for(int j = 1; j <= meta.getColumnCount(); j++) {
 				if(i.get(j).length() > colWidth.get(j)) {
 					colWidth.remove(j);
 					colWidth.put(j, i.get(j).length());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Internal function used to generate the table header
 	 * It reads the columns titles from the result set meta data
 	 * and adapts the columns widths according to colWidth
 	 * 
 	 * @param ResultSetMetaData
 	 * @param colWidth
 	 * @return String It returns the formatted string to be 
 	 * printed to the console output 
 	 * @throws SQLException
 	 */
 	private static String tblHeader(ResultSetMetaData meta, Map<Integer, Integer> colWidth) throws SQLException {
 		StringBuilder hLine = new StringBuilder();
 		hLine.append(hrLine(meta, colWidth));
 
 		hLine.append("|");
 		for(int j = 1; j <= meta.getColumnCount(); j++) {
 			hLine.append(" " + PrinterUtils.padRight(meta.getColumnLabel(j), colWidth.get(j)) + " ");
 			hLine.append("|");
 		}
 		hLine.append("\n");
 
 		hLine.append(hrLine(meta, colWidth));
 		return hLine.toString();
 	}
 	
 	/**
 	 * Internal function used to generate a horizontal line
 	 * out of dashes and plus signs, according to the colWidth 
 	 * 
 	 * @param ResultSetMetaData
 	 * @param colWidth
 	 * @return String formatted string to be printed to 
 	 * the console output  
 	 * @throws SQLException
 	 */
 	private static String hrLine(ResultSetMetaData meta, Map<Integer, Integer> colWidth) throws SQLException {
 		
 		StringBuilder hLine = new StringBuilder();
 		hLine.append("+");
 
 		for(int j = 1; j <= meta.getColumnCount(); j++) {
 			hLine.append("-" + PrinterUtils.padRight("", colWidth.get(j), '-') + "-");
 			hLine.append("+");
 		}
 		hLine.append("\n");
 		return hLine.toString();
 	}
 	
 	
 	/**
 	 * PrinterUtils - Utility class containing helper functions for printing purposes
 	 * 
 	 * @author Amer C (amer.chamseddine@epfl.ch)
 	 *
 	 */
 	public static class PrinterUtils {
 		
 		/**
 		 * Pads string s with character c from the right 
 		 * so that it reaches a length of n
 		 * 
 		 * @param s
 		 * @param n
 		 * @param c
 		 * @return String
 		 */
 		public static String padRight(String s, int n, char c) {
 			return padRight(s, n).replace(' ', c);  
 		}
 
 		/**
 		 * Pads string s with character c from the left 
 		 * so that it reaches a length of n
 		 * 
 		 * @param s
 		 * @param n
 		 * @param c
 		 * @return String
 		 */
 		public static String padLeft(String s, int n, char c) {
 			return padLeft(s, n).replace(' ', c);  
 		}
 		
 		/**
 		 * Pads string s from the right so that it reaches a length of n
 		 * 
 		 * @param s
 		 * @param n
 		 * @return String
 		 */
 		public static String padRight(String s, int n) {
 			int x = s.length() - n;
 			return String.format("%1$-" + n + "s", (x > 0 ? s.substring(0, n) : s));  
 		}
 
 		/**
 		 * Pads string s from the left so that it reaches a length of n
 		 * 
 		 * @param s
 		 * @param n
 		 * @return String
 		 */
 		public static String padLeft(String s, int n) {
 			int x = s.length() - n;
			return String.format("%1$" + n + "s", (x > 0 ? s.substring(x) : s));
 		}
 		
 	}
 	
 }
