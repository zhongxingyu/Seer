 package nl.imesiacraft.nlundeadoverrun;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import nl.imesiacraft.nlundeadoverrun.UndeadPlayers.OldPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
 import org.bukkit.craftbukkit.v1_5_R3.entity.CraftZombie;
 import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Zombie;
 import org.bukkit.inventory.EntityEquipment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 import org.bukkit.util.Vector;
 
 public class UndeadOverrun extends JavaPlugin {
 	/* ToDo:
 	 * ScoreBoard:
 	 * wave
 	 * coins
 	 * needKills
 	 */	
 	//list for players that are ingame
 	public List<Player> playing = new ArrayList<Player>();
 	//Logger
 	private Logger log = null;
 	//player inventory store
 	private HashMap<Player, ItemStack[][]> inv_store = new HashMap<Player, ItemStack[][]>();
 	//player location store
 	private HashMap<Player, Location> loc_store = new HashMap<Player, Location>();
 	//player gamemode store
 	private HashMap<Player, String> gm_store = new HashMap<Player, String>();
 	//Ready players store
 	private List<Player> ready = new ArrayList<Player>();
 	//notReady players store
 	private List<Player> notReady = new ArrayList<Player>();
 	//boolean if the game was started
 	public boolean started = false;
 	//Random generator
 	private Random random = new Random();
 	//wave
 	public int wave = 0;
 	//kills to go up waves
 	public int kills = 0;
 	//needed kills for next wave
 	public int needKills = 0;
 	//Boolean if wave is ready
 	public boolean waveReady = false;
 	//score
 	public HashMap<String, Integer> score = new HashMap<String, Integer>();
 	//IconMenu
 	public static IconMenu im;
 	//Scoreboard stuff
 	public ScoreboardManager manager = null;
 	public Scoreboard board = null;
 	public Team team = null;
 	public Objective objective = null;
 	//FireworkEffectPlayer
 	FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
 
 	//circle method
 	public static List<Location> circle (Location loc, Integer r, Integer h, Boolean hollow, Boolean sphere, int plus_y) {
 		List<Location> circleblocks = new ArrayList<Location>();
 		int cx = loc.getBlockX();
 		int cy = loc.getBlockY();
 		int cz = loc.getBlockZ();
 		for (int x = cx - r; x <= cx +r; x++)
 			for (int z = cz - r; z <= cz +r; z++)
 				for (int y = (sphere ? cy - r : cy); y < (sphere ? cy + r : cy + h); y++) {
 					double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
 					if (dist < r*r && !(hollow && dist < (r-1)*(r-1))) {
 						Location l = new Location(loc.getWorld(), x, y + plus_y, z);
 						circleblocks.add(l);
 					}
 				}
 
 		return circleblocks;
 	}
 	//blocks above method
 	public static List<Location> above (Location loc, Integer h) {
 		List<Location> aboveblocks = new ArrayList<Location>();
 		int x = loc.getBlockX();
 		int y = loc.getBlockY();
 		int z = loc.getBlockZ();
 		for(int i = 0; i < h; i++) {
 			int ny = y + i;
 			Location l = new Location(loc.getWorld(), x, ny, z);
 			aboveblocks.add(l);
 		} 	
 		return aboveblocks;
 	}
 	//Disable
 	public void onDisable() {
 		log.info("UndeadOverrun disabled!");	
 	}
 
 	//Enable
 	public void onEnable() {
 		//Scoreboards
 		manager = Bukkit.getScoreboardManager();
 		board = manager.getNewScoreboard();
 		team = board.registerNewTeam("uo");
 		objective = board.registerNewObjective(ChatColor.GRAY + "UndeadOverrun", "dummy");
 		//Listener class
 		EventListener listener = new EventListener(this);
 
 		//logger
 		log = Logger.getLogger("Minecraft");
 
 		log.info("UndeadOverrun enabled!");
 		Bukkit.getPluginManager().registerEvents(listener, this);
 	}
 
 	//Commands
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = (Player) sender;
 		if(commandLabel.equalsIgnoreCase("uo")) {
 			if(args.length == 0) {
 				showHelpMenu(player);
 			} else if(args.length == 1) {
 				if(args[0].equalsIgnoreCase("join")) {
 					if(!started == true) {
 						if(!playing.contains(player)) {
 							player.sendMessage(ChatColor.GRAY + "Successfully joined!");
 							for(Player p : playing) {
 								p.sendMessage(ChatColor.GRAY + player.getName() + " joined!");
 							}
 							playing.add(player);
 							notReady.add(player);
 							team.addPlayer(player);
 							return true;
 						} else {
 							player.sendMessage(ChatColor.GRAY + "You already joined!!");
 							return true;
 						}
 					} else {
 						player.sendMessage(ChatColor.GRAY + "Game already started!");
 						return true;
 					}
 				} else if(args[0].equalsIgnoreCase("leave")) {
 					if(playing.contains(player)) {
 						playing.remove(player);
 						player.sendMessage(ChatColor.GRAY + "You left the game!");
 						if(ready.contains(player)) {
 							ready.remove(player);
 						}
 						if(notReady.contains(player)) {
 							notReady.remove(player);
 						}
 						for(Player p : playing) {
 							p.sendMessage(player.getName() + " has left!");
 						}
 						return true;
 					} else {
 						player.sendMessage(ChatColor.GRAY + "You are not playing!");
 						return true;
 					}
 				} else if(args[0].equalsIgnoreCase("players")) {
 					player.sendMessage(ChatColor.GRAY + "Players: " + playing.size());
 					for(Player p : playing) {
 						player.sendMessage(ChatColor.GRAY + p.getName());
 					}
 				} else if(args[0].equalsIgnoreCase("stats")) {
 					if(started == false) {
 						player.sendMessage(ChatColor.GRAY + "The game hasn't started yet!");
 						return true;
 					} else {
 						player.sendMessage(ChatColor.GRAY + "Wave: " + wave);
 						player.sendMessage(ChatColor.GRAY + "Kills : " + kills);
 						player.sendMessage(ChatColor.GRAY + "Kills needed : " + needKills);
 						return true;
 					}
 				} else if(args[0].equalsIgnoreCase("ready")) {
 					ready.add(player);
 					notReady.remove(player);
 					for(Player p : playing) {
 						p.sendMessage(ChatColor.GRAY + player.getName() + " is ready to kill some zombies!");
 					}
 				} else if(args[0].equalsIgnoreCase("start")) {
 					if(!started == true) {
 						if(playing.contains(player)) {
 							if(notReady.size() == 0) {
 								//World world = Bukkit.getWorld("Imesia");
 								Location startLoc = new Location(player.getWorld(), 2790, 87, -4318);
 								for(Player p : playing) {
 
 									UndeadPlayers.addPlayer(p.getName(), new OldPlayer(p.getLocation(), p.getInventory(), p.getGameMode()));
 									p.setGameMode(GameMode.ADVENTURE);
 									PlayerInventory inv = p.getInventory();
 									
 									ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
 									ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
 									ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
 									ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
 									ItemStack sword = new ItemStack(Material.WOOD_SWORD, 1);
 									ItemStack bow = new ItemStack(Material.BOW, 1);
 									ItemStack arrows = new ItemStack(Material.ARROW, 30);
 									ItemStack food = new ItemStack(Material.BREAD,4);
 									inv.clear();
 									inv.setBoots(boots);
 									inv.setLeggings(leggings);
 									inv.setChestplate(chestplate);
 									inv.setHelmet(helmet);
 									inv.addItem(sword);
 									inv.addItem(bow);
 									inv.addItem(food);
 									inv.addItem(arrows);
 									//create player in score hashmap
 									score.put(p.getName(), 0);
 									//teleport
 									p.teleport(startLoc);
 									//set good and health to full
 									p.setFoodLevel(20);
 									p.setHealth(20);
 									//set the team properties
 									team.setPrefix(ChatColor.GRAY + "[UO]");
 									team.setAllowFriendlyFire(true);
 									team.setCanSeeFriendlyInvisibles(true);
 									//wish the player luck!
 									player.sendMessage(ChatColor.GRAY + "Goodluck!");
 									//start the next wave
 									started = true;
 									//start the updating of the scoreboard
 									updateScoreBoard();
 								}
 								nextWave();
 							} else {
 								player.sendMessage(ChatColor.GRAY + "Not all players are ready!");
 								for(Player p : notReady) {
 									player.sendMessage(ChatColor.GRAY + p.getName());
 								}
 							}
 						}
 					} else {
 						player.sendMessage(ChatColor.GRAY + "Game already started!");
 					}
 				} else if(args[0].equalsIgnoreCase("coins")) {
 					if(playing.contains(player)) {
 						int coins = score.get(player.getName());
 						player.sendMessage(ChatColor.GRAY + "Your coins: " + coins);
 					} else {
 						player.sendMessage(ChatColor.GRAY + "You are not playing!");
 					}
 				} else if(args[0].equalsIgnoreCase("quit")) {
 					if(playing.contains(player)) {
 						
 						PlayerInventory inv = player.getInventory();
 						inv.clear();
 						
 						UndeadPlayers.restorePlayer(player);
 						
 						playing.remove(player);
 						if(playing.isEmpty()) {
 							started = false;
 							Bukkit.broadcastMessage(ChatColor.GRAY + "UndeadOverrun game finished!");
 							Bukkit.broadcastMessage(ChatColor.GRAY + "Wave reached: " + wave);
 							wave = 0;
 							score.clear();
 							removeMobs();
 						}	
 						return true;
 					} else {
 						player.sendMessage(ChatColor.GRAY + "You are not playing!");
 						return true;
 					}
 				} else if(args[0].equalsIgnoreCase("")) {
 
 				}
 			} else if(args.length == 2 || args.length > 2) {
 				player.sendMessage(ChatColor.GRAY + "Too many arguments! Use /uo");
 			}
 		}
 		return false;
 	}
 	public void saveInv(Player p){
 		ItemStack[] [] store = new ItemStack[2][1];
 		store[0] = p.getInventory().getContents();
 		store[1] = p.getInventory().getArmorContents();
 		this.inv_store.put(p, store);
 	}
 
 
 	public void restoreInv(Player p){
 		p.getInventory().clear();
 		p.getInventory().setContents(this.inv_store.get(p)[0]);
 		p.getInventory().setArmorContents(this.inv_store.get(p)[1]);
 		this.inv_store.remove(p);
 		//Isn't needed anymore.
 		//p.updateInventory();
 	}
 	public void safeLoc(Player p){
 		Location loc = p.getLocation();
 		this.loc_store.put(p, loc);
 	}
 	public void restoreLoc(Player p){
 		Location loc = this.loc_store.get(p);
 		p.teleport(loc);
 	}
 	public void saveGamemode(Player p) {
 		if(p.getGameMode() == GameMode.SURVIVAL) {
 			gm_store.put(p, "s");
 		} else if(p.getGameMode() == GameMode.CREATIVE) {
 			gm_store.put(p, "c");
 		}
 	}
 	public void restoreGamemode(Player p) {
         if(gm_store.containsKey(p)) {
     		String gm = gm_store.get(p);
     		if (gm.equals("s")) {
     			p.setGameMode(GameMode.SURVIVAL);
     		} else if(gm.equals("c")) {
     			p.setGameMode(GameMode.CREATIVE);
     		}	
         }
 	}
 
 	private Vector getSpawnLoc(World world) {
 		int dx = random.nextInt(2836-2771);
 		int dz = random.nextInt(Math.abs(-4356+4292));
 
 		//worden ook gespawnd in het kasteel nu.
 		int x = 2771 + dx;
 		int z = -4356 + dz;
 		int y = world.getHighestBlockYAt(x, z) + 1;
 
 		Vector vector = new Vector(x, y, z);
 
 		for (Player player : playing) {
 			if (player.getLocation().toVector().distance(vector) < 10) {
 				return getSpawnLoc(world);
 			}
 		}	
 		return vector;
 	}
 
 	public void nextWave() {
 		//Vector vMin = new Vector(2771, 0,-4356);
 		//Vector vMax = new Vector(2836, 0,-4292);
 		waveReady = false;
 		kills = 0;
 		needKills = 0;
 		World world = Bukkit.getWorld("Imesia");
 		world.setTime(13000);
 		/*Zombie spawn locations:
 		 *  dichtbij kasteel:
 		 *     2782, 83, -4340
 		 *     2803, 83, -4342
 		 *     2832, 81, -4340
 		 *     2836, 80, -4310
 		 *     2828, 82, -4292
 		 *     2805, 86, -4289
 		 *     2790, 91, -4300
 		 *     2786, 87, -4326   
 		 */
 		//next wave
 		wave++;
 
 		//Kills needed will be set to int killsneeded, int rounds down.
 		double killsNeeded = 0.0;
 		for (int i = 1; i < wave * 10; i++) {
 			Vector v = getSpawnLoc(world);
 			int x = v.getBlockX();
 			int y = v.getBlockY();
 			int z = v.getBlockZ();
 
 			for (int j = 0; j < playing.size(); j++) {
 				try {
 					fplayer.playFirework(world, new Location(world, x, y, z), FireworkEffect.builder().with(Type.BURST).withColor(Color.FUCHSIA).build());
 				} catch (Exception exc) {	
 				}	
 				Entity ent = world.spawnEntity(new Location(world, x, y, z), EntityType.ZOMBIE);
 				if (wave > 20) {
 					((Zombie)ent).setCustomName("Hell Zombie");
 					((CraftZombie)ent).getHandle().setEquipment(0, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.DIAMOND_SWORD.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(1, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.DIAMOND_BOOTS.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(2, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.DIAMOND_LEGGINGS.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(3, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.DIAMOND_ARMOR.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(4, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.DIAMOND_HELMET.getMaterial())));
 				} else if (wave > 10) {
 					((Zombie)ent).setCustomName("Death Zombie");
 					((CraftZombie)ent).getHandle().setEquipment(0, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.IRON_SWORD.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(1, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.IRON_BOOTS.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(2, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.IRON_LEGGINGS.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(3, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.IRON_ARMOR.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(4, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.IRON_HELMET.getMaterial())));
 				} else if (wave > 5) {
 					((Zombie)ent).setCustomName("Strong Zombie");
 					((CraftZombie)ent).getHandle().setEquipment(0, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.STONE_SWORD.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(1, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.LEATHER_BOOTS.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(2, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.LEATHER_LEGGINGS.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(3, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.LEATHER_ARMOR.getMaterial())));
 					((CraftZombie)ent).getHandle().setEquipment(4, CraftItemStack.asNMSCopy(new ItemStack(ItemTypes.LEATHER_HELMET.getMaterial())));
 				} else {
 					((Zombie)ent).setCustomName("Zombie");
 				}
 				EntityEquipment eq = ((CraftLivingEntity) ent).getEquipment();
 				eq.setBootsDropChance(0.0F);
 				eq.setLeggingsDropChance(0.0F);
 				eq.setChestplateDropChance(0.0F);
 				eq.setHelmetDropChance(0.0F);
 				eq.setItemInHandDropChance(0.0F);
 				((Zombie)ent).setCustomNameVisible(true);
 				//50% needs to be killed
 				killsNeeded += 0.5D;
 			}
 		}
 		needKills = (int)killsNeeded;
 
 		for(Player pl : playing) {
 			if(!score.containsKey(pl.getName())) {
 				score.put(pl.getName(), 0);
 			}
 			int coins = score.get(pl.getName());
 			pl.sendMessage(ChatColor.GRAY + "Wave " + wave);
 			pl.sendMessage(ChatColor.GRAY + "Kills needed for next wave: " + needKills);
 			pl.sendMessage(ChatColor.GRAY + "Coins: " + coins);
 		}
 
 		if(wave % 5 == 0) {
 			for(Player player : playing) {
 				player.sendMessage(ChatColor.GRAY + "Special Wave!");
 			}
 			Location spec = new Location(world, 2806, 75, -4341);
 			world.spawnEntity(spec, EntityType.WITCH);
 		}
 	}	
 
 	public void giveReward(Player p) {
 		if(wave < 3 || wave == 3) {
 			p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
 			p.sendMessage(ChatColor.GRAY + "You earned a gold ingot!");
 		} else if(wave > 3 && wave < 5 || wave == 5) {
 			p.getInventory().addItem(new ItemStack(Material.IRON_INGOT));
 			p.sendMessage(ChatColor.GRAY + "You earned a iron ingot!"); 
 		} else if(wave > 5 && wave < 10 || wave == 10) {
 			p.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 4));
 			p.sendMessage(ChatColor.GRAY + "You earned four iron ingots!"); 
 		} else if(wave > 10) {
 			p.getInventory().addItem(new ItemStack(Material.DIAMOND, 2));
 			p.sendMessage(ChatColor.GRAY + "You earned two diamonds!"); 
 		}	
 	}
 	public void removeMobs() {
 		Location loc = new Location(Bukkit.getWorld("Imesia"), 2812, 89, -4315);
 		List<Entity> entities = loc.getWorld().getEntities();
 		Vector ownVector = loc.toVector();
 		for (Entity entity : entities) {
 			if(entity.getLocation().toVector().distance(ownVector) < 80) {
 				if(entity instanceof LivingEntity) {
 					if(entity instanceof Player){	
 					} else {
 						((LivingEntity) entity).damage(40);
 					}
 				}
 			}
 		}		
 	}
 	public void updateScoreBoard() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {	
              if(started == true) {
          		for(Player p : playing) {
         			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         			objective.setDisplayName(ChatColor.GRAY + "UndeadOverrun");
         		    Score swave = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Wave:"));	
         		    swave.setScore(wave);
         		    Score coins = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Coins:"));
         		    coins.setScore(score.get(p.getName()));
         		    Score needed = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Kills left:"));
         		    needed.setScore(needKills - kills);
         		    p.setScoreboard(board);
         		}	 
              } else {
             	 Bukkit.getServer().getScheduler().cancelTask(0); 
             	 board.clearSlot(DisplaySlot.SIDEBAR);
              }
 			}	
     	}, 20, 20);   
 	}
 
 	public void showHelpMenu(Player player) {
 		player.sendMessage(ChatColor.GRAY + "--------UndeadOverrun--------");
 		player.sendMessage(ChatColor.GRAY + "Commands:");
 		player.sendMessage(ChatColor.GRAY + "/uo join - To join the game");
 		player.sendMessage(ChatColor.GRAY + "/uo leave - To leave the game when you joined");
 		player.sendMessage(ChatColor.GRAY + "/uo players - To see the players that are in the game");
 		player.sendMessage(ChatColor.GRAY + "/uo stats - To get the stats of the current game");
 		player.sendMessage(ChatColor.GRAY + "/uo ready - To set yourself ready");
 		player.sendMessage(ChatColor.GRAY + "/uo start - To start the game");
 		player.sendMessage(ChatColor.GRAY + "Commands while playing:");
 		player.sendMessage(ChatColor.GRAY + "/uo coins - To get your amount of coins");
 		player.sendMessage(ChatColor.GRAY + "/uo quit - To quit the game");	
 	}
 	
 	public void checkGame() {
 		if(playing.isEmpty()) {
 			started = false;
 			Bukkit.broadcastMessage(ChatColor.GRAY + "UndeadOverrun game finished!");
 			Bukkit.broadcastMessage(ChatColor.GRAY + "Wave reached: " + wave);
 			wave = 0;
 			score.clear();
 			removeMobs();
 		}	
 	}
 }
