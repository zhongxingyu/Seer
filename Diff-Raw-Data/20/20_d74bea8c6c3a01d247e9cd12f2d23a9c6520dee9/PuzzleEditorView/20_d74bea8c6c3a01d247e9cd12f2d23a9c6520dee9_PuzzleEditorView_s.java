 package com.gmail.at.sabre.alissa.numberplace.editor;
 
 import com.gmail.at.sabre.alissa.numberplace.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.content.res.Resources.NotFoundException;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.FontMetrics;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
 import android.os.Bundle;
import android.text.TextPaint;
 import android.util.AttributeSet;
import android.util.FloatMath;
 import android.view.MotionEvent;
 import android.view.View;
import android.view.ViewGroup;
 
 /**
  * TODO: document your custom view class.
  * 
  */
 public class PuzzleEditorView extends View {
 	
     final private int mMajorBorderColor;
     final private int mMinorBorderColor;
     final private int mFocusBorderColor;
     final private int mFixedDigitColor;
     final private int mSolutionColor;
     
     final private int mMajorBorderWidth;
     final private int mMinorBorderWidth;
     final private int mFocusBorderWidth;
     
     final private Typeface mTypeface; // Set through fontFamily attribute.
     final private float mFontScale;
     final private float mFontAdjuster;
     
     final private float mPadDistX = 1.2f;
     final private float mPadDistY = -0.8f;
     
     final private String[] mDigitStrings;
     final private String mClearLabel;
     
     final private Paint mPaint;
 
     /***
      * Path to draw a triangle part of a digit pad.
      * This is for a version to float on the right to the focused cell.
      * The origin of the path coordinate is at the upper left corner
      * of the focused cell. 
      */
     final private Path mFloatOnRight;
     
     /***
      * Left version of {@link #mFloatOnRight}
      */
     final private Path mFloatOnLeft;
     
     private int mWidth, mHeight;
     private int mSaneWidth, mSaneHeight; 
     private int mOriginX, mOriginY;
     private int mDeltaX, mDeltaY;
     private float mCharOffsetX, mCharOffsetY;
     
     private float mPadOriginX, mPadOriginY;
     
     private int mFocusX, mFocusY;
     
     private byte[][] mFixedDigits;
     private byte[][] mSolution;
     
     public PuzzleEditorView(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public PuzzleEditorView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         // Load attributes
         final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PuzzleEditorView, defStyle, 0);
         
         mMajorBorderColor = a.getColor(R.styleable.PuzzleEditorView_majorBorderColor, Color.BLACK);
         mMinorBorderColor = a.getColor(R.styleable.PuzzleEditorView_minorBorderColor, Color.BLACK);
         mFocusBorderColor = a.getColor(R.styleable.PuzzleEditorView_focusBorderColor, Color.BLACK);
         mFixedDigitColor = a.getColor(R.styleable.PuzzleEditorView_fixedDigitColor, Color.BLACK);
         mSolutionColor = a.getColor(R.styleable.PuzzleEditorView_solutionColor, Color.BLACK);
         
         mMajorBorderWidth = a.getDimensionPixelSize(R.styleable.PuzzleEditorView_majorBorderWidth, 0);
         mMinorBorderWidth = a.getDimensionPixelSize(R.styleable.PuzzleEditorView_minorBorderWidth, 0);
         mFocusBorderWidth = a.getDimensionPixelSize(R.styleable.PuzzleEditorView_focusBorderWidth, 0);
         
         String font = a.getString(R.styleable.PuzzleEditorView_fontFamily);
         if (font == null) {
         	mTypeface = Typeface.DEFAULT;
         } else {
         	mTypeface = Typeface.create(font, Typeface.NORMAL);
         }
         mFontScale = a.getFloat(R.styleable.PuzzleEditorView_fontScale, 0.8f);
         mFontAdjuster = a.getDimension(R.styleable.PuzzleEditorView_fontAdjuster, 0f);
         
         a.recycle();
         
         // I'm not sure why, but sometimes resource access fails for me...
         Resources res = getContext().getResources();
         String[] digitStrings;
         try {
         	
         	digitStrings = res.getStringArray(R.array.digits);
         } catch (NotFoundException e) {
         	digitStrings = new String[] { "", "1", "2", "3", "4", "5", "6", "7", "8", "9" }; 
         }
         mDigitStrings = digitStrings;
         String clearLabel;
         try {
         	clearLabel = res.getString(R.string.pad_label_clear);
         } catch (NotFoundException e) {
         	clearLabel = "Clear";
         }
         mClearLabel = clearLabel;
         
         final Paint paint = mPaint = new Paint();
         paint.setAntiAlias(true);
         paint.setTypeface(mTypeface);
         paint.setTextAlign(Align.CENTER);
         
         mFloatOnRight = new Path();
         mFloatOnLeft = new Path();
 
         mFocusX = -1;
         mFocusY = -1;
         
         mFixedDigits = new byte[9][9];
         mSolution = new byte[9][9];
         
         if (isInEditMode()) {
         	// Provides some idea how the puzzle will be shown.
         	mFixedDigits = new byte[][] {
         			{ 0, 0, 0, 2, 0, 0, 0, 0, 1 },
         			{ 0, 5, 6, 0, 0, 0, 4, 3, 0 },
         			{ 0, 4, 0, 0, 0, 5, 0, 2, 0 },
         			{ 7, 0, 0, 0, 6, 0, 3, 0, 0 },
         			{ 0, 0, 0, 7, 0, 4, 0, 0, 0 },
         			{ 0, 0, 8, 0, 5, 0, 0, 0, 2 },
         			{ 0, 9, 0, 6, 0, 0, 0, 4, 0 },
         			{ 0, 8, 7, 0, 0, 0, 6, 5, 0 },
         			{ 1, 0, 0, 0, 0, 8, 0, 0, 0 }
         	};
         	mSolution = new byte[][] {
         			{ 2, 7, 6, 0, 0, 0, 0, 0, 0 },
         			{ 8, 3, 5, 0, 0, 0, 0, 0, 0 },
         			{ 1, 4, 9, 0, 0, 0, 0, 0, 0 },
         			{ 5, 6, 3, 0, 0, 0, 0, 0, 0 },
         			{ 4, 1, 2, 0, 0, 0, 0, 0, 0 },
         			{ 7, 9, 8, 0, 0, 0, 0, 0, 0 },
         			{ 6, 5, 4, 0, 0, 0, 0, 0, 0 },
         			{ 3, 2, 1, 0, 0, 0, 0, 0, 0 },
         			{ 9, 8, 7, 0, 0, 0, 0, 0, 0 }
         	};
         }
     }
     
     private static final int STATE_ARRAY_LENGTH = 9 * 9 + 9 * 9;
     
 	public void saveState(Bundle outState) {
 		final byte[] a = new byte[STATE_ARRAY_LENGTH];
 		int i = 0;
 		for (int y = 0; y < 9; y++) {
 			for (int x = 0; x < 9; x++) {
 				a[i++] = mFixedDigits[y][x];
 			}
 		}
 		for (int y = 0; y < 9; y++) {
 			for (int x = 0; x < 9; x++) {
 				a[i++] = mSolution[y][x];
 			}
 		}
 		outState.putByteArray(getClass().getName(), a);
 	}
 
 	public void restoreState(Bundle savedInstanceState) {
 		final byte[] a = savedInstanceState.getByteArray(getClass().getName());
 		if (a != null && a.length == STATE_ARRAY_LENGTH) {
 			int i = 0;
 			for (int y = 0; y < 9; y++) {
 				for (int x = 0; x < 9; x++) {
 					mFixedDigits[y][x] = a[i++];
 				}
 			}
 			for (int y = 0; y < 9; y++) {
 				for (int x = 0; x < 9; x++) {
 					mSolution[y][x] = a[i++];
 				}
 			}
 		}
 	}
 
     public int[] getCellFocus() {
     	if (mFocusX < 0 || mFocusY < 0) return null;
     	return new int[] { mFocusX, mFocusY };
     }
     
     public void setCellFocus(int[] focus) {
     	if (focus == null) {
     		mFocusX = -1;
     		mFocusY = -1;
     	} else {
     		mFocusX = focus[0];
     		mFocusY = focus[1];
     	}
     	invalidate();
     }
     
     public void setCellFocus(int focusX, int focusY) {
     	mFocusX = focusX;
     	mFocusY = focusY;
     	invalidate();
     }
     
     public void resetCellFocus() {
     	mFocusX = -1;
     	mFocusY = -1;
     	invalidate();
     }
     
     public byte[][] getFixedDigits() {
     	return duplicateDigitsArray(mFixedDigits);
     }
     
     public void setFixedDigits(byte[][] digits) {
     	if (digits == null) {
     		mFixedDigits = new byte[9][9];
     	} else {
     		mFixedDigits = duplicateDigitsArray(checkDigitsArray(digits));
     	}
 		invalidate();
     }
     
     public void setSolution(byte[][] digits) {
     	if (digits == null) {
     		mSolution = new byte[9][9];
     	} else {
     		mSolution = duplicateDigitsArray(checkDigitsArray(digits));
     	}
     	invalidate();
     }
 
 	private static byte[][] checkDigitsArray(byte[][] digits) {
 		if (digits == null) throw new NullPointerException("digits must not be null");
     	if (digits.length != 9) throw new IllegalArgumentException(String.format("digits.length must be 9 but %d", digits.length));
     	
     	for (int i = 0; i < digits.length; i++) {
     		final byte[] r = digits[i];
     		if (r == null) throw new NullPointerException(String.format("digits[%d] must not be null", i));
     		if (r.length != 9) throw new IllegalArgumentException(String.format("digits[%d].length must be 9 but %d", i, r.length));
     	}
     	
     	for (int y = 0; y < digits.length; y++) {
     		final byte[] r = digits[y];
     		for (int x = 0; x < r.length; x++) {
     			final byte d = r[x];
     			if (d < 0 || d > 9) {
     				throw new IllegalArgumentException(String.format("digits' element must be in range 0..9, but digit[%d][%d] was %d", y, x, d));
     			}
     		}
     	}
     	
 		return digits;
 	}
 	
 	private static byte[][] duplicateDigitsArray(byte[][] digits) {
     	final byte[][] a = new byte[9][9];
     	for (int i = 0; i < digits.length; i++) {
     		System.arraycopy(digits[i], 0, a[i], 0, 9);
     	}
 		return a;
 	}
 	
 	@Override
 	public void setEnabled(boolean enabled) {
 		super.setEnabled(enabled);
 		resetCellFocus();
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     	// Tries to be square by shrinking the longer side within the allowance.
     	// XXX: This code appears unneeded in the Number Place Breaker application.
     	int w = View.MeasureSpec.getSize(widthMeasureSpec);
     	int h = View.MeasureSpec.getSize(heightMeasureSpec);
     	if (w > h && View.MeasureSpec.getMode(widthMeasureSpec) != View.MeasureSpec.EXACTLY) {
     		w = Math.max(h, getSuggestedMinimumWidth());
     	}
     	if (h > w && View.MeasureSpec.getMode(heightMeasureSpec) != View.MeasureSpec.EXACTLY) {
     		h = Math.max(w, getSuggestedMinimumHeight());
     	}
     	setMeasuredDimension(w, h);
 	}
 
 	@Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         updateMetrics();
         drawPuzzle(canvas);
         if (mFocusX >= 0 && mFocusY >= 0) {
             drawFocusPad(canvas);
         }
     }
 
     private void updateMetrics() {
     	if (mWidth == getWidth() && mHeight == getHeight()) return;
 
     	final int w = mWidth = getWidth();
     	final int h = mHeight = getHeight();
     	
     	// We draw a puzzle in a square, although the drawing code allows any rectangular form.  
        final int t = Math.min(w, h) - (int)FloatMath.ceil(Math.max(mMajorBorderWidth, mMinorBorderWidth));
         final int st = t - t % 9;
         final int sw = mSaneWidth = st;
         final int sh = mSaneHeight = st;
         
         // The puzzle placement
         mOriginX = (w - sw) / 2;
         mOriginY = (h - sh) / 2;
         final int dx = mDeltaX = sw / 9;
         final int dy = mDeltaY = sh / 9;
         
         // Digits
         mPaint.setTextSize(dy * mFontScale);
         final FontMetrics m = mPaint.getFontMetrics();
         mCharOffsetX =dx / 2f;
         mCharOffsetY = (dy - m.ascent - m.descent) / 2f + mFontAdjuster;
         
         // This is not really a _metrics_, but...
         Path path;
         path = mFloatOnRight;
         path.rewind();
         path.moveTo(dx *  0.6f, dy * 0.5f);
 		path.lineTo(dx *  2.0f, dy * 0.1f);
 		path.lineTo(dx *  2.0f, dy * 0.9f);
 		path.close();
 		path = mFloatOnLeft;
 		path.rewind();
         path.moveTo(dx *  0.4f, dy * 0.5f);
 		path.lineTo(dx * -1.0f, dy * 0.9f);
 		path.lineTo(dx * -1.0f, dy * 0.1f);
 		path.close();
 		
 
     }
 
     private void drawPuzzle(Canvas canvas) {
 		final int sw = mSaneWidth;
         final int sh = mSaneHeight;
         final int ox = mOriginX;
         final int oy = mOriginY; 
         final int dx = mDeltaX;
         final int dy = mDeltaY;
         final float cx = mCharOffsetX;
         final float cy = mCharOffsetY;
 
         final Paint paint = mPaint;
         
         paint.setStyle(Style.STROKE);
         
         paint.setColor(mMinorBorderColor);
         paint.setStrokeWidth(mMinorBorderWidth);
         for (int i = 1; i <= 8; i++) {
         	if (i % 3 != 0) {
             	canvas.drawLine(ox + dx * i, oy + 0,      ox + dx * i, oy + sh,     paint);
             	canvas.drawLine(ox,          oy + dy * i, ox + sw,     oy + dy * i, paint);        		
         	}
         }
         
         paint.setColor(mMajorBorderColor);
         paint.setStrokeWidth(mMajorBorderWidth);
         for (int i = 3; i <= 6; i += 3) {
         	canvas.drawLine(ox + dx * i, oy + 0,      ox + dx * i, oy + sh,     paint);
         	canvas.drawLine(ox + 0,      oy + dy * i, ox + sw,     oy + dy * i, paint);        		
         }
         canvas.drawRect(ox, oy, ox + sw, oy + sh, paint);
         
         paint.setStyle(Style.FILL);
         for (int y = 0; y < 9; y++) {
         	final byte[] f = mFixedDigits[y];
         	final byte[] s = mSolution[y];
         	for (int x = 0; x < 9; x++) {
         		int d;
         		if (       (d = f[x]) > 0) {
         			paint.setColor(mFixedDigitColor);
         		} else if ((d = s[x]) > 0) {
         			paint.setColor(mSolutionColor);
         		}
         		if (d > 0) {
         			canvas.drawText(mDigitStrings[d], ox + dx * x + cx, oy + dy * y + cy, paint);
         		}
         	}
         }
 	}
     
 	private void drawFocusPad(Canvas canvas) {
 		final int ox = mOriginX;
 		final int oy = mOriginY; 
 		final int dx = mDeltaX;
 		final int dy = mDeltaY;
 		final float cx = mCharOffsetX;
 		final float cy = mCharOffsetY;
 		
 		final Paint paint = mPaint;
 
 		// The following focus presentation is broken.
 		// I need to follow Android ways.  FIXME!
 		paint.setStyle(Style.STROKE);
 		paint.setColor(mFocusBorderColor);
 		paint.setStrokeWidth(mFocusBorderWidth);
 		final int x0 = ox + dx * mFocusX;
 		final int y0 = oy + dy * mFocusY;
 		canvas.drawRect(x0, y0, x0 + dx, y0 + dy, paint);
 		
 		updatePadOrigin();
 		final float px = mPadOriginX;
 		final float py = mPadOriginY;
 		final float mm = dx * 0.2f; // XXX
 		
 		paint.setStyle(Style.FILL);
 		paint.setColor(Color.LTGRAY);
 		canvas.drawRect(px - mm, py - mm, px + dx * 3 + mm, py + dy * 4 + mm, paint);
 		final Path path = px < x0 ? mFloatOnLeft : mFloatOnRight;
 		path.offset(x0, y0);
 		canvas.drawPath(path, paint);
 		path.offset(-x0, -y0);
 		
 		paint.setColor(Color.BLUE);
 		paint.setStrokeWidth(0);
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 		    	paint.setStyle(Style.STROKE);        			
 				canvas.drawRect(px + dx * j, py + dy * i, px + dx * (j + 1), py + dy * (i + 1), paint);
 				paint.setStyle(Style.FILL);
 				canvas.drawText(mDigitStrings[i * 3 + j + 1], px + dx * j + cx, py + dy * i + cy, paint);
 			}
 		}
 		
 		paint.setStyle(Style.STROKE);        			
 		canvas.drawRect(px, py + dy * 3, px + dx * 3, py + dy * 4, paint);
 		paint.setStyle(Style.FILL);
 		canvas.drawText(mClearLabel, px + dx + cx, py + dy * 3 + cy, paint);
 	}
 	
 	private void updatePadOrigin() {
 		final int ox = mOriginX;
 		final int oy = mOriginY; 
 		final int dx = mDeltaX;
 		final int dy = mDeltaY;
 
 		float x = ox + dx * (mFocusX + 0.5f + mPadDistX);
 		if (x + dx * 3 >= mWidth) {
 			x = ox + dx * (mFocusX + 0.5f - mPadDistX - 3);
 		}
 		mPadOriginX = x;
 		
 		float y = oy + dy * (mFocusY + 0.5f + mPadDistY);
 		if (y < 0) {
 			y = 0;
 		} else if ( y + dy * 4 >= mHeight) {
 			y = mHeight - dy * 4;
 		}
 		mPadOriginY = y;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN: {
 				if (mFocusX >= 0 && mFocusY >= 0) {
 					// Pad is shown.
 					final int x = (int)((event.getX() - mPadOriginX) / mDeltaX);
 					final int y = (int)((event.getY() - mPadOriginY) / mDeltaY);
 					if (x >= 0 && x < 3 && y >= 0 && y < 4) {
 						// It is on the pad.
 						
 						// We need to show the focus on the pad... FIXME!
 						
 						return true;
 					}
 				}
 				
 				final int x = (int)((event.getX() - mOriginX) / mDeltaX);
 				final int y = (int)((event.getY() - mOriginY) / mDeltaY);
 				if (x >= 0 && x < 9 && y >= 0 && y < 9) {
 					
 					// It is on the puzzle.  Focus the cell and show the pad.
 					setCellFocus(x, y);
 					return true;
 	
 				} else {
 					
 					// It is out of the puzzle.  Unfocus and keep watching.
 					resetCellFocus();
 					return true;
 					
 				}
 			}
 		
 			case MotionEvent.ACTION_UP:	{
 				if (mFocusX >= 0 && mFocusY >= 0) {
 					// Pad is shown and ready.
 					final int x = (int)((event.getX() - mPadOriginX) / mDeltaX);
 					final int y = (int)((event.getY() - mPadOriginY) / mDeltaY);
 					if (x >= 0 && x < 3 && y >= 0 && y < 4) {
 						// It is on the pad.
 						int d = x + y * 3 + 1;
 						if (d > 9) d = 0;
 						mFixedDigits[mFocusY][mFocusX] = (byte)d;
 						resetCellFocus();
 						return true;
 					}				
 				}
 				return true;
 			}
 		}
 		
 		return true;
 	}
 }
