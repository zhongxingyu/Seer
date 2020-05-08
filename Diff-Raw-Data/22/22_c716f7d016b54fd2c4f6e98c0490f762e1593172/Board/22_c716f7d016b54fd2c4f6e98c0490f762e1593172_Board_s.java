 class Board {
 	// for modeling/display 
 	// should be 10x20
 	private int matrix[][];
 	
 	// the current bricks X coord
 	private int activeX;
 	// Y coord
 	private int activeY;
 	
 	// these are the bricks that are already laid.
 	// bricks contain their own rotation, so that should be fine.
 	private ArrayList bricks;
 	private ArrayList coords;
 	
 	public Board() {
 		// make a new board with nothing on it.
 		matrix = new int[10][20];
 	}
 	
 	public void checkRows() {
 		// looks to see if there any rows to clear
 		System.out.println("none here boss");
 	}
 	
 	public void display() {
 		for (int i=0;i<10 ;i++ ) {
 			for (int j=0;j<20 ;j++ ) {
 				System.out.print(matrix[i][j]);
 			}
 			System.out.print("\n");
 		}
 		System.out.println("\n\n");
 	}
 	
 	public void gravity() {
 		// move active brick down
 	}
 }
