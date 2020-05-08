 package com.avona.games.towerdefence;
 
 import java.io.Serializable;
 
 import com.avona.games.towerdefence.gfx.PortableGraphicsEngine;
 import com.avona.games.towerdefence.inputActors.GameInputActor;
 import com.avona.games.towerdefence.inputActors.LayeredInputActor;
 import com.avona.games.towerdefence.inputActors.LevelEditorInputActor;
 import com.avona.games.towerdefence.inputActors.MenuInputActor;
 import com.avona.games.towerdefence.level.Level;
 
 public abstract class PortableMainLoop implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private static final float FIXED_TICK = 0.04f;
 
 	public static final String GAME_LAYER_NAME = "game";
 	public static final String MENU_LAYER_NAME = "menu";
 
 	public Game game;
 	public PortableGraphicsEngine ge;
 	public LayeredInputActor inputActor;
 	public Mouse mouse = new Mouse();
 	public LayerHerder layerHerder = new LayerHerder();
 	public EventDistributor eventListener = new EventDistributor();
 	protected TimeTrack wallTime = new TimeTrack();
 	private float gameTicks = 0;
 
 	protected void initWithGame() {
 		Layer gameLayer = new Layer();
 		gameLayer.virtualRegion.x = Level.WIDTH;
 		gameLayer.virtualRegion.y = Level.HEIGHT;
 		gameLayer.name = GAME_LAYER_NAME;
 		layerHerder.addLayer(gameLayer);
 
 		Layer menuLayer = new Layer();
 		menuLayer.virtualRegion.x = 125;
 		menuLayer.virtualRegion.y = 480;
 		menuLayer.name = MENU_LAYER_NAME;
 		layerHerder.addLayer(menuLayer);
 	}
 
 	public void setupInputActors() {
 
 		GameInputActor gameInputActor = new GameInputActor(game, mouse);
 		if (Debug.mapEditor) {
 			gameInputActor = new LevelEditorInputActor(game, mouse);
 		}
 
 		inputActor = new LayeredInputActor(this, mouse, layerHerder);
 		inputActor.inputLayerMap.put(layerHerder
 				.findLayerByName(GAME_LAYER_NAME), gameInputActor);
		inputActor.inputLayerMap.put(layerHerder
				.findLayerByName(MENU_LAYER_NAME), new MenuInputActor(game));
 	}
 
 	public static double getWallClock() {
 		return System.nanoTime() * Math.pow(10, -9);
 	}
 
 	public void performLoop() {
 		final double wallClock = getWallClock();
 		wallTime.update(wallClock);
 
 		// Updating of inputs is done asynchronously.
 		// Update the world with a fixed rate.
 		gameTicks += wallTime.tick;
 		while (gameTicks >= FIXED_TICK) {
 			game.updateWorld(FIXED_TICK);
 			gameTicks -= FIXED_TICK;
 		}
 
 		// Show the world.
 		ge.render(wallTime.tick);
 	}
 
 	public void pauseGame() {
 		game.pause();
 	}
 
 	public void unpauseGame() {
 		game.unpause();
 	}
 
 	public void toggleGamePause() {
 		if (game.isPaused()) {
 			unpauseGame();
 		} else {
 			pauseGame();
 		}
 	}
 
 	public abstract void exit();
 
 	public void serialize() {
 		// TODO: Make this abstract.
 	}
 }
