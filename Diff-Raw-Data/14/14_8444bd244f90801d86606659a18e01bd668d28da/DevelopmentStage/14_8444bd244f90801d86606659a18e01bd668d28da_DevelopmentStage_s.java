 package fr.aumgn.tobenamed.stage;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.Listener;
 
 import fr.aumgn.tobenamed.TBN;
 import fr.aumgn.tobenamed.game.Game;
 import fr.aumgn.tobenamed.stage.listeners.DevelopmentListener;
 
 public class DevelopmentStage extends Stage {
 
     private Game game;
     private DevelopmentListener listener;
 
     public DevelopmentStage(Game game) {
         this.game = game;
         this.listener = new DevelopmentListener(this);
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
         game.sendMessage(ChatColor.GREEN + "La phase de développement commence.");
         game.incrementTurnCount();
         int duration = TBN.getConfig().getDevDuration(game.getTurnCount());
         scheduleNextStage(duration, new Runnable() {
             public void run() {
                 game.sendMessage(ChatColor.GREEN + "Fin de la phase de développement.");
                 StaticStage stage = new StaticStage(game);
                 game.nextStage(stage);
                 stage.scheduleNextStage(TBN.getConfig().getTransitionDuration(), new Runnable() {
                     public void run() {
                         game.nextStage(new FightStage(game));
                     }
                 });
             }
         });
     }
 }
