 package com.github.triarry.PvPRestore;
 
 import de.Keyle.MyPet.entity.types.CraftMyPet;
 import de.Keyle.MyPet.entity.types.MyPet;
 
 import com.github.triarry.PvPRestore.utilities.Utilities;
 
 import org.bukkit.ChatColor;
import org.bukkit.block.Dispenser;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class PvPRestorePlayerListener implements Listener {
 	
 	private PvPRestore plugin;
 	 
 	public HashMap<Player , ItemStack[]> items = new HashMap<Player , ItemStack[]>();
 	public HashMap<Player , ItemStack[]> armor = new HashMap<Player , ItemStack[]>();
 
     public PvPRestorePlayerListener(PvPRestore plugin) {
         this.plugin = plugin;
     }
     
 	@EventHandler
 	public void informPlayers(PlayerJoinEvent event) {
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
 
         if(player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent lastDamageEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
             if(!player.hasPermission("pvprestore.participate")) {
             	return;
             }
             else if(lastDamageEvent.getDamager() instanceof Player) {
                 killer = player.getKiller().getName();
             }
             else if (lastDamageEvent.getDamager() instanceof TNTPrimed && plugin.getConfig().getBoolean("other-events.tnt") == true) {
             	killer = "TNT";
             }
            //plugin.getConfig().getBoolean("dispenser") && 
            else if (lastDamageEvent.getDamager() instanceof Dispenser) {
             	killer = "Dispenser";
             }
             else if(PvPRestore.myPetEnabled && lastDamageEvent.getDamager() instanceof CraftMyPet && plugin.getConfig().getBoolean("my-pet-enabled") == true) {
                 MyPet myPet = ((CraftMyPet) lastDamageEvent.getDamager()).getMyPet();
                 killer = myPet.getOwner().getName() + "'s pet " + myPet.petName;
             }
             else if (player.getKiller() != null) {
             	killer = player.getKiller().getName();
             }
             else {
                 player.sendMessage(ChatColor.RED + "Your death was not player related, so your inventory and XP have dropped where you died.");
                 return;
             }
         }
         // This is so that if the player is killed by the environment as an effect of another player, it STILL counts. - triarry
 	    else if(!player.hasPermission("pvprestore.participate")) {
 	    	return;
 	    }
         else if (player.getKiller() != null) {
         	killer = player.getKiller().getName();
         }
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
                 event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + ", and their XP and inventory was saved!");
             }
             ItemStack[] content = player.getInventory().getContents();
             ItemStack[] content_armor = player.getInventory().getArmorContents();
             armor.put(player, content_armor);
             items.put(player, content);
             player.getInventory().clear();
             if (player.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                 dropBlacklist(event);
             }
             else if (player.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                 dropWhitelist(event);
             }
             else if (plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
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
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + ", and their XP and inventory was saved!");
                 }
                 ItemStack[] content = player.getInventory().getContents();
                 ItemStack[] content_armor = player.getInventory().getArmorContents();
                 armor.put(player, content_armor);
                 items.put(player, content);
                 player.getInventory().clear();
                 if (player.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                     dropBlacklist(event);
                 }
                 else if (player.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                     dropWhitelist(event);
                 }
                 else if (plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
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
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + ", and their XP was saved!");
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
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + ", and their XP and inventory was saved!");
                 }
                 ItemStack[] content = player.getInventory().getContents();
                 ItemStack[] content_armor = player.getInventory().getArmorContents();
                 armor.put(player, content_armor);
                 items.put(player, content);
                 player.getInventory().clear();
                 if (player.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                     dropBlacklist(event);
                 }
                 if (player.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                     dropWhitelist(event);
                 }
                 if (plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
                 	event.getDrops().clear();
                 }
             }
             else {
                 player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory has been saved.");
                 if (plugin.getConfig().getBoolean("death-message")) {
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + ", and their inventory was saved!");
                 }
                 if (player.hasPermission("pvprestore.money.steal") && plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
                     moneySteal(event);
                 }
                 ItemStack[] content = player.getInventory().getContents();
                 ItemStack[] content_armor = player.getInventory().getArmorContents();
                 armor.put(player, content_armor);
                 items.put(player, content);
                 player.getInventory().clear();
                 if (player.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
                     dropBlacklist(event);
                 }
                 else if (player.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
                     dropWhitelist(event);
                 }
                 else if (plugin.getConfig().getBoolean("whitelist.enabled") != true && plugin.getConfig().getBoolean("blacklist.enabled") != true) {
                 	event.getDrops().clear();
                 }
             }
         }
 	}
 	
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player p = event.getPlayer();
         if (items.containsKey(event.getPlayer())) {
             p.getInventory().clear();
             p.getInventory().setContents(items.get(p));
             items.remove(p);
             if (p.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
             	Utilities.getUtilities().blacklistItems(p);
             }
             if (p.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
             	Utilities.getUtilities().whitelistItems(p);
             }
         }
         if (armor.containsKey(event.getPlayer()) && armor.size() != 0) {
             p.getInventory().setArmorContents(armor.get(p));
             armor.remove(p);
             if (p.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
             	Utilities.getUtilities().blacklistItems(p);
             }
             if (p.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
             	Utilities.getUtilities().whitelistArmor(p);
             }
         }
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (event.getPlayer().isDead()) {
 			Player p = event.getPlayer();
 	        if (items.containsKey(event.getPlayer())) {
 	            p.getInventory().clear();
 	            p.getInventory().setContents(items.get(p));
 	            items.remove(p);
 	            if (p.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
 	            	Utilities.getUtilities().blacklistItems(p);
 	            }
 	            if (p.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
 	            	Utilities.getUtilities().whitelistItems(p);
 	            }
 	        }
 	        if (armor.containsKey(event.getPlayer()) && armor.size() != 0) {
 	            p.getInventory().setArmorContents(armor.get(p));
 	            armor.remove(p);
 	            if (p.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
 	            	Utilities.getUtilities().blacklistItems(p);
 	            }
 	            if (p.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
 	            	Utilities.getUtilities().whitelistArmor(p);
 	            }
 	        }
 		}
 	}
 
     @EventHandler
 	public void onPlayerKick(PlayerKickEvent event) {
 		if (event.getPlayer().isDead()) {
 			Player p = event.getPlayer();
 	        if (items.containsKey(event.getPlayer())) {
 	            p.getInventory().clear();
 	            p.getInventory().setContents(items.get(p));
 	            items.remove(p);
 	            if (p.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
 	            	Utilities.getUtilities().blacklistItems(p);
 	            }
 	            if (p.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
 	            	Utilities.getUtilities().whitelistItems(p);
 	            }
 	        }
 	        if (armor.containsKey(event.getPlayer()) && armor.size() != 0) {
 	            p.getInventory().setArmorContents(armor.get(p));
 	            armor.remove(p);
 	            if (p.hasPermission("pvprestore.blacklist.drop") && plugin.getConfig().getBoolean("blacklist.enabled") == true) {
 	            	Utilities.getUtilities().blacklistItems(p);
 	            }
 	            if (p.hasPermission("pvprestore.whitelist.drop") && plugin.getConfig().getBoolean("whitelist.enabled") == true) {
 	            	Utilities.getUtilities().whitelistArmor(p);
 	            }
 	        }
 		}
 	}
 
 	public void moneySteal(PlayerDeathEvent event) {
 		Player player = event.getEntity();
 		Player killer = player.getKiller();
 		if (!player.hasPermission("pvprestore.money.exempt") && PvPRestore.econ != null && killer != null) {
 			double r = PvPRestore.econ.getBalance(player.getName()) * (plugin.getConfig().getInt("vault.money-to-steal") / 100.0);
             PvPRestore.econ.depositPlayer(killer.getName(), r);
             PvPRestore.econ.withdrawPlayer(player.getName(), r);
 			DecimalFormat dFormat = new DecimalFormat();
 			String d = dFormat.format(r);
 			killer.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "You stole " + ChatColor.RED + d + " " + PvPRestore.econ.currencyNamePlural() + ChatColor.GREEN + " from " + ChatColor.RED + player.getName());
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
 }
