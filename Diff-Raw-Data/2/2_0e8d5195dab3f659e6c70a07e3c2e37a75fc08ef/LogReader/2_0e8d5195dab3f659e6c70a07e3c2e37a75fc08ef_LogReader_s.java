 import java.io.*;
 import java.util.Scanner;
 import java.sql.*;
 import java.util.regex.*;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 
 /**
  * OpenTTD-palvelinlogin parsija
  */
 public class LogReader {
 
     public static void main(String args[]) throws Exception {
 	
 	Properties config = new Properties();
 
 	// Some default values
 	config.setProperty("autoReconnect","true");
 	config.setProperty("allowMultiQueries","true");
 
 	// Reading config (overriding defaults if needed)
 	config.load(new InputStreamReader(new FileInputStream("database.conf"),
 					  "UTF-8"));
 
 	// Building URI for database
 	String db_url = "jdbc:mysql://" + config.getProperty("hostname") + 
 	    "/" + config.getProperty("database");
 	Connection conn = DriverManager.getConnection(db_url,config);
 	Statement stmt = conn.createStatement();
 	
	System.out.println("Got connection to the database");
 
 	try {
 	    processStream(System.in, stmt);
 	} catch (NoSuchElementException foo) {
 	    System.err.println("End of stream has been reached. If you "+
 			       "want to run this parser continuously,\n"+
 			       "try $ tail -n 0 -f filename | java LogReader");
 	}
     }
 
     public static void processStream(InputStream stream, Statement stmt)
 	throws Exception {
 
 	SQLBuilder sqlstr = new SQLBuilder();
 
 	// messages need to be feeded to this program with tail -f 
 	Scanner scanner = new Scanner(stream, "UTF-8");
 	String line = "";
 	SQLBuilder sqlLine = new SQLBuilder();
 
 	ArrayList<LineParser> lineParsers = new ArrayList<LineParser>();
 
 	// Add all line parsers to this arraylist in the order they
 	// should be tried. If one doesn't match, trying the next one.
 	lineParsers.add(new JoinParser());
 	lineParsers.add(new LeaveParser());
 	lineParsers.add(new MsgParser());
 	lineParsers.add(new CompanyParser());
 
 	// Neverending loop. Waiting new lines forever.
 	while (true) {
 	    try {
 		line = scanner.nextLine();
 		
 		LineParser thisParser = null;
 
 		// Looking for a good parser
 		for ( LineParser cur : lineParsers ) {
 		    if (cur.match(line)) {
 			thisParser = cur;
 			break;
 		    }
 		}
 
 		if ( thisParser == null) continue; // Quietly skip a line.
 
 		System.out.println("Inserted " + thisParser.parserName());
 
 		sqlLine.clear();
 		thisParser.appendSQL(sqlLine);
 		stmt.executeUpdate(sqlLine.toString());
 		
 	    } catch (Exception e) {
 		System.err.println("\nContent: "+line);
 		throw e;
 	    }
 	}
     }
 }
