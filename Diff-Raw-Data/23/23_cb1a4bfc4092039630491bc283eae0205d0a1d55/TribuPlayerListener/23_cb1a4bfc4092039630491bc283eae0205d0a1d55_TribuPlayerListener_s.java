 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu.Listeners;
 
 import graindcafe.tribu.Tribu;
 import graindcafe.tribu.Signs.TribuSign;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import org.bukkit.event.block.Action;
 
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import org.bukkit.plugin.PluginManager;
 
 public class TribuPlayerListener implements Listener {
 	private final Tribu plugin;
 
 	public TribuPlayerListener(Tribu instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
 		// If he is playing, then he is inside the world...
 		if (plugin.isPlaying(event.getPlayer())) {
 			plugin.removePlayer(event.getPlayer());
 		} else if (plugin.config().PluginModeWorldExclusive && plugin.isInsideLevel(event.getPlayer().getLocation())) {
 			// Timed out add player
 			// you need this orelse you will get kicked
 			// "Moving to fast Hacking?"
 			// its just a .5 of a second delay (it can be set to even less)
 			plugin.addPlayer(event.getPlayer(), 0.5);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (!event.isCancelled()) {
 			Block block = event.getClickedBlock();
 			if (block != null && plugin.isPlaying(event.getPlayer())) {
 				if (Sign.class.isInstance(block.getState()) && plugin.getLevel() != null) {
 					if (plugin.isRunning()) {
 						if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
 							plugin.getLevel().onSignClicked(event);
 					} else if (event.getPlayer().hasPermission("tribu.signs.place")) {
 						if (plugin.getLevel().removeSign(block.getLocation()))
 							Tribu.messagePlayer(event.getPlayer(), plugin.getLocale("Message.TribuSignRemoved"));
 						else if (plugin.getLevel().addSign(TribuSign.getObject(plugin, block.getLocation())))
 							Tribu.messagePlayer(event.getPlayer(), plugin.getLocale("Message.TribuSignAdded"));
 					}
 				} else if (plugin.isRunning()) {
 					plugin.getLevel().onClick(event);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		plugin.restoreInventory(event.getPlayer());
 		plugin.removePlayer(event.getPlayer());
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if (plugin.config().PluginModeServerExclusive || plugin.config().PluginModeWorldExclusive
				&& plugin.isInsideLevel(event.getPlayer().getLocation())) {
 			plugin.addPlayer(event.getPlayer());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		if (plugin.getLevel() != null) {
 			plugin.setDead(event.getPlayer());
 			plugin.resetedSpawnAdd(event.getPlayer(), event.getRespawnLocation());
 			event.setRespawnLocation(plugin.getLevel().getDeathSpawn());
 			plugin.restoreTempInv(event.getPlayer());
 			if (!plugin.isPlaying(event.getPlayer()))
 				plugin.restoreInventory(event.getPlayer());
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.isRunning() && plugin.isPlaying(player)) {
 			plugin.getChunkMemory().add(player.getLocation().getChunk());
 			if (plugin.config().LevelJail && !plugin.isAlive(player)
 					&& plugin.getLevel().getDeathSpawn().distance(player.getLocation()) > plugin.config().LevelJailRadius) {
 				player.teleport(plugin.getLevel().getDeathSpawn());
 				Tribu.messagePlayer(player, plugin.getLocale("Message.PlayerDSpawnLeaveWarning"));
 			}
 		}
 	}
 
 	public void registerEvents(PluginManager pm) {
 		pm.registerEvents(this, plugin);
 	}
 }
