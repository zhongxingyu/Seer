 package com.bigtheta.ragedice;
 
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private int[] mDiceImgs;
 	private int mNumPlayers;
 	private int mPlayerNum;
 	private DiceRollDAO mDiceRollDAO;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         mDiceImgs = new int[6];
         mDiceImgs[0] = R.drawable.alea_1_transface_colbg;
         mDiceImgs[1] = R.drawable.alea_2_transface_colbg;
         mDiceImgs[2] = R.drawable.alea_3_transface_colbg;
         mDiceImgs[3] = R.drawable.alea_4_transface_colbg;
         mDiceImgs[4] = R.drawable.alea_5_transface_colbg;
         mDiceImgs[5] = R.drawable.alea_6_transface_colbg;
         mNumPlayers = 2;
         mPlayerNum = 0;
     	ImageView red_die = (ImageView)findViewById(R.id.red_die);
     	red_die.setBackgroundColor(0xFFFF0000);
     	ImageView yellow_die = (ImageView)findViewById(R.id.yellow_die);
     	yellow_die.setBackgroundColor(0xFFFFFF00);
 
     	mDiceRollDAO = new DiceRollDAO(this);
     	
     	View mainView = (View)findViewById(R.id.activity_main_view);
     	mainView.setBackgroundColor(0xFF818181);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mDiceRollDAO.open();
 		/*
 	    List<DiceRoll> rolls = mDiceRollDAO.getAllDiceRolls();
 
 	    // Use the SimpleCursorAdapter to show the
 	    // elements in a ListView
 	    // XXX
 	    ArrayAdapter<DiceRoll> adapter = new ArrayAdapter<DiceRoll>(this,
 	        android.R.layout.simple_list_item_1, rolls);
 	    setListAdapter(adapter);
 		*/
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mDiceRollDAO.close();
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
    protected int rollSingleDie(ImageView die) {
     	Random rand = new Random();
     	int die_select = rand.nextInt(6);
     	die.setImageResource(mDiceImgs[die_select]);
     	return die_select + 1;
     }
     
    protected void nextPlayer(Boolean goForward) {
     	if (mNumPlayers > 1 && goForward) {
     		mPlayerNum %= mNumPlayers;
     		mPlayerNum++;
     	} else {
     		mPlayerNum--;
     		if (mPlayerNum == 0) {
     			mPlayerNum += mNumPlayers;
     		}
     	}
 		TextView player = (TextView)findViewById(R.id.player_number);
 		player.setText("Player " + Integer.toString(mPlayerNum));
     }
     
    protected void showTotals() {
     	String resultsStr = "";
     	for (int i = 2; i <= 12; i++) {
     		resultsStr += "Num " + Integer.toString(i) +
     					  " : " + Integer.toString(mDiceRollDAO.getCountForRoll(i)) + "\n";
     	}
     	resultsStr += "Prob:" + Double.toString(mDiceRollDAO.calculateKSProbability());
     	TextView results_view = (TextView)findViewById(R.id.dice_results);
     	results_view.setText(resultsStr);
     }
     
     public void resetDiceRolls(View view) {
     	mDiceRollDAO.deleteAllDiceRolls();
     	mPlayerNum = 1;
     	showTotals();
     }
     
     public void undoDiceRoll(View view) {
     	// TODO: Check if the rolls are empty. This crashes otherwise.
     	mDiceRollDAO.deleteDiceRoll(mDiceRollDAO.getLastDiceRoll());
     	nextPlayer(false);
 		showTotals();
     }
     	
     public void rollDice(View view) {
     	int die_roll_result = 0;
     	nextPlayer(true);
     	ImageView red_die = (ImageView)findViewById(R.id.red_die);
     	ImageView yellow_die = (ImageView)findViewById(R.id.yellow_die);
     	die_roll_result += rollSingleDie(red_die);
     	die_roll_result += rollSingleDie(yellow_die);
     	mDiceRollDAO.createDiceRoll(die_roll_result);
     	showTotals();
     }
 }
