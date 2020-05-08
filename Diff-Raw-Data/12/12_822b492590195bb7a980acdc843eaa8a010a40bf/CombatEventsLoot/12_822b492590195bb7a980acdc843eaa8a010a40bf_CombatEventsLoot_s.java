 package net.milkbowl.combatevents.loot;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import net.milkbowl.combatevents.CombatEventsCore;
 import net.milkbowl.combatevents.CombatEventsListener;
 
 import net.milkbowl.vault.Vault;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 public class CombatEventsLoot extends JavaPlugin {
 
 	public static String plugName;
 	public static Logger log = Logger.getLogger("Minecraft");
 
 	//Dependencies
 	private CombatEventsCore ceCore = null;
 	public static Permission perms = null;
 	public static Economy econ = null;
 
 	//Config stuffs
 	public static Map<String, LootWorldConfig> worldConfig = new HashMap<String, LootWorldConfig>(2);
 	public static Configuration wConfig;
 
 	private CombatEventsListener combatListener;
 
 	@Override
 	public void onDisable() {
 		log.info(plugName + " - " +"disabled!");
 
 	}
 
 	@Override
 	public void onEnable() {
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		plugName = "["+pdfFile.getName()+"]";
 
		//If we can't load 
		if (!setupDependencies())
			this.getServer().getPluginManager().disablePlugin(this);

 		File worldsYml = new File(getDataFolder()+"/worlds.yml");
 		setupFile(worldsYml);
 
 		wConfig = new Configuration(worldsYml);
 		wConfig.load();
 
 		createConfig(this);
 
 
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.CUSTOM_EVENT, combatListener, Priority.Normal, this);
 
 		log.info(plugName + " - " + "v" + pdfFile.getVersion() + " by Sleaker is enabled!");
 
 	}
 
 	private void setupFile(File file) {
 		if (!file.exists()) {
 			new File(getDataFolder().toString()).mkdir();
 			try {
 				file.createNewFile();
 			}
 			catch (IOException ex) {
 				log.info(plugName + " - Cannot create configuration file. And none to load check your folder permission!");
 			}
 		}   
 	}
 
 	/**
 	 * Writes the default values for our server
 	 * 
 	 * @param plugin
 	 */
 	private static void createConfig(CombatEventsLoot plugin) {
 		List<World> worlds = plugin.getServer().getWorlds();
 		for (World world : worlds) {
 			LootWorldConfig lootTable = new LootWorldConfig();
 			if (wConfig.getNode(world.getName()) == null) {
 				for (CreatureType creature : CreatureType.values()) {
 					if (creature == CreatureType.MONSTER)
 						continue;
 					ArrayList<CreatureDrop> drops = new ArrayList<CreatureDrop>();
 					lootTable.setCreature(creature, drops);
 					wConfig.setProperty(world.getName() + "." + creature.getName(), drops);
 				}
 			} else {
 				for (CreatureType creature : CreatureType.values()) {
 					if (creature == CreatureType.MONSTER)
 						continue;
 					ArrayList<String> dropData = (ArrayList<String>) wConfig.getStringList(world.getName() + "." + creature.getName(), new ArrayList<String>());
 					ArrayList<CreatureDrop> drops = new ArrayList<CreatureDrop>(16);
 					for (String s : dropData) {
 						drops.add(new CreatureDrop(s));
 					}
 					lootTable.setCreature(creature, drops);
 				}
 			}
 			worldConfig.put(world.getName(), lootTable);
 		}
 		wConfig.save();
 	}
 
 	private boolean setupDependencies() {
 		if (ceCore == null) {
 			Plugin ceCore = this.getServer().getPluginManager().getPlugin("CombatEventsCore");
 			if (ceCore != null) {
 				this.ceCore = ((CombatEventsCore) ceCore);
 				combatListener = new CombatEventsListener();
 				log.info(plugName + " - Successfully hooked " + ceCore.getDescription().getName() + "v" + ceCore.getDescription().getVersion());
 			}
 		} 
 		if (CombatEventsLoot.econ == null || CombatEventsLoot.perms == null) {
 			Plugin VAULT = this.getServer().getPluginManager().getPlugin("Vault");
 			if (VAULT != null) {
 				CombatEventsLoot.econ = ((Vault) VAULT).getEconomy();
 				CombatEventsLoot.perms = ((Vault) VAULT).getPermission();
 			}
 		}
 		if (CombatEventsLoot.perms == null || CombatEventsLoot.econ == null || ceCore == null)
 			return false;
 		else
 			return true;
 	}
 }
