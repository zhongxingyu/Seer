 /**
  * 
  */
 
 package phoenix.game.battleship;
 
 import phoenix.game.battleship.actors.Game;
 import phoenix.game.battleship.utils.Config;
 import phoenix.game.battleship.views.OnPlayerEventListener;
 import phoenix.game.battleship.views.OnUpdateGameListener;
 import phoenix.game.battleship.views.OpponentView;
 import phoenix.game.battleship.views.PlayerView;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.AnimationUtils;
 import android.widget.LinearLayout;
 import android.widget.ViewFlipper;
 
 /**
  * @author pushpan
  */
 public class GameActivity extends Activity implements OnPlayerEventListener, OnUpdateGameListener {
     private static final String TAG = "GameActivity";
 
     private PlayerView playerView;
 
     private OpponentView opponentView;
 
     private Game game;
 
     private ViewFlipper viewFlipper;
 
     private LinearLayout playerLayout;
 
     private LinearLayout secondaryLayout;
 
     private final static int MY_PLAYER_ID = 0;
 
     public Game getGame() {
         return game;
     }
 
     public void setGame(Game game) {
         this.game = game;
     }
 
     /**
 	 * 
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.game_activity);
         initialize();
     }
 
     public boolean isTablet() {
         return (secondaryLayout != null);
     }
 
     void initialize() {
         playerLayout = (LinearLayout) findViewById(R.id.playerLayout);
         secondaryLayout = (LinearLayout) findViewById(R.id.secondaryLayout);
         playerView = new PlayerView(this);
         getPlayerView().setOnPlayerEventListener(this);
         opponentView = new OpponentView(this);
         getOpponentView().setOnPlayerEventListener(this);
 
         if (isTablet()) {
             playerLayout.addView(playerView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                     LayoutParams.WRAP_CONTENT));
             secondaryLayout.addView(opponentView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                     LayoutParams.WRAP_CONTENT));
             playerView.setControlsForTablet(true);
             opponentView.setControlsForTablet(true);
         } else {
             viewFlipper = new ViewFlipper(this);
             playerLayout.addView(viewFlipper, new LayoutParams(LayoutParams.FILL_PARENT,
                     LayoutParams.FILL_PARENT));
             viewFlipper.addView(playerView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                     LayoutParams.WRAP_CONTENT));
             viewFlipper.addView(opponentView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                     LayoutParams.WRAP_CONTENT));
         }
 
         initGame();
         refresh();
         getGame().startGame();
     }
 
     private void initGame() {
         Game game = new Game(this);
         setGame(game);
         game.setTablet(isTablet());
         game.initGame();
         game.setOnUpdateGameListener(this);
         game.setOnGameEventListener(getPlayerView());
         game.initPlayers(MY_PLAYER_ID);
         getPlayerView().setGame(game);
         getOpponentView().setGame(game);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         // gameView.onResume();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         // gameView.onPause();
     }
 
     public PlayerView getPlayerView() {
         return playerView;
     }
 
     public void setPlayerView(PlayerView playerView) {
         this.playerView = playerView;
     }
 
     public OpponentView getOpponentView() {
         return opponentView;
     }
 
     public void setOpponentView(OpponentView opponentView) {
         this.opponentView = opponentView;
     }
 
     @Override
     public void switchToOpponentBoard() {
         if (!isTablet()) {
             viewFlipper.setOutAnimation(AnimationUtils.makeOutAnimation(this, false));
             viewFlipper.setInAnimation(AnimationUtils.makeInAnimation(this, false));
             getOpponentView().update();
             viewFlipper.showNext();
         } else {
             getOpponentView().update();
         }
     }
 
     @Override
     public void refresh() {
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getOpponentView().refresh();
                 getPlayerView().refresh();
             }
         });
 
     }
 
     @Override
     public void switchToPlayerBoard() {
         if (!isTablet()) {
             viewFlipper.setOutAnimation(AnimationUtils.makeOutAnimation(this, true));
             viewFlipper.setInAnimation(AnimationUtils.makeInAnimation(this, true));
             getPlayerView().update();
             viewFlipper.showPrevious();
         } else {
             getPlayerView().update();
         }
     }
 
     @Override
     public void loadPlayerPerspective() {
         getOpponentView().loadPlayerPerspective();
         getPlayerView().loadPlayerPerspective();
     }
 
     @Override
     public void onUpdateGame() {
         loadPlayerPerspective();
         refresh();
         if (Config.DEBUG) {
             Log.i(TAG, "PerspectivePlayer = " + getGame().getPerspectivePlayer().getPlayerId());
         }
     }
 }
