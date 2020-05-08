 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class LevelInformationCommand extends BaseCommand {
 
     public LevelInformationCommand(Heroes plugin) {
         super(plugin);
         name = "LevelInformation";
         description = "Player Level information";
         usage = "/lvl OR /level OR /hero level";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("hero level");
         identifiers.add("level");
         identifiers.add("lvl");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             Hero hero = plugin.getHeroManager().getHero(player);
             Properties prop = this.plugin.getConfigManager().getProperties();
            int exp = (int)hero.getExperience();
             int level = prop.getLevel(exp);
            int current = (int)prop.getExperience(level);
 
             sender.sendMessage(ChatColor.RED + "-----[ " + ChatColor.WHITE + "Your Level Information" + ChatColor.RED + " ]-----");
             sender.sendMessage(ChatColor.GREEN + "  Class: " + ChatColor.WHITE + hero.getHeroClass().getName());
             sender.sendMessage(ChatColor.GREEN + "  Level: " + ChatColor.WHITE + level);
             sender.sendMessage(ChatColor.GREEN + "  Total Exp: " + ChatColor.WHITE + exp);
             if (level != prop.maxLevel) {
                int next = (int)prop.getExperience(level + 1);
                 sender.sendMessage(ChatColor.DARK_GREEN + "  EXP.  " + createExperienceBar(exp, current, next));
             } else {
                 sender.sendMessage(ChatColor.YELLOW + "  MASTERED!");
             }
             sender.sendMessage(ChatColor.BLUE + "  MANA " + Messaging.createManaBar(hero.getMana()));
         }
     }
 
     private String createExperienceBar(int exp, int currentLevelExp, int nextLevelExp) {
         String expBar = ChatColor.RED + "[" + ChatColor.DARK_GREEN;
         int progress = (int) ((double) (exp - currentLevelExp) / (nextLevelExp - currentLevelExp) * 50);
         for (int i = 0; i < progress; i++) {
             expBar += "|";
         }
         expBar += ChatColor.DARK_RED;
         for (int i = 0; i < 50 - progress; i++) {
             expBar += "|";
         }
         expBar += ChatColor.RED + "]";
         expBar += " - " + ChatColor.DARK_GREEN + progress * 2 + "%  ";
         expBar += "" + ChatColor.DARK_GREEN + (exp - currentLevelExp) + ChatColor.RED + "/" + ChatColor.DARK_GREEN + (nextLevelExp - currentLevelExp);
         return expBar;
     }
 
 }
