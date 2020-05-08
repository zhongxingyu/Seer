 package com.example.financialrecorder;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class SignUpActivity extends Activity implements OnClickListener {
 
 	private Button mSubmitButton;
 	private Button mCancelButton;
 	private final String LLOM = "LLOM";	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sign_up);
 
 		mSubmitButton = (Button)findViewById(R.id.submit_button);
 		mCancelButton = (Button)findViewById(R.id.cancel_button);
 		
 		mSubmitButton.setOnClickListener(this);
 		mCancelButton.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.sign_up, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.submit_button) {
 			Log.d(LLOM, "Submit button clicked");
 		} else if (v.getId() == R.id.cancel_button) {
 			Log.d(LLOM, "Cancel button clicked");
 		} else {
 			Log.d(LLOM, "Wat");
 		}
 	}
 
 }
