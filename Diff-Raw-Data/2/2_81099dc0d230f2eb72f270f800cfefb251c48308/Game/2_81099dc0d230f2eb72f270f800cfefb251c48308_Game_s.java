 package com.sampleshooter;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.sampleshooter.Screen;
 import com.sampleshooter.GameScreen;
 
 
 class Game implements ApplicationListener {
 	// Set screen resolution for initialization
 	public static final int GAME_WIDTH = 720; // 720 for HD
	public static final int GAME_HEIGHT = GAME_WIDTH * (int)(16/9);
 
 	private boolean running = false;
 	// The main Screen, this can be anything like
 	// GameScreen or MenuScreen
 	
 	long a=0;
 	private Screen screen;
 	private final Input input = new Input();
 	private final boolean started = false;
 	private GameEngine gEngine;
 	
 	@Override
 	public void create() {
 		// Create everything
 		Art.load();
 		Sound.load();
 		Level.load();
 		running = true;
 		Gdx.input.setInputProcessor(input);
 		GameScreen pelitila = new GameScreen();
 		setScreen(pelitila);
 		this.gEngine = new GameEngine(pelitila);
 		this.input.setEngine(gEngine);
 		gEngine.start();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// !!!
 	}
 
 	@Override
 	public void render() {
 		
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		//System.out.println(a++ +" ");
 		try {
 			gEngine.tick();
 		} catch (InterruptedException e) {
 			System.out.println("Sit on nyt jo myhist surra");
 			e.printStackTrace();
 		}
 		
 		
 
 	}
 
 	@Override
 	public void pause() {
 		running = false;
 		gEngine.stop();
 	}
 
 	@Override
 	public void resume() {
 		running = true;
 		gEngine.resume();
 	}
 
 	@Override
 	public void dispose() {
 		// !!!
 	}
 	
 	/**
 	 * Changes the active screen which renders.
 	 * @param newScreen	new renderable screen
 	 */
 	private void setScreen(Screen newScreen)
 	{
 		if(screen != null)
 			screen.removed();
 		
 		screen = newScreen;
 		
 		if(screen != null)
 			screen.init(this);
 	}
 
 
 	
 
 
 }
