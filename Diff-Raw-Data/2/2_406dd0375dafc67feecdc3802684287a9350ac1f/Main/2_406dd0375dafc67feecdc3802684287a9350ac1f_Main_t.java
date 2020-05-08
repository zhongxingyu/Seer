 package com.jneander.tictactoe.main;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.jneander.tictactoe.R;
 import com.jneander.tictactoe.game.Game;
 import com.jneander.tictactoe.game.Mark;
 import com.jneander.tictactoe.ui.GameView;
 
 public class Main extends Activity {
   private final Game game = new Game();
 
   private GameView gameView;
   private TextView messageView;
   private Button makeMarkButton;
   private Button newGameButton;
 
   @Override
   public void onCreate( Bundle savedInstanceState ) {
     super.onCreate( savedInstanceState );
     setContentView( R.layout.main );
 
     gameView = (GameView) findViewById( R.id.game_view );
     messageView = (TextView) findViewById( R.id.game_message );
     makeMarkButton = (Button) findViewById( R.id.button_make_mark );
     newGameButton = (Button) findViewById( R.id.button_new_game );
 
     gameView.reset();
 
     makeMarkButton.setOnClickListener( new OnClickListener() {
       @Override
       public void onClick( View v ) {
         int spaceIndex = gameView.getSelectedSpaceIndex();
 
        if ( !game.positionIsMarked( spaceIndex ) && !game.isGameOver() ) {
           game.makePlayerMarkAtPosition( spaceIndex );
           gameView.updateMarkAtPosition( spaceIndex, Mark.PLAYER );
 
           if ( !game.isGameOver() ) {
             int markIndex = game.makeComputerMark();
             gameView.updateMarkAtPosition( markIndex, Mark.COMPUTER );
           }
 
           if ( game.isGameOver() ) {
             messageView.setText( (game.getWinner() == Mark.COMPUTER) ?
                 getString( R.string.lose_message ) : getString( R.string.tie_message ) );
           }
         }
       }
     } );
 
     newGameButton.setOnClickListener( new OnClickListener() {
       @Override
       public void onClick( View v ) {
         messageView.setText( "" );
         game.reset();
         gameView.reset();
       }
     } );
   }
 }
