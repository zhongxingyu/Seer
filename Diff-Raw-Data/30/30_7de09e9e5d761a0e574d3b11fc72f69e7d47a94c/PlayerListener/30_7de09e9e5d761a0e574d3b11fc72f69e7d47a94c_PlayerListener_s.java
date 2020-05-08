 package me.Bambusstock.Chaname;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class PlayerListener implements Listener{    
     private Chaname 	plugin;
     private Pattern 	pattern = Pattern.compile("@(.*)[\\s[:]](.*)");
     private Boolean 	copyTo;
     private String 	senderColor;
     private String 	messageColor;
     private Boolean 	forceColor;
     
     /**
      * Constructor assign configuration.
      * @param instance
      */
     public PlayerListener(Chaname instance) {
 	this.plugin = instance;
 	
 	copyTo 		= this.plugin.getConfig().getBoolean("copyTo");
 	senderColor 	= this.plugin.getConfig().getString("senderColor");
 	messageColor 	= this.plugin.getConfig().getString("messageColor");
 	forceColor 	= this.plugin.getConfig().getBoolean("forceColor");
     }
     
     /**
      * Sends a message. Uses colors and options.
      * @param sender 
      * @param receiver
      * @param msg
      * @return Boolean
      */
     public Boolean sendMessage(Player sender, Player receiver, String msg) {
 	String chatMessage;
 	String copyToMessage;
 	
 	if(receiver != null){
 	    if(this.forceColor == true) {
 		chatMessage = ChatColor.valueOf(this.senderColor) + "<" + sender.getName() + "> " + ChatColor.valueOf(this.messageColor) + msg.trim();
 		copyToMessage = ChatColor.valueOf(this.senderColor) + "<" + sender.getName() + "> -> <" + receiver.getName() + "> " + ChatColor.valueOf(this.messageColor) + msg.trim();
 	    }
 	    else {
 		chatMessage = ChatColor.valueOf(this.senderColor) + "<" + sender.getDisplayName() + "> " + ChatColor.valueOf(this.messageColor) + msg.trim();
 		copyToMessage = ChatColor.valueOf(this.senderColor) + "<" + sender.getDisplayName() + "> -> <" + receiver.getDisplayName() + "> " + ChatColor.valueOf(this.messageColor) + msg.trim();
 	    }
 
 	    receiver.sendMessage(chatMessage);
	    if(this.copyTo == true) sender.sendMessage(copyToMessage);
 	    return true;
 	}
 	return false;
     }
     
     /**
      * Check if a user mention another.
      * @param event
      */
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event) {
 	String msg = event.getMessage();
 	Matcher matcher = this.pattern.matcher(msg);
 	if(matcher.matches() == true && matcher.group(1).isEmpty() == false && matcher.group(2).isEmpty() == false) {
 	    Player sender = event.getPlayer();
 	    String plainMsg = matcher.group(2);
 	    event.setCancelled(true);
 	    
 	    if(!sender.hasPermission("chaname.send")) {
 		sender.sendMessage(ChatColor.RED + "You don't have the permission to send messages to a user.");
 		return;
 	    }
 	    
 	    if(sender.hasPermission("chaname.mentionMulti") == true && matcher.group(1).contains(",")) {
 		String[] receivers = matcher.group(1).split(",");
 		for(int i = 0; i < receivers.length; i++) {
 		    Player receiver = Bukkit.getPlayer(receivers[i].trim());
 		    if(!sendMessage(sender, receiver, plainMsg)) sender.sendMessage(ChatColor.RED + "The User '" + receivers[i].trim() + "' seems to be offline or doesn't exist. Check spelling. ;)");
 		}
 		return;
 	    }
 	    else if(sender.hasPermission("chaname.mentionMulti") == false && matcher.group(1).contains(",")) {
 		sender.sendMessage(ChatColor.RED + "It's not allowed to send a message to more than one user.");
 		return;
 	    }
 	    else {
 		Player receiver = Bukkit.getPlayer(matcher.group(1).trim());
 		if(!receiver.hasPermission("chaname.receive")) {
 		    sender.sendMessage(ChatColor.RED + matcher.group(1).trim() + " does not have the permission to receive messages.");
 		    return;
 		}
 		if(!sendMessage(sender, receiver, plainMsg)) sender.sendMessage(ChatColor.RED + "The User '" + matcher.group(1).trim() + "' seems to be offline or doesn't exist. Check spelling. ;)");
 		return;
 	    }
 	    
 	}	
     }
 }
