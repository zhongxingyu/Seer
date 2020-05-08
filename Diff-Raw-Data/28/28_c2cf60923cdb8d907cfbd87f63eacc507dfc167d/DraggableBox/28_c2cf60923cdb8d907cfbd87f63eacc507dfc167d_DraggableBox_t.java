 package com.google.corrigan.owen.wordformed;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.Log;
 import android.view.MotionEvent;
 
 public class DraggableBox
 {
 	private float rectX;
 	private float rectY;
 	private int rectSize = 45;
	private int borderSize = 5;
	private RectF rect, rect2;
 	private boolean dragging = false;
 	
 	public DraggableBox(float topX, float topY)
 	{
 		rectX = topX;
 		rectY = topY;
 		rect = new RectF(rectX, rectY, rectX+rectSize, rectY+rectSize);
		rect2 = new RectF(rectX + borderSize, rectY + borderSize, 
				rectX + rectSize - borderSize, rectY + rectSize - borderSize);
 	}
 	
 	public void draw(Canvas canvas)
 	{
		//Draw Outer Rectangle
 		Paint dragRectangle = new Paint();
 		dragRectangle.setColor(Color.BLUE);
 		canvas.drawRect(rect, dragRectangle);
		
		//Draw inner Rectangle
		dragRectangle.setColor(Color.WHITE);
		canvas.drawRect(rect2, dragRectangle);
 	}
 	
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		float mouseX = event.getX();
 		float mouseY = event.getY();
 		Log.d("WORDFORMED", "MouseX = "+ mouseX + ", Mouse Y = " + mouseY);
 		switch(event.getAction())
 		{
 			case MotionEvent.ACTION_DOWN:
 				if(mouseX + 10 > rectX && 
 						mouseX - 10 < rectX + rectSize && 
 						mouseY + 10 > rectY && 
 						mouseY - 10 < rectY + rectSize)
 				{
 					//offsetX = mouseX - rectX;
 					//offsetY = mouseY - rectY;
 					dragging = true;
 					Log.d("WORDFORMED", "Inside Rectangle");
 				}
 				else dragging = false;
 				break;
 			case MotionEvent.ACTION_UP:
				if(dragging)
				{
					rectX -= 40;
					rectY -= 40;
				}
 				/*
 				Log.d("MOUSEUP", "Mouse X = " + mouseX + " Mouse Y = " + mouseY );
 				if(mouseX > snapX && 
 						mouseX < snapX + rectSize && 
 						mouseY > snapY && 
 						mouseY <  snapY + rectSize)
 				{
 					Log.d("MOUSEUP", "Inside the Snapper!" );
 					rectX = snapX;
 					rectY = snapY;
 					Log.d("WORDFORMED", "Mouse up and inside snapX");
 					rect = new RectF(rectX, rectY, rectX+rectSize, rectY+rectSize);
 				}
 				*/
 				break;
 			default:
 				if(dragging)
 				{
 					rectX = mouseX;
 					rectY = mouseY;
 					rect = new RectF(rectX - 40, rectY - 40, rectX+rectSize - 40, rectY+rectSize - 40);
					rect2 = new RectF(rectX + borderSize - 40, rectY + borderSize - 40, 
							rectX + rectSize - borderSize - 40, rectY + rectSize - borderSize - 40);
 				}
 		}
 		return true;
 	}
 }
