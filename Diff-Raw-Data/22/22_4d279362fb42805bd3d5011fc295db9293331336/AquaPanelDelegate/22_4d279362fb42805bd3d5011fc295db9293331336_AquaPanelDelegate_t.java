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
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.aqualock.bukkit.input;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.display.AquaPanel;
 
 import org.getspout.spoutapi.event.input.KeyBindingEvent;
import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.keyboard.BindingExecutionDelegate;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class AquaPanelDelegate implements BindingExecutionDelegate {
 	private final AqualockPlugin plugin;
 	private final HashMap<UUID, AquaPanel> panels = new HashMap<UUID, AquaPanel>();
 
 	public AquaPanelDelegate(AqualockPlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void keyPressed(KeyBindingEvent keyBindingEvent) {
 		final SpoutPlayer player = keyBindingEvent.getPlayer();
		//Do not let them close another screen with this keybinding from this event
		if (!player.getMainScreen().getScreenType().equals(ScreenType.GAME_SCREEN)) {
			return;
		}
		//Check for GUI cache, create new cache if necessary, attach new panel
 		if (!panels.containsKey(player.getUniqueId())) {
 			final AquaPanel panel = new AquaPanel(plugin, player);
 			panels.put(player.getUniqueId(), panel);
 			player.getMainScreen().attachPopupScreen(panel);
 			return;
 		}
		//Has a cached panel, so attach it
 		player.getMainScreen().attachPopupScreen(panels.get(player.getUniqueId()));
 	}
 
 	@Override
 	public void keyReleased(KeyBindingEvent keyBindingEvent) {
		//Does nothing
 	}
 }
