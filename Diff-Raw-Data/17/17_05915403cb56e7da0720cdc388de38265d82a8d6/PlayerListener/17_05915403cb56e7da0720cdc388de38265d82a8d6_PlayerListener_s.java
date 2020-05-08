 /*
     EduMania: Bukkit plugin for educational users.
     Copyright (C) 2012  korikisulda
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.plugmania.EduMania;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.block.*;
 
 public class PlayerListener implements Listener {
 	EduMania plugin;
 
 	public PlayerListener(EduMania instance) {
 		plugin = instance;
 	}
 
 	@EventHandler()
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (!(event.getEntity() instanceof Player))
 			return;
 		if (!plugin.util.isPlayerAuthed((Player) event.getEntity()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onEntityTarget(EntityTargetEvent event) {
 		if (!(event.getTarget() instanceof Player))
 			return;
		if (!plugin.util.isPlayerAuthed((Player) event.getEntity()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerHungerChange(FoodLevelChangeEvent event) {
 		if (!(event.getEntity() instanceof Player))
 			return;
 		if (!plugin.util.isPlayerAuthed((Player) event.getEntity()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerChat(PlayerChatEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerCommandPreprocessEvent(
 			PlayerCommandPreprocessEvent event) {
 		if (plugin.util.sit(event.getMessage(), ' ', 0).equalsIgnoreCase(
 				"/auth")) {
 
 			if (!plugin.util.authPlayer(event.getPlayer(),
 					plugin.util.sit(event.getMessage(), ' ', 1)))
 				event.getPlayer().sendMessage(
 						"[EduMania][ANOK]Authentication failure.");
 			else
 				event.getPlayer().sendMessage(
 						"[EduMania][AOK]Authentication success.");
 
 			return;
 		}
 
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerBucketEmpty(
 			org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerMoveEvent(PlayerMoveEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			if (event.getFrom().getBlockX() != event.getTo().getBlockX()
 					|| event.getFrom().getBlockY() != event.getTo().getBlockY()
 					|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()) 
 			{
 				event.setTo(event.getFrom());
 			}
 	}
 
 	@EventHandler()
 	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
 		if (!plugin.util.isPlayerAuthed(event.getPlayer()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler()
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		event.getPlayer().sendMessage("[EduMania][AUTH]");
 		if(plugin.getConfig().getBoolean("TexturePack.enable")){
			event.getPlayer().sendMessage("[EduMania][TXDL]" + plugin.getConfig().getString("TexturePack.URL") + plugin.getConfig().getString("TexturePack.fileName"));
 		}
 	}
 
 	@EventHandler()
 	public void onPlayerKickEvent(PlayerKickEvent event) {
 		plugin.util.deAuthPlayer(event.getPlayer());
 	}
 
 	@EventHandler()
 	public void onPlayerQuitEvent(PlayerQuitEvent event) {
 		plugin.util.deAuthPlayer(event.getPlayer());
 	}
 
 }
