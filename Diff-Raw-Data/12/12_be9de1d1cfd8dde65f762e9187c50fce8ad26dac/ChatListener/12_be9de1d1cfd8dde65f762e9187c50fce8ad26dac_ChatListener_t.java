 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 
 public class ChatListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onChatCommand(AsyncPlayerChatEvent event)
 	{
 		// Define variables
 		Player player = event.getPlayer();
 		Set<Player> viewing = event.getRecipients();
 		String message = event.getMessage();
 
 		if(message.equals("pl")) pl(player, event);
 
		// TODO: Remove this when we determine if it's needed or not.
 		// No chat toggle
 		if(DataUtility.hasKeyTemp(player.getName(), "no_chat")) event.setCancelled(true);
 		for(Player victim : Bukkit.getOnlinePlayers())
 		{
 			if(DataUtility.hasKeyTemp(victim.getName(), "no_chat")) viewing.remove(victim);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onChatMessage(AsyncPlayerChatEvent event)
 	{
 		// Define variables
 		Player player = event.getPlayer();
 		String message = event.getMessage();
 
 		// Return if the player is praying
 		if(PlayerWrapper.isPraying(player)) return;
 
 		// Handle chat for character switching
 		if(DataUtility.hasKeyTemp(player.getName(), "chat_number"))
 		{
 			// Define variables
 			PlayerCharacter prevChar = PlayerWrapper.getPlayer(player).getPrevious();
 
 			if(prevChar == null) return;
 
 			DataUtility.saveTemp(player.getName(), "hat_number", Integer.parseInt(DataUtility.getValueTemp(player.getName(), "chat_number").toString()) + 1);
 			if(DataUtility.hasKeyTemp(player.getName(), "chat_number") && Integer.parseInt(DataUtility.getValueTemp(player.getName(), "chat_number").toString()) <= 2) event.setMessage(ChatColor.GRAY + "(Previously " + prevChar.getDeity().getInfo().getColor() + prevChar.getName() + ChatColor.GRAY + ") " + ChatColor.WHITE + message);
 			else DataUtility.removeTemp(player.getName(), "chat_number");
 		}
 	}
 
 	private void pl(Player player, AsyncPlayerChatEvent event)
 	{
 		HashMap<String, ArrayList<String>> alliances = new HashMap<String, ArrayList<String>>();
 
 		for(Player onlinePlayer : Bukkit.getOnlinePlayers())
 		{
 			String alliance = PlayerWrapper.getCurrentAlliance(player);
 
 			if(!alliances.containsKey(alliance.toUpperCase())) alliances.put(alliance.toUpperCase(), new ArrayList<String>());
 			alliances.get(alliance.toUpperCase()).add(onlinePlayer.getName());
 		}
 
 		for(String alliance : alliances.keySet())
 		{
 			String names = "";
 			for(String name : alliances.get(alliance))
 			{
 				names += " " + name;
 			}
 			player.sendMessage(ChatColor.YELLOW + alliance + ": " + ChatColor.WHITE + names);
 		}
 
 		event.getRecipients().clear();
 		event.setCancelled(true);
 	}
 }
