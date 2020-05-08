 package me.cmesh.DreamLand;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.player.*;
 import org.bukkit.event.*;
 
 
 public class DreamLandPlayerListener implements Listener
 {
 	private static DreamLand plugin;
 	
 	public DreamLandPlayerListener(DreamLand instance)
 	{
 		plugin = instance;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerPortal(PlayerPortalEvent event)
 	{
 		event.setCancelled(plugin.player(event.getPlayer()).Dreaming());
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerMove(PlayerMoveEvent event)
 	{		
 		DreamLandPlayer player = plugin.player(event.getPlayer());
 		DreamLandWorld world = plugin.world(player.getWorld());
 		
 		if (player.Dreaming())
 		{
 			player.self().setFoodLevel(20);
 			if (event.getTo().getY() < 0)
 			{
 				player.leaveDream();
 				return;
 			}
 			if(plugin.options.morningReturn && !player.hasPermission("dreamland.nowakeup", false))
 			{
 				long time = player.getBedWorld().getTime();
 				if(time >=0 && time <= 12000)
 				{
 					player.sendMessage("It is morning, WAKEUP!");
 					player.leaveDream(); 
 					return;
 				}
 			}
 			if(world.Flaming)
 			{
 				player.self().setFireTicks(3*30);
 			}
 			if(new Random().nextInt(1000) < world.MobChance)
 			{
 				for(String mob : world.Mobs)
 				{
 					EntityType ct = EntityType.fromName(mob);
 		            if (ct == null) continue;
 		            int amount = new Random().nextInt(3);
 		            for (int i = 0; i < amount; i++)
 		            {
 		            	Location loc = player.getLocation();
 		            	loc.setX(loc.getX() + 10);
		                world.getWorld().spawnEntity(loc, ct);
 		            }
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerBedEnter(final PlayerBedEnterEvent event)
 	{		
 		final DreamLandPlayer player = plugin.player(event.getPlayer());
 		
 		if(player.Dreaming()){return;}
 		
 		if (plugin.options.anyoneCanGo || player.hasPermission("dreamland.goto",plugin.options.anyoneCanGo))
 		{
 			if (new Random().nextInt(100) < (plugin.dream.Chance + plugin.nightmare.Chance))
 			{
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() 
 		        {
 		        	public void run()
 		        	{
 						event.setCancelled(true);
 						
 						Boolean nightmare = (plugin.nightmare.Chance != 0) && new Random().nextInt(100) < plugin.nightmare.Chance;
 						
 						player.enterDream(player.getLocation(),nightmare);
 						
 			    	}
 	        	}, 60L);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		DreamLandPlayer player = plugin.player(event.getPlayer());
 		if (player.Dreaming())
 		{
 			player.leaveDream();
 		}
 		plugin.removePlayer(player);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerRespawn(PlayerRespawnEvent event)
 	{
 		if(plugin.player(event.getPlayer()).Dreaming())
 		{
 			event.setRespawnLocation(plugin.player(event.getPlayer()).respawn());
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerKick(PlayerKickEvent event)
 	{
 		DreamLandPlayer player = plugin.player(event.getPlayer());
 		if(	event.getReason().contains("moved too quickly") && 
 			player.lastFly != null && 
 			player.lastFly >= player.getWorld().getTime() - 100)
 		{
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event)
 	{
 		plugin.player(event.getPlayer());
 	}
 }
