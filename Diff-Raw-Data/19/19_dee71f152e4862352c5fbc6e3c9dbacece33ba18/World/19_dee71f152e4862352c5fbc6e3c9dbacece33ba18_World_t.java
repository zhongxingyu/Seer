 package cge.zeppelin;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.media.opengl.GLAutoDrawable;
 import javax.vecmath.Vector3f;
 
 import com.bulletphysics.collision.shapes.BoxShape;
 import com.bulletphysics.collision.shapes.CollisionShape;
 
 import de.bht.jvr.core.Printer;
 import de.bht.jvr.core.Transform;
 import de.bht.jvr.math.Matrix4;
 import de.bht.jvr.math.Vector3;
 import de.bht.jvr.util.awt.*;
 
 /**
  * Represent a world of entites that can be rendered and simulated.
  */
 public class World {
 
     final Set<Entity> entities = Collections.synchronizedSet(new HashSet<Entity>());
     final InputState input;
     final Simulator simulator;
     final Renderer renderer;
     final EnvironnementManager environnement;
     final Flyer flyer;
 
     /**
      * Create a new world.
      */
     public World(InputState i, Simulator s, Renderer r) {
         input = i;
         simulator = s;
         renderer = r;
         environnement = new EnvironnementManager(this);
         
         flyer = new Flyer(renderer.zeppelinNode, new Vector3(3, 10, 0)); 
         
         populateWorld(50, 200);
     }
 
     /**
      * Add an entity.
      */
     void add(Entity... es) {
         for (Entity e : es) {
             entities.add(e);
             if (e.node != null)
                 renderer.add(e.node);
             if (e.body != null)
                 simulator.add(e.body);
         }
     }
 
     /**
      * Remove an entity.
      */
     void remove(Entity... es) {
         for (Entity e : es) {
             entities.remove(e);
             if (e.node != null)
                 renderer.remove(e.node);
             if (e.body != null)
                 simulator.remove(e.body);
         }
     }
 
     void frame(float elapsed, GLAutoDrawable drawable) {
         for (Entity e : entities) {
             e.manipulate(elapsed);
         }
         environnement.affect(flyer, elapsed);
         input.frame(elapsed);
         simulator.simulate(elapsed);
         renderer.render(drawable);
     }
     
     /**
      * Populate the world with the neccessary entities.
      */
     void populateWorld( int bullets, int boxes) {
     	
         renderer.spot.setTransform(Transform.translate(20, 20, 20)
                 .mul(Transform.rotateY(0.8f)).mul(Transform.rotateX(-0.8f)));
 
         add(Entity.makeCube(new Vector3(100, 1, 100), 0, Matrix4.translate(0, -0.5f, 0)));
 
         // the big one
         add(Entity.makeCube(new Vector3(2, 2, 2), 0, Matrix4.translate(0, 10f, 0)));
         // the small one
        add(Entity.makeCube(new Vector3(1, 1, 1), 0.1f, Matrix4.translate(0, 7f, 0)));
         
      	renderer.camera.setTransform(Transform.translate(new Vector3(0,0,0)));
      	renderer.camera2.setTransform(Transform.translate(new Vector3(0,2,25)));
 
         add(flyer);
         
         Printer.print(renderer.root);
     }
 }
