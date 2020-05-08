 package se.myllers.guestunlock;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class Listener implements org.bukkit.event.Listener {
 
 	/**
 	 * The latest version of the plugin. Set on plugin start.
 	 */
 	public static String	pluginVersion;
 	public static String	newestVersion;
 	public static boolean	enableChat	= false;
 
 	/**
 	 * Called when a player joins the game.
 	 * <p />
 	 * Checks if the player is a guest, and in that case sends him a
 	 * welcome-message
 	 * 
 	 * @param e
 	 *            - The PlayerJoinEvent
 	 */
 	@EventHandler
 	public void onPlayerJoin(final PlayerJoinEvent e) {
 		if (Permission.isGuest(e.getPlayer()) && !Permission.isModerator(e.getPlayer())) {
 			e.getPlayer().sendMessage(ChatColor.GREEN + formatJoinMessage(Main.config.getString("Guest.JoinMessage"), e.getPlayer().getName()));
 		}
 		if (Permission.isModerator(e.getPlayer()) && Main.config.getBoolean("Admin.CheckForUpdate")) {
 			if (!pluginVersion.equals(newestVersion)) {
				e.getPlayer().sendMessage(ChatColor.YELLOW + "New version available for GuestUnlock: " + UpdateCheck.newestVersion);
 				e.getPlayer().sendMessage(ChatColor.YELLOW + "Please check http://dev.bukkit.org/server-mods/GuestUnlock");
 			}
 		}
 	}
 
 	@EventHandler
 	public void onChat(final AsyncPlayerChatEvent e) {
 		if (enableChat) {
			if (e.getMessage().equalsIgnoreCase(Main.config.getString("Admin.Password"))) {
 				e.setCancelled(true);
 				e.getPlayer().sendMessage(ChatColor.RED + "You may not write the password in the chat!");
 				for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
 					if (Permission.isModerator(p)) {
 						p.sendMessage(ChatColor.YELLOW + e.getPlayer().getName() + "s message contained the password. " + " Full message was: " + ChatColor.RED + e.getMessage());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Formats a string
 	 * 
 	 * @param in
 	 *            - The String to be formatted
 	 * @param playerName
 	 *            - The players name
 	 * @return - The String in with the format applied on to it
 	 */
 	public static final String formatJoinMessage(final String in, final String playerName) {
 		return in.replace("{NAME}", playerName);
 	}
 }
