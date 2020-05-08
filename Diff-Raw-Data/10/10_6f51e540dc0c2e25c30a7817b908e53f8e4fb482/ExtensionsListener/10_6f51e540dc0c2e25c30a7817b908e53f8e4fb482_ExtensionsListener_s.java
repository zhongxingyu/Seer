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
 import org.bukkit.block.Block;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.entity.Minecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.inventory.Inventory;
 import com.barroncraft.sce.ClanBuildingList.BuildingType;
 import com.sk89q.worldedit.Vector;
 import org.kitteh.tag.TagAPI;
 import org.kitteh.tag.PlayerReceiveNameTagEvent;
 
 public class ExtensionsListener implements Listener {
     SimpleClansExtensions plugin;
     Map<String, Integer> towerCounts;
 
     public ExtensionsListener(SimpleClansExtensions plugin, World world)
     {
         this.plugin = plugin;
         this.towerCounts = new HashMap<String, Integer>();
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         loadRedBuildingLocs(world);
         loadBlueTowerLocs(world);
     }
 
     private void loadRedBuildingLocs(World world) 
     {
         ClanBuildingList redBuildings = plugin.getClanTeams().get("red").getBuildings();
         redBuildings.addBuilding(BuildingType.Tower, new Location(world, -1190, 53, 257), 5);
         redBuildings.addBuilding(BuildingType.Tower, new Location(world, -1189, 53, 347), 4);
         redBuildings.addBuilding(BuildingType.Tower, new Location(world, -1091, 54, 339), 3);
         redBuildings.addBuilding(BuildingType.Tower, new Location(world, -1001, 53, 448), 2); 
         redBuildings.addBuilding(BuildingType.Tower, new Location(world, -1089, 53, 458), 1);
         redBuildings.addBuilding(BuildingType.Nexus, new Location(world, -1159, 55, 410), 0);
         towerCounts.put("red", redBuildings.buildingsCount(BuildingType.Tower));
     }
 
     private void loadBlueTowerLocs(World world) 
     {
         ClanBuildingList blueBuildings = plugin.getClanTeams().get("blue").getBuildings();
         blueBuildings.addBuilding(BuildingType.Tower, new Location(world, -938,  53, 378), 5);
         blueBuildings.addBuilding(BuildingType.Tower, new Location(world, -939,  53, 288), 4);
         blueBuildings.addBuilding(BuildingType.Tower, new Location(world, -1044, 54, 296), 3);
         blueBuildings.addBuilding(BuildingType.Tower, new Location(world, -1134, 53, 194), 2);
         blueBuildings.addBuilding(BuildingType.Tower, new Location(world, -1046, 53, 184), 1);
         blueBuildings.addBuilding(BuildingType.Nexus, new Location(world, -976,  54, 225), 0);
         towerCounts.put("blue", blueBuildings.buildingsCount(BuildingType.Tower));
     }
 
     @EventHandler
     public void onEntityExplode(EntityExplodeEvent event) 
     {
         // Check if a TNT has exploded at a know Tower location
         if (event.getEntityType() == EntityType.PRIMED_TNT) 
         {
             Location eventLoc = event.getLocation();
             Server server = plugin.getServer();
 
             for (ClanTeam team : plugin.getClanTeams().values())
             {
                 ClanBuildingList buildings = team.getBuildings();
                 String teamName = team.getColor() + team.getName().toUpperCase() + ChatColor.YELLOW;
 
                 // Towers
                 if (buildings.destroyBuilding(BuildingType.Tower, eventLoc))
                 {
                     towerCounts.put(team.getName(), buildings.buildingsCount(BuildingType.Tower));
 
                     // Check if all towers are destroyed
                     if (towerCounts.get(team.getName()) == 0) 
                         server.broadcastMessage(ChatColor.YELLOW + "All "+ teamName + " Towers Are Destroyed! Defend the NEXUS!");
                     else
                         server.broadcastMessage(ChatColor.YELLOW + "A "+ teamName + " Tower Has Been Destroyed! (" + towerCounts.get(team.getName()) + " remaining)");
                 }
 
                 // Nexus
                 if (buildings.destroyBuilding(BuildingType.Nexus, eventLoc))
                 {
                     server.broadcastMessage(ChatColor.YELLOW + "The " + teamName + " NEXUS Has Been Destroyed!  Game over.");
 
                     if (ServerResetter.enableResetFlag())
                         server.broadcastMessage(ChatColor.YELLOW + "The map should auto reset within a few minutes.");
                     else
                         server.broadcastMessage(ChatColor.YELLOW + "There was an issue resetting the map.");
                 }
             }
         }
 
     }
 
     @EventHandler
     public void onVehicleDestroy(VehicleDestroyEvent event)
     {
         Vehicle vehicle = event.getVehicle();
         Entity attacker = event.getAttacker();
        if (vehicle.getType() != EntityType.MINECART || attacker.getType() != EntityType.PLAYER)
             return;
 
        Player player = (Player)attacker;
        Inventory inv = player.getInventory();
         if (!inv.contains(Material.BOW, 1) || !inv.contains(Material.ARROW, 1))
         {
             event.setCancelled(true);
             return;
         }
 
         Minecart cart = (Minecart)vehicle;
         Location location = cart.getLocation();
         Block block1 = location.getBlock();
         Block block2 = location.add(0, -1, 0).getBlock();
         if (block1.getType() == Material.DETECTOR_RAIL)
         {
             block1.breakNaturally();
             block2.breakNaturally();
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
         public void onPlayerMove(PlayerMoveEvent event) 
         {
             Player player = event.getPlayer();
             if (player.getHealth() == 0 || player.getGameMode() != GameMode.SURVIVAL)
                 return;
 
             ClanPlayer clanPlayer = plugin.getClanManager().getCreateClanPlayer(player.getName());
             String clanName = clanPlayer != null && clanPlayer.getClan() != null
                 ? clanPlayer.getClan().getName()
                 : null;
             if (clanName == null)
                 return;
 
             Vector pt = new Vector(
                     event.getTo().getBlockX(), 
                     event.getTo().getBlockY(), 
                     event.getTo().getBlockZ()
                     );
 
             List<String> regionNames = plugin.getGuardManager().getGlobalRegionManager()
                 .get(player.getWorld())
                 .getApplicableRegionsIDs(pt);
 
             for (ClanTeam team : plugin.getClanTeams().values())
             {
                 String teamName = team.getName();
 
                 if (!clanName.equalsIgnoreCase(teamName))
                 {
                     String baseRegionName = team.getBaseRegion();
                     String spawnRegionName = team.getSpawnRegion();
                     boolean towersUp = towerCounts.get(teamName) != 0;
 
                     for (String foundName : regionNames)
                     {
                         boolean inSpawn = foundName.equalsIgnoreCase(spawnRegionName);
                         boolean inBase = towersUp && foundName.equalsIgnoreCase(baseRegionName);
 
                         if (inSpawn)
                         {
                             player.sendMessage(ChatColor.YELLOW + "You are not allowed to enter the opposing team's spawn");
                             player.sendMessage(ChatColor.YELLOW + "Go kill the nexus instead.");
                         }
                         else if (inBase)
                         {
                             player.sendMessage(ChatColor.YELLOW + "You are not allowed to enter the opposing team's base");
                             player.sendMessage(ChatColor.YELLOW + "until all towers have been destroyed.");
                         }
 
                         if (inSpawn || inBase)
                         {
                             player.setHealth(0); // Spawn campers must DIE!!!!!
                             return;	
                         }
                     }
                 }
             }
         }
 
     @EventHandler
     public void onNameTag(PlayerReceiveNameTagEvent event)
     {
         String namedPlayerName   = event.getNamedPlayer().getName();
         ClanPlayer namedPlayer   = plugin.getClanManager().getCreateClanPlayer(namedPlayerName);
         ClanPlayer viewingPlayer = plugin.getClanManager().getCreateClanPlayer(event.getPlayer().getName());
 
         if (viewingPlayer == null || namedPlayer == null || viewingPlayer.getClan() == null || namedPlayer.getClan() == null)
             return;
 
         String viewingPlayerClan = viewingPlayer.getClan().getName();
         String namedPlayerClan   = namedPlayer.getClan().getName(); 
 
         if (viewingPlayerClan.equalsIgnoreCase(namedPlayerClan))
             event.setTag(ChatColor.GREEN + namedPlayerName);
         else
             event.setTag(ChatColor.RED + namedPlayerName);
     }
 }
