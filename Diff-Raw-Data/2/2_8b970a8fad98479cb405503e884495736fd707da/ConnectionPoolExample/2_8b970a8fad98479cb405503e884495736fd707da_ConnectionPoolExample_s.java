 package com.opower.util.powerpool;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.junit.Assert;
 
 public class ConnectionPoolExample {
 
 	
 	public static void doSomethingWithDatabase(Connection connection) throws SQLException {
 		Statement stmt = connection.createStatement();
         
         stmt.addBatch("CREATE TABLE world (hello VARCHAR(100))");
         stmt.addBatch("INSERT INTO world (hello) VALUES ('hello world')");
         
         stmt.executeBatch();
         
         ResultSet set = stmt.executeQuery("SELECT hello FROM world");
  
         while (set.next()) {
         	System.out.println(set.getString("hello")); 
         }
         
 
         stmt.execute("DROP TABLE world");
          
         
 	}
 	/**
 	 * @param args
 	 * @throws SQLException 
 	 * @throws ClassNotFoundException 
 	 */
 	public static void main(String[] args) throws ClassNotFoundException, SQLException {
 	 
 		SimpleConnectionPool pool = (args != null && args.length >= 4) ?
 
 		 		SimpleConnectionPool.createDefaultPool(args[0],args[1], args[2], args[3])
 		 	:
 		 		
 		 		SimpleConnectionPool.createDefaultPool("org.hsqldb.jdbc.JDBCDriver",
 				"jdbc:hsqldb:mem:power-test", "sa", "");
 		
		Connection connection = pool.getConnection();
 		
 		// do some stuff with the connection...
 		doSomethingWithDatabase(connection);
 		
 		pool.releaseConnection(connection);	// or, you can call connection.close();
 		
 		pool.stop();
 		
 		
 
 	}
 	
 }
