 package org.jbs.happysad;
 
import static android.provider.BaseColumns._ID;
import static org.jbs.happysad.Constants.EMO;
import static org.jbs.happysad.Constants.LAT;
import static org.jbs.happysad.Constants.LONG;
import static org.jbs.happysad.Constants.MSG;
import static org.jbs.happysad.Constants.TABLE_NAME;
import static org.jbs.happysad.Constants.TIME;
import static org.jbs.happysad.Constants.UID;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.util.Log;
 
 
 public class Prompt extends Activity implements OnClickListener{
 	private static final String TAG = "happy sad prompt";
 
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {

     	Log.d(TAG, "created"); 
    	Log.v(TAG, "CREATE TABLE "+ TABLE_NAME + 
				" (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				UID + " INTEGER, " + 
				LAT + " REAL, " +
				LONG + " REAL, " + 
				EMO + " REAL, " + 
				MSG + " TEXT, " + 
				TIME + " INTEGER");
						
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
     	
     	View sadButton = findViewById(R.id.sad_button);
     	View happyButton = findViewById(R.id.happy_button);
     	
     	happyButton.setOnClickListener(this);
     	sadButton.setOnClickListener(this);
   	  
     }
     
 	public void onClick(View v) {
 
 		Log.d(TAG, "clicked" + v.getId());
 		System.out.println(TAG + "clicked" + v.getId());
 		switch(v.getId()) {
 		case R.id.happy_button:
 			
 			Intent i = new Intent(this, More.class);
 			i.putExtra("Clicked", "Happy");
 			i.putExtra("Emotion", 1);
 			startActivity(i);
 			break;
 		case R.id.sad_button:
 			
 			Intent j = new Intent(this, More.class);
 			j.putExtra("Clicked", "Sad");
 			j.putExtra("Emotion", 0);
 			startActivity(j);
 			break;
 
 		}
 	}
 		
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 		
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.settings:
 			startActivity(new Intent(this, Prefs.class));
 			return true;
 			// More items go here (if any) ...
 			}
 		return false;
 	}
 }
