 package net.daboross.bukkitdev.uberchat;
 
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
 import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * UberChat Plugin Made By DaboRoss
  *
  * @author daboross
  */
 public final class UberChat extends JavaPlugin {
 
     private static UberChat currentInstance;
 
     /**
      *
      */
     @Override
     public void onEnable() {
         currentInstance = this;
         PluginManager pm = this.getServer().getPluginManager();
         pm.registerEvents(new UberChatListener(), this);
         getLogger().info("UberChat Fully Enabled");
     }
 
     /**
      *
      */
     @Override
     public void onDisable() {
         currentInstance = null;
         getLogger().info("UberChat Fully Disabled");
     }
 
     /**
      *
      * @return
      */
     protected static UberChat getCurrentInstance() {
         return currentInstance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("colorme")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("Sorry, Players Only");
                 return true;
             }
             Player p = (Player) sender;
             boolean on = false;
             if (p.hasMetadata("isMessageColorOn")) {
                 List<MetadataValue> meta = p.getMetadata("isMessageColorOn");
                 if (meta.size() >= 1 && (meta.get(0).asBoolean())) {
                     on = true;
                 }
             }
             if (on) {
                 p.removeMetadata("isMessageColorOn", this);
                sender.sendMessage(ChatColor.RED + "Your chat messages are no longer being colorized.");
             } else {
                 p.setMetadata("isMessageColorOn", new FixedMetadataValue(this, Boolean.TRUE));
                sender.sendMessage(ChatColor.RED + "Your future chat messages will now be colorized.");
             }
             return true;
         }
         return false;
     }
 }
