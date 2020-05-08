 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroesEventListener;
 import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillBackstab extends PassiveSkill {
 
     private String useText;
     
     public SkillBackstab(Heroes plugin) {
         super(plugin, "Backstab");
         setDescription("You have a $1% chance to deal $2% damage when attacking from behind!");
         setArgumentRange(0, 0);
         setTypes(SkillType.PHYSICAL, SkillType.BUFF);
         setEffectTypes(EffectType.BENEFICIAL, EffectType.PHYSICAL);
         
         registerEvent(Type.CUSTOM_EVENT, new SkillHeroesListener(this), Priority.Normal);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set("weapons", Util.swords);
         node.set("attack-bonus", 1.5);
         node.set("attack-chance", .5);
         node.set("sneak-bonus", 2.0); // Alternative bonus if player is sneaking when doing the backstab
         node.set("sneak-chance", 1.0);
         node.set(Setting.USE_TEXT.node(), "%hero% backstabbed %target%!");
         return node;
     }
     
     @Override
     public void init() {
         super.init();
         useText = SkillConfigManager.getRaw(this, Setting.USE_TEXT, "%hero% backstabbed %target%!").replace("%hero%", "$1").replace("%target%", "$2");
     }
 
     public class SkillHeroesListener extends HeroesEventListener {
 
         private final Skill skill;
         
         public SkillHeroesListener(Skill skill) {
             this.skill = skill;
         }
         
         @Override
         public void onWeaponDamage(WeaponDamageEvent event) {
             Heroes.debug.startTask("HeroesSkillListener");
             if (!(event.getDamager() instanceof Player)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
             
             Player player = (Player) event.getDamager();
             Hero hero = plugin.getHeroManager().getHero(player);
             
             if (hero.hasEffect(getName())) {
                 ItemStack item = player.getItemInHand();
                 
                 if (!SkillConfigManager.getUseSetting(hero, skill, "weapons", Util.swords).contains(item.getType().name())) {
                     Heroes.debug.stopTask("HeroesSkillListener");
                     return;
                 }
                 
                 if (event.getEntity().getLocation().getDirection().dot(player.getLocation().getDirection()) <= 0) {
                     Heroes.debug.stopTask("HeroesSkillListener");
                     return;
                 }
 
                 if (hero.hasEffect("Sneak") && Util.rand.nextDouble() < SkillConfigManager.getUseSetting(hero, skill, "sneak-chance", 1.0, false)) {
                     event.setDamage((int) (event.getDamage() * SkillConfigManager.getUseSetting(hero, skill, "sneak-bonus", 2.0, false)));
                 } else if (Util.rand.nextDouble() < SkillConfigManager.getUseSetting(hero, skill, "attack-chance", .5, false)) {
                     event.setDamage((int) (event.getDamage() * SkillConfigManager.getUseSetting(hero, skill, "attack-bonus", 1.5, false)));
                 }
 
                 Entity target = event.getEntity();
                 broadcastExecuteText(hero, target);
             }
             Heroes.debug.stopTask("HeroesSkillListener");
         }
     }
     
     private void broadcastExecuteText(Hero hero, Entity target) {
         Player player = hero.getPlayer();
         String targetName = target instanceof Player ? ((Player) target).getName() : target.getClass().getSimpleName().substring(5);
         broadcast(player.getLocation(), useText, player.getDisplayName(), target == player ? "himself" : targetName);
     }
 
     @Override
     public String getDescription(Hero hero) {
         double chance = SkillConfigManager.getUseSetting(hero, this, "attack-chance", .5, false);
         double percent = SkillConfigManager.getUseSetting(hero, this, "attack-bonus", 1.5, false);
        return getDescription().replace("$1", chance * 100 + "").replace("$2", percent * 100 + "");
     }
 }
