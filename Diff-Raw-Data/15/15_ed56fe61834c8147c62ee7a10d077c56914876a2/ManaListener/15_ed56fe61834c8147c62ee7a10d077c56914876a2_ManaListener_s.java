 package com.herocraftonline.dev.heroes.spout;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 
 import com.herocraftonline.dev.heroes.Heroes;
 
 public class ManaListener implements Listener {
 
     public final Heroes plugin;
 
     public ManaListener(Heroes instance) {
         plugin = instance;
        if (Heroes.useSpout()) {
            Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        }
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onSpoutcraftEnable(SpoutCraftEnableEvent event) {
         event.getPlayer().getMainScreen().attachWidget(plugin, new ManaBar(plugin.getHeroManager().getHero(event.getPlayer()), plugin));
     }
 }
