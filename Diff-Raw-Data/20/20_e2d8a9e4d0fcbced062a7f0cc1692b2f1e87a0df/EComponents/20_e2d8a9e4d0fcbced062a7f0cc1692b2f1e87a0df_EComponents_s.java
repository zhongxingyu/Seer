 package net.year4000.ecomponents;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import com.sk89q.commandbook.CommandBook;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.zachsthings.libcomponents.ComponentInformation;
 import com.zachsthings.libcomponents.ComponentManager;
 import com.zachsthings.libcomponents.bukkit.BukkitComponent;
 import com.zachsthings.libcomponents.config.ConfigurationBase;
 import com.zachsthings.libcomponents.config.Setting;
 
 @ComponentInformation(friendlyName = "eComponents", desc = "Shows the components that are running in this server.")
 public class EComponents extends BukkitComponent implements Listener {
 
 	private String component = "[eComponents]";
 	private String version = this.getClass().getPackage().getImplementationVersion();
 	private Logger logger = Logger.getLogger(component);
 	private LocalConfiguration config;
 
 	public void enable() {
 		registerCommands(Commands.class);
 		CommandBook.registerEvents(this);
 		config = configure(new LocalConfiguration());
 		logger.log(Level.INFO, component + " version " + version + " has been enabled.");
 	}
 
     public void reload() {
     	super.reload();
     	configure(config);
     	logger.log(Level.INFO, component + " has been reloaded.");
     }
 
     public static class LocalConfiguration extends ConfigurationBase {
     	@Setting("ewized") public boolean ewized = true;
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onJoin(PlayerJoinEvent event) {
     	if (config.ewized) {
     		Player player = event.getPlayer();
 
     		if (player == Bukkit.getPlayer("ewized")) {
    			player.sendMessage("This server is using eSuite running;");
    			getComponents(player);
     		}
     	}
     }
    
    public void getComponents(Player player) {
 		//CommandBook Components
 		ComponentManager<BukkitComponent> components = CommandBook.inst().getComponentManager();
 		String componentNames = ",components";
 		for (BukkitComponent component : components.getComponents()) {
 			ChatColor componentColor;
 			if (component.isEnabled()) {
 				componentColor = ChatColor.GREEN;
 			} else {
 				componentColor = ChatColor.RED;
 			}
 			componentNames = componentNames + ", " + componentColor + component.getInformation().friendlyName() + ChatColor.RESET;
 		}
 		String componentPrefix = "Components (" + components.getComponents().size() + "):";
		player.sendMessage(componentNames.replaceFirst(",components,", componentPrefix));
     }
 
     public BukkitComponent checkComponent(String comp) {
     	ComponentManager<BukkitComponent> components = CommandBook.inst().getComponentManager();
     	for (BukkitComponent component : components.getComponents()) {
     		String compName = component.getInformation().friendlyName();
     		if (comp.equalsIgnoreCase(compName)) {
     			return component;
     		}
     	}
     	return null;
     }
 
 	public class Commands {
 		@Command(aliases = {"components", "comp"}, desc = "Gets a list of components running on this server.")
 		@CommandPermissions({"ecomponents.components"})
 		public void components(CommandContext args, CommandSender sender) {
			Player player = Bukkit.getPlayerExact(sender.getName());
			getComponents(player);
 		}
 
 		@Command(aliases = {"componentmanager", "compmana"}, usage = "[enable|disable|reload] [component]", desc = "Enable, Disable, Reload Components")
 		@CommandPermissions({"ecomponents.manage"})
 		public void componentmanager(CommandContext args, CommandSender player) {
 			if (args.argsLength() == 2) {
 				String comp = args.getString(1);
 
 				if (args.getString(0).equalsIgnoreCase("enable")) {
 					try {
 						checkComponent(comp).enable();
 						player.sendMessage(ChatColor.GREEN + comp + " is enabled.");
 					} catch (Exception e) {
 						player.sendMessage(ChatColor.RED + comp + " is not a valid component.");
 					}
 				} else if(args.getString(0).equalsIgnoreCase("disable")) {
 					try {
 						checkComponent(comp).disable();
 						player.sendMessage(ChatColor.RED + comp + " is disabled.");
 					} catch (Exception e) {
 						player.sendMessage(ChatColor.RED + comp + " is not a valid component.");
 					}
 				} else if(args.getString(0).equalsIgnoreCase("reload")) {
 					try {
 						checkComponent(comp).reload();
 						player.sendMessage(ChatColor.GOLD + comp + " is reloaded.");
 					} catch (Exception e) {
 						player.sendMessage(ChatColor.RED + comp + " is not a valid component.");
 					}
 				}
 				
 			}
 		}
 	}
 }
