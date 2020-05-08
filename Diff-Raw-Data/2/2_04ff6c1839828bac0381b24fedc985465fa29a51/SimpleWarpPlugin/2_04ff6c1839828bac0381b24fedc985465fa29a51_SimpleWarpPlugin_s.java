 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.rymate.SimpleWarp;
 
 import net.rymate.SimpleWarp.Commands.ListWarpsCommand;
 import net.rymate.SimpleWarp.Commands.WarpCommand;
 import net.rymate.SimpleWarp.Commands.DeleteWarpCommand;
 import net.rymate.SimpleWarp.Commands.SetWarpsCommand;
 import com.iConomy.*;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 
 
 /**
  *
  * @author Ryan
  */
 public class SimpleWarpPlugin extends JavaPlugin {
 
     public final String FILE_WARPS = "warps.txt";
     public File m_Folder;
     public File configFile; // = new File(m_Folder + File.separator + "config.properties");
     public HashMap<String, Location> m_warps = new HashMap();
     public int warpPrice;
     public boolean useEconomy;
     public boolean economyFound;
     static Properties prop = new Properties(); //creates a new properties file
    WarpFileHandler warp = new WarpFileHandler(this);
     // This is public so we can
     public iConomy iConomy = null;
 
 
     public void onEnable() {
         m_Folder = this.getDataFolder();
         configFile = new File(m_Folder + File.separator + "config.properties");
         File homelist = new File(this.m_Folder.getAbsolutePath() + File.separator + "warps.txt");
         if (!configFile.exists()) { //Checks to see if the config file exists, defined above, if it doesn't exist then it will do the following. The ! turns the whole statement around, checking that the file doesn't exist instead of if it exists.
             try { //try catch clause explained below in tutorial
                 if (!this.m_Folder.exists()) {
                     System.out.print("[SimpleWarp] Creating Config");
                     this.m_Folder.mkdir();
                 }
                 configFile.createNewFile(); //creates the file zones.dat
                 FileOutputStream out = new FileOutputStream(configFile); //creates a new output steam needed to write to the file
                 prop.put("use-economy", "true"); //puts someting in the properties file
                 prop.put("price-to-warp", "20"); //puts someting in the properties file
                 prop.store(out, "Please edit to your desires."); //You need this line! It stores what you just put into the file and adds a comment.
                 out.flush();  //flushes any unoutputed bytes to the file
                 out.close(); //Closes the output stream as it is not needed anymore.
                 log("Done!");
                 if (!homelist.exists()) {
                     log("Missing Warplist, creating the file....");
                     homelist.createNewFile();
                     log("Done!");
                 }
 
             } catch (IOException ex) {
                 ex.printStackTrace(); //explained below.
             }
         } else if (!homelist.exists()) {
             log("Missing Warplist, creating the file....");
             try {
                 homelist.createNewFile();
                 log("Done!");
             } catch (IOException ex) {
                 log("FAILED");
                 System.out.println(ex);
             }
         } else {
             PluginManager pm = getServer().getPluginManager();
             pm.registerEvent(Event.Type.PLUGIN_ENABLE, new WarpServerListener(this), Priority.Monitor, this);
             pm.registerEvent(Event.Type.PLUGIN_DISABLE, new WarpServerListener(this), Priority.Monitor, this);
 
             log("Loading warps...");
             if (warp.loadSettings()) {
                 log("Done!");
             } else {
                 log("FAILED");
             }
             getCommand("listwarps").setExecutor(new ListWarpsCommand(this));
             getCommand("setwarp").setExecutor(new SetWarpsCommand(this));
             getCommand("warp").setExecutor(new WarpCommand(this));
             getCommand("delwarp").setExecutor(new DeleteWarpCommand(this));
 
             PluginDescriptionFile pdfFile = getDescription();
             System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
         }
     }
 
     public void onDisable() {
         System.out.println("Meh. SimpleWarp disabled.");
     }
 
     public void log(String string) {
         System.out.println("[SimpleWarp] " + string);
     }
 
     public void loadProcedure() throws IOException {
         FileInputStream in = new FileInputStream(configFile);
         prop.load(in);
         useEconomy = Boolean.getBoolean(prop.getProperty("use-economy"));
         warpPrice = Integer.getInteger(prop.getProperty("price-to-warp"));
         in.close();
     }
 }
