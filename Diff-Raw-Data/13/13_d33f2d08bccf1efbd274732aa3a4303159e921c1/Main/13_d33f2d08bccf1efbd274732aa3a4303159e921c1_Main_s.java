 package edgruberman.bukkit.simpletemplate;
 
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 public class Main extends org.bukkit.plugin.java.JavaPlugin {
 
     private static ConfigurationManager configurationManager;
     private static MessageManager messageManager;
     
     public void onLoad() {
         Main.configurationManager = new ConfigurationManager(this);
         Main.getConfigurationManager().load();
         
         Main.messageManager = new MessageManager(this);
         Main.getMessageManager().log("Version " + this.getDescription().getVersion());
         
         // TODO: Add plugin load code here.
     }
 	
     public void onEnable() {
         // TODO: Add plugin enable code here.
 
         new CommandManager(this);
         
        Main.messageManager.log("Plugin Enabled");
     }
     
     public void onDisable() {
         // TODO: Add plugin disable code here.
         
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
     }
     
     protected static ConfigurationManager getConfigurationManager() {
         return Main.configurationManager;
     }
     
     protected static MessageManager getMessageManager() {
         return Main.messageManager;
     }
 }
