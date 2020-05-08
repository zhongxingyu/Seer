 public class Asteroid extends Actor {
 	private static final float LARGE_ASTEROID_SIZE = 0.15f;
 	private static final float SMALL_ASTEROID_SIZE = 0.15f;
 
 	public Asteroid() {
 		int randSide = gen.nextInt(3);
 		float px = 0, py = 0;
 
 		//have the asteroids first appear off screen at a random spot
 		switch(randSide){
 		case(0):
 			px = -1f;
 		py = gen.nextFloat() * 2 - 1;
 		break;
 		case(1):
 			px = 1f;
 		py = gen.nextFloat() * 2 - 1;
 		break;
 		case(2):
 			px = gen.nextFloat() * 2 - 1;
 		py = -1f;
 		break;
 		case(3):
 			px = gen.nextFloat() * 2 - 1;
 		py = 1f;
 		break;
 		}
 		position = new Vector(px, py);
 		// Make our Asteroids initial velocity random, not always towards the first quadrant
 		velocity = new Vector((gen.nextFloat() - 0.5f )/40, (gen.nextFloat() - 0.5f) /40);
 		sprite = Sprite.asteroid();
 		omega = gen.nextFloat() / 60;
 		theta = gen.nextFloat() * 2.0f * (float)Math.PI;
 		size = gen.nextFloat() / 8.0f + 0.1f;
 		id = generateId();
 	}
 
 	public Asteroid(Vector p, Vector v, float s, int parent) {
 		position = p;
 		velocity = v;
 		sprite = Sprite.asteroid();
 		omega = gen.nextFloat() / 60;
 		theta = gen.nextFloat() * 2.0f * (float)Math.PI;
 		size = s;
 		id = generateId();
 		parentId = parent;
 	}
 
 
 	public void handleCollision(Actor other) {
 		// We don't want to blow up on PowerUps
 		if(other instanceof PowerUp){
 			return;
 		} else if(other instanceof Bullet){
 			ScorePanel.getScorePanel().asteroidHit(size);
 		}
 
 		// Play our awesome explosion if sound is enabled
 		if (SoundEffect.isEnabled()){
 			if (this.isLarge())
 				SoundEffect.forLargeAsteroidDeath().play();
 			else if (this.isSmall())
 				SoundEffect.forSmallAsteroidDeath().play();
 		}
 
 
 		//If asteroids is very small
 		if (size < SMALL_ASTEROID_SIZE){
 			// Remove ourself from the game since we blew up
 			delete();
 			// Add cool debrisParticles. The ParticleSystem knows if they are disabled or not
 			ParticleSystem.addDebrisParticle(this);
 		} else {
			float original_mass = size * size * size;
			Vector originalMomemntum = new Vector(velocity).scaleBy(original_mass);
 
			for (int i = 0; i < 2; i++) {
 				// pick a new direction of our asteroid	fragment
 				float direction = gen.nextFloat() * 2 * (float)Math.PI;
				float new_mass = original_mass * (gen.nextFloat() + 1) / 3; // between 1/3 and 2/3
 
 				// TODO fix velocity so energy is conserved pick an energy less than the original energy
 				Vector newVelocity = new Vector(direction).scaleBy(velocity.magnitude());
				Vector newMomentum = new Vector(newVelocity).scaleBy(new_mass);
 
				original_mass -= new_mass;
				originalMomemntum.incrementBy(newMomentum.scaleBy(-1)); // Subtract the momentum of this fragment from our parent asteroid
 
				float new_size = (float) Math.pow(new_mass, 1f/3f); // Subtract our new asteroid mass from the original asteroid
 
				Actor.actors.add(new Asteroid(new Vector(position), newVelocity, new_size, id));
 			}
			size = (float) Math.pow(original_mass, 1f/3f);
			velocity = originalMomemntum.scaleBy(1/original_mass);
 		}
 	}
 
 	public boolean isLarge(){
 		return size >= LARGE_ASTEROID_SIZE;
 	}
 
 	public boolean isSmall() {
 		return size <= SMALL_ASTEROID_SIZE;
 	}
 }
