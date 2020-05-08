 package fr.aumgn.diamondrush.views;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.GameStatistics;
 
 public class GameView extends MessagesView {
 
     public GameView(Game game, boolean showTeams, boolean showBlocksBroken) {
         super();
 
         headLn("Statistiques de la partie");
         if (showTeams) {
             entryLn("Nombres d'équipes restantes", game.getTeams().size());
         }
 
         GameStatistics stats = game.getStatistics();
         merge(new StatisticsView(stats, false, showBlocksBroken));
 
         List<Player> motg = stats.getMenOfTheGame();
         if (motg.size() == 0) {
         } else if (motg.size() == 1) {
            entryLn("Homme de la partie", motg.get(0));
         } else {
             StringBuilder builder = new StringBuilder();
             for (Player manOfTheGame : motg) {
                 builder.append(manOfTheGame.getDisplayName());
                 builder.append(" ");
             }
             entryLn("Hommes de la partie", builder.toString());
         }
     }
 }
