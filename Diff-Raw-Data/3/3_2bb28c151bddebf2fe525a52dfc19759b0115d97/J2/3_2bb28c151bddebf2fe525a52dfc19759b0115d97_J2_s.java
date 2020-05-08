 /*
  * j2Plugin
  * A bunch of fun features, put together for joe.to
  */
 
 package to.joe;
 
 import java.io.File;
 
 import jline.ConsoleReader;
 import jline.Terminal;
 import jline.ANSIBuffer.ANSICodes;
 import org.bukkit.block.Block;
 import org.bukkit.command.*;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.*;
 import org.bukkit.Location;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import to.joe.listener.*;
 import to.joe.manager.*;
 import to.joe.util.*;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
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
 public class J2 extends JavaPlugin {
 	private final PlayerChat plrlisChat = new PlayerChat(this);
 	private final PlayerInteract plrlisInteract = new PlayerInteract(this);
 	private final PlayerJoinQuit plrlisJoinQuit = new PlayerJoinQuit(this);
 	private final BlockAll blockListener = new BlockAll(this);
 	private final EntityAll entityListener = new EntityAll(this);
 	private final PlayerMovement plrlisMovement = new PlayerMovement(this);
 	/**
 	 * Chat manager
 	 */
 	public final Chats chat = new Chats(this);
 	/**
 	 * IRC manager
 	 */
 	public final IRC irc = new IRC(this);
 	/**
 	 * Kick/ban manager
 	 */
 	public final KicksBans kickbans = new KicksBans(this);
 	/**
 	 * User manager
 	 */
 	public final Users users = new Users(this);
 	/**
 	 * Report manager
 	 */
 	public final Reports reports = new Reports(this);
 	/**
 	 * Warp manager
 	 */
 	public final Warps warps = new Warps(this);
 	/**
 	 * Webpage manager
 	 */
 	public final WebPage webpage = new WebPage(this);
 	/**
 	 * IP Tracking manager
 	 */
 	public final IPTracker ip=new IPTracker(this);
 	/**
 	 * Ban cooperative manager
 	 */
 	public final BanCooperative banCoop=new BanCooperative(this);
 	/**
 	 * Damage manager
 	 */
 	public final Damages damage=new Damages(this);
 	/**
 	 * Permission manager
 	 */
 	public final Permissions perms=new Permissions(this);
 	/**
 	 * Recipe implementer
 	 */
 	public final Recipes recipes=new Recipes(this);
 	/**
 	 * Ministry of Truth
 	 */
 	public final Minitrue minitrue=new Minitrue(this);
 	/**
 	 * Jail manager
 	 */
 	public final Jailer jail = new Jailer(this);
 	/**
 	 * Movement tracker
 	 */
 	public final MoveTracker move = new MoveTracker(this);
 	/**
 	 * Activity tracker
 	 */
 	public final ActivityTracker activity = new ActivityTracker(this);
 	/**
 	 * Craftual Harassment Panda
 	 */
 	public final CraftualHarassmentPanda panda=new CraftualHarassmentPanda(this);
 	/**
 	 * Vote manager
 	 */
 	public final Voting voting=new Voting(this);
 	//public managerBlockLog blogger;
 	/**
 	 * MySQL stuffs
 	 */
 	public MySQL mysql;
 
 
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.Plugin#onDisable()
 	 */
 	public void onDisable() {
 		irc.kill();
 		stopTimer();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.Plugin#onEnable()
 	 */
 	public void onEnable() {
 		this.initLog();
 		log=Logger.getLogger("Minecraft");
 		log.setFilter(new MCLogFilter());
 		protectedUsers=new ArrayList<String>();
 		loadData();
 		this.debug("Data loaded");
 		//irc start
 		if(ircEnable)irc.prepIRC();
 		irc.startIRCTimer();
 		//if(ircEnable)irc.startIRCTimer();
 		this.debug("IRC up (or disabled)");
 		//irc end
 		loadTips();
 		this.debug("Tips loaded");
 		startTipsTimer();
 		this.debug("Tips timer started");
 
 		//Initialize BlockLogger
 		//this.blogger = new managerBlockLog(this.mysql.getConnection(),this.mysql.servnum());
 		//if(debug)this.log("Blogger init");
 		//new Thread(blogger).start();
 		//if(debug)this.log("Blogger is go");
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, plrlisChat, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, plrlisChat, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, plrlisInteract, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_PRELOGIN, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_TELEPORT, plrlisMovement, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE,plrlisMovement,Priority.Normal,this);
 		pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);
 		if(debug)this.log("Events registered");
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 		webpage.go(servernumber);
 		recipes.addRecipes();
 		minitrue.restartManager();
 		this.activity.restartManager();
 		this.banCoop.startCallback();
 	}
 
 	/**
 	 * Load a butt-ton of data for startup.
 	 */
 	public void loadData(){
 		rules=readDaFile("rules.txt");
 		blacklist=readDaFile("blacklistinfo.txt");
 		intro=readDaFile("intro.txt");
 		motd=readDaFile("motd.txt");
 		help=readDaFile("help.txt");
 		Property j2properties = new Property("j2.properties");
 		Configuration conf=this.getConfiguration();
 		HashMap<String,Object> conf_general=new HashMap<String,Object>();
 		HashMap<String,Object> conf_mysql=new HashMap<String,Object>();
 		HashMap<String,Object> conf_irc=new HashMap<String,Object>();
 		HashMap<String,Object> conf_tips=new HashMap<String,Object>();
 		HashMap<String,Object> conf_maint=new HashMap<String,Object>();
 		HashMap<String,Object> conf_blacklists=new HashMap<String,Object>();
 
 
 		try { 
 			debug = j2properties.getBoolean("debug",false);
 			conf_general.put("debug-mode", this.debug);
 			//mysql start
 			String mysql_username = j2properties.getString("user", "root");
 			String mysql_password = j2properties.getString("pass", "root");
 			String mysql_db = j2properties.getString("db", "jdbc:mysql://localhost:3306/minecraft");
 			conf_mysql.put("username", mysql_username);
 			conf_mysql.put("database", mysql_db);
 			conf_mysql.put("password", mysql_password);
 			//chatTable = properties.getString("chat","chat");
 			servernumber = j2properties.getInt("server-number", 0);
 			conf_general.put("server-number", this.servernumber);
 			mysql = new MySQL(mysql_username,mysql_password,mysql_db, servernumber, this);
 			this.warps.restartManager();
 			this.reports.restartManager();
 			this.users.restartGroups();
 			mysql.loadMySQLData();
 			//mysql end
 
 			playerLimit=j2properties.getInt("max-players",20);
 			conf_general.put("max-players", this.playerLimit);
 			tips_delay = j2properties.getInt("tip-delay", 120);
 			tips_color = "\u00A7"+j2properties.getString("tip-color", "b");
 			conf_tips.put("delay", tips_delay);
 			conf_tips.put("color", tips_color);
 			ircHost = j2properties.getString("irc-host","localhost");
 			conf_irc.put("host", ircHost);
 			ircName = j2properties.getString("irc-name","aMinecraftBot");
 			conf_irc.put("nick", ircName);
 			ircChannel = j2properties.getString("irc-channel","#minecraftbot");
 			conf_irc.put("relay-channel", ircChannel);
 			ircAdminChannel = j2properties.getString("irc-adminchannel","#minecraftbotadmin");
 			conf_irc.put("admin-channel", ircAdminChannel);
 			int ircuc = j2properties.getInt("irc-usercolor",15);
 			conf_irc.put("ingame-color", ircuc);
 			ircUserColor=mysql.toColor(ircuc);
 			ircSeparator= j2properties.getString("irc-separator","<,>").split(",");
 			conf_irc.put("ingame-separator", j2properties.getString("irc-separator","<,>"));
 			ircCharLim = j2properties.getInt("irc-charlimit",390);
 			conf_irc.put("char-limit", ircCharLim);
 			ircMsg=j2properties.getBoolean("irc-msg-enable",false);
 			conf_irc.put("require-msg-cmd", ircMsg);
 			ircEnable=j2properties.getBoolean("irc-enable",false);
 			conf_irc.put("enable", ircEnable);
 			ircEcho = j2properties.getBoolean("irc-echo",false);
 			conf_irc.put("echo-messages", ircEcho);
 			ircPort = j2properties.getInt("irc-port",6667);
 			conf_irc.put("port", ircPort);
 			ircDebug = j2properties.getBoolean("irc-debug",false);
 			conf_irc.put("debug-spam", ircDebug);
 			ircOnJoin = j2properties.getString("irc-onjoin","");
 			conf_irc.put("channel-join-message", ircOnJoin);
 			gsAuth = j2properties.getString("gs-auth","");
 			conf_irc.put("gamesurge-user", gsAuth);
 			gsPass = j2properties.getString("gs-pass","");
 			conf_irc.put("gamesurge-pass", gsPass);
 			ircLevel2 = j2properties.getString("irc-level2","").split(",");
 			conf_irc.put("level2-commands", j2properties.getString("irc-level2"));
 			safemode=j2properties.getBoolean("safemode",false);
 			conf_general.put("safemode", safemode);
 			explodeblocks=j2properties.getBoolean("explodeblocks",true);
 			conf_general.put("allow-explosions", explodeblocks);
 			ihatewolves=j2properties.getBoolean("ihatewolves", false);
 			conf_general.put("disable-wolves", ihatewolves);
 			maintenance = j2properties.getBoolean("maintenance",false);
 			conf_maint.put("enable", maintenance);
 			maintmessage = j2properties.getString("maintmessage","Server offline for maintenance");
 			conf_maint.put("message", maintmessage);
 			trustedonly=j2properties.getBoolean("trustedonly",false);
 			conf_general.put("block-nontrusted", trustedonly);
 			randomcolor=j2properties.getBoolean("randcolor",false);
 			conf_general.put("random-namecolor", randomcolor);
 			String superBlacklist = j2properties.getString("superblacklist", "0");
 			conf_blacklists.put("prevent-trusted", superBlacklist);
 			String regBlacklist = j2properties.getString("regblacklist", "0");
 			conf_blacklists.put("prevent-general", regBlacklist);
 			String watchList = j2properties.getString("watchlist","0");
 			conf_blacklists.put("watchlist", watchlist);
 			String summonList = j2properties.getString("summonlist","0");
 			conf_blacklists.put("prevent-summon", summonList);
 			mcbansapi=j2properties.getString("mcbans-api", "");
 			conf_general.put("mcbans-api", mcbansapi);
 			mcbouncerapi=j2properties.getString("mcbouncer-api", "");
 			conf_general.put("mcbouncer-api", mcbouncerapi);
 			String[] jail=j2properties.getString("jail","10,11,10,0,0").split(",");
 			conf_general.put("jail-xyzpy", j2properties.getString("jail"));
 			this.jail.jailSet(jail);
 			superblacklist=new ArrayList<Integer>();
 			itemblacklist=new ArrayList<Integer>();
 			watchlist=new ArrayList<Integer>();
 			summonlist=new ArrayList<Integer>();
 			for(String s:superBlacklist.split(",")){
 				if(s!=null){
 					superblacklist.add(Integer.valueOf(s));
 				}
 			}
 			for(String s:regBlacklist.split(",")){
 				if(s!=null){
 					itemblacklist.add(Integer.valueOf(s));
 				}
 			}
 			for(String s:watchList.split(",")){
 				if(s!=null){
 					watchlist.add(Integer.valueOf(s));
 				}
 			}
 			for(String s:summonList.split(",")){
 				if(s!=null){
 					summonlist.add(Integer.valueOf(s));
 				}
 			}
 			if(safemode){
 				Player[] online=getServer().getOnlinePlayers();
 				if(online.length>0){
 					for(Player p:online){
 						if(p!=null)
 							damage.protect(p.getName());
 					}
 				}
 			}
 			else {
 				damage.clear();
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Exception while reading from j2.properties", e);
 		}
 		conf.setProperty("General", conf_general);
 		conf.setProperty("MySQL", conf_mysql);
 		conf.setProperty("IRC", conf_irc);
 		conf.setProperty("Maintenance", conf_maint);
 		conf.setProperty("Tips", conf_tips);
 		conf.setProperty("Blacklists", conf_blacklists);
 		conf.save();
 		if(safemode){
 			Player[] online=getServer().getOnlinePlayers();
 			if(online.length>0){
 				for(Player p:online){
 					if(p!=null)
 						damage.protect(p.getName());
 				}
 			}
 		}
 		else {
 			damage.clear();
 		}
 		this.perms.load();
 	}
 
 
 
 	/**
 	 * Read named file
 	 * @param filename
 	 * @return array of lines
 	 */
 	public String[] readDaFile(String filename)
 	{
 
 		FileReader fileReader = null;
 		try {
 			fileReader = new FileReader(filename);
 		} catch (FileNotFoundException e2) {
 			//e2.printStackTrace();
 			log.severe("File not found: "+filename);
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
 
 
 
 
 
 	//tips
 	private void startTipsTimer() {
 		tips_stopTimer = false;
 		final Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				if (tips_stopTimer) {
 					timer.cancel();
 					return;
 				}
 				broadcastTip();
 			}
 		}, 3000, tips_delay*1000);
 	}
 
 
 	private void stopTimer() {
 		tips_stopTimer = true;
 	}
 
 	private void broadcastTip()
 	{
 		if (tips.isEmpty())
 			return;
 		String message = tips_color+"[TIP] "+tips.get(curTipNum);
 		this.chat.messageAll(message);
 		this.log(message);
 		curTipNum++;
 		if (curTipNum >= tips.size())
 			curTipNum = 0;			
 	}
 
 
 	private void loadTips() {
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
 
 	/**
 	 * Is the ID on the super-blacklist?
 	 * @param id
 	 * @return
 	 */
 	public boolean isOnSuperBlacklist(int id) {
 		return superblacklist.contains(Integer.valueOf(id));
 	}
 	/**
 	 * Is the ID on the regular blacklist?
 	 * @param id
 	 * @return
 	 */
 	public boolean isOnRegularBlacklist(int id) {
 		return itemblacklist.contains(Integer.valueOf(id));
 	}
 	/**
 	 * Is the ID being watched for summoning?
 	 * @param id
 	 * @return
 	 */
 	public boolean isOnWatchlist(int id) {
 		return watchlist.contains(Integer.valueOf(id));
 	}
 	/**
 	 * Is the ID being blocked from summoning?
 	 * @param id
 	 * @return
 	 */
 	public boolean isOnSummonlist(int id) {
 		return summonlist.contains(Integer.valueOf(id));
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
 
 
 	/**
 	 * Combine a String array from startIndex with separator
 	 * @param startIndex
 	 * @param string
 	 * @param seperator
 	 * @return
 	 */
 	public String combineSplit(int startIndex, String[] string, String seperator) {
 		StringBuilder builder = new StringBuilder();
 		for (int i = startIndex; i < string.length; i++) {
 			builder.append(string[i]);
 			builder.append(seperator);
 		}
 		builder.deleteCharAt(builder.length() - seperator.length()); 
 		return builder.toString();
 	}
 
 	/**
 	 * Does the user have this flag when authed?
 	 * @param playername
 	 * @param flag
 	 * @return
 	 */
 	public boolean reallyHasFlag(String playername, Flag flag){
 		User user=users.getUser(playername);
 		if(user!=null){
 			if(user.getUserFlags().contains(flag) || users.groupHasFlag(user.getGroup(), flag)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Does user have this flag currently?
 	 * @param playername
 	 * @param flag
 	 * @return
 	 */
 	public boolean hasFlag(String playername, Flag flag){
 		User user=users.getUser(playername);
 		if(user!=null){
 			if((flag.equals(Flag.ADMIN)||flag.equals(Flag.SRSTAFF))&&!users.isAuthed(playername)){
 				return false;
 			}
 			if(user.getUserFlags().contains(flag) || users.groupHasFlag(user.getGroup(), flag)){
 				return true;
 			}
 		}
 		else{
 			Player player=this.getServer().getPlayer(playername);
 			if(player!=null&&player.isOnline()){
 				player.kickPlayer("Rejoin in 10 seconds.");
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Lazy hasFlag
 	 * @param player
 	 * @param flag
 	 * @return
 	 */
 	public boolean hasFlag(Player player, Flag flag){
 		return this.hasFlag(player.getName(), flag);
 	}
 
 	/**
 	 * Send jail message to player
 	 * @param player
 	 */
 	public void jailMsg(Player player){
 		player.sendMessage(ChatColor.RED+"You are "+ChatColor.DARK_RED+"IN JAIL");
 		player.sendMessage(ChatColor.RED+"for violation of our server rules");
 		player.sendMessage(ChatColor.RED+"Look around you for info on freedom");
 	}
 
 	/**
 	 * Part of fakeCraftIRC. Send message to named tag.
 	 * @param message
 	 * @param tag
 	 */
 	public void craftIRC_sendMessageToTag(String message, String tag){
 		if(debug){
 			this.log("J2: Got message, tag \""+tag+"\"");
 		}
 		if(tag.equalsIgnoreCase("nocheat")){
 			irc.ircAdminMsg(message);
 			if(debug){
 				this.log("J2.2: Got message, tag \""+tag+"\"");
 			}
 		}
 	}
 
 	/**
 	 * How many sanitized users does this string match?
 	 * @param name
 	 * @return
 	 */
 	public int playerMatches(String name){
 		List<Player> list=this.minitrue.matchPlayer(name,true);
 		if(list==null){
 			return 0;
 		}
 		return list.size();
 	}
 
 	private final Map<ChatColor, String> ANSI_replacements = new EnumMap<ChatColor, String>(ChatColor.class);
 	private final ChatColor[] ANSI_colors = ChatColor.values();
 	private Terminal ANSI_terminal;
 	private ConsoleReader ANSI_reader;
 	private void initLog(){
 		this.ANSI_reader = ((CraftServer)this.getServer()).getReader();
 		this.ANSI_terminal = ANSI_reader.getTerminal();
 		ANSI_replacements.put(ChatColor.BLACK, ANSICodes.attrib(0));
 		ANSI_replacements.put(ChatColor.RED, ANSICodes.attrib(31));
 		ANSI_replacements.put(ChatColor.DARK_RED, ANSICodes.attrib(31));
 		ANSI_replacements.put(ChatColor.GREEN, ANSICodes.attrib(32));
 		ANSI_replacements.put(ChatColor.DARK_GREEN, ANSICodes.attrib(32));
 		ANSI_replacements.put(ChatColor.YELLOW, ANSICodes.attrib(33));
 		ANSI_replacements.put(ChatColor.GOLD, ANSICodes.attrib(33));
 		ANSI_replacements.put(ChatColor.BLUE, ANSICodes.attrib(34));
 		ANSI_replacements.put(ChatColor.DARK_BLUE, ANSICodes.attrib(34));
 		ANSI_replacements.put(ChatColor.LIGHT_PURPLE, ANSICodes.attrib(35));
 		ANSI_replacements.put(ChatColor.DARK_PURPLE, ANSICodes.attrib(35));
 		ANSI_replacements.put(ChatColor.AQUA, ANSICodes.attrib(36));
 		ANSI_replacements.put(ChatColor.DARK_AQUA, ANSICodes.attrib(36));
 		ANSI_replacements.put(ChatColor.WHITE, ANSICodes.attrib(37));
 	}
 
 	private String logPrep(String message){
 		if (ANSI_terminal.isANSISupported()) {
 			String result = message;
 
 			for (ChatColor color : ANSI_colors) {
 				if (ANSI_replacements.containsKey(color)) {
 					result = result.replaceAll(color.toString(), ANSI_replacements.get(color));
 				} else {
 					result = result.replaceAll(color.toString(), "");
 				}
 			}
 			return result + ANSICodes.attrib(0);
 		} else {
 			return ChatColor.stripColor(message);
 		}
 	}
 
 	/**
 	 * Add string to log, as INFO
 	 * @param message
 	 */
 	public void log(String message){
 		this.log.info(this.logPrep(message));
 	}
 
 	/**
 	 * Add string to log, as WARNING
 	 * @param message
 	 */
 	public void logWarn(String message){
 		this.log.warning(this.logPrep(message));
 	}
 
 	/**
 	 * If debugging enabled, log message.
 	 * @param message
 	 */
 	public void debug(String message){
 		if(this.debug){
 			this.log(message);
 		}
 	}
 
 	/**
 	 * Message admins and the log.
 	 * @param message
 	 */
 	public void sendAdminPlusLog(String message){
 		this.chat.messageByFlag(Flag.ADMIN, message);
 		this.log(message);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
 	 */
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		String commandName = command.getName().toLowerCase();
 		Player player=null;
 		String playerName="Console";
 		boolean isPlayer=(sender instanceof Player);
 		if(isPlayer){
 			player=(Player)sender;
 			playerName=player.getName();
 		}
 
 		if(commandName.equals("kickall")&&(!isPlayer||hasFlag(player,Flag.SRSTAFF))&&args.length>0){
 			Player[] list=getServer().getOnlinePlayers();
 			String reason=combineSplit(0,args," ");
 			if(list!=null){
 				for(int x=0;x<list.length;x++){
 					if(list[x]!=null)
 						list[x].kickPlayer(reason);
 				}
 			}
 			this.log(playerName+" kicked all: "+reason);
 			return true;
 		}
 
 		if(commandName.equals("smackirc")&&(!isPlayer||hasFlag(player,Flag.SRSTAFF))){
 			irc.getBot().quitServer("Back in a moment <3");
 			this.ircEnable=false;
 			irc.restart=true;
 			return true;
 		}
 
 
 
 		if(isPlayer && hasFlag(player,Flag.JAILED)){
 			if(commandName.equals("confess")){
 				users.getUser(player).dropFlag(Flag.JAILED);
 			}
 
 			return true;
 		}
 
 		/*if (commandName.equals("jail") && hasFlag(player, Flag.ADMIN)){
 			if(args.length<2){
 				player.sendMessage(ChatColor.RED+"Usage: /jail <playername> <reason>");
 			}
 			else {
 				String name=args[0];
 				String adminName=player.getName();
 				String reason=combineSplit(1, args, " ");
 				users.jail(name,reason,player.getName());
 				this.log("Jail: "+adminName+" jailed "+name+": "+reason);
 			}
 		}*/
 
 		if (isPlayer && commandName.equals("rules")){
 			for(String line : rules){
 				player.sendMessage(line);
 			}
 			return true;
 		}
 		if (isPlayer && commandName.equals("help")){
 			for(String line : help){
 				player.sendMessage(line);
 			}
 
 			return true;
 		}
 		if (isPlayer && commandName.equals("motd")){
 			for(String line : motd){
 				player.sendMessage(line);
 			}
 
 			return true;
 		}
 		if (isPlayer && commandName.equals("blacklist")){
 			for(String line : blacklist){
 				player.sendMessage(line);
 			}
 
 			return true;
 		}
 		if (isPlayer && commandName.equals("intro")){
 			for(String line : intro){
 				player.sendMessage(line);
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("protectme") && hasFlag(player, Flag.TRUSTED)){
 			String playersName = player.getName().toLowerCase();
 			if(tpProtect.getBoolean(playersName,false)){
 				tpProtect.setBoolean(playersName, false);
 				player.sendMessage(ChatColor.RED + "You are now no longer protected from teleportation");
 			}
 			else{
 				tpProtect.setBoolean(playersName, true);
 				player.sendMessage(ChatColor.RED + "You are protected from teleportation");
 			}
 
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("tp") && (hasFlag(player, Flag.FUN))&& args.length>0){
 			List<Player> inquest = this.minitrue.matchPlayer(args[0],this.hasFlag(player, Flag.ADMIN));
 			if(inquest.size()==1){
 				Player inquestion=inquest.get(0);
 				if(minitrue.invisible(inquestion)&&!hasFlag(player,Flag.ADMIN)){
 					player.sendMessage(ChatColor.RED+"No such player, or matches multiple");
 				}
 				if(!hasFlag(player, Flag.ADMIN) && inquestion!=null && (hasFlag(inquestion, Flag.TRUSTED)) && tpProtect.getBoolean(inquestion.getName().toLowerCase(), false)){
 					player.sendMessage(ChatColor.RED + "Cannot teleport to protected player.");
 				}
 				else if(inquestion.getName().equalsIgnoreCase(player.getName())){
 					player.sendMessage(ChatColor.RED+"Can't teleport to yourself");
 				}
 				else {
 					safePort(player, inquestion.getLocation());
 					player.sendMessage("OH GOD I'M FLYING AAAAAAAAH");
 					this.log("Teleport: " + player.getName() + " teleported to "+inquestion.getName());
 				}
 			}
 			else{
 				player.sendMessage(ChatColor.RED+"No such player, or matches multiple");
 			}
 
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("tphere") && hasFlag(player, Flag.ADMIN)){
 			List<Player> inquest = this.minitrue.matchPlayer(args[0],true);
 			if(inquest.size()==1){
 				Player inquestion=inquest.get(0);
 
 				if(inquestion.getName().equalsIgnoreCase(player.getName())){
 					player.sendMessage(ChatColor.RED+"Can't teleport yourself to yourself. Derp.");
 				}
 				else {
 					safePort(inquestion, player.getLocation());
 					inquestion.sendMessage("You've been teleported");
 					player.sendMessage("Grabbing "+inquestion.getName());
 					this.sendAdminPlusLog(ChatColor.AQUA + playerName + " pulled "+inquestion.getName()+" to self");
 				}
 			}
 			else{
 				player.sendMessage(ChatColor.RED+"No such player, or matches multiple");
 			}
 
 			return true;
 		}
 
 		if(commandName.equals("spawn") && (!isPlayer ||hasFlag(player, Flag.FUN))){
 			if(isPlayer && (!hasFlag(player, Flag.ADMIN)|| args.length<1)){
 				player.sendMessage(ChatColor.RED+"WHEEEEEEEEEEEEEEE");
 				safePort(player, player.getWorld().getSpawnLocation());
 			}
 			else if (args.length ==1){
 				List<Player> inquest = this.minitrue.matchPlayer(args[0],true);
 				if(inquest.size()==1){
 					Player inquestion=inquest.get(0);
 					safePort(inquestion, inquestion.getWorld().getSpawnLocation());
 					inquestion.sendMessage(ChatColor.RED+"OH GOD I'M BEING PULLED TO SPAWN OH GOD");
 					this.sendAdminPlusLog(ChatColor.RED+playerName+" pulled "+inquestion.getName()+" to spawn");
 				}
 				else {
 					sender.sendMessage(ChatColor.RED+"No such player, or matches multiple");
 				}
 			}
 			return true;
 		}
 
 		if(isPlayer && (commandName.equals("msg")||commandName.equals("tell"))){
 			if(args.length<2){
 				player.sendMessage(ChatColor.RED+"Correct usage: /msg player message");
 				return true;
 			}
 			List<Player> inquest = this.minitrue.matchPlayer(args[0],this.hasFlag(player, Flag.ADMIN));
 			if(inquest.size()==1){
 				this.chat.handlePrivateMessage(player,inquest.get(0),this.combineSplit(1, args, " "));
 			}
 			else{
 				player.sendMessage(ChatColor.RED+"Could not find player");
 			}
 
 			return true;
 		}
 
 		if(isPlayer && (commandName.equals("item") || commandName.equals("i")) && hasFlag(player, Flag.FUN)){
 			if (args.length < 1) {
 				player.sendMessage(ChatColor.RED+"Correct usage is: /i [item](:damage) (amount)");
 				return true;
 			}
 
 			Player playerFor = null;
 			Material material = null;
 
 			int count = 1;
 			String[] gData = null;
 			Byte bytedata = null;
 			if (args.length >= 1) {
 				gData = args[0].split(":");
 				if(gData[0].equals("0")){
 					gData[0]="1";
 				}
 				material = Material.matchMaterial(gData[0]);
 				if (gData.length == 2) {
 					try{
 						bytedata = Byte.valueOf(gData[1]);
 					}
 					catch(NumberFormatException e){
 						player.sendMessage("No such damage value. Giving you damage=0");
 					}
 				}
 			}
 			if (args.length >= 2) {
 				try {
 					count = Integer.parseInt(args[1]);
 				} catch (NumberFormatException ex) {
 					player.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a number!");
 					return false;
 				}
 			}
 			/* With this, if i want, I could limit item amounts
 			 * if(!hasFlag(player,Flag.TRUSTED)){
 				if(count>64)
 					count=64;
 				if(count<1){
 					count=1;
 				}
 			}*/
 			if (args.length == 3 && this.hasFlag(playerName, Flag.ADMIN)) {
 				playerFor = getServer().getPlayer(args[2]);
 				if (playerFor == null) {
 					player.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid player!");
 					return false;
 				}
 			} else{
 				playerFor=player;
 			}
 			if (material == null) {
 				player.sendMessage(ChatColor.RED + "Unknown item");
 				return false;
 			}
 			if(!hasFlag(player,Flag.ADMIN)&& isOnSummonlist(material.getId())){
 				player.sendMessage(ChatColor.RED+"Can't give that to you right now");
 				return true;
 			}
 			if (bytedata != null) {
 				playerFor.getInventory().addItem(new ItemStack(material, count, (short) 0, bytedata));
 			} else {
 				playerFor.getInventory().addItem(new ItemStack(material, count));
 			}
 			player.sendMessage("Given " + playerFor.getDisplayName() + " " + count + " " + material.toString());
 			this.log("Giving "+playerName+" "+count+" "+material.toString());
 			if((isOnWatchlist(material.getId()))&&(count>10||count<1)){
 				irc.ircAdminMsg("Detecting summon of "+count+" "+material.toString()+" by "+playerName);
 				this.sendAdminPlusLog(ChatColor.LIGHT_PURPLE+"Detecting summon of "+ChatColor.WHITE+count+" "+ChatColor.LIGHT_PURPLE+material.toString()+" by "+ChatColor.WHITE+playerName);
 			}
 			return true;
 		}
 
 		if(commandName.equals("time") && (!isPlayer ||hasFlag(player, Flag.ADMIN))){
 			if(args.length!=1){
 				sender.sendMessage(ChatColor.RED+"Usage: /time day|night");
 				return true;
 			}
 			long desired;
 			if(args[0].equalsIgnoreCase("day")){
 				desired=0;
 			}
 			else if(args[0].equalsIgnoreCase("night")){
 				desired=13000;
 			}
 			else{
 				sender.sendMessage(ChatColor.RED+"Usage: /time day|night");
 				return true;
 			}
 
 			long curTime=getServer().getWorlds().get(0).getTime();
 			long margin = (desired-curTime) % 24000;
 			if (margin < 0) {
 				margin += 24000;
 			}
 			getServer().getWorlds().get(0).setTime(curTime+margin);
 			this.sendAdminPlusLog(ChatColor.DARK_AQUA+playerName+" changed time");
 
 			return true;
 		}
 
 		if((commandName.equals("who") || commandName.equals("playerlist") || commandName.equals("list"))){
 			minitrue.who(sender);
 			return true;
 		}
 
 		if(commandName.equals("a") && (!isPlayer ||hasFlag(player, Flag.ADMIN))){
 			if(args.length<1){
 				sender.sendMessage(ChatColor.RED+"Usage: /a Message");
 				return true;
 			}
 			String message=combineSplit(0, args, " ");
 			chat.adminOnlyMessage(playerName,message);
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("report")){
 			if(args.length>0){
 				String theReport=combineSplit(0, args, " ");
 				if(!this.hasFlag(player, Flag.ADMIN)){
 					Report report=new Report(0, player.getLocation(), player.getName(), theReport, (new Date().getTime())/1000,false);
 					reports.addReport(report);
 					player.sendMessage(ChatColor.RED+"Report received. Thanks! :)");
 					player.sendMessage(ChatColor.RED+"Assuming you gave a description, we will handle it");
 				}
 				else{
 					String message=ChatColor.LIGHT_PURPLE+"Report from the field: <"+ChatColor.RED+playerName+ChatColor.LIGHT_PURPLE+"> "+ChatColor.WHITE+theReport;
 					this.sendAdminPlusLog(message);
 					this.irc.ircAdminMsg(ChatColor.stripColor(message));
 					player.sendMessage(ChatColor.RED+"Report transmitted. Thank you soldier.");
 				}
 
 			}
 			else {
 				player.sendMessage(ChatColor.RED+"To report to the admins, say /report MESSAGE");
 				player.sendMessage(ChatColor.RED+"Where MESSAGE is what you want to tell them");
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("r") && hasFlag(player, Flag.ADMIN)){
 			if(args.length==0){
 				ArrayList<Report> reps=reports.getReports();
 				int size=reps.size();
 				if(size==0){
 					player.sendMessage(ChatColor.RED+"No reports. Hurray!");
 
 					return true;
 				}
 				player.sendMessage(ChatColor.DARK_PURPLE+"Found "+size+" reports:");
 				for(Report r:reps){
 					if(!r.closed()){
 						Location location=r.getLocation();
 						String x=ChatColor.GOLD.toString()+location.getBlockX()+ChatColor.DARK_PURPLE+",";
 						String y=ChatColor.GOLD.toString()+location.getBlockY()+ChatColor.DARK_PURPLE+",";
 						String z=ChatColor.GOLD.toString()+location.getBlockZ()+ChatColor.DARK_PURPLE;
 						player.sendMessage(ChatColor.DARK_PURPLE+"["+r.getID()+"]["+x+y+z+"]<"
 								+ChatColor.GOLD+r.getUser()+ChatColor.DARK_PURPLE+"> "+ChatColor.WHITE
 								+r.getMessage());
 					}
 				}
 			}
 			else{
 				String action=args[0].toLowerCase();
 				if(action.equals("close")){
 					if(args.length>2){
 						int id=Integer.parseInt(args[1]);
 						if(id!=0){
 							this.reports.close(id, playerName, this.combineSplit(2, args, " "));
 							player.sendMessage(ChatColor.DARK_PURPLE+"Report closed");
 						}
 					}
 					else{
 						player.sendMessage(ChatColor.DARK_PURPLE+"/r close ID reason");
 					}
 				}
 				if(action.equals("tp")){
 					if(args.length>1){
 						Report report=this.reports.getReport(Integer.valueOf(args[1]));
 						if(report!=null){
 							safePort(player, report.getLocation());
 							player.sendMessage(ChatColor.DARK_PURPLE+"Wheeeeeeeee");
 						}
 						else{
 							player.sendMessage(ChatColor.DARK_PURPLE+"Report not found");
 						}
 					}
 					else{
 						player.sendMessage(ChatColor.DARK_PURPLE+"/r tp ID");
 					}
 				}
 			}
 
 			return true;
 		}
 		if(commandName.equals("g") && (!isPlayer ||hasFlag(player, Flag.ADMIN))){
 			if(args.length<1){
 				sender.sendMessage(ChatColor.RED+"Usage: /g Message");
 				return true;
 			}
 			String text = "";
 			text+=combineSplit(0, args, " ");
 			chat.globalAdminMessage(playerName,text);
 			return true;
 		}
 		if((commandName.equals("ban")||commandName.equals("b")) && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 2){
 				sender.sendMessage(ChatColor.RED+"Usage: /ban playername reason");
 				sender.sendMessage(ChatColor.RED+"       reason can have spaces in it");
 				return true;
 			}
 			Location loc;
 			if(!isPlayer){
 				loc=new Location(getServer().getWorlds().get(0),0,0,0);
 			}
 			else{
 				loc=player.getLocation();
 			}
 			kickbans.callBan(playerName,args,loc);
 			return true;
 		}
 		if((commandName.equals("kick")||commandName.equals("k")) && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 2){
 				sender.sendMessage(ChatColor.RED+"Usage: /kick playername reason");
 				return true;
 			}
 			kickbans.callKick(args[0],playerName,combineSplit(1, args, " "));
 			return true;
 		}
 		if(commandName.equals("addban") && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 2){
 				sender.sendMessage(ChatColor.RED+"Usage: /addban playername reason");
 				sender.sendMessage(ChatColor.RED+"        reason can have spaces in it");
 				return true;
 			}
 			Location loc;
 			if(!isPlayer){
 				loc=new Location(getServer().getWorlds().get(0),0,0,0);
 			}
 			else{
 				loc=player.getLocation();
 			}
 			kickbans.callAddBan(playerName,args,loc);
 			return true;
 		}
 
 		if((commandName.equals("unban") || commandName.equals("pardon")) && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 1){
 				sender.sendMessage(ChatColor.RED+"Usage: /unban playername");
 				return true;
 			}
 			String name=args[0];
 			kickbans.unban(playerName, name);
 			return true;
 		}
 
 
 		if(isPlayer && commandName.equals("trust")){
 			if(this.hasFlag(player,Flag.ADMIN)){
 				if(args.length<2 || !(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("drop"))){
 					player.sendMessage(ChatColor.RED+"Usage: /trust add/drop player");
 					return true;
 				}
 				String name=args[1];
 				if(args[0].equalsIgnoreCase("add")){
 					users.addFlag(name,Flag.TRUSTED);
 				}
 				else {
 					users.dropFlag(name,Flag.TRUSTED);
 				}
 				String tolog=ChatColor.RED+player.getName()+" changed flags: "+name + " "+ args[0] +" flag "+ Flag.TRUSTED.getDescription();
 				this.sendAdminPlusLog(tolog);
 			}
 			else{
 				player.sendMessage(ChatColor.AQUA+"Trusted status gives special privileges");
 				player.sendMessage(ChatColor.AQUA+"You want it? Visit our forums Minecraft section");
 				player.sendMessage(ChatColor.AQUA+"http://forums.joe.to");
 			}
 			return true;
 
 		}
 
 		if(commandName.equals("getflags") && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length==0){
 				sender.sendMessage(ChatColor.RED+"/getflags playername");
 				return true;
 			}
 			List<Player> match = this.minitrue.matchPlayer(args[0],true);
 			if(match.size()!=1 || match.get(0)==null){
 				sender.sendMessage(ChatColor.RED+"Player not found");
 
 				return true;
 			}
 			Player who=match.get(0);
 			String message="Player "+match.get(0).getName()+": ";
 			for(Flag f: users.getAllFlags(who)){
 				message+=f.getDescription()+", ";
 			}
 			sender.sendMessage(ChatColor.RED+message);
 			this.log(playerName+" looked up "+ who.getName());
 			return true;
 		}
 
 		if(commandName.equals("getgroup")&&(!isPlayer||hasFlag(player,Flag.ADMIN))){
 			if(args.length==0){
 				sender.sendMessage(ChatColor.RED+"/getgroup playername");
 				return true;
 			}
 			List<Player> match = this.minitrue.matchPlayer(args[0],true);
 			if(match.size()!=1 || match.get(0)==null){
 				sender.sendMessage(ChatColor.RED+"Player not found");
 				return true;
 			}
 			Player who=match.get(0);
 			String message="Player "+match.get(0).getName()+": "+users.getUser(who).getGroup();
 			sender.sendMessage(ChatColor.RED+message);
 			this.log(playerName+" looked up "+ who.getName());
 			return true;
 		}
 
 		if (isPlayer && commandName.equals("me") && args.length>0)
 		{
 			this.chat.handleChat(player, this.combineSplit(0, args, " "), true);
 			return true;
 		}
 
 		/*if (commandName.equals("forcekick") && hasFlag(player, Flag.ADMIN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"Usage: /forcekick playername");
 				player.sendMessage(ChatColor.RED+"       Requires full name");
 
 				return true;
 			}
 			String name=args[0];
 			String reason="";
 			String admin=player.getName();
 			if(args.length>1)
 				reason=combineSplit(1, args, " ");
 			kickbans.forceKick(name,reason);
 			log.log(Level.INFO, "Kicking " + name + " by " + admin + ": " + reason);
 			chat.msgByFlag(Flag.ADMIN,ChatColor.RED + "Kicking " + name + " by " + admin + ": " + reason);
 			chat.msgByFlagless(Flag.ADMIN,ChatColor.RED + name+" kicked ("+reason+")");
 
 			return true;
 		}*/
 		if(commandName.equals("ircrefresh") && (!isPlayer ||hasFlag(player, Flag.SRSTAFF))){
 			irc.reloadIRCAdmins();
 			chat.messageByFlag(Flag.SRSTAFF, ChatColor.RED+"IRC admins reloaded by "+playerName);
 			this.log(playerName+ " reloaded irc admins");
 
 			return true;
 		}
 
 		if(commandName.equals("j2reload") && (!isPlayer ||hasFlag(player, Flag.SRSTAFF))){
 			loadData();
 			chat.messageByFlag(Flag.SRSTAFF, "j2 data reloaded by "+playerName);
 			this.log("j2 data reloaded by "+playerName);
 			return true;
 		}
 
 		if(commandName.equals("maintenance") && (!isPlayer ||hasFlag(player, Flag.SRSTAFF))){
 			if(!maintenance){
 				this.sendAdminPlusLog(ChatColor.AQUA+playerName+" has turned on maintenance mode");
 				maintenance=true;
 				for (Player p : getServer().getOnlinePlayers()) {
 					if (p != null && !hasFlag(p, Flag.ADMIN)) {
 						p.sendMessage(ChatColor.AQUA+"Server entering maintenance mode");
 						p.kickPlayer("Server entering maintenance mode");
 					}
 				}
 			}
 			else{
 				this.sendAdminPlusLog(ChatColor.AQUA+playerName+" has turned off maintenance mode");
 				maintenance=false;
 			}
 
 			return true;
 		}
 
 		if(commandName.equals("flags") && (!isPlayer ||hasFlag(player, Flag.SRSTAFF))){
 
 			if(args.length<3){
 				sender.sendMessage(ChatColor.RED+"Usage: /flags player add/drop flag");
 				return true;
 			}
 			String action=args[1];
 			if(!(action.equalsIgnoreCase("add") || action.equalsIgnoreCase("drop"))){
 				sender.sendMessage(ChatColor.RED+"Usage: /flags player add/drop flag");
 				return true;
 			}
 
 			String name=args[0];
 			char flag=args[2].charAt(0);
 			if(action.equalsIgnoreCase("add")){
 				users.addFlag(name,Flag.byChar(flag));
 			}
 			else {
 				users.dropFlag(name,Flag.byChar(flag));
 			}
 			String tolog=ChatColor.RED+playerName+" changed flags: "+name + " "+ action +" flag "+ Flag.byChar(flag).getDescription();
 			chat.messageByFlag(Flag.ADMIN, tolog);
 			this.log(tolog);
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("warp") && hasFlag(player, Flag.FUN)) {
 			if(args.length==0){
 				String warps_s=warps.listWarps(player);
 				if(!warps_s.equalsIgnoreCase("")){
 					player.sendMessage(ChatColor.RED+"Warp locations: "+ChatColor.WHITE+warps_s);
 					player.sendMessage(ChatColor.RED+"To go to a warp, say /warp warpname");
 
 				}else{
 					player.sendMessage("The are no warps available.");
 				}
 			}
 			else{
 				String target=args[0];
 				Warp warp=warps.getPublicWarp(target);
 				if(warp!=null && (hasFlag(player, warp.getFlag())||warp.getFlag().equals(Flag.Z_SPAREWARP_DESIGNATION))){
 					player.sendMessage(ChatColor.RED+"Welcome to: "+ChatColor.LIGHT_PURPLE+target);
 					this.log(ChatColor.AQUA+"Player "+playerName+" went to warp "+target);
 					safePort(player, warp.getLocation());
 				}
 				else {
 					player.sendMessage(ChatColor.RED+"Warp does not exist. For a list, say /warp");
 				}
 
 			}
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("home") && hasFlag(player, Flag.FUN)) {
 			if(args.length==0){
 				String homes_s=warps.listHomes(player.getName());
 				if(!homes_s.equalsIgnoreCase("")){
 					player.sendMessage(ChatColor.RED+"Homes: "+ChatColor.WHITE+homes_s);
 					player.sendMessage(ChatColor.RED+"To go to a home, say /home homename");
 
 				}else{
 					player.sendMessage(ChatColor.RED+"You have no homes available.");
 					player.sendMessage(ChatColor.RED+"Use the command /sethome");
 				}
 			}
 			else{
 				Warp home=warps.getUserWarp(player.getName(),args[0]);
 				if(home!=null){
 					player.sendMessage(ChatColor.RED+"Whoosh!");
 					safePort(player, home.getLocation());
 				}
 				else {
 					player.sendMessage(ChatColor.RED+"That home does not exist. For a list, say /home");
 				}
 
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("setwarp") && hasFlag(player, Flag.ADMIN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"Usage: /setwarp warpname");
 				player.sendMessage(ChatColor.RED+"optional: /setwarp warpname flag");
 				player.sendMessage(ChatColor.RED+"Admin flag is a, trusted is t");
 			}
 			else{
 				Flag flag=Flag.Z_SPAREWARP_DESIGNATION;
 				if(args.length>1){
 					flag=Flag.byChar(args[1].charAt(0));
 				}
 				Warp newWarp=new Warp(args[0], player.getName(), player.getLocation(), flag);
 				warps.addWarp(newWarp);
 				player.sendMessage(ChatColor.RED+"Warp created");
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("sethome") && hasFlag(player, Flag.FUN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"Usage: /sethome name");
 			}
 			else{
 				Warp newWarp=new Warp(args[0], player.getName(), player.getLocation(), Flag.byChar('0'));
 				warps.addWarp(newWarp);
 				player.sendMessage(ChatColor.RED+"Home created");
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("removewarp") && hasFlag(player, Flag.ADMIN) && args.length>0){
 			String toRemove=args[0];
 			player.sendMessage(ChatColor.RED+"Removing warp "+toRemove);
 			warps.killWarp(warps.getPublicWarp(toRemove));
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("removehome") && hasFlag(player,Flag.FUN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"Usage: /removehome homename");
 				if(hasFlag(player, Flag.ADMIN)){
 					player.sendMessage(ChatColor.RED+"Or: /removehome homename playername");
 				}
 			}
 			if(args.length==1){
 				String toRemove=args[0];
 				player.sendMessage(ChatColor.RED+"Removing home "+toRemove);
 				warps.killWarp(warps.getUserWarp(player.getName(), toRemove));
 			}
 			if(args.length==2 && hasFlag(player, Flag.ADMIN)){
 				String toRemove=args[0];
 				String plr=args[1];
 				player.sendMessage(ChatColor.RED+"Removing home "+toRemove+" of player "+plr);
 				warps.killWarp(warps.getUserWarp(plr, toRemove));
 			}
 
 			return true;
 		}
 		if(isPlayer && (commandName.equals("homeinvasion")||
 				commandName.equals("invasion")||
 				commandName.equals("hi"))
 				&& hasFlag(player,Flag.ADMIN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"Usage: /homeinvasion player");
 				player.sendMessage(ChatColor.RED+"      to get a list");
 				player.sendMessage(ChatColor.RED+"       /homeinvasion player homename");
 				player.sendMessage(ChatColor.RED+"      to visit a specific home");
 			}
 			if(args.length==1){
 				String target=args[0];
 				boolean isOnline=users.isOnline(target);
 				if(!isOnline){
 					warps.loadPlayer(target);
 				}
 				player.sendMessage(ChatColor.RED+target+" warps: "+ChatColor.WHITE+warps.listHomes(target));
 				if(!isOnline){
 					warps.dropPlayer(target);
 				}
 			}
 			if(args.length==2){
 				String target=args[0];
 				boolean isOnline=users.isOnline(target);
 				if(!isOnline){
 					warps.loadPlayer(target);
 				}
 				Warp warptarget=warps.getUserWarp(target, args[1]);
 				if(warptarget!=null){
 					player.sendMessage(ChatColor.RED+"Whooooosh!  *crash*");
 					safePort(player, warptarget.getLocation());
 				}
 				else {
 					player.sendMessage(ChatColor.RED+"No such home");
 				}
 				if(!isOnline){
 					warps.dropPlayer(target);
 				}
 			}
 
 			return true;
 		}
 		if(commandName.equals("clearinventory")||commandName.equals("ci")&&hasFlag(player,Flag.FUN)){
 			Player target = null;
 			if(isPlayer && args.length==0){
 				target=player;
 				player.sendMessage(ChatColor.RED+"Inventory emptied");
 				this.log(ChatColor.RED+player.getName()+" emptied inventory");
 			}
 			else if(args.length==1 && (!isPlayer||hasFlag(player,Flag.ADMIN))){
 				List<Player> targets=this.minitrue.matchPlayer(args[0],true);
 				if(targets.size()==1){
 					target=targets.get(0);
 					target.sendMessage(ChatColor.RED+"Your inventory has been cleared by an admin");
 					this.sendAdminPlusLog(ChatColor.RED+playerName+" emptied inventory of "+target.getName());
 				}
 				else {
 					sender.sendMessage(ChatColor.RED+"Found "+targets.size()+" matches. Try again");
 				}
 			}
 			if(target!=null){
 				PlayerInventory targetInventory=target.getInventory();
 				targetInventory.clear(36);
 				targetInventory.clear(37);
 				targetInventory.clear(38);
 				targetInventory.clear(39);
 				targetInventory.clear();
 			}
 			return true;
 		}
 		if(isPlayer && commandName.equals("removeitem")){
 			player.getInventory().clear(player.getInventory().getHeldItemSlot());
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("mob") && hasFlag(player, Flag.SRSTAFF)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"/mob mobname");
 			}
 			else {
 				CreatureType creat=CreatureType.fromName(args[0]);
 				if(creat!=null){
 					Block block=player.getTargetBlock(null, 50);
 					if(block!=null){
 						Location bloc=block.getLocation();
 						if(bloc.getY()<126){
 							Location loc=new Location(bloc.getWorld(),bloc.getX(),bloc.getY()+1,bloc.getZ());
 							player.getWorld().spawnCreature(loc, CreatureType.fromName(args[0]));
 						}
 					}
 				}
 			}
 			return true;
 		}
 		if(isPlayer && commandName.equals("kibbles")
 				&&hasFlag(player, Flag.ADMIN)){
 			this.sendAdminPlusLog( ChatColor.RED+playerName+" enabled GODMODE");
 			//chat.msgByFlagless(Flag.ADMIN,ChatColor.DARK_RED+"!!! "+ChatColor.RED+playerName+" is ON FIRE "+ChatColor.DARK_RED+"!!!");
 			if(args.length>0&&args[0].equalsIgnoreCase("a"))
 				chat.messageByFlagless(Flag.ADMIN,ChatColor.RED+"    "+playerName+" is an admin. Pay attention to "+playerName);
 			users.getUser(playerName).tempSetColor(ChatColor.RED);
 			damage.protect(playerName);
 			player.getInventory().setHelmet(new ItemStack(51));
 			this.users.addFlagLocal(playerName, Flag.GODMODE);
 			return true;
 		}
 		if(isPlayer && commandName.equals("bits")
 				&&hasFlag(player, Flag.ADMIN)){
 			String name=player.getName();
 			player.sendMessage(ChatColor.RED+"You fizzle out");
 			this.sendAdminPlusLog( ChatColor.RED+playerName+" disabled GODMODE");
 			users.getUser(name).restoreColor();
 			player.getInventory().clear(39);
 			if(!safemode){
 				damage.danger(playerName);
 				player.sendMessage(ChatColor.RED+"You are no longer safe");
 			}
 			this.users.dropFlagLocal(playerName, Flag.GODMODE);
 			return true;
 		}
 		if(isPlayer && (commandName.equals("coo")||
 				commandName.equals("xyz"))
 				&&hasFlag(player, Flag.ADMIN)){
 			if(args.length<3){
 				player.sendMessage(ChatColor.RED+"You did not specify an X, Y, and Z");
 			}
 			else {
 				safePort(player, new Location(player.getWorld(),Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2]),0,0));
 				player.sendMessage(ChatColor.RED+"WHEEEEE I HOPE THIS ISN'T UNDERGROUND");
 			}
 
 			return true;
 		}
 		if(commandName.equals("whereis") && (!isPlayer ||hasFlag(player,Flag.ADMIN))){
 			if(args.length==0){
 				sender.sendMessage(ChatColor.RED+"/whereis player");
 			}
 			else {
 				List<Player> possible=this.minitrue.matchPlayer(args[0],true);
 				if(possible.size()==1){
 					Player who=possible.get(0);
 					Location loc=who.getLocation();
 					sender.sendMessage(ChatColor.RED+who.getName()+": "+loc.getX()+" "+loc.getY()+" "+loc.getZ());
 				}
 				else {
 					sender.sendMessage(ChatColor.RED+args[0]+" does not work. Either 0 or 2+ matches.");
 				}
 			}
 
 			return true;
 		}
 		if(commandName.equals("madagascar")&&(!isPlayer||hasFlag(player,Flag.SRSTAFF))){
 			this.madagascar(playerName);
 			return true;
 		}
 		if(commandName.equals("lookup")&&isPlayer&&hasFlag(player,Flag.ADMIN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.LIGHT_PURPLE+"/lookup player");
 				return true;
 			}
 			String name=args[0];
 			this.log(ChatColor.LIGHT_PURPLE+playerName+" looked up "+name);
 			this.banCoop.lookup(name, player);
 			return true;
 		}
 		if(commandName.equals("j2lookup")&&isPlayer&&hasFlag(player,Flag.ADMIN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.LIGHT_PURPLE+"/j2lookup player");
 				return true;
 			}
 			String target=args[0];
 			this.log(ChatColor.LIGHT_PURPLE+"[j2bans] "+playerName+" looked up "+target);
 			String x="";
 			boolean allbans=false;
 			if(args.length>1&&args[1].equalsIgnoreCase("all")){
 				allbans=true;
 			}
 			ArrayList<Ban> bans=this.mysql.getBans(target,allbans);
 			ArrayList<String> messages=new ArrayList<String>();
 			boolean banned=false;
 			for(Ban ban:bans){
 				if(ban.isBanned()){
 					x=ChatColor.DARK_RED+"X";
 					banned=true;
 				}
 				else{
 					x=ChatColor.GREEN+"U";
 				}
 				String c=ChatColor.DARK_AQUA.toString();
 				messages.add(c+"["+x+c+"] "+this.shortdateformat.format(new Date(ban.getTimeOfBan()*1000))+" "+ChatColor.GOLD+ban.getReason());
 			}
 			String c2=ChatColor.GREEN.toString();
 			if(banned){
 				c2=ChatColor.RED.toString();
 			}
 			player.sendMessage(ChatColor.AQUA+"Found "+ChatColor.GOLD+bans.size()+ChatColor.AQUA+" bans for "+c2+target);
 			for(String message:messages){
 				player.sendMessage(message);
 			}
 			return true;
 		}
 		if(isPlayer && commandName.equals("iplookup") && hasFlag(player, Flag.ADMIN)){
 			if(args.length == 1){
 				String lastIP = mysql.IPGetLast(args[0]);
 
 				if(!lastIP.isEmpty())
 				{
 					player.sendMessage(ChatColor.AQUA+"IPLookup on "+ChatColor.WHITE+args[0]+ChatColor.AQUA+"\'s last IP: "+ChatColor.WHITE+lastIP);
 					HashMap<String, Long> nameDates = mysql.IPGetNamesOnIP(lastIP);
 					if(!nameDates.isEmpty()){
 						for(String key :nameDates.keySet()){
 							if(!key.isEmpty() && key.toLowerCase() != "null")
 							{
 								Long time = nameDates.get(key);
 								Date date = new Date(time);
 								player.sendMessage(ChatColor.AQUA+key+" : "+ChatColor.BLUE+date);
 							}
 						}
 					}
 				}
 				else{
 					player.sendMessage(ChatColor.AQUA+"Could not find any matches.");
 				}
 			}
 			else{
 				player.sendMessage(ChatColor.RED+"Invalid Usage, /iplookup <player_name>");
 			}
 
 			return true;
 		}
 		if(commandName.equals("smite")&&(!isPlayer||hasFlag(player,Flag.ADMIN))){
 			if(args.length==0){
 				sender.sendMessage(ChatColor.RED+"/smite player");
 				return true;
 			}
 			List<Player> results=this.minitrue.matchPlayer(args[0],true);
 			if(results.size()==1){
 				Player target=results.get(0);
 				boolean weather=target.getWorld().isThundering();
 				this.damage.danger(target.getName());
 				this.damage.addToTimer(target.getName());
 				target.getWorld().strikeLightning(target.getLocation());
 				//player.sendMessage(ChatColor.RED+"Judgment enacted");
 				this.sendAdminPlusLog( ChatColor.RED+playerName+" has zapped "+target.getName());
 				target.sendMessage(ChatColor.RED+"You have been judged");
 				//this.damage.processJoin(playerName);
 				target.getWorld().setStorm(weather);
 			}
 			else if(results.size()>1){
 				sender.sendMessage(ChatColor.RED+"Matches too many players");
 			}
 			else{
 				sender.sendMessage(ChatColor.RED+"Matches no players");
 			}
 			return true;
 		}
 		if(commandName.equals("storm")&&(isPlayer)&&hasFlag(player,Flag.ADMIN)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"/storm start/stop");
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("start")){
 				player.getWorld().setStorm(true);
 				this.sendAdminPlusLog(ChatColor.RED+playerName+" starts up a storm");
 				this.chat.messageByFlagless(Flag.ADMIN, ChatColor.RED+"Somebody has started a storm!");
 			}
 			if(args[0].equalsIgnoreCase("stop")){
 				player.getWorld().setStorm(false);
 				this.sendAdminPlusLog(ChatColor.RED+playerName+" stops the storm");
 				this.chat.messageByFlagless(Flag.ADMIN, ChatColor.RED+"Somebody has prevented a storm!");
 			}
 			return true;
 		}
 		/*if(commandName.equals("pvpon")&&isPlayer&&(safemode||hasFlag(player,Flag.ADMIN))){
 			if(args.length>0&&hasFlag(player,Flag.ADMIN)){
 				damage.dangerP(args[0]);
 				player.sendMessage(ChatColor.RED+args[0]+" can be smacked by fellow players");
 				this.log(playerName+" enabled PvP on "+args[0]);
 				return true;
 			}
 			damage.dangerP(playerName);
 			player.sendMessage(ChatColor.RED+"You can be smacked by fellow players");
 			this.log(playerName+" enabled PvP on self");
 			return true;
 		}
 		if(commandName.equals("pvpoff")&&isPlayer&&(safemode||hasFlag(player,Flag.ADMIN))){
 			if(args.length>0&&hasFlag(player,Flag.ADMIN)){
 				damage.protectP(args[0]);
 				player.sendMessage(ChatColor.RED+args[0]+" is safe from fellow players");
 				this.log(playerName+" disabled PvP on "+args[0]);
 				return true;
 			}
 			damage.protectP(playerName);
 			player.sendMessage(ChatColor.RED+"You are safe from fellow players");
 			this.log(playerName+" disabled PvP on self");
 			return true;
 		}
 		if(commandName.equals("woof") && (!isPlayer ||hasFlag(player,Flag.ADMIN))){
 			if(args.length==0){
 				sender.sendMessage(ChatColor.RED+"/woof player");
 				return true;
 			}
 			if(damage.woof(args[0])){
 				player.sendMessage("Dirty deed done");
 			}
 			else{
 				player.sendMessage("Dirty deed fail");
 			}
 			return true;
 		}*/
 		if(isPlayer && commandName.equals("ixrai12345")||commandName.equals("cjbmodsxray")){
 			kickbans.ixrai(playerName,commandName);
 			return true;
 		}
 		if(commandName.equals("ircmsg")&&!isPlayer){
 			if(args.length<2){
 				return false;
 			}
 			irc.getBot().sendMessage(args[0], this.combineSplit(1, args, " "));
 			return true;
 		}
 		if(commandName.equals("flex")&&(!isPlayer||hasFlag(player,Flag.SRSTAFF)||playerName.equalsIgnoreCase("MrBlip"))){
 			String message=""+ChatColor.GOLD;
 			switch(this.random.nextInt(5)){
 			case 0:
 				message+="All the ladies watch as "+playerName+" flexes";break;
 			case 1:
 				message+="Everybody stares as "+playerName+" flexes";break;
 			case 2:
 				message+="Sexy party! "+playerName+" flexes and the gods stare";break;
 			case 3:
 				message+=playerName+" is too sexy for this party";break;
 			case 4: 
 				message+=playerName+" knows how to flex";break;
 			}
 			if(playerName.equalsIgnoreCase("MrBlip")&&random.nextBoolean()){
 				if(random.nextBoolean())
 					message=ChatColor.GOLD+"MrBlip shows off his chin";
 				else
 					message=ChatColor.GOLD+"MrBlip shows off his hat";
 			}
 			chat.messageAll(message);
 			this.log(ChatColor.GOLD+playerName+" flexed.");
 			return true;
 		}
 		//DO NOT USE THIS COMMAND FOR YOUR BENEFIT. IT IS FOR TESTING.
 		if(commandName.equals("thor")&&isPlayer&&hasFlag(player,Flag.ADMIN)){
 			if(hasFlag(player,Flag.THOR)){
 				player.sendMessage(ChatColor.GOLD+"You lose your mystical powers");
 				users.dropFlagLocal(playerName, Flag.THOR);
 			}
 			else {
 				player.sendMessage(ChatColor.GOLD+"You gain mystical powers");
 				users.addFlagLocal(playerName, Flag.THOR);
 			}
 			return true;
 		}
 		if(commandName.equals("slay")&&(!isPlayer||hasFlag(player,Flag.ADMIN))){
 			if(args.length==0){
 				sender.sendMessage(ChatColor.RED+"I can't kill anyone if you don't tell me whom");
 				return true;
 			}
 			List<Player> list=this.minitrue.matchPlayer(args[0],true);
 			if(list.size()==0){
 				sender.sendMessage(ChatColor.RED+"That matches nobody, smart stuff");
 				return true;
 			}
 			if(list.size()>1){
 				sender.sendMessage(ChatColor.RED+"That matches more than one, smart stuff");
 				return true;
 			}
 			Player target=list.get(0);
 			if(target!=null){
 				target.damage(9001);
 				target.sendMessage(ChatColor.RED+"You have been slayed");
 				this.sendAdminPlusLog(ChatColor.RED+playerName+" slayed "+target.getName());
 			}
 			return true;
 		}
 		if(commandName.equals("amitrusted")){
 			if(!isPlayer||this.hasFlag(player,Flag.ADMIN)){
 				sender.sendMessage(ChatColor.AQUA+"You're a sexy beast");
 			}
 			else{
 				if(hasFlag(player, Flag.TRUSTED)){
 					player.sendMessage(ChatColor.AQUA+"You are trusted! Yay!");
 				}
 				else{
 					player.sendMessage(ChatColor.AQUA+"You are not trusted. Get it!");
 					player.sendMessage(ChatColor.AQUA+"Visit http://forums.joe.to   Minecraft section");
 				}
 			}
 			return true;
 		}
 		if(isPlayer&&commandName.equals("vanish")){
 			if(hasFlag(player,Flag.ADMIN))
 				minitrue.vanish(player);
 			return true;
 		}
 		if(isPlayer&&commandName.equals("imatool")&&hasFlag(player,Flag.ADMIN)){
 			if(hasFlag(player,Flag.TOOLS)){
 				player.sendMessage(ChatColor.AQUA+"Tool mode disabled.");
 				users.dropFlagLocal(playerName, Flag.TOOLS);
 			}
 			else {
 				player.sendMessage(ChatColor.AQUA+"Tool mode enabled. Be careful.");
 				users.addFlagLocal(playerName, Flag.TOOLS);
 			}
 			return true;
 		}
 		if(isPlayer&&(commandName.equals("f3")||commandName.equals("loc"))){
 			Location loc=player.getLocation();
 			String x=""+ChatColor.GOLD+(int)loc.getX()+ChatColor.DARK_AQUA;
 			String y=""+ChatColor.GOLD+(int)loc.getY()+ChatColor.DARK_AQUA;
 			String z=""+ChatColor.GOLD+(int)loc.getZ();
 			player.sendMessage(ChatColor.DARK_AQUA+"You are at X:"+x+" Y:"+y+" Z:"+z);
 			return true;
 		}
 		if(commandName.equals("setspawn")&&(!isPlayer||hasFlag(player,Flag.SRSTAFF))){
 			if(args.length<3){
 				player.sendMessage(ChatColor.RED+"/setspawn x y z");
 				return true;
 			}
 			player.getWorld().setSpawnLocation(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]));
 			this.sendAdminPlusLog(ChatColor.RED+"Spawn set to "+args[0]+" "+args[1]+" "+args[2]+" by "+playerName);
 			return true;
 		}
 		if(isPlayer&&commandName.equals("auth")){
 			User user=users.getUser(player);
 			if(user!=null&&args.length==1){
 				String safeword=user.getSafeWord();
 				if(!safeword.equalsIgnoreCase("")&&safeword.equals(args[0])){
 					this.users.authenticatedAdmin(playerName);
 					this.sendAdminPlusLog(ChatColor.LIGHT_PURPLE+"[J2AUTH] "+playerName+" authenticated");
 					return true;
 				}
 			}
 			if(users.isAuthed(playerName)){
 				this.sendAdminPlusLog(ChatColor.LIGHT_PURPLE+"[J2AUTH] "+playerName+" deauthenticated");
 			}
 			this.users.resetAuthentication(player);
 			//player.sendMessage(ChatColor.LIGHT_PURPLE+"You no can has permissions");
 			this.minitrue.vanish.updateInvisible(player);
 			return true;
 		}
 		if(commandName.equals("harass")&&(!isPlayer||hasFlag(player,Flag.ADMIN))){
 			if(args.length!=1){
 				sender.sendMessage(ChatColor.AQUA+"Missing a name!");
 				return true;
 			}
 			Player target=this.getServer().getPlayer(args[0]);
 			if(target==null||!target.isOnline()){
 				sender.sendMessage(ChatColor.AQUA+"Fail. No such user \""+args[0]+"\"");
 				return true;
 			}
 			if(!this.panda.panda(target)){
 				this.panda.harass(target.getName());
 				this.sendAdminPlusLog(ChatColor.AQUA+"[HARASS] Target Acquired: "+ChatColor.DARK_AQUA+target.getName()+ChatColor.AQUA+". Thanks, "+playerName+"!");
 				this.irc.ircAdminMsg("[HARASS] Target Acquired: "+target.getName()+". Thanks, "+playerName+"!");
 			}
 			else{
 				this.panda.remove(target.getName());
 				this.sendAdminPlusLog(ChatColor.AQUA+"[HARASS] Target Removed: "+ChatColor.DARK_AQUA+target.getName()+ChatColor.AQUA+". Thanks, "+playerName+"!");
 				this.irc.ircAdminMsg("[HARASS] Target Removed: "+target.getName()+". Thanks, "+playerName+"!");
 			}
 			return true;
 		}
 		if(commandName.equals("muteall")&&(!isPlayer||hasFlag(player,Flag.ADMIN))){
 			String messageBit;
 			if(this.chat.muteAll){
 				messageBit=" has unmuted all players";
 			}
 			else{
 				messageBit=" has muted all players";
 			}
 			this.sendAdminPlusLog(ChatColor.YELLOW+playerName+messageBit);
 			this.chat.messageByFlagless(Flag.ADMIN, ChatColor.YELLOW+"The ADMIN"+messageBit);
 			this.chat.muteAll=!this.chat.muteAll;
 			return true;
 		}
 		if(commandName.equals("mute")&&(!isPlayer||hasFlag(player,Flag.ADMIN))){
 			String messageBit="";
 			if(args.length<1){
 				sender.sendMessage(ChatColor.RED+"Requires a name. /mute name");
 				return true;
 			}
 			String targetString=args[0];
 			List<Player> matches=this.getServer().matchPlayer(targetString);
 			if(matches==null||matches.size()==0){
 				sender.sendMessage(ChatColor.RED+"No matches for "+targetString);
 				return true;
 			}
 			if(matches.size()>1){
 				sender.sendMessage(ChatColor.RED+String.valueOf(matches.size())+" matches for "+targetString);
 				return true;
 			}
 			Player target=matches.get(0);
 			String targetName=target.getName();
 			boolean muted=this.hasFlag(targetName, Flag.MUTED);
 			if(muted){
 				messageBit="un";
 				this.users.dropFlagLocal(targetName,Flag.MUTED);
 			}
 			else{
 				this.users.addFlagLocal(targetName, Flag.MUTED);
 			}
 			target.sendMessage(ChatColor.YELLOW+"You have been "+messageBit.toUpperCase()+"MUTED");
 			this.sendAdminPlusLog(ChatColor.YELLOW+playerName+" has "+messageBit+"muted "+targetName);
 		}
 		if(commandName.equals("say")&&(!isPlayer||hasFlag(player,Flag.SRSTAFF))){
 			if(args.length<1){
 				sender.sendMessage(ChatColor.RED+"Dude, you gotta /say SOMETHING");
 				return true;
 			}
 			String message=ChatColor.LIGHT_PURPLE+"[SERVER] "+this.combineSplit(0, args, " ");
 			this.log(message);
 			this.chat.messageAll(message);
 			return true;
 		}
 		if(commandName.equals("nsa")&&isPlayer&&this.hasFlag(player, Flag.ADMIN)){
 			String message;
 			if(this.hasFlag(player, Flag.NSA)){
 				message=ChatColor.DARK_AQUA+playerName+ChatColor.AQUA+" takes off headphones. That's enough chatter";
 				this.users.dropFlagLocal(playerName, Flag.NSA);
 			}
 			else{
 				message=ChatColor.DARK_AQUA+playerName+ChatColor.AQUA+" puts on headphones. Intercepting...";
 				this.users.addFlagLocal(playerName, Flag.NSA);
 			}
 			this.sendAdminPlusLog(message);
 			return true;
 		}
 		if(isPlayer&&this.servernumber==2&&commandName.equals("station")){
 
 			Warp target=this.warps.getClosestWarp(player.getLocation());
 			String name=target.getName();
 			if(args.length==1&&args[0].equalsIgnoreCase("go")){
 				safePort(player, target.getLocation());
 				player.sendMessage(ChatColor.AQUA+"You are now at "+ChatColor.DARK_AQUA+"Station "+name);
 				player.sendMessage(ChatColor.AQUA+"You can return here by saying "+ChatColor.DARK_AQUA+"/warp "+name);
 			}
 			else{
 				player.sendMessage(ChatColor.AQUA+"You are closest to "+ChatColor.DARK_AQUA+"Station "+name);
 				player.sendMessage(ChatColor.AQUA+"You can always get there with "+ChatColor.DARK_AQUA+"/warp "+name);
 				player.sendMessage(ChatColor.AQUA+"To travel to the closest station say "+ChatColor.DARK_AQUA+"/station go");
 			}
 		}
 		if(isPlayer&&(commandName.equals("voteadmin")||commandName.equals("va"))&&hasFlag(player, Flag.ADMIN)){
 			this.voting.voteAdminCommand(player, args);
 		}
 		if(isPlayer&&commandName.equals("vote")){
 			this.voting.voteCommand(player, args);
 		}
 		if(args.length>0&&commandName.equals("maxplayers")&&(!isPlayer||hasFlag(player,Flag.SRSTAFF))){
 			int newCount;
 			try{
 				newCount=Integer.parseInt(args[0]);
 			}
 			catch(NumberFormatException e){
 				newCount=this.playerLimit;
 			}
 			this.playerLimit=newCount;
 			this.sendAdminPlusLog(ChatColor.RED+playerName+" set max players to "+this.playerLimit);
 		}
 		if(commandName.equals("shush")&&isPlayer&&this.hasFlag(playerName, Flag.ADMIN)){
 			if(this.hasFlag(playerName,Flag.SHUT_OUT_WORLD)){
 				this.users.dropFlagLocal(playerName, Flag.SHUT_OUT_WORLD);
 				this.sendAdminPlusLog(ChatColor.DARK_AQUA+playerName+" can now hear you again");
 			}
 			else{
 				this.users.addFlagLocal(playerName, Flag.SHUT_OUT_WORLD);
 				this.sendAdminPlusLog(ChatColor.DARK_AQUA+playerName+" has fingers to ears and is singing");
 			}
 			return true;
 		}
 		if(commandName.equals("hat")&&isPlayer&&this.hasFlag(playerName, Flag.ADMIN)){
 			ItemStack meow=player.getItemInHand();
 			if(meow.getAmount()>0&&meow.getTypeId()<256){
 				player.getInventory().setHelmet(new ItemStack(meow.getType(),1));
 				meow.setAmount(meow.getAmount()-1);
 				player.sendMessage(ChatColor.RED+"You pat your new helmet");
 			}
 			else{
 				player.sendMessage(ChatColor.RED+"You pat your head");
 			}
 			return true;
 		}
 		if((commandName.equals("note")||commandName.equals("anote"))&&isPlayer){
 			if(args.length<2){
 				player.sendMessage(ChatColor.RED+"/note username message");
 				return true;
 			}
 			boolean adminMode=false;
 			if(commandName.equals("anote")){
 				if(this.hasFlag(playerName, Flag.ADMIN)){
 					adminMode=true;
 				}
 			}
 			String targetName=args[0];
 			String message=this.combineSplit(1, args, " ");
 			List<Player> match=this.getServer().matchPlayer(targetName);
 			Player targetPlayer=null;
 			if(match.size()>0){
 				for(Player p:match){
 					if(p!=null&&p.isOnline()&&p.getName().toLowerCase().equals(targetName.toLowerCase())){
 						targetPlayer=p;
 					}
 				}
 			}
 			String a=ChatColor.AQUA.toString();
 			String da=ChatColor.DARK_AQUA.toString();
 			if(targetPlayer!=null){
 				if(adminMode){
 					targetPlayer.sendMessage(a+"HEY "+ChatColor.RED+targetPlayer.getName()+a+": "+message);
 					this.sendAdminPlusLog(a+"Priv <"+da+playerName+a+"->"+da+targetName+a+"> "+message);
 				}
 				else{
 					this.chat.handlePrivateMessage(player, targetPlayer, message);
 				}
 			}
 			else{
 				this.mysql.addNote(playerName, targetName, message, adminMode);
 				player.sendMessage(ChatColor.AQUA+"Note left for "+args[0]);
 				String bit=a+"Note <"+da+playerName+a+"->"+da+targetName+a+"> "+message;
 				this.log(bit);
 				if(adminMode){
 					this.chat.messageByFlag(Flag.ADMIN, bit);
 				}
 				else{
 					this.chat.messageByFlag(Flag.NSA, bit);
 				}
 			}
 			return true;
 		}
 		return true;
 	}
 
 	/**
 	 * SHUT. DOWN. EVERYTHING.
 	 * @param name Admin shutting down
 	 */
 	public void madagascar(String name){
 		this.sendAdminPlusLog(name+" wants to SHUT. DOWN. EVERYTHING.");
 		if(this.ircEnable){
 			if(name.equalsIgnoreCase("console")){
 				irc.getBot().sendMessage(this.ircAdminChannel, "A MAN IN BRAZIL IS COUGHING");
 			}
 			ircEnable=false;
 			irc.getBot().quitServer("SHUT. DOWN. EVERYTHING.");
 		}
 		this.maintenance=true;
 		kickbans.kickAll("We'll be back after these brief messages");
 		this.getServer().dispatchCommand(new ConsoleCommandSender(this.getServer()), "stop");
 	}
 	
 	public void safePort(Player player, Location location){
 		Entity vehicle=player.getVehicle();
 		if(vehicle!=null){
 			player.leaveVehicle();
 			vehicle.remove();
 		}
 		player.teleport(location);
 	}
 
 	SimpleDateFormat shortdateformat=new SimpleDateFormat("yyyy-MM-dd kk:mm");
 	private boolean debug;
 	private Logger log;
 
 	public ArrayList<String> protectedUsers;
 	public String[] rules, blacklist, intro, motd, help;
 
 	public String ircName,ircHost,ircChannel,ircOnJoin,gsAuth,gsPass,ircAdminChannel;
 	public ChatColor ircUserColor;
 	public boolean ircMsg,ircEcho,ircDebug;
 	public int ircCharLim,ircPort;
 	public String[] ircSeparator;
 	private String tips_location = "tips.txt";
 	private String  tips_color = ChatColor.AQUA.toString();
 	private boolean tips_stopTimer = false;
 	private int tips_delay = 120;
 	private ArrayList<String> tips;
 	private int curTipNum = 0;
 	public String[] ircLevel2;
 	public boolean ircEnable;
 	public ArrayList<Integer> itemblacklist,superblacklist,watchlist,summonlist;
 	//private int natureXmin,natureXmax,natureZmin,natureZmax;
 	public boolean maintenance=false;
 	public String maintmessage;
 	public boolean safemode;
 	public boolean explodeblocks;
 	public boolean ihatewolves;
 	public boolean trustedonly;
 	public Property tpProtect=new Property("tpProtect.list");
 	public Player OneByOne = null;
 	public boolean randomcolor;
 	public Random random = new Random();
 	public int playerLimit;
 	public int servernumber;
 	ArrayList<String> srstaffList,adminsList,trustedList;
 	public String mcbansapi,mcbouncerapi;
 }
