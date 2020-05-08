 package ru.dark32.chat;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
 import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.dark32.perm.PermissionsHandler;
 
 public class Main extends JavaPlugin {
 
 	public static final Logger			LOG				= Logger.getLogger("Minecraft");
 	public PluginManager				pluginManager;
 	public static boolean				economyHook;
 
 	private static IMute				muteStorage;
 	private static IDeaf				deafStorage;
 	private static IIgnore				ignorStorage;
 	public static final String			VERSION			= "RPchat v 2.0.7w (2u1t)";
 	public static final String			VERSION_NAME	= "Primal steak birch";
 	public static FileConfiguration		config;
 	public static File					storageFile;
 	public static YamlConfiguration		storage;
 	public static File					chatConfigFile;
 	public static YamlConfiguration		chatConfig;
 	public static File					localeConfigFile;
 	public static YamlConfiguration		localeConfig;
 	private static PermissionsHandler	permissionsHandler;
 	// выключить в релизе
 	public static final boolean			DEBUG_MODE		= !true;
 	public static final String			BASE_PERM		= "mcnw";
 
 	// private static SimpleClans core;
 	public static boolean				SCenable		= false;
 	public static Economy				economy			= null;
 
 	@Override
 	public void onEnable() {
 		pluginManager = Bukkit.getPluginManager();
 		config = this.getConfig();
 
 		Main.storageFile = new File(getDataFolder(), "storage.yml");
 		if (Main.storageFile.exists()) {
 			Main.storage = YamlConfiguration.loadConfiguration(storageFile);
 		} else {
 			Main.storage = new YamlConfiguration();
 		}
 
 		String chat = config.getString("chat", "chat");
 		Main.chatConfigFile = new File(getDataFolder(), chat + ".yml");
 		if (Main.chatConfigFile.exists()) {
 			Main.chatConfig = YamlConfiguration.loadConfiguration(chatConfigFile);
 		} else {
 			getLogger().warning("[RPChat] " + chat + ".yml not found");
 		}
 
 		String locale = config.getString("locale", "locale");
 		Main.localeConfigFile = new File(getDataFolder(), locale + ".yml");
 		if (Main.localeConfigFile.exists()) {
 			Main.localeConfig = YamlConfiguration.loadConfiguration(localeConfigFile);
 		} else {
 			getLogger().warning("[RPChat] " + locale + ".yml not found");
 		}
 		if (hookSimpleClans()) {
 			Bukkit.getConsoleSender().sendMessage("[RPChat] Simple clans was hooked");
 			SCenable = true;
 		}
 		if (setupEconomy()) {
 			Bukkit.getConsoleSender().sendMessage("[RPChat] Vault Economy was hooked");
 			economyHook = true;
 		}
 		
 		Util.init(this);
 		ValueStorage.init();
 		ChanelRegister.init();
 		permissionsHandler = new PermissionsHandler(this);
 
 		getServer().getPluginManager().registerEvents(new TabListener(), this);
 		getServer().getPluginManager().registerEvents(new JoinListener(), this);
 		getServer().getPluginManager().registerEvents(new LeftListener(), this);
 		Main.muteStorage = new Mute();
 		Main.deafStorage = new Deaf();
 		Main.ignorStorage = new Ignore();
 
 		final RPChatCommandExecutor executer = new RPChatCommandExecutor();
 		getCommand("rpchat").setExecutor(executer);
 		getCommand("mute").setExecutor(executer);
 		getCommand("unmute").setExecutor(executer);
 		getCommand("deaf").setExecutor(executer);
 		getCommand("undeaf").setExecutor(executer);
 		getCommand("sw").setExecutor(executer);
 		getCommand("ignore").setExecutor(executer);
 		getCommand("unignore").setExecutor(executer);
 		getCommand("chatinfo").setExecutor(executer);
 		getServer().getPluginManager().registerEvents(new ChatListener(), this);
 	}
 
 	@Override
 	public void onDisable() {
 		muteStorage.save();
 	}
 
 	public static IMute getBanStorage() {
 		return muteStorage;
 	}
 
 	public static IDeaf getDeafStorage() {
 		return deafStorage;
 	}
 
 	public static IIgnore getIgnoreStorage() {
 		return ignorStorage;
 	}
 
 	private boolean hookSimpleClans() {
 		try {
 			for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
 				if (plugin instanceof SimpleClans) {
 					// core = (SimpleClans) plugin;
 					return true;
 				}
 			}
 		}
 		catch (NoClassDefFoundError e) {
 			return false;
 		}
 
 		return false;
 	}
 
 	public static ClanManager getClanManager() {
 		return SimpleClans.getInstance().getClanManager();
 	}
 
 	public static PermissionsHandler getPermissionsHandler() {
 		return permissionsHandler;
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
 				net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return (economy != null);
 	}
 }
