 package com.rachum.amir.skyhiking.android;
 
 import com.rachum.amir.skyhiking.android.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MenuActivity extends Activity {
 	private long lastTapTime = -1;
	private String playerName = "Player";
 	private final int CHANGE_NAME_DIALOG = 1;
     /** Called when the activity is first created. */
     @Override
    public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         showPlayerName();
     }
     
     public void beginGame(final View view) {
     	final Intent intent = new Intent(this, GameActivity.class);
     	intent.putExtra("playerName", playerName);
         startActivity(intent);
    }
     
     public void playerNameClick(final View view) {
     	long thisTime = System.currentTimeMillis();
     	if (thisTime - lastTapTime < 250) {
     		changePlayerName();
     		lastTapTime = -1;
     	} else {
     		lastTapTime = thisTime;
     	}
     }
     
     @Override
     protected Dialog onCreateDialog(int id) {
     	switch (id) {
     	case CHANGE_NAME_DIALOG:
     		LayoutInflater factory = LayoutInflater.from(this);
     		final View textEntryView = factory.inflate(R.layout.change_name, null);
     		return new AlertDialog.Builder(MenuActivity.this)
     		//.setIconAttribute(android.R.attr.alertDialogIcon)
     		.setTitle(R.string.change_name_title)
     		.setView(textEntryView)
     		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int whichButton) {
 					final EditText name = (EditText) textEntryView.findViewById(R.id.name_input);
     				playerName = name.getText().toString();
     				MenuActivity.this.showPlayerName();
     			}
     		})
     		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int whichButton) {
     			}
     		})
     		.create();
     	}
     	return null;
     }
     
     private void showPlayerName() {
     	TextView nameDisplay = (TextView) findViewById(R.id.player_name_display);
     	nameDisplay.setText("You are " + playerName);
     }
 
 	private void changePlayerName() {
 		showDialog(CHANGE_NAME_DIALOG);
 		showPlayerName();
 	}
 }
