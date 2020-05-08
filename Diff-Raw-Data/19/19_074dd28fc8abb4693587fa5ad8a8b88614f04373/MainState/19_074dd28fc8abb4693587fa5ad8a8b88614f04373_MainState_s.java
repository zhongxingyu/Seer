 package jam.ld24.game;
  
 import infinitedog.frisky.events.InputEvent;
 import infinitedog.frisky.game.ManagedGameState;
 import jam.ld24.game.editor.EditorManager;
 import jam.ld24.level.Level;
 import jam.ld24.level.LevelManager;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 public class MainState extends ManagedGameState {
     private boolean paused = false;
     private EditorManager edm = EditorManager.getInstance();
     LevelManager lm = LevelManager.getInstance();
     
     public MainState(int stateID)
     {
         super(stateID);
         em.setGameState(C.States.MAIN_STATE.name);
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
         em.setGameState(C.States.MAIN_STATE.name);
         
         evm.addEvent(C.Events.CLOSE_WINDOW.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_ESCAPE));
         
         //Player movement
         evm.addEvent(C.Events.MOVE_LEFT.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_A));
         evm.addEvent(C.Events.MOVE_RIGHT.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_D));
         evm.addEvent(C.Events.MOVE_UP.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_W));
         evm.addEvent(C.Events.MOVE_DOWN.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_S));
         evm.addEvent(C.Events.ACTION.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_SPACE, (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.NEXT_ZOMBIE.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_J, (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.PREV_ZOMBIE.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_K, (Integer) C.Logic.SELECT_OPTION_DELAY.data));
                 
         //Load textures
         tm.addTexture(C.Textures.TILE_SET.name, C.Textures.TILE_SET.path);
         tm.addTexture(C.Textures.ZOMBIE.name, C.Textures.ZOMBIE.path);
         tm.addTexture(C.Textures.AVATAR.name, C.Textures.AVATAR.path);
         tm.addTexture(C.Textures.ENEMY.name, C.Textures.ENEMY.path);
         tm.addTexture(C.Textures.AVATAR.name, C.Textures.AVATAR.path);
         
         //Load sounds
         sm.addSound(C.Sounds.ZOMBIE_BITE.name, C.Sounds.ZOMBIE_BITE.path);
         sm.addSound(C.Sounds.ZOMBIE_GROWL.name, C.Sounds.ZOMBIE_GROWL.path);
         
         restart();
     }
     
     @Override
     public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         lm.render(gc, g);
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
         em.setGameState(C.States.MAIN_STATE.name);
         evm.update(gc, delta);
         
         if(lm.isCleared()) {
             if(!lm.loadNextLevel()) {
                 game.enterState(C.States.CREDITS_STATE.value, new FadeOutTransition(), new FadeInTransition());
             }
         }
         else {
             lm.update(gc, delta);
         }
         
         if(evm.isHappening(C.Events.CLOSE_WINDOW.name, gc)) {
             gc.exit();
         }
     }
 
     void restart() {
         lm.clearLevels();
         //lm.addLevel(new Level("level_0"));
        //lm.addLevel(new Level("level_1"));
         //lm.addLevel(new Level("level_2"));
        lm.addLevel(new Level("level_3"));
         lm.loadLevel(0);
     }
 }
