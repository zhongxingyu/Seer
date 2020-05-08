 package pro.oneredpixel.deflektorclassic;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.input.GestureDetector.GestureListener;
 import com.badlogic.gdx.math.Vector2;
 
 public class Deflektor implements ApplicationListener {
 	
 	Texture spritesImage;
 	Texture menuImage;
 	Sound burnCellSound;
 	Sound burnBombSound;
 	Sound exitOpenSound;
 	Sound laserFillInSound;
 	Sound laserOverheatSound;
 	Sound laserReadySound;
 	Sound levelCompletedSound;
 	Sound transferEnergySound;
 	Music music;
 	
 	OrthographicCamera camera;
 	SpriteBatch batch;
 	
 	int screenWidth;
 	int screenHeight;
 	int sprSize = 8;
 	int sprScale=1;
 	int winX;
 	int winY;
 	int winWidth;
 	int winHeight;
 	int panScale;
 	
 	int playingLevel = 1;
 	int unlockedLevel = 6;
 	final int countOfLevels = 60;
 
 
 	final static int APPSTATE_STARTED = 0;
 	final static int APPSTATE_LOADING = 1;
 	final static int APPSTATE_MENU = 2;
 	final static int APPSTATE_SELECTLEVEL = 3;
 	final static int APPSTATE_GAME = 4;
 	int appStateId = 0;
 	State appState;
 	
 	GameState gameState;
 	MenuState menuState;
 	LevelsState levelsState;
 	
 	public long lastFrameTime = 0;
 	
 	boolean soundEnabled = true;
 	   
 	@Override
 	public void create() {
 		
 		screenWidth = Gdx.graphics.getWidth();
 		screenHeight = Gdx.graphics.getHeight();
 		
 		sprSize = Math.min(screenWidth/GameState.field_width, screenHeight/(GameState.field_height+1));
 		sprScale = sprSize/8/2;
 		sprSize = 8;
 		
 		//sprScale =1;
 		//sprSize=8;
 		
 		winWidth = GameState.field_width*sprSize*2*sprScale;
 		winHeight = (GameState.field_height+1)*sprSize*2*sprScale;
 		
 		winX = (screenWidth-winWidth)/2;
 		winY = (screenHeight-winHeight)/2;		
 		
 		panScale=sprSize*sprScale/1;
 		
 		// load the images for the droplet and the bucket, 48x48 pixels each
 		spritesImage = new Texture(Gdx.files.internal("sprites.png"));
 		menuImage = new Texture(Gdx.files.internal("menu.png"));
 		  
 		// load the drop sound effect and the rain background "music"
 		burnCellSound = Gdx.audio.newSound(Gdx.files.internal("burn-cell.wav"));
 		burnBombSound = Gdx.audio.newSound(Gdx.files.internal("burn-bomb.wav"));
 		//burnBombSound.loop();
 		exitOpenSound = Gdx.audio.newSound(Gdx.files.internal("exit-open.wav"));
 		laserFillInSound = Gdx.audio.newSound(Gdx.files.internal("laser-fill-in.wav"));
 		//laserFillInSound.loop();
 		laserOverheatSound = Gdx.audio.newSound(Gdx.files.internal("laser-overheat.wav"));
 		//laserOverheatSound.loop();
 		laserReadySound = Gdx.audio.newSound(Gdx.files.internal("laser-ready.wav"));
 		levelCompletedSound = Gdx.audio.newSound(Gdx.files.internal("level-completed.wav"));
 		transferEnergySound = Gdx.audio.newSound(Gdx.files.internal("transfer-energy.wav"));
 		//transferEnergySound.loop();
 
 		//burnBombSound.setLooping(,);
 		music = Gdx.audio.newMusic(Gdx.files.internal("zxmusic.ogg"));
 		  
 		// start the playback of the background music immediately
 		music.setLooping(true);
 		
 		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		
 		batch = new SpriteBatch();
 		
 		//GestureDetector(float halfTapSquareSize, float tapCountInterval, float longPressDuration, float maxFlingDelay, GestureDetector.GestureListener listener) 
 		//GestureDetector(GestureDetector.GestureListener listener)
 		//Creates a new GestureDetector with default values: halfTapSquareSize=20, tapCountInterval=0.4f, longPressDuration=1.1f, maxFlingDelay=0.15f.
 		Gdx.input.setInputProcessor(new GestureDetector(sprSize/2*sprScale, 0.4f, 1.1f, 0.15f, new MyGestureListener()));
 		
 		gotoAppState(APPSTATE_MENU);
 
 	   }
 
 	
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public class MyGestureListener implements GestureListener {
 		
 		public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
 			return false;
 		}
 
 		@Override
 		public boolean fling(float arg0, float arg1, int arg2) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean longPress(float arg0, float arg1) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean tap(float x, float y, int tapCount, int button) {
 			appState.tap(x, y, tapCount, button);
 			return false;
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer, int button) {
 			appState.touchDown(x, y, pointer, button);
 			return false;
 		} 
 
 		@Override
 		public boolean pan(float x, float y, float deltaX, float deltaY) {
 			appState.pan(x, y, deltaX, deltaY);
 			return false;
 		}
 		
 		@Override
 		public boolean zoom(float arg0, float arg1) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 	}
 	
 	void gotoAppState(int newState) {
 		if (appState!=null) {
 			appState.stop();
 		};
 		
 		switch (newState) {
 		case APPSTATE_STARTED:
 			break;
 		case APPSTATE_LOADING:
 			break;
 		case APPSTATE_MENU:
 			if (menuState==null) {
 				menuState = new MenuState(this);
 				menuState.create();
 			};
 			appState = menuState;
 			Gdx.input.setCatchBackKey(false);
 			if (soundEnabled) music.play();
 			break;
 		case APPSTATE_SELECTLEVEL:
 			if (levelsState==null) {
 				levelsState = new LevelsState(this);
 				levelsState.create();
 			};
 			appState = levelsState;
 			Gdx.input.setCatchBackKey(false);
 			break;
 			
 		case APPSTATE_GAME:
 			if (gameState==null) {
 				gameState = new GameState(this);
 				gameState.create();
 			};
 			appState = gameState;
 			
 			Gdx.input.setCatchBackKey(true);
 			music.stop();
 			break;
 		};
 		appStateId = newState;
 		appState.start();
 	}
 
 	@Override
 	public void render() {
 		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.update();
 		appState.render(batch);
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	void menu_putRegion(int x, int y, int srcWidth, int srcHeight, int srcX, int srcY) {
 		batch.draw(menuImage, winX+x*sprScale, screenHeight-winY-y*sprScale-srcHeight*sprScale, srcWidth*sprScale,srcHeight*sprScale, srcX, srcY, srcWidth,srcHeight,false,false);
 	};
 	
 	void spr_putRegion(int x, int y, int srcWidth, int srcHeight, int srcX, int srcY) {
 		batch.draw(spritesImage, winX+x*sprScale, screenHeight-winY-y*sprScale-srcHeight*sprScale, srcWidth*sprScale,srcHeight*sprScale, srcX, srcY, srcWidth,srcHeight,false,false);
 	};
 	
 	void spr_putRegionSafe(int x, int y, int srcWidth, int srcHeight, int srcX, int srcY) {
 		batch.draw(spritesImage, winX+x*sprScale, screenHeight-winY-y*sprScale-srcHeight*sprScale, srcWidth*sprScale,srcHeight*sprScale, srcX, srcY, srcWidth,srcHeight,false,false);
 	};
 	
 	void showBigNumber(int x, int y, int num) {
 		int up=num/10;
 		int lo=num-up*10;
 		menu_putRegion(x, y, 8, 8, up*8,96);
 		menu_putRegion(x+8, y, 8, 8, lo*8,96);
 	}
 	
 	void unlockLevel(int level) {
		unlockedLevel=level;
 	}
 
 }
 
