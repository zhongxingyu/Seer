 package org.blink.game.model.player.healthbar;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.font.Rectangle;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
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
     private Geometry healthBar;
 
     public HealthBar(AssetManager assetManager, String name) {
         super(name);
 
        Geometry bgBar = new Geometry("Quad", new Quad(0.9f, 0.3f));
         healthBar = new Geometry("Quad", new Quad(.86f, 0.04f));
 
         //FIXME: set bar height according to player
        bgBar.setLocalTranslation(-0.45f, 0.42f, 0.5f);
         healthBar.setLocalTranslation(-0.43f, 0.44f, 0.51f);
 
         Material background = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 
         Material mat = background.clone();
         mat.setColor("m_Color", ColorRGBA.Green);
 
         background.setTexture("ColorMap", assetManager.loadTexture("Interface/ui/healthbar.png"));
        
         
         BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
         BitmapText txt = new BitmapText(fnt, false);
         txt.setQueueBucket(Bucket.Opaque);
         txt.setSize( 0.2f );
         txt.setText(name);
         txt.setColor(ColorRGBA.White);
        txt.setLocalTranslation(-0.43f, 0.73f, 0.52f);
         
 
 
         bgBar.setMaterial(background);
         healthBar.setMaterial(mat);
 
         attachChild(bgBar);
         attachChild(healthBar);
         attachChild(txt);
 
         BillboardControl bbControl = new BillboardControl();
         addControl(bbControl);
 
         HealthBarControl hbc = new HealthBarControl(this);
         addControl(hbc);
     }
 
     public void setPercent(float percent) {
         if (percent <= 0f) {
             healthBar.setLocalScale(0, 1f, 1f);
         }
         if (0f < percent && percent <= 1f) {
             healthBar.setLocalScale(percent, 1f, 1f);
         }
     }
 
     public float getPercent() {
         return healthBar.getLocalScale().getX();
     }
 
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
     public boolean isVisible() {
         return visible;
     }
 }
