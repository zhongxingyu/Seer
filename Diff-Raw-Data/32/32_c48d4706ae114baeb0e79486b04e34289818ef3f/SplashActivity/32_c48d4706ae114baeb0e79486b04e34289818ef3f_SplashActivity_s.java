 package com.example.portfolioapp;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageButton;
 
 
 
 
 public class SplashActivity extends Activity implements android.view.View.OnClickListener {
 
 	ImageButton gotoMainBtn;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_splash);
 
 		gotoMainBtn = (ImageButton) findViewById(R.id.gotoMainBtn);

 	}
 	@Override
 	public void onClick(View v) {
 
 		if(v==gotoMainBtn){
			System.out.println("gotoMainBtn pressed");
			Intent myintent = new Intent(this, MainActivity.class);
 			startActivity(myintent);
 		}
 
 	}
 
 }
 
