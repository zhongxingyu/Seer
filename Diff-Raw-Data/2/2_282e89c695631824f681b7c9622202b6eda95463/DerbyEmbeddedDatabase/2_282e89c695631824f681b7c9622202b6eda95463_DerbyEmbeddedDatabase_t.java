 package org.oobium.persist.db.derby.embedded;
 
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.sql.ConnectionPoolDataSource;
 
 import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
 import org.oobium.persist.db.Database;
 import org.oobium.utils.FileUtils;
 
 public class DerbyEmbeddedDatabase extends Database {
 
 	public DerbyEmbeddedDatabase(String client, Map<String, Object> properties) {
 		super(client, properties);
 	}
 
 	@Override
 	protected Map<String, Object> initProperties(Map<String, Object> properties) {
 		Map<String, Object> props = new HashMap<String, Object>(properties);
 		
 		if(coerce(properties.get("memory"), boolean .class)) {
 			if(props.get("database") == null) {
 				props.put("database", client);
 			}
 		} else {
 			Object o = properties.get("database");
 			if(!(o instanceof String)) {
 				o = ".." + File.separator + "data" + File.separator + client;
 			}
 			File file = new File((String) o);
 			if(!file.isAbsolute()) {
 				file = new File(System.getProperty("user.dir"), (String) o);
 			}
 			try {
 				props.put("database", file.getCanonicalPath());
 			} catch(IOException e) {
 				logger.error(e);
 			}
 		}
 
 		if(props.get("username") == null) {
 			props.put("username", "root");
 		}
 		if(props.get("password") == null) {
 			props.put("password", "");
 		}
 		return props;
 	}
 
 	@Override
 	protected ConnectionPoolDataSource createDataSource() {
 		EmbeddedConnectionPoolDataSource ds = new EmbeddedConnectionPoolDataSource();
 		if(inMemory()) {
 			ds.setDatabaseName("memory:" + properties.get("database"));
 		} else {
 			ds.setDatabaseName((String) properties.get("database"));
 		}
 		ds.setUser((String) properties.get("username"));
 		ds.setPassword((String) properties.get("password"));
 		return ds;
 	}
 
 	public boolean inMemory() {
 		return coerce(properties.get("memory"), false);
 	}
 	
 	@Override
 	protected void createDatabase() throws SQLException {
         try {
 			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
 		} catch(ClassNotFoundException e) {
 			logger.error(e);
 		}
 
 		String database = (String) properties.get("database");
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("jdbc:derby:");
 		if(inMemory()) {
 			sb.append("memory:");
 		}
 		sb.append(database);
 		sb.append(";create=true;");
 		sb.append("user=\"").append(properties.get("username")).append("\";password=\"").append(properties.get("password")).append('"');
 
 		String dbURL = sb.toString();
 		if(logger.isLoggingDebug()) {
 			logger.debug("create connection: " + dbURL);
 		}
 		
     	Connection connection = DriverManager.getConnection(dbURL);
         Statement s = connection.createStatement();
         try {
         	String sql = "SET SCHEMA APP";
         	logger.debug(sql);
 	        s.executeUpdate(sql);
         	sql = 
 	        	"CREATE PROCEDURE APP.CHECK_UNIQUE(tableName VARCHAR(128), columnName VARCHAR(128), id INTEGER) " +
 	        	"PARAMETER STYLE JAVA " +
 	        	"READS SQL DATA " +
 	        	"LANGUAGE JAVA " +
 	        	"EXTERNAL NAME '" + UniqueColumnTrigger.class.getCanonicalName() + ".checkUnique'";
         	logger.debug(sql);
 	        s.executeUpdate(sql);
         } finally {
         	try {
         		s.close();
         	} catch(SQLException e) {
         		// discard
         	}
         }
 	}
 
 	@Override
 	protected void dropDatabase() throws SQLException {
 		if(inMemory()) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("jdbc:derby:memory:");
 			sb.append(properties.get("database"));
 			sb.append(";drop=true;");
 			sb.append("user=\"").append(properties.get("username")).append("\";password=\"").append(properties.get("password")).append('"');
 
 			String dbURL = sb.toString();
 			if(logger.isLoggingDebug()) {
 				logger.debug("drop database: " + dbURL);
 			}
 
 			try {
 				DriverManager.getConnection(dbURL);
 			} catch(SQLException e) {
 				if(!e.getSQLState().equals("08006")) { // 08006 indicates success
 					throw e;
 				}
 			}
 		} else {
 			File db = new File(File.separator + properties.get("database"));
 			if(logger.isLoggingDebug()) {
 				logger.debug("drop database: " + db);
 			}
 			dispose();
 			if(db.exists()) {
 				FileUtils.delete(db);
 			} else {
 				throw new SQLException("database does not exist: " + db);
 			}
 		}
 	}
 
 	@Override
 	protected String getDatabaseIdentifier() {
 		EmbeddedConnectionPoolDataSource ds = (EmbeddedConnectionPoolDataSource) getDataSource();
 		if(ds != null) {
 			return ds.getDatabaseName();
 		} else {
 			return "";
 		}
 	}
 
 }
