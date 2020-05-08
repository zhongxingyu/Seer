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
 	ImageView pinB;
 	FrameLayout.LayoutParams pinAParams;
 	FrameLayout.LayoutParams pinBParams;
 	FrameLayout fLayout;
 	FrameLayout ticketHolder;
 	int width = 200, height =100,  pinAMarginLeft = 10, marginRight =0, pinAMarginTop = 10, marginBottom = 0, pinBMarginLeft=80, pinBMarginTop=80;
 	
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
 	TextView lastTrain1;
 	TextView lastTrain2;
 public void instantiateLayout(){
 	drawView = new MainView(this);
 	fLayout = (FrameLayout)findViewById(R.id.mapholder);
 	fLayout.addView(drawView);
 	pinAParams = new FrameLayout.LayoutParams(width, height);
 	pinBParams = new FrameLayout.LayoutParams(width, height);
 	
 	pinA = new ImageView(this);
 	Bitmap bitmapPinA = BitmapFactory.decodeResource(getResources(), R.drawable.pina);
 	pinA.setImageBitmap(bitmapPinA);
 	pinAParams.setMargins(pinAMarginLeft, pinAMarginTop, marginRight, marginBottom);
 	fLayout.addView(pinA, pinAParams);
 	
 	
 	pinB = new ImageView(this);
 	Bitmap bitmapPinB = BitmapFactory.decodeResource(getResources(), R.drawable.pinb);
 	pinB.setImageBitmap(bitmapPinB);
 	pinBParams.setMargins(pinBMarginLeft, pinBMarginTop, marginRight, marginBottom);
 	fLayout.addView(pinB, pinBParams);
 
 
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
 	
 	public boolean isOnPinB(float x, float y){
 		boolean xProper = (x>=pinBMarginLeft && x<=pinBMarginLeft + width);
 		boolean yProper = (y>=pinBMarginTop && y<= pinBMarginTop + height);
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
 	TextView lastStartTime;
 	TextView lastEndTime;
 	TextView oneWayTitle;
 	TextView oneWayValue;
 	TextView roundTripTitle;
 	TextView roundTripValue;
 	TextView lastOneWay;
 	TextView lastRoundTrip;
 	TextView lastStartTimeDifference;
 	TextView lastEndTimeDifference;
 	public void updateTripInfo(){
 		String timeNow = task.getTimeNow();
 		String departureTime = task.getStartTime();
 		String arrivalTime = task.getArrivalTime();
 		String fare = task.getFare();
 		String difference = TimeHelper.difference(timeNow, departureTime);
 		String train1 = task.getTrain1();
 
 		int grey = getResources().getColor(R.color.TitleText);
 		int white = getResources().getColor(R.color.ValueText);
 		
 		ticket.invalidate();
 
 		int stationTextSize= 15;
 		int timeTextSize = 15;
 		int fareTextSize=25;
 		int fontSize = stationTextSize;
 		int topMargin = 80;
 		
 		if (lastStart !=null){
 			Log.i("plazadrama", "it was not null");
 			ticketHolder.removeView(lastStart);
 		}
 		if (lastTrain1 != null){
 			ticketHolder.removeView(lastTrain1);
 		}
 		if (lastStartTime !=null){
 			ticketHolder.removeView(lastStartTime);
 		}
 		if (lastEndTime !=null){
 			ticketHolder.removeView(lastEndTime);
 		}
 		if (lastOneWay!=null){
 			ticketHolder.removeView(lastOneWay);
 		}
 	
 		if (lastRoundTrip!=null){
 			ticketHolder.removeView(lastRoundTrip);
 		}
 		if (lastStartTimeDifference!=null){
 			ticketHolder.removeView(lastStartTimeDifference);
 		}
 		
 		if (lastEndTimeDifference!=null){
 			ticketHolder.removeView(lastEndTimeDifference);
 		}
 
 		
 		TextView startStation = new TextView(this);
 		ViewGroup.MarginLayoutParams source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(40, topMargin, 0, 0);
 		String startStationString= drawView.lastPinALocation.getFullName();
 		startStation.setText(startStationString);
 		startStation.setTextColor(white);
 		startStation.setTextSize(stationTextSize);
 		LayoutParams p = new LayoutParams(source);
 		startStation.setLayoutParams(p);
 		ticketHolder.addView(startStation, p);
 		lastStart=startStation;
 		
 		TextView startTime = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(40, topMargin+30, 0, 0);
 		startTime.setText(departureTime);
 		startTime.setTextColor(white);
 		startTime.setTextSize(timeTextSize);
 		p = new LayoutParams(source);
 		startTime.setLayoutParams(p);
 		ticketHolder.addView(startTime, p);
 		lastStartTime=startTime;
 		
 
 		TextView startTimeDifference = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(40+80, topMargin+30, 0, 0);
 		startTimeDifference.setText("(" + difference+ ")");
 		startTimeDifference.setTextColor(grey);
 		startTimeDifference.setTextSize(timeTextSize);
 		p = new LayoutParams(source);
 		startTimeDifference.setLayoutParams(p);
 		ticketHolder.addView(startTimeDifference, p);
 		lastStartTimeDifference=startTimeDifference;
 		
 		
 		TextView oneWayTitle = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(40, topMargin+80, 0, 0);
 		oneWayTitle.setText("one-way:");
 		oneWayTitle.setTextColor(grey);
 		oneWayTitle.setTextSize(fareTextSize);
 		p = new LayoutParams(source);
 		oneWayTitle.setLayoutParams(p);
 		ticketHolder.addView(oneWayTitle, p);
 		
 		
 		TextView oneWayValue = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(40 + 150, topMargin+80, 0, 0);
 		oneWayValue.setText("$" +fare);
 		oneWayValue.setTextColor(white);
 		oneWayValue.setTextSize(fareTextSize);
 		p = new LayoutParams(source);
 		oneWayValue.setLayoutParams(p);
 		ticketHolder.addView(oneWayValue, p);
 		lastOneWay=oneWayValue;
 		
 		
 		TextView roundTripTitle = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(300, topMargin+80, 0, 0);
 		roundTripTitle.setText("round-trip:");
 		roundTripTitle.setTextColor(grey);
 		roundTripTitle.setTextSize(fareTextSize);
 		p = new LayoutParams(source);
 		roundTripTitle.setLayoutParams(p);
 		ticketHolder.addView(roundTripTitle, p);
 		
 		
 		TextView roundTripValue = new TextView(this);
 		String roundTripCostString;
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(300 + 170, topMargin+80, 0, 0);
 		float roundTripCost = Float.parseFloat(fare)*2;
 		if (Float.toString(roundTripCost).split("\\.")[1].length()==1){
 			roundTripCostString= Float.toString(roundTripCost) + "0";
 		}
 		else if (Float.toString(roundTripCost).split("\\.")[1] == null || Float.toString(roundTripCost).split("\\.")[1].length()==0){
 			roundTripCostString= Float.toString(roundTripCost) + "00";
 		}
 		else{
 			roundTripCostString= Float.toString(roundTripCost);
 		}
 		
 		roundTripValue.setText("$" +roundTripCostString);
 		roundTripValue.setTextColor(white);
 		roundTripValue.setTextSize(fareTextSize);
 		p = new LayoutParams(source);
 		roundTripValue.setLayoutParams(p);
 		ticketHolder.addView(roundTripValue, p);
 		lastRoundTrip=roundTripValue;
 		
 		
 		train1 = stationNames.get(task.getTrain1());
 		TextView train1View = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		train1View.setText(train1.toUpperCase());
 		
 		if (lastTrain2 != null){
 			ticketHolder.removeView(lastTrain2);
 		}
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
 			source.setMargins(390- (int)(middleText.length()*(fontSize*.7)), topMargin, 0, 0);
 			transferStation.setTextColor(white);
 			transferStation.setTextSize(stationTextSize);
 			p = new LayoutParams(source);
 			transferStation.setLayoutParams(p);
 			ticketHolder.addView(transferStation, p);
 			lastTransfer=transferStation;
 			ticket.drawTransfer();
 			
 			
 			
 			source.setMargins(240- (int)(train1.length()*(fontSize*.7)), 20, 0, 0);
 			train1View.setTextColor(grey);
 			train1View.setTextSize(stationTextSize);
 			p = new LayoutParams(source);
 			train1View.setLayoutParams(p);
 			ticketHolder.addView(train1View, p);
 			lastTrain1=train1View;
 			
 			train2 = stationNames.get(task.getTrain2());
 			TextView train2View = new TextView(this);
 			source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 			train2View.setText(train2.toUpperCase());
 			source.setMargins(560- (int)(train2.length()*(fontSize*.7)), 20, 0, 0);
 			train2View.setTextColor(grey);
 
 			train2View.setTextSize(stationTextSize);
 			p = new LayoutParams(source);
 			train2View.setLayoutParams(p);
 			ticketHolder.addView(train2View, p);
 			lastTrain2=train2View;
 			//HAS CONNECTION: PUT TRAINS ON BOTH SIDES 
 		}
 		else {
 			//DOES NOT HAVE CONNECTION: PUT TRAIN IN MIDDLE
 			source.setMargins(400- (int)(train1.length()*(fontSize*.7)), 20, 0, 0);
 			train1View.setTextColor(grey);
 			train1View.setTextSize(stationTextSize);
 			p = new LayoutParams(source);
 			train1View.setLayoutParams(p);
 			ticketHolder.addView(train1View, p);
 			lastTrain1=train1View;
 			
 		}
 		
 		if (lastEnd !=null){
 			ticketHolder.removeView(lastEnd);
 		}
 		TextView endStation = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		Log.i("MyApplication", "abbrev in update is: " + task.getStationDest());
 		
 		String endText = stationNames.get(task.getStationDest());
 		endStation.setText(endText.toUpperCase());
 		source.setMargins( 650- endText.length()*(int)(fontSize*.7), topMargin, 0, 0);
 		endStation.setTextColor(white);
 		endStation.setTextSize(stationTextSize);
 		p = new LayoutParams(source);
 		endStation.setLayoutParams(p);
 		ticketHolder.addView(endStation, p);
 		lastEnd=endStation;
 		
 		
 		TextView endTime = new TextView(this);
 		source= new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
 		source.setMargins(650- arrivalTime.length()*(int)(fontSize*.7), topMargin+30, 0, 0);
 		endTime.setText(arrivalTime);
 		endTime.setTextColor(white);
 		endTime.setTextSize(timeTextSize);
 		p = new LayoutParams(source);
 		endTime.setLayoutParams(p);
 		ticketHolder.addView(endTime, p);
 		lastEndTime=endTime;
 		
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
 		pinAParams.setMargins(pinAMarginLeft, pinAMarginTop, marginRight, marginBottom);
 		fLayout.addView(pinA, pinAParams);
 
 	}
 	
 	
 	public void movePinB(float dX, float dY){
 		pinBMarginLeft +=dX;
 		pinBMarginTop +=dY;
 		fLayout.removeView(pinB);
 		pinBParams.setMargins(pinBMarginLeft, pinBMarginTop, marginRight, marginBottom);
 		fLayout.addView(pinB, pinBParams);
 
 	}
 	
 	public void movePinATo(float dX, float dY){
		
		Log.i("MyApplication", "called move pin A ");
 		pinAMarginLeft = (int) dX;
 		pinAMarginTop =(int)dY;
 		fLayout.removeView(pinA);
 		pinAParams.setMargins(pinAMarginLeft, pinAMarginTop, marginRight, marginBottom);
 		fLayout.addView(pinA, pinAParams);
 		
 		
 	}
 	
 	public void movePinBTo(float dX, float dY){
 		pinBMarginLeft = (int) dX;
 		pinBMarginTop =(int)dY;
 		fLayout.removeView(pinB);
		pinBParams.setMargins(pinBMarginLeft, pinBMarginTop, marginRight, marginBottom);
 		fLayout.addView(pinB, pinBParams);
 		
 		
 	}
 	
 	public int getPinAMarginLeft(){
 		return pinAMarginLeft;
 	}
 	
 	public int getPinAMarginTop(){
 		return pinAMarginTop;
 	}
 	
 	public int getPinBMarginLeft(){
 		return pinBMarginLeft;
 	}
 	
 	public int getPinBMarginTop(){
 		return pinBMarginTop;
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
 			
         }
         
 
         Polygon mapPath;
         boolean aSelected;
         boolean bSelected;
         Polygon p1;
         Polygon p2;
         Polygon nullPolygon;
         Bitmap bgr;       
         String destinationFullName = "Castro Valley";
         String Xprogram = "int[] xlala = {";
         String Yprogram = "int[] ylala = {";
        int coordCount = 0;
         float pinAdXGrab;
         float pinAdYGrab;
         float pinBdXGrab;
         float pinBdYGrab;
         boolean newShape=false;
         ArrayList<Polygon> pinADamaged = new ArrayList<Polygon>();
         ArrayList<Polygon> pinBDamaged = new ArrayList<Polygon>();
         ArrayList<Polygon> stations = new ArrayList<Polygon>();
         String mode; 
         Polygon lastPinALocation;
         Polygon lastPinBLocation;
         float[] lastPinACoords = {0, 0};
         float[] lastPinBCoords = {0, 0};
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
             float pinAdX;
             float pinAdY;
             float pinBdX;
             float pinBdY;
             int oldPinAMarginLeft = getPinAMarginLeft();
             int oldPinAMarginTop = getPinAMarginTop();
             int oldPinBMarginLeft = getPinBMarginLeft();
             int oldPinBMarginTop = getPinBMarginTop();
             
             int oldPinX= oldPinAMarginLeft + (width/2);
             int oldPinY= oldPinAMarginTop + height;
 
             pinAdX=newX-oldPinAMarginLeft;
     		pinAdY=newY-oldPinAMarginTop;
     		
             pinBdX=newX-oldPinBMarginLeft;
     		pinBdY=newY-oldPinBMarginTop;
             /*if (mode.equals("circleStroke") || mode.equals("rectangleStroke")){
             	vPaint.setStyle(Paint.Style.STROKE);
             }*/
 
             
             
     		if (aSelected && pinADamaged.size()>0){
     			//Log.i("MyApplication", "damaged size is:" + damaged.size() );
     			lastPinALocation = pinADamaged.remove(0);
     			//Log.i("MyApplication", "removing from damaged last pin:" + lastPinALocation );
     			//Log.i("MyApplication", "damaged size is:" + damaged.size() );
     			drawStation(lastPinALocation, false);
    			if (lastPinBLocation !=null){
    			drawStation(lastPinBLocation, true);}
     		}
     		
     		if (bSelected && pinBDamaged.size()>0){
     			//Log.i("MyApplication", "damaged size is:" + damaged.size() );
     			lastPinBLocation = pinBDamaged.remove(0);
     			//Log.i("MyApplication", "removing from damaged last pin:" + lastPinALocation );
     			//Log.i("MyApplication", "damaged size is:" + damaged.size() );
     			drawStation(lastPinBLocation, false);
    			if (lastPinALocation!=null){
    			drawStation(lastPinALocation, true);}
     		}
     		
             if (event.getAction()== MotionEvent.ACTION_DOWN){
             	if (isOnPinA(newX, newY) && !bSelected){
             		//how far into the pin
             	aSelected=true;
         		pinAdXGrab=newX-oldPinAMarginLeft;
         		pinAdYGrab=newY-oldPinAMarginTop;
             	}
             	else if (isOnPinB(newX, newY) && !aSelected){
             		//how far into the pin
             	bSelected=true;
         		pinBdXGrab=newX-oldPinBMarginLeft;
         		pinBdYGrab=newY-oldPinBMarginTop;
             	}
             	Xprogram+= String.valueOf(newX).split("\\.")[0] + ",";
             	Yprogram+= String.valueOf(newY).split("\\.")[0] + ",";
                  //coordCount++;
                 }
            
             else if (event.getAction()== MotionEvent.ACTION_UP){
             	Log.i("MyApplication", "Releasing with a selected: " + aSelected + "B selected: " + bSelected);
         			
             		if (lastPinALocation!=null && aSelected){
             		
             		movePinATo(lastPinACoords[0], lastPinACoords[1]);
             		drawStation(lastPinALocation, true);
         			pinADamaged.add(lastPinALocation);
         			
             		}
             		
             		if (lastPinBLocation!=null&& bSelected){
                 		
                 		movePinBTo(lastPinBCoords[0], lastPinBCoords[1]);
                 		drawStation(lastPinBLocation, true);
             			pinBDamaged.add(lastPinBLocation);
             			
                 		}
 
             		aSelected=false;
             		bSelected=false;
             	//Log.i("MyApplication", "Coords with count "+ coordCount + Xprogram + " " + Yprogram);
             }
             else {
             	if (mapPath.contains((int)newX, (int)newY)){  
             	//if (mapPath.contains((int)newX-dXGrab + (width/2), (int)newY-dYGrab + height)){           	
             		if (aSelected){
             			movePinA(pinAdX-pinAdXGrab, pinAdY-pinAdYGrab);
                 		if (!stationForCoord(newX, newY).getName().equals("NULL")){
                 		
                 			lastPinALocation = stationForCoord(newX, newY);
                 			
                 			if (lastPinBLocation!=null){
                 			getTripInfo(lastPinALocation.getName(),lastPinBLocation.getName());
                 			}
                 			lastPinACoords[0]=oldPinAMarginLeft + pinAdX-pinAdXGrab;
                 			lastPinACoords[1]=oldPinAMarginTop +  pinAdY- pinAdYGrab;
                 			//lastPinALocation = p1;
                 			drawStation(lastPinALocation, true);
                 			pinADamaged.add(lastPinALocation);
                 			
                 		}
             		}
             		else if (bSelected){
             			movePinB(pinBdX-pinBdXGrab, pinBdY-pinBdYGrab);
                 		if (!stationForCoord(newX, newY).getName().equals("NULL")){
                 		
                 			lastPinBLocation = stationForCoord(newX, newY);
                 			
                 			if (lastPinALocation !=null){
                 			getTripInfo(lastPinALocation.getName(),lastPinBLocation.getName());
                 			}
                 			lastPinBCoords[0]=oldPinBMarginLeft + pinBdX-pinBdXGrab;
                 			lastPinBCoords[1]=oldPinBMarginTop +  pinBdY- pinBdYGrab;
                 			//lastPinALocation = p1;
                 			drawStation(lastPinBLocation, true);
                 			pinBDamaged.add(lastPinBLocation);
                 			
                 		}
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
 
 	
 
 
 	
