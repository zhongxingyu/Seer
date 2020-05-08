 package fr.aumgn.tobenamed.command;
 
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkit.command.Command;
 import fr.aumgn.bukkit.command.CommandArgs;
 import fr.aumgn.bukkit.command.CommandError;
 import fr.aumgn.bukkit.command.Commands;
 import fr.aumgn.tobenamed.TBN;
 import fr.aumgn.tobenamed.stage.JoinStage;
 import fr.aumgn.tobenamed.stage.Stage;
 
 public class JoinStageCommands extends Commands {
 
     @Command(name = "init-game", flags = "a", min = 2)
     public void initGame(Player player, CommandArgs args) {
         if (TBN.getStage() != null) {
             throw new CommandError("Une partie est déja en cours.");
         }
 
         JoinStage stage = new JoinStage(args.asList(), args.hasFlag('a'));
         TBN.nextStage(stage);
     }
 
     @Command(name = "join-team", min = 0, max = 1)
     public void joinTeam(Player player, CommandArgs args) {
         Stage stage = TBN.getStage();
         if (!(stage instanceof JoinStage)) {
            throw new CommandError("Cette commande ne peut etre utilisé que durant une phase de join.");
         }
 
         JoinStage joinStage = (JoinStage) stage;
         if (joinStage.contains(player)) {
             throw new CommandError("Vous etes deja dans la partie.");
         }
 
         if (!joinStage.isRandom() && args.length() > 0) {
             String team = args.get(0);
             joinStage.addPlayer(player, team);
         } else {
             joinStage.addPlayer(player);
         }
     }
 }
