 package players.ai.minmax;
 
 import players.ai.BaseRecursiveAI;
 import board.BoardState;
 import board.Move;
 import board.Piece;
 
 public abstract class BaseMinMax extends BaseRecursiveAI {
 
 	public BaseMinMax(boolean verboseOutput, int maxDepth) {
 		super(verboseOutput, maxDepth);
 		// TODO Auto-generated constructor stub
 	}
 
 	private double searchAlphaBeta(BoardState state, final Piece place,double alpha, double beta,final boolean max,final int depth){
 		counter ++;
 
 		//System.out.println(alpha + " " + beta);
 		if(state.isQuarto()){
 			if (max) return -1;
 			else return 1;
 		}
 		if (state.isDraw())
 			return 0;
 		if (depth <= 0){
 			return eval.evaluate(state,max);
 		}
 		if (max){
 			alpha = -Double.MAX_VALUE;
 		}
 		else{
 			beta = Double.MAX_VALUE;
 		}
 		Move best = null;
 		double bestScore = 0;
 		for (Move m : BoardState.getAllMoves(state, place)){
 			BoardState newState = state.deepCopy();
 			newState.placePiece(m.getPieceToPlace(), m.getX(),m.getY());
 			newState.pickPiece(m.getPieceToGiveOpponent());
 			if(newState.isQuarto()){
 
 				if (max) return 1;
 				else return -1;
 			}
 			if (state.isDraw())
 				return 0;
 			double score = searchAlphaBeta(newState,m.getPieceToGiveOpponent(),alpha,beta,!max,depth-1);
 			
 			if (max){
 				if (best == null || score > bestScore){
 					bestScore = score;
 					best = m;
 					alpha = score;
 					if(beta < score){
 						//System.out.println("Cutting search, beta: " + beta + " score: " + score);
 						return score;
 					}
 				}
 			}
 			else{
 				if (best == null || score < bestScore){
 					bestScore = score;
 					best = m;
 					beta = score;
 					if (alpha > score){
 						//System.out.println("Cutting search, alpha: " + alpha + " score: " + score);
 						return score;
 					}
 				}
 			}
 		}
 		return bestScore;
 	}
 	
 	@Override
 	public Move getNextMove(BoardState state, Piece place) {
 		
 		if(state.getRemainingPieces().size()>=16){
 			return randomizer.getNextMove(state, place);
 		}
 		if (state.getRemainingPieces().size() == 0){
 			for (int [] coord : state.getOpenSlots())
 			return new Move(place,null,coord[0],coord[1]);
 		}
 		// TODO Auto-generated method stub
 		Move best = null;
 		double bestScore = 0;
 
 		double alpha = -Double.MAX_VALUE;
 		double beta = Double.MAX_VALUE;
 		for (Move m : BoardState.getAllMoves(state, place)){
 			BoardState newState = state.deepCopy();
 			newState.placePiece(place, m.getX(),m.getY());
 			newState.pickPiece(m.getPieceToGiveOpponent());
 			double score = searchAlphaBeta(newState,m.getPieceToGiveOpponent(),alpha,beta,false,maxDepth-1);
 			if (best == null || score > bestScore){
 				bestScore = score;
 				best = m;
 				alpha = bestScore;
 			}
 			//System.out.println(score);
 		}
 		int branchingFactor = state.getRemainingPieces().size();
 		double totalCounter = getBranches(branchingFactor);
 		//System.out.println("Counter " + counter + " of " + (int)totalCounter + "(" + ((double)counter/totalCounter) + ")" );
 	//	if (totalCounter < counter){
 		//	System.out.println(branchingFactor + " " + maxDepth);
 	//}
 		counter = 0;
 		return best;
 	}
 	private double getBranches(double branchingFactor){
 		double r = 1;
 		for (int i = 0; i < maxDepth+1;i++){
 			r = r*branchingFactor*(branchingFactor+1);
 			branchingFactor--;
 		}
 		
 		return r;
 	}
 
 
 }
