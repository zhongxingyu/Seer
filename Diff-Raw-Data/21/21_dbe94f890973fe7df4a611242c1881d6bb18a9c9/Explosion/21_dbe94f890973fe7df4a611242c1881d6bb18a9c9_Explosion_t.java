 package pl.spaceshooters.particle;
 
 import pl.blackburn.graphics.Animation;
 import pl.spaceshooters.aurora.particle.ParticleType;
import pl.spaceshooters.main.Spaceshooters;
 import pl.spaceshooters.util.ResourceLoader;
 
 public class Explosion extends AnimatedParticle {
 	
 	private static int duration = 100;
 	
 	public Explosion(float startX, float startY) {
		super(startX, startY, true, ParticleType.EXPLOSION, new Animation(ResourceLoader.getSpriteSheet("effects/explosion.png", 64, 60, 1, 1), duration, false));
 	}
 	
 	@Override
 	public void update(int delta) {
 		position.y += velocity * delta;
 		
		if (animation.doneRendering() || position.y > Spaceshooters.HEIGHT) {
 			this.setAlive(false);
 		}
 	}
 }
