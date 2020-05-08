 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.*;
 
 import java.util.ArrayList;
 
 import org.jbox2d.callbacks.DebugDraw;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 import playn.core.CanvasImage;
 import playn.core.Color;
 import playn.core.DebugDrawBox2D;
 import playn.core.Font;
 import playn.core.Game;
 import playn.core.GroupLayer;
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.Layer;
 import playn.core.Sound;
 import playn.core.TextFormat;
 import playn.core.TextFormat.Alignment;
 import playn.core.TextLayout;
 
 public class TinyWorld implements Game {
 
 	ArrayList<Asteroid> planetoids;
 	ArrayList<Body> destroyList;
 
 	Player player;
 	World world;
 
 	DynamicFactory factory;
 	StarFactory starFactory;
 
 	private static final float nanoToSecs = 1E9f;
 
 	private static final int spawnIntervalSecs = 1;
 	private long lastSpawnTime;
 
 	private static final boolean debugPhysics = false;
 
 	// MusicPlayer music;
 
 	private KeyboardInput keyboard;
 	private MouseInput mouse;
 
 	private ContactListener contactListner;
 
 	private ImageLayer debugLayer;
 
 	private DebugDrawBox2D debugDraw;
 	private CanvasImage canv;
 
 	private static GroupLayer planetoidLayer;
 	Image planetoidImage;
 
 	boolean createAstr;
 
 	Menus menus;
 
 	Sound clickSound;
 
 	// FPS
 	int frameCount;
 	int fps;
 	long oldTime;
 	private static final boolean showFps = false;
 
 	private TextHandler fpsHandler;
 	private TextHandler atmosphereHandler;
 	private TextHandler massHandler;
 	private TextHandler heatHandler;
 
 	@Override
 	public void init() {
 		menus = new Menus();
 
 		keyboard = new KeyboardInput(this);
 		mouse = new MouseInput(this);
 
 		createAstr = false;
 
 		Globals.globalScale = 1.0f;
 
 		// Set up FPS
 		frameCount = 0;
 		fps = 0;
 		oldTime = System.nanoTime();
 		lastSpawnTime = System.nanoTime();
 
 		planetoidLayer = graphics().createGroupLayer();
 
 		planetoidImage = assets().getImage("images/planetoid.png");
 
 		Globals.state = Globals.STATE_MENU;
 
 		// create and add background image layer
 		Image bgImage = assets().getImage("images/starfield.png");
 		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
 		graphics().rootLayer().add(bgLayer);
 
 		graphics().setSize(1024, 768);
 
 		// music = new MusicPlayer();
 		// music.add("music/e");
 		// music.start();
 
 		menus.menuInit();
 
 		if (showFps) {
 			initFPSCounter();
 		}
 	}
 
 	public void gameInit() {
 		Globals.state = Globals.STATE_GAME;
 		graphics().rootLayer().remove(menus.menuLayer);
 
 		planetoids = new ArrayList<Asteroid>();
 		destroyList = new ArrayList<Body>();
 		planetoidLayer = graphics().createGroupLayer();
 
 		Image sunImage = assets().getImage("images/sun.png");
 
 		// Set up the world
 		world = new World(new Vec2(), false);
 
 		// Set up the factory and Asteroids
 		factory = new DynamicFactory(world, planetoidLayer);
 		starFactory = new StarFactory(world, sunImage, planetoidLayer);
 
 		factory.createDebris(5);
 
 		for (int i = 0; i < 1; i++) {
 			starFactory.getStar(new Vec2(0, 0));
 		}
 
 		// Set up player
 		Vec2 playerStart = new Vec2(graphics().width() / 2,
 				graphics().height() / 2);
 
 		BodyDef playerBodyDef = new BodyDef();
 		playerBodyDef.type = BodyType.DYNAMIC;
 
 		playerBodyDef.position.set(playerStart.mul(1 / Globals.PHYS_RATIO));
 
 		player = new Player(new Sprite((int) playerStart.x,
 				(int) playerStart.y, planetoidImage), playerBodyDef, world);
 		planetoidLayer.add(player.getSprite().getImageLayer());
 		graphics().rootLayer().add(planetoidLayer);
 
 		contactListner = new ContactListener(player, factory);
 		world.setContactListener(contactListner);
 
 		setScale(2.0f);
 
 		if (debugPhysics) {
 			debugInit();
 		}
 		
 		initHeatCounter();
 		initAtmosphereCounter();
 		initMassCounter();
 		
 		if (showFps) {
 			initFPSCounter();
 		}
 	}
 
 	public void debugInit() {
 		// Debug stuff
 		debugDraw = new DebugDrawBox2D();
 
 		int scaleCanvasSize = 1;
 		canv = graphics().createImage(graphics().width() * scaleCanvasSize,
 				graphics().height() * scaleCanvasSize);
 		debugDraw.setCanvas(canv);
 		debugDraw.setFlipY(false);
 		debugDraw.setStrokeAlpha(100);
 		debugDraw.setFillAlpha(50);
 		debugDraw.setStrokeWidth(1.0f);
 		debugDraw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit);
 		debugDraw.setCamera(0, 0, 1f);
 
 		world.setDebugDraw(this.debugDraw);
 
 		debugLayer = graphics().createImageLayer(canv);
 		debugLayer.setTranslation(Globals.PHYS_RATIO, Globals.PHYS_RATIO);
 		graphics().rootLayer().add(debugLayer);
 	}
 
 	public void setScale(float scale) {
 		Globals.globalScale = 1 / scale;
 		player.getSprite().setScale(scale);
 		planetoidLayer.setScale(Globals.globalScale);
 	}
 
 	@Override
 	public void paint(float alpha) {
 		// the background automatically paints itself, so no need to do anything
 		// here!
 		if (Globals.state == Globals.STATE_GAME) {
 			if (debugPhysics) {
 				canv.canvas().clear();
 				world.drawDebugData();
 			}
 
 			if (showFps) {
 				// Calculate FPS
 				frameCount++;
 				if (frameCount > 50) {
 					float curTime = System.nanoTime();
 
 					float dTime = (curTime - oldTime) / nanoToSecs;
 
 					oldTime = (long) curTime;
 					fps = (int) (frameCount / dTime);
 					frameCount = 0;
 
 					fpsHandler.setText("Atmosphere: " + fps);
 					fpsHandler.update();
 				}
 			}
 
 			// Update the atmosphere value on screen
 			atmosphereHandler.setText("A: " + player.getAtmosphere());
 			atmosphereHandler.update();
 
 			// Update the mass value on screen
 			massHandler.setText("M: " + player.getMass());
 			massHandler.update();
 
 			// Update the heat value on screen
			heatHandler.setText("H: " + (int) player.getHeat());
 			heatHandler.update();
 		}
 	}
 
 	@Override
 	public void update(float delta) {
 
 		// music.update();
 
 		// MENU STATE
 		if (Globals.state == Globals.STATE_MENU
 				|| Globals.state == Globals.STATE_CREDITS) {
 			// X position is the middle of the screen subtract half the width of
 			// the title
 			float posX = (graphics().width() / 2)
 					- (menus.menuItemLayers.get(Globals.currentItem).layout
 							.width() / 2);
 			float posY = menus.menuItemLayers.get(Globals.currentItem).posY;
 
 			// Increase the value of menuSin until it reaches 360, then reset
 			if (Globals.menuSin > 360) {
 				Globals.menuSin = 0;
 			} else {
 				Globals.menuSin = Globals.menuSin + 0.1f;
 			}
 
 			menus.menuLayer.get(Globals.currentItem + 1).setTranslation(posX,
 					(float) (posY + Math.sin(Globals.menuSin) * 10));
 		}
 
 		// GAME STATE
 		else if (Globals.state == Globals.STATE_GAME) {
 			// Bounds for atmosphere value
 			if (player.getAtmosphere() > 100) {
 				player.setAtmosphere(100);
 			} else if (player.getAtmosphere() < 0) {
 				player.setAtmosphere(0);
 			}
 
 			// setScale(player.getMass());
 
 			// Values need playing with, and to be stored
 			world.step(30, 6, 3);
 			world.clearForces();
 
 			if (!keyboard.isSpaceDown()) {
 				player.applyThrust(keyboard.getMovement());
 			} else {
 				// When space is pressed, set Velocity to 0
 				player.getBody().setLinearVelocity(new Vec2());
 			}
 			
 			if (keyboard.ispKeyDown()) {
 				while(world.getBodyList() != null) {
 					factory.clearAsteroids();
 				}
 			}
 
 			planetoidGenerator();
 
 			spawnBodies();
 
 			cameraFollowPlayer();
 
 			if (debugPhysics) {
 				world.drawDebugData();
 				cameraFollowDebug();
 			}
 
 			player.update();
 			player.updateHeat();
 			
 			factory.update();
 		}
 	}
 
 	@Override
 	public int updateRate() {
 		return 25;
 	}
 
 	/**
 	 * Spawn bodies after spawnIntervale frames
 	 */
 	public void spawnBodies() {
 		long curTime = System.nanoTime();
 		long dTime = (long) ((curTime - lastSpawnTime) / nanoToSecs);
 
 		if (dTime > spawnIntervalSecs) {
 			factory.createDebris(1);
 			lastSpawnTime = curTime;
 		}
 	}
 
 	/**
 	 * Generate deleted planetoids and replace them
 	 */
 	public void planetoidGenerator() {
 
 		// Creates an asteroid if one was previously destroyed
 		// What happens if two were destroyed?
 		factory.createDebris(contactListner.getCreateCount());
 
 		contactListner.clearCreateCount();
 	}
 
 	// Translates the planetoid layer so that the player planet is always centre
 	public void cameraFollowPlayer() {
 		int tx;
 		tx = (int) (player.getBody().getWorldCenter().x * Globals.PHYS_RATIO);
 		tx = (int) (tx - graphics().width() + (player.getSprite().getWidth()));
 
 		int ty;
 		ty = (int) (player.getBody().getWorldCenter().y * Globals.PHYS_RATIO);
 		ty = (int) (ty - graphics().height() + (player.getSprite().getHeight()));
 
 		planetoidLayer.setOrigin(tx, ty);
 	}
 
 	public void cameraFollowDebug() {
 		int tx;
 		tx = (int) (player.getBody().getWorldCenter().x * Globals.PHYS_RATIO);
 		tx = (int) (tx - (graphics().width() / 2) * (1 / Globals.globalScale));
 
 		int ty;
 		ty = (int) (player.getBody().getWorldCenter().y * Globals.PHYS_RATIO);
 		ty = (int) (ty - (graphics().height() / 2) * (1 / Globals.globalScale));
 
 		// debugLayer.setOrigin(tx, ty);
 	}
 	
 	/**
 	 * Render text to show an FPS Counter
 	 */
 	public void initFPSCounter() {
 		Font textFont = graphics().createFont("Courier", Font.Style.BOLD, 12);
 
 		fpsHandler = new TextHandler("" + fps, new Vec2(20, 20), textFont,
 				Color.rgb(255, 247, 50));
 
 		graphics().rootLayer().add(fpsHandler.getTextLayer());
 	}
 
 	// Render text to show an atmosphere counter
 	public void initAtmosphereCounter() {
 		Font textFont = graphics().createFont("Courier", Font.Style.BOLD, 12);
 
 		atmosphereHandler = new TextHandler("A: " + player.getAtmosphere(),
 				new Vec2(20, 40), textFont, Color.rgb(255, 247, 50));
 
 		graphics().rootLayer().add(atmosphereHandler.getTextLayer());
 	}
 
 	// Render text to show a mass counter
 	public void initMassCounter() {
 		Font textFont = graphics().createFont("Courier", Font.Style.BOLD, 12);
 
 		massHandler = new TextHandler("M: " + player.getMass(),
 				new Vec2(20, 60), textFont, Color.rgb(255, 247, 50));
 
 		graphics().rootLayer().add(massHandler.getTextLayer());
 	}
 
 	// Render text to show a head counter
 	public void initHeatCounter() {
 		Font textFont = graphics().createFont("Courier", Font.Style.BOLD, 12);
 
 		heatHandler = new TextHandler("H: " + player.getHeat(),
 				new Vec2(20, 80), textFont, Color.rgb(255, 247, 50));
 		
 		heatHandler.getPos();
 		graphics().rootLayer().add(heatHandler.getTextLayer());
 	}
 }
