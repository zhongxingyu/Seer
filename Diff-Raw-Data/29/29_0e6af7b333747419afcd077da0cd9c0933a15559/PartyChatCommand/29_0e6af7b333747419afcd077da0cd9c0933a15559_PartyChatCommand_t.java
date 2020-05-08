 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 
 public class PartyChatCommand extends BaseCommand {
 
     public PartyChatCommand(Heroes plugin) {
         super(plugin);
         name = "Party Chat Command";
         description = "Sends a message to the whole party";
         usage = "/heroes admin reload";
         minArgs = 1;
        maxArgs = 100000;
         identifiers.add("p");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             Player p = (Player) sender;
             if(plugin.getHeroManager().getHero(p).getParty() != null){

                 for(Player player : plugin.getHeroManager().getHero(p).getParty().getMembers()){
                    player.sendMessage("[p]" + p.getName() + ":" + args.toString());
                 }
             }
         }
     }
 
 }
