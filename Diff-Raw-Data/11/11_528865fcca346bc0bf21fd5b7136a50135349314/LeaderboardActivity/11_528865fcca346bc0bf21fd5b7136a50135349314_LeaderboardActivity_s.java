 package edu.rit.cs.distrivia;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import edu.rit.cs.distrivia.api.DistriviaAPI;
 import edu.rit.cs.distrivia.model.GameData;
 
 /**
  * Activity for players to answer questions during a round.
  */
 public class LeaderboardActivity extends GameActivityBase {
     final int USER = 0;
     final int SCORE = 1;
     final int NAME_WIDTH = 200;
     private final int UPDATE_MS = 5000;
     TableLayout leaderTable;
     Thread updateThread;
 
     Handler boardHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             String userName = gameData().getUserName();
             String[][] board = (String[][]) msg.getData().getSerializable(
                     "board");
 
             leaderTable.removeAllViews();
             for (int i = 0; i < board.length; i++) {
                 if (!board[i][USER].equals("status")) {
                     TableRow row = new TableRow(getApplicationContext());
 
                     if (i % 2 == 0) {
                         row.setBackgroundResource(R.drawable.black);
                     } else {
                         row.setBackgroundResource(R.drawable.gray);
                     }
                     TextView name = new TextView(getApplicationContext());
                     TextView score = new TextView(getApplicationContext());
 
                     name.setText(board[i][USER]);
                     name.setWidth(NAME_WIDTH);
                     name.setTextSize(25);
                     name.setGravity(Gravity.CENTER_VERTICAL);
                     score.setText(board[i][SCORE]);
                     score.setTextSize(25);
                     score.setGravity(Gravity.CENTER_VERTICAL);
                     if (board[i][USER].equals(userName)) {
                         name.setTextColor(Color.GREEN);
                         score.setTextColor(Color.GREEN);
                     }
 
                     ImageView iv = new ImageView(getBaseContext());
 
                     iv.setImageDrawable(getResources().getDrawable(
                             R.drawable.user));
 
                     /*
                      * LayoutParams p = new LayoutParams(); p.width =
                      * android.view.ViewGroup.LayoutParams.FILL_PARENT;
                      * name.setLayoutParams(p); iv.setLayoutParams(p); //
                      * score.setLayoutParams(p);
                      */
                     iv.setPadding(4, 0, 4, 0);
                     row.addView(iv);
                     row.addView(name);
                     row.addView(score);
                     leaderTable.addView(row);
                 }
             }
         }
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.leaderboard);
 
         final Button okBut = (Button) findViewById(R.id.lead_complete);
         leaderTable = (TableLayout) findViewById(R.id.leader_table);
         // leaderTable.setShrinkAllColumns(true);
         loadTable(gameData().loadLocal());
         if (gameData().loadLocal()) {
             updateTable();
         }
 
         okBut.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Stop I/O thread
            	if (updateThread != null) {
            		updateThread.stop();
            	}
 
                 GameData gd = gameData();
                 gd.setLoadLocal(false);
                 setGameData(gd);
                 startActivity(JOIN_ACTIVITY);
             }
         });
     }
 
     /**
      * loads the leaderboard table with user/score data
      * 
      * @param loadLocal
      *            - grab local data if true, global if false
      */
     private void loadTable(boolean loadLocal) {
         String[][] board = null;
         if (loadLocal) {
             board = gameData().getLeaderboard();
         } else {
             try {
                 board = DistriviaAPI.leaderBoard(gameData());
             } catch (Exception e) {
                 Log.d("Load leaderboard: ", e.getMessage());
                 makeToast("The network is DOWN!");
                 return;
             }
         }
 
         Message msg = new Message();
         Bundle data = new Bundle();
         data.putSerializable("board", board);
         msg.setData(data);
         boardHandler.sendMessage(msg);
     }
 
     private void updateTable() {
         updateThread = new Thread() {
             @Override
             public void run() {
                 SystemClock.sleep(UPDATE_MS);
                 try {
                    while (true) {
                         setGameData(DistriviaAPI.status(gameData()));
                         loadTable(true);
                         SystemClock.sleep(UPDATE_MS);
                     }
                 } catch (Exception e) {
                     makeToast("Service is down, please try again later");
                     return;
                 }
             }
         };
         updateThread.start();
     }
 }
