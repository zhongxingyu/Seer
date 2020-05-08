 package actor;
 
import game.types.AsteroidField;
 import graphics.Model;
 import graphics.particles.FireParticle;
 import graphics.particles.ParticleSystem;
 import math.Quaternion;
 import math.Vector3f;
 
 public class Asteroid extends Actor {
     private static final long serialVersionUID = 916554544709785597L;
     private static final String MODEL_NAME = Model.Models.ASTEROID;
     protected int hitPoints;
     protected game.types.AsteroidField field;
 
     public Asteroid(){
         super();
         angularVelocity = new Quaternion(Vector3f.randomPosition(1), 1);
         scale = new Vector3f(2,2,2).plus(Vector3f.randomPosition(1));
         hitPoints = (int) scale.magnitude2(); 
         modelName = MODEL_NAME;
     }
 
     public Asteroid(Vector3f position, Vector3f velocity){
         this();
         setPosition(position);
         setVelocity(velocity);
     }
 
     @Override
     public void handleCollision(Actor other) {
         System.err.println("Collision Detected Between " + other + " and " + this);
         if(other instanceof Projectile){
             hitPoints -= ((Projectile) other).getDamage();
             if(hitPoints < 0)
                 die();
         }
         bounce(other);
     }
 
     @Override
     public void die(){
         if(field != null)
             field.asteroidDied();
         if(ParticleSystem.isEnabled())
             for(int i = 0; i < 16; i++){
                 ParticleSystem.addParticle( new FireParticle(this,Vector3f.randomPosition(1)));
             }
         delete();
     }
 
     public void setAsteroidField(game.types.AsteroidField asteroidField) {
         // Don't let it change if it points somewhere
         if(field == null){
             field = asteroidField;
         }
     }
 
     public void update(){
         super.update();
         //TODO: Write code that detects if it is out of the skybox. (or some other boundry).
     }
 }
