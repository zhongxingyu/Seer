 package edu.smcm.gamedev.butterseal;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 
 /**
  * Contains program logic for the user interface.
  * 
  * This class handles such things as
  *   the pause menu,
  *   the directional pad,
  *   and power selection.
  * 
  * @author Sean
  *
  */
 public class BSInterface {
     BSSession session;
     SpriteBatch batch;
     AssetManager assets;
     Map<Rectangle, BSGameStateActor> activeRegions;
     BSPlayer player;
 	
     public BSInterface(BSSession session, SpriteBatch batch, AssetManager assets) {
         this.session = session;
         this.batch = batch;
         this.assets = assets;
         this.activeRegions = new HashMap<Rectangle, BSGameStateActor>();
 		
         // test active region
         activeRegions.put(new Rectangle().set(0, 0, 100, 100), new BSGameStateActor() {
                 @Override
                 public void act(BSPlayer player) {
                     // TODO Auto-generated method stub
                     System.out.println("test");
                 }
             });
     }
 	
     /**
      * Polls the given {@link Input} for valid player interaction
      *   and handles it appropriately. 
      * @param input
      */
     public void poll(Input input) {
         for(Rectangle r : activeRegions.keySet()){
             if (isTouchingInside(input, r)){
                 activeRegions.get(r).act(player);
             }
         }
     }
 	
     /**
      * 
      * @param input
      * @param region
      * @return true if input is being touched within the given region, false otherwise
      */
     public boolean isTouchingInside(Input input, Rectangle region) {
         int x = input.getX();
         int y = input.getY();
         return region.x < x && x < region.width
            && region.y < x && x < region.height;
     }
 	
     /**
      * Draws the interface on the screen.
      */
     public void draw() {
         /* If the game is in session, make the major interface elements.
          * If the game is additionally paused, handle that as well.
          * 
          * If we are not in a game, then draw the title screen.
          */
 		
         if (session.isInGame) {
             MakePowerBar();
             MakePowerSelector();
             MakeDirectionalPad();
             MakePauseButton();
             session.state.currentMap.draw();
             if (session.isPaused) {
                 MakePauseScreen();
             }
         } else {
             MakeTitleScreen();
         }
     }
 
     private void MakePowerBar() {
 		
     }
 	
     private void MakePowerSelector() {
 		
     }
 	
     private void MakeDirectionalPad() {
 		
     }
 	
     /**
      * Dims the screen and displays the pause menu
      */
     private void MakePauseButton() {
 		
     }
 	
     private void MakePauseScreen() {
 		
     }
 	
     private void MakeTitleScreen() {
         //batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation)
     }
 }
 
 // Local Variables:
 // indent-tabs-mode: nil
 // End:
