 package com.thiago.sliding;
 
 
import android.R;

 import com.thiago.framework.Game;
 import com.thiago.framework.Graphics;
 import com.thiago.framework.Screen;
 import com.thiago.framework.Graphics.ImageFormat;
 
 
 public class LoadingScreen extends Screen {
     public LoadingScreen(Game game) {
         super(game);
     }
 
 
     @Override
     public void update(float deltaTime) {
         Graphics g = game.getGraphics();
        Assets.menu = g.newImage("menu.jpg", ImageFormat.RGB565);
         Assets.puzzleImage = g.newImage("puzzleImage0101.jpg", ImageFormat.RGB565);
         //Assets.click = game.getAudio().createSound("explode.ogg");
 
 
         
         game.setScreen(new MainMenuScreen(game));
 
 
     }
 
 
     @Override
     public void paint(float deltaTime) {
 
 
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
 
 
     @Override
     public void backButton() {
 
 
     }
 }
