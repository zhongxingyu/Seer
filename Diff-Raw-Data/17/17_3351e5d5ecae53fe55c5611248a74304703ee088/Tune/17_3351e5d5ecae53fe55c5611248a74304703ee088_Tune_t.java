 package kiv.janecekz.ma.tuner;
 
import kiv.janecekz.ma.MainActivity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
import android.util.Log;
 import android.view.View;
 
 public class Tune extends View {
 	Paint mPaint;
 	float val;
 	float widthHalf;
 
 	public Tune(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		setWillNotDraw(false);
 
 		mPaint = new Paint();
         mPaint.setStyle(Paint.Style.FILL);
         mPaint.setStrokeWidth(30f);
         mPaint.setColor(0xFFFFFFFF);
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		setMeasuredDimension(widthMeasureSpec, 30);
 
 		widthHalf = MeasureSpec.getSize(widthMeasureSpec) / 2;
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
		Log.d(MainActivity.TAG, "onDraw");
 		
 		mPaint.setARGB(255, 255, (int) Math.ceil(255 - Math.abs(val)*255), (int) Math.ceil(255 - Math.abs(val)*255));
 		canvas.drawLine(widthHalf, 15f, widthHalf + val*(widthHalf - 10), 15f, mPaint);
 	}
 
 	public void setVal(float f) {
		Tune.this.val = f;
		Tune.this.invalidate();
 	}
 }
