 package ut.veeb.lambiprojekt123;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.hsqldb.jdbc.JDBCDriver;
 
 public class DataBase {
 	public static final String DRIVER = "org.hsqldb.jdbc.JDBCDriver";
 	public static JDBCDriver instance = null;
 	public static final String PROTOCOL = "jdbc:hsqldb:mem:myDb";
 	
 	public synchronized static void ensure() {
 		if (instance == null) {
 			try {
 				instance = (JDBCDriver) Class.forName(DRIVER).newInstance();
 				Connection conn = getConnection();
 				if (!tableExists(conn, "Candidates")) {
 					Statement sta = conn.createStatement();
 					sta.executeUpdate("CREATE TABLE Candidates ("
 							+ "first_name VARCHAR(100)," // eesnimi
 							+ "last_name VARCHAR(100)," // pereknimi
 							+ "candidate_id INTEGER," //kandidaadi number
							+ "id INTEGER," //isikukood
 							+ "party VARCHAR(100)," //partei nimi
 							+ "county VARCHAR(100))"); //maakond
 					sta.executeUpdate("INSERT INTO Candidates (first_name, last_name, candidate_id, id, party, county) VALUES('Eduard', 'Ekskavaator',1, 38908120987, 'SINISED', 'TARTUMAA')");
 					sta.close();
 				}
 				conn.close();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static boolean tableExists(Connection conn, String name) {
 		ResultSet tables;
 		try {
 			tables = conn.getMetaData().getTables(conn.getCatalog(), null, "%", null);
 			while(tables.next()) {
 				if (tables.getString(3).equalsIgnoreCase(name)) {
 					return true;
 				}
 			}
 		} catch (SQLException e) {
 			return false;
 		}
 		
 		return false;
 	}
 	
 	public static Connection getConnection() throws SQLException {
 		return instance.connect(PROTOCOL, null);
 	}
 }
