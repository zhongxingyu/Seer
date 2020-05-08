 package com.avona.games.towerdefence;
 
 public abstract class PortableMainLoop {
 
 	private static final float FIXED_TICK = 0.04f;
 
 	public Game game = new Game();
 	public PortableGraphicsEngine ge;
 	public InputActor inputActor;
 
 	protected TimeTrack gameTime = new TimeTrack();;
 	protected TimeTrack graphicsTime = new TimeTrack();
 	private float gameTicks = 0;
 
	public static float getWallClock() {
		return (float) (System.nanoTime() * Math.pow(10, -9));
 	}
 
 	public void performLoop() {
		final float wallClock = getWallClock();
 		graphicsTime.update(wallClock);
 		gameTime.update(wallClock);
 
 		// Updating of inputs is done asynchronously.
 
 		// Update the world with a fixed rate.
 		gameTicks += gameTime.tick;
 		while (gameTicks >= FIXED_TICK) {
 			game.updateWorld(FIXED_TICK);
 			gameTicks -= FIXED_TICK;
 		}
 
 		// Show the world.
 		ge.render(gameTime.tick, graphicsTime.tick);
 	}
 
 	public void pauseGame() {
 		gameTime.stopClock();
 	}
 
 	public void unpauseGame() {
 		gameTime.startClock();
 	}
 
 	public void toggleGamePause() {
 		if (gameTime.isRunning()) {
 			pauseGame();
 		} else {
 			unpauseGame();
 		}
 	}
 
 	public abstract void exit();
 
 }
