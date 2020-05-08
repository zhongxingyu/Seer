 package com.additt.custom_views;
 
 import java.util.Iterator;
 
 import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScroller;
 import com.twotoasters.android.horizontalimagescroller.widget.TextStyle;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class MapleTextView extends ImageView implements OnTouchListener {
 
 	private Bitmap mCurrAd;
 	private String mText;
 	private PointF mTextPos;
     private static final float DEFAULT_TEXT_SIZE = 30;
     
     // Percent size of image
     private static final float BANNER_SIZE = .2f;
     private static final float BANNER_TEXT_SIZE = .4f;
 	private PointF mPrevTouch;
 	private TextStyle mTextStyle;
 	
 	private ScaleGestureDetector mScaleDetector;
 	private float mScaleFactor = 1.f;
 	private Builder mAddTextDialogBuilder;
 	private AlertDialog mAddTextDialog;
 	private GestureDetector mGestureDetector;
 	private float mRatio;
 	private RectF mBanner;
 	private Paint mBannerPaint;
 	private Paint mBannerTextPaint;
 	
 	// The scroller holding the text styles
 	private HorizontalImageScroller mScroller;
 
 	public MapleTextView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mCurrAd = null;
 		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
 		mGestureDetector = new GestureDetector(context, new GestureListener());
 
 		mText = "";
 		mTextPos = new PointF(0, DEFAULT_TEXT_SIZE);
 		
 		mBannerPaint = new Paint();
 		mBannerPaint.setARGB(170, 0, 0, 0);
 		
 		mBannerTextPaint = new Paint();
 		mBannerTextPaint.setColor(Color.WHITE);
 		mBannerTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
 		mBannerTextPaint.setTextAlign(Align.CENTER);
 		
 		mBanner = new RectF();
 		
 		/****** Dialog for user to enter text ****************/
 		mAddTextDialogBuilder = new AlertDialog.Builder(context);
 		mAddTextDialogBuilder.setTitle("Add Text");
 		mAddTextDialogBuilder.setMessage("Enter the text you would like to include in your ad");
 
 		final EditText input = new EditText(context);
 		mAddTextDialogBuilder.setView(input);
 		mAddTextDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString();
 				mText = value;
 				invalidate();
 			}
 		});
 
 		mAddTextDialogBuilder.setNegativeButton("Cancel",
 			new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					mAddTextDialog.dismiss();
 				}
 	
 		});
 		
 		mAddTextDialog = mAddTextDialogBuilder.create();
 		
 		
 	}
 	
 	/**
	 * Draws text on the canvas given a position.
 	 * @param canvas
 	 * @param pos
	 * @param ratio, this denotes how much the text needs to be scaled for particular bitmap (the one displayed on screen vs the real one)
 	 */
	private void drawText(Canvas canvas, PointF pos, float ratio) {
 		if (mText != null && !mText.equals("")) {
 			for (Paint p : mTextStyle) {
				p.setTextSize((DEFAULT_TEXT_SIZE * mScaleFactor) / ratio);
 				canvas.drawText(mText, pos.x, pos.y, p);
 			}
 		}
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
		drawText(canvas, mTextPos, 1.0f);
 		
 		// If there's no text draw a banner with instructions.
 		if (mText == null || mText.equals("")) {
 			canvas.drawRect(mBanner, mBannerPaint);
 			canvas.drawText("Hold to add text", mBanner.width() / 2,
 					mBanner.bottom - (mBanner.height() / 2) + (mBannerTextPaint.getTextSize() / 2), 
 					mBannerTextPaint);
 			if(mScroller != null) mScroller.setVisibility(INVISIBLE);
 		}
 		
 		// show the style scroller if there is text
 		else if(mScroller != null) mScroller.setVisibility(VISIBLE);
 	}
 	
 	/**
 	 * Moves text by provided delta
 	 * @param deltaX
 	 * @param deltaY
 	 */
 	private void moveText(float deltaX, float deltaY) {
 		mTextPos.set(mTextPos.x + deltaX, mTextPos.y + deltaY);
 		invalidate();
 	}
 	
 	/**
 	 * Sets the styel of the text
 	 * @param textStyle
 	 */
 	public void setStyle(TextStyle textStyle) {
 		mTextStyle = textStyle;
 		invalidate();
 	}
 	/**
 	 * Sets the ad. Also takes a ratio so that we can scale the text to the same size later.
 	 * @param ad
 	 * @param ratio
 	 */
 	public void setAd(Bitmap ad, float ratio) {
 		setImageBitmap(ad);
 		mCurrAd = ad;
 		mRatio = ratio;
 		
 		float top = (mCurrAd.getHeight() * mRatio) - (BANNER_SIZE * mCurrAd.getHeight() * mRatio);
 		float right = (mCurrAd.getWidth() * mRatio);
 		float bottom = mCurrAd.getHeight() * mRatio;
 		mBanner.set(0, top, right, bottom);
 		mBannerTextPaint.setTextSize(BANNER_TEXT_SIZE * mBanner.height());
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		return false;
 	}
 	
 	/**
 	 * When it detects a scaling gesture, it will scale. Moves the text if there is only
 	 * one pointer. Also will activate long click to edit text.
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 	    // Let the ScaleGestureDetector inspect all events.
 	    mScaleDetector.onTouchEvent(ev);
 	    mGestureDetector.onTouchEvent(ev);
 	    
 	    if (ev.getPointerCount() == 1) {
 		    switch (ev.getAction()) {
 			    case MotionEvent.ACTION_DOWN:
 			    	if (mPrevTouch == null) {
 			    		mPrevTouch = new PointF(ev.getX(), ev.getY());
 			    	} else {
 			    		mPrevTouch.set(ev.getX(), ev.getY());	
 		    		}
 			    	break;
 				case MotionEvent.ACTION_MOVE:
 					moveText(ev.getX() - mPrevTouch.x, ev.getY() - mPrevTouch.y);
 		    		mPrevTouch.set(ev.getX(), ev.getY());
 					break;
 				case MotionEvent.ACTION_CANCEL:
 					break;
 				case MotionEvent.ACTION_UP:
 					break;
 				default:
 					break;
 		    }
 	    }
 	    return true;
 	}
 	
 	/**
 	 * Creates a bitmap from the displayed image with the text included.
 	 * @return
 	 */
 	public Bitmap addText() {
 		Bitmap newAd = Bitmap.createBitmap(mCurrAd.getWidth(), mCurrAd.getHeight(), mCurrAd.getConfig());
         Canvas canvas = new Canvas(newAd);
         canvas.drawBitmap(mCurrAd, new Matrix(), null);
        PointF pos = new PointF((mTextPos.x - (this.getPaddingLeft() / 2)) / mRatio, (mTextPos.y - (this.getPaddingTop() / 2)) / mRatio);
		drawText(canvas, pos, mRatio);
 
         return newAd;
         
 	}
 
 	/**
 	 * Android has built in scale listener.
 	 * 
 	 *
 	 */
 	private class ScaleListener 
         extends ScaleGestureDetector.SimpleOnScaleGestureListener {
 
 		@Override
 	    public boolean onScale(ScaleGestureDetector detector) {
 	        mScaleFactor *= detector.getScaleFactor();
 	
 	        // Don't let the object get too small or too large.
 	        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
 	        for (Paint p : mTextStyle) {
 				p.setTextSize(DEFAULT_TEXT_SIZE * mScaleFactor);
 
 	        }
 	        invalidate();
 	        return true;
 	    }
 	}
 	
 	private class GestureListener
 		extends GestureDetector.SimpleOnGestureListener {
 		
 		@Override
 		public void onLongPress(MotionEvent e) {
 			mAddTextDialog.show();
 		}
 	}
 
 	/** Pass in the style scroller so that it can be hidden
 	 * when there is no text shown
 	 * @param mScroller The scroller displaying the text styles
 	 */
 	public void setStyleScroller(HorizontalImageScroller scroller) {
 		mScroller = scroller;		
 	}
 
 }
