 package com.aj.androidatdd;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.TextView;
 
 import com.aj.androidatdd.R.id;
 import com.aj.androidatdd.R.layout;
 
 public class UserStartActivity extends Activity {
 
 	private EditText userName;
	private TextView errorMessage;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(layout.user_start);
 		userName = (EditText) findViewById(id.et_user_name);
		errorMessage = (TextView) findViewById(id.tv_user_name_error);
 		Button startBtn = ((Button) findViewById(id.btn_start));
 		startBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (nameHasBeenEntered())
 					start();
				else
					showErrorMessage();
 			}
 
 		});
 	}
 
	private void showErrorMessage() {
		errorMessage.setVisibility(View.VISIBLE);
	}

 	private boolean nameHasBeenEntered() {
		return !getNameEntered().isEmpty();
	}

	private String getNameEntered() {
		return userName.getText().toString().trim();
 	}
 
 	private void start() {
 		startActivity(new Intent(getApplicationContext(), SelectRecipeActivity.class));
 		finish();
 	}
 }
