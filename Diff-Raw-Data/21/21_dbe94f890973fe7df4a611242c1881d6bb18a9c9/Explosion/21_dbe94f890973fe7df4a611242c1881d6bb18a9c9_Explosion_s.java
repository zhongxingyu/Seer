 package pl.spaceshooters.particle;
 
 import pl.blackburn.graphics.Animation;
 import pl.spaceshooters.aurora.particle.ParticleType;
 import pl.spaceshooters.util.ResourceLoader;
 
 public class Explosion extends AnimatedParticle {
 	
 	private static int duration = 100;
	private int life;
 	
 	public Explosion(float startX, float startY) {
		super(startX, startY, true, ParticleType.EXPLOSION, new Animation(ResourceLoader.getSpriteSheet("effects/explosion.png", 64, 60, 1, 1), duration));
		life = duration * animation.getFrameCount();
 	}
 	
 	@Override
 	public void update(int delta) {
 		position.y += velocity * delta;
 		
		life -= 20;
		
		if (life <= 0) {
 			this.setAlive(false);
 		}
 	}
 }
