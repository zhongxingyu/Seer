 package de.philworld.bukkit.magicsigns.signs.command;
 
 import java.util.LinkedList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.philworld.bukkit.magicsigns.InvalidSignException;
 import de.philworld.bukkit.magicsigns.MagicSignInfo;
 import de.philworld.bukkit.magicsigns.config.MacroConfiguration;
 import de.philworld.bukkit.magicsigns.signs.PurchasableMagicSign;
 import de.philworld.bukkit.magicsigns.util.MacroUtil;
 
 /**
  * A sign that executes commands on the sign in the player's context.
  *
  * <code>%p</code> will be replaced with the player's name.
  *
  */
 @MagicSignInfo(
 		friendlyName = "Command sign",
 		description = "A sign that executes commands on the sign in the player's context.",
 		buildPerm = "magicsigns.command.create",
 		usePerm = "magicsigns.command.use")
 public class CommandSign extends PurchasableMagicSign {
 
 	/**
 	 * Configuration that saves all macros.
 	 */
 	public static class LocalConfiguration extends MacroConfiguration {
 		public LocalConfiguration() {
 			super("command-macros");
 		}
 	}
 
 	public static LocalConfiguration config = new LocalConfiguration();
 
 	public static void loadConfig(ConfigurationSection section) {
 		config.load(section);
 	}
 
 	public static boolean takeAction(Block sign, String[] lines) {
 		return lines[0].equalsIgnoreCase("[Command]");
 	}
 
 	protected LinkedList<String> commands = new LinkedList<String>();
 
 	public CommandSign(Block sign, String[] lines) throws InvalidSignException {
 		super(sign, lines);
		commands.addAll(MacroUtil.format(lines[1] + lines[2], config.getMacros()));
 	}
 
 	@Override
 	public void onRightClick(PlayerInteractEvent event) {
		for (String cmd : this.commands) {
 			Bukkit.getServer().dispatchCommand(event.getPlayer(),
 					cmd.replace("%p", event.getPlayer().getName()));
 		}
 
 	}
 
 }
