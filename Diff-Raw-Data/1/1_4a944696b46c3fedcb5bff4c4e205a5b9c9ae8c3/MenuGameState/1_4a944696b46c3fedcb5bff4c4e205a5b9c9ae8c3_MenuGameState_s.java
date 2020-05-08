 package engine;
 
 import org.newdawn.slick.*;
 
 import engine.menu.DirList;
 
 public class MenuGameState extends BasicGameState {
         
     public static final int ID = Engine.GAME_STATE_MENU;
     
     public MenuGameState() {
         super();
     }
     
     @Override
     public void keyReleased(int key, char c) {
         switch (key) {
             case Input.KEY_S:
                 DirList.getInstance().nextMenuItem();
                 break;
             case Input.KEY_W:
                 DirList.getInstance().previousMenuItem();
                 break;
             default: break;
         }
     }
     
     @Override
     public int getID() { return ID; }
     
     @Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
         
     }
     
     @Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
         
     }
     
     @Override
     public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         DirList.getInstance().draw();
     }
 }
