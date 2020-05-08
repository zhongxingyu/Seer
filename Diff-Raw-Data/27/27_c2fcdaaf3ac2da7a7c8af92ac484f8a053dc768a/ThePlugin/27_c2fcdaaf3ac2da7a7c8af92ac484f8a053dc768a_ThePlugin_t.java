 package net.kiwz.ThePlugin;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.kiwz.ThePlugin.Commands;
 import net.kiwz.ThePlugin.listeners.BlockListener;
 import net.kiwz.ThePlugin.listeners.EntityListener;
 import net.kiwz.ThePlugin.listeners.MoveListener;
 import net.kiwz.ThePlugin.listeners.PlayerListener;
 import net.kiwz.ThePlugin.mysql.Homes;
 import net.kiwz.ThePlugin.mysql.BuildTables;
 import net.kiwz.ThePlugin.mysql.ConnectToMySQL;
 import net.kiwz.ThePlugin.mysql.Places;
 import net.kiwz.ThePlugin.mysql.Players;
 import net.kiwz.ThePlugin.mysql.Worlds;
 import net.kiwz.ThePlugin.utils.ConsoleFilter;
 import net.kiwz.ThePlugin.utils.HandleWorlds;
 import net.kiwz.ThePlugin.utils.Permissions;
 import net.kiwz.ThePlugin.utils.ServerManagement;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ThePlugin extends JavaPlugin {
 	public static ChatColor c1 = ChatColor.GOLD;
 	public static ChatColor c2 = ChatColor.RED;
 	public static ChatColor c3 = ChatColor.YELLOW;
 	public static ChatColor c4 = ChatColor.WHITE;
 	public static ChatColor c5 = ChatColor.DARK_GRAY;
 	
 	private PluginManager pm = Bukkit.getServer().getPluginManager();
 	
 	private Homes homes = new Homes();
 	public static HashMap<Integer, Homes> getHomes;
 	private Places places = new Places();
 	public static HashMap<Integer, Places> getPlaces;
 	public static HashMap<String, Integer> remPlaces = new HashMap<String, Integer>();
 	private Players players = new Players();
 	public static HashMap<String, Players> getPlayers;
 	private Worlds worlds = new Worlds();
 	public static HashMap<String, Worlds> getWorlds;
 	public static ArrayList<String> remWorlds = new ArrayList<String>();
 	public static ArrayList<String> admins = new ArrayList<String>();
 	public static HashMap<String, List<String>> chatIgnore;
 	
 	@Override
 	public void onLoad() {
 		new File("plugins\\ThePlugin\\").mkdirs();
 		
 		Connection conn = new ConnectToMySQL().openConnection();
 		
 		new BuildTables().createTables(conn);
 		
 		getHomes = homes.getTableHomes(conn);
 		getPlaces = places.getTablePlaces(conn);
 		getPlayers = players.getTablePlayers(conn);
 		getWorlds = worlds.getTableWorlds(conn);
 		
 		try {
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void onEnable() {
		ConsoleFilter filter = new ConsoleFilter();
		Bukkit.getLogger().setFilter(filter);
		Logger.getLogger("Minecraft").setFilter(filter);
		Logger.getLogger("global").setFilter(filter);
		
 		admins = new Permissions().loadAdmins();
 		chatIgnore = new HashMap<String, List<String>>();
 		
 		HandleWorlds hWorlds = new HandleWorlds();
 		hWorlds.loadWorlds();
 		for (World world : Bukkit.getServer().getWorlds()) {
 			if (!getWorlds.containsKey(world.getName())) {
 				hWorlds.addWorld(world);
 			}
 		}
 		
 		Commands cmds = new Commands();
 		getCommand("entity").setExecutor(cmds);
 		getCommand("feed").setExecutor(cmds);
 		getCommand("fly").setExecutor(cmds);
 		getCommand("gm").setExecutor(cmds);
 		getCommand("heal").setExecutor(cmds);
 		getCommand("help").setExecutor(cmds);
 		getCommand("home").setExecutor(cmds);
 		getCommand("ignore").setExecutor(cmds);
 		getCommand("sethome").setExecutor(cmds);
 		getCommand("list").setExecutor(cmds);
 		getCommand("minne").setExecutor(cmds);
 		getCommand("mute").setExecutor(cmds);
 		getCommand("unmute").setExecutor(cmds);
 	    getCommand("oi").setExecutor(cmds);
 	    getCommand("plass").setExecutor(cmds);
 		getCommand("spawn").setExecutor(cmds);
 		getCommand("setspawn").setExecutor(cmds);
 	    getCommand("tell").setExecutor(cmds);
 	    getCommand("tid").setExecutor(cmds);
 	    getCommand("tp").setExecutor(cmds);
 	    getCommand("ticks").setExecutor(cmds);
 		getCommand("whois").setExecutor(cmds);
 		getCommand("world").setExecutor(cmds);
 		getCommand("test").setExecutor(cmds);
 
 	    pm.registerEvents(new PlayerListener(), this);
 	    pm.registerEvents(new EntityListener(), this);
 	    pm.registerEvents(new BlockListener(), this);
 	    pm.registerEvents(new MoveListener(), this);
 	    
 		ServerManagement sm = new ServerManagement();
 	    sm.autoStop();
 	    sm.save(10L);
 	    
 	    //Dynmap dynmap = new Dynmap();
 	    //dynmap.activatedynmap();
 	}
 	
 	@Override
 	public void onDisable() {
 		Connection conn = new ConnectToMySQL().openConnection();
 		
 		homes.setTableHomes(conn, getHomes);
 		places.setTablePlaces(conn, getPlaces);
 		players.setTablePlayers(conn, getPlayers);
 		worlds.setTableWorlds(conn, getWorlds);
 		
 		try {
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
