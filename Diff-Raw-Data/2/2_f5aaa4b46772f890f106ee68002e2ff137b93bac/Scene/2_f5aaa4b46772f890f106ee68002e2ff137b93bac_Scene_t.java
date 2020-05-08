 package scenes.Main;
 
 import static core.BeatshotGame.INTERNAL_RES;
 import static logic.level.Level.FOV;
 import logic.Engine;
 import logic.level.Level;
 
 import scenes.Main.ui.KeyDisplay;
 import scenes.Main.ui.ScoreField;
 import scenes.Main.ui.StatBars;
 import util.SpriteSheet;
 
import CEF.InputSystem;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Graphics.DisplayMode;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GLCommon;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.utils.Array;
 
 import core.Consts.DataDir;
 
 public class Scene implements Screen {
 
 	private Sprite statBars;
 	private Sprite scoreField;
 	//private FieldDisplay field;
 	private KeyDisplay keydisp;
 	
 	private Level level;		//current loaded level
 	private String nextLevel;	//name of next level to load
 	
 	private SpriteBatch batch;
 	
 	Matrix4 normalProjection;
 
 	//loading booleans
 	private boolean uiReady;
 	private boolean levelReady;
 	private boolean doneLoading;
 
 	//curtains
 	private Sprite curtainLeft;
 	private Sprite curtainRight;
 		
 	private InputMultiplexer input;
 	
 	private Music nextBgm;	//preloaded bgm;
 
 	private Array<FileHandle> bgmPaths;
 	
 	public Scene()
 	{
 		batch = new SpriteBatch();
 		normalProjection = new Matrix4().setToOrtho2D(0, 0, INTERNAL_RES[0], INTERNAL_RES[1]);
 		uiReady = false;
 	}
 	
 	@Override
 	public void dispose() {
 		
 	}
 
 	@Override
 	public void hide() {
 		
 	}
 	
 	@Override
 	public void pause() {}
 
 	@Override
 	public void render(float delta) {
 		
 		GLCommon gl = Gdx.gl;
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		
 		//wait for assets to load
 		if (doneLoading = Engine.assets.update())
 		{
 			if (!uiReady)
 			{
 				create();
 				uiReady = true;
 			}
 			if (!this.levelReady && level != null && nextLevel == null)
 			{
 				level.start();
 				this.levelReady = true;
 				
 				input.addProcessor(level.world.getSystem(InputSystem.class));
 				Gdx.input.setInputProcessor(input);
 			}
 		}
 		
 		if (this.uiReady)
 		{
 
 			if (level != null && doneLoading)
 			{
 				level.advance(delta);
 				level.draw(batch);	
 			}
 			
 			batch.begin();
 			if (!this.levelReady)
 			{
 				//draw curtains
 				if (curtainLeft.getX() < 0)
 				{
 					curtainLeft.translate(curtainLeft.getWidth()*.75f*delta, 0f);
 					if (curtainLeft.getX() >= 0)
 					{
 						curtainLeft.setX(0);		
 					}
 				}
 				
 				if (curtainRight.getX() > 0)
 				{
 					curtainRight.translate(-curtainRight.getWidth()*.75f*delta, 0f);
 					if (curtainRight.getX() <= 0)
 					{		
 						curtainRight.setX(0);
 					}
 				}
 				curtainLeft.draw(batch);
 				curtainRight.draw(batch);
 				
 				if (curtainLeft.getX() >= 0 && curtainRight.getX() <= 0 && nextLevel != null)
 				{
 					Engine.GameOver = false;
 					if (level != null)
 					{
 						level.unloadAssets();
 						Gdx.input.setInputProcessor(null);
 						input.removeProcessor(level.world.getSystem(InputSystem.class));
 					}
 					level = new Level(nextLevel);
 					level.loadAssets();
 					nextLevel = null;
 				}
 			}
 			else
 			{
 				//hide curtains
 				if (curtainLeft.getX() > -curtainLeft.getWidth())
 				{
 					curtainLeft.translate(-curtainLeft.getWidth()*.75f*delta, 0f);
 					if  (curtainLeft.getX() <= -curtainLeft.getWidth())
 					{
 						curtainLeft.setX(-curtainLeft.getWidth());		
 					}
 					curtainLeft.draw(batch);
 				}
 				
 				if (curtainRight.getX() < FOV[3])
 				{
 					curtainRight.translate(curtainRight.getWidth()*.75f*delta, 0f);
 					if (curtainRight.getX() >= FOV[3])
 					{
 						curtainRight.setX(FOV[3]);
 					}
 					curtainRight.draw(batch);
 				}
 			}
 			
 			batch.end();
 			batch.setProjectionMatrix(normalProjection);
 			batch.begin();
 			scoreField.draw(batch);
 			statBars.draw(batch);
 			keydisp.draw(batch, 1.0f);
 			batch.end();
 		}
 		
 		if (Engine.bgm != null && !Engine.bgm.isPlaying())
 		{
 			Engine.bgm.dispose();
 			Engine.bgm = nextBgm;
 			Engine.bgm.play();
 			
 			FileHandle n = bgmPaths.get((int)(Math.random()*bgmPaths.size));
 			nextBgm = Gdx.audio.newMusic(n);
 			nextBgm.setLooping(false);
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		/*
 		normalProjection.setToOrtho2D(0f, 0f, width, height);
 		
 		int sW = (int)(width/INTERNAL_RES[0]);
 		int sH = (int)(height/INTERNAL_RES[1]);
 		
 		int scale = Math.min(sW, sH);
 		
 		normalProjection.scale(scale, scale, 1.0f);
 		
 		normalProjection.translate((width - scale*INTERNAL_RES[0]) / 2f, (height - scale*INTERNAL_RES[1]) / 2f, 0);
 		*/
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 	}
 	
 	/**
 	 * Set a new level to go to
 	 * @param levelName
 	 */
 	public void setLevel(String levelName)
 	{
 		this.nextLevel = levelName;
 		this.levelReady = false;
 	}
 
 	@Override
 	public void show() {
 		load();
 	}
 	
 	public void create()
 	{
 		statBars = new StatBars();
 		statBars.setPosition(0, 74);
 		
 		keydisp = new KeyDisplay();
 		
 		scoreField = new ScoreField();
 		scoreField.setPosition(50, 295);
 		
 		SpriteSheet curtainTex = new SpriteSheet(Gdx.files.internal(DataDir.Ui + "curtain.png"), 2, 1);
 		curtainLeft = new Sprite(curtainTex.getFrame(0));
 		curtainRight = new Sprite(curtainTex.getFrame(1));
 		
 		input = new InputMultiplexer();
 		input.addProcessor(this.keydisp.inputListener);
 		input.addProcessor(new SystemKeys(this));
 		
 		uiReady = true;
 		
 		FileHandle b = bgmPaths.get((int)(Math.random()*bgmPaths.size));
 		FileHandle n = bgmPaths.get((int)(Math.random()*bgmPaths.size));
 		if (Engine.bgm != null)
 			Engine.bgm.dispose();
 		Engine.bgm = Gdx.audio.newMusic(b);
 		nextBgm = Gdx.audio.newMusic(n);
 		nextBgm.setLooping(false);
 		Engine.bgm.setLooping(false);
 		Engine.bgm.play();
 	}
 	
 	/**
 	 * Loads all visual assets of the scene into memory
 	 */
 	public void load()
 	{
 		KeyDisplay.loadAssets();
 		StatBars.loadAssets();
 		ScoreField.loadAssets();
 		uiReady = false;
 		
 		bgmPaths = new Array<FileHandle>(Gdx.files.internal(DataDir.BGM).list());
 	
 		this.setLevel("level001");
 	}
 	
 	private class SystemKeys implements InputProcessor
 	{
 		Scene s;
 		
 		public SystemKeys(Scene s)
 		{
 			super();
 			this.s = s;
 		}
 		
 		@Override
 		public boolean keyDown(int key) {
 			if (key == Input.Keys.F11)
 			{
 				int width, height;
 				width = Gdx.graphics.getWidth();
 				height = Gdx.graphics.getHeight();
 				
 				DisplayMode desktop = Gdx.graphics.getDesktopDisplayMode();
 				if (width != desktop.width || height != desktop.height)
 				{
 					Gdx.graphics.setDisplayMode(desktop);
 				}
 				else
 				{
 					Gdx.graphics.setDisplayMode((int)INTERNAL_RES[0]*2, (int)INTERNAL_RES[1]*2, false);
 				}
 				return true;
 			}
 			if (key == Input.Keys.F9)
 			{
 				Gdx.app.exit();
 				return true;
 			}
 			if (key == Input.Keys.ENTER)
 			{
 				if (Engine.GameOver)
 				{
 					s.setLevel("level001");
 				}
 				return true;
 			}
 			if (key == Input.Keys.F1)
 			{
 				s.setLevel("level001");
 				return true;
 			}
 			
 			return false;
 		}
 
 		@Override
 		public boolean keyTyped(char arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean keyUp(int arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean mouseMoved(int arg0, int arg1) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean scrolled(int arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean touchDragged(int arg0, int arg1, int arg2) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 		
 	}
 }
