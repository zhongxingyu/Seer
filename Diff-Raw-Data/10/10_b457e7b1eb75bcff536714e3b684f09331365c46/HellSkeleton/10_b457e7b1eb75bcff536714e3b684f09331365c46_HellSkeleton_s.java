 package plugin.moremobs.Mobs;
 
 import org.bukkit.Color;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 
 public class HellSkeleton {
 	
 	public static boolean isHellSkeleton(Entity entity) {
 		if(entity instanceof Skeleton) {
 			LeatherArmorMeta chestMeta;
 			ItemStack HSC = new ItemStack(Material.LEATHER_CHESTPLATE, 1, (short) -98789);
 			chestMeta = (LeatherArmorMeta) HSC.getItemMeta();
 			chestMeta.setColor(Color.fromRGB(218, 28, 28));
 			HSC.setItemMeta(chestMeta);
 			Skeleton HS = (Skeleton) entity;
 			if(HS.getEquipment().getChestplate().equals(new ItemStack(Material.LEATHER_CHESTPLATE, 1, (short) -98789))) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void spawnHellSkeleton(Location loc, int amount) {
 		int i = 0 ;
 		while(i < amount) {
 			Skeleton HS = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
 			LeatherArmorMeta legMeta;
 			ItemStack HSLeg = new ItemStack(Material.LEATHER_LEGGINGS, 1);
 			legMeta = (LeatherArmorMeta) HSLeg.getItemMeta();
 			legMeta.setColor(Color.fromRGB(218, 28, 28));
 			HSLeg.setItemMeta(legMeta);
 			LeatherArmorMeta chestMeta;
 			ItemStack HSChest = new ItemStack(Material.LEATHER_CHESTPLATE, 1, (short) -98789);
 			chestMeta = (LeatherArmorMeta) HSChest.getItemMeta();
 			chestMeta.setColor(Color.fromRGB(218, 28, 28));
 			HSChest.setItemMeta(chestMeta);
 			LeatherArmorMeta bootsMeta;
 			ItemStack HSBoots = new ItemStack(Material.LEATHER_BOOTS, 1);
 			bootsMeta = (LeatherArmorMeta) HSBoots.getItemMeta();
 			bootsMeta.setColor(Color.fromRGB(218, 28, 28));
 			HSBoots.setItemMeta(bootsMeta);
 			HS.getEquipment().setChestplate(HSChest);
			HS.getEquipment().setChestplate(HSLeg);
 			HS.getEquipment().setBoots(HSBoots);
 			HS.getEquipment().setChestplateDropChance(0.30F);
 			HS.getEquipment().setLeggingsDropChance(0.30F);
 			HS.getEquipment().setBootsDropChance(0.40F);
 			HS.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 2147483647, 1));
			HS.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, 1));
			HS.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2147483647, 1));
 			HS.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2147483647, 10));
			HS.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2147483647, 1));
 			HS.setFireTicks(2147483647);
 			i++;
 		}
 	}
 }
