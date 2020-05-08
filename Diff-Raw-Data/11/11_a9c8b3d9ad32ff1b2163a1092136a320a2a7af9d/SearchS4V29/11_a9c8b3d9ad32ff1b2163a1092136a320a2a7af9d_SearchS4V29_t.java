 package search;
 
 import java.io.FileWriter;
 import java.io.IOException;
 
 import util.board4.BitUtil;
 import util.board4.Masks;
 import util.board4.MoveEncoder;
 import util.board4.State4;
 import util.board4.ZMap;
 import ai.modularAI2.Evaluator2;
 
 /** very similar to v27, slightly fewer nodes searched*/
 public final class SearchS4V29 implements Search3{
 	public final static class SearchStat27 extends SearchStat{
 		public long hashHits;
 		/** scores returned from quiet search without bottoming out*/
 		public long forcedQuietCutoffs;
 		public long nullMoveVerifications;
 		public long nullMoveCutoffs;
 		public long nullMoveFailLow;
 		/** scores seen pes ply*/
 		public double[] scores;
 		
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
 		public long prevMove;
 	}
 	
 	private final static int[] pawnOffset = new int[]{9,7,8,16};
 	
 	private final State4 s;
 	private final SearchStat27 stats = new SearchStat27();
 	private final Evaluator2<State4> e;
 	private final int qply = 8;
 	private final ZMap m;
 	private FileWriter f;
 	
 	/** stores history heuristic information*/
 	private final static int tteMoveRank = -1;
 	
 	private boolean cutoffSearch = false;
 	
 	public SearchS4V29(int maxDepth, State4 s, Evaluator2<State4> e, int hashSize, boolean record){
 		this.s = s;
 		this.e = e;
 		m = new ZMap(hashSize);
 		stack = new MoveList[(maxDepth+qply)*2];
 		for(int i = 0; i < stack.length; i++){
 			stack[i] = new MoveList();
 		}
 		stats.scores = new double[64];
 		
 
 		if(record){
 			try{
 				f = new FileWriter("search29.stats", true);
 			} catch(IOException ex){
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	public SearchStat27 getStats(){
 		return stats;
 	}
 	
 	public void search(int player, int[] moveStore){
 		search(player, moveStore, -1);
 	}
 	
 	public void search(int player, int[] moveStore, int maxPly){
 		stats.nodesSearched = 0;
 		stats.hashHits = 0;
 		stats.forcedQuietCutoffs = 0;
 		stats.nullMoveVerifications = 0;
 		stats.nullMoveCutoffs = 0;
 		stats.searchTime = System.currentTimeMillis();
 		
 		//search initialization
 		m.incSeq();
 		e.initialize(s);
 		cutoffSearch = false;
 		
 		long bestMove = 0;
 		double score = 0;
 		
 		final double max = 90000;
 		final double min = -90000;
 		
 		System.out.println("searching with player = "+player);
 		
 		for(int i = 1; (maxPly == -1 || i <= maxPly) && !cutoffSearch; i++){
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
 			
 			
 			if(alpha > beta){
 				final double temp = alpha;
 				alpha = beta;
 				beta = temp;
 			}
 			
 			//System.out.println("starting depth "+i);
 			stack[0].prevMove = 0;
 			score = recurse(player, alpha, beta, i, true, true, 0);
 			
 			
 			if(score <= alpha && !cutoffSearch){
 				//System.out.println("search failed low, researching");
 				alpha = score-100;
 				beta = score+5;
 				if(alpha > beta){
 					double temp = alpha;
 					alpha = beta;
 					beta = temp;
 				}
 				stack[0].prevMove = 0;
 				score = recurse(player, alpha, beta, i, true, true, 0);
 				if((score <= alpha || score >= beta)  && !cutoffSearch){
 					//System.out.println("double fail");
 					alpha = min;
 					beta = max;
 					stack[0].prevMove = 0;
 					score = recurse(player, alpha, beta, i, true, true, 0);
 				}
 			} else if(score >= beta && !cutoffSearch){
 				//System.out.println("search failed high, researching");
 				alpha = score-5;
 				beta = score+100;
 				if(alpha > beta){
 					double temp = alpha;
 					alpha = beta;
 					beta = temp;
 				}
 				stack[0].prevMove = 0;
 				score = recurse(player, alpha, beta, i, true, true, 0);
 				if((score <= alpha || score >= beta)  && !cutoffSearch){
 					//System.out.println("double fail");
 					alpha = min;
 					beta = max;
 					stack[0].prevMove = 0;
 					score = recurse(player, alpha, beta, i, true, true, 0);
 				}
 			}
 			if(m.get(s.zkey()) != null && m.get(s.zkey()).encoding != 0 && !cutoffSearch){
 				bestMove = m.get(s.zkey()).encoding;
 				//System.out.println("info depth "+i+" nodes "+stats.nodesSearched+" score cp "+score);
 				System.out.println("pv "+i+": ["+score+"] "+getPVString(player, s, "", 0, i));
 			}
 			if(i-1 < stats.scores.length){
 				stats.scores[i-1] = score;
 			}
 		}
 		
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
 		//System.out.println(stats.nullMoveFailLow);
 		//System.out.println(pos1+" -> "+pos2);
 		//System.out.println("final score = "+score);
 		/*System.out.println("total nodes searched = "+stats.nodesSearched);
 		System.out.println("total time (sec) = "+(stats.searchTime/1000.));
 		System.out.println("nodes/sec = "+(stats.nodesSearched*1000./stats.searchTime));
 		System.out.println("hash hits = "+stats.hashHits);
 		System.out.println("quiet forced cutoffs = "+stats.forcedQuietCutoffs);
 		System.out.println("null move verification searches = "+stats.nullMoveVerifications);
 		System.out.println("null move cutoffs = "+stats.nullMoveCutoffs);*/
 		
 		/*System.out.println("castle props:");
 		System.out.println("white: casled = "+s.isCastled[0]+" (king moved = "+s.kingMoved[0]+
 				", left rook move = "+s.rookMoved[0][0]+
 				", right rook moved = "+s.rookMoved[0][1]+")");
 		System.out.println("black: castled = "+s.isCastled[1]+" (king moved = "+s.kingMoved[1]+
 				", left rook move = "+s.rookMoved[1][0]+
 				", right rook moved = "+s.rookMoved[1][1]+")");*/
 		
 		
 		//System.out.println(stats);
 	}
 	
 	private String getPVString(int player, State4 s, String pv, int depth, int maxDepth){
 		ZMap.Entry e = m.get(s.zkey());
 		if(depth < maxDepth && e != null && e.encoding != 0){
 			int pos1 = MoveEncoder.getPos1(e.encoding);
 			int pos2 = MoveEncoder.getPos2(e.encoding);
 			
 			double eval = this.e.eval(s, player);
 			
 			pv += moveString(pos1)+"->"+moveString(pos2)+" ("+eval+"), ";
 			long pmask = 1L<<pos1;
 			long mmask = 1L<<pos2;
 			s.executeMove(player, pmask, mmask);
 			String r = getPVString(1-player, s, pv, depth+1, maxDepth);
 			s.undoMove();
 			return r;
 		}
 		return pv;
 	}
 	
 	public long[] recoverPV(int player, int maxDepth, double endScore){
 		final long[] l = new long[maxDepth];
 		cutoffSearch = false;
 		recoverPVHelper(player, s, maxDepth, maxDepth, l, endScore);
 		return l;
 	}
 	
 	private void recoverPVHelper(int player, State4 s, int depth, int maxDepth, long[] pv, double endScore){
 		if(depth <= 0)
 			return;
 		
 		ZMap.Entry e = m.get(s.zkey());
 		if(e != null && e.encoding != 0){
 			pv[maxDepth-1-(depth-1)] = e.encoding;
 			
 			int pos1 = MoveEncoder.getPos1(e.encoding);
 			int pos2 = MoveEncoder.getPos2(e.encoding);
 			long pmask = 1L<<pos1;
 			long mmask = 1L<<pos2;
 			s.executeMove(player, pmask, mmask);
 			recoverPVHelper(1-player, s, depth-1, maxDepth, pv, endScore);
 			s.undoMove();
 		} else{
 			double alpha = -(endScore+3);
 			double beta = endScore+3;
 			boolean isPV = true;
 			recurse(player, alpha, beta, depth, isPV, false, 0);
 			
 			
 
 			/*double eval = this.e.eval(s, player);
 			System.out.println("fail at depth "+depth+" eval="+eval);
 			ZMap.Entry a = m.get(s.zkey());
 			if(a == null){
 				System.out.println(s);
 				for(long encoding: pv){
 					System.out.println(MoveEncoder.getString(encoding));
 				}
 				System.exit(0);
 			}*/
 			
 			
 			
 			recoverPVHelper(player, s, depth, maxDepth, pv, endScore);
 		}
 	}
 	
 	private static String moveString(int pos){
 		return ""+(char)('A'+(pos%8))+(pos/8+1);
 	}
 	
 	public void cutoffSearch(){
 		cutoffSearch = true;
 	}
 	
 	/**
 	 * recurse and search
 	 * @param player
 	 * @param depth current depth
 	 * @param alpha
 	 * @param beta
 	 * @param softMaxDepth max depth, can be increased by extensions
 	 * @param hardMaxDepth hard max depth, search cuts off here regardless of soft max depth value
 	 * @param quiesce
 	 * @return
 	 */
 	private double recurse(int player, double alpha, double beta, int depth,
 			boolean pv, boolean rootNode, int stackIndex){
 		stats.nodesSearched++;
 		
 		if(depth <= 0){
 			//dont descend into quiescent search until out of check
 			final boolean inCheck = State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
 			if(!inCheck){
 				return qsearch(player, alpha, beta, 0, stackIndex);
 			}
 			depth = 1;
 			return qsearch(player, alpha, beta, 0, stackIndex);
 		}
 		
 
 		final long[] pieceMasks = stack[stackIndex].pieceMasks; //piece moving
 		final long[] moves = stack[stackIndex].moves; //moves available to piece (can be multiple)
 		final int[] ranks = stack[stackIndex].ranks; //move ranking
 		int w = 0;
 
 		final ZMap.Entry e = m.get(s.zkey());
 		boolean tteMove = false;
 		
 		if(e != null){
 			stats.hashHits++;
 			if(e.depth >= depth){ //check depth on hash entry greater than or equal to current
 				if(e.cutoffType == ZMap.CUTOFF_TYPE_UPPER && !pv){
 					if(e.score <= alpha){
 						return alpha;
 					} else if(e.score < beta){
 						beta = e.score;
 					}
 				} else if(e.cutoffType == ZMap.CUTOFF_TYPE_LOWER && !pv){
 					if(e.score >= beta){
 						return beta;
 					} else if(e.score > alpha){
 						alpha = e.score;
 					}
 				} else if(e.cutoffType == ZMap.CUTOFF_TYPE_EXACT){
 					//exact score
 					return e.score;
 				}
 			}
 			if(e.encoding != 0){
 				long encoding = e.encoding;
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(encoding);
 				moves[w] = 1L<<MoveEncoder.getPos2(encoding);
 				ranks[w++] = tteMoveRank;
 				tteMove = true;
 			}
 		}
 		
 		final MoveList ml = stack[stackIndex];
 		ml.kingAttacked[player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
 		ml.kingAttacked[1-player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[1-player]), player, s);
 		
 		//null move pruning (hashes result, but might not be sound)
 		boolean hasNonPawnMaterial = s.pieceCounts[player][0]-s.pieceCounts[player][State4.PIECE_TYPE_PAWN] > 1;
 		if(!pv && !ml.skipNullMove && depth > 0 &&  !cutoffSearch &&
 				!ml.kingAttacked[player] && hasNonPawnMaterial){
 			
 			int r = 3 + depth/4;
 			
 			stack[stackIndex+1].skipNullMove = true;
 			stack[stackIndex+1].prevMove = 0;
 			s.nullMove();
 			double n = -recurse(1-player, -beta, -alpha, depth-r, pv, rootNode, stackIndex+1);
 			//double n = -recurse(1-player, -beta-1, -beta, depth-r, pv, rootNode, stackIndex+1);
 			s.undoNullMove();
 			stack[stackIndex+1].skipNullMove = false;
 			
 			if(n >= beta){
 				//if(n == 88888 || n == 77777){
 				if(n >= 70000){
 					n = beta;
 				}
 				if(depth < 6){
 					stats.nullMoveCutoffs++;
 					//if(!cutoffSearch) m.put2(s.zkey(), 0, n, depth, ZMap.CUTOFF_TYPE_LOWER);
 					return n;
 				}
 				
 				stats.nullMoveVerifications++;
 				//verification search
 				stack[stackIndex+1].skipNullMove = true;
 				double v = recurse(player, alpha, beta, depth-r, pv, rootNode, stackIndex+1);
 				stack[stackIndex+1].skipNullMove = false;
 				if(v >= beta){
 					stats.nullMoveCutoffs++;
 					//if(!cutoffSearch) m.put2(s.zkey(), 0, n, depth, ZMap.CUTOFF_TYPE_LOWER);
 					return n;
 				}
 			} else if(n <= alpha){
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
 			ZMap.Entry temp = null;
 			if((temp = m.get(s.zkey())) != null && temp.encoding != 0){
 				tteMove = true;
 				long encoding = temp.encoding;
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(encoding);
 				moves[w] = 1L<<MoveEncoder.getPos2(encoding);
 				ranks[w++] = tteMoveRank;
 			}
 		}
 
 		//move generation
 		ml.length = w;
 		genMoves(player, s, ml, m, false);
 		final int length = ml.length;
 		if(length == 0){ //no moves, draw
 			m.put2(s.zkey(), 0, 0, depth, ZMap.CUTOFF_TYPE_EXACT);
 			return 0;
 		}
 		isort(pieceMasks, moves, ranks, length);
 		
 		
 		double g = alpha;
 		long bestMove = 0;
 		double bestScore = 0;
 		int cutoffFlag = ZMap.CUTOFF_TYPE_UPPER;
 		
 		//final long enemy = s.pieces[1-player]|s.enPassante;
 		final long zkey = s.zkey(); //for testing purposes
 		
 		boolean firstRun = true;
 		boolean hasMove = ml.kingAttacked[player];
 		for(int i = 0; i < length && !cutoffSearch; i++){
 			for(long movesTemp = moves[i]; movesTemp != 0 ; movesTemp &= movesTemp-1){
 				
 				long encoding = s.executeMove(player, pieceMasks[i], movesTemp&-movesTemp);
 				this.e.processMove(encoding);
 				stack[stackIndex+1].prevMove = encoding;
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
 					
 					if(depth > 2 && !pvMove && !isCapture && !inCheck && !givesCheck){
 						int reducedDepth = pv? depth-2: depth/2;
 						g = -recurse(1-player, -(alpha+1), -alpha, reducedDepth, false, false, stackIndex+1);
 						fullSearch = g > alpha;
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
 						/*if(alphaRaised || !pv){
 							g = -recurse(1-player, -(alpha+1), -alpha, depth-1, false, false, stackIndex+1);
 							if(alpha < g && g < beta && pv){
 								g = -recurse(1-player, -beta, -alpha, depth-1, pv, false, stackIndex+1);
 							}
 						} else{
 							g = -recurse(1-player, -beta, -alpha, depth-1, pv, false, stackIndex+1);
 						}*/
 					}
 				}
 				s.undoMove();
 				this.e.undoMove(encoding);
 				assert zkey == s.zkey(); //keys should be unchanged after undo
 				
 				if(isDrawable && 0 > g){// && -10*depth > g){ //can draw instead of making the move
 					g = 0;
 					encoding = 0;
 				} 
 				
 				if(firstRun || g > bestScore){
 					bestScore = g;
 					bestMove = encoding;
 					firstRun = false;
 				}
 				
 				if(g > alpha){
 					alpha = g;
 					cutoffFlag = ZMap.CUTOFF_TYPE_EXACT;
 				}
 				if(alpha >= beta){
 					if(!cutoffSearch){
 						m.put2(s.zkey(), bestMove, alpha, depth, ZMap.CUTOFF_TYPE_LOWER);
 					}
 					return g;
 				}
 			}
 		}
 		
 		if(!hasMove){
 			//no moves except king into death - draw
 			bestMove = 0;
 			bestScore = 0;
 			cutoffFlag = ZMap.CUTOFF_TYPE_EXACT;
 		}
 		
 		if(!cutoffSearch)
 				m.put2(s.zkey(), bestMove, bestScore, depth, cutoffFlag);
 		return bestScore;
 	}
 	
 	private double qsearch(int player, double alpha, double beta, int depth, int stackIndex){
 		stats.nodesSearched++;
 		
 		if(depth < -qply){
 			stats.forcedQuietCutoffs++;
 			return beta; //qply bottomed out, return bad value
 		}
 
 		
 		int w = 0;
 		final long[] pieceMasks = stack[stackIndex].pieceMasks; //piece moving
 		final long[] moves = stack[stackIndex].moves; //moves available to piece (can be multiple)
 		final int[] ranks = stack[stackIndex].ranks; //move ranking
 		
 		final ZMap.Entry e = m.get(s.zkey());
 		if(e != null){
 			stats.hashHits++;
 			if(e.depth >= depth){ //check depth on hash entry greater than or equal to current
 				if(e.cutoffType == ZMap.CUTOFF_TYPE_UPPER){
 					if(e.score <= alpha){
 						return alpha;
 					} else if(e.score < beta){
 						beta = e.score;
 					}
 				} else if(e.cutoffType == ZMap.CUTOFF_TYPE_LOWER){
 					if(e.score >= beta){
 						return beta;
 					} else if(e.score > alpha){
 						alpha = e.score;
 					}
 				} else if(e.cutoffType == ZMap.CUTOFF_TYPE_EXACT){
 					return e.score;
 				}
 			}
 			if(e.encoding != 0){
 				long encoding = e.encoding;
 				pieceMasks[w] = 1L<<MoveEncoder.getPos1(encoding);
 				moves[w] = 1L<<MoveEncoder.getPos2(encoding);
 				ranks[w++] = 0;
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
 			} else if(bestScore > alpha){
 				alpha = bestScore;
 			}
 		}
 		ml.kingAttacked[1-player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[1-player]), player, s);
 		
 		ml.length = w;
 		genMoves(player, s, ml, m, true);
 		final int length = ml.length;
 		isort(pieceMasks, moves, ranks, length);
 
 		
 		double g = alpha;
 		int cutoffFlag = ZMap.CUTOFF_TYPE_UPPER;
 		final long zkey = s.zkey(); //for testing purposes
 		
 		for(int i = 0; i < length && !cutoffSearch; i++){
 			for(long movesTemp = moves[i]; movesTemp != 0 ; movesTemp &= movesTemp-1){
 				long encoding = s.executeMove(player, pieceMasks[i], movesTemp&-movesTemp);
 				this.e.processMove(encoding);
				final boolean isDrawable = s.isDrawable();
 				
 				if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
 					//king in check after move
 					g = -77777;
 				} else{
 					g = -qsearch(1-player, -(alpha+1), -alpha, depth-1, stackIndex+1);
 					if(alpha < g && g < beta){
 						g = -qsearch(1-player, -beta, -alpha, depth-1, stackIndex+1);
 					}
 				}
 				s.undoMove();
 				this.e.undoMove(encoding);
 				
 				if(isDrawable && 0 > g){// && -10*depth > g){ //can draw instead of making the move
 					g = 0;
 					encoding = 0;
 				} 
 				
 				assert zkey == s.zkey();
 				
 				if(g > bestScore){
 					bestScore = g;
 				}
 				
 				if(g > alpha){
 					alpha = g;
 					if(g >= beta){
 						if(!cutoffSearch)
 							m.put2(s.zkey(), encoding, g, depth, ZMap.CUTOFF_TYPE_LOWER);
 						return g;
 					}
 					cutoffFlag = ZMap.CUTOFF_TYPE_EXACT;
 				}
 			}
 		}
 
 		if(!cutoffSearch)
 			m.put2(s.zkey(), 0, bestScore, depth, cutoffFlag);
 		return bestScore;
 	}
 	
 	/** record moves as blocks*/
 	private static void recordMoves(int player, int pieceMovingType, long pieceMask,
 			long moves, MoveList ml, State4 s, ZMap m, boolean quiesce, long retakeMask){
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
 			/*if(!quiesce){
 				long hashMove = 0;
 				long nonHashMove = 0;
 				for(long nonTake = moves & ~enemy; nonTake != 0; nonTake &= nonTake-1){
 					final long t = nonTake & -nonTake;
 					s.executeMove(player, piece, t);
 					final ZMap.Entry e = m.get(s.zkey());
 					if(e != null && e.cutoffType == ZMap.CUTOFF_TYPE_EXACT){
 						hashMove |= t;
 					} else{
 						nonHashMove |= t;
 					}
 					s.undoMove();
 				}
 				if(hashMove != 0){
 					ml.pieceMasks[w] = piece;
 					ml.moves[w] = hashMove;
 					ml.ranks[w] = 1;
 					w++;
 				}
 				if(nonHashMove != 0){
 					ml.pieceMasks[w] = piece;
 					ml.moves[w] = nonHashMove;
 					ml.ranks[w] = 5;
 					w++;
 				}
 			}*/
 			ml.length = w;
 		}
 	}
 	
 	private static void genMoves(final int player, State4 s, MoveList ml, ZMap m, boolean quiece){
 		ml.upTakes[State4.PIECE_TYPE_KING] = s.pieces[1-player];
 		ml.upTakes[State4.PIECE_TYPE_QUEEN] = s.queens[1-player]|s.kings[1-player];
 		ml.upTakes[State4.PIECE_TYPE_ROOK] = ml.upTakes[State4.PIECE_TYPE_QUEEN]|s.rooks[1-player];
 		ml.upTakes[State4.PIECE_TYPE_KNIGHT] = ml.upTakes[State4.PIECE_TYPE_ROOK]|s.knights[1-player]|s.bishops[1-player];
 		ml.upTakes[State4.PIECE_TYPE_BISHOP] = ml.upTakes[State4.PIECE_TYPE_KNIGHT];
 		ml.upTakes[State4.PIECE_TYPE_PAWN] = s.pieces[1-player];
 		
 		long retakeMask = 0;
 		if(MoveEncoder.getTakenType(ml.prevMove) != State4.PIECE_TYPE_EMPTY){
 			retakeMask = 1L<<MoveEncoder.getPos2(ml.prevMove);
 		}
 		
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
 }
