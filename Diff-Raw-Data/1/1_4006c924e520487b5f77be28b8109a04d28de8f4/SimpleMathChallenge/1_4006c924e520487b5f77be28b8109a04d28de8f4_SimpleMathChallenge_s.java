 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.challenge;
 
 import java.util.Random;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.activity.ChallengeActivity;
 import android.content.Context;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 /**
  * A Class for randomly generating simple arithmetic challenges.
  * 
  * @author Laszlo Sall Vesselenyi, Danny Lam, Johan Hasselqvist
  */
 
 public class SimpleMathChallenge implements Challenge {
 
 	private Random random = new Random();
 	private int operand1 = 0;
 	private int operand2 = 0;
 	private int operation = 0;
 	private int result = 0;
 
 	public void runChallenge() {
 		nextOp();
 		nextInts();
 	}
 
 	/**
 	 * The random interval of the operands 
 	 * case 0: addition 
 	 * case 1: subtraction
 	 * case 2: multiplication 
 	 * case 3: division
 	 */
 
 	private void nextInts() {
 		switch (operation) {
 		case 0:
 			operand1 = random.nextInt(99) + 1;
 			operand2 = random.nextInt(99) + 1;
 			result = operand1 + operand2;
 			break;
 		case 1:
 			operand1 = random.nextInt(99) + 1;
 			operand2 = random.nextInt(99) + 1;
 			result = operand1 - operand2;
 			break;
 		case 2:
 			operand1 = random.nextInt(8) + 2;
 			operand2 = random.nextInt(8) + 2;
 			result = operand1 * operand2;
 			break;
 		case 3:
 			result = random.nextInt(8) + 2;
 			operand2 = random.nextInt(8) + 2;
 			operand1 = result * operand2;
 			break;
 		}
 	}
 
 	// What the next operation will be, add/sub/mul/div
 	private void nextOp() {
 		operation = random.nextInt(4);
 	}
 
 	// Get the result
 	public int getResult() {
 		return result;
 	}
 
 	// Get the calculation between the operands in strings
 	public String getCalculation() {
 		if (operation == 0) {
 			return operand1 + " + " + operand2;
 		} else if (operation == 1) {
 			return operand1 + " - " + operand2;
 		} else if (operation == 2) {
 			return operand1 + " * " + operand2;
 		} else {
 			return operand1 + " / " + operand2;
 		}
 	}
 
 	/**
 	 * An activity with the math layout; 
 	 * TextView, EditText and an answer-Button.
 	 * 
 	 */
 	
 	@Override
 	public void start(final ChallengeActivity activity) {
 		activity.setContentView(R.layout.alarm_challenge_math);
 		runChallenge();
 
 		final TextView userText = (TextView) activity
 				.findViewById(R.id.questionField);
 
 		userText.setText(getCalculation());
 
 		
 		final EditText editText = (EditText) activity
 				.findViewById(R.id.answerField);
 		
 		
 		editText.setOnEditorActionListener(new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				boolean handled = false;
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					handleAnswer(editText, activity, userText);
 					handled = true;
 				}
 				return handled;
 			}
 		});
 
 		Button answerButton = (Button) activity.findViewById(R.id.btnAnswer);
 		answerButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				handleAnswer(editText, activity, userText);
 			}
 		});
 		
 		// make the keyboard appear.
 		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 				
 	}
 	
 	
 	/**
 	 * Handles what will happen when you answer
 	 */
 
 	private void handleAnswer(final EditText editText,
 			final ChallengeActivity activity, final TextView userText) {
 		boolean correctAnswer = false;
 		try {
 			if (Integer.parseInt(editText.getText().toString()) == getResult()) {
 				activity.complete();
 				correctAnswer = true;
 				Toast.makeText(activity.getBaseContext(), "Alarm deactivated!",
 						Toast.LENGTH_SHORT).show();
 			}
 		} catch (NumberFormatException e) {
 			// Handles exception when the user answer with empty strings
 		}
 		if (!correctAnswer) {
 			Toast.makeText(activity.getBaseContext(), "Wrong answer!",
 					Toast.LENGTH_SHORT).show();
 			runChallenge();
 			userText.setText(getCalculation());
 			editText.setText("");
 		}
 	}
 }
