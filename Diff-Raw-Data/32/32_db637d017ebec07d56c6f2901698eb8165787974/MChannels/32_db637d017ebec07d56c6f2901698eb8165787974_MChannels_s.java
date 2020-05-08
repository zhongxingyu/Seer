 package ca.q0r.mchannels;
 
 import ca.q0r.mchannels.channels.ChannelManager;
 import ca.q0r.mchannels.commands.MChannelsCommand;
 import ca.q0r.mchannels.configs.ChannelUtil;
 import ca.q0r.mchannels.events.ChannelListener;
 import com.miraclem4n.mchat.metrics.Metrics;
 import com.miraclem4n.mchat.util.MessageUtil;
 import com.miraclem4n.mchat.util.TimerUtil;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MChannels extends JavaPlugin {
     // Default Plugin Data
    public PluginManager pm;
    public PluginDescriptionFile pdfFile;

    // Metrics
    public Metrics metrics;
 
     public void onEnable() {
         // Initialize Plugin Data
         pm = getServer().getPluginManager();
         pdfFile = getDescription();
 
         try {
             // Initialize and Start the Timer
             TimerUtil timer = new TimerUtil();
 
             // Initialize Config
             ChannelUtil.initialize();
 
             // Initialize Classes
             ChannelManager.initialize();
 
             // Register Events
             registerEvents();
 
             // Setup Command
            regCommands("mchannel", new MChannelsCommand(this));
 
             // Stop the Timer
             timer.stop();
 
             // Calculate Startup Timer
             long diff = timer.difference();
 
             MessageUtil.log("[" + pdfFile.getName() + "] " + pdfFile.getName() + " v" + pdfFile.getVersion() + " is enabled! [" + diff + "ms]");
         } catch(NoClassDefFoundError ignored) {
             pm.disablePlugin(this);
         }
     }
 
     public void onDisable() {
         try {
             // Initialize and Start the Timer
             TimerUtil timer = new TimerUtil();
 
             getServer().getScheduler().cancelTasks(this);
 
             // Kill Config
             ChannelUtil.dispose();
 
             // Stop the Timer
             timer.stop();
 
             // Calculate Shutdown Timer
             long diff = timer.difference();
 
             MessageUtil.log("[" + pdfFile.getName() + "] " + pdfFile.getName() + " v" + pdfFile.getVersion() + " is disabled! [" + diff + "ms]");
         } catch(NoClassDefFoundError ignored) {
             System.err.println("[" + pdfFile.getName() + "] MChat not found disabling!");
             System.out.println("[" + pdfFile.getName() + "] " + pdfFile.getName() + " v" + pdfFile.getVersion() + " is disabled!");
         }
     }
 
     void registerEvents() {
         pm.registerEvents(new ChannelListener(), this);
     }
 
    public void reloadConfigs() {
        ChannelUtil.initialize();
    }

     void regCommands(String command, CommandExecutor executor) {
         if (getCommand(command) != null)
             getCommand(command).setExecutor(executor);
     }

    void initializeClasses() {
        ChannelManager.initialize();

    }
 }
