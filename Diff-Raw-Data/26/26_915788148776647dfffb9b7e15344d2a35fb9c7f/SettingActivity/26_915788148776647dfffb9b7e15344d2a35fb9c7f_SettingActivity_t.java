 package com.ddyystudio.audiocontrollight;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;
 
 public class SettingActivity extends Activity {
	private Button mbtBack;
	private Button radio1,radio2,radio3;
	private LayoutParams params = new LayoutParams(0, 0);;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.setting);
		params.width = 50;
		params.height = 50;
		params.setMargins(20, 20, 0, 0);
 		mbtBack = (Button) findViewById(R.id.back);
		mbtBack.setLayoutParams(params);
		params.setMargins(20, 120, 0, 0);
		radio1 = (Button) findViewById(R.id.radio1);
		radio1.setLayoutParams(params);
		params.setMargins(20, 220, 0, 0);
		radio2 = (Button) findViewById(R.id.radio2);
		radio2.setLayoutParams(params);
		params.setMargins(20, 320, 0, 0);
		radio3 = (Button) findViewById(R.id.radio3);
		radio3.setLayoutParams(params);
		
 		mbtBack.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
				finish();
 			}
 		});
 	}
 }
