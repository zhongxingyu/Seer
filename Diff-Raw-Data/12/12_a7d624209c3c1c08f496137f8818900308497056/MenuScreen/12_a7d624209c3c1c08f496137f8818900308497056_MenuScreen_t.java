 package lamprey.seprphase3.GUI.Screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.TextInputListener;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.EventListener;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 import eel.seprphase2.Simulator.GameManager;
 import eel.seprphase2.Simulator.PlantController;
 import eel.seprphase2.Simulator.PlantStatus;
 import lamprey.seprphase3.GUI.BackyardReactor;
 import lamprey.seprphase3.GUI.Images.HoverButton;
 
 /**
  *
  * @author Simeon
  */
 public class MenuScreen extends AbstractScreen {
     Texture menubgTexture;
     Texture gamelogoTexture;
     Texture newgameTexture;
     Texture loadgameTexture;
     Texture optionsTexture;
     Texture highscoresTexture;
     Texture lampreylogoTexture;
     Image menubgImage;
     Image gamelogoImage;
     HoverButton newgameImage;
     HoverButton loadgameImage;
     HoverButton optionsImage;
     HoverButton highscoresImage;
     Image lampreylogoImage;
     
    boolean isInputShown;
    
     OperatorNameInput nameListener;
     
     public MenuScreen(BackyardReactor game, PlantController controller, PlantStatus status, GameManager manager) {
         super(game, controller, status, manager);
         menubgTexture      = new Texture(Gdx.files.internal("assets\\menu\\menubg.png"));
         gamelogoTexture    = new Texture(Gdx.files.internal("assets\\menu\\gamelogo.png"));
         newgameTexture     = new Texture(Gdx.files.internal("assets\\menu\\newgame.png"));
         loadgameTexture    = new Texture(Gdx.files.internal("assets\\menu\\loadgame.png"));
         optionsTexture     = new Texture(Gdx.files.internal("assets\\menu\\options.png"));
         highscoresTexture  = new Texture(Gdx.files.internal("assets\\menu\\highscores.png"));
         lampreylogoTexture = new Texture(Gdx.files.internal("assets\\menu\\lampreylogo.png"));
         
         nameListener = new OperatorNameInput();
        isInputShown = false;
     }
      
     @Override
     public void show() {
         super.show();
         
         stage.clear();
         
         menubgImage      = new Image(menubgTexture);
         gamelogoImage    = new Image(gamelogoTexture);
         newgameImage     = new HoverButton(newgameTexture, true);
         loadgameImage    = new HoverButton(loadgameTexture, true);
         optionsImage     = new HoverButton(optionsTexture, true);
         highscoresImage  = new HoverButton(highscoresTexture, true);
         lampreylogoImage = new Image(lampreylogoTexture);
         
         menubgImage.setPosition(0, 0);
         gamelogoImage.setPosition(272, 333);
         newgameImage.setPosition(361, 283);
         loadgameImage.setPosition(349, 241);
         optionsImage.setPosition(392, 199);
         highscoresImage.setPosition(349, 155);
         lampreylogoImage.setPosition(436, 0);
         
         newgameImage.addListener(getNewgameListener());
         
         stage.addActor(menubgImage);
         stage.addActor(gamelogoImage);
         stage.addActor(newgameImage);
         stage.addActor(loadgameImage);
         stage.addActor(optionsImage);
         stage.addActor(highscoresImage);
         stage.addActor(lampreylogoImage);
     }
 
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);        
     }
 
     @Override
     public void render(float delta) {
         super.render(delta);
 //        drawHovers();
     }
 
     @Override
     public void dispose() {
     }
     
     @Override
     public void hide() {
         stage.clear();
     }
     
     private ClickListener getNewgameListener() {
         return new ClickListener() {
             @Override
             public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (!isInputShown) {
                    Gdx.input.getTextInput(nameListener, "Please enter your name", "Player");
                    isInputShown = true;
                }
             }
         };
     }
     
     private class OperatorNameInput implements TextInputListener {
         @Override
         public void input (String text) {
             manager.setUsername(text);
             game.setScreen(game.getGameplayScreen());
         }
 
         @Override
         public void canceled () {
             game.setScreen(game.getMenuScreen());
            isInputShown = false;
         }
     }
 }
