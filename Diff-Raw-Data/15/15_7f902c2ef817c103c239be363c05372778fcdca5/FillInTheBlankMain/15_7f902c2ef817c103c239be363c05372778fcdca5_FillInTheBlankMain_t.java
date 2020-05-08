 package team.kyb.extragames;
 
 import team.kyb.MainActivity;
 import team.kyb.R;
 import team.kyb.animationAPI.AnimationHelper;
 import team.kyb.animationAPI.LoseEffect;
 import team.kyb.animationAPI.WinEffect;
 import team.kyb.database.DatabaseConnector;
 import team.kyb.database.EditScripture;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class FillInTheBlankMain extends Activity {
 	DatabaseConnector database = new DatabaseConnector(this);
 
 	private AnimationHelper animationHelper = new AnimationHelper();
 
 	private Button mNewGame, mCheckGame;
 
 	private TextView mWord1, mWord2, mWord3, mWord4, mScripture, mStatus;
 
 	private FillInTheBlankGame mGame;
 
 	private EditText mOrder1, mOrder2, mOrder3, mOrder4;
 
 	private String scripture;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.filling_the_blank);
 
 		mNewGame = (Button) findViewById(R.id.button2);
 		mNewGame.setOnClickListener(mClickNewGame);
 
 		mCheckGame = (Button) findViewById(R.id.button1);
 		mCheckGame.setOnClickListener(mClickCheck);
 
 		mWord1 = (TextView) findViewById(R.id.word1);
 		mWord2 = (TextView) findViewById(R.id.word2);
 		mWord3 = (TextView) findViewById(R.id.word3);
 		mWord4 = (TextView) findViewById(R.id.word4);
 
 		mStatus = (TextView) findViewById(R.id.statusFill);
 
 		mScripture = (TextView) findViewById(R.id.scripture_fill);
 
 		mOrder1 = (EditText) findViewById(R.id.order1);
 		mOrder2 = (EditText) findViewById(R.id.order2);
 		mOrder3 = (EditText) findViewById(R.id.order3);
 		mOrder4 = (EditText) findViewById(R.id.order4);
 
 		database.open();
 		ScriptureForGameHelper scriptureHelper = new ScriptureForGameHelper(
 				database.getRandomScriptureForGame());
 		database.close();
 
 		scripture = scriptureHelper.getPassage();
 
 		mGame = new FillInTheBlankGame(scripture);
 		
 		mScripture.setText(mGame.getMissingString() + " "
 				+ scriptureHelper.getBook() + " " + scriptureHelper.getChapter() + " : "
 				+ scriptureHelper.getVerse());
 				
 		mWord1.setText(mGame.getWords()[0]);
 		mWord2.setText(mGame.getWords()[1]);
 		mWord3.setText(mGame.getWords()[2]);
 		mWord4.setText(mGame.getWords()[3]);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.hangman_menu, menu);
 		return true;
 	}
 
 	// handle choice from options menu
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// create a new Intent to launch
 		Intent exitHangman = new Intent(this, MainActivity.class);
 		startActivity(exitHangman);
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void loseEffect() {
 		Intent intent = new Intent(this, LoseEffect.class);
 		startActivity(intent);
 		mNewGame.setVisibility(1);
 	}
 
 	public void winEffect() {
 		Intent intent = new Intent(this, WinEffect.class);
 		startActivity(intent);
 		mNewGame.setVisibility(1);
 	}
 
 	private OnClickListener mClickCheck = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			
 			if(mOrder1.getText().length() == 0 || mOrder2.getText().length() == 0 
 					|| mOrder3.getText().length() == 0 || mOrder4.getText().length() == 0 ){
 				AlertDialog.Builder vFillBlank = new AlertDialog.Builder(FillInTheBlankMain.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vFillBlank.setTitle(R.string.vFillBlankTitle);
 				vFillBlank.setMessage(R.string.vFillBlankMessage);
 				vFillBlank.setPositiveButton(R.string.vButton, null);
 				vFillBlank.show();
 				
 			} else if(mOrder1.getText().length() > 1 || mOrder2.getText().length() > 1 
 					|| mOrder3.getText().length() > 1 || mOrder4.getText().length() > 1 ){
 				AlertDialog.Builder vFillBlank = new AlertDialog.Builder(FillInTheBlankMain.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vFillBlank.setTitle(R.string.vFillBlankTitle);
 				vFillBlank.setMessage(R.string.vFillBlankMessage);
 				vFillBlank.setPositiveButton(R.string.vButton, null);
 				vFillBlank.show();
 				
 			} else if(Integer.parseInt(mOrder1.getText().toString()) > 4 
 					|| Integer.parseInt(mOrder2.getText().toString()) > 4
 					|| Integer.parseInt(mOrder3.getText().toString()) > 4			
 					|| Integer.parseInt(mOrder4.getText().toString()) > 4 ){
 				AlertDialog.Builder vFillBlank = new AlertDialog.Builder(FillInTheBlankMain.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vFillBlank.setTitle(R.string.vFillBlankTitle);
 				vFillBlank.setMessage(R.string.vFillBlankMessage);
 				vFillBlank.setPositiveButton(R.string.vButton, null);
 				vFillBlank.show();
 				
 			} else if(Integer.parseInt(mOrder1.getText().toString()) < 1 
 					|| Integer.parseInt(mOrder2.getText().toString()) < 1
 					|| Integer.parseInt(mOrder3.getText().toString()) < 1			
 					|| Integer.parseInt(mOrder4.getText().toString()) < 1 ){
 				AlertDialog.Builder vFillBlank = new AlertDialog.Builder(FillInTheBlankMain.this);
 				
 				//set dialog title and message, and provide the button to dismiss
 				vFillBlank.setTitle(R.string.vFillBlankTitle);
 				vFillBlank.setMessage(R.string.vFillBlankMessage);
 				vFillBlank.setPositiveButton(R.string.vButton, null);
 				vFillBlank.show();
 				
 			} else {
 				
 				mScripture.setText(scripture);
				mCheckGame.setEnabled(false);
 
 				mNewGame.setVisibility(0);				
 				
 				int[] input = new int[4];
 				input[0] = Integer.parseInt(mOrder1.getText().toString());
 				input[1] = Integer.parseInt(mOrder2.getText().toString());
 				input[2] = Integer.parseInt(mOrder3.getText().toString());
 				input[3] = Integer.parseInt(mOrder4.getText().toString());
 				String inputString = getOrderString(input);
 				String orderString = getOrderString(mGame.getOrder());
 
 				if (inputString.equals(orderString)) {
 					mStatus.setText("You won");
 					winEffect();
 				} else {
 					mStatus.setText("You lose");
 					loseEffect();
 				}				
 				
 				
 			} // end of if validation number
 	
 		}
 	};
 
 	private OnClickListener mClickNewGame = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			finish();
 			startActivity(getIntent());
 		}
 	};
 	
 	private String getOrderString (int[] input) {
 		return input[0] + " " + input[1] + " "
 				+ input[2] + " " + input[3];
 	}
 
 }
