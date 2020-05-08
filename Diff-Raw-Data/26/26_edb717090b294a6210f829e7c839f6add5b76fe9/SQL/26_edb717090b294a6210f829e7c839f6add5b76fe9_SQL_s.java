 package net.lala.CouponCodes.sql;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import net.lala.CouponCodes.CouponCodes;
 import net.lala.CouponCodes.api.SQLAPI;
 import net.lala.CouponCodes.api.events.EventHandle;
 import net.lala.CouponCodes.sql.options.DatabaseOptions;
 import net.lala.CouponCodes.sql.options.MySQLOptions;
 import net.lala.CouponCodes.sql.options.SQLiteOptions;
 
 /**
  * SQL.java - MySQL, SQL handling
  * @author mike101102
  */
 public class SQL extends SQLAPI {
 	
 	private DatabaseOptions dop;
 	
 	private Connection con;
 	
 	public SQL(CouponCodes plugin, DatabaseOptions dop) {
 		super(plugin);
 		this.dop = dop;
 		plugin.getDataFolder().mkdirs();
 		
 		if (dop instanceof SQLiteOptions) {
 			try {
 				((SQLiteOptions) dop).getSQLFile().createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public DatabaseOptions getDatabaseOptions() {
 		return dop;
 	}
 	
 	@Override
 	public Connection getConnection() {
 		return con;
 	}
 	
 	@Override
 	public boolean open() throws SQLException {
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			con = null;
 			return false;
 		}
 		if (dop instanceof MySQLOptions){
 			this.con = DriverManager.getConnection("jdbc:mysql://"+((MySQLOptions) dop).getHostname()+":"+
 					((MySQLOptions) dop).getPort()+"/"+
 					((MySQLOptions) dop).getDatabase(), 
 					((MySQLOptions) dop).getUsername(), 
 					((MySQLOptions) dop).getPassword());
 			EventHandle.callDatabaseOpenConnectionEvent(con, dop, true);
 			return true;
 		}
 		else if (dop instanceof SQLiteOptions) {
 			this.con = DriverManager.getConnection("jdbc:sqlite:"+((SQLiteOptions) dop).getSQLFile().getAbsolutePath());
 			EventHandle.callDatabaseOpenConnectionEvent(con, dop, true);
 			return true;
 		} else {
 			EventHandle.callDatabaseOpenConnectionEvent(con, dop, false);
 			return false;
 		}
 	}
 	
 	@Override
	public void close(boolean disable) throws SQLException {
 		con.close();
		if (disable) EventHandle.callDatabaseCloseConnectionEvent(con, dop);
 	}
 	
 	@Override
 	public boolean reload() throws SQLException {
 		con.close();
 		return open();
 	}
 	
 	@Override
 	public ResultSet query(String query) throws SQLException {
 		Statement st = null;
 		ResultSet rs = null;
 		
 		st = con.createStatement();
 		if (query.toLowerCase().contains("delete") || query.toLowerCase().contains("update")) {
 			st.executeUpdate(query);
 			EventHandle.callDatabaseQueryEvent(dop, query, rs);
 			return rs;
 		} else {
 			rs = st.executeQuery(query);
 			EventHandle.callDatabaseQueryEvent(dop, query, rs);
 			return rs;
 		}
 	}
 	
 	@Override
 	public boolean createTable(String table) throws SQLException {
 		Statement st = con.createStatement();
 		return st.execute(table);
 	}
 }
