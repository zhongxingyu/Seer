 package fr.aumgn.diamondrush.command;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.aumgn.bukkitutils.util.Vector;
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.TeamColor;
 import fr.aumgn.diamondrush.stage.JoinStage;
 import fr.aumgn.diamondrush.stage.RandomJoinStage;
 import fr.aumgn.diamondrush.stage.SimpleJoinStage;
 import fr.aumgn.diamondrush.stage.Stage;
 
 @NestedCommands(name = "diamondrush")
 public class GameCommands extends DiamondRushCommands {
 
     public GameCommands(DiamondRush dr) {
         super(dr);
     }
 
     @Command(name = "init", min = 1, max = -1, flags = "cn")
     public void initGame(Player player, CommandArgs args) {
         if (dr.isRunning()) {
             throw new CommandError("Une partie est déjà en cours.");
         }
 
         Map<String, TeamColor> teams = new HashMap<String, TeamColor>();
         if (args.hasFlag('n')) {
             List<String> teamsNames = args.asList();
             Iterator<TeamColor> colors = 
                     TeamColor.getRandomColors(teamsNames.size()).iterator();
             for (String name : teamsNames) {
                 teams.put(name, colors.next());
             }
         } else {
             if (args.length() > 1) {
                 throw new CommandUsageError("Cette commande ne prend qu'un seul argument.");
             }
            int amount = Integer.parseInt(args.get(0));
             Iterator<TeamColor> colors = 
                     TeamColor.getRandomColors(amount).iterator();
             for (;amount > 0; amount--) {
                 TeamColor color = colors.next();
                 teams.put(color.getColorName(), color);
             }
         }
 
         Game game = new Game(teams, player.getWorld(),
                 new Vector(player.getLocation()), dr.getConfig().getLives());
 
         JoinStage stage;
         if (args.hasFlag('c')) {
             stage = new SimpleJoinStage(dr);
         } else {
             stage = new RandomJoinStage(dr);
         }
         dr.initGame(game, stage);
     }
 
     @Command(name = "start")
     public void startGame(CommandSender sender, CommandArgs args) {
         ensureIsRunning();
         dr.startGame();
     }
 
     @Command(name = "stop")
     public void stopGame(CommandSender sender, CommandArgs args) {
         ensureIsRunning();
         dr.gameStop();
     }
 
     @Command(name = "pause")
     public void pauseGame(CommandSender sender, CommandArgs args) {
         ensureIsRunning();
         if (dr.isPaused()) {
             throw new CommandError("Le jeu est déjà en pause.");
         }
         dr.pause();
     }
 
     @Command(name = "resume")
     public void resumeGame(CommandSender sender, CommandArgs args) {
         ensureIsRunning();
         if (!dr.isPaused()) {
             throw new CommandError("Le jeu n'est pas en pause.");
         }
 
         Game game = dr.getGame();
         Stage stage = dr.getStage();
         if (stage.hasNextStageScheduled()) {
             throw new CommandError(
                     "La partie est déjà sur le point de redémarrer.");
         }
 
         game.sendMessage(ChatColor.GREEN + "La partie va reprendre.");
         stage.schedule(3, new Runnable() {
             public void run() {
                 dr.resume();
             }
         });
     }
 }
