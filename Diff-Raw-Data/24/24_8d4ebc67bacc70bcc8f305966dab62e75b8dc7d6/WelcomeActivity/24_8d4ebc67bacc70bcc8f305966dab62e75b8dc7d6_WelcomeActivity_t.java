 package edu.cis350.mosstalkwords;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class WelcomeActivity extends UserActivity {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_menu);
 	}
 	
 	public void onStart(){
 		super.onStart();
 		if(!isLoggedIn()){
 			Intent gotoBase = new Intent(this, BaseActivity.class);
 			startActivity(gotoBase);
 		}
 	}
 		
 	public void onResume(){
 		super.onResume();
 		TextView v = (TextView) findViewById(R.id.txtScore);
 		v.setText(getScore() + " Points");
 	}
 	
 	public void openWordQuest(View v){
 		Intent activityWordQuest = new Intent(this, WordQuestActivity.class);
 		startActivity(activityWordQuest);
 	}
 	
 	public void openFavourites(View v){
 		Intent activityMain= new Intent(this, MainActivity.class);
 		activityMain.putExtra("startFavourites", true);
 		startActivity(activityMain);
 	}
 	
 	public void openCategories(View v){
 		Intent i = new Intent(this, CategoryList.class);	
 		startActivity(i);
 		finish();
 	}
 	
 	public void resetUserData(View v) {
 		new AlertDialog.Builder(this)
 				.setTitle("Reset User Data")
 				.setMessage("Are you sure you want to reset all user data? This cannot be undone.")
 				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						logout();
 						new AlertDialog.Builder(WelcomeActivity.this)
 								.setTitle("Successfully reset")
 								.setMessage("All user data has been successfully reset. ")
 								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int id) {
 										dialog.cancel();
 										Intent gotoBaseMenu = new Intent(WelcomeActivity.this, BaseActivity.class);
 										startActivity(gotoBaseMenu);
 									}
 								})
 								.setCancelable(false)
 								.show();
 					}
 				})
 				.setNegativeButton("No", null)
 				.show();
 	}
 	
	public void sendDatabase(View v){
	}
 	
 }
