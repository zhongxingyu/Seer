 package fr.aumgn.diamondrush.stage;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.diamondrush.exception.NotEnoughPlayers;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.game.TeamsView;
 
 public class RandomJoinStage extends JoinStage {
 
     private List<Player> players;
 
     public RandomJoinStage(Game game) {
         super(game);
         players = new ArrayList<Player>();
     }
 
     @Override
     public boolean contains(Player player) {
         return players.contains(player);
     }
 
     @Override
     public void addPlayer(Player player, Team team) {
         if (team != null) {
            player.sendMessage(ChatColor.RED +
                    "Les équipes sont constituées aléatoirement.");
         }
 
         if (players.size() > 0) {
             player.sendMessage(ChatColor.YELLOW + "Joueur actuels : ");
         }
         for (Player playerInStage : players) {
             player.sendMessage(ChatColor.YELLOW + " - " +
                     playerInStage.getDisplayName());
            playerInStage.sendMessage(player.getDisplayName() +
                    ChatColor.YELLOW + " a rejoint la partie.");
         }
         players.add(player);
     }
 
     @Override
     public void removePlayer(Player player) {
         players.remove(player);
         String msg = player.getDisplayName() + ChatColor.YELLOW +
                 " a quitté la partie.";
        for (Player playerInStage : players) {
            playerInStage.sendMessage(msg);
        }
     }
 
     @Override
     public void ensureIsReady() {
         if (game.getTeams().size() > players.size()) {
             throw new NotEnoughPlayers();
         }
     }
 
     @Override
     public void prepare() {
         Collections.shuffle(players);
         for (Player player : players) {
             game.addPlayer(player, null);
         }
         TeamsView view = new TeamsView(game.getTeams());
         for (String message : view) {
             game.sendMessage(message);
         }
     }
 }
