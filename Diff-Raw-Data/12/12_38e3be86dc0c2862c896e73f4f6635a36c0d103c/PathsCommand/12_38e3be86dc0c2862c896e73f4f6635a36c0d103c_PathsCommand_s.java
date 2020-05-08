 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.command.CommandHandler;
 import com.herocraftonline.dev.heroes.hero.Hero;
 
 public class PathsCommand extends BasicCommand {
 
     private static final int PATHS_PER_PAGE = 8;
     private final Heroes plugin;
 
     public PathsCommand(Heroes plugin) {
         super("Paths");
         this.plugin = plugin;
         setDescription("Lists all paths available to you, primary and professions!");
         setUsage("/hero paths §8[page#]");
         setArgumentRange(0, 1);
         setIdentifiers("hero paths");
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         int page = 0;
         if (args.length != 0) {
             try {
                 page = Integer.parseInt(args[0]) - 1;
             } catch (NumberFormatException ignored) {}
         }
 
 
         List<HeroClass> paths = new ArrayList<HeroClass>();
         if (sender instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) sender);
             HeroClass hc = hero.getHeroClass();
             HeroClass sc = hero.getSecondClass();
             for (HeroClass heroClass : plugin.getClassManager().getClasses()) {
                 if (heroClass.equals(hc) || heroClass.equals(sc)) {
                     continue;
                 } else if ((!heroClass.getSpecializations().isEmpty() && heroClass.hasNoParents()) || heroClass.isDefault()) {
                     paths.add(heroClass);
                 } else if (hero.isMaster(hc) && hc.getSpecializations().contains(heroClass)) {
                     paths.add(heroClass);
                 } else if (sc != null && hero.isMaster(sc) && sc.getSpecializations().contains(heroClass)) {
                     paths.add(heroClass);
                 }
             }
         } else {
             paths.addAll(plugin.getClassManager().getClasses());
         }
 
         int numPages = paths.size() / PATHS_PER_PAGE;
         if (paths.size() % PATHS_PER_PAGE != 0) {
             numPages++;
         }
 
         if (page >= numPages || page < 0) {
             page = 0;
         }
         sender.sendMessage("§c-----[ " + "§fHeroes Paths <" + (page + 1) + "/" + numPages + ">§c ]-----");
         int start = page * PATHS_PER_PAGE;
         int end = start + PATHS_PER_PAGE;
         if (end > paths.size()) {
             end = paths.size();
         }
         for (int c = start; c < end; c++) {
             HeroClass heroClass = paths.get(c);
 
             if (!heroClass.isDefault() && !CommandHandler.hasPermission(sender, "heroes.classes." + heroClass.getName().toLowerCase())) {
                 continue;
             }
 
             String prefix = "";
             if (heroClass.isPrimary()) {
                 prefix += "Pri";
             }
             if (heroClass.isSecondary()) {
                 if (prefix != "") {
                     prefix += " | ";
                     prefix = "  §d" + prefix;
                 } else {
                     prefix += "  §c";
                 }
                 prefix += "Prof";
             } else if (heroClass.isPrimary())
                 prefix = "  §9" + prefix;
 
             String description = heroClass.getDescription();
             if (description == null || description.isEmpty()) {
                 sender.sendMessage(prefix + " | §a" + heroClass.getName());
             } else {
                sender.sendMessage(prefix +  " | §a" + heroClass.getName() + " - " + heroClass.getDescription());
             }
         }
 
         sender.sendMessage("§cTo choose a path, type §f/hero choose <path>");
         return true;
     }
 
 }
