 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.effects.Expirable;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.effects.Periodic;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroManager;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class SkillBlackjack extends ActiveSkill {
 
     private String applyText;
     private String expireText;
     private String stunApplyText;
     private String stunExpireText;
 
     private PlayerListener playerListener = new SkillPlayerListener();
     private EntityListener entityListener = new SkillEntityListener(this);
 
     private Random random = new Random();
 
     public SkillBlackjack(Heroes plugin) {
         super(plugin);
         setName("Blackjack");
         setDescription("Occasionally stuns your opponent");
         setUsage("/skill blackjack");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("skill blackjack");
 
         registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal);
         registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("apply-text", "%hero% prepared his blackjack!");
         node.setProperty("expire-text", "%hero% sheathed his blackjack!");
         node.setProperty("stun-duration", 5000);
         node.setProperty("stun-chance", 0.20);
         node.setProperty("stun-apply-text", "%target% is stunned!");
         node.setProperty("stun-expire-text", "%target% is no longer stunned!");
         node.setProperty("duration", 20000);
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, "apply-text", "%hero% prepared his blackjack!").replace("%hero%", "$1");
         expireText = getSetting(null, "expire-text", "%hero% sheathed his blackjack!").replace("%hero%", "$1");
         stunApplyText = getSetting(null, "stun-apply-text", "%target% is stunned!").replace("%target%", "$1");
         stunExpireText = getSetting(null, "stun-expire-text", "%target% is no longer stunned!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
        int duration = getSetting(hero.getHeroClass(), "duration", 20000);
         hero.addEffect(new BlackjackEffect(this, duration));
 
         return true;
     }
 
     public class BlackjackEffect extends ExpirableEffect {
 
         public BlackjackEffect(Skill skill, long duration) {
             super(skill, "Blackjack", duration);
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
 
     }
 
     public class SkillEntityListener extends EntityListener {
 
         private final Skill skill;
 
         public SkillEntityListener(Skill skill) {
             this.skill = skill;
         }
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled()) return;
             if (event instanceof EntityDamageByEntityEvent) {
                 EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                 if (subEvent.getCause() != DamageCause.ENTITY_ATTACK) return;
 
                 Entity attackingEntity = subEvent.getDamager();
                 Entity defendingEntity = subEvent.getEntity();
 
                 if (!(attackingEntity instanceof Player)) return;
 
                 if (!(defendingEntity instanceof Player)) return;
 
                 HeroManager heroManager = plugin.getHeroManager();
                 Hero attackingHero = heroManager.getHero((Player) attackingEntity);
                 Hero defendingHero = heroManager.getHero((Player) defendingEntity);
 
                 if (attackingHero.hasEffect("Stun")) {
                     event.setCancelled(true);
                     return;
                 }
 
                 if (!attackingHero.hasEffect("Blackjack")) return;
 
                 HeroClass heroClass = attackingHero.getHeroClass();
                 double chance = getSetting(heroClass, "stun-chance", 0.20);
                 if (random.nextDouble() < chance) {
                     int duration = getSetting(heroClass, "stun-duration", 5000);
                     defendingHero.addEffect(new StunEffect(skill, duration));
                 }
             }
         }
 
     }
 
     public class SkillPlayerListener extends PlayerListener {
 
         @Override
         public void onPlayerInteract(PlayerInteractEvent event) {
             Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
             if (hero.hasEffect("Stun")) {
                 event.setCancelled(true);
             }
         }
 
     }
 
     public class StunEffect extends PeriodicEffect implements Periodic, Expirable {
 
         private static final long period = 100;
 
         private double x, y, z;
 
         public StunEffect(Skill skill, long duration) {
             super(skill, "Stun", period, duration);
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
 
             Player player = hero.getPlayer();
             Location location = hero.getPlayer().getLocation();
             x = location.getX();
             y = location.getY();
             z = location.getZ();
 
             broadcast(location, stunApplyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
 
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), stunExpireText, player.getDisplayName());
         }
 
         @Override
         public void tick(Hero hero) {
             super.tick(hero);
 
             Player player = hero.getPlayer();
             Location location = player.getLocation();
             if (location.getX() != x || location.getY() != y || location.getZ() != z) {
                 location.setX(x);
                 location.setY(y);
                 location.setZ(z);
                 player.teleport(location);
             }
         }
     }
 
 }
