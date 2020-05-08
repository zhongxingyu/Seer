 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.Arrays;
 
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 
 public class AssignSkillCommand extends BaseCommand {
 
     public AssignSkillCommand(Heroes plugin) {
         super(plugin);
         name = "AssignSkill";
         description = "Assigns a skill to an item";
         usage = "/assign <spell>";
         minArgs = 1;
         maxArgs = 1000;
         identifiers.add("assign");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
            if (args.length>0) {
                if (plugin.getHeroManager().getHero((Player) sender).getPlayerClass().getSkills().contains(args[0])) {
                    plugin.getHeroManager().getHero((Player) sender).bind(Material.getMaterial(args[0]), Arrays.copyOf(args, 1));
                     plugin.getMessager().send(sender, "That has been assigned as your skill", (String[]) null);
                 } else {
                     plugin.getMessager().send(sender, "That skill does not exist for your class", (String[]) null);
                 }
             } else {
                plugin.getHeroManager().getHero((Player) sender).unbind(Material.getMaterial(args[0]));
                 plugin.getMessager().send(sender, "Your equipped item is no longer bound to a skill.", (String[]) null);
             }
         }
     }
 }
