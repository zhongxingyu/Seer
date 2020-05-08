 package fr.frozentux.craftguard2;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
import java.util.List;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 
 import fr.frozentux.craftguard2.commands.*;
 import fr.frozentux.craftguard2.config.*;
 import fr.frozentux.craftguard2.list.*;
 import fr.frozentux.craftguard2.logger.*;
 import fr.frozentux.craftguard2.module.CraftGuardModule;
 import fr.frozentux.craftguard2.module.craft.CraftListener;
 import fr.frozentux.craftguard2.module.smelt.SmeltListener;
 import fr.frozentux.craftguard2.smeltingmanager.*;
 
 /**
  * CraftGuard 2 Plugin
  * @author FrozenTux
  *
  */
 public class CraftGuardPlugin extends JavaPlugin {
 	
 	private Metrics metrics;
 	
 	private CraftGuardLogger craftGuardLogger;
 	
 	private ModuleRegistry registry;
 	
 	private CraftGuardConfig config;
 	
 	private ListLoader listLoader;
 	private File listFile;
 	private ListManager listManager;
 	
 	private SmeltFile smeltFile;
 	
 	public void onLoad(){
 		registry = new ModuleRegistry(this);
 	}
 	
 	public void onEnable(){
 		
 		//Logger init
 		craftGuardLogger = new CraftGuardLogger(this);
 		
 		//Metrics init
 		try {
 			metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		initConfig();
 		initModules();
 		initLists();
 		
 		//Smelting init
 		smeltFile = new SmeltFile(new YamlConfiguration(), new File(this.getDataFolder().getAbsolutePath() + File.separator + "smelting.yml"), this);
 		smeltFile.load();
 		
 		//Commands init
 		this.getCommand("cg").setExecutor(new CgCommandExecutor(this));
 		
 		craftGuardLogger.info("CraftGuard version " + this.getDescription().getVersion() + " has been enabled");
 	}
 	
 	public void onDisable(){
 		config.write();
 		craftGuardLogger.info("CraftGuard version " + this.getDescription().getVersion() + " has been disabled");
 	}
 	
 	/*
 	 * Private init methods
 	 */
 	private void initConfig(){
 		config = new CraftGuardConfig(this);
 		config.load();
 		if(config.getBooleanKey("debug") == true)craftGuardLogger.enableDebug();
 	}
 	
 	private void initModules(){
 		//STEP 1 : Adding all the modules to the registry
 		registry.add(new CraftGuardModule("craft", new CraftListener(this), this));
 		registry.add(new CraftGuardModule("smelt", new SmeltListener(this), this));
 		
 		//STEP 2 : Selecting the modules to enable, and enable them
 		@SuppressWarnings("unchecked")
		List<String> toEnable = (List<String>) config.getKey("modules");
 		HashSet<CraftGuardModule> enabled = new HashSet<CraftGuardModule>();
 		
		for(String element : toEnable){
 			if(!registry.containsModule(element)){
 				toEnable.remove(element);
 				craftGuardLogger.warning("Module " + element + " does not exist ! Ignoring it...");
 			}else{
 				CraftGuardModule module = registry.getModule(element);
 				this.getServer().getPluginManager().registerEvents(module.getListener(), this);
 				enabled.add(module);
 			}
 		}
 	}
 	
 	private void initLists(){
		listFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "list.yml");
 		listLoader = new ListLoader(this, new YamlConfiguration(), listFile);
 		listManager = new ListManager(this, listLoader);
 		listManager.init();
 	}
 	
 	
 	/*
 	 * Getters
 	 */
 	public CraftGuardPlugin getCraftGuard(){
 		return this;
 	}
 	
 	public CraftGuardLogger getCraftGuardLogger(){
 		return craftGuardLogger;
 	}
 	
 	public CraftGuardConfig getConfiguration(){
 		return config;
 	}
 	
 	public ListManager getListManager(){
 		return listManager;
 	}
 	
 	public SmeltFile getSmeltFile(){
 		return smeltFile;
 	}
 	
 	public ModuleRegistry getModuleRegistry(){
 		return registry;
 	}
 	
 }
