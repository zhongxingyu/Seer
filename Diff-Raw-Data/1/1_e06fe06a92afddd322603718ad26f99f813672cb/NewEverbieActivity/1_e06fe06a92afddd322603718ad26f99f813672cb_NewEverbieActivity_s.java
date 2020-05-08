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
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.cheesymountain.woe.SimpleGestureFilter.SimpleGestureListener;
 
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
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		if(savedInstanceState != null){
 		if(savedInstanceState.get("name")!=null){
 			onRestoreInstanceState(savedInstanceState);
 			Intent main = new Intent("com.cheesymountain.woe.MAINACTIVITY");
 			startActivity(main);
 		}
 		}
 		setContentView(R.layout.activity_new_everbie);
 		
 		description = ((TextView)findViewById(R.id.everbieLongText));
 		description.setText(Race.RACELIST[selectedRace].getDescription() + RaceDescription.SWIPE_DESCRIPTION);
 		pictures = (ImageView)findViewById(R.id.everbiePicsImageView);
 		pictures.setImageResource(Race.RACELIST[selectedRace].getImageId());
 		name = (EditText)findViewById(R.id.everbieNameText);
 		name.setHint("Enter Name Here");
 
         detector = new SimpleGestureFilter(this,this);
 	}
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState){
     	super.onRestoreInstanceState(savedInstanceState);
     	int[] values = {savedInstanceState.getInt("maxHealth"),
     	savedInstanceState.getInt("health"),
     	savedInstanceState.getInt("Strength"),
     	savedInstanceState.getInt("intelligence"),
     	savedInstanceState.getInt("stamina"),
     	savedInstanceState.getInt("charm"),
     	savedInstanceState.getInt("fullness"),
     	savedInstanceState.getInt("happiness"),
     	savedInstanceState.getInt("toxicity"),
     	savedInstanceState.getInt("cuteness"),
     	savedInstanceState.getInt("money")};
     	Everbie.getEverbie().restoreEverbie(savedInstanceState.getString("Name"), values,
     			savedInstanceState.getBoolean("alive"), savedInstanceState.getInt("imagePath"));	
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
 		Everbie.createEverbie(name, Race.RACELIST[selectedRace]);
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
 	 * Is called when the user swipes across the screen
 	 * @param direction - the direction in which the user swipes
 	 */
 	public void onSwipe(int direction){
 		switch(direction){
 			case SimpleGestureFilter.SWIPE_RIGHT:
 				if(selectedRace > 0){
 					pictures.setImageResource(Race.RACELIST[--selectedRace].getImageId());
 				}
 				break;
 			case SimpleGestureFilter.SWIPE_LEFT:
 				if(selectedRace < Race.RACELIST.length-1){
 					pictures.setImageResource(Race.RACELIST[++selectedRace].getImageId());
 				}
 				break;
 		}
 		description.setText(Race.RACELIST[selectedRace].getDescription() + RaceDescription.SWIPE_DESCRIPTION);
 	}
 	
 	/**
 	 * Is called automatically when pressing the exit button
 	 * @param view - the view from which exit button was pressed
 	 */
 	public void exit(View view){
     	Intent start = new Intent("com.cheesymountain.woe.STARTSCREENACTIVITY");
 		startActivity(start);
 		finish();
 	}
 
 	/**
 	 * Inherited from SimpleGestureListener (not used)
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
