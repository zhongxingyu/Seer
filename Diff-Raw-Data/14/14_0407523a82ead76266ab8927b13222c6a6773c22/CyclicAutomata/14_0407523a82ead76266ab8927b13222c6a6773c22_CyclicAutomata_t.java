 public class CyclicAutomata extends CellularAutomata {
 	
 	int cycleLength;
 	
 	public int getNeighbour (int x, int y, int ind){
 		return super.getMooreNeighbour(x, y, ind);
 	}
 	
 	public CyclicAutomata (int[][] grid, int inCycleLength){
 		super(grid);
 		cycleLength = inCycleLength;
 	}
 	
 	public int rules (int x, int y){
 		
		for(int i = 0; i<=7; i++){
 			if(getNeighbour(x, y, i) == ((currentGrid[x][y]+1) % cycleLength)){
 				return getNeighbour(x, y, i);
 			}
 		}
 		
 		return currentGrid[x][y];
 	}
 	
 }
