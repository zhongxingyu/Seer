 package com.tehbeard.BeardStat.DataProviders;
 
 import java.sql.SQLException;
 
 import com.tehbeard.BeardStat.BeardStat;
 
 public class SQLiteStatDataProvider extends JDBCStatDataProvider {
 
 	public SQLiteStatDataProvider(String filename) throws SQLException {
 		
 		super("org.sqlite.JDBC");
 		
 		tblConfig.put("TBL_ENTITY", "entity");
 		tblConfig.put("TBL_KEYSTORE","keystore");
 		
 		connectionUrl = String.format("jdbc:sqlite:%s",filename);
 		
		initialise();
 		
 		saveEntityData = conn.prepareStatement(BeardStat.self().readSQL("sql/load/saveStat.sqlite.sql", tblConfig));
 	}
 
 }
