 package de.blablubbabc.billboards;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Billboards extends JavaPlugin {
 
 	public static Billboards instance;
 	public static Logger logger;
 	public static Economy economy = null;
 	public static String ADMIN_PERMISSION = "billboards.admin";
 	public static String RENT_PERMISSION = "billboards.rent";
 	public static String CREATE_PERMISSION = "billboards.create";
 	
 	public static String trimTo16(String input) {
		return input.length() > 16 ? input.substring(0, 16) : input;
 	}
 	
 	private int defaultPrice = 10;
 	private int defaultDurationDays = 7;
 	
 	public Map<String, BillboardSign> customers = new HashMap<String, BillboardSign>();
 	
 	private List<BillboardSign> signs = new ArrayList<BillboardSign>();
 	
 	@Override
 	public void onEnable() {
 		instance = this;
 		logger = getLogger();
 		if (!setupEconomy()) {
 			logger.severe("No economy plugin was found! Disables now!");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		// load messages
 		Messages.loadMessages("plugins" + File.separator + "Billboards" + File.separator + "messages.yml");
 		
 		// load config and signs:
 		loadConfig();
 		
 		// register listener
 		getServer().getPluginManager().registerEvents(new EventListener(), this);
 		
 		// start refresh timer:
 		getServer().getScheduler().runTaskTimer(this, new Runnable() {
 			
 			@Override
 			public void run() {
 				refreshAllSigns();
 			}
 		}, 5L, 20L * 60 * 10);
 	}
 	
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		instance = null;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage(Messages.getMessage(Message.ONLY_AS_PLAYER));
 			return true;
 		}
 		Player player = (Player) sender;
 		if (!player.hasPermission(CREATE_PERMISSION)) {
 			player.sendMessage(Messages.getMessage(Message.NO_PERMISSION));
 			return true;
 		}
 		if (args.length > 3) {
 			return false;
 		}
 		
 		Block block = player.getTargetBlock(null, 10);
 		if (block == null || !(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
 			player.sendMessage(Messages.getMessage(Message.NO_TARGETED_SIGN));
 		} else {
 			Location loc = block.getLocation();
 			if (getBillboard(loc) != null) {
 				player.sendMessage(Messages.getMessage(Message.ALREADY_BILLBOARD_SIGN));
 			} else {
 				int duration = defaultDurationDays;
 				int price = defaultPrice;
 				
 				// /billboard [<price> <duration>] [creator]
 				if (args.length >= 2) {
 					Integer priceArgument = parseInteger(args[0]);
 					if (priceArgument == null) {
 						player.sendMessage(Messages.getMessage(Message.INVALID_NUMBER, args[0]));
 						return true;
 					}
 					Integer durationArgument = parseInteger(args[1]);
 					if (durationArgument == null) {
 						player.sendMessage(Messages.getMessage(Message.INVALID_NUMBER, args[1]));
 						return true;
 					}
 					price = priceArgument.intValue();
 					duration = durationArgument.intValue();
 				}
 				
 				String creator = null;
 				if (args.length == 1) {
 					creator = args[0];
 				} else if (args.length == 3) {
 					creator = args[2];
 				}
 				
 				BillboardSign billboard = new BillboardSign(new SoftLocation(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), creator, null, duration, price, 0);
 				signs.add(billboard);
 				refreshSign(billboard);
 				saveCurrentConfig();
 				
 				player.sendMessage(Messages.getMessage(Message.ADDED_SIGN));
 			}
 		}
 		return true;
 	}
 	
 	private Integer parseInteger(String string) {
 		try {
 			return Integer.parseInt(string);
 		} catch(NumberFormatException e) {
 			return null;
 		}
 	}
 	
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 		
 		return (economy != null);
 	}
 	
 	public void removeBillboard(BillboardSign billboard) {
 		signs.remove(billboard);
 		saveCurrentConfig();
 	}
 	
 	public BillboardSign getBillboard(Location loc) {
 		for (BillboardSign billboard : signs) {
 			if (billboard.getLocation().isSameLocation(loc)) return billboard;
 		}
 		return null;
 	}
 	
 	// return true if the sign is still valid
 	public boolean refreshSign(BillboardSign billboard) {
 		if (!signs.contains(billboard)) {
 			logger.warning("Billboard '" + billboard.getLocation().toString() + "' is no longer an valid billboard sign, but was refreshed.");
 			return false;
 		}
 		
 		Location location = billboard.getLocation().getBukkitLocation(this);
 		if (location == null) {
 			logger.warning("World '" + billboard.getLocation().getWorldName() + "' not found. Removing this billboard sign.");
 			removeBillboard(billboard);
 			return false;
 		}
 		
 		Block block = location.getBlock();
 		Material type = block.getType();
 		if (type != Material.WALL_SIGN && type != Material.SIGN_POST) {
 			logger.warning("Billboard '" + billboard.getLocation().toString() + "' is no longer a sign. Removing this billboard sign.");
 			removeBillboard(billboard);
 			return false;
 		}
 		
 		// check rent time if it has an owner:
 		if (billboard.hasOwner() && billboard.isRentOver()) {
 			billboard.resetOwner();
 		}
 		// update text if it has no owner:
 		if (!billboard.hasOwner()) {
 			Sign sign = (Sign) block.getState();
 			setRentableText(billboard, sign);
 		}
 		
 		return true;
 	}
 	
 	private void setRentableText(BillboardSign billboard, Sign sign) {	
 		sign.setLine(0, trimTo16(Messages.getMessage(Message.SIGN_LINE_1)));
 		sign.setLine(1, trimTo16(Messages.getMessage(Message.SIGN_LINE_2)));
 		sign.setLine(2, trimTo16(Messages.getMessage(Message.SIGN_LINE_3, String.valueOf(billboard.getPrice()))));
 		sign.setLine(3, trimTo16(Messages.getMessage(Message.SIGN_LINE_4, String.valueOf(billboard.getDurationInDays()))));
 		sign.update();
 	}
 	
 	public void refreshAllSigns() {
 		List<BillboardSign> forRemoval = new ArrayList<BillboardSign>();
 		for (BillboardSign billboard : signs) {
 			Location location = billboard.getLocation().getBukkitLocation(this);
 			if (location == null) {
 				logger.warning("World '" + billboard.getLocation().getWorldName() + "' not found. Removing this billboard sign.");
 				forRemoval.add(billboard);
 				continue;
 			}
 			Block block = location.getBlock();
 			if (!(block.getState() instanceof Sign)) {
 				logger.warning("Billboard sign '" + billboard.getLocation().toString() + "' is no longer a sign. Removing this billboard sign.");
 				forRemoval.add(billboard);
 				continue;
 			}
 			
 			// check rent time if has owner:
 			if (billboard.hasOwner() && billboard.isRentOver()) {
 				billboard.resetOwner();
 			}
 			// update text if has no owner:
 			if (!billboard.hasOwner()) {
 				Sign sign = (Sign) block.getState();
 				setRentableText(billboard, sign);
 			}
 			
 		}
 		// remove invalid billboards:
 		if (forRemoval.size() > 0) {
 			for (BillboardSign billboard : forRemoval) {
 				signs.remove(billboard);
 			}
 			saveCurrentConfig();
 		}
 	}
 	
 	public void loadConfig() {
 		FileConfiguration config = getConfig();
 		
 		// load settings:
 		ConfigurationSection settingsSection = config.getConfigurationSection("Settings");
 		if (settingsSection != null) {
 			defaultPrice = settingsSection.getInt("DefaultPrice", 10);
 			defaultDurationDays = settingsSection.getInt("DefaultDurationInDays", 7);
 		}
 		
 		// load signs:
 		ConfigurationSection signsSection = config.getConfigurationSection("Signs");
 		if (signsSection != null) {
 			for (String softString : signsSection.getKeys(false)) {
 				
 				ConfigurationSection signSection = signsSection.getConfigurationSection(softString);
 				if (signSection == null) {
 					logger.warning("Couldn't load a sign section: " + softString);
 					continue;
 				}
 				
 				SoftLocation soft = SoftLocation.getFromString(softString);
 				if (soft == null) {
 					logger.warning("Couldn't load a signs location: " + softString);
 					continue;
 				}
 				
 				String creator = signSection.getString("Creator", null);
 				String owner = signSection.getString("Owner", null);
 				int durationInDays = signSection.getInt("Duration", defaultDurationDays);
 				int price = signSection.getInt("Price", defaultPrice);
 				long startTime = signSection.getLong("StartTime", 0L);
 				
 				signs.add(new BillboardSign(soft, creator, owner, durationInDays, price, startTime));
 			}
 		}
 		
 		// write changes back to config:
 		saveCurrentConfig();
 		
 	}
 	
 	public void saveCurrentConfig() {
 		FileConfiguration config = getConfig();
 		
 		// write settings to config:
 		config.set("Settings.DefaultPrice", defaultPrice);
 		config.set("Settings.DefaultDurationInDays", defaultDurationDays);
 		
 		// write signs to config:
 		// first clear signs section:
 		config.set("Signs", null);
 		// then insert current information:
 		for (BillboardSign billboard : signs) {
 			String node = "Signs." + billboard.getLocation().toString();
 			config.set(node + ".Creator", billboard.getCreator());
 			config.set(node + ".Owner", billboard.getOwner());
 			config.set(node + ".Duration", billboard.getDurationInDays());
 			config.set(node + ".Price", billboard.getPrice());
 			config.set(node + ".StartTime", billboard.getStartTime());
 		}
 		
 		saveConfig();
 	}
 }
