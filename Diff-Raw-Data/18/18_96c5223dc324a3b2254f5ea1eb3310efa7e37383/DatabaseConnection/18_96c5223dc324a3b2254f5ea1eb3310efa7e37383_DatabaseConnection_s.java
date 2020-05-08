 package persistency;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import utils.ArrayUtils;
 import utils.StringUtils;
 
 public class DatabaseConnection {
 
 	private Connection conn;
 	private Statement stmt;
 
 	public DatabaseConnection(String dbFile) {
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			this.conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			this.stmt = conn.createStatement();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public int create(String table, String[] columns, String[] values) {
 		String[] cols = ArrayUtils.wrapElementsWith(columns, "\"");
 		String[] vals = ArrayUtils.wrapElementsWith(values, "\"");
 		final String query = String
 				.format("insert into %s (%s) values (%s)", table,
 						StringUtils.join(cols, ','),
 						StringUtils.join(vals, ','));
 		int id = -1;
 		try {
 			this.execUpdate(query);
 
 			// Get the id of the inserted row
 			ResultSet rs = this.execQuery("select last_insert_rowid()");
 			if (rs.next()) {
 				id = rs.getInt("last_insert_rowid()");
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return id;
 	}
 
 	public ResultSet execQuery(String query) {
 //		System.out.println(query);
 		try {
 			return this.stmt.executeQuery(query);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public int execUpdate(String query)  {
 //		System.out.println(query);
 		try {
 			return this.stmt.executeUpdate(query);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return -1;
 	}
	
 	public ResultSet readSpecific(String table, String key, String value){
 		String query = "SELECT * FROM " + table + " WHERE " + key + "='" + value +"'";
 		return this.execQuery(query);
 	}
 
 	public ResultSet readByID(String table, int id)  {
 //		String query = String.format("select * from %s where id=%d", table, id);
 //		return this.execQuery(query);
 		return this.readSpecific(table, "id", String.valueOf(id));
 	}
 
 	public ResultSet readAll(String table)  {
 		String query = String.format("select * from %s", table);
 		return this.execQuery(query);
 	}
 }
