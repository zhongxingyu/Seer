 package uk.co.ipodling.skeuosc;
 
 import java.net.SocketException;
 import uk.co.ipodling.skeuosc.R;
 import android.widget.Toast;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.Window;
 /*
  * Bugs:
  * Moving slider is a bit hoopy
  * firstX position for grid returns as 0 instead of 90, causes snapping to left of screen
  * */
 public class MainActivity extends Activity{
 	//this is so badly coded it's nearly funny, one does not simply 
     private GestureDetector gestureDetector;
 	Networking network;
     DrawableView drawableView; 
     int index = 0;
 	private final static int START_DRAGGING = 0;
 	private final static int STOP_DRAGGING = 1;
 	private int status;
 	SkeuOSCPrefs prefs;//
 	GridSystem grid;
 	float width;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE); //kinda not good way to do it if it takes a second to load but gets the job done
 		final java.lang.Object[] args = {"hello"};
 		prefs = new SkeuOSCPrefs(getApplicationContext());
 		grid = new GridSystem(getApplicationContext());
 		width = grid.getGridSize();
         drawableView = new DrawableView(this);
         drawableView.setBackgroundColor(Color.WHITE);
         setContentView(drawableView);
 		super.onCreate(savedInstanceState);
 		try {
 			networkSetup(); // try to create socket
 		} catch (SocketException e1) {
 			Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
 			e1.printStackTrace();
 		}
         gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() { // crazy gesture detector of crazy 
             @Override
             public boolean onDoubleTap(MotionEvent e) {
         		for(int i = 0; i < DrawableView.myButtonArray.size(); i++){ //tests is event was near position of a button or toggle in their arrays
         			if (e.getX() <= DrawableView.myButtonArray.get(i).getX()+width/2 && e.getX() >= DrawableView.myButtonArray.get(i).getX()-width/2 &&
         					e.getY()-150 <= DrawableView.myButtonArray.get(i).getY()+width/2 && e.getY()-150 >= DrawableView.myButtonArray.get(i).getY()-width/2){
                         Toast.makeText(MainActivity.this, "/button"+Integer.toString(i), Toast.LENGTH_LONG).show();
         		}
         		}
         		for(int i = 0; i < DrawableView.myToggleArray.size(); i++){
         			if (e.getX() <= DrawableView.myToggleArray.get(i).getX()+width/2 && e.getX() >= DrawableView.myToggleArray.get(i).getX()-width/2 &&
         					e.getY()-150 <= DrawableView.myToggleArray.get(i).getY()+width/2 && e.getY()-150 >= DrawableView.myToggleArray.get(i).getY()-width/2){
         				DrawableView.myToggleArray.get(i).setXY(0, 20);
                         Toast.makeText(MainActivity.this, "/toggle"+Integer.toString(i), Toast.LENGTH_LONG).show();
         		}
         		}
                 return true;
             }
             @Override
             public boolean onSingleTapConfirmed(MotionEvent e) {
             	if (e.getX() > DrawableView.screenWidth - 200 && e.getY()-150 < 100){
     				Intent i = new Intent(getApplicationContext(), MainMenu.class);
     				startActivity(i);
             	}
         		for(int i = 0; i < DrawableView.myButtonArray.size(); i++){
         			if (e.getX() <= DrawableView.myButtonArray.get(i).getX()+width/2 && e.getX() >= DrawableView.myButtonArray.get(i).getX()-width/2 &&
         					e.getY() <= DrawableView.myButtonArray.get(i).getY()+width/2 && e.getY() >= DrawableView.myButtonArray.get(i).getY()-width/2){
         			Log.d("event:", "button");
         			index = i;
         	        	try{
         	        		String host = prefs.getHost();
         	        		int port = prefs.getPort();
         	        		if(host.matches("") || port == 0){
         	            		Toast.makeText(MainActivity.this, "Please set host and port in settings", Toast.LENGTH_SHORT).show();//  toasty
         	        		}else{
         	        			network.sendOSCMessage("/button"+Integer.toString(i), DrawableView.myButtonArray.get(i).getMessage());        	        				//here send button message
         	        		}
         			}catch(Exception ex){
         	    		Log.d("Exception:", ex.toString());
         	    	}
         		}
         		}
         		for(int i = 0; i < DrawableView.myToggleArray.size(); i++){
         			if (e.getX() <= DrawableView.myToggleArray.get(i).getX()+width/2 && e.getX() >= DrawableView.myToggleArray.get(i).getX()-width/2 &&
         					e.getY() <= DrawableView.myToggleArray.get(i).getY()+width/2 && e.getY() >= DrawableView.myToggleArray.get(i).getY()-width/2){
         			Log.d("event:", "toggle");
         			index = i; 			
         	        	try{
         	        		String host = prefs.getHost();
         	        		int port = prefs.getPort();
         	        		if(host.matches("") || port == 0){
         	            		Toast.makeText(MainActivity.this, "Please set host and port in settings", Toast.LENGTH_SHORT).show();//  toasty
         	        		}else{
         	        			network.sendOSCMessage("/toggle"+Integer.toString(i), DrawableView.myToggleArray.get(i).getMessage());
         	            		drawableView.invalidate();
         	        		}
         			}catch(Exception ex){
         	    		Log.d("Exception:", ex.toString());
         	    	}
         		}
         		}
         		drawableView.invalidate();
                 return true;
             }
         	public void onLongPress(MotionEvent e) {
         	    boolean button = false;
         	    boolean toggle = false;
        	    boolean slider = false;
         		for(int i = 0; i < DrawableView.myButtonArray.size(); i++){ //tests is event was near position of a button or toggle in their arrays
         			if (e.getX() <= DrawableView.myButtonArray.get(i).getX()+50 && e.getX() >= DrawableView.myButtonArray.get(i).getX()-50 &&
         					e.getY()-150 <= DrawableView.myButtonArray.get(i).getY()+50 && e.getY()-150 >= DrawableView.myButtonArray.get(i).getY()-50){
         				button = true;
 //        				Intent j = new Intent(getApplicationContext(), Inspector.class);
 //        				startActivity(j);
         		}
         		}
         		for(int i = 0; i < DrawableView.myToggleArray.size(); i++){
         			if (e.getX() <= DrawableView.myToggleArray.get(i).getX()+50 && e.getX() >= DrawableView.myToggleArray.get(i).getX()-50 &&
         					e.getY()-150 <= DrawableView.myToggleArray.get(i).getY()+50 && e.getY()-150 >= DrawableView.myToggleArray.get(i).getY()-50){
         				toggle = true;
 //                		Intent j = new Intent(getApplicationContext(), Inspector.class);
 //        				startActivity(j);
         		}
         		}
        		for(int i = 0; i < DrawableView.mySliderArray.size(); i++){
        			if (e.getX() <= DrawableView.mySliderArray.get(i).getX()+50 && e.getX() >= DrawableView.mySliderArray.get(i).getX()-50 &&
        					e.getY()-150 <= DrawableView.mySliderArray.get(i).getY()+75 && e.getY()-150 >= DrawableView.mySliderArray.get(i).getY()-75){
        				slider = true;
//                		Intent j = new Intent(getApplicationContext(), Inspector.class);
//        				startActivity(j);
        		}
        		}
        			if (button == false && toggle == false && slider == false) {
         				Intent j = new Intent(getApplicationContext(), AddItem.class);
         				startActivityForResult(j, 0);
         		}
             		drawableView.invalidate();
         	}
         	@Override
         	public boolean onSingleTapUp(MotionEvent e) {
         		return false;
         	}
         	@Override
         	public boolean onDown(MotionEvent e) {	
         		drawableView.invalidate();
         		return false;
         	}
         	
         });
 	}//end of method brace
 
 	@Override
 	public boolean onTouchEvent(MotionEvent me){
 		if (me.getAction() == MotionEvent.ACTION_DOWN) {
 			status = START_DRAGGING;
 		}
 		if (me.getAction() == MotionEvent.ACTION_UP) {
 			status = STOP_DRAGGING;
 			for(int i = 0; i < DrawableView.myButtonArray.size(); i++){
     			if(DrawableView.myButtonArray.get(i).isMoving()){
     				DrawableView.myButtonArray.get(i).placeWithCoords(me.getX(), me.getY());
     			}
     		}
 			for(int i = 0; i < DrawableView.myToggleArray.size(); i++){
     			if(DrawableView.myToggleArray.get(i).isMoving()){
     				DrawableView.myToggleArray.get(i).placeWithCoords(me.getX(), me.getY());
     			}
     		}
 			for(int i = 0; i < DrawableView.mySliderArray.size(); i++){
     			if(DrawableView.mySliderArray.get(i).isMoving()){
     				DrawableView.mySliderArray.get(i).placeWithCoords(me.getX(), me.getY());
     			}
     		}
 			Log.i("Drag", "Stopped Dragging");
 		} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
 			if (status == START_DRAGGING) {
         		for(int i = 0; i < DrawableView.myButtonArray.size(); i++){
         			if (me.getX() <= DrawableView.myButtonArray.get(i).getX()+width/2 && me.getX() >= DrawableView.myButtonArray.get(i).getX()-width/2 &&(
         					me.getY() <= DrawableView.myButtonArray.get(i).getY()+width/2 && me.getY() >= DrawableView.myButtonArray.get(i).getY()-width/2))
         					{
         				DrawableView.myButtonArray.get(i).move(me.getX(), me.getY(), true);  //move stuff
         			}
         		}	
         		for(int i = 0; i < DrawableView.myToggleArray.size(); i++){
         			if (me.getX() <= DrawableView.myToggleArray.get(i).getX()+width/2 && me.getX() >= DrawableView.myToggleArray.get(i).getX()-width/2 &&
         					me.getY() <= DrawableView.myToggleArray.get(i).getY()+width/2 && me.getY() >= DrawableView.myToggleArray.get(i).getY()-width/2){
         				DrawableView.myToggleArray.get(i).move(me.getX(), me.getY(), true);
         			}
         		}
         		for(int i = 0; i < DrawableView.mySliderArray.size(); i++){
         			if (me.getX() <= DrawableView.mySliderArray.get(i).getX()+width/2 && me.getX() >= DrawableView.mySliderArray.get(i).getX()-width/2 &&
         					(me.getY() <= DrawableView.mySliderArray.get(i).getY()+width && me.getY() >= DrawableView.mySliderArray.get(i).getY()-width)){
         				if(me.getX()>= DrawableView.mySliderArray.get(i).getX()-width/2+30 && me.getX()<= DrawableView.mySliderArray.get(i).getX()+width/2-30){
             				DrawableView.mySliderArray.get(i).setY(me.getX(), me.getY());
             				try{
             	        		String host = prefs.getHost();
             	        		int port = prefs.getPort();
             	        		if(host.matches("") || port == 0){
             	            		Toast.makeText(MainActivity.this, "Please set host and port in settings", Toast.LENGTH_SHORT).show();//  toasty
             	        		}else{
             	        			network.sendOSCMessage("/slider"+Integer.toString(i), DrawableView.mySliderArray.get(i).getMessage());
             	            		drawableView.invalidate();
             	        		}
             			}catch(Exception ex){
             	    		Log.d("Exception:", ex.toString());
             	    	}
         				}else{
         				DrawableView.mySliderArray.get(i).move(me.getX(), me.getY(), true);
         				}
         			}
         		}
         		drawableView.invalidate();
 			}
 		}
         if (!gestureDetector.onTouchEvent(me))
             return super.onTouchEvent(me);
         return true;
 //	return gestureScanner.onTouchEvent(me);
 	} // here be gesture stuff
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 		
 	public void networkSetup() throws SocketException{
 		network = new Networking(getApplicationContext());
 		if(prefs.isItThere("host") == true && prefs.isItThere("port") == true){
 			network.updateHost(prefs.getHost(), prefs.getPort());
 		}else{
     		Toast.makeText(MainActivity.this, "Please open setup and specify host and port", Toast.LENGTH_SHORT).show();
 		}
 
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		  if (requestCode == 0) {
 
 		     if(resultCode == RESULT_OK){ 
 		    	 if(prefs.isItThere("button") == true){
 		    		 if(prefs.getBoolean("button") == true){
 		    			 DrawableView.myButtonArray.add(new MyButton(getApplicationContext(), width-30));
 		    			 prefs.putBoolean("button", false);
 		    		 }
 		    	 }
 		    	 if(prefs.isItThere("toggle") == true){
 		    		 if(prefs.getBoolean("toggle") == true){
 		    			 DrawableView.myToggleArray.add(new MyToggle(getApplicationContext(), width-30));
 		    			 prefs.putBoolean("toggle", false);
 		    		 }
 		    	 }
 		    	 if(prefs.isItThere("slider") == true){
 		    		 if(prefs.getBoolean("slider") == true){
 		    			 DrawableView.mySliderArray.add(new MySlider(getApplicationContext(), width-30));
 		    			 prefs.putBoolean("slider", false);
 		    		 }
 		    	 }
 		     }
 		     }
 	}
 
 	
 }
