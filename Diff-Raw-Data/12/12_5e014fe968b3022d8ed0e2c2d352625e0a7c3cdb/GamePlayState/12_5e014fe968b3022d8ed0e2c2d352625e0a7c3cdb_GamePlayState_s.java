 
 package com.ninjacannon.quantum;
 
 import com.ninjacannon.quantum.entity.EntityManager;
 import com.ninjacannon.quantum.entity.component.render.ImageLibrary;
 import com.ninjacannon.quantum.level.Background;
 import com.ninjacannon.quantum.level.SceneManager;
 import java.io.IOException;
 import java.util.Random;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.openal.SoundStore;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.ResourceLoader;
 
 /**
  * @author Dan Cannon
  */
 public class GamePlayState extends BasicGameState
 {
     private int stateID = -1;
     private Player player = null;
     private SceneManager scene = null;
     private Background background = null;
     private Image hud = null;
     private Image powered = null;
     private float hudOpacity;
     private Audio loop = null;
     private STATE GAME_STATE = null;
     
     private enum STATE{GAME, DEAD, COMPLETE, LEAVE};
     
     GamePlayState(int stateID){
         this.stateID = stateID;
     }
     
     @Override
     public int getID(){
         return stateID;
     }
     
     @Override
     public void init(GameContainer gc, StateBasedGame sbg) 
             throws SlickException
     {
         background = new Background();
         player = new Player();
         EntityManager.manager.addEntity(player.getShip());
         hud = ImageLibrary.getInstance().getImage("hud");
         powered = ImageLibrary.getInstance().getImage("powered");
         try{
             Random rand = new Random();
             loop = AudioLoader.getStreamingAudio("OGG", ResourceLoader.getResource("sounds/music"+ rand.nextInt(3) + ".ogg"));
         } catch (IOException ex){
             
         }
         GAME_STATE = STATE.GAME;
     }
     
     @Override
     public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
             throws SlickException
     {
         background.render(gc, sbg, g);
         switch(GAME_STATE){
             case GAME:
                 EntityManager.manager.render(gc, sbg, g);
                 hud.setAlpha(hudOpacity);
                 hud.draw(0, 676);
                 powered.setAlpha(hudOpacity);
                 if(player.upgrade1()){
                     powered.draw(15, 691);
                 }
                 if(player.upgrade2()){
                     powered.draw(56, 691);
                 }
                 if(player.upgrade3()){
                     powered.draw(97, 691);
                 }
                 g.setColor(Color.yellow);
                 g.fillRect(50, 736, 200 * player.getEnergy(), 5);
                 g.setColor(Color.cyan);
                 g.fillRect(50, 750, 200 * player.getShields(), 5);
                gc.getDefaultFont().drawString(900, 15, 
                        "Score:" + String.valueOf(EntityManager.manager.score));
                 break;
             case LEAVE:
                 break;
             case COMPLETE:
                 ImageLibrary.getInstance().large.drawString(400, 340, "Level Complete!");
                 gc.getDefaultFont().drawString(415, 367, "Press enter to continue!");
                 break;
             case DEAD:
                 ImageLibrary.getInstance().large.drawString(430, 340, "Game Over!");
                 gc.getDefaultFont().drawString(415, 367, "Press enter to continue!");
                 break;
         }
     }
     
     @Override
     public void update(GameContainer gc, StateBasedGame sbg, int delta)
             throws SlickException
     {
         if(!gc.hasFocus()){
             gc.setPaused(true);
             SoundStore.get().pauseLoop();
         }else{
             gc.setPaused(false);
             if(loop.isPaused()){
                 SoundStore.get().restartLoop();
             }
         }                
         if(!loop.isPlaying()){
             loop.playAsMusic(1.0f, .50f, true);
         }
         SoundStore.get().poll(delta);
         
         gc.setMouseGrabbed(true);
         Input input = gc.getInput();
 
         if(input.isKeyDown(Input.KEY_ESCAPE)){
             gc.exit();
         } 
         background.update(gc, sbg, delta);
         
         switch(GAME_STATE){
             case GAME:
                 if(input.isKeyPressed(Input.KEY_TAB)){
                     player.energy.addEnergy(100);
                 }
 
 
                 player.update(gc, sbg, delta);
                 EntityManager.manager.update(gc, sbg, delta);
                 scene.update(gc, sbg, delta);
 
                 if(player.getShip().getPosition().x < 250 && player.getShip().getPosition().y > 612){
                     hudOpacity = .15f;
                 } else {
                     hudOpacity = .75f;
                 }
                 
                 if(!player.alive){
                     GAME_STATE = STATE.DEAD;
                 }
                 if(scene.isComplete()){
                     GAME_STATE = STATE.COMPLETE;
                 }
                 break;
             case LEAVE:
                 sbg.enterState(QuantumGame.MAINMENUSTATE);
                 break;
             case COMPLETE:
                 if(input.isKeyPressed(Input.KEY_ENTER)){
                     GAME_STATE = STATE.LEAVE;
                 }
                 break;
             case DEAD:
                 if(input.isKeyPressed(Input.KEY_ENTER)){
                     GAME_STATE = STATE.LEAVE;
                 }
                 break;
         }
     }
     
     @Override
     public void leave(GameContainer gc, StateBasedGame sbg)
             throws SlickException
     {
         scene = null;
         GAME_STATE = STATE.GAME;
         EntityManager.manager.reset();
         player.reset();
     }
     
     @Override
     public void enter(GameContainer gc, StateBasedGame sbg)
             throws SlickException
     {
         this.init(gc, sbg);
     }
     
     public void loadLevel(int difficulty){
         scene = new SceneManager(difficulty, 2);
     }
 }
