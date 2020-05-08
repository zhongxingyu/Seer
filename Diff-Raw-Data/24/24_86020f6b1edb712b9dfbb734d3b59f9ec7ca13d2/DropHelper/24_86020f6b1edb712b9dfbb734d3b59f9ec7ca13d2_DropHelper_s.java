 /**
  * DropHelpers.java
  * Purpose: Handles all entity dropping for the plugin.
  * 
  * @version 1.2.0 11/5/12
  * @author Scott Woodward
  */
 package com.scottwoodward.headhunter.helpers;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 
 public class DropHelper {
 	/**
 	 * Called when the plugin is enabled upon server startup. Registers all
 	 * events and commands for the plugin
 	 * 
 	 * @param head
 	 *            is the type of head to be dropped
 	 * @param loc
 	 *            is the location to drop the head
 	 * @param name
 	 *            is the player name to be put on a human skull (null if not a
 	 *            human skull)
 	 * @param world
 	 *            is the world for the drop to occur in
 	 */
 	public static void drop(HeadType head, Location loc, String name, World world, Player killer) {
 		int amount = 0;
 
 		// Check for killers weapon of choice. If it is looting, determine level
 		// and then give user that level
 		if (ConfigHelper.getLooting() == true) {
 			ItemStack itemKill = killer.getItemInHand();
 			if (itemKill != null) {
 				if (itemKill.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
 					int totalLevelOfEnchantments = itemKill.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
 					for (int i = 0; i < totalLevelOfEnchantments; i++) {
 						if (shouldDrop(head, name)) {
 							amount++;
 						}
 					}
 				}
 			}
 		}
 		// Check for normally applied drop.
 		if (shouldDrop(head, name))
 			amount++;
 		if (amount != 0) {
 			ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, amount, (short) head.getValue());
 			String headName = getName(name);
 			if(headName != null){
				ItemMeta meta = itemStack.getItemMeta();
				meta.setDisplayName(headName);
 				itemStack.setItemMeta(meta);
 			}
			Item drop = world.dropItemNaturally(loc, itemStack);
			if (name != null) {
				drop.setItemStack(setSkin(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), name));
 			}
 		}
 	}
 
 	private static boolean shouldDrop(HeadType head, String name) {
 		double fraction = Math.random();
 		double chance = 0;
 		if (head.getValue() == 0 && ConfigHelper.getBoolean("Skeleton")) {
 			chance = ConfigHelper.getDropChance("Skeleton");
 		} else if (head.getValue() == 1 && ConfigHelper.getBoolean("WitherSkeleton")) {
 			chance = ConfigHelper.getDropChance("WitherSkeleton");
 		} else if (head.getValue() == 2 && ConfigHelper.getBoolean("Zombie")) {
 			chance = ConfigHelper.getDropChance("Zombie");
 		} else if (head.getValue() == 3) {
 			if (name.equals("MHF_Blaze")) {
 				if(ConfigHelper.getBoolean("Blaze")){
 					chance = ConfigHelper.getDropChance("Blaze");
 				}
 			}  else if (name.equals("MHF_CaveSpider")) {
 				if(ConfigHelper.getBoolean("CaveSpider")){
 					chance = ConfigHelper.getDropChance("CaveSpider");
 				}
 			}  else if (name.equals("MHF_Chicken")) {
 				if(ConfigHelper.getBoolean("Chicken")){
 					chance = ConfigHelper.getDropChance("Chicken");
 				}
 			}  else if (name.equals("MHF_Cow")) {
 				if( ConfigHelper.getBoolean("Cow")){
 					chance = ConfigHelper.getDropChance("Cow");
 				}
 			}  else if (name.equals("MHF_Ghast")) {
 				if(ConfigHelper.getBoolean("Ghast")){
 					chance = ConfigHelper.getDropChance("Ghast");
 				}
 			}  else if (name.equals("MHF_LavaSlime")) {
 				if(ConfigHelper.getBoolean("MagmaCube")){
 					chance = ConfigHelper.getDropChance("MagmaCube");
 				}
 			}  else if (name.equals("MHF_MushroomCow")) {
 				if(ConfigHelper.getBoolean("Mooshroom")){
 					chance = ConfigHelper.getDropChance("Mooshroom");
 				}
 			}  else if (name.equals("MHF_Pig")) {
 				if(ConfigHelper.getBoolean("Pig")){
 					chance = ConfigHelper.getDropChance("Pig");
 				}
 			}  else if (name.equals("MHF_PigZombie")) {
 				if(ConfigHelper.getBoolean("ZombiePigman")){
 					chance = ConfigHelper.getDropChance("ZombiePigman");
 				}
 			}  else if (name.equals("MHF_Sheep")) {
 				if(ConfigHelper.getBoolean("Sheep")){
 					chance = ConfigHelper.getDropChance("Sheep");
 				}
 			}  else if (name.equals("MHF_Slime")) {
 				if(ConfigHelper.getBoolean("Slime")){
 					chance = ConfigHelper.getDropChance("Slime");
 				}
 			}  else if (name.equals("MHF_Spider")) {
 				if(ConfigHelper.getBoolean("Spider")){
 					chance = ConfigHelper.getDropChance("Spider");
 				}
 			}  else if (name.equals("MHF_Squid")) {
 				if(ConfigHelper.getBoolean("Squid")){
 					chance = ConfigHelper.getDropChance("Squid");
 				}
 			}  else if (name.equals("MHF_Villager")) {
 				if(ConfigHelper.getBoolean("Villager")){
 					chance = ConfigHelper.getDropChance("Villager");
 				}
 			}else if (name.equals("MHF_Ocelot")) {
 				if(ConfigHelper.getBoolean("Cat")){
 					chance = ConfigHelper.getDropChance("Cat");
 				}
 			}  else if (name.equals("MHF_Golem")) {
 				if(ConfigHelper.getBoolean("Golem")){
 					chance = ConfigHelper.getDropChance("Golem");
 				}
 			}  else if (name.equals("MHF_Enderman")) {
 				if(ConfigHelper.getBoolean("Enderman")){
 					chance = ConfigHelper.getDropChance("Enderman");
 				}
 			} else if(ConfigHelper.getBoolean("Player")){
 				chance = ConfigHelper.getDropChance("Player");
 			}
 
 		}  else if (head.getValue() == 4 && ConfigHelper.getBoolean("Creeper")) {
 			chance = ConfigHelper.getDropChance("Creeper");
 		}
 		if (chance >= 100) {
 			return true;
 		} else if (chance <= 0) {
 			return false;
 		} else {
 			return ((fraction * 100) <= chance);
 		}
 	}
 
 	private static ItemStack setSkin(ItemStack item, String nick) {
 		SkullMeta meta = (SkullMeta) item.getItemMeta();
 		meta.setOwner(nick);
 		item.setItemMeta(meta);
 		return item;
 	}
 
 	private static String getName(String name){
		String headName = null;
 		if (name.equals("MHF_Blaze")) {
 			headName = "Blaze Head";
 		}  else if (name.equals("MHF_CaveSpider")) {
 			headName = "Cave Spider Head";
 		}  else if (name.equals("MHF_Chicken")) {
 			headName = "Chicken Head";
 		}  else if (name.equals("MHF_Cow")) {
 			headName = "Cow Head";
 		}  else if (name.equals("MHF_Ghast")) {
 			headName = "Ghast Head";
 		}  else if (name.equals("MHF_LavaSlime")) {
 			headName = "Magma Cube Head";
 		}  else if (name.equals("MHF_MushroomCow")) {
 			headName = "Mooshroom Head";
 		}  else if (name.equals("MHF_Pig")) {
 			headName = "Pig Head";
 		}  else if (name.equals("MHF_PigZombie")) {
 			headName = "Zombie Pigman Head";
 		}  else if (name.equals("MHF_Sheep")) {
 			headName = "Sheep Head";
 		}  else if (name.equals("MHF_Slime")) {
 			headName = "Slime Head";
 		}  else if (name.equals("MHF_Spider")) {
 			headName = "Spider Head";
 		}  else if (name.equals("MHF_Squid")) {
 			headName = "Squid Head";
 		}  else if (name.equals("MHF_Villager")) {
 			headName = "Villager Head";
 		}  else if (name.equals("MHF_Ocelot")) {
 			headName = "Ocelot Head";
 		}  else if (name.equals("MHF_Golem")) {
 			headName = "Iron Golem Head";
 		}  else if (name.equals("MHF_Enderman")) {
 			headName = "Enderman Head";
 		}
 		return headName;
 	}
 }
