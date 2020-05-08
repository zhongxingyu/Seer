 /*****************************************************************************************
  * 
  * BooksCmd.java
  *  
  * Aaron Averill
  * John Lloyd
  * Matthew Marum
  * Rob Parsons
  * 
  * CSC 540
  * Fall 2012
  * 
  * BooksCmd contains the main that parses the command and for processing by the 
  * appropriate classes. The commands are sent to classes as specified in our
  * report 2 section 4.1 Tasks and Operations.
  * 
  */
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 
 public class BooksCmd {
 
 	private static final String jdbcURL = "jdbc:oracle:thin:@ora.csc.ncsu.edu:1521:orcl";
 	private static Connection connection = null;
 
 	// Put your oracle ID and password here
 	private static final String user = "jlloyd";
	private static final String password = "xxxxx";
 
 
 	private static enum Operations {BILLING, BOOK, CUSTOMER, PURCHASE, REPORT, SALE, STAFF, STOCKS, VENDOR};
 
 	public static void main(String[] args) {
 
 		// spit out parameters read
 		System.out.println("Command Received:");
 		for (int i=0;i<args.length;i++){
 			System.out.print(args[i]+" ");
 		}
 		System.out.println();System.out.println();
		
 		// We need at least one command
 		if (args.length < 1) {
 			usage();
 			System.exit(-1);
 		}
 
 		try {
 
 			try {
 				// Loading the driver. This creates an instance of the driver
 				// and calls the registerDriver method to make Oracle Thin
 				// driver, at ora.csc.ncsu.edu, available to clients. 
 				Class.forName("oracle.jdbc.driver.OracleDriver");
 
 				// Get a connection instance from the first driver in the 
 				// DriverManager list that recognizes the URL jdbcURL
 				connection = DriverManager.getConnection(jdbcURL, user, password);
 			} catch (Throwable oops) {
 				System.out.println("Error opening connection to database");
 				oops.printStackTrace();
 				System.exit(-1);
 			}
 
      if (invokeViaReflection(connection, args)) {
        return;
      }
      
 			// switch on the command type and then create a member of 
 			// that class to handle the request.
 			switch (Operations.valueOf(args[0].toUpperCase())) {
 
 			case BILLING:
 
 				Billing billing = new Billing(connection); 
 				if (billing.exec(args) == -1){
 					System.out.println("Invalid Billing request!");
 				}
 
 				break;
 
 			case REPORT:
 
 				Report rpt = new Report(connection); 
 				if (rpt.exec(args) == -1){
 					System.out.println("Invalid Report request!");
 				}
 
 				break;
				
 			} // switch
 
 		} catch (IllegalArgumentException e) {
 			usage();
      e.printStackTrace();
 		} catch(Throwable oops) {
 			// print a stack trace if something goes wrong
 			oops.printStackTrace();
 		} finally {
 			// always close the connection to the database
 			close(connection);
 		}
 
 	} // main
 
 
 	// Print the commands usage
 	private static void usage() {
 		System.out.println("Command Required. Legal values:");
 		for (Operations t : Operations.values()) {
 			System.out.println(t.toString());
 		}
 
 	} // usage
 
 
 	// Close the connection to the database if one if open
 	static void close(Connection connection) {
 		if(connection != null) {
 			try { 
 				connection.close(); 
 			} catch(Throwable whatever) {}
 		}
 
 	} // close
 
  static boolean invokeViaReflection(Connection connection, String[] args) throws Exception {
    ReflectionCommandInvoker invoker = new ReflectionCommandInvoker(connection);
    return invoker.execute(args);
  }
 
 } // BooksCmd
