 package fr.aumgn.tobenamed.stage;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.Listener;
 
 import fr.aumgn.tobenamed.TBN;
 import fr.aumgn.tobenamed.game.Game;
 import fr.aumgn.tobenamed.game.Team;
 import fr.aumgn.tobenamed.stage.listeners.FightListener;
 
 public class FightStage extends Stage {
 
     private Game game;
     private FightListener listener;
     private Map<Team, Integer> deathsCounts;
 
     public FightStage(Game game) {
         this.game = game;
         this.listener = new FightListener(this);
         this.deathsCounts = new HashMap<Team, Integer>();
         for (Team team : game.getTeams()) {
             deathsCounts.put(team, 0);
         }
     }
 
     @Override
     public List<Listener> getListeners() {
         return Collections.<Listener>singletonList(listener);
     }
 
     @Override
     public Game getGame() {
         return game;
     }
 
     @Override
     public void start() {
         game.sendMessage(ChatColor.GREEN + "La phase de combat commence.");
         int duration = TBN.getConfig().getFightDuration(game.getTurnCount());
         scheduleNextStage(duration, new Runnable() {
             public void run() {
                 game.sendMessage(ChatColor.GREEN + "Fin de la phase de combat.");
                game.sendMessage(ChatColor.YELLOW + "C'est le moment de changer de channel.");
                 StaticStage stage = new StaticStage(game);
                 game.nextStage(stage);
                 stage.scheduleNextStage(TBN.getConfig().getTransitionDuration(), new Runnable() {
                     public void run() {
                         game.nextStage(new DevelopmentStage(game));
                     }
                 });
             }
         });
     }
 
     public int getDeathCount(Team team) {
         return deathsCounts.get(team);
     }
 
     public void incrementDeathCount(Team team) {
         int count = getDeathCount(team) + 1;
         deathsCounts.put(team, count);
     }
 }
