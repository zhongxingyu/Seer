 package org.mctourney.openhouse.listeners;
 
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import org.mctourney.autoreferee.regions.AutoRefRegion;
 import org.mctourney.openhouse.OpenHouseAdmin;
 import org.mctourney.openhouse.RegionData;
 import org.mctourney.openhouse.util.RegionUtil;
 
 import org.apache.commons.lang.math.RandomUtils;
 
 import com.google.common.collect.Lists;
 
 public class LobbyListener implements Listener
 {
 	protected OpenHouseAdmin plugin;
 
 	public LobbyListener(OpenHouseAdmin plugin)
 	{
 		this.plugin = plugin;
 		new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				for (String regname : LobbyListener.this.plugin.regions.keySet())
 					LobbyListener.this.plugin.regions.get(regname).update();
 			}
 		// run a sign update for all signs every 5 seconds
 		}.runTaskTimer(plugin, 0L, 5 * 20L);
 	}
 
 	private class DeferredSignUpdateTask extends BukkitRunnable
 	{
 		private String regname;
 
 		public DeferredSignUpdateTask(String regname)
 		{ this.regname = regname; }
 
 		@Override
 		public void run()
 		{ plugin.regions.get(regname).update(); }
 	}
 
 	private void updateLocationSign(Location loc)
 	{
 		for (Map.Entry<String, RegionData> e : plugin.regions.entrySet())
 			for (Sign sign : e.getValue().signs) if (sign.getLocation().equals(loc))
 				new DeferredSignUpdateTask(e.getKey()).runTask(plugin);
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void playerTeleport(PlayerTeleportEvent event)
 	{
 		updateLocationSign(event.getFrom());
 		updateLocationSign(event.getTo());
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void playerLogin(PlayerJoinEvent event)
 	{
 		updateLocationSign(event.getPlayer().getLocation());
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void playerLogout(PlayerQuitEvent event)
 	{
 		updateLocationSign(event.getPlayer().getLocation());
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void playerRespawn(PlayerRespawnEvent event)
 	{
 		updateLocationSign(event.getRespawnLocation());
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void makeTeleportSign(SignChangeEvent event)
 	{
 		Block block = event.getBlock();
 		if (block.getWorld() == plugin.getLobbyWorld())
 		{
 			String[] lines = event.getLines();
 			if (lines[0] != null && "[OpenHouse]".equals(ChatColor.stripColor(lines[0].trim())))
 			{
 				String regname = lines[1].trim().toUpperCase();
 				RegionData rdata = plugin.regions.get(regname);
 				if (rdata != null && rdata.signs.add((Sign) block.getState()))
 					new DeferredSignUpdateTask(regname).runTask(plugin);
 			}
 		}
 	}
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void hitTeleportSign(PlayerInteractEvent event)
 	{
 		Block block = event.getClickedBlock();
 		Player player = event.getPlayer();
 
		if (block.getWorld() == plugin.getLobbyWorld() && block.getState() instanceof Sign)
 		{
 			String[] lines = ((Sign) block.getState()).getLines();
 			if (lines[0] != null && "[OpenHouse]".equals(ChatColor.stripColor(lines[0].trim())))
 			{
 				String regname = lines[1].trim().toUpperCase();
 				RegionData rdata = null;
 
 				// teleport to the largest, non-full region
 				if (regname.startsWith("@ANY"))
 				{
 					List<RegionData> aregions = Lists.newArrayList();
 					int bestsize = 0, regsize;
 
 					for (RegionData reg : plugin.regions.values()) if (reg.canJoin(player))
 					{
 						if ((regsize = reg.getPlayers().size()) > bestsize)
 						{ bestsize = regsize; aregions = Lists.newArrayList(); }
 
 						aregions.add(reg);
 					}
 					rdata = aregions.get(RandomUtils.nextInt(aregions.size()));
 				}
 				// teleport back to spawn
 				else if (regname.startsWith("@BACK"))
 				{
 					// teleport the player to the lobby world spawn
 					player.teleport(plugin.getLobbyWorld().getSpawnLocation());
 
 					// setting rdata to null skips the block below
 					rdata = null; event.setCancelled(true);
 				}
 				// get the region we need
 				else rdata = plugin.regions.get(regname);
 
 				if (rdata != null && !event.isCancelled() &&
 					event.getAction() == Action.RIGHT_CLICK_BLOCK)
 				{
 					if (!rdata.canJoin(player)) player.sendMessage(
 						ChatColor.RED + "You do not have permission to join this region!");
 					else player.teleport(RegionUtil.getRegionCenter(rdata.region));
 				}
 			}
		}
 
		if (!player.hasPermission("openhouse.coach"))
			event.setCancelled(true);
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void playerMove(PlayerMoveEvent event)
 	{
 		Location to = event.getTo(), fm = event.getPlayer().getLocation();
 		for (Map.Entry<String, RegionData> e : plugin.regions.entrySet())
 		{
 			AutoRefRegion reg = e.getValue().region;
 			if (reg.contains(fm) != reg.contains(to))
 				new DeferredSignUpdateTask(e.getKey()).runTask(plugin);
 		}
 	}
 }
