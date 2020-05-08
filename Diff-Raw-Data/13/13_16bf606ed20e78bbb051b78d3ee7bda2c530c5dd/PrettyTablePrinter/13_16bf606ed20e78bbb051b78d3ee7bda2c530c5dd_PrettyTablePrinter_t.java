 package de.softwarekollektiv.dbs.app;
 
 import java.io.PrintStream;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 
 public class PrettyTablePrinter {
 	
 	public static void print(PrintStream out, ResultSet rs) throws SQLException {
 		ResultSetMetaData rsmd = rs.getMetaData();
 		
 		// calculate column width
 		// In case ResultSet is not scrollable, we fall back to variable column width
 		// indicated by widths == {0, 0, 0, ... }	
 		int[] widths;
 		if(rs.getType() == ResultSet.TYPE_FORWARD_ONLY)
 			widths = new int[rsmd.getColumnCount()];
 		else
 			widths = calculateColumnWidth(rs, rsmd);
 				
 		// Print header
 		out.print("# ");
 		for(int i = 0; i < rsmd.getColumnCount(); ++i) {
 			out.print(padRight(rsmd.getColumnLabel(i + 1), widths[i]));
 			out.print(" # ");
 		}
 		out.println();
 		
 		// Print rows
 		while(rs.next()) {
 			out.print("# ");
 			for(int i = 0; i < rsmd.getColumnCount(); ++i) {
 				String value = rs.getString(rsmd.getColumnLabel(i + 1));
				if(value != null)
					out.print(padRight(value, widths[i]));
				else
					out.print(padRight("null", widths[i]));
 				out.print(" # ");
 			}
 			out.println();
 		}
 	}
 	
 	private static String padRight(String s, int n) {
 		if(n > 0)
 			return String.format("%1$-" + n + "s", s); 
 		else
 			return s;
 	}
 
 
 	private static int[] calculateColumnWidth(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
 		int[] retval = new int[rsmd.getColumnCount()];
 		for(int i = 0; i < rsmd.getColumnCount(); ++i)
 			retval[i] = rsmd.getColumnLabel(i + 1).length();
 				
		int len;
 		while(rs.next()) {
 			for(int i = 0; i < rsmd.getColumnCount(); ++i) {
 				String value = rs.getString(rsmd.getColumnLabel(i + 1));
				len = (value == null) ? 4 : value.length();
				if(retval[i] < len)
					retval[i] = len;
 			}
 		}
 		rs.beforeFirst();
 				
 		return retval;
 	}	
 }
