 package com.lenis0012.bukkit.pvp.data;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import com.lenis0012.bukkit.pvp.utils.StackUtil;
 
 public class SQLite implements DataManager {
 	Connection con;
 	Statement st;
 	private String fileDir;
 	private String fileName;
 	Table table;
 	
 	public SQLite(String fileDir, String fileName) {
 		this.fileDir = fileDir;
 		this.fileName = fileName;
 		
 		File dir = new File(fileDir);
 		dir.mkdirs();
 		
 		File file = new File(fileDir + File.separator + fileName);
 		if(!file.exists()) {
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				StackUtil.dumpStack(e);
 			}
 		}
 		
 		this.open();
 	}
 	
 	protected SQLite(FileConfiguration config) {}
 	
 	@Override
 	public void setTable(Table table) {
 		this.table = table;
 		try {
 			this.st.executeUpdate("CREATE TABLE IF NOT EXISTS "+table.getName()+table.getUsage());
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed creating SQL task:");
 			StackUtil.dumpStack(e);
 			return;
 		}
 	}
 
 	@Override
 	public void set(Object... values) {
 		try {
 			String valueCount = "";
 			for(int i = 0; i < values.length; i++) {
 				valueCount += "?";
 				if(i < (values.length - 1))
 					valueCount += ",";
 			}
 			
 			PreparedStatement ps = con.prepareStatement("INSERT INTO "+table.getName()+table.getValues()+" VALUES("+valueCount+");");
 			for(int i = 0; i < values.length; i++) {
 				ps.setObject(i + 1, values[i]);
 			}
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed SQL task:");
 			StackUtil.dumpStack(e);
 			return;
 		}
 	}
 
 	@Override
 	public Object get(String index, String toGet, Object value) {
 		try {
 			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+table.getName()+" WHERE "+index+"=?;");
 			ps.setObject(1, value);
 			ResultSet result = ps.executeQuery();
 			if(result.next()) {
 				return result.getObject(toGet);
 			}
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed SQL task:");
 			StackUtil.dumpStack(e);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public boolean contains(String index, Object value) {
 		try {
 			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+table.getName()+" WHERE "+index+"=?;");
 			ps.setObject(1, value);
 			ResultSet result = ps.executeQuery();
 			return result.next();
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed SQL task:");
 			StackUtil.dumpStack(e);
 			return false;
 		}
 	}
 
 	@Override
 	public void update(String index, String toUpdate, Object indexValue, Object updateValue) {
 		try {
 			PreparedStatement ps = con.prepareStatement("UPDATE "+table.getName()+" SET "+toUpdate+"=? WHERE "+index+"=?;");
 			ps.setObject(1, updateValue);
 			ps.setObject(2, indexValue);
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed SQL task:");
 			StackUtil.dumpStack(e);
 			return;
 		}
 	}
 
 	@Override
 	public void remove(String index, Object value) {
 		try {
 			PreparedStatement ps = con.prepareStatement("DELTE FROM "+table.getName()+" WHERE "+index+"=?;");
 			ps.setObject(1, value);
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed SQL task:");
 			StackUtil.dumpStack(e);
 			return;
 		}
 	}
 
 	@Override
 	public boolean isOpen() {
 		return con != null || st != null;
 	}
 
 	@Override
 	public void open() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed to init SQLite driver:");
 			StackUtil.dumpStack(e);
 			return;
 		}
 		
 		try {
			this.con = DriverManager.getConnection("jdbc:sqlite:" + fileDir + File.separator + fileName);
 			this.st = con.createStatement();
 			
 			//Set the query timeout
 			st.setQueryTimeout(30);
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed to init SQLite connection:");
 			StackUtil.dumpStack(e);
 			return;
 		}
 	}
 
 	@Override
 	public void close() {
 		try {
 			if(st != null)
 				st.close();
 			if(con != null)
 				con.close();
 		} catch (SQLException e) {
 			Bukkit.getLogger().severe("[PvpLevels] Failed to close SQL:");
 			StackUtil.dumpStack(e);
 		}
 	}
 }
