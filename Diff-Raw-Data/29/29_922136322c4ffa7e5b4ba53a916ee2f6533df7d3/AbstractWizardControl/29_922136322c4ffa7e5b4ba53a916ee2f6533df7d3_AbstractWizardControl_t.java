 package control;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.collision.shapes.SphereCollisionShape;
 import com.jme3.light.SpotLight;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import leveldata.LevelContentGenerator;
 import spatial.Player;
 import spatial.hazard.LinearFireball;
 import state.LevelGeneratingState;
 
 /**
  * A class for a wizard who shoots fireballs when the player is within a certain
  * range.
  *
  * @author jonatankilhamn
  */
 public abstract class AbstractWizardControl extends AbstractPlayerInteractorControl implements LevelContentGenerator {
 
     protected boolean readyToShoot = true;
     protected LevelGeneratingState levelControl;
     protected float reloadTimer;
     protected float fireballSpeed = 15f;
     protected AssetManager assetManager;
     protected float speed = 3f;
     protected SpotLight spotlight;
 
     /**
      * Creates a new wizard with an aggro radius of 50. The assetManager is
      * needed to create fireballs.
      *
      * @param assetManager
      */
     public AbstractWizardControl(AssetManager assetManager, SpotLight spotlight) {
         this(new SphereCollisionShape(50f), assetManager, spotlight);
     }
     
     public AbstractWizardControl(CollisionShape s, AssetManager assetManager, SpotLight spotlight) {
         super(s);
         this.assetManager = assetManager;
         this.spotlight = spotlight;
         this.spotlight.setColor(ColorRGBA.Green);
         this.spotlight.setSpotOuterAngle(FastMath.DEG_TO_RAD * 90);
         this.spotlight.setSpotInnerAngle(FastMath.DEG_TO_RAD * 20);
         this.spotlight.setSpotRange(70f);
 
     }
 
     @Override
     public void update(float tpf) {
         super.update(tpf);
         if (!readyToShoot) {
             reloadTimer += tpf;
             if (reloadTimer > 0) {
                 readyToShoot = true;
             }
         }
     }
     
     protected Vector3f wizardLightOffset = new Vector3f(0f,15f,0f);
 
     @Override
     /**
      * Wizards do not move.
      */
     protected void positionUpdate(float tpf) {
         spatial.setLocalTranslation(spatial.getLocalTranslation().add(tpf*speed,0f,0f));
         this.spotlight.getPosition().set(this.spatial.getLocalTranslation()).addLocal(wizardLightOffset);
     }
 
     @Override
     public void collideWithPlayer(Player player) {
         if (readyToShoot) {
             shootAtPlayerAndReload(player);
         }
         lookAt(player.getLocalTranslation());
     }
 
     /**
      * Try to shoot at the player. Implementations of this method must take care
      * of setting readyToShoot = false and decreasing reloadTimer by an
      * appropriate amount.
      *
      * @param player Handle to the player, for aiming.
      */
     protected abstract void shootAtPlayerAndReload(Player player);
 
     /**
      * Shoots a fireball from the wizard towards the given position.
      */
     protected void shootFireballAt(Vector3f targetPosition) {
         Vector3f direction = targetPosition.
                 subtract((this.spatial).getWorldTranslation());
         //direction.setZ(0);
         direction.normalizeLocal();
         LinearFireball fireball = new LinearFireball(assetManager, direction.mult(fireballSpeed));
         this.levelControl.addToLevel(fireball,
                 this.spatial.getWorldTranslation().add(direction));
     }
 
     public void lookAt(Vector3f position) {
         Vector3f direction = position.subtract(spatial.getLocalTranslation());
        float theta = FastMath.atan2(direction.x,direction.z + 3f)/2 + FastMath.PI + 0.3f;
        float[] angles = {0f, theta, 0.0f};
         Quaternion rotation = new Quaternion(angles);
         this.spatial.setLocalRotation(rotation);
         this.spotlight.setDirection(direction.subtractLocal(wizardLightOffset));
     }
 
     public void setLevelControl(LevelGeneratingState levelControl) {
         this.levelControl = levelControl;
     }
     
     public void collideWithStatic() {
     }
     
 }
