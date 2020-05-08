 package de.cubenation.plugins.utils.pluginapi;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.avaje.ebean.EbeanServer;
 
 import de.cubenation.plugins.utils.chatapi.ChatService;
 import de.cubenation.plugins.utils.commandapi.CommandsManager;
 import de.cubenation.plugins.utils.commandapi.ErrorHandler;
 import de.cubenation.plugins.utils.commandapi.exception.CommandException;
 import de.cubenation.plugins.utils.commandapi.exception.CommandManagerException;
 import de.cubenation.plugins.utils.commandapi.exception.CommandWarmUpException;
 import de.cubenation.plugins.utils.confirmapi.ConfirmService;
 import de.cubenation.plugins.utils.permissionapi.PermissionService;
 
 public abstract class BasePlugin extends JavaPlugin {
     // framework services
     protected PermissionService permissionService;
     protected ChatService chatService;
     protected ConfirmService confirmService;
     protected CommandsManager commandsManager;
 
     private EbeanServer customDatabaseServer = null;
     private ErrorHandler errorHandler;
 
     @Override
     public final EbeanServer getDatabase() {
         if (customDatabaseServer != null) {
             return customDatabaseServer;
         } else {
             return super.getDatabase();
         }
     }
 
     @Override
     public final void onEnable() {
         ScheduleManager.addPlugin(this);
 
         preEnableActions();
 
         errorHandler = createCustomErrorHandler();
         if (errorHandler == null) {
             errorHandler = new ErrorHandler() {
                 @Override
                 public void onError(Throwable thrown) {
                     BasePlugin.this.onError(thrown);
                 }
             };
         }
 
         if (getResource("config.yml") != null) {
             getLogger().info("save default config");
             saveDefaultConfig();
         } else {
             getLogger().info("save no default config");
         }
         reloadConfig();
 
         customDatabaseServer = getCustomDatabaseServer();
         if (customDatabaseServer == null) {
             setupDatabase();
         }
         migrateOldData();
 
         permissionService = new PermissionService();
         chatService = new ChatService(this, permissionService);
         confirmService = new ConfirmService();
 
         initialCustomServices();
 
         startCustomServices();
 
         initialCustomEventListeners();
 
         registerCustomEventListeners();
 
         try {
             commandsManager = new CommandsManager(this);
             commandsManager.setPermissionInterface(permissionService);
             commandsManager.setErrorHandler(errorHandler);
             registerCommandSets();
         } catch (CommandWarmUpException e) {
             getLogger().log(Level.SEVERE, "error on register command", e);
         } catch (CommandManagerException e) {
             getLogger().log(Level.SEVERE, "error on inital command manager", e);
         }
 
         getLogger().info("version " + getDescription().getVersion() + " enabled");
 
        startCustomServices();

         startScheduleTasks();
 
         postEnableActions();
     }
 
     @Override
     public final void onDisable() {
         confirmService.clear();
         commandsManager.clear();
 
         stopCustomServices();
 
         getServer().getScheduler().cancelTasks(this);
 
         saveConfig();
 
         ScheduleManager.removePlugin(this);
 
         getLogger().info("unloaded");
     }
 
     private void startScheduleTasks() {
         List<ScheduleTask> scheduledTasks = new ArrayList<ScheduleTask>();
 
         registerScheduledTasks(scheduledTasks);
 
         for (ScheduleTask scheduledTask : scheduledTasks) {
             if (scheduledTask.getRepeat() != null) {
                 getServer().getScheduler().scheduleSyncRepeatingTask(this, scheduledTask.getTask(), scheduledTask.getStart(), scheduledTask.getRepeat());
             } else {
                 getServer().getScheduler().scheduleSyncDelayedTask(this, scheduledTask.getTask(), scheduledTask.getStart());
             }
         }
     }
 
     private void registerCustomEventListeners() {
         List<Listener> customEvents = new ArrayList<Listener>();
         registerCustomEventListeners(customEvents);
 
         for (Listener customEvent : customEvents) {
             getServer().getPluginManager().registerEvents(customEvent, this);
         }
     }
 
     private void setupDatabase() {
         try {
             List<Class<?>> databaseClasses = getDatabaseClasses();
 
             for (Class<?> databaseClass : databaseClasses) {
                 getDatabase().find(databaseClass).findRowCount();
             }
         } catch (PersistenceException ex) {
             getLogger().info("Installing database due to first time usage");
             installDDL();
         }
     }
 
     @Override
     public final List<Class<?>> getDatabaseClasses() {
         List<Class<?>> databaseModel = super.getDatabaseClasses();
 
         registerDatabaseModel(databaseModel);
 
         return databaseModel;
     }
 
     @Override
     public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         try {
             preCommandAction(sender, command, label, args);
         } catch (CommandIgnoreException e) {
             return true;
         }
 
         try {
             commandsManager.execute(sender, command, label, args);
         } catch (CommandException e) {
             getLogger().log(Level.SEVERE, "error on command", e);
             return false;
         }
 
         return true;
     }
 
     @Override
     public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
         return commandsManager.getTabCompleteList(sender, command, alias, args);
     }
 
     public final void onError(Throwable thrown) {
         getLogger().log(Level.SEVERE, "error on plugin", thrown);
     }
 
     private void registerCommandSets() throws CommandWarmUpException {
         List<CommandSet> commandSets = new ArrayList<CommandSet>();
 
         registerCommands(commandSets);
 
         for (CommandSet commandSet : commandSets) {
             commandsManager.add(commandSet.getClazz(), commandSet.getParameters());
         }
     }
 
     protected ErrorHandler createCustomErrorHandler() {
         return null;
     }
 
     protected void preEnableActions() {
     }
 
     protected void postEnableActions() {
     }
 
     protected void initialCustomServices() {
     }
 
     protected void startCustomServices() {
     }
 
     protected void stopCustomServices() {
     }
 
     protected void preCommandAction(CommandSender sender, Command command, String label, String[] args) throws CommandIgnoreException {
     }
 
     protected void registerCommands(List<CommandSet> list) {
     }
 
     protected void initialCustomEventListeners() {
     }
 
     protected void registerCustomEventListeners(List<Listener> list) {
     }
 
     protected void registerDatabaseModel(List<Class<?>> list) {
     }
 
     protected void migrateOldData() {
     }
 
     protected EbeanServer getCustomDatabaseServer() {
         return null;
     }
 
     protected void registerScheduledTasks(List<ScheduleTask> list) {
     }
 }
