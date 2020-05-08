 package com.Cayviel.HardCoreWorlds;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 public class BanManager{
 	HardCoreWorlds hcw;
 
 	BanManager(HardCoreWorlds HCW){
 		hcw = HCW;
 	}
 	
 	public static World Ereturnworld;
 	static File BannedListFile;
     static FileConfiguration BannedList = new YamlConfiguration();	
     
 	public static void init() {
 		File BLF = new File(HardCoreWorlds.datafolder, "Ban.yml");
 	    FileSetup.initialize(BLF,BannedList);
 	    BannedListFile = BLF;
 	    checkUnbannable();
 		BannedList.addDefault("Unbannable World", "world");
 		Ereturnworld = HardCoreWorlds.server.getWorld(BannedList.getString("Unbannable World"));
 		//this is null if the "Unbannable World" doesn't exist on the server
 	}
 	
 	private static void setubW(FileConfiguration BannedList, File BLF){
 		BannedList.set("Unbannable World","world");
 		try { BannedList.save(BLF);} catch (IOException e) {e.printStackTrace();}
 	}
 	
 	public static String getubWN(){
 		playerL.safetyWcheck();
 		return Ereturnworld.getName();
 	}
 	
 	public static boolean isBanned(Player player, World world){
 		updateBan(player, world);
 		boolean banned = BannedList.getBoolean("Player."+player.getName().toLowerCase()+".World."+world.getName()+".Banned",false);
 		return banned;
 	}
 	
 	public static boolean isBanned(String playerN, String worldN){
 		playerN=playerN.toLowerCase();
 		updateBan(playerN, worldN);
 		boolean banned = BannedList.getBoolean("Player."+playerN+".World."+worldN+".Banned",false);
 		return banned;
 	}
 
 	public static boolean isServerBanned(String playerN){
 		FileSetup.load(BannedList, BannedListFile);
 		boolean banned = BannedList.getBoolean("Player."+playerN.toLowerCase()+".Server.Ban",false);
 		return banned;
 	}
 	
 	public static boolean isBanPerm(Player player, World world){
 		FileSetup.load(BannedList, BannedListFile);
 		if (getBanBegin(player, world) > getBanEnds(player, world) ) return true;
 		return false;
 	}
 	
 	public static boolean isBanPerm(String player, String world){
 		FileSetup.load(BannedList, BannedListFile);
 		if (getBanBegin(player, world) > getBanEnds(player, world) ) return true;
 		return false;
 	}
 	
 	public static double getTimeLeft(Player player, World world){
 		FileSetup.load(BannedList, BannedListFile);
 		return getBanEnds(player, world)-getHour();
 	}
 	public static void checkUnbannable(){ 
 	    String ubW = BannedList.getString("Unbannable World");
 		if (ubW == null){setubW(BannedList,BannedListFile);}else{ 
 			if (ubW.trim().isEmpty()){
 				setubW(BannedList,BannedListFile);
     		}
 		}
 	}
 	
 	private static double getBanBegin(Player player, World world){
 		Double ii = BannedList.getDouble("Player."+ player.getName().toLowerCase()+".World."+world.getName()+".Ban Began", getHour());
 		double i;
 		if (ii != null){i = ii;}else{i=0;}
 		return i;
 	}
 
 	private static double getBanBegin(String player, String world){
 		Double ii = BannedList.getDouble("Player."+ player+".World."+world+".Ban Began", getHour());
 		double i;
 		if (ii != null){i = ii;}else{i=0;}
 		return i;
 	}
 	
 	private static double getBanEnds(Player player, World world){
 		Double ii = BannedList.getDouble("Player."+ player.getName().toLowerCase()+".World."+world.getName()+".Ban Ends", getHour());
 		double i;
 		if (ii != null){i = ii;}else{i=0;}
 		return i;
 	}
 
 	private static double getBanEnds(String playerN, String worldN){
 		Double ii = BannedList.getDouble("Player."+ playerN+".World."+worldN+".Ban Ends", getHour());
 		double i;
 		if (ii != null){i = ii;}else{i=0;}
 		return i;
 	}
 	
 	public static void ban(OfflinePlayer player, World world, HardCoreWorlds hcw){
 	
 		/*
 		if (!player.isOnline()){
 			if (hasServerBanI(player)){
 				serverBan(player,hcw);
 				return;
 			}
 		}else{
 			Player playeron = Bukkit.getPlayer(player.getName());
 			if (hasServerBanI(playeron)){
 				serverBan(playeron,hcw);
 				return;
 			}
 		}
 		*/
 		
 		playerL.safetyWcheck();
 		if (Ereturnworld == world){return;}
 		BannedList.set("Player."+ player.getName()+".World."+world.getName()+".Banned", true);
 		BannedList.set("Player."+ player.getName()+".World."+world.getName()+".Ban Began", getHour());
 		BannedList.set("Player."+ player.getName()+".World."+world.getName()+".Ban Ends", getHour() + Config.getBanL(world.getName()));
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 	}
 	
 	public static boolean ban(String player, String world){
 		player = player.toLowerCase();
 		playerL.safetyWcheck();
 		if (Ereturnworld.getName() == world){return false;}
 		BannedList.set("Player."+ player+".World."+world+".Banned", true);
 		BannedList.set("Player."+ player+".World."+world+".Ban Began", getHour());
 		BannedList.set("Player."+ player+".World."+world+".Ban Ends", getHour() + Config.getBanL(world));
 		
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 		return true;
 	}
 	
 	public static void serverBan(OfflinePlayer player, HardCoreWorlds hcw){
 		if (player instanceof Player){
 		banKickIn5(((Player)player),"You have been banned from this server!",hcw);
 		((Player)player).sendMessage(ChatColor.RED + "You have been Banned from this server!  You have 5 seconds... Goodbye!");
 		}
 		String playerN = player.getName().toLowerCase(); 
 		BannedList.set("Player."+playerN+".Server.Lives",0);
 		BannedList.set("Player."+playerN+".Server.Ban", true);
 		BannedList.set("Player."+playerN+".Server.Ban Began", getHour());
 		BannedList.set("Player."+playerN+".Server.Ban Ends",  getHour()+Config.getServerBanDuration());
 		
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 	}
 	
 	public static void serverBan(OfflinePlayer player, HardCoreWorlds hcw, double duration){
 		if (player instanceof Player){
 		banKickIn5(((Player)player),"You have been banned from this server!",hcw);
 		((Player)player).sendMessage(ChatColor.RED + "You have been Banned from this server!  You have 5 seconds... Goodbye!");
 		}
 		String playerN = player.getName().toLowerCase(); 
 		BannedList.set("Player."+playerN+".Server.Lives",0);
 		BannedList.set("Player."+playerN+".Server.Ban", true);
 		BannedList.set("Player."+playerN+".Server.Ban Began", getHour());
 		BannedList.set("Player."+playerN+".Server.Ban Ends", getHour() + duration);
 		
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 	}
 
 	public static void unServerBan(String playerN){
 		String nPlayerN = playerN;
 		playerN=playerN.toLowerCase();
 		String useLife=BannedList.getString("Player."+playerN+".Server.Use Lives","defaulted");
 		BannedList.set("Player."+playerN+".Server", null);
 		BannedList.set("Player."+playerN+".Server.Lives",Config.getServerLives());
 		if (useLife.equalsIgnoreCase("true") || useLife.equalsIgnoreCase("false")){ //if it was defined and not defaulted, then redefine to previous value, otherwise leave it gone.
 			BannedList.set("Player."+playerN+".Server.Use Lives",Boolean.parseBoolean(useLife.toLowerCase()));
 		}
 
 		OfflinePlayer player = Bukkit.getOfflinePlayer(nPlayerN);
 		player.setBanned(false);
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 	}
 
 	public static double[] getServerBanTimes(Player player){
 		double[] times = {0,0};
 		times[0]=BannedList.getDouble("Player."+player.getName().toLowerCase()+".Server.Ban Began");
 		times[1]=BannedList.getDouble("Player."+player.getName().toLowerCase()+".Server.Ban Ends");
 		return times;
 	}
 
 	public static double[] getServerBanTimes(String playerN){
 		playerN=playerN.toLowerCase();
 		double[] times = {0,0};
 		times[0]=BannedList.getDouble("Player."+playerN+".Server.Ban Began");
 		times[1]=BannedList.getDouble("Player."+playerN+".Server.Ban Ends");
 		return times;
 	}
 
 	public static boolean EnterWorldRequest(Player playerE, World worldE){
 		if (hasIgnoreBan(playerE,worldE)) return true; // if the player has ignore ban for this specific world, immediately grant access
 		
 		if (! Config.getHc(worldE,playerE)) return true;	//if the world is not hardcore for the player, grant access
 			
 		if (hasIgnoreBan(playerE)&&(! hasWorldBan(playerE, worldE))) return true; //if the player has a general ignore, and is not specifically banned in this world, grant access
 
 		if (!isBanned(playerE, worldE)) return true; //if the player is not banned in the world, grant access
 
 		//The player doesn't ignore bans, is hardcore in this world, and is banned in this world.
 		return false;
 	}
 
 	static double getHour(){
 		Calendar cal = Calendar.getInstance();
 		return (cal.get(Calendar.YEAR)-2012)*8760+(cal.get(Calendar.DAY_OF_YEAR)-1)*24+cal.get(Calendar.HOUR_OF_DAY)+cal.get(Calendar.MINUTE)/60.0+cal.get(Calendar.SECOND)/3600.0;
 	}
 
 	public static void updateBan(Player player, World world){
 		FileSetup.load(BannedList, BannedListFile);
 		if ( getHour() >=  getBanEnds(player, world) && (! isBanPerm(player, world)) ) {
 			unBan(player.getName(),world.getName());
 		}
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 	}
 
 	public static void updateBan(String playerN, String worldN){
 		playerN=playerN.toLowerCase();
 		FileSetup.load(BannedList, BannedListFile);
 		if ( getHour() >=  getBanEnds(playerN, worldN) && (! isBanPerm(playerN, worldN)) ) {
 			unBan(playerN,worldN);
 		}
 	}
 
 	public static void updateServerBan(String playerN){
 		playerN=playerN.toLowerCase();
 		double begins = getServerBanTimes(playerN)[0];
 		double ends = getServerBanTimes(playerN)[1];
 		FileSetup.load(BannedList, BannedListFile);
 		boolean banned = BannedList.getBoolean("Player."+playerN+".Server.Ban",false);
 		if ((getHour() >= ends && begins <= ends)|| !banned) {
 			unServerBan(playerN);
 		}
 	}
 	
 	
 	public static boolean getSHc(OfflinePlayer player){ //get hardcore status of server
 		FileSetup.load(BannedList, BannedListFile);
 		if(hasServerBanI(player)){
 			return BannedList.getBoolean("Player."+player.getName().toLowerCase()+".Server.Use Lives",Config.getUseServerLives());
 		}else{
 			return BannedList.getBoolean("Player."+player.getName().toLowerCase()+".Server.Use Lives", false);
 		}		
 		}
 
 	public static void setSLives(OfflinePlayer player, int lives, HardCoreWorlds hcw){
 		if (lives >0) if (isServerBanned(player.getName().toLowerCase())) unServerBan(player.getName());
 		if (lives <=0) if (!isServerBanned(player.getName().toLowerCase())) serverBan(player, hcw);
 		BannedList.set("Player."+player.getName().toLowerCase()+".Server.Lives",lives);
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 	}
 	
 	public static int getSLives(String playerN){
 		playerN=playerN.toLowerCase();
 		FileSetup.load(BannedList, BannedListFile);
 		return (BannedList.getInt("Player."+playerN+".Server.Lives",Config.getServerLives()));
 	}
 	
 	public static void unBan(String playerN, String worldN){
 		playerN=playerN.toLowerCase();
 		if (playerInList(playerN)){	
 			BannedList.set("Player."+ playerN +".World."+worldN+".Banned",false);
 			BannedList.set("Player."+ playerN +".World."+worldN+".Ban Began",null);
 			BannedList.set("Player."+ playerN +".World."+worldN+".Ban Ends",null);
 			
 			try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 			
 			if (getPlayerLives(playerN, worldN)<=0){
 				setPlayerLives(playerN, worldN, Config.getWorldLives(worldN));
 			}
 
 		}
 	}
 
 	public static boolean hasIgnoreBan(Player player){
 		String subPerm1 = "ban.ignore";
 		String subPerm2 = "ban.rmvforignore";
 		return hasWRperm(player,subPerm1,subPerm2, false);
 	}
 	
 	public static boolean hasIgnoreBan(Player player, World world){
 		String subPerm1 = "ban.ignore."+world.getName();
 		String subPerm2 = "ban.rmvforignore."+world.getName();
 		return hasWRperm(player,subPerm1,subPerm2, false);
 	}
 	
 	public static boolean hasWorldBan(Player player, World world){
 		String subPerm1 = "ban.world."+world.getName();
 		String subPerm2 = "ban.rmvforworld."+world.getName();
 		return hasWRperm(player,subPerm1,subPerm2, false);
 	}
 	
 		
 	public static boolean hasServerBanI(OfflinePlayer player){
 		String subPerm1 = "ban.server";
 		String subPerm2 = "ban.rmvforserver";
 		if (player instanceof Player){
 			return hasWRperm((Player)player,subPerm1,subPerm2, false);
 		}else{
 			return hasWRperm(player,subPerm1,subPerm2, false);
 			}
 	}
 
 	public static boolean hasWRperm(Player player,String subPerm1, String subPerm2, boolean def){
 		if (HardCoreWorlds.getPerm(subPerm1, player, def)) return(!HardCoreWorlds.getPerm(subPerm2, player, def));
 		return def;
 	}
 	
 	public static boolean hasWRperm(OfflinePlayer player,String subPerm1, String subPerm2, boolean def){
 		if (HardCoreWorlds.getPerm(subPerm1, player, def)) return(!HardCoreWorlds.getPerm(subPerm2, player, def));
 		return def;
 	}
 	
 	public static boolean playerInList(String playerN){
 		playerN=playerN.toLowerCase();
 		return BannedList.contains("Player."+playerN);
 	}
 
 	public static void setBanDuration(String playerN, String worldN, double hours){
 		playerN=playerN.toLowerCase();
 		BannedList.set("Player."+playerN+".World."+worldN+".Ban Ends", getHour()+hours);
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 	}
 	public static int getPlayerLives(String playerN, String worldN){
 		playerN=playerN.toLowerCase();
 		FileSetup.load(BannedList,BannedListFile);
 		return BannedList.getInt("Player."+playerN+".World."+worldN+".Lives Remaining",Config.getWorldLives(worldN));
 	}
 
 	public static void setPlayerLives(String playerN, String worldN, int lives){
 		playerN=playerN.toLowerCase();
 		BannedList.set("Player."+playerN+".World."+worldN+".Lives Remaining",lives);
 		try {BannedList.save(BannedListFile);} catch (IOException e) {e.printStackTrace();}
 		if (lives>0){
 			if (isBanned(playerN,worldN)){
 				unBan(playerN, worldN);
 			}
 		}else{
 			if (!isBanned(playerN,worldN)){
 				ban(playerN,worldN);
 			}
 		}
 	}
 	
 	public static void banMessage(Player player, World world){
 		double timeleft = getTimeLeft(player, world);
 		int hours = (int)getTimeLeft(player, world);
 		int minutes = (int)((timeleft-hours)*60);
 		if (isBanPerm(player, world)){
 			player.sendMessage(ChatColor.LIGHT_PURPLE + "You are expelled from world '" + world.getName() + "' forever!");
 		}else{
 			player.sendMessage(ChatColor.LIGHT_PURPLE + "You are expelled from world '" + world.getName() + "' for about " +hours+ " more hours and "+minutes+" minutes.");	
 		}
 	}
 	
 	public static void banKickIn5(final Player player, final String kickmessage, HardCoreWorlds hcw){
 		
 		Runnable runit = new Runnable() {
 		    public void run() {
 		    		player.kickPlayer(kickmessage);
 		    		player.setBanned(true);
 		    	}
 		};
 		player.getServer().getScheduler().scheduleSyncDelayedTask(hcw, runit, 100);
 	}
 }
