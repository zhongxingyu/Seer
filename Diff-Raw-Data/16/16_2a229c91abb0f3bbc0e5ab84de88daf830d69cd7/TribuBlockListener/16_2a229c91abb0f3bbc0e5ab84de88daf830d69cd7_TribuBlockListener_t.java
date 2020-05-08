 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu.Listeners;
 
 import graindcafe.tribu.Tribu;
 import graindcafe.tribu.Signs.TribuSign;
 
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 
 public class TribuBlockListener implements Listener {
 	private final Tribu	plugin;
 
 	public TribuBlockListener(final Tribu instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.LOW)
 	public void onBlockBreak(final BlockBreakEvent event) {
 		if (!event.isCancelled()) if (TribuSign.isIt(plugin, event.getBlock())) {
 			if (event.getPlayer().hasPermission("tribu.signs.break"))
 				plugin.getLevel().removeSign(event.getBlock().getLocation());
 			else {
 				if (event.getPlayer() != null) Tribu.messagePlayer(event.getPlayer(), plugin.getLocale("Message.ProtectedBlock"));
 				TribuSign.update((Sign) event.getBlock().getState());
 				event.setCancelled(true);
 			}
 		} else if (plugin.isRunning() && plugin.isPlaying(event.getPlayer()) && !(event.getPlayer().hasPermission("tribu.super.break") || plugin.config().PlayersAllowBreak)) event.setCancelled(true);
		//else Logger.getLogger("Minecraft").info(event.getPlayer().hasPermission("tribu.super.break") ? "has permission" : plugin.config().PlayersAllowBreak ? "allowed" : "disallowed");
 	}
 
 	@EventHandler
 	public void onBlockPlace(final BlockPlaceEvent event) {
 		if (!event.isCancelled()) if (plugin.isRunning() && plugin.isPlaying(event.getPlayer()) && !(event.getPlayer().hasPermission("tribu.super.place") || plugin.config().PlayersAllowPlace)) event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onBlockRedstoneChange(final BlockRedstoneEvent event) {
 		if (plugin.isRunning()) // plugin.getBlockTrace().pushRedstoneChanged(event.getBlock());
 			if (plugin.getLevel() != null) plugin.getLevel().onRedstoneChange(event);
 	}
 
 	@EventHandler
 	public void onSignChange(final SignChangeEvent event) {
 		if (TribuSign.isIt(plugin, event.getLines())) if (event.getPlayer().hasPermission("tribu.signs.place")) {
 			final TribuSign sign = TribuSign.getObject(plugin, event.getBlock().getLocation(), event.getLines());
 			if (sign != null) if (plugin.getLevel() != null) {
 				if (plugin.getLevel().addSign(sign)) Tribu.messagePlayer(event.getPlayer(), plugin.getLocale("Message.TribuSignAdded"));
 			} else {
 				Tribu.messagePlayer(event.getPlayer(), plugin.getLocale("Message.NoLevelLoaded"));
 				Tribu.messagePlayer(event.getPlayer(), plugin.getLocale("Message.NoLevelLoaded2"));
 				event.getBlock().setTypeId(0);
 				event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
 				event.setCancelled(true);
 			}
 		} else {
 			Tribu.messagePlayer(event.getPlayer(), plugin.getLocale("Message.CannotPlaceASpecialSign"));
 			event.getBlock().setTypeId(0);
 			event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
 			event.setCancelled(true);
 		}
 	}
 
 	public void registerEvents(final PluginManager pm) {
 		pm.registerEvents(this, plugin);
 	}
 }
