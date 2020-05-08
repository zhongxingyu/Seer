 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillBladegrasp extends ActiveSkill {
 
     private String applyText;
     private String expireText;
     private String parryText;
 
     public SkillBladegrasp(Heroes plugin) {
         super(plugin, "Bladegrasp");
         setDescription("Blocks incoming melee damage");
         setUsage("/skill bladegrasp");
         setArgumentRange(0, 0);
         setIdentifiers("skill bladegrasp", "skill bgrasp");
         setTypes(SkillType.PHYSICAL, SkillType.BUFF);
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DURATION.node(), 5000);
         node.setProperty(Setting.APPLY_TEXT.node(), "%hero% tightened his grip!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "%hero% loosened his grip!");
         node.setProperty("parry-text", "%hero% parried an attack!");
         node.setProperty(Setting.CHANCE_LEVEL.node(), .02);
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%hero% tightened his grip!").replace("%hero%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%hero% loosened his grip!").replace("%hero%", "$1");
         parryText = getSetting(null, "parry-text", "%hero% parried an attack!").replace("%hero%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
         int duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 5000);
         hero.addEffect(new BladegraspEffect(this, duration));
 
         return true;
     }
 
     public class BladegraspEffect extends ExpirableEffect {
 
         public BladegraspEffect(Skill skill, long duration) {
             super(skill, "Bladegrasp", duration);
             this.types.add(EffectType.PHYSICAL);
             this.types.add(EffectType.BENEFICIAL);
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
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             Heroes.debug.startTask("HeroesSkillListener");
             // Ignore cancelled damage events & 0 damage events for Spam Control
             if (event.getDamage() == 0 || event.isCancelled() || !(event instanceof EntityDamageByEntityEvent)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Entity defender = event.getEntity();
             if (defender instanceof Player) {
                 Player player = (Player) defender;
                 Hero hero = plugin.getHeroManager().getHero(player);
                 if (hero.hasEffect(getName())) {
                     double parryChance = getSetting(hero.getHeroClass(), "chance-per-level", .02) * hero.getLevel();
                     if (Util.rand.nextDouble() > parryChance)
                         return;
 
                     event.setCancelled(true);
                     Messaging.send(player, parryText.replace("$1", player.getDisplayName()));
                     EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                     if (subEvent.getDamager() instanceof Player) {
                         Messaging.send((Player) subEvent.getDamager(), parryText.replace("$1", player.getDisplayName()));
                     }
                 }
             }
             Heroes.debug.stopTask("HeroesSkillListener");
         }
     }
 }
