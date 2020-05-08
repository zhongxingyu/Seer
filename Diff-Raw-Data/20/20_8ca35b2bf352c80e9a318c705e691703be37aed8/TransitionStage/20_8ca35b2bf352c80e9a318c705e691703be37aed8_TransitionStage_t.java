 package fr.aumgn.tobenamed.stage;
 
 import org.bukkit.ChatColor;
 
 import fr.aumgn.tobenamed.TBN;
 import fr.aumgn.tobenamed.game.Game;
 
 public class TransitionStage extends StaticStage {
 
    public TransitionStage(final Game game, final Runnable nextStage) {
         super(game);
         scheduleNextStage(TBN.getConfig().getTransitionDuration(), new Runnable() {
             public void run() {
                nextStage.run();
             }
         });
     }

    @Override
    public void start() {
        super.start();
        game.sendMessage(ChatColor.YELLOW + "C'est le moment de changer de channel.");
    }
 }
