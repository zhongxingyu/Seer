 package com.theminequest.bukkit;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.apache.commons.io.FileUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.WorldType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 
 import com.alta189.simplesave.exceptions.ConnectionException;
 import com.theminequest.api.Managers;
 import com.theminequest.api.Platform;
 import com.theminequest.api.platform.IChatColor;
 import com.theminequest.api.platform.IChatStyle;
 import com.theminequest.api.platform.MQBlock;
 import com.theminequest.api.platform.MQEvent;
 import com.theminequest.api.platform.MQInventory;
 import com.theminequest.api.platform.MQItemStack;
 import com.theminequest.api.platform.MQLocation;
 import com.theminequest.api.platform.MQMaterial;
 import com.theminequest.api.platform.MQPlayer;
 import com.theminequest.api.statistic.LogStatistic;
 import com.theminequest.api.statistic.SnapshotStatistic;
 import com.theminequest.api.statistic.StatisticManager;
 import com.theminequest.api.util.PropertiesFile;
 import com.theminequest.bukkit.frontend.cmd.MineQuestCommandFrontend;
 import com.theminequest.bukkit.frontend.cmd.PartyCommandFrontend;
 import com.theminequest.bukkit.frontend.cmd.QuestCommandFrontend;
 import com.theminequest.bukkit.frontend.sign.QuestSign;
 import com.theminequest.bukkit.group.BukkitGroupManager;
 import com.theminequest.bukkit.impl.event.CollectEvent;
 import com.theminequest.bukkit.impl.event.DestroyEvent;
 import com.theminequest.bukkit.impl.event.KillEvent;
 import com.theminequest.bukkit.impl.event.LockWorldTimeEvent;
 import com.theminequest.bukkit.impl.event.NRCollectEvent;
 import com.theminequest.bukkit.impl.event.RewardDamagedEvent;
 import com.theminequest.bukkit.impl.event.RewardEnchantedEvent;
 import com.theminequest.bukkit.impl.event.RewardMoneyEvent;
 import com.theminequest.bukkit.impl.event.RewardPermEvent;
 import com.theminequest.bukkit.impl.requirement.ItemInHandRequirement;
 import com.theminequest.bukkit.impl.requirement.LevelRequirement;
 import com.theminequest.bukkit.impl.requirement.MoneyRequirement;
 import com.theminequest.bukkit.impl.requirement.PermissionRequirement;
 import com.theminequest.bukkit.impl.requirement.TimeRequirement;
 import com.theminequest.bukkit.impl.requirement.WeatherRequirement;
 import com.theminequest.bukkit.platform.BukkitBlock;
 import com.theminequest.bukkit.platform.BukkitEvent;
 import com.theminequest.bukkit.platform.BukkitInventory;
 import com.theminequest.bukkit.platform.BukkitItemStack;
 import com.theminequest.bukkit.platform.BukkitLocation;
 import com.theminequest.bukkit.platform.BukkitMaterial;
 import com.theminequest.bukkit.platform.BukkitPlayer;
 import com.theminequest.bukkit.quest.BukkitQuestManager;
 import com.theminequest.bukkit.quest.handler.BukkitQuestHandlerManager;
 import com.theminequest.bukkit.statistic.Statistics;
 import com.theminequest.bukkit.util.TimeUtils;
 import com.theminequest.common.Common;
 import com.theminequest.common.quest.requirement.CommonRequirementManager;
 import com.theminequest.common.quest.v1.V1EventManager;
 import com.theminequest.common.util.ExceptionHandler;
 
 public class BukkitPlatform extends JavaPlugin implements Platform {
 	
 	private PropertiesFile config;
 	
 	public static final int NETHER_FLAG = 0x00000001;
 	public static final int END_FLAG = 0x00000002;
 	public static final int ENV_FLAGS = NETHER_FLAG | END_FLAG;
 	
 	public static final int FLAT_FLAG = 0x00000010;
 	public static final int LARGE_BIOME_FLAG = 0x00000020;
 	public static final int TYPE_FLAGS = FLAT_FLAG | LARGE_BIOME_FLAG;
 	
 	private Permission permission;
 	private Economy economy;
 	
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null)
 			permission = permissionProvider.getProvider();
 		return (permission != null);
 	}
 	
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null)
 			economy = economyProvider.getProvider();
 		return (economy != null);
 	}
 	
 	public Permission getPermission() {
 		return permission;
 	}
 	
 	public Economy getEconomy() {
 		return economy;
 	}
 	
 	@Override
 	public void onDisable() {
 		
 		try {
 			StatisticManager statisticManager = Managers.getStatisticManager();
 			if (statisticManager != null)
 				statisticManager.connect(false);
 		} catch (ConnectionException e) {
 			e.printStackTrace();
 		}
 
 		Managers.setQuestManager(null);
 		Managers.setGroupManager(null);
 		Managers.setQuestHandlerManager(null);
 		Managers.setRequirementManager(null);
 		
 		if (Common.getCommon() != null)
 			Common.getCommon().stopCommon();
 		
 		Managers.setPlatform(null);
 		HandlerList.unregisterAll(this);
 	}
 	
 	@Override
 	public void onEnable() {
 		
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			getServer().getLogger().severe("================= MineQuest ==================");
 			getServer().getLogger().severe("Vault is required for MineQuest to operate!");
 			getServer().getLogger().severe("Please install Vault first!");
 			getServer().getLogger().severe("You can find the latest version here:");
 			getServer().getLogger().severe("http://dev.bukkit.org/server-mods/vault/");
 			getServer().getLogger().severe("==============================================");
 			setEnabled(false);
 			return;
 		}
 		
 		if (!getDataFolder().exists())
 			getDataFolder().mkdirs();
 		
 		config = new PropertiesFile(getDataFolder().getAbsolutePath() + File.separator + "config.properties");
 		
 		Managers.setVersion(getDescription().getVersion());
 		Managers.setPlatform(this);
 		
 		// Try starting the statistics manager
 		try {
 			Statistics s = new Statistics();
 			// Register
 			s.registerStatistic(LogStatistic.class);
 			s.registerStatistic(SnapshotStatistic.class);
 			// Then set
 			Managers.setStatisticManager(s);
 			Managers.setQuestStatisticManager(s);
 		} catch (ConnectionException e) {
 			Managers.logf(Level.SEVERE, "[Statistic] Can't start statistic manager: %s", e);
 			setEnabled(false);
 			return;
 		}
 		
 		// version check
 		if (Managers.getVersion().equals("unofficialDev")) {
 			Managers.log(Level.SEVERE, "[Core] You're using an unofficial dev build!");
 			Managers.log(Level.SEVERE, "[Core] We cannot provide support for this unless you know the GIT hash.");
 		}
 		
 		ExceptionHandler.init();
 		Common.setCommon(new Common());
 		
 		// Common version check
 		if (Common.getCommon().getVersion().equals("unofficialDev")) {
 			Managers.log(Level.SEVERE, "[Common] You're using an unofficial dev build!");
 			Managers.log(Level.SEVERE, "[Common] We cannot provide support for this unless you know the GIT hash.");
 		}
 		
 		try {
 			new Metrics(this).start();
 		} catch (IOException e) {
 			Managers.logf(Level.WARNING, "[Metrics] unable to start metrics: %s", e);
 		}
 		
 		// Try setting the group manager
 		BukkitGroupManager groupMgr = new BukkitGroupManager();
 		Managers.setGroupManager(groupMgr);
 		Bukkit.getPluginManager().registerEvents(groupMgr, this);
 		
 		// Setup the Quest Handlers
 		BukkitQuestHandlerManager questHandlerManager = new BukkitQuestHandlerManager();
 		Managers.setQuestHandlerManager(questHandlerManager);
 		
 		// Setup the Quest Manager
 		BukkitQuestManager questManager = new BukkitQuestManager();
 		Managers.setQuestManager(questManager);
 		Bukkit.getPluginManager().registerEvents(questManager, this);
 		
 		// Setup the Requirement Manager
 		CommonRequirementManager requireManager = new CommonRequirementManager();
 		Managers.setRequirementManager(requireManager);
 		
 		// leftovers: add in bukkit specific events
 		V1EventManager v1eventmgr = Common.getCommon().getV1EventManager();
 		v1eventmgr.addEvent("CollectEvent", CollectEvent.class);
 		v1eventmgr.addEvent("DestroyEvent", DestroyEvent.class);
 		v1eventmgr.addEvent("KillEvent", KillEvent.class);
 		v1eventmgr.addEvent("LockWorldTimeEvent", LockWorldTimeEvent.class);
 		v1eventmgr.addEvent("NRCollectEvent", NRCollectEvent.class);
 		v1eventmgr.addEvent("RewardDamagedEvent", RewardDamagedEvent.class);
 		v1eventmgr.addEvent("RewardEnchantedEvent", RewardEnchantedEvent.class);
 		v1eventmgr.addEvent("RewardMoneyEvent", RewardMoneyEvent.class);
 		v1eventmgr.addEvent("RewardPermEvent", RewardPermEvent.class);
 		
 		// leftovers: add in bukkit specific requirements
 		requireManager.register("ItemInHandRequirement", ItemInHandRequirement.class);
 		requireManager.register("LevelRequirement", LevelRequirement.class);
 		requireManager.register("MoneyRequirement", MoneyRequirement.class);
 		requireManager.register("PermissionRequirement", PermissionRequirement.class);
 		requireManager.register("TimeRequirement", TimeRequirement.class);
 		requireManager.register("WeatherRequirement", WeatherRequirement.class);
 		
 		// leftovers: vault
 		if (!setupPermissions())
 			Managers.log(Level.SEVERE, "[Vault] You don't seem to have any permissions plugin...");
 		if (!setupEconomy())
 			Managers.log(Level.SEVERE, "[Vault] You don't seem to have any economy plugin...");
 		
 		// leftovers: sign frontend
 		getServer().getPluginManager().registerEvents(new QuestSign(), this);
 		
 		// leftovers: command frontend
 		MineQuestCommandFrontend fe = new MineQuestCommandFrontend();
		getCommand("mp").setExecutor(fe);
 		getCommand("minequest").setExecutor(fe);
 		getCommand("quest").setExecutor(new QuestCommandFrontend());
 		getCommand("party").setExecutor(new PartyCommandFrontend());
 		
 		// leftovers: queue quest loading
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			@Override
 			public void run() {
 				Managers.getQuestManager().reloadQuests();
 				try {
 					Managers.getStatisticManager().connect(true);
 				} catch (ConnectionException e) {
 					Managers.log(Level.SEVERE, "[Core] Can't start Statistic Manager!");
 					e.printStackTrace();
 					Bukkit.getPluginManager().disablePlugin(BukkitPlatform.this);
 				}
 			}
 		});
 		
 	}
 	
 	@Override
 	public File getResourceDirectory() {
 		return getDataFolder();
 	}
 	
 	@Override
 	public PropertiesFile getConfigurationFile() {
 		return config;
 	}
 	
 	@Override
 	public File getJarFile() {
 		return getFile();
 	}
 	
 	@Override
 	public Object getPlatformObject() {
 		return this;
 	}
 	
 	@Override
 	public IChatColor chatColor() {
 		return new IChatColor() {
 			
 			@Override
 			public String BLACK() {
 				return ChatColor.BLACK + "";
 			}
 			
 			@Override
 			public String DARK_BLUE() {
 				return ChatColor.DARK_BLUE + "";
 			}
 			
 			@Override
 			public String DARK_GREEN() {
 				return ChatColor.DARK_GREEN + "";
 			}
 			
 			@Override
 			public String DARK_CYAN() {
 				return ChatColor.DARK_AQUA + "";
 			}
 			
 			@Override
 			public String DARK_RED() {
 				return ChatColor.DARK_RED + "";
 			}
 			
 			@Override
 			public String PURPLE() {
 				return ChatColor.LIGHT_PURPLE + "";
 			}
 			
 			@Override
 			public String GOLD() {
 				return ChatColor.GOLD + "";
 			}
 			
 			@Override
 			public String GRAY() {
 				return ChatColor.GRAY + "";
 			}
 			
 			@Override
 			public String DARK_GRAY() {
 				return ChatColor.DARK_GRAY + "";
 			}
 			
 			@Override
 			public String BLUE() {
 				return ChatColor.BLUE + "";
 			}
 			
 			@Override
 			public String BRIGHT_GREEN() {
 				return ChatColor.GREEN + "";
 			}
 			
 			@Override
 			public String CYAN() {
 				return ChatColor.AQUA + "";
 			}
 			
 			@Override
 			public String RED() {
 				return ChatColor.RED + "";
 			}
 			
 			@Override
 			public String PINK() {
 				return ChatColor.RED + "";
 			}
 			
 			@Override
 			public String YELLOW() {
 				return ChatColor.YELLOW + "";
 			}
 			
 			@Override
 			public String WHITE() {
 				return ChatColor.WHITE + "";
 			}
 			
 		};
 	}
 	
 	@Override
 	public IChatStyle chatStyle() {
 		
 		return new IChatStyle() {
 			
 			@Override
 			public String RANDOM() {
 				return ChatColor.MAGIC + "";
 			}
 			
 			@Override
 			public String BOLD() {
 				return ChatColor.BOLD + "";
 			}
 			
 			@Override
 			public String STRIKETHROUGH() {
 				return ChatColor.STRIKETHROUGH + "";
 			}
 			
 			@Override
 			public String UNDERLINED() {
 				return ChatColor.UNDERLINE + "";
 			}
 			
 			@Override
 			public String ITALIC() {
 				return ChatColor.ITALIC + "";
 			}
 			
 			@Override
 			public String PLAIN_WHITE() {
 				return ChatColor.RESET + "";
 			}
 			
 		};
 		
 	}
 	
 	@Override
 	public MQMaterial findMaterial(String material) {
 		Material mat = Material.matchMaterial(material);
 		
 		if (mat == null)
 			return null;
 		
 		return new BukkitMaterial(mat);
 	}
 	
 	@Override
 	public MQMaterial toMaterial(Object platformMaterial) {
 		if (platformMaterial instanceof Material)
 			return new BukkitMaterial((Material) platformMaterial);
 		else if (platformMaterial instanceof MaterialData)
 			return new BukkitMaterial((MaterialData) platformMaterial);
 		throw new IllegalArgumentException("Need Bukkit Material or MaterialData!");
 	}
 	
 	@Override
 	public MQItemStack toItemStack(Object platformItemStack) {
 		if (!(platformItemStack instanceof ItemStack))
 			throw new IllegalArgumentException("Need Bukkit ItemStack!");
 		ItemStack stack = (ItemStack) platformItemStack;
 		return new BukkitItemStack(stack);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T> T fromItemStack(MQItemStack stack) {
 		if (stack instanceof BukkitItemStack)
 			return (T) ((BukkitItemStack) stack).getUnderlyingStack();
 		return (T) new ItemStack(Material.matchMaterial(stack.getMaterial().getName()), stack.getAmount(), (short) stack.getData());
 	}
 	
 	@Override
 	public MQInventory toInventory(Object platformInventory) {
 		return new BukkitInventory((Inventory) platformInventory);
 	}
 	
 	@Override
 	public MQLocation toLocation(Object platformLocation) {
 		Location loc = (Location) platformLocation;
 		return new BukkitLocation(loc);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T> T fromLocation(MQLocation location) {
 		return (T) new Location(Bukkit.getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ());
 	}
 	
 	@Override
 	public MQPlayer toPlayer(Object platformPlayer) {
 		return new BukkitPlayer((Player) platformPlayer);
 	}
 	
 	@Override
 	public MQPlayer getPlayer(String name) {
 		Player p = Bukkit.getPlayerExact(name);
 		if (p == null)
 			return null;
 		return new BukkitPlayer(p);
 	}
 	
 	@Override
 	public Set<MQPlayer> getPlayers() {
 		HashSet<MQPlayer> set = new HashSet<MQPlayer>();
 		for (Player p : Bukkit.getOnlinePlayers())
 			set.add(new BukkitPlayer(p));
 		
 		return set;
 	}
 	
 	@Override
 	public Set<String> getWorlds() {
 		HashSet<String> worlds = new HashSet<String>();
 		for (World w : Bukkit.getWorlds())
 			worlds.add(w.getName());
 		
 		return worlds;
 	}
 	
 	@Override
 	public boolean hasWorld(String world) {
 		return (Bukkit.getWorld(world) != null);
 	}
 	
 	@Override
 	public void loadWorld(String world, int flags) {
 		WorldCreator create = new WorldCreator(world);
 		switch (flags & ENV_FLAGS) {
 		case NETHER_FLAG:
 			create.environment(Environment.NETHER);
 			break;
 		case END_FLAG:
 			create.environment(Environment.THE_END);
 			break;
 		case 0:
 			create.environment(Environment.NORMAL);
 			break;
 		default:
 			throw new IllegalArgumentException("Illegal env flag passed");
 		}
 		
 		switch (flags & TYPE_FLAGS) {
 		case FLAT_FLAG:
 			create.type(WorldType.FLAT);
 			break;
 		case LARGE_BIOME_FLAG:
 			create.type(WorldType.LARGE_BIOMES);
 			break;
 		case 0:
 			create.type(WorldType.NORMAL);
 			break;
 		default:
 			throw new IllegalArgumentException("Illegal type flag passed");
 		}
 		
 		create.createWorld();
 	}
 	
 	@Override
 	public void deloadWorld(String world, boolean save) {
 		TimeUtils.unlock(Bukkit.getWorld(world));
 		Bukkit.unloadWorld(world, save);
 	}
 	
 	@Override
 	public void destroyWorld(String world, boolean areyousure) {
 		if (!areyousure)
 			throw new IllegalArgumentException("second parameter to this method must be true");
 		
 		TimeUtils.unlock(Bukkit.getWorld(world));
 		if (Bukkit.unloadWorld(world, false))
 			if (!FileUtils.deleteQuietly(new File(world)))
 				FileUtils.deleteQuietly(new File(world));
 	}
 	
 	@Override
 	public String copyWorld(String originalWorld) {
 		if (!hasWorld(originalWorld))
 			throw new IllegalArgumentException("need originalWorld already loaded");
 		
 		World w = Bukkit.getWorld(originalWorld);
 		Random rand = new Random();
 		
 		String newname;
 		File newdirectory;
 		do {
 			newname = "mqinstance_" + rand.nextLong();
 			newdirectory = new File(newname);
 		} while (newdirectory.exists());
 		try {
 			FileUtils.copyDirectory(w.getWorldFolder(), newdirectory);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 		File uid = new File(newdirectory + File.separator + "uid.dat");
 		if (uid.exists())
 			uid.delete();
 		
 		WorldCreator tmp = new WorldCreator(newname);
 		tmp.copy(w);
 		World newWorld = Bukkit.createWorld(tmp);
 		return newWorld.getName();
 	}
 	
 	@Override
 	public MQBlock getBlock(MQLocation location) {
 		Location loc = Managers.getPlatform().fromLocation(location);
 		return new BukkitBlock(loc.getBlock());
 	}
 	
 	@Override
 	public <T extends MQEvent> void callEvent(T event) {
 		Bukkit.getServer().getPluginManager().callEvent(new BukkitEvent<T>(event));
 	}
 	
 	@Override
 	public <T> Future<T> callSyncTask(Callable<T> c) {
 		return Bukkit.getScheduler().callSyncMethod(this, c);
 	}
 	
 	@Override
 	public int scheduleSyncTask(Runnable task) {
 		return Bukkit.getScheduler().scheduleSyncDelayedTask(this, task);
 	}
 	
 	@Override
 	public int scheduleSyncTask(Runnable task, long tickDelay) {
 		return Bukkit.getScheduler().scheduleSyncDelayedTask(this, task, tickDelay);
 	}
 	
 	@Override
 	public int scheduleSyncRepeatingTask(Runnable task, long tickDelay, long tickRepeat) {
 		return Bukkit.getScheduler().scheduleSyncRepeatingTask(this, task, tickDelay, tickRepeat);
 	}
 	
 	@Override
 	public int scheduleAsynchronousTask(Runnable task) {
 		return Bukkit.getScheduler().runTaskAsynchronously(this, task).getTaskId();
 	}
 	
 	@Override
 	public int scheduleAsynchronousTask(Runnable task, long tickDelay) {
 		return Bukkit.getScheduler().runTaskLaterAsynchronously(this, task, tickDelay).getTaskId();
 	}
 	
 	@Override
 	public int scheduleAsynchronousRepeatingTask(Runnable task, long tickDelay, long tickRepeat) {
 		return Bukkit.getScheduler().runTaskTimerAsynchronously(this, task, tickDelay, tickRepeat).getTaskId();
 	}
 	
 	@Override
 	public boolean hasFinished(int taskID) {
 		return !(Bukkit.getScheduler().isCurrentlyRunning(taskID) || Bukkit.getScheduler().isQueued(taskID));
 	}
 	
 	@Override
 	public void cancelTask(int taskID) {
 		Bukkit.getScheduler().cancelTask(taskID);
 	}
 	
 	@Override
 	public void callCommand(String command) {
 		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
 	}
 	
 }
