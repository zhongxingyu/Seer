 package me.lenis0012.pvp;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Set;
 
 public class DataHandler
 {
 	private Main plugin = null;
 	private boolean isMySQL = false;
 	static private Connection con = null;
 	static private Statement state = null;
 	static private ResultSet result = null;
 	private static PreparedStatement ps = null;
 	String table = "mc_PvpLevels";
 	
 	public void setup(Main i)
 	{
 		plugin = i;
 		if(plugin.getConfig().getBoolean("settings.MySQL.use"))
 			isMySQL = true;
 		if(isMySQL)
 		{
 			if(this.setupDB())
 			{
 				this.createTable();
 				plugin.log("Loaded MySQL driver");
 			}
 		}
 	}
 	
 	public boolean setupDB()
 	{
 		if(isMySQL)
 		{
 			try
 			{
 				Class.forName("com.mysql.jdbc.Driver");
 			} catch (ClassNotFoundException e)
 			{
 				plugin.warn("Failed to load MySQL driver, not installed?");
 				return false;
 			}
 			try
 			{
 				String host = plugin.getConfig().getString("settings.MySQL.host", "localhost");
 				String port = String.valueOf(plugin.getConfig().getInt("settings.MySQL.port", 3306));
 				String user = plugin.getConfig().getString("settings.MySQL.username", "root");
 				String pass = plugin.getConfig().getString("settings.MySQL.password", "");
 				String database = plugin.getConfig().getString("settings.MySQL.database", "database");
 				con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port +"/" + database + "?" +
 						"user=" + user + "&password=" + pass);
 				state = con.createStatement();
 			} catch (SQLException e)
 			{
 				plugin.warn("Failed to open MySQL connection, that sucks D:");
 				plugin.warn(e.getMessage());
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public void createTable()
 	{
 		try
 		{
 			state.executeUpdate("CREATE TABLE IF NOT EXISTS "+table+
 					" (username VARCHAR(120) NOT NULL UNIQUE,level INT,kills INT);");
 		} catch(SQLException e)
 		{
 			plugin.warn("Failed to create table, lost connection?");
 		}
 	}
 	
 	public int getValue(String user, String value)
 	{
 		if(isMySQL)
 		{
 			try
 			{
 				if(value == "lvl"){ value = "level"; }
 				ps = con.prepareStatement("SELECT * FROM "+table+" WHERE username=?");
 				ps.setString(1, user);
 				result = ps.executeQuery();
 				if(result.next())
 					return result.getInt(value);
 			} catch(SQLException e)
 			{
 				plugin.warn("Failed to load MySQL, trying to reconnect");
 				plugin.warn(e.getMessage());
 			}
 		}else
 		{
 			int par1Int = plugin.getCustomConfig().getInt(value+"."+user);
 			return par1Int;
 		}
 		return 0;
 	}
 	
 	public void setValue(String user, int level, int kills, int mode)
 	{
 		if(isMySQL)
 		{
 			try
 			{
 				if(mode == 0)
 				{
 					ps = con.prepareStatement("INSERT INTO "+table+"(username,level,kills) VALUES(?,?,?);");
 					ps.setString(1, user);
 					ps.setInt(2, level);
 					ps.setInt(3, kills);
 					ps.executeUpdate();
 				}else
 				{
					ps = con.prepareStatement("UPDATE "+table+" SET level=? kills=? WHERE username=?");
 					ps.setInt(1, level);
					ps.setInt(2, kills);
					ps.setString(3, user);
 					ps.executeUpdate();
 				}
 			} catch(SQLException e)
 			{
 				plugin.warn("Failed to load MySQL, trying to reconnect");
 				plugin.warn(e.getMessage());
 			}
 		}else
 		{
 			plugin.getCustomConfig().set("lvl."+user, level);
 			plugin.getCustomConfig().set("kills."+user, kills);
 			plugin.saveCustomConfig();
 		}
 	}
 	
 	public boolean isset(String user)
 	{
 		if(isMySQL)
 		{
 			try
 			{
 				ps =  con.prepareStatement("SELECT * FROM "+table+" WHERE username=?;");
 				ps.setString(1, user);
 				result = ps.executeQuery();
 				if(result.next())
 					return true;
 			} catch(SQLException e)
 			{
 				plugin.warn("Failed to load MySQL, trying to reconnect");
 				plugin.warn(e.getMessage());
 			}
 		}else
 		{
 			try
 			{
 				Set<String> set = plugin.getCustomConfig().getConfigurationSection("lvl").getKeys(false);
 				return set.contains(user);
 			} catch(Exception e)
 			{
 				//user not set
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	public void stopCon()
 	{
 		try
 		{
 			if(result != null)
 				result.close();
 			if(state != null)
 				state.close();
 			if(con != null)
 				con.close();
 		} catch(SQLException e)
 		{
 			plugin.warn("Failed to stop MySQL connection, invalid connection?");
 		}
 	}
 }
