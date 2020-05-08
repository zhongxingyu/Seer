 package org.nc.engine;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class GameLoop extends Thread {
 	Game game;
 	GameCanvas canvas;
 	public static Timer timer = new Timer();
 	int frame;
 	long lastLoopTime = System.currentTimeMillis();
 
 	public GameLoop(Game game, GameCanvas canvas) {
 		this.game = game;
 		this.canvas = canvas;

 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				loop();
 			}
 		}, 0, 50);
 	}
 
 	public void loop() {
 		if (!game.isOver()) {
 			frame++;
 			game.update();
 			canvas.repaint();
 			game.debuginfo = game.stateName + " X: " + game.mouseX() + " Y: " + game.mouseY();
 			long delta = System.currentTimeMillis() - lastLoopTime;
 			lastLoopTime = System.currentTimeMillis();
 			game.deltapro = (int) delta;
 		}
 	}

	@Override
	public void run() {
		game.init();

	}
 }
