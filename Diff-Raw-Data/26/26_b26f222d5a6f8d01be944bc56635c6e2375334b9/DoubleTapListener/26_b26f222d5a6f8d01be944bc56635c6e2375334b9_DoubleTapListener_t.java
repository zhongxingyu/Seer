 package info.mdcraft.parkour.listener;
 
 import java.util.ArrayList;
 
 import info.mdcraft.parkour.manager.EffectsManager;
 import info.mdcraft.parkour.manager.LocationManager;
 import info.mdcraft.parkour.manager.SettingsManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerToggleFlightEvent;
 import org.bukkit.plugin.Plugin;
 
 public class DoubleTapListener implements Listener{
 	
 	LocationManager l = new LocationManager();
 	SettingsManager s = SettingsManager.getInstance();
 	EffectsManager em =  new EffectsManager();
 	public Plugin plugin;
 	
 	ArrayList <Player> p = new ArrayList<Player>();
 	static ArrayList<Player> fly = new ArrayList<Player>();
 	
 	public DoubleTapListener(Plugin i) {
 		plugin = i;
 	}
 
 	@EventHandler
	public void onPlayerDoubleTap(PlayerToggleFlightEvent event){
 		Player pl = event.getPlayer();
 		if (fly.contains(pl)){
 			return;
 		}
 		if(getHunger(pl) < 20) {
 			event.setCancelled(true);
 			pl.playSound(pl.getLocation(), Sound.ANVIL_LAND, 5 ,5);
 			return;
 		}
 		em.launchPlayer(1, pl);
 		pl.setFoodLevel(getHunger(pl) - 20);
 		pl.playSound(pl.getLocation(), Sound.ENDERDRAGON_WINGS, 10 ,10);
 		pl.setAllowFlight(false);
 		pl.setFlying(false);
 		event.setCancelled(true);
 		return;
 	}
 	
 	@EventHandler
 	public void onPlayerJoinDouble(PlayerJoinEvent e) {
 		Player player = e.getPlayer();
 		player.setAllowFlight(true);
 
 	}
 	
 	@EventHandler
 	public void onPlayerFlyCommand(PlayerCommandPreprocessEvent e){
 		Player p = e.getPlayer();
		String[] cmd = e.getMessage().split(" ");
 		if (cmd[0].equalsIgnoreCase("/fly")){
 			if (p.isOp()){
 				if (fly.contains(p)){
 					fly.remove(p);
 					p.sendMessage(ChatColor.GREEN +  "Fly mode Disabled!");
 					return;
 				}
 				fly.add(p);
 				p.sendMessage(ChatColor.GREEN + "Fly mode Enabled!");
 				return;
 			}
 			return;
 		}
 	}
 	
 	
 	public void startHungerRegen(Plugin plugin){
 			Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 				
 				@Override
 				public void run() {
 					for (Player pl : Bukkit.getOnlinePlayers()){
 					int h = getHunger(pl);
					if (h >= 19){
 						pl.setFoodLevel(20);
						pl.setAllowFlight(true);
 					}else {
					int t = h + 4;
 					pl.setFoodLevel(t);
 					}
 					}
 				}
 			}, 5, 5);
 	}
 	
 	
 	
 	public int getHunger(Player pl){
 		int h = pl.getFoodLevel();
 		if (h < 0){
 			pl.setFoodLevel(0);
 			return 0;
 		}else if (h == 0){
 			pl.setFoodLevel(0);
 			return 0;
 		}else if (h > 20){
 			pl.setFoodLevel(20);
 			return 20;
		}else { 
			return h;
		}
 	}
 	
	
	
 	public void removeHunger(Player pl){
 		int c = getHunger(pl);
 		pl.setFoodLevel(c - 6);
 	}
 }
 
 
