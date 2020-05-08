 package com.musicgame.PumpAndJump.game.gameStates;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.musicgame.PumpAndJump.game.GameThread;
 import com.musicgame.PumpAndJump.game.PumpAndJump;
 import com.musicgame.PumpAndJump.game.ThreadName;
 
 /**
  * It happens before the game starts
  * @author gigemjt
  *
  */
 public class OptionsGame extends GameThread
 {
 
 	Skin uiSkin;
 	Stage stage;
 	SpriteBatch batch;
 	ThreadName reverseThread;
 	public OptionsGame()
 	{
 		batch = new SpriteBatch();
 		stage = new Stage();
 
         FileHandle skinFile = Gdx.files.internal( "uiskin/uiskin.json" );
         uiSkin = new Skin( skinFile );
 
         Table container = new Table();
 		stage.addActor(container);
 		container.setFillParent(true);
 
 		Table table = new Table();
 		// table.debug();
 
 		final ScrollPane scroll = new ScrollPane(table, uiSkin);
 
 		InputListener stopTouchDown = new InputListener() {
 			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 				event.stop();
 				return false;
 			}
 		};
 
 		table.pad(10).defaults().expandX().space(4);
 		for (int i = 0; i < 10; i++) {
 			table.row();
 			table.add(new Label(i + "uno", uiSkin)).expandX().fillX();
 
 			TextButton button = new TextButton(i + "dos", uiSkin);
 			table.add(button);
 			button.addListener(new ClickListener() {
 				public void clicked (InputEvent event, float x, float y) {
 					System.out.println("click " + x + ", " + y);
 				}
 			});
 
 			Slider slider = new Slider(0, 100, 1, false, uiSkin);
 			slider.addListener(stopTouchDown); // Stops touchDown events from propagating to the FlickScrollPane.
 			table.add(slider);
 
 			table.add(new Label(i + "tres long0 long1 long2 long3 long4 long5", uiSkin));
 		}
 
 		final TextButton flickButton = new TextButton("Flick Scroll", uiSkin.get("toggle", TextButtonStyle.class));
 		flickButton.setChecked(true);
 		flickButton.addListener(new ChangeListener() {
 			public void changed (ChangeEvent event, Actor actor) {
 				scroll.setFlickScroll(flickButton.isChecked());
 			}
 		});
 
 		final TextButton fadeButton = new TextButton("Fade Scrollbars", uiSkin.get("toggle", TextButtonStyle.class));
 		fadeButton.setChecked(true);
 		fadeButton.addListener(new ChangeListener() {
 			public void changed (ChangeEvent event, Actor actor) {
 				scroll.setFadeScrollBars(fadeButton.isChecked());
 			}
 		});
 
 		final TextButton smoothButton = new TextButton("Smooth Scrolling", uiSkin.get("toggle", TextButtonStyle.class));
 		smoothButton.setChecked(true);
 		smoothButton.addListener(new ChangeListener() {
 			public void changed (ChangeEvent event, Actor actor) {
 				scroll.setSmoothScrolling(smoothButton.isChecked());
 			}
 		});
 
 		final TextButton onTopButton = new TextButton("Scrollbars On Top", uiSkin.get("toggle", TextButtonStyle.class));
 		onTopButton.addListener(new ChangeListener() {
 			public void changed (ChangeEvent event, Actor actor) {
 				scroll.setScrollbarsOnTop(onTopButton.isChecked());
 			}
 		});
 
 		final TextButton backButton = new TextButton("Back", uiSkin);
 		backButton.addListener(
 					new ChangeListener()
 					{
 						public void changed(ChangeEvent event, Actor actor)
 						{
 							goBack();
 						}
 					});
 		container.add(scroll).size(450,200);
 		container.row().space(10).padBottom(10);
 		container.add(backButton).size(250,50).pad(5);
 		//container.add(flickButton).right().expandX();
 		//container.add(onTopButton);
 		//container.add(smoothButton);
 		//container.add(fadeButton).left().expandX();
 	}
 
 	@Override
     public void render(float delta)
 	{
 		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		stage.act(Math.min(delta, 1 / 30f));
 		stage.draw();
 		Table.drawDebug(stage);
     }
 
 	@Override
     public void resize(int width, int height)
 	{
 		super.resize(width, height);
         stage.setViewport(width, height, false);
     }
 
 	public void goBack()
 	{
 		switch(reverseThread)
 		{
 			case PreGame:
 				PumpAndJump.switchThread(ThreadName.PreGame, this);break;
 			case PauseGame:
 				PumpAndJump.removeThread(ThreadName.OptionsGame, this);break;
 		}
 	}
 
 	@Override
 	public void switchFrom(GameThread currentThread)
 	{

 		Gdx.input.setInputProcessor(stage);
		reverseThread = currentThread.getThreadName();
 
 	}
 
 	@Override
	public void addFrom(GameThread currentThread)
	{
		Gdx.input.setInputProcessor(stage);
		reverseThread = currentThread.getThreadName();
 	}
 
 	@Override
	public void removeFrom(GameThread currentThread)
	{
 	}
 
 	@Override
 	public void unpause() {
 	}
 
 	@Override
 	public ThreadName getThreadName() {
 		return ThreadName.OptionsGame;
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void show() {
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 }
