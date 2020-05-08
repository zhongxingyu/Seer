 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 
 import net.minecraft.server.MathHelper;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.common.SlowEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillIcebolt extends ActiveSkill {
 
     private HashSet<Snowball> snowballs = new HashSet<Snowball>();
 
     private String applyText;
     private String expireText;
     
     public SkillIcebolt(Heroes plugin) {
         super(plugin, "Icebolt");
         setDescription("Fires a snowball that hurts the player and if they're on fire, puts them out");
         setUsage("/skill icebolt");
         setArgumentRange(0, 0);
         setIdentifiers("skill icebolt");
         setTypes(SkillType.ICE, SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.HARMFUL);
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(this), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DAMAGE.node(), 3);
         node.setProperty("slow-duration", 5000); // 5 seconds
         node.setProperty("speed-multiplier", 2);
         node.setProperty(Setting.APPLY_TEXT.node(), "%target% has been slowed by %hero%!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "%target% is no longer slowed!");
         return node;
         
     }
     @Override
     public void init() {
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% has been slowed by %hero%!").replace("%target%", "$1").replace("%hero%", "$2");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target% is no longer slowed!").replace("%target%", "$1");
     }
     
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         Location location = player.getEyeLocation();
 
         float pitch = location.getPitch() / 180.0F * 3.1415927F;
         float yaw = location.getYaw() / 180.0F * 3.1415927F;
 
         double motX = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
         double motZ = MathHelper.cos(yaw) * MathHelper.cos(pitch);
         double motY = -MathHelper.sin(pitch);
         Vector velocity = new Vector(motX, motY, motZ);
 
         Snowball snowball = player.throwSnowball();
         snowball.setVelocity(velocity);
         snowballs.add(snowball);
 
         broadcastExecuteText(hero);
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
         
         private final Skill skill;
         
         public SkillEntityListener(Skill skill) {
             this.skill = skill;
         }
         
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             Heroes.debug.startTask("HeroesSkillListener");
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             Entity projectile = subEvent.getDamager();
             if (!(projectile instanceof Snowball) || !snowballs.contains(projectile)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             snowballs.remove(projectile);
 
             Entity dmger = ((Snowball) subEvent.getDamager()).getShooter();
             if (dmger instanceof Player) {
                 Hero hero = plugin.getHeroManager().getHero((Player) dmger);
 
                 event.getEntity().setFireTicks(0);
                 int damage = getSetting(hero, Setting.DAMAGE.node(), 3, false);
                 
                 long duration = getSetting(hero, "slow-duration", 10000, false);
                 int amplifier = getSetting(hero, "speed-multiplier", 2, false);
                 
                 SlowEffect iceSlowEffect = new SlowEffect(skill, duration, amplifier, false, applyText, expireText, hero);
                 LivingEntity target = (LivingEntity) event.getEntity();
                 if (target instanceof Player) {
                     Hero tHero = plugin.getHeroManager().getHero((Player) target);
                     tHero.addEffect(iceSlowEffect);
                 } else if (target instanceof Creature) {
                     plugin.getEffectManager().addCreatureEffect((Creature) target, iceSlowEffect);
                 }
                 
                addSpellTarget(event.getEntity(), hero);
                 event.setDamage(damage);
             }
 
 
             Heroes.debug.stopTask("HeroesSkillListener");
         }
     }
 }
