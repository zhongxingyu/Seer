 package edu.cornell.opencomm;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 /** The main chat screen with EVERYBODY'S icon in it 
  * 
  * 1) Menu button @ bottom-left, should open up into an "Add Private Space" and "Delete PrivateSpace"
  * 2) Bottom bar with icons representing all existing Private Spaces
  * 3) Main button @ bottom-right, should take you back to this window
  */
 
 
 
 public class MainChat extends Activity {
     /** Called when the activity is first created. */
 	private ScreenView root;
 	private MainSpace space;
 	LinearLayout.LayoutParams PSparams = new LinearLayout.LayoutParams(
 			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
 			0.0f);
 	
 	
     @Override
     /** Initialize all views (buttons, bars, etc.) and set OnClickListeners for necessary components
      * 
      */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){
 
 			public void uncaughtException(Thread thread, Throwable ex) {
 				Log.e("TAG_STRING", "Uncaught Exception Caught", ex);
 			}
         
         });
         setContentView(R.layout.main);
 
         // Create a space with everyone in it (the main space)
         space = new MainSpace(this);
         space.initializeEveryone(); // add everyone to it!
         
         root = (ScreenView) findViewById(R.id.space_view);
         root.setSpace(space);
         root.invalidate();
         
         
         
         
      // set listener to main button
         Button mainButton = (Button)findViewById(R.id.main_button);
         mainButton.setOnTouchListener( new View.OnTouchListener() {
 			public boolean onTouch(View view, MotionEvent evt) {
 				switch(evt.getAction()){
 				case MotionEvent.ACTION_DOWN:		
 					break;
 				case MotionEvent.ACTION_MOVE:
 					break;
 				case MotionEvent.ACTION_UP:
					createNewPrivateSpace();
 					break;
 				}
 				return false;
 			}
 		}); 
      // set listener to trash button
         Button trashButton = (Button)findViewById(R.id.trash_button);
          trashButton.setOnTouchListener( new View.OnTouchListener() {
 			public boolean onTouch(View view, MotionEvent evt) {
 				switch(evt.getAction()){
 				case MotionEvent.ACTION_DOWN:		
 					break;
 				case MotionEvent.ACTION_MOVE:
 					break;
 				case MotionEvent.ACTION_UP:
 					for(PrivateSpace p: PrivateSpace.currentSpaces){
 					   if(p.isSelected())
 							removePrivateSpace(p);
 					}  
 					break;
 				}
 				return false;
 			}
 		});  
     }
     
     // add a new private space, that you can add to the xml file
     public PrivateSpace createNewPrivateSpace(){
     	 LinearLayout bottomBar = (LinearLayout)findViewById(R.id.privateSpaceLinearLayout);
          PrivateSpace p = new PrivateSpace(this);
          p.setLayoutParams(PSparams);
          p.setMinimumWidth(50);
          bottomBar.addView(p);  
          bottomBar.invalidate();
          return p;
     }
     
     // remove a private space button from the screen
     public void removePrivateSpace(PrivateSpace p){
     	LinearLayout bottomBar = (LinearLayout)findViewById(R.id.privateSpaceLinearLayout);
     	bottomBar.removeView(p);
     	bottomBar.invalidate();
     	PrivateSpace.currentSpaces.remove(p);
    	PrivateSpace.privateSpaceCounter--;
     }
     
 
 }
