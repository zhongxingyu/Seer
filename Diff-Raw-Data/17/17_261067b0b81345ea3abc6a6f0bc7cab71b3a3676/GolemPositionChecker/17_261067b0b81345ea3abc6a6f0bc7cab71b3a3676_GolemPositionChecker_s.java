 package me.cmastudios.ironball.task;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.BukkitUtil;
 import com.sk89q.worldedit.regions.Region;
 import me.cmastudios.ironball.Game;
 import me.cmastudios.ironball.IronBall;
 import me.cmastudios.ironball.TeamType;
 import org.bukkit.entity.IronGolem;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Task to check position of Iron Golem. This task checks for goals and illegal
  * golem positions.
  *
  * @author Connor
  */
 public class GolemPositionChecker extends BukkitRunnable {
 
     private final Game game;
     private final IronBall plugin;
 
     public GolemPositionChecker(Game game, IronBall plugin) {
         this.plugin = plugin;
         this.game = game;
     }
 
     public void run() {
         if (!plugin.activeGames.contains(game)) {
             // Game ended
             return;
         }
         IronGolem golem = game.getGolem();
         if (golem == null) {
             // Cancel task until a new GolemSpawnTask is run. This may occur if
             // there is a respawn delay.
             game.getTasks().remove(this).cancel();
             return;
         }
         if (golem.isDead()) {
             Player killer = golem.getKiller();
             game.setGolem(null);
             if (plugin.getConfig().getBoolean("point.killgolem") && killer != null && game.isPlaying(killer)) {
                 TeamType team = game.getTeam(killer);
                 if (game.addPoint(team.getOtherTeam(), 1)) {
                     return;
                 }
             }
             plugin.getServer().getScheduler().runTaskLater(plugin, new GolemSpawnTask(game, plugin), plugin.getConfig().getInt("golem.respawndelay"));
             return;
         }
         Vector loc = BukkitUtil.toVector(golem.getLocation());
         Region region = game.getArena().getRegion();
         if (!region.contains(loc)) {
             // We lost the golem, so lets kill him and spawn a new one
             golem.remove();
             if (plugin.getConfig().getBoolean("point.outofbounds") && golem.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                 EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) golem.getLastDamageCause();
                 if (event.getDamager() instanceof Player) {
                     Player damager = (Player) event.getDamager();
                     if (game.isPlaying(damager)) {
                         if (game.addPoint(game.getTeam(damager).getOtherTeam(), 1)) {
                             return;
                         }
                     }
                 }
             }
             plugin.getServer().getScheduler().runTaskLater(plugin, new GolemSpawnTask(game, plugin), plugin.getConfig().getInt("golem.respawndelay"));
             return;
         }
         Region redGoal = game.getArena().getRedGoalRegion();
         Region blueGoal = game.getArena().getBlueGoalRegion();
         if (redGoal.contains(loc)) {
             golem.remove();
             if (plugin.getConfig().getBoolean("point.goal") && game.addPoint(TeamType.BLUE, 1)) {
                 return;
             }
             plugin.getServer().getScheduler().runTaskLater(plugin, new GolemSpawnTask(game, plugin), plugin.getConfig().getInt("golem.respawndelay"));
             return;
         }
         if (blueGoal.contains(loc)) {
             golem.remove();
             if (plugin.getConfig().getBoolean("point.goal") && game.addPoint(TeamType.RED, 1)) {
                 return;
             }
             plugin.getServer().getScheduler().runTaskLater(plugin, new GolemSpawnTask(game, plugin), plugin.getConfig().getInt("golem.respawndelay"));
             return;
         }
     }
 }
