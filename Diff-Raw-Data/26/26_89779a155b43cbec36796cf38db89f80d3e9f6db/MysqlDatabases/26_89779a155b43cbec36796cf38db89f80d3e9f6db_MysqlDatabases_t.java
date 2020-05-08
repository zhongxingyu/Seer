 package nl.pojoquery.integrationtest;
 
 import javax.sql.DataSource;
 
 import nl.pojoquery.DB;
 
 import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
 
 public class MysqlDatabases {
 
 	public static DataSource createDatabase(String host, String schema, String username, String password) {
		DataSource db = getMysqlDataSource(host, null, username, password);

 		DB.executeDDL(db, "DROP DATABASE IF EXISTS " + schema);
 		DB.executeDDL(db, "CREATE DATABASE " + schema + " DEFAULT CHARSET utf8");
 
		return getMysqlDataSource(host, schema, username, password);
	}
	
	public static DataSource getMysqlDataSource(String host, String schema, String username, String password) {
		String jdbcUrl = "jdbc:mysql://" + host + (schema != null ? "/" + schema : "");
		return getDataSource(jdbcUrl, username, password);
 	}
 	
	public static DataSource getDataSource(String jdbcUrl, String user, String pass) {
 		try {
 			MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
 			dataSource.setUrl(jdbcUrl);
 			dataSource.setUser(user);
 			dataSource.setPassword(pass);
 			dataSource.setCharacterEncoding("utf8");
 			dataSource.setUseUnicode(true);
 			dataSource.setSessionVariables("storage_engine=InnoDB");
 			return dataSource;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
