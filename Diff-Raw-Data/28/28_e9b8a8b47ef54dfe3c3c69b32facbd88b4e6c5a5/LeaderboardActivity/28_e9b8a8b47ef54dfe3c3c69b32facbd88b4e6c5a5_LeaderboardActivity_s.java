 package edu.rit.cs.distrivia;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.TableLayout;
import android.widget.LinearLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 
 /**
  * Activity for players to answer questions during a round.
  */
 public class LeaderboardActivity extends Activity {
 
     //private String authToken;
     //private String gameId;
 	TableLayout leaderTable;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.leaderboard);
         
         leaderTable = (TableLayout) findViewById(R.id.leader_table);
        loadTable();
         leaderTable.setStretchAllColumns(true);
     }
     
     private void loadTable() {
     	TableRow row = new TableRow(getApplicationContext());
    	LinearLayout rowLayout = new LinearLayout(getApplicationContext());
    	rowLayout.setOrientation(LinearLayout.HORIZONTAL);
     	TextView name = new TextView(getApplicationContext());
     	name.setText("Prof1");
     	TextView score = new TextView(getApplicationContext());
     	score.setText("10000");
    	rowLayout.addView(name);
    	rowLayout.addView(score);
    	row.addView(rowLayout);
     	leaderTable.addView(row);
     	
     }
 
 }
