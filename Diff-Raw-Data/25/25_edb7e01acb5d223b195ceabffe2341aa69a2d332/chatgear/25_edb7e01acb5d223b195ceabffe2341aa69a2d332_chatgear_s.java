 package com.github.mpotacon.chatgear;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class chatgear extends JavaPlugin {
 	public static chatgear plugin;
 	public final chatListener cl = new chatListener();
 	public final chatPEx cPEx = new chatPEx();
 	File configFile;            
     FileConfiguration config;   
 	
 	@Override
 	public void onEnable(){
        configFile = new File(getDataFolder(), "config.yml");   
         try {
             firstRun();
         } catch (Exception e) {
             e.printStackTrace();
         }
         config = new YamlConfiguration();
         loadYamls();
 		PluginManager pm = getServer().getPluginManager();
 		getCommand("tell").setExecutor(new tellCommand());
 		getCommand("ping").setExecutor(new pingCommand());
 		getCommand("mute").setExecutor(new muteCommand());
 		if(pm.isPluginEnabled("PermissionsEx")){
 			pm.registerEvents(this.cPEx, this);
 		}else{
 			pm.registerEvents(this.cl, this);
 		}		
 	}	
 	
 	 private void firstRun() throws Exception {
	        if(!configFile.exists()){                        
 	            configFile.getParentFile().mkdirs();         
 	            copy(getResource("config.yml"), configFile);
 	        }
 	    }
 	 
 	 private void copy(InputStream in, File file) {
 	        try {
 	            OutputStream out = new FileOutputStream(file);
 	            byte[] buf = new byte[1024];
 	            int len;
 	            while((len=in.read(buf))>0){
 	                out.write(buf,0,len);
 	            }
 	            out.close();
 	            in.close();
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	    }
 	 
 	 public void loadYamls() {
	        try {
	            config.load(configFile); 
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	    } 
 	 
 	 public void saveYamls() {
 	        try {
 	            config.save(configFile);
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        }
 	    }
 
 	@Override
 	public void onDisable(){
		 saveYamls();
 	}
 	
 }
