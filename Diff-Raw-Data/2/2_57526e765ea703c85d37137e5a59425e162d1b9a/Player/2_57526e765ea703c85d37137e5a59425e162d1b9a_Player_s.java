 package stkl.spectropolarisclient;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
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
 	private final int NUM_BULLETS_FIRED_SHOTGUN = 6;
 	private final int NUM_BULLETS_CONSUMED_SHOTGUN = 2;
 	private final int SHOTGUN_MAX_SPREAD_MOD = 1;
 	// Index 0: pistol, Index 1: shotgun
 	
 	private Paint d_paint;
 	
 	private int d_id;
 	
 	private ArrayList<Bullet> d_lastBullets;
 	
 	private Vibrator d_vibrator;
 	
 	private float d_radius = 2.5f;
 	
 	private Random d_random;
	
	public Player(int x, int y, Paint paint) {
 
 	private static Bitmap s_bitmap;
 	private static boolean s_initialized = false;
 	
 	public Player(int x, int y, Paint paint) {		
 		d_x = x;
 		d_y = y;
 		d_direction = 0;
 		d_shootDirection = 0;
 		d_speed = 0;
 		d_paint = paint;
 		d_id = -1;
 		d_health = 100;
 		d_lastBullets = new ArrayList<Bullet>();
 		d_vibrator = null;
 		d_ammo = MAX_AMMO;
 		d_weaponIndex = 0;
 		d_random = new Random();
 	}
 	
 	private long d_timeSinceLastBullet = 0;
 	
 	public void update(float xMoveOffset, float yMoveOffset, float xShootOffset, float yShootOffset) {
 		d_direction = (float) Math.atan2(xMoveOffset, yMoveOffset);
 		
 		float distance = (float) Math.hypot(xMoveOffset, yMoveOffset);
 		d_speed = (float) (Math.max(Math.min(distance, MAX_OFFSET), MIN_OFFSET) * SPEED_MOD);
 		
 		if(d_ammo > 0 && xShootOffset != 0 && yShootOffset != 0 && System.nanoTime() - d_timeSinceLastBullet > 250000000) {
 			d_shootDirection = (float) Math.atan2(xShootOffset, yShootOffset);
 			Bullet bullet;
 			switch(d_weaponIndex) {
 			case 0:
 				bullet = GameActivity.getInstance().model().addBullet();
 				synchronized(d_lastBullets) {
 					d_lastBullets.add(bullet);
 				}
 				bullet.instantiate(d_x, d_y, d_shootDirection, d_id);
 				d_ammo--;
 				break;
 			case 1:
 				if(d_ammo > NUM_BULLETS_CONSUMED_SHOTGUN) {
 					for(int idx = 0; idx < NUM_BULLETS_FIRED_SHOTGUN; ++idx) {
 						bullet = GameActivity.getInstance().model().addBullet();
 						synchronized(d_lastBullets) {
 							d_lastBullets.add(bullet);
 						}
 						float direction = d_shootDirection + ((d_random.nextFloat()-0.5f) * SHOTGUN_MAX_SPREAD_MOD);
 						
 						bullet.instantiate(d_x, d_y, direction, d_id);
 					}
 					d_ammo -= NUM_BULLETS_CONSUMED_SHOTGUN;
 				}
 				break;
 			}
 			d_timeSinceLastBullet = System.nanoTime();
 		}
 		
 	}
 	
 	public void step() {
 		if(d_health == 0)
 			return;
 		
 		float potentialX = d_x + FloatMath.sin(d_direction) * d_speed;
 		float potentialY = d_y + FloatMath.cos(d_direction) * d_speed;
 		
 		if(GameActivity.getInstance().model().collision(potentialX, potentialY, d_radius) == false)
 		{
 			d_x = potentialX;
 			d_y = potentialY;
 		}
 		
 		synchronized(d_lastBullets) {
 			Client.getInstance().sent(d_x, d_y, d_direction, d_speed, d_health, d_ammo, d_lastBullets);
 			d_lastBullets.clear();
 		}
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
 	
 	private Rect d_rect = new Rect(0, 0, 0, 0);
 
     public void draw(Canvas canvas, int centerHorizontal, int centerVertical) {
 		if(s_initialized == false) {
 			s_bitmap = BitmapFactory.decodeResource(GameActivity.getInstance().getResources(), R.drawable.cross);
 			s_initialized = true;
 		}
     	
     	if(d_health > 0)
     		canvas.drawCircle(centerHorizontal, centerVertical, d_radius, d_paint);
     	else {
     		d_rect.bottom = (int) (centerVertical + d_radius);
     		d_rect.left = (int) (centerHorizontal - d_radius);
     		d_rect.right = (int) (centerHorizontal + d_radius);
     		d_rect.top = (int) (centerVertical - d_radius);
     		
     		canvas.drawBitmap(s_bitmap, null, d_rect, null);
     	}
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
 		d_weaponIndex = d_weaponIndex + direction;
 		if(d_weaponIndex < 0)
 			d_weaponIndex = MAX_WEAPON_INDEX;
 		if(d_weaponIndex > MAX_WEAPON_INDEX)
 			d_weaponIndex = 0;
 		
 		System.out.println("Weapon switched");
 	}
 }
