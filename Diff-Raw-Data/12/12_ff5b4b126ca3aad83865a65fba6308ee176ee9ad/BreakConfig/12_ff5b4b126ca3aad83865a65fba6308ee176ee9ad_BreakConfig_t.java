 package com.Cayviel.FallBreaks;
 
 import java.io.File;
 
 import org.bukkit.util.config.Configuration;
 
 public class BreakConfig{
 
     public static String directory;
     public static File configFile;
     
     public static boolean breakReqBootTier;
     public static boolean breakReqDamageTier;
     public static boolean breakReqBoots;
     public static boolean dropReqBootTier;
     public static boolean dropEnabled;
     public static boolean dropSimPunch;
     
 	
 	
 	BreakConfig(File conf, String dir){
 		directory = dir;
 		configFile = conf;
 	}
 
 	public void configCheck(){
 	        new File(directory).mkdir();
 	        if(!configFile.exists()){
 	            try {
 	            	configFile.createNewFile();
 	                addDefaults();
 
 	            } catch (Exception ex) {
 	                ex.printStackTrace();
 	            }
 	        }
             loadkeys();
 	    }
 	
    public void write(String root, Object x){
        Configuration config = load();
        config.setProperty(root, x);
        config.save();
    }
 
    private static Configuration load(){
 
        try {
            Configuration config = new Configuration(configFile);
            config.load();
            return config;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void addDefaults(){
     FallBreaks.log.info("Generating Config File...");
 
     write("Drops.Enabled", true);
     write("Drops.Lowest Tier Only", false);
     write("Drops.Requirements.Tiered Boots", true);
     write("Break.Requirements.Tiered Boots", true);
     write("Break.Requirements.Tiered Damage", true);
     write("Break.Requirements.Boots", true);
     
     write("Breakables.DEFAULT",false);
     write("Breakables.GLASS",true);
     write("Breakables.ICE",true);
     FallBreaks.log.info("Loading Config File...");
    }
    
    private static void loadkeys(){
 	   
        Configuration config = load();
        
        dropEnabled = config.getBoolean("Drops.Enabled", true);
        dropSimPunch = config.getBoolean("Boots.Lowest Tier Only", false);
        dropReqBootTier = config.getBoolean("Break.Requirements.Tiered Boots", true);
        breakReqBootTier = config.getBoolean("Break.Requirements.Tiered Boots", true);
        breakReqDamageTier = config.getBoolean("Break.Requirements.Tiered Damage", true);
        breakReqBoots = config.getBoolean("Break.Requirements.Boots", true);
    
    }
    public static boolean getdefaultbreak(){
        Configuration config = load();
       return config.getBoolean("Breakables.DEFAULT",false);
    }
    
    public static boolean getbreak(String materialname){
        Configuration config = load();
       return config.getBoolean("Breakables."+materialname,getdefaultbreak());
    }
 }
