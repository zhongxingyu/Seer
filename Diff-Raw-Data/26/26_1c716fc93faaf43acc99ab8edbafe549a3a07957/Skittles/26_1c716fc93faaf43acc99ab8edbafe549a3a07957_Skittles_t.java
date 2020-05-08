 package skittles.sim;
 
 public class Skittles 
 {
 	public static void main( String[] args )
 	{		
		
		for (int i = 0; i < 500; i++) {
			Game gamNew = new Game( "GameConfig.xml" );
			gamNew.runGame();
		}
 	}
 }
