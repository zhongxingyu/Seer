 package fr.eyal.datalib.sample.netflix.ui;
 
 import android.content.Context;
import android.content.res.ColorStateList;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Typeface;
 import android.text.TextPaint;
 import android.util.AttributeSet;
 import android.widget.TextView;
 
 public class LightTextViewUnderlined extends LightTextView {
 
 	Paint mLinePaint = null;
 	
 	public LightTextViewUnderlined(Context context) {
 		super(context);
 		init(context);
 	}
 
 	public LightTextViewUnderlined(Context context, AttributeSet attrs) {
 		super(context, attrs);
		init(context);
 	}
 
 	public LightTextViewUnderlined(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
		init(context);
 	}
 
 	@Override
 	protected void init(Context context) {
 		super.init(context);
 		if(mLinePaint == null){
			ColorStateList colors = getTextColors();
 			mLinePaint = new Paint();
			mLinePaint.setColor(colors.getDefaultColor());
 			mLinePaint.setStrokeWidth(2);
 		}
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 		
 		canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, mLinePaint);
 	}
 }
