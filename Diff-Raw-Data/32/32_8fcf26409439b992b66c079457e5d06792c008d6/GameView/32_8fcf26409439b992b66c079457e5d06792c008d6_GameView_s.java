 package com.hungry_bubbles;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class GameView extends View 
 {
 	private GameRenderer renderer;
 	
 	public GameView(Context context, GameRenderer renderer) {
 		super(context);
 		
 		this.renderer = renderer;
 	}
 
 	@Override
 	protected void onDraw(Canvas c)
 	{
 		super.onDraw(c);
 		renderer.renderGame(c);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent e)
 	{
 		return renderer.handlePlayerTouch(e);
 	}
 }
