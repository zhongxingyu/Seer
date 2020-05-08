 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 
 import edu.gatech.CS2340.GrandTheftPoke.GTPoke;
 
 //import com.badlogic.gdx.math.Rectangle;
 
 public class SplashScreen extends AbstractScreen {
 	private Texture splashTexture;
 
 	public SplashScreen(GTPoke game) {
 		super(game);
 	}
 
 	@Override
 	public void show() {
 		super.show();
 
		splashTexture = new Texture("images//splash.jpg");
 		stage.clear();
 		Image splashImage = new Image(splashTexture);
 		splashImage.setFillParent(true);
 
 		splashImage.getColor().a = 0f;
 
 		splashImage.addAction(sequence(fadeIn(1f), delay(1f), fadeOut(.2f),
 				new Action() {
 					@Override
 					public boolean act(float delta) {
 						game.setScreen(game.getMainMenuScreen());
 						return true;
 					}
 				}));
 
 		stage.addActor(splashImage);
 		// stage.addActor(new Rectangle(0, 0, 200, 200));
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		stage.act(delta);
 		stage.draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		splashTexture.dispose();
 	}
 }
