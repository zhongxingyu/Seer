 package org.morningcoffee.powerteam.server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import com.google.gson.*;
 
 
 public class DBLogger {
 
	private String dbURL = "jdbc:derby:" + System.getProperty("user.dir") + "/db;create=true";
     private Connection conn = null;
     private Statement stmt = null;
 	
 	public void createConnection()
     {
         try
         {
             Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
             conn = DriverManager.getConnection(dbURL, "user", "user"); 
         }
         catch (Exception e)
         {
         	System.out.println("\nCannot connect to DB\n");
     		System.exit(-1);
         }
     }
 	
 	public void closeConnection()
     {
         try
         {
             if (stmt != null)
                 stmt.close();
             if (conn != null)
             {
                 DriverManager.getConnection(dbURL + ";shutdown=true");
                 conn.close();
             }           
         }
         catch (SQLException e)
         {
         	System.out.println("\nCannot close connection to DB\n");
     		System.exit(-2);
         }
     }
 	
 	public void addLog(String data)
     {
 		LogRequest rq = new Gson().fromJson(data, LogRequest.class);
 		String sqlReq = "";
		if(rq.type.equals("plugin"))
			sqlReq = String.format("INSERT INTO APP.PLUGINLOGS (start_time, end_time, test_result, user_id) values " +
					"(%d, %d, '%s', (SELECT USER_ID FROM APP.USERS WHERE USER_NAME = '%s'))", 
					rq.startTime, rq.endTime, rq.testResult, rq.userName);
		else sqlReq = String.format("INSERT INTO APP.CLIENTLOGS (hash, date, user_id) values ('%s', %d, " +
				"(SELECT USER_ID FROM APP.USERS WHERE USER_NAME = '%s'))", rq.hash, rq.date, rq.userName);
 		
         try
         {
             stmt = conn.createStatement();
             stmt.execute(sqlReq);
             stmt.close();
         }
         catch (SQLException sqlExcept)
         {
         	System.out.println("\nCannot write log to DB\n");
     		System.exit(-3);
         }
     }
 }
