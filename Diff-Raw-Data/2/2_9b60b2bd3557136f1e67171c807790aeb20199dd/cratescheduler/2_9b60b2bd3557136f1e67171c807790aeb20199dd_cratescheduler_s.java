 package tv.mineinthebox.ManCo.events;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitTask;
 import tv.mineinthebox.ManCo.manCo;
 import tv.mineinthebox.ManCo.configuration.configuration;
 import tv.mineinthebox.ManCo.utils.normalCrate;
 import tv.mineinthebox.ManCo.utils.normalCrateList;
 import tv.mineinthebox.ManCo.utils.rareCrate;
 import tv.mineinthebox.ManCo.utils.rareCrateList;
 import tv.mineinthebox.ManCo.utils.util;
 import tv.mineinthebox.ManCo.utils.vanish;
 import tv.mineinthebox.ManCo.utils.worldguard;
 
 public class cratescheduler {
 
 	public static BukkitTask task;
 	public static BukkitTask task2;
 
 	public static void startScheduler() {
 		BukkitTask taskID = Bukkit.getScheduler().runTaskTimer(manCo.getPlugin(), new Runnable() {
 
 			@Override
 			public void run() {
 				if(Bukkit.getOnlinePlayers().length > configuration.roundsPerTime()) {
 					for(int i = 0; i < configuration.roundsPerTime(); i++) {
 						if(util.isWorldGuardEnabled()) {
 							doCrateWithWG();
 						} else {
 							doCrateWithoutWG();
 						}
 					}
 				}
 			}
 
 		}, 0, configuration.getTime());
 		task = taskID;
 	}
 
 	public static void startRareScheduler() {
 		BukkitTask taskID = Bukkit.getScheduler().runTaskTimer(manCo.getPlugin(), new Runnable() {
 
 			@Override
 			public void run() {
 				if(Bukkit.getOnlinePlayers().length > configuration.roundsPerTime()) {
 					for(int i = 0; i < configuration.roundsPerTime(); i++) {
 						if(util.isWorldGuardEnabled()) {
 							doRareCrateWithWG();
 						} else {
 							doRareCrateWithoutWG();
 						}
 					}
 				}
 			}
 
		}, 0, configuration.getTime()+15);
 		task2 = taskID;
 	}
 
 	public static void doRareCrateWithoutWG() {
 		//lets crawl through all avaible rare chests!
 		int maxRareCrates = rareCrate.getRareCrateList().size();
 		Random rand = new Random();
 		int RandomCrate = rand.nextInt(maxRareCrates) + 0;
 		if(rareCrate.getRareCrateList().get(RandomCrate) != null) {
 			//now lets do the chance!
 			int getCrateChance = rareCrate.getRareCrateChance(rareCrate.getRareCrateList().get(RandomCrate));
 			Random rand2 = new Random();
 			int crateChance = rand2.nextInt(100) + 1;
 			if(crateChance <= getCrateChance) {
 				//chance success
 				Random rarerand = new Random();
 				int i = Bukkit.getOnlinePlayers().length;
 				if(i > 0) {
 					int playerID = rarerand.nextInt(i) + 0;
 					Player p = Bukkit.getOnlinePlayers()[playerID];
 					if(rareCrateList.rareCrates.containsKey(p.getName()) || normalCrateList.getFallingStateChest.containsValue(p.getName()) || rareCrateList.getCrateList.containsKey(p.getName()) || rareCrateList.getCrateList2.containsKey(p.getName()) || normalCrateList.getCrateList.containsKey(p.getName()) || normalCrateList.getCrateList2.containsKey(p.getName()) || vanish.isVanished(p)) {
 
 						//player already has a crate, loop through all possible online players to see if they can get a crate.
 						//then use the native method.
 
 						for(Player p2 : Bukkit.getOnlinePlayers()) {
 							if(!(rareCrateList.rareCrates.containsKey(p2.getName()) || normalCrateList.getFallingStateChest.containsValue(p2.getName()) ||rareCrateList.getCrateList.containsKey(p2.getName()) || rareCrateList.getCrateList2.containsKey(p2.getName()) || normalCrateList.getCrateList.containsKey(p2.getName()) || normalCrateList.getCrateList2.containsKey(p2.getName()) || vanish.isVanished(p2))) {
 								if(!p2.getName().equalsIgnoreCase(p.getName())) {
 									doRareCrateNative(p2, RandomCrate);
 									break;
 								}
 							}
 						}
 					} else {
 
 						//Random player doesn't have a crate so we can proceed the code:)
 
 						Location loc = p.getLocation();
 						if(configuration.spawnCrateNearby()) {
 							Random randx = new Random();
 							Random randz = new Random();
 							loc.setX(loc.getX() + (randx.nextDouble() + 16));
 							loc.setZ(loc.getZ() + (randz.nextDouble() + 16));
 							loc.setY(normalCrate.getCrateSpawnHeight(p));
 						} else {
 							loc.setY(normalCrate.getCrateSpawnHeight(p));	
 						}
 						Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 						rareCrateList.getFallingStateChest.put(entity, p.getName()+","+rareCrate.getRareCrateList().get(RandomCrate));	
 						rareCrateList.rareCrates.put(p.getName(), rareCrate.getRareCrateList().get(RandomCrate));
 						Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + rareCrate.getCrateFoundMessage(rareCrate.getRareCrateList().get(RandomCrate)).replace("%p", p.getName()));	
 					}
 				}
 			}
 		}
 	}
 
 	public static void doRareCrateWithWG() {
 		//lets crawl through all avaible rare chests!
 		int maxRareCrates = rareCrate.getRareCrateList().size();
 		Random rand = new Random();
 		int RandomCrate = rand.nextInt(maxRareCrates) + 0;
 		if(rareCrate.getRareCrateList().get(RandomCrate) != null) {
 			//now lets do the chance!
 			int getCrateChance = rareCrate.getRareCrateChance(rareCrate.getRareCrateList().get(RandomCrate));
 			Random rand2 = new Random();
 			int crateChance = rand2.nextInt(100) + 0;
 			if(crateChance <= getCrateChance) {
 				//chance success
 				Random rarerand = new Random();
 				int i = Bukkit.getOnlinePlayers().length;
 				if(i > 0) {
 					int playerID = rarerand.nextInt(i) + 0;
 					Player p = Bukkit.getOnlinePlayers()[playerID];
 					if(rareCrateList.rareCrates.containsKey(p.getName()) || normalCrateList.getFallingStateChest.containsValue(p.getName()) || rareCrateList.getCrateList.containsKey(p.getName()) || rareCrateList.getCrateList2.containsKey(p.getName()) || normalCrateList.getCrateList.containsKey(p.getName()) || normalCrateList.getCrateList2.containsKey(p.getName()) || !worldguard.canPlayerBuild(p) || vanish.isVanished(p)) {
 						//player already has a crate, loop through all possible online players to see if they can get a crate.
 						//then use the native method.
 						for(Player p2 : Bukkit.getOnlinePlayers()) {
 							if(!(rareCrateList.rareCrates.containsKey(p2.getName()) || normalCrateList.getFallingStateChest.containsValue(p2.getName()) || rareCrateList.getCrateList.containsKey(p2.getName()) || rareCrateList.getCrateList2.containsKey(p2.getName()) || normalCrateList.getCrateList.containsKey(p2.getName()) || normalCrateList.getCrateList2.containsKey(p2.getName()) || vanish.isVanished(p2) || !worldguard.canPlayerBuild(p2))) {
 								if(!p2.getName().equalsIgnoreCase(p.getName())) {
 									doRareCrateNative(p2, RandomCrate);
 									break;
 								}
 							}
 						}
 					} else {
 
 						//Random player doesn't have a crate so we can proceed the code:)
 
 						Location loc = p.getLocation();
 						if(configuration.spawnCrateNearby()) {
 							Random randx = new Random();
 							Random randz = new Random();
 							loc.setX(loc.getX() + (randx.nextDouble() + 16));
 							loc.setZ(loc.getZ() + (randz.nextDouble() + 16));
 							loc.setY(normalCrate.getCrateSpawnHeight(p));
 						} else {
 							loc.setY(normalCrate.getCrateSpawnHeight(p));	
 						}
 						Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 						rareCrateList.getFallingStateChest.put(entity, p.getName()+","+rareCrate.getRareCrateList().get(RandomCrate));
 						rareCrateList.rareCrates.put(p.getName(), rareCrate.getRareCrateList().get(RandomCrate));
 						Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + rareCrate.getCrateFoundMessage(rareCrate.getRareCrateList().get(RandomCrate)).replace("%p", p.getName()));	
 					}
 				}
 			}
 		}
 	}
 
 	public static void doCrateWithoutWG() {
 		Random rand = new Random();
 		int i = Bukkit.getOnlinePlayers().length;
 		if(i > 0) {
 			int playerID = rand.nextInt(i) + 0;
 			Player p = Bukkit.getOnlinePlayers()[playerID];
 			if(rareCrateList.rareCrates.containsKey(p.getName()) || normalCrateList.getFallingStateChest.containsValue(p.getName()) || normalCrateList.getCrateList.containsKey(p.getName()) || normalCrateList.getCrateList2.containsKey(p.getName()) || rareCrateList.getCrateList.containsKey(p.getName()) || rareCrateList.getCrateList2.containsKey(p.getName()) || vanish.isVanished(p)) {
 
 				//player already has a crate, loop through all possible online players to see if they can get a crate.
 				//then use the native method.
 
 				for(Player p2 : Bukkit.getOnlinePlayers()) {
 					if(!(rareCrateList.rareCrates.containsKey(p2.getName()) || normalCrateList.getFallingStateChest.containsValue(p2.getName()) || normalCrateList.getCrateList.containsKey(p2.getName()) || normalCrateList.getCrateList2.containsKey(p2.getName()) || rareCrateList.getCrateList.containsKey(p2.getName()) || rareCrateList.getCrateList2.containsKey(p2.getName()) || vanish.isVanished(p2))) {
 						if(!p2.getName().equalsIgnoreCase(p.getName())) {
 							doCrateNative(p2);
 							break;
 						}
 					}
 				}
 			} else {
 
 				//Random player doesn't have a crate so we can proceed the code:)
 
 				Location loc = p.getLocation();
 				if(configuration.spawnCrateNearby()) {
 					Random randx = new Random();
 					Random randz = new Random();
 					loc.setX(loc.getX() + (randx.nextDouble() + 16));
 					loc.setZ(loc.getZ() + (randz.nextDouble() + 16));
 					loc.setY(normalCrate.getCrateSpawnHeight(p));
 				} else {
 					loc.setY(normalCrate.getCrateSpawnHeight(p));	
 				}
 				Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 				normalCrateList.getFallingStateChest.put(entity, p.getName());
 				Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + normalCrate.getCrateFoundMessage().replace("%p", p.getName()));	
 			}
 		}
 	}
 
 	public static void doCrateWithWG() {
 		Random rand = new Random();
 		int i = Bukkit.getOnlinePlayers().length;
 		if(i > 0) {
 			int playerID = rand.nextInt(i) + 0;
 			Player p = Bukkit.getOnlinePlayers()[playerID];
 			if(rareCrateList.rareCrates.containsKey(p.getName()) || normalCrateList.getFallingStateChest.containsValue(p.getName()) || rareCrateList.getFallingStateChest.containsValue(p.getName()) || normalCrateList.getCrateList.containsKey(p.getName()) || normalCrateList.getCrateList2.containsKey(p.getName()) || rareCrateList.getCrateList.containsKey(p.getName()) || rareCrateList.getCrateList2.containsKey(p.getName()) || vanish.isVanished(p) || !worldguard.canPlayerBuild(p)) {
 				//player already has a crate, loop through all possible online players to see if they can get a crate.
 				//then use the native method.
 				for(Player p2 : Bukkit.getOnlinePlayers()) {
 					if(!(rareCrateList.rareCrates.containsKey(p2.getName()) || normalCrateList.getFallingStateChest.containsValue(p2.getName()) || normalCrateList.getCrateList.containsKey(p2.getName()) || normalCrateList.getCrateList2.containsKey(p2.getName()) || rareCrateList.getCrateList.containsKey(p2.getName()) || rareCrateList.getCrateList2.containsKey(p2.getName()) || vanish.isVanished(p2) || !worldguard.canPlayerBuild(p2))) {
 						if(!p2.getName().equalsIgnoreCase(p.getName())) {
 							doCrateNative(p2);
 							break;
 						}
 					}
 				}
 			} else {
 
 				//Random player doesn't have a crate so we can proceed the code:)
 
 				Location loc = p.getLocation();
 				if(configuration.spawnCrateNearby()) {
 					Random randx = new Random();
 					Random randz = new Random();
 					loc.setX(loc.getX() + (randx.nextDouble() + 16));
 					loc.setZ(loc.getZ() + (randz.nextDouble() + 16));
 					loc.setY(normalCrate.getCrateSpawnHeight(p));
 				} else {
 					loc.setY(normalCrate.getCrateSpawnHeight(p));	
 				}
 				Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 				normalCrateList.getFallingStateChest.put(entity, p.getName());
 				Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + normalCrate.getCrateFoundMessage().replace("%p", p.getName()));	
 			}
 		}
 	}
 
 	public static void doCrateNative(Player p) {
 		Location loc = p.getLocation();
 		if(configuration.spawnCrateNearby()) {
 			Random randx = new Random();
 			Random randz = new Random();
 			loc.setX(loc.getX() + (randx.nextDouble() + 16));
 			loc.setZ(loc.getZ() + (randz.nextDouble() + 16));
 			loc.setY(normalCrate.getCrateSpawnHeight(p));
 		} else {
 			loc.setY(normalCrate.getCrateSpawnHeight(p));	
 		}
 		Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 		normalCrateList.getFallingStateChest.put(entity, p.getName());
 		Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + normalCrate.getCrateFoundMessage().replace("%p", p.getName()));
 	}
 
 	public static void doRareCrateNative(Player p, int RandomCrate) {
 		Location loc = p.getLocation();
 		if(configuration.spawnCrateNearby()) {
 			Random randx = new Random();
 			Random randz = new Random();
 			loc.setX(loc.getX() + (randx.nextDouble() + 16));
 			loc.setZ(loc.getZ() + (randz.nextDouble() + 16));
 			loc.setY(normalCrate.getCrateSpawnHeight(p));
 		} else {
 			loc.setY(normalCrate.getCrateSpawnHeight(p));	
 		}
 		Entity entity = p.getWorld().spawnFallingBlock(loc, Material.CHEST, (byte) 1);
 		rareCrateList.getFallingStateChest.put(entity, p.getName()+","+rareCrate.getRareCrateList().get(RandomCrate));
 		rareCrateList.rareCrates.put(p.getName(), rareCrate.getRareCrateList().get(RandomCrate));
 		Bukkit.broadcastMessage(ChatColor.GREEN + "[ManCo] " + rareCrate.getCrateFoundMessage(rareCrate.getRareCrateList().get(RandomCrate)).replace("%p", p.getName()));
 	}
 
 }
