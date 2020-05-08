 package tv.mineinthebox.ManCo.events;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitTask;
 
 import tv.mineinthebox.ManCo.chestList;
 import tv.mineinthebox.ManCo.manCo;
 import tv.mineinthebox.ManCo.configuration.configuration;
 
 public class cratescheduler {
 
 	public static BukkitTask task;
 	
 	public static void startScheduler() {
 		BukkitTask taskID = Bukkit.getScheduler().runTaskTimer(manCo.getPlugin(), new Runnable() {
 
 			@Override
 			public void run() {
				if(Bukkit.getOnlinePlayers().length > configuration.roundsPerTime()) {
 					for(int i = 0; i < configuration.roundsPerTime(); i++) {
 						doCrate();
 					}
 				}
 			}
 
 		}, 0, configuration.getTime());
 		task = taskID;
 	}
 	
 	public static void doCrate() {
 		Random rand = new Random();
 		int i = Bukkit.getOnlinePlayers().length;
 		if(i > 0) {
 			int playerID = rand.nextInt(i) + 0;
 			Player p = Bukkit.getOnlinePlayers()[playerID];
 			if(chestList.getCrateList.containsKey(p.getName()) || chestList.getCrateList2.containsKey(p.getName())) {
 				//player allready has a chest!
 			} else {
 				Location loc = p.getLocation();
 				loc.setY(120);
 				Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 				chestList.getFallingStateChest.put(entity, p.getName());
 				Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + ChatColor.GRAY + p.getName() + " found a ManCo crate!");	
 			}
 		}
 	}
 
 }
