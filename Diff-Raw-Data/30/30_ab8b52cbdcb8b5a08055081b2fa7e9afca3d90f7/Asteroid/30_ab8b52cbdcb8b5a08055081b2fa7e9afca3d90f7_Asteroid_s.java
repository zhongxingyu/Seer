 package actor;
 
 import actor.ship.projectile.Projectile;
 import graphics.core.Model;
 import graphics.particles.Fire;
 import graphics.particles.ParticleSystem;
 import graphics.particles.generators.Explosion;
 import math.Quaternion;
 import math.Vector3f;
 
 public class Asteroid extends Actor {
     private static final long serialVersionUID = 916554544709785597L;
     private static final String MODEL_NAME = Model.Models.ASTEROID;
     private static final int SHATTER_THRESHOLD = 4;
     private static final float DEFAULT_SCALE = 10f;
     private static final float HEALTH_MODIFIER= 1.0f / 2.0f;
     private static final String SOUND_EFFECT = "explode.wav";
     private static final float EFFECT_VOLUME = 10.5f;
     private static final float FRAGMENT_SPEED = 0.01f;
    private static final float DAMAGE_FACTOR = 0.5f;
     private static final float MAX_ROTATION_SPEED = 1.5f;
 
     protected int hitPoints;
 
     public Asteroid(){
         super();
         this.angularVelocity = Quaternion.newRandom(MAX_ROTATION_SPEED);
         this.scale = new Vector3f(DEFAULT_SCALE,DEFAULT_SCALE,DEFAULT_SCALE).plus(Vector3f.newRandom(1));
         this.hitPoints = calculateHitpoints(); 
         this.modelName = MODEL_NAME;
     }
 
     private int calculateHitpoints() {
         return (int) (scale.magnitude2() * HEALTH_MODIFIER);
     }
 
     public Asteroid(Vector3f position, Vector3f velocity){
         this();
         this.setPosition(position);
         this.setVelocity(velocity);
         this.modelName = MODEL_NAME;
     }
 
     public Asteroid(Asteroid other){
         super(other);
         hitPoints = other.hitPoints;
     }
 
     @Override
     public void handleCollision(Actor other) {
         if(other instanceof Projectile){
             hitPoints -= ((Projectile) other).getDamage();
         } else if ( other instanceof Asteroid || other instanceof actor.ship.Ship){
            float otherKE = other.getMass() * other.getVelocity().magnitude2() * 0.5f;

            hitPoints -= otherKE * DAMAGE_FACTOR;
         }
         if(hitPoints < 0)
             die();
        bounce(other);
     }
 
     @Override
     public void die(){
         sound.Event effect = new sound.Event(getPosition(), getVelocity(),sound.Library.findByName(SOUND_EFFECT));
         // make bigger asteroids explode bigger
         effect.gain = EFFECT_VOLUME * scale.magnitude2();
         effect.pitch = 0.6f;
         sound.Manager.addEvent(effect);
         velocity.timesEquals(0);
         if(ParticleSystem.isEnabled())
             ParticleSystem.addEvent((new Explosion<Fire>(Fire.class,this)).setIntensity((int)scale.magnitude2()));
 
         if(scale.magnitude2() > SHATTER_THRESHOLD){
             // Make a copy of this, but be sure it reset its HP
             Asteroid a = new Asteroid(this);
             a.scale = this.scale.times(0.5f);
             a.hitPoints = a.calculateHitpoints();
             a.id = null; // ID must be null or the actor set wont add it
             // Move it a random direction half the radius for each piece
             Vector3f displacement = Vector3f.newRandom(1).normalize().times(getRadius() * 0.52f);
             Asteroid b = new Asteroid(a);
             a.position.plusEquals(displacement);
             a.velocity.plusEquals(displacement.times(FRAGMENT_SPEED));
             a.setRotation(Quaternion.newRandom(100));
             a.setAngularVelocity(Quaternion.newRandom(MAX_ROTATION_SPEED));
             b.position.plusEquals(displacement.negate());
             b.velocity.plusEquals(displacement.negate().times(FRAGMENT_SPEED));
             b.setRotation(Quaternion.newRandom(100));
             b.setAngularVelocity(Quaternion.newRandom(MAX_ROTATION_SPEED));
 
             actors.add(a);
             actors.add(b);
         }
 
         delete();
     }
 
     public void update(){
         super.update();
         //TODO: Write code that detects if it is out of the skybox. (or some other boundry).
     }
 }
