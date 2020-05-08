 package net.digiex;
 
 import java.util.logging.Logger;
 
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class clickme extends JavaPlugin {
 
 	private Logger log;
 	private Listener clickmelistener = new cmListener();
 
 	@Override
 	public void onDisable() {
 		log = getLogger();
 		log.info("v6 disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(clickmelistener, this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		getLogger().info(
 				pdfFile.getName() + " version " + pdfFile.getVersion()
 						+ " is enabled!");
 	}
 
 	public boolean has(Player player) {
 		return player.hasPermission("clickme.console");
 	}
 
 	public boolean hasConsole(Player player) {
 		return player.hasPermission("clickme.player");
 	}
 
 	private class cmListener implements Listener {
 
 		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 		public void onPlayerInteract(PlayerInteractEvent event) {
 			if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)
 					|| ((event.getAction() == Action.LEFT_CLICK_BLOCK))) {
 				BlockState state = event.getClickedBlock().getState();
 				Player player = event.getPlayer();
 				if ((state instanceof Sign)) {
 					Sign sign = (Sign) state;
 					if (sign.getLines()[0].equalsIgnoreCase("[ClickMe]")) {
 						if (has(event.getPlayer())) {
 						if (sign.getLines()[1].equalsIgnoreCase("console")) {
 							if (hasConsole(player))
 								getServer()
 										.dispatchCommand(
 												getServer().getConsoleSender(),
 												sign.getLines()[2].toString()
 														+ sign.getLines()[3]
 																.toString());
 							else
 								player.sendMessage("You don't have the required permissions for this...");
 						} else
 							getServer().dispatchCommand(
 									event.getPlayer(),
 									sign.getLines()[1].toString()
 											+ sign.getLines()[2].toString()
 											+ sign.getLines()[3].toString());
					}
 					} else {
 						player.sendMessage("You don't have the required permissions for this...");
 					}
 
 				}
 			}
 		}
 	}
 }
