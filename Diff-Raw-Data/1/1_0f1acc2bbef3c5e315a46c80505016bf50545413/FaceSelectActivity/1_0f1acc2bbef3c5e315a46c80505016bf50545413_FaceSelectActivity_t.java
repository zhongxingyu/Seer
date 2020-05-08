 package com.bn.bobblehead;
 
 import java.io.FileOutputStream;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 
 @SuppressLint("NewApi")
 public class FaceSelectActivity extends Activity {
 
 	private SelectView mSelectView;
 	private SensorManager mSensorManager;
 	private PowerManager mPowerManager;
 	private WindowManager mWindowManager;
 	private WakeLock mWakeLock;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		// Get an instance of the SensorManager
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 
 		// Get an instance of the PowerManager
 		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
 
 		// Get an instance of the WindowManager
 		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
 		mWindowManager.getDefaultDisplay();
 
 		// Create a bright wake lock
 		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
 				.getName());
 
 		// instantiate our simulation view and set it as the activity's content
 		mSelectView = new SelectView(this);
 		setContentView(mSelectView);
 	}
 	
 	
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		mWakeLock.acquire();
 
 		setContentView(mSelectView);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	
 		// and release our wake-lock
 		mWakeLock.release();
 	}
 
 
 	class SelectView extends View implements OnClickListener {
 
 		private final Bitmap button;
 
 		private Sensor mAccelerometer;
 		private Bitmap backg;
 
 
 
 		public SelectView(Context context) {
 			super(context);
 			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 
 			DisplayMetrics metrics = new DisplayMetrics();
 			getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
 			//get go button and dimensions
 			Bitmap tmp = BitmapFactory.decodeResource(getResources(), R.drawable.go_button);
 			button=Bitmap.createScaledBitmap(tmp, 100, 100, false);
 			buttonR=new Rect(metrics.widthPixels-150,metrics.heightPixels-200,metrics.widthPixels,metrics.heightPixels);
 
 			//Get background
 			tmp=BitmapFactory.decodeFile(HomeScreen.backFil.toString());
 			backg=Bitmap.createScaledBitmap(tmp, metrics.widthPixels, metrics.heightPixels, false);
 
 			Options opts = new Options();
 			opts.inDither = true;
 			opts.inPreferredConfig = Bitmap.Config.RGB_565;
 
 
 		}
 
 
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			// TODO Auto-generated method stub
 
 		}
 
 		
 		//Use to crop a bitmap to the specified rectangle using an oval cut
 		public Bitmap getCroppedBitmap(Bitmap bitmap,RectF r) {
 			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
 					bitmap.getHeight(), Config.ARGB_8888);
 			Canvas canvas = new Canvas(output);
 
 			final int color = 0xff424242;
 			final Paint paint = new Paint();
 			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
 
 			paint.setAntiAlias(true);	
 			canvas.drawARGB(0, 0, 0, 0);
 			paint.setColor(color);
 
 			canvas.drawOval(r,new Paint());
 			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
 			canvas.drawBitmap(bitmap, rect, rect, paint);
 
 
 			Matrix matrix = new Matrix();
 			// resize the bit map
 			matrix.postScale(1,1);
 			Bitmap resizedBitmap = Bitmap.createBitmap(output, (int)r.left, (int)r.top, 
 					(int) r.width(), (int)r.height(), matrix, true); 
 
 			System.out.println(canvas.getWidth()+":"+canvas.getHeight());
 			return resizedBitmap;
 
 
 		}
 
 		private Paint p=new Paint();
 
 		protected void onDraw(Canvas canvas) {
 			p.setStyle(Paint.Style.STROKE) ;
 			p.setStrokeWidth(4f);
 			p.setARGB(255, 0, 200, 0);
 			canvas.drawBitmap(backg, 0, 0, null);
 			if (selection != null){
 				canvas.drawOval(selection, p);
 			}
 			canvas.drawBitmap(button, null,buttonR, null);
 
 			// and make sure to redraw asap
 			invalidate();
 		}
 
 		private Rect buttonR;
 		private RectF selection;
 		private boolean rectDrawn=false;
 		private int ooX;//ovals originx
 		private int ooY;//avals origin y
 		int top=0;
 		int left=0;
 		int bottom=0;
 		int right=0;
 		public boolean onTouchEvent(MotionEvent e) {
 			// TODO Auto-generated method stub
 
 			int x = (int) e.getX();
 			int y = (int) e.getY();
 
 
 			//Check for button press
 			if (buttonR.contains(x, y)){
 				if (selection.width()>0 && selection.height() >0){
 					Intent i = new Intent(FaceSelectActivity.this,BobActivity.class);
 					Bitmap face=Bitmap.createBitmap(backg,(int)selection.left, (int)selection.top,(int)selection.width(),(int)selection.height());
 					face=getCroppedBitmap(backg,selection);
 
 					try {
 						if(!HomeScreen.faceFil.exists()){HomeScreen.faceFil.createNewFile();}
 						FileOutputStream out = new FileOutputStream(HomeScreen.faceFil);
 
 						face.compress(Bitmap.CompressFormat.PNG, 90, out);
 						out.flush();out.close();
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 					
 					RectF rec= new RectF(selection.left, selection.top,selection.left+selection.width(),selection.top+selection.height());
 
 					i.putExtra("rec", rec);
 
 					startActivity(i);
					finish();
 					return true;
 
 				}
 				else {
 					showDialog(DIALOG_ALERT);
 				}
 
 			}
 
 			boolean backFlag=false;//true if the oval is being drawn towards the origin
 			//Check for oval drawings
 			switch (e.getAction()) {
 				//get an origin for the oval
 				case MotionEvent.ACTION_DOWN:
 					if (!rectDrawn){
 						top=(int) y;
 						left=(int) x;
 						ooX=(int) x;
 						ooY=(int) y;
 					}
 				
 				//check x,y against the origin then save the rectangle
 				case MotionEvent.ACTION_MOVE:
 					if (x<ooX){
 						left=x;
 						right=ooX;
 					}else{
 						left=ooX;
 						right=x;
 					}
 					if (y<ooY){
 						top=y;
 						bottom=ooY;
 					}else{
 						top=ooY;
 						bottom=y;
 					}
 				
 			}
 
 			//if no oval is drawn
 			if (top==0 && left==0 && right==0 && bottom==0){
 				selection=null;
 			}
 			selection=new RectF(left,top,right,bottom);
 			return true;
 		}
 
 
 
 
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 
 	private static final int DIALOG_ALERT = 10;
 
 	
 	
 
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_ALERT:
 			// Create out AlterDialog
 			Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Please Select a face.");
 			builder.setCancelable(true);
 			builder.setPositiveButton("Ok", new OkOnClickListener());
 			AlertDialog dialog = builder.create();
 			dialog.show();
 		}
 		return super.onCreateDialog(id);
 	}
 
 	private final class OkOnClickListener implements DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int which) {
 			//FaceSelectActivity.this.finish();
 		}
 	}
 }
