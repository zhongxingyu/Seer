 /**
  *
  */
 package games.pack.protap;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 
 /**
  * @author Tariq
  *
  */
 public class BoxingActivity extends PracticeBoxingActivity {
     private static int sHighScore;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         new GetBoxing().execute();
     }
 
     @Override
     protected void end() {
         final int finalScore = Integer.parseInt(mCounterView.getText().toString());
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Your score: " + finalScore).setCancelable(true)
                 .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog, final int id) {
                         finish();
                     }
                 });
        builder.create().show();
         if (finalScore > sHighScore) {
             sHighScore = finalScore;
             new PostTopScore(this, PostTopScore.BOXING).execute(sHighScore);
             final PostHighScore newHighscore = new PostHighScore(BoxingActivity.this, "boxing",
                     finalScore);
             newHighscore.enterName();
         }
     } // end
 
     private final class GetBoxing extends RetrieveTopScore {
         public GetBoxing() {
             super(BoxingActivity.this, BOXING);
         }
 
         @Override
         protected void onPostExecute(final Integer[] result) {
             if (result[0] != null) sHighScore = result[0];
         }
     } // GetBoxing
 } // BoxingActivity
