 package com.nityankhanna.androidutils;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 /**
  * Created by Nityan Khanna on 22/10/13.
  */
public class MainActivity extends Activity implements View.OnClickListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Button button = (Button) findViewById(R.id.button);
 
 		button.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View view) {
 
 		switch (view.getId()) {
 
 			case R.id.button:
 				break;
 
 			default:
 				break;
 		}
 	}
 }
