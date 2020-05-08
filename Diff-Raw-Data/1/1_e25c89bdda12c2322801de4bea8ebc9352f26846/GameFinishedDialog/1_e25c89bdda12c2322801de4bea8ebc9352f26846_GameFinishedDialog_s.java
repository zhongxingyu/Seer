 package edu.hm.androidsweeper.dialogs;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import edu.hm.androidsweeper.R;
 import edu.hm.androidsweeper.activities.DifficultActivity;
 import edu.hm.androidsweeper.activities.MainMenuActivity;
 
 /**
  * The dialog which informs the user of the game state if the game is finished.<br>
  * The user can retry the game, show the highscore or go back to the main menu.
  */
 public class GameFinishedDialog extends AlertDialog {
     
     private final Context context;
     
     /**
      * Creates a new instance of {@link GameFinishedDialog}.
      * 
      * @param context
      *            the activity context
      * @param isWon
      *            the state is the game was won or lose
      * @param time
      *            the total playtime for this game
      */
     public GameFinishedDialog(final Context context, final boolean isWon, final double time) {
         super(context);
         this.context = context;
         init(isWon, time);
     }
     
     private void init(final boolean isWon, final double time) {
         setCancelable(true);
         String title;
         String message;
         if (isWon) {
             title = context.getString(R.string.dialog_finished_won_title);
             message = context.getString(R.string.dialog_finished_won_message, time);
             setIcon(R.drawable.smily_happy);
         }
         else {
             title = context.getString(R.string.dialog_finished_lose_title);
             message = context.getString(R.string.dialog_finished_lose_message, time);
             setIcon(R.drawable.smily_sad);
         }
         
         createButtons();
         setTitle(title);
         setMessage(message);
     }
     
     private void createButtons() {
         setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_finished_button_back),
                 new OnClickListener() {
             
             @Override
             public void onClick(final DialogInterface dialog, final int which) {
                 Intent intent = new Intent(context, MainMenuActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                         | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 context.startActivity(intent);
                 dialog.dismiss();
             }
         });
         setButton(BUTTON_NEUTRAL, context.getString(R.string.dialog_finished_button_highscore),
                 new OnClickListener() {
             
             @Override
             public void onClick(final DialogInterface dialog, final int which) {
                 // TODO Auto-generated method stub
                 
             }
         });
         setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_finished_button_restart),
                 new OnClickListener() {
             
             @Override
             public void onClick(final DialogInterface dialog, final int which) {
                 Intent intent = new Intent(context, DifficultActivity.class);
                 context.startActivity(intent);
                 dialog.dismiss();
             }
         });
     }
     
 }
