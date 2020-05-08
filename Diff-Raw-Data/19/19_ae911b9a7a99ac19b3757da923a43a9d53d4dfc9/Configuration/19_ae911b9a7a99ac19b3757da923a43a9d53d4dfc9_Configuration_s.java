 package at.junction.glacier;
 
 class Configuration {
     private final Glacier plugin;
     
     public boolean FREEZE_LAVA;
     private boolean ALLOW_TOGGLE;
    
     public Configuration(Glacier plugin) {
         this.plugin = plugin;
     }
     
     public void load() {
         FREEZE_LAVA = plugin.getConfig().getBoolean("freeze-lava", true);
         ALLOW_TOGGLE = plugin.getConfig().getBoolean("allow-toggle", true);
     }
    
    public void save() {
        
    }
 }
