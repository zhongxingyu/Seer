 package me.devcom.pdrill;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.util.config.Configuration;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 public class ConfigurationManager {
 	//private static final String CONFIG_HEADER = "";
 
 	public PDrill plugin;
 
 	public File configFile;
 	public Configuration config;
 	
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public final String prefix = "[PDrill] ";
 	String fileName;
 	String fileDir;
 	
 	//		Scripts< name, script>
 	public HashMap< String, String > scripts = new HashMap< String, String>();
 	public HashMap< Integer, Fuel > fuels = new HashMap< Integer, Fuel > ();
 	public HashMap< Integer, Integer > drops = new HashMap< Integer, Integer > ();
 	public HashMap< Integer, Integer > placeCosts = new HashMap< Integer, Integer > ();
 	
 	public double blockSpeed = 0.1;
 	
 	public boolean dropItemNaturally;
 	public List<Integer> dropItemList;
 	public List< Integer > stopblocks = new ArrayList< Integer >();

	public boolean checkItemChange;
 	
 	public ConfigurationManager(PDrill instance, String name, String dir) {
 		plugin = instance;
 		fileName = name;
 		fileDir = dir;
 		
 		configFile = loadFile(fileName, fileDir);
 		config = new Configuration( configFile );
 	}
 
 	public void load() {
 		
 		config.load();
 		
 		dropItemNaturally = config.getBoolean( "config.dropItemNaturally", false );
 		dropItemList = config.getIntList( "config.dropItemList", null );
		checkItemChange = config.getBoolean( "config.checkItemChange", false );
 		blockSpeed = config.getDouble("config.speed", 0.1);
 		stopblocks = config.getIntList("config.stopBlocks", null);
 		
 		
 		loadPlaceCosts();
 		loadDrops();
 		loadScripts();
 		loadFuels();
 		
 		logger.info( prefix + "Config loaded!");
 		
 		//try {
         //    config.setHeader(CONFIG_HEADER);
         //} catch (Throwable t) {}
 		
 		config.save();
 	}
 	
 	private void loadPlaceCosts() {
 		List<String> costList = config.getStringList("config.itemPlaceCost", null);
 		if( costList != null){
 			for( String dropNode : costList ){
 				String[] splitNode = dropNode.split( ";" );
 				Integer fuel = Integer.parseInt( splitNode[0] );
 				Integer cost = Integer.parseInt( splitNode[1] );
 				
 				placeCosts.put(fuel, cost);
 			}
 			//logger.info( prefix + "Drops loaded!");
 		}
 	}
 
 	public void loadDrops() {
 		List<String> dropList = config.getStringList("config.dropItemChange", null);
 		if( dropList != null){
 			for( String dropNode : dropList ){
 				String[] splitNode = dropNode.split( ";" );
 				Integer sourceId = Integer.parseInt( splitNode[0] );
 				Integer targetId = Integer.parseInt( splitNode[1] );
 				
 				//logger.info( prefix + "Id [" + sourceId + "] converts to [" + targetId + "]");
 				drops.put(sourceId, targetId);
 			}
 			//logger.info( prefix + "Drops loaded!");
 		}
 	}
 
 	public void loadScripts() {
 		List<String> scriptList = config.getKeys("script");
 		if( scriptList != null){
 			for( String scriptNode : scriptList){
 				String path = "script."+scriptNode+".";
 				String name = scriptNode;
 				String script = config.getString(path + "script");
 				
 				//logger.info( prefix + "New script added! [" + name + "][" + script + "]");
 				scripts.put(name, script);
 			}
 			//logger.info( prefix + "Scripts loaded!");
 		}
 	}
 
 	public void loadFuels() {
 		List<String> fuelList = config.getKeys("fuel");
 		if( fuelList != null){
 			for( String fuelNode : fuelList){
 				String path = "fuel."+fuelNode+".";
 			    Integer fuelId = config.getInt(path + "fuelId", -1);
 			    Integer drillAirSpeed = config.getInt(path + "drillAirSpeed", -1);
 			    Integer drillBlockSpeed = config.getInt(path + "drillBlockSpeed", -1);
 			    Integer fuelConsumptionBlockCount = config.getInt(path + "fuelConsumptionBlockCount", -1);
 			    Integer fuelConsumptionFuelCount = config.getInt(path + "fuelConsumptionFuelCount", -1);
 			    Fuel fuel = new Fuel(fuelId, drillAirSpeed, drillBlockSpeed, fuelConsumptionBlockCount, fuelConsumptionFuelCount, fuelNode);
 			    
 			    //logger.info( prefix + "New fuel added! [" + fuelId + " " + drillAirSpeed + " " + drillBlockSpeed + " " + fuelConsumptionBlockCount + " " + fuelConsumptionFuelCount + "]");
 			    fuels.put(fuelId, fuel);
 			}
 			//logger.info( prefix + "Fuels loaded!");
 		}
 	}
 
 	public File loadFile( String name, String dir ){
 		File file = new File( dir + name );
 		if(!file.exists()){
 			
 			try {
 				file.createNewFile();
 			} catch ( IOException e ){
 				logger.info(prefix + "File creation failed! (" + e.toString() + ")");
 				return null;
 			} finally {
 				logger.info(prefix + "File creation succeeded!");
 				
 				createDefaultConfig( file );
 			}
 			
 		}
 		return file;
 	}
 
 	private void createDefaultConfig(File file) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public String getScriptByName(String scriptName) {
 		if( scripts.containsKey( scriptName )){
 			return scripts.get( scriptName );
 		}else{
 			return "";
 		}
 	}
 
 	public Fuel getFuelByName(String string) {
 		for( Entry<Integer, Fuel> entry : fuels.entrySet() ){
 			if( entry.getValue().configName.equalsIgnoreCase( string )){
 				return entry.getValue();
 			}
 		}
 		return null;
 	}
 }
