 package edgruberman.bukkit.simpletemplate;
 
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 public class Main extends org.bukkit.plugin.java.JavaPlugin {
 
    protected static MessageManager messageManager;
     
     public void onLoad() {
         Configuration.load(this);
     }
 	
     public void onEnable() {
         Main.messageManager = new MessageManager(this);
         Main.messageManager.log("Version " + this.getDescription().getVersion());
         
         // TODO: Add plugin enable code here.
 
         Main.messageManager.log("Plugin Enabled");
     }
     
     public void onDisable() {
         // TODO: Add plugin disable code here.
         
         Main.messageManager.log("Plugin Disabled");
         Main.messageManager = null;
     }
 }
