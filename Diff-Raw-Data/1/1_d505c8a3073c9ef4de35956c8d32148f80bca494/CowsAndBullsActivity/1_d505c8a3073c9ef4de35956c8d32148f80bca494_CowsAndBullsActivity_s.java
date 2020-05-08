 package com.sampleapp.cowsandbulls;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class CowsAndBullsActivity extends Activity {
 	private static final int INPUT_LENGTH_MISMATCH = 1;
 	private static final int INPUT_CONTAINS_DUPLICATES = 2;
 	private static final int INPUT_MATCHES_ANSWER = 3;
 	private static final String[] words4Len = {"oxen","plus","axes","scad","fade","dark","crap","beak","easy","glue","hose","icon","joke","lady","mast","name",
 						"pear","rues","suit","toad","undo","vice","ward","your"};
 	private String[] words5Len = {"scalp","aunty","blurt","bumpy","cajon","crypt","dewax","frisk","epoxy","equid","gnarl","helix","herbs","jawed","knows","lurch","magot"};
 	private String currentWord;
 	private int wordLength;
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Intent currIntent = this.getIntent();
         wordLength = Integer.parseInt(currIntent.getStringExtra(HomePageActivity.EXTRA_WORDLEN));
         
         setCurrentWord();
         
         EditText inputText = (EditText)findViewById(R.id.txtInput);
         InputFilter[] filterArr = new InputFilter[1];
         filterArr[0] = new InputFilter.LengthFilter(wordLength);
         inputText.setFilters(filterArr);
         
         inputText.setOnEditorActionListener(new OnEditorActionListener() {
 			
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				// TODO Auto-generated method stub
 				if(event.getAction() == EditorInfo.IME_NULL && actionId == KeyEvent.ACTION_DOWN) {
 					checkTheWordEntered(v);
 					return true;
 				}
 				return false;
 			}
 		});
     }
     
     private void setCurrentWord() {
     	Random rand = new Random();
     	int wordsArrayLength;
     	switch(wordLength) {
     	case 4:
     		wordsArrayLength = words4Len.length;
             currentWord = words4Len[rand.nextInt(wordsArrayLength)];
     		break;
     	case 5:
     		wordsArrayLength = words5Len.length;
             currentWord = words5Len[rand.nextInt(wordsArrayLength)];
     		break;
     	}
     	
     }
     
     
     @Override
     protected Dialog onCreateDialog(int id) {
     	// TODO Auto-generated method stub
     	AlertDialog dialog = null;
     	AlertDialog.Builder builder = null;
     	switch(id) {
     	case INPUT_LENGTH_MISMATCH:
     		builder = new AlertDialog.Builder(this);
     		builder.setMessage("Input string length should be exactly " + wordLength + " letters.")
     				.setCancelable(false)
     				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							dialog.cancel();
 						}
 					});
     		dialog = builder.create();
     		break;
     	case INPUT_CONTAINS_DUPLICATES:
     		builder = new AlertDialog.Builder(this);
     		builder.setMessage("Letters in the input string should not appear more than once.")
     				.setCancelable(false)
     				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							dialog.cancel();
 						}
 					});
     		dialog = builder.create();
     		break;
     	case INPUT_MATCHES_ANSWER:
     		builder = new AlertDialog.Builder(this);
     		builder.setMessage("You have guesses the word correctly. Want to play again?")
     				.setCancelable(false)
     				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							prepareForNewGame();
 						}
 					})
 					.setNegativeButton("No", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							dialog.cancel();
 						}
 					});
     		break;
     	default:
     		break;
     	}
     	return dialog;
     }
     
     private void prepareForNewGame() {
     	setCurrentWord();
     	EditText inputText = (EditText)findViewById(R.id.txtInput);
     	inputText.setText("");
     	TextView tView = (TextView)findViewById(R.id.resultPane);
     	tView.setText("");
     }
     
     public void checkTheWordEntered(View v) {
     	EditText inputTextField = (EditText)findViewById(R.id.txtInput);
     	String txt = inputTextField.getText().toString();
     	if(txt.length() != wordLength) {
     		onCreateDialog(INPUT_LENGTH_MISMATCH).show();
     	}
     	else if(checkIfDuplicatesExist(txt)) {
     		onCreateDialog(INPUT_CONTAINS_DUPLICATES).show();
     	}
     	else {
     		ArrayList<Integer> count = checkCowsAndBullsCount(txt);
     		int bullsCount = count.get(0);
     		if(bullsCount == wordLength) {
     			onCreateDialog(INPUT_MATCHES_ANSWER).show();
     			return;
     		}
     		TextView resultPane = (TextView)findViewById(R.id.resultPane);
     		String str = txt+ " : Cows-"+count.get(1).toString()+" Bulls-"+count.get(0).toString();
     		resultPane.setText(str + "\n"+ resultPane.getText());
     		inputTextField.setText("");
     	}
     	}
     
     private boolean checkIfDuplicatesExist(String str) {
     	char[] arr = str.toCharArray();
     	for(int i=0; i< arr.length - 1; i++) {
     		for(int j=i+1; j < arr.length; j++) {
     			if(arr[i] == arr[j]) {
     				return true;
     			}
     		}
     	}
     	return false;
     }
     
     public void clearInputText(View v) {
     	EditText inputTextField = (EditText)findViewById(R.id.txtInput);
     	inputTextField.setText("");    	
 	}
     
     public void btnGiveUpClicked(View v) {
     	prepareForNewGame();
     }
     
     private ArrayList<Integer> checkCowsAndBullsCount(String attmptString) {
     	ArrayList<Integer> list = new ArrayList<Integer>();
     	int cowCount = 0, bullCount = 0;
     	for(int i=0;i<currentWord.length();i++) {
 				if(attmptString.charAt(i)==currentWord.charAt(i)){
 					bullCount+=1;
 				}
 				else {
 					for(int j=0;j<currentWord.length();j++){
 						if(attmptString.charAt(i)==currentWord.charAt(j)){
 							cowCount+=1;							
 						}
 					}
 				}
     	}
     	list.add(bullCount);
     	list.add(cowCount);
     	return list;
     }
 
 }
