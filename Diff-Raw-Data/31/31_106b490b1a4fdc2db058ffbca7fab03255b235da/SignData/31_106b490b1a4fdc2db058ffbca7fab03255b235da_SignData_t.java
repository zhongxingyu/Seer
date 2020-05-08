 package com.wolvencraft.prison.mines.settings;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.DisplaySign;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class SignData {	
 	/**
 	 * Saves all the sign data to disc
 	 */
 	public static void saveAll() {
 		for (DisplaySign sign : PrisonMine.getLocalSigns()) sign.saveFile();
 	}
 	
 	/**
 	 * Loads the sign data from disc
 	 * @return Loaded list of signs
 	 */
 	public static List<DisplaySign> loadAll() {
 		List<DisplaySign> signs = new ArrayList<DisplaySign>();
 		File signFolder = new File(PrisonMine.getInstance().getDataFolder(), "signs");
         if (!signFolder.exists() || !signFolder.isDirectory()) {
             signFolder.mkdir();
             return signs;
         }
        File[] signFiles;
        if(PrisonMine.getInstance().getVersion() < 1.3) {
            signFiles = signFolder.listFiles(new FileFilter() {
                public boolean accept(File file) { return file.getName().contains(".yml"); }
            });
        } else {
            signFiles = signFolder.listFiles(new FileFilter() {
                public boolean accept(File file) { return file.getName().contains(".psign.yml"); }
            });
        }
         
         for (File signFile : signFiles) {
         	try {
 	            FileConfiguration mineConf = YamlConfiguration.loadConfiguration(signFile);
 	            Object sign = mineConf.get("displaysign");
 	            if (sign instanceof DisplaySign) signs.add((DisplaySign) sign);
         	}
         	catch (IllegalArgumentException ex) {
         		Message.log(Level.SEVERE, ex.getMessage());
         		continue;
         	}
         }
         return signs;
 	}
 }
