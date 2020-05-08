 package com.example.handsonandroid;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class RegisterActivity extends Activity implements OnClickListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_register);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.register, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.registerButton:
 			break;
 		}
 		
 	}
 	
 	@Override
 	public void onStart(){
 		this.findViewById(R.id.registerButton).setOnClickListener(this);
 	}
 	
 
 }
