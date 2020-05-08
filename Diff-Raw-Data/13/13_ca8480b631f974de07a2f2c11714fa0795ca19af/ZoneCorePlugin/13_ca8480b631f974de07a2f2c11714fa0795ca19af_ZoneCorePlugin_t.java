 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package info.jeppes.ZoneCore;
 
 import info.jeppes.ZoneCore.Commands.ZoneCommand;
 import info.jeppes.ZoneCore.Listeners.PlayerListener;
 import info.jeppes.ZoneCore.Users.ZoneUserManager;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 
 /**
  *
  * @author Jeppe
  */
 public class ZoneCorePlugin extends ZonePlugin{
 
     private ZoneUserManager userManager;
     
     @Override
     public String[] preLoadConfig() {
         return new String[]{"users.yml"};
     }
 
     @Override
     public void loadDefaultConfig(ZoneConfig config) {
     }
 
     @Override
     public void loadConfig(ZoneConfig config) {
         userManager = new ZoneUserManager(config);
     }
 
     @Override
     public Object initAPI() {
         getZoneCore().setCore(this);
         return getZoneCore();
     }
 
     @Override
     public ZoneCore getAPI(){
         return getZoneCore();
     }
     
     @Override
     public String[] initCommandAliases() {
         return new String[]{"zonecore","zc"};
     }
 
     @Override
     public String initCommandPackageDirectory() {
         return "info.jeppes.ZoneCore.Commands.Commands";
     }
     
     @Override
     public void onEnable(){
         super.onEnable();
         loadCommandsFromPluginDirectory();
         PlayerListener playerListener = new PlayerListener();
         this.getServer().getPluginManager().registerEvents(playerListener, this);
     }
     
     @Override
     public void onDisable() {
         ZoneUserManager.getUsersConfig().save();
         ZoneUserManager.getUsers().clear();
         super.onDisable();
     }
     
     public ZoneUserManager getUserManager() {
         return userManager;
     }
     
     public void reloadEveryPlugin(final CommandSender cs) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
             @Override
             public void run() {
                 for(ZonePlugin zonePlugin : ZoneCore.getZonePluginsList()){
                     zonePlugin.reloadPlugin(cs);
                 }
             }
         });
     }
     public void loadCommandsFromPluginDirectory(){
         //load commands from plugin specific command folder
         File commandsDirectory = new File(ZoneCore.getMainPluginDirectory()+File.separator+"commands");
         if(commandsDirectory != null){
             if(commandsDirectory.isDirectory()){
                 ArrayList<String> commandClassFiles = getFilesFromDirectoryAndSubDirectories(commandsDirectory,new ArrayList<String>(),"");
         
                 ClassLoader loader = null;
                 try {
                     URL url = commandsDirectory.toURI().toURL(); 
                     URL[] urls = new URL[]{url};
 
                     loader = new URLClassLoader(urls);
 
                 } catch (MalformedURLException ex) {
                     Logger.getLogger(ZonePlugin.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 for(String classPackageAndName : commandClassFiles){
                     try {
                         Class commandClass = loader.loadClass(classPackageAndName);
                         Object commandObject = commandClass.newInstance();
                         if(commandObject != null && commandObject instanceof ZoneCommand){
                             ZoneCommand zoneCommand = (ZoneCommand) commandObject;
                             getCommandManager().registreCommand(zoneCommand);
                         }
                     } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                         Logger.getLogger(ZonePlugin.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
         }
     }
     public ArrayList<String> getFilesFromDirectoryAndSubDirectories(File aFile, ArrayList<String> files, String startPath) {
         if(aFile.isFile()){
             String fileName = aFile.getName();
             fileName = fileName.substring(0, fileName.lastIndexOf("."));
             files.add(startPath + "." + fileName);
         } else if (aFile.isDirectory()) {
             File[] listOfFiles = aFile.listFiles();
             if(listOfFiles!=null) {
                for (File file : listOfFiles) {
                    files = getFilesFromDirectoryAndSubDirectories(file,files,startPath + "." + file.getName());
                }
             } else {
                 Logger.getLogger(ZonePlugin.class.getName()).log(Level.SEVERE, null, "FILE ACCESS DENIED");
             }
         }
         return files;
     }
 }
