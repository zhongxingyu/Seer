 package tk.kirlian.SignTraderWithDucks;
 
 import com.nijikokun.register.payment.Method;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import java.util.logging.Logger;
 import tk.kirlian.util.CustomLogger;
 import tk.kirlian.util.itemdb.*;
 import tk.kirlian.SignTraderWithDucks.signs.SignManager;
 import tk.kirlian.SignTraderWithDucks.permissions.PermissionsProvider;
 
 /**
  * A Bukkit plugin that allows fully automated shops using signs!
  */
 public class SignTraderPlugin extends JavaPlugin {
     public Logger log;
     private SignTraderBlockListener blockListener;
     public SignTraderPlayerListener playerListener; // Needed by SignTraderCommand
     private SignTraderServerListener serverListener;
     private SignManager signManager;
     public Method economyMethod; // Needed by various inventory adapters
 
     @Override
     public void onEnable() {
         // Logger needs to be created here because the description file
         // isn't loaded until now
         log = CustomLogger.getLogger(getDescription().getName());
 
         // I don't know where to put this, so it's going here!
        if(getDataFolder().isDirectory()) {
            log.info("Data folder already exists -- very nice.");
        } else {
            if(getDataFolder().mkdirs()) {
                log.info("Created data folder.");
            } else {
                log.warning("Could not create data folder. Stuff may fail later.");
            }
         }
 
         // Load the ItemDB beforehand, so if it fails, it fails now, and
         // not in the obscure depths of the sign handling code :)
         ItemDB itemDB = ItemDB.getInstance();
 
         // Register events
         PluginManager pm = getServer().getPluginManager();
         blockListener = new SignTraderBlockListener(this);
         playerListener = new SignTraderPlayerListener(this);
         serverListener = new SignTraderServerListener(this);
         pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.High, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Event.Priority.Normal, this);
 
         // Initialize the sign manager
         log.info("Initializing sign manager...");
         signManager = SignManager.getInstance(this);
 
         // Initialize Permissions
         PermissionsProvider permissionsProvider = PermissionsProvider.getBest(this);
         log.info("Using " + permissionsProvider.getName() + " for permissions.");
 
         // Register commands
         getCommand(SignTraderCommand.COMMAND_NAME).setExecutor(new SignTraderCommand(this));
 
         final String version = getDescription().getVersion();
         log.info("Version " + version + " enabled. " +
                  "No viruses, honest!");
     }
 
     @Override
     public void onDisable() {
         final String version = getDescription().getVersion();
         log.info("Version " + version + " disabled. " +
                  "Have a nice day!");
         signManager.store();
     }
 }
