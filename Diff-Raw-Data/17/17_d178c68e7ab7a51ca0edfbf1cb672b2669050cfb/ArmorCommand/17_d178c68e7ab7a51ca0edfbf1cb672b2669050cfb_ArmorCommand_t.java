 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.Set;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.MaterialUtil;
 
 public class ArmorCommand extends BaseCommand {
 
     public ArmorCommand(Heroes plugin) {
         super(plugin);
         name = "Armor";
         description = "Displays armor available for your class";
         usage = "/hero armor";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("hero armor");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             Hero hero = plugin.getHeroManager().getHero(player);
             HeroClass heroClass = hero.getHeroClass();
 
             Set<String> allArmors = heroClass.getAllowedArmor();
             String[] categories = { "Helmet", "Chestplate", "Leggings", "Boots" };
             String[] categorizedArmors = new String[categories.length];
 
            for (int i = 0; i < categories.length; i++) {
                categorizedArmors[i] = "";
            }
            
             for (String armor : allArmors) {
                 for (int i = 0; i < categories.length; i++) {
                     if (armor.endsWith(categories[i].toUpperCase())) {
                         categorizedArmors[i] += MaterialUtil.getFriendlyName(armor).split(" ")[0] + ", ";
                         break;
                     }
                 }
             }
 
             for (int i = 0; i < categories.length; i++) {
                 if (!categorizedArmors[i].isEmpty()) {
                     categorizedArmors[i] = categorizedArmors[i].substring(0, categorizedArmors[i].length() - 2);
                 }
             }
 
             sender.sendMessage("§c--------[ §fAllowed Armor§c ]--------");
             for (int i = 0; i < categories.length; i++) {
                 player.sendMessage("  §a" + categories[i] + ": §f" + categorizedArmors[i]);
             }
         }
     }
 
 }
