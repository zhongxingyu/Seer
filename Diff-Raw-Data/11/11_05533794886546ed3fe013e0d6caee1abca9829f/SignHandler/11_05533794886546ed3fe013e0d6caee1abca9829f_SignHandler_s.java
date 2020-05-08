 /*
  * Copyright (C) 2012 TomyLobo
  *
  * This file is part of Routes.
  *
  * Routes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.tomylobo.routes.sign;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.Plugin;
 
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.Multimap;
 
 import eu.tomylobo.abstraction.block.BlockState;
 import eu.tomylobo.abstraction.block.Sign;
 import eu.tomylobo.abstraction.bukkit.BukkitUtils;
 import eu.tomylobo.abstraction.entity.MobType;
 import eu.tomylobo.abstraction.entity.Player;
 import eu.tomylobo.math.Location;
 import eu.tomylobo.routes.Routes;
 import eu.tomylobo.routes.commands.system.CommandException;
 import eu.tomylobo.routes.trace.SignShape;
 import eu.tomylobo.routes.trace.SignTraceResult;
 import eu.tomylobo.routes.util.Ini;
 
 public class SignHandler implements Listener {
 	private final Routes plugin;
 	private Map<Location, TrackedSign> trackedSigns = new HashMap<Location, TrackedSign>();
 	public Map<Player, SignSession> sessions = new HashMap<Player, SignSession>();
 
 	public SignHandler(Routes plugin) {
 		this.plugin = plugin;
 
 		Bukkit.getServer().getPluginManager().registerEvents(this, (Plugin) plugin.getFrameworkPlugin());
 	}
 
 	private SignSession getOrCreateSession(Player player) {
 		SignSession session = sessions.get(player);
 		if (session == null) {
 			sessions.put(player, session = new SignSession(this, player));
 		}
 		return session;
 	}
 
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
 	public void onSignChange(SignChangeEvent event) {
 		final Location block = BukkitUtils.wrap(event.getBlock().getLocation());
 		final Sign sign = (Sign) block.getBlockState();
 
 		boolean hasDestinations = false;
 		for (int i = 0; i < 4; ++i) {
 			final String line = event.getLine(i);
 			sign.setLine(i, line);
 			if (line.startsWith(plugin.config.signsRoutePrefix)) {
 				hasDestinations = true;
 			}
 		}
 
 		if (hasDestinations) {
			trackedSigns.put(block, new TrackedSign(block));
 			save();
 
 			event.getPlayer().sendMessage("Added tracked sign.");
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
 	public void onBlockBreak(BlockBreakEvent event) {
 		final Location block = BukkitUtils.wrap(event.getBlock().getLocation());
 
 		final TrackedSign trackedSign = trackedSigns.remove(block);
 		if (trackedSign == null)
 			return;
 
 		save();
 
 		event.getPlayer().sendMessage("Broke tracked sign");
 	}
 
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
 			return; // We only want RIGHT_CLICK_BLOCK
 
 		final Location block = BukkitUtils.wrap(event.getClickedBlock().getLocation());
 		final BlockState blockState = block.getBlockState();
 
 		if (!(blockState instanceof Sign))
 			return; // We only want signs
 
 		TrackedSign trackedSign = trackedSigns.get(block);
 		if (trackedSign == null)
 			return; // We only want tracked signs
 
 		final Player player = BukkitUtils.wrap(event.getPlayer());
 		final Location eyeLocation = player.getEyeLocation();
 
 		final SignShape shape = new SignShape(block);
 		final SignTraceResult trace = shape.trace(eyeLocation);
 
 		final int index = trace.index;
 
 		if (index < 0 || index >= 4)
 			return; // We only want valid indexes
 
 		if (!trackedSign.hasEntry(index))
 			return; // We only want indexes for which the sign has a tracked entry
 
 		final SignSession session = getOrCreateSession(player);
 		if (session.isSelected(trackedSign, index)) {
 			session.close();
 
 			final String routeName = trackedSign.getEntry(index);
 
 			try {
 				plugin.travelAgency.addTravellerWithMount(routeName, player, MobType.ENDER_DRAGON);
 				player.sendMessage("Travelling on route '"+routeName+"'.");
 			}
 			catch (CommandException e) {
 				player.sendMessage(e.getMessage());
 			}
 		}
 		else {
 			session.select(trackedSign, index);
 		}
 	}
 
 	public void save() {
 		Multimap<String, Multimap<String, String>> sections = LinkedListMultimap.create();
 		for (Entry<Location, TrackedSign> entry : trackedSigns.entrySet()) {
 			final TrackedSign trackedSign = entry.getValue();
 
 			sections.put("sign", trackedSign.save());
 		}
 
 		Ini.save(plugin.getConfigFileName("signs.txt"), sections);
 	}
 
 	public void load() {
 		trackedSigns.clear();
 
 		final Multimap<String, Multimap<String, String>> sections = Ini.load(plugin.getConfigFileName("signs.txt"));
 		if (sections == null)
 			return;
 
 		for (Multimap<String, String> section : sections.get("sign")) {
 			final TrackedSign trackedSign = new TrackedSign(section);
 
 			trackedSigns.put(trackedSign.getLocation(), trackedSign);
 		}
 	}
 }
