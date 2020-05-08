 package com.sabayrean.hangman;
 
 import java.util.List;
 import java.util.regex.Matcher;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.inputmethod.InputMethodManager;
 
 import com.sabayrean.widget.MyEditText;
 
 public class MainActivity extends Activity {
 
 	public static final String TAG = Debug.TAG;
	private Guess guess = null;
 
 	private MyEditText editor;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
 				InputMethodManager.HIDE_IMPLICIT_ONLY);
 
 //		String randomString = RandomString.getInstance().getWord();
 //		TextResult result = new TextResult(randomString);
 //		Guess guess = new Guess(result);
 //		
 //		// user typing keyboard
 //		List<Integer> indexes = guess.getIndexes(c);
 //		for(Integer index : indexes){
 //			guess.getResult().charAt(index);
 //			// display character on _______
 //		}
 		
 		String randomString = RandomString.getInstance().getWord();
 		TextResult result = new TextResult(randomString);
		guess = new Guess(result);
 		
 		editor = (MyEditText) findViewById(R.id.editor);
 
 		setObjectListenner();
 	}
 
 	private void setObjectListenner() {
 		editor.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				char lastChar = s.charAt(s.toString().length() - 1);
 //				Guess.isExists
				List<Integer> indexes = guess.getIndexes(lastChar);
				if(indexes != null)
					for(Integer index : indexes) {
						char c = guess.getResult().getText().charAt(index);
						Log.e(TAG, "char" + c);
					}
 
 			}
 			
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 		});
 	}
 }
