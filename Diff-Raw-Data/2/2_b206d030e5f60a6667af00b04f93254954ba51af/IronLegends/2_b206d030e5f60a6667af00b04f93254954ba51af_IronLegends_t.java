 package jig.ironLegends;
 
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.util.Iterator;
 
 import javax.imageio.spi.ServiceRegistry;
 
 import jig.engine.Mouse;
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.Sprite;
 import jig.engine.ViewableLayer;
 import jig.engine.hli.ScrollingScreenGame;
 //import jig.engine.hli.StaticScreenGame;
 import jig.engine.physics.AbstractBodyLayer;
 import jig.engine.physics.Body;
 import jig.engine.physics.BodyLayer;
 import jig.engine.physics.vpe.VanillaAARectangle;
 import jig.engine.util.Vector2D;
 import jig.engine.physics.vpe.VanillaPhysicsEngine;
 import jig.misc.sat.PolygonFactory;
 
 import jig.ironLegends.collision.*;
 import jig.ironLegends.core.Fonts;
 import jig.ironLegends.core.GameScreen;
 import jig.ironLegends.core.GameScreens;
 import jig.ironLegends.core.HighScore;
 import jig.ironLegends.core.HighScorePersistance;
 import jig.ironLegends.core.InstallInfo;
 import jig.ironLegends.core.KeyCommands;
 import jig.ironLegends.core.ResourceReader;
 import jig.ironLegends.core.SoundFx;
 import jig.ironLegends.core.StaticBodyLayer;
 import jig.ironLegends.core.Tile;
 import jig.ironLegends.core.GameScreens.ScreenTransition;
 import jig.ironLegends.core.ui.Button;
 import jig.ironLegends.core.ui.IUIEvent;
 import jig.ironLegends.core.ui.MouseState;
 import jig.ironLegends.screens.CustomizePlayerGS;
 import jig.ironLegends.screens.CustomizePlayerTextLayer;
 import jig.ironLegends.screens.GameInfoTextLayer;
 import jig.ironLegends.screens.GameOverTextLayer;
 import jig.ironLegends.screens.GamePlayTextLayer;
 import jig.ironLegends.screens.GameWonTextLayer;
 import jig.ironLegends.screens.HelpTextLayer;
 import jig.ironLegends.screens.SplashTextLayer;
 
 public class IronLegends extends ScrollingScreenGame
 {
 	static final int TILE_WIDTH = 32;
 	static final int TILE_HEIGHT = 32;
 	
 	public static final int WORLD_WIDTH = 1200;
 	public static final int WORLD_HEIGHT = 800;
 	public static final int SCREEN_WIDTH = 800;
 	public static final int SCREEN_HEIGHT = 600;
 	static final String RESOURCE_ROOT = "jig/ironLegends/";
 	
 	public static final String SPRITE_SHEET = RESOURCE_ROOT + "hr-spritesheet.png";
 	static final String MY_RESOURCES = "hr-resources.xml";
 
 	static final int START_LIVES = 2;
 	
 	static final int SPLASH_SCREEN = 0;
 	static final int HELP_SCREEN = 1;
 	static final int GAMEOVER_SCREEN = 2;
 	static final int GAMEPLAY_SCREEN = 3;
 	static final int GAMEWON_SCREEN = 4;
 	//static final int LEVELCOMPLETE_SCREEN = 5;
 	static final int CUSTOMIZEPLAYER_SCREEN = 6;
 	
 	Fonts m_fonts = new Fonts();
 	
 	Mitko m_mitko;
 	BodyLayer<Body> m_mitkoLayer;
 	BodyLayer<Body> m_antLayer;
 	BodyLayer<Body> m_spiderLayer;
 	BodyLayer<Body> m_batLayer;
 	BodyLayer<Body> m_batCaveLayer;
 	
 	BodyLayer<Body> m_hedgeLayer;
 	BodyLayer<Body> m_bgLayer;
 	BodyLayer<Body> m_powerUpLayer;
 	BodyLayer<Body> m_creatures;
 	
 	jig.engine.physics.vpe.VanillaPhysicsEngine m_physicsEngine;
 	
 	// TODO: move into level class
 	LevelProgress m_levelProgress;
 	GameProgress m_gameProgress;
 	//int m_bricksHit = 0;
 	BodyLayer<Body> m_weedLayer;
 
 	boolean m_bGameOver = false;
 
 	GameScreens m_screens = new GameScreens();
 	
 	//protected jig.engine.audio.jsound.AudioClip m_audioBallBrick2 = null;
 	//protected jig.engine.audio.jsound.AudioClip m_audioBallBrick3 = null;
 	protected HighScore m_highScore = new HighScore();
 	protected String m_sError;
 
 	protected String m_sInstallDir = "\\Temp";
 	protected HighScorePersistance m_highScorePersist;
 	protected PolygonFactory m_polygonFactory;
 	LevelGrid m_grid;
 	//VanillaAARectangle	m_mPos;
 	ResourceReader m_rr;
 	protected SoundFx m_sfx;
 	PlayerInfo m_playerInfo;
 
 	public IronLegends() 
 	{
 		super(SCREEN_WIDTH, SCREEN_HEIGHT, false);
 		gameframe.setTitle("Iron Legends");
 
 		setWorldBounds(0,0, WORLD_WIDTH, WORLD_HEIGHT);
 		
		m_sInstallDir 	= InstallInfo.getInstallDir(RESOURCE_ROOT + "IronLegends.class", "IronLegends.jar");
 		
 		m_levelProgress = new LevelProgress();
 		m_gameProgress 	= new GameProgress(m_levelProgress);
 		m_rr 			= new ResourceReader(m_sInstallDir);
 		m_sfx 			= new SoundFx();
 		m_playerInfo    = new PlayerInfo("Mitko");
 		
 		loadResources();
 		
 		// create persons polygon factory 
 		m_polygonFactory = null;
 
 		//*
 		for (Iterator<PolygonFactory> f = ServiceRegistry.lookupProviders(PolygonFactory.class); f.hasNext();) 
 		{
 			m_polygonFactory = f.next();
 			if (m_polygonFactory.getClass().getName().equals("PersonsFactory"))
 				break;
 		}
 		if (m_polygonFactory == null)
 			return;
 		//*/
 		
 		m_highScorePersist = new HighScorePersistance(m_sInstallDir);
 		m_highScorePersist.load(m_highScore);		
 				
 		// SCREENS
 		m_screens.addScreen(new GameScreen(SPLASH_SCREEN));
 		m_screens.addScreen(new GameScreen(GAMEPLAY_SCREEN));
 		m_screens.addScreen(new GameScreen(HELP_SCREEN));
 		m_screens.addScreen(new GameScreen(GAMEWON_SCREEN));
 		//m_screens.addScreen(new MPGameScreen(LEVELCOMPLETE_SCREEN));
 		m_screens.addScreen(new CustomizePlayerGS(CUSTOMIZEPLAYER_SCREEN, m_playerInfo));
 		
 		GameScreen gameplayScreen = m_screens.getScreen(GAMEPLAY_SCREEN);
 		
 		// HELP Screen
 		{
 			GameScreen helpScreen = m_screens.getScreen(HELP_SCREEN);
 
 			BodyLayer<VanillaAARectangle> bgTileLayer = new StaticBodyLayer.NoUpdate<VanillaAARectangle>();
 			
 			BgTileGenerator tileGenerator = new BgTileGenerator();
 			tileGenerator.Tile(bgTileLayer, IronLegends.SPRITE_SHEET + "#testTile2"
 					, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, TILE_WIDTH,TILE_HEIGHT);
 			
 			helpScreen.addViewableLayer(bgTileLayer);
 			CustomizePlayerGS customizePlayerScreen = (CustomizePlayerGS)m_screens.getScreen(CUSTOMIZEPLAYER_SCREEN);
 			
 			customizePlayerScreen.addViewableLayer(bgTileLayer);
 			customizePlayerScreen.setTextLayer(new CustomizePlayerTextLayer(m_fonts, m_gameProgress));
 			customizePlayerScreen.addViewableLayer(customizePlayerScreen.m_textLayer);
 			
 			helpScreen.addViewableLayer(new HelpTextLayer(m_fonts));
 		}
 		// SPLASH Screen
 		{
 			VanillaAARectangle splashBg = new VanillaAARectangle(RESOURCE_ROOT + "hr-splash.png") 
 			{
 				@Override
 				public void update(long deltaMs) 
 				{
 					// TODO Auto-generated method stub
 				}
 			};
 			
 			splashBg.setPosition(new Vector2D(0, 0));
 			
 			// add splash bg to both splash screen and gameplay screen
 			GameScreen splashScreen = m_screens.getScreen(SPLASH_SCREEN);
 			GameScreen gameWonScreen = m_screens.getScreen(GAMEWON_SCREEN);
 			{
 				BodyLayer<Body> splashBgLayer = new StaticBodyLayer.NoUpdate<Body>();
 
 				splashBgLayer.add(splashBg);
 		
 				splashScreen.addViewableLayer(splashBgLayer);
 				splashScreen.addViewableLayer(new SplashTextLayer(m_fonts, m_highScore, m_playerInfo));
 			}
 			{
 				BodyLayer<Body> bgLayer = new AbstractBodyLayer.NoUpdate<Body>();
 
 				bgLayer.add(splashBg);
 				gameWonScreen.addViewableLayer(bgLayer);				
 
 				gameWonScreen.addViewableLayer(new GameWonTextLayer(m_fonts, m_gameProgress));
 			}
 			{
 				/*
 				BodyLayer<Body> gameBgLayer = new StaticBodyLayer.NoUpdate<Body>();
 				gameBgLayer.add(splashBg);
 				
 				gameplayScreen.addViewableLayer(gameBgLayer);
 				*/
 			}
 		}
 	
 
 		// GAME OBJECTS
 		m_physicsEngine = new VanillaPhysicsEngine();
 
 		m_mitko = new Mitko(m_polygonFactory.createRectangle(new Vector2D(40,WORLD_HEIGHT-22), Mitko.WIDTH,Mitko.HEIGHT));
 		/*
 		{
 			PaintableCanvas c1  = new PaintableCanvas((int)Mitko.WIDTH, (int)Mitko.HEIGHT, 1, new Color(128,128,128));
 			
 			c1.setWorkingFrame(0);
 			c1.fillRectangle(0,0, (int)Mitko.WIDTH, (int)Mitko.HEIGHT, new Color(0,255,0));
 			//c1.fillRectangle(0,0, 1, (int)Mitko.HEIGHT, new Color(128,128,128));
 			//c1.fillRectangle(0,0, 1, (int)Mitko.HEIGHT, new Color(128,128,128));
 			c1.loadFrames("mitkoPoly");
 			
 			m_mPos = new VanillaAARectangle("mitkoPoly") {
 				
 				@Override
 				public void update(long deltaMs) {
 					// TODO Auto-generated method stub
 					
 				}
 			};
 		}
 		*/
 
 		// could be moved below to "creating level" section
 		m_weedLayer = new AbstractBodyLayer.NoUpdate<Body>();
 
 		m_hedgeLayer= new AbstractBodyLayer.NoUpdate<Body>();
 		m_bgLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		
 		m_powerUpLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 
 		m_antLayer	= new AbstractBodyLayer.NoUpdate<Body>();
 		m_spiderLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		m_batLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		
 		m_batCaveLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 		m_creatures = new AbstractBodyLayer.NoUpdate<Body>();
 		
 		m_grid 		= new LevelGrid(TILE_WIDTH, TILE_HEIGHT);
 		m_navigator = new Navigator(m_grid);
 		
 		m_mitkoLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		m_mitkoLayer.add(m_mitko);
 
 		// collect 25% weeds -> make powerup active
 		gameplayScreen.addViewableLayer(m_bgLayer);
 		gameplayScreen.addViewableLayer(m_hedgeLayer);
 		gameplayScreen.addViewableLayer(m_batCaveLayer);
 		gameplayScreen.addViewableLayer(m_weedLayer);
 		gameplayScreen.addViewableLayer(m_powerUpLayer);
 		gameplayScreen.addViewableLayer(m_antLayer);
 		gameplayScreen.addViewableLayer(m_spiderLayer);
 		gameplayScreen.addViewableLayer(m_batLayer);
 		gameplayScreen.addViewableLayer(m_mitkoLayer);
 		
 		gameplayScreen.addViewableLayer(new GameInfoTextLayer(m_fonts, m_gameProgress, m_mitko, m_highScore));
 		
 		// gameover screen has all the layers of gameplay except the text layer is different
 		{
 			Iterator<ViewableLayer> iter = gameplayScreen.getViewableLayers();
 			GameScreen gameOverScreen = new GameScreen(GAMEOVER_SCREEN);
 			while (iter.hasNext())
 			{
 				gameOverScreen.addViewableLayer(iter.next());				
 			}
 			gameOverScreen.addViewableLayer(new GameOverTextLayer(m_fonts, m_gameProgress));
 			m_screens.addScreen(gameOverScreen);
 		}
 		
 		gameplayScreen.addViewableLayer(new GamePlayTextLayer(m_fonts, m_gameProgress, m_playerInfo));
 
 		// start with splash screen
 		m_screens.addTransition(SPLASH_SCREEN, GAMEPLAY_SCREEN, "enter");
 		m_screens.addTransition(GAMEOVER_SCREEN, SPLASH_SCREEN, "enter");
 		m_screens.addTransition(SPLASH_SCREEN, HELP_SCREEN, "F1");
 		m_screens.addTransition(SPLASH_SCREEN, HELP_SCREEN, "h" );
 		m_screens.addTransition(HELP_SCREEN, SPLASH_SCREEN, "enter");
 		m_screens.addTransition(HELP_SCREEN, SPLASH_SCREEN, "esc");
 		m_screens.addTransition(GAMEWON_SCREEN, SPLASH_SCREEN, "enter");
 		m_screens.addTransition(SPLASH_SCREEN, CUSTOMIZEPLAYER_SCREEN, "c");
 		m_screens.addTransition(CUSTOMIZEPLAYER_SCREEN, SPLASH_SCREEN, "enter");
 		m_screens.addTransition(CUSTOMIZEPLAYER_SCREEN, SPLASH_SCREEN, "esc");
 		m_screens.setActiveScreen(SPLASH_SCREEN);		
 		
 		// Configure Commands
 		/*
 		keyCmds.addCommand("left", KeyEvent.VK_J);
 		keyCmds.addCommand("right", KeyEvent.VK_L);
 		keyCmds.addCommand("up", KeyEvent.VK_I);
 		keyCmds.addCommand("down", KeyEvent.VK_K);
 		*/
 		m_keyCmds.addCommand("left", KeyEvent.VK_LEFT);
 		m_keyCmds.addCommand("right", KeyEvent.VK_RIGHT);
 		m_keyCmds.addCommand("up", KeyEvent.VK_UP);
 		m_keyCmds.addCommand("down", KeyEvent.VK_DOWN);
 		
 		m_keyCmds.addCommand("space", KeyEvent.VK_SPACE);
 		m_keyCmds.addCommand("pause", KeyEvent.VK_P);
 		m_keyCmds.addCommand("enter", KeyEvent.VK_ENTER);
 		
 		m_keyCmds.addCommand("h", KeyEvent.VK_H);
 		m_keyCmds.addCommand("F1", KeyEvent.VK_F1);
 		m_keyCmds.addCommand("esc", KeyEvent.VK_ESCAPE);
 		m_keyCmds.addCommand("backspace", KeyEvent.VK_BACK_SPACE);
 		// cheat codes
 		m_keyCmds.addCommand("splat", KeyEvent.VK_8);
 		m_keyCmds.addCommand("faint", KeyEvent.VK_F);
 		m_keyCmds.addCommand("weedsCollected", KeyEvent.VK_W);
 			
     	m_keyCmds.addCommand("smoke", KeyEvent.VK_S);
 		m_keyCmds.addCommand("a", KeyEvent.VK_A);
 		m_keyCmds.addCommand("b", KeyEvent.VK_B);
 		m_keyCmds.addCommand("c", KeyEvent.VK_C);
 		m_keyCmds.addCommand("d", KeyEvent.VK_D);
 		m_keyCmds.addCommand("e", KeyEvent.VK_E);
 		m_keyCmds.addCommand("f", KeyEvent.VK_F);
 		m_keyCmds.addCommand("g", KeyEvent.VK_G);
 		m_keyCmds.addCommand("h", KeyEvent.VK_H);
 		m_keyCmds.addCommand("i", KeyEvent.VK_I);
 		m_keyCmds.addCommand("j", KeyEvent.VK_J);
 		m_keyCmds.addCommand("k", KeyEvent.VK_K);
 		m_keyCmds.addCommand("l", KeyEvent.VK_L);
 		m_keyCmds.addCommand("m", KeyEvent.VK_M);
 		m_keyCmds.addCommand("n", KeyEvent.VK_N);
 		m_keyCmds.addCommand("o", KeyEvent.VK_O);
 		m_keyCmds.addCommand("p", KeyEvent.VK_P);
 		m_keyCmds.addCommand("q", KeyEvent.VK_Q);
 		m_keyCmds.addCommand("r", KeyEvent.VK_R);
 		m_keyCmds.addCommand("s", KeyEvent.VK_S);
 		m_keyCmds.addCommand("t", KeyEvent.VK_T);
 		m_keyCmds.addCommand("u", KeyEvent.VK_U);
 		m_keyCmds.addCommand("v", KeyEvent.VK_V);
 		m_keyCmds.addCommand("w", KeyEvent.VK_W);
 		m_keyCmds.addCommand("x", KeyEvent.VK_X);
 		m_keyCmds.addCommand("y", KeyEvent.VK_Y);
 		m_keyCmds.addCommand("z", KeyEvent.VK_Z);
 		
 		
 		// button test
 		// TODO put collection of buttons as part of each GameScreen?..
 		m_btnTest = new Button(200, 100, IronLegends.SPRITE_SHEET + "#powerup", -1);
 		
 		loadLevel(m_gameProgress.getCurLevel());
 	}
 	
 	protected Button m_btnTest;
 
 	
 	protected boolean loadResources()
 	{
 		boolean bSuccess = true;
 		ResourceFactory resourceFactory = ResourceFactory.getFactory();
 
 		resourceFactory.loadResources(RESOURCE_ROOT, MY_RESOURCES);
 
 		// FONTS
 		m_fonts.create(resourceFactory);
 
 		// AUDIO
 		// works within IDE using MP3, but when package as executable jar, must
 		// be a wav file
 		String audioRoot = RESOURCE_ROOT;
 		//m_audioBallBrick2 = resourceFactory.getAudioClip(audioRoot + "hr-ballBrick2.wav");
 		//m_audioBallBrick3 = resourceFactory.getAudioClip(audioRoot + "hr-ballBrick3.wav");
 		
 		m_sfx.addSfx("faint1", resourceFactory.getAudioClip(audioRoot + "hr-sfx-AyeYay.wav"));
 		m_sfx.addSfx("faint2", resourceFactory.getAudioClip(audioRoot + "hr-sfx-CreatureMeal.wav"));
 		
 		m_sfx.addSfx("weedPulled1", resourceFactory.getAudioClip(audioRoot + "hr-sfx-PickingWeed.wav"));
 		
 		m_sfx.addSfx("collectPowerup1", resourceFactory.getAudioClip(audioRoot + "hr-sfx-CollectPowerUp.wav"));
 		
 		m_sfx.addSfx("powerup1", resourceFactory.getAudioClip(audioRoot + "hr-sfx-Courage.wav"));
 		
 		m_sfx.addSfx("trapCreature1", resourceFactory.getAudioClip(audioRoot + "hr-sfx-Gotcha.wav"));
 		m_sfx.addSfx("trapCreature2", resourceFactory.getAudioClip(audioRoot + "hr-sfx-CreatureSmash.wav"));
 		
 		m_sfx.addSfx("smoke1", resourceFactory.getAudioClip(audioRoot + "hr-sfx-YumYum.wav"));
 		
 		return bSuccess;
 	}
 	
 	protected boolean loadLevel(int level)
 	{
 		m_grid.clear();
 		m_hedgeLayer.clear();
 		m_bgLayer.clear();
 		m_batCaveLayer.clear();
 		m_weedLayer.clear();
 		m_antLayer.clear();
 		m_spiderLayer.clear();
 		m_batLayer.clear();
 		m_powerUpLayer.clear();
 		m_creatures.clear();
 		
 		m_levelProgress.reset();
 		
 		if (!LevelLoader.loadGrid(level, m_grid, m_rr))
 		{
 			// TODO: won the game?... or error. for now "won" the game
 			int totalScore = m_gameProgress.gameOver(); 
 			if (totalScore > m_highScore.getHighScore())
 			{
 				m_highScore.setHighScore(totalScore);
 				m_highScore.setPlayer(m_playerInfo.getName());
 				m_highScorePersist.save(m_highScore);
 			}	
 			m_screens.setActiveScreen(GAMEWON_SCREEN);
 			populateGameLayers();
 			return true;
 		}		
 		 
 		if (!LevelLoader.populate(level, m_grid, m_mitko, m_polygonFactory
 				, m_hedgeLayer, m_bgLayer
 				, m_weedLayer
 				, m_antLayer, m_spiderLayer, m_batLayer, m_powerUpLayer
 				, m_batCaveLayer, m_creatures))
 		{
 			return  false;
 		}
 		
 		int countWeeds = m_weedLayer.size();
 		// for now require all weeds to be collected before finishing the level
 		m_levelProgress.setWeedsRequired(countWeeds);
 		populateGameLayers();
 		m_levelProgress.setIntro(2999);
 		m_bFirstLevelUpdate = false; 
 		return true;
 	}
 	
 	protected boolean advanceLevel() 
 	{
 		m_mitko.reset();
 		return loadLevel(m_gameProgress.advanceLevel());
 	}
 
 	/**
 	 * based on which state game is in, 
 	 * this should be called after resources have been created
 	 * 	including audio/graphic resources as well as level objects
 	 */
 	protected void populateGameLayers() 
 	{
 		gameObjectLayers.clear();
 		m_physicsEngine.clear();
 
 		Iterator<ViewableLayer> layerIterator = null;
 		
 		// TODO: switch active screen to numbers instead of names!
 		layerIterator = m_screens.getScreen(m_screens.activeScreen()).getViewableLayers();			
 		
 		if (layerIterator != null)
 		{
 			while (layerIterator.hasNext()) {
 				gameObjectLayers.add(layerIterator.next());
 			}
 		}
 
 		// configure physics engine to handle updates only if 
 		// in state gameplay and not game over
 		int curScreen = m_screens.activeScreen();
 		switch (curScreen)
 		{
 			case SPLASH_SCREEN:
 			case GAMEOVER_SCREEN:
 			case HELP_SCREEN:
 			case GAMEWON_SCREEN:
 			case CUSTOMIZEPLAYER_SCREEN:
 			break;
 			case GAMEPLAY_SCREEN:
 			default:
 				m_physicsEngine.manageViewableSet(m_mitkoLayer);
 				m_physicsEngine.manageViewableSet(m_antLayer);
 				m_physicsEngine.manageViewableSet(m_spiderLayer);
 				m_physicsEngine.manageViewableSet(m_batLayer);
 				m_physicsEngine.manageViewableSet(m_weedLayer);
 				//m_physicsEngine.manageViewableSet(m_hedgeLayer);
 				
 				// don't hit the hedges
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPB_BodyLayer(m_mitko, m_hedgeLayer, m_polygonFactory, Tile.WIDTH, Tile.HEIGHT, null));
 				
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPBLayer_BodyLayer(m_polygonFactory, m_antLayer, m_hedgeLayer, Tile.WIDTH, Tile.HEIGHT
 									, new CollisionSink_CreatureHedge(m_grid)));
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPBLayer_BodyLayer(m_polygonFactory, m_spiderLayer, m_hedgeLayer, Tile.WIDTH, Tile.HEIGHT
 									, new CollisionSink_CreatureHedge(m_grid)));
 				// power-up
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPB_BodyLayer(m_mitko, m_powerUpLayer, m_polygonFactory, PowerUp.WIDTH, PowerUp.HEIGHT
 								, new CollisionSink_PowerUp(m_levelProgress, m_sfx)));
 				
 				// mitko-creatures
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPB_CPBLayer(m_mitko, m_antLayer
 								, new CollisionSink_Creature(m_levelProgress, m_sfx)));
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPB_CPBLayer(m_mitko, m_spiderLayer
 								, new CollisionSink_Creature(m_levelProgress, m_sfx)));
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPB_CPBLayer(m_mitko, m_batLayer
 								, new CollisionSink_Creature(m_levelProgress, m_sfx)));
 				// mitko-weeds
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPB_BodyLayer(m_mitko, m_weedLayer, m_polygonFactory, Weed.WIDTH, Weed.HEIGHT
 								, new CollisionSink_Weed(m_levelProgress, m_sfx)));
 				// bat-creatures
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPBLayer_CPBLayer(m_batLayer, m_antLayer
 								, new CollisionSink_BatCreature(m_levelProgress, m_sfx)));
 				m_physicsEngine.registerCollisionHandler(
 						new Handler_CPBLayer_CPBLayer(m_batLayer, m_spiderLayer
 								, new CollisionSink_BatCreature(m_levelProgress, m_sfx)));
 				/*
 				 collision resolution in following order
 				 mitko - hedge
 				 creature - hedge
 				 mitko - creatures
 				 mitko - weeds/powerups
 				 
 				 // test in following order
 				 mitko - hedge
 				 mitko - weeds/powerups
 				 mitko - creatures
 				 */
 			break;
 		}
 	}
 
 	protected void newGame() 
 	{
 		m_bGameOver = false;
 
 		m_gameProgress.reset();
 		m_mitko.newGame();
 		loadLevel(m_gameProgress.getCurLevel());
 	}
 	
 	protected KeyCommands m_keyCmds = new KeyCommands();
 	protected boolean m_paused = false;
 	
 
 	protected void processCommands(final long deltaMs)
 	{
 		m_keyCmds.update(keyboard);
 		m_btnTest.update(mouse, deltaMs);
 		if (m_btnTest.wasLeftClicked())
 			System.out.println("button: " + m_btnTest.getId() + " was left clicked");
 		
 		//mouse.getLocation();
 		//if (mouse.isLeftButtonPressed())
 		//	System.out.println("LeftButtonDown: " + mouse.getLocation().x + "," + mouse.getLocation().y);
 
 		if (m_levelProgress.isExitActivated())
 		{
 			if (m_keyCmds.wasPressed("enter"))
 			{
 				m_levelProgress.setExitComplete(true);
 			}
 		}
 		
 		GameScreen curScreen = m_screens.getActiveScreen();
 		curScreen.processInput(m_keyCmds);
 		
 		if (m_keyCmds.wasPressed("weedsCollected"))
 		{
 			while (m_levelProgress.getWeedsRemaining() > 0)
 				m_levelProgress.addWeedCollected();
 		}
 		
 		if (m_keyCmds.wasPressed("splat")){
 			// hack to start new level
 			advanceLevel();
 			return;
 		}
 		if (m_screens.activeScreen() == GAMEPLAY_SCREEN)
 		{
 			if (m_keyCmds.wasPressed("faint"))
 			{
 				m_mitko.activateFaint();
 			}
 		}
 
 		ScreenTransition t = m_screens.transition(m_keyCmds);
 		if (t != null)
 		{
 			GameScreen newScreen = m_screens.getActiveScreen();
 			// TODO: create handlers for transition so game can be modified appropriately
 			// e.g. no need to test what the transition is to execute newGame
 			if (t.m_to == GAMEPLAY_SCREEN && t.m_from == SPLASH_SCREEN)
 			{
 				newGame();
 			}
 			else if (t.m_to == SPLASH_SCREEN && t.m_from == GAMEOVER_SCREEN)
 			{
 				m_mitko.reset();
 				populateGameLayers();
 			}
 			else
 			{
 				populateGameLayers();
 			}
 			curScreen.deactivate();
 			newScreen.activate();
 		}
 		
 		if (m_keyCmds.wasPressed("pause")){
 			if (m_paused) {
 				//m_ball.setSpeed(m_ballSlowSpeed);
 			} else {
 				//m_ball.setSpeed(m_ballSlowSpeed / 100.0);
 			}
 			m_paused = !m_paused;
 			System.out.println("");
 		}
 
 		/*
 		if (m_keyCmds.wasPressed("space")){
 			if (m_ballSlow)
 			{
 				m_ball.setSpeed(m_ballNormalSpeed);
 			}
 			else
 			{
 				m_ball.setSpeed(m_ballSlowSpeed);
 			}
 			m_ballSlow = !m_ballSlow;
 		}
 		*/
 	}
 	
 	protected void move(final long deltaMs) 
 	{
 		
 		//processCommands(deltaMs);
 		
 		boolean left 	= m_keyCmds.isPressed("left");
 		boolean right	= m_keyCmds.isPressed("right");
 		boolean up	 	= m_keyCmds.isPressed("up");
 		boolean down 	= m_keyCmds.isPressed("down");
 		boolean smoke	= m_keyCmds.wasPressed("smoke");
 		if (smoke && m_mitko.getStoredPowerUps() > 0)
 		{
 			m_mitko.smoke();
 			m_sfx.play("smoke1");
 		}
 		
 		m_mitko.move(left, right, up, down, deltaMs);
 	}
 
 	boolean m_bFirstLevelUpdate = false;
 	
 	static public Vector2D bodyPosToPolyPos(int w, int h, Vector2D pos)
 	{
 		double r = Math.sqrt(w*w + h*h)/2;
 		double deltaX = r - w/2;
 		double deltaY = r - h/2;
 		
 		return new Vector2D(pos.getX()-deltaX, pos.getY()-deltaY);
 	}
 
 	@Override
 	public void update(final long deltaMs) 
 	{
 		processCommands(deltaMs);
 		
 		if (m_levelProgress.isExitActivated())
 		{
 			super.update(deltaMs);
 			processCommands(deltaMs);
 			if (m_levelProgress.isExitComplete())
 				advanceLevel();
 			//m_levelProgress.update(deltaMs);
 			return;
 		}
 		if (!m_levelProgress.isIntro()  )
 		{
 			super.update(deltaMs);
 		}
 		else if (!m_bFirstLevelUpdate)
 		{
 			super.update(deltaMs);
 			m_bFirstLevelUpdate = true;
 		}
 		
 
 		int curScreen = m_screens.activeScreen();
 		
 		switch (curScreen)
 		{
 			case SPLASH_SCREEN:
 			break;
 			case GAMEOVER_SCREEN:
 				m_physicsEngine.applyLawsOfPhysics(deltaMs);
 			break;
 			case GAMEPLAY_SCREEN:
 				if (m_levelProgress.isIntro())
 				{
 					m_levelProgress.update(deltaMs);
 				}
 				else if (m_levelProgress.isExitActivated())
 				{
 					m_levelProgress.update(deltaMs);
 				}
 				else
 				{
 					m_physicsEngine.applyLawsOfPhysics(deltaMs);
 					//System.out.println("delta ms: " + deltaMs);
 					//m_physicsEngine.applyLawsOfPhysics(5);
 				}
 
 				if (m_mitko.isFainting() 
 						&& m_mitko.doneFainting() 
 						&& m_screens.activeScreen() != GAMEOVER_SCREEN)
 				{
 					if (m_gameProgress.getFaintsRemaining() == 0)
 					{
 						m_screens.setActiveScreen(GAMEOVER_SCREEN);
 						
 						int totalScore = m_gameProgress.gameOver(); 
 						if (totalScore > m_highScore.getHighScore())
 						{
 							m_highScore.setHighScore(totalScore);
 							m_highScore.setPlayer(m_playerInfo.getName());
 							m_highScorePersist.save(m_highScore);
 						}	
 					}
 					else
 					{
 						m_gameProgress.mitkoFainted();
 						m_mitko.reset();
 					}
 					
 					// TODO: update level info.. faints remaining etc
 					// if faints still remaining, then reset
 					// otherwise ..... game over
 					// must populate screen
 					populateGameLayers();			
 				}
 
 				if (m_levelProgress.isLevelComplete()) 
 				{
 					if (!m_levelProgress.isExitActivated())
 					{
 						//m_levelProgress.setExit(3000);
 						m_levelProgress.setExit(true);
 					}
 					else
 					{					
 						// all bricks hit, start new level
 						//advanceLevel();
 					}
 				}
 
 			break;
 		}
 
 		move(deltaMs);
 		{
 			Iterator<Body> iter =  m_antLayer.iterator();
 			while (iter.hasNext())
 			{
 				Ant a = (Ant)iter.next();
 				double dLastChangeMs = a.getLastMoveChange();
 				if (dLastChangeMs > 2000)
 				{
 					if (m_navigator.selectOption(a))
 						a.resetMoveChange();
 				}
 			}
 		}
 		{
 			Iterator<Body> iter =  m_batLayer.iterator();
 			while (iter.hasNext())
 			{
 				Bat a = (Bat)iter.next();
 				double dLastChangeMs = a.getLastMoveChange();
 				if (dLastChangeMs > 4000)
 				{
 					if (m_navigator.selectRandom(a))
 						a.resetMoveChange();
 				}
 			}
 		}
 		// don't center if would cause scrolling past "edge" of screen
 		{
 			//Vector2D pos = m_mitko.getCenterPosition();
 			// translate to screen, if pos + world 
 			centerOn(m_mitko);
 		}
 	}
 
 	protected Navigator m_navigator;
 		
 	@Override
 	public void render(final RenderingContext rc) 
 	{
 		if (rc != null)
 		{
 			super.render(rc);
 			m_btnTest.render(rc);			
 		}
 		
 		//Area gameInfoArea = new Area(new Rectangle2D.Float(600, 0, 200, 600));
 		//Color clear = new Color(128,128,128);
 		//m_side.drawArea(gameInfoArea, clear);
 		// get polygon
 		/*
 		Vector2D pos = m_mitko.getPosition();
 		ConvexPolygon p = m_mitko.getShape();
 		int x = 0;
 		int y = 0;
 		boolean bDone = false;
 		for (x = (int)pos.getX(); !bDone && x < m_mitko.getWidth()+pos.getX(); x++) {
 			for (y = (int)pos.getY(); !bDone && y < m_mitko.getHeight()+pos.getY(); y++) {
 				if (p.contains(x, y)) 
 				{
 					bDone = true;
 					break;
 					
 				}
 			}
 			if (bDone)
 				break;
 		}
 		
 		//m_mPos.setPosition(new Vector2D(pos.getX()+x, pos.getY()+y));
 		m_mPos.setPosition(new Vector2D(x, y));
 		m_mPos.render(rc);
 		*/
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) 
 	{
 		IronLegends game = new IronLegends();
 		game.run();
 	}
 }
