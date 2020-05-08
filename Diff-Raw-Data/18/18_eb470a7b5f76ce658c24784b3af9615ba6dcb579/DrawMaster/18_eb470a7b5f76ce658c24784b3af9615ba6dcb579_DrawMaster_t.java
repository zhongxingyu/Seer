 /*
   Copyright 2008 Google Inc.
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   
        http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 package com.kmagic.solitaire;
 
 import android.content.*;
 import android.graphics.*;
import android.view.*;
 import com.kmagic.solitaire.render.*;
 
 
 public class DrawMaster {
 
 	private Context mContext;
 
 	// Background
 	private int mScreenWidth;
 	private int mScreenHeight;
 	private Paint mBGPaint;
 
 	// Card stuff
 	private final Paint mSuitPaint = new Paint();
 	private Bitmap[] mCardBitmap;
 	private Bitmap mCardHidden;
 
 	private Paint mEmptyAnchorPaint;
 	private Paint mDoneEmptyAnchorPaint;
 	private Paint mShadePaint;
 	private Paint mLightShadePaint;
 
 	private Paint mTimePaint;
 	private int mLastSeconds;
 	private String mTimeString;
 	private Paint mScorePaint;
 
 	private Bitmap mBoardBitmap;
 	private Canvas mBoardCanvas;
 
 	public DrawMaster( Context context ) {
 
 		mContext = context;

		WindowManager wm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		mScreenWidth  = wm.getDefaultDisplay().getWidth();
		mScreenHeight = wm.getDefaultDisplay().getHeight();
 
 		// Background
 		mBGPaint = new Paint();
 		mBGPaint.setARGB( 255, 0, 128, 0 );
 
 		mShadePaint = new Paint();
 		mShadePaint.setARGB( 200, 0, 0, 0 );
 
 		mLightShadePaint = new Paint();
 		mLightShadePaint.setARGB( 100, 0, 0, 0 );
 
 		// Card related stuff
 		mEmptyAnchorPaint = new Paint();
 		mEmptyAnchorPaint.setARGB( 255, 0, 64, 0 );
 		mDoneEmptyAnchorPaint = new Paint();
 		mDoneEmptyAnchorPaint.setARGB( 128, 255, 0, 0 );
 
 		mTimePaint = new Paint();
 		mTimePaint.setTextSize( 18 );
 		mTimePaint.setTypeface( Typeface.create( Typeface.SANS_SERIF, Typeface.BOLD ) );
 		mTimePaint.setTextAlign( Paint.Align.RIGHT );
 		mTimePaint.setAntiAlias( true );
 		mLastSeconds = -1;
 
 		mCardBitmap = new Bitmap[52];
 		DrawCards( false );
 		mBoardBitmap = Bitmap.createBitmap( mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565 );
 		mBoardCanvas = new Canvas( mBoardBitmap );
 	}
 
 	public int GetWidth() {
 		return mScreenWidth;
 	}
 
 	public int GetHeight() {
 		return mScreenHeight;
 	}
 
 	public Canvas GetBoardCanvas() {
 		return mBoardCanvas;
 	}
 
 	public void DrawCard( Canvas canvas, Card card ) {
 		float x = card.GetX();
 		float y = card.GetY();
 		int idx = card.GetSuit() * 13 + (card.GetValue() - 1);
 		canvas.drawBitmap( mCardBitmap[idx], x, y, mSuitPaint );
 	}
 
 	public void DrawHiddenCard( Canvas canvas, Card card ) {
 		float x = card.GetX();
 		float y = card.GetY();
 		canvas.drawBitmap( mCardHidden, x, y, mSuitPaint );
 	}
 
 	public void DrawEmptyAnchor( Canvas canvas, float x, float y, boolean done ) {
 		RectF pos = new RectF( x, y, x + Card.getWidth(), y + Card.getHeight() );
 		if ( !done ) {
 			canvas.drawRoundRect( pos, 4, 4, mEmptyAnchorPaint );
 		} else {
 			canvas.drawRoundRect( pos, 4, 4, mDoneEmptyAnchorPaint );
 		}
 	}
 
 	public void DrawBackground( Canvas canvas ) {
 		canvas.drawRect( 0, 0, mScreenWidth, mScreenHeight, mBGPaint );
 	}
 
 	public void DrawShade( Canvas canvas ) {
 		canvas.drawRect( 0, 0, mScreenWidth, mScreenHeight, mShadePaint );
 	}
 
 	public void DrawLightShade( Canvas canvas ) {
 		canvas.drawRect( 0, 0, mScreenWidth, mScreenHeight, mLightShadePaint );
 	}
 
 	public void DrawLastBoard( Canvas canvas ) {
 		canvas.drawBitmap( mBoardBitmap, 0, 0, mSuitPaint );
 	}
 
 	public void SetScreenSize( int width, int height ) {
 		mScreenWidth = width;
 		mScreenHeight = height;
 		mBoardBitmap = Bitmap.createBitmap( width, height, Bitmap.Config.RGB_565 );
 		mBoardCanvas = new Canvas( mBoardBitmap );
 	}
 
 	public void DrawCards( boolean bigCards ) {
 		CardRenderer renderer;
 		if ( bigCards ) {
 			renderer = new LargeSuitCardRenderer( mContext.getResources(), mSuitPaint );
 		} else {
 			renderer = new NormalCardRenderer( mContext.getResources(), mSuitPaint );
 		}
 		renderer.render();
 		mCardHidden = renderer.getHiddenCard();
 		mCardBitmap = renderer.getCards();
 	}
 
 	public void DrawTime( Canvas canvas, int millis ) {
 		int seconds = (millis / 1000) % 60;
 		int minutes = millis / 60000;
 		if ( seconds != mLastSeconds ) {
 			mLastSeconds = seconds;
 			// String.format is insanely slow (~15ms)
 			if ( seconds < 10 ) {
 				mTimeString = minutes + ":0" + seconds;
 			} else {
 				mTimeString = minutes + ":" + seconds;
 			}
 		}
 		mTimePaint.setARGB( 255, 20, 20, 20 );
 		canvas.drawText( mTimeString, mScreenWidth - 9, mScreenHeight - 9, mTimePaint );
 		mTimePaint.setARGB( 255, 0, 0, 0 );
 		canvas.drawText( mTimeString, mScreenWidth - 10, mScreenHeight - 10, mTimePaint );
 	}
 
 	public void DrawRulesString( Canvas canvas, String score ) {
 		mTimePaint.setARGB( 255, 20, 20, 20 );
 		canvas.drawText( score, mScreenWidth - 9, mScreenHeight - 29, mTimePaint );
 		if ( score.charAt( 0 ) == '-' ) {
 			mTimePaint.setARGB( 255, 255, 0, 0 );
 		} else {
 			mTimePaint.setARGB( 255, 0, 0, 0 );
 		}
 		canvas.drawText( score, mScreenWidth - 10, mScreenHeight - 30, mTimePaint );
 
 	}
 }
