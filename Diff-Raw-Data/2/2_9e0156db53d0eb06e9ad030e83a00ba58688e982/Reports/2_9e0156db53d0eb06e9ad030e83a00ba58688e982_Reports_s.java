 import java.sql.*;
 import java.util.ArrayList;
 
 public class Reports {
 	
 	private static Connection con;
 	
 	public static ArrayList<ArrayList<String>> mostPopularBooks(String year, String quantity) throws IllegalArgumentException
 	{
 		int quantityNumber = Integer.parseInt(quantity);
 		int yearNumber = Integer.parseInt(year);
 		int yearStart = (yearNumber -1970)*31556926;
 		int yearEnd = (yearNumber -1970+1)*31556926;
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		
 		Statement  stmt;
 		ResultSet  rs;
 		
 
 		try {
 				con = db_helper.connect("ora_i7f7", "a71163091");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		
 		try
 		{
 		stmt = con.createStatement();
 		
		rs = stmt.executeQuery("SELECT callNumber, name, COUNT(*) as qty FROM book b1, " +
 				"(SELECT * FROM borrowing WHERE outDate >= "+yearStart+
 				" AND inDate <= "+yearEnd+") b2 " +
 				"WHERE b1.callNumber = b2.callNumber "+
 				"GROUP BY callNumber ORDER BY qty DESC");
 		
 		int i = 0;
 		
 		while ((i<quantityNumber)&&(rs.next()))
 		{
 			ArrayList<String> aPopularBook = new ArrayList<String>();
 			aPopularBook.add(rs.getString(1));
 			aPopularBook.add(rs.getString(2));
 			aPopularBook.add(rs.getString(3));
 			result.add(aPopularBook);
 		}
 				
 		
 		} catch (SQLException e2) {
 			e2.printStackTrace();
 		}
 		
 		return result;
 		
 	}
 	
 	
 	
 	
 	
 }
