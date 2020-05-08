 package com.untamedears.civchat;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*
  * Coded by Rourke750 & ibbignerd
  */
 public class CivChat extends JavaPlugin implements Listener {
 
     public PluginDescriptionFile pdf = this.getDescription();
     private ChatManager chat = null;
     private ChatListener cl = null;
     private FileConfiguration config = null;
     public File record = null;
    public File ignored=null;
     public BufferedWriter writer;
     public BufferedWriter writ;
 
     public void onEnable() {
         config = getConfig();
         initConfig();
         this.saveConfig();

         String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
         String dir = this.getDataFolder() + File.separator + "ChatLogs" + File.separator;
         String dirign= this.getDataFolder()+ File.separator+ "ignored"+ File.separator;        
         Boolean a = (new File(dir).mkdirs());
         record = new File(dir);
        ignored= new File(dirign+"ignorelist.txt");
         fileManagement(date, dir);
         Boolean b=(new File(dirign).mkdirs());
 
         try {
             File existing = new File(dir + date + ".txt");
             if (existing.exists()) {
                 FileWriter fw = new FileWriter(existing.getAbsoluteFile(), true);
                 writer = new BufferedWriter(fw);
                 Logger.getLogger(CivChat.class.getName()).log(Level.INFO, "Existing file", "");
             } else {
                 Logger.getLogger(CivChat.class.getName()).log(Level.INFO, "Making a new file", "");
                 PrintWriter fstream = new PrintWriter(dir + date + ".txt");
                 writer = new BufferedWriter(fstream);
             }
         } catch (IOException ex) {
             Logger.getLogger(CivChat.class.getName()).log(Level.WARNING, "File Failed" + ex, "");
         }
         try{
        	if (ignored.exists()) {
                FileWriter fw = new FileWriter(ignored.getAbsoluteFile(), true);
                 writ = new BufferedWriter(fw);
                 Logger.getLogger(CivChat.class.getName()).log(Level.INFO, "Existing file", "");
                 chat.load(ignored);
             } else {
                 Logger.getLogger(CivChat.class.getName()).log(Level.INFO, "Making a new file", "");
                PrintWriter fstream = new PrintWriter(ignored);
                 writ = new BufferedWriter(fstream);
             }
         } catch (IOException ex) {
             Logger.getLogger(CivChat.class.getName()).log(Level.WARNING, "File Failed" + ex, "");
         }
         
         try {
             writer.write("Chat log created at " + new Date());
             writer.newLine();
             writer.newLine();
             writer.flush();
         } catch (IOException ex) {
             Logger.getLogger(CivChat.class.getName()).log(Level.SEVERE, "Could not write to file", ex);
         }

        chat = new ChatManager(this);
         cl = new ChatListener(chat);
         registerEvents();
         Commands commands = new Commands(chat, this);
 
         for (String command : getDescription().getCommands().keySet()) {
             getCommand(command).setExecutor(commands);
         }
     }
 
     public void onDisable() {
         try {
             writer.write("Server closed at " + new Date());
             writer.newLine();
             writer.newLine();
             writer.flush();
             writer.close();
             chat.save(ignored);
         } catch (IOException ex) {
             Logger.getLogger(CivChat.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void fileManagement(String date, String dir) {
         File[] filtered = record.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.toLowerCase().endsWith(".txt");
             }
         });
         Logger.getLogger(CivChat.class.getName()).log(Level.INFO, (filtered.length) + "", "");
         if (filtered != null && filtered.length > config.getInt("chat.fileManagement.filesToZip", 15)) {
             try {
                 Logger.getLogger(CivChat.class.getName()).log(Level.INFO, "Zipping them up", "");
                 FileOutputStream fos = new FileOutputStream(dir + date + ".zip");
                 ZipOutputStream zos = new ZipOutputStream(fos);
                 for (File file : filtered) {
                     ZipEntry ze = new ZipEntry(file.toString());
                     zos.putNextEntry(ze);
                     zos.closeEntry();
                     file.delete();
                 }
                 zos.close();
             } catch (Exception e) {
             	Logger.getLogger(CivChat.class.getName()).log(Level.SEVERE, null, e);
             	return;
             }
         }
         zipFile();
     }
     public void zipFile(){
     	File[] zipList = record.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.toLowerCase().endsWith(".zip");
             }
         });
         Logger.getLogger(CivChat.class.getName()).log(Level.INFO, "zipList.length = " + zipList.length, "");
         if (zipList != null && zipList.length > config.getInt("chat.fileManagement.maxNumberOfZips", 4)) {
             Logger.getLogger(CivChat.class.getName()).log(Level.INFO, "Deleting zips", "");
             long holder = 0;
             long tester;
             File toDelete = zipList[0];
             for (File file : zipList) {
                 tester = file.lastModified();
                 if (tester < holder) {
                     holder = tester;
                     toDelete = file;
                 }
             }
             toDelete.delete();
             Logger.getLogger(CivChat.class.getName()).log(Level.INFO, toDelete.getName(), "");
         }
     }
 
     public void initConfig() {
         config.options().header("Authors: Rourke750, ibbignerd\n"
                 + " Last updated on 06/21/13\n"
                 + " This plugin was designed for use by Civcraft\n"
                 + " \n"
                 + " variation: The range from 0-value added to the garble to make it more random\n"
                 + " defaultcolor: Default color of chat. Must use a ChatColor.COLOR option\n"
                 + " greyscale: Chat changes for reciever based on distance. Configured below\n"
                 + " yvariation: The higher the sender is, the farther the max chat range.\n"
                 + "   noGarbLevel: At what y level (and above) will the yvariation come into effect?\n"
                 + " shout: Expand max range based on number of shout chars\n"
                 + "   char: character uesd at the beginning of the message\n"
                 + "   distanceAdded: Amount added to the max range to make chat go further\n"
                 + "   hungerReduced: Amount of hunger reduced per shout\n"
                 + "   cooldown: Amount of time in seconds between shouts.\n"
                 + " whisper: Reduced chat range when whisper char is used\n"
                 + "   char: Character used at the beginning of the message\n"
                 + "   distance: set distance a whisper is heard\n"
                 + "   color: configurable color for whisper messages\n"
                 + " range: currently has 3 configurable distances\n"
                 + "   distance: subtraced from maxrange (e.g. maxrange = 1000, \n"
                 + "     distance = 100, from 900-1000 is the range)\n"
                 + "   garble: Integer as a percent. (garble: 5 = 5% of string will be garbled)\n"
                 + "   color: Color of chat for this range\n"
                 + " maxrange: Max distance a player can be heard");
         if (!config.contains("chat.fileManagement.filesToZip")) {
             config.set("chat.fileManagement.filesToZip", 15);
         }
         if (!config.contains("chat.fileManagement.maxNumberOfZips")) {
             config.set("chat.fileManagement.maxNumberOfZips", 4);
         }
         if (!config.contains("chat.garblevariation")) {
             config.set("chat.variation", 5);
         }
         if (!config.contains("chat.defaultcolor")) {
             config.set("chat.defaultcolor", "WHITE");
         }
         if (!config.contains("chat.greyscale")) {
             config.set("chat.greyscale", true);
         }
         if (!config.contains("chat.yvariation.enabled")) {
             config.set("chat.yvariation.enabled", true);
         }
         if (!config.contains("chat.yvariation.noGarbLevel")) {
             config.set("chat.yvariation.noGarbLevel", 70);
         }
         if (!config.contains("chat.shout.enabled")) {
             config.set("chat.shout.enabled", true);
         }
         if (!config.contains("chat.shout.char")) {
             config.set("chat.shout.char", "!");
         }
         if (!config.contains("chat.shout.distanceAdded")) {
             config.set("chat.shout.distanceAdded", 300);
         }
         if (!config.contains("chat.shout.color")) {
             config.set("chat.shout.color", "WHITE");
         }
         if (!config.contains("chat.shout.hungerreduced")) {
             config.set("chat.shout.hungerreduced", 4);
         }
         if (!config.contains("chat.shout.cooldown")) {
             config.set("chat.shout.cooldown", 10);
         }
         if (!config.contains("chat.whisper.enabled")) {
             config.set("chat.whisper.enabled", true);
         }
         if (!config.contains("chat.whisper.char")) {
             config.set("chat.whisper.char", "#");
         }
         if (!config.contains("chat.whisper.distance")) {
             config.set("chat.whisper.distance", 50);
         }
         if (!config.contains("chat.whisper.color")) {
             config.set("chat.whisper.color", "ITALIC");
         }
         if (!config.contains("chat.range.garble")) {
             config.set("chat.range.garbleEnabled", false);
         }
         if (!config.contains("chat.range.1.distance")) {
             config.set("chat.range.1.distance", 100);
         }
         if (!config.contains("chat.range.1.garble")) {
             config.set("chat.range.1.garble", 0);
         }
         if (!config.contains("chat.range.1.color")) {
             config.set("chat.range.1.color", "GRAY");
         }
         if (!config.contains("chat.range.2.distance")) {
             config.set("chat.range.2.distance", 50);
         }
         if (!config.contains("chat.range.2.garble")) {
             config.set("chat.range.2.garble", 0);
         }
         if (!config.contains("chat.range.2.color")) {
             config.set("chat.range.2.color", "DARK_GRAY");
         }
         if (!config.contains("chat.range.maxrange")) {
             config.set("chat.range.maxrange", 1000);
         }
     }
 
     private void registerEvents() {
         getServer().getPluginManager().registerEvents(cl, this);
     }
 }
