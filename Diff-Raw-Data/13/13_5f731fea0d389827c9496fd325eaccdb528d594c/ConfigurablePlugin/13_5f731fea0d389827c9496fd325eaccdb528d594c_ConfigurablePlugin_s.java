 package net.craftminecraft.bungee.bungeeyaml.pluginapi;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Level;
 
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.FileConfiguration;
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.YamlConfiguration;
 import net.md_5.bungee.api.plugin.Plugin;
 
 public class ConfigurablePlugin extends Plugin {
     private FileConfiguration newConfig = null;
     private File configFile = null;
 
     public FileConfiguration getConfig() {
		if (newConfig == null) {
             reloadConfig();
         }
         return newConfig;
     }
 
	public void reloadConfig() {
         newConfig = YamlConfiguration.loadConfiguration(configFile);
 
         InputStream defConfigStream = this.getResourceAsStream("config.yml");
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 
             newConfig.setDefaults(defConfig);
         }
	}
	
     public void saveConfig() {
         try {
             getConfig().save(configFile);
         } catch (IOException ex) {
             this.getProxy().getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
         }
     }
 
     public void saveDefaultConfig() {
         if (!configFile.exists()) {
             saveResource("config.yml", false);
         }
     }
     
     public void saveResource(String resourcePath, boolean replace) {
         if (resourcePath == null || resourcePath.equals("")) {
             throw new IllegalArgumentException("ResourcePath cannot be null or empty");
         }
 
         resourcePath = resourcePath.replace('\\', '/');
         InputStream in = getResourceAsStream(resourcePath);
         if (in == null) {
             throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + this.getDescription().getName());
         }
 
         File outFile = new File(this.getDataFolder(), resourcePath);
         int lastIndex = resourcePath.lastIndexOf('/');
         File outDir = new File(this.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
 
         if (!outDir.exists()) {
             outDir.mkdirs();
         }
 
         try {
             if (!outFile.exists() || replace) {
                 OutputStream out = new FileOutputStream(outFile);
                 byte[] buf = new byte[1024];
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
                 out.close();
                 in.close();
             } else {
                 this.getProxy().getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
             }
         } catch (IOException ex) {
             this.getProxy().getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
         }
     }
     
     @Override
     public final void onLoad() {
    	this.configFile = new File(this.getDataFolder(), "config.yml");
         this.onLoading();
     }
 
     public void onLoading() {
 
     }
 }
