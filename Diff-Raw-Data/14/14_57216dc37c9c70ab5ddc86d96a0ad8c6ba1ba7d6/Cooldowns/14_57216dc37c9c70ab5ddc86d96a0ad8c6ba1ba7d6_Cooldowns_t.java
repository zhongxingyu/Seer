 package com.lol768.LiteKits.extensions.cooldowns;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.lol768.LiteKits.LiteKits;
 import com.lol768.LiteKits.API.KitCheckEvent;
 
 public class Cooldowns extends JavaPlugin implements Listener {
     private LiteKits lk;
     
     public void onEnable() {
         Object obj = Bukkit.getServer().getPluginManager().getPlugin("LiteKits");
         if (obj != null) {
             lk = (LiteKits) obj;
             if (lk.getDescription().getVersion().equals("1.0") || lk.getDescription().getVersion().equals("1.1") || lk.getDescription().getVersion().equals("1.2")) {
                 getLogger().severe("LiteKits version is too old to use this extension. Disabling self...");
                 setEnabled(false);
             } else {
                 Bukkit.getPluginManager().registerEvents(this, this);
             }
         } else {
             getLogger().severe("Couldn't find LiteKits. Disabling self...");
             setEnabled(false);
         }
         
         if (getConfig().getLong("cooldown", -1) < 0) {
             getConfig().set("cooldown", 60);
             saveConfig();
         }
         
         if (!getConfig().contains("once-per-life")) {
             getConfig().set("once-per-life", false);
             saveConfig();
         }
         
        if (!getConfig().contains("clear-cooldown-on-death")) {
            getConfig().set("clear-cooldown-on-death", true);
            saveConfig();
        }
        
         
     }
     
     @EventHandler
     public void onKitAttempt(KitCheckEvent e) {
         if (getConfig().getBoolean("once-per-life", false) && getMetadata(e.getPlayer(), "gotKitThisLife") != null && ((Boolean)getMetadata(e.getPlayer(), "gotKitThisLife"))) {
             e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You can only receieve one kit per life");
             e.setCancelled(true);
             return;
         }
         Long amount = (Long) getMetadata(e.getPlayer(), "lastKitTime");
         long now = System.currentTimeMillis() / 1000l;
         if (amount != null) {
             
             if ((now - amount) < getConfig().getLong("cooldown", 0)) {
                e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You must wait " + (getConfig().getLong("cooldown", 0) - (now - amount)) + " more second(s) before selecting a kit");
                 e.setCancelled(true);
                 return;
             }
         }
         setMetadata(e.getPlayer(), "lastKitTime", now);
         setMetadata(e.getPlayer(), "gotKitThisLife", true);
     }
     
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent e) {
         setMetadata(e.getEntity(), "gotKitThisLife", false);
        if ((boolean) getConfig().get("clear-cooldown-on-death", true)) {
            setMetadata(e.getEntity(), "lastKitTime", 0);
        }
     }
     
     public void setMetadata(Player player, String key, Object value) {
         player.setMetadata(key,new FixedMetadataValue(this,value));
       }
       public Object getMetadata(Player player, String key) {
         List<MetadataValue> values = player.getMetadata(key);  
         for(MetadataValue value : values){
            if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){
               return value.value();
            }
         }
         return null;
       }
     
    
 
 }
