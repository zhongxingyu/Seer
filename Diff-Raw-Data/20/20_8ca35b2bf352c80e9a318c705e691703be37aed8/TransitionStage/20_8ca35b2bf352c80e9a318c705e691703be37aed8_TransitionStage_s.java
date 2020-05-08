 package fr.aumgn.tobenamed.stage;
 
 import org.bukkit.ChatColor;
 
 import fr.aumgn.tobenamed.TBN;
 import fr.aumgn.tobenamed.game.Game;
 
 public class TransitionStage extends StaticStage {
 
    public TransitionStage(final Game game, Runnable nextStage) {
         super(game);
         scheduleNextStage(TBN.getConfig().getTransitionDuration(), new Runnable() {
             public void run() {
                game.sendMessage(ChatColor.YELLOW + "C'est le moment de changer de channel.");
                game.nextStage(new DevelopmentStage(game));
             }
         });
     }
 }
