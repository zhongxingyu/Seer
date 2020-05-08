 package uk.codingbadgers.bFundamentals;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import n3wton.me.BukkitDatabaseManager.BukkitDatabaseManager;
 import n3wton.me.BukkitDatabaseManager.BukkitDatabaseManager.DatabaseType;
 import n3wton.me.BukkitDatabaseManager.Database.BukkitDatabase;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import com.nodinchan.ncbukkit.NCBL;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 
 public class bFundamentals extends JavaPlugin {
 	
 	private static bFundamentals m_instance = null;
 	private static final Logger m_log = Logger.getLogger("bFundamentals");
 	private static BukkitDatabase m_database = null;	
 	private static Permission m_permissions = null;
 	public ModuleLoader m_moduleLoader = null;
 	private ConfigurationManager m_configuration = null;
 	private final CommandListener m_commandListener = new CommandListener();
 	
 	@Override
 	public void onLoad() {
 		m_instance = this;
 		
 		log(Level.INFO, "bFundamentals Loading");
 		// update NC-loader
 		updateLib();
 	}
 	
 	@Override
 	public void onEnable() {
 		
 		// load the configuration into the configuration manager
 		m_configuration = new ConfigurationManager();
 		m_configuration.loadConfiguration(m_instance);
 		
 		// load the modules in
 		m_moduleLoader = new ModuleLoader();
 		m_moduleLoader.load();
 		m_moduleLoader.enable();
 		
 		this.getServer().getPluginManager().registerEvents(m_commandListener, this);
 		
 		bFundamentals.log(Level.INFO, "bFundamentals Loaded.");
 	}
 
 	@Override
 	public void onDisable() {
 		bFundamentals.log(Level.INFO, "bFundamentals Disabled.");
 		m_moduleLoader.disable();
 	}
 	
 	public static bFundamentals getInstance() {
 		return m_instance;
 	}
 	
 	public static void log(Level level, String msg) {
 		m_log.log(level, "[" + m_instance.getDescription().getName() +"] " + msg);
 	}
 	
 	public ConfigurationManager getConfigurationManager() {
 		return m_configuration;
 	}
 	
 	public static BukkitDatabase getBukkitDatabase() {
 		if (m_database == null) {
 			m_database = BukkitDatabaseManager.CreateDatabase("bFundamentals", m_instance, DatabaseType.SQLite);
 		}
 		return m_database;
 	}
 	
 	public static Permission getPermissions() {
 		if (m_permissions == null) {
 			RegisteredServiceProvider<Permission> permissionProvider = m_instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		    if (permissionProvider != null) {
 		    	m_permissions = permissionProvider.getProvider();
 		    }
 		}
 	    return m_permissions;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("bfundamentals")) {
 			handleCommand(sender, cmd, label, args);
 			return true;
 		}
 		
 		if (label.equalsIgnoreCase("modules")) {
 			handleModulesCommand(sender);
 			return true;
 		}
 		
 		return m_moduleLoader.onCommand(sender, cmd, label, args);
 	}
 
 	private void handleModulesCommand(CommandSender sender) {
 		List<Module> modules = m_moduleLoader.getModules();
 		String moduleString = ChatColor.GREEN + "Modules: ";
 		boolean first = true;
 		
 		for (Module module : modules) {
 			moduleString += (first ? "" : ", ") + module.getName();
 			first = false;
 		}
 		
 		sender.sendMessage(moduleString);
 	}
 
 	private void handleCommand(CommandSender sender, Command cmd, String label,	String[] args) {
 		if (args.length < 1) {
 			sender.sendMessage(ChatColor.DARK_AQUA + "[bFundamentals] " + ChatColor.WHITE + "/bFundamentals");			
 			return;
 		}
 		
 		if (args[0].equalsIgnoreCase("reload")) {
 			this.getPluginLoader().disablePlugin(this);
 			this.getPluginLoader().enablePlugin(this);
 			sender.sendMessage(ChatColor.DARK_AQUA + "[bFundamentals] " + ChatColor.WHITE + "Reloading plugin");
 			return;
 		}
 		
 		if (args[0].equalsIgnoreCase("module")) {
 			if (args.length < 2) {
 				sender.sendMessage(ChatColor.DARK_AQUA + "[bFundamentals] " + ChatColor.WHITE + "use /bFundamentals <reload/load>");
 				return;
 			}
 			
 			if (args[1].equalsIgnoreCase("unload")) {
 				m_moduleLoader.unload();
 				sender.sendMessage(ChatColor.DARK_AQUA + "[bFundamentals] " + ChatColor.WHITE + "UnLoaded all modules");			
 				return;
 			}
 			
 			if (args[1].equalsIgnoreCase("load")) {
 				m_moduleLoader.load();
 				m_moduleLoader.enable();
 				sender.sendMessage(ChatColor.DARK_AQUA + "[bFundamentals] " + ChatColor.WHITE + "Loaded all modules");			
 				return;
 			}
 			
 			if (args[1].equalsIgnoreCase("reload")) {
 				m_moduleLoader.unload();
 				m_moduleLoader.load();
 				sender.sendMessage(ChatColor.DARK_AQUA + "[bFundamentals] " + ChatColor.WHITE + "Reloaded all modules");			
 				return;
 			}
 			
 			sender.sendMessage(ChatColor.DARK_AQUA + "[bFundamentals] " + ChatColor.WHITE + "use /bFundamentals <reload/load>");
 			return;
 		}
 	}
 	
 	public void disable(Module module) {
 		m_moduleLoader.unload(module);
 	}
 	
 	private void updateLib() {
 		PluginManager pm = getServer().getPluginManager();
 		
 		NCBL libPlugin = (NCBL) pm.getPlugin("NC-BukkitLib");
 		
 		File destination = new File(getDataFolder().getParentFile().getParentFile(), "lib");
 		destination.mkdirs();
 		
 		File lib = new File(destination, "NC-BukkitLib.jar");
 		File pluginLib = new File(getDataFolder().getParentFile(), "NC-BukkitLib.jar");
 		
 		boolean inPlugins = false;
 		boolean download = false;
 		
 		try {
 			URL url = new URL("http://bukget.org/api/plugin/nc-bukkitlib");
 			
 			JSONObject jsonPlugin = (JSONObject) new JSONParser().parse(new InputStreamReader(url.openStream()));
 			JSONArray versions = (JSONArray) jsonPlugin.get("versions");
 			
 			if (libPlugin == null) {
 				log(Level.WARNING, "Missing NC-Bukkit lib");
 				inPlugins = true;
 				download = true;
 				
 			} else {
 				double currentVer = libPlugin.getVersion();
 				double newVer = currentVer;
 				
 				for (int ver = 0; ver < versions.size(); ver++) {
 					JSONObject version = (JSONObject) versions.get(ver);
 				
 					if (version.get("type").equals("Release")) {
 						newVer = Double.parseDouble(((String) version.get("name")).split(" ")[1].trim().substring(1));
 						break;
 					}
 				}
 			
 				if (newVer > currentVer) {
 					log(Level.WARNING, "NC-Bukkit lib outdated");
 					download = true;
 				}
 			}
 			
 			if (download) {
 				log(Level.INFO, "Downloading NC-Bukkit lib");
 				
 				String dl_link = "";
 				
 				for (int ver = 0; ver < versions.size(); ver++) {
 					JSONObject version = (JSONObject) versions.get(ver);
 					
 					if (version.get("type").equals("Release")) {
 						dl_link = (String) version.get("dl_link");
 						break;
 					}
 				}
 				
 				if (dl_link == null)
 					throw new Exception();
 				
 				URL link = new URL(dl_link);
 				ReadableByteChannel rbc = Channels.newChannel(link.openStream());
 				
 				if (inPlugins) {
 					FileOutputStream output = new FileOutputStream(pluginLib);
 					output.getChannel().transferFrom(rbc, 0, 1 << 24);
 					libPlugin = (NCBL) pm.loadPlugin(pluginLib);
					output.close();
 				} else {
 					FileOutputStream output = new FileOutputStream(lib);
 					output.getChannel().transferFrom(rbc, 0, 1 << 24);
					output.close();
 				}
 				
 				log(Level.INFO, "Downloaded NC-Bukkit lib");
 			}
 			
 			libPlugin.hook(this);
 			
 		} catch (Exception e) { log(Level.WARNING, "Failed to check for library update"); }
 		}
 
 }
