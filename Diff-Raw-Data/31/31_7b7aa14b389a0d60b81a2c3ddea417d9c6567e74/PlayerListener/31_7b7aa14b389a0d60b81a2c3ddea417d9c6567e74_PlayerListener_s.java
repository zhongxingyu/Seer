 package com.minecarts.familyjewels.listener;
 
 import com.minecarts.familyjewels.FamilyJewels;
 import org.bukkit.Bukkit;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
public class PlayerListener extends org.bukkit.event.player.PlayerListener {
     private FamilyJewels plugin;
     public PlayerListener(FamilyJewels plugin){
         this.plugin = plugin;
     }
 
    @Override
     public void onPlayerJoin(final PlayerJoinEvent e){
         //Doing this 1 tick later can result in some chunks not being fully hidden (usually just the one they spawn in)
         //  We have to do it 1 tick later becuase the NSH is set in NetworkListenThread AFTER the player join event
         //  so we can't correctly remove it until the next tick. Possibly another event would be better, but didn't look
         //  into which one
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
             public void run() {
                 plugin.hookNSH(e.getPlayer());
             }
         },1);
     }
 
    @Override
     public void onPlayerQuit(final PlayerQuitEvent e){
         plugin.unhookNSH((e.getPlayer()));
     }
 }
