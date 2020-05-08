 package net.loadingchunks.plugins.GuardWolf;
 
 import java.sql.*;
 
 /**
  * Handler for the /gw sample command.
  * @author Cue
  */
 public class GWSQL {
 	public Connection con;
 	public Statement stmt;
 	public final GuardWolf plugin;
 	
 	public GWSQL(GuardWolf plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	public void Connect()
 	{
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.con = DriverManager.getConnection(this.plugin.gwConfig.get("db_address"), this.plugin.gwConfig.get("db_user"), this.plugin.gwConfig.get("db_pass"));
 		} catch ( SQLException e )
 		{
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) { e.printStackTrace(); }
 	}
 	
 	public void Ban(String name, String banner, long time, String reason)
 	{
 		Integer strike = 1;
 		try {
 			PreparedStatement stat = con.prepareStatement("INSERT INTO `" + this.plugin.gwConfig.get("db_table") + "`" +
 					"(`user`,`country`,`banned_at`,`expires_at`,`reason`,`banned_by`,`strike`,`strike_expires`)" +
 					" VALUES ('" + name + "','GB',NOW(),FROM_UNIXTIME(" + time + "),'" + reason + "','" + banner + "'," + strike + ",NOW())"
 					);
 			stat.execute();
 		} catch ( SQLException e ) { e.printStackTrace(); }
 	}
 	
 	public void Stats()
 	{
 		try {
 			Statement stat = con.createStatement();
 			ResultSet result = stat.executeQuery("SELECT COUNT(*) as counter FROM `" + this.plugin.gwConfig.get("db_table") + "`");
 			result.next();
 			System.out.println("Ban Records: " + result.getInt("counter"));
 		} catch ( SQLException e ) { e.printStackTrace(); }
 	}
 	
 	public String CheckBan(String user)
 	{
 		System.out.println("[GW] Checking ban status...");
 		try {
 			PreparedStatement stat = con.prepareStatement("SELECT * FROM `mcusers_ban` WHERE expires_at > NOW() AND `user` = '" + user + "' ORDER BY id DESC");
 			ResultSet result = stat.executeQuery();
			result.next();
			if(!result.wasNull())
 			{
 				if(result.getString("expires_at").equalsIgnoreCase("00/00/00 00:00:00"))
 					return result.getString("reason") + System.getProperty("line.separator") + " (Permanent Ban)";
 				else
 					return result.getString("reason") + System.getProperty("line.separator") + " (Expires " + result.getString("expires_at") + ")";
 			} else return null;
 		} catch ( SQLException e ) { e.printStackTrace(); }
 		return null;
 	}
 }
