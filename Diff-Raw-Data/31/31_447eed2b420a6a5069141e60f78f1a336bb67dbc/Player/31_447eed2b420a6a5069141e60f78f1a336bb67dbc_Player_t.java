 package stkl.spectropolarisclient;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Vibrator;
 import android.util.FloatMath;
 
 public class Player {
 	private final int MAX_OFFSET = 50;
 	private final int MIN_OFFSET = -MAX_OFFSET;
 	private final double SPEED_MOD = 0.05;
 	
 	private final int MAX_AMMO = 100;
 	
 	private float d_x;
 	private float d_y;
 	
 	private float d_direction;
 	private float d_speed;
 	
 	private float d_shootDirection;
 	
 	private int d_health;
 	private int d_ammo;
 	
 	private int d_weaponIndex;
 	private final int MAX_WEAPON_INDEX = 1;
 	// Index 0: pistol, Index 1: shotgun
 	
 	private Paint d_paint;
 	
 	private int d_id;
 	
 	private Bullet d_lastBullet;
 	
 	private Vibrator d_vibrator;
 	
 	private float d_radius = 2.5f;
 	
 	public Player(int x, int y, Paint paint) {
 		d_x = x;
 		d_y = y;
 		d_direction = 0;
 		d_shootDirection = 0;
 		d_speed = 0;
 		d_paint = paint;
 		d_id = -1;
 		d_health = 100;
 		d_lastBullet = null;
 		d_vibrator = null;
 		d_ammo = MAX_AMMO;
 		d_weaponIndex = 0;
 	}
 	
 	private long d_timeSinceLastBullet = 0;
 	
 	public void update(float xMoveOffset, float yMoveOffset, float xShootOffset, float yShootOffset) {
 		d_direction = (float) Math.atan2(xMoveOffset, yMoveOffset);
 		
 		float distance = (float) Math.hypot(xMoveOffset, yMoveOffset);
 		d_speed = (float) (Math.max(Math.min(distance, MAX_OFFSET), MIN_OFFSET) * SPEED_MOD);
 		
 		if(d_ammo > 0 && xShootOffset != 0 && yShootOffset != 0 && System.nanoTime() - d_timeSinceLastBullet > 250000000) {
 			switch(d_weaponIndex) {
 			case 0:
 				d_shootDirection = (float) Math.atan2(xShootOffset, yShootOffset);
 				d_lastBullet = GameActivity.getInstance().model().addBullet();
 				
 				d_lastBullet.instantiate(d_x, d_y, d_shootDirection, d_id);
 				d_ammo--;
 				d_timeSinceLastBullet = System.nanoTime();
 				break;
 			case 1:
				d_shootDirection = (float) Math.atan2(xShootOffset, yShootOffset);
				d_lastBullet = GameActivity.getInstance().model().addBullet();
				
				d_lastBullet.instantiate(d_x, d_y, d_shootDirection, d_id);
				d_ammo--;
				d_timeSinceLastBullet = System.nanoTime();
 				break;
 			}
 		}
 		
 	}
 	
 	public void step() {
 		float potentialX = d_x + FloatMath.sin(d_direction) * d_speed;
 		float potentialY = d_y + FloatMath.cos(d_direction) * d_speed;
 		
 		if(GameActivity.getInstance().model().collision(potentialX, potentialY, d_radius) == false)
 		{
 			d_x = potentialX;
 			d_y = potentialY;
 		}
 			
 		Client.getInstance().sent(d_x, d_y, d_direction, d_speed, d_health, d_ammo, d_lastBullet);
 		d_lastBullet = null;
 	}
 	
 	public boolean changeHealth(int change) {
 		d_health = Math.max(Math.min(d_health + change, 100), 0);
 		
 		if(d_health == 0)
 			return true;
 		
 		if(change < 0) {
 			if(d_vibrator == null)
 				d_vibrator = (Vibrator) GameActivity.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
 			
 			d_vibrator.vibrate(100);
 		}
 		
 		return false;
 	}
 	
 	public int health() {
 		return d_health;
 	}
 	
 	public void incAmmo(int change) {
 		d_ammo = Math.max(Math.min(d_ammo + change, MAX_AMMO), 0);
 	}
 	
 	public int ammo() {
 		return d_ammo;
 	}
 	
 	public float xOffset() {
 		return d_x;
 	}
 	
 	public float yOffset() {
 		return d_y;
 	}
 
     public void draw(Canvas canvas, int centerHorizontal, int centerVertical) {
     	canvas.drawCircle(centerHorizontal, centerVertical, d_radius, d_paint);
 	}
 
 	public int id() {
 		return d_id;
 	}
 	
 	public void setId(int id) {
 		d_id = id;
 	}
 
 	public void checkIfShot(float x1, float y1, float x2, float y2, int id) {
 		if(id == d_id)
 			return;
 		
 		System.err.println("checkIfShot");
 		
 		if(sqrDistanceToLine(x1, y1, x2, y2) < d_radius * d_radius)
 			changeHealth(-1);
 	}
 	
 	private float sqrDistanceToLine(float x1, float y1, float x2, float y2) {
 		float sqrLength = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
 		
 		if(sqrLength == 0)
 			return (x1 - d_x) * (x1 - d_x) + (y1 - d_y) * (y1 - d_y);
 			
 		float t = dot(d_x - x1, d_y - y1, x2 - x1, y2 - y1) / sqrLength;
 		if(t < 0)
 			return (x1 - d_x) * (x1 - d_x) + (y1 - d_y) * (y1 - d_y);
 		if(t > 1)
 			return (x2 - d_x) * (x2 - d_x) + (y2 - d_y) * (y2 - d_y);
 		
 		float xProj = x1 + t * (x2 - x1);
 		float yProj = y1 + t * (y2 - y1);
 		
 		return (xProj - d_x) * (xProj - d_x) + (yProj - d_y) * (yProj - d_y);
 	}
 
 	private float dot(float x1, float y1, float x2, float y2) {
 		return x1 * x2 + y1 * y2;
 	}
 	
 	public void changeWeapon(int direction) {
 		d_weaponIndex = Math.min(Math.max(d_weaponIndex + direction, 0), MAX_WEAPON_INDEX);
 	}
 }
