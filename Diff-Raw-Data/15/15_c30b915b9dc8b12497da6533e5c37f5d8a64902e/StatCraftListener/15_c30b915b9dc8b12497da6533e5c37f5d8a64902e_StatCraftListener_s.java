 package me.tehbeard.BeardStat.listeners;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.CraftItemEvent;
  
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 public class StatCraftListener implements Listener {
 
     List<String> worlds;
     private PlayerStatManager playerStatManager;
 
 
     public StatCraftListener(List<String> worlds,
             PlayerStatManager playerStatManager) {
         this.worlds = worlds;
         this.playerStatManager = playerStatManager;
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onCraftItem(CraftItemEvent event){
         if(!worlds.contains(event.getWhoClicked().getWorld().getName())){
             String item = event.getRecipe().getResult().getType().toString().toLowerCase().replace("_","");
             int amount = event.getRecipe().getResult().getAmount();
             Player p = (Player)event.getWhoClicked();
             
             /**
              * if MetaDataable, make the item string correct
              */
             if(MetaDataCapture.hasMetaData(event.getRecipe().getResult().getType())){
                 item = 
                         event.getRecipe().getResult().getType().toString().toLowerCase().replace("_","") + 
                        "_" + event.getRecipe().getResult().getData();
                 
             }
             
             System.out.println(event.getRecipe().getResult().toString());
             playerStatManager.getPlayerBlob(p.getName()).getStat("crafting", item).incrementStat(amount);
         }
     }
     
     
 }
