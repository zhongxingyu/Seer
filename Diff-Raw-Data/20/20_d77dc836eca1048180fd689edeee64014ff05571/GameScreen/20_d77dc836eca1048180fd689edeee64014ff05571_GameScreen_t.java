 package com.stuckinadrawer.dungeongame.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.input.GestureDetector;
 import com.stuckinadrawer.dungeongame.*;
 import com.stuckinadrawer.dungeongame.actors.Player;
 import com.stuckinadrawer.dungeongame.actors.enemies.Enemy;
 import com.stuckinadrawer.dungeongame.levelGeneration.LevelCreator;
 import com.stuckinadrawer.dungeongame.tiles.Tile;
 
 import java.util.ArrayList;
 
 public class GameScreen extends AbstractScreen {
 
     private OrthographicCamera camera;
     private Renderer renderer;
 
     private Level level;
     private Player player;
 
     float timer = 0;
 
     public GameScreen(DungeonGame dungeonGame) {
         super(dungeonGame);
         LevelCreator levelCreator = new LevelCreator();
         level = levelCreator.getNewLevel();
         player = level.getPlayer();
         camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 
         renderer = new Renderer(level, camera);
         Gdx.input.setInputProcessor(new GestureDetector(new GestureDetection(camera, level)));
         camera.position.set(player.getPosition().getX()*Constants.TILE_SIZE, player.getPosition().getY()*Constants.TILE_SIZE, 0);
 
 
     }
 
     private void update(float delta){
         timer+=delta;
         if(timer>0.1){
             //moves player if his movement queue is not empty
            if(player.move()){
                camera.position.set(player.getPosition().getX()*Constants.TILE_SIZE, player.getPosition().getY()*Constants.TILE_SIZE, 0);
                processTurn();
            }
             timer = 0;
         }
     }
 
     private void processTurn() {
 
 
     }
 
     @Override
     public void render(float delta) {
         super.render(delta);
         update(delta);
         camera.update();
         renderer.update();
 
 
 
     }
 }
