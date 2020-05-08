 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.collision.shapes.SphereCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Sphere;
 import java.util.Random;
 
 /**
  *
  * @author Hendrik
  */
 public class Ball extends Node {
 
     RigidBodyControl ball_phy;
    
     Material Ballmat;
 
     /**
      * Maakt het bal object aan
      * @param assetManager
      */
     public Ball(AssetManager assetManager) {
         Sphere sphere = new Sphere(32, 32, 0.1f, true, false);
         Ballmat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         Ballmat.setBoolean("UseMaterialColors", true);
         Ballmat.setColor("Ambient", ColorRGBA.White);
         Ballmat.setColor("Diffuse", // surface color
                 ColorRGBA.Gray);
         Ballmat.setColor("Specular", ColorRGBA.White);
         Ballmat.setFloat("Shininess", 12);
         /**
          * Create a cannon ball geometry and attach to scene graph.
          */
         Geometry ball_geo = new Geometry("cannon ball", sphere);
         ball_geo.setMaterial(Ballmat);
         this.attachChild(ball_geo);
 
         ball_phy = new RigidBodyControl(0.1f);
         ball_phy.setCollisionGroup(2);
     }
 
     /**
      * Physics initialiseren
      */
     public void SetPhysics() {
         SphereCollisionShape sphereShape =
                 new SphereCollisionShape(0.1f);
         ball_phy.setCollisionShape(sphereShape);
         this.addControl(ball_phy);
         ball_phy.setLinearVelocity(new Vector3f(4, 0, 0));
         ball_phy.setCcdMotionThreshold(0f);
 
     }
 
     /**
      * Physics van de bal ophalen, als nog niet is initialiseren geeft null terug
      * @return Ball physicis
      */
     public RigidBodyControl GetPhysics() {
         return ball_phy;
     }
 
     /**
      *  Zorgt dat de bal niet boven de vloer uitkomt en dat hij snelheid houdt
      * @param tpf
      */
     public void Update(float tpf) {
 //Bal in de net boven de vloer houden
       ball_phy.setLinearVelocity(new Vector3f(ball_phy.getLinearVelocity().x, 0, ball_phy.getLinearVelocity().z));
         //a^2+b^2
         double combinedValue = Math.pow(ball_phy.getLinearVelocity().x, 2) + Math.pow(ball_phy.getLinearVelocity().z, 2);
         double c = Math.sqrt(combinedValue);
         if (c < 3.5) {
             float Factor = ((float) 3.5 / (float) c);
             ball_phy.setLinearVelocity(ball_phy.getLinearVelocity().mult(Factor));
 
         }
         //  System.out.println(c);
 
     }
 
     /**
      * Bal resetten naar het midden van het spelveld
      */
     public void reset() {
         ball_phy.setEnabled(false);
         this.setLocalTranslation(-4f, -0.1f, 0);
         int mult = 1;
         Random rand = new Random();
         if (rand.nextBoolean()) {
             mult = -1;
         }
 
         Vector3f vector = new Vector3f(rand.nextFloat() * mult, 0, rand.nextFloat() * mult);
 
         ball_phy.setLinearVelocity(vector);
         ball_phy.setEnabled(true);
 
     }
     
     public void changeColor(){
         Ballmat.setColor("Diffuse", ColorRGBA.randomColor());
         
     }
 }
