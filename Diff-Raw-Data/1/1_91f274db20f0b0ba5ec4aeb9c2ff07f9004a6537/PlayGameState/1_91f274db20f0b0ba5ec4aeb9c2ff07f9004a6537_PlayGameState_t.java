 package run;
 
 import java.awt.Font;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class PlayGameState extends BasicGameState {
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
     }
 
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         Input input = container.getInput();
         if (input.isKeyPressed(input.KEY_ESCAPE)) {
             container.exit();
         }
     }
 
     @Override
     public int getID() {
         return 1;
     }
 
     @Override
     public void enter(GameContainer container, StateBasedGame game) {
     }
 
 }
