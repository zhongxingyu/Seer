 package uk.codingbadgers.bFundamentals;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 
 public class bFundamentals extends JavaPlugin {
 	
 	private static bFundamentals m_instance;
 	private static final Logger log = Logger.getLogger("bFundamentals");
 	
 	private ModuleLoader m_moduleLoader = null;
 	
 	@Override
 	public void onEnable() {
 		m_instance = this;
 		
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		// load the modules in
 		m_moduleLoader = new ModuleLoader();
 		m_moduleLoader.load();
 		m_moduleLoader.enable();
 		
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
 		log.log(level, "[" + m_instance.getDescription().getName() +"] " + msg);
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
		String moduleString = "";
 		boolean first = true;
 		
		for (Module module : modules) 
 			moduleString += (first ? "" : ", ") + module.getName();
 		
 		sender.sendMessage(moduleString);
 	}
 
 	private void handleCommand(CommandSender sender, Command cmd, String label,	String[] args) {
		if (args.length > 1) {
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
 
 }
