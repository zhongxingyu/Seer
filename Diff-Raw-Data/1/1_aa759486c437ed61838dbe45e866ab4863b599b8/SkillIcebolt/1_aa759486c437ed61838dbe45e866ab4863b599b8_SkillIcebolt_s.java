 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 
 import net.minecraft.server.MathHelper;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.util.Vector;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.common.SlowEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillIcebolt extends ActiveSkill {
 
     private HashSet<Snowball> snowballs = new HashSet<Snowball>();
 
     private String applyText;
     private String expireText;
     
     public SkillIcebolt(Heroes plugin) {
         super(plugin, "Icebolt");
         setDescription("You launch a ball of ice that deals $1 damage to your target and slows them for $2 seconds.");
         setUsage("/skill icebolt");
         setArgumentRange(0, 0);
         setIdentifiers("skill icebolt");
         setTypes(SkillType.ICE, SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.HARMFUL);
         Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.DAMAGE.node(), 3);
         node.set("slow-duration", 5000); // 5 seconds
         node.set("speed-multiplier", 2);
         node.set(Setting.APPLY_TEXT.node(), "%target% has been slowed by %hero%!");
         node.set(Setting.EXPIRE_TEXT.node(), "%target% is no longer slowed!");
         return node;
         
     }
     @Override
     public void init() {
         applyText = SkillConfigManager.getRaw(this, Setting.APPLY_TEXT, "%target% has been slowed by %hero%!").replace("%target%", "$1").replace("%hero%", "$2");
         expireText = SkillConfigManager.getRaw(this, Setting.EXPIRE_TEXT, "%target% is no longer slowed!").replace("%target%", "$1");
     }
     
     @Override
     public SkillResult use(Hero hero, String[] args) {
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
         return SkillResult.NORMAL;
     }
 
     public class SkillEntityListener implements Listener {
         
         private final Skill skill;
         
         public SkillEntityListener(Skill skill) {
             this.skill = skill;
         }
         
         @EventHandler()
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                 return;
             }
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             Entity projectile = subEvent.getDamager();
             if (!(projectile instanceof Snowball) || !snowballs.contains(projectile)) {
                 return;
             }
 
             snowballs.remove(projectile);
 
             Entity dmger = ((Snowball) subEvent.getDamager()).getShooter();
             if (dmger instanceof Player) {
                 Hero hero = plugin.getHeroManager().getHero((Player) dmger);
 
                 event.getEntity().setFireTicks(0);
                 int damage = SkillConfigManager.getUseSetting(hero, skill, Setting.DAMAGE, 3, false);
                 
                 long duration = SkillConfigManager.getUseSetting(hero, skill, "slow-duration", 10000, false);
                 int amplifier = SkillConfigManager.getUseSetting(hero, skill, "speed-multiplier", 2, false);
                 
                 SlowEffect iceSlowEffect = new SlowEffect(skill, duration, amplifier, false, applyText, expireText, hero);
                 LivingEntity target = (LivingEntity) event.getEntity();
                 if (target instanceof Player) {
                     Hero tHero = plugin.getHeroManager().getHero((Player) target);
                     tHero.addEffect(iceSlowEffect);
                 } else
                     plugin.getEffectManager().addEntityEffect(target, iceSlowEffect);
                 
                 addSpellTarget(event.getEntity(), hero);
                 event.setDamage(damage);
             }
         }
     }
 
     @Override
     public String getDescription(Hero hero) {
         int duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 5000, false);
         int damage = SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE, 4, false);
         return getDescription().replace("$1", damage + "").replace("$2", duration / 1000 + "");
     }
 }
