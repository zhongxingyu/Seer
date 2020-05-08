 package stkl.spectropolarisclient;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.FloatMath;
 
 public class Player {
 	private final int MAX_OFFSET = 50;
 	private final int MIN_OFFSET = -MAX_OFFSET;
 	private final double SPEED_MOD = 0.05;
 	
 	private float d_x;
 	private float d_y;
 	
 	private float d_direction;
 	private float d_speed;
 	
 	private float d_shootDirection;
 	
 	private int d_health;
 	
 	private Paint d_paint;
 	
 	private int d_id;
 	
 	public Player(int x, int y, Paint paint) {
 		d_x = x;
 		d_y = y;
 		d_direction = 0;
 		d_shootDirection = 0;
 		d_speed = 0;
 		d_paint = paint;
 		d_id = -1;
 		d_health = 100;
 	}
 	
 	private long d_timeSinceLastBullet = 0;
 	
 	public void update(float xMoveOffset, float yMoveOffset, float xShootOffset, float yShootOffset) {
 		d_direction = (float) Math.atan2(xMoveOffset, yMoveOffset);
 		
 		float distance = (float) Math.hypot(xMoveOffset, yMoveOffset);
 		d_speed = (float) (Math.max(Math.min(distance, MAX_OFFSET), MIN_OFFSET) * SPEED_MOD);
 		
 		if(xShootOffset != 0 && yShootOffset != 0 && System.nanoTime() - d_timeSinceLastBullet > 250000000) {
 			d_shootDirection = (float) Math.atan2(xShootOffset, yShootOffset);
			GameActivity.getInstance().model().addBullet().instantiate(d_x, d_y, d_shootDirection, d_id);
 			d_timeSinceLastBullet = System.nanoTime();
 		}
 		
 	}
 	
 	public void step() {
 		float potentialX = d_x + FloatMath.sin(d_direction) * d_speed;
 		float potentialY = d_y + FloatMath.cos(d_direction) * d_speed;
 		
 		if(GameActivity.getInstance().model().collision(potentialX, potentialY, 5) == false)
 		{
 			d_x = potentialX;
 			d_y = potentialY;
 		}
 		
 		Client.getInstance().sent(d_x, d_y, d_direction, d_speed, d_health);
 	}
 	
 	public void addHealth() {
 		d_health = Math.min(d_health + 25, 100);
 	}
 	
 	public int health() {
 		return d_health;
 	}
 	
 	public float xOffset() {
 		return d_x;
 	}
 	
 	public float yOffset() {
 		return d_y;
 	}
 
     public void draw(Canvas canvas, int centerHorizontal, int centerVertical) {
     	canvas.drawCircle(centerHorizontal, centerVertical, 5, d_paint);
 	}
 
 	public int id() {
 		return d_id;
 	}
 	
 	public void setId(int id) {
 		d_id = id;
 	}
 }
