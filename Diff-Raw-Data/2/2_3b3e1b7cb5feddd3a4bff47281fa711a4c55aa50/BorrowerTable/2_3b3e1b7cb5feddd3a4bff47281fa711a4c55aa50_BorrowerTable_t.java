 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 
 public class BorrowerTable {
 	
 	private static Connection con;
 	
 	private static final String[] attNames = 
 		{"bid", "password", "name", "address", "phone", "email", "sinOrStNo","expiryDate","type"};
 	
 	
 	//Insert a borrower into the table
 	//if successful, returns the borrower id, otherwise -1
 	public static int insertBorrower(String password, String name, String address, 
 									  String phone, String email, String sinOrStNo, String expiryDate, 
 									  String type) throws IllegalArgumentException
     {
 		int borid = -1;
 
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		PreparedStatement  ps;
 		  
 		try
 		{
 			  ps = con.prepareStatement("INSERT INTO borrower " +
 			  		"(password, name, address, phone, emailAddress, sinOrStNo, expiryDate, btype) " +
 			  		"VALUES (?,?,?,?,?,?,?,?)");
 			
 			  //Set bid
 			  //ps.setString(1, bid);
 			  
 			  //Set password
 			  if (!((password.matches(".*\\d.*"))&&(!password.matches("^\\d*$"))))
 				  throw new IllegalArgumentException("Password must contain letters and numbers");
 			  if (!((password.length()>=7)&& (password.length())<=13))
 				  throw new IllegalArgumentException("Password must be between 8 and 12 characters");
 			  else
 				  ps.setString(1, password.toString());
 			  
 			  //Set name
 			  if (name.equals(""))
 				  throw new IllegalArgumentException("Invalid name");
 			  else
 				  ps.setString(2,name);
 			  
 			  //Set address
 			  if (address.equals(""))
 				  ps.setNull(3, java.sql.Types.VARCHAR);
 			  else
 				  ps.setString(3,address);
 			  
 			  //Set phone
 			  if (phone.equals(""))
 				  ps.setNull(4, java.sql.Types.INTEGER);  
 			  else if (!phone.matches("^\\d*$"))
 				  throw new IllegalArgumentException("Invalid phone number");
 			  else
 			  {
 				  int p = Integer.parseInt(phone);
 				  ps.setInt(4, p);
 			  }
 			  //Set email
 			  if (!email.matches(".*@.*"))
 				  throw new IllegalArgumentException("Invalid email address");
 			  else
 				  ps.setString(5, email);
 			  
 			  //Set sinOrstNo
 			  if ((!sinOrStNo.matches("^\\d*$"))||sinOrStNo.equals(""))
 				  throw new IllegalArgumentException("Invalid SIN or Student Number");
 			  else
 			  {
 				  int s = Integer.parseInt(sinOrStNo);
 					  ps.setInt(6, s);
 			  }
 			  
 			  //Set Expiry Date
 			  if (!expiryDate.matches("^\\d*$")||expiryDate.equals(""))
 				  throw new IllegalArgumentException("Needs to be UNIX time bro");
 			  else
 			  {
 				  int d = Integer.parseInt(expiryDate);
 				  ps.setInt(7, d);
 			  }
 			  
 			  //Set Type
 			  String lc_type = type.trim().toLowerCase();
 			  if (! ((lc_type.equals("faculty"))||(lc_type.equals("staff"))||(lc_type.equals("student"))))
 				  throw new IllegalArgumentException("Invalid Borrower Type");
 			  else
 				  ps.setString(8, type);
 			  
 			  System.out.println(ps);
 			  
 			  ps.executeUpdate();
 			  ps.close();
 			  
 			  Statement s = con.createStatement();
 			  ResultSet rs2 = s.executeQuery("SELECT bid FROM borrower WHERE password = '" + password + "' AND emailaddress = '" + email + "'");
 			  while (rs2.next()) borid = Integer.parseInt(rs2.getString("bid"));
 	
 			  // commit work 
 			  con.commit();
 		
 			}
 			catch (SQLException ex)
 			{
 			    System.out.println("Message: " + ex.getMessage());
 			    try 
 			    {
 					// undo the insert
 					con.rollback();	
 			    }
 			    catch (SQLException ex2)
 			    {
 					System.out.println("Message: " + ex2.getMessage());
 					System.exit(-1);
 			    }
 			}
 		return borid;
 	}
 	
 		//Display all borrowers in the database
 		public static ArrayList<ArrayList<String>> showBorrowers()
 	    {
 	
 			Statement  stmt;
 			ResultSet  rs;
 			
 			ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		   
 		try
 		{
 		  stmt = con.createStatement();
 	
 		  rs = stmt.executeQuery("SELECT * FROM borrowers");
 	
 		  // get info on ResultSet
 		  ResultSetMetaData rsmd = rs.getMetaData();
 	
 		  // get number of columns
 		  int numCols = rsmd.getColumnCount();
 	
 	
 		  while(rs.next())
 		  {
 		      // for display purposes get everything from Oracle 
 		      // as a string
 	
 		      // simplified output formatting; truncation may occur
 			  
 			  ArrayList<String> aBorrower = new ArrayList<String>();
 	
 			  for (String anAttribute: attNames)
 				  aBorrower.add(rs.getString(anAttribute));
 		      
 			  result.add(aBorrower);
 	
 	
 		  }
 	 
 		  // close the statement; 
 		  // the ResultSet will also be closed
 		  stmt.close();
 		  
 		}
 		catch (SQLException ex)
 		{
 		    System.out.println("Message: " + ex.getMessage());
 		}	
 		return result;
 	    }
 		
 	/*
 	 * Processes a book return. 0 is returned if the book is applied on time. 1 is returned if a fine was applied.
 	 */
 	static int processReturn(String callNo, String copyNo)
 	{
 
 		long inDate = 0;
 		int borid = 0;
 		long curTime = System.currentTimeMillis()/1000;
 		try
 		{
 			ResultSet rs;
 			Statement s2;
 			PreparedStatement  ps;
 			PreparedStatement ps3;
 			
 			con = db_helper.connect("ora_i7f7", "a71163091");
 
 			ps3 = con.prepareStatement("UPDATE BOOKCOPY SET STATUS = ? WHERE CALLNUMBER = ? AND COPYNO = ?");
 			ps3.setString(1, "in");
 			ps3.setString(2, callNo);
 			ps3.setString(3, copyNo);
 			ps3.executeUpdate();
 			
   			s2 = con.createStatement();
 			rs = s2.executeQuery("SELECT * FROM Borrowing WHERE callNumber = " + callNo + " AND copyNo = " + copyNo);
 						
 			while(rs.next())
 			{
 				inDate = Long.valueOf(rs.getString("inDate")).longValue();
 				borid = Integer.parseInt(rs.getString("borid"));
 			}
 			
 			if(curTime > inDate)
 			{
 				ps = con.prepareStatement("INSERT INTO Fine (amount, issuedDate, paidDate, boridId)" + 
 						"VALUES (?,?,?,?)");
 				ps.setInt(1, 5);
 				
 				ps.setString(2, String.valueOf(curTime));
 				
 				ps.setNull(3, java.sql.Types.VARCHAR);
 				
 				ps.setInt(4, borid);
 				
 				ps.executeUpdate();
 				con.commit();	
 				con.close();
 				
 				return 1;
 			}
  
 
 		} catch (Exception e){e.printStackTrace();}
 		return 0;
 
 		
 	}
 	public static int placeHold(String callNumber, String bid) throws IllegalArgumentException
 	{
 		int hid = -1;
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		ResultSet rs;
 		Statement stmt;
 		PreparedStatement  ps;
 		long date = System.currentTimeMillis()/1000;
 		
 		try{
 			stmt = con.createStatement();
 			rs = stmt.executeQuery("SELECT COUNT(*) AS numin FROM BookCopy WHERE callNumber = '" + callNumber + "' AND status = 'in'");
 			rs.next();
 			if(rs.getInt("numin") > 0)
 			{
 				throw new IllegalArgumentException("There are currently copies of the book in");
 			}
 			rs = stmt.executeQuery("SELECT Count(*) AS tSize FROM HoldRequest");
 			rs.next();
 			hid = rs.getInt("tSize") + 1;
 			ps = con.prepareStatement("INSERT INTO HoldRequest VALUES (?,?,?,?)");
 			ps.setString(1, String.valueOf(hid));
			ps.setString(2, String.valueOf(bid));
 			ps.setString(3, callNumber);
 			ps.setString(4, String.valueOf(date));
 			ps.executeUpdate();
 		}catch(SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return hid;
 	}
 }
