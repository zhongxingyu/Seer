 package ca.wacos.nametagedit;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import java.util.LinkedHashMap;
 
 /**
  * This class is responsible for handling various events in the server.
  * 
  * @author Levi Webb
  *
  */
 class NametagEventHandler implements Listener {
 	
 	/**
 	 * Called when a player joins the server. This event is set to <i>HIGHEST</i> priority to address a conflict
 	 * created with plugins that read player information in this event.<br><br>
 	 * 
 	 * This event updates nametag information, and the tab list (if enabled).
 	 * 
 	 * @param e  the {@link org.bukkit.event.player.PlayerJoinEvent} associated with this listener.
 	 */
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void onPlayerJoin(final PlayerJoinEvent e) {
 
         NametagManager.sendTeamsToPlayer(e.getPlayer());
 
 		NametagManager.clear(e.getPlayer().getName());
 
 		boolean setGroup = true;
 
 		LinkedHashMap<String, String> playerData = PlayerLoader.getPlayer(e.getPlayer().getName());
 		if (playerData != null) {
 			String prefix = playerData.get("prefix");
 			String suffix = playerData.get("suffix");
 			if (prefix != null)
 				prefix = NametagUtils.formatColors(prefix);
 			if (suffix != null)
 				suffix = NametagUtils.formatColors(suffix);
 			if (GroupLoader.DEBUG) {
 				System.out.println("Setting prefix/suffix for " + e.getPlayer().getName() + ": " + prefix + ", " + suffix + " (user)");
 			}
             NametagManager.overlap(e.getPlayer().getName(), prefix, suffix);
 			setGroup = false;
 		}
 
 		if (setGroup) {
 			for (String key : NametagEdit.groups.keySet().toArray(new String[NametagEdit.groups.keySet().size()])) {
 				if (e.getPlayer().hasPermission(key)) {
 					String prefix = NametagEdit.groups.get(key).get("prefix");
 					String suffix = NametagEdit.groups.get(key).get("suffix");
 					if (prefix != null)
 						prefix = NametagUtils.formatColors(prefix);
 					if (suffix != null)
 						suffix = NametagUtils.formatColors(suffix);
 					if (GroupLoader.DEBUG) {
 						System.out.println("Setting prefix/suffix for " + e.getPlayer().getName() + ": " + prefix + ", " + suffix + " (node)");
 					}
                     NametagCommand.setNametagHard(e.getPlayer().getName(), prefix, suffix, NametagChangeEvent.NametagChangeReason.GROUP_NODE);
 
 					break;
 				}
 			}
 		}
 		if (NametagEdit.tabListEnabled) {
 			String str = "§f" + e.getPlayer().getName();
 			String tab = "";
 			for (int t = 0; t < str.length() && t < 16; t++)
 				tab += str.charAt(t);
 			e.getPlayer().setPlayerListName(tab);
 		}
 
 		if (e.getPlayer().isOp() && NametagEdit.checkForUpdatesEnabled) {
 			Updater.checkForUpdates(e.getPlayer());
 		}
 
         // Why? Because I have swag.
         if (e.getPlayer().getName().equals("Jarcode"))
             NametagAPI.setNametagHard("Jarcode", "§eThe §b", " §cSwagmaster");
 
         // Leedle.
         if (e.getPlayer().getName().equals("SovietCoder"))
            NametagAPI.setNametagHard("Jarcode", "§eLeedle §f", "");
 	}
 	/**
 	 * Called when a player dies in the server. If enabled, this plugin will parse through the death message
 	 * and remove any formatting created by the player's nametag.
 	 *
 	 * @param e  the {@link org.bukkit.event.entity.PlayerDeathEvent} associated with this listener.
 	 */
 	@EventHandler
 	void onPlayerDeath(PlayerDeathEvent e) {
 		if (NametagEdit.deathMessageEnabled) {
 			String formattedName = NametagManager.getFormattedName(e.getEntity().getName());
 			if (!formattedName.equals(e.getEntity().getName()))
 				e.setDeathMessage(e.getDeathMessage().replace(formattedName, e.getEntity().getName()));
 		}
 	}
 }
