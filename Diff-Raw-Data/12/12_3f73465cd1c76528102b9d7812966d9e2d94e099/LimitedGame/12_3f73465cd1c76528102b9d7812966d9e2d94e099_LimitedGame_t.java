 package me.mothma.limitedgame;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LimitedGame extends JavaPlugin implements Listener {
 	
 	private Logger log;
 	
 	private FileConfiguration config;
 	private GamePlayerFile gamePlayerData;
 	private LocationFile locationFile;	
 	
 	private Location lobbySpawn;
 	private Location arenaSpawn;
 	
 	boolean autostart;
 	int autostartMin = 2;
 	int autostopMin = 1;		
 	
 	@Override
 	public void onEnable() {
 		log = getLogger();
 		
 		String pluginFolder = getDataFolder().getAbsolutePath();
 		new File(pluginFolder).mkdirs();
 		gamePlayerData = new GamePlayerFile(new File(pluginFolder + File.separator
 				+ "playerdata.txt"));
 		locationFile = new LocationFile(new File(pluginFolder + File.separator
 				+ "stringdata.txt"), getServer());
 		gamePlayerData.load();
 		locationFile.load();
 		lobbySpawn = locationFile.getTable().get("lobby");
 		arenaSpawn = locationFile.getTable().get("arena");
 		
 		//Set up config		
 		File f = new File(this.getDataFolder(), "config.yml");
 	    if (!f.exists()) {
 	        this.saveDefaultConfig();
 		}
 	    config = this.getConfig();
 	    this.autostart = config.getBoolean("general.autostart");
 	    this.autostartMin = config.getInt("general.autostartMin");
 	    this.autostopMin = config.getInt("general.autostopMin");
 		
 		log.info("Limited Game has been enabled!");	
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		log.info("Limited Game has been disabled.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getName().equalsIgnoreCase("lg")) {
 			if (args.length < 1) {
 				return false;
 			}
 			
 			// Find the player if there is one
 			Player player = null;
 			if (sender instanceof Player) {
 				player = (Player) sender;
 			}
 			
 			if (args[0].equalsIgnoreCase("list")) {
 				sender.sendMessage(ChatColor.GREEN + "Players (Red means in lobby):");
 				String list = "";
 				int n = 0;
 				for (GamePlayer p : gamePlayerData.getSet()) {
 					if (p.inLobby()) {
 						list += ChatColor.RED;
 					} else {
 						list += ChatColor.GREEN;
 					}					
 					if (n == gamePlayerData.getSet().size() - 1) {
 						list += p.getName() + ".";
 					} else {
 						list += p.getName() + ", ";
 					}
 					n++;
 				}				
 				sender.sendMessage(list);
 			} else if (args[0].equalsIgnoreCase("setlobby")) {
 				if (player == null) {
 					sender.sendMessage(ChatColor.RED + "You must be a player!");
 				} else {
 					lobbySpawn = player.getLocation();
 					locationFile.getTable().put("lobby", player.getLocation());
 					locationFile.save();
 					player.sendMessage(ChatColor.GREEN + "The lobby spawn has been set to your position!");
 				}
 			} else if (args[0].equalsIgnoreCase("setarena")) {
 				if (player == null) {
 					sender.sendMessage(ChatColor.RED + "You must be a player!");
 				} else {
 					arenaSpawn = player.getLocation();
 					locationFile.getTable().put("arena", player.getLocation());
 					locationFile.save();
 					player.sendMessage(ChatColor.GREEN + "The arena spawn has been set to your position!");
 				}
 			} else if (args[0].equalsIgnoreCase("start")) {
 				getServer().broadcastMessage(ChatColor.GREEN + "Let the games begin!");
 				sender.sendMessage("Forcing a game start");
 				startGame();
 			} else if (args[0].equalsIgnoreCase("stop")) {
 				sender.sendMessage("Forcing a game stop");
 				stopGame();
 			} else if (args[0].equalsIgnoreCase("updateplayers")) {
 				for (Player serverPlayer : getServer().getOnlinePlayers()) {
 					for (GamePlayer p : gamePlayerData.getSet()) {
 						if (p.getName().equals(serverPlayer.getName()) && !p.inLobby()) {
 							Location l = new Location(getServer().getWorld("world"), p.getX(), p.getY(), p.getZ());
 							p.setLocation(l);
 							p.setInventory(serverPlayer.getInventory().getContents());
 							p.setFoodlevel(serverPlayer.getFoodLevel());
 							p.setHealth(serverPlayer.getHealth());
 						}
 					}					
 				}
 				gamePlayerData.save();
 			} else {
 				return false;
 			}
 			return true;
 		}		
 		return false;
 	}
 	
 	private void startGame() {		
 		for (GamePlayer p : gamePlayerData.getSet()) {			
 			Player serverPlayer = getServer().getPlayer(p.getName());
 			if (serverPlayer != null && p.inLobby()) {
 				Location nullLocation = new Location(getServer().getWorld("world"), 0, 0, 0);
 				Location l = new Location(getServer().getWorld("world"), p.getX(), p.getY(), p.getZ());
 				if (l.equals(nullLocation) && arenaSpawn != null) {
 					serverPlayer.teleport(arenaSpawn);
 				} else {
 					serverPlayer.teleport(l);
 					serverPlayer.getInventory().setContents(p.getInventory());
 					serverPlayer.getInventory().setArmorContents(p.getArmor());
 					serverPlayer.setFoodLevel(p.getFoodlevel());
 					serverPlayer.setHealth(p.getHealth());
 				}
 			}			
 			p.setInLobby(false);
 		}
 		gamePlayerData.save();
 	}
 	
 	private void stopGame() {
 		getServer().broadcastMessage(ChatColor.RED + "The games have stopped!");
 		for (GamePlayer p : gamePlayerData.getSet()) {
 			if (!p.inLobby()) {
 				Player serverPlayer = getServer().getPlayer(p.getName());				
 				if (serverPlayer != null) {
 					p.setLocation(serverPlayer.getLocation());
 					p.setInventory(serverPlayer.getInventory().getContents());
 					p.setArmor(serverPlayer.getInventory().getArmorContents());
 					p.setFoodlevel(serverPlayer.getFoodLevel());
 					p.setHealth(serverPlayer.getHealth());
 					if (lobbySpawn != null) {
 						serverPlayer.teleport(lobbySpawn);
 						serverPlayer.getInventory().clear();
 						ItemStack air = new ItemStack(Material.AIR);
 						ItemStack[] armor = {air,air,air,air};
 						serverPlayer.getInventory().setArmorContents(armor);						
 					}
 				}
 			}
 			p.setInLobby(true);
 		}
 		gamePlayerData.save();
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	void playerJoin(PlayerJoinEvent event) {
 		boolean alreadyAdded = false;
 		for (GamePlayer p : gamePlayerData.getSet()) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				alreadyAdded = true;				
 			}
 		}
 		if (!alreadyAdded) {			
 			gamePlayerData.getSet().add(new GamePlayer(event.getPlayer().getName(), true, event.getPlayer().getInventory().getContents(), event.getPlayer().getInventory().getArmorContents(), new Location(getServer().getWorld("world"), 0, 0, 0)));
 		}
 		for (GamePlayer p : gamePlayerData.getSet()) {
 			if (p.getName().equals(event.getPlayer().getName())) {
 				if (p.inLobby() && lobbySpawn != null) {					
 					event.getPlayer().teleport(lobbySpawn);
 				}
 			}
 		}
 		
 		if (getServer().getOnlinePlayers().length >= autostartMin && autostart) {			
 			startGame();
 			if (getServer().getOnlinePlayers().length == autostartMin) {
 				getServer().broadcastMessage(ChatColor.GREEN + "Let the games begin!");
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	void playerQuit(PlayerQuitEvent event) {		
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				int players = getServer().getOnlinePlayers().length - autostopMin;
				if ( (players < 1 && players >-2) && autostart) {
					stopGame();
				}
			}
		}, 20L);	
 	}
 	
 }
