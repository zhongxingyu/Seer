 package com.wra.bukkit.ItemSound;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.gui.InGameHUD;
 import org.getspout.spoutapi.gui.Screen;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: raptors
  * Date: 8/6/11
  * Time: 5:26 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ItemSoundCommand implements CommandExecutor {
     private ItemSound plugin;
 
     public ItemSoundCommand(ItemSound plugin) {
         super();
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
         if(!sender.hasPermission("itemsound.config")) {
            sender.sendMessage("No permission to modify item sounds");
            return true;
         }
         if(split.length == 1) {
            String[] ts = new String[2];
            ts[0] = split[0];
            ts[1] = "";
            split = ts;
         }
         if(split.length != 2) {
             sender.sendMessage("Incorrect number of parameters");
             return false;
         }
         plugin.config.setProperty("effect."+split[0].toUpperCase(), split[1]);
         sender.sendMessage("Set item pickup "+split[0].toUpperCase()+" to "+ split[1]);
 
         return true;
     }
 
 }
