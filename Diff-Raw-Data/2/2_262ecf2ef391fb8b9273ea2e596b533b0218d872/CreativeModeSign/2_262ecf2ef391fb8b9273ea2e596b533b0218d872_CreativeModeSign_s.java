 package de.philworld.bukkit.magicsigns.signs;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.philworld.bukkit.magicsigns.InvalidSignException;
 import de.philworld.bukkit.magicsigns.MagicSignInfo;
 
 @MagicSignInfo(
		name = "Clear",
 		friendlyName = "Creative Mode sign",
 		description = "Sets the player's game mode to Creative Mode.",
 		buildPerm = "magicsigns.creative.create",
 		usePerm = "magicsigns.creative.use")
 public class CreativeModeSign extends PurchasableMagicSign {
 
 	public CreativeModeSign(Location location, String[] lines)
 			throws InvalidSignException {
 		super(location, lines);
 	}
 
 	@Override
 	public void onRightClick(PlayerInteractEvent event) {
 		event.getPlayer().setGameMode(GameMode.CREATIVE);
 	}
 
 }
