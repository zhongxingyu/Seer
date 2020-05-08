 package com.minecarts.miraclegrow.listener;
 
 import com.minecarts.miraclegrow.MiracleGrow;
 import org.bukkit.event.world.*;
 
 
 public class WorldListener extends org.bukkit.event.world.WorldListener {
     
     private MiracleGrow plugin;
     
     public WorldListener(MiracleGrow plugin) {
         this.plugin = plugin;
     }
     
     
     @Override
     public void onWorldUnload(WorldUnloadEvent event) {
     }
     @Override
     public void onWorldSave(WorldSaveEvent event) {
     }
     
 }
