 package jam.ld26.game;
 
 import infinitedog.frisky.events.InputEvent;
 import infinitedog.frisky.game.ManagedGameState;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 /**
  *
  * @author InfiniteDog
  */
 public class InstructionsLevelEditorState extends ManagedGameState {
     
     public InstructionsLevelEditorState(int stateID) {
         super(stateID);
     }
 
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
         em.setGameState(C.States.INSTRUCTIONS_LEVEL_EDITOR_STATE.name);
         evm.addEvent(C.Events.BACK.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_SPACE, (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.FORWARD.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_ENTER, (Integer) C.Logic.SELECT_OPTION_DELAY.data));
         evm.addEvent(C.Events.CLOSE_WINDOW.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_ESCAPE));
         
         //TODO
         tm.addTexture(C.Textures.START_BACKGROUND.name, C.Textures.START_BACKGROUND.path);
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
         em.setGameState(C.States.INSTRUCTIONS_LEVEL_EDITOR_STATE.name);
         
         g.setColor(Color.white);
         g.drawString("press <SPACE> to back", 530, 50);
         g.drawString("Editor Instructions", 100, 50);
         
         g.drawString("F1 - New level. Enter the level name (use lowercase without spaces)", 100, 100);
         g.drawString("and press ENTER", 100, 115);
         g.drawString("F2 - Load current level. You will lose unsaved changes", 100, 130);
         g.drawString("F3 - Save level. Use often please.", 100, 145);
         g.drawString("n - Edit next level", 100, 160);
         g.drawString("p - Edit previous level", 100, 175);
         
         g.drawString("t - Open tile set palette. After choosing a tile click to paint.", 100, 225);
         g.drawString("h - Place player. Click to select the position.", 100, 240);
         g.drawString("g - Place goal. Click to select the position.", 100, 255);
         g.drawString("e - Place enemy. Click to select the position. Press e again to", 100, 270);
         g.drawString("change enemy type. If you click an enemy it will be erased.", 100, 285);
         
         g.drawString("c - Erase current map", 100, 325);
         g.drawString("r - Run current map. Press again to return to the editor.", 100, 340);
         
        g.drawString("press <ENTER> to enter editor", 460, 400);
 
         
         em.render(container, g);
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         em.setGameState(C.States.INSTRUCTIONS_LEVEL_EDITOR_STATE.name);
         
         em.update(container, delta);
         evm.update(container, delta);
         
         if(evm.isHappening(C.Events.BACK.name, container)) {
             game.enterState(C.States.START_STATE.value, new FadeOutTransition(), new FadeInTransition());
         }
         if(evm.isHappening(C.Events.FORWARD.name, container)) {
             game.enterState(C.States.LEVEL_EDITOR_STATE.value, new FadeOutTransition(), new FadeInTransition());
         }
         if(evm.isHappening(C.Events.CLOSE_WINDOW.name, container)) {
             container.exit();
         }
     }
     
 }
