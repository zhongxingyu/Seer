 package com.me.tft_02.assassin.listeners;
 
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.me.tft_02.assassin.Assassin;
 import com.me.tft_02.assassin.AssassinMode;
 import com.me.tft_02.assassin.Bounty;
 import com.me.tft_02.assassin.config.Config;
 import com.me.tft_02.assassin.datatypes.Status;
 import com.me.tft_02.assassin.runnables.EndCooldownTimer;
 import com.me.tft_02.assassin.util.BlockChecks;
 import com.me.tft_02.assassin.util.ItemChecks;
 import com.me.tft_02.assassin.util.Misc;
 import com.me.tft_02.assassin.util.PlayerData;
 import com.me.tft_02.assassin.util.player.UserManager;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 public class PlayerListener implements Listener {
 
     private AssassinMode assassin = new AssassinMode(Assassin.p);
     private Bounty bounty = new Bounty(Assassin.p);
     private PlayerData data = new PlayerData(Assassin.p);
     private ItemChecks itemcheck = new ItemChecks(Assassin.p);
 
     /**
      * Monitor PlayerJoin events.
      *
      * @param event The event to watch
      */
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     private void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
 
         if (Misc.isNPCEntity(player)) {
             return;
         }
 
         UserManager.addUser(player);
 
         if (Assassin.p.updateAvailable && player.isOp()) {
             player.sendMessage(ChatColor.DARK_RED + "[Assassin]: " + ChatColor.GOLD + "New version available on BukkitDev!");
             player.sendMessage(ChatColor.DARK_RED + "[Assassin]: " + ChatColor.AQUA + "http://dev.bukkit.org/server-mods/Assassin/");
             Assassin.p.getLogger().log(Level.INFO, "New version available on BukkitDev! http://dev.bukkit.org/server-mods/Assassin/");
         }
 
         if (!data.isAssassin(player) || !data.isHostile(player)) {
             UserManager.getPlayer(player).setStatus(Status.NORMAL);
         }
         else if (data.isAssassin(player)) {
             event.setJoinMessage(ChatColor.DARK_RED + "AN ASSASSIN JOINED THE GAME");
             assassin.applyTraits(player);
             assassin.applyMaskForce(player);
         }
         if (!data.cooledDown(player)) {
             long cooldowntime = Config.getInstance().getCooldownLength();
             new EndCooldownTimer(player.getName()).runTaskLater(Assassin.p, cooldowntime);
         }
     }
 
     @EventHandler
     private void onPlayerRespawn(PlayerRespawnEvent event) {
         final Player player = event.getPlayer();
         if (data.isAssassin(player)) {
             assassin.applyMaskForce(player);
 
             if (!Config.getInstance().getPotionEffectsEnabled()) {
                 return;
             }
 
             Bukkit.getScheduler().scheduleSyncDelayedTask(Assassin.p, new Runnable() {
                 @Override
                 public void run() {
                     player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1));
                     player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 30, 1));
                 }
             }, 5);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     private void onPlayerQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         if (data.isAssassin(player)) {
             event.setQuitMessage(ChatColor.DARK_RED + "AN ASSASSIN LEFT THE GAME");
             data.addLogoutTime(player);
             data.saveActiveTime(player);
             data.leaveAssassinChat(player);
         }
     }
 
     @EventHandler
     void onInventoryClick(InventoryClickEvent event) {
         HumanEntity player = event.getWhoClicked();
         ItemStack itemstack = event.getCurrentItem();
 
        if (event.getSlotType() == SlotType.ARMOR && itemcheck.isMask(itemstack)) {
             assassin.activateHostileMode((Player) player);
         }
     }
 
     /**
      * Monitor PlayerInteract events.
      *
      * @param event The event to watch
      */
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         Action action = event.getAction();
         Block block = event.getClickedBlock();
         ItemStack inHand = player.getItemInHand();
 
         switch (action) {
             case RIGHT_CLICK_BLOCK:
             case RIGHT_CLICK_AIR:
                 if ((inHand.getTypeId() != 35) || !BlockChecks.abilityBlockCheck(block)) {
                     return;
                 }
 
                 if (!itemcheck.isMask(inHand)) {
                     return;
                 }
 
                 if (!player.hasPermission("assassin.assassin")) {
                     player.sendMessage(ChatColor.RED + "You haven't got permission.");
                     return;
                 }
 
                 if (!data.cooledDown(player)) {
                     player.sendMessage(ChatColor.RED + "You need to wait before you can use that again...");
                     return;
                 }
 
                 if (data.isAssassin(player)) {
                     player.sendMessage(ChatColor.RED + "You already are an Assassin.");
                     return;
                 }
 
                 double activation_cost = Config.getInstance().getActivationCost();
                 if (Assassin.p.vaultEnabled && activation_cost > 0) {
                     EconomyResponse r = Assassin.econ.withdrawPlayer(player.getName(), activation_cost);
 
                     if (r.transactionSuccess()) {
                         player.sendMessage(String.format(ChatColor.RED + "You were charged %s %s", Assassin.econ.format(r.amount), Assassin.econ.currencyNamePlural()));
                     }
                     else {
                         player.sendMessage(String.format("An error occured: %s", r.errorMessage));
                         return;
                     }
                 }
 
                 Assassin.p.debug("Activating AssassinMode for " + player.getName());
                 assassin.activateAssassin(player);
                 long cooldowntime = Config.getInstance().getCooldownLength();
                 new EndCooldownTimer(player.getName()).runTaskLater(Assassin.p, cooldowntime);
 
                 event.setCancelled(true);
                 return;
             default:
                 break;
         }
     }
 
     //
     //	@EventHandler
     //	public void onItemDrop(PlayerDropItemEvent event) {
     //		ItemStack droppeditem = event.getItemDrop().getItemStack();
     //		if (itemcheck.isMask(droppeditem)) {
     //			event.setCancelled(true);
     //			event.getPlayer().sendMessage(ChatColor.RED + "You're not allowed to drop masks.");
     //		}
     //	}
 
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onPlayerDeathLowest(PlayerDeathEvent event) {
         Player player = event.getEntity();
 
         if (!data.isAssassin(player)) {
             return;
         }
 
         List<ItemStack> drops = event.getDrops();
         for (ItemStack drop : drops) {
             if (itemcheck.isMask(drop)) {
                 drops.remove(drop);
                 return;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerDeathHighest(PlayerDeathEvent event) {
         Player player = event.getEntity();
         EntityDamageEvent lastDamageCause = player.getLastDamageCause();
         String deathmessage = event.getDeathMessage();
 
         if (data.isAssassin(player)) {
             deathmessage = deathmessage.replaceAll(player.getName(), ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.RESET);
         }
 
         if (lastDamageCause instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) lastDamageCause;
             Entity damager = entityDamageByEntityEvent.getDamager();
 
             if (damager instanceof Projectile) {
                 damager = ((Projectile) damager).getShooter();
             }
 
             if (damager instanceof Player) {
                 if (data.isAssassin((Player) damager)) {
                     String damagername = ((Player) damager).getName();
                     deathmessage = deathmessage.replaceAll(damagername, ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.RESET);
                 }
             }
         }
         event.setDeathMessage(deathmessage);
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         Player killer = event.getEntity().getKiller();
 
         if (killer != null && player != killer) {
             bounty.handleBounties(player, killer);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
         Player player = event.getPlayer();
         String command = event.getMessage();
         List<String> blockedCmds = Config.getInstance().getBlockedCommands();
 
         if (data.isAssassin(player) && blockedCmds.contains(command)) {
             player.sendMessage(ChatColor.RED + "You're not allowed to use " + ChatColor.GOLD + command + ChatColor.RED + " command while an Assassin.");
             event.setCancelled(true);
         }
     }
 }
