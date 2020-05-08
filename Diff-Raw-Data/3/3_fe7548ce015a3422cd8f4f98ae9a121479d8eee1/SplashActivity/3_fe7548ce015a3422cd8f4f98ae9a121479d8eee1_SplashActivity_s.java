 package core.september.textmesecure;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import core.september.textmesecure.configs.Route;
 import core.september.textmesecure.supertypes.O9BaseActivity;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  * 
  * //@see SystemUiHider
  */
 public class SplashActivity extends O9BaseActivity {
 
 	private final static String TAG = SplashActivity.class.getSimpleName();
 	private ProgressBar progressBar;
 //	private enum Route {
 //		NEED_SIGNUP,
 //		NEED_SIGNIN,
 //		READY_TO_START
 //	}
 //	
 //	private User user = null;
 //	private Route route;
 
 
     @Override
    protected void onCreate(Bundle savedInstanceState) { 
     	while(imService == null) {}
     	Route route = imService.getRoute();
     	Toast.makeText(this, "Route is "+route, 300).show();
     	
     	
     }
 
 
 	
 
 
 	@Override
 	protected void onReceiveBrodcast(Context context, Intent intent) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	protected String[] getAction() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
    
 }
