 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.*;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 import playn.core.Game;
 import playn.core.GroupLayer;
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.Keyboard;
 import playn.core.Pointer;
 import playn.core.Keyboard.Event;
 import playn.core.Keyboard.TypedEvent;
 
 public class TinyWorld implements Game, Keyboard.Listener, Pointer.Listener {
 	ArrayList<Planetoid> planetoids;
 	
 	Player player;
 	
 	World world;
 	
 	private boolean upKeyDown;
 	private boolean downKeyDown;
 	private boolean leftKeyDown;
 	private boolean rightKeyDown;
 	
 	private GroupLayer planetoidLayer;
 	// TODO remove everything from root layer
 	
 	@Override
 	public void init() {		
 		initKeyboard();
 		
 		planetoids = new ArrayList<Planetoid>();
 		planetoidLayer = graphics().createGroupLayer();
 		
 		pointer().setListener(this);
 		keyboard().setListener(this);
 	
 		// create and add background image layer
 		Image bgImage = assets().getImage("images/starfield.png");
 		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
 		graphics().rootLayer().add(bgLayer);
 		
 		// Load grey asteroid image asset
 		Image asteroid = assets().getImage("images/grey-asteroid.png");
 		
 		//Set up the world
 		world = new World(new Vec2(), false);
 		
 		for (int i=0; i < 30; i++) {
 			createAsteroid(asteroid);
 		}
 		
 		BodyDef playerBodyDef = new BodyDef();
 		playerBodyDef.type = BodyType.DYNAMIC;
		playerBodyDef.position.set(new Vec2(60,60).mul(1/Constants.PHYS_RATIO));
 		
 		//TODO Player should be in middle
		player = new Player(new Vec2(60,60),new Sprite(100,100), playerBodyDef, world);
 		planetoidLayer.add(player.getSprite().getImageLayer());
 		player.getSprite().addFrame(asteroid);
 		
 		graphics().rootLayer().add(planetoidLayer);
 	}
 
 	public void createAsteroid(Image asteroid) {
 
 		//Start Vector off screen (This should be random)
 		Vec2 astrStart = genStartPos(graphics().width(), graphics().height());
 		
 		//Set up an asteroid
 		BodyDef astrBodyDef = new BodyDef();
 		astrBodyDef.type= BodyType.DYNAMIC;
 		
 		// Create the player body definition
 		//Initial Position
 		astrBodyDef.position.set(astrStart.mul(1/Constants.PHYS_RATIO));
 		
 		Asteroid astr = new Asteroid(astrStart, new Sprite((int) astrStart.x, (int) astrStart.y), astrBodyDef, world);
 		astr.getSprite().addFrame(asteroid);
 		
 		
 		//Apply a force to the asteroid
 		Vec2 forceDir = astr.getStartDirVec(graphics().width(), graphics().height());
 		
 		/*
 		 * Generate a nice random vector by multiplying direction by random numbers 
 		 */
 		float forceFactor = 100f;
 		
 		Random rand = new Random();
 		
 		float xComp = forceDir.x * rand.nextInt((int) forceFactor) / forceFactor;
 		float yComp = forceDir.y * rand.nextInt((int) forceFactor) / forceFactor;
 		
 		//Initial Force, need to randomise
 		Vec2 thrust = new Vec2(xComp, yComp);
 		astr.applyThrust(thrust);
 		
 		planetoids.add(astr);
 		planetoidLayer.add(astr.getSprite().getImageLayer());
 	}
 	
 	
 	@Override
 	public void paint(float alpha) {
 		// the background automatically paints itself, so no need to do anything
 		// here!
 		
 	}
 
 	@Override
 	public void update(float delta) {
 		//Values need playing with, and to be stored
 		world.step(60, 6, 3);
 		world.clearForces();
 		
 		handleKeyboard();
 		cameraFollowPlayer();
 		player.update();
 		
 		// For every planetoid update it's sprite
 		for (Planetoid p : planetoids) {
 			p.update();
 		}
 	}
 
 	@Override
 	public int updateRate() {
 		return 25;
 	}
 	
 	public void cameraFollowPlayer(){	
 		int tx;
 		tx = (int) (player.getBody().getWorldCenter().x * Constants.PHYS_RATIO);
 		tx = tx - (graphics().width());
 		
 		int ty;
 		ty = (int) (player.getBody().getWorldCenter().y * Constants.PHYS_RATIO);
 		ty = ty - (graphics().height());
 		
 		planetoidLayer.setOrigin(tx,ty);
 	}
 	
 	public void initKeyboard(){
 		upKeyDown = false;
 		downKeyDown = false;
 		leftKeyDown = false;
 		rightKeyDown = false;
 	}
 	
 	public void handleKeyboard(){
 		int px, py;
 		px = py = 0;
 		
 		if(upKeyDown){
 			py = py - (int) Constants.PHYS_RATIO;
 		}
 		if(downKeyDown){
 			py = py + (int) Constants.PHYS_RATIO;
 		}
 		if(leftKeyDown){
 			px = px - (int) Constants.PHYS_RATIO;
 		}
 		if(rightKeyDown){
 			px = px + (int) Constants.PHYS_RATIO;
 		}
 		player.applyThrust(new Vec2(px,py));
 	}
 
 	@Override
 	public void onKeyDown(Event event) {
 		switch (event.key()) {
 		case W:
 			upKeyDown = true;
 			break;
 		case A:
 			leftKeyDown = true;
 			break;
 		case S:
 			downKeyDown = true;
 			break;
 		case D:
 			rightKeyDown = true;
 			break;
 		case ESCAPE:
 			System.exit(0);
 		}
 		
 	}
 
 	@Override
 	public void onKeyTyped(TypedEvent event) {
 	}
 
 	@Override
 	public void onKeyUp(Event event) {
 		// TODO Auto-generated method stub
 		switch (event.key()) {
 		case W:
 			upKeyDown = false;
 			break;
 		case A:
 			leftKeyDown = false;
 			break;
 		case S:
 			downKeyDown = false;
 			break;
 		case D:
 			rightKeyDown = false;
 			break;
 		}
 	}
 
 	@Override
 	public void onPointerStart(playn.core.Pointer.Event event) {
 	}
 
 	@Override
 	public void onPointerEnd(playn.core.Pointer.Event event) {
 	}
 
 	@Override
 	public void onPointerDrag(playn.core.Pointer.Event event) {
 	}
 	
 	public void movePlayer(){
 	}
 
 	/**
 	 * Get the starting position of an object
 	 * @param screenWidth
 	 * @param screenHeight
 	 * @return
 	 */
 	public Vec2 genStartPos(int width, int height) {
 		
 		float x = getSpawnBound(-width, width);
 		float y = getSpawnBound(-height, height);
 		
 		Vec2 pos = new Vec2(x, y);
 
 		return pos;
 	}
 	
 	/**
 	 * Randomly choose a number NOT between 0 and limit
 	 * @param max
 	 * @return
 	 */
 	private int getSpawnBound(int min, int max) {
 		
 		Random rand = new Random();
 		
 		int spawnBound = 100;
 		//Set x
 		boolean belowBound = rand.nextBoolean();
 		
 		int pos = rand.nextInt(spawnBound) + 50;
 		
 		if (belowBound) {
 			pos = -pos + min;
 		} else {
 			pos += max;
 		}
 		
 		return pos;
 	}
 }
