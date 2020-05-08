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
	private RectF rect;
 	private boolean dragging = false;
 	
 	public DraggableBox(float topX, float topY)
 	{
 		rectX = topX;
 		rectY = topY;
 		rect = new RectF(rectX, rectY, rectX+rectSize, rectY+rectSize);
 	}
 	
 	public void draw(Canvas canvas)
 	{
		//Draw Rectangle
 		Paint dragRectangle = new Paint();
 		dragRectangle.setColor(Color.BLUE);
 		canvas.drawRect(rect, dragRectangle);
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
					rectX -= 40;
					rectY -= 40;
 				}
				dragging = false;
 				*/
 				break;
 			default:
 				if(dragging)
 				{
 					rectX = mouseX;
 					rectY = mouseY;
 					rect = new RectF(rectX - 40, rectY - 40, rectX+rectSize - 40, rectY+rectSize - 40);
 				}
 		}
 		return true;
 	}
 }
