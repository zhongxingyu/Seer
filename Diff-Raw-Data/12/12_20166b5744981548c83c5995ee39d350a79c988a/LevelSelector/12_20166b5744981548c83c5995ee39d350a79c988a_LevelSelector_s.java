 package view;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class LevelSelector extends BasicGameState {
     private StateBasedGame game;
     private GameContainer container;
     private Integer selected = 0;
     
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
         this.container = gc;
         this.game = sbg;
     }
     
     public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException { }
     
     public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
         g.setColor(new Color(255, 215, 0));
         for (int x = 0; x < 7; ++x) {
             for (int y = 0; y < 5; ++y) {
                 if (y * 7 + x == selected) {
                     g.setColor(new Color(255, 0, 0));
                 }
                 g.fillRect(64 + x * 2 * 64, 64 + y * 2 * 64, 64, 64);
                 g.setColor(new Color(255, 215, 0));
             }
         }
     }
 
     public void keyPressed(int key, char c) {
        if (key == Input.KEY_ENTER) {
             try {
                 game.getState(1).init(container, game);
                 ((Level) game.getState(1)).setLevel(selected + 1);
             } catch (SlickException e) {
                 e.printStackTrace();
             }
             game.enterState(1);
         }
         else {
             int d = 0;
             switch (key) {
                 case Input.KEY_UP : d = -7; break;
                 case Input.KEY_DOWN : d = 7;; break;
                 case Input.KEY_LEFT : d = -1; break;
                 case Input.KEY_RIGHT : d = 1; break;
             }
             if (selected + d >= 0 && selected + d < 7 * 5) {
                 selected += d;
             }
         }
     }
     public int getID() {
         return 0;
     }
 }
