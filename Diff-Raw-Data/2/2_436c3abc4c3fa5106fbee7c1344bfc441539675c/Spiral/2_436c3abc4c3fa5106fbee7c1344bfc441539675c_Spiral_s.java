 
 
 /**
  * The Class Spiral.
  */
 public class Spiral {
 
 	// the number of integers to display in the spiral
 	int spiralLength = 25; // CHANGE THIS TO INCREASE OR DECREASE THE SPIRAL LENGTH
 
 	// calculate the dimension of the square spiral
 	int dimensions = (int) Math.ceil(Math.sqrt(spiralLength));
 
 	// The buffer to build the spiral for display
 	Integer[][] displayMatrix = new Integer[dimensions][dimensions];
 
 	// find the center of the matrix and handle even number dim
 	int center = (dimensions / 2) - ((dimensions % 2 == 1) ? 0 : 1);
 
 	// initial coordinates
 	int x = center;
 	int y = center;
 	
 	// our steps for spiraling outwards
 	int layer = 1; 
 	
 	// our counter
 	int count = 1; 
 
 	/**
 	 * Insert into matrix.
 	 *  add the count value to the matrix given the direction of forward or backwards with respect to the x,y coordinates
 	 * @param isForward
 	 *           
 	 */
 	private void insertIntoMatrix(boolean isForward) {
 		// x motion 
 		for (int j = 0; j < layer && count < spiralLength; j++)
 			displayMatrix[y][x += (isForward) ? 1 : -1] = count++;
 
 		// y motion
 		for (int j = 0; j < layer && count < spiralLength; j++)
 			displayMatrix[y += (isForward) ? 1 : -1][x] = count++;
 		
 		// layer steps
 		layer++;
 	}
 
 	/**
 	 * Show spiral list. Display the initial array list, call function to create the display matrix , and display the spiral.
 	 */
 	private void showSpiralList() {
 
 		displayMatrix[center][center] = 0;
 
		// create the spiral in a display matrix to be show later
 		while (count < spiralLength) {
 			insertIntoMatrix(true);
 			insertIntoMatrix(false);
 		}
 
 		// used to evaluate the display length for each number
 		int numberWidth = String.valueOf(spiralLength).length();
 
 		System.out.println("\nThe sprial  ...");
 		// display the matrix
 		for (int i = (displayMatrix[0][0] == null) ? 1 : 0; i < displayMatrix.length; i++) {
 			for (int j = 0; j < displayMatrix[i].length; j++)
 				System.out.printf("%" + numberWidth + "s ", (displayMatrix[i][j] == null) ? "" : displayMatrix[i][j]);
 			System.out.println();
 		}
 		System.out.println("Done!");
 	}
 
 	/**
 	 * The main method.
 	 */
 	public static void main(String[] args) {
 		System.out.println("Exercise number three : number spiral");
 
 		new Spiral().showSpiralList();
 
 	}
 
 }
