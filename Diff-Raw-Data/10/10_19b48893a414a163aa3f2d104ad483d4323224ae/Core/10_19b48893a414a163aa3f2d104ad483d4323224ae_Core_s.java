 package de.jaschastarke.bukkit.lib;
 
 import java.util.Collection;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.jaschastarke.I18n;
 import de.jaschastarke.bukkit.lib.commands.BukkitCommandHandler;
 import de.jaschastarke.bukkit.lib.database.DBHelper;
 import de.jaschastarke.bukkit.lib.permissions.PermissionManager;
 import de.jaschastarke.database.DatabaseConfigurationException;
 import de.jaschastarke.database.db.Database;
 import de.jaschastarke.minecraft.lib.PluginCore;
 import de.jaschastarke.modularize.IHasModules;
 import de.jaschastarke.modularize.IModule;
 import de.jaschastarke.modularize.ModuleEntry;
 import de.jaschastarke.modularize.ModuleManager;
 import de.jaschastarke.utils.ClassDescriptorStorage;
 
 public class Core extends JavaPlugin implements PluginCore, IHasModules {
     protected boolean initialized = false;
     protected EventHandlerList listeners = new EventHandlerList(this);
     protected BukkitCommandHandler commands = new BukkitCommandHandler(this);
     protected ClassDescriptorStorage cds = null;
     protected PermissionManager permission;
     protected Database db = null;
     private I18n lang;
     
     private PluginLogger log;
     
     public boolean isDebug() {
         return false;
     }
     
     public void onInitialize() {
         log = new PluginLogger(this);
         if (permission == null)
             permission = PermissionManager.getDefaultPermissionManager(this);
         
         initialized = true;
     }
     
     @Override
     public void onEnable() {
         super.onEnable();
         if (!initialized) {
             this.onInitialize();
             initialized = true;
         }
         
         modules.activateAll();
         listeners.registerAllEvents();
     }
     @Override
     public void onDisable() {
         modules.disableAll();
         listeners.unregisterAllEvents();
     }
     
     public Database getDatabaseConnection() throws DatabaseConfigurationException {
         if (db == null) {
             db = DBHelper.connect(this);
             db.setLogger(log);
         }
         return db;
     }
     
     /*@Override
     public List<Class<?>> getDatabaseClasses() {
         if (db != null)
             return db.getDatabaseClasses();
         return super.getDatabaseClasses();
     }*/
 
     public PluginLogger getLog() {
         return log;
     }
     public BukkitCommandHandler getCommandHandler() {
         return commands;
     }
     public PermissionManager getPermManager() {
         return permission;
     }
     public I18n getLang() {
         return lang;
     }
     public void setLang(final I18n plang) {
         this.lang = plang;
     }
 
     /* IHasModules */
     protected ModuleManager modules = new ModuleManager();
     @Override
     public <T extends IModule> ModuleEntry<T> addModule(final T module) {
         try {
             ModuleEntry<T> modentry = modules.addModule(module);
             return modentry;
         } catch (Exception e) {
             e.printStackTrace();
             getLog().warn("Unexpected Exception while loading Module " + module.getClass().getCanonicalName() + ". The module wasn't loaded");
             return null;
         }
     }
     @Override
     public <T extends IModule> T getModule(final Class<T> module) {
         return modules.getModuleType(module);
     }
     public Collection<ModuleEntry<IModule>> getModules() {
         return modules.getModuleList();
     }
 
     public ClassDescriptorStorage getDocCommentStorage() {
         if (cds == null)
             cds = new ClassDescriptorStorage();
         return cds;
     }
 }
