 /*
  * File Name: Game.java
  * Contributors:	Jonathan Bradley - 7/17/2013
  * 					Ryan Meier
  * 					Ben Emrick
  * 
  * Purpose: This file handles all of the main parts of the game
  */
 package td;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 
 import javax.swing.JFrame;
 
 import td.map.Map;
 
 public class Game extends Canvas implements Runnable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static final String NAME = "TD - Thursday Build";
 	public static final int HEIGHT = 720;
 	public static final int WIDTH = 1280;
 	
 	private Map testMap;
 	
 	private void init() {
 		
 		
		//Map testMap = new Map(10, 10);
 	}
 		
 	@Override
 	public void run() {
 		long lastTime = System.nanoTime();
 		double unprocessed = 0;
 		double nsPerTick = 1000000000.0 / 60;
 		int ticks = 0;
 		int frames = 0;
 		init();
 		long lastTimer1 = System.currentTimeMillis();
 		
 		while(true) {
 			long now = System.nanoTime();
 			unprocessed += (now - lastTime) / nsPerTick;
 			lastTime = now;
 			boolean shouldRender = true;
 			while (unprocessed >= 1) {
 				ticks++;
 				tick();
 				unprocessed -= 1;
 				shouldRender = true;
 			}
 
 			try {
 				Thread.sleep(2);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			if (shouldRender) {
 				frames++;
 				render();
 			}
 
 			if (System.currentTimeMillis() - lastTimer1 > 1000) {
 				lastTimer1 += 1000;
 				System.out.println(ticks + " ticks, " + frames + " fps");
 				frames = 0;
 				ticks = 0;
 			}
 		}
 	}
 	
 	private void tick() {
 		
 	}
 	
 	private void render() {
		//testMap.render();
 	}
 	
 
 	public static void main(String[] args) {
 		Game game = new Game();
 		
 		JFrame frame = new JFrame(Game.NAME);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//frame.setLayout(new BorderLayout());
 		frame.add(game, BorderLayout.CENTER);
 		//frame.setSize(WIDTH, HEIGHT);
 		frame.pack();
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		
 		game.run();
 	}
 
 }
