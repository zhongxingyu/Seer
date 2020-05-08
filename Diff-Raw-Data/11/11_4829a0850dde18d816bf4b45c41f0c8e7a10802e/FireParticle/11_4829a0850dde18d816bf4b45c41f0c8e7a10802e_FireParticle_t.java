 public class FireParticle extends Particle{
 	private static final long serialVersionUID = 9082476987034302079L;
	private static final float PARTICLE_VELOCITY = 0.01f;
 	private static final int PARTICLE_LIFETIME = 30;
 	private static final float PARTICLE_SIZE = 0.02f;
 	private static final float PARTICLE_SPIN = 0.01f;
 	private static final float SHRINK_RATE = 0.00005f;
 	
 	FireParticle(Actor ship) {
 		position = new Vector(ship.getTailPosition());
 		// Relative to the ship
 		velocity = new Vector(ship.getVelocity());
 		// Add the speed of the shot
 		velocity.incrementXBy(-PARTICLE_VELOCITY * (Math.cos(ship.getTheta()) + (gen.nextFloat() - 0.5f) / 2));
 		velocity.incrementYBy(-PARTICLE_VELOCITY * (Math.sin(ship.getTheta()) + (gen.nextFloat() - 0.5f) / 2));
 		init();
 	}
 	
 	FireParticle(Vector pos, Vector vel) {
 		position = new Vector(pos);
 		velocity = vel.normalize().scaleBy(PARTICLE_VELOCITY);
 		init();
 	}
 	
 	FireParticle(Vector pos) {
 		position = new Vector(pos);
 		velocity = new Vector(gen.nextFloat() * 2 * Math.PI);
 		velocity.scaleBy(PARTICLE_VELOCITY);
 		
 		init();
 	}
 	
 	private void init(){
 		velocity.scaleBy(rand(0.9f, 1));
 		
 		theta = 0;
 		//TODO textures for our particles
 		//sprite = Sprite.particle();
 		omega = PARTICLE_SPIN;
 		size = PARTICLE_SIZE;
 		
 		colorR = 1.0f;
 		colorG = 1.0f;
 		colorB = 1.0f;
 		colorA = 1.0f;
 	}
 	
 	public void update() {
 		super.update();
 		// Shrink the particles
 		size -= age * SHRINK_RATE;
 		
 		if (age > PARTICLE_LIFETIME)
 			delete();
 	}
 	
 	protected void updateColor() {
 		colorR -= 0.015f;
 		colorG -= 0.05f;
 		colorB -= 0.2f;
 	}
 	
 	/* returns a random float between positive floats low and high */
 	private static float rand(float l, float h) {
 		return gen.nextFloat() * (h - l) + l;
 	}
 }
