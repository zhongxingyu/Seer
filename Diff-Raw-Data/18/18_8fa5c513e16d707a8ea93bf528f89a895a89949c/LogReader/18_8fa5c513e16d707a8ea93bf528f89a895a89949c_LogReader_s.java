 import java.io.*;
 import java.util.Scanner;
 import java.sql.*;
 import java.util.regex.*;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import java.sql.SQLException;
 
 /**
  * OpenTTD-palvelinlogin parsija
  */
 public class LogReader {
 
     private String db_url;
     private Properties db_config = new Properties();
     private static ArrayList<LineParser> lineParsers =
 	new ArrayList<LineParser>();
     private int maxReconnects;
     private String sqlInitQuery;
     
     static {
 	// Some stateful parsers for statistics headings
 	AnnualHeadingParser annualHeading = new AnnualHeadingParser();
 	QuarterHeadingParser quarterHeading = new QuarterHeadingParser();
 	
 	// Add all line parsers to this arraylist in the order they
 	// should be tried. If one doesn't match, trying the next one.
 	lineParsers.add(new JoinParser());
 	lineParsers.add(new LeaveParser());
 	lineParsers.add(new MsgParser());
 	lineParsers.add(annualHeading);
 	lineParsers.add(quarterHeading);
 	lineParsers.add(new AnnualStatsParser(annualHeading));
 	lineParsers.add(new QuarterStatsParser(quarterHeading));
     }
     
     /**
      * Dynamic thingies to be done once per instance
      */
     public LogReader() throws Exception {
 	
 	final String db_file = "database.conf";
 
 	// Some default values
 	this.db_config.setProperty("allowMultiQueries","true");
 
 	// Reading config (overriding defaults if needed)
 	this.db_config.load(new InputStreamReader(new FileInputStream(db_file), "UTF-8"));
 
 	// Building URI for database
 	this.db_url = "jdbc:mysql://" + db_config.getProperty("hostname") + 
 	    "/" + db_config.getProperty("database");
 
 	// We are using database.conf for own configuration, too.
 	// TODO Maybe this should be in different Properties to avoid name clash. 
 	this.maxReconnects = Integer.parseInt(db_config.getProperty("max_reconnects"));
 	
 	// Constructing SQL line for default values
 	int cur_game = Integer.parseInt(db_config.getProperty("game_id"));
 	this.sqlInitQuery = "SET @cur_game = "+cur_game+";";
     }
 
     /**
      * Gives a new connection statement. Establishes a new connection to the
      * database.
      */
     public Statement newConnection() throws SQLException {
 	Connection conn = DriverManager.getConnection(db_url,db_config);
 	Statement stmt = conn.createStatement();
	stmt.executeUpdate(sqlInitQuery); // set some defaults
 	return stmt;
     }
     
     public void processStream(InputStream stream)
 	throws Exception {
 
 	SQLBuilder sqlstr = new SQLBuilder();
 
 	// messages need to be feeded to this program with tail -f 
 	Scanner scanner = new Scanner(stream, "UTF-8");
 	String line = "";
 	SQLBuilder sqlLine = new SQLBuilder();
 
 	Statement stmt = newConnection();
 	int reconnectsLeft = maxReconnects;
 	System.out.println("Got a connection to the database");
 
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
 
 		// Quietly skip a line if no parser matches this line.
 		if ( thisParser == null) continue;
 
 		System.out.println("Inserted " + thisParser.parserName());
 
 		sqlLine.clear();
 		thisParser.appendSQL(sqlLine);
 
 		if (sqlLine.isEmpty()) continue; // No SQL to add.
 
 		// Trying to put lines to the database
 		try {
		    stmt.executeUpdate(sqlLine.toString());
 		} catch (SQLException sql_e) {
 		    System.err.println("Database connection has been lost, reconnecting.");
 		    
 		    while (reconnectsLeft != 0) {
 			try {
 			    stmt = newConnection();
 			    // If no Exception occurs, jump out.
 			    break;
 			} catch(Exception e) {
 			    reconnectsLeft--;
 			    System.err.println("Failed to connect the database.");
 			    
 			    // If it fails too much
 			    if (reconnectsLeft == 0) throw e;
 			}
 		    }
 		    
 		    // Got a new connection
 		    System.err.println("Got a new connection to the database.");
 		    reconnectsLeft = maxReconnects;
		    stmt.executeUpdate(sqlLine.toString());
 		}   
 	    } catch (Exception e) {
 		System.err.println("\nContent: "+line);
 		throw e;
 	    }
 	}
     }
 
     /**
      * Main.
      */
     public static void main(String args[]) throws Exception {
 	
 	LogReader me = new LogReader();
 
 	try {
 	    me.processStream(System.in);
 	} catch (NoSuchElementException foo) {
 	    System.err.println("End of stream has been reached. If you "+
 			       "want to run this parser continuously,\n"+
 			       "try $ tail -n 0 -f filename | java LogReader");
 	}
     }
 }
