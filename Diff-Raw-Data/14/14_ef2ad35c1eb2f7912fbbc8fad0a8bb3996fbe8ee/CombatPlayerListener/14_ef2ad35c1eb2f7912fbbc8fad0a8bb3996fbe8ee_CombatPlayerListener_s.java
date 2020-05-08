 package net.milkbowl.combatevents.listeners;
 
 import net.milkbowl.combatevents.CombatEventsCore;
 import net.milkbowl.combatevents.CombatReason;
 import net.milkbowl.combatevents.Config;
 import net.milkbowl.combatevents.LeaveCombatReason;
 import net.milkbowl.combatevents.events.PlayerLeaveCombatEvent;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 
 public class CombatPlayerListener extends PlayerListener {
 	CombatEventsCore plugin;
 
 	public CombatPlayerListener(CombatEventsCore plugin) {
 		this.plugin = plugin;
 	}
 
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if (event.isCancelled() || Config.getDenyCommands().isEmpty())
 			return;
 
		String message = event.getMessage();
		for (String cmd : Config.getDenyCommands()) {
			if (message.startsWith("/" + cmd)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Config.getDenyCommandMessage());
				return;
 			}
 		}
 	}
 
 	public void onPlayerQuit (PlayerQuitEvent event) { 
 		//Cleanup the player if they are kicked.
 		if (plugin.isInCombat(event.getPlayer())) {
 			CombatReason[] cReasons = plugin.getCombatPlayer(event.getPlayer()).getReasons();
 			plugin.getCombatPlayer(event.getPlayer()).clearReasons();
 			if (throwPlayerLeaveCombatEvent(event.getPlayer(), LeaveCombatReason.QUIT, cReasons))
 				plugin.leaveCombat(event.getPlayer());
 		}
 	}
 
 	public void onPlayerKick (PlayerKickEvent event) {
 		if (plugin.isInCombat(event.getPlayer())) {
 			CombatReason[] cReasons = plugin.getCombatPlayer(event.getPlayer()).getReasons();
 			plugin.getCombatPlayer(event.getPlayer()).clearReasons();
 			if (throwPlayerLeaveCombatEvent(event.getPlayer(), LeaveCombatReason.KICK, cReasons))
 				plugin.leaveCombat(event.getPlayer());
 		}
 	}
 
 	private boolean throwPlayerLeaveCombatEvent(Player player, LeaveCombatReason reason, CombatReason[] cReasons) {
 		PlayerLeaveCombatEvent event = new PlayerLeaveCombatEvent(player, reason, cReasons);
 		plugin.getServer().getPluginManager().callEvent(event);
 		//Return if the event was successful
 		return !event.isCancelled();
 	}
 }
