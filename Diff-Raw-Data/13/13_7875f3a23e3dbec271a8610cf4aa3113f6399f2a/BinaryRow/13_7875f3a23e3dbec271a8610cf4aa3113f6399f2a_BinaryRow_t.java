 package edu.csumb.ideasofmarch.codecruncher;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.lang.Integer;
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class BinaryRow implements GameRow {
 	private BinaryRow instance = null; 
 	static int rowCount = 0;
 	private Activity ctx;
 	private LinearLayout pll,ll;
 	private int randNum, numDigits;
 	private TextView question;
 	private ToggleButton t1;
 	private int ANSWER_TYPE; // 0 - Decimal ; 1 - Binary ; 2 - Hexadecimal
 	private List<ToggleButton> buttonList = new ArrayList<ToggleButton>();
 	
 	public BinaryRow(LinearLayout aLayout, Activity aContext, int Digits, int answerType) {
 		instance = this;
 		ctx = aContext;
 		pll = aLayout;
 		numDigits = Digits;
 		ANSWER_TYPE = answerType;
 		randNum = (int) Math.floor(Math.random() * (Math.pow(2,numDigits)));	
 		question = new Button(aContext);
 		
 		int resID = (int) Math.floor(Math.random()*3);
 		if(resID == 0) {
 			resID = R.drawable.redbutton;
 		} else if(resID == 1) {
 			resID = R.drawable.yellowbutton;
 		}else {
 			resID = R.drawable.bluebutton;
 		}
 		
 		for (int i = 0; i < numDigits; i++) {
 			t1 = new ToggleButton(aContext);
 			t1.setBackgroundResource(resID);
 			t1.setText(R.string.binaryInput); 
 			t1.setTextOff("0");
 			t1.setTextOn("1");
 			t1.setTextColor(Color.WHITE);
 			t1.setChecked(false);
 			buttonList.add(t1);
 		}
 		
 		ll = new LinearLayout(aContext);
 		for (int i = 0; i < numDigits; i++) {
 			ll.addView(buttonList.get(i));
 		}
 		
 		if (ANSWER_TYPE == 0) // Decimal
 			question.setText("" + randNum);
 		if (ANSWER_TYPE == 1) // Binary
 			question.setText(Integer.toBinaryString(randNum));
 		if (ANSWER_TYPE == 2) // Hexadecimal
 			question.setText(Integer.toHexString(randNum));
 		
 		question.setTextColor(Color.WHITE);
 		question.setTextSize(20);
 		question.setBackgroundResource(R.drawable.graybutton);
 		question.setOnClickListener(new SubmitButtonListener());
 		ll.addView(question);
 	}
 
 	@Override
 	public boolean putNewRow() {
 		if(rowCount < 8){
 			rowCount++;
 			pll.addView(ll);
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	@Override
 	public boolean checkProblem() {
 		String answer = getGuess();
 		int intAnswer = Integer.parseInt(answer, 2);
 		
 		if(intAnswer == randNum){
 			// Sound will play in the BinaryToDecimal and BinaryToHex activity files
 			removeRow();
 			return true;
 		}else{
 			question.setTextColor(Color.RED);
 			return false;
 		}
 	}
 	
     public String getGuess() {
     	String binaryInput = "";
     	
     	for (int i = 0; i < numDigits; i++){
 			if (buttonList.get(i).isChecked()) {
 				binaryInput += "1";
 			} else {
 				binaryInput += "0";
 			}
     	}
     	    	
     	return binaryInput;
     }
 
 	@Override
 	public boolean removeRow() {
 		if(rowCount > 0){
 			ll.setVisibility(View.GONE);
 			((ViewGroup) ll.getParent()).removeView(ll);
 			rowCount--;
 			return true;
 		}else{
 			randNum = (int) Math.floor(Math.random()*(Math.pow(2,numDigits)));
 			question.setText("" + randNum);
 		return false;
 		}
 	}
 	
 	public int score(){
 		return randNum;
 	}
 	
 	public void resetRowCount(){
 		rowCount = 0;
 	}
 	
 	private class SubmitButtonListener implements OnClickListener {
 		
 		public void onClick(View view) {
 			if(checkProblem()){
 				 if(numDigits == 4){
					 if (ANSWER_TYPE == 0) 
						 ((BinaryToDecimal) ctx).correctAnswer(instance);
					 if (ANSWER_TYPE == 2)
						 ((BinaryToHex) ctx).correctAnswer(instance);
 				 }else{
					 if (ANSWER_TYPE == 0)
						 ((BinaryToDecimalHard) ctx).correctAnswer(instance);
					 if (ANSWER_TYPE == 2)
						 ((BinaryToHexHard) ctx).correctAnswer(instance);
 				 }
 			}
 		}	
 	}
 
 }
 
