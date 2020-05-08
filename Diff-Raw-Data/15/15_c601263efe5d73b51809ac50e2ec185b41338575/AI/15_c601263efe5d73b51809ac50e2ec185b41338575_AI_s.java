 /* AI.java */
 
 package machine;
 
 import player.*;
 
 /**
  *	A package protected class that contains static methods that can evaluate a 
  *  given board and return the best possible moves.
  */
 
 class AI{
 
 	public static Move smartMove(Board bd, int myColor, int oppColor, int searchDepth){
 	    int maxScore = -1000;
 	    int currScore;
 	    Move maxMove = null;
 	    Move currMove = null;
 
 	    MoveIterator it = new MoveIterator(bd, myColor);
 
 	    while((currMove = it.getNext()) != null){
 	      currScore = scoreMove(bd, currMove, myColor, oppColor, 1, searchDepth, maxScore);
 	      if (currScore > maxScore){
 	        maxScore = currScore;
 	        maxMove = currMove;
 	      }
     	}
     
     	return maxMove;
   	}
 
 	private static int scoreMove(Board bd, Move m, int myColor, int oppColor, int currDepth, int maxDepth, int cutoff){
 		int retVal;
 	    bd.addChip(m, myColor);
 
 	    if (currDepth >= maxDepth) {
 	      	if(currDepth % 2 == 0) //this means we are at a depth where we must eval for an opponent Move
 	      		retVal = evaluate(bd, m, oppColor);   // eval is higher if pos is good for this color
 	      	else
 	      		retVal = evaluate(bd, m, myColor);
 	      	bd.removeChip(m.x1, m.y1);
 	      	return retVal;
 	    }
 
 	    if (bd.hasNetwork(myColor)  &&  !bd.hasNetwork(oppColor)) {
 	      bd.removeChip(m.x1, m.y1);
 	      return 1000;
 	    }
 
 	    retVal = -tryAll(bd, m, myColor, oppColor, currDepth, maxDepth, cutoff);
 	    bd.removeChip(m.x1, m.y1);
 	    return retVal;
   	}
 
 	private static int tryAll(Board bd, Move m, int myColor, int oppColor, int currDepth, int maxDepth, int cutoff){
 	    int currVal, maxVal = -1000;
 	    MoveIterator it = new MoveIterator(bd, oppColor);
 	    Move currMove;
 
 	    while((currMove = it.getNext()) != null) {
 	      currVal = scoreMove(bd, currMove, myColor, oppColor, currDepth+1, maxDepth, maxVal);
 	      if (currVal > maxVal) {
 			maxVal = currVal;
 			if (maxVal >= -cutoff)
 		  		break;
 	      	}
 	    }
 	    return maxVal;
   	}
 	
 	public static int evaluate(Board bd, Move mv, int color){
 		int score = 0;
 		Board board = bd.copyBoard();
 	        Move move = mv;	
 		int pieces = bd.getPieces(color);
 		Board oldBoard = bd;
 		
 		//some useful values to have
 		int exploreLength;
 		int enemyColor = getEnemyColor(color);
 		int prevConnections = oldBoard.howManyConnections(color);
 		int preMoveEnemyConnections = oldBoard.howManyConnections(enemyColor);
 
 		board.addChip(mv,color);
 		
 		// If proposed move gives us a network...without giving the enemy a network.					
 		if(board.hasNetwork(color) && !board.hasNetwork(enemyColor)){//and enemy does NOT have network.
 			score = 1000;
 			return score;
 		}
 		//Replacement for critical threat.This isn't what critical threat does...
 		if(board.hasNetwork(enemyColor))
 			score = -1000;
 
 		exploreLength = bd.getMaxExploreLength(); // must be after a network test.
 		System.out.println("exploreLength is " + exploreLength);
 		
 
 		// Take the center in the beginning.
 		if (pieces <3){ //love pieces.
 			if(mv.x1 > 2 && mv.x1 < 5 && mv.y1 > 2 && mv.y1 < 5)
 				score += 250;
 		}
 
 		//If we are into the game and still don't have a chip in the endgoal. 
 		if(pieces >= 4 && oldBoard.endGoalEmpty(color))
 		{				
 			if(!board.endGoalEmpty(color) || !board.startGoalEmpty(color)) 
 				score += 125; //Fixed it,						
 		}
 
 
 		//Difference in enemy connections
 		int diffConnections = board.howManyConnections(color) - prevConnections;
 		if (diffConnections < 0)
 			score -= diffConnections*100;
 		if (diffConnections > 0)
 			score += diffConnections*100; //creating more connections is good. 
 		if (diffConnections == 0)
 			score -= 50;
 		
 		// Difference in enemy connections. I made offense more valuable then defense. This can change if needbe.
 		int diffEnemyConnections = board.howManyConnections(enemyColor) - preMoveEnemyConnections;
 		if (diffEnemyConnections < 0)
 			score += diffEnemyConnections*75;
 		if (diffEnemyConnections > 0 )
 			score -= diffEnemyConnections*75;
 		if (diffEnemyConnections == 0)
 			score += 50;
 
 		// If proposed move neutralizes a critical threat
		/*if(criticalThreat(oldBoard, color))
 		{
 			
 			if(!criticalThreat(board, color))
 				{	
 					score += 500;
 					if(score >= 1000)
 						score = 999;
 				}
 				
 				
 		}
 		// If proposed move causes a critical threat.
 		if(criticalThreat(board, color))
 			{
 				score = -1000;				
 			}
			*/
 
 		return score; 		
 		}
 		
 
 	private static int getEnemyColor(int myColor){
 		int enemyColor; 
 
 		if(myColor==Chip.BLACK)			
 			enemyColor = Chip.WHITE;
 		else
 			enemyColor=Chip.BLACK;
 
 		return  enemyColor;
 	}
 
 
 
 	// Returns true if the enemy can place a wining move, but doesnt have a network YET
	/*	static boolean criticalThreat(Board bd, int color)
 	{	
 		Board Newboard = bd.copyBoard();
 			
 		int enemyColor= getEnemyColor(color);
 		boolean enemyHasNetwork;
 		
 		
 		
 		boolean threat = false;
 		
 		Move[] possibleMoves = LegalMoves.possibleMoves( Newboard, enemyColor);
 		
 		
 		for(int i = 0; i < possibleMoves.length; i++){			
			
 		Newboard.addChip(possibleMoves[i], enemyColor);
 		if(Newboard.hasNetwork(enemyColor))
 			return true;
 		
 		Newboard = bd.copyBoard();
 		
 		}
 		
 		return threat;								
 	}
*/								
 	
 		
 }
