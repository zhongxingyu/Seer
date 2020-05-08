 package me.hopps.ld27.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
 import me.hopps.ld27.utils.ResourceManager;
 import me.hopps.ld27.utils.ui.ColorButton;
 
 public class MenuScreen implements Screen {
 
     ResourceManager resManager;
     ColorButton start, exit;
 
     public MenuScreen(ResourceManager resManager) {
         this.resManager = resManager;
 
         start = new ColorButton("Start Game", Gdx.graphics.getWidth()/2 - 75, 350, resManager.mediumFont, Color.WHITE, Color.GRAY);
         exit = new ColorButton("Exit", Gdx.graphics.getWidth()/2 - 25, 275, resManager.mediumFont, Color.WHITE, Color.GRAY);
     }
 
     @Override
     public void render(float delta) {
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
         if(Gdx.input.isButtonPressed(0)) {
             if(start.hovered) {
                 resManager.assets.get("res/sounds/select.wav", Sound.class).play();
                 resManager.game.setScreen(resManager.gameScreen);
                 return;
             }
             if(exit.hovered) {
                 resManager.assets.get("res/sounds/select.wav", Sound.class).play();
                 Gdx.app.exit();
                 return;
             }
         }
 
         resManager.spriteBatch.begin();
        resManager.spriteBatch.draw(resManager.assets.get("res/img/intro.png", Texture.class), 0, 0);
         resManager.bigFont.draw(resManager.spriteBatch, "Game Name", Gdx.graphics.getWidth() / 2 - resManager.bigFont.getBounds("Game Name").width / 2, Gdx.graphics.getHeight() - 100);
         start.render(resManager.spriteBatch);
         exit.render(resManager.spriteBatch);
         resManager.spriteBatch.end();
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
