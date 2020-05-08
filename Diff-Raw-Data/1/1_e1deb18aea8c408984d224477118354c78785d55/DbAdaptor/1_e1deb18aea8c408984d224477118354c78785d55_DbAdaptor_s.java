 package db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Properties;
 
 public class DbAdaptor {
 	
 	private final static String URL = "jdbc:postgresql://db:5432/g1236218_u";
 	private final static String USER = "g1236218_u";
 	private final static String PASSWORD = "RLTn4ViKks";
 	
 	public static Connection connect() throws ClassNotFoundException, SQLException{
 		Class.forName("org.postgresql.Driver");
 		Properties properties = new Properties();
 		properties.setProperty("user", USER);
 		properties.setProperty("password", PASSWORD);
 		Connection conn = DriverManager.getConnection(URL, properties);
		conn.prepareStatement("SET search_path TO john;").execute();
 		return conn;
 	}
 	
 }
