 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuramc.aqualock.bukkit.input;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.display.AquaPanel;
 import com.almuramc.aqualock.bukkit.util.BlockUtil;
 import com.almuramc.aqualock.bukkit.util.LockUtil;
 import com.almuramc.bolt.lock.Lock;
 
 import org.getspout.spoutapi.event.input.KeyBindingEvent;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.keyboard.BindingExecutionDelegate;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 
 public class AquaPanelDelegate implements BindingExecutionDelegate {
 	private final AqualockPlugin plugin;
 	private static final HashMap<UUID, AquaPanel> panels = new HashMap<UUID, AquaPanel>();
 
 	public AquaPanelDelegate(AqualockPlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void keyPressed(KeyBindingEvent keyBindingEvent) {
 		final SpoutPlayer player = keyBindingEvent.getPlayer();
 		//Do not let them close another screen with this keybinding from this event
		if (!keyBindingEvent.getScreenType().equals(ScreenType.GAME_SCREEN)) {
 			return;
 		}
 		Block block = BlockUtil.getTarget(player, null, 4);
 		if (block == null) {
 			player.sendMessage(plugin.getPrefix() + "No valid block at the cursor could be found!");
 			return;
 		}
 		AquaPanel panel;
 		final Location loc = block.getLocation();
 		final Lock lock = plugin.getRegistry().getLock(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 		if (!LockUtil.canPerformAction(player, lock == null ? "LOCK" : "UPDATE")) {
 			return;
 		}
 		//Check for GUI cache, create new cache if necessary, attach new panel
 		if (!panels.containsKey(player.getUniqueId())) {
 			panel = new AquaPanel(plugin);
 			panels.put(player.getUniqueId(), panel);
 			player.getMainScreen().attachPopupScreen(panel);
 		} else {
 			//Has a cached panel, so attach it
 			panel = panels.get(player.getUniqueId());
 			player.getMainScreen().attachPopupScreen(panels.get(player.getUniqueId()));
 		}
 		panel.setLocation(block.getLocation());
 		panel.populate(lock);
 	}
 
 	@Override
 	public void keyReleased(KeyBindingEvent keyBindingEvent) {
 		//Does nothing
 	}
 }
