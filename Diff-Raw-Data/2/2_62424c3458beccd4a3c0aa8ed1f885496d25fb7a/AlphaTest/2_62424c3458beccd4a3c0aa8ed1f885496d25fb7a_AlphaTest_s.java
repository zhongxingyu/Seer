 package com.inferno.boozegauge;
 
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class AlphaTest extends SuperActivity implements OnClickListener {           //we only use one OCL, so our activity simply implements onClick()
 	private static Button buttonUL;
 	private static Button buttonUR;
 	private static Button buttonCL;
 	private static Button buttonCR;
 	private static Button buttonLL;
 	private static Button buttonLR;
 	
 	Random generator = new Random();
 	private int currentScore;
 	private int correctButton;
 	private int currentPosition;
 	
 	
 	private Button[] appButtons;
     //This array is probably unnecessary. ASCII values could have also been used
 	private char[] revAlphabet = {'Z','Y','X','W','V','U','T','S','R','Q','P','O','N','M','L','K','J','I','H','G','F','E','D','C','B','A'};
 		
 	
 	/*
      * Function: onCreate(Bundle savedInstanceState)
      * Purpose: Initialize instance variables upon activity creation; inflate activity UI
      * Parameter: Bundle
      * Return: void
      */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_alpha_test);
 		currentScore = 0;
 		currentPosition = 0;
 		buttonUL = (Button)findViewById(R.id.alphaButtonUL);                                        //resolve a reference to each UI button
 		buttonUR = (Button)findViewById(R.id.alphaButtonUR);
 		buttonCL = (Button)findViewById(R.id.alphaButtonCL);
 		buttonCR = (Button)findViewById(R.id.alphaButtonCR);
 		buttonLL = (Button)findViewById(R.id.alphaButtonLL);
 		buttonLR = (Button)findViewById(R.id.alphaButtonLR);
 		appButtons = new Button[] {buttonUL, buttonUR, buttonCL, buttonCR, buttonLL, buttonLR};
 		for(Button b: appButtons) {                                                                 //attach onClickListener to each button
 			b.setOnClickListener(this);
 		}
 		shuffleButtons();
 		
 	}
 
 	/*
      * Function: onResume()
      * Purpose: Readies activity for user input
      * Parameter: void
      * Returns: void
      */
 
 	protected void onResume() {
 		super.onResume();
 		super.builder.setMessage(R.string.alpha_instructions);
 		AlertDialog dialog = super.builder.create();
 		dialog.show();
 		
 	}
 
     /*
      * Function: shuffleButtons()
      * Purpose: Shuffles where the correct letter is displayed, and which letters are on all other buttons
      * Parameter: void
      * Returns: void
      */
 	
 	private void shuffleButtons() {
 		int randpos = currentPosition;
 		correctButton = generator.nextInt(appButtons.length);
 		appButtons[correctButton].setText(Character.toString(revAlphabet[currentPosition]));
 		
 		for (Button b : appButtons) {
 			randpos = currentPosition;			
 			if(b != appButtons[correctButton]) {
 				while(randpos == currentPosition)
 					randpos = generator.nextInt(revAlphabet.length);
 				b.setText(Character.toString(revAlphabet[randpos]));
 			}
 				
 		}
 	}
 
 /*	@Override                                                   //Fairly sure I don't need this, left it in for later
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.alpha_test, menu);
 		return true;
 	}*/
 	
 	/*
      * Function: calculateScore()
      * Purpose: Aggregate local score into global score
      * Parameter: void
      * Returns: void
      */
 
 	@Override
 	public void calculateScore() {
		Globals.score += 100 * (currentScore / revAlphabet.length);		
 	}
 	
     /*
      * Function: onKeyDown()
      * Purpose: Overrides Activity.onKeyDown(), handles back button presses
      * Parameter: void
      * Returns: void
      */
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(keyCode == KeyEvent.KEYCODE_BACK) {
 			finish();
 		}		
 		return true;
 	}
 
     /*
      * Function: onClick
      * Purpose: Handles input from main UI buttons.
      *      Checks if correct button pressed, and informs the user of this result.
      *      If correct, increments score and shuffles buttons.
      *      If incorrect, decrements score.
      * Parameter: View v, a reference to button pressed
      * Returns: void
      */
 
 	@Override
 	public void onClick(final View v) {
 		new Thread (new Runnable() {                                    //All click handling is in a separate thread to avoid bogging down the UI thread
 			public void run() {
 				if((Button)v == appButtons[correctButton]) {
 					/*v.post(new Runnable() {                             //We can only touch UI elements from UI thread, so we post an anonymous Runnable
 						public void run() {                             //which is executed on the UI thread in order to change button text
 							((Button) v).setText("Correct!");
 						}
 					});
 					android.os.SystemClock.sleep(500);*/                  //Delay so user can see result
 					currentScore++;
 					if(currentPosition != 25) {                         //Make sure we don't run past the end of the alphabet
 						currentPosition++;					
 						runOnUiThread(new Runnable() {                  //ShuffleButtons touches UI elements, so we run it on the UI thread
 							public void run() {
 								shuffleButtons();
 							}
 						});
 					}
 					else
 						endTest();
 				}
 				else {
 					final CharSequence cs = ((Button) v).getText();     //Same logic as above
 					v.post(new Runnable() {
 						public void run() {
 							((Button) v).setText("Incorrect!");
 						}
 					});
 					android.os.SystemClock.sleep(500);
 					currentScore--;
 					v.post(new Runnable() {
 						public void run() {
 							((Button) v).setText(cs);
 						}
 					});
 				}
 			}
 		}).start();
 	}
 }
