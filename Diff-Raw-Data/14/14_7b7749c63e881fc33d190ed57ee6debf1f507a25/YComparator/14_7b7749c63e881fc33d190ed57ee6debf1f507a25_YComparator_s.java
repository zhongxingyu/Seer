 import java.util.Comparator;
 
 
 public class YComparator implements Comparator<GameBoardNode> {
 
 	private int condition;
 	
 	public final static int NEXTBOXIS_EVERY_BOX = 1;
 	public final static int NEXTBOXIS_EVERY_UNPLACED_BOX = 2;
 	public final static int NEXTBOXIS_LEXICOGRAPHICAL_ORDER = 3;
 	public final static int NEXTBOXIS_LOWEST_MANHATTAN_SUM = 4;
 	public final static int NEXTBOXIS_CLOSEST_BOX = 5;
 	public final static int NEXTBOXIS_RANDOM_BOX = 6;
 	
 	public final static int BOARD_WIDE_UNPLACED_BOXES = 10; 
 	public final static int BOARD_WIDE_MANHATTAN_SUM = 11;
 	
 	public YComparator(int condition) {
 		this.condition = condition;
 	}
 	
 	@Override
 	public int compare(GameBoardNode a, GameBoardNode b) {
 		String keyWord;
 		Double aVal = 0.0;
 		Double bVal = 0.0;		

		
 		
 		switch(condition) {
 			// Every box
 			case NEXTBOXIS_EVERY_BOX: 
 				return 0;
 				
 			case NEXTBOXIS_EVERY_UNPLACED_BOX:
 				//for(int box = 0; box < ).getBoxIndex()
 				a.priorMove.getBoxIndex();
 				return 0;
 				
 			// Lexicographical order
 			case NEXTBOXIS_LEXICOGRAPHICAL_ORDER:
 				int aDone = 0;
 				int bDone = 0;
 				 for(int i = 0; i < a.gameBoard.boxPositions.length; i++) {
 		        	if(a.gameBoard.boxPositions[i] == StaticBoard.startBoard.boxPositions[i])
 		        		aDone++;
 		        	if(b.gameBoard.boxPositions[i] == StaticBoard.startBoard.boxPositions[i])
 		        		bDone++;
 		        }
 			        
 				if (aDone > bDone)
 		            return -1;
 
 				if (aDone < bDone)
 		            return 1;
 				
 			// Box closest to target
 			case NEXTBOXIS_LOWEST_MANHATTAN_SUM:
 				return 0;
 				// TODO must know which box is going to be moved, look at GameBoardNode
 				
 			// Random box
 			case NEXTBOXIS_CLOSEST_BOX:
 				return 0;
 
 			// Random box
 			case NEXTBOXIS_RANDOM_BOX:
 				return 0;
 				
 			////////////////////////////////////////////////////////////////////////////////////
 				
 			// Every unplaced box
 			case BOARD_WIDE_UNPLACED_BOXES:
 				//aVal = getNumPlacedBoxes(a.gameBoard);
 				//bVal = getNumPlacedBoxes(b.gameBoard);
 				
 				keyWord = "placed boxes";
 				aVal = (Double)a.cache.get(keyWord);
 				if(aVal == null) {
 					aVal = getNumUnplacedBoxes(a.gameBoard);
 					a.cache.put(keyWord, aVal);
 				}
 				
 				bVal = (Double)a.cache.get(keyWord);
 				if(bVal == null) {
 					bVal = getNumUnplacedBoxes(b.gameBoard);
 					a.cache.put(keyWord, bVal);
 				}
 				
 				if (aVal > bVal)
 		            return -1;
 
 		        if (aVal < bVal)
 		            return 1;
 		        break;
 				
 			case BOARD_WIDE_MANHATTAN_SUM:
 				//aVal = getManhattanSum(a.gameBoard);
 				//bVal = getManhattanSum(b.gameBoard);
 				
 				keyWord = "manhattan sum";
 				aVal = (Double)a.cache.get(keyWord);
 				if(aVal == null) {
 					aVal = getManhattanSum(a.gameBoard);
 					a.cache.put(keyWord, aVal);
 				}
 				
 				bVal = (Double)a.cache.get(keyWord);
 				if(bVal == null) {
 					bVal = getManhattanSum(b.gameBoard);
 					a.cache.put(keyWord, bVal);
 				}
 				if (aVal < bVal)
 		            return -1;
 
 		        if (aVal > bVal)
 		            return 1;
 		}
 
      	return 0;
 	}
 
 	private double getManhattanSum(GameBoard board) {
 		double[][] manhattanTable = getManhattanDistances(board);
 		
 		int numBoxes = board.boxPositions.length;
 		double sum = 0;
 		
 		//double shortestPairwiseDistances[] = new double[numBoxes];
 		
 		// Let closest pair bind, then the next free pair, etc
 		// Make a list of matching boxes and startBoxes
 		for(int p = 0; p < numBoxes; p++) {
 			double minDist = Double.POSITIVE_INFINITY;
 			int minBox = -1;
 			int minStartBox = -1;
 			
 			// Find closest remaining pair
 			for(int box = 0; box < numBoxes; box++) {
 				for(int startBox = 0; startBox < numBoxes; startBox++) {
 					double dist = manhattanTable[box][startBox];
 					if(dist < minDist) {
 						minDist = dist;
 						minBox = box;
 						minStartBox = startBox;
 					}
 				}
 			}
 			
 			// Save pair with dist
 			//pairs[p][0] = minBox;
 			//pairs[p][1] = minStartBox;
 			//shortestPairwiseDistances[p] = minDist;
 			
 			// Sum the list's pairwise distances and return
 			sum += minDist;
 			
 			// Purge row box and column startBox
 			for(int boxN = 0; boxN < numBoxes; boxN++) {
 				manhattanTable[boxN][minStartBox] = Double.POSITIVE_INFINITY;
 			}
 			for(int boxM = 0; boxM < numBoxes; boxM++) {
 				manhattanTable[minBox][boxM] = Double.POSITIVE_INFINITY;
 			}
 		}
 		
 		// Sum the list's pairwise distances and return
 		/*
 		double sum = 0;
 		
 		for(int p = 0; p < numBoxes; p++) {
 			sum += shortestPairwiseDistances[p];
 		}
 		*/
 		
 		return sum;
 	}
 	
 	private double[][] getManhattanDistances(GameBoard board) {
 		int numBoxes = board.boxPositions.length;
 		double[][] distances = new double[numBoxes][numBoxes];
 		
 		for(int box = 0; box < numBoxes; box++) {
 			for(int startBox = 0; startBox < numBoxes; startBox++) {
 				int x1 = board.boxPositions[box] % StaticBoard.MAP_WIDTH;
 				int y1 = board.boxPositions[box] / StaticBoard.MAP_WIDTH;
 				int x2 = StaticBoard.startBoard.boxPositions[startBox] % StaticBoard.MAP_WIDTH;
 				int y2 = StaticBoard.startBoard.boxPositions[startBox] / StaticBoard.MAP_WIDTH;
 				
 				int dx = Math.abs(x1 - x2);
 				int dy = Math.abs(y1 - y2);
 				
 				distances[box][startBox] = dx + dy;
 			}
 		}
 		
 		return distances;
 	}
 	
 	private double getNumUnplacedBoxes(GameBoard board) {
 		int done = 0;
 		int numBoxes = board.boxPositions.length;
 		
         for(int startBox = 0; startBox < numBoxes; startBox++) {
         	for(int box = 0; box < numBoxes; box++) {
         		if(board.boxPositions[box] == StaticBoard.startBoard.boxPositions[startBox])
         			done++;
         	}
         }
         
         return numBoxes - done;
 	}
 	
 }
