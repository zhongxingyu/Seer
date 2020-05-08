 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 public class BorrowerTable {
 
 	private static Connection con;
 
 	private static final String[] attNames = 
 		{"bid", "password", "name", "address", "phone", "email", "sinOrStNo","expiryDate","type"};
 
 	private static final int bidRequiredLength = 10;
 
 	private static final int bidMinLength = 0;
 	// our format for dates
 	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
 	
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
 				throw new IllegalArgumentException("Invalid SIN or Student Number, we're seeing you entered "+ sinOrStNo);
 			else
 			{
 				int s = Integer.parseInt(sinOrStNo);
 				ps.setInt(6, s);
 			}
 
 			//Set Expiry Date
 			int currentUnixTime = (int) (System.currentTimeMillis() / 1000L);
 			if ( Integer.parseInt(expiryDate) < currentUnixTime)
 				throw new IllegalArgumentException("The expirary date must be after the current date, current date is " +currentUnixTime);
 			if (!expiryDate.matches("^\\d*$")||expiryDate.equals(""))
 				throw new IllegalArgumentException("Needs to be UNIX time bro, the current UNIX time is "+ currentUnixTime);
 			else
 			{
 				int d = Integer.parseInt(expiryDate);
 				ps.setInt(7, d);
 			}
 
 			//Set Type
 			String lc_type = type.trim().toLowerCase();
 			if (! ((lc_type.equals("faculty"))||(lc_type.equals("staff"))||(lc_type.equals("student"))))
 				throw new IllegalArgumentException("Invalid Borrower Type, types can be faculty, staff, or student");
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
 
 	
 	
 
 	
 	
 	/**
 	 * Checks all borrowing, for given bid, then for each transaction, is there a fine in the finetable?
 	 * @param bid
 	 * @return
 	 */
 	// Set paid date to current date, where null
 	public static ArrayList<ArrayList<String>> checkFinesExist (String bid){
 		Statement fineCheck;
 		ResultSet fineCheckRS;
 		Statement borCheck;
 		ResultSet borCheckRS;
 		ArrayList<String> borrowingID = new ArrayList<String>();
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		if(bid.length() > bidRequiredLength || bid.length() < bidMinLength ) throw new IllegalArgumentException("Bid needs to be at least "+bidMinLength+" numbers long, and at most "+bidRequiredLength);
 		try{
 			con = db_helper.connect("ora_i7f7", "a71163091");
 
 			borCheck = con.createStatement();
 			//Get borid's that bid is associated with
 			 borCheckRS = borCheck.executeQuery("SELECT * FROM Borrowing WHERE bid = " + bid);
 
 			while (borCheckRS.next()){
 				borrowingID.add(borCheckRS.getString("borid"));
 			}
 			 
 			/*
 			 * For each borid, get 
 			 */
 			for (String transaction : borrowingID){
 				fineCheck = con.createStatement();
 				fineCheckRS = fineCheck.executeQuery("SELECT * FROM Fine WHERE boridid = " + transaction +" AND PAIDDATE IS NULL");
 				if (fineCheckRS.next()){
 					//add it to the list of fines in result
 					String amount = fineCheckRS.getString("amount");
 					String issuedDate = fineCheckRS.getString("issueddate");
 					String paidDate = fineCheckRS.getString("paiddate");
 
 					//TODO: If it can get working, uncomment the other stuff
 					Date readableIssueDate = new Date(Long.parseLong(issuedDate));
 					Date readablePaidDate;
 
 					if (paidDate != null){
 						readablePaidDate = new Date(Long.parseLong(paidDate));
 						paidDate = readablePaidDate.toString();
 					}
 					else paidDate = "";
 
 					ArrayList<String> oneFine = new ArrayList<String>();
 					oneFine.add(0, amount);
 					oneFine.add(1, readableIssueDate.toString());
 					oneFine.add(2, paidDate);
 					result.add(oneFine);
 				}
 			}
 
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	static boolean insertPaidDate(String fid, String date) throws IllegalArgumentException{
 		ResultSet fineCheckRS;
 		if(date.matches("^\\d*$")) throw new IllegalArgumentException("Date must be a in a format like: YYYY-MM-DD");
 		try{
 			Statement fineCheck = con.createStatement();
 			con = db_helper.connect("ora_i7f7", "a71163091");
 			//Convert the date to unix time
 			java.util.Date convertedDate = sdf.parse(date);
 			long time = convertedDate.getTime();
 			//TODO: Is this inserting? Its not seeminly finishing.
 			fineCheckRS = fineCheck.executeQuery("UPDATE Fine SET paidDate = '" +time+"' WHERE fid = '" +fid+ "' ");
 			con.commit();
 			return true;
 		}catch (Exception e){
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 
 	static boolean checkHoldExists(String callNo){		
 		Statement holdCheck;
 		ResultSet holdCheckRS;
 		boolean holdExists = false;
 		try{
 			con = db_helper.connect("ora_i7f7", "a71163091");
 
 			holdCheck = con.createStatement();
 			holdCheckRS = holdCheck.executeQuery("SELECT * FROM HoldRequest WHERE callNumber = '" + callNo + "'");
 
 			if (holdCheckRS.next()){
 				//if there is a next row at all, that is, if there exists a hold request
 				holdExists = true;
 			}
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		return holdExists;
 	}
 
 	/*
 	 * Processes a book return. 0 is returned if the book is returned on time. 1 is returned if a fine was applied.
 	 * Return -1 if the book was never checked out.
 	 */
 	static int processReturn(String callNo, String copyNo)
 	{
 		long curTime = System.currentTimeMillis()/1000;
 		long outDate = 0;
 		long dueDate = 0;
 
 		try
 		{	
 			con = db_helper.connect("ora_i7f7", "a71163091");
 
 			//check that the book is even out for this user, this callno/copyno combination
 			Statement check;
 			ResultSet checkRS;
 			check = con.createStatement();
 			checkRS = check.executeQuery("SELECT * FROM borrowing WHERE " +
 					"copyno = '" + copyNo + "' AND callnumber = '" + callNo + "' AND inDate IS NULL");
 			if (!checkRS.next()) return -1; //book was never checked out for this user
 
 			check = con.createStatement();
 			checkRS = check.executeQuery("SELECT * FROM borrowing WHERE " +
 					"copyno = '" + copyNo + "' AND callnumber = '" + callNo + "' AND inDate IS NULL");
 
 			String bid = "";
 			String borid = "";
 			while (checkRS.next()){
 				borid = checkRS.getString("borid");
 				bid = checkRS.getString("bid");
 				outDate = Long.parseLong(checkRS.getString("outdate"));
 			}
 			//there is a checkout entry, so now we need the indate to check when it was taken out and decide if its overdue
 
 			//get bid type
 			Statement typeCheck;
 			ResultSet typeCheckRS;
 			typeCheck = con.createStatement();
 			typeCheckRS = check.executeQuery("SELECT btype FROM borrower WHERE bid = '" + bid + "'");
 
 			//have their type now, now compute due dates
 			String btype = "";
 			while (typeCheckRS.next()) btype = typeCheckRS.getString("btype");
 
 			if (btype.toLowerCase().trim().equals("student")) dueDate = outDate + 2*604800;
 			if (btype.toLowerCase().trim().equals("faculty")) dueDate = outDate + 6*604800;
 			if (btype.toLowerCase().trim().equals("staff")) dueDate = outDate + 12*604800;
 
 			System.out.println(btype + " " + curTime + " " + dueDate + " " + outDate);
 
 
 			if (curTime > dueDate){
 				//the book is overdue, we need to apply a fine
 				PreparedStatement fine;
 				fine = con.prepareStatement("INSERT INTO FINE (amount, issueddate, paiddate, boridid) VALUES " +
 						"(?,?,?,?)");
 
 				fine.setString(1, "5");
 				fine.setString(2, String.valueOf(curTime));
 				fine.setString(3, null);
 				fine.setString(4, borid);
 
 				fine.execute();
 				con.commit();
 
 			}
 
 			//finally, we set the status of the book, from out, to in
 
 			Statement updateStatement;
 			updateStatement = con.createStatement();
 			updateStatement.executeUpdate("UPDATE bookcopy SET status = 'in' WHERE callnumber = '" + callNo + "' AND copyno = '" + copyNo + "'");
 
 			Statement inUpdate;
 			inUpdate = con.createStatement();
 			inUpdate.executeUpdate("UPDATE borrowing SET indate = '" + curTime + "' WHERE callnumber = '" + callNo + "' AND copyno = '" + copyNo + "'");
 
 		} catch (Exception e){e.printStackTrace();}
 		if (curTime > dueDate) return 1;
 		else return 0;
 
 
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
 
 	// Check his/her account. The system will display the items the borrower has currently borrowed 
 	// 		and not yet returned, any outstanding fines and the hold requests that have been placed by the borrower.
 
 	public static ArrayList<ArrayList<String>> checkOut(String borrowerID)
 	{
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		int bid = Integer.parseInt(borrowerID);
 		ResultSet outrs;
 		Statement outCheck;
 		ArrayList<ArrayList<String>> outs = new ArrayList<ArrayList<String>>();
 
 		try{
 
 			outCheck = con.createStatement();
 
 			outrs = outCheck.executeQuery("SELECT callNumber, title, mainAuthor AS author FROM Book b WHERE EXISTS "
 					+ "(SELECT * FROM Borrowing c WHERE "
					+ "c.bid = '" + bid + "' AND c.callNumber = b.callNumber AND c.inDate IS NULL)");
 
 			int i = 0;
 
 			while(outrs.next())
 			{
 				ArrayList<String> elem = new ArrayList<String>();
 				elem.add(0, outrs.getString("callNumber"));
 				elem.add(1, outrs.getString("title"));
 				elem.add(2, outrs.getString("author"));
 				outs.add(i, elem);
 			}
 
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 
 		return outs;
 	}
 
 	public static ArrayList<ArrayList<String>> checkFines(String borrowerID)
 	{
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		int bid = Integer.parseInt(borrowerID);
 		ResultSet finers;
 		Statement fineCheck;
 		ArrayList<ArrayList<String>> fines = new ArrayList<ArrayList<String>>();
 
 		try{
 
 			fineCheck = con.createStatement();
 
 			finers = fineCheck.executeQuery("SELECT * FROM Fine f WHERE paidDate IS NULL AND EXISTS (SELECT * FROM Borrowing b WHERE b.bid = '" + bid + "' AND b.borid = f.boridID)");
 			int i = 0;
 			while(finers.next())
 			{
 				ArrayList<String> elem = new ArrayList<String>();
 				elem.add(0, finers.getString("amount"));
 				elem.add(1, finers.getString("issuedDate"));
 				fines.add(i, elem);
 			}
 
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 
 		return fines;
 	}
 
 	public static ArrayList<ArrayList<String>> checkHolds(String borrowerID)
 	{
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		int bid = Integer.parseInt(borrowerID);
 		ResultSet holdrs;
 		Statement holdCheck;
 		ArrayList<ArrayList<String>> holds = new ArrayList<ArrayList<String>>();
 
 		try{
 
 			holdCheck = con.createStatement();
 
 			holdrs = holdCheck.executeQuery("SELECT h.callNumber, h.issuedDate, b.title FROM " +
 					"HoldRequest h, Book b WHERE " +
 					"h.callNumber = b.callNumber and h.bid = " + bid);
 			int i = 0;
 			while(holdrs.next())
 			{
 				ArrayList<String> elem = new ArrayList<String>();
 				elem.add(0, holdrs.getString("callNumber"));
 				elem.add(1, holdrs.getString("title"));
 				elem.add(2, holdrs.getString("issuedDate"));
 				holds.add(i, elem);
 			}
 
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 
 		return holds;
 	}
 }
