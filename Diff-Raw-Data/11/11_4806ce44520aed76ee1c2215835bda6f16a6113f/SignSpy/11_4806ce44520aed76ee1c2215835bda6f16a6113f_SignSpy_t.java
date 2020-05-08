 package tehtros.bukkit.SignSpy;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import tehtros.bukkit.SignSpy.Metrics.Graph;
 
 public class SignSpy extends JavaPlugin {
 	public static SignSpy plugin;
 	public final Logger log = Logger.getLogger("Minecraft");
 	public String name;
 	public String text;
 	public int signs = 0;
 	PluginDescriptionFile pdf;
 	PluginManager pm;
 	public final SignListener signListener = new SignListener(this);
 	private Set<CommandSender> ignoreSet = new HashSet<CommandSender>();
 
 	@Override
 	public void onEnable() {
 		pdf = getDescription();
 		pm = getServer().getPluginManager();
 		pm.registerEvents(this.signListener, this);
 		log.info("[" + pdf.getName() + "] " + pdf.getName() + " v" + pdf.getVersion() + " is enabled! :D");
 
 		try {
 			startMetrics();
 		} catch(IOException e) {
 			log.warning("Metrics fails to start! D:");
 		}
 	}
 
 	@Override
 	public void onDisable() {
 		pdf = getDescription();
 		log.info("[" + pdf.getName() + "] " + pdf.getName() + " v" + pdf.getVersion() + " is disabled! D:");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("signspy")) {
 			if(sender.hasPermission("signspy.spy")) {
 				if(args.length != 1) {
 					sender.sendMessage("Incorrect usage. Please use /signspy [on|off] to enable or disable sign logging to your in-game chat.");
 				} else {
 					if(args[0].equalsIgnoreCase("on")) {
 						if(ignoreSet.contains(sender)) {
 							ignoreSet.remove(sender);
 							sender.sendMessage("You will now recieve SignSpy messages!");
						} else {
							sender.sendMessage("You are already set recieve SignSpy messages!");
 						}
 					} else if(args[0].equalsIgnoreCase("off")) {
						if(!ignoreSet.contains(sender)) {
							ignoreSet.add(sender);
							sender.sendMessage("You will no longer recieve SignSpy messages!");
						} else {
							sender.sendMessage("You are already set to not recieve SignSpy messages!");
						}
 					} else {
 						sender.sendMessage("Incorrect usage. Please use /signspy [on|off] to enable or disable sign logging to your in-game chat.");
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public void startMetrics() throws IOException {
 		Metrics mets = new Metrics(this);
 		Graph exGraph = mets.createGraph("Extra Stats");
 
 		exGraph.addPlotter(new Metrics.Plotter("Signs Spyed") {
 
 			@Override
 			public int getValue() {
 				return signs;
 			}
 
 		});
 
 		mets.start();
 	}
 }
