 package pl.spaceshooters.entity;
 
 import java.util.Random;
 
 import pl.blackburn.graphics.Animation;
 import pl.blackburn.graphics.Animation.AnimationInfo;
 import pl.blackburn.graphics.SpriteSheet;
 import pl.spaceshooters.aurora.entity.EntityType;
 import pl.spaceshooters.aurora.level.ILevel;
 import pl.spaceshooters.main.Spaceshooters;
 
 /**
  * Simple enemy implementation. Enemy is slower than {@link Asteroid}, but it
  * has double the health.
  * 
  * @author Mat
  * 
  */
 public class Enemy extends AnimatedEntity {
 	
	private static final Animation anim = new Animation(new SpriteSheet("data/vanilla/textures/entities/enemy.png", 34, 41, 3, 1), new AnimationInfo(Enemy.class.getResourceAsStream("/data/vanilla/textures/entities/enemy.animinfo")));
 	private Random random = new Random();
 	
 	public Enemy(ILevel level, float startX, float startY) {
 		super(level, startX, startY, EntityType.ENEMY, anim);
 		velocity = 0.28F; // 0.4F
 		health = 1;
 	}
 	
 	@Override
 	public void update(int delta) {
 		this.collideCheck();
 		
 		if ((health <= 0) || (position.y >= Spaceshooters.HEIGHT)) {
 			this.setAlive(false);
 		}
 		
 		position.y += velocity * delta;
 	}
 	
 	@Override
 	public void onDeath() {
 		super.onDeath();
 		if (random.nextInt(100) <= 30) { // && level.getPlayer().getAmmo() < 30
 			level.spawnEntity(new BulletSupply(level, this.getX(), this.getY()));
 		}
 	}
 }
