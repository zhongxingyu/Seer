 package me.bluejelly.main.getters;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import me.bluejelly.main.GuildZ;
 import me.bluejelly.main.configs.PlayerConfig;
 
 public class GuildPlayer {
 
 	static GuildZ main;
 	
 	public GuildPlayer(GuildZ instance)
 	{
 		main = instance;
 	}
 	
 	private static FileConfiguration pConfig = PlayerConfig.config;
 	
 	public static boolean exists(String playerName) {
 		if(!pConfig.contains(playerName)) {return false;}
 		return true;
 	}
 	
 	public static boolean isInGuild(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("isInGuild for " + playerName + " returned null!");}
 		if(PlayerConfig.config.getBoolean(playerName+".isInGuild")) return true;
 		return false;
 	}
 	
 	public String guildName(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("guildName for " + playerName + " returned null!");}
 		return pConfig.getString(playerName+".guildName");
 	}
 
 	public String getRole(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("getRole for " + playerName + " returned null!");}
 		return pConfig.getString(playerName+".role");
 	}
 
 	public String getTitle(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("getTitle for " + playerName + " returned null!");}
 		return pConfig.getString(playerName+".title");
 	}
 
 	public String getChatMode(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("getChatMode for " + playerName + " returned null!");}
 		return pConfig.getString(playerName+".chatmode");
 	}
 
 	public double getHonor(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("getReputation for " + playerName + " returned null!");}
 		return pConfig.getDouble(playerName+".honor");
 	}
 
 	public double getReputation(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("getReputation for " + playerName + " returned null!");}
 		return pConfig.getDouble(playerName+".reputation");
 	}
 
 	public void setGuild(String playerName, String guild) {
 		pConfig.set(playerName+".isInGuild", true);
 		pConfig.set(playerName+".guildName", guild);
		PlayerConfig.saveConfig();
 	}
 	
 	public void setRole(String playerName, String role) {
 		pConfig.set(playerName+".role", role);
		PlayerConfig.saveConfig();
 	}
 	
 	public void setChatMode(String playerName, String chatMode) {
 		pConfig.set(playerName+".chatmode", chatMode);
		PlayerConfig.saveConfig();
 	}
 	
 	public void setHonor(String playerName, double honor) {
 		pConfig.set(playerName+".honor", honor);
		PlayerConfig.saveConfig();
 	}
 	
 	public void setReputation(String playerName, double reputation) {
 		pConfig.set(playerName+".reputation", reputation);
		PlayerConfig.saveConfig();
 	}
 	
 	public boolean removeFromGuild(String playerName) {
 		if(!pConfig.contains(playerName)) {throw new NullPointerException("getReputation for " + playerName + " returned null!");}
 		if(GuildPlayer.isInGuild(playerName)) {
 			pConfig.set(playerName+".isInGuild", false);
 			pConfig.set(playerName+".guildName", null);
			PlayerConfig.saveConfig();
 		}
 		return false;
 	}
 	
 }
