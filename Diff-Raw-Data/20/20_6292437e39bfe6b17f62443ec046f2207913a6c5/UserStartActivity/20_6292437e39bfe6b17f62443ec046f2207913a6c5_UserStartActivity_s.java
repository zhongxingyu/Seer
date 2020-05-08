 package com.aj.androidatdd;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.aj.androidatdd.R.id;
 import com.aj.androidatdd.R.layout;
 
 public class UserStartActivity extends Activity {
 
 	private EditText userName;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(layout.user_start);
 		userName = (EditText) findViewById(id.et_user_name);
 		Button startBtn = ((Button) findViewById(id.btn_start));
 		startBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (nameHasBeenEntered())
 					start();
 			}
 
 		});
 	}
 
 	private boolean nameHasBeenEntered() {
		return !userName.getText().toString().trim().isEmpty();
 	}
 
 	private void start() {
 		startActivity(new Intent(getApplicationContext(), SelectRecipeActivity.class));
 		finish();
 	}
 }
