 package com.schneenet.minecraft.waypoints.storage;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.sql.DataSource;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.sqlite.SQLiteDataSource;
 
 import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
 
 public class WaypointStorageSQL implements WaypointStorage {
 	
 	private static final String SQL_WAYPOINT_ADD = "INSERT INTO WpWaypoints (name, owner, description, world, loc_x, loc_y, loc_z) VALUES (?,?,?,?,?,?,?)";
 	private static final String SQL_WAYPOINT_EDIT = "UPDATE WpWaypoints SET description = ?, loc_x = ?, loc_y = ?, loc_z = ? WHERE name = ?";
 	private static final String SQL_WAYPOINT_DELETE = "DELETE FROM WpWaypoints WHERE name = ?";
 	private static final String SQL_WAYPOINT_FIND_ONE = "SELECT * FROM WpWaypoints WHERE name = ?";
 	private static final String SQL_WAYPOINT_FIND_ALL = "SELECT * FROM WpWaypoints";
 	private static final String SQL_WAYPOINT_FIND_ALL_PAGE = "SELECT * FROM WpWaypoints LIMIT ? OFFSET ?";
 	private static final String SQL_WAYPOINT_FIND_USER = "SELECT * FROM WpWaypoints WHERE owner = ?";
 	
 	private static final String SQL_WAYPOINTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS WpWaypoints (" +
 			"name VARCHAR(32) NOT NULL PRIMARY KEY UNIQUE, " +
 			"owner VARCHAR(32) NOT NULL, " +
 			"description TEXT DEFAULT NULL, " +
 			"world VARCHAR(32) NOT NULL, " +
 			"loc_x DOUBLE NOT NULL, " +
 			"loc_y DOUBLE NOT NULL, " +
 			"loc_z DOUBLE NOT NULL" +
 			")";
 	
 	private static Dbms dbms;
 	private static DataSource dbSource;
 	private static Logger logger;
 	private static Server server;
 
 	@Override
 	public void init(Server s) {
 		server = s;
 		logger = server.getLogger();
 	}
 	
 	@Override
 	public boolean add(Waypoint waypoint) {
 		try {
			Connection dbConn = dbSource.getConnection();
 			PreparedStatement ps = dbConn.prepareStatement(SQL_WAYPOINT_ADD);
 			ps.setString(1, waypoint.getName());
 			ps.setString(2, waypoint.getOwner().getName());
 			ps.setString(3, waypoint.getDescription());
 			ps.setString(4, waypoint.getWorld().getName());
 			ps.setDouble(5, waypoint.getLocation().getX());
 			ps.setDouble(6, waypoint.getLocation().getY());
 			ps.setDouble(7, waypoint.getLocation().getZ());
 			if (ps.executeUpdate() == 1) {
 				return true;
 			}
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "[WaypointStorageSQL] " + e.getMessage(), e);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean edit(Waypoint waypoint, String newDescription, Location newLocation) {
 		try {
			Connection dbConn = dbSource.getConnection();
 			PreparedStatement ps = dbConn.prepareStatement(SQL_WAYPOINT_EDIT);
 			ps.setString(1, newDescription);
 			ps.setDouble(2, newLocation.getX());
 			ps.setDouble(3, newLocation.getY());
 			ps.setDouble(4, newLocation.getZ());
 			ps.setString(5, waypoint.getName());
 			if (ps.executeUpdate() == 1) {
 				return true;
 			}
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "[WaypointStorageSQL] " + e.getMessage(), e);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean delete(Waypoint waypoint) {
 		PreparedStatement ps;
 		try {
			Connection dbConn = dbSource.getConnection();
 			ps = dbConn.prepareStatement(SQL_WAYPOINT_DELETE);
 			ps.setString(1, waypoint.getName());
 			if (ps.executeUpdate() == 1) {
 				return true;
 			}
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "[WaypointStorageSQL] " + e.getMessage(), e);
 		}
 		return false;
 	}
 
 	@Override
 	public Waypoint find(String name) throws WaypointNotFoundException {
 		PreparedStatement ps;
 		try {
			Connection dbConn = dbSource.getConnection();
 			ps = dbConn.prepareStatement(SQL_WAYPOINT_FIND_ONE);
 			ps.setString(1, name);
 			ResultSet rs = ps.executeQuery();
 			Waypoint wp = null;
 			if (rs.next()) {
 				wp = Waypoint.build(server, rs.getString("name"), rs.getString("description"), rs.getString("owner"), rs.getString("world"), rs.getDouble("loc_x"), rs.getDouble("loc_y"), rs.getDouble("loc_z"));
 			}
 			if (rs.next() || wp == null) {
 				throw new WaypointNotFoundException();
 			} else {
 				return wp;
 			}
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "[WaypointStorageSQL] " + e.getMessage(), e);
 		}
 		throw new WaypointNotFoundException();
 	}
 
 	@Override
 	public List<Waypoint> findAll() {
 		ArrayList<Waypoint> list = new ArrayList<Waypoint>();
 		PreparedStatement ps;
 		try {
			Connection dbConn = dbSource.getConnection();
 			ps = dbConn.prepareStatement(SQL_WAYPOINT_FIND_ALL);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				list.add(Waypoint.build(server, rs.getString("name"), rs.getString("description"), rs.getString("owner"), rs.getString("world"), rs.getDouble("loc_x"), rs.getDouble("loc_y"), rs.getDouble("loc_z")));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "[WaypointStorageSQL] " + e.getMessage(), e);
 		}	
 		return list;
 	}
 
 	@Override
 	public List<Waypoint> findAllPage(int page, int size) {
 		ArrayList<Waypoint> list = new ArrayList<Waypoint>();
 		PreparedStatement ps;
 		try {
			Connection dbConn = dbSource.getConnection();
 			ps = dbConn.prepareStatement(SQL_WAYPOINT_FIND_ALL_PAGE);
 			ps.setInt(1, size);
 			ps.setInt(2, (page - 1) * size);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				list.add(Waypoint.build(server, rs.getString("name"), rs.getString("description"), rs.getString("owner"), rs.getString("world"), rs.getDouble("loc_x"), rs.getDouble("loc_y"), rs.getDouble("loc_z")));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "[WaypointStorageSQL] " + e.getMessage(), e);
 		}	
 		return list;
 	}
 	
 	@Override
 	public List<Waypoint> findAllByUser(Player player) {
 		ArrayList<Waypoint> list = new ArrayList<Waypoint>();
 		PreparedStatement ps;
 		try {
			Connection dbConn = dbSource.getConnection();
 			ps = dbConn.prepareStatement(SQL_WAYPOINT_FIND_USER);
 			ps.setString(1, player.getName());
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				list.add(Waypoint.build(server, rs.getString("name"), rs.getString("description"), rs.getString("owner"), rs.getString("world"), rs.getDouble("loc_x"), rs.getDouble("loc_y"), rs.getDouble("loc_z")));
 			}
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "[WaypointStorageSQL] " + e.getMessage(), e);
 		}	
 		return list;
 	}
 
 	@Override
 	public boolean load() {
 		// Don't do anything, we operate directly on the database
 		return true;
 	}
 
 	@Override
 	public boolean save() {
 		// Don't do anything, we operate directly on the database
 		return true;
 	}
 	
 	public void initSql(String dbmsName, String uri, String username, String password) throws Exception {
 		logger.log(Level.INFO, "[WaypointStorageSQL] Initializing SQL storage...");
 		logger.info("[WaypointStorageSQL] Trying DBMS Driver: \"" + dbmsName + "\"");
 		try {
 			dbms = Dbms.valueOf(dbmsName);
 		} catch (IllegalArgumentException e) {
 			logger.warning("[WaypointStorageSQL] Invalid DBMS from config. Defaulting to SQLITE...");
 			
 			dbms = Dbms.SQLITE;
 		}
 		try {
 			Class.forName(dbms.getDriver());
 		} catch (ClassNotFoundException e) {
 			throw new Exception("Unable to load SQL Driver!", e);
 		}
 		dbSource = dbms.getDataSource(username, password, uri);
		Connection dbConn = dbSource.getConnection();
 		Statement stmt = dbConn.createStatement();
 		stmt.addBatch(SQL_WAYPOINTS_TABLE_CREATE);
 		stmt.executeBatch();
 		logger.log(Level.INFO, "[WaypointStorageSQL] SQL storage initialized.");
 	}
 
 	public static enum Dbms {
 		SQLITE("org.sqlite.JDBC"), MYSQL("com.mysql.jdbc.Driver");
 		private final String driver;
 
 		Dbms(String driverClass) {
 			this.driver = driverClass;
 		}
 
 		public String getDriver() {
 			return driver;
 		}
 
 		public DataSource getDataSource(String username, String password, String url) {
 			switch (this) {
 			case MYSQL:
 				MysqlDataSource mds = new MysqlDataSource();
 				mds.setUser(username);
 				mds.setPassword(password);
 				mds.setUrl(url);
 				return mds;
 			default:
 			case SQLITE:
 				SQLiteDataSource sds = new SQLiteDataSource();
 				sds.setUrl(url);
 				return sds;
 			}
 		}
 	}
 
 }
