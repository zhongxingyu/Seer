 /* *
 
  * HiddenSwitch - Hidden switches and buttons for Bukkit.
  * Copyright (C) 2011  Luphie (devLuphie) luphie@lumpcraft.com
 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
  * */
 package lc.Luphie.hiddenswitch;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class BrockListener extends BlockListener {
 
 	public static HiddenSwitch me;
 
 	public BrockListener(HiddenSwitch instance) {
 
 		me = instance;
 
 	}
 
 	@Override
 	public void onSignChange(SignChangeEvent ev) {
 
 		// Make sure the event wasn't canceled before this
 		if (ev.isCancelled()) {
 			return;
 		}
 
 		// Are sign based switches allowed?
 		if (!me.getConfig().getBoolean("lchs.signcontrol.allow-signs", true)) {
 			return;
 		}
 
 		// Get the sign text
 		String[] lns = ev.getLines();
 
 		// Is this a HiddenSwitch sign?
 		if (!lns[0].equals(me.getConfig().getString(
 			"lchs.signcontrol.sign-text",
 			"[lchs]"))) {
 			return;
 		}
 
 		// Get the involved Player
 		Player pl = ev.getPlayer();
 
 		// Does this player have permission to create a sign?
 		if (!pl.hasPermission("hiddenswitch.user.create")) {
 			signBreak(ev.getBlock(), pl);
 			return;
 		}
 
 		// Check if there is a user line set and if the player has that
 		// permission
 		if (!lns[1].isEmpty() && !pl.hasPermission("hiddenswitch.user.lockuser")) {
 			signBreak(ev.getBlock(), pl);
 			return;
 		}
 
 		// Check if there is a key line set and if the player has that
 		// permission
 		if (!lns[2].isEmpty() && !pl.hasPermission("hiddenswitch.user.lockitem")) {
 			signBreak(ev.getBlock(), pl);
 			return;
 		}
 
 	}
 
 	public void signBreak(Block sign, Player pl) {
 
 		sign.setTypeId(0);
 		ItemStack signdrop = new ItemStack(323, 1);
 		pl.getWorld().dropItem(sign.getLocation(), signdrop);
 	}
 
 }
