 package me.chriznight.cosmicshift;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author ChrizNight
  * 
  */
 public class CosmicShift extends JavaPlugin {
 	private CosmicShiftCommandExecutor CE;
 	private Listener PL;
 	private Logger logger;
 	protected static String N1;
 	protected static String N;
 
 	public CosmicShift() {
		this.logger = Logger.getLogger("CosmicShift");
		N1 = "[CS] ";
 		N = ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "CS" + ChatColor.GRAY
 				+ "] ";
 	}
 
 	@Override
 	public void onDisable() {
 		logStatus("disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 		PL = new CosmicShiftPlayerListener(this);
 		Bukkit.getServer().getPluginManager().registerEvents(PL, this);
 		CE = new CosmicShiftCommandExecutor(this);
 		getCommand("cs").setExecutor(CE);
 		logStatus("enabled!");
 	}
 
 	private final void logStatus(String state) {
 		PluginDescriptionFile pdf = this.getDescription();
 		this.logger.info(N1 + pdf.getName() + " v" + pdf.getVersion() + " "
 				+ state);
 	}
 
 	public final void log(String msg) {
 		this.logger.info(N1 + msg);
 	}
 
 	public final void send(CommandSender sender, String msg) {
 		sender.sendMessage(N + msg);
 	}
 }
