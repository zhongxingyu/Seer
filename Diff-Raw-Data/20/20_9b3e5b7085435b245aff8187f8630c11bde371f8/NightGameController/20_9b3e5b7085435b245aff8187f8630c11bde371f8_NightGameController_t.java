 package controllers.server;
 
 import controllers.PlayerListener;
 import controllers.WorkFlow;
 import model.Player;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class NightGameController implements PlayerListener {
     private Game game;
     private final List<Player> players;
     private final WorkFlow workFlow;
     private NightGameView nightGameview;
 
     public NightGameController (List<Player> players, WorkFlow workFlow) {
         this.players = players;
         this.workFlow = workFlow;
     }
 
     public static NightGameController createController (List<Player> players, WorkFlow workFlow) {
         return new NightGameController (players, workFlow);
     }
 
     public void bind (NightGameView nightGameview, Game game) {
         this.nightGameview = nightGameview;
         this.game = game;
         for (Player player : players) {
             player.bindPlayerListener (this);
         }
     }
 
 
     public void start () {
         nightGameview.display (game.getPlayersChart ());
         new Timer ().schedule (new TimerTask () {
             @Override
             public void run () {
                 game.sendPlayersListToMafias ();
             }
         }, 1000);
     }
 
     @Override
     public void playerQuits (Player player) {
     }
 
     @Override
     public void onVoteArrived (String name, String voteTo) {
         game.sendNightVotesInfoToMafias (name, voteTo);
     }
 
     public void stopServer () {
         for (Player player : players)
             player.stop ();
         workFlow.goHome ();
     }
 
     public void sendEliminatedPlayerInfo () {
         game.sendDayArrivedMessage ();
         game.sendEliminatedPlayerInfo ();
        new Timer ().schedule (new TimerTask () {
            @Override
            public void run () {
                if (!game.calculateRatio ()) {
                     workFlow.startDayGame (players, game);
                 }
            }
        }, 1000);
     }
 }
