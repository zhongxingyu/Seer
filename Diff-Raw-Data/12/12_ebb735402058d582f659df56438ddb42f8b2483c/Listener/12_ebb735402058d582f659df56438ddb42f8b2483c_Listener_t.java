 package me.wildn00b.prekick;
 
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 public class Listener implements org.bukkit.event.Listener {
 
 	private PreKick prekick;
 
 	public Listener(PreKick prekick) {
 		this.prekick = prekick;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void OnLogin(PlayerLoginEvent event) {
		if (!prekick.config.GetBoolean("PreKick.Enabled") || event.getResult() != PlayerLoginEvent.Result.ALLOWED)
 			return;
 
 		int reason = prekick.whitelist.IsPlayerOnWhitelist(event.getPlayer().getName(), event.getAddress().getHostAddress());
 
 		if (reason != 1) {
 			event.disallow(Result.KICK_WHITELIST, prekick.whitelist.processColors(prekick.whitelist.GetKickMessage(event.getPlayer().getName(), reason)));
 
 			String message = "[" + ChatColor.RED + "PreKick" + ChatColor.RESET + "] " + prekick.language.GetText("PreKick.Message.Base").replaceAll("%PLAYER%", event.getPlayer().getName()).replaceAll("%IP%", event.getAddress().getHostAddress()) + " ";
 			if (reason == 0)
 				message += prekick.language.GetText("PreKick.Message.Whitelist").replaceAll("%PLAYER%", event.getPlayer().getName()).replaceAll("%IP%", event.getAddress().getHostAddress());
 			else if (reason == 2)
 				message += prekick.language.GetText("PreKick.Message.IP").replaceAll("%PLAYER%", event.getPlayer().getName()).replaceAll("%IP%", event.getAddress().getHostAddress());
 			else if (reason == 3)
 				message += prekick.language.GetText("PreKick.Message.Blacklist").replaceAll("%PLAYER%", event.getPlayer().getName()).replaceAll("%IP%", event.getAddress().getHostAddress());
 			else {
 				PreKick.log.log(Level.WARNING, "[" + ChatColor.RED + "PreKick" + ChatColor.RESET + "] " + prekick.language.GetText("PreKick.Message.Missbehaving").replaceAll("%REASON%", reason + "").replaceAll("%PLAYER%", event.getPlayer().getName()).replaceAll("%IP%", event.getAddress().getHostAddress()));
 				message += prekick.language.GetText("PreKick.Message.Missbehaving").replaceAll("%PLAYER%", event.getPlayer().getName()).replaceAll("%IP%", event.getAddress().getHostAddress());
 			}
 			for (Player player : prekick.getServer().getOnlinePlayers()) {
 				if (prekick.permissions.HasPermissions(player, "prekick.seekick"))
 					player.sendMessage(message);
 			}
 
 			PreKick.log.log(Level.INFO, message);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void EasterEgg(PlayerLoginEvent event) {
 		if (prekick.config.GetBoolean("PreKick.Easter-egg") && event.getResult() == Result.ALLOWED && event.getPlayer().getName().equals("WildN00b")) {
 			prekick.getServer().broadcastMessage("[" + ChatColor.RED + "PreKick" + ChatColor.RESET + "] " + prekick.language.GetText("Listener.EasterEgg"));
 			PreKick.log.log(Level.INFO, "[" + ChatColor.RED + "PreKick" + ChatColor.RESET + "] " + prekick.language.GetText("Listener.Console.EasterEgg"));
 		}
 	}
 }
