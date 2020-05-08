 package com.github.triarry.PvPRestore;
 
 import de.Keyle.MyPet.entity.types.CraftMyPet;
 import de.Keyle.MyPet.entity.types.MyPet;
 import de.Keyle.MyPet.skill.skills.implementation.ranged.MyPetProjectile;
 
 import com.github.triarry.PvPRestore.utilities.Utilities;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 public class PvPRestorePlayerListener implements Listener {
 	
 	private PvPRestore plugin;
 	 
 	public HashMap<Player , ItemStack[]> items = new HashMap<Player , ItemStack[]>();
 	public HashMap<Player , ItemStack[]> armor = new HashMap<Player , ItemStack[]>();
 	
 	public List<ItemStack> itemsToBeRemoved = new ArrayList<ItemStack>();
 
     public PvPRestorePlayerListener(PvPRestore plugin) {
         this.plugin = plugin;
     }
     
 	@EventHandler
 	public void informAdmins(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		if (PvPRestore.update == true && p.hasPermission("pvprestore.update")) {
 			p.sendMessage(ChatColor.GREEN + "An update is available: " + ChatColor.GOLD + PvPRestore.ver);
 			p.sendMessage(ChatColor.GREEN + "Download it here: " + ChatColor.GOLD + "http://dev.bukkit.org/server-mods/pvp-restore/");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         String killer;
         EntityDamageEvent playerDamage = event.getEntity().getLastDamageCause();
 
         if(player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent lastDamageEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
             if(!player.hasPermission("pvprestore.participate"))
             	return;
             else if(PvPRestore.myPetEnabled && lastDamageEvent.getCause() == DamageCause.PROJECTILE && plugin.getConfig().getBoolean("my-pet-enabled") == true) {
             	Projectile a = (Projectile) lastDamageEvent.getDamager();
             	if(a.getShooter() instanceof CraftMyPet) {
             		MyPet myPet = ((CraftMyPet) a.getShooter()).getMyPet();
             		killer = myPet.getOwner().getName() + "'s pet " + myPet.getPetName();
             	}
            	else 
             		return;
             }
             else if(PvPRestore.myPetEnabled && lastDamageEvent.getDamager() instanceof CraftMyPet && plugin.getConfig().getBoolean("my-pet-enabled") == true) {
                 MyPet myPet = ((CraftMyPet) lastDamageEvent.getDamager()).getMyPet();
                 killer = myPet.getOwner().getName() + "'s pet " + myPet.getPetName();
             }
             else if(PvPRestore.myPetEnabled && lastDamageEvent.getDamager() instanceof MyPetProjectile && plugin.getConfig().getBoolean("my-pet-enabled") == true) {
                 MyPet myPet = ((CraftMyPet) lastDamageEvent.getDamager()).getMyPet();
                 killer = myPet.getOwner().getName() + "'s pet " + myPet.getPetName();
             }
             else if(lastDamageEvent.getDamager() instanceof Player)
                 killer = player.getKiller().getName();
             else if (lastDamageEvent.getDamager() instanceof TNTPrimed && plugin.getConfig().getBoolean("other-events.tnt") == true && player.hasPermission("pvprestore.events.tnt"))
             	killer = "TNT";
             else if (player.getKiller() != null) 
             	killer = player.getKiller().getName();
             else if ((playerDamage.getCause() == DamageCause.FIRE || playerDamage.getCause() == DamageCause.FIRE_TICK) && plugin.getConfig().getBoolean("other-events.fire") == true && player.hasPermission("pvprestore.events.fire")) 
             	killer = "Fire";
             else if (playerDamage.getCause() == DamageCause.VOID && plugin.getConfig().getBoolean("other-events.void") == true && player.hasPermission("pvprestore.events.void")) 
             	killer = "The Void";
             else if (playerDamage.getCause() == DamageCause.LAVA && plugin.getConfig().getBoolean("other-events.lava") == true && player.hasPermission("pvprestore.events.lava")) 
             	killer = "Lava";
             else if (playerDamage.getCause() == DamageCause.CONTACT && plugin.getConfig().getBoolean("other-events.cactus") == true && player.hasPermission("pvprestore.events.cactus")) 
             	killer = "A Cactus";
             else if (playerDamage.getCause() == DamageCause.DROWNING && plugin.getConfig().getBoolean("other-events.drowning") == true && player.hasPermission("pvprestore.events.drowning")) 
             	killer = "The Water";
             else if (playerDamage.getCause() == DamageCause.STARVATION && plugin.getConfig().getBoolean("other-events.starvation") == true && player.hasPermission("pvprestore.events.starvation")) 
             	killer = "Their Lack of Food";
             else if (playerDamage.getCause() == DamageCause.SUFFOCATION && plugin.getConfig().getBoolean("other-events.suffocation") == true && player.hasPermission("pvprestore.events.suffocation")) 
             	killer = "Their Lack of Air";
             else if (playerDamage.getCause() == DamageCause.FALL && plugin.getConfig().getBoolean("other-events.falling") == true && player.hasPermission("pvprestore.events.falling")) 
             	killer = "Breaking their legs";
             else {
                 player.sendMessage(ChatColor.RED + "Your death was not player related, so your inventory and XP have dropped where you died.");
                 return;
             }
         }
         // This is so that if the player is killed by the environment as an effect of another player, it STILL counts. - triarry
 	    else if(!player.hasPermission("pvprestore.participate")) 
 	    	return;
         else if ((playerDamage.getCause() == DamageCause.FIRE || playerDamage.getCause() == DamageCause.FIRE_TICK) && plugin.getConfig().getBoolean("other-events.fire") == true && player.hasPermission("pvprestore.events.fire")) 
         	killer = "Fire";
         else if (playerDamage.getCause() == DamageCause.VOID && plugin.getConfig().getBoolean("other-events.void") == true && player.hasPermission("pvprestore.events.void"))
         	killer = "The Void";
         else if (playerDamage.getCause() == DamageCause.LAVA && plugin.getConfig().getBoolean("other-events.lava") == true && player.hasPermission("pvprestore.events.lava"))
         	killer = "Lava";
         else if (playerDamage.getCause() == DamageCause.CONTACT && plugin.getConfig().getBoolean("other-events.cactus") == true && player.hasPermission("pvprestore.events.cactus"))
         	killer = "A Cactus";
         else if (playerDamage.getCause() == DamageCause.DROWNING && plugin.getConfig().getBoolean("other-events.drowning") == true && player.hasPermission("pvprestore.events.drowning"))
         	killer = "The Water";
         else if (playerDamage.getCause() == DamageCause.STARVATION && plugin.getConfig().getBoolean("other-events.starvation") == true && player.hasPermission("pvprestore.events.starvation"))
         	killer = "Their Lack of Food";
         else if (playerDamage.getCause() == DamageCause.SUFFOCATION && plugin.getConfig().getBoolean("other-events.suffocation") == true && player.hasPermission("pvprestore.events.suffocation"))
         	killer = "Their Lack of Air";
         else if (playerDamage.getCause() == DamageCause.FALL && plugin.getConfig().getBoolean("other-events.falling") == true && player.hasPermission("pvprestore.events.falling"))
         	killer = "Breaking their legs";
         else if (player.getKiller() != null) 
         	killer = player.getKiller().getName();
         else {
             player.sendMessage(ChatColor.RED + "Your death was not player related, so your inventory and XP have dropped where you died.");
             return;
         }
 
         if (player.hasPermission("pvprestore.keep") && plugin.getConfig().getBoolean("keep-inventory") && plugin.getConfig().getBoolean("keep-xp")) {
             event.setKeepLevel(true);
             if (plugin.getConfig().getInt("xp-to-remove") < 100 && plugin.getConfig().getInt("xp-to-remove") >= 0) {
                 player.setLevel((int) (player.getLevel() * ((100.0 - plugin.getConfig().getInt("xp-to-remove")) / 100.0)));
             }
             player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory and " + (100 - plugin.getConfig().getInt("xp-to-remove")) + "% of your XP has been saved.");
             if (player.hasPermission("pvprestore.money.steal") && plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
                 moneySteal(event);
             }
             event.setDroppedExp(0);
             if (plugin.getConfig().getBoolean("death-message")) {
                 event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + "!");
             }
             ItemStack[] content = player.getInventory().getContents();
             ItemStack[] content_armor = player.getInventory().getArmorContents();
             armor.put(player, content_armor);
             items.put(player, content);
             player.getInventory().clear();
             if (plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                 dropBlacklist(event);
             }
             else if (plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                 dropWhitelist(event);
             }
             if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true) {
                 dropPercentage(event);
             }
             if (plugin.getConfig().getBoolean("percentage-drop.enabled") != true && plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
             	event.getDrops().clear();
             }
         }
         else if ((player.hasPermission("pvprestore.keep.xp") || player.hasPermission("pvprestore.keep")) && plugin.getConfig().getBoolean("keep-xp")) {
             if (player.hasPermission("pvprestore.keep.inventory") && plugin.getConfig().getBoolean("keep-inventory")) {
                 event.setKeepLevel(true);
                 if (plugin.getConfig().getInt("xp-to-remove") < 100 && plugin.getConfig().getInt("xp-to-remove") >= 0) {
                     player.setLevel((int) (player.getLevel() * ((100.0 - plugin.getConfig().getInt("xp-to-remove")) / 100.0)));
                 }
                 if (player.hasPermission("pvprestore.money.steal") && plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
                     moneySteal(event);
                 }
                 event.setDroppedExp(0);
                 if (plugin.getConfig().getBoolean("death-message")) {
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + "!");
                 }
                 ItemStack[] content = player.getInventory().getContents();
                 ItemStack[] content_armor = player.getInventory().getArmorContents();
                 armor.put(player, content_armor);
                 items.put(player, content);
                 player.getInventory().clear();
                 if (plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                     dropBlacklist(event);
                 }
                 else if (plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                     dropWhitelist(event);
                 }
                 if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true) {
                    dropPercentage(event);
                 }
                 if (plugin.getConfig().getBoolean("percentage-drop.enabled") != true && plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
                 	event.getDrops().clear();
                 }
             }
             else {
                 event.setKeepLevel(true);
                 if (plugin.getConfig().getInt("xp-to-remove") < 100 && plugin.getConfig().getInt("xp-to-remove") >= 0) {
                     player.setLevel((int) (player.getLevel() * ((100.0 - plugin.getConfig().getInt("xp-to-remove")) / 100.0)));
                 }
                 if (player.hasPermission("pvprestore.money.steal") && plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
                     moneySteal(event);
                 }
                 player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so " + (100 - plugin.getConfig().getInt("xp-to-remove")) + "% of your XP has been saved.");
                 if (plugin.getConfig().getBoolean("death-message")) {
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + "!");
                 }
                 event.setDroppedExp(0);
             }
         }
         else if ((player.hasPermission("pvprestore.keep.inventory") || player.hasPermission("pvprestore.keep")) && plugin.getConfig().getBoolean("keep-inventory")) {
             if (player.hasPermission("pvprestore.keep.xp") && plugin.getConfig().getBoolean("keep-xp")) {
                 event.setKeepLevel(true);
                 if (plugin.getConfig().getInt("xp-to-remove") < 100 && plugin.getConfig().getInt("xp-to-remove") >= 0) {
                     player.setLevel((int) (player.getLevel() * ((100.0 - plugin.getConfig().getInt("xp-to-remove")) / 100.0)));
                 }
                 if (player.hasPermission("pvprestore.money.steal") && plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
                     moneySteal(event);
                 }
                 player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory and " + (100 - plugin.getConfig().getInt("xp-to-remove")) + "% of your XP has been saved.");
                 event.setDroppedExp(0);
                 if (plugin.getConfig().getBoolean("death-message")) {
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + "!");
                 }
                 ItemStack[] content = player.getInventory().getContents();
                 ItemStack[] content_armor = player.getInventory().getArmorContents();
                 armor.put(player, content_armor);
                 items.put(player, content);
                 player.getInventory().clear();
                 if (plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                     dropBlacklist(event);
                 }
                 else if (plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                     dropWhitelist(event);
                 }
                 if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true) {
                     dropPercentage(event);
                 }
                 if (plugin.getConfig().getBoolean("percentage-drop.enabled") != true && plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
                 	event.getDrops().clear();
                 }
             }
             else {
                 player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory has been saved.");
                 if (plugin.getConfig().getBoolean("death-message")) {
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + "!");
                 }
                 if (player.hasPermission("pvprestore.money.steal") && plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
                     moneySteal(event);
                 }
                 ItemStack[] content = player.getInventory().getContents();
                 ItemStack[] content_armor = player.getInventory().getArmorContents();
                 armor.put(player, content_armor);
                 items.put(player, content);
                 player.getInventory().clear();
                 if (plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                     dropBlacklist(event);
                 }
                 else if (plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                     dropWhitelist(event);
                 }
                 if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true) {
                     dropPercentage(event);
                 }
                 if (plugin.getConfig().getBoolean("percentage-drop.enabled") != true && plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
                 	event.getDrops().clear();
                 }
             }
         }
 	}
 	
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player p = event.getPlayer();
         if (items.containsKey(p)) {
             p.getInventory().clear();
             p.getInventory().setContents(items.get(p));
             items.remove(p);
             if (plugin.getConfig().getBoolean("blacklist.enabled") == true)
             	Utilities.getUtilities().blacklistItems(p);
             if (plugin.getConfig().getBoolean("whitelist.enabled") == true)
             	Utilities.getUtilities().whitelistItems(p);
             if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true)
             	Utilities.getUtilities().dropPercentageRemove(p, itemsToBeRemoved);
         }
         if (armor.containsKey(event.getPlayer()) && armor.size() != 0) {
             p.getInventory().setArmorContents(armor.get(p));
             armor.remove(p);
             if (plugin.getConfig().getBoolean("blacklist.enabled") == true) 
             	Utilities.getUtilities().blacklistItems(p);
             if (plugin.getConfig().getBoolean("whitelist.enabled") == true) 
             	Utilities.getUtilities().whitelistArmor(p);
             if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true)
             	Utilities.getUtilities().dropPercentageRemove(p, itemsToBeRemoved);
         }
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (event.getPlayer().isDead()) {
 			Player p = event.getPlayer();
 	        if (items.containsKey(p)) {
 	            p.getInventory().clear();
 	            p.getInventory().setContents(items.get(p));
 	            items.remove(p);
 	            if (plugin.getConfig().getBoolean("blacklist.enabled") == true) 
 	            	Utilities.getUtilities().blacklistItems(p);
 	            if (plugin.getConfig().getBoolean("whitelist.enabled") == true) 
 	            	Utilities.getUtilities().whitelistItems(p);
 	            if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true)
 	            	Utilities.getUtilities().dropPercentageRemove(p, itemsToBeRemoved);
 	        }
 	        if (armor.containsKey(p) && armor.size() != 0) {
 	            p.getInventory().setArmorContents(armor.get(p));
 	            armor.remove(p);
 	            if (plugin.getConfig().getBoolean("blacklist.enabled") == true) 
 	            	Utilities.getUtilities().blacklistItems(p);
 	            if (plugin.getConfig().getBoolean("whitelist.enabled") == true) 
 	            	Utilities.getUtilities().whitelistArmor(p);
 	            if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true)
 	            	Utilities.getUtilities().dropPercentageRemove(p, itemsToBeRemoved);
 	        }
 		}
 	}
 
     @EventHandler
 	public void onPlayerKick(PlayerKickEvent event) {
 		if (event.getPlayer().isDead()) {
 			Player p = event.getPlayer();
 	        if (items.containsKey(p)) {
 	            p.getInventory().clear();
 	            p.getInventory().setContents(items.get(p));
 	            items.remove(p);
 	            if (plugin.getConfig().getBoolean("blacklist.enabled") == true) 
 	            	Utilities.getUtilities().blacklistItems(p);
 	            if (plugin.getConfig().getBoolean("whitelist.enabled") == true) 
 	            	Utilities.getUtilities().whitelistItems(p);
 	            if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true)
 	            	Utilities.getUtilities().dropPercentageRemove(p, itemsToBeRemoved);
 	        }
 	        if (armor.containsKey(event.getPlayer()) && armor.size() != 0) {
 	            p.getInventory().setArmorContents(armor.get(p));
 	            armor.remove(p);
 	            if (plugin.getConfig().getBoolean("blacklist.enabled") == true) 
 	            	Utilities.getUtilities().blacklistItems(p);
 	            if (plugin.getConfig().getBoolean("whitelist.enabled") == true) 
 	            	Utilities.getUtilities().whitelistArmor(p);
 	            if (plugin.getConfig().getBoolean("percentage-drop.enabled") == true)
 	            	Utilities.getUtilities().dropPercentageRemove(p, itemsToBeRemoved);
 	        }
 		}
 	}
 
 	public void moneySteal(PlayerDeathEvent event) {
 		Player p = event.getEntity();
 		Player k = p.getKiller();
 		if (!p.hasPermission("pvprestore.money.exempt") && PvPRestore.econ != null && k != null) {
 			double r = PvPRestore.econ.getBalance(p.getName()) * (plugin.getConfig().getInt("vault.money-to-steal") / 100.0);
             PvPRestore.econ.depositPlayer(k.getName(), r);
             PvPRestore.econ.withdrawPlayer(p.getName(), r);
 			DecimalFormat dFormat = new DecimalFormat();
 			String d = dFormat.format(r);
 			k.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "You stole " + ChatColor.RED + d + " " + PvPRestore.econ.currencyNamePlural() + ChatColor.GREEN + " from " + ChatColor.RED + p.getName());
 		}
 	}
 	
 	public void dropBlacklist(PlayerDeathEvent event) {
 		Player p = event.getEntity();
 		for (Integer itemList : plugin.getConfig().getIntegerList("blacklist.items")) {
 			for (ItemStack itemStackList : event.getDrops()) {
 				if (itemStackList.getTypeId() == itemList) {
 					p.getLocation().getWorld().dropItem(p.getLocation(), itemStackList);
 				}
 			}
 		}
 		event.getDrops().clear();
 	}
 	
 	public void dropWhitelist(PlayerDeathEvent event) {
 		for (Integer itemList : plugin.getConfig().getIntegerList("whitelist.items")) {
 			Iterator<ItemStack> iterator = event.getDrops().iterator();
 			while (iterator.hasNext()) {
 				ItemStack itemStack = iterator.next();
 				if (itemStack.getTypeId() == itemList) {
 					iterator.remove();
 				}
 			}
 		}
 	}
 	
 	public void dropPercentage(PlayerDeathEvent event) {
 		Iterator<ItemStack> iterator = event.getDrops().iterator();
 		Double percentage = (plugin.getConfig().getInt("percentage-drop.percentage") / 100.0);
 		Integer i = 0;
 		Integer dropModifier = (int) (percentage * event.getDrops().size());
 		while (i < dropModifier && iterator.hasNext()) {
 			ItemStack stackToRemove = iterator.next();
 			itemsToBeRemoved.add(stackToRemove);
 			iterator.remove();
 			i++;
 		}
 	}
 }
