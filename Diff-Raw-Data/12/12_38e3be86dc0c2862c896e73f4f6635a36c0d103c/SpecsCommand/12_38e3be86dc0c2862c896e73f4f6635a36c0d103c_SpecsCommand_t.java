 package com.herocraftonline.dev.heroes.command.commands;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SpecsCommand extends BasicCommand {
 
     private static final int SPECS_PER_PAGE = 8;
     private final Heroes plugin;
 
     public SpecsCommand(Heroes plugin) {
         super("Specializations");
         this.plugin = plugin;
         setDescription("Lists all specializations available to your path");
         setUsage("/hero specs [primary|profession] §8[page#]");
         setArgumentRange(0, 2);
         setIdentifiers("hero specs");
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         if (!(sender instanceof Player))
             return false;
 
         Hero hero = plugin.getHeroManager().getHero((Player) sender);
         Set<HeroClass> childClasses = new HashSet<HeroClass>();
 
         int page = 0;
         String classNames = "";
         if (args.length == 0) {
             childClasses.addAll(hero.getHeroClass().getSpecializations());
             if (hero.getSecondClass() != null) {
                 childClasses.addAll(hero.getSecondClass().getSpecializations());
                 classNames = hero.getHeroClass().getName() + " and " + hero.getSecondClass().getName();
             } else
                 classNames = hero.getHeroClass().getName();
         } else if (args.length == 1) {
             try {
                 page = Integer.parseInt(args[0]) - 1;
             } catch (NumberFormatException ignored) {
                 if (args[0].toLowerCase().contains("pri")) {
                     childClasses.addAll(hero.getHeroClass().getSpecializations());
                     classNames = hero.getHeroClass().getName();
                 } else if (args[0].toLowerCase().contains("prof")) {
                     if (hero.getSecondClass() != null) {
                         childClasses.addAll(hero.getSecondClass().getSpecializations());
                         classNames = hero.getSecondClass().getName();
                     } else {
                         Messaging.send(sender, "You don't have a profession!");
                         return false;
                     }
                 } else {
                     Messaging.send(sender, getUsage());
                     return false;
                 }
             }
         } else if (args.length == 2) {
             try {
                 page = Integer.parseInt(args[1]) - 1;
             } catch (NumberFormatException ignored) {
 
             }
             if (args[0].toLowerCase().contains("pri")) {
                 childClasses.addAll(hero.getHeroClass().getSpecializations());
                 classNames = hero.getHeroClass().getName();
             } else if (args[0].toLowerCase().contains("prof")) {
                 if (hero.getSecondClass() != null) {
                     childClasses.addAll(hero.getSecondClass().getSpecializations());
                     classNames = hero.getSecondClass().getName();
                 } 
             } else {
                 Messaging.send(sender, getUsage());
                 return false;
             }
         }
 
 
         HeroClass[] specs = childClasses.toArray(new HeroClass[childClasses.size()]);
         if (specs.length == 0) {
             Messaging.send(sender, "Your classes have no specializations.");
             return false;
         }
 
         int numPages = specs.length / SPECS_PER_PAGE;
         if (specs.length % SPECS_PER_PAGE != 0) {
             numPages++;
         }
 
         if (page >= numPages || page < 0) {
             page = 0;
         }
         sender.sendMessage("§c-----[ " + "§f" + classNames + " Specializations <" + (page + 1) + "/" + numPages + ">§c ]-----");
         int start = page * SPECS_PER_PAGE;
         int end = start + SPECS_PER_PAGE;
         if (end > specs.length) {
             end = specs.length;
         }
         for (int c = start; c < end; c++) {
             HeroClass heroClass = specs[c];
             String description = heroClass.getDescription();
             if (description == null || description.isEmpty()) {
                 sender.sendMessage("  §a" + heroClass.getName());
             } else {
                sender.sendMessage("  §a" + heroClass.getName() + " - §f" + heroClass.getDescription());
             }
         }
 
        sender.sendMessage("§cTo choose a specialization, type §f/hero choose <spec> §cor §f/hero prof <spec>");
         return true;
     }
 
 }
