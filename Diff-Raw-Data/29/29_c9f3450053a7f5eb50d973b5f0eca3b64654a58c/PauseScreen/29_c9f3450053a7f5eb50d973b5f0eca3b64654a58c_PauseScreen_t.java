 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import eel.seprphase2.Simulator.GameManager;
 import eel.seprphase2.Simulator.PlantController;
 import eel.seprphase2.Simulator.PlantStatus;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import lamprey.seprphase3.GUI.BackyardReactor;
 import lamprey.seprphase3.GUI.Images.HoverButton;
 import lamprey.seprphase3.GUI.Images.HoverButtonType;
 
 /**
  *
  * @author Simeon
  */
 public class PauseScreen extends AbstractScreen {
     private Texture pauseBlurTexture;
     private Texture pausePausedTexture;
     private Texture pauseReturnTexture;
     private Texture pauseSaveTexture;
     private Texture pauseLoadTexture;
     private Texture pauseMenuTexture;
     
     private Image pauseBlurImage;
     private Image pausePausedImage;
     private HoverButton pauseReturnImage;
     private HoverButton pauseSaveImage;
     private HoverButton pauseLoadImage;
     private HoverButton pauseMenuImage;
 
     
     public PauseScreen(BackyardReactor game, PlantController controller, PlantStatus status, GameManager manager) {
         super(game, controller, status, manager);
         pauseBlurTexture   = new Texture(Gdx.files.internal("assets\\pause\\pauseblur.png"));
         pausePausedTexture = new Texture(Gdx.files.internal("assets\\pause\\pause_PAUSED.png"));
         pauseReturnTexture = new Texture(Gdx.files.internal("assets\\pause\\pause_RETURN.png"));
         pauseSaveTexture   = new Texture(Gdx.files.internal("assets\\pause\\pause_SAVE.png"));
         pauseLoadTexture   = new Texture(Gdx.files.internal("assets\\pause\\pause_LOAD.png"));
         pauseMenuTexture   = new Texture(Gdx.files.internal("assets\\pause\\pause_MENU.png"));
     }
     
     @Override
     public void show() {
         super.show();
         pauseBlurImage   = new Image(pauseBlurTexture);
         pausePausedImage = new Image(pausePausedTexture);
         pauseReturnImage = new HoverButton(pauseReturnTexture, HoverButtonType.Transparent);
         pauseSaveImage   = new HoverButton(pauseSaveTexture, HoverButtonType.Transparent);
         pauseLoadImage   = new HoverButton(pauseLoadTexture, HoverButtonType.Transparent);
         pauseMenuImage   = new HoverButton(pauseMenuTexture, HoverButtonType.Transparent);
         
         pausePausedImage.setPosition(367, 347);
         pauseReturnImage.setPosition(399, 276);
         pauseSaveImage.setPosition(368, 232);
         pauseLoadImage.setPosition(361, 188);
         pauseMenuImage.setPosition(367, 145);
         
         pauseReturnImage.addListener(getReturnListener());
         pauseSaveImage.addListener(getSaveListener());
         pauseLoadImage.addListener(getLoadListener());
         pauseMenuImage.addListener(getMenuListener());
 
         stage.addActor(pauseBlurImage);
         stage.addActor(pausePausedImage);
         stage.addActor(pauseReturnImage);
         stage.addActor(pauseSaveImage);
         stage.addActor(pauseLoadImage);
         stage.addActor(pauseMenuImage);
     }
     
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);
     }
     
     @Override
     public void hide() {
         stage.clear();
     }
     
     public ClickListener getReturnListener() {
         return new ClickListener() {
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                 game.setScreen(game.getGameplayScreen());
                 return true;
             }
         };
     }
     
     public ClickListener getSaveListener() {
         return new ClickListener() {
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                 // refactor out of here?
                 try {
                     manager.saveGame();
                     game.setScreen(game.getGameplayScreen());
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 } 
                 return true;
             }
         };
     }
     
     public ClickListener getLoadListener() {
         return new ClickListener() {
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                 try {
                     manager.loadGame();
                     game.setScreen(game.getGameplayScreen());
                 } catch (ClassNotFoundException e) {
                     e.printStackTrace();
 
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return true;
             }
         };
     }
     
     public ClickListener getMenuListener() {
         return new ClickListener() {
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                manager.initGame();
                 game.setScreen(game.getMenuScreen());
                 return true;
             }
         };
     }
 }
