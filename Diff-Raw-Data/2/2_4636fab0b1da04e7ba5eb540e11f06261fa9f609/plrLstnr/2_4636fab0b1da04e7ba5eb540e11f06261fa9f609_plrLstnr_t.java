 package me.cmastudios.plugins.WarhubModChat;
 
 import java.util.ArrayList;
 import java.util.List;
 import me.cmastudios.plugins.WarhubModChat.util.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.dynmap.DynmapCommonAPI;
 
 public class plrLstnr implements Listener {
 	public static WarhubModChat plugin;
     public plrLstnr(WarhubModChat instance) {
         plugin = instance;
     }
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerChat (final PlayerChatEvent event) {
     	DynmapCommonAPI dynmap = (DynmapCommonAPI)plugin.getServer().getPluginManager().getPlugin("dynmap");
 
     	Player player = event.getPlayer();
     	if (plugin.mutedplrs.containsKey(player.getName())) {
     		event.setCancelled(true);
     		player.sendMessage(ChatColor.RED + "You are muted.");
     		return;
     	}
     	if (plugin.spamcheck.containsKey(player)) {
     		if (event.getMessage().equalsIgnoreCase(plugin.spamcheck.get(player)) && !player.hasPermission("warhub.moderator")) {
     			player.sendMessage(ChatColor.RED + "You cannot send the same message twice in a row");
     			event.setCancelled(true);
     		}
     	}
     	plugin.spamcheck.put(player, event.getMessage());
     	event.setMessage(Capslock(player, event.getMessage()));
     	ArrayList<Player> plrs = new ArrayList<Player>();
     	for (Player plr : plugin.getServer().getOnlinePlayers()) {
     		if (plugin.ignores.containsKey(plr)) plrs.add(plr);
     	}
     	for (Player plr : plrs) {
     		event.getRecipients().remove(plr);
     	}
     	if (event.getMessage().contains("\u00A7") && !player.hasPermission("warhub.moderator")) {
     		event.setMessage(event.getMessage().replaceAll("\u00A7[0-9a-fA-FkK]", ""));
     	}
     	if (plugin.channels.containsKey(event.getPlayer())) {
 			dynmap.setDisableChatToWebProcessing(true);
     		if (plugin.channels.get(event.getPlayer()).equalsIgnoreCase("mod")) {
     			event.setCancelled(true);
     			sendToMods(plugin.messageUtil.colorizeText(Config.read("modchat-format")).replace("%player", event.getPlayer().getDisplayName()).replace("%message", event.getMessage()));
     			plugin.log.info("[MODCHAT] "+event.getPlayer().getDisplayName()+": "+event.getMessage());
     		}
     		if (plugin.channels.get(event.getPlayer()).equalsIgnoreCase("alert")) {
     			event.setCancelled(true);
     			Bukkit.getServer().broadcastMessage(plugin.messageUtil.colorizeText(Config.read("alert-format")).replace("%player", event.getPlayer().getDisplayName()).replace("%message", event.getMessage()));
    			dynmap.sendBroadcastToWeb("Attention", event.getMessage());
     		}
     	} else {
 			dynmap.setDisableChatToWebProcessing(false);
     	}
     	
     }
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerJoin (final PlayerJoinEvent event) {
     	plugin.channels.remove(event.getPlayer());
     	plugin.ignores.remove(event.getPlayer());
     }
     private String Capslock(Player player, String message)
     {
       int countChars = 0;
       int countCharsCaps = 0;
       boolean on = true;
 
         if (player.hasPermission("warhub.moderator") || (player.isOp()))
           on = false;
         else {
           on = true;
         }
 
       
 
       if (on) {
         countChars = message.length();
         if ((countChars > 0) && 
           (countChars > 8)) {
           for (int i = 0; i < countChars; i++) {
             char c = message.charAt(i);
             String ch = Character.toString(c);
             if (ch.matches("[A-Z]")) {
               countCharsCaps++;
             }
           }
           if (100 / countChars * countCharsCaps >= 40) {
         	  //Message has too many capital letters
               message = message.toLowerCase();
               plugin.warnings.put(player.getName(), getWarnings(player)+1);
               player.sendMessage(ChatColor.YELLOW + "Do not type in all caps ["+getWarnings(player)+" Violations]");
               sendToMods(ChatColor.DARK_RED+"[WHChat] "+ChatColor.WHITE+player.getDisplayName() + ChatColor.YELLOW + " all caps'd ["+getWarnings(player)+" Violations]");
           }
         }
 
       }
       return message;
     }
     private int getWarnings (Player key) {
     	if (plugin.warnings.get(key.getName()) != null) {
     		return plugin.warnings.get(key.getName());
     	}
     	else {
     		plugin.warnings.put(key.getName(), 0);
     	}
     	return 0;
     }
     private void sendToMods (String message) {
 		List<Player> sendto = new ArrayList<Player>();
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			if (p.hasPermission("warhub.moderator")) {
 				sendto.add(p);
 			}
 		}
 		for (Player p : sendto) {
 			p.sendMessage(message);
 		}
     }
 }
