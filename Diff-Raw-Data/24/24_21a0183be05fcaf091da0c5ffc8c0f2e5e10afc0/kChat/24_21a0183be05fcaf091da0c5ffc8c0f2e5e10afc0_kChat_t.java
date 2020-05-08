 package me.KeybordPiano459.kChat;
 
import org.bukkit.Bukkit;
 import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class kChat extends JavaPlugin {
     
    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("kEssentials");
    public String opcolor = plugin.getConfig().getString("op-name-color");
    public boolean colorallowed = plugin.getConfig().getBoolean("colors-allowed-chat");
    public int chatradius = plugin.getConfig().getInt("chat-radius");
    public boolean usechatspam = plugin.getConfig().getBoolean("use-spam-blocker");
     
     @Override
     public void onEnable() {
         if (kEssentials()) {
             getLogger().info("kChat v1.0 has been enabled!");
            registerEvents();
         } else {
            getLogger().warning("+----------------------------------------------+");
             getLogger().warning("|         Could not load kChat v1.0!!!         |");
             getLogger().warning("|      kChat is an add-on for kEssentials.     |");
             getLogger().warning("|      It can't load without it. Download      |");
             getLogger().warning("|     the newest version of kEssentials at     |");
             getLogger().warning(" http://dev.bukkit.org/server-mods/kEssentials/ ");
            getLogger().warning("+----------------------------------------------+");
             this.setEnabled(false);
         }
     }
     
     @Override
     public void onDisable() {
         getLogger().info("kChat v1.0 has been disabled.");
     }
     
     private void registerEvents() {
         registerEvent(new ChatColors(this));
         registerEvent(new ChatRadius(this));
         if (usechatspam) {
             registerEvent(new ChatSpam(this));
         }
     }
     
     private void registerEvent(Listener listener) {
         getServer().getPluginManager().registerEvents(listener, this);
     }
     
     public boolean kEssentials() {
         if (getServer().getPluginManager().getPlugin("kEssentials") != null) {
             return true;
         } else {
             return false;
         }
     }
 }
