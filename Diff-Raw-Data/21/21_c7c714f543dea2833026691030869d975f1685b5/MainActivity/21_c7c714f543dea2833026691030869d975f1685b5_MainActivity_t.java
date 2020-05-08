 package edu.umich.emjansen.tictactoetwo;
 
 import android.os.Bundle;
 import android.R.bool;
 import android.R.integer;
 import android.app.Activity;
 import android.util.Log;
import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.GridLayout;
 import android.widget.TextView;
 //import askinterfaces.scanninglibrary.ScanningController;
 //import askinterfaces.tic_tac_toe.R;
import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	boolean mTurn;
 	GridLayout mGridLayout;
 	
 	public static void Log(String msg) {
 		Log.v("MainActivity", msg);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mGridLayout = (GridLayout)findViewById(R.id.TicTacToeGrid);
 		
 		OnClickListener clickListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.v("MainActivity", "Button View");
 				Button buttonView = (Button)v;
				System.out.println("Button " + buttonView.getText().toString() + " pressed");
 				if (buttonView.getText().toString().isEmpty()) {
 					if (mTurn) {
 						buttonView.setText("X");
 					} else {
 						buttonView.setText("O");
 					}
 					mTurn = !mTurn;
 				}
				else {
					Toast invalidmoveToast = Toast.makeText(MainActivity.this, "Invalid Move", Toast.LENGTH_SHORT);
					invalidmoveToast.setGravity(Gravity.CENTER, 0, 0);
					invalidmoveToast.show();
				}
 				if(winner(mGridLayout))
 				{
 					TextView textView = (TextView) findViewById(R.id.PlayerTurnText);
 					textView.setText("Winner!!");
					Toast toast = Toast.makeText(MainActivity.this, "Winner!!", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
 				}
 			}
 		};
 		
 		for (int i = 0; i < mGridLayout.getChildCount(); i++) {
 			mGridLayout.getChildAt(i).setOnClickListener(clickListener);
 		}
 		
 		Button newGameButton = (Button)findViewById(R.id.newGameButton);
 		OnClickListener newGameListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.v("MainActivity", "Button View");
 				Button buttonView = (Button)v;
				System.out.println("Clear grid");
 				clearGrid(mGridLayout);
 			}
 		};
 		newGameButton.setOnClickListener(newGameListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	private boolean winner(GridLayout mGridLayout)
 	{
 		String myTurnSymbol;
 		Button buttonView;
 		int numColumns = mGridLayout.getColumnCount();
 		int numRows = mGridLayout.getRowCount();
 		if(mTurn) //mTurn has already been flipped
 			myTurnSymbol = "O";
 		else if(!mTurn)
 			myTurnSymbol = "X";
 		else
 			myTurnSymbol = " ";
 		//check all rows
 /*		for(int a=0; a < numRows; a++)
 		{
 			int beginChild = a*numColumns;
 			buttonView = (Button) mGridLayout.getChildAt(beginChild);
 			potentialWinner = buttonView.getText().toString();
 			int bound = beginChild+numColumns;
 			for(int b=beginChild; b < bound; b++)
 			{
 				buttonView = (Button) mGridLayout.getChildAt(b);
 				if(potentialWinner != buttonView.getText())
 					break;
 				if(b == bound-1){
 	    			//report win for potentialWinner
 					return true;
 	    		}
 			}
 		}
 		*/
 		for (int i = 0; i < mGridLayout.getChildCount(); ++i) {
 			System.out.println("Button text at " + i + ": " + ((Button)mGridLayout.getChildAt(i)).getText().toString());
 		}
 		System.out.println("Meow" + ((Button) mGridLayout.getChildAt(0)).getText().toString());
 		if(((Button)findViewById(R.id.button0)).getText().toString()
 				== ((Button)findViewById(R.id.button1)).getText().toString()
 				&& ((Button)findViewById(R.id.button0)).getText().toString()
 				== ((Button)findViewById(R.id.button2)).getText().toString()
 				&& ((Button)findViewById(R.id.button0)).getText().toString()
 				== myTurnSymbol)
 			return true;
 		if(((Button)findViewById(R.id.button3)).getText().toString()
 				== ((Button)findViewById(R.id.button4)).getText().toString()
 				&& ((Button)findViewById(R.id.button3)).getText().toString()
 				== ((Button)findViewById(R.id.button5)).getText().toString()
 				&& ((Button)findViewById(R.id.button3)).getText().toString()
 				== myTurnSymbol)
 			return true;
 		if(((Button)findViewById(R.id.button6)).getText().toString()
 				== ((Button)findViewById(R.id.button7)).getText().toString()
 				&& ((Button)findViewById(R.id.button6)).getText().toString()
 				== ((Button)findViewById(R.id.button8)).getText().toString()
 				&& ((Button)findViewById(R.id.button6)).getText().toString()
 				== myTurnSymbol)
 			return true;
 			
 		//check all columns
 /*		for(int a=0; a < numColumns; a++)
 		{
 			buttonView = (Button) mGridLayout.getChildAt(a);
 			potentialWinner = buttonView.getText().toString();
 			for(int b=numColumns; b < mGridLayout.getRowCount(); b+=numColumns)
 			{
 				buttonView = (Button) mGridLayout.getChildAt(b);
 				if(potentialWinner != buttonView.getText())
 					break;
 				if(b >= mGridLayout.getRowCount()*(numColumns-1)){
 	    			//report win for potentialWinner
 					return true;
 	    		}
 			}
 		}
 		*/
 		
 		if(((Button)findViewById(R.id.button0)).getText().toString()
 				== ((Button)findViewById(R.id.button3)).getText().toString()
 				&& ((Button)findViewById(R.id.button0)).getText().toString()
 				== ((Button)findViewById(R.id.button6)).getText().toString()
 				&& ((Button)findViewById(R.id.button0)).getText().toString()
 				== myTurnSymbol)
 			return true;
 		if(((Button)findViewById(R.id.button1)).getText().toString()
 				== ((Button)findViewById(R.id.button4)).getText().toString()
 				&& ((Button)findViewById(R.id.button1)).getText().toString()
 				== ((Button)findViewById(R.id.button7)).getText().toString()
 				&& ((Button)findViewById(R.id.button1)).getText().toString()
 				== myTurnSymbol)
 			return true;
 		if(((Button)findViewById(R.id.button2)).getText().toString()
 				== ((Button)findViewById(R.id.button5)).getText().toString()
 				&& ((Button)findViewById(R.id.button2)).getText().toString()
 				== ((Button)findViewById(R.id.button8)).getText().toString()
 				&& ((Button)findViewById(R.id.button2)).getText().toString()
 				== myTurnSymbol)
 			return true;
 
 		//check right diagonal
 /*		buttonView = (Button) mGridLayout.getChildAt(0);
 		potentialWinner = buttonView.getText().toString();
 		for(int a=0; a < mGridLayout.getChildCount(); a+=(numColumns+1))
 		{
 			buttonView = (Button) mGridLayout.getChildAt(a);
 			if(potentialWinner != buttonView.getText())
 			{
 				break;
 			}
 			a += numColumns;
 			if(a >= mGridLayout.getChildCount())
 				return true;
 		}
 		*/
 		
 		if(((Button)findViewById(R.id.button0)).getText().toString()
 				== ((Button)findViewById(R.id.button4)).getText().toString()
 				&& ((Button)findViewById(R.id.button0)).getText().toString()
 				== ((Button)findViewById(R.id.button8)).getText().toString()
 				&& ((Button)findViewById(R.id.button0)).getText().toString()
 				== myTurnSymbol)
 			return true;
 		
 		//check left diagonal
 /*		buttonView = (Button) mGridLayout.getChildAt(mGridLayout.getColumnCount());
 		potentialWinner = buttonView.getText().toString();
 		for(int a=numColumns; a < mGridLayout.getChildCount(); a+=(numColumns-1))
 		{
 			buttonView = (Button) mGridLayout.getChildAt(a);
 			if(potentialWinner != buttonView.getText())
 			{
 				break;
 			}
 			if(a >= (numColumns*numRows-numColumns))
 				return true;
 		}
 		*/
 		
 		if(((Button)findViewById(R.id.button2)).getText().toString()
 				== ((Button)findViewById(R.id.button4)).getText().toString()
 				&& ((Button)findViewById(R.id.button2)).getText().toString()
 				== ((Button)findViewById(R.id.button6)).getText().toString()
 				&& ((Button)findViewById(R.id.button2)).getText().toString()
 				== myTurnSymbol)
 			return true;
 		
 		return false;
 	}
 
 	private void clearGrid(GridLayout mGridLayout)
 	{
 		for(int a=0; a < mGridLayout.getChildCount()-1; a++)
 		{
 			((Button) mGridLayout.getChildAt(a)).setText("");
 		}
 		TextView textView = (TextView) findViewById(R.id.PlayerTurnText);
 		textView.setText("Next Player's Turn");
 	}
 }
