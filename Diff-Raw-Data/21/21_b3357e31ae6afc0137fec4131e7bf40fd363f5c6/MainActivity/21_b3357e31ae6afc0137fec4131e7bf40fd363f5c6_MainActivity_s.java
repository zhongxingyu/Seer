 package com.example.rnpfsc;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	public static final String SEND_MESSAGE = "com.example.SEND_MESSAGE";
 
	private String strSelection = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void onClick(View view) {
 		Button button = (Button) view;
		strSelection = button.getText().toString();
 
 		TextView selection = (TextView) findViewById(R.id.selection);
		selection.setText("You selected " + strSelection);
 
 		// TODO get Facebook ID
 		// TODO send strSelection to NFC tag
 	}
 
 	public void send(View view) {
		if (strSelection == null) {
 			return;
 		}
 
 		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra(SEND_MESSAGE, strSelection);
 
 		startActivity(intent);
 	}
 }
