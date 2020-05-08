 /*
  * Waker project 2013
  * 
  * folks studio
  * 
  * by caifangmao8@gmail.com
  */
 package com.cfm.waker.widget;
 
 import com.cfm.waker.R;
 import com.cfm.waker.util.DensityUtil;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class DebossFontText extends View {
 	
 	private Typeface tf;
 	private int textColor;
 	private float textSize;
 	private int colorOffsetBright;
 	private int colorOffsetDark;
 	
 	private String text;
 	
 	private Paint paint;
 	private Rect textBound;
 	
 	private int centerX,centerY;
 	private int width,height;
 	private float offset;
 
 	public DebossFontText(Context context){
 		this(context, null);
 	}
 	
 	public DebossFontText(Context context, AttributeSet attrs){
 		this(context, attrs, 0);
 	}
 	
 	public DebossFontText(Context context, AttributeSet attrs, int defStyle){
 		super(context, attrs, defStyle);
 		
 		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		textBound = new Rect();
 		
 		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DebossFontText);
 		
 		String font = ta.getString(R.styleable.DebossFontText_font);
 		if(null != font){
 			tf = Typeface.createFromAsset(context.getAssets(), font);
 			paint.setTypeface(tf);
 		}
 		
 		text = ta.getString(R.styleable.DebossFontText_text);
 		textSize = ta.getDimension(R.styleable.DebossFontText_textSize, context.getResources().getDimension(R.dimen.debossfonttext_default_textsize));
 		paint.setTextSize(textSize);
 		textColor = ta.getColor(R.styleable.DebossFontText_textColor, 0xFF999999);
 		
 		int a = textColor >> 24;
 		int r = textColor >> 16 & 0xFF;
 		int g = textColor >> 8 & 0xFF;
 		int b = textColor & 0xFF;
 		
 		int max = Math.max(r, Math.max(g, b));
 		int min = Math.min(r, Math.min(g, b));
 		
 		int colorIntOffsetDark = 75;
 		int colorIntOffsetBright = 75;
 		
 		if(max + colorIntOffsetBright >= 255) colorIntOffsetBright = 255 - max;
 		if(min - colorIntOffsetDark <= 0) colorIntOffsetDark = min;
 		
 		colorOffsetBright = a << 24 | (r + colorIntOffsetBright) << 16 | (g + colorIntOffsetBright) << 8 | (b + colorIntOffsetBright);
 		colorOffsetDark = a << 24 | (r - colorIntOffsetDark) << 16 | (g - colorIntOffsetDark) << 8 | (b - colorIntOffsetDark);
 		
 		offset = ta.getDimension(R.styleable.DebossFontText_textPaddingOffset, context.getResources().getDimension(R.dimen.debossfonttext_default_offset));
 		
 		ta.recycle();
 	}
 	
 	public void setText(String text){
 		this.text = text;
		invalidate();
 	}
 	
 	@Override
 	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		doMeasureWork();
 	}
 	
 	private void doMeasureWork(){
 		if(null != text){
 			paint.getTextBounds(text, 0, text.length(), textBound);
 			int textWidth = (int) paint.measureText(text);
 			this.setMeasuredDimension(textWidth, (int) (textSize * 1.2F + (offset * 2)));
 		}
 		
 		width = getMeasuredWidth();
 		height = getMeasuredHeight();
 		centerX = width / 2;
 		centerY = height / 2;
 	}
 	
 	@Override
 	public void onDraw(Canvas canvas){
 		super.onDraw(canvas);
 		
 		paint.setColor(colorOffsetDark);
 		canvas.drawText(text, 0, centerY + (textBound.height() / 2) - (offset * 2), paint);
 		paint.setColor(colorOffsetBright);
 		canvas.drawText(text, 0, centerY + (textBound.height() / 2), paint);
 		
 		paint.setColor(textColor);
 		canvas.drawText(text, 0, centerY + (textBound.height() / 2) - offset, paint);
 		
 	}
 	
 }
