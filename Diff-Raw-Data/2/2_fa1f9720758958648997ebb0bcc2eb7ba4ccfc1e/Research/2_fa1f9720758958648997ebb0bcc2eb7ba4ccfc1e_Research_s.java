 package net.croxis.plugins.research;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Research extends JavaPlugin {
 	static TechManager techManager;
 	public boolean useSpout = false;
 	public ArrayList<String> permissions = new ArrayList<String>();
 	public ArrayList<Integer> cantPlace = new ArrayList<Integer>();
 	public ArrayList<Integer> cantBreak = new ArrayList<Integer>();
 	public ArrayList<Integer> cantCraft = new ArrayList<Integer>();
 	public Logger logger;
 	
 	private FileConfiguration techConfig = null;
 	private File techConfigFile = new File(getDataFolder(), "tech.yml");
 	private RBlockListener blockListener = new RBlockListener();
 	
     public void onDisable() {
         // TODO: Place any custom disable code here.
         System.out.println(this + " is now disabled!");
     }
     
     public void logInfo(String message){
     	logger.log(Level.INFO, "[Research] " + message);
     }
     
     public void logWarning(String message){
     	logger.log(Level.WARNING, "[Research] " + message);
     }
 
     @SuppressWarnings("unchecked")
 	public void onEnable() {
     	logger = Logger.getLogger(JavaPlugin.class.getName());
     	techManager = new TechManager(this);
     	// Set up default systems
     	useSpout = this.getConfig().getBoolean("useSpout", false);
     	permissions = (ArrayList<String>) this.getConfig().getStringList("default.permissions");
     	cantPlace = (ArrayList<Integer>) this.getConfig().getIntegerList("default.cantplace");
     	cantBreak = (ArrayList<Integer>) this.getConfig().getIntegerList("default.cantbreak");
    	cantCraft = (ArrayList<Integer>) this.getConfig().getIntegerList("default.spout.cantcraft");
     	getConfig().options().copyDefaults(true);
         saveConfig();
         logInfo("Loaded default permissions. Now loading techs.");
         
         // To make this work better I am creating a root node that all starting techs link to
         //Tech t = new Tech();
         //t.name = "root";
         //TechManager.techs.put("root", t);
         
         
         // Load tech config
         getTechConfig().options().copyDefaults(true);
         saveTechConfig();
         
         Set<String> techNames = techConfig.getKeys(false);
         int i = 0;
         for (String techName : techNames){
         	Tech tech = new Tech();
         	tech.name = techName;
         	if(!techConfig.contains(techName + ".cost"))
         		continue;
         	tech.cost = techConfig.getInt(techName + ".cost");        	
         	if(techConfig.contains(techName + ".permissions"))
         		tech.permissions = techConfig.getStringList(techName + ".permissions");        	
         	if(techConfig.contains(techName + ".description"))
         		tech.description = techConfig.getString(techName + ".description");
         	if(techConfig.contains(techName + ".prereqs"))
         		tech.preReqs = techConfig.getStringList(techName + ".prereqs");        	
         	if(techConfig.contains(techName + ".canPlace"))
         		tech.canPlace = techConfig.getIntegerList(techName + ".canPlace");        	
         	if(techConfig.contains(techName + ".canBreak"))
         		tech.canBreak = techConfig.getIntegerList(techName + ".canBreak");        	
         	if(useSpout)
         		if(techConfig.contains(techName + ".spout.canCraft"))
             		tech.canCraft = techConfig.getIntegerList(techName + ".spout.canCraft");
         	
         	techManager.techs.put(techName, tech);
         	i++;
         }        
         logInfo(Integer.toString(i) + " techs loaded. Now linking tree.");
         
         for (Tech tech : techManager.techs.values()){
         	// Check if it is a starting tech
         	//if(tech.preReqs.isEmpty()){
         	//	tech.parents.add(techManager.techs.get("root"));
         	//	techManager.techs.get("root").children.add(tech);
         	//} else {
         		for(String parentName : tech.preReqs){
         			if(techManager.techs.containsKey(parentName)){
         				tech.parents.add(techManager.techs.get(parentName));
         				techManager.techs.get(parentName).children.add(tech);
         			} else {
         				this.logWarning("Could not link " + tech.name + " with " + parentName + ". One of them is malformed!");
         			}
         		}
         	//}
         }
         logInfo("Tech tree linking complete. Mounting player database.");
         setupDatabase();
         logInfo("Database mounted. Setup complete.");
         
         // This should be it. Permission setups should happen onPlayerJoin
         
         // Test for spout
         Plugin test = getServer().getPluginManager().getPlugin("Spout");
         if (test == null)
         	this.useSpout = false;
         
         this.getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, new RPlayerListener(this), Priority.Normal, this);
         this.getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
         this.getServer().getPluginManager().registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Highest, this);
         if(useSpout)
         	this.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, new RInventoryListener(), Priority.Highest, this);
         else
         	this.logWarning("Spout is not in use. Please get spout for maximum enjoyment");
         
         System.out.println(this + " is now enabled!");
     }
     
     public static TechManager getTechManager(){
     	return techManager;
     }
     
     public void reloadCustomConfig() {
         techConfig = YamlConfiguration.loadConfiguration(techConfigFile);
      
         // Look for defaults in the jar
         InputStream defConfigStream = getResource("tech.yml");
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             techConfig.setDefaults(defConfig);
         }
     }
     
     public FileConfiguration getTechConfig() {
         if (techConfig == null) {
             reloadCustomConfig();
         }
         return techConfig;
     }
     
     public void saveTechConfig() {
         try {
             techConfig.save(techConfigFile);
         } catch (IOException ex) {
             Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + techConfigFile, ex);
         }
     }
     
     private void setupDatabase() {
         try {
             getDatabase().find(SQLPlayer.class).findRowCount();
         } catch (PersistenceException ex) {
             System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
             installDDL();
         }
     }
     
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(SQLPlayer.class);
         return list;
     }
 }
