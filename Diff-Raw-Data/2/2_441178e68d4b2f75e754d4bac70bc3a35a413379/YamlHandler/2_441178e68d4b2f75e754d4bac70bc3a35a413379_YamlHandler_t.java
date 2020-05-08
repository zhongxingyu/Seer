 package regalowl.databukkit;
 
 
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.scheduler.BukkitTask;
 
 public class YamlHandler {
     private Logger log;
     private Plugin plugin;
     private BukkitTask saveTask;
     private Long saveInterval;
     private String currentFC;
     private HashMap<String, FileConfiguration> yml = new HashMap<String, FileConfiguration>();
     private HashMap<String, File> files = new HashMap<String, File>();
     
     YamlHandler(Plugin plugin) {
     	this.plugin = plugin;
     	log = Logger.getLogger("Minecraft");
     }
 
     public void registerFileConfiguration(String file) {
     	File configFile = new File(plugin.getDataFolder(), file + ".yml");
     	files.put(file, configFile);
     	checkFile(configFile);
     	FileConfiguration fileConfiguration = new YamlConfiguration();
     	loadFile(configFile, fileConfiguration);
     	yml.put(file, fileConfiguration);
     }
     
 	private void checkFile(File file) {
 		try {
 			if (!file.exists()) {
 				file.getParentFile().mkdirs();
 				file.createNewFile();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
     private void loadFile(File file, FileConfiguration fileConfiguration) {
         try {
         	fileConfiguration.load(file);
         } catch (Exception e) {
             e.printStackTrace();
 	    	log.severe("[DataBukkit["+plugin.getName()+"]]Bad "+file.getName()+" file.");
         }
     }
     
 	public void saveYamls() {
 		Collection<String> keys = yml.keySet();
 		ArrayList<String> newKeys = new ArrayList<String>();
 		for (String key : keys) {
 			newKeys.add(key);
 		}
 		for (String key : newKeys) {
 			try {
 				yml.get(key).save(files.get(key));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
     }
 
 	public FileConfiguration getFileConfiguration(String fileConfiguration){
 		if (yml.containsKey(fileConfiguration)) {
 			return yml.get(fileConfiguration);
 		} else {
 			return null;
 		}
 	}
 	public FileConfiguration gFC(String fileConfiguration){
 		if (yml.containsKey(fileConfiguration)) {
 			return yml.get(fileConfiguration);
 		} else {
 			return null;
 		}
 	}
 	
 	
 	public void setSaveInterval(long interval) {
 		this.saveInterval = interval;
 		if (saveTask != null) {saveTask.cancel();}
 		saveTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
 			public void run() {
 				saveYamls();
 			}
 		}, saveInterval, saveInterval);
 	}
 	
 	public void shutDown() {
 		if (saveTask != null) {saveTask.cancel();}
 		saveYamls();
 	}
 
 	
 	
 	public void registerDefault(String path, Object def) {
 		if (!gFC(currentFC).isSet(path)) {
 			gFC(currentFC).set(path, def);
 			try {
 				gFC(currentFC).save(files.get(currentFC));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		};
 	}
 	public void set(String path, Object value) {
 		gFC(currentFC).set(path, value);
 	}
 	public void setCurrentFileConfiguration(String fileConfiguration) {
 		if (getFileConfiguration(fileConfiguration) != null) {
 			currentFC = fileConfiguration;
 		} else {
 			currentFC = null;
 		}
 	}
 	public String gS(String path) {
 		if (currentFC == null) {return null;}
 		return gFC(currentFC).getString(path);
 	}
 	public int gI(String path) {
 		if (currentFC == null) {return -1;}
 		return gFC(currentFC).getInt(path);
 	}
 	public double gD(String path) {
 		if (currentFC == null) {return -1;}
 		return gFC(currentFC).getDouble(path);
 	}
 	public boolean gB(String path) {
 		if (currentFC == null) {return false;}
 		return gFC(currentFC).getBoolean(path);
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
 
 	public void copyFromJar(String name) {
 		File configFile = new File(plugin.getDataFolder(), name + ".yml");
 	    if(!configFile.exists()){
 	    	configFile.getParentFile().mkdirs();
	        copy(plugin.getClass().getResourceAsStream("/"+name+".yml"), configFile);
 	    }
 	}
 
 }
