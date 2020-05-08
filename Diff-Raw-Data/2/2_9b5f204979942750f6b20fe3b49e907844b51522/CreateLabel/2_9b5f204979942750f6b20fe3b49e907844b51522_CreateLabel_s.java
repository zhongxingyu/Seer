 package com.zebra.android;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnKeyListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 public class CreateLabel extends Activity {
 	private EditText serial;
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.create_label);
 		
 		
 		Button CtoMain = (Button) findViewById(R.id.c_to_main);
 		CtoMain.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				Intent intent = new Intent();
         		setResult(RESULT_OK, intent);
         		finish();
 			}
 		});
 		
 		serial = (EditText)findViewById(R.id.manual_serial);
 	}
 
 		
 	
 	public boolean onKey(View v, int keyCode, KeyEvent event){
 		if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
 			//enter key pressed
 			String serialNumber = serial.getText().toString();
 			
 			
 			
 			return true;
 		}
 		return false;
 	}
 	
 	private void toggleEditField(EditText editText, boolean set) {
 		/*
 		 * Note: Disabled EditText fields may still get focus by some other means, and allow text input.
 		 *       See http://code.google.com/p/android/issues/detail?id=2771
 		 */
 		editText.setEnabled(set);
 		editText.setFocusable(set);
 		editText.setFocusableInTouchMode(set);
 	}
     private String getSerialFieldText() {
         return serial.getText().toString();
     }
 	
 }
