 package com.hawkfalcon.deathswap.utilities;
 
 import com.hawkfalcon.deathswap.DeathSwap;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class Loc {
 
     public DeathSwap plugin;
 
     public Loc(DeathSwap ds) {
         this.plugin = ds;
     }
 
     public Location getLocation(String cloc) {
         String[] loc = cloc.split("\\,");
         World w = Bukkit.getWorld(loc[0]);
         Double x = Double.parseDouble(loc[1]);
         Double y = Double.parseDouble(loc[2]);
         Double z = Double.parseDouble(loc[3]);
         if (loc.length != 4) {
             float yaw = Float.parseFloat(loc[4]);
             float pitch = Float.parseFloat(loc[5]);
             Location location = new Location(w, x, y, z, yaw, pitch);
             return location;
         }
         Location location = new Location(w, x, y, z);
         return location;
     }
 
    public void randomTeleport(final Player playerone, final Player playertwo) {
         Location lobby = getLocation(plugin.data.getString("lobby_spawn"));
        final Location locone = randomLoc(lobby);
        final Location loctwo = randomLoc(lobby);
         loadLoc(locone);
         loadLoc(loctwo);
        locone.add(0, 2, 0);
        loctwo.add(0, 2, 0);
         if (plugin.getConfig().getBoolean("countdown")) {
             countdown(10, locone, loctwo, playerone, playertwo);
         } else {
             tpPlayer(locone, playerone);
             tpPlayer(loctwo, playertwo);
            new BukkitRunnable() {

                public void run() {
                    playerone.teleport(locone);
                    playertwo.teleport(loctwo);
                }
            }.runTaskLater(plugin, 10L);
         }
     }
 
     public void loadLoc(Location loc) {
         loc.getBlock().getRelative(BlockFace.DOWN).setTypeId(7);
         loc.getChunk().load();
     }
 
     public void tpPlayer(Location loc, Player player) {
         plugin.utility.message("Teleporting, be ready!", player);
         player.teleport(loc);
     }
 
     public void countdown(final int time, final Location locone, final Location loctwo, final Player playerone, final Player playertwo) {
         new BukkitRunnable() {
 
             int count = time;
 
             @Override
             public void run() {
                 if (count < 11 && count > 0) {
                     plugin.utility.message("Teleportation commencing in " + count + " seconds!", playerone);
                     plugin.utility.message("Teleportation commencing in " + count + " seconds!", playertwo);
                 }
                 count--;
                 if (count == 0) {
                     this.cancel();
                     tpPlayer(locone, playerone);
                     tpPlayer(loctwo, playertwo);
                    new BukkitRunnable() {

                        public void run() {
                            playerone.teleport(locone);
                            playertwo.teleport(loctwo);
                        }
                    }.runTaskLater(plugin, 20L);
                 }
             }
 
        }.runTaskTimer(plugin, 0, 10L);
     }
 
     public Location randomLoc(Location center) {
         World world = plugin.getServer().getWorld(plugin.getConfig().getString("world"));
         if (world == null) {
             world = center.getWorld();
         }
         Random rand = new Random();
         int rad = plugin.getConfig().getInt("random_spawn_radius", 10000);
         if (rad < 10000) {
             plugin.utility.broadcast("The radius in the config is too small, using 10000!", true);
             plugin.getLogger().warning("The radius in the config is too small, using to 10000!");
         }
         int apart = plugin.getConfig().getInt("random_spawn_distance_apart", 100);
         int min = -(rad / apart);
         int max = rad / apart;
         double x = 0;
         double y = 0;
         double z = 0;
         Material below = null;
         Material above = null;
         while (true) {
             if (below == null || below == Material.LAVA || below == Material.WATER || below == Material.STATIONARY_WATER || below == Material.BEDROCK || above != Material.AIR) {
                 x = (rand.nextInt(max - min + 1) + min) * apart;
                 z = (rand.nextInt(max - min + 1) + min) * apart;
                 y = world.getHighestBlockAt((int) x, (int) z).getY();
                 below = world.getBlockAt((int) x, (int) y - 1, (int) z).getType();
                 above = world.getBlockAt((int) x, (int) y + 1, (int) z).getType();
             } else {
                 break;
             }
         }
         return new Location(world, x, y, z);
     }
 }
