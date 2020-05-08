 package net.LoadingChunks.SyncingFeeling;
 
 /*
     This file is part of SyncingFeeling
 
     SyncingFeeling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     SyncingFeeling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with SyncingFeeling. If not, see <http://www.gnu.org/licenses/>.
  */
 
 import net.LoadingChunks.SyncingFeeling.Inventory.SerializableInventory;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.*;
 
 public class SyncingFeelingEventListener implements Listener {
 
 	private SyncingFeeling plugin;
 
 	public SyncingFeelingEventListener(SyncingFeeling plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onQuit(PlayerQuitEvent event) {
 		if(event.getPlayer().hasPermission("sync.do"))
 			SerializableInventory.fromInventory(event.getPlayer(), event.getPlayer().getInventory()).commit();		
 	}
 	
 	@EventHandler
 	public void onKick(PlayerKickEvent event) {
 		if(event.getPlayer().hasPermission("sync.do"))
 			SerializableInventory.fromInventory(event.getPlayer(), event.getPlayer().getInventory()).commit();
 	}
 	
 	@EventHandler
 	public void onDeath(PlayerDeathEvent event) {
 		if(event.getEntity().hasPermission("sync.do"))
 			SerializableInventory.fromInventory(event.getEntity(), event.getEntity().getInventory()).commit();
 	}
 	
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event) {
 		if(event.getPlayer().hasPermission("sync.mirror"))
 			SerializableInventory.recover(event.getPlayer());
 	}
 }
