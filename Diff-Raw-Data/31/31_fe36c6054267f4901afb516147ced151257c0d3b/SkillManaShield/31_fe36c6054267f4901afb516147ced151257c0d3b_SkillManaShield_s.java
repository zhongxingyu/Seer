 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
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
 
 public class SkillManaShield extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillManaShield(Heroes plugin) {
         super(plugin, "ManaShield");
         setDescription("Uses your mana as a shield");
         setUsage("/skill manashield");
         setArgumentRange(0, 0);
         setIdentifiers("skill manashield", "skill mshield");
         setTypes(SkillType.BUFF, SkillType.SILENCABLE, SkillType.MANA);
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("mana-amount", 20);
         node.setProperty(Setting.DURATION.node(), 20000);
         node.setProperty(Setting.APPLY_TEXT.node(), "%hero% was surrounded by a mana shield!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "%hero% lost his mana shield!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%hero% was surrounded by a mana shield!").replace("%hero%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%hero% lost his mana shield!").replace("%hero%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
 
         int duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 5000);
         hero.addEffect(new ManaShieldEffect(this, duration));
 
         return true;
     }
 
     public class ManaShieldEffect extends ExpirableEffect {
 
         public ManaShieldEffect(Skill skill, long duration) {
             super(skill, "ManaShield", duration);
             this.types.add(EffectType.DISPELLABLE);
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
             if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Player player = (Player) event.getEntity();
             Hero hero = plugin.getHeroManager().getHero(player);
             if (hero.hasEffect(getName())) {
                 int absorbamount = getSetting(hero.getHeroClass(), "mana-amount", 20);
                 event.setDamage(event.getDamage() / 2);
                 int mana = hero.getMana();
                 if (mana < absorbamount) {
                     hero.removeEffect(hero.getEffect("ManaShield"));
                 } else {
                     mana -= absorbamount;
                     hero.setMana(mana);
                     if (mana != 100 && hero.isVerbose()) {
                         Messaging.send(player, ChatColor.BLUE + "MANA " + Messaging.createManaBar(hero.getMana()));
                     }
                 }
             }
             Heroes.debug.stopTask("HeroesSkillListener");
         }
     }
 }
