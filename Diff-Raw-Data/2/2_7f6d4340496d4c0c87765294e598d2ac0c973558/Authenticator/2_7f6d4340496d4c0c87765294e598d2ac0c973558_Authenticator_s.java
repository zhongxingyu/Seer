 package fr.areku.Authenticator;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import fr.areku.Authenticator.events.PlayerOfflineModeLogin;
 import fr.areku.commons.UpdateChecker;
 
 public class Authenticator extends JavaPlugin {
 	private static Authenticator instance;
 	private OfflineMode controller;
	private boolean debugSwitch;
 
 	public static void setDebug(boolean state, Plugin enabler) {
 		if (instance.debugSwitch != state)
 			log("Debug " + (state ? "enabled" : "disabled") + " by "
 					+ enabler.getName());
 		
 		instance.debugSwitch = state;
 	}
 
 	public static boolean isDebug() {
 		return instance.debugSwitch;
 	}
 
 	public static void log(Level level, String m) {
 		instance.getLogger().log(level, m);
 	}
 
 	public static void d(Level level, String m) {
 		if (isDebug())
 			instance.getLogger().log(level, "[DEBUG] " + m);
 	}
 
 	public static void log(String m) {
 		log(Level.INFO, m);
 	}
 
 	public static void d(String m) {
 		d(Level.INFO, m);
 	}
 
 	@Override
 	public void onLoad() {
 		instance = this;
 		debugSwitch = false;
 		controller = new OfflineMode(this);
 	}
 
 	@Override
 	public void onEnable() {
 		controller.hookAuthPlugins();
 		Bukkit.getServer()
 				.getPluginManager()
 				.registerEvents(new InternalPlayerListener(this.controller),
 						this);
 		startMetrics();
 		startUpdate();
 	}
 
 	private void startMetrics() {
 
 		try {
 			log("Starting Metrics");
 			Metrics metrics = new Metrics(this);
 			Metrics.Graph fitersCount = metrics
 					.createGraph("Offline-mode plugins");
 			if (controller.isUsingOfflineModePlugin())
 				fitersCount.addPlotter(new Metrics.Plotter(controller
 						.getSelectedAuthPlugin().getName()) {
 
 					@Override
 					public int getValue() {
 						return 1;
 					}
 
 				});
 			metrics.start();
 		} catch (IOException e) {
 			log("Cannot start Metrics...");
 		}
 	}
 
 	private void startUpdate() {
 		try {
 			UpdateChecker update = new UpdateChecker(this);
 			update.start();
 		} catch (MalformedURLException e) {
 			log("Cannot start Plugin Updater...");
 		}
 	}
 
 	public void notifyPlayerLogin(Player player) {
 		Bukkit.getPluginManager().callEvent(
 				new PlayerOfflineModeLogin(player, controller
 						.getSelectedAuthPlugin()));
 	}
 
 	public void notifyPlayerLogout(Player player) {
 		// Bukkit.getPluginManager().callEvent(new
 		// PlayerOfflineModeLogout(player));
 	}
 
 	public static boolean isPlayerLoggedIn(Player p) {
 		return instance.controller.isPlayerLoggedIn(p);
 	}
 
 	public static boolean isUsingOfflineModePlugin() {
 		return instance.controller.isUsingOfflineModePlugin();
 	}
 
 }
