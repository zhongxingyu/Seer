 package com.musicgame.PumpAndJump.game.gameStates;
 
 import java.io.File;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 import com.musicgame.PumpAndJump.Util.FileChooser;
 import com.musicgame.PumpAndJump.game.GameThread;
 import com.musicgame.PumpAndJump.game.PumpAndJump;
 import com.musicgame.PumpAndJump.game.ThreadName;
 
 public class FileChooserState extends GameThread
 {
 	Stage stage;
 	public static FileChooser fileDialog;
 	public static String type;
 	public static String test;
 	//need to make a file chooser
 	public FileChooserState()
 	{
 		stage = new Stage();
 
 		// A skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but strongly
 		// recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
 		if(uiSkin == null)
 		{
 	        FileHandle skinFile = Gdx.files.internal( "uiskin/uiskin.json" );
 	        uiSkin = new Skin( skinFile );
 		}
 
 		// Create a table that fills the screen. Everything else will go inside this table.
 		Table table = new Table();
 		//table.debug(); // turn on all debug lines (table, cell, and widget)
 		//table.debugTable(); // turn on only table lines
 		table.setFillParent(true);
 		stage.addActor(table);
 
 		// Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
 		final TextButton startGameButton = new TextButton("Hand That Feeds", uiSkin);
 		// Add a listener to the button. ChangeListener is fired when the button's checked state changes, eg when clicked,
 		// Button#setChecked() is called, via a key press, etc. If the event.cancel() is called, the checked state will be reverted.
 		// ClickListener could have been used, but would only fire when clicked. Also, canceling a ClickListener event won't
 		// revert the checked state.
 		startGameButton.addListener(
 			new ChangeListener()
 			{
 				public void changed(ChangeEvent event, Actor actor)
 				{
 					RunningGame.pick=false;
 					RunningGame.test="the_hand_that_feeds.wav";
 					PumpAndJump.switchThread(ThreadName.RunningGame, FileChooserState.this);
 				}
 			});
 		table.add().expand().fill();
 		table.add(startGameButton).expand().fill().pad(5);
 		table.add().expand().fill();
 
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
 
 	@Override
 	public void switchFrom(GameThread currentThread)
 	{
 		Gdx.input.setInputProcessor(stage);
 	}
 
 	@Override
 	public void addFrom(GameThread currentThread) {
 	}
 
 	@Override
 	public void removeFrom(GameThread currentThread) {
 	}
 
 	@Override
 	public void unpause() {
 	}
 
 	@Override
 	public ThreadName getThreadName() {
 		return ThreadName.FileChooser;
 	}
 
 	@Override
 	public void repause() {
 	}
 
 }
