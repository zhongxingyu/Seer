 package de.philworld.bukkit.magicsigns.signs;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.philworld.bukkit.magicsigns.InvalidSignException;
 import de.philworld.bukkit.magicsigns.MSMsg;
 import de.philworld.bukkit.magicsigns.MagicSignInfo;
 import de.philworld.bukkit.magicsigns.util.BlockLocation;
 
 /**
  * A sign that adds some health to a player.
  * 
  * @see {@link HealthSign} - Sets the health directly to a value.
  */
 @MagicSignInfo(
 		name = "Heal",
 		friendlyName = "Heal sign",
 		description = "A sign that adds some health to a player.",
 		buildPerm = "magicsigns.heal.create",
 		usePerm = "magicsigns.heal.use")
 public class HealSign extends PurchasableMagicSign {
 
 	private int healAmount = 20;
 
 	public HealSign(BlockLocation location, String[] lines) throws InvalidSignException {
 		super(location, lines);
 
 		if (!lines[1].isEmpty()) {
 			try {
 				healAmount = new Integer(lines[1]);
 			} catch (NumberFormatException e) {
 				throw new InvalidSignException("The amount on line 2 must be a number or empty!");
 			}
 		}
 	}
 
 	@Override
 	public void onRightClick(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 
		int newHealth = p.getHealth() + healAmount;
 
 		if (newHealth > p.getMaxHealth()) {
 			event.getPlayer().setHealth(p.getMaxHealth());
 		} else {
 			p.setHealth(newHealth);
 		}
 
 		MSMsg.HEAL_SUCCESS.send(p);
 	}
 
 }
