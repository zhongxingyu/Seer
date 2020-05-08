 /**
  * @author TerrorBite
  */
 package net.lethargiclion.tradingpost;
 
 import java.util.logging.Logger;
 
 import net.lethargiclion.tradingpost.CommandProcessor.TPCommand;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * The main class for the TradingPost Bukkit plugin.
  * @author TerrorBite
  *
  */
 public class TradingPost extends JavaPlugin {
 	
 	static {
 		// Register the classes that we will be serializing
 		ConfigurationSerialization.registerClass(TradeStorage.class);
 		ConfigurationSerialization.registerClass(ItemBid.class);
 		ConfigurationSerialization.registerClass(TradeOffer.class);
 		ConfigurationSerialization.registerClass(QueuedItemDelivery.class);
 	}
 	
 	/**
 	 * The logger for this plugin.
 	 * Statically accessible for convenience.
 	 */
 	public static Logger log;
 
 	private TradeManager manager;
 	
 	CommandProcessor processor;
 	
 	public TradingPost() {
		log = org.bukkit.Bukkit.getLogger();
 	}
 	
 	/**
 	 * Called when the TradeManager plugin is enabled.
 	 * This takes care of initializing the TradeManager.
 	 */
 	@Override
 	public void onEnable() {
 		if(manager != null && manager instanceof TradeManager) {
 			log.warning("[TradingPost] The TradeManager is already initialized.");
 			manager.deserialize();
 		}
 		else {
 			// Start out by initializing the TradeManager.
 			manager = new TradeManager(this);
 		}
 		
 		processor = new CommandProcessor(log, manager);
 		
 		// Register events
 		getServer().getPluginManager().registerEvents(manager, this);
 		
 		log.info("[TradingPost] Successfully enabled.");
 	}
 	
 	/**
 	 * Called when the plugin is disabled.
 	 * This takes care of shutting down the TradeManager properly.
 	 */
 	@Override
 	public void onDisable() {
 		manager.serialize();
 		manager = null;
 		log.info("[TradingPost] Successfully disabled. Have a nice day!.");
 	}
 	
 	/**
 	 * Called when a command specified in plugin.yml is run by a user.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		
 		if(cmd.getName().equalsIgnoreCase("tr")) {
 			// If we didn't get a subcommand, fail and show usage message
 			if(args.length == 0) {
 				return false;
 			}
 			
 			// Otherwise look up the subcommand
 			TPCommand subcmd;
 			try {
 				subcmd = processor.getCommand(args[0]);
 			} catch (CommandNotFoundException e) {
 				// If it doesn't exist, fail and show usage message
 				log.warning(String.format("%s used unknown subcommand %s", sender.getName(), args[0]));
 				return false;
 			}
 			
 			// Log command usage (configurable option in future?)
 			log.info(String.format("[TradingPost] %s ran command: /%s %s", sender.getName(), cmd.getName().toLowerCase(), StringUtils.join(args, " ")));
 			
 			// Now run the command
 			return processor.runCommand(sender, subcmd, args);
 		}
 		else if(cmd.getName().equalsIgnoreCase("tradmin")) {
 			//TODO: Add admin command processor
 			return false;
 		}
 		else {
 			log.warning(String.format("[TradingPost] %s used unrecognised command \"/%s\" (bad plugin.yml?)", sender.getName(), cmd.getName().toLowerCase()));
 		}
 		
 		return false;
 	}
 
 }
