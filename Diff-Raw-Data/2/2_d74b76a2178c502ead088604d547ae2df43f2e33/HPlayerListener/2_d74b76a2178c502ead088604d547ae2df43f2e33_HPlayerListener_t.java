 package com.herocraftonline.dev.heroes;
 
 import java.util.List;
 
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.Command;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroManager;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class HPlayerListener extends PlayerListener {
 
     public final Heroes plugin;
 
     public HPlayerListener(Heroes instance) {
         plugin = instance;
     }
 
     @Override
     public void onItemHeldChange(PlayerItemHeldEvent event) {
         this.plugin.getInventoryChecker().checkInventory(event.getPlayer());
     }
 
     @Override
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         Player player = event.getPlayer();
         Hero hero = plugin.getHeroManager().getHero(player);
         hero.setHealth(hero.getMaxHealth());
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.useItemInHand() == Result.DENY)
             return;
 
         Player player = event.getPlayer();
 
         Material material = player.getItemInHand().getType();
         Hero hero = plugin.getHeroManager().getHero(player);
         if (hero.getBinds().containsKey(material)) {
             if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 event.setCancelled(false);
                 String[] args = hero.getBinds().get(material);
                 plugin.onCommand(player, null, "skill", args);
             }
         }
     }
 
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         plugin.getHeroManager().loadHero(player);
         plugin.switchToHNSH(player);
         this.plugin.getInventoryChecker().checkInventory(player);
         if(plugin.getConfigManager().getProperties().prefixClassName) {
             Hero hero = plugin.getHeroManager().getHero(player);
            player.setDisplayName("[" + hero.getHeroClass().getName() + "]" + player.getName());
         }
     }
 
     @Override
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if (event.isCancelled()) {
             return;
         }
 
         final Player player = event.getPlayer();
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             public void run() {
                 plugin.getInventoryChecker().checkInventory(player.getName());
             }
         });
 
         Hero hero = plugin.getHeroManager().getHero(player);
 
         if (!hero.hasParty()) {
             return;
         }
         HeroParty party = hero.getParty();
         if (!party.updateMapDisplay() && event.getItem().getItemStack().getType().toString().equalsIgnoreCase("MAP")) {
             party.setUpdateMapDisplay(true);
         }
     }
 
     @Override
     public void onPlayerQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         HeroManager heroManager = plugin.getHeroManager();
         heroManager.getHero(event.getPlayer()).clearEffects();
         heroManager.saveHero(player);
         heroManager.removeHero(heroManager.getHero(player));
         for (Command command : plugin.getCommandHandler().getCommands()) {
             if (command.isInteractive()) {
                 command.cancelInteraction(player);
             }
         }
     }
 
     @Override
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         if (event.isCancelled())
             return;
 
         Player player = event.getPlayer();
         if (event.getFrom().getWorld() != event.getTo().getWorld()) {
             Hero hero = plugin.getHeroManager().getHero(player);
             HeroClass heroClass = hero.getHeroClass();
 
             List<Command> commands = plugin.getCommandHandler().getCommands();
             for (Command cmd : commands) {
                 if (cmd instanceof OutsourcedSkill) {
                     OutsourcedSkill skill = (OutsourcedSkill) cmd;
                     if (heroClass.hasSkill(skill.getName())) {
                         skill.tryLearningSkill(hero);
                     }
                 }
             }
         }
 
     }
 
     @Override
     public void onPlayerBedEnter(PlayerBedEnterEvent event) {
         if (event.isCancelled() || !plugin.getConfigManager().getProperties().bedHeal) {
             return;
         }
 
         Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
         long period = plugin.getConfigManager().getProperties().healInterval * 1000;
         double tickHealPercent = plugin.getConfigManager().getProperties().healPercent / 100.0;
         BedHealEffect bhEffect = new BedHealEffect(period, 600000, tickHealPercent);
         hero.addEffect(bhEffect);
     }
 
     @Override
     public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
         if (!plugin.getConfigManager().getProperties().bedHeal) {
             return;
         }
 
         // This player is no longer in bed so remove them from the bedHealer set
         Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
         hero.removeEffect(hero.getEffect("BedHeal"));
     }
 
     public class BedHealEffect extends PeriodicEffect {
 
         private final double tickHealPercent;        
         public BedHealEffect(long period, long duration, double tickHealPercent) {
             super(null, "BedHeal", period, duration);
             this.tickHealPercent = tickHealPercent;
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             this.lastTickTime = getApplyTime();
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
         }
 
         @Override
         public void tick(Hero hero) {
             super.tick(hero);
             Player player = hero.getPlayer();
             double healAmount = hero.getMaxHealth() * tickHealPercent;
             hero.setHealth(hero.getHealth() + healAmount);
             hero.syncHealth();
             if (hero.isVerbose()) {
                 player.sendMessage(Messaging.createFullHealthBar(hero.getHealth(), hero.getMaxHealth()));
             }
         }
     }
 }
