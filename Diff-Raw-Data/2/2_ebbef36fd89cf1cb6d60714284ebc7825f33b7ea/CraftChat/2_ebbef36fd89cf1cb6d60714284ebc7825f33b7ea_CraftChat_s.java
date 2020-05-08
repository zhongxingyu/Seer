 package com.craftminecraft.craftchat;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.event.HandlerList;
 
 import net.milkbowl.vault.chat.Chat;
 
 import com.craftminecraft.craftchat.listeners.ChatListener;
 
 public class CraftChat extends JavaPlugin {
     public static Chat chat = null;
     public static CraftChat instance;
     @Override
     public void onEnable() {
         instance = this;
         getLogger().info("Enabling CraftChat version " + getDescription().getVersion());
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
           getServer().getPluginManager().unloadPlugin(this); 
         }
         setupChat();
         getServer().getPluginManager().registerEvents(new ChatListener(this), this);
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Disabling CraftChat");
         HandlerList.unregisterAll(this);
     }
     private Boolean setupChat() {
         RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
         if (chatProvider != null) chat = chatProvider.getProvider();
         return (chat != null);
     }
 }
