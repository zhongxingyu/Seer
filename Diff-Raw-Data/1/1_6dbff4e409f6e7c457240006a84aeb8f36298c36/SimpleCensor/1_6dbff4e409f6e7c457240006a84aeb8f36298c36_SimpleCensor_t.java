 package com.evosysdev.bukkit.taylorjb.simplecensor;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * SimpleCensor for Bukkit
  * 
  * This is a simple plugin that censors things contained in config.yml with the character specified
  * 
  * @author taylorjb
  * 
  */
 public class SimpleCensor extends JavaPlugin
 {
     // our censor
     private Censor censor;
 
     /**
      * On enable start listening to PLAYER_CHAT events
      */
     public void onEnable()
     {
         new SimpleCensorPlayerListener(this);
 
         List<String> censorList; // array of censored words to pass to censor
         char censorChar; // character to censor words with
         
         // load config
         FileConfiguration config = getConfig();
         try
         {
             // getStringList doesn't work here because config.set inserts single quotes around it
             censorList = Arrays.asList(config.getString("censor.words").split(",")); // >=(
             censorChar = config.getString("censor.char").charAt(0);
         }
         catch (NullPointerException npe)
         { // should happen on first-run when config doesn't exist
             getLogger().warning("Generating new config...");
             config.set("censor.words",
                     "fuck,shit,cunt,pussy,asshole,whore,douche,fag,bitch,nigger,slut,vagina,penis,cock,dick,queef,queer");
             config.set("censor.char", "*");
             saveConfig();
             
             censorList = Arrays.asList(config.getString("censor.words").split(",")); // >=(
             censorChar = config.getString("censor.char").charAt(0);
         }
 
         // create the censor
         censor = new Censor(censorChar, censorList);
         
         getLogger().info(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
     }
 
     /**
      * Tell people when disabled
      */
     public void onDisable()
     {
         getLogger().info(getDescription().getName() + " disabled!");
     }
 
     /**
      * Get our censor
      * 
      * @return the censor
      */
     public Censor getCensor()
     {
         return censor;
     }
 }
