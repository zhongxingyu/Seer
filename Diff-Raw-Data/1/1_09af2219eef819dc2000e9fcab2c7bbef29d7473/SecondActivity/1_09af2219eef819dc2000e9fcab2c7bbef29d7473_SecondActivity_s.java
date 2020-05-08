 package net.morodomi.lecture3;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 /**
  * Activity for Android Lecture 3
  * This shows when the button of FirstActivity is clicked;
  * @author Masahiro Morodomi <morodomi at gmail.com>
  */
 public class SecondActivity extends Activity implements OnClickListener {
 	/** Called when the activity is created */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.second);
 		// create button instance
 		Button btn = (Button) findViewById(R.id.second_btn);
 		// set click event
 		btn.setOnClickListener(this);
 	}
 
 	/** Called when the button is clicked */
 	@Override
 	public void onClick(View v) {
 		// move back to first activity
 		finish();
 	}
 }
