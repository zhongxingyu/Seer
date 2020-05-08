 package com.adamki11s.questx;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.adamki11s.commands.QuestXCommands;
 import com.adamki11s.dialogue.dynamic.DynamicStrings;
 import com.adamki11s.events.ConversationRegister;
 import com.adamki11s.events.EntityDeathMonitor;
 import com.adamki11s.events.MovementMonitor;
 import com.adamki11s.events.NPCDamageEvent;
 import com.adamki11s.events.NPCInteractEvent;
 import com.adamki11s.events.PlayerJoinLeaveEvents;
 import com.adamki11s.events.TagColourEvent;
 import com.adamki11s.io.GeneralConfigData;
 import com.adamki11s.io.InitialSetup;
 import com.adamki11s.io.WorldConfigData;
 import com.adamki11s.metrics.Metrics;
 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.loading.FixedLoadingTable;
 import com.adamki11s.payload.ExtractPayload;
 import com.adamki11s.quests.QuestManager;
 import com.adamki11s.reputation.ReputationManager;
 import com.adamki11s.threads.ThreadController;
 import com.adamki11s.updates.UpdateNotifier;
 import com.adamki11s.updates.Updater;
 import com.adamki11s.updates.Updater.UpdateType;
 import com.adamki11s.validation.Validator;
 
 public class QuestX extends JavaPlugin {
 
 	static final Logger log = Logger.getLogger("QuestX");
 
 	public static boolean tagAPIEnabled;
 
 	NPCHandler handle;
 	ThreadController tControl;
 
 	NPCDamageEvent npcDamageEvent;
 	NPCInteractEvent npcInteractEvent;
 	ConversationRegister playerChatEvent;
 	MovementMonitor playerMoveEvent;
 	EntityDeathMonitor entityDeathMonitorEvent;
 	PlayerJoinLeaveEvents playerJLEvent;
 	TagColourEvent tagColourEvent;
 
 	public static Plugin p;
 	public static String version;
 	public static File f;
 
 	Metrics metrics;
 
 	public static synchronized void logMSG(String msg) {
 		log.info("[QuestX] " + msg);
 	}
 
 	public static boolean debug = false;
 
 	public static synchronized void logDebug(String msg) {
 		if (debug) {
 			log.info("[QuestX] " + msg);
 		}
 	}
 
 	public static synchronized void logError(String msg) {
 		log.info("[QuestX][ERROR] " + msg);
 	}
 
 	public static final void logChat(Player p, String message) {
 		p.sendMessage(ChatColor.AQUA + "[QuestX] " + ChatColor.RESET + DynamicStrings.getDynamicReplacement(message, p.getName()));
 	}
 
 	public static final void logChatError(Player p, String message) {
 		p.sendMessage(ChatColor.AQUA + "[QuestX]" + ChatColor.RED + "[ERROR] " + ChatColor.RESET + DynamicStrings.getDynamicReplacement(message, p.getName()));
 	}
 
 	public NPCHandler getNPCHandler() {
 		return this.handle;
 	}
 
 	public static Permission permission = null;
 	public static Economy economy = null;
 
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 		return (permission != null);
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return (economy != null);
 	}
 
 	static void m() {
 
 	}
 
 	@Override
 	public void onEnable() {
 
 		version = this.getDescription().getVersion();
 		f = this.getFile();
 
		boolean perms = this.setupPermissions();
		if (perms) {
			QuestX.logMSG("Hooked into Vault permissions successfully.");
 		} else {
			QuestX.logMSG("There was an error hooking into Vault permissions!");
 		}
 
		boolean econ = this.setupEconomy();
		if (econ) {
			QuestX.logMSG("Hooked into Vault Economy successfully.");
		} else {
			QuestX.logMSG("There was an error hooking into Vault economy!");
		}
 
 		Plugin plug = Bukkit.getPluginManager().getPlugin("TagAPI");
 		if (plug != null) {
 			tagAPIEnabled = true;
 		} else {
 			tagAPIEnabled = false;
 		}
 
 		InitialSetup.run();
 
 		try {
 			metrics = new Metrics(this);
 			boolean result = metrics.start();
 			if (result) {
 				QuestX.logMSG("Plugin Metrics connection was successful!");
 			} else {
 				QuestX.logMSG("Plugin Metrics connection denied, config may be set to opt-out:false");
 			}
 		} catch (IOException e) {
 			QuestX.logError("Failed to submit statistics!");
 		}
 
 		if (GeneralConfigData.isCheckingUpdates()) {
 			Updater u;
 			if (GeneralConfigData.isAutoDLUpdates()) {
 				u = new Updater(this, "questx", this.getFile(), UpdateType.DEFAULT, true);
 			} else {
 				u = new Updater(this, "questx", this.getFile(), UpdateType.NO_DOWNLOAD, true);
 			}
 			if (GeneralConfigData.isNotifyAdmin()) {
 				UpdateNotifier.setNotifier(u);
 			}
 			QuestX.logMSG("Update result = " + u.getResult().toString());
 		}
 
 		ExtractPayload.extractPayload();
 
 		handle = new NPCHandler(this, WorldConfigData.getWorlds());
 		
 		Validator.validateAll();
 
 		FixedLoadingTable.spawnFixedNPCS(handle);
 
 		p = this;
 		this.getCommand("QuestX").setExecutor(new QuestXCommands(this));
 		this.tControl = new ThreadController(handle);
 		this.tControl.initiateAsyncThread(10L);
 
 		// register events
 
 		npcDamageEvent = new NPCDamageEvent(this, handle);
 		npcInteractEvent = new NPCInteractEvent(this, handle);
 		playerChatEvent = new ConversationRegister(this, handle);
 		playerMoveEvent = new MovementMonitor(this, handle);
 		entityDeathMonitorEvent = new EntityDeathMonitor(this);
 		playerJLEvent = new PlayerJoinLeaveEvents(this);
 		if (tagAPIEnabled && GeneralConfigData.isTagAPISupported()) {
 			tagColourEvent = new TagColourEvent(this);
 		}
 
 		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 			ReputationManager.loadPlayerReputation(player.getName());
 			QuestManager.loadCurrentPlayerQuest(player.getName());
 		}
 	}
 
 	@Override
 	public void onDisable() {
 		tControl.terminateAsyncThread();
 		tControl.terminateSyncronousThread();
 		Bukkit.getServer().getScheduler().cancelTasks(this);
 	}
 
 }
