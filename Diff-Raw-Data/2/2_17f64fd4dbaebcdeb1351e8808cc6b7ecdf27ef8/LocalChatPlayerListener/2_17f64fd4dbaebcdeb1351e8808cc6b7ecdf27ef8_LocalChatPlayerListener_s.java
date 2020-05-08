 package fr.minekahest.localchat.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import fr.minekahest.localchat.LocalChat;
 
 public class LocalChatPlayerListener implements Listener {
 	
 	private LocalChat plugin;
 	
 	public LocalChatPlayerListener(LocalChat instance) {
 		plugin = instance;
 	}
 	
 	// Le joueur parle
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		
 		// On stop tout si l'event est annule
 		if (event.isCancelled())
 			return;
 		
 		// Le joueur qui parle
 		Player talkingPlayer = event.getPlayer();
 		// Le message
 		String msg = event.getMessage();
 		
 		// On casse le chat classique pour pouvoir gerer l'evenement
 		event.setCancelled(true);
 		
 		// Message global
 		if (event.getMessage().startsWith(plugin.globalSign) && plugin.globalSign != "false") {
 			// Suppresion du signe
 			msg = msg.substring(1).trim();
 			String finalColoredPrefix = preFormatMessage("global");
 			sendAllMessage(talkingPlayer, finalColoredPrefix, msg);
 		}
 		// Message world actuel
 		else if (event.getMessage().startsWith(plugin.worldSign) && plugin.worldSign != "false") {
 			msg = msg.substring(1).trim();
 			String finalColoredPrefix = preFormatMessage("world");
 			sendWorldMessage(talkingPlayer, finalColoredPrefix, msg);
 		}
 		
 		// Message definit pour une zone
 		else {
 			
 			String finalColoredPrefix;
 			int radius = 0;
 			
 			// Whisp
 			if (event.getMessage().startsWith(plugin.whispSign) && plugin.whispSign != "false") {
 				msg = msg.substring(1).trim();
 				finalColoredPrefix = preFormatMessage("whisp");
 				radius = plugin.whispRadius;
 			}
 			
 			// Shout
 			else if (event.getMessage().startsWith(plugin.shoutSign) && plugin.shoutSign != "false") {
 				msg = msg.substring(1).trim();
 				finalColoredPrefix = preFormatMessage("shout");
 				radius = plugin.shoutRadius;
 			}
 			
 			// Hors-roleplay
 			else if (event.getMessage().startsWith(plugin.hrpSign) && plugin.hrpSign != "false") {
 				msg = msg.substring(1).trim();
 				finalColoredPrefix = preFormatMessage("hrp");
 				radius =  plugin.hrpRadius;
 			}
 			
 			// Local si aucun sign
 			else {
 				finalColoredPrefix = preFormatMessage("local");
 				radius = plugin.localRadius;
 			}
 			
 			checkDistanceAndSendMessage(talkingPlayer, radius, finalColoredPrefix, msg);
 		}
 		
 	}
 	
 	// Remplacement du broadcast serveur par un maison
 	private void sendAllMessage(Player talkingPlayer, String finalColoredPrefix, String msg) {
 		for (Player p : plugin.getServer().getOnlinePlayers()) {
 			p.sendMessage(finalColoredPrefix + talkingPlayer.getName() + ": " + msg);
 		}
 	}
 	
 	// Broadcast par World (pull PunKeel modifie :p)
 	protected void sendWorldMessage(Player talkingPlayer, String finalColoredPrefix, String msg) {
 		// Boucle de verification de joueurs sur le meme monde
 		for (Player p : plugin.getServer().getOnlinePlayers()) {
			if (p.getWorld() == talkingPlayer.getWorld()) {
 				p.sendMessage(finalColoredPrefix + talkingPlayer.getName() + ": " + msg);
 			}
 		}
 	}
 	
 	// Un peu de formattage
 	public String preFormatMessage(String chatType) {
 		String prefix = plugin.getConfig().getString(chatType + "-prefix");
 		String color = plugin.getConfig().getString(chatType + "-color");
 		String coloredPrefix;
 		// Si pas de prefix on va faire une petite exeption
 		if (prefix != "false") {
 			coloredPrefix = ChatColor.translateAlternateColorCodes('&', color) + prefix + " ";
 		} else {
 			coloredPrefix = ChatColor.translateAlternateColorCodes('&', color);
 		}
 		return coloredPrefix;
 	}
 	
 	// Calcul des distances et envois du message
 	public void checkDistanceAndSendMessage(Player player, Integer radius, String prefix, String message) {
 		
 		for (Player listeningPlayer : plugin.getServer().getOnlinePlayers()) {
 			// Positions des 2 joueurs test�s
 			Location pLoc = listeningPlayer.getLocation();
 			Location sLoc = player.getLocation();
 			// Si distance = ok ou qu'un op est en ecoute, envois, sinon pas de messages 
 			if (sLoc.distance(pLoc) <= radius || plugin.spies.contains(listeningPlayer.getName())){
 				listeningPlayer.sendMessage(prefix + player.getDisplayName() + ": " + message);				
 			}
 
 		}
 		// On log tout de meme sur le serveur
 		plugin.getLogger().info((prefix + player.getName() + ": " + message).substring(2));
 	}
 }
