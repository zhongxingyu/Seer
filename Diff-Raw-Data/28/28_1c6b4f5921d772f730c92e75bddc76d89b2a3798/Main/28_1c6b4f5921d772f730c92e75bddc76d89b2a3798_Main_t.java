 package collisionTest;
 import java.awt.Dimension;
 
 import com.golden.gamedev.GameLoader;
 
 import collisionTest.PlatformGame;
 
 public class Main {
 	
 		public static void main(String[] args) {
 			GameLoader game = new GameLoader();
 			game.setup(new PlatformGame(), new Dimension(800,600), false);
 			game.start();
 		}
 		
 }
