 package edu.grinnell.helpdeskmobile;
 
import edu.grinnell.helpdeskmobile.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HelpdeskAppActivity extends ListActivity {
 	/* Global variables for the activity go here. */
 
 	/* Called when the activity is first created. */
 	@Override	    
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		String[] actions = getResources().getStringArray(R.array.Actions);
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, actions));
 		ListView lv = getListView();
 
 		lv.setTextFilterEnabled(true);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				
 				switch (position) {
 
 				// call helpdesk
 				case 0:
 					try {
 						Intent intent = new Intent(Intent.ACTION_CALL);
 						intent.setData(Uri.parse("tel:+16412694400"));
 						startActivity(intent);
 					} catch (Exception e) {
 						Log.e("Helpdesk Mobile", "Failed to invoke call", e);
 					}
 					break;
 
 					// TODO: text helpdesk
 				case 1:
 					Context context = getApplicationContext();
 					CharSequence text = "text message helpdesk placeholder";
 					int duration = Toast.LENGTH_SHORT;
 					Toast toast = Toast.makeText(context, text, duration);
 					toast.show();
 					break;
 
 					// TODO: email helpdesk
 				case 2:
 					Context context2 = getApplicationContext();
 					CharSequence text2 = "email helpdesk placeholder";
 					int duration2 = Toast.LENGTH_SHORT;
 					Toast toast2 = Toast.makeText(context2, text2, duration2);
 					toast2.show();
 					break;
 
 					// TODO: chat with helpdesk
 				case 3:
 					Context context3 = getApplicationContext();
 					CharSequence text3 = "chat with helpdesk placeholder";
 					int duration3 = Toast.LENGTH_SHORT;
 					Toast toast3 = Toast.makeText(context3, text3, duration3);
 					toast3.show();
 					break;
 				}
 			}
 		});
 	}	
 
 	// old attempt at doing action listeners
 	// TODO Finish method.  Not much will be going here though, because this app doesn't really need to update anything.
 
 	/* Listener invokes the ACTION_CALL intent when the user wants to call the Helpdesk. */
 	/*private class phoneButtonListener implements OnClickListener {
 		public void onClick(View v) {
 
 
 			try {
 				Intent intent = new Intent();
 				intent.setData(Uri.parse("tel:6412694400"));
 				startActivity(intent);
 			} catch (Exception e) {
 				Log.e("SampleApp", "Failed to invoke call", e);
 			}
 
 
 
 			/*AlertDialog.Builder builder = new AlertDialog.Builder(null); // TODO: Check whether or not this line works (provided null as context).
 			builder.setMessage("Before calling the Helpdesk, make sure that you're ready to:\nAre you ready to proceed?")
 			       .setCancelable(false)
 			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			    	   public void onClick(DialogInterface dialog, int which) {
 						// TODO Call the intent to call the Helpdesk number.
 			    	   }
 			       })
 			       .setNegativeButton("No", new DialogInterface.OnClickListener() { // User is not ready to call, cancel the dialog.
 			    	   public void onClick(DialogInterface dialog, int which) {
 			    		   dialog.cancel();
 			    	   }
 			       });
 			AlertDialog alert = builder.create(); // Creates the dialog.*/
 	//	}
 }
 
 /* Listener invokes the e-mail intent when the user wants to e-mail the helpdesk. */
 //}
