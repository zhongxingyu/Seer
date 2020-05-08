 package com.example.rnpfsc;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class ResultActivity extends Activity {
 
 	public static final String YOU = "You are the winner!!!";
 	public static final String THEM = "He/She is the winner!!!";
 	public static final String TIE = "Tie!!!";
 	public static final String ERROR = "Error!!!";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_result);
 
 		TextView you = (TextView) findViewById(R.id.you);
 		TextView them = (TextView) findViewById(R.id.them);
 		TextView winner = (TextView) findViewById(R.id.winner);
 
 		Intent intent = getIntent();
 		String yourSelection = intent.getStringExtra(MainActivity.SEND_MESSAGE);
 		you.setText("You selected " + yourSelection);
 
 		// TODO read the other person's NFC instead
 		double random = Math.random();
 		String theirSelection;
 		if (random < 0.33) {
 			theirSelection = "Rock";
 		} else if (random > 0.33 && random < 0.67) {
 			theirSelection = "Paper";
 		} else {
 			theirSelection = "Scissors";
 		}
		them.setText("He/She selected " + theirSelection);
 
 		winner.setText(calculateWinner(yourSelection, theirSelection));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_result, menu);
 		return true;
 	}
 
 	private String calculateWinner(String you, String them) {
 		if (you.equalsIgnoreCase(them)) {
 			return TIE;
 		}
 
 		if (you.equalsIgnoreCase("rock")) {
 			if (them.equalsIgnoreCase("scissors")) {
 				return YOU;
 			} else if (them.equalsIgnoreCase("paper")) {
 				return THEM;
 			} else {
 				return ERROR;
 			}
 
 		} else if (you.equalsIgnoreCase("paper")) {
 			if (them.equalsIgnoreCase("scissors")) {
 				return THEM;
 			} else if (them.equalsIgnoreCase("rock")) {
 				return YOU;
 			} else {
 				return ERROR;
 			}
 
 		} else if (you.equalsIgnoreCase("scissors")) {
 			if (them.equalsIgnoreCase("rock")) {
 				return THEM;
 			} else if (them.equalsIgnoreCase("paper")) {
 				return YOU;
 			} else {
 				return ERROR;
 			}
 
 		} else {
 			return "Error!!!";
 		}
 	}
 }
