 package com.me.mygdxgame;
 //
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.TimeUtils;
 
 public class MyGdxGame implements ApplicationListener {
 /*
  * Assets
  * 
  * Assets are things such as Images, music
  * 
  * All assets are stored in the Android project folder under assets
  */
 	
 	
 	//Textures
 	private Texture floor;
 	
 	//Music
 	private Music music1;
 	private Music music2;
 	
 	//Sound
 	
 	
 /*
  * End Assets
  */
 	
 	private static final float RUN_SPEED = 8;
 	
 	//Fonts
 	private BitmapFont font;
 	
 	
 	//This will determine what we see on the screen and also pans the view
 	private OrthographicCamera camera;
 	
 	
 	//Used to render sprites
 	private SpriteBatch batch;
 	
 	// Used to determine where mouse/finger is pressed
 	private Vector3 touchPos = new Vector3();
 	
 /*
  * In game objects	
  */
 	private ArrayList<Obstacle> obstacles;
 	private Runner runner;
 /*
  * End in game objects
  */
 	
 	//Used as a timer for spawning obstacles
 	private long lastSpawnTime;
 	private float lastSpawnPos;
 	
 	//Current game timer
 	private float timer;
 	
 	//Current game score
 	private int score;
 	
 	
 	
 	//Resolution Width
 	public final int RESW = 800;
 	//Resolution Height
 	public final int RESH = 480; 
 	
 	//private boolean played;
 	
 	// Position of the floor
 	private final int FLOOR_POS = 30;
 	
 	
 	
 	/*
 	 * The asset manager requires you to type more code, but 
 	 * allows you to prevent crashes caused by trying to use an asset
 	 * before it's fully loaded.
 	 */
 	AssetManager manager = new AssetManager();
 	
 	//The create class is called when the application is launched. It's not exactly a main class
 	@Override
 	public void create() {
 
 		
 		//dropSound = Gdx.audio.newSound(Gdx.files.internal("data/waterdrop.wav"));
 		//rainMusic = Gdx.audio.newMusic(Gdx.files.internal("data/music/01-TRACK 2.wav"));
 		//music2 = Gdx.audio.newMusic(Gdx.files.internal("data/music/2.mp3"));
 		manager.load("data/music/1.mp3", Music.class);
 		manager.load("data/music/2.mp3", Music.class);
 		manager.load("data/floor.png", Texture.class);
 		manager.update();
 		manager.finishLoading();
 		music1 =  manager.get("data/music/1.mp3", Music.class);
 		music2 = manager.get("data/music/2.mp3", Music.class);
 		floor = manager.get("data/floor.png", Texture.class);
 		
 		
 		
 		music2.setLooping(true);
 		
 
 		music1.setLooping(true);
 		//rainMusic.play();
 
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, RESW, RESH);
 
 		batch = new SpriteBatch();
 		runner = new Runner(camera, FLOOR_POS);
 
 		
 
 		obstacles = new ArrayList<Obstacle>();
 		lastSpawnPos = camera.position.x + RESW;
 		
 		
 		score = 0;
 		timer = 0;
 		
 		//font = new BitmapFont(Gdx.files.internal("Calibri.fnt"),Gdx.files.internal("Calibri.png"),false);
 		font = new BitmapFont();
 		
 		//played = false;
 
 	}
 
 	@Override
 	public void render() {
 		
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		//Moves player
 		camera.position.add(RUN_SPEED, 0 , 0);
 		camera.update();
 		
 		//if (timer >= (float)15/2 && played == false) {
 			//music2.play();
 			//played = true;
 		//}
 		
 		
 		//Updates
 		runner.update();
 		
 		//Rendering
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		runner.draw(batch);
 		//Draws all objects in Array List
 		for (int i = 0; i < obstacles.size(); i++) {
 			obstacles.get(i).draw(batch);
 		}
 		font.draw(batch, String.valueOf(camera.position.x), camera.position.x, 420);
 		font.draw(batch, String.valueOf(TimeUtils.nanoTime()), camera.position.x, 400);
 		batch.draw(floor, camera.position.x-RESW/2, FLOOR_POS-15);
 		batch.end();
 		
 		
 		//Input
 		if (Gdx.input.isKeyPressed(Keys.ANY_KEY)) {
 			runner.jump();
		} 
		else if (Gdx.input.isKeyPressed(Keys.P)) {
 		}
		else if (Gdx.input.isTouched()) {
 			runner.jump();
 		} else {
 			runner.release();
 		}
 
 		if (TimeUtils.nanoTime() - lastSpawnTime > 400000000) {
 			lastSpawnPos = MathUtils.random(lastSpawnPos, lastSpawnPos + RESW);
 			obstacles.add(new Obstacle(camera, lastSpawnPos, FLOOR_POS));
 			lastSpawnTime = TimeUtils.nanoTime();
 			System.out.println(lastSpawnPos);
 			
 		}
 
 		
 		
 		for (int i = 0; i < obstacles.size(); i++) {
 			if (obstacles.get(i).hitbox.overlaps(runner.hitbox)) {
 				runner.kill();
 			}
 			if (obstacles.get(i).isOffScreen()) {
 				obstacles.remove(i);
 			}
 		}
 
 	}
 
 	
 	@Override
 	public void dispose() {
 		for (int i = 0; i < obstacles.size(); i++) {
 			obstacles.get(i).dispose();
 		}
 		runner.dispose();
 		music1.dispose();
 		batch.dispose();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 }
