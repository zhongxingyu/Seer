 package com.roltekk.game.dragonchess_desktop;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 
 public class DragonChessDesktop {
   public static void main(String[] args) {
    new LwjglApplication(new Game(), "Game", 480, 320, false);
   }
 }
