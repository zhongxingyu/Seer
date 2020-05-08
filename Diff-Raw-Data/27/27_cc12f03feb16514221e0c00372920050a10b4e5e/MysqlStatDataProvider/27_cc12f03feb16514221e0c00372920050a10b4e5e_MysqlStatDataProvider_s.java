 package com.tehbeard.BeardStat.DataProviders;
 
 import java.sql.SQLException;
 
 public class MysqlStatDataProvider extends JDBCStatDataProvider {
 
 	public MysqlStatDataProvider(String host,int port,String database,String tablePrefix,String username,String password) throws SQLException {
 		
 		super("sql","com.mysql.jdbc.Driver");
		tblPrefix = tablePrefix;
 
 		connectionUrl =  String.format("jdbc:mysql://%s:%s/%s",
 				host,
 				port,
 				database);
 		connectionProperties.put("user",username);
 		connectionProperties.put("password",password);
 		connectionProperties.put("autoReconnect", "true");
 		
 		initialise();
 	}
 
 }
