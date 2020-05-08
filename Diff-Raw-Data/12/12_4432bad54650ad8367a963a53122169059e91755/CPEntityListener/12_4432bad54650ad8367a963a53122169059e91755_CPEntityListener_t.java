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
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import be.Balor.Manager.Permissions.PermissionManager;
 import be.Balor.Tools.Utils;
 
 import de.Lathanael.CP.CreativePlus.CPConfigEnum;
import de.Lathanael.CP.CreativePlus.CreativePlus;
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class CPEntityListener implements Listener {
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDeath(EntityDeathEvent event) {
 		if (CPConfigEnum.DROP_ON_DEATH.getBoolean())
 			return;
 		if (!(event.getEntity() instanceof Player))
 			return;
 		Player p = (Player) event.getEntity();
		if (!CreativePlus.worlds.contains(p.getWorld().getName()))
			return;
 		if (PermissionManager.hasPerm(p, "creativeplus.deathdrop", false))
 			return;
 		if (p.getGameMode() == GameMode.CREATIVE) {
 			event.getDrops().clear();
 			Utils.sI18n(p, "NoDeathDrop");
 		}
 	}
 }
