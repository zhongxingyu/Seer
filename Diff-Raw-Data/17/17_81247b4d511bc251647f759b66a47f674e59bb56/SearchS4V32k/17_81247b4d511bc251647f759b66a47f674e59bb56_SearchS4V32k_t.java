 package search.search32k;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import search.Search3;
 import search.SearchListener;
 import search.SearchStat;
 import state4.BitUtil;
 import state4.Masks;
 import state4.MoveEncoder;
 import state4.State4;
 import util.zmap.ZMap;
 import eval.Evaluator2;
 
 public final class SearchS4V32k implements Search3{
 	public final static class SearchStat32k extends SearchStat{
 		/** scores returned from quiet search without bottoming out*/
 		public long forcedQuietCutoffs;
 		public long nullMoveVerifications;
 		public long nullMoveCutoffs;
 		public long nullMoveFailLow;
 		/** scores seen pes ply*/
 		public double[] scores;
 		public int maxPlySearched;
 		
 		public String toString(){
 			return "n="+nodesSearched+", t="+searchTime+", hh="+hashHits+", qc="+forcedQuietCutoffs;
 		}
 	}
 	
 	final MoveList[] stack;
 	
 	private final static class MoveList{
 		private final static int defSize = 128;
 		public final long[] pieceMasks = new long[defSize];
 		public final long[] moves = new long[defSize];
 		public final int[] ranks = new int[defSize];
 		public final long[] pawnMoves = new long[4];
 		public int length;
 		public final boolean[] kingAttacked = new boolean[2];
 		public final long[] upTakes = new long[7];
 		public boolean skipNullMove = false;
 		/** holds killer moves as first 12 bits (ie, masked 0xFFF) of move encoding*/
 		public final long[] killer = new long[2];
 	}
 	
 	private final static int[] pawnOffset = new int[]{9,7,8,16};
 	
 	private final State4 s;
 	private final SearchStat32k stats = new SearchStat32k();
 	private final Evaluator2<State4> e;
 	private final int qply = 12;
 	private final Hash m;
 	private FileWriter f;
 	private SearchListener l;
 	private final static int stackSize = 128;
 	/** sequence number for hash entries*/
 	private int seq;
 	
 	/** controls printing pv to console for debugging*/
 	private final static boolean printPV = true;
 	private final static boolean uciPV = true;
 	
 	private final TTEntry fillEntry = new TTEntry();
 	
 	private final static int tteMoveRank = -1;
 	/** rank set to the first of the non takes*/
 	private final static int killerMoveRank = 5;
 	
 	private final AtomicBoolean cutoffSearch = new AtomicBoolean(false);
 	
 	public SearchS4V32k(State4 s, Evaluator2<State4> e, int hashSize, boolean record){
 		this.s = s;
 		this.e = e;
 		
 		//m = new ZMap3(hashSize);
 		m = new ZMap4(hashSize);
 		
 		stack = new MoveList[stackSize];
 		for(int i = 0; i < stack.length; i++){
 			stack[i] = new MoveList();
 		}
 		stats.scores = new double[stackSize];
 		
 
 		if(record){
 			try{
 				f = new FileWriter("search32k.stats", true);
 			} catch(IOException ex){
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	public SearchStat32k getStats(){
 		return stats;
 	}
 	
 	public void search(int player, int[] moveStore){
 		search(player, moveStore, -1);
 	}
 	
 	public void clearHash(){
 		m.clear();
 	}
 	
 	public void search(int player, int[] moveStore, int maxPly){
 		stats.nodesSearched = 0;
 		stats.hashHits = 0;
 		stats.forcedQuietCutoffs = 0;
 		stats.nullMoveVerifications = 0;
 		stats.nullMoveCutoffs = 0;
 		stats.maxPlySearched = 0;
 		stats.searchTime = System.currentTimeMillis();
 		
 		//search initialization
 		seq++;
 		e.initialize(s);
 		
 		long bestMove = 0;
 		double score = 0;
 		
 		final double max = 90000;
 		final double min = -90000;
 		
 		final int failOffset = 100;
 		long nodesSearched = 0;
 		for(int i = 1; (maxPly == -1 || i <= maxPly) && !cutoffSearch.get() && i <= stackSize; i++){
 			s.resetHistory();
 			double alpha = min;
 			double beta = max;
 			
 			if(i > 3){
 				/*alpha = score-35;
 				beta = score+35;*/
 				final int index = i-1-1; //index of most recent score observation
 				double est = stats.scores[index];
 				est += stats.scores[index-1];
 				est /= 2;
 				final double dir = stats.scores[index]-stats.scores[index-1];
 				est += dir/2;
 				alpha = est-35;
 				beta = est+35;
 			}
 			
 			//System.out.println("starting depth "+i);
 			score = recurse(player, alpha, beta, i, true, true, 0);
 			
 			
 			if(score <= alpha && !cutoffSearch.get()){
 				//System.out.println("search failed low, researching");
 				if(l != null){
 					l.failLow(i);
 				}
 				alpha = score-failOffset;
 				beta = score+5;
 				score = recurse(player, alpha, beta, i, true, true, 0);
 				if((score <= alpha || score >= beta)  && !cutoffSearch.get()){
 					//System.out.println("double fail");
 					if(l != null){
 						if(score <= alpha){
 							l.failLow(i);
 						} else if(score >= beta){
 							l.failHigh(i);
 						}
 					}
 					alpha = min;
 					beta = max;
 					score = recurse(player, alpha, beta, i, true, true, 0);
 				}
 			} else if(score >= beta && !cutoffSearch.get()){
 				//System.out.println("search failed high, researching");
 				if(l != null){
 					l.failHigh(i);
 				}
 				alpha = score-5;
 				beta = score+failOffset;
 				score = recurse(player, alpha, beta, i, true, true, 0);
 				if((score <= alpha || score >= beta)  && !cutoffSearch.get()){
 					//System.out.println("double fail");
 					if(l != null){
 						if(score <= alpha){
 							l.failLow(i);
 						} else if(score >= beta){
 							l.failHigh(i);
 						}
 					}
 					alpha = min;
 					beta = max;
 					score = recurse(player, alpha, beta, i, true, true, 0);
 				}
 			}
 			if(!cutoffSearch.get()){
 				nodesSearched = stats.nodesSearched;
 				stats.maxPlySearched = i;
 			}
 			final TTEntry tte;
 			if((tte = m.get(s.zkey())) != null && tte.move != 0 && !cutoffSearch.get()){
 				bestMove = tte.move;
 				if(l != null){
 					l.plySearched(bestMove, i);
 				}
 				if(printPV){
 					final String pvString = getPVString(player, s, "", 0, i, uciPV);
 					if(!uciPV) System.out.println("pv "+i+": ["+score+"] "+pvString);
 					else System.out.println("info depth "+i+" score cp "+(int)score+" time "+
 							((System.currentTimeMillis()-stats.searchTime)/1000.)+
 							" nodes "+stats.nodesSearched+" nps "+(int)(stats.nodesSearched*1000./
 							(System.currentTimeMillis()-stats.searchTime))+" pv "+pvString);
 				}
 			}
 			if(i-1 < stats.scores.length){
 				stats.scores[i-1] = score;
 			}
 		}
 		
 		stats.empBranchingFactor = Math.pow(nodesSearched, 1./stats.maxPlySearched);
 		
 		if(f != null){
 			//record turn, piece counts, and scores at each level of search
 			String pieceCounts = "";
 			for(int q = 0; q < 2; q++){
 				pieceCounts += "<piece-counts-"+q+"=";
 				for(int a = 0; a < s.pieceCounts[0].length; a++){
 					pieceCounts += s.pieceCounts[q][a];
 					if(a != s.pieceCounts[0].length-1)
 						pieceCounts+=",";
 				}
 				pieceCounts += ">";
 			}
 			String scorestr = "<scores=";
 			for(int a = 0; a < maxPly && a < stats.scores.length; a++){
 				scorestr += stats.scores[a];
 				if(a != maxPly-1)
 					scorestr += ",";
 			}
 			scorestr += ">";
 			String record = "<turn="+player+">"+pieceCounts+scorestr;
 			//ps.println(record);
 			try{
 				f.write(record+"\n");
 				f.flush();
 			} catch(IOException a){
 				a.printStackTrace();
 			}
 			//ps.append(record+"\n");
 		}
 
 		stats.endScore = score;
 		
 		if(moveStore != null){
 			int pos1 = MoveEncoder.getPos1(bestMove);
 			int pos2 = MoveEncoder.getPos2(bestMove);
 			moveStore[0] = pos1;
 			moveStore[1] = pos2;
 		}
 		
 		stats.searchTime = System.currentTimeMillis()-stats.searchTime;
 		cutoffSearch.set(false);
 	}
 	
 	private String getPVString(int player, State4 s, String pv, int depth, int maxDepth, boolean uci){
 		final TTEntry e = m.get(s.zkey());
 		if(depth < maxDepth && e != null && e.move != 0){
 			int pos1 = MoveEncoder.getPos1(e.move);
 			int pos2 = MoveEncoder.getPos2(e.move);
 
 			long pmask = 1L<<pos1;
 			long mmask = 1L<<pos2;
 			s.executeMove(player, pmask, mmask);
 
 			if(!uci){
 				this.e.initialize(s);
 				final double eval = this.e.eval(s, player);
 				pv += moveString(pos1)+"->"+moveString(pos2)+" ("+eval+"), ";
 			} else{
 				pv += moveString(pos1)+moveString(pos2)+" ";
 			}
 			
 			String r = getPVString(1-player, s, pv, depth+1, maxDepth, uci);
 			s.undoMove();
 			return r;
 		}
 		return pv;
 	}
 	
 	private static String moveString(int pos){
		return ""+(char)('a'+(pos%8))+(pos/8+1);
 	}
 	
 	/**
 	 * Traverses TT entries until the leaf state from which the evaluation was performed
 	 * is found. This can potentially fail if the TT path has been broken
 	 * <p> note, this should be called directly after a search has been performed
 	 * @param player
 	 * @param s
 	 * @param depth
 	 * @param maxDepth
 	 * @param result store for the result
 	 */
 	public void getPredictedLeafState(int player, State4 s, int depth, int maxDepth, State4 result){
 		final TTEntry e = m.get(s.zkey());
 		if(depth < maxDepth && e != null && e.move != 0){
 			final long pmask = 1L<<MoveEncoder.getPos1(e.move);
 			final long mmask = 1L<<MoveEncoder.getPos2(e.move);
 			s.executeMove(player, pmask, mmask);
 			getPredictedLeafState(1-player, s, depth+1, maxDepth, result);
 			s.undoMove();
 			return;
 		}
 		State4.copy(s, result);
 	}
 	
 	public void cutoffSearch(){
 		cutoffSearch.set(true);
 	}
 	
 	/** tests to see if the passed player is in check*/
 	private static boolean isChecked(final int player, final State4 s){
 		return State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
 	}
 	
 	private double recurse(int player, double alpha, double beta, int depth,
 			boolean pv, boolean rootNode, int stackIndex){
 		stats.nodesSearched++;
 		assert alpha < beta;
 		
 		if(s.isForcedDraw()){
 			return 0;
 		} else if(depth <= 0){
 			/*if(!isChecked(player, s)){
 				return qsearch(player, alpha, beta, 0, stackIndex, pv);
 			}
 			depth = 1;*/
 			return qsearch(player, alpha, beta, 0, stackIndex, pv);
 		}
 		
 
 		final MoveList ml = stack[stackIndex];
 		final long[] pieceMasks = ml.pieceMasks; //piece moving
 		final long[] moves = ml.moves; //moves available to piece (can be multiple)
 		final int[] ranks = ml.ranks; //move ranking
 		ml.killer[0] = 0;
 		ml.killer[1] = 0;
 		
 		int w = 0;
 
 		final long zkey = s.zkey();
 		final TTEntry e = m.get(zkey);
 		boolean tteMove = false;
 		long tteMoveEncoding = 0;
 		
 		if(e != null){
 			stats.hashHits++;
 			if(e.depth >= depth){
 				if(pv ? e.cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
 						e.cutoffType == TTEntry.CUTOFF_TYPE_LOWER: e.cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
 					
 					if(stackIndex-1 >= 0 && e.score >= beta){
 						attemptKillerStore(e.move, ml.skipNullMove, stack[stackIndex-1]);
 					}
 					
 					return e.score;
 				}
 			}
 			if(e.move != 0){
 				tteMoveEncoding = e.move;
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(tteMoveEncoding);
 				moves[w] = 1L<<MoveEncoder.getPos2(tteMoveEncoding);
 				ranks[w++] = tteMoveRank;
 				tteMove = true;
 			}
 		}
 		
 		//load killer moves
 		if(stackIndex-1 >= 0 && !ml.skipNullMove){
 			final MoveList prev = stack[stackIndex-1];
 			if(prev.killer[0] != 0 && isPseudoLegal(player, prev.killer[0], s)){
 				assert MoveEncoder.getPos1(prev.killer[0]) != MoveEncoder.getPos2(prev.killer[0]);
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(prev.killer[0]);
 				moves[w] = 1L<<MoveEncoder.getPos2(prev.killer[0]);
 				ranks[w++] = killerMoveRank;
 			}
 			if(prev.killer[1] != 0 && isPseudoLegal(player, prev.killer[1], s)){
 				assert MoveEncoder.getPos1(prev.killer[1]) != MoveEncoder.getPos2(prev.killer[1]);
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(prev.killer[1]);
 				moves[w] = 1L<<MoveEncoder.getPos2(prev.killer[1]);
 				ranks[w++] = killerMoveRank;
 			}
 		}
 		
 		ml.kingAttacked[player] = isChecked(player, s);
 		ml.kingAttacked[1-player] = isChecked(1-player, s);
 		
 		//null move pruning (hashes result, but might not be sound)
 		boolean hasNonPawnMaterial = s.pieceCounts[player][0]-s.pieceCounts[player][State4.PIECE_TYPE_PAWN] > 1;
 		if(!pv && !ml.skipNullMove && depth > 0 &&  !cutoffSearch.get() &&
 				!ml.kingAttacked[player] && hasNonPawnMaterial){
 			
 			int r = 3 + depth/4;
 			
 			//note, non-pv nodes are null window searched - no need to do it here explicitly
 			stack[stackIndex+1].skipNullMove = true;
 			s.nullMove();
 			double n = -recurse(1-player, -beta, -alpha, depth-r, pv, rootNode, stackIndex+1);
 			s.undoNullMove();
 			stack[stackIndex+1].skipNullMove = false;
 			
 			if(n >= beta){
 				if(n >= 70000){
 					n = beta;
 				}
 				if(depth < 6){
 					stats.nullMoveCutoffs++;
 					return n;
 				}
 				
 				stats.nullMoveVerifications++;
 				//verification search
 				stack[stackIndex+1].skipNullMove = true;
 				double v = recurse(player, alpha, beta, depth-r, pv, rootNode, stackIndex+1);
 				stack[stackIndex+1].skipNullMove = false;
 				if(v >= beta){
 					stats.nullMoveCutoffs++;
 					return n;
 				}
 			} else if(n < alpha){
 				stats.nullMoveFailLow++;
 			}
 		}
 
 		//internal iterative deepening
 		double lazyEval = this.e.lazyEval(s, player);
 		if(!tteMove && depth >= (pv? 5: 8) && (pv || (!ml.kingAttacked[player] && lazyEval+256 >= beta))){
 			int d = pv? depth-2: depth/2;
 			stack[stackIndex+1].skipNullMove = true;
 			recurse(player, alpha, beta, d, pv, rootNode, stackIndex+1);
 			stack[stackIndex+1].skipNullMove = false;
 			final TTEntry temp;
 			if((temp = m.get(zkey)) != null && temp.move != 0){
 				tteMove = true;
 				tteMoveEncoding = temp.move;
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(tteMoveEncoding);
 				moves[w] = 1L<<MoveEncoder.getPos2(tteMoveEncoding);
 				ranks[w++] = tteMoveRank;
 			}
 		}
 
 		//move generation
 		ml.length = w;
 		genMoves(player, s, ml, m, false);
 		final int length = ml.length;
 		if(length == 0){ //no moves, draw
 			//m.put2(zkey, 0, 0, depth, ZMap.CUTOFF_TYPE_EXACT);
 			fillEntry.fill(zkey, 0, 0, depth, TTEntry.CUTOFF_TYPE_EXACT, seq);
 			m.put(zkey, fillEntry);
 			return 0;
 		}
 		isort(pieceMasks, moves, ranks, length);
 		
 		
 		double g = alpha;
 		long bestMove = 0;
 		double bestScore = -99999;
 		int cutoffFlag = ZMap.CUTOFF_TYPE_UPPER;
 		
 		final int drawCount = s.drawCount; //stored for error checking purposes
 		
 		boolean hasMove = ml.kingAttacked[player];
 		for(int i = 0; i < length && !cutoffSearch.get(); i++){
 			for(long movesTemp = moves[i]; movesTemp != 0 ; movesTemp &= movesTemp-1){
 				
 				long encoding = s.executeMove(player, pieceMasks[i], movesTemp&-movesTemp);
 				this.e.processMove(encoding);
 				boolean isDrawable = s.isDrawable(); //player can take a draw
 
 				if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
 					//king in check after move
 					g = -88888+stackIndex+1;
 				} else{
 					hasMove = true;
 					
 					final boolean pvMove = pv && i==0;
 					final boolean isCapture = MoveEncoder.getTakenType(encoding) != State4.PIECE_TYPE_EMPTY;
 					final boolean inCheck = ml.kingAttacked[player];
 					final boolean givesCheck = !ml.kingAttacked[1-player] && State4.isAttacked2(BitUtil.lsbIndex(s.kings[1-player]), player, s);
 					boolean fullSearch = false;
 					
 					if(depth > 2 && !pvMove && !isCapture && !inCheck && !givesCheck && 
 							(encoding&0xFFF) != ml.killer[0] &&
 							(encoding&0xFFF) != ml.killer[1] &&
 							(!tteMove || encoding != tteMoveEncoding)){
 						int reducedDepth = pv? depth-1: depth-2;
 						g = -recurse(1-player, -(alpha+1), -alpha, reducedDepth, false, false, stackIndex+1);
 						fullSearch = g+100 > alpha;
 					} else{
 						fullSearch = true;
 					}
 					
 					if(fullSearch){
 						//descend negascout style
 						if(!pvMove){
 							g = -recurse(1-player, -(alpha+1), -alpha, depth-1, false, false, stackIndex+1);
 							if(alpha < g && g < beta && pv){
 								g = -recurse(1-player, -beta, -alpha, depth-1, pv, false, stackIndex+1);
 							}
 						} else{
 							g = -recurse(1-player, -beta, -alpha, depth-1, pv, false, stackIndex+1);
 						}
 					}
 				}
 				s.undoMove();
 				this.e.undoMove(encoding);
 				assert zkey == s.zkey(); //keys should be unchanged after undo
 				if(drawCount != s.drawCount){
 					System.out.println(drawCount+" != "+s.drawCount);
 				}
 				assert drawCount == s.drawCount;
 				
 				if(isDrawable && 0 > g){ //can take a draw instead of making the move
 					g = 0;
 					encoding = 0;
 				} 
 				
 				if(g > bestScore){
 					bestScore = g;
 					bestMove = encoding;
 					if(g > alpha){
 						alpha = g;
 						cutoffFlag = ZMap.CUTOFF_TYPE_EXACT;
 						if(alpha >= beta){
 							if(!cutoffSearch.get()){
 								//m.put2(zkey, bestMove, alpha, depth, ZMap.CUTOFF_TYPE_LOWER);
 								fillEntry.fill(zkey, encoding, alpha, depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
 								m.put(zkey, fillEntry);
 							}
 							
 							//check to see if killer move can be stored
 							//if used on null moves, need to have a separate killer array
 							if(stackIndex-1 >= 0){
 								attemptKillerStore(bestMove, ml.skipNullMove, stack[stackIndex-1]);
 							}
 							
 							return g;
 						}
 					}
 				}
 			}
 		}
 		
 		if(!hasMove){
 			//no moves except king into death - draw
 			bestMove = 0;
 			bestScore = 0;
 			cutoffFlag = ZMap.CUTOFF_TYPE_EXACT;
 		}
 		
 		if(!cutoffSearch.get()){
 			//m.put2(zkey, bestMove, bestScore, depth, cutoffFlag);
 			fillEntry.fill(zkey, bestMove, bestScore, depth, pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
 			m.put(zkey, fillEntry);
 		}
 		return bestScore;
 	}
 	
 	private double qsearch(int player, double alpha, double beta, int depth, int stackIndex, boolean pv){
 		stats.nodesSearched++;
 		
 		if(depth < -qply){
 			stats.forcedQuietCutoffs++;
 			return beta; //qply bottomed out, return bad value
 		}
 
 		
 		int w = 0;
 		final long[] pieceMasks = stack[stackIndex].pieceMasks; //piece moving
 		final long[] moves = stack[stackIndex].moves; //moves available to piece (can be multiple)
 		final int[] ranks = stack[stackIndex].ranks; //move ranking
 
 		final long zkey = s.zkey();
 		final TTEntry e = m.get(zkey);
 		if(e != null){
 			stats.hashHits++;
 			if(e.depth >= depth){
 				if(pv ? e.cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
 						e.cutoffType == TTEntry.CUTOFF_TYPE_LOWER: e.cutoffType == ZMap.CUTOFF_TYPE_UPPER)){
 					return e.score;
 				}
 			}
 			if(e.move != 0){
 				long encoding = e.move;
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(encoding);
 				moves[w] = 1L<<MoveEncoder.getPos2(encoding);
 				ranks[w++] = tteMoveRank;
 			}
 		}
 		
 		MoveList ml = stack[stackIndex];
 
 		double bestScore = 0;
 		ml.kingAttacked[player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
 		if(ml.kingAttacked[player]){
 			bestScore = -77777;
 		} else{
 			bestScore = this.e.eval(s, player);
 			if(bestScore >= beta){ //standing pat
 				return bestScore;
 			} else if(bestScore > alpha && pv){
 				alpha = bestScore;
 			}
 		}
 		ml.kingAttacked[1-player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[1-player]), player, s);
 		
 		ml.length = w;
 		genMoves(player, s, ml, m, true);
 		final int length = ml.length;
 		isort(pieceMasks, moves, ranks, length);
 
 		
 		double g = alpha;
 		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
 		long bestMove = 0;
 		final int drawCount = s.drawCount; //stored for error checking purposes
 		for(int i = 0; i < length && !cutoffSearch.get(); i++){
 			for(long movesTemp = moves[i]; movesTemp != 0 ; movesTemp &= movesTemp-1){
 				long encoding = s.executeMove(player, pieceMasks[i], movesTemp&-movesTemp);
 				this.e.processMove(encoding);
 				final boolean isDrawable = s.isDrawable();
 				
 				if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
 					//king in check after move
 					g = -77777;
 				} else{
 					g = -qsearch(1-player, -beta, -alpha, depth-1, stackIndex+1, pv);
 				}
 				s.undoMove();
 				this.e.undoMove(encoding);
 				
 				if(isDrawable && 0 > g){// && -10*depth > g){ //can draw instead of making the move
 					g = 0;
 					encoding = 0;
 				} 
 				
 				assert zkey == s.zkey();
 				assert drawCount == s.drawCount;
 				
 				if(g > bestScore){
 					bestScore = g;
 					bestMove = encoding;
 					if(g > alpha){
 						alpha = g;
 						cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
 						if(g >= beta){
 							if(!cutoffSearch.get()){
 								fillEntry.fill(zkey, encoding, g, depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
 								m.put(zkey, fillEntry);
 							}
 							return g;
 						}
 					}
 				}
 			}
 		}
 
 		if(!cutoffSearch.get()){
 			fillEntry.fill(zkey, bestMove, bestScore, depth, pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
 			m.put(zkey, fillEntry);
 		}
 		return bestScore;
 	}
 
 	/**
 	 * checks too see if a move is legal, assumming we do not start in check,
 	 * moving does not yield self check, we are not castling, and if moving a pawn
 	 * we have chosen a non take move that could be legal if no piece is
 	 * blocking the target square
 	 * 
 	 * <p> used to check that killer moves are legal
 	 * @param player
 	 * @param piece
 	 * @param move
 	 * @param s
 	 * @return
 	 */
 	private static boolean isPseudoLegal(final int player, final long encoding, State4 s){
 		final long p = 1L<<MoveEncoder.getPos1(encoding);
 		final long m = 1L<<MoveEncoder.getPos2(encoding);
 		final long[] pieces = s.pieces;
 		if((pieces[player] & p) != 0 && (pieces[player] & pieces[1-player] & m) == 0){
 			final int type = s.mailbox[BitUtil.lsbIndex(p)];
 			switch(type){
 			case State4.PIECE_TYPE_BISHOP:
 				final long tempBishopMoves = State4.getBishopMoves(player, pieces, p);
 				return (m & tempBishopMoves) != 0;
 			case State4.PIECE_TYPE_KNIGHT:
 				final long tempKnightMoves = State4.getKnightMoves(player, pieces, p);
 				return (m & tempKnightMoves) != 0;
 			case State4.PIECE_TYPE_QUEEN:
 				final long tempQueenMoves = State4.getQueenMoves(player, pieces, p);
 				return (m & tempQueenMoves) != 0;
 			case State4.PIECE_TYPE_ROOK:
 				final long tempRookMoves = State4.getRookMoves(player, pieces, p);
 				return (m & tempRookMoves) != 0;
 			case State4.PIECE_TYPE_KING:
 				final long tempKingMoves = State4.getKingMoves(player, pieces, p);
 				return (m & tempKingMoves) != 0;
 			case State4.PIECE_TYPE_PAWN:
 				final long tempPawnMoves = State4.getPawnMoves2(player, pieces, s.pawns[player]);
 				return (m & tempPawnMoves) != 0;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * attempts to store a move as a killer move
 	 * @param move move to be stored
 	 * @param skipNullMove current skip null move status
 	 * @param prev previous move list
 	 */
 	private static void attemptKillerStore(final long move, final boolean skipNullMove, final MoveList prev){
 		assert prev != null;
 		if(move != 0 && !skipNullMove &&
 				(move&0xFFF) != prev.killer[0] &&
 				(move&0xFFF) != prev.killer[1] &&
 				MoveEncoder.getTakenType(move) == State4.PIECE_TYPE_EMPTY &&
 				MoveEncoder.isCastle(move) == 0 &&
 				MoveEncoder.isEnPassanteTake(move) == 0 &&
 				!MoveEncoder.isPawnPromoted(move)){
 			if(prev.killer[0] != move){
 				prev.killer[1] = prev.killer[0];
 				prev.killer[0] = move & 0xFFF;
 			} else{
 				prev.killer[0] = prev.killer[1];
 				prev.killer[1] = move & 0xFFF;
 			}
 		}
 	}
 	
 	/** record moves as blocks*/
 	private static void recordMoves(int player, int pieceMovingType, long pieceMask,
 			long moves, MoveList ml, State4 s, Hash m, boolean quiesce, long retakeMask){
 		final long piece = pieceMask&-pieceMask;
 		if(piece != 0 && moves != 0){
 			final long enemy = s.pieces[1-player];
 			int w = ml.length;
 			if((moves & enemy) != 0){
 				
 				//retakes provides very small gains
 				final long retakes = moves & retakeMask;
 				if(retakes != 0){
 					ml.pieceMasks[w] = piece;
 					ml.moves[w] = retakes;
 					ml.ranks[w] = 2;
 					w++;
 				}
 				
 				final long upTakes = moves & enemy & ml.upTakes[pieceMovingType] & ~retakes;
 				if(upTakes != 0){
 					ml.pieceMasks[w] = piece;
 					ml.moves[w] = upTakes;
 					ml.ranks[w] = 3;
 					w++;
 				}
 				
 				final long takes = moves & enemy & ~ml.upTakes[pieceMovingType] & ~retakes;
 				if(takes != 0){
 					ml.pieceMasks[w] = piece;
 					ml.moves[w] = takes;
 					ml.ranks[w] = 4;
 					w++;
 				}
 			}
 			final long nonTake = moves & ~enemy;
 			if(nonTake != 0 && !quiesce){
 				ml.pieceMasks[w] = piece;
 				ml.moves[w] = nonTake;
 				ml.ranks[w] = 5;
 				w++;
 			}
 			ml.length = w;
 		}
 	}
 	
 	private static void genMoves(final int player, State4 s, MoveList ml, Hash m, boolean quiece){
 		ml.upTakes[State4.PIECE_TYPE_KING] = s.pieces[1-player];
 		ml.upTakes[State4.PIECE_TYPE_QUEEN] = s.queens[1-player]|s.kings[1-player];
 		ml.upTakes[State4.PIECE_TYPE_ROOK] = ml.upTakes[State4.PIECE_TYPE_QUEEN]|s.rooks[1-player];
 		ml.upTakes[State4.PIECE_TYPE_KNIGHT] = ml.upTakes[State4.PIECE_TYPE_ROOK]|s.knights[1-player]|s.bishops[1-player];
 		ml.upTakes[State4.PIECE_TYPE_BISHOP] = ml.upTakes[State4.PIECE_TYPE_KNIGHT];
 		ml.upTakes[State4.PIECE_TYPE_PAWN] = s.pieces[1-player];
 		
 		long retakeMask = 0;
 		/*if(MoveEncoder.getTakenType(ml.prevMove) != State4.PIECE_TYPE_EMPTY){
 			retakeMask = 1L<<MoveEncoder.getPos2(ml.prevMove);
 		}*/
 		
 		if(ml.kingAttacked[player]){
 			long kingMoves = State4.getKingMoves(player, s.pieces, s.kings[player]);
 			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves, ml, s, m, false, retakeMask);
 		}
 		
 		long queens = s.queens[player];
 		recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
 				State4.getQueenMoves(player, s.pieces, queens), ml, s, m, quiece, retakeMask);
 		queens &= queens-1;
 		recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
 				State4.getQueenMoves(player, s.pieces, queens), ml, s, m, quiece, retakeMask);
 		queens &= queens-1;
 		if(queens != 0){
 			while(queens != 0){
 				recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
 						State4.getQueenMoves(player, s.pieces, queens), ml, s, m, quiece, retakeMask);
 				queens &= queens-1;
 			}
 		}
 
 		long rooks = s.rooks[player];
 		recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
 				State4.getRookMoves(player, s.pieces, rooks), ml, s, m, quiece, retakeMask);
 		rooks &= rooks-1;
 		recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
 				State4.getRookMoves(player, s.pieces, rooks), ml, s, m, quiece, retakeMask);
 		
 		long knights = s.knights[player];
 		recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
 				State4.getKnightMoves(player, s.pieces, knights), ml, s, m, quiece, retakeMask);
 		knights &= knights-1;
 		recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
 				State4.getKnightMoves(player, s.pieces, knights), ml, s, m, quiece, retakeMask);
 		
 		long bishops = s.bishops[player];
 		recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
 				State4.getBishopMoves(player, s.pieces, bishops), ml, s, m, quiece, retakeMask);
 		bishops &= bishops-1;
 		recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
 				State4.getBishopMoves(player, s.pieces, bishops), ml, s, m, quiece, retakeMask);
 
 		
 		//handle pawn moves specially
 		final long[] pawnMoves = ml.pawnMoves;
 		final long pawns = s.pawns[player];
 		pawnMoves[0] = State4.getRightPawnAttacks(player, s.pieces, s.enPassante, pawns);
 		pawnMoves[1] = State4.getLeftPawnAttacks(player, s.pieces, s.enPassante, pawns);
 		pawnMoves[2] = State4.getPawnMoves(player, s.pieces, pawns);
 		pawnMoves[3] = State4.getPawnMoves2(player, s.pieces, pawns);
 		
 		int w = ml.length;
 		final long[] pieces = ml.pieceMasks;
 		final long[] moves = ml.moves;
 		final int[] ranks = ml.ranks;
 
 		//pawn movement promotions
 		for(long tempPawnMoves = pawnMoves[2]&Masks.pawnPromotionMask[player]; tempPawnMoves != 0; tempPawnMoves&=tempPawnMoves-1){
 			long moveMask = tempPawnMoves&-tempPawnMoves;
 			long pawnMask = player == 0? moveMask>>>pawnOffset[2]: moveMask<<pawnOffset[2];
 			pieces[w] = pawnMask;
 			moves[w] = moveMask;
 			ranks[w] = 2;
 			w++;
 		}
 		//pawn takes
 		for(int i = 0; i < 2; i++){
 			for(long tempPawnMoves = pawnMoves[i]; tempPawnMoves != 0; tempPawnMoves&=tempPawnMoves-1){
 				long moveMask = tempPawnMoves&-tempPawnMoves;
 				long pawnMask = player == 0? moveMask>>>pawnOffset[i]: moveMask<<pawnOffset[i];
 				pieces[w] = pawnMask;
 				moves[w] = moveMask;
 				ranks[w] = (moveMask & Masks.pawnPromotionMask[player]) == 0? 3: 1;
 				w++;
 			}
 		}
 		if(!quiece){
 			//non-promoting pawn movement
 			for(int i = 2; i < 4; i++){
 				for(long tempPawnMoves = pawnMoves[i]&~Masks.pawnPromotionMask[player];
 						tempPawnMoves != 0; tempPawnMoves&=tempPawnMoves-1){
 					long moveMask = tempPawnMoves&-tempPawnMoves;
 					long pawnMask = player == 0? moveMask>>>pawnOffset[i]: moveMask<<pawnOffset[i];
 					pieces[w] = pawnMask;
 					moves[w] = moveMask;
 					ranks[w] = 5;
 					w++;
 				}
 			}
 		}
 		ml.length = w;
 
 		if(!ml.kingAttacked[player]){
 			long kingMoves = State4.getKingMoves(player, s.pieces, s.kings[player])|State4.getCastleMoves(player, s);
 			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves, ml, s, m, quiece, retakeMask);
 		}
 	}
 	
 	/**
 	 * insertion sort (lowest rank first)
 	 * @param pieceMasks entries to sort
 	 * @param moves moves to sort
 	 * @param rank rank of entries
 	 * @param length number of entries to sort
 	 */
 	private static void isort(final long[] pieceMasks, final long[] moves, final int[] rank, final int length){
 		for(int i = 1; i < length; i++){
 			//for(int a = i; moves[i] != 0 && a > 0 && (rank[a-1]>rank[a] || moves[a-1] == 0); a--){
 			for(int a = i; a > 0 && rank[a-1]>rank[a]; a--){
 				long templ1 = pieceMasks[a];
 				pieceMasks[a] = pieceMasks[a-1];
 				pieceMasks[a-1] = templ1;
 				long templ2 = moves[a];
 				moves[a] = moves[a-1];
 				moves[a-1] = templ2;
 				
 				int tempr = rank[a];
 				rank[a] = rank[a-1];
 				rank[a-1] = tempr;
 			}
 		}
 	}
 
 	@Override
 	public void setListener(SearchListener l) {
 		this.l = l;
 	}
 }
