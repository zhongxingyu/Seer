 package com.bukkit.jason.pitfall;
 
 import java.io.File;
 
 import org.bukkit.util.config.Configuration;
 
 public class PitfallSettings {
 
     /**
 * Settings
 */
 
     public static int pitItem = 82;
    public static String[] blackList = null;
 
     /**
 * Bukkit config class
 */
     public static Configuration config = null;
 
     /**
 * Load and parse the YAML config file
 */
     public static void load() {
 
         File dataDirectory = new File("plugins" + File.separator + "Pitfall" + File.separator);
 
         dataDirectory.mkdirs();
 
         File file = new File("plugins" + File.separator + "Pitfall", "config.yml");
 
 
         config = new Configuration(file);
         config.load();
 
         if (!file.exists()) {
             
 //            HashMap<String, Integer> pitHash = new HashMap<String, Integer>();
 //            pitHash.put( "pitItem", 82 );
             config.setProperty("pitfall.pitItem", 82);
             config.setProperty("pitfall.block.blackList", "[]");
             
             config.save();
         }
         setSettings();
     }
 
     /**
 * Sets the internal variables
 */
     private static void setSettings() {
 
         pitItem = config.getInt("pitfall.pitItem", pitItem);
         String blackListStr ="";
         blackListStr= config.getString("pitfall.block.blackList", blackListStr);
         if(blackListStr.startsWith("[") && blackListStr.endsWith("]"))
         {
         	blackListStr=blackListStr.substring(1,blackListStr.length()-1);
         	blackList=blackListStr.split(",");
         }
         else
         {
         	System.out.println("WARNING: pitfall.block.blackList is malformed! Must be in following format: [blockId, blockId, blockId]");
         }
         
 //        rainInterval = config.getInt("mothernature.rain.interval", rainInterval);
 //        rainLength = config.getInt("mothernature.rain.duration", rainLength);
 //
 //        thunderInterval = config.getInt("mothernature.rain.interval", thunderInterval);
 //        thunderLength = config.getInt("mothernature.rain.duration", thunderLength);
 //
 //        lightningWand = config.getInt("mothernature.wand", lightningWand);
 
 
     }
 
 
 }
