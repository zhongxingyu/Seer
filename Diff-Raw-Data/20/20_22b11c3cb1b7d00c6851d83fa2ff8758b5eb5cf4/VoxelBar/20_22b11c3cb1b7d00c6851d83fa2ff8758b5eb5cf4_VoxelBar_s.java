 package com.thevoxelbox.voxelbar;
 
 import java.util.*;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class VoxelBar extends JavaPlugin {
     private static VoxelBar instance;
     @Override
     public void onDisable() {  
        VoxelBarToggleManager.savePlayers();
     }
     @Override
     public void onEnable() {
         
         getServer().getPluginManager().registerEvents(new VoxelBarListener(), this); // Register VoxelBarListener
         
         getCommand("vbar").setExecutor(new VoxelBarCommands()); // Register VoxelBarCommands
        VoxelBarToggleManager.setupConfig();
        VoxelBarToggleManager.loadPlayers();
         
     }
    public static VoxelBar getInstance() {
        return instance;
    }

     
 }
 
