 package com.hkw.assassins;
 
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class KillDialog extends DialogFragment {
 	SharedPreferences settings;
 	String target_name;
 
 	public KillDialog(SharedPreferences settings, String target_name) {
 		this.settings = settings;
 		this.target_name = target_name;
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceStage) {
 		View view = inflater.inflate(R.layout.kill_dialog, container);
 		getDialog().setTitle("Kill Success!");
 		TextView killText = (TextView) view.findViewById(R.id.killText);
 		killText.setText("You've just killed: " + target_name);
 
 		// Share button
 		Button shareButton = (Button) view.findViewById(R.id.killShare);
 		shareButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent sharingIntent = new Intent(
 						android.content.Intent.ACTION_SEND);
 				sharingIntent.setType("text/plain");
				String shareBody = "I've just killed: " + target_name
 						+ " in Assassins game!";
 				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 						"Assassins game is awesome!");
 				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
 						shareBody);
 				startActivity(Intent.createChooser(sharingIntent, "Share via"));
 			}
 		});
 
 		return view;
 	}
 }
