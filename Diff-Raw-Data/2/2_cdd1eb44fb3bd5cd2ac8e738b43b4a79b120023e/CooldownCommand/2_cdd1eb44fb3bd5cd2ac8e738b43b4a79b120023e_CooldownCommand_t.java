 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class CooldownCommand extends BasicCommand {
 
     private Heroes plugin;
     
     public CooldownCommand(Heroes plugin) {
         super("Cooldowns");
         this.plugin = plugin;
         setDescription("Displays your cooldowns");
         setUsage("/cd");
         setArgumentRange(0, 0);
         setIdentifiers(new String[] { "cooldowns", "cd" });
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         if (!(sender instanceof Player))
             return false;
         
         Player player = (Player) sender;
         Hero hero = plugin.getHeroManager().getHero(player);
         if (hero.getCooldowns().isEmpty()) {
             Messaging.send(hero.getPlayer(), "You have no skills on cooldown!");
             return true;
         }
         long time = System.currentTimeMillis();
         Map<String, Skill> skillMap = plugin.getSkillMap();
         for (Entry<String, Long> entry : hero.getCooldowns().entrySet()) {
             Skill skill = skillMap.get(entry.getKey());
            long timeLeft = entry.getValue() - time;
             if (timeLeft <= 0)
                 continue;
             
             Messaging.send(hero.getPlayer(), "$1 has $2 seconds left on cooldown!", skill.getName(), timeLeft / 1000);
         }
         
         return true;
     }
 
 }
