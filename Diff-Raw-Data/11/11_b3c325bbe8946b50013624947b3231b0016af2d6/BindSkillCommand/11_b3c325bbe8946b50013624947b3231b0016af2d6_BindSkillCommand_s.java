 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.MaterialUtil;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class BindSkillCommand extends BasicCommand {
 
     private final Heroes plugin;
 
     public BindSkillCommand(Heroes plugin) {
         super("BindSkill");
         this.plugin = plugin;
         setDescription("Binds a skill with an item");
         setUsage("/bind §9<skill> §8[args]");
         setArgumentRange(0, 1000);
         setIdentifiers(new String[] { "bind" });
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         if (!(sender instanceof Player))
             return false;
 
         Player player = (Player) sender;
         Hero hero = plugin.getHeroManager().getHero(player);
         HeroClass heroClass = hero.getHeroClass();
         Material material = player.getItemInHand().getType();
         if (args.length > 0) {
            if (heroClass.hasSkill(args[0]) || hero.hasSkill(args[0])) {
                 if (material == Material.AIR) {
                     Messaging.send(sender, "You must be holding an item to bind a skill!");
                     return false;
                 }
                 hero.bind(material, args);
                 Messaging.send(sender, "$1 has been bound to $2.", MaterialUtil.getFriendlyName(material), args[0]);
             } else {
                 Messaging.send(sender, "That skill does not exist for your class.");
             }
         } else {
             hero.unbind(material);
             Messaging.send(sender, "$1 is no longer bound to a skill.", MaterialUtil.getFriendlyName(material));
         }
         return true;
     }
 }
