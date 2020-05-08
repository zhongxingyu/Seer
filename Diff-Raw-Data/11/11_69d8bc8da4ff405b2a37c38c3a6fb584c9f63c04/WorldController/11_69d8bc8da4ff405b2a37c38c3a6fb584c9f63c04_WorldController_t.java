 package com.aquasheep.Static.controller;
 
 import com.aquasheep.Static.StaticGame;
 import com.aquasheep.Static.model.World;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 
 public class WorldController implements InputProcessor {
 
 	StaticGame game;
 	World world;
 	
 	public WorldController(StaticGame game, World world) {
 		this.game = game;
 		this.world = world;
 		Gdx.input.setInputProcessor(this);
 	}
 	
	/** Checks to see if a mouse button is down, and if so, applies current tool */
 	public boolean isMouseButtonDown() {
 		if (Gdx.input.isButtonPressed(0))
 			world.applyTool(0);
 		else if (Gdx.input.isButtonPressed(1))
 			world.applyTool(1);
 		return true;
 	}
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.SHIFT_LEFT)
 			world.switchTool();
 		//Handle color channel changes
 		switch(keycode) {
 			case Keys.NUM_0:
 				world.setColorChannel(0);
 				break;
 			case Keys.NUM_1:
 				world.setColorChannel(1);
 				break;
 			case Keys.NUM_2:
 				world.setColorChannel(2);
 				break;
 			case Keys.NUM_3:
 				world.setColorChannel(3);
 				break;
 			case Keys.NUM_4:
 				world.setColorChannel(4);
 				break;
 			case Keys.NUM_5:
 				world.setColorChannel(5);
 				break;
 			case Keys.NUM_6:
 				world.setColorChannel(6);
 				break;
 			case Keys.NUM_7:
 				world.setColorChannel(7);
 				break;
 			default: break;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		//Buttons - 0-left,1-right,2-middle
 		world.applyTool(button);
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	/** Scrolling mousewheel changes volume */
 	public boolean scrolled(int amount) {
 		world.addToVolume(amount*5);
 		return true;
 	}
 
 }
