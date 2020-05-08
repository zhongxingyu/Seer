 package de.atp.requester;
 
 import android.app.Activity;
import android.os.Bundle;
 import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
 
 public class StartActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_start);
		
		Button button = (Button) findViewById(R.id.button1);
		EditText editText = (EditText) findViewById(R.id.editTextCode);
		if (editText.length()==5) button.isClickable();
		
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText editText = (EditText) findViewById(R.id.editTextCode);
				TextView textview = (TextView) findViewById(R.id.textView1);
				String message = editText.getText().toString();
				textview.setText(message);
			}
		});	
 	}
	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.start, menu);
 		return true;
 	}
 
 }
