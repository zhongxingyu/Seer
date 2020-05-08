 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Images;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import eel.seprphase2.Simulator.KeyNotFoundException;
 import eel.seprphase2.Simulator.PlantStatus;
 import eel.seprphase2.Utilities.Energy;
 
 /**
  *
  * @author Simeon
  */
 public class InformationPanels extends Image {
     private final static Texture texture = new Texture(Gdx.files.internal("assets\\game\\infopanels.png"));
     private PlantStatus status;
     private BitmapFont font11;
     private BitmapFont font16;
     private FreeTypeFontGenerator generator;
 
     
     public InformationPanels(PlantStatus status) {
         super(texture);
         this.status = status;
         generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts\\arialbd.ttf"));
         font11 = generator.generateFont(11);
         font16 = generator.generateFont(16);
     }
     
     @Override
     public void draw(SpriteBatch batch, float parentAlpha) {
         super.draw(batch, parentAlpha);
         
         font11.draw(batch, status.reactorPressure().toString(), 287, 324);
         font11.draw(batch, status.reactorTemperature().toString(), 287, 296);
         font11.draw(batch, status.reactorWear().toString(), 293, 266);
         font11.draw(batch, status.reactorWaterLevel().toString(), 287, 245);
         
         font11.draw(batch, status.condenserPressure().toString(), 500, 337);
         font11.draw(batch, status.condenserTemperature().toString(), 505, 307);
         font11.draw(batch, status.condenserWear().toString(), 505, 284);
         font11.draw(batch, status.condenserWaterLevel().toString(), 505, 264);
         
         double powerOutput = status.getOutputPower();
        font16.draw(batch, "" + powerOutput, 780, 487);
         font11.draw(batch, status.turbineWear().toString(), 850, 467);
         
         
         try {
             font11.draw(batch, status.getPumpWear(1).toString(), 405, 230);
             font11.draw(batch, status.getPumpWear(2).toString(), 780, 300);
         }
         catch(KeyNotFoundException e) {
         }
     }
 }
