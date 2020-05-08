 package com.sunpshine.guessnumber;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.NumberPicker;
 import android.widget.NumberPicker.OnValueChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PlayGame extends Activity implements OnClickListener,
 		OnValueChangeListener {
 
 	NumberPicker numberPicker1;
 	NumberPicker numberPicker2;
 	NumberPicker numberPicker3;
 	NumberPicker numberPicker4;
 
 	String nunber1;
 	String nunber2;
 	String nunber3;
 	String nunber4;
 
 	String answer1;
 	String answer2;
 	String answer3;
 	String answer4;
 	
 	String resultA;
 	String resultB;
 
 	Button buGuess;
 
 	TextView guessHistory;
 	String guessing="";
 	String guessHistoryList="";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.play_game);
 
 		setupLayout();
 
 	}
 
 	private void setupLayout() {
 		// TODO Auto-generated method stub
 		
 		
 		while(answer1 == null || answer2 == null || answer3 == null || answer4 == null ||
 				answer1 == answer2 || 
 				answer1 == answer3 || 
 				answer1 == answer4 || 
 				answer2 == answer1 ||
 				answer2 == answer3 || 
 				answer2 == answer4 || 
 				answer3 == answer1 ||
 				answer3 == answer2 ||
 				answer3 == answer4 ||
 				answer4 == answer1 ||
 				answer4 == answer2 ||
 				answer4 == answer3
 				
 				){
 		
 			final Random rr1 = new Random();
 			int rrr1 = rr1.nextInt(9);
 			final Random rr2 = new Random();
 			int rrr2 = rr2.nextInt(9);		
 			final Random rr3 = new Random();
 			int rrr3 = rr3.nextInt(9);
 			final Random rr4 = new Random();
 			int rrr4 = rr4.nextInt(9);
 			answer1 = Integer.toString(rrr1);
 			answer2 = Integer.toString(rrr2);
 			answer3 = Integer.toString(rrr3);
 			answer4 = Integer.toString(rrr4);
 		
			
 
 		}
 		
 		numberPicker1 = (NumberPicker) findViewById(R.id.numberPicker1);
 		numberPicker2 = (NumberPicker) findViewById(R.id.numberPicker2);
 		numberPicker3 = (NumberPicker) findViewById(R.id.numberPicker3);
 		numberPicker4 = (NumberPicker) findViewById(R.id.numberPicker4);
 
 		numberPicker1.setMaxValue(9);
 		numberPicker2.setMaxValue(9);
 		numberPicker3.setMaxValue(9);
 		numberPicker4.setMaxValue(9);
 
 		numberPicker1.setMinValue(0);
 		numberPicker2.setMinValue(0);
 		numberPicker3.setMinValue(0);
 		numberPicker4.setMinValue(0);
 
 		numberPicker1.setValue(0);
 		numberPicker2.setValue(0);
 		numberPicker3.setValue(0);
 		numberPicker4.setValue(0);
 
 		nunber1 = "0";
 		nunber2 = "0";
 		nunber3 = "0";
 		nunber4 = "0";
 
 		numberPicker1.setOnValueChangedListener(this);
 		numberPicker2.setOnValueChangedListener(this);
 		numberPicker3.setOnValueChangedListener(this);
 		numberPicker4.setOnValueChangedListener(this);
 
 		buGuess = (Button) findViewById(R.id.buGuess);
 		buGuess.setOnClickListener(this);
 
 		guessHistory = (TextView) findViewById(R.id.guessHistory);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		switch (v.getId()) {
 
 		case R.id.buGuess:
 
 			if (nunber1 != nunber2 && 
 				nunber1 != nunber3 && 
 				nunber1 != nunber4 && 
 				nunber2 != nunber3 && 
 				nunber2 != nunber4 && 
 				nunber3 != nunber4) {
 
 					checkAnswer(answer1,nunber1,answer2,nunber2,answer3,nunber3,answer4,nunber4);
 					
 			} else {
 
 				Toast.makeText(v.getContext(),
 						"4 numbers must be all different", Toast.LENGTH_LONG)
 						.show();
 			}
 
 			break;
 
 		}
 
 	}
 
 	
 
 	private void checkAnswer(String a1, String n1, String a2,
 			String n2, String a3, String n3, String a4,
 			String n4) {
 		// TODO Auto-generated method stub
 		
 		//check ?A
 		int ar1=0;
 		int ar2=0;
 		int ar3=0;
 		int ar4=0;
 		
 		if(a1.equals(n1)){
 			ar1=1;
 		}
 		if(a2.equals(n2)){
 			ar2=1;
 		}
 		if(a3.equals(n3)){
 			ar3=1;
 		}
 		if(a4.equals(n4)){
 			ar4=1;
 		}
 		
 		int ar=ar1+ar2+ar3+ar4;
 		resultA = Integer.toString(ar);
 			
 		//check ?B
 		int br1=0;
 		int br2=0;
 		int br3=0;
 		int br4=0;
 		
 		if(a1.equals(n2)||a1.equals(n3)||a1.equals(n4)){
 			br1=1;
 		}
 		if(a2.equals(n1)||a2.equals(n3)||a2.equals(n4)){
 			br2=1;
 		}
 		if(a3.equals(n2)||a3.equals(n1)||a3.equals(n4)){
 			br3=1;
 		}
 		if(a4.equals(n2)||a4.equals(n3)||a4.equals(n1)){
 			br4=1;
 		}
 		
 		int br=br1+br2+br3+br4;
 		resultB = Integer.toString(br);
 		
 		if (ar==4){
 			
 			Intent over = new Intent(PlayGame.this, GameOver.class);			
 			startActivity(over);
 			
 			
 		} else {
 			
 			printHistory(resultA,resultB);
 			
 		}
 		
 	}
 
 	private void printHistory(String RA, String RB) {
 		// TODO Auto-generated method stub
 		
 		guessing = 
 				//test
 				answer1 + answer2 + answer3 + answer4 +
 				'\n' 
 				+ nunber1 + nunber2 + nunber3 + nunber4 + " is " + RA + " A " + RB + " B.";
 		
 		guessHistoryList = guessHistoryList + guessing;
 
 		guessHistory.setText(
 				"You had made your guessing... "
 				+ '\n' + guessHistoryList);
 		
 	}
 
 	@Override
 	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
 		// TODO Auto-generated method stub
 
 		switch (picker.getId()) {
 
 		case R.id.numberPicker1:
 
 			nunber1 = Integer.toString(newVal);
 
 			break;
 
 		case R.id.numberPicker2:
 
 			nunber2 = Integer.toString(newVal);
 
 			break;
 
 		case R.id.numberPicker3:
 
 			nunber3 = Integer.toString(newVal);
 
 			break;
 
 		case R.id.numberPicker4:
 
 			nunber4 = Integer.toString(newVal);
 
 			break;
 
 		}
 
 	}
 
 }
