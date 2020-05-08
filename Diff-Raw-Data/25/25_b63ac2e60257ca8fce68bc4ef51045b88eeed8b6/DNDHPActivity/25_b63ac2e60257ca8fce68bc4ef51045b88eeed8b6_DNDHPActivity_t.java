 //git repository: "git@github.com:codonnell/dnd-hp-tracker.git"
 package com.admteal.dndhp;
 
 import com.admteal.dndhp.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.Editable;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import android.os.Bundle;
 
 public class DNDHPActivity extends Activity {
 	public Player player;
 
 	// Some variables for cleanliness's sake
 	public static String PLUS, MINUS, BLANK, COLON_SPACE, INPUT;
 
 	public final int DIALOG_CLEAR = 0;
 	public final int DIALOG_NEW_CUSTOM_PLAYER = 1;
 
 	public int currentEntry;
 
	// Initialize the status toggles
 	public ToggleButton toggleBlinded, toggleDazed, toggleDominated,
 			toggleGrabbed, toggleMarked, toggleProne, toggleStunned,
 			toggleWeakened;
 
 	// Initialize the calculator function buttons
 	public Button inputTHP, inputAdd, inputSub, inputClear, inputHS;
 
 	// Initialize the ongoing function buttons
 	public Button ongoAdd, ongoSub, inputOngo, inputDS;
 
 	// Initialize the surges function buttons
 	public Button surgesAdd, surgesSub, inputSurges;
 
 	// Initialize the Death Saves function buttons
 	public Button DSAdd, DSSub;
 
	// Initialize our important views
 	public TextView currentEntryView, currentHPView, currentTHPView;
 	public ScrollView showWorkScroller;
	public LinearLayout showWorkLayout;
 
 	// Called when the activity is first created.
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		PLUS = getString(R.string.plus);
 		MINUS = getString(R.string.minus);
 		BLANK = getString(R.string.blank);
 		COLON_SPACE = getString(R.string.colonspace);
 		INPUT = getString(R.string.input);
 
 		// Create the new default player if there isn't one already
 		if (savedInstanceState == null) {
 			player = new Player();
 		}
 
 		// Set the toggle buttons
 		toggleBlinded = (ToggleButton) findViewById(R.id.toggleBlinded);
 		toggleBlinded.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleBlinded.isChecked()) {
 					player.blind();
 				} else {
 					player.unblind();
 				}
 			}
 		});
 		toggleDazed = (ToggleButton) findViewById(R.id.toggleDazed);
 		toggleDazed.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleDazed.isChecked()) {
 					player.daze();
 				} else {
 					player.undaze();
 				}
 			}
 		});
 		toggleDominated = (ToggleButton) findViewById(R.id.toggleDominated);
 		toggleDominated.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleDominated.isChecked()) {
 					player.dominate();
 				} else {
 					player.undominate();
 				}
 			}
 		});
 		toggleGrabbed = (ToggleButton) findViewById(R.id.toggleGrabbed);
 		toggleGrabbed.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleGrabbed.isChecked()) {
 					player.grab();
 				} else {
 					player.ungrab();
 				}
 			}
 		});
 		toggleMarked = (ToggleButton) findViewById(R.id.toggleMarked);
 		toggleMarked.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleMarked.isChecked()) {
 					player.mark();
 				} else {
 					player.unmark();
 				}
 			}
 		});
 		toggleProne = (ToggleButton) findViewById(R.id.toggleProne);
 		toggleProne.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleProne.isChecked()) {
 					player.knockProne();
 				} else {
 					player.getUp();
 				}
 			}
 		});
 		toggleStunned = (ToggleButton) findViewById(R.id.toggleStunned);
 		toggleStunned.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleStunned.isChecked()) {
 					player.stun();
 				} else {
 					player.unstun();
 				}
 			}
 		});
 		toggleWeakened = (ToggleButton) findViewById(R.id.toggleWeakened);
 		toggleWeakened.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (toggleStunned.isChecked()) {
 					player.weaken();
 				} else {
 					player.unweaken();
 				}
 			}
 		});
 
 		// Set the calculator number buttons and their onClickListeners.
 		// Creates the 10 buttons at once
 		for (int i = 0; i < 10; i++) {
 			String buttonID = INPUT + Integer.toString(i);
 			int resourceID = getResources().getIdentifier(buttonID, "id",
 					"com.admteal.dndhp");
 			Button b = (Button) findViewById(resourceID);
 			final int j = i; // allows passing i into a new
 								// View.OnClickListener's onClick
 			b.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentEntryViewUpdater(j);
 				}
 			});
 		}
 
 		// Create the calculator function buttons
 		inputTHP = (Button) findViewById(R.id.inputTHP);
 		inputTHP.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				tempHPUpdater(currentEntry);
				clearEntry();
 			}
 		});
 		inputAdd = (Button) findViewById(R.id.inputAdd);
 		inputAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(currentEntry);
 			}
 		});
 		inputSub = (Button) findViewById(R.id.inputSub);
 		inputSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(-currentEntry);
 			}
 		});
 		inputClear = (Button) findViewById(R.id.inputClear);
 		inputClear.setOnLongClickListener(new View.OnLongClickListener() {
 			public boolean onLongClick(View v) {
 				showDialog(DIALOG_CLEAR);
 				return true; // stops click event from also being processed
 			}
 		});
 		inputClear.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				clearEntry();
 			}
 		});
 		inputHS = (Button) findViewById(R.id.inputHS);
 		inputHS.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(player.getHS());
 			}
 		});
 		/*
 		 * Long click to set currentEntry into Healing surge value, and display
 		 * it on the button
 		 */
 		inputHS.setOnLongClickListener(new View.OnLongClickListener() {
 			public boolean onLongClick(View v) {
 				player.setHS(currentEntry);
 				inputHS.setText(getResources().getString(R.string.hs)
 						+ COLON_SPACE + Integer.toString(currentEntry));
 				clearEntry();
 				return true;
 			}
 		});
 
 		// Create the ongoing function buttons
 		ongoAdd = (Button) findViewById(R.id.ongoAdd);
 		ongoAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				ongoUpdater(PLUS);
 			}
 		});
 		ongoSub = (Button) findViewById(R.id.ongoSub);
 		ongoSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				ongoUpdater(MINUS);
 			}
 		});
 		inputOngo = (Button) findViewById(R.id.inputOngo);
 		inputOngo.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				/*
 				 * current ongo stores the number backwards, so it is
 				 * invertedfirst
 				 */
 				showWorkUpdater(-player.getOngo());
 			}
 		});
 
 		// Create the surges function buttons
 		surgesAdd = (Button) findViewById(R.id.surgesAdd);
 		surgesAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				surgesUpdater(PLUS);
 			}
 		});
 		surgesSub = (Button) findViewById(R.id.surgesSub);
 		surgesSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				surgesUpdater(MINUS);
 			}
 		});
 		inputSurges = (Button) findViewById(R.id.inputSurges);
 		inputSurges.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (player.getSurges() == 0) {
 					return;
 				}
 				showWorkUpdater(player.getHS());
 				surgesUpdater(MINUS);
 			}
 		});
 
 		// Create the Death Saves function buttons
 		DSAdd = (Button) findViewById(R.id.DSAdd);
 		DSAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				DSUpdater(PLUS);
 			}
 		});
 		DSSub = (Button) findViewById(R.id.DSSub);
 		DSSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				DSUpdater(MINUS);
 			}
 		});
 
 		inputDS = (Button) findViewById(R.id.inputDS);
 
 		currentEntryView = (TextView) findViewById(R.id.currentEntryView);
 		showWorkScroller = (ScrollView) findViewById(R.id.showWorkScroller);
 		showWorkLayout = (LinearLayout) findViewById(R.id.showWorkLayout);
 		currentHPView = (TextView) findViewById(R.id.currentHPView);
 		currentTHPView = (TextView) findViewById(R.id.currentTHPView);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		relaunchWithPlayer(player);
 		currentEntryView.setText(Integer.toString(currentEntry));
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		savedInstanceState.putInt("currentEntry", currentEntry);
 		savedInstanceState.putSerializable("Player", player);
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		currentEntry = savedInstanceState.getInt("currentEntry");
 		player = (Player) savedInstanceState.getSerializable("Player");
 	}
 
 	// Generate my alert dialogs
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_CLEAR:
 			return new AlertDialog.Builder(this)
 					.setMessage(R.string.DIALOG_CLEAR_msg)
 					.setCancelable(true)
 					.setPositiveButton("Yes",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									clearEntry();
 									player.extendedRest();
 									relaunchWithPlayer(player);
 								}
 							}).setNegativeButton("No", null).create();
 		case DIALOG_NEW_CUSTOM_PLAYER:
 			final View customizePlayerView = View.inflate(this,
 					R.layout.dialog_custom_player, null);
 			final EditText newMaxHPEdit = (EditText) customizePlayerView
 					.findViewById(R.id.maxHPEdit);
 			final EditText newCurrentHPEdit = (EditText) customizePlayerView
 					.findViewById(R.id.currentHPEdit);
 			final EditText newMaxSurgesEdit = (EditText) customizePlayerView
 					.findViewById(R.id.maxSurgesEdit);
 			final EditText newCurrentSurgesEdit = (EditText) customizePlayerView
 					.findViewById(R.id.currentSurgesEdit);
 			final EditText newCurrentHSEdit = (EditText) customizePlayerView
 					.findViewById(R.id.currentHSEdit);
 
 			return new AlertDialog.Builder(this)
 					.setTitle(R.string.DIALOG_CUSTOM_PLAYER_title)
 					.setCancelable(true)
 					.setView(customizePlayerView)
 					.setTitle("Test test")
 					.setPositiveButton(R.string.savePlayer,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									String customPlayerNewMaxHP = newMaxHPEdit
 											.getText().toString();
 									String customPlayerNewCurrentHP = newCurrentHPEdit
 											.getText().toString();
 									String customPlayerNewMaxSurges = newMaxSurgesEdit
 											.getText().toString();
 									String customPlayerNewCurrentSurges = newCurrentSurgesEdit
 											.getText().toString();
 									String customPlayerNewHS = newCurrentHSEdit
 											.getText().toString();
 
 									if (customPlayerNewMaxHP.equals("")
 											|| customPlayerNewMaxSurges
 													.equals("")) {
 										Toast.makeText(
 												getApplicationContext(),
 												getString(R.string.TOAST_couldNotMakePlayer),
 												Toast.LENGTH_SHORT).show();
 										return;
 									} else {
 										if (customPlayerNewHS.equals("")) {
 											int customPlayerNewHSInt = Integer
 													.parseInt(customPlayerNewMaxHP) / 2;
 											customPlayerNewHSInt = (customPlayerNewHSInt / 2) / 2;
 											customPlayerNewHS = Integer
 													.toString(customPlayerNewHSInt);
 										}
 										if (customPlayerNewCurrentHP.equals("")
 												|| customPlayerNewCurrentSurges
 														.equals("")) {
 											player = new Player(
 													Integer.parseInt(customPlayerNewMaxHP),
 													Integer.parseInt(customPlayerNewMaxSurges),
 													Integer.parseInt(customPlayerNewHS));
 											Toast.makeText(
 													getApplicationContext(),
 													getString(R.string.TOAST_madeFullHPPlayer),
 													Toast.LENGTH_SHORT).show();
 											relaunchWithPlayer(player);
 										} else {
 											player = new Player(
 													Integer.parseInt(customPlayerNewMaxHP),
 													Integer.parseInt(customPlayerNewCurrentHP),
 													Integer.parseInt(customPlayerNewMaxSurges),
 													Integer.parseInt(customPlayerNewCurrentSurges),
 													Integer.parseInt(customPlayerNewHS));
 											Toast.makeText(
 													getApplicationContext(),
 													getString(R.string.TOAST_madePartialHPPlayer),
 													Toast.LENGTH_SHORT).show();
 											relaunchWithPlayer(player);
 										}
 									}
 
 								}
 							}).setNegativeButton(R.string.abandonPlayer, null)
 					.create();
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.newPlayer:
 			relaunchWithPlayer(new Player());
 			Toast.makeText(getApplicationContext(),
 					getString(R.string.TOAST_madeDefaultPlayer),
 					Toast.LENGTH_SHORT).show();
 			return true;
 		case R.id.newCustomPlayer:
 			showDialog(DIALOG_NEW_CUSTOM_PLAYER);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void relaunchWithPlayer(Player newPlayer) {
 		player = newPlayer;
 		currentHPView.setText(Integer.toString(player.getHP()));
 		inputSurges.setText(getResources().getString(R.string.surges)
 				+ COLON_SPACE + Integer.toString(player.getSurges()));
 		ongoUpdater(BLANK);
 		DSUpdater(BLANK);
 		surgesUpdater(BLANK);
 		inputHS.setText(getResources().getString(R.string.hs) + COLON_SPACE
 				+ Integer.toString(player.getHS()));
 		togglesUpdater();
 		tempHPUpdater();
 		/*
 		 * Because of the way they're created, player.changeHistory and
 		 * player.HPHistory are always the same length. First, removeAllViews
 		 * just in case any were already here.
 		 */
 		showWorkLayout.removeAllViews();
 		for (int i = 0; i < player.getChangeHistory().size(); i++) {
 			showWorkViewMaker(player.getChangeHistory().get(i), player
 					.getHPHistory().get(i));
 		}
 	}
 
 	// Sets all toggle buttons to the states from the Player class
 	public void togglesUpdater() {
 		toggleBlinded.setChecked(player.isBlinded());
 		toggleDazed.setChecked(player.isDazed());
 		toggleDominated.setChecked(player.isDominated());
 		toggleGrabbed.setChecked(player.isGrabbed());
 		toggleMarked.setChecked(player.isMarked());
 		toggleProne.setChecked(player.isProne());
 		toggleStunned.setChecked(player.isStunned());
 		toggleWeakened.setChecked(player.isWeakened());
 	}
 
 	/*
 	 * Method injures or heals the player based on input, then calls for the
 	 * view maker to show work
 	 */
 	public void showWorkUpdater(int value) {
 		// First we must pick our operation.
 		if (value > 0) {
 			player.heal(value);
 		} else if (value < 0) {
 			player.injure(-value);
 		} else {
 			clearEntry();
 			return;
 		}
 		showWorkViewMaker(value);
 		clearEntry();
 
 		currentHPView.setText(Integer.toString(player.getHP()));
 	}
 
 	/*
 	 * Use the player's HP if an hpToList was not specified. Generally, this
 	 * will be the case
 	 */
 	public void showWorkViewMaker(int value) {
 		showWorkViewMaker(value, player.getHP());
 	}
 
 	/*
 	 * Method which adds the red or green numbers and new current HP values to
 	 * showWorkView
 	 */
 	public void showWorkViewMaker(int value, int hpToList) {
 		String operation = BLANK;
 		TextView adjustment = new TextView(this);
 		TextView sum = new TextView(this);
 		if (value > 0) {
 			adjustment.setTextColor(Color.GREEN);
 		} else if (value < 0) {
 			adjustment.setTextColor(Color.RED);
 			/*
 			 * if we do not clear operation at this point, an extra "-" will
 			 * appear
 			 */
 			operation = BLANK;
 		}
 		// First line shows how much was added or subtracted as +n or -n
 		adjustment.setText(operation + Integer.toString(value));
 		adjustment.setGravity(Gravity.RIGHT);
 		// 14 px converted to 14 dip
		adjustment.setTextSize(dpi(14));
 		// Second line shows new total number
 		sum.setText(Integer.toString(hpToList));
 		sum.setGravity(Gravity.LEFT);
 
 		if (hpToList == player.getMaxHP() && !player.isDefaultPlayer()) {
 			sum.setTextColor(Color.GREEN);
 		} else if (player.isBloodied() && !player.isDefaultPlayer()) {
 			sum.setTextColor(Color.YELLOW);
 		} else if (player.isDying()) {
 			sum.setTextColor(Color.RED);
 		}
 
 		// 14 px converted to 14 dip
		sum.setTextSize(dpi(14));
 		// Now commit those lines to the view
 		showWorkLayout.addView(adjustment);
 		showWorkLayout.addView(sum);
 		showWorkScroller.post(new Runnable() {
 			public void run() {
 				// I tried a .fullScroll(ScrollView.FOCUS_DOWN), but this
 				// failed to scroll ALL the way
 				// This solution provides an alternative.
 				showWorkScroller.scrollTo(showWorkLayout.getMeasuredWidth(),
 						showWorkLayout.getMeasuredHeight());
 			}
 		});
 		tempHPUpdater();
 	}
 
 	// Controls adding temporary HP to player class
 	public void tempHPUpdater(int value) {
 		player.addTHP(value);
 		currentTHPView.setTextColor(Color.GREEN);
 		if (player.getTHP() > 0) {
 			currentTHPView.setText("(" + player.getTHP() + ")");
 		} else {
 			currentTHPView.setText("");
 		}
 	}
 
 	// If no value is passed to tempHPUpdater, use a 0 value
 	public void tempHPUpdater() {
 		tempHPUpdater(0);
 	}
 
 	/*
 	 * Controls adding and removing death saves to player and updating the
 	 * button's text to reflect changes
 	 */
 	public void DSUpdater(String how) {
 		if (how.equals(PLUS)) {
 			player.addDeathSave();
 		} else if (how.equals(MINUS)) {
 			player.remDeathSave();
 		}
 		inputDS.setText(getResources().getString(R.string.ds) + COLON_SPACE
 				+ Integer.toString(player.getDeathSaves()));
 	}
 
 	/*
 	 * Controls adding and removing healing surges to player and updating the
 	 * button's text to reflect changes
 	 */
 	public void surgesUpdater(String how) {
 		if (how.equals(PLUS)) {
 			player.addSurge();
 		} else if (how.equals(MINUS)) {
 			player.remSurge();
 		}
 		inputSurges.setText(getResources().getString(R.string.surges)
 				+ COLON_SPACE + Integer.toString(player.getSurges()));
 	}
 
 	/*
 	 * Controls adding and removing ongoing damage to the player and updating
 	 * the button's text to reflect changes TODO: Is UX here best? Turning to
 	 * regen in negative numbers may be confusing.
 	 */
 	public void ongoUpdater(String how) {
 		String dotOrHot, valueToUse;
 		// Pick operation and adjust currentOngo number
 		if (how.equals(PLUS)) {
 			player.addOngo();
 		} else if (how.equals(MINUS)) {
 			player.remOngo();
 		}
 		// It's a regen if it is under 0, otherwise it is ongoing
 		if (player.getOngo() < 0) {
 			dotOrHot = getResources().getString(R.string.regen);
 			valueToUse = Integer.toString(player.getRegen());
 		} else {
 			dotOrHot = getResources().getString(R.string.ongoing);
 			valueToUse = Integer.toString(player.getOngo());
 		}
 		// Change the button text to reflect variable
 		inputOngo.setText(dotOrHot + COLON_SPACE + valueToUse);
 	}
 
 	/*
 	 * Displays the numbers that represent the current entry based on key
 	 * presses.
 	 */
 	public void currentEntryViewUpdater(int updateWith) {
 		if (currentEntry * 10 > 999) {
 			return;
 		} // Max 3 digit number in currentEntry
 		currentEntry = (currentEntry * 10) + updateWith;
 		currentEntryView.setText(Integer.toString(currentEntry));
 	}
 
 	public void clearEntry() {
 		currentEntry = 0;
 		currentEntryView.setText(Integer.toString(currentEntry));
 	}
	
	// Turns a pixel size (int) to a dpi size.
	public float dpi(int px) {
		return px * getResources().getDisplayMetrics().density + 0.5f;
	}
 }
