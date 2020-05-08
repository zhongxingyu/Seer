 package com.musicgame.PumpAndJump.game.gameStates;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 import com.musicgame.PumpAndJump.GameObject;
 import com.musicgame.PumpAndJump.LevelInterpreter;
 import com.musicgame.PumpAndJump.Player;
 import com.musicgame.PumpAndJump.Util.AnimationUtil.Point;
 import com.musicgame.PumpAndJump.Util.MusicOutputStream;
 import com.musicgame.PumpAndJump.game.GameThread;
 import com.musicgame.PumpAndJump.game.PumpAndJump;
 import com.musicgame.PumpAndJump.game.ThreadName;
 import com.musicgame.PumpAndJump.game.physics.PersonPhysics;
 import com.musicgame.musicCompiler.MusicInputStreamer;
 
 public class RunningGame extends GameThread
 {
 
 	Stage stage;
 	SpriteBatch batch;
 	PersonPhysics physics;
 
 	MusicInputStreamer streamer;
 	MusicOutputStream outStreamer = new MusicOutputStream();
 
 	//this is a list of the on screen objects
 	//(by on screen it does include some that are partially off the screen too)
 	//the objects are basically a queue added at the end and removed from the front
 	ArrayList<GameObject> levelObjects = new ArrayList<GameObject>();
 	//contains the list of all objects that are in the level
 	ArrayList<GameObject> actualObjects = new ArrayList<GameObject>();
 
 	//Player object
 	Player player;
 	long time;
 	double frame;
 	//the current frame that the sound player is at
 	long soundFrame = 0;
 	//the distance between the frame
 	int bufferDistance = 20;
 	long sampleRate = 44100;
 	long start = 0;
 	boolean toWait = false;
 
 	boolean paused = false;
 	public RunningGame()
 	{
 		batch = new SpriteBatch();
 		stage = new Stage();
 
 		// A skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but strongly
 		// recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
         FileHandle skinFile = Gdx.files.internal( "uiskin/uiskin.json" );
         Skin uiSkin = new Skin( skinFile );
 		// Create a table that fills the screen. Everything else will go inside this table.
 		Table table = new Table();
 		table.setFillParent(true);
 		//table.debug(); // turn on all debug lines (table, cell, and widget)
 		//table.debugTable(); // turn on only table lines
 		stage.addActor(table);
 		
         player = new Player( new Point( 400.0f, 300.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ) );
 		// Create a table that fills the screen. Everything else will go inside this table.
 
         //seting up the buttons
 		final TextButton pauseButton = new TextButton("Pause", uiSkin);
 		pauseButton.setColor(0.0f,0.0f,0.0f, 0.0f); //make buttons invisible when on screen
 		pauseButton.addListener(
 				new ChangeListener()
 				{
 					public void changed(ChangeEvent event, Actor actor)
 					{
 						pausingButton();
 					}
 				});
 		final TextButton jumpButton = new TextButton("Jump", uiSkin);
 		jumpButton.setColor(0.0f,0.0f,0.0f, 0.0f); //make buttons invisible when on screen
 		jumpButton.setDisabled(true);
 		jumpButton.addListener(
 				new ChangeListener()
 				{
 					@Override
 					public void changed(ChangeEvent event, Actor actor)
 					{
 						physics.jump();
 					}
 				});
 		
 		final TextButton duckButton = new TextButton("Duck", uiSkin);
 		duckButton.setDisabled(true);
 		duckButton.setColor(0.0f,0.0f,0.0f, 0.0f); //make buttons invisible when on screen
 		duckButton.addListener(
 				new ChangeListener()
 				{
 					@Override
 					public void changed(ChangeEvent event, Actor actor)
 					{
 						physics.duck();
 					}
 				});
 		table.add(jumpButton).expand().fill();
 		table.add(pauseButton).expand().size(250,100).pad(5);
 		table.add(duckButton).expand().fill();
 
 		//setting up people and time and location
 		physics = new PersonPhysics();
 		time = 0;
 	}
 
 	public synchronized void resetTime()
 	{
 		time = 0;
 	}
 
 	public synchronized long getTimeLocation()
 	{
 		return time;
 	}
 
 	/**
 	 * Run method happens while the game is running
 	 */
 	 @Override
 	 public void run()
 	 {
 		 time = System.currentTimeMillis();
 		 start = System.currentTimeMillis();
 		 while(true)
 		 {
 			 if(bufferingNeeded())
 			 {
 				 goBuffer();
 			 }else
 			 {
 				 writeSound();
 			//	 System.out.println(before-after);
 			 }
 			 time = System.currentTimeMillis() - start;
 			 frame = time*1000.0/sampleRate;
 			 frame/=streamer.frameSize;
 
 			 /*
 			  * do math here to make sure everything is in sync
 			 */
 			 if(toWait)
 				 myWait();
 			 try {
 				Thread.sleep(5);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		 }
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
 	public boolean scrolled(int amount)
 	{
 		return false;
 	}
 
 	@Override
 	public void pause()
 	{
 		toWait = true;
 	}
 
 	@Override
 	public void render(float delta)
 	{
 		for(int k = 0;k<levelObjects.size();k++)
 		{
 			levelObjects.get(k).draw((SpriteBatch)batch);
 		}
 		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		stage.act(Math.min(delta, 1 / 30f));
 		stage.draw();
 
 		batch.begin();
 		player.draw( batch );
 		batch.end();
 
 		Table.drawDebug(stage);
 	//	System.out.println(frame);
 	}
 
 	@Override
 	public void show()
 	{
 		toWait = false;
 	}
 
 	@Override
 	public void hide()
 	{
 		toWait = true;
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	@Override
 	public void switchFrom(GameThread currentThread)
 	{
 		System.out.println("Switching!");
 		//Pause button won't work without this commented out
 		if(currentThread instanceof PauseGame)
 		{
 			Gdx.input.setInputProcessor(stage);
 			this.myNotify();
 			System.out.println("unpause");
 		}else
 		if(currentThread instanceof Buffering)
 		{
 			Gdx.input.setInputProcessor(stage);
 			System.out.println("NOTIFYING");
 			this.myNotify();
 		}else
 		if(currentThread instanceof PreGame)
 		{
 			Gdx.input.setInputProcessor(stage);
 			try {
 				actualObjects = LevelInterpreter.loadLevel();
 			} catch (Exception e) {
 				actualObjects = new ArrayList<GameObject>();
 				e.printStackTrace();
 			}
 			streamer = new MusicInputStreamer();
 			streamer.loadSound();
 			streamer.start();
 			this.start();
 		}
 			//mysounddecoder = new WavDecoder(Gdx.files.internal("drop.wav"));
 	}
 
 
 	@Override
 	public void addFrom(GameThread currentThread)
 	{
 	}
 
 	@Override
 	public void removeFrom(GameThread currentThread) {
 	}
 
 	/**
 	 * Called after notify
 	 */
 	@Override
 	public void unpause() {
 		toWait = false;
 	}
 
 	/**
 	 * This method will pause the game and go buffer for a little big
 	 */
 	public void goBuffer()
 	{
 		System.out.println("GO BUFFER!");
 		streamer.buffering = true;
 		toWait = true;
 		PumpAndJump.addThread(ThreadName.Buffering, this);
 	}
 
 	/**
 	 * The method that is called to pause the game for the pause button
 	 */
 	public void pausingButton()
 	{
 		pause();
 		toWait = true;
 		PumpAndJump.addThread(ThreadName.PauseGame, this);
 	}
 
 	/**
 	 * Returns true if the bufferingDistance is less than the bufferDistance value
 	 * it is calculated by: MusicInputStream.currentFrame - OuputStream.currentFrame
 	 * @return
 	 */
 	public boolean bufferingNeeded()
 	{
 		return streamer.currentFrame-soundFrame<bufferDistance;
 	}
 
 	/**
 	 * Returns true if the bufferingDistance is less than the bufferDistance value
 	 * it is calculated by: MusicInputStream.currentFrame - OuputStream.currentFrame
 	 * @return
 	 */
 	public long bufferingDistance()
 	{
 		return bufferDistance - (streamer.currentFrame-soundFrame);
 	}
 
 	public void writeSound()
 	{
 	//	System.out.println("Output Sound "+soundFrame);
 		outStreamer.write(streamer.frames.get((int)soundFrame));
 		soundFrame++;
 	}
 }
