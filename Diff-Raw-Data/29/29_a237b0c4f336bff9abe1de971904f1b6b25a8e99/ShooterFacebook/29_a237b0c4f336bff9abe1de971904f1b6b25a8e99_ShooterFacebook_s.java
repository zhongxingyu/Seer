 package no.hist.gruppe5.pvu.visionshooter.entity;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.visionshooter.ShooterElement;
 
 /**
  *
  * @author Frode
  */
 public class ShooterFacebook extends ShooterElement {
 
     public ShooterFacebook(float elementY) {
        super(new Sprite(Assets.visionShooterFacebookRegion), elementY,60f);
     }
  
     
 }
