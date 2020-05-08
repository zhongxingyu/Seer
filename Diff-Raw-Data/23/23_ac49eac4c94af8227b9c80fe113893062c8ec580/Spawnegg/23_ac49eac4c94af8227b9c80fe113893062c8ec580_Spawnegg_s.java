 package net.milkycraft;
 
 import java.net.URL;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkycraft.ASEConfiguration.eConfiguration;
 import net.milkycraft.Listeners.EnchantListener;
 import net.milkycraft.Listeners.ExpListener;
 import net.milkycraft.Listeners.MyDispenseListener;
 import net.milkycraft.Listeners.MySpawnEggListener;
 import net.milkycraft.Listeners.SpawnListener;
 import net.milkycraft.Listeners.TargetListener;
 import net.milkycraft.Listeners.ThrowListener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class Spawnegg extends EggWrapper {
 	private eConfiguration config;
 	private double newVersion;
 	private double currentVersion;
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static boolean Thrower = true;
 	public static WorldGuardPlugin worldguardPlugin = null;
 	public static Economy econ = null;
 
 	@Override
 	public void onEnable() {
 		currentVersion = Double.valueOf(getDescription().getVersion()
 				.split("-")[0].replaceFirst("\\.", ""));
 		String ver = this.getDescription().getVersion();
 		setupPluginDependencies();
 		log.info(ChatColor.RED + " EntityManager " + ver + " enabled!");
 		saveConfig();
 		config = new eConfiguration(this);
 		config.create();
 		config.reload();
 		getServer().getPluginManager().registerEvents(new ASEListener(), this);
 		this.getServer().getPluginManager()
 				.registerEvents(new MyDispenseListener(this), this);
 		this.getServer().getPluginManager()
 				.registerEvents(new MySpawnEggListener(this), this);
 		this.getServer().getPluginManager()
 				.registerEvents(new SpawnListener(this), this);
 		this.getServer().getPluginManager()
 				.registerEvents(new TargetListener(this), this);
 		this.getServer().getPluginManager()
 				.registerEvents(new ThrowListener(this), this);
 		this.getServer().getPluginManager()
 		.registerEvents(new ExpListener(this), this);
 		this.getServer().getPluginManager()
 		.registerEvents(new EnchantListener(this), this);
 		
 		// Update alerter 
 		this.getServer().getScheduler()
 				.scheduleAsyncRepeatingTask(this, new Runnable() {
 					@Override
 					public void run() {
 						try {
 							newVersion = updateCheck(currentVersion);
 							if (newVersion > currentVersion) {
 								log.warning("EM " + newVersion
 										+ " is out! You are running: ASE "
 										+ currentVersion);
								log.warning("Update ASE at: http://dev.bukkit.org/server-mods/antispawnegg");
 							}
 						} catch (Exception e) {
 							// ignore exceptions
 						}
 					}
 
 				}, 0, 216000);
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		String ver = this.getDescription().getVersion();
 		if (args.length == 0) {
 			sender.sendMessage(ChatColor.WHITE
 					+ "*************************************");
 			sender.sendMessage(ChatColor.GREEN + "EntityManager "
 					+ ChatColor.YELLOW + ver + ChatColor.GREEN
 					+ " by milkywayz loaded!");
 			sender.sendMessage(ChatColor.GREEN
 					+ "Always check bukkit dev page for latest version");
 			sender.sendMessage(ChatColor.GREEN
 					+ "Sumbit requests on bukkit dev to have them added!");
 			sender.sendMessage(ChatColor.GREEN
 					+ "If you find any issues or bugs, please report them!");
 			sender.sendMessage(ChatColor.WHITE
 					+ "*************************************");
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("entitymanager")) {
 			if (args.length == 2) {
 				if (args[0].equalsIgnoreCase("purge")) {
 					Player target = sender.getServer().getPlayer(args[1]);
 					if (target.getInventory().contains(Material.MONSTER_EGG)) {
 						target.getInventory().remove(Material.MONSTER_EGG);
 					} else if (target.getInventory()
 							.contains(Material.FIREBALL)) {
 						target.getInventory().remove(Material.FIREBALL);
 					} else if (target.getInventory().contains(Material.EGG)) {
 						target.getInventory().remove(Material.EGG);
 					} else if (target.getInventory().contains(
 							Material.EXP_BOTTLE)) {
 						target.getInventory().remove(Material.EXP_BOTTLE);
 					}
 					sender.sendMessage(ChatColor.GREEN + "[EM] "
 							+ ChatColor.GOLD + args[1] + ChatColor.RED
 							+ "'s inventory was purged of: ");
 					sender.sendMessage(ChatColor.GREEN + "[EM] "
 							+ ChatColor.YELLOW
							+ "FireBalls, XpBottles, Eggs and Spawneggs");
 					return true;
 				}
 			}
 		}
 		if (args[0].equalsIgnoreCase("reload")
 				&& sender.hasPermission("entitymanager.admin")) {
 			this.reloadConfig();
 			sender.sendMessage(ChatColor.AQUA + "[EntityManager] "
 					+ ChatColor.GREEN + "Version " + ver + ChatColor.ITALIC
 					+ " Config reloaded from disk");
 			return true;
 		}
 		if (args[0].equalsIgnoreCase("crystal")
 				&& sender.hasPermission("entitymanager.endercrystal")) {
 			if (((Player) sender).getItemInHand().getTypeId() == 383) {
 				((Player) sender).getItemInHand().setAmount(1);
 				((Player) sender).getItemInHand().setDurability((short) 200);
 				sender.sendMessage(ChatColor.AQUA + "[EM] " + ChatColor.GREEN
						+ "Version " 
 						+ " Egg converted to usable EnderCrystal spawnegg");
 				return true;
 			}
 		}
 		if (args.length == 0) {
 			sender.sendMessage(ChatColor.WHITE
 					+ "*************************************");
 			sender.sendMessage(ChatColor.GREEN + "EntityManager "
 					+ ChatColor.YELLOW + ver + ChatColor.GREEN
 					+ " by milkywayz loaded!");
 			sender.sendMessage(ChatColor.GREEN
 					+ "Always check bukkit dev page for latest version");
 			sender.sendMessage(ChatColor.GREEN
 					+ "Sumbit requests on bukkit dev to have them added!");
 			sender.sendMessage(ChatColor.GREEN
 					+ "If you find any issues or bugs, please report them!");
 			sender.sendMessage(ChatColor.WHITE
 					+ "*************************************");
 			return true;
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ " Not enough permission to use that command!");
 			return false;
 		}
 	}
 
 	@Override
 	public void onDisable() {
 		log.info(getDescription().getName() + " "
 				+ getDescription().getVersion() + " unloaded.");
 	}
 
 	private void setupPluginDependencies() {
 		try {
 			setupWorldGuard();
 		} catch (Exception e) {
 			log.warning("[EntityManager] Failed to load WorldGuard");
 			e.printStackTrace();
 		}
 		try {
 			setupEconomy();
 		} catch (Exception e) {
 			log.warning("[EntityManager] Failed to load Vault");
 			e.printStackTrace();
 		}
 	}
 
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			econ = null;
 			log.warning("[EntityManager] Vault not found, economy support disabled");
 		} else {
 			log.info("[EntityManager] Hooked into Vault!");
 		}
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = getServer()
 				.getServicesManager().getRegistration(Economy.class);
 		if (rsp == null) {
 			return false;
 		}
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 
 	private void setupWorldGuard() {
 		Plugin wg = this.getServer().getPluginManager().getPlugin("WorldGuard");
 		if (wg == null) {
 			log.info("[EntityManager] Couldn't hook into worldguard");
 		} else {
 			Spawnegg.worldguardPlugin = (WorldGuardPlugin) wg;
 			log.info("[EntityManager] Hooked into WorldGuard!");
 		}
 	}
 
 	public eConfiguration config() {
 		return config;
 	}
 
 	public double updateCheck(double currentVersion) throws Exception {
		String pluginUrlString = "http://dev.bukkit.org/server-mods/antispawnegg/files.rss";
 		try {
 			URL url = new URL(pluginUrlString);
 			Document doc = DocumentBuilderFactory.newInstance()
 					.newDocumentBuilder()
 					.parse(url.openConnection().getInputStream());
 			doc.getDocumentElement().normalize();
 			NodeList nodes = doc.getElementsByTagName("item");
 			Node firstNode = nodes.item(0);
 			if (firstNode.getNodeType() == 1) {
 				Element firstElement = (Element) firstNode;
 				NodeList firstElementTagName = firstElement
 						.getElementsByTagName("title");
 				Element firstNameElement = (Element) firstElementTagName
 						.item(0);
 				NodeList firstNodes = firstNameElement.getChildNodes();
 				return Double.valueOf(firstNodes.item(0).getNodeValue()
 						.replace("v", "").replaceFirst(".", "").trim());
 			}
 		} catch (Exception localException) {
 		}
 		return currentVersion;
 	}
 
 	public class ASEListener implements Listener {
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		public void onPlayerJoin(PlayerJoinEvent event) {
 			Player player = event.getPlayer();
 			if (player.hasPermission("entitymanager.admin")) {
 				try {
 					if (newVersion > currentVersion) {
 						player.sendMessage(newVersion
 								+ " is out! You are running " + currentVersion);
						player.sendMessage("Update ASE at: http://dev.bukkit.org/server-mods/antispawnegg");
 					}
 				} catch (Exception e) {
 					// Ignore exceptions
 				}
 			}
 		}
 	}
 }
