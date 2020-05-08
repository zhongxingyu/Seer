 package com.potato.evolutiongame.views;
 
 import java.util.ArrayList;
 
 import com.potato.evolutiongame.game.cards.Card;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Region;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Scroller;
 
 public class CardDisplayView extends View {
 	Paint bgPaint;
 	Paint whitePaint;
 	Rect wholeRect;
 	ArrayList<Card> list;
 	GestureDetector detector;
 	Scroller scroll;
 	int cardSelected;
 	
 	int offsetX;
 	int maxOffsetX;
 	int cardWidth;
 	int cardHeight;
 	int cardPadding;
 	Rect cardDest;
 	
 	public CardDisplayView(Context ctx, AttributeSet attr)
 	{
 		super(ctx, attr);
 		bgPaint = new Paint();
 		bgPaint.setColor(Color.BLACK);
 		bgPaint.setStyle(Paint.Style.FILL);
 		whitePaint = new Paint();
 		whitePaint.setColor(Color.WHITE);
 		whitePaint.setStyle(Paint.Style.FILL);
 		
 		list = new ArrayList<Card>();
 		offsetX = 0;
 		detector = new GestureDetector(ctx, new gestureListen());
 		scroll = new Scroller(ctx);
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldW, int oldH)
 	{
 		cardPadding = h / 16 + 1;
 		cardHeight = h - (2 * cardPadding);
 		cardWidth = (int)(cardHeight / 1.4f);
 		
 		maxOffsetX = list.size() * (cardWidth + cardPadding) - getWidth() + cardPadding;
 		
 		cardDest = new Rect(0, (int)(cardPadding * 1.5), cardWidth, cardHeight);
 		wholeRect = new Rect(0, 0, getWidth(), getHeight());	
 	}
 	
 	@Override
 	protected void onDraw(Canvas c)
 	{
 		c.clipRect(wholeRect, Region.Op.REPLACE);
 		c.drawRect(wholeRect, bgPaint);
 		
 		for (int i = 0; i < list.size(); ++i)
 		{
 			Card card = list.get(i);
 			cardDest.left = cardPadding + i * (cardWidth + cardPadding) - offsetX;
 			cardDest.right = cardPadding + i * (cardWidth + cardPadding) + cardWidth - offsetX;
 			c.drawBitmap(card.getImage(), null, cardDest, null);
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent evt)
 	{
		detector.onTouchEvent(evt);
		super.onTouchEvent(evt);
		return true;
 	}
 	
 	private class gestureListen implements GestureDetector.OnGestureListener
 	{

 		@Override
 		public boolean onDown(MotionEvent arg0) {
			return false;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
 				float arg3) {
 			return false;
 		}
 
 		@Override
 		public void onLongPress(MotionEvent arg0) {
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float dX,
 				float dY) {
 			offsetX += dX;
 			if (offsetX < 0 || getWidth() > list.size() * (cardWidth + cardPadding)) 
 				offsetX = 0;
 			if (offsetX > maxOffsetX)
 				offsetX = maxOffsetX;
 			postInvalidate();
 			return false;
 		}
 
 		@Override
 		public void onShowPress(MotionEvent arg0) {
 		}
 
 		@Override
 		public boolean onSingleTapUp(MotionEvent arg0) {
 			int x = (int)(arg0.getX() + offsetX);
 			cardSelected = x / (cardWidth + cardPadding);
 			// Intentionally non-consume to allow for onClickEvent
 			return true;
 		}
 	}
 
 	
 	public int getCardIndexSelected()
 	{
 		return cardSelected;
 	}
 	public void setCardList(ArrayList<Card> l)
 	{
 		list = l;
 		maxOffsetX = list.size() * (cardWidth + cardPadding) - getWidth() + cardPadding;
 	}
 }
