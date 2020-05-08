 package edu.upenn.cis350.Trace2Learn;
 
 import android.app.Activity;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.view.View;
 
 public class CharacterCreationActivity extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		mPaint = new Paint();
 		
 		//Sam: This is an error since DrawingPane is abstract.....
		setContentView(new DrawingPane(this, mPaint));
 
 		mPaint.setAntiAlias(true);
 		mPaint.setDither(true);
 		mPaint.setColor(0xFFFF0000);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.ROUND);
 		mPaint.setStrokeCap(Paint.Cap.ROUND);
 		mPaint.setStrokeWidth(12);
 
 	}
 
 	public void setContentView(View view) {
         super.setContentView(view);
     }
 	
 	private Paint mPaint;
 
 	public void colorChanged(int color) {
 		mPaint.setColor(color);
 	}
 
 }
