 package com.parasitefrog.colorfall.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.parasitefrog.colorfall.ColorFallGame;
 
 
 public class SplashScreen extends AbstractScreen {
 
 	public SplashScreen(ColorFallGame game) {
 		super(game);
 	}
 
 	@Override
 	public void show() {
 		super.show();
 		
		if(ColorFallGame.DEVELOPMENT_MODE) {
			game.setScreen(new MenuScreen(game));
		}
		
 		Texture pfrog = new Texture("splashscreen/pfrog.png");
 		Image splash = new Image(pfrog);
 		
 		splash.getColor().a = 0f;
 		splash.addAction(sequence(fadeIn(1.5f), delay(2f), fadeOut(1.5f), Actions.run(new Runnable() {
 			
 			@Override
 			public void run() {
 				game.setScreen(new MenuScreen(game));
 			}
 		})));
 		
 		stage.addActor(splash);
 	}
 
 }
