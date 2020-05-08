 package no.hist.gruppe5.pvu.visionshooter.entity;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.visionshooter.ShooterElement;
 
 /**
  *
  * @author Frode
  */
 public class ShooterDokument extends ShooterElement {
     
     public ShooterDokument(float elementY) {
        super(new Sprite(Assets.visionShooterDocumentRegion),elementY, 60f);
     }
 
 }
