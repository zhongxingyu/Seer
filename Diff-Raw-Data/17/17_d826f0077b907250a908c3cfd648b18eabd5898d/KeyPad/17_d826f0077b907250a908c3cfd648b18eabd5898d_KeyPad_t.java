 package embedded2.ESecure;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.os.Environment;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 
 public class KeyPad extends View implements OnTouchListener {
 	private static final String TAG = "DrawView";
 	private static final String FILE_LOCATION = "/ESecure/users/";
 //	Random r;
 //	int numDots = 15;
 //	List<myPoint> myPoints = new ArrayList<myPoint>();
 	Paint paint = new Paint();
 	Vibrator v;
 	WindowManager wm;
 	Display display;
 	Point size;
 	int width, height;
 	float lastx = 0.0f,lasty = 0.0f,x = 0.0f,y = 0.0f;
 	float dist,touchDist;
 	char keyChar,lastChar;
 	int entryMode = 1;
 	long lastTime;
 	long lastE = 0;
 	int count,attempts;
 	float avgPressure, avgMajorAxis, avgMinorAxis,avgDist;
 	boolean training = false;
 	Attempt newAttempt;
 	TouchPoint newPoint;
 	Profile myProfile;
 	String enterCode;
 	String loadedProfile;
 	String status;
 	RectF rect;
 	FileOutputStream outFile = null;
 	ObjectOutputStream out = null;
 	FileInputStream  inFile = null;
 	ObjectInputStream  in = null;
 	File file;
 	File dir;
 	
 	float [][] centers; 
 
 	public KeyPad(Context context, Vibrator v) {
 		super(context);
 		setFocusable(true);
 		setFocusableInTouchMode(true);
 		this.v = v;
 //		r = new Random();
 		rect = new RectF();
 		this.setOnTouchListener(this);
 		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 		size = new Point();
 		display = wm.getDefaultDisplay();
 		display.getSize(size);
 		enterCode = "";
 		loadedProfile = "";
 		status = "";
 		width = size.x;
 		height = size.y;
 		paint.setColor(Color.GREEN);
 		paint.setStrokeWidth(10);
 		paint.setAntiAlias(true);
 		
 		dir = new File(Environment.getExternalStorageDirectory(),FILE_LOCATION);
 		if(!dir.exists()){
 			dir.mkdirs();
 		}
 		Log.v("DBUG","Directory: " + dir);
 		
 		centers = new float[10][2];
 		
 		centers[0][0] = (float).5*width;
 		centers[0][1] = (float)0.85*height;
 		centers[1][0] = (float)(1.0/6.0)*width;
 		centers[1][1] = (float)0.55*height;
 		centers[2][0] = (float).5*width;
 		centers[2][1] = (float)0.55*height;
 		centers[3][0] = (float)(5.0/6.0)*width;
 		centers[3][1] = (float)0.55*height;
 		centers[4][0] = (float)(1.0/6.0)*width;
 		centers[4][1] = (float)0.65*height;
 		centers[5][0] = (float).5*width;
 		centers[5][1] = (float)0.65*height;
 		centers[6][0] = (float)(5.0/6.0)*width;
 		centers[6][1] = (float)0.65*height;
 		centers[7][0] = (float)(1.0/6.0)*width;
 		centers[7][1] = (float)0.75*height;
 		centers[8][0] = (float).5*width;
 		centers[8][1] = (float)0.75*height;
 		centers[9][0] = (float)(5.0/6.0)*width;
 		centers[9][1] = (float)0.75*height;
 	}
 	
 	public void newPassword(){
		//this.save();
 		myProfile = new Profile();
 		attempts = 0;
 		training = true;
 	}
 
 	public void save(){
 		if(attempts == 10){
 			Log.v("DBUG","Finishing and saving");
 			file = new File(dir,enterCode);
 			Log.v("DBUG","Filename: " + file);
 			try{
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 				outFile = new FileOutputStream(file);
 				out = new ObjectOutputStream(outFile);
 				out.writeObject(myProfile);
 				out.close();
 				outFile.close();
 			}catch(FileNotFoundException e1){
 				Log.v("DBUG","OUT - FILE NOT FOUND");
 			}catch(IOException e2){	
 				Log.v("DBUG","OUT - IOEXCEPTION");
 				e2.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		// paint.setColor(Color.rgb(r.nextInt(4)*64, r.nextInt(4)*64,
 		// r.nextInt(4)*64));
 		paint.setStrokeWidth(10);
 
 //		for (myPoint myPoint : myPoints) {
 //			// paint.setColor(Color.rgb(myPoint.r, myPoint.g, myPoint.b));
 //			canvas.drawCircle(myPoint.x, myPoint.y, 5, paint);
 //			// Log.d(TAG, "Painting: "+myPoint);
 //		}
 
 //		for (int i = 1; i < myPoints.size(); i++) {
 //			canvas.drawLine(myPoints.get(i - 1).x, myPoints.get(i - 1).y, myPoints.get(i).x, myPoints.get(i).y, paint);
 //		}
 //		while (myPoints.size() > numDots) {
 //			myPoints.remove(0);
 //		}
 
 		// Vibrate for 500 milliseconds
 		// v.vibrate(15);
 		// Draw boxes
 		canvas.drawLine((float) 0, (float) (height * .5), (float) width, (float) (height * .5), paint);
 		canvas.drawLine((float) 0, (float) (height * .6), (float) width, (float) (height * .6), paint);
 		canvas.drawLine((float) 0, (float) (height * .7), (float) width, (float) (height * .7), paint);
 		canvas.drawLine((float) 0, (float) (height * .8), (float) width, (float) (height * .8), paint);
 		canvas.drawLine((float) 0, (float) (height * .9), (float) width, (float) (height * .9), paint);
 		canvas.drawLine((float) (width / 3), (float) (height * .5), (float) (width / 3), (float) (height * .9), paint);
 		canvas.drawLine((float) (width - 5), (float) (height * .5), (float) (width - 5), (float) (height * .9), paint);
 		canvas.drawLine((float) (5), (float) (height * .5), (float) (5), (float) (height * .9), paint);
 		canvas.drawLine((float) (width / 3 * 2), (float) (height * .5), (float) (width / 3 * 2), (float) (height * .9), paint);
 		paint.setTextSize(100);
 		canvas.drawText("1", (float) (width * .13), (float) (height * .58), paint);
 		canvas.drawText("2", (float) (width / 3 + .13 * width), (float) (height * .58), paint);
 		canvas.drawText("3", (float) (2 * width / 3 + .13 * width), (float) (height * .58), paint);
 		canvas.drawText("4", (float) (width * .13), (float) (height * .68), paint);
 		canvas.drawText("5", (float) (width / 3 + .13 * width), (float) (height * .68), paint);
 		canvas.drawText("6", (float) (2 * width / 3 + .13 * width), (float) (height * .68), paint);
 		canvas.drawText("7", (float) (width * .13), (float) (height * .78), paint);
 		canvas.drawText("8", (float) (width / 3 + .13 * width), (float) (height * .78), paint);
 		canvas.drawText("9", (float) (2 * width / 3 + .13 * width), (float) (height * .78), paint);
 		canvas.drawText("0", (float) (width / 3 + .13 * width), (float) (height * .88), paint);
 		canvas.drawText("E", (float) (2 * width / 3 + .13 * width), (float) (height * .88), paint);
 		canvas.drawText(enterCode,(float)(0.2*width),(float)(0.1*height),paint);
 		canvas.drawText(status,(float)(0.2*width),(float)(0.2*height),paint);
 		canvas.drawOval(rect, paint);
 	}
 
 	public boolean onTouch(View view, MotionEvent event) {
 		// if(event.getAction() != MotionEvent.ACTION_DOWN)
 		// return super.onTouchEvent(event);
 		//Log.v("DBUG","event Orientation: "+event.getOrientation());
 
 		//Log.v("PRESSURE", "" + event.getSize());
 
 		// Log.v
 //		myPoint myPoint = new myPoint();
 		x = event.getX();
 		y = event.getY();
 		//Log.v("DBUG","TOUCH x: " + x + " y: " + y);
 //		myPoint.r = r.nextInt(255);
 //		myPoint.g = r.nextInt(255);
 //		myPoint.b = r.nextInt(255);
 //		myPoints.add(myPoint);
 //		Log.d("POINT LOCATION", "" + myPoint);
 		float major, minor;
 		major = event.getTouchMajor(0);
 		minor = event.getTouchMinor(0);
 		//rect = myOval(minor, major, event.getX(), event.getY());
 //		Log.v("MAJOR/MINOR", "" + major + ":::" + minor);
 		keyChar = calcButtons(event.getX(), event.getY());
 
 		
 
 //		Log.v("COLLECTED", "entryMode = " + entryMode);
 		if(keyChar == 'E'){
 			if(newAttempt != null && ((System.currentTimeMillis()) - lastE > 500)){
 				Log.v("DBUG","Attempt Logged");
 				enterCode = newAttempt.getCode().toString();
 				Log.v("DBUG","Code = " + enterCode);
 				lastE = System.currentTimeMillis();
 				//				if(myProfile == null){
 				//					myProfile = new Profile(newAttempt);
 				//				}else{
 				if(!loadedProfile.equals(enterCode)){
 					file = new File(dir,enterCode);
 					Log.v("DBUG","Filename: " + file);
 					if(!training && file.exists()){
 						Log.v("DBUG","FILE EXISTS");
 						try{
 							inFile = new FileInputStream(file);
 							in = new ObjectInputStream(inFile);
 							myProfile = (Profile)in.readObject();
 							in.close();
 							inFile.close();
 							attempts = 10;
 							loadedProfile = enterCode;
 						}catch(FileNotFoundException e1){
 							Log.v("DBUG","IN - FILE NOT FOUND");
 						}catch(IOException e2){	
 							Log.v("DBUG","IN - IOEXCEPTION");
 						} catch (ClassNotFoundException e3) {
 							Log.v("DBUG","IN - CLASS NOT FOUND");
 						}
 					}
 				}
 				
 				if(myProfile != null){
 					Log.v("DBUG", "Added attempt to profile");
 					boolean result = myProfile.add(newAttempt);
 					if(result && attempts < 10){
 						attempts++;
 						status = "Attempt " + attempts;
 						if(attempts == 10){
 							loadedProfile = enterCode;
 							this.save();
 						}
 					}else if(result){
 						status = "Pass";
 					}else{
 						status = "Fail";
 					}
 				}
 				entryMode = 1;
 
 				newAttempt = null;
 				lastx = 0;
 				lasty = 0;
 			}
 		}else if(keyChar == 'x'){
 			Log.v("COLLECTED","None");
 		}else{
 			status="";
 			if(event.getAction() == MotionEvent.ACTION_UP && entryMode < 2){
 				entryMode = 0;
 			}
 			if(entryMode > 0){
 				if(lastx == 0 && lasty == 0){ //first point of attempt
 					lastx = x;
 					lasty = y;
 					lastChar = keyChar;
 					lastTime = System.currentTimeMillis();
 					count = 1;
 					avgPressure = event.getPressure();
 					avgMajorAxis = event.getTouchMajor();
 					avgMinorAxis = event.getTouchMinor();
 					float tdx = x - centers[Integer.parseInt(""+keyChar)][0];
 					float tdy = y - centers[Integer.parseInt(""+keyChar)][1];
 					touchDist = (float)Math.sqrt(tdx*tdx + tdy*tdy);
 					avgDist = touchDist;
 				}else{
 					float dx = x - lastx;
 					float dy = y - lasty;
 					dist = (float) Math.sqrt(dx*dx + dy*dy); //distance from original point
 					Log.v("TOUCH", "distance: " + dist);
 					if(dist > 50 || lastChar != keyChar){ //distance is over threshold
 						Log.v("COLLECTED", "Point");
 						entryMode++;
 						newPoint = new TouchPoint(lastx,lasty,lastTime);
 						newPoint.setEnd(System.currentTimeMillis());
 						newPoint.setPressure(avgPressure);
 						newPoint.setShape(avgMajorAxis, avgMinorAxis);
 						newPoint.setDist(touchDist);
 						newPoint.setKey(keyChar);
 						if(newAttempt == null){
 							newAttempt = new Attempt(0,newPoint);
 						}else{
 							newAttempt.addPoint(newPoint);
 						}
 						lastx = x;
 						lasty = y;
 						lastChar = keyChar;
 						lastTime = System.currentTimeMillis();
 						avgPressure = event.getPressure();
 						avgMajorAxis = event.getTouchMajor();
 						avgMinorAxis = event.getTouchMinor();
 						float tdx = x - centers[Integer.parseInt(""+keyChar)][0];
 						float tdy = y - centers[Integer.parseInt(""+keyChar)][1];
 						touchDist = (float)Math.sqrt(tdx*tdx + tdy*tdy);
 						avgDist = touchDist;
 					}else{ //same point just update stats
 						avgPressure = ((avgPressure*count) + event.getPressure());
 						avgMajorAxis = ((avgMajorAxis*count) + event.getTouchMajor());
 						avgMinorAxis = ((avgMinorAxis*count) + event.getTouchMinor());
 						float tdx = x - centers[Integer.parseInt(""+keyChar)][0];
 						float tdy = y - centers[Integer.parseInt(""+keyChar)][1];
 						touchDist = (float)Math.sqrt(tdx*tdx + tdy*tdy);
 						avgDist = (avgDist*count) + touchDist;
 						count++;
 						avgPressure = avgPressure/count;
 						avgMajorAxis = avgMajorAxis/count;
 						avgMinorAxis = avgMinorAxis/count;
 						avgDist = avgDist/count;
 					}
 
 				}
 			}else{
 				if(lastx == 0 && lasty == 0){ //first point of attempt
 					status = "";
 					lastx = x;
 					lasty = y;
 					lastChar = keyChar;
 					lastTime = System.currentTimeMillis();
 					count = 1;
 					avgPressure = event.getPressure();
 					avgMajorAxis = event.getTouchMajor();
 					avgMinorAxis = event.getTouchMinor();
 				}else{
 //					float dx = x - lastx;
 //					float dy = y - lasty;
 //					dist = (float) Math.sqrt(dx*dx + dy*dy); //distance from original point
 //					Log.v("TOUCH", "distance: " + dist);
 					if(event.getAction() == MotionEvent.ACTION_UP){ //distance is over threshold
 						Log.v("COLLECTED", "Point");
 						newPoint = new TouchPoint(lastx,lasty,lastTime);
 						newPoint.setEnd(System.currentTimeMillis());
 						newPoint.setPressure(avgPressure);
 						newPoint.setShape(avgMajorAxis, avgMinorAxis);
 						newPoint.setKey(keyChar);
 						if(newAttempt == null){
 							newAttempt = new Attempt(1,newPoint);
 						}else{
 							newAttempt.addPoint(newPoint);
 						}
 						lastx = x;
 						lasty = y;
 						lastChar = keyChar;
 						lastTime = System.currentTimeMillis();
 						avgPressure = event.getPressure();
 						avgMajorAxis = event.getTouchMajor();
 						avgMinorAxis = event.getTouchMinor();
 					}else{ //same point just update stats
 						avgPressure = ((avgPressure*count) + event.getPressure());
 						avgMajorAxis = ((avgMajorAxis*count) + event.getTouchMajor());
 						avgMinorAxis = ((avgMinorAxis*count) + event.getTouchMinor());
 						count++;
 						avgPressure = avgPressure/count;
 						avgMajorAxis = avgMajorAxis/count;
 						avgMinorAxis = avgMinorAxis/count;
 					}
 
 				}
 			}
 		}
 		invalidate();
 		return true;
 	}
 
 	private char calcButtons(float x, float y) {
 		// TODO Auto-generated method stub
 		int a = (int) x * 3 / width;
 
 		if (a == 0) {
 			if (y >= 0.5 * height && y < 0.6 * height) {
 				Log.v("BUTTONPRESSED", "1");
 				return '1';
 			}
 			if (y >= 0.6 * height && y < 0.7 * height) {
 				Log.v("BUTTONPRESSED", "4");
 				return '4';
 			}
 			if (y >= 0.7 * height && y < 0.8 * height) {
 				Log.v("BUTTONPRESSED", "7");
 				return '7';
 			}
 			if (y >= 0.8 * height && y < 0.9 * height) {
 				Log.v("BUTTONPRESSED", "BlankThing");
 				return 'x';
 			}
 		} else if (a == 1) {
 			if (y >= 0.5 * height && y < 0.6 * height) {
 				Log.v("BUTTONPRESSED", "2");
 				return '2';
 			}
 			if (y >= 0.6 * height && y < 0.7 * height) {
 				Log.v("BUTTONPRESSED", "5");
 				return '5';
 			}
 			if (y >= 0.7 * height && y < 0.8 * height) {
 				Log.v("BUTTONPRESSED", "8");
 				return '8';
 			}
 			if (y >= 0.8 * height && y < 0.9 * height) {
 				Log.v("BUTTONPRESSED", "0");
 				return '0';
 			}
 		} else if (a == 2) {
 			if (y >= 0.5 * height && y < 0.6 * height) {
 				Log.v("BUTTONPRESSED", "3");
 				return '3';
 			}
 			if (y >= 0.6 * height && y < 0.7 * height) {
 				Log.v("BUTTONPRESSED", "6");
 				return '6';
 			}
 			if (y >= 0.7 * height && y < 0.8 * height) {
 				Log.v("BUTTONPRESSED", "9");
 				return '9';
 			}
 			if (y >= 0.8 * height && y < 0.9 * height) {
 				Log.v("BUTTONPRESSED", "E");
 				return 'E';
 			}
 		}
 		return 'x';
 	}
 
 //	class myPoint {
 //		float x, y;
 //		int r, g, b;
 //
 //		@Override
 //		public String toString() {
 //			return x + ", " + y;
 //		}
 //	}
 
 //	public RectF myOval(float width, float height, float x, float y) {
 //		float halfW = width / 2;
 //		float halfH = height / 2;
 //		return new RectF(x - halfW, y - halfH, x + halfW, y + halfH);
 //	}
 
 	public void setColor(int i) {
 		// TODO Auto-generated method stub
 		switch (i) {
 		case 1:
 			paint.setColor(Color.RED);
 			break;
 		case 2:
 			paint.setColor(Color.rgb(255, 102, 0));
 			break;
 		case 3:
 			paint.setColor(Color.YELLOW);
 			break;
 		case 4:
 			paint.setColor(Color.GREEN);
 			break;
 		case 5:
 			paint.setColor(Color.BLUE);
 			break;
 		case 6:
 			paint.setColor(Color.rgb(204, 0, 255));
 			break;
 		}
 		invalidate();
 	}
 
 }
