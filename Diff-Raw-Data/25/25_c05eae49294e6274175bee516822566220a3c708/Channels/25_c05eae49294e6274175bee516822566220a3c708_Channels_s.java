 /*
  * This file is part of mmoChat <http://github.com/mmoMinecraftDev/mmoChat>.
  *
  * mmoChat is free software: you can redistribute it and/or modify
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
 package mmo.Chat;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import mmo.Core.ChatAPI.MMOChatEvent;
 import mmo.Core.MMO;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 public class Channels implements Listener {
 
 	private static HashMap<String, String> tells = new HashMap<String, String>();
 	private static final int SAY_RANGE = 25;
 	private static final int YELL_RANGE = 300;
 	private static final int DEFAULT_RANGE = 100;

 	@EventHandler
 	public void onMMOChat(final MMOChatEvent event) {
 		final Player from = event.getPlayer();
 		final Set<Player> recipients = event.getRecipients();
 		if (event.hasFilter("disabled")) {
 			recipients.clear();
 		} else if (event.hasFilter("server")) {
 			// Should really refresh the target list...
 		} else if (event.hasFilter("world") || event.hasFilter("say") || event.hasFilter("range") || event.hasFilter("yell")) {
 			// Some channels have an implied "world" filter
 			for (final Iterator<Player> it = recipients.iterator(); it.hasNext();) {
 				final Player to = it.next();
 				if (from.getWorld() != to.getWorld()) {
 					it.remove();
 				}
 			}
 		}
 		int range = -1;
 		if (event.hasFilter("say")) {
 			range = SAY_RANGE;
 		} else if (event.hasFilter("range")) {
 			final String[] args = event.getArgs("range");
 			if (args.length > 0) {
 				range = Integer.parseInt(args[0]);
 			} else {
 				range = DEFAULT_RANGE;
 			}
 		} else if (event.hasFilter("yell")) {
 			range = YELL_RANGE;
 		}
 		if (range >= 0) {
 			for (final Iterator<Player> it = recipients.iterator(); it.hasNext();) {
 				final Player to = it.next();
 				if (from.getLocation().distance(to.getLocation()) > range) {
 					it.remove();
 				}
 			}
 		}
 		final boolean isTell = event.hasFilter("tell");
 		if (isTell || event.hasFilter("reply")) {
 			final Player to = Bukkit.getServer().getPlayer(
 					isTell
 					? MMO.firstWord(event.getMessage())
					: tells.get(from.getName()));
 			if (isTell) {
 				event.setMessage(MMO.removeFirstWord(event.getMessage()));
 			}
 			recipients.clear();
 			if (to == null) {
 				tells.remove(from.getName());
 				event.setCancelled(true);
 			} else {
 				tells.put(to.getName(), from.getName());
 				recipients.add(from);
 				recipients.add(to);
 				event.setFormat(to, event.getFormat().replaceAll("%2\\$s", "%2\\$s&f tells you"));
 				event.setFormat(from, event.getFormat().replaceAll("%2\\$s", "You tell " + MMO.getColor(from, to) + to.getName() + ChatColor.WHITE));
 			}
 		}
 	}
 }
