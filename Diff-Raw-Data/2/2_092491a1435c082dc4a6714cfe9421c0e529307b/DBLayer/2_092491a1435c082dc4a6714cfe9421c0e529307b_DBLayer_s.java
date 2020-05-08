 package key.model;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 
 public class DBLayer {
 
	private final static String DB_URL_TEMPLATE = "jdbc:sqlserver://%s;databaseName='%s';";
 
 	private final static String SQL_CREATE_SET = 
 			"CREATE TABLE SETS ( " 
 			+ "ID INTEGER IDENTITY PRIMARY KEY CLUSTERED, "
 			+ "DATETIME_CREATED DATETIME, " 
 			+ "COMMENT VARCHAR(255) NOT NULL, " 
 			+ "DATETIME_FROM DATETIME, " 
 			+ "DATETIME_TO DATETIME)";
 
 	private final static String SQL_CREATE_KEY = 
 			"CREATE TABLE KEYS ( "
 			+ "SET_ID BIGINT NOT NULL, " 
 			+ "KEY_VALUE VARCHAR(255))"; 
 
 	private final static String SQL_ADD_SET = 
             "INSERT INTO SETS (DATETIME_CREATED, COMMENT, DATETIME_FROM, DATETIME_TO) "
                     + "values (?,?,?,?)";
 
 	private final static String SQL_ADD_KEY = 
             "INSERT INTO KEYS (SET_ID, KEY_VALUE) "
                     + "values (?,?)";
 
     private static final String SQL_GET_SETS = "SELECT "
             + "ID, DATETIME_CREATED, COMMENT, DATETIME_FROM, DATETIME_TO "
             + "FROM SETS";
 
     private static final String SQL_FILTER_SETS = "SELECT "
             + "SETS.ID, SETS.DATETIME_CREATED, SETS.COMMENT, SETS.DATETIME_FROM, SETS.DATETIME_TO, KEYS.KEY_VALUE "
             + "FROM KEYS "
             + "INNER JOIN SETS ON ID=KEYS.SET_ID "
     		+ "WHERE KEYS.KEY_VALUE LIKE '%%%s%%'";
 
     private static final String SQL_GET_KEYS = "SELECT "
             + "KEY_VALUE "
             + "FROM KEYS WHERE SET_ID=";
 
     private static final String SQL_GET_KEY = "SELECT "
             + "SET_ID "
             + "FROM KEYS WHERE KEY_VALUE='%s'";
 
     private static final String SQL_DELETE_SET = "DELETE FROM SETS "
             + "WHERE ID=";
 
     private static final String SQL_DELETE_KEYS = "DELETE FROM KEYS "
             + "WHERE SET_ID=";
 
     private static final String SQL_DELETE_KEY = "DELETE FROM KEYS "
             + "WHERE KEY_VALUE='%s'";
 
     private static Connection connection;
 
 	private DBLayer() {
 	}
 
 	public static final void create() {
 		try {
 			getConnection().createStatement().executeUpdate(SQL_CREATE_SET);
 			getConnection().createStatement().executeUpdate(SQL_CREATE_KEY);
 		} catch (SQLException e) {
 			throw new KeyException("  ", e);
 		}
 	}
 
 	static final int save(SetData setData) {
 		try {
 			PreparedStatement ps = getConnection().prepareStatement(SQL_ADD_SET, Statement.RETURN_GENERATED_KEYS);
 			ps.setDate(1, new java.sql.Date(setData.getCreated().getTime()));
 			ps.setString(2, setData.getComment());
 			ps.setDate(3, new java.sql.Date(setData.getFrom().getTime()));
 			ps.setDate(4, new java.sql.Date(setData.getTo().getTime()));
 			ps.executeUpdate();
 			ResultSet rs = ps.getGeneratedKeys();
 			if (rs.next()) {
 				int id = rs.getInt(1);
 				for (KeyData key : setData.getKeys()) {
 					ps = getConnection().prepareStatement(SQL_ADD_KEY);
 					ps.setInt(1, id);
 					ps.setString(2, key.getKey());
 					ps.executeUpdate();
 				}
 				return id;
 			}
 		} catch (SQLException e) {
 			throw new KeyException("   ", e);
 		}
 		return -1;
 	}
 
 	static final Collection<SetData> load() {
 		List<SetData> sets = new ArrayList<SetData>();
 		try {
 			ResultSet rs = getConnection().createStatement().executeQuery(SQL_GET_SETS);
 			while (rs.next()) {
 				sets.add(new SetData(
 						rs.getInt(1),
 						rs.getDate(2),
 						rs.getString(3),
 						rs.getDate(4),
 						rs.getDate(5)));
 			}
 			for (SetData setData : sets) {
 				rs = getConnection().createStatement().executeQuery(SQL_GET_KEYS + setData.getId());
 				while (rs.next()) {
 					setData.addKey(rs.getString(1));
 				}
 			}
 		} catch (SQLException e) {
 			throw new KeyException("   ", e);
 		}
 		return sets;
 	}
 
 	static final Collection<SetData> load(String filter) {
 		List<SetData> sets = new ArrayList<SetData>();
 		try {
 			ResultSet rs = getConnection().createStatement().executeQuery(String.format(SQL_FILTER_SETS, filter));
 			SetData setData = null;
 			while (rs.next()) {
 				int id = rs.getInt(1);
 				if (null == setData || id != setData.getId()) {
 					setData = new SetData(
 						rs.getInt(1),
 						rs.getDate(2),
 						rs.getString(3),
 						rs.getDate(4),
 						rs.getDate(5));
 					sets.add(setData);
 				}
 				setData.addKey(rs.getString(6));
 			}
 		} catch (SQLException e) {
 			throw new KeyException("   ", e);
 		}
 		return sets;
 	}
 
 	static final boolean isKeyExists(String key) {
 		try {
 			ResultSet rs = getConnection().createStatement().executeQuery(String.format(SQL_GET_KEY, key));
 			return rs.next();
 		} catch (SQLException e) {
 			throw new KeyException("   ", e);
 		}
 	}
 
 	static final void deleteSet(int setId) {
 		try {
 			getConnection().createStatement().executeUpdate(SQL_DELETE_SET + setId);
 			getConnection().createStatement().executeUpdate(SQL_DELETE_KEYS + setId);
 		} catch (SQLException e) {
 			throw new KeyException("     ", e);
 		}
 	}
 
 	static final void deleteKey(String key) {
 		try {
 			getConnection().createStatement().executeUpdate(String.format(SQL_DELETE_KEY, key));
 		} catch (SQLException e) {
 			throw new KeyException("     ", e);
 		}
 	}
 
 	private static final Connection getConnection() {
 		if (null == connection) {
 			try {
 				String username = KeyProperties.getDbUsername();
 				String password = KeyProperties.getDbPassword();
 				connection = DriverManager.getConnection(
 						String.format(DB_URL_TEMPLATE, KeyProperties.getDbHost(), KeyProperties.getDbDatabase()),
 						username.isEmpty() ? null : username,
 						password.isEmpty() ? null : password);
 			} catch (Exception e) {
 				throw new KeyException("    ", e);
 			}
 		}
 		return connection;
 	}
 
 }
