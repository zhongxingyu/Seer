 package me.hopps.ld27.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import me.hopps.ld27.utils.ResourceManager;
 
 public class LoadingScreen implements Screen {
 
     ResourceManager resManager;
 
     public LoadingScreen(ResourceManager resManager) {
         this.resManager = resManager;
 
         this.createLoadingList();
     }
 
     private void createLoadingList() {
         resManager.assets.load("res/sounds/boom.wav", Sound.class);
         resManager.assets.load("res/sounds/jay.wav", Sound.class);
         resManager.assets.load("res/sounds/jump.wav", Sound.class);
         resManager.assets.load("res/sounds/ouch.wav", Sound.class);
         resManager.assets.load("res/sounds/select.wav", Sound.class);
         resManager.assets.load("res/sounds/beep.wav", Sound.class);
     }
 
     @Override
     public void render(float delta) {
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
         float percentage = resManager.assets.getProgress();
 
         resManager.spriteBatch.begin();
         resManager.bigFont.draw(resManager.spriteBatch, "Loading", Gdx.graphics.getWidth() / 2 - resManager.bigFont.getBounds("Loading").width / 2, Gdx.graphics.getHeight() - 100);
         resManager.spriteBatch.end();
 
         resManager.shapeRenderer.begin(ShapeRenderer.ShapeType.Circle);
         resManager.shapeRenderer.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 100, 64);
         resManager.shapeRenderer.end();
         resManager.shapeRenderer.begin(ShapeRenderer.ShapeType.FilledCircle);
         resManager.shapeRenderer.filledCircle(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 100*percentage, 64);
         resManager.shapeRenderer.end();
 
         if(resManager.assets.update()) {
             resManager.menuScreen = new MenuScreen(resManager);
             resManager.gameScreen = new GameScreen(resManager);
             resManager.gameOverScreen = new GameOverScreen(resManager);
 
             resManager.game.setScreen(resManager.menuScreen);
         }
     }
 
     @Override
     public void resize(int x, int y) {
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
