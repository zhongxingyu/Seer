 package com.example.tomatroid;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.util.Log;
 
 public class DialogManager implements OnClickListener {
 
 	int tag = 0;
 	MainActivity mA;
 	AlertDialog dialog;
 	boolean longBreak = false;
 
 	public DialogManager(MainActivity mA) {
 		this.mA = mA;
 	}
 
 	public void show(int tag, boolean longBreak) {
 		// Cancel previous Dialog
 		if (dialog != null) {
 			dialog.cancel();
 		}
 
 		this.tag = tag;
 		this.longBreak = longBreak;
 
 		// Start new Dialog
 		switch (tag) {
 		// Pomodoro
 		case 0:
 			if (longBreak) {
 				dialog = getBeforePomodoroDialog(mA);
 				dialog.setTitle("Long Breake!");
 				dialog.setMessage("You are done with your Pomodoro, lets take a long break.");
 			} else {
 				dialog = getBeforePomodoroDialog(mA);
 				dialog.setTitle("Short Break!");
 				dialog.setMessage("You are done with your Pomodoro, lets take a short break.");
 			}
 			break;
 		// After Short of Long break
 		default:
 			dialog = getBeforePomodoro(mA);
 			break;
 		}
 
 		dialog.show();
 	}
 
 	private AlertDialog getBeforePomodoroDialog(Context context) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setNegativeButton("Void it & Stop!", this);
 		builder.setNeutralButton("Skip it!", this);
 		builder.setPositiveButton("Take the break!", this);
 		return builder.create();
 	}
 
 	private AlertDialog getBeforePomodoro(Context context) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setTitle("Pomodoro Time!");
 		builder.setMessage("Your break is over, lets do some work.");
 		builder.setNegativeButton("Void it & Stop!", this);
 		builder.setNeutralButton("Extend the break! (10min)", this);
 		builder.setPositiveButton("Do a pomodoro!", this);
 		return builder.create();
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		Log.e("Dialog", "Which " + which);
 		switch (which) {
 		// Button Void it & Stop everything
 		case -2:
 			mA.controlListener.stop();
 			break;
 		// Button Skip it / Extend Break
 		case -3:
 			switch (tag) {
 			// Is pomodoro, start pomodoro
 			case 0:
				mA.controlListener.start(0);
 				break;
 			// On break, so nothing
 			default:
 				break;
 			}
 			break;
 		// Button Take it
 		case -1:
 			switch (tag) {
 			// Is pomodoro, start a break
 			case 0:
 				// Start Long or Short
 				if (longBreak) {
 					mA.controlListener.start(2);
 				} else {
 					mA.controlListener.start(1);
 				}
 				break;
 			// Is break, start pomodoro
 			default:
 				mA.controlListener.start(0);
 				break;
 			}
 			break;
 		}
 	}
 }
