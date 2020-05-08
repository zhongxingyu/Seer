 package Staartvin.ArmorControl;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * @author Staartvin
  *
  */
 public class Methods {
 	
 	ArmorControl plugin;
 	
 	public Methods (ArmorControl plugin) {
 		this.plugin = plugin;
 	}
 	
 	protected String checkWeaponType(int ID) {
 		if (ID == 268) return "wood";
 		else if (ID == 272) return "stone";
 		else if (ID == 267) return "iron";
 		else if (ID == 283) return "gold";
 		else if (ID == 276) return "diamond";
 		else if (ID == 261) return "bow";
 		else return "ID unknown";
 	}
 	
 	protected void loadConfiguration() {
 		plugin.getConfig().options().header(
 				"ArmorControl v" + plugin.getDescription().getVersion() + " Config"
 				+ "\nDo not touch the 'upgrade' part. It will mess up the config."
 				+ "\nLatest CraftBukkit version tested on: 1.4.6-R0.1 #2570"
 				+ "\n\nThanks for using Armor Control! Questions? http://dev.bukkit.org/server-mods/armor-control/");
 		
 		plugin.getConfig().addDefault("verboseLogging", true);
 		
 		plugin.getConfig().addDefault("Armor.Leather", 5);
 		plugin.getConfig().addDefault("Armor.Chain", 11);
 		plugin.getConfig().addDefault("Armor.Gold", 17);
 		plugin.getConfig().addDefault("Armor.Iron", 22);
 		plugin.getConfig().addDefault("Armor.Diamond", 30);
 		
 		plugin.getConfig().addDefault("Tools.Wood", 5);
 		plugin.getConfig().addDefault("Tools.Stone", 11);
 		plugin.getConfig().addDefault("Tools.Gold", 17);
 		plugin.getConfig().addDefault("Tools.Iron", 22);
 		plugin.getConfig().addDefault("Tools.Diamond", 30);
 		
 		plugin.getConfig().addDefault("Weapons.Wood", 5);
 		plugin.getConfig().addDefault("Weapons.Stone", 11);
 		plugin.getConfig().addDefault("Weapons.Gold", 17);
 		plugin.getConfig().addDefault("Weapons.Iron", 22);
 		plugin.getConfig().addDefault("Weapons.Diamond", 30);
 		
 		plugin.getConfig().addDefault("bowLevel", 15);
 		
 		plugin.getConfig().addDefault("UseArmorControl", true);
 		plugin.getConfig().addDefault("UseToolControl", false);
 		plugin.getConfig().addDefault("UseWeaponControl", false);
 		plugin.getConfig().addDefault("Upgrade.12-to-13", true);
 		
 		plugin.getConfig().options().copyDefaults(true);
 		plugin.saveConfig();
 	}
 	
 	protected void upgradeConfig(String versionfrom_to) {
 		if (versionfrom_to.equals("1.2-to-1.3")) {
 			
 			if (!plugin.getConfig().getBoolean("Upgrade.12-to-13")) {
 				return;
 			}
 			
 			//Convert old config
 			plugin.getConfig().set("Armor.Leather", plugin.getConfig().getInt("leatherArmorLevel"));
 			plugin.getConfig().set("Armor.Chain", plugin.getConfig().getInt("chainArmorLevel"));
 			plugin.getConfig().set("Armor.Gold", plugin.getConfig().getInt("goldArmorLevel"));
 			plugin.getConfig().set("Armor.Iron", plugin.getConfig().getInt("ironArmorLevel"));
 			plugin.getConfig().set("Armor.Diamond", plugin.getConfig().getInt("diamondArmorLevel"));
 			
 			plugin.getConfig().set("Tools.Wood", plugin.getConfig().getInt("woodToolLevel"));
 			plugin.getConfig().set("Tools.Stone", plugin.getConfig().getInt("stoneToolLevel"));
 			plugin.getConfig().set("Tools.Gold", plugin.getConfig().getInt("goldToolLevel"));
 			plugin.getConfig().set("Tools.Iron", plugin.getConfig().getInt("ironToolLevel"));
 			plugin.getConfig().set("Tools.Diamond", plugin.getConfig().getInt("diamondToolLevel"));
 			
 			plugin.getConfig().set("Weapons.Wood", plugin.getConfig().getInt("woodWeaponLevel"));
 			plugin.getConfig().set("Weapons.Stone", plugin.getConfig().getInt("stoneWeaponLevel"));
 			plugin.getConfig().set("Weapons.Gold", plugin.getConfig().getInt("goldWeaponLevel"));
 			plugin.getConfig().set("Weapons.Iron", plugin.getConfig().getInt("ironWeaponLevel"));
 			plugin.getConfig().set("Weapons.Diamond", plugin.getConfig().getInt("diamondWeaponLevel"));
 			
 			// Remove old options
 			plugin.getConfig().set("leatherArmorLevel", null);
 			plugin.getConfig().set("chainArmorLevel", null);
 			plugin.getConfig().set("goldArmorLevel", null);
 			plugin.getConfig().set("ironArmorLevel", null);
 			plugin.getConfig().set("diamondArmorLevel", null);
 			
 			plugin.getConfig().set("woodToolLevel", null);
 			plugin.getConfig().set("stoneToolLevel", null);
 			plugin.getConfig().set("goldToolLevel", null);
 			plugin.getConfig().set("ironToolLevel", null);
 			plugin.getConfig().set("diamondToolLevel", null);
 			
 			plugin.getConfig().set("woodWeaponLevel", null);
 			plugin.getConfig().set("stoneWeaponLevel", null);
 			plugin.getConfig().set("goldWeaponLevel", null);
 			plugin.getConfig().set("ironWeaponLevel", null);
 			plugin.getConfig().set("diamondWeaponLevel", null);
 			
 			// Notice admin
 			if (plugin.getConfig().getBoolean("verboseLogging")) {
 				System.out.print("[ArmorControl] Old config has been converted to new config");
 			}
 			plugin.getConfig().set("Upgrade.12-to-13", false);
 			
 			plugin.saveConfig();
 		}
 	}
 	/**
 	 * Checks level of experience of a player against level limit
 	 * @param player Player to check
 	 * @param mode Type of tool (wood, stone, iron, diamond, gold, bow, leather, chain)
 	 * @param item Type of item (weapon, tool or armor)
 	 * @return True if player has enough xp
 	 */
 	protected boolean checkLevel(Player player, String mode, String item) {
 		if (item.equals("weapon")) {
 			if (mode.equals("wood")) {if (player.getLevel() >= plugin.woodWeaponLevel) return true;}
 			else if (mode.equals("stone")) {if (player.getLevel() >= plugin.stoneWeaponLevel) return true;}
 			else if (mode.equals("gold")) {if (player.getLevel() >= plugin.goldWeaponLevel) return true;}
 			else if (mode.equals("iron")) {if (player.getLevel() >= plugin.ironWeaponLevel) return true;}
 			else if (mode.equals("diamond")) {if (player.getLevel() >= plugin.diamondWeaponLevel) return true;}
 			else if (mode.equals("bow")) {if (player.getLevel() >= plugin.bowLevel) return true;}
 		}
 		else if (item.equals("tool")) {
 			if (mode.equals("wood")) {if (player.getLevel() >= plugin.woodToolLevel) return true;}
 			else if (mode.equals("stone")) {if (player.getLevel() >= plugin.stoneToolLevel) return true;}
 			else if (mode.equals("gold")) {if (player.getLevel() >= plugin.goldToolLevel) return true;}
 			else if (mode.equals("iron")) {if (player.getLevel() >= plugin.ironToolLevel) return true;}
 			else if (mode.equals("diamond")) {if (player.getLevel() >= plugin.diamondToolLevel) return true;}	
 		}
 		else if (item.equals("armor")) {
 			if (mode.equals("leather")) {if (player.getLevel() >= plugin.leatherArmorLevel) return true;}
 			else if (mode.equals("chain")) {if (player.getLevel() >= plugin.chainArmorLevel) return true;}
 			else if (mode.equals("gold")) {if (player.getLevel() >= plugin.goldArmorLevel) return true;}
 			else if (mode.equals("iron")) {if (player.getLevel() >= plugin.ironArmorLevel) return true;}
 			else if (mode.equals("diamond")) {if (player.getLevel() >= plugin.diamondArmorLevel) return true;}	
 		}
 		else if (mode.equals("bow")) {if (player.getLevel() >= plugin.bowLevel) return true;}
 		
 		return false;
 	}
 	
 	/**
 	 * Reads limits from configuration file.
 	 */
 	protected void readLimits() {
 		try {
 			plugin.leatherArmorLevel = plugin.getConfig().getInt("Armor.Leather");
 			plugin.chainArmorLevel = plugin.getConfig().getInt("Armor.Chain");
 			plugin.goldArmorLevel = plugin.getConfig().getInt("Armor.Gold");
 			plugin.ironArmorLevel = plugin.getConfig().getInt("Armor.Iron");
 			plugin.diamondArmorLevel = plugin.getConfig().getInt("Armor.Diamond");
 			
 			plugin.woodToolLevel = plugin.getConfig().getInt("Tools.Wood");
 			plugin.stoneToolLevel = plugin.getConfig().getInt("Tools.Stone");
 			plugin.goldToolLevel = plugin.getConfig().getInt("Tools.Gold");
 			plugin.ironToolLevel = plugin.getConfig().getInt("Tools.Iron");
 			plugin.diamondToolLevel = plugin.getConfig().getInt("Tools.Diamond");
 			
 			plugin.woodWeaponLevel = plugin.getConfig().getInt("Weapons.Wood");
 			plugin.stoneWeaponLevel = plugin.getConfig().getInt("Weapons.Stone");
 			plugin.goldWeaponLevel = plugin.getConfig().getInt("Weapons.Gold");
 			plugin.ironWeaponLevel = plugin.getConfig().getInt("Weapons.Iron");
 			plugin.diamondWeaponLevel = plugin.getConfig().getInt("Weapons.Diamond");
 
 			plugin.bowLevel = plugin.getConfig().getInt("bowLevel");
 			
 			if (plugin.getConfig().getBoolean("verboseLogging")) {
 			System.out.print("[Armor Control] -------------------------------------------");
 			System.out.print("[Armor Control] Leather Armor level is set to: " + plugin.leatherArmorLevel);
 			System.out.print("[Armor Control] Chain Armor level is set to: " + plugin.chainArmorLevel);
 			System.out.print("[Armor Control] Gold Armor level is set to: " + plugin.goldArmorLevel);
 			System.out.print("[Armor Control] Iron Armor level is set to: " + plugin.ironArmorLevel);
 			System.out.print("[Armor Control] Diamond Armor level is set to: " + plugin.diamondArmorLevel);
 			System.out.print("[Armor Control] -------------------------------------------");
 			System.out.print("[Armor Control] Wooden Tool level is set to: " + plugin.woodToolLevel);
 			System.out.print("[Armor Control] Chain Tool level is set to: " + plugin.stoneToolLevel);
 			System.out.print("[Armor Control] Gold Tool level is set to: " + plugin.goldToolLevel);
 			System.out.print("[Armor Control] Iron Tool level is set to: " + plugin.ironToolLevel);
 			System.out.print("[Armor Control] Diamond Tool level is set to: " + plugin.diamondToolLevel);
 			System.out.print("[Armor Control] -------------------------------------------");
 			System.out.print("[Armor Control] Wooden Weapon level is set to: " + plugin.woodWeaponLevel);
 			System.out.print("[Armor Control] Chain Weapon level is set to: " + plugin.stoneWeaponLevel);
 			System.out.print("[Armor Control] Gold Weapon level is set to: " + plugin.goldWeaponLevel);
 			System.out.print("[Armor Control] Iron Weapon level is set to: " + plugin.ironWeaponLevel);
 			System.out.print("[Armor Control] Diamond Weapon level is set to: " + plugin.diamondWeaponLevel);
 			System.out.print("[Armor Control] Bow level is set to: " + plugin.bowLevel);
 			System.out.print("[Armor Control] -------------------------------------------");
 			}	
 		} catch (Exception e) {
 			System.out.print("[Armor Control] Could not parse levels in config to integers!");
 			System.out.print("[Armor Control] Switching to default levels!");
 		}
 	}
 	
 	/**
 	 * Gets level required to use/wear item/armor
 	 * @param mode Type of tool/armor (wood, stone, gold, iron, diamond, bow, leather, chain)
 	 * @param item Type of item (weapon, tool, armor)
 	 * @return Level required to use/wear item/armor.
 	 */
 	protected int findLevel(String mode, String item) {
 		if (item.equals("weapon")) {
			if (mode.equals("wood")) return plugin.woodWeaponLevel;
			else if (mode.equals("stone")) return plugin.stoneWeaponLevel;
			else if (mode.equals("gold")) return plugin.goldWeaponLevel;
			else if (mode.equals("iron")) return plugin.ironWeaponLevel;
			else if (mode.equals("diamond")) return plugin.diamondWeaponLevel;
 			else if (mode.equals("bow")) return plugin.bowLevel;
 		}
 		else if (item.equals("tool")) {
 			if (mode.equals("wood")) return plugin.woodToolLevel;
 			else if (mode.equals("stone")) return plugin.stoneToolLevel;
 			else if (mode.equals("gold")) return plugin.goldToolLevel;
 			else if (mode.equals("iron")) return plugin.ironToolLevel;
 			else if (mode.equals("diamond")) return plugin.diamondToolLevel;
 		}
 		else if (item.equals("armor")) {
 			if (mode.equals("leather")) return plugin.leatherArmorLevel;
 			else if (mode.equals("chain")) return plugin.chainArmorLevel;
 			else if (mode.equals("gold")) return plugin.goldArmorLevel;
 			else if (mode.equals("iron")) return plugin.ironArmorLevel;
 			else if (mode.equals("diamond")) return plugin.diamondArmorLevel;
 		}
 		return 0;
 	}
 	
 	protected void checkInventoryforArmor(Player player) {
 	    if (plugin.inv.getHelmet() != null) {
 	    	for (int i=0;i<plugin.helmetIDs.length;i++) {
 		    	if (plugin.helmetIDs[i] == plugin.inv.getHelmet().getType().getId()) {
 		    		
 		    		String mode;
 		    		if (plugin.inv.getHelmet().getType().getId() == 298) {
 		    			if (checkLevel(player, "leather", "armor")) {
 		    				break;
 		    			}
 		    			mode = "leather";
 		    		} else if (plugin.inv.getHelmet().getType().getId() == 302) {
 		    			if (checkLevel(player, "chain", "armor")) {
 		    				break;
 		    			}
 		    			mode = "chain";
 		    		} else if (plugin.inv.getHelmet().getType().getId() == 306) {
 		    			if (checkLevel(player, "iron", "armor")) {
 		    				break;
 		    			}
 		    			mode = "iron";
 		    		} else if (plugin.inv.getHelmet().getType().getId() == 310) {
 		    			if (checkLevel(player, "diamond", "armor")) {
 		    				break;
 		    			}
 		    			mode = "diamond";
 		    		} else if (plugin.inv.getHelmet().getType().getId() == 314) {
 		    			if (checkLevel(player, "gold", "armor")) {
 		    				break;
 		    			}
 		    			mode = "gold";
 		    		} else {
 		    			break;
 		    		}
 		    		plugin.armorPart = new ItemStack(plugin.inv.getHelmet().getType().getId());
 		    		plugin.armorPart.setDurability(plugin.inv.getHelmet().getDurability());
 		    		if (plugin.inv.getHelmet().getEnchantments().size() != 0) {
 		    			try {
 		    				plugin.armorPart.addEnchantments(plugin.inv.getHelmet().getEnchantments());
 		    			} catch (Exception e) {
 		    				plugin.armorPart.addUnsafeEnchantments(plugin.inv.getHelmet().getEnchantments());
 		    			}
 		    		}
 		    		plugin.inv.addItem(plugin.armorPart);
 		    		plugin.inv.setHelmet(plugin.air);
 		    		player.sendMessage(ChatColor.RED + "You cannot wear this helmet. You must be at least level: " + findLevel(mode, "armor"));
 		    		break;
 		    	}
 		    }
 	    }
 	    if (plugin.inv.getChestplate() != null) {
 	    	for (int i=0;i<plugin.chestplateIDs.length;i++) {
 		    	if (plugin.chestplateIDs[i] == plugin.inv.getChestplate().getType().getId()) {
 		    		
 		    		String mode;
 		    		if (plugin.inv.getChestplate().getType().getId() == 299) {
 		    			if (checkLevel(player, "leather", "armor")) {
 		    				break;
 		    			}
 		    			mode = "leather";
 		    		} else if (plugin.inv.getChestplate().getType().getId() == 303) {
 		    			if (checkLevel(player, "chain", "armor")) {
 		    				break;
 		    			}
 		    			mode = "chain";
 		    		} else if (plugin.inv.getChestplate().getType().getId() == 307) {
 		    			if (checkLevel(player, "iron", "armor")) {
 		    				break;
 		    			}
 		    			mode = "iron";
 		    		} else if (plugin.inv.getChestplate().getType().getId() == 311) {
 		    			if (checkLevel(player, "diamond", "armor")) {
 		    				break;
 		    			}
 		    			mode = "diamond";
 		    		} else if (plugin.inv.getChestplate().getType().getId() == 315) {
 		    			if (checkLevel(player, "gold", "armor")) {
 		    				break;
 		    			}
 		    			mode = "gold";
 		    		} else {
 		    			break;
 		    		}
 		    		plugin.armorPart = new ItemStack(plugin.inv.getChestplate().getType().getId());
 		    		plugin.armorPart.setDurability(plugin.inv.getChestplate().getDurability());
 		    		if (plugin.inv.getChestplate().getEnchantments().size() != 0) {
 		    			try {
 		    				plugin.armorPart.addEnchantments(plugin.inv.getChestplate().getEnchantments());
 		    			} catch (Exception e) {
 		    				plugin.armorPart.addUnsafeEnchantments(plugin.inv.getChestplate().getEnchantments());
 		    			}
 		    		}
 		    		plugin.inv.addItem(plugin.armorPart);
 		    		plugin.inv.setChestplate(plugin.air);
 		    		player.sendMessage(ChatColor.RED + "You cannot wear this chestplate. You must be at least level: " + findLevel(mode, "armor"));
 		    		break;
 		    		}
 		    	}
 		    }
 	    if (plugin.inv.getLeggings() != null) {
 	    	for (int i=0;i<plugin.leggingIDs.length;i++) {
 		    	if (plugin.leggingIDs[i] == plugin.inv.getLeggings().getType().getId()) {
 		    		
 		    		String mode;
 		    		if (plugin.inv.getLeggings().getType().getId() == 300) {
 		    			if (checkLevel(player, "leather", "armor")) {
 		    				break;
 		    			}
 		    			mode = "leather";
 		    		} else if (plugin.inv.getLeggings().getType().getId() == 304) {
 		    			if (checkLevel(player, "chain", "armor")) {
 		    				break;
 		    			}
 		    			mode = "chain";
 		    		} else if (plugin.inv.getLeggings().getType().getId() == 308) {
 		    			if (checkLevel(player, "iron", "armor")) {
 		    				break;
 		    			}
 		    			mode = "iron";
 		    		} else if (plugin.inv.getLeggings().getType().getId() == 312) {
 		    			if (checkLevel(player, "diamond", "armor")) {
 		    				break;
 		    			}
 		    			mode = "diamond";
 		    		} else if (plugin.inv.getLeggings().getType().getId() == 316) {
 		    			if (checkLevel(player, "gold", "armor")) {
 		    				break;
 		    			}
 		    			mode = "gold";
 		    		} else {
 		    			break;
 		    		}
 		    		plugin.armorPart = new ItemStack(plugin.inv.getLeggings().getType().getId());
 		    		plugin.armorPart.setDurability(plugin.inv.getLeggings().getDurability());
 		    		if (plugin.inv.getLeggings().getEnchantments().size() != 0) {
 		    			try {
 		    				plugin.armorPart.addEnchantments(plugin.inv.getLeggings().getEnchantments());
 		    			} catch (Exception e) {
 		    				plugin.armorPart.addUnsafeEnchantments(plugin.inv.getLeggings().getEnchantments());
 		    			}
 		    		}
 		    		plugin.inv.addItem(plugin.armorPart);
 		    		plugin.inv.setLeggings(plugin.air);
 		    		player.sendMessage(ChatColor.RED + "You cannot wear these leggings. You must be at least level: " + findLevel(mode, "armor"));
 		    		break;
 		    	}
 		    }
 	    }
 	    if (plugin.inv.getBoots() != null) {
 	    	for (int i=0;i<plugin.bootIDs.length;i++) {
 		    	if (plugin.bootIDs[i] == plugin.inv.getBoots().getType().getId()) {
 
 		    		String mode;
 		    		if (plugin.inv.getBoots().getType().getId() == 301) {
 		    			if (checkLevel(player, "leather", "armor")) {
 		    				break;
 		    			}
 		    			mode = "leather";
 		    		} else if (plugin.inv.getBoots().getType().getId() == 305) {
 		    			if (checkLevel(player, "chain", "armor")) {
 		    				break;
 		    			}
 		    			mode = "chain";
 		    		} else if (plugin.inv.getBoots().getType().getId() == 309) {
 		    			if (checkLevel(player, "iron", "armor")) {
 		    				break;
 		    			}
 		    			mode = "iron";
 		    		} else if (plugin.inv.getBoots().getType().getId() == 313) {
 		    			if (checkLevel(player, "diamond", "armor")) {
 		    				break;
 		    			}
 		    			mode = "diamond";
 		    		} else if (plugin.inv.getBoots().getType().getId() == 317) {
 		    			if (checkLevel(player, "gold", "armor")) {
 		    				break;
 		    			}
 		    			mode = "gold";
 		    		} else {
 		    			break;
 		    		}
 		    		plugin.armorPart = new ItemStack(plugin.inv.getBoots().getType().getId());
 		    		plugin.armorPart.setDurability(plugin.inv.getBoots().getDurability());
 		    		if (plugin.inv.getBoots().getEnchantments().size() != 0) {
 		    			try {
 		    				plugin.armorPart.addEnchantments(plugin.inv.getBoots().getEnchantments());
 		    			} catch (Exception e) {
 		    				plugin.armorPart.addUnsafeEnchantments(plugin.inv.getBoots().getEnchantments());
 		    			}
 		    		}
 		    		plugin.inv.addItem(plugin.armorPart);
 		    		plugin.inv.setBoots(plugin.air);
 		    		player.sendMessage(ChatColor.RED + "You cannot wear these boots. You must be at least level: " + findLevel(mode, "armor"));
 		    		break;
 		    	}
 		    }
 		}
 	}
 }
