 package fr.noogotte.useful_commands.command;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 
 @NestedCommands(name = "useful")
 public class FunCommands extends UsefulCommands {
 
     @Command(name = "rocket", min = 0, max = 2)
     public void rocket(CommandSender sender, CommandArgs args) {
         List<Player> targets = args.getPlayers(0)
                 .match(sender, "useful.fun.rocket.other");
 
         for(Player target : targets) {
             target.setVelocity(new Vector(0, 50, 0));
             target.sendMessage(ChatColor.GREEN + "Rocket !");
 
             if(!sender.equals(target)) {
                 sender.sendMessage(ChatColor.GREEN + "Vous avez propulsés " +
                         ChatColor.AQUA + target.getName());
             }
         }
     }
 
    @Command(name = "strike")
     public void strike(CommandSender sender, CommandArgs args) {
         List<Player> targets = args.getPlayers(0)
                 .match(sender, "useful.fun.strike.other");
 
         for(Player target : targets) {
             target.getWorld().strikeLightning(target.getLocation());
             target.sendMessage(ChatColor.GREEN + "Vous avez ête foudroyé.");
 
             if(!sender.equals(target)) {
                 sender.sendMessage(ChatColor.GREEN + "Vous avez foudroyé " +
                         ChatColor.GOLD + target.getName());
             }
         }
     }
 }
