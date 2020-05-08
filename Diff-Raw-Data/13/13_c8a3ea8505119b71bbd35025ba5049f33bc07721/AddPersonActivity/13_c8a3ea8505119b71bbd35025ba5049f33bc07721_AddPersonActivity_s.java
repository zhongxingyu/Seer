 package com.tiptax;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class AddPersonActivity extends Activity {
 
 	private String personName, personValue;
 	private int origPos;
 
 	public void addPersonOKClick(View v) {
 		personName = ((EditText) findViewById(R.id.namePersonET)).getText().toString();
 		personValue = ((EditText) findViewById(R.id.valuePersonET)).getText().toString();
 
 		if (personName.equals("")) {
 			Toast toast = Toast.makeText(getApplicationContext(), "Please enter a name!", Toast.LENGTH_LONG);
 			toast.show();
 			return;
 		} else if (personValue.equals("")) {
 			Toast toast = Toast.makeText(getApplicationContext(), "Please enter a value!", Toast.LENGTH_LONG);
 			toast.show();
 			return;
 		}
 
 		Intent in = new Intent();
 		in.putExtra("name", personName);
 		in.putExtra("value", personValue);
 		in.putExtra("origPos", origPos);
 		setResult(Activity.RESULT_OK, in);
 		finish();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_person);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Intent i = getIntent();
 		personName = i.getStringExtra("name");
 		personValue = i.getStringExtra("value");
 		origPos = i.getIntExtra("origPos", -1);
 
 		((EditText) findViewById(R.id.namePersonET)).setText(personName);
 		((EditText) findViewById(R.id.valuePersonET)).setText(personValue);
 	}
 
 }
