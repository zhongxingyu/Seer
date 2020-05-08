 package me.sd5.pvplogger;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.bukkit.entity.Player;
 
 public class PLDatabase {
 	
 	private static Connection connection;
 	private static Statement statement;
 	private static ResultSet resultSet;
 	
 	public PLDatabase() {
 		System.out.println("[PVPLogger] Connecting to database...");
 		try {
 			connection = DriverManager.getConnection(PLConfig.dbUrl, PLConfig.dbUser, PLConfig.dbPassword);
 			statement = connection.createStatement();
 			System.out.println("[PVPLogger] Successfully connected to database!");
 		} catch (SQLException e) {
 			System.out.println("[PVPLogger] Could not connect to database!");
 			return;
 		}
 		
 		System.out.println("[PVPLogger] Creating MySQL table...");
 		
 		String sql = "CREATE TABLE pvplogger ("
 		    + "date           VARCHAR(255), "
 		    + "attacker       VARCHAR(255), "
 		    + "attacker_world VARCHAR(255), "
 		    + "attacker_x     INTEGER, "
 		    + "attacker_y     INTEGER, "
 		    + "attacker_z     INTEGER, "
 		    + "victim         VARCHAR(255), "
 		    + "victim_world   VARCHAR(255), "
 		    + "victim_x       INTEGER, "
 		    + "victim_y       INTEGER, "
 		    + "victim_z       INTEGER, "
 		    + "weapon         VARCHAR(255), "
 		    + "damage         INTEGER, "
 		    + "fatal          BOOL)";
 		    
 		try {
 		    statement.executeUpdate(sql);		
 			System.out.println("[PVPLogger] Successfully created MySQL table!");
 		} catch(SQLException e) {
 			System.out.println("[PVPLogger] MySQL table already exists!");
 			return;
 		}
 	}
 	
 	public static void writeEntry(Player p1, Player p2, int damage) {
 		String date = getDate();
 		String attacker = p1.getName();
 		String attacker_world = p1.getWorld().getName();
 		int attacker_x = p1.getLocation().getBlockX();
 		int attacker_y = p1.getLocation().getBlockY();
 		int attacker_z = p1.getLocation().getBlockZ();
 		String victim = p2.getName();
 		String victim_world = p2.getWorld().getName();
 		int victim_x = p2.getLocation().getBlockX();
 		int victim_y = p2.getLocation().getBlockZ();
 		int victim_z = p2.getLocation().getBlockZ();
 		String weapon = p1.getItemInHand().getType().toString().toLowerCase();
		int fatal = (p2.getHealth() > 0) ? 0 : 1;
 		
 		String sql = "INSERT INTO " + PLConfig.dbTable + " ("
 			+ "date, "
 			+ "attacker, attacker_world, attacker_x, attacker_y, attacker_z, "
 			+ "victim, victim_world, victim_x, victim_y, victim_z, "
 			+ "weapon, damage, fatal"
 			+ ") VALUES ("
 			+ "'" + date + "', '"
 			+ attacker + "', '" + attacker_world + "', '" + attacker_x + "', '" + attacker_y + "', '" + attacker_z + "', '"
 			+ victim + "', '" + victim_world + "', '" + victim_x + "', '" + victim_y + "', '" + victim_z + "', '"
 			+ weapon + "', '" + damage + "', '" + fatal + "')";
 		
 		try {
 			statement.execute(sql);
 		} catch (SQLException e) {
 			System.out.println("[PVPLogger] Could not write data into database!");
 		}
 	}
 	
 	private static String getDate() {
 		Date date = new Date();
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		return dateFormat.format(date);
 	}
 	
 }
