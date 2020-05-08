 package com.vulcastudios.states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.vulcastudios.TestGame;
 
 public class CreditsState extends BasicGameState {
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		g.drawString("Alex Meade", 50, 50);
 		g.drawString("Forrest Meade", 50, 75);
 		g.drawString("Andrew Melton", 50, 100);
 		g.drawString("Taylor Paschal", 50, 125);
 		g.drawString("Stephanie Reese", 50, 150);
 		g.drawString("Sam Thomas", 50, 175);
		g.drawString("press esc to return to the main menu", 50, 225);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(TestGame.MAIN_MENU_STATE_ID);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return TestGame.CREDITS_STATE;
 	}
 
 }
