 /**
  * @Class: Config
  * @Author: Mike111177
  * @Purpose: This is a class that will handle every action related to the config
  * @Extends: JavaPlugin
  *  */
 package net.othercraft.steelsecurity;
 
 import java.util.Arrays;
import java.util.List;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Config extends JavaPlugin {
 		
 		public void loadConfiguration() {
 			defaults();
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 		}	
 		private void defaults() { //Defaults
 			//TODO chat and player join listeners.
 			getConfig().addDefault("General.Prefix", "[STS]");
 			getConfig().addDefault("General.Logon_Message_Enabled", true);
 			getConfig().addDefault("General.Logon_Message", "Steel Security is running on this server.");
 			getConfig().addDefault("AntiSpam.Censoring.Enabled", false );
 			getConfig().addDefault("AntiSpam.Censoring.Block_Words", Arrays.asList("Nodus", "Avo", "ICHG"));
 			getConfig().addDefault("AntiSpam.AntiCaps.Enabled", true);
 			getConfig().addDefault("AntiSpam.AntiCaps.Minimum_Length", 4);
 			getConfig().addDefault("AntiSpam.AntiCaps.Percent", 70);
 		}
		@SuppressWarnings("unchecked")
		public List<String> getConfigurationList(String Listpath) {//Gets a list
			return (List<String>) getConfig().getList(Listpath);
		}
		public String getConfigurationString(String Stringpath){//Gets a string
			return getConfig().getString(Stringpath);
		}
		public Boolean getConfigurationBoolean(String Booleanpath) {//gets a boolean
			return getConfig().getBoolean(Booleanpath);
		}
		public int getConfigurationInt(String Intpath) {//gets an int
			return getConfig().getInt(Intpath);
		}
 	}
