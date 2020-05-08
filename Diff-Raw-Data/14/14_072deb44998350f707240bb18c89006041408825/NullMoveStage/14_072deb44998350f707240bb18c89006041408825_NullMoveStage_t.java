 package chess.search.search34.pipeline;
 
 import chess.search.search34.Hash;
 import chess.search.search34.Search34;
 import chess.search.search34.StackFrame;
 import chess.search.search34.TTEntry;
import chess.state4.MoveEncoder;
 import chess.state4.State4;
 
 /** null move pruning */
 public final class NullMoveStage implements MidStage {
 
 	private final StackFrame[] stack;
 	private final Hash m;
 	private final Search34 searcher;
 	private final MidStage next;
 
 	public NullMoveStage(StackFrame[] stack, Hash m, Search34 searcher, MidStage next){
 		this.stack = stack;
 		this.m = m;
 		this.searcher = searcher;
 		this.next = next;
 	}
 
 	@Override
 	public int eval(int player, int alpha, int beta, int depth, int nt, int stackIndex, State4 s) {
 
 		StackFrame frame = stack[stackIndex];
 
 		if(nt != SearchContext.NODE_TYPE_PV &&
 				!frame.skipNullMove &&
 				depth > 3 * Search34.ONE_PLY &&
 				!frame.alliedKingAttacked &&
 				frame.hasNonPawnMaterial &&
 				frame.nonMateScore){
 
 			final int r = 3*Search34.ONE_PLY + depth/4;
 
 			//note, non-pv nodes are null window searched - no need to do it here explicitly
 			//stack[c.stackIndex+1].futilityPrune = true;
 			s.nullMove();
 			stack[stackIndex+1].skipNullMove = true;
 			int n = -searcher.recurse(1 - player, -beta, -alpha, depth - r, nt, stackIndex + 1, s);
 			stack[stackIndex+1].skipNullMove = false;
 			s.undoNullMove();
 
 			if(n >= beta){
 				if(n >= 70000){
 					n = beta;
 				}
 				if(depth < 12*Search34.ONE_PLY){
 					return n;
 				}
 
 				//verification search
 				int v = next.eval(player, alpha, beta, depth - r, nt, stackIndex, s);
 
 				if(v >= beta){
 					return n;
 				}
 			} else if(n < alpha){
 				//store the threat move as killer for opp
 				long move = stack[stackIndex+1].bestMove;
				if(move != 0 &&
						MoveEncoder.getTakenType(move) == State4.PIECE_TYPE_EMPTY &&
						MoveEncoder.isEnPassanteTake(move) == 0 &&
						!MoveEncoder.isPawnPromotion(move)){
 					//doesnt matter which we store to, no killers stored at this point in execution
 					stack[stackIndex].killer[0] = move & 0xFFFL;
 				}
 			} //case alpha < n < beta can only happen in pv nodes, at which we dont null move prune
 		}
 
 		return next.eval(player, alpha, beta, depth, nt, stackIndex, s);
 	}
 }
