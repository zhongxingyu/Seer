 package com.mutinycraft.jigsaw.FactionsExtra;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.massivecraft.factions.Factions;
 import com.massivecraft.factions.P;
 
 public class FactionsExtra extends JavaPlugin implements Listener{
 	
 	Logger log;
 	File factionFile;
     FileConfiguration factionData;
     
     private FactionsExtraCommandExecutor cmdExecutor;
 	private static final String VERSION = " v1.0";
 	
 	/***************** Enable *****************/
 	
 	@Override
 	public void onEnable(){
 		log = this.getLogger();
 		
 		getServer().getPluginManager().registerEvents(this, this);
 		new FactionsExtraEventHandler(this);
 		
 		try {
 			factionFile = new File(getDataFolder(), "FactionScores.yml");
 	        firstRun();
 	        // Get Factions
 			//getFactionsFromFile();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 		
 		factionData = new YamlConfiguration();
 		loadYamls();
 		
 		cmdExecutor = new FactionsExtraCommandExecutor(this);
 		getCommand("factionscore").setExecutor(cmdExecutor);
 		
 		log.info(this.getName() + VERSION + " enabled!");
 	}
 	
 	private void firstRun() throws Exception{
 		if(!factionFile.exists()){
 			factionFile.getParentFile().mkdirs();
 	        copy(getResource("FactionScores.yml"), factionFile);
 	    }
 	}
 	
 	private void copy(InputStream in, File file) {
 	    try {
 	        OutputStream fout = new FileOutputStream(file);
 	        byte[] buf = new byte[1024];
 	        int len;
 	        while((len=in.read(buf))>0){
 	            fout.write(buf,0,len);
 	        }
 	        fout.close();
 	        in.close();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 	
 	private void loadYamls() {
 	    try {
 	    	factionData.load(factionFile);
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 	
 	/***************** Config Handling *****************/
 	
 	public FileConfiguration getCustomConfig() {
 	    if (factionData == null) {
 	        this.reloadCustomConfig();
 	    }
 	    return factionData;
 	}
 	
 	public void reloadCustomConfig() {
 	    if (factionFile == null) {
 	    	factionFile = new File(getDataFolder(), "customConfig.yml");
 	    }
 	    factionData = YamlConfiguration.loadConfiguration(factionFile);
 	 
 	    InputStream defConfigStream = this.getResource("customConfig.yml");
 	    if (defConfigStream != null) {
 	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        factionData.setDefaults(defConfig);
 	    }
 	}
 	
 	/***************** Score Handling *****************/
 	
 	public List<Integer> getFactionData(String factionID){
 		if(factionData.contains(factionID)){
 			return factionData.getIntegerList(factionID);
 		}
 		else{
 			addFaction(factionID);
 			return factionData.getIntegerList(factionID);
 		}
 	}
 	
 	public int getFactionTier(String factionID){
 		List<Integer> data = factionData.getIntegerList(factionID);
 		if(data != null && data.size() >= 2){
 			return data.get(1);
 		}
 		return 0;
 	}
 	
 	public int getFactionScore(String factionID){
 		List<Integer> data = factionData.getIntegerList(factionID);
 		if(data != null && data.size() >= 2){
 			return data.get(0);
 		}
 		return 0;
 	}
 	
 	public void addFaction(String factionID){
 		if(!factionData.contains(factionID)){
 			List<Integer> data = new ArrayList<Integer>(1);
 			// Default score of 0
 			data.add(0);
 			// Default tier of 1
 			data.add(1);
 			updateFaction(factionID, data);
 		}
 	}
 	
 	public void updateFaction(String factionID, List<Integer> data){
 		
 		getCustomConfig().set(factionID, data);	
 		
 		if (factionData == null || factionFile  == null) {
 		    return;
 		    }
 	    try {
 	    	getCustomConfig().save(factionFile);
 	    } catch (IOException ex) {
 	        this.getLogger().log(Level.SEVERE, "Could not save data to " + factionFile, ex);
 	    }
 	    
 	}
 	
 	
 	// This should only be called one time when the plugin is first ran.
 	public void getFactionsFromFile(){
 		Set<String> factions = P.p.getFactionTags();
 		Iterator<String> iter = factions.iterator();
 	    while (iter.hasNext()) {
 	    	String factionName = iter.next();
 	    	List<Integer> data = new ArrayList<Integer>(1);
 			// Default score of 0
 			data.add(0);
 			// Default tier of 1
 			data.add(1);
 			updateFaction(Factions.i.getByTag(factionName).getId(), data);
 	    }
 	}
 	
 	/***************** Disable *****************/
	public void onDisable(){
 		log.info(this.getName() + VERSION + " disabled!");
 	}
 }
