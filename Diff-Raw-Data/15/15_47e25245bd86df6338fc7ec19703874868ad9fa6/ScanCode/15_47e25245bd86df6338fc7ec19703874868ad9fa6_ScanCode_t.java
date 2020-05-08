 
 package kap.placity_beta;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 
 public class ScanCode extends Activity {
 
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_scan_code);
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_scan_code, menu);
         return true;
     }
     
     public void startGame(View view) {
 //    	Intent download = new Intent (this, DownloadService.class);
//		  download.putExtra("ID", "161");
 //		  startService(download);
 //	    	
 //		  Intent i = new Intent (this, MainActivity.class);
//		  i.putExtra("ID", "161");
 //		  i.putExtra("sender", "scanCode");
 //		  startActivity(i);  
     	
     	IntentIntegrator integrator = new IntentIntegrator(this);
     	integrator.initiateScan(); //Start external barcode scanner
     	
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent); //get result from external barcode scanner
     	  if (resultCode == RESULT_OK) {
     		  Intent download = new Intent (this, DownloadService.class);
     		  download.putExtra("ID", scanResult.getContents().substring(scanResult.getContents().lastIndexOf("?")+1));
     		  startService(download); //start download service with scanned id
     	    	
     		  Intent i = new Intent (this, MainActivity.class);
     		  i.putExtra("ID", scanResult.getContents().substring(scanResult.getContents().lastIndexOf("?")+1));
     		  i.putExtra("sender", "scanCode");
     		  i.putExtra("loading", true);
     		  startActivity(i); //start main activity
     	  }
     	  else {
     		  Intent i = new Intent (this, ScanCode.class); //Return to activity
     		  startActivity(i);
     	  }
     	}
 }
 
