 package com.aj.androidatdd;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.aj.androidatdd.R.id;
 import com.aj.androidatdd.R.layout;
 
 public class UserStartActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(layout.user_start);

 		Button startBtn = ((Button) findViewById(id.btn_start));
 		startBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				start();
 			}
 
 		});
 	}
 
 	private void start() {
 		startActivity(new Intent(getApplicationContext(), SelectRecipeActivity.class));
 		finish();
 	}
 }
