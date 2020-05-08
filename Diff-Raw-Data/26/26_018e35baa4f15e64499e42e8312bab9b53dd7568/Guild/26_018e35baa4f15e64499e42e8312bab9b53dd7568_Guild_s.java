 package me.bluejelly.main.getters;
 
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import me.bluejelly.main.GuildZ;
 import me.bluejelly.main.configs.*;
 
 public class Guild {
 
 	static GuildZ main;
 	
 	public Guild(GuildZ instance)
 	{
 		main = instance;
 	}
 
 	private static FileConfiguration gConfig = GuildConfig.config;
 	
 	public static boolean exists(String guildName) {
 		if(!gConfig.contains(guildName)) {return false;}
 		return true;
 	}
 	
 	public static String getOwner(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getString(guildName+".owner");
 	}
 	
 	public static String getDescription(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getString(guildName+".description");
 	}
 	
 	public static int getLevel(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getInt(guildName+".level");
 	}
 	
 	public static boolean getOpen(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getBoolean(guildName+".open");
 	}
 	
 	public static boolean getPeaceful(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getBoolean(guildName+".peaceful");
 	}
 	
 	public static double getMoney(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getDouble(guildName+".money");
 	}
 	
 	public static List<?> getInvites(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getList(guildName+".invites");
 	}
 	
 	public static List<?> getRelation(String guildName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		return gConfig.getList(guildName+".relations");
 	}
 	
 	public static void setOwner(String guildName, String playerName) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		if(GuildPlayer.isInGuild(playerName)) {
 			GuildPlayer.setRole(playerName, "OWNER");
 			GuildPlayer.setGuild(playerName, guildName);
 		}
 		gConfig.set(guildName+".owner", playerName);
 	}
 	
 	public static void setDescription(String guildName, String description) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		gConfig.set(guildName+".description", description);
 	}
 	
 	public static void setLevel(String guildName, int lvl) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		gConfig.set(guildName+".level", lvl);
 	}
 	
 	public static void setOpen(String guildName, boolean isOpen) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		gConfig.set(guildName+".open", isOpen);
 	}
 	
 	public static void setPeaceful(String guildName, boolean isPeaceful) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		gConfig.set(guildName+".peaceful", isPeaceful);
 	}
 	
 	public static void setMoney(String guildName, double money) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		gConfig.set(guildName+".money", money);
 	}
 	
 	public static void addMoney(String guildName, double money) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		double cMoney = gConfig.getDouble(guildName+".money");
 		double nMoney = money+cMoney;
 		gConfig.set(guildName+".money", nMoney);
 	}
 	
 	public static void removeMoney(String guildName, double money) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		double cMoney = gConfig.getDouble(guildName+".money");
 		double nMoney = money-cMoney;
 		gConfig.set(guildName+".money", nMoney);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void invitePlayer(String guildName, String player) {
 		if(!gConfig.contains(guildName)) {throw new NullPointerException("isInGuild for " + guildName + " returned null!");}
 		List<String> list = (List<String>) gConfig.getList(guildName+".invites");
 		list.add(player);
 		gConfig.set(guildName+".invites", list);
 	}
 	
 }
