 package com.xatulu.org.LudumDare;
 
 import com.xatulu.org.LudumDare.Intro.Text;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Patrick
  * Date: 27.04.13
  * Time: 05:06
  * To change this template use File | Settings | File Templates.
  */
 public class IntroGameState extends BasicGameState {
 
     private int stateID = 0;
     Font font = null;
 
     Text Intro0 = null;
     Text Intro1 = null;
 
    SpriteSheet sheet = null;
 
     public IntroGameState(int stateID) {
         this.stateID = stateID;
     }
 
     @Override
     public void enter(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException{
         super.enter(gameContainer, stateBasedGame);
     }
 
     @Override
     public int getID() {
         return stateID;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
         font = new AngelCodeFont("res/font.fnt", "res/font_0.png");
        sheet = new SpriteSheet("res/SpriteSheet.png", 16, 16);
         Intro0 = new Text("This is the void.", font);
         Intro1 = new Text(Intro0.getX(), Intro0.getY()+50, "It's a pretty lonely place.", font);
     }
 
     @Override
     public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) throws SlickException {
         graphics.setBackground(Color.white);
         graphics.setColor(Color.black);
         graphics.drawString(Intro0.getText(), Intro0.getX(), Intro0.getY());
         graphics.drawString(Intro1.getText(), Intro1.getX(), Intro1.getY());
        graphics.drawImage(sheet.getSprite(0,0), 100, 100); //TODO Remove
     }
 
     @Override
     public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int i) throws SlickException {
            while(Intro0.getY()>50){
                Intro0.setY((int) (Intro0.getY() - i * 0.1));
                Intro1.setY((int) (Intro1.getY() - i * 0.1));
            }
         }
    //TODO Switch to GamePlayState
     }
