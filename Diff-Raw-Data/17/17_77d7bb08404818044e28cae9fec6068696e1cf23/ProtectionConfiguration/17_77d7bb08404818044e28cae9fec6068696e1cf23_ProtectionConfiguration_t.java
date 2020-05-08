 package com.hsun324.protection.config;
 
 import com.hsun324.protection.Protection;
 import com.hsun324.protection.ProtectionSystem;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 public class ProtectionConfiguration
 {
 	private static YamlConfiguration config = new YamlConfiguration();
 	private static File configFile = null;
 
 	public static final List<String> listedPlayers = new ArrayList<String>();
 	public static ListMode listMode = ListMode.DISABLED;
 	public static boolean playerControllable = true;
 
 	public static int maxAttempts = 5;
 	public static boolean warnEnabled = true;
 	public static long warnInterval = 10000000000L;
 
 	public static PunishmentType punishmentType = PunishmentType.KICK;
 	public static int punishmentTime = 360;
 
 	public static void readFromFile()
 	{
 		try
 		{
 			config = new YamlConfiguration();
 			config.load(getConfigFile());
 
 			listMode = (ListMode)match(config.getString("list.mode", "DISABLED"), ListMode.DISABLED);
 			playerControllable = config.getBoolean("list.player-controllable", true);
 
 			listedPlayers.clear();
 			for (String player : config.getStringList("list.players"))
 				listedPlayers.add(player.toLowerCase());
 
 			maxAttempts = config.getInt("ban.max-attempts", 5);
 			punishmentType = (PunishmentType)match(config.getString("ban.punishment.type", "KICK"), PunishmentType.KICK);
 			punishmentTime = config.getInt("ban.punishment.time", 360);
 
 			warnEnabled = config.getBoolean("warn.enabled", true);
 			warnInterval = config.getLong("warn.interval", 10L) * 1000000000L;
 		}
 		catch (Exception e)
 		{
 			Protection.getInstance().getLogger().warning("[LoginProtection] Failed loading config entries.");
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
 
 			config.set("list.mode", listMode.name());
 			config.set("list.players", listedPlayers);
 			config.set("list.player-controllable", playerControllable);
 
 			config.set("ban.max-attempts", Integer.valueOf(maxAttempts));
 			config.set("ban.punishment.type", punishmentType.name());
 			config.set("ban.punishment.time", Integer.valueOf(punishmentTime));
 
 			config.set("warn.enabled", Boolean.valueOf(warnEnabled));
 			config.set("warn.interval", Long.valueOf(warnInterval / 1000000000L));
 
 			config.save(getConfigFile());
 		}
 		catch (Exception e)
 		{
 			Protection.getInstance().getLogger().warning("[LoginProtection] Failed saving config entries.");
 			Protection.getInstance().getLogger().warning(e.getClass().getName());
 		}
 	}
 
 	private static File getConfigFile()
 	{
 		if (configFile == null)
 			configFile = new File(Protection.getInstance().getDataFolder(), "config.yml").getAbsoluteFile();
 		File dataFolder = Protection.getInstance().getDataFolder();
 		if ((!dataFolder.exists()) || (!dataFolder.isDirectory()))
 			configFile.getParentFile().mkdirs();
 		if ((!configFile.exists()) || (!configFile.isFile()))
 		{
 			try
 			{
 				configFile.createNewFile();
 				initialSave();
 			} catch (Exception e) { }
 		}
 		return configFile;
 	}
 
 	private static void initialSave()
 	{
 		try
 		{
 			config.load(getConfigFile());
 
 			config.set("list.mode", "DISABLED");
 			config.set("list.player-controllable", true);
 			config.set("list.players", new ArrayList<String>());
 
 			config.set("ban.max-attempts", Integer.valueOf(5));
 			config.set("ban.punishment.type", "KICK");
 			config.set("ban.punishment.time", Integer.valueOf(360));
 
 			config.set("warn.enabled", Boolean.valueOf(true));
 			config.set("warn.interval", Long.valueOf(10L));
 
 			config.save(getConfigFile());
 		}
 		catch (Exception e)
 		{
 			Protection.getInstance().getLogger().warning("[LoginProtection] Failed creating config entries.");
 			Protection.getInstance().getLogger().warning(e.getClass().getName());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private static <T extends Enum<?>> T match(String name, T def)
 	{
 		try
 		{
 			return (T) Enum.valueOf(def.getClass(), name);
 		}
 		catch (IllegalArgumentException e)
 		{
 			return def;
 		}
 	}
 
 	public static boolean getDefaultLoginStatus(Player player)
 	{
 		if (ProtectionSystem.getPlayerPasswordHash(player.getName()).isEmpty())
 			return false;
 		if (listMode == ListMode.BLACKLIST)
 			return !listedPlayers.contains(player.getName().toLowerCase());
 		if (listMode == ListMode.WHITELIST)
 			return listedPlayers.contains(player.getName().toLowerCase());
 		return false;
 	}
 
 	public static enum ListMode
 	{
 		WHITELIST, 
 		BLACKLIST, 
 		DISABLED;
 	}
 
 	public static enum PunishmentType
 	{
 		KICK, 
 		BAN, 
 		NONE;
 	}
 }
