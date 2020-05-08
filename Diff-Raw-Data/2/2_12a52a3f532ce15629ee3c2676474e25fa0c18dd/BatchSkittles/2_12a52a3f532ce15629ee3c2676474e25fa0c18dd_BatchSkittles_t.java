 package skittles.sim;
 
 import java.io.File;
 
 /**
  * Runs all the sims specific in the configs/ directory.
  */
 public class BatchSkittles 
 {
 	public static void main( String[] args )
 	{	
 		File dir = new File("configs/");
 		for (String s: dir.list()) {
			new Game("configs/" + s).runGame();
 		}
 	}
 }
