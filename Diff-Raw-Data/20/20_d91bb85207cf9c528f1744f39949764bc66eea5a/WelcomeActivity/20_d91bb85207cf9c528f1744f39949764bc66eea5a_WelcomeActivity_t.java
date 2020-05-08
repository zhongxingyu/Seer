 package com.goodhearted.smokebegone;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class WelcomeActivity extends Activity implements OnClickListener {
 
 	Button save;
 	EditText numPack, pricePack, numCigPerDay;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_welcome);
 
 		save = (Button) findViewById(R.id.bSave);
 		save.setOnClickListener(this);
 
 		numPack = (EditText) findViewById(R.id.etNumPerPack);
 		pricePack = (EditText) findViewById(R.id.etPrice);
 		numCigPerDay = (EditText) findViewById(R.id.etsmokesperDay);
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		switch (arg0.getId()) {
 		case R.id.bSave:
 			if (saveOptions()) {
 				Intent i = new Intent(this, MainActivity.class);
 				this.startActivity(i);
 			}
 			break;
 		}
 	}
 
 	private boolean saveOptions() {
 		PreferenceProvider.writeInteger(this, PreferenceProvider.keyCPP, Integer.parseInt(numPack.getText().toString()));
 		PreferenceProvider.writeFloat(this, PreferenceProvider.keyPPP, Float.parseFloat(pricePack.getText().toString()));
 		PreferenceProvider.writeInteger(this, PreferenceProvider.keyCPD, Integer.parseInt(numPack.getText().toString()));
 		PreferenceProvider.writeString(this, PreferenceProvider.keyQD, (new Date()).toGMTString());
 		return true;
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 	
 	
 }
