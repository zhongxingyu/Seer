 package com.ddyystudio.audiocontrollight;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class SettingActivity extends Activity {
	Button mbtBack;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.setting);
 		mbtBack = (Button) findViewById(R.id.back);
 		mbtBack.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
				Intent intent=new Intent(SettingActivity.this, MainActivity.class);
				startActivity(intent);
 			}
 		});
 	}
 }
