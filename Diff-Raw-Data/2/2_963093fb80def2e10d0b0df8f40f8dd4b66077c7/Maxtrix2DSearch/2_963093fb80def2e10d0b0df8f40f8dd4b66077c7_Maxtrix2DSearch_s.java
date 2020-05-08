 package topcoder.alex.misc;
 
 public class Maxtrix2DSearch {
 	public static boolean find(int matrix[][], int value) {
 		int rows = matrix.length;
 		int cols = matrix[0].length;
 		return findCore(matrix, value, 0, 0, rows - 1, cols - 1);
 	}
 	//O(m+n)
 	private static boolean findCore(int matrix[][], int value, int row1,
 			int col1, int row2, int col2) {
 		if (value < matrix[row1][col1] || value > matrix[row2][col2])
 			return false;
 		if (value == matrix[row1][col1] || value == matrix[row2][col2])
 			return true;
 		int copyRow1 = row1, copyRow2 = row2;
 		int copyCol1 = col1, copyCol2 = col2;
 		int midRow = (row1 + row2) / 2;
 		int midCol = (col1 + col2) / 2;
 		
 		// find the last element less than value on diagonal
 		while ((midRow != row1 || midCol != col1)
 				&& (midRow != row2 || midCol != col2)) {
 			if (value == matrix[midRow][midCol])
 				return true;
 			if (value < matrix[midRow][midCol]) {
 				row2 = midRow;
 				col2 = midCol;
 			} else {
 				row1 = midRow;
 				col1 = midCol;
 			}
 			midRow = (row1 + row2) / 2;
 			midCol = (col1 + col2) / 2;
 		}
 		// find value in two sub-matrices
 		boolean found = false;
 		if (midRow < matrix.length - 1)
 			found = findCore(matrix, value, midRow + 1, copyCol1, copyRow2,
 					midCol);
 		if (!found && midCol < matrix[0].length - 1)
 			found = findCore(matrix, value, copyRow1, midCol + 1, midRow,
 					copyCol2);
 		return found;
 
 	}
 	public static boolean find2(int matrix[][], int value){
 		int row = 0;
 		int col = matrix[0].length-1;
		while(row < matrix[0].length && col >=0){
 			if(value == matrix[row][col]){
 				System.out.printf("Row = %d Col=%d \n ", row, col);
 				return true;
 			}
 			if(matrix[row][col] > value){
 				col--;
 			}else{
 				row++;
 			}
 		}
 		System.out.println("Not found!");
 		return false;
 	}
 
 	public static void main(String[] args) {
 		int [][] matrix = {{1,2,8,9},{2,4,9,12},{4,7,10,13},{6,8,11,15}};
 		System.out.println(find(matrix, 13));
 		System.out.println(find(matrix, 7));
 		System.out.println(find(matrix, 100));
 		
 		System.out.println(find2(matrix, 13));
 		System.out.println(find2(matrix, 7));
 		System.out.println(find2(matrix, 100));
 
 	}
 
 }
