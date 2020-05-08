 package com.ktross.horsemount;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Damageable;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.inventory.HorseInventory;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.apache.commons.lang3.text.WordUtils;
 
 public final class HorseMount extends JavaPlugin implements Listener {
 	
 	public Map<String, Horse.Variant> mountVariants;
 	public Map<String, Horse.Style> mountStyles;
 	public Map<String, Horse.Color> mountColors;
 	public Map<String, Material> mountArmor;
 	public boolean DisableSpawning;
 	public boolean DisableItemDrops;
 	
 	public void onEnable() {
 		
 		this.saveDefaultConfig();
 		
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this, this);
 		
 		this.DisableSpawning = ((Boolean) this.getConfig().get("disable-spawning") == true) ? true : false;
 		this.DisableItemDrops = ((Boolean) this.getConfig().get("disable-item-drops") == true) ? true : false;
 		
 		this.mountVariants = new HashMap<String, Horse.Variant>();
 		this.mountVariants.put("horse", Horse.Variant.HORSE);
 		this.mountVariants.put("mule", Horse.Variant.MULE);
 		this.mountVariants.put("donkey", Horse.Variant.DONKEY);
 		this.mountVariants.put("skeleton", Horse.Variant.SKELETON_HORSE);
 		this.mountVariants.put("zombie", Horse.Variant.UNDEAD_HORSE);
 		
 		this.mountStyles = new HashMap<String, Horse.Style>();
 		this.mountStyles.put("default", Horse.Style.NONE);
 		this.mountStyles.put("white", Horse.Style.WHITE);
 		this.mountStyles.put("whitefield", Horse.Style.WHITEFIELD);
 		this.mountStyles.put("whitedots", Horse.Style.WHITE_DOTS);
 		this.mountStyles.put("blackdots", Horse.Style.BLACK_DOTS);
 		
 		this.mountColors = new HashMap<String, Horse.Color>();
 		this.mountColors.put("white", Horse.Color.WHITE);
 		this.mountColors.put("creamy", Horse.Color.CREAMY);
 		this.mountColors.put("chestnut", Horse.Color.CHESTNUT);
 		this.mountColors.put("brown", Horse.Color.BROWN);
 		this.mountColors.put("black", Horse.Color.BLACK);
 		this.mountColors.put("gray", Horse.Color.GRAY);
 		this.mountColors.put("darkbrown", Horse.Color.DARK_BROWN);
 		
 		this.mountArmor = new HashMap<String, Material>();
 		this.mountArmor.put("iron", Material.IRON_BARDING);
 		this.mountArmor.put("gold", Material.GOLD_BARDING);
 		this.mountArmor.put("diamond", Material.DIAMOND_BARDING);
 		
 		// Commands
 		getCommand("hm").setExecutor(new HorseMountCommandExecutor(this));
 		getCommand("horsemount").setExecutor(new HorseMountCommandExecutor(this));
 		getCommand("mount").setExecutor(new HorseMountCommandExecutor(this));
 		getCommand("dismount").setExecutor(new HorseMountCommandExecutor(this));
 		getCommand("setmount").setExecutor(new HorseMountCommandExecutor(this));
 		getCommand("setarmor").setExecutor(new HorseMountCommandExecutor(this));
 		getCommand("showmount").setExecutor(new HorseMountCommandExecutor(this));
 		
 		// Plugin Metrics
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		} catch (IOException e) {
 			getLogger().info("Failed to submit stats to MCStats.org");
 		}
 	}
 	
 	public void onDisable() {}
 	
 	public void msgPlayer(CommandSender sender, String message) {
 		sender.sendMessage(ChatColor.GOLD+"["+ChatColor.YELLOW+"HorseMount"+ChatColor.GOLD+"]"+ChatColor.GRAY+" "+message);
 	}
 	
 	@EventHandler
 	public void onVehicleEnter(VehicleEnterEvent event) {
 		Player player = (Player) event.getEntered();
 		if (player.getVehicle() instanceof Horse) {
 			player.getVehicle().remove();
 			msgPlayer(player, "Automatically dismounted due to vehicle change.");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		if (event.getRightClicked() instanceof Horse && event.getRightClicked().isEmpty() && event.getPlayer().getItemInHand().getType() != Material.LEASH) {
 			boolean eventCancelled = true;
 			Player p = (Player) event.getPlayer();
 			if (p.hasPermission("horsemount.mount")) {
 				eventCancelled = false;
 			} else {
 				msgPlayer(p, "You do not have permission to mount horses.");
 			}
 			event.setCancelled(eventCancelled);
 		}
 	}
 	
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event) {
 		if (event.getInventory() instanceof HorseInventory && event.getSlotType() != InventoryType.SlotType.QUICKBAR && event.getSlot() < 9) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onVehicleExit(VehicleExitEvent event) {
 		if (event.getVehicle() instanceof Horse) {
 			Horse h = (Horse) event.getVehicle();
 			h.remove();
 		}
 	}
 	
 	@EventHandler
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		if (event.getEntityType() == EntityType.HORSE && event.getSpawnReason() != SpawnReason.CUSTOM && this.DisableSpawning == true) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (event.getEntityType() == EntityType.PLAYER && event.getEntity().getVehicle() != null) {
 			Damageable p = (Damageable) event.getEntity();
 			if (event.getDamage() >= p.getHealth()) {
 				Horse h = (Horse) event.getEntity().getVehicle();
 				h.remove();
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		if(event.getEntityType() == EntityType.HORSE && this.DisableItemDrops == true) {
 			event.getDrops().clear();
 			event.setDroppedExp(0);
 		}
 	}
 	
 	@EventHandler
 	public void onItemSpawn(ItemSpawnEvent event) {
 		if ((event.getEntity().getItemStack().getType() == Material.SADDLE || event.getEntity().getItemStack().getType() == Material.IRON_BARDING || event.getEntity().getItemStack().getType() == Material.GOLD_BARDING || event.getEntity().getItemStack().getType() == Material.DIAMOND_BARDING) && this.DisableItemDrops == true) {
 			// Workaround to prevent saddle/armor drops due to bukkit bug
 			if (event.getEntity().getVelocity().getY() == 0.20000000298023224) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onSignChange(SignChangeEvent event) {
 		if ((event.getLine(0).equalsIgnoreCase("[HM]") || event.getLine(0).equalsIgnoreCase("[HorseMount]")) && event.getPlayer().hasPermission("horsemount.signs.create")) {
 			if (this.mountArmor.get(event.getLine(1)) != null) {
 				event.setLine(0, ChatColor.AQUA+"[HorseMount]");
 				event.setLine(1, WordUtils.capitalize(event.getLine(1)));
 			} else {
 				if (this.mountVariants.get(event.getLine(1)) != null && !event.getLine(1).equalsIgnoreCase("horse")) {
 					event.setLine(0, ChatColor.AQUA+"[HorseMount]");
 					event.setLine(1, WordUtils.capitalize(event.getLine(1)));
 				}
				else if (this.mountVariants.get(event.getLine(1)) != null && this.mountStyles.get(event.getLine(2)) != null && this.mountColors.get(event.getLine(3)) != null) {
 					event.setLine(0, ChatColor.AQUA+"[HorseMount]");
 					event.setLine(1, WordUtils.capitalize(event.getLine(1)));
 					event.setLine(2, WordUtils.capitalize(event.getLine(2)));
 					event.setLine(3, WordUtils.capitalize(event.getLine(3)));
 				} else {
 					event.setLine(0, "Error:");
 					event.setLine(1, "Invalid");
 					event.setLine(2, "Parameters");
 					event.setLine(3, "");
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onRightClick(PlayerInteractEvent event) {
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
 			Sign clickedSign = (Sign) event.getClickedBlock().getState();
 			if (ChatColor.stripColor(clickedSign.getLine(0)).equalsIgnoreCase("[HorseMount]") && event.getPlayer().hasPermission("horsemount.signs.use")) {
 				if (this.mountArmor.get(WordUtils.uncapitalize(clickedSign.getLine(1))) != null) {
 					PluginCommand setArmor = getServer().getPluginCommand("setarmor");
 					String armorArgs[];
 					armorArgs = new String[1];
 					armorArgs[0] = WordUtils.uncapitalize(clickedSign.getLine(1));
 					setArmor.execute(event.getPlayer(), "setarmor", armorArgs);
 				} else {
 					String[] lines = clickedSign.getLines();
 					int argsCount = 0;
 					for (String line : lines) {
 						if (!line.equalsIgnoreCase("") && !ChatColor.stripColor(line).equalsIgnoreCase("[HorseMount]")) {
 							argsCount++;
 						}
 					}
 					PluginCommand setMount = getServer().getPluginCommand("setmount");
 					String[] mountArgs;
 					mountArgs = new String[argsCount];
 					int count = 0;
 					while (count < argsCount) {
 						mountArgs[count] = WordUtils.uncapitalize(lines[count+1]);
 						count++;
 					}
 					setMount.execute(event.getPlayer(), "setmount", mountArgs);
 				}
 			}
 		}
 	}
 }
