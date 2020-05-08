 package com.barroncraft.sce;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
 
 import org.bukkit.ChatColor;
import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import com.barroncraft.sce.ClanBuildingList.BuildingType;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 
 public class ExtensionsListener implements Listener {
 	
 	SimpleClansExtensions plugin;
 	ClanBuildingList redBuildings;
 	ClanBuildingList blueBuildings;
 	Map<String, Integer> towerCounts;
 	
 	public ExtensionsListener(SimpleClansExtensions plugin, World world)
 	{
 		this.plugin = plugin;
 		this.redBuildings = new ClanBuildingList();
 		this.blueBuildings = new ClanBuildingList();
 		this.towerCounts = new HashMap<String, Integer>();
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		loadRedBuildingLocs(world);
 		loadBlueTowerLocs(world);
 	}
 
 	private void loadRedBuildingLocs(World world) 
 	{
 		redBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1189, 53, 347));
 		redBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1190, 53, 257));
 		redBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1089, 53, 458));
 		redBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1001, 53, 448));
 		redBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1091, 54, 339));
 		redBuildings.AddBuilding(BuildingType.Nexus, new Location(world, -1159, 55, 410));
 		towerCounts.put("red", redBuildings.BuildingsCount(BuildingType.Tower));
 	}
 	
 	private void loadBlueTowerLocs(World world) 
 	{
 		blueBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1046, 53, 184));
 		blueBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1134, 53, 194));
 		blueBuildings.AddBuilding(BuildingType.Tower, new Location(world, -939, 53, 288));
 		blueBuildings.AddBuilding(BuildingType.Tower, new Location(world, -938, 53, 378));
 		blueBuildings.AddBuilding(BuildingType.Tower, new Location(world, -1044, 54, 296));
 		blueBuildings.AddBuilding(BuildingType.Nexus, new Location(world, -976, 54, 225));
 		towerCounts.put("blue", blueBuildings.BuildingsCount(BuildingType.Tower));
 	}
 	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event) 
 	{
 		// Check if a TNT has exploded at a know Tower location
 		if (event.getEntityType() == EntityType.PRIMED_TNT) 
 		{
 			Location eventLoc = event.getLocation();
 			Server server = plugin.getServer();
 			
 			// Red towers
 			if (redBuildings.DestroyBuilding(BuildingType.Tower, eventLoc))
 			{
 				towerCounts.put("red", redBuildings.BuildingsCount(BuildingType.Tower));
 				
				// Check if all towers are destroyed
 				if (towerCounts.get("red")  == 0) 
 					server.broadcastMessage(ChatColor.YELLOW + "All "+ ChatColor.RED + "RED" + ChatColor.YELLOW + " Towers Are Destroyed! Defend the NEXUS!");
				else
					server.broadcastMessage(ChatColor.YELLOW + "A "+ ChatColor.RED + "RED" + ChatColor.YELLOW + " Tower Has Been Destroyed! (" + towerCounts.get("red") + " remaining)");
 			}
 			
 			// Blue Towers
 			if (blueBuildings.DestroyBuilding(BuildingType.Tower, eventLoc))
 			{
 				towerCounts.put("blue", blueBuildings.BuildingsCount(BuildingType.Tower));
 
 				// Check if all towers are destroyed
 				if (towerCounts.get("blue") == 0) 
 					server.broadcastMessage(ChatColor.YELLOW + "All "+ ChatColor.BLUE + "BLUE" + ChatColor.YELLOW + " Towers Are Destroyed! Defend the NEXUS!");
				else
					server.broadcastMessage(ChatColor.YELLOW + "A "+ ChatColor.BLUE + "BLUE" + ChatColor.YELLOW + " Tower Has Been Destroyed! (" + towerCounts.get("blue")  + " remaining)");
 			}
 			
 			
 			// Nexus
 			if (redBuildings.DestroyBuilding(BuildingType.Nexus, eventLoc))
 			{
 				server.broadcastMessage(ChatColor.RED + "The RED NEXUS Has Been Destroyed!" + ChatColor.BLUE + " BLUE WINS!");
 			}
 			else if (blueBuildings.DestroyBuilding(BuildingType.Nexus, eventLoc))
 			{
 				server.broadcastMessage(ChatColor.BLUE + "The BLUE NEXUS Has Been Destroyed!" + ChatColor.RED + " RED WINS!");
 			}
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
     public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		ClanPlayer clanPlayer = plugin.clanManager.getCreateClanPlayer(player.getName());
 		Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
 		World world = player.getWorld();
 		
 		RegionManager regions = plugin.guardManager.getGlobalRegionManager().get(world);
 		List<String> regionNames = regions.getApplicableRegionsIDs(pt);
 		
 		for (String foundName : regionNames)
 		{
 			for (String clanName : plugin.baseRegions.keySet())
 			{
 				if (foundName.equalsIgnoreCase(plugin.baseRegions.get(clanName)))
 				{
 					if (clanPlayer != null && clanPlayer.getClan().getName().equalsIgnoreCase(clanName))
 					{
 						// TODO: Add player protection if they are in their own base
 					}
					else if (towerCounts.get(clanName) != 0 && player.getHealth() != 0 && player.getGameMode() == GameMode.SURVIVAL)
 					{
 						player.sendMessage(ChatColor.YELLOW + "You are not allowed to enter the opposing team's base");
 						player.sendMessage(ChatColor.YELLOW + "until all towers have been destroyed.");
 						player.setHealth(0); // Spawn campers must DIE!!!!!
 					}
 					
 					return;
 				}
 			}
 		}
 	}
 	
 }
