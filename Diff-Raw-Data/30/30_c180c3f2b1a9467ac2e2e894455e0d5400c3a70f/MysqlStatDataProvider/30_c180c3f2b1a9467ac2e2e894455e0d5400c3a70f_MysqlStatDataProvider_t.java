 package com.tehbeard.BeardStat.DataProviders;
 
 import java.sql.SQLException;
 
 public class MysqlStatDataProvider extends JDBCStatDataProvider {
 
 	public MysqlStatDataProvider(String host,int port,String database,String tablePrefix,String username,String password) throws SQLException {
 		
 		super("com.mysql.jdbc.Driver");
 		tblConfig.put("TBL_ENTITY", tablePrefix + "_entity");
 		tblConfig.put("TBL_KEYSTORE", tablePrefix + "_keystore");
 		
 		connectionUrl =  String.format("jdbc:mysql://%s:%s/%s",
 				host,
 				port,
 				database);
 		connectionProperties.put("user",username);
 		connectionProperties.put("password",password);
 		connectionProperties.put("autoReconnect", "true");
 		
		initialise("sql/maintenence/create.tables.sql");
 	}
 
 }
