 package com.tacoid.puyopuyo;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.tacoid.puyopuyo.logic.GameLogic;
 import com.tacoid.puyopuyo.screens.MainMenuScreen;
 
 public class Controller implements InputProcessor {
 
 	private GameLogic gameLogic;
 	private Stage stage;
 
 	public Controller(GameLogic gameLogic, Stage stage) {
 		this.gameLogic = gameLogic;
 		this.stage = stage;
 	}
 	
 	@Override
 	public boolean keyDown(int key) {
		if (key != Keys.BACK && gameLogic.isPaused()) {
			return false;
		}
 		switch (key) {
 		case Keys.LEFT:
 			gameLogic.moveLeft();
 			break;
 		case Keys.RIGHT:
 			gameLogic.moveRight();
 			break;
 		case Keys.DOWN:
 			gameLogic.down();
 			break;
 		case Keys.ALT_LEFT:
 		case Keys.UP:
 			gameLogic.rotateRight();
 			break;
 		case Keys.CONTROL_LEFT:
 			gameLogic.rotateLeft();
 			break;
 		case Keys.SPACE:
 			gameLogic.dropPiece();
 			break;
 		case Keys.BACK:
 			PuyoPuyo.getInstance().setScreen(MainMenuScreen.getInstance());
 			break;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int key) {
 		switch (key) {
 		case Keys.DOWN:
 			gameLogic.up();
 			break;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		return stage.touchDown(x, y, pointer, button);
 	}
 
 	@Override
 	public boolean touchDragged(int arg0, int arg1, int arg2) {
 		return stage.touchDragged(arg0, arg1, arg2);
 	}
 
 	@Override
 	public boolean touchMoved(int x, int y) {
 		return stage.touchMoved(x, y);
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		return stage.touchUp(x, y, pointer, button);
 	}
 
 }
