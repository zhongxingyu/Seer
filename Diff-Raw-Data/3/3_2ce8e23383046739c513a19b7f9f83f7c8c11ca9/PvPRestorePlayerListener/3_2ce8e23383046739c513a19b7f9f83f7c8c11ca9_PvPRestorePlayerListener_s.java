 package com.github.triarry.PvPRestore;
 
 import de.Keyle.MyPet.entity.types.CraftMyPet;
 import de.Keyle.MyPet.entity.types.MyPet;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 
 public class PvPRestorePlayerListener implements Listener {
 	
 	private PvPRestore plugin;
 	 
 	public HashMap<Player , ItemStack[]> items = new HashMap<Player , ItemStack[]>();
 	public HashMap<Player , ItemStack[]> armor = new HashMap<Player , ItemStack[]>();
 
     public PvPRestorePlayerListener(PvPRestore plugin) {
         this.plugin = plugin;
     }
 
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         String killer;
 
         if(player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent lastDamageEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
             if(lastDamageEvent.getDamager() instanceof Player) {
                 killer = player.getKiller().getName();
             }
             else if(PvPRestore.myPetEnabled && lastDamageEvent.getDamager() instanceof CraftMyPet && plugin.getConfig().getBoolean("my-pet-enabled") == true) {
                 MyPet myPet = ((CraftMyPet) lastDamageEvent.getDamager()).getMyPet();
                 killer = myPet.getOwner().getName() + "'s pet " + myPet.petName;
             }
             else {
                 player.sendMessage(ChatColor.RED + "Your death was not player related, so your inventory and XP have dropped where you died.");
                 return;
             }
         }
         // This is so that if the player is killed by the environment as an effect of another player, it STILL counts. - triarry
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
             if (plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
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
             event.getDrops().clear();
         }
         else if ((player.hasPermission("pvprestore.keep.xp") || player.hasPermission("pvprestore.keep")) && plugin.getConfig().getBoolean("keep-xp")) {
             if (player.hasPermission("pvprestore.keep.inventory")) {
                 event.setKeepLevel(true);
                 if (plugin.getConfig().getInt("xp-to-remove") < 100 && plugin.getConfig().getInt("xp-to-remove") >= 0) {
                     player.setLevel((int) (player.getLevel() * ((100.0 - plugin.getConfig().getInt("xp-to-remove")) / 100.0)));
                 }
                 if (plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
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
                 event.getDrops().clear();
             }
             else {
                 event.setKeepLevel(true);
                 if (plugin.getConfig().getInt("xp-to-remove") < 100 && plugin.getConfig().getInt("xp-to-remove") >= 0) {
                     player.setLevel((int) (player.getLevel() * ((100.0 - plugin.getConfig().getInt("xp-to-remove")) / 100.0)));
                 }
                 if (plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
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
             if (player.hasPermission("pvprestore.keep.xp")) {
                 event.setKeepLevel(true);
                 if (plugin.getConfig().getInt("xp-to-remove") < 100 && plugin.getConfig().getInt("xp-to-remove") >= 0) {
                     player.setLevel((int) (player.getLevel() * ((100.0 - plugin.getConfig().getInt("xp-to-remove")) / 100.0)));
                 }
                 if (plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
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
                 event.getDrops().clear();
             }
             else {
                 player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory has been saved.");
                 if (plugin.getConfig().getBoolean("death-message")) {
                     event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer + ChatColor.GREEN + ", and their inventory was saved!");
                 }
                 if (plugin.getConfig().getBoolean("vault.enabled") && killer != null) {
                     moneySteal(event);
                 }
                 ItemStack[] content = player.getInventory().getContents();
                 ItemStack[] content_armor = player.getInventory().getArmorContents();
                 armor.put(player, content_armor);
                 items.put(player, content);
                 player.getInventory().clear();
                 event.getDrops().clear();
             }
         }
 	}
 	
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
         if(items.containsKey(event.getPlayer())){
             event.getPlayer().getInventory().clear();
             event.getPlayer().getInventory().setContents(items.get(event.getPlayer()));
             items.remove(event.getPlayer());
         }
         if(armor.containsKey(event.getPlayer()) && armor.size() != 0) {
             event.getPlayer().getInventory().setArmorContents(armor.get(event.getPlayer()));
             armor.remove(event.getPlayer());
         }
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (event.getPlayer().isDead()) {
 	        if(items.containsKey(event.getPlayer())){
 	            event.getPlayer().getInventory().clear();
 	            event.getPlayer().getInventory().setContents(items.get(event.getPlayer()));
 	            items.remove(event.getPlayer());
 	        }
 	        if(armor.containsKey(event.getPlayer()) && armor.size() != 0) {
 	            event.getPlayer().getInventory().setArmorContents(armor.get(event.getPlayer()));
 	            armor.remove(event.getPlayer());
 	        }
 		}
 	}
 
     @EventHandler
 	public void onPlayerKick(PlayerKickEvent event) {
 		if (event.getPlayer().isDead()) {
 	        if(items.containsKey(event.getPlayer())){
 	            event.getPlayer().getInventory().clear();
 	            event.getPlayer().getInventory().setContents(items.get(event.getPlayer()));
 	            items.remove(event.getPlayer());
 	        }
 	        if(armor.containsKey(event.getPlayer()) && armor.size() != 0) {
 	            event.getPlayer().getInventory().setArmorContents(armor.get(event.getPlayer()));
 	            armor.remove(event.getPlayer());
 	        }
 		}
 	}
 
 	public void moneySteal(PlayerDeathEvent event) {
 		Player player = event.getEntity();
 
 		Player killer = player.getKiller();
 		if (PvPRestore.econ != null && killer != null) {
 			double r = PvPRestore.econ.getBalance(player.getName()) * (plugin.getConfig().getInt("vault.money-to-steal") / 100.0);
             //plugin.getLogger().info("" + r);
 			//plugin.getLogger().info("" + plugin.getConfig().getInt("vault.money-to-steal"));
             PvPRestore.econ.depositPlayer(killer.getName(), r);
             PvPRestore.econ.withdrawPlayer(player.getName(), r);
 			DecimalFormat dFormat = new DecimalFormat();
 			String d = dFormat.format(r);
 			killer.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "You stole " + ChatColor.RED + d + " " + PvPRestore.econ.currencyNamePlural() + ChatColor.GREEN + " from " + ChatColor.RED + player.getName());
 		}
 	}
 }
