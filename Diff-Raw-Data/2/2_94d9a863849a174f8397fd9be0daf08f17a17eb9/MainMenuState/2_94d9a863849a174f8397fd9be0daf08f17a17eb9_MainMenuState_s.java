 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package states;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.Image;
 
 /**
  *
  * @author root
  */
 public class MainMenuState extends BasicGameState {
     int stateID = 0;
 
     Image playGame;
     Image exitGame;
 
     public MainMenuState(int stateID){
         this.stateID = stateID;
     }
 
     @Override
     public int getID(){
         return 0;
     }
 
     public void init(GameContainer container, StateBasedGame sbg) throws SlickException {
        playGame = new Image("data/playnowimage.jpg");
         exitGame = new Image("data/exitimage.png");
     }
 
     public void render(GameContainer container, StateBasedGame sbg, Graphics grphcs) throws SlickException {
         grphcs.drawString("Adventure Game", 180, 50);
         grphcs.drawString("by Aron, Kelvin, Marieta", 130, 70);
         playGame.draw(150, 120);
         exitGame.draw((192), (340));
     }
 
     public void update(GameContainer container, StateBasedGame sbg, int delta) throws SlickException {
         int posX = Mouse.getX();
         int posY = Mouse.getY();
 
         //playGame button
         //coordinates start at bottom left
         if((posX > 150 && posX < 335) && (posY > 200 && posY < 350)){
             if(Mouse.isButtonDown(0)){
                 sbg.enterState(1);
             }
         }
         //exitGame button
         if((posX > 192 && posX < 292) && (posY > 40 && posY < 140)){
             if(Mouse.isButtonDown(0)){
                 System.exit(0);
             }
         }
     }
 
     
 }
