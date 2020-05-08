 package com.github.joukojo.testgame;
 
 import javax.swing.JOptionPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.joukojo.testgame.world.core.WorldCore;
 import com.github.joukojo.testgame.world.core.WorldCoreFactory;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 
 	private static final Logger LOG = LoggerFactory.getLogger(App.class);
 
 	public static void main(String[] args) throws InterruptedException {
 
 		GameEngine gameEngine = GameEngine.getInstance();
 		while (true) {
 			gameEngine.init();
 			LOG.debug("game engine initialized");
 
 			JOptionPane.showMessageDialog(null, "Ready to play");
 
 			gameEngine.startGame();
 
 			WorldCore worldCore = WorldCoreFactory.getWorld();
 			Player player = (Player) worldCore.getMoveable("player");
 
 			while (!player.isDestroyed()) {
 				Thread.sleep(200L);
 				Thread.yield();
 			}
 
 			gameEngine.stopGame();
 			JOptionPane.showMessageDialog(null, "Game Over!\nScore: " + player.score);
 			worldCore.resetWorld();
 		}
 
 	}
 }
