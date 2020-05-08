 package me.chrizc.sitm;
 
 import java.util.Random;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class SITMPlayerListener extends PlayerListener {
     
     private final StuckInTheMud plugin;
     private final SITMGameHandler gameHandler;
     private final SITMDatabaseHandler databaseHandler;
     private final SITMConfig config;
     
     public String PREFIX = ChatColor.DARK_GREEN + "[StuckInTheMud] ";
     public String ERR_PREFIX = ChatColor.RED + "[StuckInTheMud] ";
     
     public SITMPlayerListener (StuckInTheMud instance, SITMGameHandler game, SITMDatabaseHandler database, SITMConfig config) {
         plugin = instance;
         gameHandler = game;
         databaseHandler = database;
         this.config = config;
     }
     
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         //If we're not in game, we don't want to check any interactions - performance.
         if (!plugin.inGame) return;
         
         ItemStack item = event.getPlayer().getItemInHand();
         
         //If the player is not holding the list item, we don't want to know.
         if (!item.equals(new ItemStack(config.item, 1))) return;
         
         //If the player is left clicking something, we don't want to know.
         if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) return;
         
         gameHandler.listPlayers(event.getPlayer());
     }
     
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         if (config.leaderboard) databaseHandler.createRow(event.getPlayer());
         
        if (config.arena) {
            int[][] arr = new int[2][3];
            try {
                String[] p1 = config.arena_area1.split(",");
                String[] p2 = config.arena_area2.split(",");
                arr = Arena.parseMinMax(p1, p2);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            if (Arena.isWithin(arr[0], arr[1], event.getPlayer().getLocation().getBlock())) gameHandler.telePlayerToLobby(event.getPlayer());
         }
         
         if (!plugin.inRegistration) return;
         
         if (config.verbose) System.out.println(PREFIX + "Informing " + event.getPlayer().getName() + " of current game.");
         
         event.getPlayer().sendMessage(PREFIX + "There's a game of Stuck In The Mud in the registration stage! Type /mud join to join in the fun!");
     }
     
     @Override
     public void onPlayerMove(PlayerMoveEvent event) {
         //If we're not in game or in countdown, don't do anything else. - performance measure.
         if (!plugin.inGame && !plugin.inCountdown) return;
 
         //If we're in countdown, freeze players.
         if (plugin.inCountdown && plugin.players.containsKey(event.getPlayer())) {
             Location locFrom = event.getFrom();
             Location locTo = event.getTo();
             if ((locFrom.getX() != locTo.getX()) || (locFrom.getZ() != locTo.getZ())) {
                 event.setTo(new Location(event.getPlayer().getWorld(), locFrom.getX(), locTo.getY(), locFrom.getZ()));
             }
             return;
         }
         
         
         
         //If we're in game, and the player's marked as frozen, freeze them.
         if (plugin.inGame && plugin.players.containsKey(event.getPlayer()) && plugin.players.get(event.getPlayer()).equalsIgnoreCase("FROZEN")) {
             Location locFrom = event.getFrom();
             Location locTo = event.getTo();
             if ((locFrom.getX() != locTo.getX()) || (locFrom.getZ() != locTo.getZ())) {
                 event.setTo(new Location(event.getPlayer().getWorld(), locFrom.getX(), locTo.getY(), locFrom.getZ()));
             }
         }
     }
     
     @Override
     public void onPlayerQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         if (plugin.players.containsKey(player)) {
             if (plugin.inRegistration) {
                 gameHandler.restoreInventory(player);
                 plugin.players.remove(player);
                 Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.RED + " has left the server! They've been un-registered from the current game.");
                 if (plugin.numOfPlayers == 1) {
                     Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are no more players left in the game! The game has ended.");
                     plugin.inRegistration = false;
                 }
                 plugin.numOfPlayers--;
                 
                 
             } else if (plugin.inGame || plugin.inCountdown) {
                 gameHandler.restoreInventory(player);
                 plugin.players.remove(player);
                 Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.RED + " has left the server! They've been removed from the current game.");
                 if (plugin.numOfPlayers == 2) {
                     Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are not enough players left to continue. The game has ended.");
                     gameHandler.cleanUpGame();
                 } else {
                     plugin.numOfPlayers--;
                     if (gameHandler.checkVictory()) {
                         gameHandler.victory();
                     }
                 }
             }
         }
     }
     
     @Override
     public void onPlayerKick(PlayerKickEvent event) {
         Player player = event.getPlayer();
         if (plugin.players.containsKey(player)) {
             if (plugin.inRegistration) {
                 gameHandler.restoreInventory(player);
                 plugin.players.remove(player);
                 Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.RED + " has been kicked from the server! They've been un-registered from the current game.");
                 if (plugin.numOfPlayers == 1) {
                     Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are no more players left in the game! The game has ended.");
                     plugin.inRegistration = false;
                 }
                 plugin.numOfPlayers--;
                 
             } else if (plugin.inGame) {
                 gameHandler.restoreInventory(player);
                 plugin.players.remove(player);
                 Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + " has been kicked from the server! They've been removed from the current game.");
                 if (plugin.numOfPlayers == 2) {
                     Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are not enough players left to continue. The game has ended.");
                     gameHandler.cleanUpGame();
                 } else {
                     plugin.numOfPlayers--;
                     if (gameHandler.checkVictory()) {
                         gameHandler.victory();
                     }
                 }
             }
         }
     }
     
     @Override
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         if (!plugin.inGame && !plugin.inRegistration) return;
         
         if (!plugin.players.containsKey(event.getPlayer())) return;
         
         if (plugin.inRegistration) {
             if (!config.lobby) return;
             
             int[][] arr = new int[2][3];
             try {
                 String[] p1 = config.lobby_area1.split(",");
                 String[] p2 = config.lobby_area2.split(",");
                 arr = Arena.parseMinMax(p1, p2);
             } catch (NumberFormatException e) {
                 e.printStackTrace();
             }
             Random random = new Random();
             int xGap = (arr[1][0] - arr[0][0]) - 2;
             int toX = (arr[0][0] + 1) + random.nextInt(xGap);
             int zGap = (arr[1][2] - arr[0][2]) - 2;
             int toZ = (arr[0][2] + 1) + random.nextInt(zGap);
             int toY = arr[0][1] + 1;
             World w = Bukkit.getServer().getWorld(config.lobby_world);
             while (w.getBlockAt(toX, toY, toZ).getType() == Material.AIR) {
                 toY = toY - 1;
             }
             while (w.getBlockAt(toX, toY -1, toZ).getType() == Material.WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_LAVA || w.getBlockAt(toX, toY -1, toZ).getType() == Material.LAVA) {
                 toX = arr[0][0] + random.nextInt(arr[1][0] - arr[0][0]);
                 toZ = arr[0][2] + random.nextInt(arr[1][2] - arr[0][2]);
             }
 
             while (w.getBlockAt(toX, toY, toZ).getType() != Material.AIR) {
                 toY = toY + 1;
             }
             event.setRespawnLocation(new Location(w, toX, toY, toZ));
         } else if (plugin.inGame) {
             if (!config.arena) return;
             
             int[][] arr = new int[2][3];
             try {
                 String[] p1 = config.arena_area1.split(",");
                 String[] p2 = config.arena_area2.split(",");
                 arr = Arena.parseMinMax(p1, p2);
             } catch (NumberFormatException e) {
                 e.printStackTrace();
             }
             Random random = new Random();
             int xGap = (arr[1][0] - arr[0][0]) - 2;
             int toX = (arr[0][0] + 1) + random.nextInt(xGap);
             int zGap = (arr[1][2] - arr[0][2]) - 2;
             int toZ = (arr[0][2] + 1) + random.nextInt(zGap);
             int toY = arr[0][1] + 1;
             World w = Bukkit.getServer().getWorld(config.arena_world);
             while (w.getBlockAt(toX, toY -1, toZ).getType() == Material.WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_LAVA || w.getBlockAt(toX, toY -1, toZ).getType() == Material.LAVA) {
                 toX = arr[0][0] + random.nextInt(arr[1][0] - arr[0][0]);
                 toZ = arr[0][2] + random.nextInt(arr[1][2] - arr[0][2]);
             }
 
             while (w.getBlockAt(toX, toY, toZ).getType() != Material.AIR) {
                 toY = toY + 1;
             }
             event.setRespawnLocation(new Location(w, toX, toY, toZ));
             event.getPlayer().getInventory().addItem(new ItemStack(config.item, 1));
         }
     }
     
 }
