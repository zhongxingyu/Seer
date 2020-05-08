 package com.craftminecraft.craftchat;
 
 import java.util.logging.Logger;

 import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;

import com.craftminecraft.craftchat.listeners.ChatListener;
 
 public class CraftChat extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Enabling CraftChat");
        getServer().getPluginManager().registerEvents(new ChatListener(getConfig()), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling CraftChat");
        HandlerList.unregisterAll(this);
    }
 }
