 package se.chalmers.dat255.sleepfighter.activities;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.challenges.SimpleMathChallenge;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class SimpleMathActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_alarm_challenge_math);
 		
 		SimpleMathChallenge smc = new SimpleMathChallenge();
 		
 		TextView userText = (TextView) findViewById(R.id.questionField);
 
 		userText.setText(smc.getCalculation());
 		
 //		EditText editText = (EditText) findViewById(R.id.answerField);
 //		editText.setOnEditorActionListener(new OnEditorActionListener() {
 //		    @Override
 //		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 //		        boolean handled = false;
 //		        if (actionId == EditorInfo.IME_ACTION_DONE) {
 //		            handled = true;
 //		        }
 //		        return handled;
 //		    }
 //		});
 	}

 }
