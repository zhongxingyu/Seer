 package ca.idrc.tecla.hud;
 
 import ca.idrc.tecla.R;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.widget.ImageButton;
 
 public class TeclaHUDButtonView extends ImageButton {
 	
 	public final static byte POSITION_LEFT = 4;
 	public final static byte POSITION_TOPLEFT = 5;
 	public final static byte POSITION_TOP = 6;
 	public final static byte POSITION_TOPRIGHT = 7;
 	public final static byte POSITION_RIGHT = 8;
 	public final static byte POSITION_BOTTOMRIGHT = 9;
 	public final static byte POSITION_BOTTOM = 10;
 	public final static byte POSITION_BOTTOMLEFT = 11;
 	public final static float CORNER_PADDING_FRACTION = 0.16f;
 	
 	private Context mContext;
 	
     private final Paint mInnerFillPaint = new Paint();
     private final Paint mInnerStrokePaint = new Paint();
     private final Paint mOuterStrokePaint = new Paint();
     private int mWidth;
     private int mHeight;
     private int mStrokeWidth;
 	private Path mPath;
 	private Bitmap mBackground;
 	private Canvas mBackgroundCanvas;
 	private byte mPosition;
 	private boolean isHighlighted;
 
 	public TeclaHUDButtonView(Context context, AttributeSet attrs) {
         super(context, attrs);
         mContext = context;
 		init();
 	}
 
 	private void init() {
 		
 		mStrokeWidth = 1;
 		mInnerFillPaint.setColor(mContext.getResources().getColor(R.color.hud_btn_inner_fill_color));
     	mInnerStrokePaint.setColor(mContext.getResources().getColor(R.color.hud_btn_inner_stroke_color));
     	mOuterStrokePaint.setColor(mContext.getResources().getColor(R.color.hud_btn_outer_stroke_color));
     	mInnerFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
     	mInnerStrokePaint.setStyle(Paint.Style.STROKE);
     	mOuterStrokePaint.setStyle(Paint.Style.STROKE);
 
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 
 		mWidth = w;
 		mHeight = h;
 
 		updateDrawables();
     	
 	}
 	
 	public void setProperties(byte position, int stroke_width, boolean highlighted) {
 		mPosition = position;
 		mStrokeWidth = stroke_width;
 		isHighlighted = highlighted;
 		updateDrawables();
 	}
 	
 	public void setHighlighted(boolean highlighted) {
 		isHighlighted = highlighted;
 		updateDrawables();
 	}
 	
 	private void updateDrawables() {
 		updateBackground();
 		updatePadding();
 		invalidate();
 	}
 	
 	private void updatePadding () {
 		int w = getWidth();
 		int h = getHeight();
 		int ypad = Math.round(h * CORNER_PADDING_FRACTION);
 		int xpad = Math.round(w * CORNER_PADDING_FRACTION);
 		switch(mPosition) {
 		case POSITION_LEFT:
 		case POSITION_RIGHT:
 		case POSITION_TOP:
 		case POSITION_BOTTOM:
 	    	setPadding(0, 0, 0, 0);
 			break;
 		case POSITION_TOPLEFT:
	    	setPadding(xpad, ypad, 0, 0);
 			break;
 		case POSITION_TOPRIGHT:
	    	setPadding(0, ypad, xpad, 0);
 			break;
 		case POSITION_BOTTOMLEFT:
	    	setPadding(xpad, 0, 0, ypad);
 			break;
 		case POSITION_BOTTOMRIGHT:
	    	setPadding(0, 0, xpad, ypad);
 			break;
 		}
 	}
 	
 	private void updateBackground () {
 		if (mWidth > 0 && mHeight > 0) {
 			
 			mBackground = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
 	    	mBackgroundCanvas = new Canvas(mBackground);
 	    	
 	    	int outer_stroke_width = 2 * mStrokeWidth;
 	    	mInnerStrokePaint.setStrokeWidth(mStrokeWidth);
 	    	mOuterStrokePaint.setStrokeWidth(outer_stroke_width);
 	    	mPath = new Path();
 	    	int left = outer_stroke_width;
 	    	int top = outer_stroke_width;
 	    	int right = mWidth - outer_stroke_width;
 	    	int bottom = mHeight - outer_stroke_width;
 			float pad;
 
 			if (mPosition == POSITION_TOPLEFT ||
 					mPosition == POSITION_TOPRIGHT ||
 					mPosition == POSITION_BOTTOMLEFT ||
 					mPosition == POSITION_BOTTOMRIGHT) {				
 				pad = 0.28f * right;
 				mPath.moveTo(left, bottom - pad);
 		    	mPath.lineTo(right - pad, top);
 		    	mPath.lineTo(right, bottom - pad);
 		    	mPath.lineTo(right - pad, bottom);
 		    	mPath.lineTo(left, bottom - pad);
 		    	float background_rotation = 0.0f;
 				switch(mPosition) {
 				case POSITION_TOPRIGHT:
 					background_rotation = 90.0f;
 			    	break;
 				case POSITION_BOTTOMLEFT:
 					background_rotation = 270.0f;
 			    	break;
 				case POSITION_BOTTOMRIGHT:
 					background_rotation = 180.0f;
 			    	break;
 				}		    	
 		    	Matrix m = new Matrix();
 		    	m.setRotate(background_rotation, mWidth/2.0f, mHeight/2.0f);
 		    	mPath.transform(m);
 			} else {
 				switch(mPosition) {
 				case POSITION_LEFT:
 					pad = 0.2f * right;
 					mPath.moveTo(left, top);
 			    	mPath.lineTo(right, pad);
 			    	mPath.lineTo(right, bottom - pad);
 			    	mPath.lineTo(left, bottom);
 			    	mPath.lineTo(left, top);
 			    	break;
 				case POSITION_TOP:
 					pad = 0.2f * bottom;
 					mPath.moveTo(left, top);
 			    	mPath.lineTo(right, top);
 			    	mPath.lineTo(right - pad, bottom);
 			    	mPath.lineTo(pad, bottom);
 			    	mPath.lineTo(left, top);
 			    	break;
 				case POSITION_RIGHT:
 					pad = 0.2f * right;
 					mPath.moveTo(right, top);
 			    	mPath.lineTo(right, bottom);
 			    	mPath.lineTo(left, bottom - pad);
 			    	mPath.lineTo(left, pad);
 			    	mPath.lineTo(right, top);
 			    	break;
 				case POSITION_BOTTOM:
 					pad = 0.2f * bottom;
 					mPath.moveTo(left, bottom);
 			    	mPath.lineTo(pad, top);
 			    	mPath.lineTo(right - pad, top);
 			    	mPath.lineTo(right, bottom);
 			    	mPath.lineTo(left, bottom);
 			    	break;
 				}		    	
 			}
 	    	
 	    	mBackgroundCanvas.drawPath(mPath, mOuterStrokePaint);
 	    	if (isHighlighted) {
 	    		mBackgroundCanvas.drawPath(mPath, mInnerFillPaint);
 	    	}
 	    	mBackgroundCanvas.drawPath(mPath, mInnerStrokePaint);
 	    	setBackground(new BitmapDrawable(getResources(), mBackground));
 		}
 	}
 }
