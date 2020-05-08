 /*
  * This file is part of mmoInfoCoords <http://github.com/mmoMinecraftDev/mmoInfoCoords>.
  *
  * mmoInfoCoords is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mmo.Info;
 
 import java.util.HashMap;
 
 import mmo.Core.InfoAPI.MMOInfoEvent;
 import mmo.Core.MMOPlugin;
 import mmo.Core.util.EnumBitSet;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class MMOInfoCoords extends MMOPlugin implements Listener {
 	private HashMap<Player, CustomLabel> widgets = new HashMap<Player, CustomLabel>();
 
 	@Override
 	public EnumBitSet mmoSupport(EnumBitSet support) {
 		support.set(Support.MMO_NO_CONFIG);
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		pm.registerEvents(this, this); }
 
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
		CustomLabel label = widgets.get(event.getPlayer());		
 	}
 
 	@EventHandler
 	public void onMMOInfo(MMOInfoEvent event) {
 		if (event.isToken("coords")) {
 			SpoutPlayer player = event.getPlayer();
 			if (player.hasPermission("mmo.info.coords")) {
				CustomLabel label = (CustomLabel) new CustomLabel().setResize(true).setFixed(true).setWidth(110);
 				widgets.put(player, label);
 				event.setWidget(plugin, label);
 				event.setIcon("map.png");
 			}
 		}
 	}
 
 	public class CustomLabel extends GenericLabel {
		private transient int tick = 0;
 
 
 		@Override
 		public void onTick() {
			if (tick++ % 20 == 0) {		
 				Location loc = getScreen().getPlayer().getLocation();
				setText(String.format("x:%d, y:%d, z:%d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ()));				
 			}
 		}
 	}
 }
