 package no.hist.gruppe5.pvu;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.utils.TimeUtils;
 
 /**
  * Created with IntelliJ IDEA. User: karl Date: 8/28/13 Time: 9:48 AM
  */
 public abstract class GameScreen implements Screen {
 
     protected PVU game;
     protected SpriteBatch batch;
     protected OrthographicCamera camera;
     private Button soundButton;
     private Stage stage;
     private long timeSinceLastAction;
     private Skin skinSoundButton;
     private ButtonStyle styleSoundButton;
     private Skin skinPauseButton;
     private ButtonStyle stylePauseButton;
     private Button pauseButton;
     private TextureAtlas atlas;
     private boolean running;
     private LabelStyle labelStyle;
     private Label pauseLabel;
 
     public GameScreen(PVU game) {
         this.game = game;
         labelStyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
 
         camera = new OrthographicCamera();
         camera.setToOrtho(false, PVU.GAME_WIDTH, PVU.GAME_HEIGHT);
         batch = new SpriteBatch();
 
         timeSinceLastAction = 0;
         running = true;
         stage = new Stage(PVU.GAME_WIDTH * 2.7f, PVU.GAME_HEIGHT * 2.7f, true);
         atlas = new TextureAtlas("data/menuButtons/menubuttons.pack");
         initSoundButton();
         initPauseButton();
         initPauseLayout();
         Gdx.input.setInputProcessor(stage);
     }
 
     protected abstract void draw(float delta);
 
     protected abstract void update(float delta);
 
     protected abstract void cleanUp();
 
     protected void clearCamera(float r, float g, float b, float a) {
         camera.update();
         camera.apply(Gdx.gl10);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         Gdx.gl.glClearColor(r, g, b, a);
         batch.setProjectionMatrix(camera.combined);
     }
 
     @Override
     public void resize(int width, int height) {
     }
 
     @Override
     public void render(float delta) {
         if (running) {
             float deltaUpdate = (delta > 0.1f) ? 0.1f : delta;
             update(deltaUpdate);

             //if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
 
             //}
        }
         draw(delta);
        if (!running) {
             clearCamera(1, 1, 1, 1);
             batch.begin();
             batch.draw(Assets.introMainLogo, PVU.GAME_WIDTH / 3, PVU.GAME_HEIGHT / 2, PVU.GAME_WIDTH / 3, PVU.GAME_HEIGHT / 3);
             batch.end();
             stage.addActor(pauseLabel);
            if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) && (TimeUtils.millis() - timeSinceLastAction > 700l)) {
                running=true;
                pauseLabel.remove();
                timeSinceLastAction = TimeUtils.millis();
            }
         }
         checkButton();
         stage.draw();
     }
 
     private void initPauseLayout() {
         pauseLabel = new Label("PAUSE", labelStyle);
         pauseLabel.setFontScale(1.9f);
         pauseLabel.setPosition(PVU.GAME_WIDTH * 1.225f, PVU.GAME_HEIGHT);
     }
 
     @Override
     public void dispose() {
         cleanUp();
         batch.dispose();
     }
 
     @Override
     public void show() {
     }
 
     @Override
     public void hide() {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
        clearCamera(1, 1, 1, 1);
        running = true;
     }
 
     /**
      * Initializes sound button.
      */
     private void initSoundButton() {
         skinSoundButton = new Skin(atlas);
         styleSoundButton = new ButtonStyle();
         styleSoundButton.up = (Settings.GLOBAL_SOUND) ? skinSoundButton.getDrawable("sound.up") : skinSoundButton.getDrawable("nosound.up");
         soundButton = new Button(styleSoundButton);
         soundButton.setPosition(PVU.GAME_WIDTH * 2.7f - 25, PVU.GAME_HEIGHT * 2.7f - 25);
         stage.addActor(soundButton);
     }
 
     /**
      * Initializes pause button.
      */
     private void initPauseButton() {
         skinPauseButton = new Skin(atlas);
         stylePauseButton = new ButtonStyle();
         stylePauseButton.up = skinPauseButton.getDrawable("pause.up");
         stylePauseButton.down = skinPauseButton.getDrawable("pause.down");
         pauseButton = new Button(stylePauseButton);
         stage.addActor(pauseButton);
         pauseButton.setPosition(PVU.GAME_WIDTH * 2.7f - 48, PVU.GAME_HEIGHT * 2.7f - 25);
     }
 
     /**
      * Checks global sound variable and updates the button style for the sound
      * button. This method will also check touch events for the pause button.
      */
     private void checkButton() {
         if (TimeUtils.millis() - timeSinceLastAction > 450l) {
             if (Gdx.input.isTouched()) {
                 int x = Gdx.input.getX();
                 int y = Gdx.input.getY();
                 if (x > 915 && x < 950 && y > 10 && y < 45) {
                     if (Settings.GLOBAL_SOUND) {
                         styleSoundButton.up = skinSoundButton.getDrawable("nosound.up");
                         Settings.setSound(false);
                     } else {
                         styleSoundButton.up = skinSoundButton.getDrawable("sound.up");
                         Settings.setSound(true);
                     }
                 } else if (x > 875 && x < 910 && y > 10 && y < 45) {
                     if (running) {
                         running = false;
                     } else {
                        //stage.addActor(pauseButton);
                         pauseLabel.remove();
                         running = true;
                     }
                 }
                 timeSinceLastAction = TimeUtils.millis();
             }
         }
     }
 
     /**
      * Method to update soundbutton in (static) main screen room.
      */
     public void updateMainScreenSoundButton() {
         styleSoundButton.up = (Settings.GLOBAL_SOUND) ? skinSoundButton.getDrawable("sound.up") : skinSoundButton.getDrawable("nosound.up");
     }
 }
