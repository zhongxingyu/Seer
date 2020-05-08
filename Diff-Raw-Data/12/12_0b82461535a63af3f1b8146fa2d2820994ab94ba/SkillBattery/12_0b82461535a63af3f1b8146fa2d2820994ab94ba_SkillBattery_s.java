 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillBattery extends TargettedSkill{
 
     public SkillBattery(Heroes plugin) {
         super(plugin);
         name = "Battery";
         description = "Gives your target mana";
         usage = "/skill battery";
         minArgs = 0;
         maxArgs = 1;
         identifiers.add("skill battery");
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("transfer-amount", 20);
         return node;
     }
     
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Hero tHero = plugin.getHeroManager().getHero((Player) target);
         int transferamount = getSetting(hero.getHeroClass(), "transfer-amount", 20);
         if(hero.getMana() > transferamount){
             hero.setMana(hero.getMana() - transferamount);
             tHero.setMana(tHero.getMana() + transferamount);
         }
        return true;
     }
 
 }
