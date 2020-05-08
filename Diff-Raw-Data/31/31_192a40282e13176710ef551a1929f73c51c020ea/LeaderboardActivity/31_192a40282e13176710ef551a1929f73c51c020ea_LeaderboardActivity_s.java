 package edu.rit.cs.distrivia;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import edu.rit.cs.distrivia.api.DistriviaAPI;
 
 /**
  * Activity for players to answer questions during a round.
  */
 public class LeaderboardActivity extends GameActivityBase {
     final int USER = 0;
     final int SCORE = 1;
     final int NAME_WIDTH = 200;
     TableLayout leaderTable;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.leaderboard);
 
         leaderTable = (TableLayout) findViewById(R.id.leader_table);
         leaderTable.setStretchAllColumns(true);
         loadTable(gameData().loadLocal());
     }
 
     /**
      * loads the leaderboard table with user/score data
      * 
      * @param loadLocal - grab local data if true, global if false
      */
     private void loadTable(boolean loadLocal) {
     	String[][] board = null;
     	if (loadLocal) {
     		board = gameData().getLeaderboard();
     	}
     	else {
     		try {
     			board = DistriviaAPI.leaderBoard(gameData());
     		} catch (Exception e) {
     			Log.d("Load leaderboard: ", e.getMessage());
     			makeToast("The network is DOWN!");
     			return;
     		}
     	}
         for (int i = 0; i < board.length; i++) {
             if (!board[i][USER].equals("status")) {
                 TableRow row = new TableRow(getApplicationContext());
                 TextView name = new TextView(getApplicationContext());
                 TextView score = new TextView(getApplicationContext());
 
                 name.setText(board[i][USER]);
                 name.setWidth(NAME_WIDTH);
                 score.setText(board[i][SCORE]);
 
                 row.addView(name);
                 row.addView(score);
                 leaderTable.addView(row);
             }
         }
     }
 }
