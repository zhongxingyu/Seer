 package ovgu.gruppe1.ehskapp;
 
 import java.io.FileNotFoundException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
 		String usercode = preferences.getString("usercode", "");
 		Editor edit = preferences.edit();
 				
 		if (usercode.length() != 0) {
 			
 			String data = usercode + ".csv";
			if (true) {
 				edit.clear();
 				edit.commit();
 			}
 			
 		}
 		
 		
 		if (usercode.length() == 0) {
 			
 			Intent popupIntent = new Intent(this, UserCodeActivity.class);
 			startActivity(popupIntent);
 		}
 				
 
 		Button btn_usercode = (Button) findViewById(R.id.btn_usercode);
 		btn_usercode.setOnClickListener(new OnClickListener() {
 
 
 			@Override
 			public void onClick(View v) {
 				Intent popupIntent = new Intent(MainActivity.this, UserCodeActivity.class);
 				startActivity(popupIntent);
 			}
 		});
 		
 		
 		
 		
 		Button btn_preferences = (Button) findViewById(R.id.btn_preferences);
 		btn_preferences.setOnClickListener(new OnClickListener() {
 		
 			
 			@Override
 			public void onClick(View v) {
 
 				/*String[] str1 = { "Code", "Datum", "Alarmzeit", "Antwortzeit",
 						"Abbruch", "Kontakte", "Stunden", "Minuten" };
 				String[] str2 = { "lbrht", "22.06.2013", "21:23", "21:24", "0",
 						"0", "0", "0" };
 				try {
 					CSVWriter.writeLine(str1, Environment.getExternalStorageDirectory().getPath()+"/Probandencode.csv");
 					CSVWriter.writeLine(str2, Environment.getExternalStorageDirectory().getPath()+"/Probandencode.csv");
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					Toast.makeText(getApplicationContext(), "External SD card not mounted", Toast.LENGTH_LONG).show();
 					e.printStackTrace();				
 				}*/
 				
 				
 //				
 //				Intent intent = new Intent(MainActivity.this,
 //						QuickPrefsActivity.class);
 //				startActivity(intent);
 			}
 		});
 	
 		Button btn_popup = (Button) findViewById(R.id.btn_popup);
 		btn_popup.setOnClickListener(new OnClickListener() {
 
 
 			@Override
 			public void onClick(View v) {
 				Intent popupIntent = new Intent(MainActivity.this, TimeChooserActivity.class);
 				startActivity(popupIntent);
 			}
 		});
 		
 		Button btn_questions = (Button) findViewById(R.id.btn_questions);
 		btn_questions.setOnClickListener(new OnClickListener() {
 
 
 			@Override
 			public void onClick(View v) {
 				Intent popupIntent = new Intent(MainActivity.this, QuestionsActivity.class);
 				startActivity(popupIntent);
 			}
 		});
 
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	/*public boolean onOptionsItemSelected(MenuItem item) {
 	    switch(item.getItemId()) {
 	        case R.id.menuitem_preferences:
 	            Intent intentPreferences = new Intent(MainActivity.this, QuickPrefsActivity.class);
 	            startActivity(intentPreferences);
 	            return true;
 	        case R.id.menuitem_popup:
 	        	Intent intentPopup = new Intent(MainActivity.this, PopupActivity.class);
 	            startActivity(intentPopup);
 	            return true;
 	    }
 
 
 	    return super.onOptionsItemSelected(item);
 	} */
 
 }
