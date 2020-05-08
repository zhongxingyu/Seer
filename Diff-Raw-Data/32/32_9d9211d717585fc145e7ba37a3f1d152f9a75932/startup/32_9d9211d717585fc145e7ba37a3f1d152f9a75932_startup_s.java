 package gorails.com;
 
 
 
 
 
 
 import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.widget.HorizontalScrollView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TabWidget;
 
 public class startup extends TabActivity implements OnGestureListener {
     /** Called when the activity is first created. */
 
 	private static float velocity = 0;
     private GestureDetector gestureScanner;
     private HorizontalScrollView hScroll;
     private TabHost tabHost;
     private static int index = 0;
     @Override
     public void onCreate(Bundle savedInstanceState)
  
     {
     	setContentView(R.layout.main);
 
         super.onCreate(savedInstanceState);
         gestureScanner = new GestureDetector(this);        
         hScroll = (HorizontalScrollView) findViewById(R.id.HScroll);
         tabHost = getTabHost();
         tabHost.setPadding(10, 0, 10, 0);
         tabHost.setHorizontalScrollBarEnabled(true);
         TabWidget tab = tabHost.getTabWidget();
         tab.setPadding(10, 0, 10, 0);
        TabSpec newTab = tabHost.newTabSpec("Trip Planner")
        
         .setIndicator("  Trip Planner  ")
         .setContent(new Intent(this, TripPlanner.class)
         );
         tabHost.addTab(newTab);
 
         tabHost.addTab(tabHost.newTabSpec("Full Schedule")
                 .setIndicator("  Schedule  ")
                 .setContent(new Intent(this, Map.class)));
         tabHost.addTab(tabHost.newTabSpec("Fare Calculator")
                 .setIndicator("  Fare Calc  ")
                 .setContent(new Intent(this, Map.class)));
         tabHost.addTab(tabHost.newTabSpec("My Routes")
                 .setIndicator("  My Routes  ")
                 .setContent(new Intent(this, Map.class)));
         tabHost.addTab(tabHost.newTabSpec("Rail Maps")
                 .setIndicator("  Rail Maps  ")
                 .setContent(new Intent(this, Map.class)));
         tabHost.addTab(tabHost.newTabSpec("Help/Info")
                 .setIndicator("  Help/Info  ")
                 .setContent(new Intent(this, Map.class)));
         
         
         
  
     } // ----- End On Create ----- //
     @Override
     public void onResume(){
     	super.onResume();   	
     }
     
     
     @Override
     public boolean onTouchEvent(MotionEvent me){
         return gestureScanner.onTouchEvent(me);
     }
     @Override 
     public boolean onDown(MotionEvent e){
     	//text1.setText("-" + "DOWN" + "-");
         return true; 
     }
     @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){    	
     	if(velocityX<-1000){
     		//go forward
     		if(index<=4){
     			index++;
         		tabHost.setCurrentTab(index);
         		hScroll.scrollBy(100, 0);
     		}    		
     	}
     	else if(velocityX>1000){
     		//go back
     		if(index!=0){
     			index--;
     			tabHost.setCurrentTab(index);
     			hScroll.scrollBy(-100, 0);
     		}
     	}    	
         return true; 
     }
     @Override 
     public void onLongPress(MotionEvent e){
     }
     @Override
     public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
     	
         return true;
     } 
     @Override 
     public void onShowPress(MotionEvent e){
     }    
     @Override 
     public boolean onSingleTapUp(MotionEvent e){
         return true;
     }
   
 	
     
 }
