 package jam.ld26.game;
 
 import infinitedog.frisky.events.InputEvent;
 import infinitedog.frisky.game.ManagedGameState;
 import jam.ld26.entities.CrossHair;
 import jam.ld26.levels.LevelEditor;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class LevelEditorState extends ManagedGameState {
     LevelEditor lvlEditor;
     CrossHair crosshair;
     
     public LevelEditorState(int stateID)
     {
         super(stateID);
         em.setGameState(C.States.LEVEL_EDITOR_STATE.name);
     }
     
     @Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
         em.setGameState(C.States.LEVEL_EDITOR_STATE.name);
         evm.addEvent(C.Events.CLOSE_WINDOW.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_ESCAPE));
         evm.addEvent(C.Events.CLICK_LEFT_EDITOR.name, new InputEvent(InputEvent.MOUSE_CLICK, 
                 Input.MOUSE_LEFT_BUTTON, (Integer) C.Logic.CLICK_EDITOR_DELAY.data));
         evm.addEvent(C.Events.EDITOR_NEW_LEVEL.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_F1,
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.LOAD_LEVEL.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_F2));
         evm.addEvent(C.Events.SAVE_LEVEL.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_F3));
         evm.addEvent(C.Events.EDITOR_TILE_SET_MENU.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_T, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.EDITOR_ERASE_LEVEL.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_C, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.EDITOR_NEXT_LEVEL.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_N, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.EDITOR_PREV_LEVEL.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_P, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.EDITOR_PLACE_PLAYER.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_H, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.EDITOR_PLACE_ENEMY.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_E, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.EDITOR_PLACE_GOAL.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_G, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.EDITOR_RUN.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_R, 
                 (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         
         //Load textures
         tm.addTexture(C.Textures.DEFAULT_TILE_SET.name, C.Textures.DEFAULT_TILE_SET.path);
         tm.addTexture(C.Textures.ENEMIES_TILE_SET.name, C.Textures.ENEMIES_TILE_SET.path);
         tm.addTexture(C.Textures.TILE_SET.name, C.Textures.TILE_SET.path);
         
         //Crosshair movement
         evm.addEvent(C.Events.CROSSHAIR_MOVED.name, new InputEvent(InputEvent.MOUSE_MOVE, 
                 new Rectangle(0, 0, C.SCREEN_WIDTH, C.SCREEN_HEIGHT)));
         try {
             //Create level editor
             lvlEditor = new LevelEditor();
         } catch (IOException ex) {
             Logger.getLogger(LevelEditorState.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         //Add Crosshair
         crosshair = new CrossHair();
         em.addEntity(C.Entities.CROSSHAIR.name, crosshair);
     }
     
     @Override
     public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         em.setGameState(C.States.LEVEL_EDITOR_STATE.name);
         em.render(gc, g);
         lvlEditor.render(gc, g);
         crosshair.render(gc, g);
     }
     
     @Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
         em.setGameState(C.States.LEVEL_EDITOR_STATE.name);
         
         evm.update(gc, delta);
         em.update(gc, delta);
         lvlEditor.update(gc, delta, crosshair);
         
         if(evm.isHappening(C.Events.CLOSE_WINDOW.name, gc)) {
            gc.exit();
         } else if(evm.isHappening(C.Events.CLICK_LEFT_EDITOR.name, gc)) {
             lvlEditor.handleClick();
         } else if(evm.isHappening(C.Events.LOAD_LEVEL.name, gc)) {
             try {
                 lvlEditor.loadLevel();
             } catch (IOException ex) {
                 Logger.getLogger(LevelEditorState.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else if(evm.isHappening(C.Events.SAVE_LEVEL.name, gc)) {
             lvlEditor.saveLevel();
         } else if(evm.isHappening(C.Events.EDITOR_ERASE_LEVEL.name, gc)) {
             lvlEditor.eraseLevel();
         } else if(evm.isHappening(C.Events.EDITOR_TILE_SET_MENU.name, gc)) {
             lvlEditor.choseTile();
         } else if(evm.isHappening(C.Events.EDITOR_NEW_LEVEL.name, gc)) {
             lvlEditor.newLevel();
         } else if(evm.isHappening(C.Events.EDITOR_NEXT_LEVEL.name, gc)) {
             try {
                 lvlEditor.nextLevel();
             } catch (IOException ex) {
                 Logger.getLogger(LevelEditorState.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else if(evm.isHappening(C.Events.EDITOR_PREV_LEVEL.name, gc)) {
             try {
                 lvlEditor.prevLevel();
             } catch (IOException ex) {
                 Logger.getLogger(LevelEditorState.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else if(evm.isHappening(C.Events.EDITOR_PLACE_PLAYER.name, gc)) {
             lvlEditor.placePlayer();
         } else if(evm.isHappening(C.Events.EDITOR_PLACE_ENEMY.name, gc)) {
             lvlEditor.placeEnemy();
         } else if(evm.isHappening(C.Events.EDITOR_PLACE_GOAL.name, gc)) {
             lvlEditor.placeGoal();
         } else if(evm.isHappening(C.Events.EDITOR_RUN.name, gc)) {
             lvlEditor.toggleRun();
         }
     }
 }
