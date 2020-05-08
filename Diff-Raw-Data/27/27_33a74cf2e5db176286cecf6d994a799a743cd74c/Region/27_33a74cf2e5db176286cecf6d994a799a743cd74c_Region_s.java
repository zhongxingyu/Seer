 package couk.Adamki11s.Regios.Regions;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.config.Configuration;
 
 import couk.Adamki11s.Extras.Cryptography.ExtrasCryptography;
 import couk.Adamki11s.Regios.Checks.Checks;
 import couk.Adamki11s.Regios.Checks.ChunkGrid;
 import couk.Adamki11s.Regios.Checks.PermChecks;
 import couk.Adamki11s.Regios.CustomEvents.RegionCreateEvent;
 import couk.Adamki11s.Regios.CustomEvents.RegionEnterEvent;
 import couk.Adamki11s.Regios.CustomEvents.RegionExitEvent;
 import couk.Adamki11s.Regios.CustomEvents.RegionLoadEvent;
 import couk.Adamki11s.Regios.Data.ConfigurationData;
 import couk.Adamki11s.Regios.Data.MODE;
 import couk.Adamki11s.Regios.Data.Saveable;
 import couk.Adamki11s.Regios.Inventory.InventoryCacheManager;
 import couk.Adamki11s.Regios.Listeners.RegiosPlayerListener;
 import couk.Adamki11s.Regios.Permissions.PermissionsCacheManager;
 import couk.Adamki11s.Regios.Permissions.PermissionsCore;
 import couk.Adamki11s.Regios.Scheduler.HealthRegeneration;
 import couk.Adamki11s.Regios.Scheduler.LightningRunner;
 import couk.Adamki11s.Regios.Scheduler.LogRunner;
 import couk.Adamki11s.Regios.SpoutInterface.SpoutInterface;
 import couk.Adamki11s.Regios.SpoutInterface.SpoutRegion;
 
 public class Region extends PermChecks implements Checks {
 
 	private RegionLocation l1;
 
 	private RegionLocation l2;
 
 	private final Region region = this;
 
 	private static final GlobalRegionManager grm = new GlobalRegionManager();
 	private static final Saveable saveable = new Saveable();
 
 	private ChunkGrid chunkGrid;
 
 	private String world;
 
 	private Location warp = null;
 
 	private String[] customSoundUrl, commandSet, temporaryNodesCacheAdd, permanentNodesCacheAdd, permanentNodesCacheRemove, subOwners;
 
 	private ArrayList<String> exceptions = new ArrayList<String>();
 	private ArrayList<String> nodes = new ArrayList<String>();
 	private ArrayList<Integer> items = new ArrayList<Integer>();
 
 	private String welcomeMessage = "", leaveMessage = "", protectionMessage = "", preventEntryMessage = "", preventExitMessage = "", password = "", name = "", owner = "",
 			spoutEntryMessage = "", spoutExitMessage = "", spoutTexturePack = "";
 
 	private Material spoutEntryMaterial = Material.GRASS, spoutExitMaterial = Material.DIRT;
 
 	private boolean _protection = false, _protectionPlace = false, _protectionBreak = false, preventEntry = false, preventExit = false, mobSpawns = true,
 			monsterSpawns = true, healthEnabled = true, pvp = true, doorsLocked = false, chestsLocked = false, preventInteraction = false, showPvpWarning = true,
 			passwordEnabled = false, showWelcomeMessage = true, showLeaveMessage = true, showProtectionMessage = true, showPreventEntryMessage = true,
 			showPreventExitMessage = true, fireProtection = false, playCustomSoundUrl = false, permWipeOnEnter = false, permWipeOnExit = false, wipeAndCacheOnEnter = false,
 			wipeAndCacheOnExit = false, forceCommand = false, blockForm = true, forSale = false, useSpoutTexturePack = false, spoutWelcomeEnabled, spoutLeaveEnabled;
 
 	private int LSPS = 0, healthRegen = 0, playerCap = 0, salePrice = 0;
 	private double velocityWarp = 0;
 
 	private MODE protectionMode = MODE.Whitelist, preventEntryMode = MODE.Whitelist, preventExitMode = MODE.Whitelist, itemMode = MODE.Whitelist;
 
 	private HashMap<Player, Boolean> authentication = new HashMap<Player, Boolean>();
 	private HashMap<Player, Long> timeStamps = new HashMap<Player, Long>();
 	private ArrayList<Player> playersInRegion = new ArrayList<Player>();
 	private HashMap<Player, Boolean> welcomeMessageSent = new HashMap<Player, Boolean>();
 	private HashMap<Player, Boolean> leaveMessageSent = new HashMap<Player, Boolean>();
 	private HashMap<Player, PlayerInventory> inventoryCache = new HashMap<Player, PlayerInventory>();
 
 	private ExtrasCryptography exCrypt = new ExtrasCryptography();
 
 	public Region(String owner, String name, Location l1, Location l2, World world, Player p, boolean save) {
 		this.owner = owner;
 		this.name = name;
 		this.l1 = new RegionLocation(l1.getWorld(), l1.getX(), l1.getY(), l1.getZ());
 		this.l2 = new RegionLocation(l2.getWorld(), l2.getX(), l2.getY(), l2.getZ());
 		RegionLocation rl1 = new RegionLocation(l1.getWorld(), l1.getX(), l1.getY(), l1.getZ()), rl2 = new RegionLocation(l2.getWorld(), l2.getX(), l2.getY(), l2.getZ());
 		if (world != null) {
 			this.world = world.getName();
 		} else {
 			this.world = Bukkit.getServer().getWorlds().get(0).getName();
 		}
 
 		if (save) {
 			RegionCreateEvent event = new RegionCreateEvent("RegionCreateEvent");
 			event.setProperties(p, this);
 			Bukkit.getServer().getPluginManager().callEvent(event);
 			exceptions.add(owner);
 		} else {
 			RegionLoadEvent event = new RegionLoadEvent("RegionLoadEvent");
 			event.setProperties(this);
 			Bukkit.getServer().getPluginManager().callEvent(event);
 		}
 
 		this.welcomeMessage = ConfigurationData.defaultWelcomeMessage.toString();
 
 		this.leaveMessage = ConfigurationData.defaultLeaveMessage.toString();
 		this.protectionMessage = (ConfigurationData.defaultProtectionMessage.toString());
 		this.preventEntryMessage = (ConfigurationData.defaultPreventEntryMessage.toString());
 		this.preventExitMessage = (ConfigurationData.defaultPreventExitMessage.toString());
 		if (ConfigurationData.passwordEnabled) {
 			this.passwordEnabled = true;
 			this.password = ConfigurationData.password;
 		} else {
 			this.passwordEnabled = false;
 			this.password = "";
 		}
 		this._protection = ConfigurationData.regionProtected;
 		this._protectionBreak = ConfigurationData.regionProtectedBreak;
 		this._protectionPlace = ConfigurationData.regionPlaceProtected;
 		this.preventEntry = ConfigurationData.regionPreventEntry;
 		this.mobSpawns = ConfigurationData.mobSpawns;
 		this.monsterSpawns = ConfigurationData.monsterSpawns;
 		this.healthEnabled = ConfigurationData.healthEnabled;
 		this.pvp = ConfigurationData.pvp;
 		this.doorsLocked = ConfigurationData.doorsLocked;
 		this.chestsLocked = ConfigurationData.chestsLocked;
 		this.preventInteraction = ConfigurationData.preventInteraction;
 		this.showPvpWarning = ConfigurationData.showPvpWarning;
 		this.LSPS = ConfigurationData.LSPS;
 		this.healthRegen = ConfigurationData.healthRegen;
 		this.velocityWarp = ConfigurationData.velocityWarp;
 		this.protectionMode = ConfigurationData.protectionMode;
 		this.preventEntryMode = ConfigurationData.preventEntryMode;
 		this.preventExitMode = ConfigurationData.preventExitMode;
 		this.preventExit = ConfigurationData.regionPreventExit;
 		this.spoutEntryMaterial = ConfigurationData.defaultSpoutWelcomeMaterial;
 		this.spoutExitMaterial = ConfigurationData.defaultSpoutLeaveMaterial;
 		this.spoutEntryMessage = "Welcome to [NAME]";
 		this.spoutExitMessage = "You left [NAME]";
 		this.showWelcomeMessage = ConfigurationData.showWelcomeMessage;
 		this.showLeaveMessage = ConfigurationData.showLeaveMessage;
 		this.showProtectionMessage = ConfigurationData.showProtectionMessage;
 		this.showPreventEntryMessage = ConfigurationData.showPreventEntryMessage;
 		this.showPreventExitMessage = ConfigurationData.showPreventExitMessage;
 		this.fireProtection = ConfigurationData.fireProtection;
 		this.permWipeOnEnter = ConfigurationData.permWipeOnEnter;
 		this.permWipeOnExit = ConfigurationData.permWipeOnExit;
 		this.wipeAndCacheOnEnter = ConfigurationData.wipeAndCacheOnEnter;
 		this.wipeAndCacheOnExit = ConfigurationData.wipeAndCacheOnExit;
 		this.forceCommand = ConfigurationData.forceCommand;
 		this.commandSet = ConfigurationData.commandSet;
 		this.temporaryNodesCacheAdd = ConfigurationData.temporaryNodesCacheAdd;
 		this.spoutTexturePack = "";
 		this.useSpoutTexturePack = false;
 		this.forSale = ConfigurationData.forSale;
 		this.salePrice = ConfigurationData.salePrice;
 		this.blockForm = ConfigurationData.blockForm;
 		if (this.LSPS > 0 && !LightningRunner.doesStikesContain(this)) {
 			LightningRunner.addRegion(this);
 		} else if (this.LSPS == 0 && LightningRunner.doesStikesContain(this)) {
 			LightningRunner.removeRegion(this);
 		}
 		chunkGrid = new ChunkGrid(l1, l2, this);
 		GlobalRegionManager.addRegion(this);
 		if (p == null && save) {
 			saveable.saveRegion(this, rl1, rl2);
 		}
 	}
 
 	public File getLogFile() {
 		return new File("plugins" + File.separator + "Regios" + File.separator + "Database" + File.separator + this.name + File.separator + "Logs" + File.separator
 				+ this.name + ".log");
 	}
 
 	public Configuration getConfigFile() {
 		return new Configuration(new File("plugins" + File.separator + "Regios" + File.separator + "Database" + File.separator + this.name + File.separator + this.name
 				+ ".rz"));
 	}
 	
 	public File getRawConfigFile(){
 		return new File(("plugins" + File.separator + "Regios" + File.separator + "Database" + File.separator + this.name + File.separator + this.name
 				+ ".rz"));
 	}
 
 	public File getExceptionDirectory() {
 		return new File("plugins" + File.separator + "Regios" + File.separator + "Database" + File.separator + this.name + File.separator + "Exceptions");
 	}
 
 	public File getBackupsDirectory() {
 		return new File("plugins" + File.separator + "Regios" + File.separator + "Database" + File.separator + this.name + File.separator + "Backups");
 	}
 	
 	public File getDirectory(){
 		return new File("plugins" + File.separator + "Regios" + File.separator + "Database" + File.separator + this.name);
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public String getOwner() {
 		return this.owner;
 	}
 
 	public ChunkGrid getChunkGrid() {
 		return this.chunkGrid;
 	}
 
 	public void addItemException(int id) {
 		this.items.add(id);
 	}
 
 	public void removeItemException(int id) {
 		if (this.items.contains((Object) id)) {
 			this.items.remove((Object) id);
 		}
 	}
 
 	private boolean isWelcomeMessageSent(Player p) {
 		if (!welcomeMessageSent.containsKey(p)) {
 			return false;
 		} else {
 			return welcomeMessageSent.get(p);
 		}
 	}
 
 	private boolean isLeaveMessageSent(Player p) {
 		if (!leaveMessageSent.containsKey(p)) {
 			return false;
 		} else {
 			return leaveMessageSent.get(p);
 		}
 	}
 
 	private void setTimestamp(Player p) {
 		timeStamps.put(p, System.currentTimeMillis());
 	}
 
 	public boolean isSendable(Player p) {
 		boolean outcome = (timeStamps.containsKey(p) ? (System.currentTimeMillis() > timeStamps.get(p) + 5000) : true);
 		if (outcome) {
 			setTimestamp(p);
 		}
 		return outcome;
 	}
 
 	public void sendLeaveMessage(Player p) {
 		if (!isLeaveMessageSent(p)) {
 			this.registerExitEvent(p);
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' left region."));
 			if (RegiosPlayerListener.currentRegion.containsKey(p)) {
 				RegiosPlayerListener.currentRegion.remove(p);
 			}
 			leaveMessageSent.put(p, true);
 			welcomeMessageSent.remove(p);
 			removePlayer(p);
 			if (HealthRegeneration.isRegenerator(p)) {
 				HealthRegeneration.removeRegenerator(p);
 			}
 			if (this.permWipeOnExit) {
 				if (!this.canBypassProtection(p)) {
 					InventoryCacheManager.wipeInventory(p);
 				}
 			}
 			if (this.wipeAndCacheOnEnter) {
 				if (!this.canBypassProtection(p)) {
 					if (InventoryCacheManager.doesCacheContain(p)) {
 						InventoryCacheManager.restoreInventory(p);
 						LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' inventory restored upon exit"));
 					}
 				}
 			}
 			if (this.wipeAndCacheOnExit) {
 				if (!this.canBypassProtection(p)) {
 					if (!InventoryCacheManager.doesCacheContain(p)) {
 						InventoryCacheManager.cacheInventory(p);
 						InventoryCacheManager.wipeInventory(p);
 						LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' inventory cached upon exit"));
 					}
 				}
 			}
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' left region."));
 			if (PermissionsCore.hasPermissions) {
 				try {
 					if (this.temporaryNodesCacheAdd != null) {
 						if (this.temporaryNodesCacheAdd.length > 0) {
 							PermissionsCacheManager.unCacheNodes(p, this);
 							LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Temporary node caches wiped upon region exit for player '" + p.getName() + "'"));
 						}
 					}
 					if (this.permanentNodesCacheRemove != null) {
 						if (this.permanentNodesCacheRemove.length > 0) {
 							PermissionsCacheManager.permRemoveNodes(p, this);
 							LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Permanent nodes wiped upon region exit for player '" + p.getName() + "'"));
 						}
 					}
 				} catch (Exception ex) {
 					// Fail silently if the operation is unsupported
 				}
 			}
 			if (this.showLeaveMessage) {
 				p.sendMessage(this.colourFormat(this.liveFormat(leaveMessage, p)));
 			}
 			if (SpoutInterface.doesPlayerHaveSpout(p)) {
 				if (this.spoutLeaveEnabled) {
 					SpoutRegion.sendLeaveMessage(p, this);
 				}
 				if (this.playCustomSoundUrl) {
 					SpoutRegion.stopMusicPlaying(p, region);
 				}
 				if (this.useSpoutTexturePack) {
 					SpoutRegion.resetTexturePack(p);
 				}
 			}
 		}
 	}
 
 	private void registerExitEvent(Player p) {
 		RegionExitEvent event = new RegionExitEvent("RegionExitEvent");
 		event.setProperties(p, this);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 	}
 
 	private void registerWelcomeEvent(Player p) {
 		RegionEnterEvent event = new RegionEnterEvent("RegionEnterEvent");
 		event.setProperties(p, this);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 	}
 
 	public void sendWelcomeMessage(Player p) {
 		if (!isWelcomeMessageSent(p)) {
 			this.registerWelcomeEvent(p);
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' entered region."));
 			if (this.useSpoutTexturePack && SpoutInterface.doesPlayerHaveSpout(p)) {
 				SpoutRegion.forceTexturePack(p, this);
 			}
 			RegiosPlayerListener.currentRegion.put(p, this);
 			welcomeMessageSent.put(p, true);
 			leaveMessageSent.remove(p);
 			addPlayer(p);
 			if (!HealthRegeneration.isRegenerator(p)) {
 				if (this.healthRegen < 0 && !this.canBypassProtection(p)) {
 					HealthRegeneration.addRegenerator(p, this.healthRegen);
 				} else if (this.healthRegen > 0) {
 					HealthRegeneration.addRegenerator(p, this.healthRegen);
 				}
 			}
 			if (this.permWipeOnEnter) {
 				if (!this.canBypassProtection(p)) {
 					InventoryCacheManager.wipeInventory(p);
 				}
 			}
 			if (this.wipeAndCacheOnEnter) {
 				if (!this.canBypassProtection(p)) {
 					if (!InventoryCacheManager.doesCacheContain(p)) {
 						InventoryCacheManager.cacheInventory(p);
 						InventoryCacheManager.wipeInventory(p);
 						LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' inventory cached upon entry"));
 					}
 				}
 			}
 			if (this.wipeAndCacheOnExit) {
 				if (!this.canBypassProtection(p)) {
 					if (InventoryCacheManager.doesCacheContain(p)) {
 						LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' inventory restored upon entry"));
 						InventoryCacheManager.restoreInventory(p);
 					}
 				}
 			}
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' entered region."));
 			if (this.commandSet != null) {
 				if (this.commandSet.length > 0) {
 					for (String s : commandSet) {
 						if (s.length() > 1) {
 							LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player forced command '" + s + "' upon enter."));
 							p.performCommand(s.trim());
 						}
 					}
 				}
 			}
 			if (PermissionsCore.hasPermissions) {
 				try {
 					if (this.temporaryNodesCacheAdd != null) {
 						if (this.temporaryNodesCacheAdd.length > 0) {
 							PermissionsCacheManager.cacheNodes(p, this);
 							LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Temporary node caches added upon region enter for player '" + p.getName() + "'"));
 						}
 
 					}
 
 					if (this.permanentNodesCacheAdd != null) {
 						if (this.permanentNodesCacheAdd.length > 0) {
 							PermissionsCacheManager.permAddNodes(p, this);
 							LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Permanent nodes added upon region enter for player '" + p.getName() + "'"));
 						}
 					}
 				} catch (Exception ex) {
 					// Fail silently if the operation is unsupported
 				}
 			}
 			if (this.showWelcomeMessage) {
 				p.sendMessage(this.colourFormat(this.liveFormat(welcomeMessage, p)));
 			}
 			if (SpoutInterface.doesPlayerHaveSpout(p)) {
 				if (this.spoutWelcomeEnabled) {
 					SpoutRegion.sendWelcomeMessage(p, this);
 				}
 				if (this.playCustomSoundUrl) {
 					SpoutRegion.playToPlayerMusicFromUrl(p, this);
 				}
 				if (this.useSpoutTexturePack) {
 					SpoutRegion.forceTexturePack(p, this);
 				}
 			}
 		}
 	}
 
 	public boolean isRegionFull(Player p) {
 		if (this.playerCap > 0) {
 			if (this.playersInRegion.size() > this.playerCap) {
 				if (!this.canBypassProtection(p)) {
 					return true;
 				} else {
 					return false;
 				}
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public String liveFormat(String original, Player p) {
 		original = original.replaceAll("\\[", "");
 		original = original.replaceAll("\\]", "");
 		if (original.contains("PLAYER-COUNT")) {
 			original = original.replaceAll("PLAYER-COUNT", "" + this.getPlayersInRegion().size());
 		}
 		if (original.contains("BUILD-RIGHTS")) {
 			original = original.replaceAll("BUILD-RIGHTS", "" + this.canBuild(p));
 		}
 		if (original.contains("PLAYER")) {
 			original = original.replaceAll("PLAYER", "" + p.getName());
 		}
 		if (original.contains("PLAYER-LIST")) {
 			StringBuilder builder = new StringBuilder();
 			builder.append("");
 			for (Player play : this.playersInRegion) {
 				builder.append(ChatColor.WHITE).append(play.getName()).append(ChatColor.BLUE).append(", ");
 			}
 			original = original.replaceAll("PLAYER-LIST", "" + builder.toString());
 		}
 		return original;
 	}
 
 	public void addException(String exception) {
 		this.exceptions.add(exception);
 		LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Exception '" + exception + "' added."));
 	}
 
 	public void removeException(String exception) {
 		if (this.exceptions.contains(exception)) {
 			this.exceptions.remove(exception);
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Exception '" + exception + "' removed."));
 		}
 	}
 
 	public void addExceptionNode(String node) {
 		this.nodes.add(node);
 		LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Exception node '" + node + "' added."));
 	}
 
 	public void removeExceptionNode(String node) {
 		if (this.nodes.contains(node)) {
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Exception node '" + node + "' removed."));
 			this.nodes.remove(node);
 		}
 	}
 
 	public ArrayList<String> getExceptionNodes() {
 		return this.nodes;
 	}
 
 	public ArrayList<Player> getPlayersInRegion() {
 		return this.playersInRegion;
 	}
 
 	public void sendBuildMessage(Player p) {
 		if (this.showProtectionMessage && isSendable(p)) {
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' tried to build but did not have permissions."));
 			p.sendMessage(this.colourFormat(this.liveFormat(protectionMessage, p)));
 		}
 	}
 
 	public void sendPreventEntryMessage(Player p) {
 		if (this.showPreventEntryMessage && isSendable(p)) {
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' tried to enter but did not have permissions."));
 			p.sendMessage(this.colourFormat(this.liveFormat(preventEntryMessage, p)));
 		}
 	}
 
 	public void sendPreventExitMessage(Player p) {
 		if (this.showPreventExitMessage && isSendable(p)) {
 			LogRunner.addLogMessage(this, LogRunner.getPrefix(this) + (" Player '" + p.getName() + "' tried to leave but did not have permissions."));
 			p.sendMessage(this.colourFormat(this.liveFormat(preventExitMessage, p)));
 		}
 	}
 
 	public void addPlayer(Player p) {
 		playersInRegion.add(p);
 	}
 
 	public void removePlayer(Player p) {
 		if (playersInRegion.contains(p)) {
 			playersInRegion.remove(p);
 		}
 	}
 
 	public boolean isPlayerInRegion(Player p) {
 		if (playersInRegion.contains(p)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void setL1(World w, double x, double y, double z) {
 		this.l1 = new RegionLocation(w, x, y, z);
 	}
 
 	public void setL2(World w, double x, double y, double z) {
 		this.l2 = new RegionLocation(w, x, y, z);
 	}
 
 	@Override
 	public boolean canBuild(Player p) {
 		return super.canBypassProtection(p, this);
 	}
 
 	public boolean canBypassProtection(Player p) {
 		return super.canBypassProtection(p, this);
 	}
 
 	@Override
 	public boolean canEnter(Player p) {
 		return super.canBypassEntryProtection(p, this);
 	}
 
 	@Override
 	public boolean canExit(Player p) {
 		return super.canBypassExitProtection(p, this);
 	}
 
 	@Override
 	public boolean canModify(Player p) {
 		return super.canOverride(p, this);
 	}
 
 	@Override
 	public boolean isProtected() {
 		return this._protection;
 	}
 
 	@Override
 	public boolean isPreventingEntry() {
 		return this.preventEntry;
 	}
 
 	@Override
 	public boolean isPreventingExit() {
 		return this.preventExit;
 	}
 
 	public boolean canMobsSpawn() {
 		return this.mobSpawns;
 	}
 
 	public String getWelcomeMessage() {
 		return this.welcomeMessage;
 	}
 
 	public String getLeaveMessage() {
 		return this.leaveMessage;
 	}
 
 	public String getPreventEntryMessage() {
 		return this.preventEntryMessage;
 	}
 
 	public String getPreventExitMessage() {
 		return this.preventExitMessage;
 	}
 
 	public Material getSpoutWelcomeMaterial() {
 		return this.spoutEntryMaterial;
 	}
 
 	public Material getSpoutLeaveMaterial() {
 		return this.spoutExitMaterial;
 	}
 
 	public String getSpoutWelcomeMessage() {
 		return this.spoutEntryMessage;
 	}
 
 	public String getSpoutLeaveMessage() {
 		return this.spoutExitMessage;
 	}
 
 	public boolean canMonstersSpawn() {
 		return this.monsterSpawns;
 	}
 
 	public boolean isPasswordEnabled() {
 		return this.passwordEnabled;
 	}
 
 	public boolean areChestsLocked() {
 		return this.chestsLocked;
 	}
 
 	public boolean areDoorsLocked() {
 		return this.doorsLocked;
 	}
 
 	public boolean isHealthEnabled() {
 		return this.healthEnabled;
 	}
 
 	public int getHealthRegen() {
 		return this.healthRegen;
 	}
 
 	public double getVelocityWarp() {
 		return this.velocityWarp;
 	}
 
 	public int getLSPS() {
 		return this.LSPS;
 	}
 
 	public boolean isPreventingInteraction() {
 		return this.preventInteraction;
 	}
 
 	public boolean isPvpEnabled() {
 		return this.pvp;
 	}
 
 	public String getPassword() {
 		return this.password;
 	}
 
 	public boolean isForcingCommand() {
 		return this.forceCommand;
 	}
 
 	public String[] getCommandSets() {
 		return this.commandSet;
 	}
 
 	public String[] getMusicUrls() {
 		return this.customSoundUrl;
 	}
 
 	public String[] getSubOwners() {
 		return this.subOwners;
 	}
 
 	public String[] getTempCacheNodes() {
 		return this.temporaryNodesCacheAdd;
 	}
 
 	public String[] getPermAddNodes() {
 		return this.permanentNodesCacheAdd;
 	}
 
 	public String[] getPermRemoveNodes() {
 		return this.permanentNodesCacheRemove;
 	}
 
 	public ArrayList<String> getExceptions() {
 		return this.exceptions;
 	}
 
 	public ArrayList<Integer> getItems() {
 		return this.items;
 	}
 
 	public Location getWarp() {
 		return this.warp;
 	}
 
 	public MODE getProtectionMode() {
 		return this.protectionMode;
 	}
 
 	public MODE getPreventEntryMode() {
 		return this.preventEntryMode;
 	}
 
 	public MODE getPreventExitMode() {
 		return this.preventExitMode;
 	}
 
 	public MODE getItemMode() {
 		return this.itemMode;
 	}
 
 	public boolean permWipeOnEnter() {
 		return this.permWipeOnEnter;
 	}
 
 	public boolean permWipeOnExit() {
 		return this.permWipeOnExit;
 	}
 
 	public boolean wipeAndCacheOnEnter() {
 		return this.wipeAndCacheOnEnter;
 	}
 
 	public boolean wipeAndCacheOnExit() {
 		return this.wipeAndCacheOnExit;
 	}
 
 	public void cacheInventory(Player p) {
 		inventoryCache.put(p, p.getInventory());
 	}
 
 	public PlayerInventory getInventoryCache(Player p) {
 		return inventoryCache.containsKey(p) ? inventoryCache.get(p) : null;
 	}
 
 	public boolean getAuthentication(String password, Player p) {
 		if (exCrypt.compareHashes(exCrypt.computeSHA2_384BitHash(password), this.password)) {
 			authentication.put(p, true);
 			return true;
 		} else {
 			authentication.put(p, false);
 			return false;
 		}
 	}
 
 	public boolean isAuthenticated(Player p) {
 		if (authentication.containsKey(p)) {
 			return authentication.get(p);
 		} else {
 			return false;
 		}
 	}
 
 	public void resetAuthentication(Player p) {
 		authentication.put(p, false);
 	}
 
 	public RegionLocation getL1() {
 		return this.l1;
 	}
 
 	public RegionLocation getL2() {
 		return this.l2;
 	}
 
 	public String colourFormat(String message) {
 		// In case old regions were not patched properly
 		message = message.replaceAll("%BLACK%", "<BLACK>");
 		message = message.replaceAll("\\&0", "<BLACK>");
 		message = message.replaceAll("\\$0", "<BLACK>");
 
 		message = message.replaceAll("%DBLUE%", "<DBLUE>");
 		message = message.replaceAll("\\&1", "<DBLUE>");
 		message = message.replaceAll("\\$1", "<DBLUE>");
 
 		message = message.replaceAll("%DGREEN%", "<DGREEN>");
 		message = message.replaceAll("\\&2", "<DGREEN>");
 		message = message.replaceAll("\\$2", "<DGREEN>");
 
 		message = message.replaceAll("%DTEAL%", "<DTEAL>");
 		message = message.replaceAll("\\&3", "<DTEAL>");
 		message = message.replaceAll("\\$3", "<DTEAL>");
 
 		message = message.replaceAll("%DRED%", "<DRED>");
 		message = message.replaceAll("\\&4", "<DRED>");
 		message = message.replaceAll("\\$4", "<DRED>");
 
 		message = message.replaceAll("%PURPLE%", "<PURPLE>");
 		message = message.replaceAll("\\&5", "<PURPLE>");
 		message = message.replaceAll("\\$5", "<PURPLE>");
 
 		message = message.replaceAll("%GOLD%", "<GOLD>");
 		message = message.replaceAll("\\&6", "<GOLD>");
 		message = message.replaceAll("\\$6", "<GOLD>");
 
 		message = message.replaceAll("%GREY%", "<GREY>");
 		message = message.replaceAll("\\&7", "<GREY>");
 		message = message.replaceAll("\\$7", "<GREY>");
 
 		message = message.replaceAll("%DGREY%", "<DGREY>");
 		message = message.replaceAll("\\&8", "<DGREY>");
 		message = message.replaceAll("\\$8", "<DGREY>");
 
 		message = message.replaceAll("%BLUE%", "<BLUE>");
 		message = message.replaceAll("\\&9", "<BLUE>");
 		message = message.replaceAll("\\$9", "<BLUE>");
 
 		message = message.replaceAll("%BGREEN%", "<BGREEN>");
 		message = message.replaceAll("\\&A", "<BGREEN>");
 		message = message.replaceAll("\\$A", "<BGREEN>");
 
 		message = message.replaceAll("%TEAL%", "<TEAL>");
 		message = message.replaceAll("\\&B", "<TEAL>");
 		message = message.replaceAll("\\$B", "<TEAL>");
 
 		message = message.replaceAll("%RED%", "<RED>");
 		message = message.replaceAll("\\&C", "<RED>");
 		message = message.replaceAll("\\$C", "<RED>");
 
 		message = message.replaceAll("%PINK%", "<PINK>");
 		message = message.replaceAll("\\&D", "<PINK>");
 		message = message.replaceAll("\\$D", "<PINK>");
 
 		message = message.replaceAll("%YELLOW%", "<YELLOW>");
 		message = message.replaceAll("\\&E", "<YELLOW>");
 		message = message.replaceAll("\\$E", "<YELLOW>");
 
 		message = message.replaceAll("%WHITE%", "<WHITE>");
 		message = message.replaceAll("\\&F", "<WHITE>");
 		message = message.replaceAll("\\$F", "<WHITE>");
 		// In case old regions were not patched properly
 
 		message = message.replaceAll("<BLACK>", "\u00A70");
 		message = message.replaceAll("<0>", "\u00A70");
 
 		message = message.replaceAll("<DBLUE>", "\u00A71");
 		message = message.replaceAll("<1>", "\u00A71");
 
 		message = message.replaceAll("<DGREEN>", "\u00A72");
 		message = message.replaceAll("<2>", "\u00A72");
 
 		message = message.replaceAll("<DTEAL>", "\u00A73");
 		message = message.replaceAll("<3>", "\u00A73");
 
 		message = message.replaceAll("<DRED>", "\u00A74");
 		message = message.replaceAll("<4>", "\u00A74");
 
 		message = message.replaceAll("<PURPLE>", "\u00A75");
 		message = message.replaceAll("<5>", "\u00A75");
 
 		message = message.replaceAll("<GOLD>", "\u00A76");
 		message = message.replaceAll("<6>", "\u00A76");
 
 		message = message.replaceAll("<GREY>", "\u00A77");
 		message = message.replaceAll("<7>", "\u00A77");
 
 		message = message.replaceAll("<DGREY>", "\u00A78");
 		message = message.replaceAll("<8>", "\u00A78");
 
 		message = message.replaceAll("<BLUE>", "\u00A79");
 		message = message.replaceAll("<9>", "\u00A79");
 
 		message = message.replaceAll("<BGREEN>", "\u00A7a");
 		message = message.replaceAll("<A>", "\u00A7a");
 
 		message = message.replaceAll("<TEAL>", "\u00A7b");
 		message = message.replaceAll("<B>", "\u00A7b");
 
 		message = message.replaceAll("<RED>", "\u00A7c");
 		message = message.replaceAll("<C>", "\u00A7c");
 
 		message = message.replaceAll("<PINK>", "\u00A7d");
 		message = message.replaceAll("<D>", "\u00A7d");
 
 		message = message.replaceAll("<YELLOW>", "\u00A7e");
 		message = message.replaceAll("<E>", "\u00A7e");
 
 		message = message.replaceAll("<WHITE>", "\u00A7f");
 		message = message.replaceAll("<F>", "\u00A7f");
 
 		message = message.replaceAll("\\[", "");
 		message = message.replaceAll("\\]", "");
 		message = message.replaceAll("OWNER", this.getOwner());
 		message = message.replaceAll("NAME", this.getName());
 
 		return message;
 	}
 
 	public String getWorld() {
 		return world;
 	}
 
 	public void setWorld(String world) {
 		this.world = world;
 	}
 
 	public String[] getCustomSoundUrl() {
 		return customSoundUrl;
 	}
 
 	public void setCustomSoundUrl(String[] customSoundUrl) {
 		this.customSoundUrl = customSoundUrl;
 	}
 
 	public String[] getCommandSet() {
 		return commandSet;
 	}
 
 	public void setCommandSet(String[] commandSet) {
 		this.commandSet = commandSet;
 	}
 
 	public String[] getTemporaryNodesCacheAdd() {
 		return temporaryNodesCacheAdd;
 	}
 
 	public void setTemporaryNodesCacheAdd(String[] temporaryNodesCacheAdd) {
 		this.temporaryNodesCacheAdd = temporaryNodesCacheAdd;
 	}
 
 	public String[] getPermanentNodesCacheAdd() {
 		return permanentNodesCacheAdd;
 	}
 
 	public void setPermanentNodesCacheAdd(String[] permanentNodesCacheAdd) {
 		this.permanentNodesCacheAdd = permanentNodesCacheAdd;
 	}
 
 	public String[] getPermanentNodesCacheRemove() {
 		return permanentNodesCacheRemove;
 	}
 
 	public void setPermanentNodesCacheRemove(String[] permanentNodesCacheRemove) {
 		this.permanentNodesCacheRemove = permanentNodesCacheRemove;
 	}
 
 	public ArrayList<String> getNodes() {
 		return nodes;
 	}
 
 	public void setNodes(ArrayList<String> nodes) {
 		this.nodes = nodes;
 	}
 
 	public String getProtectionMessage() {
 		return protectionMessage;
 	}
 
 	public void setProtectionMessage(String protectionMessage) {
 		this.protectionMessage = protectionMessage;
 	}
 
 	public String getSpoutEntryMessage() {
 		return spoutEntryMessage;
 	}
 
 	public void setSpoutEntryMessage(String spoutEntryMessage) {
 		this.spoutEntryMessage = spoutEntryMessage;
 	}
 
 	public String getSpoutExitMessage() {
 		return spoutExitMessage;
 	}
 
 	public void setSpoutExitMessage(String spoutExitMessage) {
 		this.spoutExitMessage = spoutExitMessage;
 	}
 
 	public Material getSpoutEntryMaterial() {
 		return spoutEntryMaterial;
 	}
 
 	public void setSpoutEntryMaterial(Material spoutEntryMaterial) {
 		this.spoutEntryMaterial = spoutEntryMaterial;
 	}
 
 	public Material getSpoutExitMaterial() {
 		return spoutExitMaterial;
 	}
 
 	public void setSpoutExitMaterial(Material spoutExitMaterial) {
 		this.spoutExitMaterial = spoutExitMaterial;
 	}
 
 	public boolean is_protection() {
 		return _protection;
 	}
 
 	public void set_protection(boolean _protection) {
 		this._protection = _protection;
 	}
 
 	public boolean isPreventEntry() {
 		return preventEntry;
 	}
 
 	public void setPreventEntry(boolean preventEntry) {
 		this.preventEntry = preventEntry;
 	}
 
 	public boolean isPreventExit() {
 		return preventExit;
 	}
 
 	public void setPreventExit(boolean preventExit) {
 		this.preventExit = preventExit;
 	}
 
 	public boolean isMobSpawns() {
 		return mobSpawns;
 	}
 
 	public void setMobSpawns(boolean mobSpawns) {
 		this.mobSpawns = mobSpawns;
 	}
 
 	public boolean isMonsterSpawns() {
 		return monsterSpawns;
 	}
 
 	public void setMonsterSpawns(boolean monsterSpawns) {
 		this.monsterSpawns = monsterSpawns;
 	}
 
 	public boolean isPvp() {
 		return pvp;
 	}
 
 	public void setPvp(boolean pvp) {
 		this.pvp = pvp;
 	}
 
 	public boolean isDoorsLocked() {
 		return doorsLocked;
 	}
 
 	public void setDoorsLocked(boolean doorsLocked) {
 		this.doorsLocked = doorsLocked;
 	}
 
 	public boolean isChestsLocked() {
 		return chestsLocked;
 	}
 
 	public void setChestsLocked(boolean chestsLocked) {
 		this.chestsLocked = chestsLocked;
 	}
 
 	public boolean isPreventInteraction() {
 		return preventInteraction;
 	}
 
 	public void setPreventInteraction(boolean preventInteraction) {
 		this.preventInteraction = preventInteraction;
 	}
 
 	public boolean isShowPvpWarning() {
 		return showPvpWarning;
 	}
 
 	public void setShowPvpWarning(boolean showPvpWarning) {
 		this.showPvpWarning = showPvpWarning;
 	}
 
 	public boolean isShowWelcomeMessage() {
 		return showWelcomeMessage;
 	}
 
 	public void setShowWelcomeMessage(boolean showWelcomeMessage) {
 		this.showWelcomeMessage = showWelcomeMessage;
 	}
 
 	public boolean isShowLeaveMessage() {
 		return showLeaveMessage;
 	}
 
 	public void setShowLeaveMessage(boolean showLeaveMessage) {
 		this.showLeaveMessage = showLeaveMessage;
 	}
 
 	public boolean isShowProtectionMessage() {
 		return showProtectionMessage;
 	}
 
 	public void setShowProtectionMessage(boolean showProtectionMessage) {
 		this.showProtectionMessage = showProtectionMessage;
 	}
 
 	public boolean isShowPreventEntryMessage() {
 		return showPreventEntryMessage;
 	}
 
 	public void setShowPreventEntryMessage(boolean showPreventEntryMessage) {
 		this.showPreventEntryMessage = showPreventEntryMessage;
 	}
 
 	public boolean isShowPreventExitMessage() {
 		return showPreventExitMessage;
 	}
 
 	public void setShowPreventExitMessage(boolean showPreventExitMessage) {
 		this.showPreventExitMessage = showPreventExitMessage;
 	}
 
 	public boolean isFireProtection() {
 		return fireProtection;
 	}
 
 	public void setFireProtection(boolean fireProtection) {
 		this.fireProtection = fireProtection;
 	}
 
 	public boolean isPlayCustomSoundUrl() {
 		return playCustomSoundUrl;
 	}
 
 	public void setPlayCustomSoundUrl(boolean playCustomSoundUrl) {
 		this.playCustomSoundUrl = playCustomSoundUrl;
 	}
 
 	public boolean isPermWipeOnEnter() {
 		return permWipeOnEnter;
 	}
 
 	public void setPermWipeOnEnter(boolean permWipeOnEnter) {
 		this.permWipeOnEnter = permWipeOnEnter;
 	}
 
 	public boolean isPermWipeOnExit() {
 		return permWipeOnExit;
 	}
 
 	public void setPermWipeOnExit(boolean permWipeOnExit) {
 		this.permWipeOnExit = permWipeOnExit;
 	}
 
 	public boolean isWipeAndCacheOnEnter() {
 		return wipeAndCacheOnEnter;
 	}
 
 	public void setWipeAndCacheOnEnter(boolean wipeAndCacheOnEnter) {
 		this.wipeAndCacheOnEnter = wipeAndCacheOnEnter;
 	}
 
 	public boolean isWipeAndCacheOnExit() {
 		return wipeAndCacheOnExit;
 	}
 
 	public void setWipeAndCacheOnExit(boolean wipeAndCacheOnExit) {
 		this.wipeAndCacheOnExit = wipeAndCacheOnExit;
 	}
 
 	public boolean isForceCommand() {
 		return forceCommand;
 	}
 
 	public void setForceCommand(boolean forceCommand) {
 		this.forceCommand = forceCommand;
 	}
 
 	public boolean isBlockForm() {
 		return blockForm;
 	}
 
 	public void setBlockForm(boolean blockForm) {
 		this.blockForm = blockForm;
 	}
 
 	public boolean isForSale() {
 		return forSale;
 	}
 
 	public void setForSale(boolean forSale) {
 		this.forSale = forSale;
 	}
 
 	public int getPlayerCap() {
 		return playerCap;
 	}
 
 	public void setPlayerCap(int playerCap) {
 		this.playerCap = playerCap;
 	}
 
 	public int getSalePrice() {
 		return salePrice;
 	}
 
 	public void setSalePrice(int salePrice) {
 		this.salePrice = salePrice;
 	}
 
 	public HashMap<Player, Boolean> getAuthentication() {
 		return authentication;
 	}
 
 	public void setAuthentication(HashMap<Player, Boolean> authentication) {
 		this.authentication = authentication;
 	}
 
 	public HashMap<Player, Long> getTimeStamps() {
 		return timeStamps;
 	}
 
 	public void setTimeStamps(HashMap<Player, Long> timeStamps) {
 		this.timeStamps = timeStamps;
 	}
 
 	public HashMap<Player, Boolean> getWelcomeMessageSent() {
 		return welcomeMessageSent;
 	}
 
 	public void setWelcomeMessageSent(HashMap<Player, Boolean> welcomeMessageSent) {
 		this.welcomeMessageSent = welcomeMessageSent;
 	}
 
 	public HashMap<Player, Boolean> getLeaveMessageSent() {
 		return leaveMessageSent;
 	}
 
 	public void setLeaveMessageSent(HashMap<Player, Boolean> leaveMessageSent) {
 		this.leaveMessageSent = leaveMessageSent;
 	}
 
 	public HashMap<Player, PlayerInventory> getInventoryCache() {
 		return inventoryCache;
 	}
 
 	public void setInventoryCache(HashMap<Player, PlayerInventory> inventoryCache) {
 		this.inventoryCache = inventoryCache;
 	}
 
 	public ExtrasCryptography getExCrypt() {
 		return exCrypt;
 	}
 
 	public void setExCrypt(ExtrasCryptography exCrypt) {
 		this.exCrypt = exCrypt;
 	}
 
 	public Region getRegion() {
 		return region;
 	}
 
 	public static GlobalRegionManager getGrm() {
 		return grm;
 	}
 
 	public static Saveable getSaveable() {
 		return saveable;
 	}
 
 	public void setL1(RegionLocation l1) {
 		this.l1 = l1;
 	}
 
 	public void setL2(RegionLocation l2) {
 		this.l2 = l2;
 	}
 
 	public void setChunkGrid(ChunkGrid chunkGrid) {
 		this.chunkGrid = chunkGrid;
 	}
 
 	public void setWarp(Location warp) {
 		this.warp = warp;
 	}
 
 	public void setSubOwners(String[] subOwners) {
 		this.subOwners = subOwners;
 	}
 
 	public void setExceptions(ArrayList<String> exceptions) {
 		this.exceptions = exceptions;
 	}
 
 	public void setItems(ArrayList<Integer> items) {
 		this.items = items;
 	}
 
 	public void setWelcomeMessage(String welcomeMessage) {
 		this.welcomeMessage = welcomeMessage;
 	}
 
 	public void setLeaveMessage(String leaveMessage) {
 		this.leaveMessage = leaveMessage;
 	}
 
 	public void setPreventEntryMessage(String preventEntryMessage) {
 		this.preventEntryMessage = preventEntryMessage;
 	}
 
 	public void setPreventExitMessage(String preventExitMessage) {
 		this.preventExitMessage = preventExitMessage;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public void setHealthEnabled(boolean healthEnabled) {
 		this.healthEnabled = healthEnabled;
 	}
 
 	public void setPasswordEnabled(boolean passwordEnabled) {
 		this.passwordEnabled = passwordEnabled;
 	}
 
 	public void setLSPS(int lSPS) {
 		LSPS = lSPS;
 	}
 
 	public void setHealthRegen(int healthRegen) {
 		this.healthRegen = healthRegen;
 	}
 
 	public void setVelocityWarp(double velocityWarp) {
 		this.velocityWarp = velocityWarp;
 	}
 
 	public void setProtectionMode(MODE protectionMode) {
 		this.protectionMode = protectionMode;
 	}
 
 	public void setPreventEntryMode(MODE preventEntryMode) {
 		this.preventEntryMode = preventEntryMode;
 	}
 
 	public void setPreventExitMode(MODE preventExitMode) {
 		this.preventExitMode = preventExitMode;
 	}
 
 	public void setItemMode(MODE itemMode) {
 		this.itemMode = itemMode;
 	}
 
 	public void setPlayersInRegion(ArrayList<Player> playersInRegion) {
 		this.playersInRegion = playersInRegion;
 	}
 
 	public String getSpoutTexturePack() {
 		return spoutTexturePack;
 	}
 
 	public void setSpoutTexturePack(String spoutTexturePack) {
 		this.spoutTexturePack = spoutTexturePack;
 	}
 
 	public boolean isUseSpoutTexturePack() {
 		return useSpoutTexturePack;
 	}
 
 	public void setUseSpoutTexturePack(boolean useSpoutTexturePack) {
 		this.useSpoutTexturePack = useSpoutTexturePack;
 	}
 
 	public boolean is_protectionPlace() {
 		return _protectionPlace;
 	}
 
 	public void set_protectionPlace(boolean _protectionPlace) {
 		this._protectionPlace = _protectionPlace;
 	}
 
 	public boolean is_protectionBreak() {
 		return _protectionBreak;
 	}
 
 	public void set_protectionBreak(boolean _protectionBreak) {
 		this._protectionBreak = _protectionBreak;
 	}
 
 	public boolean isSpoutWelcomeEnabled() {
 		return spoutWelcomeEnabled;
 	}
 
 	public void setSpoutWelcomeEnabled(boolean spoutWelcomeEnabled) {
 		this.spoutWelcomeEnabled = spoutWelcomeEnabled;
 	}
 
 	public boolean isSpoutLeaveEnabled() {
 		return spoutLeaveEnabled;
 	}
 
 	public void setSpoutLeaveEnabled(boolean spoutLeaveEnabled) {
 		this.spoutLeaveEnabled = spoutLeaveEnabled;
 	}
 
 }
