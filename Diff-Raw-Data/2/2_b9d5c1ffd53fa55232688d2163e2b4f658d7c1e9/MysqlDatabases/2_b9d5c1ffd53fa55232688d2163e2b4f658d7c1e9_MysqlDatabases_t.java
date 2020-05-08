 package examples.util;
 
 import javax.sql.DataSource;
 
 import nl.pojoquery.DB;
 
 import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
 
 public class MysqlDatabases {
 
 	public static DataSource createDatabase(String host, String schema, String username, String password) {
 		DataSource db = getDataSource("jdbc:mysql://" + host, username, password);
 		DB.executeDDL(db, "DROP DATABASE IF EXISTS " + schema);
 		DB.executeDDL(db, "CREATE DATABASE " + schema + " DEFAULT CHARSET utf8");
 
		return getDataSource("jdbc:mysql://" + host + "/" + schema, username, password);
 	}
 	
 	private static DataSource getDataSource(String jdbcUrl, String user, String pass) {
 		try {
 			MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
 			dataSource.setUrl(jdbcUrl);
 			dataSource.setUser(user);
 			dataSource.setPassword(pass);
 			dataSource.setCharacterEncoding("utf8");
 			dataSource.setUseUnicode(true);
 			return dataSource;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
