 package fr.aumgn.tobenamed.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import fr.aumgn.bukkit.command.Command;
 import fr.aumgn.bukkit.command.CommandArgs;
 import fr.aumgn.bukkit.command.Commands;
 import fr.aumgn.tobenamed.TBN;
 import fr.aumgn.tobenamed.stage.Stage;
 
 public class GeneralCommands extends Commands {
 
     @Command(name = "stop-game", max = 0)
     public void stopGame(CommandSender sender, CommandArgs args) {
         Stage stage = TBN.getStage();
        TBN.nextStage(null);
         stage.getGame().sendMessage(ChatColor.RED + "La partie a été arreté.");
     }
 }
