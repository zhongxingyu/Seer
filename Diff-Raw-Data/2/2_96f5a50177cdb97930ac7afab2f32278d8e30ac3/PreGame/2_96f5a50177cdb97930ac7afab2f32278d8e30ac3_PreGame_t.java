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
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 import com.musicgame.PumpAndJump.Util.FileChooser;
 import com.musicgame.PumpAndJump.game.GameThread;
 import com.musicgame.PumpAndJump.game.PumpAndJump;
 import com.musicgame.PumpAndJump.game.ThreadName;
 
 /**
  * It happens before the game starts
  * @author gigemjt
  *
  */
 public class PreGame extends GameThread
 {
 
 	Skin uiSkin;
 	Stage stage;
 	SpriteBatch batch;
 	Dialog check;
 	TextButton yes;
 	TextButton no;
 
 	public PreGame()
 	{
 		batch = new SpriteBatch();
 		stage = new Stage();
 		
 		
 
 		// A skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but strongly
 		// recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
         FileHandle skinFile = Gdx.files.internal( "uiskin/uiskin.json" );
         uiSkin = new Skin( skinFile );
         yes=new TextButton("yes",uiSkin);
         no=new TextButton("no",uiSkin);
         yes.addListener(new ChangeListener()
 		{
 			public void changed(ChangeEvent event, Actor actor)
 			{
 				
 				RunningGame.pick=true;
 				PumpAndJump.switchThread(ThreadName.RunningGame, PreGame.this);
 			}
 		});
 		
 		no.addListener(new ChangeListener()
 			{
 				public void changed(ChangeEvent event, Actor actor)
 				{
 					
 					
 					PumpAndJump.switchThread(ThreadName.FileChooser, PreGame.this);
 				}
 			});
         check=new Dialog("Use own music?",uiSkin);
 		check.button(yes);
 		check.button(no);
 		// Create a table that fills the screen. Everything else will go inside this table.
 		Table table = new Table();
 		//table.debug(); // turn on all debug lines (table, cell, and widget)
 		//table.debugTable(); // turn on only table lines
 		table.setFillParent(true);
 		stage.addActor(table);
 
 		// Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
 		final TextButton startGameButton = new TextButton("Start Game!", uiSkin);
 		// Add a listener to the button. ChangeListener is fired when the button's checked state changes, eg when clicked,
 		// Button#setChecked() is called, via a key press, etc. If the event.cancel() is called, the checked state will be reverted.
 		// ClickListener could have been used, but would only fire when clicked. Also, canceling a ClickListener event won't
 		// revert the checked state.
 		startGameButton.addListener(
 			new ChangeListener()
 			{
 				public void changed(ChangeEvent event, Actor actor)
 				{
 					if(FileChooserState.type=="desktop")
 					{
 						check.show(stage);
 					}
					else if(FileChooserState.type=="android" && FileChooserState.test=="no")
 					{
 						PumpAndJump.switchThread(ThreadName.FileChooser, PreGame.this);
 						
 					}
 					else
 						PumpAndJump.switchThread(ThreadName.RunningGame, PreGame.this);
 
 				}
 			});
 		final TextButton aboutButton = new TextButton("About", uiSkin);
 		aboutButton.addListener(
 				new ChangeListener()
 				{
 					public void changed(ChangeEvent event, Actor actor)
 					{
 						PumpAndJump.switchThread(ThreadName.AboutGame, PreGame.this);
 					}
 				});
 		final TextButton optionsButton = new TextButton("Options", uiSkin);
 		optionsButton.addListener(
 				new ChangeListener()
 				{
 					public void changed(ChangeEvent event, Actor actor)
 					{
 						PumpAndJump.switchThread(ThreadName.OptionsGame, PreGame.this);
 						//PumpAndJump.switchThread(ThreadName.DemoGame, PreGame.this);
 					}
 				});
 		final TextButton instructionsButton = new TextButton("Instructions", uiSkin);
 		instructionsButton.addListener(
 				new ChangeListener()
 				{
 					public void changed(ChangeEvent event, Actor actor)
 					{
 						PumpAndJump.switchThread(ThreadName.InstructionGame, PreGame.this);
 					}
 				});
 		final TextButton exitButton = new TextButton("Exit Game", uiSkin);
 		exitButton.addListener(
 				new ChangeListener()
 				{
 					public void changed(ChangeEvent event, Actor actor)
 					{
 						Gdx.app.exit();
 					}
 				});
 		
 		TextButtonStyle textstyle = startGameButton.getStyle();
 		//textstyle.font.scale(0.5f);
 		//startGameButton.setStyle(textstyle);
 		
 		table.add().expand().fill();
 		table.add(startGameButton).expand().fill().pad(5);
 		table.add().expand().fill();
 		
 		table.row();
 		table.add().expand().fill();
 		table.add(optionsButton).expand().fill().pad(5);
 		table.add().expand().fill();
 		
 		table.row();
 		table.add().expand().fill();
 		table.add(instructionsButton).expand().fill().pad(5);
 		table.add().expand().fill();
 		
 		table.row();
 		table.add().expand().fill();
 		table.add(aboutButton).expand().fill().pad(5);
 		table.add().expand().fill();
 		
 		table.row();
 		table.add().expand().fill();
 		table.add(exitButton).expand().fill().pad(5);
 		table.add().expand().fill();
 
 		//Add an image actor. Have to set the size, else it would be the size of the drawable (which is the 1x1 texture).
 		//table.add(new Image(skin.newDrawable("white", Color.RED))).size(64);
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
 		return ThreadName.PreGame;
 	}
 
 	@Override
 	public void repause() {
 	}
 
 }
