 package se.jkrau.bukkit.secretword.database;
 
 import java.io.File;
 import java.io.IOException;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import se.jkrau.bukkit.secretword.SecretWord;
 import se.jkrau.bukkit.secretword.settings.Configuration;
 
 public class SecretPlayer {
 	private SecretWord core = null;
 	private String playername = null;
 	private String ipaddress = null;
 	private Player bukkitplayer = null;
 	private boolean loggedIn = false;
 	private boolean registered = false;
 	private boolean hasCreativeMode = false;
 	private int loginattempts = 0;
 	private long logintime = 0L;
 	private ItemStack[] inventory = null;
 	private String joinmessage = null;
 	private FileConfiguration data = null;
 	private Location initialLocation = null;
 	private boolean freeTeleport = true;
 	
 	public SecretPlayer(SecretWord instance, String mcuser, String ip) {
 		core = instance;
 		playername = mcuser;
 		ipaddress = ip;
 		logintime = System.currentTimeMillis();
 	}
 	
 	public void resetData() {
 		File delete = new File(new File(core.getDataFolder(), "players"), playername);
 		delete.delete();
 		loadData();
 		
 		if (Configuration.notifyPlayerOfReset) bukkitplayer.sendMessage(Configuration.prefix + ChatColor.RED + "Your secret word was reset by an administrator, please relogin to set your new secret word.");
 		
 	}
 	
 	public void loadData() {
 		data = YamlConfiguration.loadConfiguration(new File(new File(core.getDataFolder(), "players"), playername));
 	}
 	
 	public boolean canUseFreeTeleport() {
 		if (freeTeleport) {
 			freeTeleport = false;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void saveData() {
 		try {
 			data.save(new File(new File(core.getDataFolder(), "players"), playername));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void setJoinMessage(String msg) {
 		joinmessage = msg;
 	}
 	
 	public String getJoinMessage() {
 		return joinmessage;
 	}
 	
 	public FileConfiguration getData() {
 		return data;
 	}
 	
 	public void setInventory(ItemStack[] e) {
 		inventory = e;
 	}
 	
 	public ItemStack[] getInventory() {
 		return inventory;
 	}
 	
 	public void setLoggedIn(boolean e) {
 		loggedIn = e;
 		if (loggedIn) {
			bukkitplayer.sendMessage(Configuration.prefix + ChatColor.GREEN + "You've successfully logged in! Enjoy!");
 			if (data != null) {
 				data.set("Lastlogin", System.currentTimeMillis());
 				saveData();
 			}
 			
 			try {
 				// bukkitplayer.getInventory().addItem(inventory);
 			} catch (Exception bukkitderpsalot) {}
 			// notifications if necessary
 			if (joinmessage != null) {
 				Bukkit.broadcastMessage(joinmessage);
 			}
 			
 		}
 		
 		if (hasCreativeMode) {
 			bukkitplayer.setGameMode(GameMode.CREATIVE);
 		}
 	}
 	
 	public void setHasCreative(boolean e) {
 		hasCreativeMode = e;
 		if (hasCreativeMode) {
 			bukkitplayer.setGameMode(GameMode.SURVIVAL);
 		}
 	}
 	
 	public void setRegistered(boolean e) {
 		registered = e;
 		if (registered) {
 			loadData();
 		}
 	}
 	
 	public boolean triggerAttempt() {
 		loginattempts++;
 		
 		if (loginattempts > Configuration.maxloginattempts) {
 			bukkitplayer.kickPlayer(Configuration.logPrefix + "Too many login attempts!");
 			return true;
 		}
 		return false;
 	}
 	
 	public void setBukkitPlayer(Player e) {
 		if (bukkitplayer == null) {
 			bukkitplayer = e;
 		}
 	}
 	
 	public void setInitialLocation(Location loc) {
 		initialLocation = loc;
 	}
 	
 	public void teleportBack() {
 		bukkitplayer.teleport(initialLocation);
 	}
 	
 	public Location getInitialLocation() {
 		return initialLocation;
 	}
 	
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 	
 	public boolean isRegistered() {
 		return registered;
 	}
 	
 	public String getName() {
 		return playername;
 	}
 	
 	public String getIP() {
 		return ipaddress;
 	}
 	
 	public boolean threeSecondRule() {
 		return (System.currentTimeMillis() - logintime) < 3000;
 	}
 	
 	public boolean thirtySeconds() {
 		return (System.currentTimeMillis() - logintime) > 32000;
 	}
 	
 	public Player getPlayer() {
 		return bukkitplayer;
 	}
 	
 	public Location getLocation() {
 		return initialLocation;
 	}
 	
 }
