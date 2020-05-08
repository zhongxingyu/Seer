 package kap.placity_beta;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class JavaScriptInterface extends Activity{
 	
 	private Context jsContext;
 
     /** Instantiate the interface and set the context */
     JavaScriptInterface(Context c) {
         jsContext = c;
     }
     
     public void get_qrcode() {
     	IntentIntegrator integrator = new IntentIntegrator(this);
     	integrator.initiateScan();
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent intent) { //On return from QR-Scanner!
   	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent); //get result from external barcode scanner
   	  if (scanResult != null) {
   		  MainActivity.myWebView.loadUrl("javascript:onScan('" + scanResult.getContents() + "');");
   	  }
   	}
     
    public boolean isResume() {
    	return false; //TODO: Implement save position on break
    }
    
    public int getResumeCount() {
    	return 0; //TODO: Get resume count from memory
    }
    
    public int getResumePoints() {
    	return 0; //TODO: Get resume points from memory
    }
    
     public void End() {
     	Log.v("end", "end");
     	Intent i = new Intent (jsContext, Startscreen.class);
     	jsContext.startActivity(i);
     }
     
     public void alert() {
     	Log.v("tag", "message");
     }
 }
