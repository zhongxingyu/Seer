 package test.ThreeDAR.beta;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.FrameLayout;
 
 public class ThreeDARMainActivity extends Activity {
 	
 	private PreviewView _previewView;
 	private FrameLayout _layout;
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
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
