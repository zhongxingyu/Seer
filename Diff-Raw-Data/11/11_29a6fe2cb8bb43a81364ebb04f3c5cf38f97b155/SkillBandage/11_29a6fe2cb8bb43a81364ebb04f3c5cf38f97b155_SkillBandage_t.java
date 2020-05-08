 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillBandage extends TargettedSkill {
 
     private HashMap<Integer, Integer> playerSchedulers = new HashMap<Integer, Integer>();
 
     public SkillBandage(Heroes plugin) {
         super(plugin);
         name = "Bandage";
         description = "Bandages the target";
         usage = "/skill bandage [target]";
         minArgs = 0;
         maxArgs = 1;
         identifiers.add("skill bandage");
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(SETTING_MAXDISTANCE, 5);
         node.setProperty("tick-health", 1);
         node.setProperty("ticks", 10);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (target instanceof Player) {
             Player tPlayer = (Player) target;
             if (!(player.getItemInHand().getType() == Material.PAPER)) {
                 Messaging.send(player, "You need paper to perform this.");
                 return false;
             }
 
             if (playerSchedulers.containsKey(tPlayer.getEntityId())) {
                 Messaging.send(player, "$1 is already being bandaged.", tPlayer.getName());
                 return false;
             }
 
             if (tPlayer.getHealth() >= 20) {
                 Messaging.send(player, "$1 is already at full health.", tPlayer.getName());
                 return false;
             }
 
             HeroClass heroClass = hero.getHeroClass();
             int ticks = getSetting(heroClass, "ticks", 10);
             int tickHealth = getSetting(heroClass, "tick-health", 1);
             playerSchedulers.put(tPlayer.getEntityId(), plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new BandageTask(plugin, tPlayer, ticks, tickHealth), 20L, 20L));
 
             notifyNearbyPlayers(player.getLocation(), useText, player.getName(), name, tPlayer == player ? "himself" : tPlayer.getName());
 
             // The following should consume 1 piece of Paper per cast.
             int firstSlot = player.getInventory().first(Material.PAPER);
             int num = player.getInventory().getItem(firstSlot).getAmount();
             if (num == 1) {
                 player.getInventory().clear(firstSlot);
             } else if (num > 1) {
                 player.getInventory().getItem(firstSlot).setAmount(num - 1);
             }
 
             return true;
         }
         return false;
     }
 
     private class BandageTask implements Runnable {
         private JavaPlugin plugin;
         private Player target;
         private int timesRan = 0;
         private final int ticks;
         private final int tickHealth;
 
         public BandageTask(JavaPlugin plugin, Player target, int ticks, int tickHealth) {
             this.plugin = plugin;
             this.target = target;
             this.ticks = ticks;
             this.tickHealth = tickHealth;
         }
 
         @Override
         public void run() {
             int health = 20;
             if (target != null) {
                 health = target.getHealth();
             }
             if (target == null || timesRan == ticks || health >= 20) {
                 if (health >= 20) {
                     notifyNearbyPlayers(target.getLocation(), "$1 has been healed to full health by their bandages.", target.getName());
                 } else {
                     notifyNearbyPlayers(target.getLocation(), "$1 bandages have worn out.", target.getName() + "'s");
                 }
                 int id = playerSchedulers.remove(target.getEntityId());
                 plugin.getServer().getScheduler().cancelTask(id);
             } else {
                 timesRan++;
                 target = plugin.getServer().getPlayer(target.getName());
                 if (target != null) {
                    // If the Target is already dead then we stop here otherwise a bug occurs where they can't respawn.
                    if (health <= 0) {
                        int id = playerSchedulers.remove(target.getEntityId());
                        plugin.getServer().getScheduler().cancelTask(id);
                        return;
                    }
                    // Get the new Health value.
                     int newHealth = health + tickHealth;
                    // Make sure it's not above 20.
                     newHealth = newHealth > 20 ? 20 : newHealth;
                    // Make sure it's not below 0.
                     newHealth = newHealth < 0 ? 0 : newHealth;
                     target.setHealth(newHealth);
                 }
             }
         }
     }
 }
