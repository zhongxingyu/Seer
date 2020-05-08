 package at.roadrunner.android.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 import at.roadrunner.android.R;
 
 public class Roadrunner extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_roadrunner);
     }
    
     /*
      * Event onScan
      */
     public void onScanClick(View view) {
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.setPackage("com.google.zxing.client.android");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
     }
     
     /*
      * onActivityResult
      */
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	if (requestCode == 0 ) {
     		if (resultCode == RESULT_OK) {
                 String contents = intent.getStringExtra("SCAN_RESULT");
                 @SuppressWarnings("unused")
 				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
     	    	
                 Toast toast = Toast.makeText(getApplicationContext(), contents, 3);
     	    	toast.show();
             } else if (resultCode == RESULT_CANCELED) {
            	Toast toast = Toast.makeText(getApplicationContext(), R.string.roadrunner_exception_activitynotfound, 3);
    	    	toast.show();
             }
     	}
 	}
     
     /*
      * inflate menu
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.roadrunner_menu, menu);
         return true;
     }
     
     /*
      * Event OptionsMenuItemSelected
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.roadrunner_menu_settings:
         	startActivity(new Intent(this, Settings.class));
             return true;
         case R.id.roadrunner_menu_info:
         	startActivity(new Intent(this, Info.class));
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     /*
      *  TODO:
      *  
      *  bei APP start -> couchDB starten (meldung wenn nicht installiert)
      *  Einstellungen Dialog -> server IP einstellen
      *  local.ini editieren:
      *      - admin user anlegen
      *      - datenbank anlegen
      *      - datenbank von server replizieren (gefiltert)
      *      
      *  
      */
     
 }
 
 
