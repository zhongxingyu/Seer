 package spatial.hazard;
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.animation.LoopMode;
 import com.jme3.asset.AssetManager;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import spatial.PlayerInteractor;
 
 /**
  * An abstract AbstractFireball. Any class extending this one will make a PlayerInteractor
  * that looks like a fireball.
 *
  * @author jonatankilhamn
  */
 public abstract class AbstractBat extends PlayerInteractor implements AnimEventListener {
     
     //animation
     protected AnimChannel channel;
     protected AnimControl control;
     
     @Override
     protected Spatial createModel(AssetManager assetManager) {
         Node batModel = (Node) assetManager.loadModel ("Models/bat/bat02-002mirror006anim2fix.j3o");
        batModel.rotate(1.0f, 0.7f, 0);  //bat special 
         control = batModel.getChild("Sphere").getControl(AnimControl.class);
         channel = control.createChannel();
         
         control.addListener(this);
         
         channel.setAnim("ArmatureAction");
         channel.setLoopMode(LoopMode.Loop);
         channel.setSpeed(3f);
         
         return batModel;
     }
     //animation function that must be implemented even if unused
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
     }
     //animation function that must be implemented even if unused
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
     }
 }
