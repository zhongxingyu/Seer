 package ms.utils.server.database;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.List;
 
 import ms.domain.Auftrag;
 import ms.domain.ImportMedium;
 import ms.utils.log.server.ServerLog;
 
 import org.apache.log4j.Logger;
 
 public class SqlDbAdapter {
 
 	static private Connection connection;
 	static private String database = "jdbc:sqlite:db/db.sqlite";
 
 	public static Connection getConnection() throws SQLException {
 		Logger logger = ServerLog.getLogger();
 		try {
 			if (connection == null || connection.isClosed()) {
 				Class.forName("org.sqlite.JDBC");
 				connection = DriverManager.getConnection(database);
 				DatabaseMetaData metaData = connection.getMetaData();
 				logger.info("sqlite driver mode: "
 						+ metaData.getDriverVersion());
 				logger.info("database driver: " + metaData.getDriverName());
 			}
 		} catch (ClassNotFoundException e) {
 			logger.fatal("JDBC Driver nof found");
 			e.printStackTrace();
 		}
 		return connection;
 	}
 
 	public static List<Auftrag> getOrderList() {
 		return getAuftragList();
 	}
 
 	public static List<Auftrag> getAuftragList() {
 		String sql = "select * from Auftrag";
 		List<Auftrag> myList = SqlDbConnection.getObjectList(sql,
 				Auftrag.class);
 		if (myList.isEmpty())
 			return null;
 		else
 			return myList;
 	}
 
 	public static Auftrag getAuftrag(int AuftragId) {
 		String sql = "select * from Auftrag WHERE id = " + AuftragId;
 		List<Auftrag> myList = SqlDbConnection.getObjectList(sql, Auftrag.class);
 		if (myList.isEmpty())
 			return null;
 		else
 			return myList.get(0);
 	}
 
 
	//TODO: Fix this test!
 	public static int saveAuftrag(Auftrag myAuftrag) {
 		int id;
 		try {
 			String sql = "select * from Auftrag where id = " + myAuftrag.getID();
 			if(SqlDbConnection.getObjectList(sql, Auftrag.class).isEmpty()) {
 				id = SqlDbConnection.executeInsert("insert into Auftrag (status) values (?)", Integer.toString(myAuftrag.getStatus()));
 				myAuftrag.setID(id);
 				return id;
 			} else {
 				SqlDbConnection.execute("UPDATE Auftrag SET status = ? WHERE id = ?", Integer.toString(myAuftrag.getStatus()), Integer.toString(myAuftrag.getID()));
 			}
 		} catch (SQLException e) {
 			System.err.println(e);
 		}
 		return -1;
 	}
 	
 	public static boolean deleteAuftrag(Auftrag myAuftrag) {
 		try {
 			String sql = "select * from Auftrag where id = " + myAuftrag.getID();
 			if(SqlDbConnection.getObjectList(sql, Auftrag.class).isEmpty()) {
 				return false;
 			} else {
 				SqlDbConnection.execute("DELETE FROM Auftrag WHERE id=?;", Integer.toString(myAuftrag.getID()));
 				return true;
 			}
 		} catch (SQLException e) {
 			System.err.println(e);
 			return false;
 		}
 	}
 	
 	public static List<ImportMedium> getImportMediumList() {
 		String sql = "select * from ImportMedium";
 		List<ImportMedium> myList = SqlDbConnection.getObjectList(sql,
 				ImportMedium.class);
 		if (myList.isEmpty())
 			return null;
 		else
 			return myList;
 	}
 
 	public static ImportMedium getImportMediumList(int ImportMediumId) {
 		String sql = "select * from ImportMedium where id = " + ImportMediumId;
 		List<ImportMedium> myList = SqlDbConnection.getObjectList(sql,
 				ImportMedium.class);
 		if (myList.isEmpty())
 			return null;
 		else
 			return myList.get(0);
 	}
 
 	public static List<ImportMedium> getImportMediumList(
 			ImportMedium myMediensammlung) {
 		String sql = "select * from ImportMedium where fk_Mediensammlung = "
 				+ myMediensammlung.getID();
 		List<ImportMedium> myList = SqlDbConnection.getObjectList(sql,
 				ImportMedium.class);
 		if (myList.isEmpty())
 			return null;
 		else
 			return myList;
 	}
 	
 	public static int saveImportMedium(ImportMedium myMedium) {
 		int id;
 		try {
 			String sql = "select * from ImportMedium where id = " + myMedium.getID();
 			if(SqlDbConnection.getObjectList(sql, Auftrag.class).isEmpty()) {
 				id = SqlDbConnection.executeInsert("insert into ImportMedium (status, name) values (?, ?)", Integer.toString(myMedium.getID()), myMedium.getName());
 				myMedium.setId(id);
 				return id;
 			} else {
 				SqlDbConnection.execute("UPDATE ImportMedium SET status = ?, name = ? WHERE id = ?", Integer.toString(myMedium.getStatus()), myMedium.getName(), Integer.toString(myMedium.getID()));
 			}
 		} catch (SQLException e) {
 			System.err.println(e);
 		}
 		return -1;
 	}
 	
 	
 }
