 package com.appspot.manup.signature;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BlurMaskFilter;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.EmbossMaskFilter;
 import android.graphics.MaskFilter;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 /**
  * 
  * Code from com.appspot.manup.FingerPaint
  *
  */
 public class Signature extends Activity {
     private static final String TAG = "MANUP Signature";
     private MyView myView;
 	private Paint mPaint;
 	private MaskFilter mEmboss;
 	private MaskFilter mBlur;
 	private DataHelper dh;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(myView = new MyView(this));
 
 		mPaint = new Paint();
 		mPaint.setAntiAlias(true);
 		mPaint.setDither(true);
 		mPaint.setColor(Color.BLACK);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.ROUND);
 		mPaint.setStrokeCap(Paint.Cap.ROUND);
 		mPaint.setStrokeWidth(10);
 
 		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
 		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
 		
 		dh = new DataHelper(this);
 		startWatchingExternalStorage();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		stopWatchingExternalStorage();
 	}
 	
 	public class MyView extends View {
 		private Bitmap mBitmap;
 		private Canvas mCanvas;
 		private Path mPath;
 		private Paint mBitmapPaint;
 
 		public MyView(Context c) {
 			super(c);
 
 			mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
 			mCanvas = new Canvas(mBitmap);
 			mPath = new Path();
 			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
 		}
 
 		@Override
 		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 			super.onSizeChanged(w, h, oldw, oldh);
 			mCanvas.setBitmap(mBitmap = Bitmap.createBitmap(w, h,
 					Bitmap.Config.ARGB_8888));
 		}
 
 		@Override
 		protected void onDraw(Canvas canvas) {
 			canvas.drawColor(Color.WHITE);
 			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
 			canvas.drawPath(mPath, mPaint);
 		}
 
 		private float mX, mY;
 		private static final float TOUCH_TOLERANCE = 4;
 
 		private void touch_start(float x, float y) {
 			mPath.reset();
 			mPath.moveTo(x, y);
 			mX = x;
 			mY = y;
 		}
 
 		private void touch_move(float x, float y) {
 			float dx = Math.abs(x - mX);
 			float dy = Math.abs(y - mY);
 			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
 				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
 				mX = x;
 				mY = y;
 			}
 		}
 
 		private void touch_up() {
 			mPath.lineTo(mX, mY);
 			// commit the path to our offscreen
 			mCanvas.drawPath(mPath, mPaint);
 			// kill this so we don't double draw
 			mPath.reset();
 		}
 
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			float x = event.getX();
 			float y = event.getY();
 
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				touch_start(x, y);
 				invalidate();
 				break;
 			case MotionEvent.ACTION_MOVE:
 				touch_move(x, y);
 				invalidate();
 				break;
 			case MotionEvent.ACTION_UP:
 				touch_up();
 				invalidate();
 				break;
 			}
 			return true;
 		}
 
 		public Bitmap getBitMap() {
 			return mBitmap;
 		}
 	}
 
 	private static final int SUBMIT = Menu.FIRST;
 	private static final int CLEAR = 2;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		menu.add(0, SUBMIT, 0, "Submit").setShortcut('7', 's');
 		menu.add(0, CLEAR, 0, "Clear").setShortcut('3', 'c');
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		mPaint.setXfermode(null);
 		mPaint.setAlpha(0xFF);
 
 		switch (item.getItemId()) {
 		case SUBMIT:
 			onSubmit();
 			return true;
 		case CLEAR:
 			setContentView(myView = new MyView(this));
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	
 	// Monitor state of external storage
 	BroadcastReceiver mExternalStorageReceiver;
 	boolean mExternalStorageAvailable = false;
 	boolean mExternalStorageWriteable = false;
 
 	void updateExternalStorageState() {
 	    String state = Environment.getExternalStorageState();
 	    if (Environment.MEDIA_MOUNTED.equals(state)) {
 	        mExternalStorageAvailable = mExternalStorageWriteable = true;
 	        Toast.makeText(this, "External storage is writable", Toast.LENGTH_SHORT).show();
 	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 	        mExternalStorageAvailable = true;
 	        mExternalStorageWriteable = false;
 	        Toast.makeText(this, "External storage is not writable", Toast.LENGTH_SHORT).show();
 	    } else {
 	        mExternalStorageAvailable = mExternalStorageWriteable = false;
 	        Toast.makeText(this, "External storage is not mounted", Toast.LENGTH_SHORT).show();
 	    }
 	}
 
 	void startWatchingExternalStorage() {
 	    mExternalStorageReceiver = new BroadcastReceiver() {
 	        @Override
 	        public void onReceive(Context context, Intent intent) {
 	            Log.i("test", "Storage: " + intent.getData());
 	            updateExternalStorageState();
 	        }
 	    };
 	    IntentFilter filter = new IntentFilter();
 	    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
 	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
 	    registerReceiver(mExternalStorageReceiver, filter);
 	    updateExternalStorageState();
 	}
 
 	void stopWatchingExternalStorage() {
 	    unregisterReceiver(mExternalStorageReceiver);
 	}
 	
 	private void onSubmit(){
 		Bitmap sigBitmap = myView.getBitMap();
		if (mExternalStorageWriteable)
 			if (writeToExternalStorage(sigBitmap))
				dh.insert(student_id, output.getAbsolutePath(), false);
 				// This is also in datahelper so delete when using insert
				//Toast.makeText(this, "Write success", Toast.LENGTH_SHORT).show();
 			else
 				Toast.makeText(this, "Write failed", Toast.LENGTH_SHORT).show();
 		else
 			Toast.makeText(this, "Cannot write to external storage", Toast.LENGTH_SHORT).show();
 	}
 	File output;
 	long student_id = 0;
 	private boolean writeToExternalStorage(Bitmap b){
 		try{
 			File rootPath = Environment.getExternalStorageDirectory();
 			File manupPath = new File(rootPath, "manup/");
 			manupPath.mkdirs();
 			FileOutputStream fos;
 			try {
 				output = new File(manupPath, student_id + ".jpg");
 				output.createNewFile();
 				fos = new FileOutputStream(output);
 			    b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
 			    fos.flush();
 			    fos.close();
 			    return true;
 			} 
 			catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		} catch (IOException e) { e.printStackTrace(); }
 		return false;
 	}
 }
