 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.material.Material;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import com.jme3.texture.Texture;
 
 /**
  *
  * @author Robert
  */
 public class Wall extends Node {
 
     private RigidBodyControl wall_phy;
     /*
      ____A____
      |      |
      D|      |B
      |      |
      |______|
      *  C
      */
 
     /**
      * Wall Aanmaken
      * @param assetManager
      */
     public Wall(AssetManager assetManager) {
 
        Texture tex = assetManager.loadTexture("Textures/Tiles.jpg");

         Box a = new Box(8f, 0.25f, 0.5f);
         Geometry aGeo = new Geometry("", a);
         Material aMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         aMat.setTexture("DiffuseMap", tex);
         aGeo.setLocalTranslation(-5f, 0, -5.5f);
         aGeo.setMaterial(aMat);
 
         Box b = new Box(5f, 0.25f, 0.5f);
         Geometry bGeo = new Geometry("", b);
         Material bMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         bMat.setTexture("DiffuseMap", tex);
         bGeo.setMaterial(bMat);
         bGeo.rotate(0, 1.57079633f, 0);
         bGeo.setLocalTranslation(2.5f, 0, 0);
 
         Box c = new Box(8f, 0.25f, 0.5f);
         Geometry cGeo = new Geometry("", c);
         Material cMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         cMat.setTexture("DiffuseMap", tex);
         cGeo.setLocalTranslation(-5f, 0, 5.5f);
         cGeo.setMaterial(cMat);
 
         Box d = new Box(5f, 0.25f, 0.5f);
         Geometry dGeo = new Geometry("", d);
         Material dMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         dMat.setTexture("DiffuseMap", tex);
         dGeo.setMaterial(dMat);
         dGeo.rotate(0, 1.57079633f, 0);
         dGeo.setLocalTranslation(-12.5f, 0, 0);
 
         this.attachChild(aGeo);
         this.attachChild(bGeo);
         this.attachChild(cGeo);
         this.attachChild(dGeo);
 
         wall_phy = new RigidBodyControl(0f);
         wall_phy.setCollisionGroup(1);
         this.addControl(wall_phy);
 
     }
 
     /**
      * Physics body terug geven
      * @return
      */
     public RigidBodyControl GetPhysics() {
 
         return wall_phy;
     }
 }
