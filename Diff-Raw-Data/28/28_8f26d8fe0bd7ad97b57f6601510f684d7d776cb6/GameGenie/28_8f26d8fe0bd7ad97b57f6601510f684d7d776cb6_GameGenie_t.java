 package com.minecarts.gamegenie;
 
 import com.minecarts.gamegenie.command.CommandGameGenie;
 import com.minecarts.gamegenie.listener.PlayerListener;
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.HashMap;
 
 public class GameGenie extends JavaPlugin{
     private HashMap<Player, ItemStack[]> playerInventory = new HashMap<Player, ItemStack[]>();
     public PluginDescriptionFile pdf;
     public void storeInventory(Player p, ItemStack[] is){
         playerInventory.put(p,is);
     }
 
     public ItemStack[] retrieveInventory(Player p){
         return playerInventory.remove(p);
     }
     
     public void onEnable(){
         pdf = this.getDescription();
 
         PluginManager pm = Bukkit.getPluginManager();
         //Register our one event and our one listener, wooo
         pm.registerEvent(Event.Type.PLAYER_DROP_ITEM,new PlayerListener(this), Event.Priority.Low,this);
         pm.registerEvent(Event.Type.PLAYER_QUIT,new PlayerListener(this), Event.Priority.Low,this);
         pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE,new PlayerListener(this), Event.Priority.Low,this);
         
         //Register command
         getCommand("gm").setExecutor(new CommandGameGenie(this));
         
         System.out.println(pdf.getName()+"> " + pdf.getVersion() + " Enabled");
     }
 
     public void onDisable(){
         //Attempt to restore any player inventories at this point (for reload support)
         for(Player p : playerInventory.keySet()){
             if(p.getGameMode() == GameMode.CREATIVE){
                if(!(p.hasPermission("gamegenie.bypasswipe"))){
                    p.getInventory().setContents(playerInventory.remove(p));
                }
                 System.out.println(pdf.getName()+"> " + pdf.getVersion() + " inventory restored onDisable() for " + p.getName());
             }
         }
         System.out.println(pdf.getName()+"> " + pdf.getVersion() + " Disabled");
     }
 }
