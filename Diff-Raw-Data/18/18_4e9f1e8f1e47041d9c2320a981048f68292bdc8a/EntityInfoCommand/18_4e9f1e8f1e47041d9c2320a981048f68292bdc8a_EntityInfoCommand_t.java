 package com.github.thebiologist13.commands.entities;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.SpawnableEntity;
 import com.github.thebiologist13.commands.SpawnerCommand;
 import com.github.thebiologist13.serialization.SItemStack;
 import com.github.thebiologist13.serialization.SPotionEffect;
 
 public class EntityInfoCommand extends SpawnerCommand {
 
 	public EntityInfoCommand(CustomSpawners plugin) {
 		super(plugin);
 	}
 
 	@Override
 	public void run(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		//Player
 		Player p = null;
 
 		//Spawner
 		SpawnableEntity s = null;
 
 		if(arg0 instanceof Player) {
 			p = (Player) arg0;
 		}
 
 		if(p == null) {
 
 			//If the player wants to perform command with a selection.
 			if(CustomSpawners.consoleEntity != -1 && arg3.length == 1) {
 
 				s = CustomSpawners.getEntity(String.valueOf(CustomSpawners.consoleEntity));
 
 				//Arguments are for selection, but none is selected
 			} else if(arg3.length == 1) {
 
 				plugin.sendMessage(arg0, NEEDS_SELECTION);
 				return;
 
 				//If the player wants to perform command on a specific entity
 			} else if(arg3.length == 2) {
 
 				s = CustomSpawners.getEntity(arg3[1]);
 
 				if(s == null) {
 					plugin.sendMessage(arg0, NO_ID);
 					return;
 				}
 
 				//General error
 			} else {
 
 				plugin.sendMessage(arg0, GENERAL_ERROR);
 				return;
 
 			}
 
 			plugin.sendMessage(arg0, getInfo(s));
 			
 		} else {
 
 			//Permission check
 			if(p.hasPermission("customspawners.entities.info")) {
 
 				//If the player wants to perform command with a selection.
 				if(CustomSpawners.entitySelection.containsKey(p) && arg3.length == 1) {
 
 					s = CustomSpawners.getEntity(CustomSpawners.entitySelection.get(p).toString());
 
 				//Arguments are for selection, but none is selected
 				} else if(arg3.length == 1) {
 
 					plugin.sendMessage(p, NEEDS_SELECTION);
 					return;
 
 				//If the player wants to perform command on a specific entity
 				} else if(arg3.length == 2) {
 
 					s = CustomSpawners.getEntity(arg3[1]);
 					
 					if(s == null) {
 						plugin.sendMessage(p, NO_ID);
 						return;
 					}
 					
 				//General error
 				} else {
 
 					plugin.sendMessage(p, GENERAL_ERROR);
 					return;
 
 				}
 
 				plugin.sendMessage(p, getInfo(s));
 				
 			} else {
 				plugin.sendMessage(p, NO_PERMISSION);
 				return;
 			}
 			
 		}
 		
 	}
 	
 	private String[] getInfo(SpawnableEntity s) {
 		
 		String effectMessage = ""; 
 		ArrayList<SPotionEffect> effects = s.getEffects();
 		for(int i = 0; i < effects.size(); i++) {
 			if(i == 0) {
 				effectMessage += effects.get(i).getType().getName() + " " + effects.get(i).getAmplifier();
 			} else {
 				effectMessage += ", " + effects.get(i).getType().getName() + " " + effects.get(i).getAmplifier();
 			}
 		}
 		
 		if(effectMessage.isEmpty()) {
 			effectMessage = "No effects.";
 		}
 		
 		String header = ChatColor.GREEN + "Information on entity with ID " + ChatColor.GOLD + String.valueOf(s.getId());
 		
 		if(!s.getName().isEmpty()) {
 			header += " (" + s.getName() + ")";
 		}
 		
		String nameOfType = s.getType().toString();
 		
 		if(nameOfType.equals("Spider") && s.isJockey()) {
 			nameOfType = "Spider Jockey";
 		}
 		
 		String blackListMsg = "";
 		ArrayList<String> blackList = s.getDamageBlacklist();
 		for(int i = 0; i < blackList.size(); i++) {
 			if(i == 0) {
 				blackListMsg += blackList.get(i);
 			} else {
 				blackListMsg += ", " + blackList.get(i);
 			}
 		}
 		
 		String whiteListMsg = "";
 		ArrayList<String> whiteList = s.getDamageWhitelist();
 		for(int i = 0; i < whiteList.size(); i++) {
 			if(i == 0) {
 				whiteListMsg += whiteList.get(i);
 			} else {
 				whiteListMsg += ", " + whiteList.get(i);
 			}
 		}
 		
 		String itemListMsg = "";
 		ArrayList<ItemStack> itemList = s.getItemDamageList();
 		for(int i = 0; i < itemList.size(); i++) {
 			ItemStack item = itemList.get(i);
 			if(i == 0) {
 				itemListMsg += item.getTypeId() + ":" + item.getDurability();
 			} else {
 				itemListMsg += ", " + item.getTypeId() + ":" + item.getDurability();
 			}
 		}
 		
 		String dropMsg = "";
 		ArrayList<ItemStack> drops = s.getDrops();
 		for(int i = 0; i < drops.size(); i++) {
 			ItemStack item = drops.get(i);
 			String append = plugin.getItemName(item) + " #" + item.getAmount(); 
 			if(i == 0) {
 				dropMsg += append;
 			} else {
 				dropMsg += ", " + append;
 			}
 		}
 		
 		String invMsg = "";
 		invMsg += "[HEAD] -> " + plugin.getItemName(s.getInventory().getArmor()[3]) + " ";
 		invMsg += "[CHEST] -> " + plugin.getItemName(s.getInventory().getArmor()[2]) + " ";
 		invMsg += "[LEGS] -> " + plugin.getItemName(s.getInventory().getArmor()[1]) + " ";
 		invMsg += "[BOOTS] -> " + plugin.getItemName(s.getInventory().getArmor()[0]) + " ";
 		invMsg += "[HAND] -> " + plugin.getItemName(s.getInventory().getHand()) + " ";
 		
 		Map<Integer, SItemStack> content = s.getInventory().getContent();
 		for(Integer i : content.keySet()) {
 			SItemStack stack = content.get(i);
 			invMsg += "[" + i + "] ->" + plugin.getItemName(stack.toItemStack()) + " #" + stack.getCount() + " "; 
 		}
 		
 		SPotionEffect epe = s.getPotionEffect();
 		
 		String typeOfExpDrop = (s.getType().equals(EntityType.THROWN_EXP_BOTTLE)) ? "Experience Bottle Exp: " : "Mob Dropped Exp: ";
 		
 		boolean isWither = false;
 		if(s.hasProp("wither")) {
 			isWither = (Boolean) s.getProp("wither");
 		}
 		
 		boolean isVillager = false;
 		if(s.hasProp("zombie")) {
 			isVillager = (Boolean) s.getProp("zombie");
 		}
 		
 		//Send info
 		String[] message = {
 				"",
 				header + ChatColor.GREEN + ": ",
 				"",
 				ChatColor.GOLD + "Type: " + nameOfType,
 				ChatColor.GOLD + "Name: " + s.getName(),
 				ChatColor.GOLD + "Effects: " + effectMessage,
 				ChatColor.GOLD + "X Velocity: " + s.getXVelocity(),
 				ChatColor.GOLD + "Y Velocity: " + s.getYVelocity(),
 				ChatColor.GOLD + "Z Velocity: " + s.getZVelocity(),
 				ChatColor.GOLD + "Age: " + s.getAge(),
 				ChatColor.GOLD + "Health: " + s.getHealth(),
 				ChatColor.GOLD + "Air: " + s.getAir(),
 				ChatColor.GOLD + "Profession: " + s.getProfession().toString(),
 				ChatColor.GOLD + "Enderman Block ID: " + s.getEndermanBlock().getItemTypeId(),
 				ChatColor.GOLD + "Saddled: " + s.isSaddled(),
 				ChatColor.GOLD + "Charged: " + s.isCharged(),
 				ChatColor.GOLD + "Jockey: " + s.isJockey(),
 				ChatColor.GOLD + "Tamed: " + s.isTamed(),
 				ChatColor.GOLD + "Sitting: " + s.isSitting(),
 				ChatColor.GOLD + "Angry: " + s.isAngry(),
 				ChatColor.GOLD + "Cat Type: " + s.getCatType(),
 				ChatColor.GOLD + "Slime Size: " + s.getSlimeSize(),
 				ChatColor.GOLD + "Color: " + s.getColor(),
 				ChatColor.GOLD + "Passive: " + String.valueOf(s.isPassive()),
 				ChatColor.GOLD + "Fire Ticks: " + s.getFireTicks(),
 				ChatColor.GOLD + "Using Blacklist: " + s.isUsingBlacklist(),
 				ChatColor.GOLD + "Using Whitelist: " + s.isUsingWhitelist(),
 				ChatColor.GOLD + "Damage Blacklist: " + blackListMsg,
 				ChatColor.GOLD + "Damage Whitelist: " + whiteListMsg,
 				ChatColor.GOLD + "Damage Itemlist: " + itemListMsg,
 				ChatColor.GOLD + "Using Custom Damage: " + s.isUsingCustomDamage(),
 				ChatColor.GOLD + "Damage Dealt: " + s.getDamage(),
 				ChatColor.GOLD + "Potion Type: " + epe.getType().getName() + " " + epe.getAmplifier() + " - " + plugin.convertTicksToTime(epe.getDuration()),
 				ChatColor.GOLD + typeOfExpDrop + s.getDroppedExp(),
 				ChatColor.GOLD + "Fuse Ticks: " + s.getFuseTicks(),
 				ChatColor.GOLD + "Explosive Yield: " + s.getYield(),
 				ChatColor.GOLD + "Incendiary: " + s.isIncendiary(),
 				ChatColor.GOLD + "Item Type: " + plugin.getItemName(s.getItemType()),
 				ChatColor.GOLD + "Using Custom Drops: " + s.isUsingCustomDrops(),
 				ChatColor.GOLD + "Drops: " + dropMsg,
 				ChatColor.GOLD + "Inventory: " + invMsg,
 				ChatColor.GOLD + "Invincible: " + s.isInvulnerable(),
 				ChatColor.GOLD + "Wither: " + String.valueOf(isWither),
 				ChatColor.GOLD + "Villager: " + String.valueOf(isVillager),
 				ChatColor.GREEN + "Scroll Up for More Properties."
 		};
 		
 		return message;
 		
 	}
 
 }
