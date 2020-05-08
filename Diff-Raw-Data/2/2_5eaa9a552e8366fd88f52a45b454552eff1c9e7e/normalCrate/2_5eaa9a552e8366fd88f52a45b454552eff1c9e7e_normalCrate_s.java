 package tv.mineinthebox.ManCo.utils;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import tv.mineinthebox.ManCo.ManCo;
 
 public class normalCrate {
 	public static String getCrateFoundMessage() {
 		try {
 			File f = new File(ManCo.getPlugin().getDataFolder() + File.separator + "config.yml");
 			if(f.exists()) {
 				FileConfiguration con = YamlConfiguration.loadConfiguration(f);
 				return ChatColor.translateAlternateColorCodes('&', con.getString("CrateFound.message"));
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static boolean isUnCrateMessageDisabled() {
 		try {
 			File f = new File(ManCo.getPlugin().getDataFolder() + File.separator + "config.yml");
 			if(f.exists()) {
 				FileConfiguration con = YamlConfiguration.loadConfiguration(f);
 				return con.getBoolean("disableUncrateMessage");
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static int getCrateSpawnHeight(Player p) {
 		if(p.getLocation().getBlock().getY() < 100) {
 			if(p.getLocation().getBlock().getY() < 58) {
				return (p.getLocation().getBlock().getY()+3);
 			} else {
 				return 120;	
 			}
 		} else {
 			return 256;
 		}
 	}
 	
 	public static int getCrateSpawnHeight(Location loc) {
 		if(loc.getY() < 100) {
 			return 120;
 		} else {
 			return 256;
 		}
 	}
 
 }
