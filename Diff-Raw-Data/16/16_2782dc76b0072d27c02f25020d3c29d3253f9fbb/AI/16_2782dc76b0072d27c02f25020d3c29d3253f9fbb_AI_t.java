 import java.awt.Point;
 import java.util.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class AI {
     AI() {}
     
     private final int BLACKPIECE = -1;
     private final int WHITEPIECE = 1;
     private final int EMPTYSPOT = 0;
 	public int MAX_GRID_WIDTH_INDEX;
 	public int MAX_GRID_HEIGHT_INDEX;
 	
     Boolean gameOver(int[][] gameState){
 
 	// Place holder, code written just have to meld things together
 	// during our next meeting
 	return false;
 	
     }
     
     void setBounds(int rowSize, int columnSize){
     	
     	MAX_GRID_WIDTH_INDEX = rowSize;
     	MAX_GRID_HEIGHT_INDEX = columnSize;
     	
     }
     
 	int evaluateBoard(int[][] gameState){
 		int value = 0;
 		
		for (int x = 0; x < MAX_GRID_WIDTH_INDEX+1; x++){ 
			for (int y = 0; y < MAX_GRID_HEIGHT_INDEX+1; y++){ 
 				if(gameState[x][y] == 1) value++;
 				if(gameState[x][y] == -1) value--;
 			}
 		}
 		return value;
 	}	
 	
 	public Boolean aiIsOnGrid(Point move) {
 		
         if(move.x < 1 || move.x > MAX_GRID_WIDTH_INDEX+1) return false;
         
         if(move.y < 1 || move.y > MAX_GRID_HEIGHT_INDEX+1) return false; 
         
         return true;
     }
 	
 	private Boolean aiIsStrongPoint(Point a){
 		
         return (a.x % 2 == 1 && a.y % 2 == 1) || (a.x % 2 == 0 && a.y %2 == 0);
     }
 
     ArrayList<Move> getValidMoves(int[][] gameState) {//{{{
     	
     	int movingColor = 0;
     	int fwdColor = 0;
     	int bkwdColor = 0;
     	ArrayList<Point> blackPawnLocations = new ArrayList<Point>();
     	ArrayList<Move> validMoves = new ArrayList<Move>();  	
 
     	
     	//For our current board, we want to find all of the black pawns on the board.
     	
     	for (int x = 0; x < MAX_GRID_WIDTH_INDEX+1; x++){ 
 			for (int y = 0; y < MAX_GRID_HEIGHT_INDEX+1; y++){ 
 				
 				if(gameState[x][y] == BLACKPIECE){
 					
 					blackPawnLocations.add(new Point(x + 1,y + 1));
 					
 				}
 			}
 		}    	
     	
     	System.out.println("Number of black pawns: " + blackPawnLocations.size());
     	
 	    for (Point startLocation: blackPawnLocations){
 	    	
 	    	List<Point> adjacentMoves = Grid.getAdjacentPoints(startLocation);
 	    	
 	    	for(Point adjacentMove: adjacentMoves){
 	    		
 	    		if(aiIsOnGrid(adjacentMove) && (gameState[adjacentMove.x-1][adjacentMove.y-1] == EMPTYSPOT) ){	    			
 	    			
 		    		Point fwdMove = Vector.subtract(adjacentMove,startLocation);
 	    	        Point fwdTarPt = Vector.add(adjacentMove,fwdMove);
 	    	        Point bkwdMove = Vector.subtract(startLocation,adjacentMove);
 	    	        Point bkwdTarPt = Vector.add(startLocation,bkwdMove);	    	        
 	    	        movingColor = gameState[startLocation.x-1][startLocation.y-1];
 	    	        
 	    	        
 	    	        if(aiIsOnGrid(fwdTarPt)){
 	    	        	
 	    	        	fwdColor = gameState[fwdTarPt.x-1][fwdTarPt.y-1];
 	    	        	
 	    	        	if( (movingColor != fwdColor) && (fwdColor != EMPTYSPOT) )			
 	    	        		validMoves.add(new Move(startLocation.x, startLocation.y, adjacentMove.x, adjacentMove.y) );	    	        	
 	    	        }	    	        
 	    	        else if(aiIsOnGrid(bkwdTarPt)){		    			
 			    		bkwdColor = gameState[bkwdTarPt.x-1][bkwdTarPt.y-1];
 			    		
 			    		if( (movingColor != bkwdColor) && (bkwdColor != EMPTYSPOT) )			    			
 			    			validMoves.add(new Move(startLocation.x, startLocation.y, adjacentMove.x, adjacentMove.y) );			        	
 		    		}
 		    	}
 	    			
 	    			
 	    	}
 	    		
 	    }
 	        
 	    return validMoves;    
     }
     
     ArrayList<Move> getValidPaikaMoves(int[][] gameState) {//{{{
     	
     	ArrayList<Point> blackPawnLocations = new ArrayList<Point>();
     	ArrayList<Move> validMoves = new ArrayList<Move>();  	
     	
     	for (int x = 0; x < MAX_GRID_WIDTH_INDEX+1; x++){ 
 			for (int y = 0; y < MAX_GRID_HEIGHT_INDEX+1; y++){ 
 				
 				if(gameState[x][y] == BLACKPIECE){
 					
 					blackPawnLocations.add(new Point(x + 1,y + 1));
 					
 				}
 			}
 		}
     	
 	    for (Point startLocation: blackPawnLocations){
 	    	
 	    	List<Point> adjacentMoves = Grid.getAdjacentPoints(startLocation);
 	    	
 	    	for(Point adjacentMove: adjacentMoves){
 	    		
 	    		if(aiIsOnGrid(adjacentMove) && (gameState[adjacentMove.x-1][adjacentMove.y-1] == EMPTYSPOT) )  			
 	    			validMoves.add(new Move(startLocation.x, startLocation.y, adjacentMove.x, adjacentMove.y) );	     	
 		    		
 	    	}	    		
 	    }
 	        
 	    return validMoves;    
     }
 	
     int[][] alphaBetaSearch(int[][] gameState){
 
     	int bestMove = maxValue(gameState,5,-99999,99999);
 
     	return null;
     } 
 
 
     int maxValue(int[][] gameState, int depth, int alpha, int beta){
 
 
         if ( gameOver(gameState) || depth <= 0) return evaluateBoard(gameState);
 		
         int bestMove = -99999;
         int [][] previousBoard; 
         ArrayList<Move> validMoves = getValidMoves(gameState);
 
         for (Move move: validMoves){
         	
         	
         	gameState = move.makeMove(gameState);
         	move.setValue( evaluateBoard(gameState) );
             bestMove = Math.max(bestMove,minValue(gameState, depth-1, alpha, beta));	        			
             alpha = Math.max(alpha, bestMove);
             
             if (bestMove >= beta) return bestMove;
         }
         return bestMove;
     }
 
     int minValue(int[][] gameState, int depth, int alpha, int beta){
 
 
         if ( gameOver(gameState) || depth <= 0 ) return evaluateBoard(gameState);
 
         int bestMove = 99999;
         ArrayList<Move> validMoves = getValidMoves(gameState);
 
         for (Move move: validMoves){
         	
         	gameState = move.makeMove(gameState);
         	move.setValue( evaluateBoard(gameState) );
             bestMove = Math.min(bestMove, maxValue(gameState, depth-1, alpha, beta));
             beta = Math.min(beta, bestMove);
 	    
             if (bestMove <= alpha) return bestMove;
         }
         return bestMove;
     }
 
     public Move getMove(int[][] gridState) {
         //returns a Move which is 2 pts: start & end
     	Random rand = new Random();
     	int min = 0;
     	int max = 0;    	
     	int randomNum = 0;
     	ArrayList<Move> captureMoves = getValidMoves(gridState);
    	if( captureMoves.size() != 0){
    		max = captureMoves.size() - 1;
     		randomNum = rand.nextInt(max - min + 1) + min;
    		
    		System.out.println("GM. Random int chosen: " + randomNum + " " +
					   "Number of move choices: " + captureMoves.size());
    		
     		Move bestMove = captureMoves.get(randomNum);
     		
     		System.out.println("GM START POINT (" + bestMove.startPointX + "," + bestMove.startPointY + ")");
         	System.out.println("GM END POINT (" + bestMove.endPointX + "," + bestMove.endPointY + ")");
       		return bestMove;
     	}
     	else{
     	
     		ArrayList<Move> paikaMoves = getValidPaikaMoves(gridState);
    		max = paikaMoves.size() - 1;
     		randomNum = rand.nextInt(max - min + 1) + min;
     		Move bestMove = paikaMoves.get(randomNum);
     		
     		System.out.println("GM START POINT (" + bestMove.startPointX + "," + bestMove.startPointY + ")");
         	System.out.println("GM END POINT (" + bestMove.endPointX + "," + bestMove.endPointY + ")");
     		return bestMove;
     	
     	}    	
         
     }
 
     static public Move getDoubleMove(int[][] gridState, Point selectedPiece, List<Point> validEndPts) {
     	
     	Random rand = new Random();
     	int min = 0;
     	int max = validEndPts.size() - 1;    	
     	int randomNum = rand.nextInt(max - min + 1) + min;
     	
     	System.out.println("GDM. Random int chosen: " + randomNum + " " +
     					   "Number of move choices: " + validEndPts.size());
     	
         Point endPoint = validEndPts.get(randomNum);
         Move bestMove = new Move(selectedPiece.x, selectedPiece.y, endPoint.x, endPoint.y);
         System.out.println("GDM START POINT (" + bestMove.startPointX + "," + bestMove.startPointY + ")");
     	System.out.println("GDM END POINT (" + bestMove.endPointX + "," + bestMove.endPointY + ")");
         return bestMove;
     }
 }
