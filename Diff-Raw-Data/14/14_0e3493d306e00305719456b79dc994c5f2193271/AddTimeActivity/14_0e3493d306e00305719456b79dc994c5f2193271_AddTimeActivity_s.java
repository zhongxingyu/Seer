 package com.example.listapplication;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 
 public class AddTimeActivity extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_time);
 	}
 	
 	public void onCancel(View view){
 		finish();
 	}
 	
 	public void onSave(View view){
 		Intent intent = getIntent();
 		
 		EditText text = (EditText)findViewById(R.id.time_view);
 		
 		intent.putExtra("time", text.getText().toString());
 		
 		text = (EditText)findViewById(R.id.notes_view);
 		
 		intent.putExtra("note", text.getText().toString());
 		
		this.setResult(RESULT_OK);
 		finish();
 	}
 
 }
