 package edu.upenn.cis350;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class HomeActivity extends Activity {
 
 	/* Dialog Codes */
 	private final static int LOGOUT = 1;
 	
 	private String username;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.home);
 		
 		username = getIntent().getStringExtra(Constants.UNEXTRA);
 		
		TextView tv = (TextView)findViewById(R.id.homeTitle);
 		
		if (tv != null) {
			tv.setText("Welcome " + IOBasic.fullName(username));
 		}
 		
 	}
 	
 	
 	private void createDialog(int id) {
 	
 		if (id == LOGOUT) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(R.string.logoutMessage);
 			builder.setPositiveButton(R.string.yes,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							setResult(Constants.LOGOUT_SUCCESSFUL);
 							finish();
 						}
 					});
 			builder.setNegativeButton(R.string.no,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 			builder.create().show();
 		}
 		
 	}
 
 
 
 
 	public void onTapGame1 (View view) {
 		startActivity(new Intent(this, CalorieCounterActivity.class));
 	}
 	
 	public void onTapGame2 (View view) {
 		startActivity(new Intent(this, RankingGameActivity.class));		
 	}
 	
 	public void onTapLogout (View view) {
 		createDialog(LOGOUT);
 	}
 
 }
