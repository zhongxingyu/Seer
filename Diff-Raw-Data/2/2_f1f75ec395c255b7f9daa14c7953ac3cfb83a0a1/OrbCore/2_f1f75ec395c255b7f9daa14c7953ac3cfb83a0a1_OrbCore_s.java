 package net.krinsoft.orbsuite;
 
 import com.pneumaticraft.commandhandler.CommandHandler;
 import net.krinsoft.orbsuite.commands.OrbBaseCommand;
 import net.krinsoft.orbsuite.commands.OrbCheckCommand;
 import net.krinsoft.orbsuite.commands.OrbDepositCommand;
 import net.krinsoft.orbsuite.commands.OrbWithdrawCommand;
 import net.krinsoft.orbsuite.commands.PermissionHandler;
 import net.krinsoft.orbsuite.databases.Database;
 import net.krinsoft.orbsuite.listeners.OrbListener;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class OrbCore extends JavaPlugin {
     private boolean isDebugging = false;
 
     private File configFile;
     private FileConfiguration configuration;
 
     private CommandHandler commands;
 
     private Database database;
 
     @Override
     public void onEnable() {
         long time = System.nanoTime();
         initializeConfiguration();
         initializeEvents();
         initializeCommands();
         if (!initializeDb()) {
             return;
         }
         time = System.nanoTime() - time;
         getLogger().info("Enabled successfully in " + time + "ns. (" + (time / 1000000) + "ms)");
     }
 
     @Override
     public void onDisable() {
         long time = System.nanoTime();
         getServer().getScheduler().cancelTasks(this);
         database.close();
         time = System.nanoTime() - time;
         getLogger().info("Disabled successfully in " + time + "ns. (" + (time / 1000000) + "ms)");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         List<String> arguments = new ArrayList<String>(Arrays.asList(args));
         arguments.add(0, label);
         return commands.locateAndRunCommand(sender, arguments);
     }
 
     @Override
     public FileConfiguration getConfig() {
         if (configuration == null) {
             configuration = YamlConfiguration.loadConfiguration(configFile);
             configuration.setDefaults(YamlConfiguration.loadConfiguration(configFile));
         }
         return configuration;
     }
 
     @Override
     public void saveConfig() {
         try {
             getConfig().save(configFile);
         } catch (IOException e) {
             getLogger().warning("An error occurred while saving the config.yml.");
         }
     }
 
     public void setDebugging(boolean val) {
         isDebugging = val;
         getLogger().info("Debug mode: " + (val ? "enabled" : "disabled"));
     }
 
     public void debug(String message) {
         if (isDebugging) {
             getLogger().info("[Debug] " + message);
         }
     }
 
     private void initializeConfiguration() {
         configFile = new File(getDataFolder(), "config.yml");
         if (!configFile.exists()) {
             getLogger().info("Writing default config.");
             getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/config.yml")));
             getConfig().options().copyDefaults(true);
             saveConfig();
         }
         setDebugging(getConfig().getBoolean("plugin.debug", false));
     }
 
     private void initializeEvents() {
        getServer().getPluginManager().registerEvents(new OrbListener(this), this);
     }
 
     private void initializeCommands() {
         PermissionHandler perms = new PermissionHandler();
         commands = new CommandHandler(this, perms);
 
         // [BASIC] orb commands
         commands.registerCommand(new OrbBaseCommand(this));
         commands.registerCommand(new OrbCheckCommand(this));
         commands.registerCommand(new OrbDepositCommand(this));
         commands.registerCommand(new OrbWithdrawCommand(this));
 
         // [ADMIN] orb commands
         //commands.registerCommand(new OrbGiveCommand(this));
         //commands.registerCommand(new OrbTakeCommand(this));
     }
 
     private boolean initializeDb() {
         database = new Database(this);
         if (!database.connect()) {
             getLogger().warning("This plugin requires SQLite, which you do not appear to have.");
             getLogger().warning("Please add the SQLite library to your lib/ folder.");
             getServer().getPluginManager().disablePlugin(this);
             return false;
         }
         // create a task that saves player experience every 5 minutes
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 database.save();
             }
         }, 300L * 20L, 300L * 20L);
         return true;
     }
 
     public Database getDb() {
         return database;
     }
 
 }
