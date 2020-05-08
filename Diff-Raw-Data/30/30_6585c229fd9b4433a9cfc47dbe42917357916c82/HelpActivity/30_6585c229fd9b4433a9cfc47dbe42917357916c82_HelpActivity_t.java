 package dk.aau.cs.giraf.cars;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.Menu;
 import android.view.View;
 
 public class HelpActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_help);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.help, menu);
 		return true;
 	}
 
 	public void showMicHelp(View v) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(R.string.mic);
		alertDialogBuilder.setMessage(R.string.michelptext);
 
		alertDialogBuilder.setPositiveButton(R.string.okay ,new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// Dismiss dialog
 			}
 		});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 
 	public void showSettingsHelp(View v) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(R.string.settings);
		alertDialogBuilder.setMessage(R.string.micsettingstext);
 
		alertDialogBuilder.setPositiveButton(R.string.okay ,new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// Dismiss dialog
 			}
 		});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 
 
 	public void showGameHelp(View v) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(R.string.gamehelp);
		alertDialogBuilder.setMessage(R.string.gamehelptext);
 
		alertDialogBuilder.setPositiveButton(R.string.okay ,new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// Dismiss dialog
 			}
 		});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 }
