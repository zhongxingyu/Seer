 package de.philworld.bukkit.magicsigns.signs;
 
 import org.bukkit.GameMode;
 import org.bukkit.block.Block;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.philworld.bukkit.magicsigns.InvalidSignException;
 import de.philworld.bukkit.magicsigns.MagicSignInfo;
 
 @MagicSignInfo(
 		friendlyName = "Survival Mode sign",
 		description = "A sign that sets the player's game mode to Creative Mode.",
 		buildPerm = "magicsigns.survival.create",
 		usePerm = "magicsigns.survival.use")
 public class SurvivalModeSign extends PurchasableMagicSign {
 
 	public static boolean takeAction(Block sign, String[] lines) {
		return lines[0].equalsIgnoreCase("[Surival]");
 	}
 
 	public SurvivalModeSign(Block sign, String[] lines)
 			throws InvalidSignException {
 		super(sign, lines);
 	}
 
 	@Override
 	public void onRightClick(PlayerInteractEvent event) {
 		event.getPlayer().setGameMode(GameMode.SURVIVAL);
 	}
 
 }
