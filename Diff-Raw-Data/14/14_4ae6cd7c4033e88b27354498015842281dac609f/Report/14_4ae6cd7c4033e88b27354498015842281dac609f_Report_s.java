 /*****************************************************************************************
  * 
  * Report.java
  *  
  * Aaron Averill
  * John Lloyd
  * Matthew Marum
  * Rob Parsons
  * 
  * CSC 540
  * Fall 2012
  * 
  * Report.java creates the reports that:
  * 	Report the purchase history for a given customer and for a certain time period 
  * 	Return information on all the customers a given salesperson assisted during a certain time period
  * 	Return information on all the vendors a particular store has contract with.
  * 	Return information on Books-A-Thousand staff grouped by their role.
  * 
  * 
 Example CLI:
 
books report purchases 2001 24-dec-2011 22-oct-2012
books report assistance 1001 24-dec-2011 22-oct-2012
books report contracts
books report roles Salesperson
 
 -- Staff (id, name, gender, dob, jobTitle, department, salary, phone, address)
 -- Customer (id, gender, dob, status, phone, ssn, name, address)
 -- Vendor (id, phone, name, address)
 -- Book (id, retailPrice, stockQuantity, title, author)
 -- Orders (id, staffId, customerId, status, orderDate)
 -- ItemOrder (orderId, bookId, salePrice, quantity)
 -- Purchase (id, orderDate, bookId, vendorId, staffId, quantity, status, wholesalePrice) 
 -- Stocks (bookId, vendorId)
 
 -- Report the purchase history for a given customer and for a certain time period (day/month/year)
 -- The constants used for customerId, and orderDate range will be replaced by user entered values.
 
 SELECT co.customerId, co.orderDate, bk.title, co.salePrice, co.quantity FROM Book bk, (SELECT DISTINCT * FROM Orders NATURAL JOIN ItemOrder WHERE orderId=id AND customerId=2001) co WHERE bk.id=co.bookId AND (co.orderDate >= '24-dec-2011' AND co.orderDate <= '22-oct-2012');
 
 -- Return information on all the customers a given salesperson assisted during a certain time period
 -- The constants used for staffId, and orderDate range will be replaced by user entered values.
 
 SELECT id, name, address, gender, dob, status, phone, ssn FROM Customer, (SELECT DISTINCT customerId cid FROM Orders WHERE staffId=1001 AND (orderDate >= '24-dec-2011' AND orderDate <= '22-oct-2012')) WHERE id=cid;
 
 -- Return information on all the vendors a particular store has contract with.
 SELECT DISTINCT * FROM Vendor;
 
 -- Return information on Books-A-Thousand staff grouped by their role.
 SELECT * FROM Staff WHERE jobTitle='Salesperson';
 SELECT * FROM Staff WHERE jobTitle='Procurement';
 SELECT * FROM Staff WHERE jobTitle='Warehouse staff';
 SELECT * FROM Staff WHERE jobTitle='Accounting';
 SELECT * FROM Staff WHERE jobTitle='Management';
 
  */
 
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 public class Report {
 
 	private Connection connection = null;
 	private Statement statement = null;
 	private ResultSet result = null;
 
 	private static enum ReportCmds {ASSISTANCE, CONTRACTS, PURCHASES, ROLES};
 
 	// Constructor
 	Report(Connection connection){
 
 		this.connection = connection;
 
 		try {
 
 			// Create a statement instance that will be sending
 			// your SQL statements to the DBMS
 			this.statement = connection.createStatement();
 
 		} catch(Throwable oops) {
 			oops.printStackTrace();
 		}
 
 	}
 
 	/*
 	 * Method: exec
 	 * 
 	 * Determine report type requested
 	 * 
 	 * Input:
 	 * args[0] = "report"
 	 * args[1] = "purchases" | "assistance" | "contracts" | "roles"
 	 * 
 	 * Returns:
 	 * -1 = unknown report request
 	 */
 
 	public int exec(String[] args){
 
 		if (args.length < 2){
 			usage();
 			return -1;
 		}
 
 		try {
 			switch (ReportCmds.valueOf(args[1].toUpperCase())) {
 			case ASSISTANCE:
 				System.out.println("Processing \"report assistance\" request");
 				return salespersonAssist(args);
 
 
 			case CONTRACTS:
 
 				System.out.println("Processing \"report contracts\" request");
 				return vendorContracts(args);
 
 			case PURCHASES:
 				System.out.println("Processing \"report purchases\" request");
 				return purchaseHistory(args);
 
 			case ROLES:
 
 				System.out.println("Processing \"report roles\" request");
 				return staffRoles(args);
 
 			} // switch
 
 		} catch (IllegalArgumentException e) {
 			usage();
 			return -1;
 		}
 		
 		return 0;
 		
 	} 
 
 	private static void usage() {
 		System.out.println("Subcommand Required. Legal values:");
 		for (ReportCmds t : ReportCmds.values()) {
 			System.out.println(t.toString());
 		}
 	}
 
 	/*
 	 * Method: purchaseHistory
 	 * 
 	 * Report the purchase history for a given customer and for a certain time period (day/month/year)
 	 * The constants used for customerId, and orderDate range will be replaced by user entered values.
 	 * 
 	 * Input:
 	 * args[2] = Customer ID
 	 * args[3] = Beginning date of date range
 	 * args[4] = Ending date of date range
 	 * 
 	 * Output:
 	 * Print report
 	 */
 
 	private int purchaseHistory(String[] args){
 
 		// Create a hash map
 		//Map<String, String> args = new HashMap<String, String>();
 		//m.put(args1);
 
 		String s1 = "SELECT co.customerId, co.orderDate, bk.title, co.salePrice, co.quantity FROM Book bk, (SELECT DISTINCT * FROM Orders NATURAL JOIN ItemOrder WHERE orderId=id AND customerId=";
 		// s2 = Customer ID like 2001
 		String s3 = ") co WHERE bk.id=co.bookId AND (co.orderDate >= '";
 		// s4 = Beginning date of date range like 24-dec-2011
 		String s5 = "' AND co.orderDate <= '";
 		// s6 = Ending data of date range like 22-oct-2012
 		String s7 = "')";
 
 		String q = s1+args[2]+s3+args[3]+s5+args[4]+s7;
 
 		// Test
 		System.out.println(q);
 
 		try {
 
 			try {
 
 				// Create a statement instance that will be sending
 				// your SQL statements to the DBMS
 				statement = connection.createStatement();
 
 				result = statement.executeQuery(q);
 
 				System.out.println(); // skip a line
 				while (result.next()) {
 
 					int cid = result.getInt("customerId");
 					String ord = result.getString("orderDate");
 					String tit = result.getString("title");
 					String sal = result.getString("salePrice");
 					//int ret = result.getInt("retailPrice");
 					int qua = result.getInt("quantity");
 
 					System.out.println(cid + " " + ord + " " + tit + " " + sal + " " + qua);
 					//System.out.println(tit);
 				}
 
 			} finally {
 				close(result);
 				close(statement);
 			}
 
 		} catch(Throwable oops) {
 			oops.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	/*
 	 * Method: salespersonAssist
 	 * 
 	 * Return information on all the customers a given salesperson assisted during a certain time period.
 	 * 
 	 * Input:
 	 * args[2] = Staff ID
 	 * args[3] = Beginning date of date range
 	 * args[4] = Ending date of date range
 	 * 
 	 * Output:
 	 * Print report
 	 */
 
 	private int salespersonAssist(String[] args){
 
 		String s1 = "SELECT id, name, address, gender, dob, status, phone, ssn FROM Customer, (SELECT DISTINCT customerId cid FROM Orders WHERE staffId=";
 		// s2 = Staff ID like 1001
 		String s3 = " AND (orderDate >= '";
 		// s4 = Beginning date of date range like 24-dec-2011
 		String s5 = "' AND orderDate <= '";
 		// s6 = Ending data of date range like 22-oct-2012
 		String s7 = "')) WHERE id=cid";
 
 		String q = s1+args[2]+s3+args[3]+s5+args[4]+s7;
 
 		// Test
 		System.out.println(q);
 
 		try {
 
 			try {
 
 				// Create a statement instance that will be sending
 				// your SQL statements to the DBMS
 				statement = connection.createStatement();
 
 				result = statement.executeQuery(q);
 
 				System.out.println(); // skip a line
 				while (result.next()) {
 
 					//String ord = result.getString("orderDate");
 					//int sid = result.getInt("staffId");
 					String snm = result.getString("name");
 					//int cid = result.getInt("cid");
 					//String cnm = result.getString("cname");
 
 					//System.out.println(ord + " " + sid + "  " + snm + "  " +  cid + "  " + cnm);
 					System.out.println(snm);
 				}
 
 			} finally {
 				close(result);
 				close(statement);
 			}
 
 		} catch(Throwable oops) {
 			oops.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	/*
 	 * Method: vendorContracts
 	 * 
 	 * Return information on all the vendors a particular store has contract with.
 	 * 
 	 * Input:
 	 * None
 	 * 
 	 * Output:
 	 * Print report
 	 */
 
 	private int vendorContracts(String[] args){
 
 		String s1 = "SELECT DISTINCT * FROM Vendor";
 
 		String q = s1;
 
 		// Test
 		System.out.println(q);
 
 		try {
 
 			try {
 
 				// Create a statement instance that will be sending
 				// your SQL statements to the DBMS
 				statement = connection.createStatement();
 
 				result = statement.executeQuery(q);
 
 				System.out.println(); // skip a line
 				while (result.next()) {
 
 					int id = result.getInt("id");
 					String pho = result.getString("phone");
 					String nam = result.getString("name");
 					String add = result.getString("address");
 
 					System.out.println(id + "  " + pho + " " + nam + "  " + add);
 				}
 
 			} finally {
 				close(result);
 				close(statement);
 			}
 
 		} catch(Throwable oops) {
 			oops.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	/*
 	 * Method: staffRoles
 	 * 
 	 * Return information on Books-A-Thousand staff grouped by their role.
 	 * 
 	 * Input:
 	 * args[2] = "Salesperson" | "Procurement" | "Warehouse staff" | "Accounting" | "Management"
 	 * 
 	 * Output:
 	 * Print report
 	 */
 
 	private int staffRoles(String[] args){
 
		String s1 = "SELECT * FROM Staff WHERE jobTitle=";
 		// s2 = Staff role
		String s3 = "')";
 
 		String q = s1+args[2]+s3;
 
 		// Test
 		System.out.println(q);
 
 		try {
 
 			try {
 
 				// Create a statement instance that will be sending
 				// your SQL statements to the DBMS
 				statement = connection.createStatement();
 
 				result = statement.executeQuery(q);
 
 				System.out.println(); // skip a line
 				while (result.next()) {
 
 					int id = result.getInt("id");
 					String nam = result.getString("name");
 					String gen = result.getString("gender");
 					String dat = result.getString("dob");
 					String job = result.getString("jobTitle");
 					String dep = result.getString("department");
 					int sal = result.getInt("salary");
 					String pho = result.getString("phone");
 					String add = result.getString("address");
 
 					System.out.println(id + "  " + nam + " " + gen + " " + dat + "  " + job + " " + dep + " " + sal + " " + pho + " " + add);
 				}
 
 			} finally {
 				close(result);
 				close(statement);
 			}
 
 		} catch(Throwable oops) {
 			oops.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	static void close(Statement statement) {
 		if(statement != null) {
 			try { 
 				statement.close(); 
 			} catch(Throwable whatever) {}
 		}
 	}
 
 	static void close(ResultSet result) {
 		if(result != null) {
 			try { 
 				result.close(); 
 			} catch(Throwable whatever) {}
 		}
 	}
 }
