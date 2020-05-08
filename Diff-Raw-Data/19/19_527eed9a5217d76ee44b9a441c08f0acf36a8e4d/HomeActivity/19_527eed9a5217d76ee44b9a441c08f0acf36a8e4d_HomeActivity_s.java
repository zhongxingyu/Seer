 package com.spiteful.cipher.ui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.spiteful.cipher.android.R;
 
 public class HomeActivity extends Activity {
 	private static final String tag = HomeActivity.class.toString();
 	private Button scanButton;
 	private Button viewMessageButton;
 	private Button prefsButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 
 		this.scanButton = (Button) this.findViewById(R.id.btn_scan);
 		this.scanButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
 				startActivity(intent);
 			}
 		});
 
 		this.prefsButton = (Button) this.findViewById(R.id.btn_prefs);
 		this.prefsButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(HomeActivity.this, CipherPreferencesActivity.class);
 				startActivity(intent);
 			}
 		});
 
 		this.viewMessageButton = (Button) this.findViewById(R.id.btn_view);
 		this.viewMessageButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(HomeActivity.this, ViewMessageActivity.class);
 				startActivity(intent);
 			}
 		});
 	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
 }
