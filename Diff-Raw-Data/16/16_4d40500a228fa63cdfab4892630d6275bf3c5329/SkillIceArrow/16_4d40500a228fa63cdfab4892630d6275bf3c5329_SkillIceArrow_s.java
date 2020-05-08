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
 import com.herocraftonline.dev.heroes.effects.common.ImbueEffect;
 import com.herocraftonline.dev.heroes.effects.common.SlowEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillIceArrow extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillIceArrow(Heroes plugin) {
         super(plugin, "IceArrow");
         setDescription("Your arrows will freeze their target, but drain $1 mana per shot.");
         setUsage("/skill iarrow");
         setArgumentRange(0, 0);
         setIdentifiers("skill iarrow", "skill icearrow");
         setTypes(SkillType.BUFF, SkillType.ICE, SkillType.SILENCABLE);
         Bukkit.getServer().getPluginManager().registerEvents(new SkillDamageListener(this), plugin);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.DURATION.node(), 5000); // 5 seconds
         node.set("speed-multiplier", 2);
         node.set("mana-per-shot", 1); // How much mana for each attack
         node.set(Setting.USE_TEXT.node(), "%hero% imbues their arrows with ice!");
         node.set(Setting.APPLY_TEXT.node(), "%target% is slowed by ice!");
         node.set(Setting.EXPIRE_TEXT.node(), "%hero%'s arrows are no longer imbued with ice!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         setUseText("%hero% imbues their arrows with ice!".replace("%hero%", "$1"));
         applyText = SkillConfigManager.getRaw(this, Setting.APPLY_TEXT.node(), "%target% is slowed by %hero%s !").replace("%target%", "$1").replace("%hero%", "$2");
         expireText = SkillConfigManager.getRaw(this, Setting.EXPIRE_TEXT.node(), "%hero%'s arrows are no longer imbued with ice!").replace("%hero%", "$1");
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         if (hero.hasEffect("IceArrowBuff")) {
             hero.removeEffect(hero.getEffect("IceArrowBuff"));
             return SkillResult.SKIP_POST_USAGE;
         }
         hero.addEffect(new IceArrowBuff(this));
         broadcastExecuteText(hero);
         return SkillResult.NORMAL;
     }
 
     public class IceArrowBuff extends ImbueEffect {
 
         public IceArrowBuff(Skill skill) {
             super(skill, "IceArrowBuff");
             this.types.add(EffectType.ICE);
             setDescription("ice arrow");
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
     }
 
     public class SkillDamageListener implements Listener {
 
         private final Skill skill;
 
         public SkillDamageListener(Skill skill) {
             this.skill = skill;
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                 return;
             }
 
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
 
             if (hero.hasEffect("IceArrowBuff")) {
                 long duration = SkillConfigManager.getUseSetting(hero, skill, "duration", 5000, false);
                 int amplifier = SkillConfigManager.getUseSetting(hero, skill, "speed-multiplier", 2, false);
                 SlowEffect iceSlowEffect = new SlowEffect(skill, duration, amplifier, false, applyText, "$1 is no longer slowed.", hero);
                 LivingEntity target = (LivingEntity) event.getEntity();
                 if (target instanceof Player) {
                     Hero tHero = plugin.getHeroManager().getHero((Player) target);
                     tHero.addEffect(iceSlowEffect);
                     broadcast(target.getLocation(), applyText, tHero.getPlayer().getDisplayName(), player.getDisplayName());
                 } else {
                     plugin.getEffectManager().addEntityEffect(target, iceSlowEffect);
                     broadcast(target.getLocation(), applyText, Messaging.getLivingEntityName(target), player.getDisplayName());
                 }
             }
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onEntityShootBow(EntityShootBowEvent event) {
             if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow)) {
                 return;
             }
             Hero hero = plugin.getHeroManager().getHero((Player) event.getEntity());
             if (hero.hasEffect("IceArrowBuff")) {
                 int mana = SkillConfigManager.getUseSetting(hero, skill, "mana-per-shot", 1, true);
                 if (hero.getMana() < mana) {
                     hero.removeEffect(hero.getEffect("IceArrowBuff"));
                 } else {
                    hero.setMana(hero.getMana() - 1);
                 }
             }
         }
     }
 
     @Override
     public String getDescription(Hero hero) {
         int mana = SkillConfigManager.getUseSetting(hero, this, "mana-per-shot", 1, false);
         return getDescription().replace("$3", mana + "");
     }
 }
