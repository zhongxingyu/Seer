 package test.ThreeDAR.beta;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
 import android.widget.FrameLayout;
 
 public class ThreeDARMainActivity extends Activity {
 	
 	private PreviewView _previewView;
 	private FrameLayout _layout;
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        
        //Set the activity to be NO_TITLE and NO_STATUS_BAR:
        final Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
         try{
         	//Setting the _previewView as the main view of the activity:
             _previewView = new PreviewView( this.getApplicationContext() );
             _layout = new FrameLayout( this.getApplicationContext() );
             setContentView(_layout);
             _layout.addView(_previewView);
         } 
         catch(Exception e){}
     }
 }
