 package com.cauchymop.goblob;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.FrameLayout;
 
 public class GameActivity extends Activity implements Game.Listener, GoBoardView.Listener {
 
   public static final String EXTRA_GAME = "Game";
   private GoGame goGame;
   private GoBoardView goBoardView;
   private HumanPlayerController currentPlayerController;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.game_activity);
     FrameLayout container = (FrameLayout) findViewById(R.id.boardViewContainer);
     goGame = getIntent().getParcelableExtra(EXTRA_GAME);
     goGame.setBlackController(getController(goGame.getBlackPlayer()));
     goGame.setWhiteController(getController(goGame.getWhitePlayer()));
     goGame.addListener(this);
     goGame.runGame();
     goBoardView = new GoBoardView(getApplicationContext(), goGame);
     goBoardView.addListener(this);
     container.addView(goBoardView);
   }
 
   private PlayerController getController(Player player) {
     if (player.getType() == Player.PlayerType.AI) {
       return new AIPlayerController(goGame);
     }
     if (player.getType() == Player.PlayerType.HUMAN) {
       return new HumanPlayerController(goGame);
     }
     throw new RuntimeException("Unsupported player type");
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.game_menu, menu);
     return true;
   }
 
   public void pass(View v) {
     goGame.pass(Game.MoveType.REAL);
   }
 
   @Override
   public void gameChanged(Game game) {
     if (game.isGameEnd()) {
       runOnUiThread(new Runnable() {
         @Override
         public void run() {
           new AlertDialog.Builder(GameActivity.this)
               .setMessage("End of game")
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                   setResult(RESULT_OK);
                   finish();
                 }
               })
               .create()
               .show();
         }
       });
     }
    goBoardView.postInvalidate();
   }
 
   @Override
   public void played(int x, int y) {
     currentPlayerController.play(x, y);
   }
 
   private class HumanPlayerController extends PlayerController {
 
     private boolean played;
     private GoGame game;
 
     public HumanPlayerController(GoGame game) {
       this.game = game;
     }
 
     private void play(int x, int y) {
       if (game.play(x, y, Game.MoveType.REAL)) {
        played = true;
         synchronized (this) {
           this.notifyAll();
         }
       } else {
         buzz();
       }
     }
 
     private void buzz() {
       try {
         Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
         Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
         r.play();
       } catch (Exception e) {
         System.err.println("Exception while buzzing");
         e.printStackTrace();
       }
     }
 
     @Override
     public void startTurn() {
       // TODO: show that it's my turn.
 
       played = false;
       currentPlayerController = this;
       synchronized (this) {
         while (!played) {
           try {
             this.wait();
           } catch (InterruptedException e) {
             // Expected.
           }
         }
       }
 
       // TODO: show that it's not my turn anymore.
     }
   }
 }
