 package no.hist.gruppe5.pvu.visionshooter.entity;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.visionshooter.ShooterElement;
 
 /**
  *
  * @author Frode
  */
 public class ShooterYoutube extends ShooterElement {
 
     public ShooterYoutube(float elementY) {
        super(new Sprite(Assets.visionShooterYoutubeRegion), elementY,70f);
     }
 }
