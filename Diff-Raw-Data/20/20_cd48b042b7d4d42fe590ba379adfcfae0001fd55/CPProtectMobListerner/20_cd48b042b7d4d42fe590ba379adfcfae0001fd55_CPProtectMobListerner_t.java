 /*************************************************************************
  * Copyright (C) 2012 Philippe Leipold
  *
  * This file is part of CreativePlus.
  *
  * CreativePlus is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CreativePlus is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CreativePlus. If not, see <http://www.gnu.org/licenses/>.
  *
  **************************************************************************/
 
 package de.Lathanael.CP.Listeners;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import be.Balor.Manager.Permissions.PermissionManager;
 
 import de.Lathanael.CP.CreativePlus.CreativePlus;
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class CPProtectMobListerner implements Listener {
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEntityDamagedByEntityEvent(EntityDamageByEntityEvent event) {
 		Entity target = event.getEntity();
 		Entity damager = event.getDamager();
 		if (!CreativePlus.worlds.contains(damager.getWorld().getName()))
 			return;
 		if (!(damager instanceof Player))
 			return;
 		Player player = (Player) damager;
 		if (player.getGameMode() != GameMode.CREATIVE)
 			return;
 		if (target instanceof LivingEntity && !(target instanceof Player)
				&& !PermissionManager.hasPerm(player, "creativeplus.damage.mob", false))
 			event.setCancelled(true);
 	}
 }
