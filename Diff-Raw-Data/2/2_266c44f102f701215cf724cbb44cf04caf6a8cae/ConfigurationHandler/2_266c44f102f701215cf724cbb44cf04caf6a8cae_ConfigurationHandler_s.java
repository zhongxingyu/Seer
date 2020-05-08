 package main.java.me.thelunarfrog.FrogAnnounce;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 /**
  * Handles the FrogAnnounce configuration.
  * @version 1.3.27.27 (Final? Unless more configuration nodes come to be.)
  * @category Configuration
  * @since 1.0.1.3
  * (Announced as 1.0)
  * @author Dan | TheLunarFrog
  */
 @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
 public class ConfigurationHandler extends FrogAnnounce {
 	
     protected static YamlConfiguration Settings;
     protected static File configFile;
 	private static boolean loaded = false;
 	private static FrogAnnounce fa = new FrogAnnounce();
 	FrogAnnounce plugin;
     
 	@Override
     public void saveConfig(){
     	try{
     		Settings.save(configFile);
     	}
     	catch(IOException e){
     		e.printStackTrace();
     	}
     }
 	protected static void loadConfig() throws InvalidConfigurationException{
         configFile = new File(Bukkit.getServer().getPluginManager().getPlugin("FrogAnnounce").getDataFolder(), "Configuration.yml");
         if (configFile.exists()) {
             Settings = new YamlConfiguration();
             try {
                 Settings.load(configFile);
         		FrogAnnounce.interval 			= 			  			Settings.getInt(		"Settings.Interval",	5);
         		FrogAnnounce.random			= 			  				Settings.getBoolean(	"Settings.Random",		false);
         		FrogAnnounce.permissionConfig 	= 			  			Settings.getBoolean(	"Settings.Permission",	true);
         		FrogAnnounce.strings 			= (ArrayList)(			Settings.getList(		"Announcer.Strings",	new ArrayList<String>()));
         		FrogAnnounce.tag 				= FrogAnnounce.colourizeText(Settings.getString("Announcer.Tag",		"&GOLD;[FrogAnnounce]"));
         		FrogAnnounce.toGroups 			= 			  			Settings.getBoolean(	"Announcer.ToGroups",	true);
         		FrogAnnounce.Groups 			= (ArrayList)(			Settings.getList(		"Announcer.Groups",		new ArrayList<String>()));
 //        		FrogAnnounce.useChatSuite		=						Settings.getBoolean(	"ChatSuite.Use-ChatSuite",false);
         		FrogAnnounce.ignoredPlayers		= (ArrayList<String>)	Settings.getList(		"ignoredPlayers",		new ArrayList<String>());
             } catch (FileNotFoundException ex) {
             	fa.grave("An exception has occurred while FrogAnnounce was loading the configuration.");
                 ex.printStackTrace();
             } catch (IOException ex) {
             	fa.grave("An exception has occurred while FrogAnnounce was loading the configuration.");
                 ex.printStackTrace();
             } catch (InvalidConfigurationException ex) {
             	fa.grave("An exception has occurred while FrogAnnounce was loading the configuration.");
                 ex.printStackTrace();
             }
             loaded = true;
         } else {
             try {
                 Bukkit.getServer().getPluginManager().getPlugin("FrogAnnounce").getDataFolder().mkdir();
                InputStream jarURL = ConfigurationHandler.class.getResourceAsStream("/Configuration.yml");
                 copyFile(jarURL, configFile);
                 Settings = new YamlConfiguration();
                 Settings.load(configFile);
                 loaded = true;
                 fa.info("Configuration loaded successfully.");
             } catch (Exception e) {
             	fa.grave("Exception occurred while creating a new configuration file!");
                 e.printStackTrace();
             }
         }
     }
     static private void copyFile(InputStream in, File out) throws Exception {
         InputStream fis = in;
         FileOutputStream fos = new FileOutputStream(out);
         try {
             byte[] buf = new byte[1024];
             int i = 0;
             while ((i = fis.read(buf)) != -1) {
                 fos.write(buf, 0, i);
             }
         } catch (Exception e) {
             throw e;
         } finally {
             if (fis != null) {
                 fis.close();
             }
             if (fos != null) {
                 fos.close();
             }
         }
     }
     protected ConfigurationHandler() {
     	this.plugin = FrogAnnounce.plugin;
     }
 }
