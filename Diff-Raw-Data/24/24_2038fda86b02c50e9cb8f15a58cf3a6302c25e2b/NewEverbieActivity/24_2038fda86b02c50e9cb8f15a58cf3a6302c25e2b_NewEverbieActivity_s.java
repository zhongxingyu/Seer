 package com.cheesymountain.woe;
 /*=============================================================
  * Copyright 2012, Cheesy Mountain Production
  * 
  * This file is part of World of Everbies.
  * 
  * World of Everbies is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * World of Everbies is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with World of Everbies.  If not, see <http://www.gnu.org/licenses/>.
 ================================================================*/
 
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.*;
 
 import com.cheesymountain.woe.R;
 import com.cheesymountain.woe.race.Race;
 import com.cheesymountain.woe.util.SimpleGestureFilter;
 import com.cheesymountain.woe.util.SimpleGestureFilter.SimpleGestureListener;
 
 /**
  * Controller class when creating a new Everbie that handles swipe events
  * for changing race.
  * @author Markus Otterberg
  *
  */
 public class NewEverbieActivity extends Activity implements SimpleGestureListener {
 
 
 	private int selectedRace = 0;
 	private static final int DIALOG_INVALIDNAME_ID = 0;
 
 	private ImageView pictures;
 	private EditText name;
 	private TextView description;
 	private SimpleGestureFilter detector; 
 	private RadioButton[] orientationDots = new RadioButton[Race.RACELIST.length];
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_new_everbie);
 		description = ((TextView)findViewById(R.id.everbieLongText));
 		description.setText(Race.RACELIST[selectedRace].getDescription());
 		pictures = (ImageView)findViewById(R.id.everbiePicsImageView);
 		pictures.setImageResource(Race.RACELIST[selectedRace].getImageIdMin());
 		name = (EditText)findViewById(R.id.everbieNameText);
 		name.setHint("Enter name");
 		((RadioButton)(findViewById(R.id.Radio0))).setChecked(true);
 
 		detector = new SimpleGestureFilter(this,this);
 		orientationDots[0] = (RadioButton)(findViewById(R.id.Radio0));
 		orientationDots[1] = (RadioButton)(findViewById(R.id.Radio1));
 		orientationDots[2] = (RadioButton)(findViewById(R.id.Radio2));
 		orientationDots[3] = (RadioButton)(findViewById(R.id.Radio3));
 	}
 
 	/**
 	 * Is called automatically when pressing the create button.
 	 * @param view - the view from which create button was pressed
 	 */
 	@SuppressWarnings("deprecation")
 	public void create(View view){
 		String name = this.name.getText().toString();
 		name = name.trim();
 		if(name == null || name.equals("")){
 			showDialog(DIALOG_INVALIDNAME_ID);
 			return;
 		}
 		if(Everbie.exists()){
 			Everbie.getEverbie().kill();
 			try {
 				//Waiting for Occupation-thread to die
 				Thread.sleep(1050);
 			}catch(InterruptedException e) {//Just wait
 			}
 			Everbie.getEverbie().reset();	
 		}
 		Everbie.createEverbie(name, Race.RACELIST[selectedRace]);
 		Database db = new Database(this);
 		db.open();
 		db.save(Everbie.getEverbie());
 		db.close();
 		Intent main = new Intent("com.cheesymountain.woe.MAINACTIVITY");
 		startActivity(main);
 		finish();
 	}
 
 	@Override
 	public Dialog onCreateDialog(int i){
 		switch(i){
 		case DIALOG_INVALIDNAME_ID:
 			Builder builder = new Builder(this);
 			builder.setMessage("Please enter a valid name.");
 			builder.setCancelable(false);
 			builder.setNeutralButton("Okay, I will", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			});
 			return builder.create();
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * Is called when the user swipes across the screen.
 	 * @param direction - the direction in which the user swipes
 	 */
 	public void onSwipe(int direction){
 		switch(direction){
 		case SimpleGestureFilter.SWIPE_RIGHT:
 			if(selectedRace > 0){
 				pictures.setImageResource(Race.RACELIST[--selectedRace].getImageIdMin());
 			}
 			break;
 		case SimpleGestureFilter.SWIPE_LEFT:
 			if(selectedRace < Race.RACELIST.length-1){
 				pictures.setImageResource(Race.RACELIST[++selectedRace].getImageIdMin());
 			}
 			break;
 		}
 		description.setText(Race.RACELIST[selectedRace].getDescription());
 		setDotOrientation(selectedRace);
 	}
 
 	private void setDotOrientation(int selectedDot){
 		for(int i = 0; i < orientationDots.length; i++){
 			orientationDots[i].setChecked(selectedDot==i);
 		}
 	}
 
 	/**
 	 * Is called automatically when pressing the exit button.
 	 * @param view - the view from which exit button was pressed
 	 */
 	public void exit(View view){
 		Intent start = new Intent("com.cheesymountain.woe.STARTSCREENACTIVITY");
 		startActivity(start);
 		finish();
 	}
 
 	/**
 	 * Inherited from SimpleGestureListener (not used).
 	 */
 	public void onDoubleTap() {
 		/* Do nothing */
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent me){
 		this.detector.onTouchEvent(me);
 		return super.dispatchTouchEvent(me); 
 	}
 
 }
