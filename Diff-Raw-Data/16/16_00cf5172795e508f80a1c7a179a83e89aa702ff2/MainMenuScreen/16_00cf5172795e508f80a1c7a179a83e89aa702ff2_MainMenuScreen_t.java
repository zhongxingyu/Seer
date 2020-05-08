 package com.dfgames.lastplanet;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 import java.io.File;
 
 /**
  * Author: Igor Bubelov
  * Date: 4/11/12 2:42 PM
  */
 public class MainMenuScreen implements Screen {
     private Game game;
     private OrthographicCamera camera;
     private SpriteBatch spriteBatch;
 
     private Texture backgroundTexture;
     private Texture newGameTexture;
     private Texture trainingTexture;
     private Texture highscoresTexture;
     private Texture rateGameTexture;
     private Texture soundOnTexture;
     private Texture soundOffTexture;
     private Texture logoTexture;
 
     private Vector2 backgroundPosition;
     private Vector2 newGamePosition;
     private Vector2 trainingPosition;
     private Vector2 highscoresPosition;
     private Vector2 rateGamePosition;
     private Vector2 soundPosition;
     private Vector2 logoPosition;
 
     private Rectangle newGameRectangle;
     private Rectangle trainingRectangle;
     private Rectangle highScoresRectangle;
     private Rectangle rateGameRectangle;
     private Rectangle soundRectangle;
 
     private boolean soundOn = true;
 
     public MainMenuScreen(Game game) {
         this.game = game;
         camera = new OrthographicCamera(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
         camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
         spriteBatch = new SpriteBatch();
         backgroundTexture = new Texture(Gdx.files.internal("background.png"));
         backgroundPosition = new Vector2(0, 0);
 
         newGameTexture = new Texture(Gdx.files.internal("menu" + File.separator + "New Game.png"));
         trainingTexture = new Texture(Gdx.files.internal("menu" + File.separator + "Training.png"));
         highscoresTexture = new Texture(Gdx.files.internal("menu" + File.separator + "HighScores.png"));
         rateGameTexture = new Texture(Gdx.files.internal("menu" + File.separator + "Rate Game.png"));
         soundOnTexture = new Texture(Gdx.files.internal("menu" + File.separator + "Sound on.png"));
         soundOffTexture = new Texture(Gdx.files.internal("menu" + File.separator + "Sound off.png"));
         logoTexture = new Texture(Gdx.files.internal("logo.png"));
 
         newGamePosition = new Vector2(Constants.SCREEN_WIDTH/2 - 90, Constants.SCREEN_HEIGHT/2);
         trainingPosition = new Vector2(Constants.SCREEN_WIDTH/2 - 90, Constants.SCREEN_HEIGHT/2 - 60);
         highscoresPosition = new Vector2(Constants.SCREEN_WIDTH/2 - 90, Constants.SCREEN_HEIGHT/2 - 120);
         rateGamePosition = new Vector2(Constants.SCREEN_WIDTH/2 - 90, Constants.SCREEN_HEIGHT/2 - 180);
         soundPosition = new Vector2(30, 25);
         logoPosition = new Vector2(Constants.SCREEN_WIDTH/2 - 70, Constants.SCREEN_HEIGHT/2 + 70);
 
         newGameRectangle = new Rectangle(newGamePosition.x, newGamePosition.y - 45, 180, 45);
         trainingRectangle = new Rectangle(trainingPosition.x, trainingPosition.y + 75, 180, 45);
         highScoresRectangle = new Rectangle(highscoresPosition.x, highscoresPosition.y + 190, 180, 45);
         rateGameRectangle = new Rectangle(rateGamePosition.x, rateGamePosition.y, 180, 45);
         soundRectangle = new Rectangle(soundPosition.x, soundPosition.y + 400, 30, 30);
     }
 
     @Override
     public void render(float delta) {
         Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         camera.update();
 
         spriteBatch.setProjectionMatrix(camera.combined);
         spriteBatch.begin();
         spriteBatch.draw(backgroundTexture, backgroundPosition.x, backgroundPosition.y, 800, 480);
         spriteBatch.draw(newGameTexture, newGamePosition.x, newGamePosition.y, 180, 45);
         spriteBatch.draw(trainingTexture, trainingPosition.x, trainingPosition.y, 180, 45);
         spriteBatch.draw(highscoresTexture, highscoresPosition.x, highscoresPosition.y, 180, 45);
         spriteBatch.draw(rateGameTexture, rateGamePosition.x, rateGamePosition.y, 180, 45);
         if (soundOn) {
             spriteBatch.draw(soundOnTexture, soundPosition.x, soundPosition.y, 30, 30);
         } else {
             spriteBatch.draw(soundOffTexture, soundPosition.x, soundPosition.y, 30, 30);
         }
         spriteBatch.draw(logoTexture, logoPosition.x, logoPosition.y, 140, 140);
         spriteBatch.end();
 
         if(Gdx.input.isTouched()) {
             Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
 
             if(newGameRectangle.contains(touchPosition.x, touchPosition.y)) {
                 game.setScreen(new DifficultyScreen(game));
             }
 
             if(trainingRectangle.contains(touchPosition.x, touchPosition.y)) {
                 game.setScreen(new TrainingScreen(game));
             }
 
             if(highScoresRectangle.contains(touchPosition.x, touchPosition.y)) {
                 game.setScreen(new HighscoresScreen(game));
             }
 


            if (Gdx.input.justTouched()) {
                Vector3 justTouchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f);

                if(soundRectangle.contains(justTouchPosition.x, justTouchPosition.y)) {
                    if(soundOn) {
                        soundOn = false;
                    } else {
                        soundOn = true;
                    }
                 }
             }
         }
     }
 
     @Override
     public void resize(int width, int height) {
 
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
 
     }
 
     @Override
     public void dispose() {
 
     }
 }
