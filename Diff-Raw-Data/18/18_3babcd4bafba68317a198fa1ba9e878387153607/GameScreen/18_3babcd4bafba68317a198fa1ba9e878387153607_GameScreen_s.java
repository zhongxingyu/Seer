 package fi.hbp.angr.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 
 import fi.hbp.angr.BodyFactory;
 import fi.hbp.angr.GdxGame;
 import fi.hbp.angr.Preloadable;
 import fi.hbp.angr.hud.Hud;
 import fi.hbp.angr.hud.HudGameStateDisplay;
 import fi.hbp.angr.models.levels.Level;
 import fi.hbp.angr.stage.GameStage;
 
 /**
  * Screen used to show the actual game contents.
  */
 public class GameScreen implements Screen, Preloadable {
     /**
      * GdxGame reference.
      */
     private final GdxGame game;
     /**
      * Game stage where game is played.
      */
     private GameStage stage;
     /**
      * Current level.
      */
     private Level level;
     /**
      * HUD shown on screen.
      */
     private final Hud hud = new Hud();
 
     /**
      * Start the game.
      * @param game the GdxGame.
      */
     public GameScreen(GdxGame game) {
         this.game = game;
     }
 
     @Override
     public void preload() {
         BodyFactory.preload();
         level.preload();
     }
 
     @Override
     public void unload() {
         level.unload();
     }
 
     /**
      * Load next level.
      * @param level next level to be loaded.
      */
     public void loadLevel(Level level) {
         this.level = level;
        Screen splash = new SplashScreen(game, this, 0.2f);
         game.setScreen(splash);
     }
 
     @Override
     public void render(float delta) {
         Gdx.gl.glClearColor(0f, 0.75f, 1f, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
         stage.act(delta);
         stage.draw();
         hud.draw();
 
         if (stage.hasGameEnded()) {
             game.endOfGame(stage.getGameState());
         }
     }
 
     @Override
     public void resize(int width, int height) {
         stage.setViewport(width * 2, height * 2, true);
         hud.resize(width, height);
     }
 
     @Override
     public void show() {
         int xsize = Gdx.graphics.getWidth() * 2;
         int ysize = Gdx.graphics.getHeight() * 2;
         stage = new GameStage(xsize, ysize);
 
         /* Create input multiplexer */
         InputMultiplexer inputMultiplexer = new InputMultiplexer();
         Gdx.input.setInputProcessor(inputMultiplexer);
         inputMultiplexer.addProcessor(stage);
 
         /* Create score counter for HUD */
         HudGameStateDisplay score;
         score = new HudGameStateDisplay(stage.getGameState());
         hud.clear();
         hud.addActor(score);
 
         /* Create and add map/level actor */
         BodyFactory bf = new BodyFactory(stage, inputMultiplexer);
         level.show(bf, stage.getGameState());
         stage.addActor(level);
     }
 
     @Override
     public void hide() {
         this.unload();
     }
 
     @Override
     public void pause() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void resume() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void dispose() {
         stage.dispose();
         this.unload();
     }
 }
