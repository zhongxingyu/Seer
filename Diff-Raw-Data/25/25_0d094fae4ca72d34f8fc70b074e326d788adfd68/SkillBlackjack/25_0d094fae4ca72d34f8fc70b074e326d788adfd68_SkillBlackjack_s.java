 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.skill.Skill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class SkillBlackjack extends Skill {
 
     // TODO: Register this command in Heroes
     public SkillBlackjack(Heroes plugin) {
         super(plugin);
        name = getName();
         description = "Skill - blackjack";
         usage = "/blackjack";
         minArgs = 0;
         maxArgs = 0;
        identifiers.add(getName());
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) sender);
             HeroClass heroClass = plugin.getClassManager().getClass(hero.toString());
 
             if (!(heroClass.getSkills().contains(getName()))) {
                 plugin.getMessager().send(sender, "Sorry, that ability isn't for your class!");
                 return;
             }
 
             Properties properties = plugin.getConfigManager().getProperties();
             Map<String, Long> cooldowns = hero.getCooldowns();
             if (cooldowns.containsKey(getName())) {
                 if (cooldowns.get(getName()) - System.currentTimeMillis() >= Long.parseLong(properties.skillInfo.get(getName() + "cooldown"))) {
                     cooldowns.put(getName(), System.currentTimeMillis());
                 } else {
                     plugin.getMessager().send(sender, "Sorry, that skill is still on cooldown!");
                     return;
                 }
             }
 
             if (!(hero.getEffects().containsKey(getName()))) {
                 hero.getEffects().put(getName(), System.currentTimeMillis());
             } else {
                 if (hero.getEffects().get(getName()) > 60000) {
                     hero.getEffects().put(getName(), System.currentTimeMillis());
                 }
             }
         }
     }
 
     @Override
     public void use(Player player, String[] args) {
         Hero hero = plugin.getHeroManager().getHero(player);
         HeroClass heroClass = plugin.getClassManager().getClass(hero.toString());
 
         // TODO: Check for CD time left, if 0 execute.
         if (!(heroClass.getSkills().contains(getName()))) {
             plugin.getMessager().send(player, "Sorry, that ability isn't for your class!");
             return;
         }
 
         Properties properties = plugin.getConfigManager().getProperties();
         Map<String, Long> cooldowns = hero.getCooldowns();
         if (cooldowns.containsKey(getName())) {
             if (cooldowns.get(getName()) - System.currentTimeMillis() >= Long.parseLong(properties.skillInfo.get(getName() + "cooldown"))) {
                 cooldowns.put(getName(), System.currentTimeMillis());
             } else {
                 plugin.getMessager().send(player, "Sorry, that skill is still on cooldown!");
                 return;
             }
         }
 
         if (!(hero.getEffects().containsKey(getName()))) {
             hero.getEffects().put(getName(), System.currentTimeMillis());
         } else {
             if (hero.getEffects().get(getName()) > 60000) {
                 hero.getEffects().put(getName(), System.currentTimeMillis());
             }
         }
     }
 }
