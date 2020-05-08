 /*
  * The epic j2Plugin
  * A bunch of fun features, put together for joe.to
  * DO NOT DISTRIBUTE!
  * 
  * Credits:
  * mbaxter
  * 
  * Thanks to;
  * bootswithdefer for Tips code
  * Nijikokun for properties management and the item command
  * 
  */
 
 package com.J2;
 
 import java.io.File;
 import java.util.HashMap;
 import org.bukkit.entity.*;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.ChatColor;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * J2 Plugin, on Bukkit
  *
  * @author mbaxter
  */
 public class J2Plugin extends JavaPlugin {
 	private final J2PlrChat plrlisChat = new J2PlrChat(this);
 	private final J2PlrCommands plrlisCommands = new J2PlrCommands(this);
 	private final J2PlrItem plrlisItem = new J2PlrItem(this);
 	private final J2PlrJoinQuit plrlisJoinQuit = new J2PlrJoinQuit(this);
 	private final J2BlockListener blockListener = new J2BlockListener(this);
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	private final J2PlugChat func_chat = new J2PlugChat(this);
 	private final J2PlugPermissions func_permissions = new J2PlugPermissions(this);
 	private final J2PlugIRC func_irc = new J2PlugIRC(this);
 	private final J2PlugKickBan func_kickban = new J2PlugKickBan(this);
 	public BlockLogger blogger;
 
 	public J2Plugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
 		super(pluginLoader, instance, desc, folder, plugin, cLoader);
 		//Don't add anything here
 	}
 
 	public void onDisable() {
 
 				getIRC().kill();
 		stopTimer();
 		// NOTE: All registered events are automatically unregistered when a plugin is disabled
 
 	}
 
 	public void onEnable() {
 		log=Logger.getLogger("Minecraft");
 		protectedUsers=new ArrayList<String>();
 
 		//mysql start
 		PropFile properties = new PropFile("mysql.properties");
 		driver = properties.getString("driver", "com.mysql.jdbc.Driver");
 		username = properties.getString("user", "root");
 		password = properties.getString("pass", "root");
 		db = properties.getString("db", "jdbc:mysql://localhost:3306/minecraft");
 		//chatTable = properties.getString("chat","chat");
 		bansTable = properties.getString("j2bans", "j2bans");
 		usersTable = properties.getString("users", "users");
 		try {
 			Class.forName(driver);
 		} catch (ClassNotFoundException ex) {
 
 		}
 		//mysql end
 
 		loadData();
 
 		//irc start
 		if(ircEnable)getIRC().prepIRC();
 		//irc end
 		loadTips();
 		startTipsTimer();
 		
 		//Initialize BlockLogger
 		this.blogger = new BlockLogger(this.getConnection());
 		this.blogger.run();
 
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_COMMAND, plrlisCommands, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_CHAT, plrlisChat, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_ITEM, plrlisItem, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
 		//pm.registerEvent(Event.Type.ENTITY_EXPLODE, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, plrlisJoinQuit, Priority.Normal, this);
 
 		// EXAMPLE: Custom code, here we just output some info so we can check all is well
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 
 	public boolean isDebugging(final Player player) {
 		if (debugees.containsKey(player)) {
 			return debugees.get(player);
 		} else {
 			return false;
 		}
 	}
 
 	public void setDebugging(final Player player, final boolean value) {
 		debugees.put(player, value);
 	}
 
 	public void loadData(){
 		rules=readDaFile("rules.txt");
 		blacklist=readDaFile("blacklistinfo.txt");
 		intro=readDaFile("intro.txt");
 		PropFile servproperties = new PropFile("server.properties");
 		playerLimit=servproperties.getInt("max-players");
 		PropFile j2properties = new PropFile("j2.properties");
 		try { 
 			tips_location = j2properties.getString("tip-location", "tips.txt");
 			tips_delay = j2properties.getInt("tip-delay", 120);
 			tips_prefix = j2properties.getString("tip-prefix","");
 			tips_color = "\u00A7"+j2properties.getString("tip-color", "b");
 			if (tips_prefix == null || tips_prefix.length() == 0)
 				tips_prefix = "";
 			else if (tips_prefix.charAt(tips_prefix.length()-1) != ' ')
 				tips_prefix += " ";
 			ircHost = j2properties.getString("irc-host","localhost");
 			ircName = j2properties.getString("irc-name","aMinecraftBot");
 			ircChannel = j2properties.getString("irc-channel","#minecraftbot");
 			ircAdminChannel = j2properties.getString("irc-adminchannel","#minecraftbotadmin");
 			ircUserColor = j2properties.getString("irc-usercolor","f");
 			ircSeparator= j2properties.getString("irc-separator","<,>").split(",");
 			ircCharLim = j2properties.getInt("irc-charlimit",390);
 			ircMsg=j2properties.getBoolean("irc-msg-enable",false);
 			ircEnable=j2properties.getBoolean("irc-enable",false);
 			ircEcho = j2properties.getBoolean("irc-echo",false);
 			ircPort = j2properties.getInt("irc-port",6667);
 			ircDebug = j2properties.getBoolean("irc-debug",false);
 			ircOnJoin = j2properties.getString("irc-onjoin","");
 			gsAuth = j2properties.getString("gs-auth","");
 			gsPass = j2properties.getString("gs-pass","");
 			banBuckets = j2properties.getBoolean("banbuckets",false);
 			String[] superBlacklist = j2properties.getString("superblacklist", "").split(",");
 			String[] regBlacklist = j2properties.getString("regblacklist", "").split(",");
 			ircLevel2 = j2properties.getString("irc-level2","").split(",");
 			superblacklist=new ArrayList<Integer>();
 			itemblacklist=new ArrayList<Integer>();
 			/*natureXmin = j2properties.getInt("nature-xmin",2000);
 			natureXmax = j2properties.getInt("nature-xmax",2001);
 			natureZmin = j2properties.getInt("nature-zmin",2000);
 			natureZmax = j2properties.getInt("nature-zmax",2001);*/
 			mc2=j2properties.getBoolean("mc2",false);
 			maintenance = j2properties.getBoolean("maintenance",false);
 			fun=j2properties.getBoolean("funmode",false);
 			randomcolor=j2properties.getBoolean("randcolor",false);
 			for(String s:superBlacklist){
 				if(s!=null){
 					superblacklist.add(Integer.valueOf(s));
 				}
 			}
 			for(String s:regBlacklist){
 				if(s!=null){
 					itemblacklist.add(Integer.valueOf(s));
 				}
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Exception while reading from j2.properties", e);
 		}
 	}
 
 	public Connection getConnection() {
 		try {
 			return DriverManager.getConnection(db + "?autoReconnect=true&user=" + username + "&password=" + password);
 		} catch (SQLException ex) {
 
 		}
 		return null;
 	}
 
 	public String[] readDaFile(String filename)
 	{
 		FileReader fileReader = null;
 		try {
 			fileReader = new FileReader(filename);
 		} catch (FileNotFoundException e2) {
 			e2.printStackTrace();
 			String[] uhOh=new String[1];
 			uhOh[0]="";
 			return uhOh;
 		}
 		BufferedReader rulesBuffer = new BufferedReader(fileReader);
 		List<String> fileLines = new ArrayList<String>();
 		String line = null;
 		try {
 			while ((line = rulesBuffer.readLine()) != null) {
 				fileLines.add(line);
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			rulesBuffer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return fileLines.toArray(new String[fileLines.size()]);
 	}
 
 	public String stringClean(String hat){
 		return hat.replace('\"', '_').replace('\'', '_').replace(';', '_');
 	}
 
 	public void trust(String aname){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String name=stringClean(aname);
 		try {
 			conn = getConnection();
 			ps = conn.prepareStatement("SELECT count(id) FROM "+usersTable+" WHERE name=\""+name+"\"");
 			rs = ps.executeQuery();
 			boolean exists=false;
 			while (rs.next()) {
 				if(rs.getInt("count(id")>0){
 					exists=true;
 				}
 			}
 
 			if(exists)
 				ps = conn.prepareStatement("UPDATE " + usersTable + " SET groups=\"trusted\" WHERE name=\""+ name +"\"");
 			else
 				ps = conn.prepareStatement("INSERT INTO " + usersTable + " VALUES ('',\""+ name.toLowerCase() +"\",\"trusted\",'','','','','','')");
 			ps.executeUpdate();
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 
 	//tips
 	private void startTipsTimer() {
 		stopTimer = false;
 		final Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				if (stopTimer) {
 					timer.cancel();
 					return;
 				}
 				broadcastTip();
 			}
 		}, 3000, tips_delay*1000);
 	}
 
 
 	private void stopTimer() {
 		stopTimer = true;
 	}
 
 	public void broadcastTip()
 	{
 		broadcastTip(currentTip);
 		if (currentTip >= tips.size())
 			currentTip = 0;
 		else
 			currentTip++;
 	}
 
 	public void broadcastTip(int tipnum)
 	{
 		if (tips.isEmpty())
 			return;
 		if (tipnum < 0 || tipnum >= tips.size())
 			return;
 		String message = tips_color + tips_prefix + tips.get(tipnum);
 		for (Player p : this.getServer().getOnlinePlayers()) {
 			if (p != null) {
 				p.sendMessage(message);
 			}
 		}
 	}
 
 	public void loadTips() {
 		tips = new ArrayList<String>();
 		if (!new File(tips_location).exists()) {
 
 			return;
 		}
 		try {
 			Scanner scanner = new Scanner(new File(tips_location));
 			while (scanner.hasNextLine()) {
 				String line = scanner.nextLine();
 				if (line.startsWith("#") || line.equals(""))
 					continue;
 				tips.add(line);
 			}
 			scanner.close();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Exception while reading " + tips_location, e);
 			stopTimer();
 		}
 	}
 
 	//end tips
 
 	public boolean isOnSuperBlacklist(int id) {
 		return superblacklist.contains(Integer.valueOf(id));
 	}
 	public boolean isOnRegularBlacklist(int id) {
 		return itemblacklist.contains(Integer.valueOf(id));
 	}
 
 	public void travelLog(String name,int distance){
 
 	}
 
 	/*public Block locationCheck(Player player,Block block,boolean placed){
 		int x,z;
 
 		if(block==null && !placed){
 			Location l=player.getLocation();
 			x=(int)l.x;
 			z=(int)l.z;
 			int minX=natureXmin-10;
 			int maxX=natureXmax+10;
 			int minZ=natureZmin-10;
 			int maxZ=natureZmax+10;
 			boolean pancakes=false;
 			if( (x==minX || x==maxX) && z>minZ && z<maxZ){
 				pancakes=true;
 			}
 			if( (z==minZ || z==maxZ) && x>minX && x<maxX){
 				pancakes=true;
 			}
 			if(pancakes)
 			{
 				player.sendMessage(Colors.LightBlue+"IMPORTANT MESSAGE: "+Colors.LightGreen+"Nature");
 				player.sendMessage(Colors.LightGreen+"You are 10 blocks from the nature conservatory");
 				player.sendMessage(Colors.LightGreen+"DO NOT MODIFY, DO NOT BUILD. ONLY OBSERVE.");
 				player.sendMessage(Colors.LightGreen+"Harsh punishments for damaging nature");
 				player.sendMessage(Colors.LightGreen+"- Bob the Naturalist");
 				//player.sendMessage(x+" "+z+" "+natureXmin+" "+natureXmax+" "+natureZmin+" "+natureZmax);
 			}
 		}
 		else{
 			x=block.getX();
 			z=block.getZ();
 			if(x>(natureXmin) && x<(natureXmax) && z>(natureZmin) && z<(natureZmax))
 			{
 				int type=19;
 				if(!placed)
 					type=block.getType();
 				Block james=new Block(type,x,block.getY(),z);
 				if(isJ2Admin(player)){
 					player.sendMessage(Colors.LightBlue+"IMPORTANT MESSAGE: "+Colors.LightGreen+"Nature");
 					player.sendMessage(Colors.LightGreen+"You just touched the conservatory");
 					player.sendMessage(Colors.LightGreen+"Please undo what you changed");
 					player.sendMessage(Colors.LightGreen+"- Bob the Naturalist");
 					return null;
 				}
 				return james;
 			}
 		}
 		return null;
 	}*/
 
 	public void tpToCoord(Player player, double x, double y, double z, float rotation, float pitch){
		player.teleportTo(new Location(this.getServer().getWorlds().get(0), x, y, z, rotation, pitch));
 	}
 
 	public String combineSplit(int startIndex, String[] string, String seperator) {
 		StringBuilder builder = new StringBuilder();
 		for (int i = startIndex; i < string.length; i++) {
 			builder.append(string[i]);
 			builder.append(seperator);
 		}
 		builder.deleteCharAt(builder.length() - seperator.length()); 
 		return builder.toString();
 	}
 
 	public J2PlugChat getChat(){
 		return func_chat;
 	}
 
 	public J2PlugPermissions getPerm(){
 		return func_permissions;
 	}
 
 	public J2PlugIRC getIRC(){
 		return func_irc;
 	}
 	
 	public J2PlugKickBan getKickBan(){
 		return func_kickban;
 	}
 
 	public String driver, username, password, db, /*chatTable,*/ bansTable, usersTable;
 	public Logger log;
 
 	public ArrayList<String> protectedUsers;
 	public String[] rules, blacklist, intro;
 
 	public String ircName,ircHost,ircChannel,ircUserColor,ircOnJoin,gsAuth,gsPass,ircAdminChannel;
 	public boolean ircMsg,ircEcho,ircDebug;
 	public int ircCharLim,ircPort;
 	public String[] ircSeparator;
 	private String tips_location = "tips.txt";
 	private String  tips_color = ChatColor.AQUA.toString();
 	private String  tips_prefix = "";
 	private boolean stopTimer = false;
 	private int tips_delay = 120;
 	private ArrayList<String> tips;
 	private int currentTip = 0;
 	public boolean banBuckets;
 	public String[] ircLevel2;
 	public boolean ircEnable;
 	private ArrayList<Integer> itemblacklist,superblacklist;
 	//private int natureXmin,natureXmax,natureZmin,natureZmax;
 	public boolean maintenance=false;
 	public boolean mc2;
 	public PropFile tpProtect=new PropFile("tpProtect.list");
 	public Player OneByOne = null;
 	public boolean fun,randomcolor;
 	Random random = new Random();
 	int playerLimit=0;
 	ArrayList<String> srstaffList,adminsList,trustedList;
 }
