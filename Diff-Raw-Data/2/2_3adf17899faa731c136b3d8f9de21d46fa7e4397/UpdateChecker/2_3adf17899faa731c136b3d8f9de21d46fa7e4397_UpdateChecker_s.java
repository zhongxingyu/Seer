 package me.limebyte.battlenight.core.util;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.logging.Level;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class UpdateChecker {
 
     private static final String UPDATE_URL = "https://raw.github.com/BattleNight/BattleNight-Core/master/version.txt";
     private String version;
 
     public UpdateChecker(PluginDescriptionFile pdf) {
         this.version = removeSuffix(pdf.getVersion());
     }
 
     public void check() {
 
         try {
             URL update = new URL(UPDATE_URL);
             BufferedReader in = new BufferedReader(new InputStreamReader(update.openStream()));
             String latestVersion = in.readLine().trim();
             in.close();
 
             if (isNewer(latestVersion)) {
                 Messenger.log(Level.INFO, "Update v" + latestVersion + " available!");
             }
         } catch (Exception e) {
             Messenger.debug(Level.WARNING, "Failed to update check.");
             return;
         }
     }
 
     private String removeSuffix(String version) {
         return version.split("-")[0];
     }
 
     private boolean isNewer(String latestVersion) {
         if (version.equals(latestVersion)) return false;
 
         String[] verInts = version.split(".");
         String[] testInts = latestVersion.split(".");
 
         if (verInts.length != 3 || testInts.length != 3) return false;
 
         for (int i = 0; i < 3; i++) {
             int ver = Integer.parseInt(verInts[i]);
            int test = Integer.parseInt(verInts[i]);
 
             if (test > ver) return true;
         }
 
         return false;
     }
 }
