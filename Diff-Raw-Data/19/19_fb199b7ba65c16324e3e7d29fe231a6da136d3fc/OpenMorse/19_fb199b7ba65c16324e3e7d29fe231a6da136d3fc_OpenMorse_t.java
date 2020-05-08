 /*  OpenMorse - An android morse to text on-the-fly converter
  *  
    	Copyright (C) 2010 Samuel Skånberg
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.example.morse;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class OpenMorse extends Activity {
 	private static final int ACTIVITY_CREATE = 0;
 	private static final int SETTINGS_ID = Menu.FIRST;
     
 	
 	/* When the morse tone started */
 	private long start;
 	
 	/* When the morse tone stopped */
 	private long end;
 		
 	/* Have we pressed but not released? */
 	private boolean isPressed = false;
 	
 	/* The current letter we are building */
 	private StringBuilder letter = new StringBuilder();
 	
 	/* This is where our decoded morse code is presented to the user */
 	private EditText editField;
 	
 	/* This is the morse code for the ongoing letter being generated */
 	private TextView morseCodeView;
 	
 	private Handler handler = new Handler();
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         ImageView image = (ImageView)findViewById(R.id.button_image);
         image.setOnTouchListener(mOnTouchImageListener);
         image.setOnClickListener(mOnImageClickListener);
         
         editField = (EditText)findViewById(R.id.edit);
         morseCodeView = (TextView)findViewById(R.id.morse_code);
         
         /* Set default unit time to 200 */
         Morse.setUnitTime(200);
     }
     
     
     
     
     private View.OnTouchListener mOnTouchImageListener = new View.OnTouchListener() {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if (!isPressed) {
 				start = SystemClock.uptimeMillis();
 				isPressed = true;
 				
 				/* No need any more to check if we should finish a letter,
 				 * because we are now doing a new tone */
 				handler.removeCallbacks(finishLetterTask);
 				handler.removeCallbacks(finishWordTask);
 			}
 			return false;
 		}
 	};
 	
 	private View.OnClickListener mOnImageClickListener = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			end = SystemClock.uptimeMillis();
 				
 			long toneLength = end-start;
 			
 			/* A dash or a dot? */
 			char tone;
 			if (toneLength < Morse.UNIT_TIME) {
 				tone = '.';
 			} else{
 				tone = '-';
 			}
 			
 			letter.append(tone);
 			morseCodeView.append(""+tone);
 			
 			/* Set timer to check later if we should finish the letter and the word */
 			handler.postDelayed(finishLetterTask, Morse.SHORT_GAP_TIME);
 			handler.postDelayed(finishWordTask, Morse.MEDIUM_GAP_TIME);
 			
 			/* We are now no longer holding down the morse key */
 			isPressed = false;
 		}	 
 	};
 
 	Runnable finishLetterTask = new Runnable() {
 		public void run() {
 			char result = Morse.getLetter(letter.toString());
 			
 			/* Append the letter to the EditText field */
 			editField.append(""+result);
 
 			/* Reset the letter */
 			letter.delete(0, letter.length());
 
 			/* Reset the morse code presented to the user */
 			morseCodeView.setText("");
 		}
 	};
 	
 	Runnable finishWordTask = new Runnable() {
 		public void run() {
 			/* Append a space to the EditText field */
 			editField.append(" ");
 		}
 	};
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, SETTINGS_ID, 0, R.string.menu_settings);
         return true;
     }
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case SETTINGS_ID:
 			Intent i = new Intent(this, Settings.class);
 			/* Give the speed as an argument */
 			i.putExtra("speed", Morse.UNIT_TIME);
 	        startActivityForResult(i, ACTIVITY_CREATE);
 	        return true;
 		}
 		
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
		/* If the back key wasn't pressed */
		if (data != null) {
			Bundle extras = data.getExtras();
			switch(requestCode) {
			case ACTIVITY_CREATE:
				int speed = extras.getInt("speed");
				Log.d("OpenMorse", speed+"");
				Morse.setUnitTime(speed);
				break;
			}
		}
 
 	}
 	
 	
 }
