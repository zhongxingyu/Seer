 package driver;
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.DatabaseMetaData;
 
 public class DriverConnection {
 
 
 public static void main (String[] args) throws Exception {
 
 	// the sql string commands
 	String sDropTable = "DROP TABLE IF EXISTS dummy"; // this deletes the table if it already exists
 	String sMakeTable = "CREATE TABLE dummy (id numeric, response text)";
 	String sMakeInsert = "INSERT INTO dummy VALUES (1,'Hello from the database')";
 	String sMakeSelect = "SELECT response from dummy";
 	
 	
     String sDatabaseToUse = "hello.db";
     
 	
 	DbClass db = new DbClass(sDatabaseToUse);
 	try {
 		db.execute(sDropTable);
 		db.execute(sMakeTable);
 		db.execute(sMakeInsert);
 		
 		ResultSet rs = db.executeQuery(sMakeSelect);
 	try {
 		while(rs.next()) {
 			String sResult = rs.getString("response");
 			System.out.println(sResult);
 		}
 	} finally {
 		try { rs.close(); } catch (Exception ignore) {}
 	}
 	} finally {
 		try { db.closeConnection(); } catch (Exception ignore) {}
 }
 }
 }
 
