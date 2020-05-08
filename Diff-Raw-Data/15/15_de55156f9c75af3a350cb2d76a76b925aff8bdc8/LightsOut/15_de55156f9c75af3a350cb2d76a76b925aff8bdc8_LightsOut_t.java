 package com.android.lightsout;
 
 import java.util.ArrayList;
 
 import com.android.lightsout.R;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class LightsOut extends Activity implements OnClickListener {
 
     private int numButtons;
 
     private LightsOutGame game;
 
     private ArrayList<Button> buttons;
 
     private TextView gameState;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.lights_out);
 	numButtons = getIntent().getIntExtra(MainMenu.KEY_NUM_BUTTONS, 7);
 	Log.d(MainMenu.LLOM, "Starting game with " + numButtons + " buttons.");
 	game = new LightsOutGame(numButtons);
 	buttons = new ArrayList<Button>();
 	TableRow buttonRow = new TableRow(this);
 	for (int i = 0; i < numButtons; i++) {
 	    Button currentButton = new Button(this);
	    currentButton.setTag(Integer.valueOf(i)); // Preparing for later
 	    buttons.add(currentButton);
 	    buttonRow.addView(currentButton);
 	    currentButton.setOnClickListener(this);
 	}
 	gameState = (TextView) findViewById(R.id.game_state);
 	TableLayout buttonTable = (TableLayout) findViewById(R.id.button_table);
 	buttonTable.addView(buttonRow);
 	Button newGame = (Button) findViewById(R.id.new_game_button);
 	newGame.setOnClickListener(this);
 	SharedPreferences pref = getPreferences(Activity.MODE_PRIVATE);
 	if (pref.getInt("NUM_BUTTONS", numButtons) == numButtons) {
 	    for (int i = 0; i < numButtons; i++) {
 		if (pref.contains("BUTTON_" + i + "_VALUE"))
 		    game.buttonValues[i] = pref.getInt(
 			    "BUTTON_" + i + "_VALUE", game.getValueAtIndex(i));
 	    }
 	    game.numPresses = pref.getInt("NUM_PRESSES", 0);
 	}
 	if (game.checkForWin())
 	    updateView(true);
 	else
 	    updateView(false);
     }
 
     private void updateView(boolean didWin) {
 	for (int i = 0; i < this.numButtons; i++) {
 	    buttons.get(i).setText("" + this.game.getValueAtIndex(i));
 	    buttons.get(i).setEnabled(!didWin);
 	}
 	Resources r = this.getResources();
 	String newGameString;
 	int numPresses = game.getNumPresses();
 	if (didWin) {
 	    if (numPresses == 1) {
 		newGameString = r.getString(R.string.you_won_one_move);
 	    } else {
 		newGameString = r
 			.getString(R.string.you_won_format, numPresses);
 	    }
 	} else {
 	    if (numPresses == 0) {
 		newGameString = r.getString(R.string.game_start);
 	    } else if (numPresses == 1) {
 		newGameString = r.getString(R.string.game_one_move);
 	    } else {
 		newGameString = r.getString(R.string.game_format, numPresses);
 	    }
 	}
 	gameState.setText(newGameString);
     }
 
     @Override
     protected void onPause() {
 	super.onPause();
 	SharedPreferences pref = getPreferences(Activity.MODE_PRIVATE);
 	SharedPreferences.Editor editor = pref.edit();
 	for (int i = 0; i < numButtons; i++) {
 	    editor.putInt("BUTTON_" + i + "_VALUE", game.getValueAtIndex(i));
 	}
 	editor.putInt("NUM_PRESSES", game.numPresses);
 	editor.putInt("NUM_BUTTONS", numButtons);
 	editor.putString("GAME_STATE", gameState.getText().toString());
 	editor.commit();
     }
 
     @Override
     public void onClick(View v) {
 	boolean didWin = false;
 	if (v.getId() == R.id.new_game_button) {
 	    Log.d(MainMenu.LLOM, "New game pressed");
 	    this.game = new LightsOutGame(this.numButtons);
 	} else {
 	    Log.d(MainMenu.LLOM, "Button with tag " + v.getTag());
 	    didWin = this.game.pressedButtonAtIndex((Integer) v.getTag());
 	}
 	updateView(didWin);
     }
 }
