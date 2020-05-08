 package fr.aumgn.diamondrush.command;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.exception.PlayerNotInGame;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.views.GameView;
 import fr.aumgn.diamondrush.views.MessagesView;
 import fr.aumgn.diamondrush.views.PlayerView;
 import fr.aumgn.diamondrush.views.TeamView;
 import fr.aumgn.diamondrush.views.TeamsView;
 
 @NestedCommands(name = "diamondrush")
 public class InfoCommands extends DiamondRushCommands {
 
     public InfoCommands(DiamondRush dr) {
         super(dr);
     }
 
     @Command(name = "teams")
     public void teams(CommandSender sender, CommandArgs args) {
         ensureIsRunning();
 
         Game game = dr.getGame();
         TeamsView view = new TeamsView(game.getTeams());
         for (String message : view) {
             sender.sendMessage(message);
         }
     }
 
     @Command(name = "spectators")
     public void spectators(CommandSender sender, CommandArgs args) {
         ensureIsRunning();
 
         StringBuilder builder = new StringBuilder();
         for (Player spectator : dr.getGame().getSpectators()) {
             builder.append(spectator.getDisplayName());
             builder.append(" ");
         }
 
         sender.sendMessage("Spectateurs :");
         sender.sendMessage(builder.toString());
     }
 
     @Command(name = "info", min = 0, max = 1, flags = "gt")
     public void info(Player player, CommandArgs args) {
         ensureIsRunning();
 
         Game game = dr.getGame();
         MessagesView view;
         if (game.contains(player)) {
             if (args.length() != 0) {
                 throw new CommandError("Impossible de voir les " +
                         "stats des autres joueurs durant la partie.");
             } else {
                 view = getInfoViewForPlayer(player, game, args);
             }
         } else {
            if (args.length() == 0 && !args.hasFlag('g')) {
                 throw new CommandError("Vous n'êtes pas dans la partie, " +
                     "specifiez un argument");
             } else {
                 view = getInfoViewForOthers(game, args);
             }
         }
 
         for (String message : view) {
             player.sendMessage(message);
         }
     }
 
     private MessagesView getInfoViewForPlayer(Player player, Game game, CommandArgs args) {
         if (args.hasFlag('g')) {
             return new GameView(game, true, true);
         } else if (args.hasFlag('t')) {
             Team team = game.getTeam(player);
             return new TeamView(game, team);
         } else {
             return new PlayerView(game, player);
         }
     }
 
     private MessagesView getInfoViewForOthers(Game game, CommandArgs args) {
         if (args.hasFlag('g')) {
             return new GameView(game, true, true);
         } else if (args.hasFlag('t')) {
             Team team = game.getTeam(args.get(0));
             return new TeamView(game, team);
         } else {
             Player target = matchPlayer(args.get(0));
             if (!game.contains(target)) {
                 throw new PlayerNotInGame();
             }
 
             return new PlayerView(game, target);
         }
     }
 }
