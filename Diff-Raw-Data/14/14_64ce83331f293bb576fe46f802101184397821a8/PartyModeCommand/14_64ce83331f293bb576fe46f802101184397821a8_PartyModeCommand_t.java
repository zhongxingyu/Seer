 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class PartyModeCommand extends BaseCommand {
 
     public PartyModeCommand(Heroes plugin) {
         super(plugin);
         setName("Party Mode");
         setDescription("Toggles exp sharing or party pvp");
         setUsage("/party mode <pvp|exp>");
         setMinArgs(1);
         setMaxArgs(1);
         getIdentifiers().add("party mode");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             Hero hero = plugin.getHeroManager().getHero(player);

            if (hero.getParty() == null) {
                Messaging.send(player, "You are not in a party.");
                 return;
            }

             HeroParty heroParty = hero.getParty();
            if (heroParty.getLeader().equals(hero)) {
                 if (args[0].equalsIgnoreCase("pvp")) {
                     heroParty.pvpToggle();
                 } else if (args[0].equalsIgnoreCase("exp")) {
                     heroParty.expToggle();
                 }
             } else {
                 Messaging.send(player, "Sorry, you need to be the leader to do that");
             }
         }
     }
 
 }
