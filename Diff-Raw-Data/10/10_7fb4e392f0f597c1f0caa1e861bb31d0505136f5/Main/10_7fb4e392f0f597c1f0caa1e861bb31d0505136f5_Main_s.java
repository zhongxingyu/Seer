 package edgruberman.bukkit.simpletemplate;
 
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 import edgruberman.bukkit.messagemanager.MessageManager;
 import edgruberman.bukkit.simpletemplate.commands.Multiple;
 import edgruberman.bukkit.simpletemplate.commands.Single;
 
 public final class Main extends JavaPlugin {
 
     public static MessageManager messageManager;
 
     private ConfigurationFile configurationFile;
     private boolean firstEnable = true;
 
     @Override
     public void onLoad() {
         this.configurationFile = new ConfigurationFile(this);
         this.configurationFile.load();
         this.setLoggingLevel();
         Main.messageManager = new MessageManager(this);
     }
 
     private void setLoggingLevel() {
         final String name = this.configurationFile.getConfig().getString("logLevel", "INFO");
         Level level = MessageLevel.parse(name);
         if (level == null) level = Level.INFO;
         this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Logging level set to: " + level.getName());
     }
 
     @Override
     public void onEnable() {
         this.loadConfiguration();
         this.firstEnable = false;
 
         new Single(this);
         new Multiple(this);
     }
 
     @Override
     public void onDisable() {
         if (this.configurationFile.isSaveQueued()) this.configurationFile.save();
     }
 
     public void loadConfiguration() {
         if (!this.firstEnable) this.configurationFile.load();
         @SuppressWarnings("unused")
         final FileConfiguration config = this.configurationFile.getConfig();
     }
 
 }
