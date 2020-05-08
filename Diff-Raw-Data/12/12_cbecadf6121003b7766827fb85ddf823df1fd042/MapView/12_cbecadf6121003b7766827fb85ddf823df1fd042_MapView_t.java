 package second.prototype;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import control.appearance.DrawableIndex;
 import control.stage.Stage;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class MapView extends View {
 	
 	/** Members */
 	private Stage stage = ContainerBox.currentStage;
 	
 	private float viewCenterh,viewCenterw;
 	private float myX,myY;
 	
 	private float[] rotateMatrix = {1,0,1,0};
 	
 	private static final float mag = ContainerBox.meterPerPixel;
 	private static final float ruler = 100/mag; //m
 	
 	private static final float northX = 0;
	private static final float northY = ContainerBox.visibleRange/mag*2/3;
 	
 	
 	public MapView(Context context) {
 		super(context);
 		init();
 	}
 
 	public MapView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public MapView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	/** Control signal */
 	
 	public void setViewCenter(float w,float h){
 		// set center point of map
 		viewCenterh = h/2;
 		viewCenterw = w/2;
 		
 		myX = 0;
 		myY = 0;
 	}
 	
 	public void setCurrentLocation(float currentX, float currentY) {
 		myX = currentX/mag;
 		myY = currentY/mag;
 
 		invalidate();
 	}
 	
 	public void setOrientation(float theta) {
 		// rotate matrix 
 		// r[0] r[1]
 		// r[2] r[3]
 		rotateMatrix[0] = (float) Math.cos(theta/180*Math.PI);
		rotateMatrix[1] = (float)-Math.sin(theta/180*Math.PI);
		rotateMatrix[2] = (float) Math.sin(theta/180*Math.PI);
 		rotateMatrix[3] = (float) Math.cos(theta/180*Math.PI);
 		
 		invalidate();
 	}
 	
 	private float rotateX(float _x,float _y) {
 		if(ContainerBox.isTab){
 			return rotateMatrix[0]*_x+rotateMatrix[1]*_y;
 		} else {
 			return rotateMatrix[0]*_y+rotateMatrix[1]*(-_x);
 		}
 	}
 	
 	private float rotateY(float _x,float _y) {
 		if(ContainerBox.isTab){
 			return rotateMatrix[2]*_x+rotateMatrix[3]*_y;
 		} else {
 			return rotateMatrix[2]*_y+rotateMatrix[3]*(-_x);
 		}
 	}
 	
 	
 	
 	/** Utilities */
 	private void init() {
 		
 		this.setBackgroundResource(DrawableIndex.BACK_GROUND);
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 
 		Paint self,tar,text,white,blue,empty;
 		
 		self = new Paint();
 		self.setColor(Color.RED);
 		
 		tar = new Paint();
 		tar.setColor(Color.CYAN);
 		tar.setAlpha(255);
 		
 		text = new Paint();
 		text.setColor(Color.WHITE);
 		text.setStyle(Paint.Style.STROKE);
 		
 		white = new Paint();
 		white.setColor(Color.WHITE);
 		white.setStyle(Paint.Style.STROKE);
 		white.setStrokeWidth(3);
 		
 		blue = new Paint();
 		blue.setColor(Color.BLUE);
 		blue.setAlpha(100);
 		blue.setStrokeWidth(8);
 		
 		empty = new Paint();
 		empty.setColor(Color.BLACK);
 		empty.setAlpha(0);
 		
 		
 		
 		// radar
 		//canvas.drawText("Radar Mode ! White circle is visible range.", 30, 30, text);
 		//canvas.drawText("Visible range = "+ContainerBox.visibleRange, 30, 45, text);
 		//canvas.drawText("Scale = "+ContainerBox.meterPerPixel+" meters per pixel", 30, 60, text);
 
 		//canvas.drawText("Current Center = "+stage.getMapCenter("X")+":"+stage.getMapCenter("Y"),30, 110, text);
 		if(stage.isCleared()){
 			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.spir2), 5, 5, self);
 			canvas.drawText("Stage cleared",30, 110, text);
 		}
 		
 		
 		// visible range
 		canvas.drawCircle(viewCenterw, viewCenterh, ContainerBox.visibleRange/mag, white);
 		canvas.drawCircle(viewCenterw, viewCenterh, 10, self);
 		
 		// north arrow
		canvas.drawLine(viewCenterw+rotateX(northX,northY),viewCenterh-rotateY(northX,northY),
				viewCenterw+rotateX(northX*2,northY*2),viewCenterh-rotateY(northX*2,northY*2),white);
 		
 		
 		canvas.drawLine(viewCenterw-ruler/2, viewCenterh, viewCenterw+ruler/2, viewCenterh,text);
 		canvas.drawLine(viewCenterw-ruler/2, viewCenterh-10, viewCenterw-ruler/2, viewCenterh+10,text);
 		canvas.drawLine(viewCenterw+ruler/2, viewCenterh-10, viewCenterw+ruler/2, viewCenterh+10,text);
 		canvas.drawText(ruler*mag+" m", viewCenterw+ruler/2+10, viewCenterh+20, text);
 		
 		
 		// links
 		for(int i=0;i<stage.links();i++) {
 			String name = stage.getLink(i).nameOfEnd;
 			float sX,sY,eX,eY;
 			
 			sX = rotateX(stage.getLink(i).startX-myX,stage.getLink(i).startY-myY)/mag + viewCenterw;
 			sY = -rotateY(stage.getLink(i).startX-myX,stage.getLink(i).startY-myY)/mag + viewCenterh;
 			eX = rotateX(stage.getLink(i).endX-myX,stage.getLink(i).endY-myY)/mag + viewCenterw;
 			eY = -rotateY(stage.getLink(i).endX-myX,stage.getLink(i).endY-myY)/mag + viewCenterh;
 			
 			canvas.drawLine(sX,sY,eX,eY, stage.getPointOf(name).isVisible?blue:empty);
 		}
 		
 		// points
 		for(int i=0;i<stage.length();i++) {
 			
 			float x = rotateX(stage.getPointOf(i).x-myX,stage.getPointOf(i).y-myY)/mag + viewCenterw;
 			float y = -rotateY(stage.getPointOf(i).x-myX,stage.getPointOf(i).y-myY)/mag + viewCenterh;
 			
 			canvas.drawCircle(x , y, 10, stage.getPointOf(i).isVisible?tar:empty);
 			//canvas.drawBitmap(location, x-location.getWidth()/2, y-location.getHeight(), stage.getPointOf(i).isVisible?tar:empty);
 			canvas.drawText(stage.getPointOf(i).getName(), x+15 , y-15, stage.getPointOf(i).isVisible?text:empty);
 				}
 		
 		
 		super.onDraw(canvas);
 	}
 
 }
