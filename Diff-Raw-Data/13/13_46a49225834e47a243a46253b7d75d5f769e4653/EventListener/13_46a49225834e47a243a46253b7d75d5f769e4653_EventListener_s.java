 /*
  * This file is part of SimpleCronClone.
  *
  * SimpleCronClone is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SimpleCronClone is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SimpleCronClone.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonsaimind.bukkitplugins.simplecronclone;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 
 public class EventListener implements Listener {
 
 	private Plugin sccMain;
 
 	public EventListener(Plugin plugin) {
 		sccMain = plugin;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 
 		// if it is a new player, do stuff before anything else
 		if (!event.getPlayer().hasPlayedBefore()) {
 			// eventFirstJoin
 			// TODO: this seems not to be reliable? any ideas on why? :/
 			sccMain.eventEngine.runEventsFor(EventEngine.EVENT_FIRST_JOIN, new String[]{EventEngine.EVENT_FIRST_JOIN,
 						event.getPlayer().getName()});
 		}
 
 		sccMain.eventEngine.runEventsFor(EventEngine.EVENT_JOIN, new String[]{EventEngine.EVENT_JOIN,
 					event.getPlayer().getName()});
 
 		// now that the player has "joined" lets see if the player is alone...
 		if (sccMain.getServer().getOnlinePlayers().length == 1) {
 			// only user logged in means that we were just empty.
 			sccMain.eventEngine.runEventsFor(EventEngine.EVENT_SERVER_NOT_EMPTY, new String[]{
 						EventEngine.EVENT_SERVER_NOT_EMPTY, event.getPlayer().getName()});
 		}
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		// eventQuit
 		sccMain.eventEngine.runEventsFor(EventEngine.EVENT_QUIT, new String[]{EventEngine.EVENT_QUIT,
 					event.getPlayer().getName()});
 
 		if (sccMain.getServer().getOnlinePlayers().length == 1) {
 			// this event fires before the server removes the player from the OnlinePlayers, so 1 not 0
 			sccMain.eventEngine.runEventsFor(EventEngine.EVENT_SERVER_EMPTY, new String[]{
 						EventEngine.EVENT_SERVER_EMPTY, event.getPlayer().getName()});
 		}
 	}
 
 	@EventHandler
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
 		// eventPlayerWorldMove
 		sccMain.eventEngine.runEventsFor(EventEngine.EVENT_PLAYER_WORLD_MOVE, new String[]{
 					EventEngine.EVENT_PLAYER_WORLD_MOVE, event.getPlayer().getName(), event.getFrom().getName(),
 					event.getPlayer().getWorld().getName()});
 
 		if (event.getFrom().getPlayers().isEmpty()) {
 			// eventWorldEmpty
 			sccMain.eventEngine.runEventsFor(EventEngine.EVENT_WORLD_EMPTY, new String[]{
 						EventEngine.EVENT_WORLD_EMPTY, event.getPlayer().getName(), event.getFrom().getName()});
 		}
 		if (event.getPlayer().getWorld().getPlayers().size() == 1) {
 			// eventWorldNotEmpty (if one player, that must means its ours that
 			// just moved)
 			sccMain.eventEngine.runEventsFor(EventEngine.EVENT_WORLD_NOT_EMPTY, new String[]{
 						EventEngine.EVENT_WORLD_NOT_EMPTY, event.getPlayer().getName(),
 						event.getPlayer().getWorld().getName()});
 		}
 
 	}
 }
