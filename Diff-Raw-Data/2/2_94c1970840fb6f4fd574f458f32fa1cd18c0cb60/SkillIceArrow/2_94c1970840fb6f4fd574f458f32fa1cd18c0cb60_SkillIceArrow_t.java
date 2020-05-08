 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.common.ImbueEffect;
 import com.herocraftonline.dev.heroes.effects.common.SlowEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillIceArrow extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillIceArrow(Heroes plugin) {
         super(plugin, "IceArrow");
         setDescription("You fire a icy arrow from your bow");
         setUsage("/skill iarrow");
         setArgumentRange(0, 0);
         setIdentifiers("skill iarrow", "skill icearrow");
         setTypes(SkillType.BUFF, SkillType.ICE, SkillType.SILENCABLE);
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillDamageListener(this), Priority.Monitor);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("slow-duration", 5000); // 5 seconds
         node.setProperty("speed-multiplier", 2);
         node.setProperty(Setting.DURATION.node(), 60000); // milliseconds
         node.setProperty("attacks", 1); // How many attacks the buff lasts for.
        node.setProperty(Setting.APPLY_TEXT.node(), "%target% imbues their arrows with ice!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "%target%'s arrows are no longer imbued with ice!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% imbue's their arrows with ice!").replace("%target%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target%'s arrows are no longer imbued with ice!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         long duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 60000);
         int numAttacks = getSetting(hero.getHeroClass(), "attacks", 1);
         hero.addEffect(new IceArrowBuff(this, duration, numAttacks));
         return true;
     }
 
     public class IceArrowBuff extends ImbueEffect {
 
         public IceArrowBuff(Skill skill, long duration, int numAttacks) {
             super(skill, "SlowArrowBuff", duration, numAttacks);
             this.types.add(EffectType.ICE);
             setDescription("ice");
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
     }
 
     public class SkillDamageListener extends EntityListener {
 
         private final Skill skill;
 
         public SkillDamageListener(Skill skill) {
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
             if (!(subEvent.getDamager() instanceof Arrow)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Arrow arrow = (Arrow) subEvent.getDamager();
             if (!(arrow.getShooter() instanceof Player)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Player player = (Player) arrow.getShooter();
             Hero hero = plugin.getHeroManager().getHero(player);
 
             if (hero.hasEffect("SlowArrowBuff")) {
                 long duration = getSetting(hero.getHeroClass(), "slow-duration", 10000);
                 int amplifier = getSetting(hero.getHeroClass(), "speed-multiplier", 2);
                 SlowEffect iceSlowEffect = new SlowEffect(skill, duration, amplifier, false);
                 LivingEntity target = (LivingEntity) event.getEntity();
                 if (target instanceof Player) {
                     Hero tHero = plugin.getHeroManager().getHero((Player) target);
                     tHero.addEffect(iceSlowEffect);
                 } else if (target instanceof Creature) {
                     plugin.getEffectManager().addCreatureEffect((Creature) target, iceSlowEffect);
                 }
                 checkBuff(hero);
             }
             Heroes.debug.stopTask("HeroesSkillListener");
         }
 
         private void checkBuff(Hero hero) {
             IceArrowBuff iaBuff = (IceArrowBuff) hero.getEffect("SlowArrowBuff");
             if (iaBuff.hasNoApplications()) {
                 hero.removeEffect(iaBuff);
             }
         }
     }
 }
