 package com.smarthome;
 
 import java.util.LinkedList;
 
 import rajawali.RajawaliActivity;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.text.method.Touch;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SmartHomeActivity extends RajawaliActivity implements OnTouchListener {
 
 	public Room room;
 	private LinkedList<Room> rooms;
 	
 	public boolean isDebug = true;
 	public DebugController debug;
 	public static int screenWidth;
 	public static int screenHeight;
 	public SmartHomeRenderer mRenderer;
 	public TextView label;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     	Display display = getWindowManager().getDefaultDisplay();
     	Point size = new Point();
     	display.getSize(size);
     	screenHeight = size.y;
     	screenWidth = size.x;
     	
     	System.out.println("--SCREEN--");
     	System.out.println("Width: "+screenWidth);
     	System.out.println("Height: "+screenHeight);
     	
     	setUpRooms();
     	
     	mRenderer = new SmartHomeRenderer(this);
     	mRenderer.setSurfaceView(mSurfaceView);
     	super.setRenderer(mRenderer);
     	mSurfaceView.setOnTouchListener(this);
     	
     	LinearLayout ll = new LinearLayout(this);
     	ll.setOrientation(LinearLayout.VERTICAL);
         ll.setGravity(Gravity.TOP);
         
         label = new TextView(this);
         label.setText("Info:");
         label.setTextSize(20);
         label.setGravity(Gravity.LEFT);
         label.setHeight(100);
         ll.addView(label);
          
         mLayout.addView(ll);
     } 
 
 	public boolean onTouch(View v, MotionEvent event) {
 		if( event.getAction() == MotionEvent.ACTION_DOWN) {
 			/*System.out.println("TOUCH! x:"+event.getX()+" y:"+event.getY());
 			if(event.getX() > 700) {
 				mRenderer.getCamera().setX(0.5f + mRenderer.getCamera().getX());
 			}else if(event.getX() < 50) {
 				mRenderer.getCamera().setX(-0.5f + mRenderer.getCamera().getX());
 			}
 			
 			if(event.getY() > 400) {
 				mRenderer.getCamera().setY(-0.5f + mRenderer.getCamera().getY());
 			}else if(event.getY() < 50) {
 				mRenderer.getCamera().setY(0.5f + mRenderer.getCamera().getY());
 			}
 			if(event.getY() <= 400 && event.getY() >= 50 && event.getX() <= 700 && event.getX() >= 50){
 				System.out.println("Camera Info:");
 				System.out.println("X: "+mRenderer.getCamera().getX());
 				System.out.println("Y: "+mRenderer.getCamera().getY());
 				System.out.println("Z: "+mRenderer.getCamera().getZ());
 			}*/
 			if (isDebug && mRenderer != null)
 				debug.fire(event.getX(), event.getY(), this);
 			else if (room != null)
 				room.fire(event.getX(), event.getY(), this);
 		}
 		if(event.getAction() == MotionEvent.ACTION_MOVE) {
 			System.out.println("Moved!");
 		}
 		return super.onTouchEvent(event);
 	}
 
 	private void setUpRooms() {
 		debug = new DebugController(10f, 10f, 99, this);
 		
 		rooms = new LinkedList<Room>();
 		
 		//Fge Rume hinzu
 		
 		//Raum: Esszimmer
 		rooms.add(new Room(7f, -8f, 0));
 		
 		//Raum: Kche
 		rooms.add(new Room(25.5f, -8f, 1));
 		
 		//Raum: Schlafzimmer
 		rooms.add(new Room(12.0f, -30.5f, 2));
 		
 		//Raum: Wohnzimmer
 		rooms.add(new Room(41.5f, -8f, 3));
 		
 		//Raum: Flur
 		rooms.add(new Room(17.5f, -19f, 4));
 		
 		//Fge die Raumwechsel hinzu
 		rooms.get(0).gestures.add(new RoomGesture(700, 0, 800, 480, rooms.get(1)));	//Esszimmer -> Kche
 		rooms.get(0).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(4)));	//Esszimmer -> Flur
 
 		rooms.get(1).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(0)));	//Kche	-> Esszimmer
 		rooms.get(1).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(4)));	//Kche	-> Flur
 		rooms.get(1).gestures.add(new RoomGesture(700, 0, 800, 480, rooms.get(3)));	//Kche	-> Wohnzimmer
 		
 		rooms.get(2).gestures.add(new RoomGesture(0, 0, 800, 100, rooms.get(4)));
 		
 		rooms.get(3).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(1)));
 		
 		rooms.get(4).gestures.add(new RoomGesture(0, 0, 400, 100, rooms.get(0)));
 		rooms.get(4).gestures.add(new RoomGesture(401, 0, 800, 100, rooms.get(1)));
 		rooms.get(4).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(2)));
 		
 		//Fge die Lichtsteuerung hinzu
 		rooms.get(0).gestures.add(new LightGesture(100,100,700,380, "dining_light_color"));
 		rooms.get(1).gestures.add(new LightGesture(100,100,700,380, "kitchen_light_color"));
 		rooms.get(2).gestures.add(new LightGesture(100,100,700,380, "lounge_light_color"));
 		rooms.get(3).gestures.add(new LightGesture(100,100,700,380, "sleeping_light_color"));
 		rooms.get(4).gestures.add(new LightGesture(100,100,700,380, "corridor_light_color"));
 		
		this.room = rooms.get(0);
 	}
 }
