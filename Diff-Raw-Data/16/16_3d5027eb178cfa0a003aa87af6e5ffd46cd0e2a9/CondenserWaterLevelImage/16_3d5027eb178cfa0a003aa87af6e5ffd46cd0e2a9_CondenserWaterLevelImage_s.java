 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Images;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import eel.seprphase2.Simulator.PlantStatus;
 
 /**
  *
  * @author Simeon
  */
 public class CondenserWaterLevelImage extends Image {
     private final static Texture bottom = new Texture(Gdx.files.internal("assets\\game\\water\\reactorwaterbottom.png"));
     private final static Texture middle = new Texture(Gdx.files.internal("assets\\game\\water\\reactorwatermiddle.png"));
     
     private PlantStatus status;
     private Image bottomImage;
     private Image middleImage;
     
     public CondenserWaterLevelImage(PlantStatus status) {
         super();
         this.status = status;
         bottomImage = new Image(bottom);
         middleImage = new Image(middle);
         
        bottomImage.setPosition(650, 127);
        middleImage.setPosition(650, 146);
         
         bottomImage.setSize(190, 27);
         middleImage.setSize(182, 160);
         
         bottomImage.setColor(1f, 1f, 1f, 0.6f);
         middleImage.setColor(1f, 1f, 1f, 0.6f);
     }
     
     @Override
     public void draw(SpriteBatch batch, float parentAlpha) {
//        bottomImage.draw(batch, parentAlpha);
//        middleImage.draw(batch, parentAlpha);
     }
 }
