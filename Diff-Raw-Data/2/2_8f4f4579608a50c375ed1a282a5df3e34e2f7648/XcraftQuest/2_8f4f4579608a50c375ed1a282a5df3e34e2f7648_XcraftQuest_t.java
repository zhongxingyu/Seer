 package de.xcraft.engelier.quest;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.register.payment.Method;
 
 import de.xcraft.engelier.utils.Configuration;
 
 public class XcraftQuest extends JavaPlugin {	
 
 	private PluginManager pm = null;
 	private XcraftQuestPluginListener pluginListener = new XcraftQuestPluginListener(this);
 	private XcraftQuestCommandHandler commandHandler = new XcraftQuestCommandHandler(this);
 	private XcraftQuestBlockListener blockListener = new XcraftQuestBlockListener(this);
 	private XcraftQuestEntityListener entityListener = new XcraftQuestEntityListener(this);
 	private XcraftQuestInventoryListener invListener = new XcraftQuestInventoryListener(this);
 	
 	public XcraftQuestQuests quests = new XcraftQuestQuests(this);
 	public XcraftQuestQuester quester = new XcraftQuestQuester(this);
 	
 	public Logger log = Logger.getLogger("Minecraft");
 	public Configuration config = null;
 	public Configuration lang = null;
 	
 	public PermissionHandler permissions = null;
 	public Method ecoMethod = null;
 	
 	@Override
 	public void onEnable() {
 		pm = getServer().getPluginManager();
 		
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, invListener, Event.Priority.Normal, this);
 		
 		pluginListener.checkForPermissions();
 		
 		loadConfig();
 		loadLang(config.getString("global/lang", "en"));
 		quests.load();
 		quester.load();
 				
 		log.info(getNameBrackets() + "Loaded.");
 		
 		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				quester.save();
 				log.info(getNameBrackets() + "saving user data.");
 			}			
 		}, 18000L, 18000L);
 	}
 
 	@Override
 	public void onDisable() {
 		this.getServer().getScheduler().cancelAllTasks();
 		quester.save();
 		lang.save();
 		log.info(getNameBrackets() + "Disabled.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("quest")) {
 			String error = commandHandler.parse((Player)sender, args);
 			if (error != null)
 				sender.sendMessage(ChatColor.RED + "Error: " + error);
 
 			return true;
 		}
 		return false;
 	}
 	
 	public void loadConfig() {
 		config = new Configuration();
 		config.load(getDataFolder().toString(), "config.yml");
 	}
 
 	public void loadLang(String lang) {
 		File langFile = new File(getDataFolder(), "lang." + lang + ".yml");
 		
 		if (!langFile.exists()) {
 			log.severe(getNameBrackets() + "language file for '" + lang + "' not found");;
 		}
 
 		this.lang = new Configuration();
 		this.lang.load(getDataFolder().toString(), "lang." + lang + ".yml");
 	}
 	
 	public String getNameBrackets() {
 		return "[" + this.getDescription().getFullName() + "] ";
 	}
 
 }
