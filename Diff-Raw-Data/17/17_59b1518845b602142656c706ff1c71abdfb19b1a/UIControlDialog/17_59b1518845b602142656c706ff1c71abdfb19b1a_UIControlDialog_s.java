 package com.games.test1.ui;
 
 import java.util.Vector;
 
 import com.games.test1.Utility;
 import com.games.test1.GameView.GameThread;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 
 public class UIControlDialog extends UIContainer {
 	private static final float DEFAULT_TEXT_SIZE = 13.0f;
 	private static final float TITLE_TEXT_SIZE = 20.0f;
 	
 	private String mTitle;
 	private String mText;
 	private String mLeftLabel;
 	private String mRightLabel;
 	private UIEvent mLeftEvent;
 	private UIEvent mRightEvent;
 
 
 	// Typesetting-related variables.
 	private Vector<String> mLines;
 	private int mTextHeight;
 	private int mYStart;
 	
 	private static Paint dialogPaint, dialogTitlePaint;
 
 	/** Creates a new dialog box. When either button is pressed, the respective
 	 *  event is called. */
 	public UIControlDialog(String title, String text, String leftButtonLabel,
 			UIEvent leftEvent, String rightButtonLabel, UIEvent rightEvent,
 			int width, int height) {
 		super(width, height);
 		mTitle = title;
 		mText = text;
 		mLeftLabel = leftButtonLabel;
 		mRightLabel = rightButtonLabel;
 		mLeftEvent = leftEvent;
 		mRightEvent = rightEvent;
 			
 		
 		
 		setupText();
 	}
 	
 	@Override
 	public void doAfterAttach() {
 		super.doAfterAttach();
 		
 		mInnerUI.addControl(
 				new UIControlButton(100,40,mLeftLabel, mLeftEvent), GameUI.POSITION_BOTTOMLEFT);
 		mInnerUI.addControl(
 				new UIControlButton(100,40,mRightLabel, mRightEvent), GameUI.POSITION_BOTTOMRIGHT);
 	}
 
 	@Override
 	public boolean trigger(GameThread t, int x, int y) {
 		if (mInnerUI.onClick(x, y)) {
 			removeSelf();
 			return true;
 		}
 		return false;
 	}
 	
 	private void setupText() {
 		setupFont();
 		
 		// Prepare text for being drawn to the dialog.
 		mLines = new Vector<String>(); 
 		Utility.typesetText(mText, mWidth, mLines);
 		mYStart = mHeight/2 - (mLines.size() * mTextHeight) / 2 + 20;
 	}
 	
 	private void setupFont() {
 		if (dialogPaint == null) {
 			dialogPaint = new Paint();
 			dialogPaint.setTextSize(DEFAULT_TEXT_SIZE);
 			dialogPaint.setTextAlign(Paint.Align.CENTER);
 			dialogPaint.setAntiAlias(true);
 			dialogPaint.setColor(Color.WHITE);			
 
      dialogTitlePaint = new Paint(dialogPaint)
      dialogTitlePaint.setTextSize(TITLE_TEXT_SIZE);
 		}
		
 		Rect bounds = new Rect();
 		dialogPaint.getTextBounds("AAA", 0, 1, bounds);
 		mTextHeight = (int) (Math.abs(bounds.top - bounds.bottom) * 1.5);
 	}
 
 	/** Draw the dialog box. */
 	public void draw(Canvas c, int x, int y) {
 		// Shadow
 		GameUI.scratchPaint.setColor(Color.DKGRAY);
 		c.drawRect(x + 1, y + 1, x + mWidth + 1, y + mHeight + 1, GameUI.scratchPaint);
 		
 		GameUI.scratchPaint.setStyle(Paint.Style.FILL);
 		GameUI.scratchPaint.setColor(Color.GRAY);
 		
 		c.drawRect(x, y, x + mWidth, y + mHeight, GameUI.scratchPaint);
 	
 		GameUI.scratchPaint.setStyle(Paint.Style.STROKE);
 		GameUI.scratchPaint.setColor(Color.WHITE);
 		c.drawRect(x, y, x + mWidth, y + mHeight, GameUI.scratchPaint);
 		
		c.drawText(mTitle, x + mWidth / 2, y + 20, dialogPaint);
 		for (int i = 0; i < mLines.size(); i++) {					
 			c.drawText(mLines.get(i), 
 					x + mWidth/2, 
 					y + mYStart + i * mTextHeight,
 					dialogPaint);					
 		}				
 		
 		super.draw(c, x, y);
 	}
 }
