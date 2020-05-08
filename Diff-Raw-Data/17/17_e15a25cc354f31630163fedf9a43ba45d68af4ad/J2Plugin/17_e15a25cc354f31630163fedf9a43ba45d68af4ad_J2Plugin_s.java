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
 
 package to.joe;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.bukkit.command.*;
 import org.bukkit.entity.*;
 import org.bukkit.Location;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
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
 	private final listenPlrChat plrlisChat = new listenPlrChat(this);
 	private final listenPlrItem plrlisItem = new listenPlrItem(this);
 	private final listenPlrJoinQuit plrlisJoinQuit = new listenPlrJoinQuit(this);
 	private final listenBlock blockListener = new listenBlock(this);
 	private final listenEntity entityListener = new listenEntity(this);
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	public final managerChat chat = new managerChat(this);
 	public final managerIRC irc = new managerIRC(this);
 	public final managerKickBan kickbans = new managerKickBan(this);
 	public final managerUsers users = new managerUsers(this);
 	public final managerReport reports = new managerReport(this);
 	public final managerWarps warps = new managerWarps(this);
 	//public managerBlockLog blogger;
 	public managerMySQL mysql;
 
 
 	public void onDisable() {
 
 		irc.kill();
 		stopTimer();
 		// NOTE: All registered events are automatically unregistered when a plugin is disabled
 
 	}
 
 	public void onEnable() {
 		log=Logger.getLogger("Minecraft");
 		protectedUsers=new ArrayList<String>();
 		loadData();
 		if(debug)log.info("Data loaded");
 
 		//irc start
 		if(ircEnable)irc.prepIRC();
 		irc.startIRCTimer();
 		//if(ircEnable)irc.startIRCTimer();
 		if(debug)log.info("IRC up (or disabled)");
 		//irc end
 		loadTips();
 		if(debug)log.info("Tips loaded");
 		startTipsTimer();
 		if(debug)log.info("Tips timer started");
 
 		//Initialize BlockLogger
 		//this.blogger = new managerBlockLog(this.mysql.getConnection(),this.mysql.servnum());
 		//if(debug)log.info("Blogger init");
 		//new Thread(blogger).start();
 		//if(debug)log.info("Blogger is go");
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, plrlisChat, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_ITEM, plrlisItem, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, plrlisJoinQuit, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, plrlisJoinQuit, Priority.Normal, this);
 		if(debug)log.info("Events registered");
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
 		motd=readDaFile("motd.txt");
 		help=readDaFile("help.txt");
 		PropFile j2properties = new PropFile("j2.properties");
 		try { 
 			debug = j2properties.getBoolean("debug",false);
 			//mysql start
 			String mysql_username = j2properties.getString("user", "root");
 			String mysql_password = j2properties.getString("pass", "root");
 			String mysql_db = j2properties.getString("db", "jdbc:mysql://localhost:3306/minecraft");
 			//chatTable = properties.getString("chat","chat");
 			int mysql_server = j2properties.getInt("server-number", 0);
 			mysql = new managerMySQL(mysql_username,mysql_password,mysql_db, mysql_server, this);
 			mysql.loadMySQLData();
 			//mysql end
 
 			playerLimit=j2properties.getInt("max-players",20);
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
 			int ircuc = j2properties.getInt("irc-usercolor",15);
 			ircUserColor=mysql.toColor(ircuc);
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
 			ircLevel2 = j2properties.getString("irc-level2","").split(",");
 			safemode=j2properties.getBoolean("safemode",false);
 			maintenance = j2properties.getBoolean("maintenance",false);
 			maintmessage = j2properties.getString("maintmessage","Server offline for maintenance");
 			fun=j2properties.getBoolean("funmode",false);
 			randomcolor=j2properties.getBoolean("randcolor",false);
 			String superBlacklist = j2properties.getString("superblacklist", "0");				
 			String regBlacklist = j2properties.getString("regblacklist", "0");
 			superblacklist=new ArrayList<Integer>();
 			itemblacklist=new ArrayList<Integer>();
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
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Exception while reading from j2.properties", e);
 		}
 	}
 
 
 
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
 
 
 
 	public boolean hasFlag(Player player, Flag flag){
 		User user=users.getUser(player);
 		if(user!=null && (user.getUserFlags().contains(flag) || users.groupHasFlag(user.getGroup(), flag))){
 			return true;
 		}
 		return false;
 	}
 
 	public void jailMsg(Player player){
 		player.sendMessage(ChatColor.RED+"You are "+ChatColor.DARK_RED+"IN JAIL");
 		player.sendMessage(ChatColor.RED+"for violation of our server rules");
 		player.sendMessage(ChatColor.RED+"Look around you for info on freedom");
 	}
 
 	public void msg(Player player, String message){
 		if(player!=null){
 			player.sendMessage(ChatColor.RED+message);
 		}
 		else
 		{
 			System.out.println("J2:" + message);
 		}
 	}
 
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
 			log.info(playerName+" kicked all: "+reason);
 			return true;
 		}
 		
 		if(commandName.equals("smackirc")){
 			irc.getBot().quitServer("Back in a moment <3");
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
 				log.info("Jail: "+adminName+" jailed "+name+": "+reason);
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
 			List<Player> inquest = getServer().matchPlayer(args[0]);
 			if(inquest.size()==1){
 				Player inquestion=inquest.get(0);
 				if(!hasFlag(player, Flag.ADMIN) && inquestion!=null && (hasFlag(inquestion, Flag.TRUSTED)) && tpProtect.getBoolean(inquestion.getName().toLowerCase(), false)){
 					player.sendMessage(ChatColor.RED + "Cannot teleport to protected player.");
 				}
 				else if(inquestion.getName().equalsIgnoreCase(player.getName())){
 					player.sendMessage(ChatColor.RED+"Can't teleport to yourself");
 				}
 				else {
 					player.teleportTo(inquestion.getLocation());
 					player.sendMessage("OH GOD I'M FLYING AAAAAAAAH");
 					log.info("Teleport: " + player.getName() + " teleported to "+inquestion.getName());
 				}
 			}
 			else{
 				player.sendMessage(ChatColor.RED+"No such player, or matches multiple");
 			}
 
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("tphere") && hasFlag(player, Flag.ADMIN)){
 			List<Player> inquest = getServer().matchPlayer(args[0]);
 			if(inquest.size()==1){
 				Player inquestion=inquest.get(0);
 
 				if(inquestion.getName().equalsIgnoreCase(player.getName())){
 					player.sendMessage(ChatColor.RED+"Can't teleport yourself to yourself. Derp.");
 				}
 				else {
 					inquestion.teleportTo(player.getLocation());
 					inquestion.sendMessage("You've been teleported");
 					player.sendMessage("Grabbing "+inquestion.getName());
 					log.info("Teleport: " + player.getName() + " pulled "+inquestion.getName()+" to self");
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
 				player.teleportTo(player.getWorld().getSpawnLocation());
 			}
 			else if (args.length ==1){
 				List<Player> inquest = getServer().matchPlayer(args[0]);
 				if(inquest.size()==1){
 					Player inquestion=inquest.get(0);
 					inquestion.teleportTo(inquestion.getWorld().getSpawnLocation());
 					inquestion.sendMessage(ChatColor.RED+"OH GOD I'M BEING PULLED TO SPAWN OH GOD");
 					msg(player,"Pulled "+inquestion.getName()+" to spawn");
 				}
 				else {
 					msg(player,"No such player, or matches multiple");
 				}
 			}
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("msg")){
 			if(args.length<2){
 				player.sendMessage(ChatColor.RED+"Correct usage: /msg player message");
 
 				return true;
 			}
 			List<Player> inquest = getServer().matchPlayer(args[0]);
 			if(inquest.size()==1){
 				Player inquestion=inquest.get(0);
 				User userTo=users.getUser(inquestion);
 				User userFrom=users.getUser(playerName);
 				player.sendMessage("(MSG) <"+userTo.getColorName()+"> "+combineSplit(1, args, " "));
 				inquestion.sendMessage("(MSG) <"+userFrom.getColorName()+"> "+combineSplit(1, args, " "));
 				log.info("Msg to "+inquestion.getName()+": <"+playerName+"> "+combineSplit(1, args, " "));
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
 			if (args.length == 3) {
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
 			if (bytedata != null) {
 				playerFor.getInventory().addItem(new ItemStack(material, count, (short) 0, bytedata));
 			} else {
 				playerFor.getInventory().addItem(new ItemStack(material, count));
 			}
			if(!hasFlag(player,Flag.ADMIN)&& material.equals(Material.IRON_DOOR)||material.equals(Material.WOOD_DOOR)){
				player.sendMessage(ChatColor.RED+"Can't give that to you right now, sorry.");
				return false;
			}
 			player.sendMessage("Given " + playerFor.getDisplayName() + " " + count + " " + material.toString());
 			log.info("Giving "+playerName+" "+count+" "+material.toString());
 			if((material.equals(Material.GOLD_PICKAXE)
 					||material.equals(Material.GOLD_AXE)
 					||material.equals(Material.FURNACE)
 					||material.equals(Material.BURNING_FURNACE)
 					)
 					&&(count>10||count<1)){
 				irc.ircAdminMsg("Detecting summon of "+count+" "+material.toString()+" by "+playerName);
 			}
 			return true;
 		}
 
 		if(commandName.equals("time") && (!isPlayer ||hasFlag(player, Flag.ADMIN))){
 			if(args.length!=1){
 				msg(player,"Usage: /time day|night");
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
 				msg(player,"Usage: /time day|night");
 				return true;
 			}
 
 			long curTime=getServer().getWorlds().get(0).getTime();
 			long margin = (desired-curTime) % 24000;
 			if (margin < 0) {
 				margin += 24000;
 			}
 			getServer().getWorlds().get(0).setTime(curTime+margin);
 			msg(player,"Time changed");
 
 			return true;
 		}
 
 		if(isPlayer && (commandName.equals("who") || commandName.equals("playerlist"))){
 			Player[] players=getServer().getOnlinePlayers();
 			String msg="Players ("+players.length+"):";
 			for(Player p: players){
 				msg+=" "+p.getName();
 			}
 			player.sendMessage(msg);
 
 			return true;
 		}
 
 		if(commandName.equals("a") && (!isPlayer ||hasFlag(player, Flag.ADMIN))){
 			if(args.length<1){
 				msg(player,"Usage: /a Message");
 				return true;
 			}
 			String message=combineSplit(0, args, " ");
 			chat.aMsg(playerName,message);
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("report")){
 			if(args.length>0){
 				String theReport=combineSplit(0, args, " ");
 				String message="Report: <§d"+playerName+"§f>"+theReport;
 				String ircmessage="Report from "+playerName+": "+theReport;
 				chat.msgByFlag(Flag.ADMIN, message);
 				irc.ircAdminMsg(ircmessage);
 				log.info(ircmessage);
 				Report report=new Report(0, player.getLocation(), player.getName(), theReport, (new Date().getTime())/1000);
 				reports.addReport(report);
 				player.sendMessage(ChatColor.RED+"Report transmitted. Thanks! :)");
 			}
 			else {
 				player.sendMessage(ChatColor.RED+"To report to the admins, say /report MESSAGE");
 				player.sendMessage(ChatColor.RED+"Where MESSAGE is what you want to tell them");
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("r") && hasFlag(player, Flag.ADMIN)){
 			ArrayList<Report> reps=reports.getReports();
 			int size=reps.size();
 			if(size==0){
 				player.sendMessage(ChatColor.RED+"No reports. Hurray!");
 
 				return true;
 			}
 			player.sendMessage(ChatColor.DARK_PURPLE+"Found "+size+" reports:");
 			for(Report r:reps){
 				player.sendMessage(ChatColor.DARK_PURPLE+"["+r.getID()+"]<"
 						+ChatColor.WHITE+r.getUser()+ChatColor.DARK_PURPLE+"> "+ChatColor.WHITE
 						+r.getMessage());
 			}
 
 			return true;
 		}
 		if(commandName.equals("g") && (!isPlayer ||hasFlag(player, Flag.ADMIN))){
 			if(args.length<1){
 				msg(player,"Usage: /g Message");
 				return true;
 			}
 			String text = "";
 			text+=combineSplit(0, args, " ");
 			chat.gMsg(playerName,text);
 			return true;
 		}
 		if((commandName.equals("ban")||commandName.equals("b")) && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 2){
 				msg(player,"Usage: /ban playername reason");
 				msg(player,"       reason can have spaces in it");
 				return true;
 			}
 			kickbans.callBan(playerName,args,player.getLocation());
 
 			return true;
 		}
 		if((commandName.equals("kick")||commandName.equals("k")) && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 2){
 				msg(player,"Usage: /kick playername reason");
 				return true;
 			}
 			kickbans.callKick(args[0],playerName,combineSplit(1, args, " "));
 			return true;
 		}
 		if(commandName.equals("addban") && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 2){
 				msg(player,"Usage: /addban playername reason");
 				msg(player,"        reason can have spaces in it");
 				return true;
 			}
 			kickbans.callAddBan(playerName,args,player.getLocation());
 
 			return true;
 		}
 
 		if((commandName.equals("unban") || commandName.equals("pardon")) && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length < 1){
 				msg(player,"Usage: /unban playername");
 				return true;
 			}
 			String name=args[0];
 			kickbans.unban(playerName, name);
 			return true;
 		}
 
 
 		if(isPlayer && commandName.equals("trust") && hasFlag(player, Flag.ADMIN)){
 			String action=args[0];
 			if(args.length<2 || !(action.equalsIgnoreCase("add") || action.equalsIgnoreCase("drop"))){
 				player.sendMessage(ChatColor.RED+"Usage: /trust add/drop player");
 				return true;
 			}
 			String name=args[1];
 			if(action.equalsIgnoreCase("add")){
 				users.addFlag(name,Flag.TRUSTED);
 			}
 			else {
 				users.dropFlag(name,Flag.TRUSTED);
 			}
 			String tolog=ChatColor.RED+player.getName()+" changed flags: "+name + " "+ action +" flag "+ Flag.TRUSTED.getDescription();
 			chat.msgByFlag(Flag.ADMIN, tolog);
 			log.info(tolog);
 
 			return true;
 		}
 
 		if(commandName.equals("getgroup") && (!isPlayer || hasFlag(player, Flag.ADMIN))){
 			if(args.length==0){
 				msg(player,"/getgroup playername");
 				return true;
 			}
 			List<Player> match = getServer().matchPlayer(args[0]);
 			if(match.size()!=1 || match.get(0)==null){
 				msg(player,"Player not found");
 
 				return true;
 			}
 			Player who=match.get(0);
 			String message="Player "+match.get(0).getName()+": ";
 			for(Flag f: users.getAllFlags(who)){
 				message+=f.getDescription()+", ";
 			}
 			msg(player,message);
 			log.info(playerName+" looked up "+ who.getName());
 			return true;
 		}
 
 		if (isPlayer && commandName.equals("me") && args.length>0)
 		{
 			String message = "";
 			message+=combineSplit(0, args, " ");
 			chat.addChat(player.getName(), message);
 			irc.ircMsg("* "+ player.getName()+" "+message);
 			//don't cancel this after reading it. 
 			//TODO: /ignore code will also be here
 			chat.msgAll("* "+users.getUser(playerName).getColorName()+" "+message);
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
 			irc.loadIRCAdmins();
 			chat.msgByFlag(Flag.SRSTAFF, ChatColor.RED+"IRC admins reloaded by "+playerName);
 			log.info(playerName+ " reloaded irc admins");
 
 			return true;
 		}
 
 		if(commandName.equals("j2reload") && (!isPlayer ||hasFlag(player, Flag.SRSTAFF))){
 			loadData();
 			chat.msgByFlag(Flag.SRSTAFF, "j2 data reloaded by "+playerName);
 			log.info("j2 data reloaded by "+playerName);
 
 			return true;
 		}
 
 		if(commandName.equals("maintenance") && (!isPlayer ||hasFlag(player, Flag.SRSTAFF))){
 			if(!maintenance){
 				log.info(playerName+" has turned on maintenance mode");
 				maintenance=true;
 				for (Player p : getServer().getOnlinePlayers()) {
 					if (p != null && !hasFlag(player, Flag.ADMIN)) {
 						p.sendMessage("Server entering maintenance mode");
 						p.kickPlayer("Server entering maintenance mode");
 					}
 				}
 				chat.msgByFlag(Flag.ADMIN, "Mainenance mode on, by "+playerName);
 			}
 			else{
 				log.info(playerName+" has turned off maintenance mode");
 				chat.msgByFlag(Flag.ADMIN, "Mainenance mode off, by "+playerName);
 				maintenance=false;
 			}
 
 			return true;
 		}
 
 		if(isPlayer && commandName.equals("1x1") && hasFlag(player, Flag.ADMIN)){
 			player.sendMessage("Next block you break (not by stick), everything above it goes byebye");
 			log.info(player.getName()+" is gonna break a 1x1 tower");
 			OneByOne=player;
 
 			return true;
 		}
 
 		if(commandName.equals("flags") && (!isPlayer ||hasFlag(player, Flag.SRSTAFF))){
 
 			if(args.length<3){
 				msg(player,"Usage: /flags player add/drop flag");
 				return true;
 			}
 			String action=args[1];
 			if(!(action.equalsIgnoreCase("add") || action.equalsIgnoreCase("drop"))){
 				msg(player,"Usage: /flags player add/drop flag");
 				return true;
 			}
 
 			String name=args[0];
 			char flag=args[2].charAt(0);
 			User user=users.getUser(name);
 			if(user==null){
 				user=mysql.getUser(name);
 			}
 			if(action.equalsIgnoreCase("add")){
 				user.addFlag(Flag.byChar(flag));
 			}
 			else {
 				user.dropFlag(Flag.byChar(flag));
 			}
 			String tolog=ChatColor.RED+playerName+" changed flags: "+name + " "+ action +" flag "+ Flag.byChar(flag).getDescription();
 			chat.msgByFlag(Flag.ADMIN, tolog);
 			log.info(tolog);
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("loc")) {
 			Location p_loc = player.getLocation();
 			player.sendMessage("You are located at X:"+p_loc.getBlockX()+" Y:"+p_loc.getBlockY()+" Z:"+p_loc.getBlockZ());
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
 				Warp warp=warps.getPublicWarp(args[0]);
 				if(warp!=null && (hasFlag(player, warp.getFlag())||warp.getFlag().equals(Flag.Z_SPAREWARP_DESIGNATION))){
 					player.sendMessage(ChatColor.RED+"Whoosh!");
 					player.teleportTo(warp.getLocation());
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
 					player.teleportTo(home.getLocation());
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
 					player.teleportTo(warptarget.getLocation());
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
 		if(commandName.equals("clearinventory")){
 			if(isPlayer && args.length==0){
 				player.getInventory().clear();
 				player.sendMessage(ChatColor.RED+"Inventory emptied");
 				log.info(player.getName()+" emptied inventory");
 			}
 			else if(args.length==1 && (!isPlayer||hasFlag(player,Flag.ADMIN))){
 				List<Player> targets=getServer().matchPlayer(args[0]);
 				if(targets.size()==1){
 					Player target=targets.get(0);
 					target.getInventory().clear();
 					target.sendMessage(ChatColor.RED+"Your inventory has been cleared by an admin");
 					log.info(playerName+" emptied inventory of "+target.getName());
 				}
 				else {
 					msg(player,"Found "+targets.size()+" matches. Try again");
 				}
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("mobhere") && hasFlag(player, Flag.SRSTAFF)){
 			if(args.length==0){
 				player.sendMessage(ChatColor.RED+"/mobhere mobname");
 			}
 			else {
 				player.getLocation().getWorld().spawnCreature(player.getLocation(), CreatureType.valueOf(args[0]));
 			}
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("over9000")
 				&&hasFlag(player, Flag.ADMIN)){
 			String name=player.getName();
 			chat.msgAll(ChatColor.RED+"!!! "+ChatColor.DARK_RED+name+" is ON FIRE !!!");
 			chat.msgAll(ChatColor.RED+"    Also, "+name+" is an admin. Pay attention to "+name);
 			users.getUser(name).tempSetColor(ChatColor.DARK_RED);
 			player.getInventory().setHelmet(new ItemStack(51));
 			log.info(name+" set mode to SUPERSAIYAN");
 
 			return true;
 		}
 		if(isPlayer && commandName.equals("under9000")
 				&&hasFlag(player, Flag.ADMIN)){
 			String name=player.getName();
 			player.sendMessage(ChatColor.RED+"You fizzle out");
 			users.getUser(name).restoreColor();
 			player.getInventory().setHelmet(new ItemStack(2));
 			log.info(name+" set mode to NOT-SO-SAIYAN");
 
 			return true;
 		}
 		if(isPlayer && (commandName.equals("coo")||
 				commandName.equals("xyz"))
 				&&hasFlag(player, Flag.ADMIN)){
 			if(args.length<3){
 				player.sendMessage(ChatColor.RED+"You did not specify an X, Y, and Z");
 			}
 			else {
 				player.teleportTo(new Location(player.getWorld(),Double.valueOf(args[0]),Double.valueOf(args[1]),Double.valueOf(args[2]),0,0));
 				player.sendMessage(ChatColor.RED+"WHEEEEE I HOPE THIS ISN'T UNDERGROUND");
 			}
 
 			return true;
 		}
 		if(commandName.equals("whereis") && (!isPlayer ||hasFlag(player,Flag.ADMIN))){
 			if(args.length==0){
 				msg(player,"/whereis player");
 			}
 			else {
 				List<Player> possible=getServer().matchPlayer(args[0]);
 				if(possible.size()==1){
 					Player who=possible.get(0);
 					Location loc=who.getLocation();
 					msg(player,who.getName()+": "+loc.getX()+" "+loc.getY()+" "+loc.getZ());
 				}
 				else {
 					msg(player,args[0]+" does not work. Either 0 or 2+ matches.");
 				}
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean debug;
 	public Logger log;
 
 	public ArrayList<String> protectedUsers;
 	public String[] rules, blacklist, intro, motd, help;
 
 	public String ircName,ircHost,ircChannel,ircOnJoin,gsAuth,gsPass,ircAdminChannel;
 	public ChatColor ircUserColor;
 	public boolean ircMsg,ircEcho,ircDebug;
 	public int ircCharLim,ircPort;
 	public String[] ircSeparator;
 	private String tips_location = "tips.txt";
 	private String  tips_color = ChatColor.AQUA.toString();
 	private String  tips_prefix = "";
 	private boolean tips_stopTimer = false;
 	private int tips_delay = 120;
 	private ArrayList<String> tips;
 	private int currentTip = 0;
 	public String[] ircLevel2;
 	public boolean ircEnable;
 	private ArrayList<Integer> itemblacklist,superblacklist;
 	//private int natureXmin,natureXmax,natureZmin,natureZmax;
 	public boolean maintenance=false;
 	public String maintmessage;
 	public boolean safemode;
 	public PropFile tpProtect=new PropFile("tpProtect.list");
 	public Player OneByOne = null;
 	public boolean fun,randomcolor;
 	Random random = new Random();
 	int playerLimit;
 	ArrayList<String> srstaffList,adminsList,trustedList;
 }
