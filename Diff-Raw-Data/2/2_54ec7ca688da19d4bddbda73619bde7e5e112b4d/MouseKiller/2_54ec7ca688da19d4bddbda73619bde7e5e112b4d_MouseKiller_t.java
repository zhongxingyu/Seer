 package com.github.unluckyninja.mousekiller;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.github.unluckyninja.mousekiller.model.Killer;
 import com.github.unluckyninja.mousekiller.model.listener.SimpleInputListener;
 
 public class MouseKiller extends Game {
 
     public static void main(String[] args) {
         LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "MouseKiller";
         cfg.width = width;
         cfg.height = height;
         cfg.useGL20 = true;
         new LwjglApplication(new MouseKiller(), cfg);
     }
     SpriteBatch batch;
     Killer killer;
     private static int width = 800;
     private static int height = 600;
     @Override
     public void create() {
         Gdx.input.setCursorCatched(true);
         batch = new SpriteBatch();
         killer = new Killer(Gdx.input.getX(),height-Gdx.input.getY());
         this.setScreen(new MainMenu(this, batch));
         Gdx.input.setInputProcessor(new SimpleInputListener(killer));
     }
 
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);
         MouseKiller.width = width;
         MouseKiller.height = height;
     }
     
     @Override
     public void render() {
         super.render();
     }
 
     @Override
     public void dispose() {
         getScreen().dispose();
         killer.getTexture().getTexture().dispose();
         batch.dispose();
     }
 
     public static int getWidth() {
         return width;
     }
 
     public static int getHeight() {
         return height;
     }
     
 }
