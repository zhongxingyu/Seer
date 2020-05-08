 /*
  * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
  *
  * mmoMinecraft is free software: you can redistribute it and/or modify
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
 import java.util.Map;
 import mmo.Core.InfoAPI.MMOInfoEvent;
 import mmo.Core.MMOListener;
 import mmo.Core.MMOPlugin;
 import mmo.Core.util.EnumBitSet;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /** Adds {Gold} to the mmoInfo token list. */
 public final class MMOInfoGold extends MMOPlugin {
 
 	/** Map of player to label, used for telling widget that it needs to be updated. */
 	private final transient Map<Player, CustomLabel> widgets = new HashMap<Player, CustomLabel>();
 	/** The Vault economy in use. */
 	private static Economy economy;
 
 	@Override
 	public EnumBitSet mmoSupport(final EnumBitSet support) {
 		support.set(Support.MMO_PLAYER);
 		support.set(Support.MMO_NO_CONFIG);
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		final RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 
 			pm.registerEvent(Type.CUSTOM_EVENT,
 					new MMOListener() {
 
 						@Override
 						public void onMMOInfo(final MMOInfoEvent event) {
 							if (event.isToken("gold")) {
 								final SpoutPlayer player = event.getPlayer();
 								if (player.hasPermission("mmo.info.gold")) {
 									final CustomLabel label = (CustomLabel) new CustomLabel().setResize(true).setFixed(true);
 									widgets.put(player, label);
 									event.setWidget(plugin, label);
 									event.setIcon("coin.png");
 								} else {
 									event.setCancelled(true);
 								}
 							}
 						}
 					}, Priority.Normal, this);
 		}
 	}
 
 	@Override
 	public void onPlayerQuit(final Player player) {
 		widgets.remove(player);
 	}
 
 	/** One per player. */
 	public static final class CustomLabel extends GenericLabel {
 
 		/** If the widget needs to update the display. */
 		private transient int tick = 0;
 
 		@Override
 		public void onTick() {
 			if (tick++ % 20 == 0) {
				final String[] money = Float.toString((float) economy.getBalance(this.getScreen().getPlayer().getName())).split("\\.");
 				setText(String.format(ChatColor.WHITE + "%s" + ChatColor.YELLOW + "g " + ChatColor.WHITE + "%s" + ChatColor.GRAY + "s", money.length > 0 ? money[0] : "0", money.length > 1 ? money[1] : "0"));
 			}
 		}
 	}
 }
