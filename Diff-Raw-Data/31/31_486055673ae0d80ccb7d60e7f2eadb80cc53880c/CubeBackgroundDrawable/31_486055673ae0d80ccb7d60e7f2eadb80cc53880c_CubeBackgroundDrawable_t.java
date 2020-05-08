 package com.project202.adapter;
 
import com.project202.DimenHelper;

import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.ColorFilter;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.drawable.Drawable;
 
 public class CubeBackgroundDrawable extends Drawable {
 
	private  float HEIGHT;
	private  float THICKNESS;
	private  float PADDING;
 	
 	private Paint paint;
 	private int color;
 	
	public CubeBackgroundDrawable(Context context, int color) {
 		this.color = color;
		HEIGHT = DimenHelper.pixelSize(context, 38f);
		THICKNESS = DimenHelper.pixelSize(context, 8f);
		PADDING = DimenHelper.pixelSize(context, 10f);
 		paint = new Paint();
 		paint.setAntiAlias(true);
 		paint.setStyle(Style.FILL);
 	}
 	
 	@Override
 	public void draw(Canvas canvas) {
 		canvas.drawColor(0xFFFFFFFF);
 		
 		Path path = new Path();
 		float canvasWidth = canvas.getWidth();
 		float canvasHeight = canvas.getHeight();
 		
 		float top = canvasHeight - HEIGHT - THICKNESS;
 		float left = PADDING;
 		float width = canvasWidth - PADDING*2 - THICKNESS;
 		
 		path.reset();
 		path.moveTo(left, top+THICKNESS);
 		path.lineTo(left+THICKNESS, top);
 		path.lineTo(left+THICKNESS+width, top);
 		path.lineTo(left+THICKNESS+width, top+HEIGHT);
 		path.lineTo(left+width, top+HEIGHT+THICKNESS);
 		path.lineTo(left, top+HEIGHT+THICKNESS);
 		path.close();
 		paint.setColor(color);
 		canvas.drawPath(path, paint);
 		
 		path.reset();
 		path.moveTo(left, top+THICKNESS);
 		path.lineTo(left+THICKNESS, top);
 		path.lineTo(left+THICKNESS+width, top);
 		path.lineTo(left+width, top+THICKNESS);
 		path.close();
 		paint.setColor(0x33000000);
 		canvas.drawPath(path, paint);
 
 		path.reset();
 		path.moveTo(left+THICKNESS+width, top);
 		path.lineTo(left+THICKNESS+width, top+HEIGHT);
 		path.lineTo(left+width, top+HEIGHT+THICKNESS);
 		path.lineTo(left+width, top+THICKNESS);
 		path.close();
 		paint.setColor(0x11000000);
 		canvas.drawPath(path, paint);
 
 	}
 
 	@Override
 	public int getOpacity() {
 		return 255;
 	}
 
 	@Override
 	public void setAlpha(int alpha) {
 		
 	}
 
 	@Override
 	public void setColorFilter(ColorFilter cf) {
 		
 	}
 
 	public void setColor(int color) {
 		this.color = color;
 		invalidateSelf();
 	}
 
 }
