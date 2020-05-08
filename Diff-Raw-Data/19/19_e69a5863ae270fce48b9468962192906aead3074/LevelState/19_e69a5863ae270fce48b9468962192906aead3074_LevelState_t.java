 package edu.chl.codenameg.controller;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import edu.chl.codenameg.model.Action;
 import edu.chl.codenameg.model.GameModel;
 import edu.chl.codenameg.view.LevelView;
 
 public class LevelState extends BasicGameState {
 	
 	LevelView view;
 	GameModel model;
 	boolean player1LeftKeyPressed;
 	boolean player1RightKeyPressed;
 	boolean player2LeftKeyPressed;
 	boolean player2RightKeyPressed;
 	
 	public LevelState(GameModel model) {
 		this.view = new LevelView(model);
 		
 		this.model = model;
 
 		player1LeftKeyPressed = false; player1RightKeyPressed = false;
 		player2LeftKeyPressed = false; player2RightKeyPressed = false;
 	}
 
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		view.repaint(g);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int elapsedTime)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		model.update(elapsedTime);
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return 2;
 	}
 	
 	@Override
 	public void keyPressed(int key, char c) {
 		
 		Action action = KeyBindings.getAction(key);
 		switch (action) {
 		case PLAYER_1_MOVE_LEFT:
 			player1LeftKeyPressed = true;
 			break;
 		case PLAYER_1_MOVE_RIGHT:
 			player1RightKeyPressed = true;
 			break;
 		case PLAYER_2_MOVE_LEFT:
 			player2LeftKeyPressed = true;
 			break;
 		case PLAYER_2_MOVE_RIGHT:
 			player2RightKeyPressed = true;
 			break;
 		default:
 			break;
 		}
 		model.performAction(action);
 		
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
 		Action action = KeyBindings.getAction(key);
 		switch (action) {
 		case PLAYER_1_MOVE_LEFT:
 			player1LeftKeyPressed = false;
 			if (player1RightKeyPressed) {
 				model.performAction(Action.PLAYER_1_MOVE_RIGHT);
			} else {
				model.stopAction(action);
 			}
 			break;
 		case PLAYER_1_MOVE_RIGHT:
 			player1RightKeyPressed = false;
 			if (player1LeftKeyPressed) {
 				model.performAction(Action.PLAYER_1_MOVE_LEFT);
			}else {
				model.stopAction(action);
 			}
 			break;
 		case PLAYER_2_MOVE_LEFT:
 			player2LeftKeyPressed = false;
 			if (player2RightKeyPressed) {
 				model.performAction(Action.PLAYER_2_MOVE_RIGHT);
			}else {
				model.stopAction(action);
 			}
 			break;
 		case PLAYER_2_MOVE_RIGHT:
 			player2RightKeyPressed = false;
 			if (player2RightKeyPressed) {
 				model.performAction(Action.PLAYER_2_MOVE_RIGHT);
			}else {
				model.stopAction(action);
 			}
 			break;
 		default:
 			model.stopAction(action);
 			break;
 		}
 		
 		// Doesn't matter if I send both stopActions or not
 		if (!player1LeftKeyPressed && !player1RightKeyPressed) {
 			model.stopAction(Action.PLAYER_1_MOVE_LEFT);
 		} else if (!player2LeftKeyPressed && !player2RightKeyPressed) {
 			model.stopAction(Action.PLAYER_2_MOVE_LEFT);
 		}
 		
 	}
 
 }
