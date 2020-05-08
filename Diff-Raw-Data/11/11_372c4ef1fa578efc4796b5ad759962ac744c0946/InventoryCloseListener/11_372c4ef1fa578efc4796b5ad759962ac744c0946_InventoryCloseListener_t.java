 package net.minesocket.potionpermissions;
 
 import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class InventoryCloseListener implements Listener {
 
 
 	public static PotionPermissions plugin;
 
 	public InventoryCloseListener(PotionPermissions instance) {
 		plugin = instance;
 	}
 
	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent event) {
 		Player p = (Player) event.getPlayer();
 		// Speed
 		if (p.hasPermission("potionpermissions.speed.one")) {
 			p.removePotionEffect(PotionEffectType.SPEED);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.speed.two")) {
 			p.removePotionEffect(PotionEffectType.SPEED);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 18000, 1));
 		} if (p.hasPermission("potionpermissions.speed.three")) {
 			p.removePotionEffect(PotionEffectType.SPEED);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 18000, 2));
 			// Blindness
 		}if (p.hasPermission("potionpermissions.blindness.one")) {
 			p.removePotionEffect(PotionEffectType.BLINDNESS);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.blindness.two")) {
 			p.removePotionEffect(PotionEffectType.BLINDNESS);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 18000, 1));
 		} if (p.hasPermission("potionpermissions.blindness.three")) {
 			p.removePotionEffect(PotionEffectType.BLINDNESS);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 18000, 2));
 		} // Confusion
 		if (p.hasPermission("potionpermissions.confusion.one")) {
 			p.removePotionEffect(PotionEffectType.CONFUSION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.confusion.two")) {
 			p.removePotionEffect(PotionEffectType.CONFUSION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 18000, 1));
 		} if (p.hasPermission("potionpermissions.confusion.three")) {
 			p.removePotionEffect(PotionEffectType.CONFUSION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 18000, 2));
 		} // Damage Resistance
 		if (p.hasPermission("potionpermissions.damageresistance.one")) {
 			p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.damageresistance.two")) {
 			p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 18000, 1));
 		} if (p.hasPermission("potionpermissions.damageresistance.three")) {
 			p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 18000, 2));
 		} // Fast Digging
 		if (p.hasPermission("potionpermissions.fastdigging.one")) {
 			p.removePotionEffect(PotionEffectType.FAST_DIGGING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.fastdigging.two")) {
 			p.removePotionEffect(PotionEffectType.FAST_DIGGING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 18000, 1));
 		} if (p.hasPermission("potionpermissions.fastdigging.three")) {
 			p.removePotionEffect(PotionEffectType.FAST_DIGGING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 18000, 2));
 		} // Fire Resistance
 		if (p.hasPermission("potionpermissions.fireresistance.one")) {
 			p.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.fireresistance.two")) {
 			p.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 18000, 1));
 		} if (p.hasPermission("potionpermissions.fireresistance.three")) {
 			p.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 18000, 2));
 		} // Harm
 		if (p.hasPermission("potionpermissions.harm.one")) {
 			p.removePotionEffect(PotionEffectType.HARM);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.harm.two")) {
 			p.removePotionEffect(PotionEffectType.HARM);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 18000, 1));
 		} if (p.hasPermission("potionpermissions.harm.three")) {
 			p.removePotionEffect(PotionEffectType.HARM);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 18000, 2));
 		}// Heal
 		if (p.hasPermission("potionpermissions.heal.one")) {
 			p.removePotionEffect(PotionEffectType.HEAL);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.heal.two")) {
 			p.removePotionEffect(PotionEffectType.HEAL);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 18000, 1));
 		} if (p.hasPermission("potionpermissions.heal.three")) {
 			p.removePotionEffect(PotionEffectType.HEAL);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 18000, 2));
 		}// Hunger
 		if (p.hasPermission("potionpermissions.hunger.one")) {
 			p.removePotionEffect(PotionEffectType.HEAL);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.hunger.two")) {
 			p.removePotionEffect(PotionEffectType.HEAL);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 18000, 1));
 		} if (p.hasPermission("potionpermissions.hunger.three")) {
 			p.removePotionEffect(PotionEffectType.HEAL);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 18000, 2));
 		}// Increased Damage
 		if (p.hasPermission("potionpermissions.increasedamage.one")) {
 			p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.increasedamage.two")) {
 			p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 18000, 1));
 		} if (p.hasPermission("potionpermissions.increasedamage.three")) {
 			p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 18000, 2));
 		}// Invisibility
 		if (p.hasPermission("potionpermissions.invisibility.one")) {
 			p.removePotionEffect(PotionEffectType.INVISIBILITY);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.invisibility.two")) {
 			p.removePotionEffect(PotionEffectType.INVISIBILITY);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 18000, 1));
 		} if (p.hasPermission("potionpermissions.invisibility.three")) {
 			p.removePotionEffect(PotionEffectType.INVISIBILITY);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 18000, 2));
 		}// Jump
 		if (p.hasPermission("potionpermissions.jump.one")) {
 			p.removePotionEffect(PotionEffectType.JUMP);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.jump.two")) {
 			p.removePotionEffect(PotionEffectType.JUMP);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 18000, 1));
 		} if (p.hasPermission("potionpermissions.jump.three")) {
 			p.removePotionEffect(PotionEffectType.JUMP);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 18000, 2));
 		}
 		// Night Vision
 		if (p.hasPermission("potionpermissions.nightvision.one")) {
 			p.removePotionEffect(PotionEffectType.NIGHT_VISION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.nightvision.two")) {
 			p.removePotionEffect(PotionEffectType.NIGHT_VISION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
 		} if (p.hasPermission("potionpermissions.nightvision.three")) {
 			p.removePotionEffect(PotionEffectType.NIGHT_VISION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 2));
 		}// Poison
 		if (p.hasPermission("potionpermissions.poison.one")) {
 			p.removePotionEffect(PotionEffectType.POISON);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.poison.two")) {
 			p.removePotionEffect(PotionEffectType.POISON);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 18000, 1));
 		} if (p.hasPermission("potionpermissions.poison.three")) {
 			p.removePotionEffect(PotionEffectType.POISON);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 18000, 2));
 		}// Regeneration
 		if (p.hasPermission("potionpermissionsregeneration.one")) {
 			p.removePotionEffect(PotionEffectType.REGENERATION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissionsregeneration.two")) {
 			p.removePotionEffect(PotionEffectType.REGENERATION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 18000, 1));
 		} if (p.hasPermission("potionpermissionsregeneration.three")) {
 			p.removePotionEffect(PotionEffectType.REGENERATION);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 18000, 2));
 		}// Slow
 		if (p.hasPermission("potionpermissions.slow.one")) {
 			p.removePotionEffect(PotionEffectType.SLOW);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.slow.two")) {
 			p.removePotionEffect(PotionEffectType.SLOW);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 18000, 1));
 		} if (p.hasPermission("potionpermissions.slow.three")) {
 			p.removePotionEffect(PotionEffectType.SLOW);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 18000, 2));
 		}// Slow Digging
 		if (p.hasPermission("potionpermissions.slowdigging.one")) {
 			p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.slowdigging.two")) {
 			p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 18000, 1));
 		} if (p.hasPermission("potionpermissions.slowdigging.three")) {
 			p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 18000, 2));
 		}// Water Breathing
 		if (p.hasPermission("potionpermissions.waterbreathing.one")) {
 			p.removePotionEffect(PotionEffectType.WATER_BREATHING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.waterbreathing.two")) {
 			p.removePotionEffect(PotionEffectType.WATER_BREATHING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 18000, 1));
 		} if (p.hasPermission("potionpermissions.waterbreathing.three")) {
 			p.removePotionEffect(PotionEffectType.WATER_BREATHING);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 18000, 2));
 		} // Weakness
 		if (p.hasPermission("potionpermissions.weakness.one")) {
 			p.removePotionEffect(PotionEffectType.WEAKNESS);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.weakness.two")) {
 			p.removePotionEffect(PotionEffectType.WEAKNESS);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 18000, 1));
 		} if (p.hasPermission("potionpermissions.weakness.three")) {
 			p.removePotionEffect(PotionEffectType.WEAKNESS);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 18000, 2));
 		}// Wither
 		if (p.hasPermission("potionpermissions.wither.one")) {
 			p.removePotionEffect(PotionEffectType.WITHER);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 18000, 0));
 		}
 		if (p.hasPermission("potionpermissions.wither.two")) {
 			p.removePotionEffect(PotionEffectType.WITHER);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 18000, 1));
 		} if (p.hasPermission("potionpermissions.wither.three")) {
 			p.removePotionEffect(PotionEffectType.WITHER);
 			p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 18000, 2));
 		}
 	}
 }
