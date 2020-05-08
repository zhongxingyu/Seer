 package fr.noogotte.useful_commands.command;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.noogotte.useful_commands.GodComponent;
 
 @NestedCommands(name = "useful")
 public class GodCommand extends UsefulCommands {
 
     private GodComponent godComponent;
 
     public GodCommand(GodComponent godComponent) {
         this.godComponent = godComponent;
     }
 
     @Command(name = "god", min = 0, max = 1)
     public void god(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
 
         for (Player target : targets) {
             if (godComponent.isGod(target)) {
                 godComponent.removeGod(target);
                 target.sendMessage(ChatColor.AQUA
                         + "Mode dieu arreté.");
                 if (!player.equals(target)) {
                     player.sendMessage(ChatColor.AQUA
                             + "Mode dieux arreté pour "
                             + ChatColor.BLUE + target.getName());
                 }
             } else {
                 godComponent.setGod(target);
                 target.sendMessage(ChatColor.GREEN
                         + "Vous êtes en mode Dieu, tapez "
                         + ChatColor.BLUE + " /god "
                         + ChatColor.GREEN + " pour en resortir.");
                 if (!player.equals(target)) {
                     player.sendMessage(ChatColor.AQUA
                             + "Vous avez mis le mode Dieux pour "
                             + ChatColor.BLUE + target.getName());
                 }
             }
         }
     }
 }
