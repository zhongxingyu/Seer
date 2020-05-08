 package stkl.spectropolarisclient;
 
 import java.io.DataInputStream;
 import java.nio.ByteBuffer;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.util.FloatMath;
 
 public class Bullet {	
 	private float d_x1;
 	private float d_y1;
 	
 	private float d_x2;
 	private float d_y2;
 	
 	//private float d_direction;
 	//private float d_speed;
 	
 	private int d_id;
 	
 	private Paint d_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	
 	private int d_transparency;
 	
 	public Bullet() {
 		d_transparency = 0;
 		d_x1 = 0;
 		d_y1 = 0;
     	d_x2 = 0;
 		d_y2 = 0;
 		
 		d_id = -1;		
 
 		d_paint.setColor(Color.CYAN);
 	}
 	
 	public void instantiate(float x, float y, float direction, int id) {
 		d_x1 = x;
 		d_y1 = y;
     	d_x2 = d_x1 + FloatMath.sin(direction) * 1000;
 		d_y2 = d_y1 + FloatMath.cos(direction) * 1000;
 		Point point = GameActivity.getInstance().model().collision(d_x1, d_y1, d_x2, d_y2);
 		
 		if(point != null) {
 			d_x2 = point.x;
 			d_y2 = point.y;
 		}
 		
 		d_id = id;
 		d_transparency = 255;
 		d_paint.setAlpha(d_transparency);
 	}
 	
 	public void instantiate(float x1, float y1, float x2, float y2, int id, int transparency) {
 		d_x1 = x1;
 		d_y1 = y1;
     	d_x2 = x2;
 		d_y2 = y2;
 		
 		d_id = id;
 		d_transparency = transparency;
 		d_paint.setAlpha(d_transparency);
 		
 		
 	}
 	
 	public void instantiate(Bullet other) {
 		d_x1 = other.d_x1;
 		d_y1 = other.d_y1;
     	d_x2 = other.d_x2;
 		d_y2 = other.d_y2;
 		//d_speed = other.d_speed;
 		d_id = other.d_id;
 		d_transparency = other.d_transparency;
 		d_paint.setAlpha(d_transparency);
 	}
 	
 	public void instantiate(DataInputStream in) throws Exception {
 		d_x1 = in.readFloat();
 		d_y1 = in.readFloat();
 		d_x2 = in.readFloat();
 		d_y2 = in.readFloat();
 		
 		d_id = in.readInt();
		d_transparency = in.readInt();
 		
 		if(d_id == GameActivity.getInstance().model().player().id()) {
 			d_transparency = 0;
 		} else {
 			GameActivity.getInstance().model().player().checkIfShot(d_x1, d_y1, d_x2, d_y2, d_id);
 		}
 			
 		d_paint.setAlpha(d_transparency);
 	}
 	
 	public void addToBuffer(ByteBuffer buffer) {
 		buffer.putFloat(d_x1);
 		buffer.putFloat(d_y1);
 		buffer.putFloat(d_x2);
 		buffer.putFloat(d_y2);
 		
 		buffer.putInt(d_id);
 		buffer.putInt(d_transparency);
 	}
 	
 	public boolean step() {
 		if(destroyed())
 			return false;
 				
 		if(d_transparency <= 0) {
 			destroy();
 			return true;
 		} else {
 			d_transparency -= 3000/d_transparency;
 		}
 		d_paint.setAlpha(d_transparency);
 		
 		return false;
 	}
 	
 	public void destroy() {
 		d_id = -1;
 	}
 	
 	public boolean destroyed() {
 		return d_id == -1;
 	}
 
     public void draw(Canvas canvas) {		
     	//float potentialX = d_x + FloatMath.sin(d_direction) * 1000;
 		//float potentialY = d_y + FloatMath.cos(d_direction) * 1000;
     	if(!destroyed()) {
     		//canvas.drawCircle(d_x, d_y, 1, d_paint);
     		canvas.drawLine(d_x1, d_y1, d_x2, d_y2, d_paint);
     	}
 	}
 
 	public static int sendSize() {
 		return 6 * 4;
 	}
 }
