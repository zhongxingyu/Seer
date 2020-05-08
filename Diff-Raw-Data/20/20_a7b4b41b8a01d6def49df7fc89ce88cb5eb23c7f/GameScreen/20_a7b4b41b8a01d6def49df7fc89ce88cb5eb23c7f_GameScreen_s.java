 package com.parasitefrog.colorfall.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.parasitefrog.colorfall.ColorFallGame;
 import com.parasitefrog.colorfall.game.AbstractToken;
 import com.parasitefrog.colorfall.game.token.LToken;
 
 public class GameScreen extends AbstractScreen {
 
 	public GameScreen(ColorFallGame game) {
 		super(game);
 	}
 
 	AbstractToken activeToken;
 	
 	
 	@Override
 	public void show() {
 		super.show();
 		
 		// TODO get knowledge about groups in scene2d
 		
 		
 		
 		activeToken = new LToken();
 		activeToken.setName("activeToken");
		activeToken.setPosition(10, 10);
 		stage.addActor(activeToken);
		
 			
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 	}
 	
 	
 
 }
