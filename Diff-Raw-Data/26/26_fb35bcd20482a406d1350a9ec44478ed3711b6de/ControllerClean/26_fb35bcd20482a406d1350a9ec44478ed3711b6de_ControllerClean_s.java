 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 public class ControllerClean {
 
 	private Model model = new Model();
 
 	// Container for result path
 	private SortedSet<Tuple> result = new TreeSet<Tuple>(new Comparator<Tuple>() {
 
 		@Override
 		public int compare(Tuple t1, Tuple t2) {
 			if (t1.x < t2.x && t1.y < t2.y && t1.z < t2.z)
 				return -1;
 			if (t1.x > t2.x && t1.y > t2.y && t1.z > t2.z)
 				return 1;
 			if (t1.x == t2.x && t1.y == t2.y && t1.z == t2.z)
 				return 0;
 			
 			if ((t1.x == t2.x && t1.y <= t2.y && t1.z <= t2.z) || (t1.x <= t2.x && t1.y == t2.y && t1.z <= t2.z) || (t1.x <= t2.x && t1.y <= t2.y && t1.z == t2.z))
 				return -1;
 			if ((t1.x == t2.x && t1.y >= t2.y && t1.z >= t2.z) || (t1.x == t2.x && t1.y >= t2.y && t1.z >= t2.z) || (t1.x == t2.x && t1.y >= t2.y && t1.z >= t2.z))
 				return 1;
 			return 0;
 		}
 	});
 	// Container for memory stats
 	private HashSet<Long> memoryAllocation = new HashSet<Long>();
 
 	public void findSimilarity(String first, String second, String third, int offsetX, int offsetY, int offsetZ) {
 		memStats();
 		int cols = first.length() + 1;
 		int rows = second.length() + 1;
 		int depth = third.length() + 1;
 		
 		// Build full matrix
 		if (first.length() < 2 || second.length() < 2 || third.length() < 2) {
 			Cell matrix[][][] = new Cell[cols][rows][depth];
 
 			// Initialize matrix
 			for (int i = 0; i < cols; i++) {
 				matrix[i][0][0] = new Cell(i*model.c, Directions.DIRECTION_LEFT);
 			}
 			for (int i = 0; i < rows; i++) {
 				matrix[0][i][0] = new Cell(i*model.c, Directions.DIRECTION_UP);
 			}
 			for (int i = 0; i < depth; i++) {
 				matrix[0][0][i] = new Cell(i*model.c, Directions.DIRECTION_RIGHT);
 			}
 
 			for ( int i = 1; i < rows; i++ ) {
 				for ( int j = 1; j < depth; j++ ) {
 					int rightCross = matrix[0][i-1][j-1].getScore() + match('-',second.charAt(i-1),third.charAt(j-1));
 					int up = matrix[0][i-1][j].getScore() + model.gap;
 					int right = matrix[0][i][j-1].getScore() + model.gap;
 					int max = Math.max(Math.max(rightCross, up), right);
 					if (max==rightCross)
 						matrix[0][i][j] = new Cell(max, Directions.DIRECTION_RIGHTCROSS);
 					else if (max==up)
 						matrix[0][i][j] = new Cell(max, Directions.DIRECTION_UP);
 					else // gapHorizontal
 						matrix[0][i][j] = new Cell(max, Directions.DIRECTION_RIGHT);
 				}
 			}
 
 			for (int i = 1; i < cols; i++) {
 				for (int j = 0; j < rows; j++) {
 					for (int k = 0; k < depth; k++) {
 						if (j==0 && k==0) { // already initialized
 							continue;
 						}
 						else if (j==0) { // top wall
 							int cross = matrix[i-1][j][k-1].getScore() + match(first.charAt(i-1),third.charAt(k-1));
 							int right = matrix[i][j][k-1].getScore() + match('-', '-', third.charAt(k-1));
 							int left = matrix[i-1][j][k].getScore() + match(first.charAt(i-1), '-', '-');
 							int max = Math.max(Math.max(cross,right),left);
 							if (max==cross) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_CROSS);
 							}
 							else if (max==right) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_RIGHT);
 							}
 							else { //left
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_LEFT);
 							}
 						}
 						else if (k==0) { // front wall
 							int left = matrix[i-1][j][k].getScore() + match(first.charAt(i-1), '-', '-');
 							int leftCross = matrix[i-1][j-1][k].getScore() + match(first.charAt(i-1), second.charAt(j-1), '-');
 							int up = matrix[i][j-1][k].getScore() + match('-', second.charAt(j-1), '-');
 							int max = Math.max(Math.max(leftCross,left),up);
 							if (max==leftCross) {
								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_RIGHTCROSS);
 							}
 							else if (max==left) {
								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_RIGHT);
 							}
 							else { //up
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_UP);
 							}
 						} else {
 							// DIRECTION_CROSS
 							int cross = matrix[i-1][j][k-1].getScore() + match(first.charAt(i-1), '-', third.charAt(k-1));
 							// DIRECTION_LEFT
 							int left = matrix[i-1][j][k].getScore() + match(first.charAt(i-1), '-', '-');
 							// DIRECTION_RIGHT
 							int right = matrix[i][j][k-1].getScore() + match('-', '-', third.charAt(k-1));
 							// DIRECTION_CROSSCROSS
 							int crossCross = matrix[i-1][j-1][k-1].getScore() + match(first.charAt(i-1), second.charAt(j-1), third.charAt(k-1));
 							// DIRECTION_LEFTCROSS
 							int leftCross = matrix[i-1][j-1][k].getScore() + match(first.charAt(i-1), second.charAt(j-1), '-');
 							// DIRECTION_RIGHTCROSS
 							int rightCross = matrix[i][j-1][k-1].getScore() + match('-', second.charAt(j-1), third.charAt(k-1));
 							// DIRECTION_UP
 							int up = matrix[i][j-1][k].getScore() + match('-', second.charAt(j-1), '-');
 
 							// Find max
 							int [] array = {crossCross, cross, left, right, leftCross, rightCross, up};
 							int max = array[0];
 							for ( int m = 1; m < array.length; m++ ) {
 								max = max > array[m] ? max : array[m];
 							}
 
 							if (cross == max) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_CROSS);
 							} else if (left == max) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_LEFT);
 							} else if (right == max) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_RIGHT);
 							} else if (crossCross == max) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_CROSSCROSS);
 							} else if (leftCross == max) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_LEFTCROSS);
 							} else if (rightCross == max) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_RIGHTCROSS);
 							} else if (up == max) {
 								matrix[i][j][k] = new Cell(max, Directions.DIRECTION_UP);
 							}
 						}
 					}
 				}
 			}
 
 			// Go back the result line
 			int column = cols-1;
 			int row = rows-1;
 			int dep = depth-1;
			while (column != 0 && row != 0 && dep != 0) {
 				result.add(new Tuple(column+offsetX, row+offsetY, dep+offsetZ));
 				switch(matrix[column][row][dep].getDirection()) {
 				case DIRECTION_CROSS:
 					column--;
 					dep--;
 					break;
 				case DIRECTION_LEFT:
 					column--;
 					break;
 				case DIRECTION_RIGHT:
 					dep--;
 					break;
 				case DIRECTION_CROSSCROSS:
 					column--;
 					row--;
 					dep--;
 					break;
 				case DIRECTION_RIGHTCROSS:
 					row--;
 					dep--;
 					break;
 				case DIRECTION_LEFTCROSS:
 					column--;
 					row--;
 					break;
 				case DIRECTION_UP:
 					row--;
 					break;
 				default:
 					break;
 				}
 			}
 		} else { // Divide and conquer
 			// Beginning of index propagation
 			int x = cols/2;
 
 			// Parts of matrix
 			Cell col1[][] = new Cell[rows][depth];
 			Cell col2[][] = new Cell[rows][depth];
 
 			// Initialize col1
 			for (int i = 0; i < rows; i++) {
 				col1[i][0] = new Cell(i*model.c);
 			}
 			for (int i = 0; i < depth; i++) {
 				col1[0][i] = new Cell(i*model.c);
 			}
 			for (int i = 1; i < rows; i++) {
 				for (int j = 1; j < depth; j++) {
 					int cross = col1[i-1][j-1].getScore() + match(second.charAt(i-1), third.charAt(j-1));
 					int gapVertical = col1[i-1][j].getScore() + model.gap;
 					int gapHorizontal = col1[i][j-1].getScore() + model.gap;
 					int max = Math.max(Math.max(cross, gapVertical), gapHorizontal);
 					col1[i][j] = new Cell(max);
 				}
 			}
 			
 			for (int i = 1; i < cols; i++) {
 				// Create and fill col2 based on col1
 				col2 = new Cell[rows][depth];
 				for (int j = 0; j < rows; j++) {
 					for (int k = 0; k < depth; k++) {
 						if ( j == 0 && k == 0 )
 							col2[0][0] = new Cell(i*model.c, 0, 0);
 						else if (j == 0 && k != 0) {
 							// top wall
 							int cross = col1[j][k-1].getScore() + match(first.charAt(i-1), third.charAt(k-1));
 							int gapVertical = col2[j][k-1].getScore() + model.gap;
 							int gapHorizontal = col1[j][k].getScore() + model.gap;
 							int max = Math.max(Math.max(cross, gapVertical), gapHorizontal);
 
 							if (i <= x)
 								col2[j][k] = new Cell(max,j,k);
 							else {
 								int index, index2;
 								if (cross == max) {
 									index = col1[j][k-1].getIndex();
 									index2 = col1[j][k-1].getIndex2();
 								}
 								else if (gapHorizontal == max) {
 									index = col1[j][k].getIndex();
 									index2 = col1[j][k].getIndex2();
 								}
 								else {
 									index = col2[j][k-1].getIndex();
 									index2 = col2[j][k-1].getIndex2();
 								}
 								col2[j][k] = new Cell(max, index, index2);
 							}				
 						}
 						else if (j != 0 && k == 0) {
 							// front wall
 							int cross = col1[j-1][k].getScore() + match(first.charAt(i-1), second.charAt(j-1));
 							int gapVertical = col2[j-1][k].getScore() + model.gap;
 							int gapHorizontal = col1[j][k].getScore() + model.gap;
 							int max = Math.max(Math.max(cross, gapVertical), gapHorizontal);						
 
 							if (i <= x)
 								col2[j][k] = new Cell(max,j,k);
 							else {
 								int index, index2;
 								if (cross == max) {
 									index = col1[j-1][k].getIndex();
 									index2 = col1[j-1][k].getIndex2();
 								}
 								else if (gapHorizontal == max) {
 									index = col1[j][k].getIndex();
 									index2 = col1[j][k].getIndex2();
 								}
 								else {
 									index = col2[j-1][k].getIndex();
 									index2 = col2[j-1][k].getIndex2();
 								}
 								col2[j][k] = new Cell(max, index, index2);
 							}	
 						} else {
 							int cross = col1[j][k-1].getScore() + match(first.charAt(i-1),'-',third.charAt(k-1));
 							int left = col1[j][k].getScore() + match(first.charAt(i-1),'-','-');
 							int right = col2[j][k-1].getScore() + match('-','-',third.charAt(k-1));
 							int crossCross = col1[j-1][k-1].getScore() + match(first.charAt(i-1),second.charAt(j-1),third.charAt(k-1));
 							int leftCross = col1[j-1][k].getScore() + match(first.charAt(i-1),second.charAt(j-1),'-');
 							int rightCross = col2[j-1][k-1].getScore() + match('-',second.charAt(j-1),third.charAt(k-1));
 							int up = col2[j-1][k].getScore() + match('-',second.charAt(j-1),'-');
 
 							int [] array = {crossCross, cross, left, right, leftCross, rightCross, up};
 							int max = array[0];
 							for ( int m = 1; m < array.length; m++ ) {
 								max = max > array[m] ? max : array[m];
 							}
 							if (i <= x)
 								col2[j][k] = new Cell(max, j, k);
 							else {
 								if (crossCross == max) {
 									col2[j][k] = new Cell(max, col1[j-1][k-1].getIndex(), col1[j-1][k-1].getIndex2());
 								}
 								else if (cross == max) {
 									col2[j][k] = new Cell(max, col1[j][k-1].getIndex(), col1[j][k-1].getIndex2());
 								}
 								else if (left == max) {
 									col2[j][k] = new Cell(max, col1[j][k].getIndex(), col1[j][k].getIndex2());
 								}
 								else if (right == max) {
 									col2[j][k] = new Cell(max, col2[j][k-1].getIndex(), col2[j][k-1].getIndex2());
 								}
 								else if (leftCross == max) {
 									col2[j][k] = new Cell(max, col1[j-1][k].getIndex(), col1[j-1][k].getIndex2());
 								}
 								else if (rightCross == max) {
 									col2[j][k] = new Cell(max, col2[j-1][k-1].getIndex(), col2[j-1][k-1].getIndex2());
 								}
 								else { // up
 									col2[j][k] = new Cell(max, col2[j-1][k].getIndex(), col2[j-1][k].getIndex2());
 								}
 							}
 						}
 					}
 				}
 				// Swap cols
 				col1 = col2.clone();
 			}
 			
 			// Generate substrings
 			int y = col2[rows-1][depth-1].getIndex();
 			int z = col2[rows-1][depth-1].getIndex2();
 			String sub1L = first.substring(0, x);
 			String sub1R = first.substring(x,first.length());
 			String sub2L = second.substring(0, y);
 			String sub2R = second.substring(y,second.length());
 			String sub3L = third.substring(0, z);
 			String sub3R = third.substring(z,third.length());
 			findSimilarity(sub1L, sub2L, sub3L, offsetX, offsetY, offsetZ);
 			findSimilarity(sub1R, sub2R, sub3R, offsetX+x, offsetY+y, offsetZ+z);
 		}
 	}
 
 	// Returns similarity of nucleotides
 	private int match(char a, char b) {
 		int row = charToIndex(a);
 		int col = charToIndex(b);
 		return model.similarityMatrix[row][col];
 	}
 
 	// Returns similarity of nucleotides
 	private int match(char a, char b, char c) {
 		int row = charToIndex(a);
 		int col = charToIndex(b);
 		int dep = charToIndex(c);
 		return ( model.similarityMatrix[row][col] +
 				model.similarityMatrix[row][dep] +
 				model.similarityMatrix[dep][col] );
 	}
 
 
 	// Translates character to index
 	private int charToIndex(char a) {
 		int index;
 		if (a == 'A') {
 			index = 0;
 		}
 		else if (a == 'G') {
 			index = 1;
 		}
 		else if (a == 'C') {
 			index = 2;
 		}
 		else if (a == 'T') {
 			index = 3;
 		}
 		else
 			index = 4;
 		return index;
 	}
 	
 	public Model getModel() {
 		return model;
 	}
 
 	public SortedSet<Tuple> getResult() {
 		return result;
 	}
 	
 	// Memory stats
 	private void memStats() {
 		Runtime runtime = Runtime.getRuntime();
 		long allocatedMemory = runtime.totalMemory();
 		memoryAllocation.add(allocatedMemory);
 	}
 	
 	// Returns memory allocation in MB
 	public long getMaxMemory() {
 		long max = 0;
 		for ( long l : memoryAllocation ) {
 			if ( l > max )
 				max = l;
 		}
 		return max / (1024*1024);
 	}
 	
 	public void reset() {
 		result.clear();
 	}
 
 	public static void main(String[] args) {
 		ControllerClean c = new ControllerClean();
		String first = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		c.findSimilarity(first, first, first, 0, 0, 0);
		/*for ( Tuple t : c.result ) {
 			System.out.println(t);
		}*/
		System.out.println(c.getMaxMemory());
 	}
 
 }
