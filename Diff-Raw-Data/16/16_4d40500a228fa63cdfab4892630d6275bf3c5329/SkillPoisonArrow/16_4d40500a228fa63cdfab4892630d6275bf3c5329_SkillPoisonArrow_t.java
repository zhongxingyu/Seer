 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.PeriodicDamageEffect;
 import com.herocraftonline.dev.heroes.effects.common.ImbueEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillPoisonArrow extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillPoisonArrow(Heroes plugin) {
         super(plugin, "PoisonArrow");
         setDescription("Your arrows will poison their target dealing $1 damage over $2 seconds, each arrow will drain $3 mana.");
         setUsage("/skill parrow");
         setArgumentRange(0, 0);
         setIdentifiers("skill parrow", "skill poisonarrow");
         setTypes(SkillType.BUFF);
         Bukkit.getServer().getPluginManager().registerEvents(new SkillDamageListener(this), plugin);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.DURATION.node(), 10000); // milliseconds
         node.set(Setting.PERIOD.node(), 2000); // 2 seconds in milliseconds
         node.set("mana-per-shot", 1); // How much mana for each attack
         node.set("tick-damage", 2);
         node.set(Setting.USE_TEXT.node(), "%hero% imbues their arrows with poison!");
         node.set(Setting.APPLY_TEXT.node(), "%target% is poisoned!");
         node.set(Setting.EXPIRE_TEXT.node(), "%target% has recovered from the poison!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         setUseText("%hero% imbues their arrows with poison!".replace("%hero%", "$1"));
         applyText = SkillConfigManager.getRaw(this, Setting.APPLY_TEXT, "%target% is poisoned!").replace("%target%", "$1");
         expireText = SkillConfigManager.getRaw(this, Setting.EXPIRE_TEXT, "%target% has recovered from the poison!").replace("%target%", "$1");
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         if (hero.hasEffect("PoisonArrowBuff")) {
             hero.removeEffect(hero.getEffect("PoisonArrowBuff"));
             return SkillResult.SKIP_POST_USAGE;
         }
         hero.addEffect(new PoisonArrowBuff(this));
         broadcastExecuteText(hero);
         return SkillResult.NORMAL;
     }
 
     public class ArrowPoison extends PeriodicDamageEffect {
         
         public ArrowPoison(Skill skill, long period, long duration, int tickDamage, Player applier) {
             super(skill, "ArrowPoison", period, duration, tickDamage, applier);
             this.types.add(EffectType.POISON);
             addMobEffect(19, (int) (duration / 1000) * 20, 0, true);
         }
 
         @Override
         public void apply(LivingEntity lEntity) {
             super.apply(lEntity);
             broadcast(lEntity.getLocation(), applyText, Messaging.getLivingEntityName(lEntity).toLowerCase());
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(LivingEntity lEntity) {
             super.remove(lEntity);
             broadcast(lEntity.getLocation(), expireText, Messaging.getLivingEntityName(lEntity).toLowerCase());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
     }
 
     public class PoisonArrowBuff extends ImbueEffect {
 
         public PoisonArrowBuff(Skill skill) {
             super(skill, "PoisonArrowBuff");
             this.types.add(EffectType.POISON);
             setDescription("poison");
         }
     }
 
     public class SkillDamageListener implements Listener {
 
         private final Skill skill;
 
         public SkillDamageListener(Skill skill) {
             this.skill = skill;
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity) || !(event instanceof EntityDamageByEntityEvent)) {
                 return;
             }
 
             LivingEntity target = (LivingEntity) event.getEntity();
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             
             if (!(subEvent.getDamager() instanceof Arrow)) {
                 return;
             }
 
             Arrow arrow = (Arrow) subEvent.getDamager();
             if (!(arrow.getShooter() instanceof Player)) {
                 return;
             }
 
             Player player = (Player) arrow.getShooter();
             Hero hero = plugin.getHeroManager().getHero(player);
 
             if (hero.hasEffect("PoisonArrowBuff")) {
                 long duration = SkillConfigManager.getUseSetting(hero, skill, "poison-duration", 10000, false);
                 long period = SkillConfigManager.getUseSetting(hero, skill, Setting.PERIOD, 2000, true);
                 int tickDamage = SkillConfigManager.getUseSetting(hero, skill, "tick-damage", 2, false);
                 ArrowPoison apEffect = new ArrowPoison(skill, period, duration, tickDamage, player);
                 
                 if (target instanceof Player) {
                     Hero hTarget = plugin.getHeroManager().getHero((Player) target);
                     hTarget.addEffect(apEffect);
                 } else {
                     plugin.getEffectManager().addEntityEffect(target, apEffect);
                 }
             }
         }
         
         @EventHandler(priority = EventPriority.MONITOR)
         public void onEntityShootBow(EntityShootBowEvent event) {
             if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow)) {
                 return;
             }
             Hero hero = plugin.getHeroManager().getHero((Player) event.getEntity());
             if (hero.hasEffect("PoisonArrowBuff")) {
                 int mana = SkillConfigManager.getUseSetting(hero, skill, "mana-per-shot", 1, true);
                 if (hero.getMana() < mana) {
                     hero.removeEffect(hero.getEffect("PoisonArrowBuff"));
                 } else {
                    hero.setMana(hero.getMana() - mana);
                 }
             }
         }
     }
 
     @Override
     public String getDescription(Hero hero) {
         int duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 10000, false);
         int period = SkillConfigManager.getUseSetting(hero, this, Setting.PERIOD, 2000, false);
         int damage = SkillConfigManager.getUseSetting(hero, this, "tick-damage", 1, false);
         int mana = SkillConfigManager.getUseSetting(hero, this, "mana-per-shot", 1, true);
         damage = damage * duration / period;
         return getDescription().replace("$1", damage + "").replace("$2", duration / 1000 + "").replace("$3", mana + "");
     }
 }
