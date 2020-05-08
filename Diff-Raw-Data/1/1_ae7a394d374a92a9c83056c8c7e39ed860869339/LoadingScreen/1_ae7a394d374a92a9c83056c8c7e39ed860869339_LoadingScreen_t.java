 package org.doublelong.catchr.screens;
 
 import org.doublelong.catchr.Catchr;
 import org.doublelong.catchr.entity.Textr;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 
 public class LoadingScreen extends AbstractScreen
 {
 	private Stage stage;
 
 	private Image logo;
 	private Textr textr;
 	private Label label;
 
 	public LoadingScreen(Catchr game)
 	{
 		super(game);
 		this.textr = new Textr();
 	}
 
 	@Override
 	public void show()
 	{
 		this.game.manager.load("assets/sounds/contemplation_2.mp3", Music.class);
 		this.game.manager.load("assets/sounds/laser1.mp3", Sound.class);
 		this.game.manager.load("assets/images/catchr_logo.png", Texture.class);
 
 		this.game.manager.finishLoading();
 
 		this.stage = new Stage();
 
 		this.logo = new Image(this.game.manager.get("assets/images/catchr_logo.png", Texture.class));
 		this.label = new Label("Press Space to continue", new LabelStyle(this.textr.font, Color.RED));
 		this.stage.addActor(this.logo);
 
 	}
 
 	@Override
 	public void resize(int width, int height)
 	{
 		stage.setViewport(width, height, false);
 		this.logo.setX((width - logo.getWidth()) / 2);
 		this.logo.setY((height - logo.getHeight()) / 2);
 		this.label.setX(50f);
 		this.label.setY(50f);
 	}
 
 	@Override
 	public void render(float delta)
 	{
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		if(this.game.manager.update())
 		{
 			this.stage.addActor(this.label);
 			if (Gdx.input.isKeyPressed(Keys.SPACE))
 			{
 				game.setScreen(new CatchrScreen(this.game, true));
 			}
 		}
		//this.logo.setRotation(this.logo.getRotation() + .01f);
 		this.stage.act();
 		this.stage.draw();
 	}
 }
