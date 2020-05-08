 package com.openboxsoftware.battlepong;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.MotionEvent;
 
 public class VerticalPaddle extends Paddle 
 {
 	protected Paint blue;
 
 	public VerticalPaddle(Context context) {
 		super(context);
 		
 		PADDLE_HEIGHT_RATIO = 0.25f;
 		PADDLE_WIDTH_RATIO = 0.045f;
 
         sideX = 0;
         sideY = (screenHeight / 2) - ((int)getPaddleHeight() / 2);
 		
         blue = new Paint();
         blue.setColor(Color.BLUE);
 	}
 	
 	@Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         
         if(paddle.top < 0) 
         {
             sideY = 0;
         }
             
         if(paddle.bottom > screenHeight) 
         {
            sideY = screenHeight - (int)getPaddleHeight();
         }
         
         paddle.set(sideX, sideY, (int)getPaddleWidth() + sideX, (int)getPaddleHeight() + sideY);
         canvas.drawRect(paddle, blue);
         
         this.invalidate();
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         
         int action = event.getAction();
     	
     	int x = (int)event.getX();
         int y = (int)event.getY();
         
         if(touchLocked) 
         {
         	
         	if(action == MotionEvent.ACTION_UP) 
         	{
         		touchLocked = false;
         	}
         	
         	sideY = y - ((int)getPaddleHeight() / 2);
         	
         	return true;
         	
         } 
         else if(x >= paddle.left && x <= paddle.right
 	                && y >= paddle.top && y <= paddle.bottom) 
         {
 	            
         	if(action == MotionEvent.ACTION_DOWN) 
         	{
         		touchLocked = true;
         	}
         	
             return true;
         }
         else
         {
             return super.onTouchEvent(event);
         }
     }
 
 }
