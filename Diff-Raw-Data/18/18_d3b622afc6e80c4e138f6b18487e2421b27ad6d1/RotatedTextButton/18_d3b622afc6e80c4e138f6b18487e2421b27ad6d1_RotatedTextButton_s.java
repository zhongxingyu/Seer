 package albin.oredev2012.ui;
 
 import albin.oredev2012.R;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.text.TextPaint;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.widget.Button;
 
 public class RotatedTextButton extends Button {
 
 	private int angle;
 	private int myWidth;
 	private int myHeight;
 
 	public RotatedTextButton(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(attrs);
 	}
 
 	public RotatedTextButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(attrs);
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
 		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
 		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
 		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
 		int width;
 		int height;
 		if (widthMode == MeasureSpec.EXACTLY) {
 			width = widthSize;
 		} else {
 			width = myWidth + getCompoundPaddingLeft()
 					+ getCompoundPaddingRight();
 			if (widthMode == MeasureSpec.AT_MOST) {
 				width = Math.min(width, widthSize);
 			}
 		}
 		if (heightMode == MeasureSpec.EXACTLY) {
 			height = heightSize;
 		} else {
 			height = myHeight + getCompoundPaddingBottom()
 					+ getCompoundPaddingTop();
 			if (heightMode == MeasureSpec.AT_MOST) {
 				height = Math.min(height, heightSize);
 			}
 		}
 		setMeasuredDimension(width, height);
 	}
 
 	public RotatedTextButton(Context context) {
 		super(context);
 	}
 
 	private void init(AttributeSet attrs) {
 		TypedArray styles = getContext().obtainStyledAttributes(attrs,
 				R.styleable.rotated_text_button);
 		angle = styles
 				.getInteger(R.styleable.rotated_text_button_text_angle, 0);
 		TextPaint paint = getPaint();
 		float absAngle = (angle < 0) ? -angle : angle;
 		float absAngleRadians = (float) (absAngle * Math.PI / 180);
 		float textWidth = paint.measureText((String) getText());
 		float textHeight = getLineHeight();
 		float sin = FloatMath.sin(absAngleRadians);
 		float cos = FloatMath.cos(absAngleRadians);
 		myWidth = (int) (textWidth * cos + textHeight * sin);
 		myHeight = (int) (textHeight * cos + textWidth * sin);
 		styles.recycle();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		canvas.save();
		canvas.rotate(angle, canvas.getWidth() / 2, canvas.getHeight() / 2);
 		super.onDraw(canvas);
 		canvas.restore();
 	}

 }
