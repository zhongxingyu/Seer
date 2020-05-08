 package de.philworld.bukkit.magicsigns.signs.command;
 
 import java.util.LinkedList;
import java.util.List;
 
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
 
	private static List<String> removeSlashes(List<String> list)
			throws IllegalArgumentException {
		List<String> result = new LinkedList<String>();
		for (String cmd : list) {
			if (cmd.charAt(0) == '/') {
				result.add(cmd.substring(1));
			} else {
				throw new IllegalArgumentException(
						"All commands must begin with a slash!");
			}
		}
		return result;
	}

 	protected LinkedList<String> commands = new LinkedList<String>();
 
 	public CommandSign(Block sign, String[] lines) throws InvalidSignException {
 		super(sign, lines);
		try {
			commands.addAll(removeSlashes(MacroUtil.format(lines[1] + lines[2],
					config.getMacros())));
		} catch (IllegalArgumentException e) {
			throw new InvalidSignException(e.getMessage());
		}
 	}
 
 	@Override
 	public void onRightClick(PlayerInteractEvent event) {
		for (String cmd : commands) {
 			Bukkit.getServer().dispatchCommand(event.getPlayer(),
 					cmd.replace("%p", event.getPlayer().getName()));
 		}
 
 	}
 
 }
