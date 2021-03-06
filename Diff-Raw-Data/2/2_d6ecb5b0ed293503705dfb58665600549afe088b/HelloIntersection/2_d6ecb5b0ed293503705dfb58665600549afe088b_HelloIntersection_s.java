 package jmetest.TutorialGuide;
 
 import java.net.URL;
 import java.util.Random;
 
 import com.jme.app.SimpleGame;
 import com.jme.bounding.BoundingSphere;
 import com.jme.image.Texture;
 import com.jme.input.KeyInput;
 import com.jme.input.action.AbstractInputAction;
 import com.jme.intersection.Intersection;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Controller;
 import com.jme.scene.Skybox;
 import com.jme.scene.Text;
 import com.jme.scene.TriMesh;
 import com.jme.scene.shape.Sphere;
 import com.jme.scene.state.MaterialState;
 import com.jme.scene.state.TextureState;
 import com.jme.sound.SoundAPIController;
 import com.jme.util.TextureManager;
 import com.jme.sound.SoundPool;
 import com.jme.sound.scene.ProgrammableSound;
 import com.jme.sound.scene.SoundNode;
 
 /**
  * Started Date: Jul 24, 2004 <br>
  * <br>
  *
  * Demonstrates intersection testing, sound, and making your own controller.
  *
  * @author Jack Lindamood
  */
 public class HelloIntersection extends SimpleGame {
     /** Material for my bullet */
     MaterialState bulletMaterial;
 
     /** Target you're trying to hit */
     Sphere target;
 
     /** Location of laser sound */
     URL laserURL;
 
     /** Location of hit sound */
     URL hitURL;
 
     /** Used to move target location on a hit */
     Random r = new Random();
 
     /** A sky box for our scene. */
     Skybox sb;
 
     /**
      * The programmable sound that will be in charge of maintaining our sound
      * effects.
      */
     ProgrammableSound laserSound;
 
     ProgrammableSound targetSound;
 
     /** The node where attached sounds will be propagated from */
     SoundNode snode;
 
     /**
      * The ID of our laser shooting sound effect. The value is not important. It
      * should just be unique in our game to this sound.
      */
     private int laserEventID = 1;
 
     private int hitEventID = 2;
 
     public static void main(String[] args) {
         HelloIntersection app = new HelloIntersection();
         app.setDialogBehaviour(SimpleGame.ALWAYS_SHOW_PROPS_DIALOG);
         app.start();
     }
 
     protected void simpleInitGame() {
         setupSound();
         BoundingSphere.useExactBounds=true;
 
         /** Create a + for the middle of the screen */
         Text cross = new Text("Crosshairs", "+");
 
         // 8 is half the width of a font char
         /** Move the + to the middle */
         cross.setLocalTranslation(new Vector3f(display.getWidth() / 2f - 8f,
                 display.getHeight() / 2f - 8f, 0));
         fpsNode.attachChild(cross);
         target = new Sphere("my sphere", 15, 15, 1);
         target.setModelBound(new BoundingSphere());
         target.updateModelBound();
         rootNode.attachChild(target);
 
         /** Create a skybox to suround our world */
         sb = new Skybox("skybox", 200, 200, 200);
         URL monkeyLoc = HelloIntersection.class.getClassLoader().getResource(
                 "jmetest/data/texture/clouds.png");
         TextureState ts = display.getRenderer().createTextureState();
         ts.setTexture(TextureManager.loadTexture(monkeyLoc, Texture.MM_LINEAR,
                 Texture.FM_LINEAR, true));
         sb.setRenderState(ts);
 
         // Attach the skybox to our root node, and force the rootnode to show
         // so that the skybox will always show
         rootNode.attachChild(sb);
         rootNode.setForceView(true);
 
         /**
          * Set the action called "firebullet", bound to KEY_F, to performAction
          * FireBullet
          */
         input.addKeyboardAction("firebullet", KeyInput.KEY_F, new FireBullet());
 
         /** Make bullet material */
         bulletMaterial = display.getRenderer().createMaterialState();
         bulletMaterial.setEmissive(ColorRGBA.green);
 
         /** Make target material */
         MaterialState redMaterial = display.getRenderer().createMaterialState();
         redMaterial.setDiffuse(ColorRGBA.red);
         target.setRenderState(redMaterial);
     }
 
     private void setupSound() {
         /** init sound API acording to the rendering enviroment you're using */
         SoundAPIController.getSoundSystem(properties.getRenderer());
         /** Set the 'ears' for the sound API */
         SoundAPIController.getRenderer().setCamera(cam);
         snode = new SoundNode();
         /** Create program sound */
         targetSound = new ProgrammableSound();
         /** Make the sound softer */
         targetSound .setLooping(false);
         targetSound.setMaxDistance(140f);
 
         laserSound = new ProgrammableSound();
         laserSound.setLooping(false);
         
         
         /** locate laser and register it with the prog sound. */
 
         laserURL = HelloIntersection.class.getClassLoader().getResource(
                 "jmetest/data/sound/laser.ogg");
         hitURL = HelloIntersection.class.getClassLoader().getResource(
                 "jmetest/data/sound/explosion.ogg");
         // Ask the system for a program id for this resource
         int programid = SoundPool.compile(new URL[] { laserURL });
         int hitid = SoundPool.compile(new URL[] { hitURL });
         // Then we bind the programid we received to our laser event id.
         laserSound.bindEvent(laserEventID, programid);
         targetSound.bindEvent(hitEventID, hitid);
         //        programSound.setNextProgram(programid);
         snode.attachChild(laserSound);
         snode.attachChild(targetSound);
     }
 
     class FireBullet extends AbstractInputAction {
         int numBullets;
 
         FireBullet() {
             setAllowsRepeats(false);
         }
 
         public void performAction(float time) {
             System.out.println("BANG");
             /** Create bullet */
             Sphere bullet = new Sphere("bullet" + numBullets++, 8, 8, .25f);
             bullet.setModelBound(new BoundingSphere());
             bullet.updateModelBound();
             /** Move bullet to the camera location */
             bullet.setLocalTranslation(new Vector3f(cam.getLocation()));
             bullet.setRenderState(bulletMaterial);
             /**
              * Update the new world locaion for the bullet before I add a
              * controller
              */
             bullet.updateGeometricState(0, true);
             /**
              * Add a movement controller to the bullet going in the camera's
              * direction
              */
             bullet.addController(new BulletMover(bullet, new Vector3f(cam
                     .getDirection())));
             rootNode.attachChild(bullet);
             bullet.updateRenderState();
             /** Signal our sound to play laser during rendering */
             laserSound.setPosition(cam.getLocation());
             snode.onEvent(laserEventID);
         }
     }
 
     class BulletMover extends Controller {
         /** Bullet that's moving */
         TriMesh bullet;
 
         /** Direciton of bullet */
         Vector3f direction;
 
         /** speed of bullet */
         float speed = 10;
 
         /** Seconds it will last before going away */
         float lifeTime = 5;
 
         BulletMover(TriMesh bullet, Vector3f direction) {
             this.bullet = bullet;
             this.direction = direction;
             this.direction.normalizeLocal();
         }
 
         public void update(float time) {
             lifeTime -= time;
             /** If life is gone, remove it */
             if (lifeTime < 0) {
                 rootNode.detachChild(bullet);
                 bullet.removeController(this);
                 return;
             }
             /** Move bullet */
             Vector3f bulletPos = bullet.getLocalTranslation();
             bulletPos.addLocal(direction.mult(time * speed));
             bullet.setLocalTranslation(bulletPos);
             /** Does the bullet intersect with target? */
             if (Intersection.intersection(bullet.getWorldBound(), target
                     .getWorldBound())) {
                 System.out.println("OWCH!!!");
                targetSound.setPosition(target.getLocalTranslation());
                 target.setLocalTranslation(new Vector3f(r.nextFloat() * 10, r
                         .nextFloat() * 10, r.nextFloat() * 10));
                 lifeTime = 0;
                 
                 snode.onEvent(hitEventID);
             }
         }
     }
 
     /**
      * Called every frame for rendering
      */
     protected void simpleRender() {
         // Give control to the sound in case sound changes are needed.
         SoundAPIController.getRenderer().draw(snode);
     }
 
     /**
      * Called every frame for updating
      */
     protected void simpleUpdate() {
         // Let the programmable sound update itself.
         snode.updateGeometricState(tpf, true);
     }
 }
