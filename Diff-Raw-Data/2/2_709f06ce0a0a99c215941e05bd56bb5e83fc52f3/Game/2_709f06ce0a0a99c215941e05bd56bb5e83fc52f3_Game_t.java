 package com.folkol.paskhack;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class Game extends BasicGame {
     private Scene woods;
     private Scene currentScene;
     private Cave cave;
     private Outro gameWon;
     private Splash splash;
     private Intro intro;
 
     public Game() {
         super("Hello World");
     }
 
     @Override
     public void init(GameContainer gc) throws SlickException {
         splash = new Splash();
         intro = new Intro();
         woods = new Woods();
         cave = new Cave();
         gameWon = new Outro();
         splash.setNextScene(intro);
         intro.setNextScene(woods);
         woods.setNextScene(cave);
         cave.setNextScene(gameWon);
         currentScene = splash;
         splash.reset();
         splash.start();
     }
 
     @Override
     public void update(GameContainer gc, int delta) throws SlickException {
         if(gc.getInput().isKeyPressed(Input.KEY_C)) {
             nextScene();
         }
         if (currentScene.finished()) {
             if (!currentScene.checkWinConditions(gc)) {
                 nextScene();
             } else {
                 currentScene.reset();
             }
         }
         currentScene.update(gc, delta);
     }
 
     private void nextScene() throws SlickException {
         Scene nextScene = currentScene.getNextScene();
         nextScene.reset();
         nextScene.start();
         currentScene = nextScene;
     }
 
     @Override
     public void render(GameContainer gc, Graphics g) throws SlickException {
         currentScene.render(gc, g);
     }
 
     public static void main(String[] args) throws SlickException {
         AppGameContainer app = new AppGameContainer(new Game());
         app.setVSync(true);
         app.setDisplayMode(800, 600, false);
        app.setShowFPS(false);
         app.start();
     }
 }
