 package mygame;
 
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import java.util.Random;
 
 /**
  *
  * @author adam & zach
  */
 public class LevelState extends AbstractAppState{
     private SimpleApplication app;
     private Node levelRoot = new Node("levelRoot");
 
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
       super.initialize(stateManager, app); 
       this.app = (SimpleApplication)app;          // cast to a more specific class
       
        //Just a generic test of StarControl
                   
       for(int i=0;i<100;i++)
       {
         Random ran = new Random();
         int x,y,z,s;
         x=ran.nextInt(500);
         y=ran.nextInt(500);
         z=ran.nextInt(500);
         s=ran.nextInt(10);
         Vector3f temp = new Vector3f(x,y,z);    //used for generating a position(0-100,0-100,0-100)
           
         StarControl testStar = new StarControl(s);
         Box box1 = new Box(temp,s,s,s);
         Spatial star = new Geometry("star", box1 );
         RigidBodyControl starControl = new RigidBodyControl();
         Material mat1 = new Material(app.getAssetManager(), 
                                         "Common/MatDefs/Misc/Unshaded.j3md");
         mat1.setColor("Color", ColorRGBA.Blue);
         star.setMaterial(mat1);
         
         /*testStar.setSpatial(star); 
         starControl.setSpatial(star);*/
         
         star.addControl(testStar);
         starControl.setSpatial(star);
         
         starControl.setPhysicsLocation(temp);
         starControl.setMass(0);
         starControl.setFriction(1);
         starControl.setKinematic(false);
         starControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        starControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01 | PhysicsCollisionObject.COLLISION_GROUP_02);
         app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(starControl); 
         levelRoot.attachChild(star); 
         
            
       }
    
       this.app.getRootNode().attachChild(levelRoot);
              
    }
  
    @Override
     public void cleanup() {
       super.cleanup();
     }
  
     @Override
     public void setEnabled(boolean enabled) {
       // Pause and unpause
       super.setEnabled(enabled);
     }
     @Override
     public void update(float tpf) {
     }
 
 }
