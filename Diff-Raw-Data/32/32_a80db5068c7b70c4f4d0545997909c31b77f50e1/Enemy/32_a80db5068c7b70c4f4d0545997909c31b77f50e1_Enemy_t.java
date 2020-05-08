 package com.raiden.game;
 
 import java.util.ArrayList;
 
 import com.raiden.framework.Image;
 
 import android.graphics.Point;
 
 public class Enemy extends Ship {
 	public boolean outOfRange;
 	public float angle, nextAngle;
 	private float radians;
 	
 	private int moveX;
 	private int moveY;
 
 	private static final int MAX_BULLETS = 10;
 	
 	private int turnSpeed;
 	
 	private FlightPattern flightPattern;
 	
 	public Type type;
 	public PowerUp.Type powerUpTypeDrop;
 	
 	// iterating variables
 	private static Bullet bullet;
 	private static int length;
 	
 	public static enum Type{
 		Normal(6 , 2, 4, 10, 50, 1400, Bullet.Type.Enemy,      Assets.enemy1, "Normal"),
 		Fast  (10, 4, 2, 20, 50, 2000, Bullet.Type.EnemyHeavy, Assets.enemy2, "Fast"),
		TankBoss (1, 1, 300, 1000, 100, 500, Bullet.Type.EnemyHeavy, Assets.tankBoss, "TankBoss"),
		TechBoss (4, 5, 200, 2000, 70, 700, Bullet.Type.EnemyHeavy, Assets.techBoss, "TechBoss");
 		
 		public int speed, turnSpeed, armor, score, radius, reloadDone;
 		public Image image;
 		public Bullet.Type bulletType;
 		public String id;
 		
 		public static Type getType(String id){
 			Type[] types = Type.values();
 			for (Type type : types){
 				if ( type.id.equals(id) )
 					return type;
 			}
 			return null;
 		}
 		
 		Type(int speed, int turnSpeed, int armor, int score, 
 				int radius, int reloadDone, Bullet.Type bulletType, Image image, String id){
 			this.speed = speed;
 			this.turnSpeed = turnSpeed;
 			this.armor = armor;
 			this.score = score;
 			this.radius = radius;
 			this.reloadDone = reloadDone;
 			this.bulletType = bulletType;
 			this.image = image;
 			this.id = id;
 		}
 	}
 	
 	public void dropPowerUp(){
 		if (this.powerUpTypeDrop != null) {
 			gameScreen.spawnPowerUp(this.x, this.y, powerUpTypeDrop);
 			powerUpTypeDrop = null;
 		}
 	}
 	
 	public void setTarget(Ship target){
 		this.target = target;
 		for (Turret turret: turrets) {
 			turret.setTarget(target);
 		}
 	}
 
 	public Enemy(Ship target) {
 		this.visible = false;
 		this.outOfRange = true;
 		this.alive = false;
 		this.autofire = true;
 		this.target = target;
 		this.type = Type.Normal;
 
 		emptyTurretPositions = new ArrayList<Point>();
 		emptyTurretPositions.add(new Point(0, 0));
 
 		turrets = new ArrayList<Turret>();
 		addTurret(0.0f, this.type.bulletType);
 		
 		shots = new Bullet[MAX_BULLETS];
 		
 		for (int i = 0; i < MAX_BULLETS; i++)
 		{
 			shots[i] = new Bullet();
 		}
 	}
 	
 	public void setType(Type type){
 		this.type = type;
 		this.setSpeed(type.speed);
 		this.turnSpeed = type.turnSpeed;
 		this.bulletType = type.bulletType;
 		this.armor = type.armor;
 		this.score = type.score;
 		this.radius = type.radius;
 		this.reloadDone = type.reloadDone;
 		for (Turret turret : turrets) {
 			turret.setBulletType(this.type.bulletType);
 		}
 	}
 
 	public void spawn(int x, int y, float angle, Type type, FlightPattern flightPattern, PowerUp.Type PowerUpDrop) {
 		this.x = x;
 		this.y = y;
 		this.angle = angle; this.nextAngle = angle;
 		this.visible = true;
 		this.outOfRange = false;
 		this.radians = (float) Math.toRadians(angle);
 		this.setType(type);
 		this.alive = true;
 		this.impactTimer = IMPACT_INTERVAL;
 		this.reloadTime = reloadDone;
 		this.readyToFire = true;
 		this.powerUpTypeDrop = PowerUpDrop;
 		if (flightPattern != null)
 			this.flightPattern = new FlightPattern(flightPattern);
 	}
 
 	public void update(float deltaTime){
 
 		if (outOfRange || !alive) return;
 		
 		x += moveX;
 		y += moveY;
 
 		if (x < minX || y < minY || x > maxX || y > maxY)
 			visible = false;
 		else
 			visible = true;
 
 		if ( !visible ){
 			// if the enemy is out of bounds, it isn't coming back to the screen
 			if (x < outMinX || y < outMinY || x > outMaxX || y > outMaxY){
 				outOfRange = true;
 				flightPattern = null;
 				return;
 			}
 		}
 		
 		if (target != null)
 			length = target.shots.length;
 		else 
 			length = 0;
 		for (int i = 0; i < length; i++) {
 			bullet = target.shots[i];
 			if (bullet.visible)
 				this.checkCollision(bullet);
 		}
 
 		reload(deltaTime);
 		
 		if (Float.compare(angle, nextAngle) != 0 && flightPattern != null){
 			adjustAngle(flightPattern.getCurrentDirection());
 		} else if (flightPattern != null) {
 			flightPattern.update(deltaTime);
 			nextAngle = flightPattern.getCurrentAngle();
 		}
 
 		if (target != null && autofire) {
 			shoot();
 		}
 
 		if (impactTimer < IMPACT_INTERVAL)
 			impactTimer += deltaTime;
 	}
 
 	public boolean isInGame(){
 		return (alive && !outOfRange);
 	}
 
 	public void setFlightPattern(FlightPattern flightPattern){
 		this.flightPattern = flightPattern;
 	}
 
 	public boolean isOutOfRange(){
 		return outOfRange;
 	}
 	
 	public boolean isAlive(){
 		return alive;
 	}
 
 	public float getAngle() {
 		return angle;
 	}
 	
 	@Override
 	public void setSpeed(int speed){
 		this.speed = speed;
 		this.moveX = Math.round(speed * FastMath.cos( radians));
 		this.moveY = Math.round(speed * FastMath.sin(-radians));
 	}
 	
 	public void setAngle(float angle){
 		if (Float.compare(this.angle, angle) == 0) return;
 		this.angle = angle;
 		
 		if (this.angle > 360.0f)
 			this.angle -= 360.0f;
 		else if (this.angle < 0.0f)
 			this.angle += 360.0f;
 		
 		this.radians = (float) Math.toRadians(angle);
 		this.setSpeed(this.speed);
 	}
 	
 	public void setNextAngle(float angle){
 		this.nextAngle = angle;
 	}
 	
 	public void adjustAngle(Direction direction){
 		if (Float.compare(angle, nextAngle) != 0){
 			float turningAngle =  Math.abs(this.angle - nextAngle);
 			if ( turningAngle > turnSpeed){
 				setAngle(direction.turn(this.angle,turnSpeed));
 			} else {
 				setAngle(direction.turn(this.angle, turningAngle));
 			}
 		}
 	}
 	
 	@Override
 	public void takeDamage(Collidable collidable){
 		notifyObservers(collidable, Event.EnemyHit);
 		super.takeDamage(collidable);
 	}
 	
 	public boolean checkIfDestroyed(){
 		if (armor < 1){
 			alive = false; visible = false; flightPattern = null;
 			notifyObservers(Event.Explosion);
 			notifyObservers(this, Event.ScoreUp);
 			dropPowerUp();
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
