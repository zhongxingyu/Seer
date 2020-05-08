 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.msm595.NoDrop;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.event.Event;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.File;
 /**
  *
  * @author Alex
  */
 public class NoDrop extends JavaPlugin{
     NDEntityListener entityListener = new NDEntityListener(this);
     NDPlayerListener playerListener = new NDPlayerListener(this);
     
     public static PluginDescriptionFile pdfFile;
     public static Configuration conf;
     
     private HashMap<Player, NDInventory> drops = new HashMap();
     private ArrayList<Player> died = new ArrayList<Player>();
     
     
     private ArrayList<String> drop;
     private ArrayList<String> nodrop;
     private ArrayList<String> keep;
     
     //@Override
     public void onLoad() {
         
     }
     
     @Override
     public void onEnable() {
         pdfFile = this.getDescription();
         conf = this.getConfiguration();
         PluginManager pm = getServer().getPluginManager();
         
         if(getDataFolder().mkdir()) {
             conf.load();
             conf.save();
             System.out.println("["+pdfFile.getName().toUpperCase() + "] The settings file been created. Please set it up before using the plugin. The plugin will be disabled until you do.");
             pm.disablePlugin(this);
             return;
         }
         
         if(conf.getProperty("useFor")==null) {
             System.err.println("["+pdfFile.getName().toUpperCase() + "] The settings file isn't properly set up. Please set it up before using the plugin. The plugin will be disabled until you do.");
             pm.disablePlugin(this);
             return;
         }
         
         pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
         
         drop = (ArrayList<String>)conf.getStringList("drop", null);
         nodrop = (ArrayList<String>)conf.getStringList("nodrop", null);
         keep = (ArrayList<String>)conf.getStringList("keep", null);
         
         System.out.println( "["+pdfFile.getName() + "] " + pdfFile.getVersion() + " is enabled!" );
     }
     
     @Override
     public void onDisable() {
         System.out.println("["+pdfFile.getName()+"] Disabled");
     }
     
     public void addDrop(Player player) {
        //if(!drops.containsKey(player))
             drops.put(player, new NDInventory(player, this));
     }
     
     public void getDrop(Player player) {
         if(canDrop(player) && drops.containsKey(player)) {
             drops.get(player).restore(player);
             drops.remove(player);
             died.remove(player);
         }
     }
     
     public void setCanDrop(Player player) {
         if(!died.contains(player)) {
             died.add(player);
         }
     }
     
     public boolean canDrop(Player player) {
         return died.contains(player);
     }
     
     
     public boolean noDrop(Player player) {
         //TODO
         if(player.isOp()) {
             return !"not ops".equals(conf.getString("useFor"));
         }
         
         return !"ops".equals(conf.getString("useFor"));
     }
     
     public boolean drops(String item) {
         if(drop!=null) {
             return drop.contains("all")||drop.contains(item);
         } else if(nodrop!=null) {
             return !nodrop.contains("all")&&!nodrop.contains(item);
         }
         
         System.err.println("Neither drop nor nodrop are listed, everything will be dropped.");
         return true; //they didn't have drop or nodrop
     }
     
     public boolean keeps(String item) {
         return (keep==null || keep.contains("all") || keep.contains(item))&&(!drops(item));
     }
     
     public ArrayList<ItemStack> armors(Player player) {
         return drops.get(player).getArmor();
     }
     
 //    public boolean storeDrop(Player player) {
 //        //TODO
 //        return player.isOp();
 //    }
 }
