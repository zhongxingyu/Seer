 package de.game.bomberman;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * ??????????????????????????????????????????????????????????????????????????????????????????
  *
  */
 public class SingleplayerDummy extends BasicGameState {
   
   public static final int stateID = 2;
   
 //KONSTRUKTOR:
   
  
  /* 
  * @see org.newdawn.slick.state.BasicGameState#getID()
  */
   public int getID() {
     return stateID;
   }
 
 
  /* 
  * @see org.newdawn.slick.state.GameState#init(org.newdawn.slick.GameContainer, org.newdawn.slick.state.StateBasedGame)
  */
 public void init(GameContainer container, StateBasedGame sb) throws SlickException {
        
   }
 
 
  /* 
  * @see org.newdawn.slick.state.GameState#render(org.newdawn.slick.GameContainer, org.newdawn.slick.state.StateBasedGame, org.newdawn.slick.Graphics)
  */
 public void render(GameContainer container, StateBasedGame sb, Graphics g) throws SlickException {
    g.drawString("Hier kommt der Singleplayermodus gegen die KI hin." + "\n" + "Durch ESC wieder zum Menu", 150, 300);    
   }
 
 
  /* 
  * @see org.newdawn.slick.state.GameState#update(org.newdawn.slick.GameContainer, org.newdawn.slick.state.StateBasedGame, int)
  */
 public void update(GameContainer container, StateBasedGame sb, int arg1) throws SlickException {
     
     if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
       sb.enterState(0);     
   }
 
  }
 }
