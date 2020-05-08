 /*
  * This file is part of mmoInfoTime <http://github.com/mmoMinecraftDev/mmoInfoTime>.
  *
  * mmoInfoTime is free software: you can redistribute it and/or modify
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
 
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import mmo.Core.InfoAPI.MMOInfoEvent;
 import mmo.Core.MMOPlugin;
 import mmo.Core.MMOPlugin.Support;
 import mmo.Core.util.EnumBitSet;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.PluginManager;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.Screen;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class mmoInfoTime extends MMOPlugin
 implements Listener
 {
 	private HashMap<Player, CustomLabel> widgets = new HashMap();
 	private static int config_timetype = 12;
 
 	public EnumBitSet mmoSupport(EnumBitSet support)
 	{		
 		support.set(MMOPlugin.Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	public void onEnable() {
 		super.onEnable();
 		this.pm.registerEvents(this, this);
 	}
 
 	@Override
 	public void loadConfiguration(final FileConfiguration cfg) {
 		config_timetype = cfg.getInt("timetype", config_timetype);				
 	}
 
 	@EventHandler
 	public void onMMOInfo(MMOInfoEvent event)
 	{
 		if (event.isToken("time")) {
 			SpoutPlayer player = event.getPlayer();
 			if (player.hasPermission("mmo.info.time")) {
				CustomLabel label = (CustomLabel)new CustomLabel().setResize(true).setFixed(true).setMaxWidth(25);
				label.setText("12am");
 				this.widgets.put(player, label);
 				event.setWidget(this.plugin, label);
 				event.setIcon("clock.png");
 			} else {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	public class CustomLabel extends GenericLabel
 	{
 		private boolean check = true;		
 
 		public CustomLabel() {
 		}
 
 		public void change() {
 			this.check = true;
 		}
 		private transient int tick = 0;
 		//int minutes = (int)(60L * (getServer().getWorld(getScreen().getPlayer().getWorld().getName()).getTime() % 1000L) / 1000L);
 		public void onTick()
 		{
 			if (tick++ % 100 == 0) {
 				int hours = (int)((getServer().getWorld(getScreen().getPlayer().getWorld().getName()).getTime() / 1000L + 6L) % 24L);
 				if (config_timetype == 12) {
 					if (hours == 1)
 						setText(String.format("12am"));
 					if (hours > 1 && hours <= 11)
 						setText(String.format(hours + "am"));
 					if (hours == 12)
 						setText(String.format(hours + "pm"));
 					if (hours >= 13) 
 						setText(String.format((hours-12) + "pm"));			    
 				} else {
 					setText(String.format(hours + "hrs"));
 				}
 			}
 		}
 	}
 }
