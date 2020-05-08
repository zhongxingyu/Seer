 package com.vulcastudios.states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.vulcastudios.TestGame;
 
 public class GameOptionsState extends BasicGameState {
 	
 	private int level = 1;
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		TiledMap tiledMap = ((TestGame)game).getResourceManager().maps.get("level" + level);
 		tiledMap.render(0, 0);
 		g.drawString("Level " + level, 50, 50);
 		g.drawString("Press enter to select this level", 50, 75);
 		g.drawString("Press esc to return to the main menu", 50, 100);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(TestGame.MAIN_MENU_STATE_ID);
 		} else if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
			if (level == ((TestGame)game).getLevels().size()) {
 				level = 1;
 			} else {
 				level++;
 			}
 		} else if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
 			if (level == 1) {
				level = ((TestGame)game).getLevels().size();
 			} else {
 				level--;
 			}
 		} else if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
			((TestGame)game).goToLevel(level-1);
 			game.enterState(TestGame.IN_GAME_STATE);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return TestGame.GAME_OPTIONS_STATE;
 	}
 	
 }
