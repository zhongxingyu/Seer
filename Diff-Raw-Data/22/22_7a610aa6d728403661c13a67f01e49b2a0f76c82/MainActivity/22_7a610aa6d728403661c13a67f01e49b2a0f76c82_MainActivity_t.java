 package edu.berkeley.cs160.clairetuna.prog3;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	ImageMap mImageMap;
 	LocationManager manager;
 	public TripCostTask task;
 	public StationsTask stationsTask;
 	private HashMap<String, Double[]> stationCoordinates;
 	private HashMap<String, String> stationNames;
 	
 	LocationActivity locationActivity;
 	Location userLocation; 
 	MainView drawView;
 	ImageView pinA;
 	FrameLayout.LayoutParams pinParams;
 	FrameLayout fLayout;
 	FrameLayout ticketHolder;
 	int width = 200, height =100,  pinAMarginLeft = 10, marginRight =0, pinAMarginTop = 10, marginBottom = 0;
 	
 	@Override
 	//Try BitmapFactory.decodeFile() and then setImageBitmap() on the ImageView.
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.activity_main); 
 		ticketHolder = (FrameLayout) findViewById(R.id.ticketholder);
 		
 		
 		instantiateLayout();
 		
 		
 		
 		super.onCreate(savedInstanceState);
 
 	  
         
 		Log.i("MyApplication", "Starting application");
 //	    
 	   // updateLocation();
 	   getStationInfo();
 	}
 
 	FrameLayout myLayout;
 	FrameLayout.LayoutParams params;
 	
 public void instantiateLayout(){
 	drawView = new MainView(this);
 	fLayout = (FrameLayout)findViewById(R.id.mapholder);
 	fLayout.addView(drawView);
 	pinParams = new FrameLayout.LayoutParams(width, height);
 	
 	pinA = new ImageView(this);
 	Bitmap bitmapPinA = BitmapFactory.decodeResource(getResources(), R.drawable.pina);
 	pinA.setImageBitmap(bitmapPinA);
 	pinParams.setMargins(pinAMarginLeft, pinAMarginTop, marginRight, marginBottom);
 	fLayout.addView(pinA, pinParams);
 	int pinAMarginTop = getRelativeTop(pinA);
 	int pinAMarginLeft = getRelativeLeft(pinA);
 	
 	
 
 
 	ticket = new Ticket(this);
 	ticketHolder.addView(ticket);
 
 	
 }
 Ticket ticket;
 
 	public void updateTicket(){
 		ticket.drawCircle(10,10, 10);
 	}
 	private int getRelativeLeft(View myView) {
 	        return myView.getLeft();
 	}
 
 	private int getRelativeTop(View myView) {
 	        return myView.getTop();
 	}
 	
 	
 	public void drawSomething(){
 		
 	}
 	public void updateLocation(){
 		Log.i("MyApplication", "Updated Location");
 		LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		Log.i("MyApplication", "GOT A LOCATION SERVICE");
 		userLocation = locationActivity.checkLocation(manager);
 		
     	//LocationHelper locationHelper = new LocationHelper(this);
 	}
 	
 	public String closestStation(){
 		Log.i("MyApplication", "In MainActivity/closestStation");
 		
 		String closestStation=null;
 		updateLocation();
 		//Log.i("MyApplication", "User coords: " + userLocation.getLatitude()+ "," + userLocation.getLongitude());
 		Location locationB = new Location("point B");
 		float minDistance = Float.POSITIVE_INFINITY;
 		float currentDistance;
 		Log.i("MyApplication", "positive infinity: " + minDistance);
 
 		Log.i("MyApplication", "latitude of user is: "+ userLocation.getLatitude());
 		Log.i("MyApplication", "longitude of user is: "+ userLocation.getLongitude());
 		for (String key: stationCoordinates.keySet()){
 			Log.i("MyApplication", key + ": latitude in stationcoords is: " + stationCoordinates.get(key)[0]);
 			Log.i("MyApplication", key + ": longitude in stationcoords is: " + stationCoordinates.get(key)[1]);
 			
 			
 			locationB.setLatitude(stationCoordinates.get(key)[0]);
 			locationB.setLongitude(stationCoordinates.get(key)[1]);
 			
 			currentDistance = userLocation.distanceTo(locationB);
 			//Log.i("MyApplication", "Distance to " + key.toUpperCase() + " is: "+ currentDistance);
 			if (currentDistance< minDistance){
 				minDistance=currentDistance;
 				closestStation = key;
 			}
 		}
 		Log.i("MyApplication", "Closest station is: " + closestStation);
 		return closestStation;
 	}
 	
 	
 	
 	public boolean isOnPinA(float x, float y){
 		boolean xProper = (x>=pinAMarginLeft && x<=pinAMarginLeft + width);
 		boolean yProper = (y>=pinAMarginTop && y<= pinAMarginTop + height);
 		return xProper && yProper;
 	}
 	
 	public void setCoordinates(HashMap<String, Double[]> coords){
 		this.stationCoordinates=coords;
 		//String s = closestStation();
 		Log.i("MyApplication", "Hello Hello please don't crash");
 	}
 	
 	public void setStationNames(HashMap<String, String> names){
 		this.stationNames=names;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
     
 	public void getTripInfo(String stationOrig, String stationDest){		
 		task = new TripCostTask();
 		task.setMaster(this);
 		task.execute(stationOrig, stationDest);
 	}
 	
 	public void getStationInfo(){
 		stationsTask = new StationsTask();
 		stationsTask.setMaster(this);
 		stationsTask.execute();
 	}
 	
 	TextView lastStart;
 	TextView lastTransfer;
 	TextView lastEnd;
 	public void updateTripInfo(){
 		String timeNow = task.getTimeNow();
 		String departureTime = task.getStartTime();
 		String fare = task.getFare();
 		String difference = TimeHelper.difference(timeNow, departureTime);
 		String train1 = task.getTrain1();
 
 		ticket.invalidate();
 
 		int stationTextSize= 15;
 		int fontSize = stationTextSize;
 		int topMargin = 80;
 		
 		if (lastStart !=null){
 			Log.i("plazadrama", "it was not null");
 			ticketHolder.removeView(lastStart);
 		}
 		TextView startStation = new TextView(this);
 		ViewGroup.MarginLayoutParams source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(40, topMargin, 0, 0);
 		String startStationString= drawView.lastPinALocation.getFullName();
 		startStation.setText(startStationString);
 		startStation.setTextColor(Color.BLACK);
 		startStation.setTextSize(stationTextSize);
 		LayoutParams p = new LayoutParams(source);
 		startStation.setLayoutParams(p);
 		ticketHolder.addView(startStation, p);
 		lastStart=startStation;
 		
 		
 
 		if (task.hasConnection()){
 			String train2 = task.getTrain2();
 			String middleText = stationNames.get(task.getTransferStation());
			ticket.hasTransfer=true;
 			if (lastTransfer !=null){
 				ticketHolder.removeView(lastTransfer);
 			}
 			TextView transferStation = new TextView(this);
 			source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 
 			transferStation.setText(middleText.toUpperCase());
 			source.setMargins(400- (int)(middleText.length()*(fontSize*.7)), topMargin, 0, 0);
 			transferStation.setTextColor(Color.BLACK);
 			transferStation.setTextSize(stationTextSize);
 			p = new LayoutParams(source);
 			transferStation.setLayoutParams(p);
 			ticketHolder.addView(transferStation, p);
 			lastTransfer=transferStation;
 			ticket.drawTransfer();
 		}
 		
 		if (lastEnd !=null){
 			ticketHolder.removeView(lastEnd);
 		}
 		TextView endStation = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		String endText = stationNames.get(task.getStationDest());
 		endStation.setText(endText.toUpperCase());
 		source.setMargins( 650- endText.length()*(int)(fontSize*.7), topMargin, 0, 0);
 		endStation.setTextColor(Color.BLACK);
 		endStation.setTextSize(stationTextSize);
 		p = new LayoutParams(source);
 		endStation.setLayoutParams(p);
 		ticketHolder.addView(endStation, p);
 		lastEnd=endStation;
 		
 		
 		//journeyFrom.setText(drawView.getLastPinA().getFullName());
 		//journeyCost.setText("$"+fare);
 		//journeyTimeUntil.setText("departs at " + departureTime + "("+difference+")");
 		//journeyTrain.setText(stationNames.get(train1));
 		boolean isAfter = TimeHelper.isAfter(timeNow, departureTime);
 		Log.i("MyApplication", "TIME UNTIL IS: " + difference);
 		Log.i("MyApplication", "FARE IS: " + fare);
 		Log.i("MyApplication", "Is leave in the future?: " + isAfter);
 	}
 	
 	
 	public void movePinA(float dX, float dY){
 		pinAMarginLeft +=dX;
 		pinAMarginTop +=dY;
 		fLayout.removeView(pinA);
 		pinParams.setMargins(pinAMarginLeft, pinAMarginTop, marginRight, marginBottom);
 		fLayout.addView(pinA, pinParams);
 		
 		
 	}
 	
 	public void movePinATo(float dX, float dY){
 		pinAMarginLeft = (int) dX;
 		pinAMarginTop =(int)dY;
 		fLayout.removeView(pinA);
 		pinParams.setMargins(pinAMarginLeft, pinAMarginTop, marginRight, marginBottom);
 		fLayout.addView(pinA, pinParams);
 		
 		
 	}
 	
 	
 	public int getPinAMarginLeft(){
 		return pinAMarginLeft;
 	}
 	
 	public int getPinAMarginTop(){
 		return pinAMarginTop;
 	}
 	
 	public class MainView extends View {
 
         private static final int BACKGROUND = Color.WHITE;
 
         private Bitmap  vBitmap;
         private Canvas  vCanvas;
         private Path    vPath;
         private Paint  vPaint;
         
         ImageView pinA;
         ImageView pinB;
         
         
         float oldX;
         float newX;
         float oldY;
         float newY;
         int strokeWidth;
         float startX;
         float startY;
         Context c;
         Bitmap bitmapBartMap;
         Bitmap originalBartMap;
         int mapWidth;
         int mapHeight;
         
         
         public void setStrokeWidth(int newWidth){
         	this.strokeWidth=newWidth;
         	vPaint.setStrokeWidth(strokeWidth);
         	
         }
         Paint erasePaint;
         public MainView(Context context, AttributeSet attrs) {
             super(context, attrs);
             
 			vPath= new Path();
         }
         
         public MainView(Context c) {
         	
             super(c);
             this.c=c;
 			//vPath= new Path();
         }
         
     		/**
     		
     	
     	}
 
         public void initializePaint(){
         	strokeWidth=30;
             vPaint= new Paint();
 			vPaint.setStrokeWidth(strokeWidth);
             vPaint.setColor(Color.BLACK);
 			vPaint.setStyle(Paint.Style.STROKE);
 			vPaint.setStrokeWidth(strokeWidth);
 			vPaint.setStrokeCap(Paint.Cap.ROUND);
 			vPaint.setAntiAlias(true);
 			erasePaint = new Paint();
 			erasePaint.setStrokeWidth(500);
 			erasePaint.setColor(Color.WHITE);
         }
         
         public void setColor(int color){
         	vPaint.setColor(color);
         }*/
       
         Polygon mapPath;
         Polygon p1;
         Polygon p2;
         Polygon nullPolygon;
         Bitmap bgr;       
         String destinationFullName = "Castro Valley";
         String Xprogram = "int[] xlala = {";
         String Yprogram = "int[] ylala = {";
        int coordCount = 0;
         float dXGrab;
         float dYGrab;
         boolean newShape=false;
         ArrayList<Polygon> damaged = new ArrayList<Polygon>();
         ArrayList<Polygon> stations = new ArrayList<Polygon>();
         String mode; 
         Polygon lastPinALocation;
         
         float[] lastPinACoords = {0, 0};
         
         @Override
         protected void onSizeChanged(int w, int h, int oldw, int oldh) {
             super.onSizeChanged(w, h, oldw, oldh);
             Log.i("MyApplication", "ONSIZECHANGED");
             int mapId= getResources().getIdentifier("bartthick", "drawable", "edu.berkeley.cs160.clairetuna.prog3");
            
           //  (Resources res, int id, BitmapFactory.Options opts) and specify inMutable
             BitmapFactory.Options opts = new BitmapFactory.Options();
             opts.inMutable=true;
             bitmapBartMap  = BitmapFactory.decodeResource(getResources(), mapId, opts);
             vCanvas = new Canvas(bitmapBartMap);
             instantiatePolygons();
             //23 total
 
     
         }
 
         //
 
         public void instantiatePolygons(){
             float[] xCoords = {262,355,435,494,612,684,551,442,417,475,715,719,568,632,554,322,150,226,145,13,249,309,340, 262};
             float[] yCoords = {81,29,211,197,83,154,288,309,423,501,495,601,611,698,764,493,690,777,841,690,410,400,257, 81};
             mapPath = new Polygon(xCoords, yCoords, 23);
             float[] xCoords2 = {276,369,378,284};
             float[] yCoords2 = {108,62,83,129};
             Polygon delNorte = new Polygon(xCoords2, yCoords2, 4, "DELN", "EL CERRITO DEL NORTE");
             stations.add(delNorte);
 
             
            
             float[] xCoords3 = {290,384,392,299};
             float[] yCoords3 = {141,96,114,160};
             Polygon plaza = new Polygon(xCoords3, yCoords3, 4, "PLZA", "EL CERRITO PLAZA");
             stations.add(plaza);
             
             nullPolygon = new Polygon(xCoords2, yCoords2, 4, "NULL", "NULL");
             
             float[] xCoords4 = {232,367,380,245};
             float[] yCoords4 = {81,9,37,110};
             Polygon richmondPoly = new Polygon (xCoords4, yCoords4, 4, "RICH", "RICHMOND");
             stations.add(richmondPoly);
            //INSTANTIATE LAST PIN A LOCATION
             
         }
         
         public Polygon stationForCoord(float x, float y){
         	for (Polygon station : stations){
         		if (station.contains(x, y)){
         			return station;
         		}
         	}
         	return nullPolygon;
         }
         
         @Override
         protected void onDraw(Canvas canvas) {
         	Log.i("MyApplication", "ONDRAW CALLED");
         	Log.i("MyApplication", "BITMAP IS: " + bitmapBartMap);
             canvas.drawBitmap(bitmapBartMap, 0, 0, null);
 
         }
         
         public void drawStation(Polygon poly, boolean drawing){
         	int color;
         	if (drawing){
         		color = getResources().getColor(R.color.StationColor);
         	}
         	else {
         		color = Color.BLACK;
         	}
         	float[] xCoords2 = poly.getXCoords();
             float[] yCoords2 = poly.getYCoords();
         	Paint paint = new Paint();
         	paint.setColor(color);
         	paint.setStyle(Paint.Style.FILL);
         	paint.setStrokeWidth(20);
         	vPath= new Path();
         	vPath.moveTo(xCoords2[0], yCoords2[0]);
         	for (int i = 1; i < 4; i++){
         		vPath.lineTo(xCoords2[i], yCoords2[i]);
         	}
         	vCanvas.drawPath(vPath, paint);
             
             
         }
         
         public void setMode(String newMode){
         	mode=newMode;
         }       
         
        
         
         public boolean onTouchEvent(MotionEvent event) {
         	/*if (mode.equals("scribble")){
         		vPaint.setStyle(Paint.Style.STROKE);
         	}*/
             newX = event.getX();
             newY = event.getY();
             
             Log.i("MyApplication", "LAST PIN A COORDS ARE: (" + lastPinACoords[0] + ", " + lastPinACoords[1]+")");
             float dX;
             float dY;
             int oldMarginLeft = getPinAMarginLeft();
             int oldMarginTop = getPinAMarginTop();
             
             int oldPinX= oldMarginLeft + (width/2);
             int oldPinY= oldMarginTop + height;
 
             dX=newX-oldMarginLeft;
     		dY=newY-oldMarginTop;
             /*if (mode.equals("circleStroke") || mode.equals("rectangleStroke")){
             	vPaint.setStyle(Paint.Style.STROKE);
             }*/
 
             
             
     		if (damaged.size()>0){
     			//Log.i("MyApplication", "damaged size is:" + damaged.size() );
     			lastPinALocation = damaged.remove(0);
     			//Log.i("MyApplication", "removing from damaged last pin:" + lastPinALocation );
     			//Log.i("MyApplication", "damaged size is:" + damaged.size() );
     			drawStation(lastPinALocation, false);
     		}
     		
             if (event.getAction()== MotionEvent.ACTION_DOWN){
             	if (isOnPinA(newX, newY)){
             		//how far into the pin
             	}
         		dXGrab=newX-oldMarginLeft;
         		dYGrab=newY-oldMarginTop;
             	Xprogram+= String.valueOf(newX).split("\\.")[0] + ",";
             	Yprogram+= String.valueOf(newY).split("\\.")[0] + ",";
                  //coordCount++;
                 }
            
             else if (event.getAction()== MotionEvent.ACTION_UP){
 
         			
             		if (lastPinALocation!=null){
             		
             		movePinATo(lastPinACoords[0], lastPinACoords[1]);
             		drawStation(lastPinALocation, true);
         			damaged.add(lastPinALocation);
         			
             		}
             	
             	/**
             	drawStuff();
         		if (mapPath.contains((int)newX, (int)newY)){
         			Log.i("MyApplication", "CLICK IS ON PATH");
         		}
             	;*/
             	//Log.i("MyApplication", "Coords with count "+ coordCount + Xprogram + " " + Yprogram);
             }
             else {
             	if (mapPath.contains((int)newX, (int)newY)){  
             	//if (mapPath.contains((int)dX-dXGrab + (width/2), (int)dY-dYGrab + height)){           	
             		movePinA(dX-dXGrab, dY-dYGrab);
             		if (!stationForCoord(newX, newY).getName().equals("NULL")){
             		
             			lastPinALocation = stationForCoord(newX, newY);
             			getTripInfo(lastPinALocation.getName(),"CAST");
             			Log.i("plazadrama", "setting text to: " + lastPinALocation.getFullName());
             			
             			
             			
             			lastPinACoords[0]=oldMarginLeft + dX-dXGrab;
             			lastPinACoords[1]=oldMarginTop +  dY- dYGrab;
             			//lastPinALocation = p1;
             			drawStation(lastPinALocation, true);
             			damaged.add(lastPinALocation);
             			
             		}
             	}
             	}
             invalidate();
          return true;   
         }
         
         public Polygon getLastPinA(){
         	return lastPinALocation;
         }
 
         public void drawRectangle(float startX, float startY, float newX, float newY){
         	
     		float dX=newX-startX;
     		float dY=newY-startY;
     		
     		vCanvas.drawRect(startX, startY, startX+dX, startY + dY, vPaint);
     	}
         
         
         public void drawCircle(float x, float y, float radius){
         	
     		
     		vCanvas.drawCircle(x, y, radius, vPaint);
     	}
     }
 
 
 }
 
 	
 
 
 	
