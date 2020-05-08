 /*  This file is part of TheGaffer.
  * 
  *  TheGaffer is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TheGaffer is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with TheGaffer.  If not, see <http://www.gnu.org/licenses/>.
  */
 package co.mcme.thegaffer.listeners;
 
 import co.mcme.thegaffer.storage.Job;
 import co.mcme.thegaffer.storage.JobDatabase;
 import co.mcme.thegaffer.utilities.PermissionsUtil;
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 
 public class ProtectionListener implements Listener {
 
     @EventHandler
     public void onPlace(BlockPlaceEvent event) {
         if (event.getPlayer().hasPermission(PermissionsUtil.getIgnoreWorldProtection())) {
             event.setCancelled(false);
         } else {
             World world = event.getBlock().getWorld();
             if (!JobDatabase.getActiveJobs().isEmpty()) {
                 HashMap<Job, World> workingworlds = new HashMap();
                 HashMap<Job, Rectangle2D> areas = new HashMap();
                 for (Job job : JobDatabase.getActiveJobs().values()) {
                     workingworlds.put(job, job.getBukkitWorld());
                     areas.put(job, job.getBounds());
                 }
                 if (workingworlds.containsValue(world)) {
                     boolean playerisworking = false;
                     for (Job job : workingworlds.keySet()) {
                         if (job.isPlayerWorking(event.getPlayer())) {
                             playerisworking = true;
                         }
                     }
                     if (playerisworking) {
                         boolean isinjobarea = false;
                         int x = event.getBlock().getX();
                         int z = event.getBlock().getZ();
                         for (Job job : JobDatabase.getActiveJobs().values()) {
                             if (job.isPlayerWorking(event.getPlayer()) && job.getBounds().contains(x, z)) {
                                 isinjobarea = true;
                             }
                         }
                         if (isinjobarea) {
                             event.setCancelled(false);
                         } else {
                             event.getPlayer().sendMessage(ChatColor.RED + "You have gone out of bounds for the job.");
                             event.setCancelled(true);
                         }
                     } else {
                         event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are not part of any job.");
                         event.setBuild(false);
                     }
                 } else {
                     event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are not allowed to build in this world.");
                     event.setBuild(false);
                 }
             } else {
                 event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are not allowed to build when there are no jobs.");
                 event.setBuild(false);
             }
         }
     }
 
     @EventHandler
     public void onBreak(BlockBreakEvent event) {
         if (event.getPlayer().hasPermission(PermissionsUtil.getIgnoreWorldProtection())) {
             event.setCancelled(false);
         } else {
             World world = event.getBlock().getWorld();
             if (!JobDatabase.getActiveJobs().isEmpty()) {
                 HashMap<Job, World> workingworlds = new HashMap();
                 HashMap<Job, Rectangle2D> areas = new HashMap();
                 for (Job job : JobDatabase.getActiveJobs().values()) {
                     workingworlds.put(job, job.getBukkitWorld());
                     areas.put(job, job.getBounds());
                 }
                 if (workingworlds.containsValue(world)) {
                     boolean playerisworking = false;
                     for (Job job : workingworlds.keySet()) {
                         if (job.isPlayerWorking(event.getPlayer())) {
                             playerisworking = true;
                         }
                     }
                     if (playerisworking) {
                         boolean isinjobarea = false;
                         int x = event.getBlock().getX();
                         int z = event.getBlock().getZ();
                         for (Job job : JobDatabase.getActiveJobs().values()) {
                             if (job.isPlayerWorking(event.getPlayer()) && job.getBounds().contains(x, z)) {
                                 isinjobarea = true;
                             }
                         }
                         if (isinjobarea) {
                             event.setCancelled(false);
                         } else {
                             event.getPlayer().sendMessage(ChatColor.RED + "You have gone out of bounds for the job.");
                             event.setCancelled(true);
                         }
                     } else {
                         event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are not part of any job.");
                         event.setCancelled(true);
                     }
                 } else {
                     event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are not allowed to build in this world.");
                     event.setCancelled(true);
                 }
             } else {
                 event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are not allowed to build when there are no jobs.");
                 event.setCancelled(true);
             }
         }
     }
     
     @EventHandler
     public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
             Player player = (Player) event.getRemover();
             if (player.hasPermission(PermissionsUtil.getIgnoreWorldProtection())) {
                 event.setCancelled(false);
             } else {
                 World world = event.getEntity().getWorld();
                 if (!JobDatabase.getActiveJobs().isEmpty()) {
                     HashMap<Job, World> workingworlds = new HashMap();
                     HashMap<Job, Rectangle2D> areas = new HashMap();
                     for (Job job : JobDatabase.getActiveJobs().values()) {
                         workingworlds.put(job, job.getBukkitWorld());
                         areas.put(job, job.getBounds());
                     }
                     if (workingworlds.containsValue(world)) {
                         boolean playerisworking = false;
                         for (Job job : workingworlds.keySet()) {
                             if (job.isPlayerWorking(player)) {
                                 playerisworking = true;
                             }
                         }
                         if (playerisworking) {
                             boolean isinjobarea = false;
                             double x = event.getEntity().getLocation().getX();
                             double z = event.getEntity().getLocation().getZ();
                             for (Job job : JobDatabase.getActiveJobs().values()) {
                                 if (job.isPlayerWorking(player) && job.getBounds().contains(x, z)) {
                                     isinjobarea = true;
                                 }
                             }
                             if (isinjobarea) {
                                 event.setCancelled(false);
                             } else {
                                 player.sendMessage(ChatColor.RED + "You have gone out of bounds for the job.");
                                 event.setCancelled(true);
                             }
                         } else {
                             player.sendMessage(ChatColor.DARK_RED + "You are not part of any job.");
                             event.setCancelled(true);
                         }
                     } else {
                         player.sendMessage(ChatColor.DARK_RED + "You are not allowed to build in this world.");
                         event.setCancelled(true);
                     }
                 } else {
                     player.sendMessage(ChatColor.DARK_RED + "You are not allowed to build when there are no jobs.");
                     event.setCancelled(true);
                 }
             }
         }
     }
     
     @EventHandler
     public void onHangingPlace(HangingPlaceEvent event) {
         Player player = (Player) event.getPlayer();
         if (player.hasPermission(PermissionsUtil.getIgnoreWorldProtection())) {
             event.setCancelled(false);
         } else {
             World world = event.getEntity().getWorld();
             if (!JobDatabase.getActiveJobs().isEmpty()) {
                 HashMap<Job, World> workingworlds = new HashMap();
                 HashMap<Job, Rectangle2D> areas = new HashMap();
                 for (Job job : JobDatabase.getActiveJobs().values()) {
                     workingworlds.put(job, job.getBukkitWorld());
                     areas.put(job, job.getBounds());
                 }
                 if (workingworlds.containsValue(world)) {
                     boolean playerisworking = false;
                     for (Job job : workingworlds.keySet()) {
                         if (job.isPlayerWorking(player)) {
                             playerisworking = true;
                         }
                     }
                     if (playerisworking) {
                         boolean isinjobarea = false;
                         double x = event.getEntity().getLocation().getX();
                         double z = event.getEntity().getLocation().getZ();
                         for (Job job : JobDatabase.getActiveJobs().values()) {
                             if (job.isPlayerWorking(player) && job.getBounds().contains(x, z)) {
                                 isinjobarea = true;
                             }
                         }
                         if (isinjobarea) {
                             event.setCancelled(false);
                         } else {
                             player.sendMessage(ChatColor.RED + "You have gone out of bounds for the job.");
                             event.setCancelled(true);
                         }
                     } else {
                         player.sendMessage(ChatColor.DARK_RED + "You are not part of any job.");
                         event.setCancelled(true);
                     }
                 } else {
                     player.sendMessage(ChatColor.DARK_RED + "You are not allowed to build in this world.");
                     event.setCancelled(true);
                 }
             } else {
                 player.sendMessage(ChatColor.DARK_RED + "You are not allowed to build when there are no jobs.");
                 event.setCancelled(true);
             }
         }
     }
 }
