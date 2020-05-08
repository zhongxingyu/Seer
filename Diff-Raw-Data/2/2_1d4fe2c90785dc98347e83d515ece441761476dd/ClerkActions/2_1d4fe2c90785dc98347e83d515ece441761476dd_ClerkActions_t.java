 package library;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 public class ClerkActions extends UserActions {
 
 	public ClerkActions(Connection c) {
 		super(c);
 	}
 
 	public void addBorrower(String bid, String password, String username, String phone,
 			String email, String sin, java.util.Date expiry, String type) {
 		try {
 			PreparedStatement ps = con.prepareStatement("INSERT INTO borrowers VALUES (?,?,?,?,?,?,?,?)");
 			ps.setInt(1, Integer.parseInt(bid));
 			ps.setString(2, password);
 			ps.setString(3, username);
 			ps.setString(4, phone);
 			ps.setString(4, email);
 			ps.setInt(5, Integer.parseInt(sin));
 			ps.setDate(6, new java.sql.Date(expiry.getTime()));
 			ps.setString(7, type);
 			
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 
 		} catch (SQLException ex) {
 			System.out.println("Message: " + ex.getMessage());
 			try {
 				// undo the insert
 				con.rollback();
 			} catch (SQLException ex2) {
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 	public void checkOut(String bid, List<String> callNos) {
 		for (String callNumber : callNos) {
 			try {
 				// check if this call number is available for borrowing
 				Statement statusSearch = con.createStatement();
 				ResultSet statusResult = statusSearch.executeQuery("SELECT copyNo, status FROM bookcopy WHERE callNumber = '" + callNumber + "'");
 				String status = statusResult.getString("status");
 				String copyNo = null;
 				if (status.equals("in")) {
 					copyNo = statusResult.getString("copyNo");
 				} else if (status.equals("on-hold")) {
 					// the book is on hold; check that the person who is borrowing has placed a hold on the book
 					Statement holdSearch = con.createStatement();
 					ResultSet holdResult = holdSearch.executeQuery("SELECT bid FROM HoldRequest WHERE callNumber = '" + callNumber + "' AND bid = " + bid);
					if (holdResult.next()) {
 						// the person has placed a hold on this book
 						copyNo = statusResult.getString("copyNo");
 					} else {
 						System.out.println("Borrower " + bid + " is trying to borrow a held book with call number " + callNumber + " without placing a hold first. Skipping...");
 						continue;
 					}
 				} else if (status.equals("out")) {
 					System.out.println("Book with call number " + callNumber + " already has all copies out. Skipping...");
 					continue;
 				}
 				// calculate the due date
 				Statement timeLimitSearch = con.createStatement();
 				ResultSet timeLimitResult = timeLimitSearch.executeQuery("SELECT bookTimeLimit FROM BorrowerType t, Borrower b WHERE b.bid = " + bid + " AND b.type = t.type");
 				GregorianCalendar cal = new GregorianCalendar();
 				cal.add(GregorianCalendar.DATE, timeLimitResult.getInt("bookTimeLimit"));
 
 				PreparedStatement ps = con.prepareStatement("INSERT INTO borrowing VALUES (borid_counter.nextval,?,?,?,?,?)");
 				ps.setInt(1, Integer.parseInt(bid));
 				ps.setString(2, callNumber);
 				ps.setString(3, copyNo);
 				ps.setDate(4, new java.sql.Date(new java.util.Date().getTime()));
 				ps.setDate(5, new java.sql.Date(cal.getTimeInMillis()));
 
 				ps.executeUpdate();
 				con.commit();
 				ps.close();
 				
 				// TODO prints a note with the items and their due day (which is given to the borrower).  
 			} catch (SQLException ex) {
 				System.out.println("Message: " + ex.getMessage());
 				try {
 					// undo the insert
 					con.rollback();
 				} catch (SQLException ex2) {
 					System.out.println("Message: " + ex2.getMessage());
 					System.exit(-1);
 				}
 			}
 		}
 	}
 
 	public void checkIn(String callNumber, String copyNo) {
 		try {
 			// check if item is late
 			PreparedStatement lateSearch = con.prepareStatement("SELECT bid, inDate FROM Borrowing WHERE callNumber = '" + callNumber + "' AND copyNo = '" + copyNo + "'");
 			ResultSet lateResult = lateSearch.executeQuery();
 			java.util.Date dueDate = lateResult.getDate("inDate");
 			if (new java.util.Date().after(dueDate)) {
 				// today is after the due date; item is overdue. Fine the borrower
 				PreparedStatement fine = con.prepareStatement("INSERT INTO fine VALUES (fineid_counter.nextval,?,?,NULL,?)");
 				// TODO calculate amount for fine
 				fine.setInt(1, 1);
 				fine.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
 				fine.setInt(3, lateResult.getInt("bid"));
 				
 				fine.executeUpdate();
 				con.commit();
 				fine.close();
 				
 			}
 			// set item as 'in'
 			PreparedStatement checkIn = con.prepareStatement("UPDATE BookCopy SET status = ? WHERE callNumber = ? AND copyNo = ?");
 			checkIn.setString(2, callNumber);
 			checkIn.setString(3, copyNo);
 			
 			
 			// check if hold for this item exists
 			// TODO modify query to return the earliest placed hold for the book
 			PreparedStatement holdSearch = con.prepareStatement("SELECT bid FROM HoldRequest WHERE callNumber = '" + callNumber + "'"); 
 			ResultSet holdResult = holdSearch.executeQuery();
 			if (holdResult.next()) {
 				checkIn.setString(1, "on-hold");
 			} else {
 				checkIn.setString(1, "in");
 			}
 			
 			checkIn.executeUpdate();
 			con.commit();
 			checkIn.close();
 
 		} catch (SQLException ex) {
 			System.out.println("Message: " + ex.getMessage());
 			try {
 				// undo the insert
 				con.rollback();
 			} catch (SQLException ex2) {
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	public void listOverdue() {
 		try {
 			PreparedStatement search = con.prepareStatement("SELECT * FROM borrowing WHERE ? > inDate");
 			search.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
 			ResultSet searchResult = search.executeQuery();
 			// TODO display data in a table
 			
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
