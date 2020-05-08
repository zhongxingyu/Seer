 package com.herocraftonline.dev.heroes.command.commands;
 
import java.util.EnumSet;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.MaterialUtil;
 
 public class ArmorCommand extends BasicCommand {
 
     private final Heroes plugin;
 
     public ArmorCommand(Heroes plugin) {
         super("Armor");
         this.plugin = plugin;
         setDescription("Displays armor available for your class");
         setUsage("/hero armor");
         setArgumentRange(0, 0);
         setIdentifiers("hero armor");
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         if (!(sender instanceof Player))
             return false;
 
         Player player = (Player) sender;
         Hero hero = plugin.getHeroManager().getHero(player);
         HeroClass heroClass = hero.getHeroClass();
 
        Set<Material> allArmors = EnumSet.noneOf(Material.class);
        allArmors.addAll(heroClass.getAllowedArmor());
        if (hero.getSecondClass() != null)
            allArmors.addAll(hero.getSecondClass().getAllowedArmor());
        
         String[] categories = { "Helmet", "Chestplate", "Leggings", "Boots" };
         String[] categorizedArmors = new String[categories.length];
 
         for (int i = 0; i < categories.length; i++) {
             categorizedArmors[i] = "";
         }
 
         for (Material mat : allArmors) {
             String armor = mat.name();
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
 
         return true;
     }
 
 }
