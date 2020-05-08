 package com.flobi.WhatIsIt;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 /**
  * 
  * WhatIsIt
  * Friendly names plugin for Bukkit
  * 
  * @author Joshua "Flobi" Hatfield
  */
 
 public class WhatIsIt extends JavaPlugin {
 	private static final Logger log = Logger.getLogger("Minecraft");
 
 	private static File configFile = null;
 	private static InputStream defConfigStream;
 	private static FileConfiguration config = null;
 	private static boolean showDataValues = false;
 	private static File namesConfigFile = null;
 	private static InputStream defNamesConfigStream;
 	private static FileConfiguration namesConfig = null;
 	private static YamlConfiguration defConfig = null;
 	private static YamlConfiguration defNamesConfig = null;
 
 	private static File dataFolder;
 	private static Permission perms = null;
 	private static ConsoleCommandSender console;
 	
 
 	/**
 	 * Event executed when enabled.
 	 */
 	public void onEnable() {
 		console = getServer().getConsoleSender();
 		dataFolder = getDataFolder();
 		defConfigStream = getResource("config.yml");
 		defNamesConfigStream = getResource("names.yml");
 		loadConfig();
 
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 	        setupPermissions();
 		}
 
         // Load up the Plugin metrics
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             // Failed to submit the stats :-(
         }
 		console.sendMessage(chatPrep(config.getString("messages.has-been-enabled")));
 	}
 	/**
 	 * Event executed when disabled.
 	 */
 	public void onDisable() { 
 		console.sendMessage(chatPrep(config.getString("messages.has-been-disabled")));
 	}
 	
 	/**
 	 * Event executed when commands are entered.
 	 * 
 	 * @param CommandSender sender
 	 * @param Command command being sent
 	 * @param String command label
 	 * @param String[] command arguments
 	 * 
 	 * @return boolean success of command
 	 */
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
     	Player player = null;
     	String iATR = "XXXITEMXAMPERSANDXTEMPRARYXREPLACEMENTXXX";
     	if (sender instanceof Player) {
     		player = (Player) sender;
     	} else {
     		console.sendMessage(chatPrep(config.getString("messages.console-error")));
 			return true;
     	}
     	String newName = null;
      
     	if (
     		cmd.getName().equalsIgnoreCase("wis") ||
         	cmd.getName().equalsIgnoreCase("wit")
     	) {
     		if(hasPermission(player, "whatisit.admin")) {
 	    		if (args.length > 0) {
 	    			if (args[0].equalsIgnoreCase("itis")) {
 	    				String[] tmpArgs = (String[]) Array.newInstance(String.class, args.length - 1);
 	    				System.arraycopy(args, 1, tmpArgs, 0, args.length - 1);
 	    				newName = StringUtils.join(tmpArgs, " ");
 	    				if (newName.length() == 0) {
 	    					newName = null;
 	    				}
 	    			} else if (args[0].equalsIgnoreCase("reload")) {
 		    			namesConfig = null;
 		    			loadConfig();
 	    			}
 	    		}
     		}
     	}
     	if (cmd.getName().equalsIgnoreCase("wis")) {
     		if(!hasPermission(player, "whatisit.use")) {
     			player.sendMessage(chatPrep(config.getString("messages.no-permission")));
     			return false;
     		}
     		ItemStack heldItem = player.getItemInHand();
     		player.sendMessage(chatPrep(config.getString("messages.this-is") + WhatIsIt.itemName(heldItem, showDataValues, newName).replaceAll("&", iATR)).replaceAll(iATR, "&"));
     		Map<Enchantment, Integer> enchantments = heldItem.getEnchantments();
     		for (Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
    				player.sendMessage(chatPrep(config.getString("messages.enchanted") + enchantmentName(enchantment, showDataValues, newName).replaceAll("&", iATR)).replaceAll(iATR, "&"));
     		}				
     		return true;
     		
     	} else if (cmd.getName().equalsIgnoreCase("wit")) {
     		if(!hasPermission(player, "whatisit.use")) {
     			player.sendMessage(chatPrep(config.getString("messages.no-permission")));
     			return false;
     		}
     		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
     			namesConfig = null;
     			loadConfig();
     		}
     		Object targetedObject = getTarget(player);
     		player.sendMessage(chatPrep(config.getString("messages.that-is") + WhatIsIt.name(targetedObject, showDataValues, newName).replaceAll("&", iATR)).replaceAll(iATR, "&"));
     		return true;
     	}
     	return false;
     }
     
 	/**
 	 * Permissions wrapper, uses Vault if available, otherwise superPerms.
 	 * 
 	 * @param Player player to check
 	 * @param String permission level
 	 * @return boolean has permission
 	 */
     private boolean hasPermission(Player player, String permission) {
     	if (player == null) {
     		return false;
     	}
     	if (perms == null) {
     		return player.hasPermission(permission);
     	} else {
     		return perms.has(player, permission);
     	}
     }
     
 	/**
 	 * Prepares chat, prepending prefix and processing colors.
 	 * 
 	 * @param String message to prepare
 	 * @return String prepared message
 	 */
     private static String chatPrep(String message) {
     	message = config.getString("messages.prefix") + message;
     	message = ChatColor.translateAlternateColorCodes('&', message);//message.replaceAll("&([0-9a-fA-F])", "$1");
     	return message;
     }
 
 	/**
 	 * Attempts to retrieve targeted entity or object.
 	 * 
 	 * @param Player player whose sight to check for target
 	 * @return Object target entity or block
 	 */
     private static Object getTarget(Player entity) {
     	int range = 100;
     	Iterable<Entity> entities = entity.getNearbyEntities(range, range, range);
     	Entity targetEntity = null;
     	double threshold = 1.2;
     	for (Entity other:entities) {
 	    	Vector n = other.getLocation().toVector().subtract(entity.getLocation().toVector());
 	    	if (entity.getLocation().getDirection().normalize().crossProduct(n).lengthSquared() < threshold && n.normalize().dot(entity.getLocation().getDirection().normalize()) >= 0) {
 		    	if (targetEntity == null || targetEntity.getLocation().distanceSquared(entity.getLocation()) > other.getLocation().distanceSquared(entity.getLocation()))
 		    		targetEntity = other;
     		}
     	}
     	Block targetBlock = entity.getTargetBlock(null, range);
     	if (targetBlock == null) {
     		return targetEntity;
     	} else if (targetEntity == null) {
     		return targetBlock;
     	} else {
         	if (targetBlock.getLocation().distanceSquared(entity.getLocation()) > targetEntity.getLocation().distanceSquared(entity.getLocation())) {
     	    	return targetEntity;
         	} else {
     	    	return targetBlock;
         	}
     	}
     }
 
     /**
 	 * Loads config.yml and names.yml configuration files.
 	 */
     private static void loadConfig() {
 		if (configFile == null) {
 	    	configFile = new File(dataFolder, "config.yml");
 	    }
 	    config = YamlConfiguration.loadConfiguration(configFile);
 	 
 	    // Look for defaults in the jar
 	    if (defConfig != null) {
 	    	config.setDefaults(defConfig);
 	        defConfigStream = null;
 	    }
 	    if (defConfigStream != null) {
 	        defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        config.setDefaults(defConfig);
 	        defConfigStream = null;
 	    }
 	    if (!configFile.exists() && defConfig != null) {
 	    	try {
 	    		defConfig.save(configFile);
 			} catch(IOException ex) {
 				log.severe(chatPrep(config.getString("messages.cannot-save-default-config")));
 			}
 	    }
 	    if (namesConfigFile == null) {
 	    	namesConfigFile = new File(dataFolder, "names.yml");
 	    }
 	    namesConfig = YamlConfiguration.loadConfiguration(namesConfigFile);
 	 
 	    // Look for defaults in the jar
 	    if (defNamesConfig != null) {
 	        namesConfig.setDefaults(defNamesConfig);
 	        defNamesConfigStream = null;
 	    }
 	    if (defNamesConfigStream != null) {
 	        defNamesConfig = YamlConfiguration.loadConfiguration(defNamesConfigStream);
 	        namesConfig.setDefaults(defNamesConfig);
 	        defNamesConfigStream = null;
 	    }
 	    if (!namesConfigFile.exists() && defNamesConfig != null) {
 	    	try {
 	    		defNamesConfig.save(namesConfigFile);
 			} catch(IOException ex) {
 				log.severe(chatPrep(config.getString("messages.cannot-save-default-names")));
 			}
 	    }
 		showDataValues = config.getBoolean("config.display-data-values");
     }
 
 	/**
 	 * Saves names.yml configuration file
 	 */
     private static void saveNamesConfig() {
     	try {
     		namesConfig.save(namesConfigFile);
 		} catch(IOException ex) {
 			log.severe(chatPrep(config.getString("messages.cannot-save-names")));
 		}
     }
 
 	/**
 	 * Retrieves the name of Object.
 	 * 
 	 * @param Object what to identify
 	 * @param Boolean show data values
 	 * @param String new name
 	 * @return String name of object
 	 */
     private static String name(Object whatToIdentify, Boolean showData, String newName) {
 		if (whatToIdentify instanceof Entity) {
 			return entityName((Entity) whatToIdentify, showData, newName);
 		}
 		if (whatToIdentify instanceof ItemStack) {
 			return itemName((ItemStack) whatToIdentify, showData, newName);
 		}
 		if (whatToIdentify instanceof Block) {
 			return blockName((Block) whatToIdentify, showData, newName);
 		}
 		if (whatToIdentify instanceof ItemStack) {
 			return itemName((ItemStack) whatToIdentify, showData, newName);
 		}
 		return config.getString("messages.unknown-object");
 	}
 	
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Entry&lt;Enchantment,&nbsp;Integer> enchantment to identify
 	 * @return String name of enchantment
 	 */
 	public static String enchantmentName(Entry<Enchantment, Integer> enchantment) {
 		return enchantmentName(enchantment, false);
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Entry&lt;Enchantment,&nbsp;Integer> enchantment to identify
 	 * @param Boolean show data values
 	 * @return String name of enchantment
 	 */
 	public static String enchantmentName(Entry<Enchantment, Integer> enchantment, Boolean showData) {
 		return enchantmentName(enchantment, showData, null);
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Entry&lt;Enchantment,&nbsp;Integer> enchantment to identify
 	 * @param Boolean show data values
 	 * @param String new name
 	 * @return String name of enchantment
 	 */
 	private static String enchantmentName(Entry<Enchantment, Integer> enchantment, Boolean showData, String newName) {
 		if (enchantment == null) {
 			return namesConfig.getString("enchantments.UNKNOWN");
 		}
 		return enchantmentName(enchantment.getKey(), enchantment.getValue());
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 *
 	 * @param Enchantment enchantment to identify
 	 * @param Integer level of enchantment
 	 * @return String name of enchantment
 	 */
 	public static String enchantmentName(Enchantment enchantment, Integer level) {
 		return enchantmentName(enchantment, level, false);
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Enchantment enchantment to identify
 	 * @param Integer level of enchantment
 	 * @param Boolean show data values
 	 * @return String name of enchantment
 	 */
 	public static String enchantmentName(Enchantment enchantment, Integer level, Boolean showData) {
 		return enchantmentName(enchantment, level, showData, null);
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Enchantment enchantment to identify
 	 * @param Integer level of enchantment
 	 * @param Boolean show data values
 	 * @param String new name
 	 * @return String name of enchantment
 	 */
 	private static String enchantmentName(Enchantment enchantment, Integer level, Boolean showData, String newName) {
 		if (enchantment == null) {
 			return namesConfig.getString("enchantments.UNKNOWN");
 		}
 		return enchantmentName(enchantment) + 
 			config.getString("messages.enchantment-level") + 
 			enchantmentLevelName(level);
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Enchantment enchantment to identify
 	 * @return String name of enchantment
 	 */
 	public static String enchantmentName(Enchantment enchantment) {
 		return enchantmentName(enchantment, false);
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Enchantment enchantment to identify
 	 * @param Boolean show data values
 	 * @return String name of enchantment
 	 */
 	public static String enchantmentName(Enchantment enchantment, Boolean showData) {
 		return enchantmentName(enchantment, showData, null);
 	}
 	/**
 	 * Retrieves the name of Enchantment.
 	 * 
 	 * @param Enchantment enchantment to identify
 	 * @param Boolean show data values
 	 * @param String new name
 	 * @return String name of enchantment
 	 */
 	private static String enchantmentName(Enchantment enchantment, Boolean showData, String newName) {
 		if (enchantment == null) {
 			return namesConfig.getString("enchantments.UNKNOWN");
 		}
 		
 		String data = Integer.toString(enchantment.getId());
 
 		if (newName != null) {
 			namesConfig.set("entities." + data, newName);
 			saveNamesConfig();
 		}
 		
 		String name = namesConfig.getString("enchantments." + data);
 		if (name == null) {
 			name = namesConfig.getString("enchantments.UNKNOWN");
 		}
 		if (showData) {
 			name = "(" + Integer.toString(enchantment.getId()) + ") " + name;
 		}
 		return name;
 	}
 	/**
 	 * Retrieves the name of Enchantment level.
 	 * 
 	 * @param Integer enchantment level to identify
 	 * @return String name of enchantment level
 	 */
 	public static String enchantmentLevelName(Integer level) {
 		String name = namesConfig.getString("enchantmentlevels." + Integer.toString(level));
 		if (name == null) {
 			name = namesConfig.getString("enchantmentlevels.UNKNOWN");
 		}
 		return name;
 	}
 	
 	/**
 	 * Retrieves the name of Block.
 	 * 
 	 * @param Block block to identify
 	 * @return String name of block
 	 */
 	public static String blockName(Block block) {
 		return blockName(block, false);
 	}
 	/**
 	 * Retrieves the name of Block.
 	 * 
 	 * @param Block block to identify
 	 * @param Boolean show data values
 	 * @return String name of block
 	 */
 	public static String blockName(Block block, Boolean showData) {
 		return blockName(block, showData, null);
 	}
 	/**
 	 * Retrieves the name of Block.
 	 * 
 	 * @param Block block to identify
 	 * @param Boolean show data values
 	 * @param String new name
 	 * @return String name of block
 	 */
 	private static String blockName(Block block, Boolean showData, String newName) {
 		if (block == null) {
 			return namesConfig.getString("items.UNKNOWN");
 		}
 		
 		byte blockData = block.getData();
 		if (block.getTypeId() == 52) {
 			// Monster Spawner, get type for data to pass to itemName:
 			CreatureSpawner spawner = (CreatureSpawner) block.getState();
 			blockData = (byte) spawner.getSpawnedType().getTypeId();
 		} else {
 			
 		}
 		ItemStack item = new ItemStack(block.getType(), 1, (short) 0, blockData);
 		return itemName(item, showData, newName);
 	}
 	
 	/**
 	 * Retrieves the name of Player.
 	 * 
 	 * @param Player player to identify
 	 * @return String name of player
 	 */
 	private static String playerName(Player player) {
 		if (player == null) {
 			return namesConfig.getString("entities.UNKNOWN");
 		}
 		return player.getName();
 	}
 	
 	/**
 	 * Retrieves the name of Entity.
 	 * 
 	 * @param Entity entity to identify
 	 * @return String name of entity
 	 */
 	public static String entityName(Entity entity) {
 		return entityName(entity, false);
 	}
 	/**
 	 * Retrieves the name of Entity.
 	 * @param Entity entity to identify
 	 * @param Boolean show data values
 	 * @return String name of entity
 	 */
 	public static String entityName(Entity entity, Boolean showData) {
 		return entityName(entity, showData, null);
 	}
 	/**
 	 * Retrieves the name of Entity.
 	 * 
 	 * @param Entity entity to identify
 	 * @param Boolean show data values
 	 * @param String new name
 	 * @return String name of entity
 	 */
 	private static String entityName(Entity entity, Boolean showData, String newName) {
 		if (entity == null) {
 			return namesConfig.getString("entities.UNKNOWN");
 		} else if (entity.getType() == EntityType.SPLASH_POTION) {
 			Item item = (Item) entity;
 			return itemName(item.getItemStack());
 		} else if (entity.getType() == EntityType.DROPPED_ITEM) {
 			Item item = (Item) entity;
 			return itemName(item.getItemStack());
 		} else if (entity instanceof Player) {
 			return playerName((Player) entity);
 		}
 		
 		String typeId = "";
 		String data = "";
 		String name = "";
 		String owner_prefix = "";
 		typeId = Short.toString(entity.getType().getTypeId());
 		
 		if (entity.getType() == EntityType.SHEEP) {
 			Sheep sheep = (Sheep) entity;
 			data = Byte.toString(sheep.getColor().getData());
 		} else if (entity.getType() == EntityType.OCELOT) {
 			Ocelot ocelot = (Ocelot) entity;
 			data = Integer.toString(ocelot.getCatType().getId());
 			Player owner = (Player) ocelot.getOwner();
 			if (owner != null) {
 				owner_prefix = owner.getDisplayName() + "'s ";
 			}
 		} else if (entity.getType() == EntityType.WOLF) {
 			Wolf wolf = (Wolf) entity;
 			Player owner = (Player) wolf.getOwner();
 			if (owner != null) {
 				owner_prefix = owner.getDisplayName() + "'s ";
 			}
 		} else if (entity.getType() == EntityType.VILLAGER) {
 			Villager villager = (Villager) entity;
 			data = Integer.toString(villager.getProfession().getId());
 		} else {
 			data = "0";
 		}
 		
 		if (newName != null) {
 			namesConfig.set("entities." + typeId + ";" + data, newName);
 			saveNamesConfig();
 		}
 		
 		name = namesConfig.getString("entities." + typeId + ";" + data);
 		if (name == null) {
 			name = namesConfig.getString("entities." + typeId + ";0");
 		}
 		if (showData) {
 			name = "(" + typeId + ":" + data + ") " + name;
 		}
 		if (name == null) {
 			name = namesConfig.getString("entities.UNKNOWN");
 		}
 		return owner_prefix + name;
 	}
 	
 	/**
 	 * Retrieves the name of Item.
 	 * 
 	 * @param ItemStack item to identify
 	 * @return String name of item
 	 */
 	public static String itemName(ItemStack item) {
 		return itemName(item, false);
 	}
 	/**
 	 * Retrieves the name of Item.
 	 * 
 	 * @param ItemStack item to identify
 	 * @param Boolean show data values
 	 * @return String name of item
 	 */
 	public static String itemName(ItemStack item, Boolean showData) {
 		return itemName(item, showData, null);
 	}
 	/**
 	 * Retrieves the name of Item.
 	 * 
 	 * @param ItemStack item to identify
 	 * @param Boolean show data values
 	 * @param String new name
 	 * @return String name of item
 	 */
 	private static String itemName(ItemStack item, Boolean showData, String newName) {
 		if (item == null) {
 			return namesConfig.getString("items.UNKNOWN");
 		}
 		String typeId = "";
 		String data = "";
 		String name = null;
 		String prefix = "";
 		String suffix = "";
 		typeId = Integer.toString(item.getTypeId());
 		if (item.getType().getMaxDurability() > 0) {
 			data = "0";
 		} else if (item.getDurability() > 0) {
 			data = Short.toString(item.getDurability());
 		} else if (item.getData().getData() > 0) {
 			data = Byte.toString(item.getData().getData());
 		} else {
 			data = "0";
 		}
 		
 		if (typeId.equals("373")) {
 			// Data on potions is a bit more complicated...
 			short potionData = Short.parseShort(data);
 			if (potionData == 0) {
 				name = namesConfig.getString("potion-parts.absolute-zero");
 			} else {
 				for (short bitPos = 14; bitPos > 5; bitPos--) {
 					
 					short bitPow = (short) Math.pow(2, bitPos);
 					if (potionData >= bitPow) {
 						potionData -= bitPow;
 						String tmpPrefix = namesConfig.getString("potion-parts.prefix-bit-" + bitPos);
 						if (tmpPrefix != null) prefix += tmpPrefix; 
 						String tmpSuffix = namesConfig.getString("potion-parts.suffix-bit-" + bitPos);
						if (tmpSuffix != null) prefix += tmpSuffix; 
 					}
 				}
 				data = Short.toString(potionData);
 			}
 		}
 		
 		if (newName != null) {
 			namesConfig.set("items." + typeId + ";" + data, newName);
 			saveNamesConfig();
 		}
 		
 		if (name == null) {
 			name = namesConfig.getString("items." + typeId + ";" + data);
 		}
 		if (name == null) {
 			name = namesConfig.getString("items." + typeId + ";0");
 		}
 		if (name == null) {
 			name = namesConfig.getString("items.UNKNOWN");
 		}
 		name = prefix + name + suffix;
 		if (showData) {
 			name = "(" + typeId + ":" + data + ") " + name;
 		}
 		return name;
 	}
 	/**
 	 * Registers Vault permissions to local object.
 	 * 
 	 * @return boolean is Vault perms enabled
 	 */
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perms = rsp.getProvider();
         return perms != null;
     }
 }
