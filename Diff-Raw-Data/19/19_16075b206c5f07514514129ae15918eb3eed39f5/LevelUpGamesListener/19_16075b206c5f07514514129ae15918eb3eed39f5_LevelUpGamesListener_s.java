 package us.manniL.xapfeltortexp.LevelUpGames.listeners;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import us.manniL.xapfeltortexp.LevelUpGames.LevelUpGamesMain;
 
 public class LevelUpGamesListener implements Listener {
 	private LevelUpGamesMain plugin;
 
 	public LevelUpGamesListener(LevelUpGamesMain plugin) {
 		this.plugin = LevelUpGamesMain.Instance;
 	}
 
 	@EventHandler
 	public void onDeath(EntityDeathEvent e) {
 
 	}
 
 	@EventHandler
 	public void onQuit(PlayerQuitEvent e) {
 		if (plugin.players.contains(e.getPlayer().getName())) {
 			e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
 			plugin.limit--;
 			plugin.players.remove(e.getPlayer().getName());
 		}
 	}
 
 	@EventHandler
 	public void onMove(PlayerMoveEvent event) {
 		final Player player = (Player) event.getPlayer();
 		if (plugin.players.contains(player.getName()) && player.getLocation().getBlockY() <= 1) {
 			int r = plugin.getConfig().getInt("RandomSpawnRadius");
 			Random r1 = new Random();
 			Random r2 = new Random();
 			int z = r1.nextInt(r);
 			int x = r2.nextInt(r);
 			int sign1 = r1.nextInt(2);
 			int sign2 = r2.nextInt(2);
 			if (sign1 == 1) {
 				z = -(z);
 			}
 			if (sign2 == 1) {
 				x = -(x);
 			}
 			player.teleport(new Location(Bukkit.getWorld(plugin.getConfig().getString("RandomSpawn.w")), x, plugin.getConfig().getDouble("RandowmSpawn.y"), z));
 		}
 	}
 
 	@EventHandler
 	public void onFoodLevel(FoodLevelChangeEvent event) {
 		final Player player = (Player) event.getEntity();
		if(plugin.players.contains(player.getName())) {
 			event.setCancelled(true);
 		}
 	}
 }
