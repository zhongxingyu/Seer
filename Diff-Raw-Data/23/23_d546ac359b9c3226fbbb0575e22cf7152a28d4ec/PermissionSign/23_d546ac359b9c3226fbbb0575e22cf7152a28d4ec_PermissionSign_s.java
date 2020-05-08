 package de.philworld.bukkit.magicsigns.signs.permission;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.philworld.bukkit.magicsigns.InvalidSignException;
 import de.philworld.bukkit.magicsigns.MagicSignInfo;
 import de.philworld.bukkit.magicsigns.MagicSigns;
 import de.philworld.bukkit.magicsigns.config.MacroConfiguration;
 import de.philworld.bukkit.magicsigns.signs.PurchasableMagicSign;
 import de.philworld.bukkit.magicsigns.util.MacroUtil;
 
 @MagicSignInfo(
 		buildPerm = "magicsigns.permission.create",
 		description = "A sign that gives a player permissions.",
 		friendlyName = "Permission sign",
 		usePerm = "magicsigns.permission.use")
 public class PermissionSign extends PurchasableMagicSign {
 
 	public static class LocalConfiguration extends MacroConfiguration {
 		public LocalConfiguration() {
 			super("permission-macros");
 		}
 	}
 
 	public static LocalConfiguration config = new LocalConfiguration();
 
 	public static void loadConfig(ConfigurationSection section) {
 		config.load(section);
 	}
 
 	private final List<String> permissions;
 
 	public static boolean takeAction(Block sign, String[] lines) {
 		return lines[0].equalsIgnoreCase("[Permission]")
 				&& MagicSigns.getPermission() != null;
 	}
 
 	public PermissionSign(Block sign, String[] lines)
 			throws InvalidSignException {
 		super(sign, lines);
 		permissions = MacroUtil.format(lines[1] + lines[2], config.getMacros());
 		if (permissions.size() == 0)
 			throw new InvalidSignException("No permissions found!");
 	}
 
 	@Override
 	public void onRightClick(PlayerInteractEvent event) {
 		for (String perm : permissions) {
 			MagicSigns.getPermission().playerAdd(event.getPlayer(), perm);
 		}
 		event.getPlayer().sendMessage(
 				ChatColor.GREEN + "You got some permissions!");
 	}
 }
