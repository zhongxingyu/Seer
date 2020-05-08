 package mtu.notes;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.os.Environment;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.EditText;
 
 public class ScribbleView extends View{
 	private static final float MINP = 0.25f; 
 	private static final float MAXP = 0.75f;
 
 	private Paint       mPaint;
 	private Bitmap  mBitmap;
 	private Bitmap  hBitmap;
 	private Canvas  mCanvas;
 	private Canvas hCanvas;
 	private Path    mPath;
 	private Paint   mBitmapPaint;
 	private Paint highlight;
 	Context context;
 	boolean highlighter = false;
 	String text="";
 	int height = 500;
 	int width = 500;
 
 	public ScribbleView(Context c){
 		super(c);
 		init(c);
 	}
 	public ScribbleView(Context c, AttributeSet at){
 		this(c, at,0);
 		init(c);
 	}
 
 	public ScribbleView(Context c, AttributeSet at, int defStyle){
 		super(c,at,defStyle);
 		init(c);
 	}
 
 	public  void init(Context c) {
 		context = c;
 		this.requestFocus();
 		this.setFocusableInTouchMode(true);
 		mPaint = new Paint();
 		mPaint.setColor(Color.BLACK);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.ROUND);
 		mPaint.setStrokeCap(Paint.Cap.ROUND);
 		mPaint.setStrokeWidth(0);
 		mPaint.setTextSize(30);
 		highlight = new Paint();
 		highlight.setStrokeWidth(20);
 		highlight.setStyle(Paint.Style.STROKE);
 		highlight.setStrokeJoin(Paint.Join.MITER);
 		highlight.setStrokeCap(Paint.Cap.SQUARE);
 		highlight.setColor(Color.YELLOW);
 		mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 		hBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 		mCanvas = new Canvas(mBitmap);
 		hCanvas = new Canvas(hBitmap);
 		mPath = new Path();
 		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
 	}
 
 	public void Save(EditText editText) {
		Bitmap finBit = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas fin = new Canvas(finBit);
		fin.drawBitmap(hBitmap, 0, 0, mBitmapPaint);
		fin.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
 		if(!editText.getText().toString().isEmpty())
 		{
 			int count = -1;
 			String filename = editText.getText().toString() + ".png";
 			File file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
 			while(file.exists())
 			{
 				count++;
 				filename = editText.getText().toString() + count + ".png";
 				file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
 			}
 			try {
 				FileOutputStream out = new FileOutputStream(file);
				finBit.compress(Bitmap.CompressFormat.PNG, 90, out);
 				out.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			int count = 0;
 			String filename = "note" + count + ".png";
 			File file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
 			while(file.exists())
 			{
 				count++;
 				filename = "note" + count + ".png";
 				file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
 			}
 
 			try {
 				FileOutputStream out = new FileOutputStream(file);
				finBit.compress(Bitmap.CompressFormat.PNG, 90, out);
 				out.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		if(!highlighter)
 			canvas.drawPath(mPath, highlight);
 		canvas.drawBitmap(hBitmap, 0, 0, mBitmapPaint);
 		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
 		if (highlighter)
 			canvas.drawPath(mPath,mPaint);
 	}
 
 	private float mX, mY;
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent ev){
 		Log.i(text,"T");
 		invalidate();
 		if (keyCode==KeyEvent.KEYCODE_ENTER)
 			text = "";
 		return true;
 	}
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		float x = event.getX();
 		float y = event.getY();
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			System.out.println(text);
 			mPath.reset();
 			mPath.moveTo(x, y);
 			if (!highlighter){
 				//((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
 				highlighter  = true; 
 			}
 			else{
 				//((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), 0);
 				highlighter = false;
 			}
 			if (highlighter)
 				mCanvas.drawPath(mPath,mPaint);
 			else
 				hCanvas.drawPath(mPath,highlight);
 			mX = x;
 			mY = y;
 			invalidate();
 			break;
 		case MotionEvent.ACTION_MOVE:
 			mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
 			mX = x;
 			mY = y;
 			invalidate();
 			break;
 		case MotionEvent.ACTION_UP:
 			if (highlighter)
 				mCanvas.drawPath(mPath, mPaint);
 			else
 				hCanvas.drawPath(mPath, highlight);
 			mPath.reset();
 			invalidate();
 			break;
 		}
 		return true;
 	}   
 }
