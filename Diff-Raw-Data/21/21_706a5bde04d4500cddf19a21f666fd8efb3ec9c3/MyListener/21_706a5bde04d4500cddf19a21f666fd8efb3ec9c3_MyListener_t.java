 package com.outlook.corey_young.spellarrows;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class MyListener implements Listener {
 	
 	@EventHandler
 	public void onLeftClick(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		boolean sortArrows = true;
 		if (SpellArrows.sortArrowMap.containsKey(player.getName())) {
 			sortArrows = SpellArrows.sortArrowMap.get(player.getName());
 		}
		Inventory inventory = player.getInventory();
		if (sortArrows && inventory.first(Material.ARROW) != -1) {
 			if (event.getAction() == Action.LEFT_CLICK_AIR
 					|| event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				if (player.getItemInHand().getType() == Material.BOW) {
 					//Sort arrowStacks
 					ItemStack[] arrowStacks = getArrowStacks(inventory);
 					int[] arrowStackIndices = getArrowStackIndices(inventory);
 					int numberOfStacks = getNumberOfArrowStacks(inventory);
 					ItemStack tempStack = arrowStacks[0];
 					for (int i = 1; i < arrowStacks.length; i ++) {
 						inventory.setItem(arrowStackIndices[i-1], arrowStacks[i]);
 					}
 					inventory.setItem(arrowStackIndices[numberOfStacks-1], tempStack);
 					ItemStack arrowStackSelected = inventory.getItem(inventory.first(Material.ARROW));
 					String arrowSelectedName = arrowStackSelected.getItemMeta().getDisplayName();
 					if (isMagicArrow(arrowSelectedName)) {
 						player.sendMessage(arrowSelectedName + " selected.");
 					} else {
 						player.sendMessage("Normal Arrow selected.");
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onShoot(EntityShootBowEvent event) {
 		if (event.getEntityType() == EntityType.PLAYER) {
 			Player player = (Player) (event.getEntity());
 			PlayerInventory inventory = player.getInventory();
			int itemPos = inventory.first(Material.ARROW);
			if (itemPos != -1) {
				ItemStack arrowConsumed = inventory.getItem(itemPos);
				String arrowType = arrowConsumed.getItemMeta().getDisplayName();
				Arrow arrow =  (Arrow) event.getProjectile();
				SpellArrows.arrowMap.put(arrow.getEntityId(), arrowType);
			}
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void arrowHitMob(EntityDamageByEntityEvent event) {
 		if (event.getDamager() instanceof Arrow) {
 			Arrow arrow = (Arrow) event.getDamager();
 			LivingEntity entity = (LivingEntity) event.getEntity();
 			if (SpellArrows.arrowMap.containsKey(arrow.getEntityId())) {
 				String arrowType = SpellArrows.arrowMap.get(arrow.getEntityId());
 				if (isMagicArrow(arrowType)) {
 					event.setDamage(0);
 				}
 				if (arrowType != null) {
 					//Negative Effects
 					int ID = -1;
 					for (int mapID : SpellArrows.arrowTypeMap.keySet()) {
 						if (SpellArrows.arrowTypeMap.get(mapID).equals(arrowType)) {
 							ID = mapID;
 						}
 					}
 					if (ID != -1) {
 						//Fix this later
 						Potion potion = Potion.fromDamage(ID);
 						potion.apply(entity);
 					} else {
 						Bukkit.broadcastMessage("Error: Cannot apply potion effect!");
 					}
 				}
				SpellArrows.arrowMap.remove(arrow.getEntityId());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if (SpellArrows.arrowMap.containsKey(event.getItem().getEntityId())) {
 			event.setCancelled(true);
 			event.getItem().remove();
 			ItemStack itemStack = event.getItem().getItemStack();
 			ItemMeta itemMeta = itemStack.getItemMeta();
 			itemMeta.setDisplayName(SpellArrows.arrowMap.get(event.getItem().getEntityId()));
 			itemStack.setItemMeta(itemMeta);
 			event.getPlayer().getInventory().addItem(itemStack);
			SpellArrows.arrowMap.remove(event.getItem().getEntityId());
 		}
 	}
 	
 	public void addEffectToEntity(Entity entity, PotionEffectType effectType, int length, int strength) {
 		LivingEntity livingEntity = (LivingEntity) entity;
 		livingEntity.addPotionEffect(new PotionEffect(effectType, length, strength));
 	}
 	
 	public int getNumberOfArrowStacks(Inventory inventory) {
 		int numberOfStacks = 0;
 		for (int i = 0; i < inventory.getSize(); i ++) {
 			if (inventory.getItem(i) != null) {
 				if (inventory.getItem(i).getType() == Material.ARROW) {
 					numberOfStacks ++;
 				}
 			}
 		}
 		return numberOfStacks;
 	}
 	
 	public ItemStack[] getArrowStacks(Inventory inventory) {
 		int numberOfStacks = getNumberOfArrowStacks(inventory);
 		ItemStack [] arrowStacks = new ItemStack[numberOfStacks];
 		int count = 0;
 		while (count < numberOfStacks) {
 			for (int i = 0; i < inventory.getSize(); i ++) {
 				ItemStack itemStack = inventory.getItem(i);
 				if (itemStack != null) {
 					if (itemStack.getType() == Material.ARROW) {
 						arrowStacks[count] = itemStack;
 						count ++;
 					}
 				}
 			}
 		}
 		return arrowStacks;
 	}
 	
 	public int[] getArrowStackIndices(Inventory inventory) {
 		int numberOfStacks = getNumberOfArrowStacks(inventory);
 		int[] arrowStackIndices = new int[numberOfStacks];
 		int count = 0;
 		while (count < numberOfStacks) {
 			for (int i = 0; i < inventory.getSize(); i ++) {
 				ItemStack itemStack = inventory.getItem(i);
 				if (itemStack != null) {
 					if (itemStack.getType() == Material.ARROW) {
 						arrowStackIndices[count] = i;
 						count ++;
 					}
 				}
 			}
 		}
 		return arrowStackIndices;
 	}
 	
 	public Boolean isMagicArrow(String arrowType) {
 		if (arrowType == null) {
 			return false;
 		}
 		Boolean isMagic = false;
 		for (String value : SpellArrows.arrowTypeMap.values()) {
 			if (value.equals(arrowType)) {
 				isMagic = true;
 			}
 		}
 		return isMagic;
 	}
 }
