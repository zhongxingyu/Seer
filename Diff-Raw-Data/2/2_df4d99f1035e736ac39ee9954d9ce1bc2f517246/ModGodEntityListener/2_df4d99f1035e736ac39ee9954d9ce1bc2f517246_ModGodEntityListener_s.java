 package com.github.CubieX.ModGod;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import net.minecraft.server.Item;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class ModGodEntityListener implements Listener
 {
     Logger eLog;
     ArrayList<String> playersInSM = new ArrayList<String>();
     private ModGod plugin = null;
     private ModGodConfigHandler configHandler = null;
     private Logger log = null;
     
     public ModGodEntityListener(ModGod plugin, ModGodConfigHandler configHandler, Logger log)
     {        
         this.plugin = plugin;
         this.configHandler = configHandler;
         this.log = log;
         
         plugin.getServer().getPluginManager().registerEvents(this, plugin);    
        eLog.info("Bin im onPlayerLogin");
     }
 
   //----------------------------------------------------------------------------------------------------    
     @EventHandler // event has Normal priority
     public void onPlayerItemHeld(PlayerItemHeldEvent event)
     {            
         if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
         {
             boolean doContinue = false;
             if(configHandler.getConfig().getBoolean("debug")){log.info("bin im onPlayerItemEvent");}        
             if(event.getPlayer().hasPermission("modgod.service"))
             {
                 if(configHandler.getConfig().getBoolean("debug")){log.info("permission erkannt");}    
                 ItemStack newItem;            
                 newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
                 if (newItem != null) // is null if empty slot
                 {                    
                     if(configHandler.getConfig().getBoolean("debug")){log.info("ItemID: " + String.valueOf(newItem.getTypeId()));}
 
                     switch(newItem.getTypeId()) //check config. Change theses cases also in onInventoryClose Event!
                     {
                     case 8:
                         if(configHandler.getConfig().getBoolean("FLOWING_WATER")){
                             doContinue = true;
                         }
                         break;
                     case 9:
                         if(configHandler.getConfig().getBoolean("STILL_WATER")){
                             doContinue = true;
                         }
                         break;
                     case 10:
                         if(configHandler.getConfig().getBoolean("FLOWING_LAVA")){
                             doContinue = true;
                         }
                         break;
                     case 11:
                         if(configHandler.getConfig().getBoolean("STILL_LAVA")){
                             doContinue = true;
                         }
                         break;
                     case 51:
                         if(configHandler.getConfig().getBoolean("FIRE")){
                             doContinue = true;
                         }
                         break;
                     case 79:
                         if(configHandler.getConfig().getBoolean("ICE_BLOCK")){
                             doContinue = true;
                         }
                         break;                                        
                     }
                 }
                 if(doContinue)
                 {
                     if(configHandler.getConfig().getBoolean("debug")){log.info("ServiceItem " + String.valueOf(newItem) + " erkannt.");}
                     if(false == playersInSM.contains(event.getPlayer().getName()))
                     {
                         event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus EIN.");
                         playersInSM.add(event.getPlayer().getName());
                     }                            
                 }      
                 else // Player with permission has no service item in hand, so delete him from the List if hes on it.
                 {
                     if(configHandler.getConfig().getBoolean("debug")){log.info("Kein ServiceItem erkannt");}
                     if(playersInSM.contains(event.getPlayer().getName()))
                     {
                         event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus AUS.");
                         playersInSM.remove(event.getPlayer().getName());                    
                     }                    
                 }           
 
             }
         }
     }  
 
     //----------------------------------------------------------------------------------------------
     @EventHandler // event has Normal priority
     public void onInventoryClose(InventoryCloseEvent event)
     {
         if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
         {
             ItemStack heldItem;
             heldItem = event.getPlayer().getItemInHand();
             boolean doContinue = false;
             if (heldItem != null) // is null if empty slot
             {
                 if(configHandler.getConfig().getBoolean("debug")){log.info("ItemID: " + String.valueOf(heldItem.getTypeId()));}
 
                 switch( heldItem.getTypeId()) //check config
                 {
                 case 8:
                     if(configHandler.getConfig().getBoolean("FLOWING_WATER")){
                         doContinue = true;
                     }
                     break;
                 case 9:
                     if(configHandler.getConfig().getBoolean("STILL_WATER")){
                         doContinue = true;
                     }
                     break;
                 case 10:
                     if(configHandler.getConfig().getBoolean("FLOWING_LAVA")){
                         doContinue = true;
                     }
                     break;
                 case 11:
                     if(configHandler.getConfig().getBoolean("STILL_LAVA")){
                         doContinue = true;
                     }
                     break;
                 case 51:
                     if(configHandler.getConfig().getBoolean("FIRE")){
                         doContinue = true;
                     }
                     break;
                 case 79:
                     if(configHandler.getConfig().getBoolean("ICE_BLOCK")){
                         doContinue = true;                        
                     }
                     break;                                    
                 }
             } 
             if(doContinue)
             {
                 if(configHandler.getConfig().getBoolean("debug")){log.info("ServiceItem " + String.valueOf(heldItem) + " erkannt.");}
                 if(false == playersInSM.contains(event.getPlayer().getName()))
                 {                    
                     playersInSM.add(event.getPlayer().getName());
                 }                            
             }      
             else // Player with permission has no service item in hand, so delete him from the List if he's on it.
             {
                 if(configHandler.getConfig().getBoolean("debug")){log.info("Kein ServiceItem erkannt");}
                 if(playersInSM.contains(event.getPlayer().getName()))
                 {                    
                     playersInSM.remove(event.getPlayer().getName());                    
                 }                    
             }
         }
         else
         {
             if(configHandler.getConfig().getBoolean("debug")){log.info("leerer Slot");}
             if(playersInSM.contains(event.getPlayer().getName()))
             {                
                 playersInSM.remove(event.getPlayer().getName());                    
             }    
         }
     }
 
     //----------------------------------------------------------------------------------------------------
     @EventHandler
     public void onPlayerDamageEvent (EntityDamageEvent event)
     {
         if(event.getEntity() instanceof Player)
         {
             Player p = (Player) event.getEntity();
 
             if(playersInSM.contains(p.getName())) //check if player is in ServiceMode
             {
                 //if(event.getCause() == event.) // optional: look what caused the damage. (to not have a complete god mode)
                 event.setCancelled(true); // if yes, dont apply damage (e.g. from falling into lava or creeper explosion)
             }    
         }
     }
 }
