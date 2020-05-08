 /*
  * This file is part of mmoInfoBed <http://github.com/mmoMinecraftDev/mmoInfoBed>.
  *
  * mmoInfoBed is free software: you can redistribute it and/or modify
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
 
 public class MMOInfoBed extends MMOPlugin implements Listener  {
 	private HashMap<Player, Label> widgets = new HashMap<Player, Label>();
 
 	@Override
 	public EnumBitSet mmoSupport(EnumBitSet support) {
 		support.set(Support.MMO_NO_CONFIG);
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		pm.registerEvents(this, this);
 	}
 
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		Label label = widgets.get(player);
 		if (label != null) {
 			String coords = getBedCoords(player);
 			if (!coords.equals(label.getText())) {
 				label.setText(coords).setDirty(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onMMOInfo(MMOInfoEvent event) {
 		if (event.isToken("bed")) {
 			SpoutPlayer player = event.getPlayer();
 			if (player.hasPermission("mmo.info.bed")) {
 				Label label = (Label) new GenericLabel(getBedCoords(player)).setResize(true).setFixed(true);
 				widgets.put(player, label);
 				event.setWidget(plugin, label);
 				event.setIcon("map.png");
 			} else {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	public String getBedCoords(Player player) {
 		Location loc = player.getBedSpawnLocation();
		if (player.getBedSpawnLocation()!= null) {
			if (loc.getBlockX() != 0 && loc.getBlockY() != 0 && loc.getBlockZ() != 0) {
				return String.format("[Bed] x:%d, y:%d, z:%d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
			} else {
				return String.format("BedSpawn coords not avaible");
			}
 		}
		return String.format("BedSpawn coords not avaible");
 	}
 }
