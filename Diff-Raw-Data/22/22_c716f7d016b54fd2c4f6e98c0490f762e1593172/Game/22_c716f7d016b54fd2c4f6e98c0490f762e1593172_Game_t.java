 class Game implements Runnable {
 	private Board matrix;
 	
 	public Game() {
 		// needs a board
 		matrix = new Board();
 	}
 	
 	public void run() {
 		while (true) {
 			// display board
			//matrix.display();
			System.out.println(new Piece());
 			Thread.yield();
 			// breakpoint
 		}
 	}
 }
