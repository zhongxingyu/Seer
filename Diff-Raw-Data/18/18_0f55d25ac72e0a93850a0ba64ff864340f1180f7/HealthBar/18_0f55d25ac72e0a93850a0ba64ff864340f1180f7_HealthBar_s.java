 package org.blink.game.model.healthbar;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.control.BillboardControl;
 import com.jme3.scene.shape.Quad;
 
 /**
  *
  * @author cmessel
  */
 public class HealthBar extends Node {
 
     private boolean visible = true;
     private Geometry health;
 
     public HealthBar(AssetManager assetManager, String name) {
         super(name);
 
         BillboardControl bbControl = new BillboardControl();
         addControl(bbControl);
 
         Quad q = new Quad(1.4f, 0.2f);
         Geometry g = new Geometry("Quad", q);
 
         health = g.clone();
         health.setLocalScale(1f, 1f, 1f);
 
         //FIXME: set bar height according to player
         g.setLocalTranslation(-1.5f, 2f, 2f);
         health.setLocalTranslation(-1.30f, 2f, 2.01f);
 
         Material background = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         background.setColor("m_Color", ColorRGBA.Black);
 
         Material mat = background.clone();
         mat.setColor("m_Color", ColorRGBA.Green);
 
         g.setMaterial(background);
         health.setMaterial(mat);
 
         attachChild(g);
         attachChild(health);
 
         HealthBarControl hbc = new HealthBarControl(this);
         addControl(hbc);
     }
 
     public void setPercent(float percent) {
         if (0f < percent && percent <= 1f) {
             health.setLocalScale(percent, 1f, 1f);
         }
     }
 
     public float getPercent() {
         return health.getLocalScale().getX();
     }
 
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
     public boolean isVisible() {
         return visible;
     }
 }
