 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Images;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import eel.seprphase2.Simulator.PlantStatus;
 import lamprey.seprphase3.GUI.Screens.GameplayScreen;
 
 /**
  *
  * @author Simeon
  */
 public class ErrorMessagesImage extends Image {
     private final static Texture errorTexture = new Texture(Gdx.files.internal("assets\\game\\errormsgs.png"));
     private final static int COLS = 3;
     private final static int ROWS = 2;
 
     private PlantStatus status;
     private GameplayScreen screen;
     private TextureRegion normal;
     private TextureRegion turbine;
     private TextureRegion pump2;
     private TextureRegion pump1;
     private TextureRegion condenser;
     private TextureRegion softwareError;
     private TextureRegionDrawable drawable;
     private float delta;
    private boolean buttonsRemoved;
     
     public ErrorMessagesImage(PlantStatus status, GameplayScreen screen) {
         super();
         this.status = status;
         this.screen = screen;
         drawable = new TextureRegionDrawable();
         this.setSize(160, 160);
         this.setPosition(400, -10);
         this.setColor(1f, 1f, 1f, 1f);
         this.setTouchable(Touchable.disabled);
         
         TextureRegion[][] split = TextureRegion.split(errorTexture, errorTexture.getWidth() / COLS, errorTexture.getHeight() / ROWS);
         normal        = split[0][0];
         turbine       = split[0][1];
         pump2         = split[0][2];
         pump1         = split[1][0];
         condenser     = split[1][1];
         softwareError = split[1][2];
         delta = 0f;
        buttonsRemoved = false;
     }
     
     @Override
     public void draw(SpriteBatch batch, float parentAlpha) {        
         if (status.getSoftwareFailureTimeRemaining() > 0) {
             drawable.setRegion(softwareError);
             screen.removeButtonsListeners();
            buttonsRemoved = true;
         }
        else if (buttonsRemoved && status.getSoftwareFailureTimeRemaining() == 0) {
             drawable.setRegion(normal);
             screen.addButtonsListeners();
            buttonsRemoved = false;
         }
         
         if(status.wornComponent().equals("Condenser") && delta < 3f) {
             drawable.setRegion(condenser);
             delta += Gdx.graphics.getDeltaTime();
         }
         else if(status.wornComponent().equals("Turbine") && delta < 3f) {
             drawable.setRegion(turbine);
             delta += Gdx.graphics.getDeltaTime();
        }
         else if(status.wornComponent().equals("Pump 1") && delta < 3f) {
             drawable.setRegion(pump1);
             delta += Gdx.graphics.getDeltaTime();
         }
         else if(status.wornComponent().equals("Pump 2") && delta < 3f) {
             drawable.setRegion(pump2);
             delta += Gdx.graphics.getDeltaTime();
         }
         else if(delta > 3f && status.getSoftwareFailureTimeRemaining() == 0) {
             drawable.setRegion(normal);
             delta = 0;
         }
         else if(delta > 3f) {
             delta = 0;
         }
        drawable.setRegion(normal);
         this.setDrawable(drawable);
         super.draw(batch, parentAlpha);
     }
 }
  
