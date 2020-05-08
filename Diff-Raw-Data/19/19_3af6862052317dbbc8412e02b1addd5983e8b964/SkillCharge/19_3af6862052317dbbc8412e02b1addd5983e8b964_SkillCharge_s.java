 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.Vector;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.common.RootEffect;
 import com.herocraftonline.dev.heroes.effects.common.SilenceEffect;
 import com.herocraftonline.dev.heroes.effects.common.SlowEffect;
 import com.herocraftonline.dev.heroes.effects.common.StunEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillCharge extends TargettedSkill {
 
     private Set<String> chargingPlayers = new HashSet<String>();
 
     public SkillCharge(Heroes plugin) {
         super(plugin, "Charge");
         setDescription("You charge toward your target!");
         setUsage("/skill charge");
         setArgumentRange(0, 1);
         setIdentifiers("skill charge");
         setTypes(SkillType.PHYSICAL, SkillType.MOVEMENT, SkillType.HARMFUL);
         Bukkit.getServer().getPluginManager().registerEvents(new ChargeEntityListener(this), plugin);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection section = super.getDefaultConfig();
         section.set("stun-duration", 5000);
         section.set("slow-duration", 0);
         section.set("root-duration", 0);
         section.set("silence-duration", 0);
         section.set(Setting.DAMAGE.node(), 0);
         section.set(Setting.RADIUS.node(), 2);
         return section;
     }
 
     @Override
     public SkillResult use(Hero hero, LivingEntity target, String[] args) {
         final Player player = hero.getPlayer();
 
         Location playerLoc = player.getLocation();
         Location targetLoc = target.getLocation();
 
         double xDir = (targetLoc.getX() - playerLoc.getX());
         double zDir = (targetLoc.getZ() - playerLoc.getZ());
         Vector v = new Vector(xDir / 3, .5, zDir / 3);
         player.setVelocity(v);
 
         chargingPlayers.add(hero.getName());
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 player.setFallDistance(8f);
             }
         }, 2);
         broadcastExecuteText(hero, target);
         return SkillResult.NORMAL;
     }
 
     public class ChargeEntityListener implements Listener {
         private final Skill skill;
 
         public ChargeEntityListener(Skill skill) {
             this.skill = skill;
         }
 
         @EventHandler(priority = EventPriority.LOWEST)
         public void onEntityDamage(EntityDamageEvent event) {
             if (!event.getCause().equals(DamageCause.FALL) || !(event.getEntity() instanceof Player) || !chargingPlayers.contains(event.getEntity())) {
                 return;
             }
 
             Player player = (Player) event.getEntity();
             Hero hero = plugin.getHeroManager().getHero(player);
             chargingPlayers.remove(hero.getName());
             Heroes.log(Level.INFO, "Player landed!");
             event.setDamage(0);
             event.setCancelled(true);
 
             int radius = SkillConfigManager.getUseSetting(hero, skill, Setting.RADIUS.node(), 2, false);
             long stunDuration = SkillConfigManager.getUseSetting(hero, skill, "stun-duration", 5000, false);
             long slowDuration = SkillConfigManager.getUseSetting(hero, skill, "slow-duration", 0, false);
             long rootDuration = SkillConfigManager.getUseSetting(hero, skill, "root-duration", 0, false);
             long silenceDuration = SkillConfigManager.getUseSetting(hero, skill, "silence-duration", 0, false);
             int damage = SkillConfigManager.getUseSetting(hero, skill, Setting.DAMAGE.node(), 0, false);
 
             for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                 if (!(e instanceof LivingEntity)) {
                     continue;
                 }
                 LivingEntity le = (LivingEntity) e;
 
                 if (!damageCheck(player, le)) {
                     continue;
                 }
 
                 if (e instanceof Player) {
                     Player p = (Player) e;
                     Hero tHero = plugin.getHeroManager().getHero(p);
                     if (stunDuration > 0)
                         tHero.addEffect(new StunEffect(skill, stunDuration));
                     if (slowDuration > 0)
                         tHero.addEffect(new SlowEffect(skill, slowDuration, 2, true, p.getDisplayName() + " has been slowed by " + player.getDisplayName(),
                                 p.getDisplayName() + " is no longer slowed by " + player.getDisplayName(), hero));
                     if (rootDuration > 0)
                         tHero.addEffect(new RootEffect(skill, rootDuration));
                     if (silenceDuration > 0)
                         tHero.addEffect(new SilenceEffect(skill, silenceDuration));
                    if (damage > 0)
                         Skill.damageEntity(le, player, damage, DamageCause.ENTITY_ATTACK);
                 } else if (e instanceof LivingEntity) {
                     if (slowDuration > 0)
                         plugin.getEffectManager().addEntityEffect(le, new SlowEffect(skill, slowDuration, 2, true, Messaging.getLivingEntityName(le) + " has been slowed by " + player.getDisplayName(),
                                 Messaging.getLivingEntityName(le) + " is no longer slowed by " + player.getDisplayName(), hero));
                     if (rootDuration > 0)
                         plugin.getEffectManager().addEntityEffect(le, new RootEffect(skill, rootDuration));
                 }
 
                 if (damage > 0) {
                     Skill.damageEntity(le, player, damage, DamageCause.ENTITY_ATTACK);
                 }
             }
         }
     }
 
     @Override
     public String getDescription(Hero hero) {
         return getDescription();
     }
 }
