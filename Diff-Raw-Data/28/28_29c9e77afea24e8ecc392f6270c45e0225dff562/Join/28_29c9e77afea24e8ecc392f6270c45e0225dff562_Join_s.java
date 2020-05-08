 package com.example.voter;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 public class Join extends Activity {
 	LinearLayout layout;
 	EditText inputvoteidEditText;
 	Button confirmButton;
 	
 	@Override
 	public void onCreate(Bundle saveInstanceState) {
 		Bundle savedInstanceState = null;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.join);
 		inputvoteidEditText = (EditText)findViewById(R.id.inputvoteid);
 		confirmButton = (Button)findViewById(R.id.confirm);
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				int voteid = Integer.parseInt(inputvoteidEditText.getText().toString());
				voteid = voteid + 1;
 			}
 		});
		
 	}
 }
