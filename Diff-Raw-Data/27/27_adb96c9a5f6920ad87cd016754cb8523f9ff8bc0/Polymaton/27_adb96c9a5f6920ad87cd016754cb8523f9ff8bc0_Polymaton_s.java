 package com.wolliw.polymaton;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.MotionEvent;
 import android.widget.LinearLayout;
 import android.widget.EditText;
 
 public class Polymaton extends Activity
 {
 	private Board board;
 
 	// Dialogs for option menu
 	private static final int DIALOG_COLOR_PICKER = 0;
 	private static final int DIALOG_SETSPEED = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 
 		// Check for a board file path to open
 		String fileName = "default.json";
 		Bundle e = getIntent().getExtras();
 		if (e != null) {
 			if (e.getString("file_path") != null)
 				fileName = e.getString("file_path");
 		}
		android.util.Log.d("Poly",fileName);
 
 		super.onCreate(savedInstanceState);
 
 		board = new Board(this,fileName);
 		setContentView(board);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		board.stopUpdateThread();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		board.startUpdateThread();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		//Update the Pause/Play menu item based on current status of board
 		MenuItem mi = menu.findItem(R.id.pause_play_simulator);
 		if (board.isPaused())
 			mi.setTitle(R.string.main_menu_play);
 		else
 			mi.setTitle(R.string.main_menu_pause);
 
 		// show right show/hide id text
 		mi = menu.findItem(R.id.config_show_hide_ids);
 		if (board.isShowingIDs())
 			mi.setTitle(R.string.main_menu_config_hide_ids);
 		else
 			mi.setTitle(R.string.main_menu_config_show_ids);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.pick_board:
 				Intent i = new Intent(this, BoardListActivity.class);
 				startActivity(i);
 				return true;
 			case R.id.pause_play_simulator:
 				board.pauseUnpause();
 				return true;
 			case R.id.config_speed:
 				showDialog(DIALOG_SETSPEED);
 				return true;
 			case R.id.config_show_hide_ids:
 				board.toggleShowIDs();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch(id) {
 			case DIALOG_SETSPEED:
 				final SetSpeedDialog dialog = new SetSpeedDialog(this, this.board);
 				return dialog;
 			//case R.id.dialog_color_picker:
 			//	break;
 			default:
 				return null;
 		}
 	}
 
 	// Called for submenu dialog clicks
 	public void onClickDialog(View v) {
 		switch (v.getId()) {
 			case R.id.dialog_setspeed_button_ok:
 				LinearLayout ll = (LinearLayout) v.getParent();
 				EditText et = (EditText) ll.findViewById(R.id.dialog_setspeed_edittext);
 				if (et.getText().toString().compareTo("") != 0) {
 					int speed = Integer.parseInt(et.getText().toString());
 					if (speed > 1)
 						board.setSpeed(speed);
 					else
 						board.setSpeed(0);
 				}
 				board.stopUpdateThread();
 				board.startUpdateThread();
 				dismissDialog(DIALOG_SETSPEED);
 		}
 	}
 
 }
