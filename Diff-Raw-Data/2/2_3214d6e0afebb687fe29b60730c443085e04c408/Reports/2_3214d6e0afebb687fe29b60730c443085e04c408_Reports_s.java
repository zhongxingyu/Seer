 import java.sql.*;
 import java.util.ArrayList;
 
 public class Reports {
 	
 	private static Connection con;
 	private static final long CURRENT_TIME = (System.currentTimeMillis() / 1000L);
 	
 	/*
 	 * Generate a report with the most popular items in a given year. The librarian provides 
 	 * a year and a number n. The system lists out the top n books that where borrowed the 
 	 * most times during that year. The books are ordered by the number of times they were borrowed.
 	*/
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
 		
 		
 		/*
 		rs = stmt.executeQuery("SELECT b1.callNumber, COUNT(*) as qty FROM book b1, " +
 				"(SELECT * FROM borrowing WHERE outDate >= "+yearStart+
 				" AND inDate <= "+yearEnd+") b2 " +
 				"WHERE b1.callNumber = b2.callNumber "+
 				"GROUP BY b1.callNumber ORDER BY qty DESC");
 		*/
 		
 		rs = stmt.executeQuery(
 				
 				"Select t1.title, t2.callNumber, t2.qty FROM book t1, "+
 				"(SELECT b1.callNumber, COUNT(*) as qty FROM book b1, " +
 				"(SELECT * FROM borrowing WHERE outDate >= "+yearStart+
 				" AND outDate <= "+yearEnd+") b2 " +
 				"WHERE b1.callNumber = b2.callNumber "+
 				"GROUP BY b1.callNumber ORDER BY qty DESC) t2 "+
 				"WHERE t1.callNumber = t2.callNumber"
 		
 				);
 		
 		int i = 0;
 		
 		while ((i<quantityNumber)&&(rs.next()))
 		{
 			ArrayList<String> aPopularBook = new ArrayList<String>();
 			aPopularBook.add(rs.getString(1));
 			aPopularBook.add(rs.getString(2));
 			aPopularBook.add(rs.getString(3));
 			result.add(aPopularBook);
 			i++;
 		}	
 		
 		} catch (SQLException e2) {
 			e2.printStackTrace();
 		}
 		
 		return result;
 		
 	}
 	
 	/*
 	 * Generate a report with all the books that have been checked out. For each book the report
 	 * shows the date it was checked out and the due date. The system flags the items that are 
 	 * overdue. The items are ordered by the book call number.  If a subject is provided the report 
 	 * lists only books related to that subject, otherwise all the books that are out are listed by 
 	 * the report.
 	 * 
 	*/
 	
 	public static ArrayList<ArrayList<String>> borrowedItemsReport(String subject)
 	{
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		Statement  stmt;
 		ResultSet  rs;
 		
 		try 
 		{
 
 				stmt = con.createStatement();
 				
 				rs = stmt.executeQuery("SELECT * FROM Borrowing b0 WHERE b0.inDate is NULL ORDER BY b0.callNumber ASC");
 				
 				
 				/*
 				rs = stmt.executeQuery("SELECT DISTINCT o.callnumber, b1.title, b2.copyNo, o.outDate, o.inDate, b3.btype, o.borid "+
 										"FROM borrowing o, book b1, bookCopy b2, borrower b3, hasSubject s " +
 										"WHERE o.callNumber = b1.callNumber AND b1.callNumber = b2.callNumber "+
 										"AND s.subject LIKE '%"+subject+"%' AND o.bid = b3.bid AND o.inDate is NULL "+
 										"ORDER BY o.callNumber ASC");
 				*/
 			
 			
 			while(rs.next())
 			{
 				
 				Statement  stmtSubject = con.createStatement();
 				String callNumber = rs.getString(3);
 				ResultSet rsSubject = stmtSubject.executeQuery("SELECT * FROM Book b, hasSubject s WHERE b.callNumber = '"+callNumber+"'"+
						"AND s.subject LIKE '%"+ subject + "%'");
 				
 				int i = 0;
 				while (rsSubject.next()&&i<1)
 				{
 					String title = rsSubject.getString("title");
 					Statement  stmtBorrower = con.createStatement();
 					String bid = rs.getString(2);
 					ResultSet rsBorrower = stmtBorrower.executeQuery("SELECT * FROM Borrower WHERE bid = '"+bid+"'");
 					String borrowerType ="";
 					long dueDate = 0;
 					while(rsBorrower.next())
 					{
 						borrowerType = rsBorrower.getString(9);
 					}
 					
 					long outDate = Long.valueOf(rs.getString(5)).longValue();
 					/*  
 					boolean studentOverdue = borrowerType.toLowerCase().trim().equals("student") & 
 							  outDate + 2*604800 < CURRENT_TIME ; 
 					  
 					boolean staffOverdue = borrowerType.toLowerCase().trim().equals("staff") & 
 							  outDate + 6*604800 < CURRENT_TIME ; 
 					  
 					boolean facultyOverdue = borrowerType.toLowerCase().trim().equals("faculty") & 
 							  outDate + 12*604800 < CURRENT_TIME ; 
 					*/
 					
 					if (borrowerType.toLowerCase().trim().equals("student"))
 						dueDate = outDate + 2*604800;
 					
 					if (borrowerType.toLowerCase().trim().equals("staff"))
 						dueDate = outDate + 6*604800;
 					
 					if (borrowerType.toLowerCase().trim().equals("faculty"))
 						dueDate = outDate + 12*604800;
 					
 					
 					ArrayList<String> aBorrowing = new ArrayList<String>();
 					String overdue = "";
 					
 					if (dueDate < CURRENT_TIME)
 						overdue = "Item Overdue";
 					aBorrowing.add(rs.getString(3));	
 					aBorrowing.add(title);
 					aBorrowing.add(rs.getString(4));
 					aBorrowing.add(rs.getString(5));
 					aBorrowing.add(String.valueOf(dueDate));
 					aBorrowing.add(overdue);
 					result.add(aBorrowing);
 					i++;
 			}
 			}
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 }
