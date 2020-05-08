 import java.io.Serializable;
 
 public class LifeSimulation implements Serializable {
 
 	private static final long serialVersionUID = 1454070721099717422L;
 	private static final boolean BOOLEAN_DEAD = false;
 	
 	private boolean states[][];
	
 	public LifeSimulation(int row, int col) {
 		states = new boolean[row][col];
 		for (int i=0; i<row; i++) {
 			for (int j=0; j<col; j++) {
 				states[i][j] = BOOLEAN_DEAD;	//All cell's are dead
 			}
 		}
 	}
 	
 	public int getRow() {
 		return states.length;
 	}
 	
 	public int getColumn() {
 		return states[0].length;
 	}
 	
 	public boolean getLife(int row, int col) {
 		return states[row][col];
 	}
 	
 	public void setLife(int row, int col, boolean life) {
 		states[row][col] = life;
 	}
 	
 	public void evolve() {
 		boolean temp[][] = new boolean [getRow()][getColumn()];
 		
 		for (int row=0; row<getRow(); row++) {
 			for (int col=0; col<getColumn(); col++) {
 				temp[row][col] = getNewGeneration( calculateNeighbours(row, col), row, col);
 			}
 		}
 		setNewGeneration(temp);
 	}
 	
 	private void setNewGeneration(boolean[][] temp) {
 		for (int row=0; row<getRow(); row++) {
 			for (int col=0; col<getColumn(); col++) {
 				states[row][col] = temp[row][col];
 			}
 		}
 	}
	
 	private boolean isInBorder(int row, int col) {
 		return row >= 0 && col >= 0 && row < getRow() && col < getColumn();
 	}
 	
 	private int calculateNeighbours(int row, int col) { //Calculates neighbours for specific cell
 		int r = 0;
 		int c = 0;
 		int countNeighbours = 0;
 		
 		countNeighbours = states[row][col] ? -1 : 0;		//Cuz it will calculate himself too if it's alive
 		for (int rowMod = -1; rowMod <=1; rowMod++) {
 			for (int colMod = -1; colMod <=1; colMod++) {
 				r = row + rowMod;
 				c = col + colMod;
 				if (isInBorder(r,c) && states[r][c]) {		//If cell exists and it is alive..
 					countNeighbours += 1;	 //count this neighbour
 				}
 			}
 		}
 		return countNeighbours;
 	}
 	
 	public void setLifeByString(String rowCol, boolean life) {
 		String tmp[] = rowCol.split(",");
 		int row = Integer.parseInt(tmp[0]);
 		int col = Integer.parseInt(tmp[1]);
 		
 		states[row][col] = life;
 	}
 	
 	private boolean getNewGeneration(int count, int row, int col) {
 		return (3 == count || 2 == count && states[row][col]);
 	}
 }
