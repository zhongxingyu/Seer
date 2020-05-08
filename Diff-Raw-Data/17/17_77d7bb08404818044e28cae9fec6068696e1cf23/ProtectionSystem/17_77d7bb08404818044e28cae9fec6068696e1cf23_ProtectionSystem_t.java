 package com.hsun324.protection;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.hsun324.protection.config.ProtectionConfiguration;
 import com.hsun324.protection.config.ProtectionConfiguration.PunishmentType;
 
 public class ProtectionSystem
 {
 	private static YamlConfiguration config = new YamlConfiguration();
 	private static File configFile = null;
 	public static final MessageDigest digest;
 	static
 	{
 		MessageDigest digestu = null;
 		try
 		{
 			digestu = MessageDigest.getInstance("SHA-512");
 		}
 		catch (NoSuchAlgorithmException e)
 		{
 			try
 			{
 				digestu = MessageDigest.getInstance("SHA-384");
 			}
 			catch (NoSuchAlgorithmException e2)
 			{
 				try
 				{
 					digestu = MessageDigest.getInstance("SHA-256");
 				}
 				catch (NoSuchAlgorithmException e3)
 				{
 					try
 					{
 						digestu = MessageDigest.getInstance("SHA-1");
 					}
 					catch (NoSuchAlgorithmException e4)
 					{
 						try
 						{
 							digestu = MessageDigest.getInstance("MD5");
 						}
 						catch (NoSuchAlgorithmException e5)
 						{
 							Protection.getInstance().getLogger().warning("[LoginProtection] No suitable encryption was found.");
 							Protection.getInstance().getServer().getPluginManager().disablePlugin(Protection.getInstance());
 						}
 					}
 				}
 			}
 		}
 		digest = digestu;
 	}
 	public static class IPBanStatus implements Serializable
 	{
 		private static final long serialVersionUID = 4259480240642526705L;
 		
 		private final String address;
 		private final long startTime;
 		private final long duration;
 
 		private IPBanStatus(InetAddress address)
 		{
 			this(address.getHostAddress(), System.nanoTime(), ProtectionConfiguration.punishmentTime * 1000000000L);
 		}
 		private IPBanStatus(String address)
 		{
 			this(address, System.nanoTime(), ProtectionConfiguration.punishmentTime * 1000000000L);
 		}
 		private IPBanStatus(InetAddress address, long startTime)
 		{
 			this(address.getHostAddress(), startTime, ProtectionConfiguration.punishmentTime * 1000000000L);
 		}
 		private IPBanStatus(String address, long startTime)
 		{
 			this(address, startTime, ProtectionConfiguration.punishmentTime * 1000000000L);
 		}
 		private IPBanStatus(InetAddress address, long startTime, long duration)
 		{
 			this(address.getHostAddress(), startTime, duration);
 		}
 		private IPBanStatus(String address, long startTime, long duration)
 		{
 			this.address = address;
 			this.startTime = startTime;
 			this.duration = duration;
 		}
 		
 		public boolean isExpired()
 		{
 			return System.nanoTime() - this.startTime >= this.duration;
 		}
 		public int secondsLeft()
 		{
 			return (int) Math.floor((this.duration - (System.nanoTime() - this.startTime)) / 1000000000L);
 		}
 	}
 	
 	/**
 	 * Saves the player protection status in an object that can be converted to a storable string.
 	 * @author hsun324
 	 */
 	public static class PlayerProtectionStatus implements Serializable
 	{
 		private static final long serialVersionUID = 4259480240642526705L;
 		
 		private transient boolean loggedIn = false;
 		private String password = "";
 		private transient long lastWarnTime = 0;
 		private transient int attempts = 0;
 		
 		public PlayerProtectionStatus(boolean loggedIn, String password)
 		{
 			this.loggedIn = loggedIn;
 			this.password = password;
 		}
 		
 		public boolean isLoggedIn()
 		{
 			return loggedIn;
 		}
 		public void setLoggedIn(boolean loggedIn)
 		{
 			this.loggedIn = loggedIn;
 		}
 
 		public String getPassword()
 		{
 			return password;
 		}
 		public void setPassword(String password)
 		{
 			this.password = password;
 		}
 		
 		public boolean shouldWarn()
 		{
 			if(!ProtectionConfiguration.warnEnabled)
 				return false;
 			long now = System.nanoTime();
 			if(now - lastWarnTime >= ProtectionConfiguration.warnInterval)
 			{
 				lastWarnTime = now;
 				return true;
 			}
 			return false;
 		}
 		
 		public int getAttempts()
 		{
 			return attempts;
 		}
 		public int incrementAttempts()
 		{
 			return ++attempts;
 		}
 		public void setAttempts(int attempts)
 		{
 			this.attempts = attempts;
 		}
 	}
 
 	private static final HashMap<String, PlayerProtectionStatus> playerProtectionMap = new HashMap<String, PlayerProtectionStatus>();
 	private static final HashMap<String, IPBanStatus> bannedIPs = new HashMap<String, IPBanStatus>();
 	
 	public static boolean isPlayerLoggedIn(String name)
 	{
 		name = name.toLowerCase();
 		if(!Protection.getInstance().isEnabled())
 			return true;
 		if(playerProtectionMap.containsKey(name))
 			return playerProtectionMap.get(name).isLoggedIn();
 		return false;
 	}
 	
 	public static void setPlayerLoggedIn(String playerName, boolean loggedIn)
 	{
 		playerName = playerName.toLowerCase();
 		if(playerProtectionMap.containsKey(playerName))
 			playerProtectionMap.get(playerName).setLoggedIn(loggedIn);
 		else
 			playerProtectionMap.put(playerName, new PlayerProtectionStatus(loggedIn, ""));
 	}
 	
 	public static String getPlayerPasswordHash(String playerName)
 	{
 		playerName = playerName.toLowerCase();
 		if(playerProtectionMap.containsKey(playerName))
 			return playerProtectionMap.get(playerName).getPassword();
 		return "";
 	}
 
 	public static void setPlayerPassword(String playerName, String password)
 	{
 		playerName = playerName.toLowerCase();
 		String securePassword = getHash(password);
 		if(playerProtectionMap.containsKey(playerName))
 			playerProtectionMap.get(playerName).setPassword(securePassword);
 		else
 			playerProtectionMap.put(playerName, new PlayerProtectionStatus(false, securePassword));
 	}
 	
 	public static void setPlayerEntry(String playerName, boolean loggedIn, String password)
 	{
 		playerName = playerName.toLowerCase();
 		if(playerProtectionMap.containsKey(playerName))
 		{
 			playerProtectionMap.get(playerName).setLoggedIn(loggedIn);
 			playerProtectionMap.get(playerName).setPassword(password);
 		}
 		else
 			playerProtectionMap.put(playerName, new PlayerProtectionStatus(loggedIn, password));
 	}
 	
 	public static int getPlayerAttempts(String playerName)
 	{
 		playerName = playerName.toLowerCase();
 		if(playerProtectionMap.containsKey(playerName))
 			return playerProtectionMap.get(playerName).getAttempts();
 		return 0;
 	}
 	public static int incrementPlayerAttempts(Player player)
 	{
 		String playerName = player.getName().toLowerCase();
 		if(playerProtectionMap.containsKey(playerName))
 		{
 			if(playerProtectionMap.get(playerName).incrementAttempts() >= ProtectionConfiguration.maxAttempts)
 			{
 				setPlayerAttempts(player, 0);
 				actUponIllegalAction(player);
 			}
 		}
 		else
 		{
 			PlayerProtectionStatus newStatus = new PlayerProtectionStatus(false, playerName);
 			newStatus.setAttempts(1);
 			playerProtectionMap.put(playerName, newStatus);
 		}
 		return 0;
 	}
 	private static void actUponIllegalAction(Player player)
 	{
 		if(ProtectionConfiguration.punishmentType == PunishmentType.KICK)
		{
 			player.kickPlayer("[LoginProtection] You attempted to login unsuccessfully too many times.");
		}
 		else if(ProtectionConfiguration.punishmentType == PunishmentType.BAN)
 		{
 			String ip = player.getAddress().getAddress().getHostAddress();
 			bannedIPs.put(ip, new IPBanStatus(ip));
 			saveToFile();
 			player.kickPlayer("\u00A7e[LoginProtection]\u00A7f  You attempted to login unsuccessfully too many times. Your IP has been banned for \u00A76" + ProtectionConfiguration.punishmentTime + " seconds\u00A7f.");
 		}
 	}
 
 	public static void setPlayerAttempts(Player player, int attempts)
 	{
 		String playerName = player.getName().toLowerCase();
 		if(playerProtectionMap.containsKey(playerName))
 			playerProtectionMap.get(playerName).setAttempts(attempts);
 		else
 		{
 			PlayerProtectionStatus newStatus = new PlayerProtectionStatus(false, playerName);
 			newStatus.setAttempts(attempts);
 			playerProtectionMap.put(playerName, newStatus);
 		}
 	}
 
 	public static boolean isPlayerBanned(String address)
 	{
 		if(bannedIPs.containsKey(address))
 		{
 			IPBanStatus ban = bannedIPs.get(address);
 			if(ban.isExpired())
 				bannedIPs.remove(address);
 			return !ban.isExpired();
 		}
 		return false;
 	}
 
 	public static int getPlayerBanTimeLeft(String address)
 	{
 		if(bannedIPs.containsKey(address))
 		{
 			IPBanStatus ban = bannedIPs.get(address);
 			if(ban.isExpired())
 			{
 				bannedIPs.remove(address);
 				return 0;
 			}
 			return ban.secondsLeft();
 		}
 		return 0;
 	}
 
 	public static void warn(Player player)
 	{
 		warn(player, true);
 	}
 
 	public static void warn(Player player, boolean check)
 	{
 		if(!check || !playerProtectionMap.containsKey(player.getName()) || playerProtectionMap.get(player.getName()).shouldWarn())
 			player.sendMessage("\u00A7c[LoginProtection] You are not allowed to do anything until you have logged in. \u00A77See /protection for more info.");
 	}
 
 	public static void updatePlayerUsername(Player player)
 	{
 		if(player.getDisplayName().contains("\u00A7c[Unverified]\u00A7f") && isPlayerLoggedIn(player.getName()))
 			player.setDisplayName(player.getDisplayName().replace("\u00A7c[Unverified]\u00A7f ", ""));
 		else if(!player.getDisplayName().contains("\u00A7c[Unverified]\u00A7f") && !isPlayerLoggedIn(player.getName()))
 			player.setDisplayName("\u00A7c[Unverified]\u00A7f " + player.getDisplayName());
 	}
 
 	public static void createStatusIfNone(Player player)
 	{
 		if(!playerProtectionMap.containsKey(player.getName().toLowerCase()))
 			ProtectionSystem.setPlayerEntry(player.getName().toLowerCase(), false, "");
 	}
 	
 	public static void readFromFile()
 	{
 		try
 		{
 			playerProtectionMap.clear();
 			config = new YamlConfiguration();
 			config.load(getConfigFile());
 			
 			playerProtectionMap.clear();
 			Set<String> players = config.getKeys(false);
 			for(String player : players)
 			{
 				String playerPassword = config.getString(player, "");
 				if(!playerPassword.trim().isEmpty())
 					playerProtectionMap.put(player, new PlayerProtectionStatus(false, playerPassword.trim()));
 			}
 			
 			bannedIPs.clear();
 			ConfigurationSection banSection = config.getConfigurationSection("banlist");
 			if(banSection != null)
 			{
 				Set<String> bans = banSection.getKeys(false);
 				for(String ban : bans)
 				{
 					long time = config.getLong("banlist." + ban + ".time", 0);
 					long duration = config.getLong("banlist." + ban + ".duration", 0);
 					String key = ban.replace('\u00A8', '.');
 					IPBanStatus banStatus = new IPBanStatus(key, time, duration);
 					if(!banStatus.isExpired())
 						bannedIPs.put(banStatus.address, banStatus);
 				}
 			}
 			
 			Protection.getInstance().getLogger().info("[LoginProtection] Loaded " + playerProtectionMap.size() + " player entries and " + bannedIPs.size() + " IP bans.");
 		}
 		catch (Exception e)
 		{
 			Protection.getInstance().getLogger().warning("[LoginProtection] Failed loading data.");
 			Protection.getInstance().getLogger().warning(e.getClass().getName());
 			Protection.getInstance().getServer().getPluginManager().disablePlugin(Protection.getInstance());
 		}
 	}
 	
 	public static void saveToFile()
 	{
 		try
 		{
 			config = new YamlConfiguration();
 			config.load(getConfigFile());
 			
 			for(Entry<String, PlayerProtectionStatus> entry : playerProtectionMap.entrySet())
 				config.set(entry.getKey(), entry.getValue().getPassword());
 
 			config.set("banlist", null);
 			for(Entry<String, IPBanStatus> entry : bannedIPs.entrySet())
 			{
 				if(!entry.getValue().isExpired())
 				{
 					String key = entry.getKey().replace('.', '\u00A8');
 					config.set("banlist." + key + ".time", entry.getValue().startTime);
 					config.set("banlist." + key + ".duration", entry.getValue().duration);
 				}
 			}
 			
 			config.save(getConfigFile());
 		}
 		catch (Exception e)
 		{
 			Protection.getInstance().getLogger().warning("[LoginProtection] Failed saving data.");
 			Protection.getInstance().getLogger().warning(e.getClass().getName());
 		}
 	}
 	
 	private static File getConfigFile()
 	{
 		if(configFile == null)
 			configFile = new File(Protection.getInstance().getDataFolder(), "data.yml").getAbsoluteFile();
 		File dataFolder = Protection.getInstance().getDataFolder();
 		if(!dataFolder.exists() || !dataFolder.isDirectory())
 			configFile.getParentFile().mkdirs();
 		if(!configFile.exists() || !configFile.isFile())
 			try { configFile.createNewFile(); } catch (Exception e) { }
 		return configFile;
 	}
 	
 	public static String getHash(String string)
 	{
 		String secure = string;
 		if(digest != null)
 			secure = joinBytes(digest.digest(string.getBytes()));
 		return secure;
 	}
 	
 	private static String joinBytes(byte... bytes)
 	{
 		String res = "";
 		for(byte bite : bytes)
 		{
             String s = Integer.toHexString(new Byte(bite));
             while (s.length() < 2)
                 s = "0" + s;
             s = s.substring(s.length() - 2);
             res += s;
 		}
 		return res;
 	}
 } 
