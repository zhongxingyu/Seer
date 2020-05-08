 package eval.e9;
 
 import state4.BitUtil;
 import state4.Masks;
 import state4.MoveEncoder;
 import state4.State4;
 import eval.Evaluator3;
 import eval.PositionMasks;
 import eval.ScoreEncoder;
 
 public final class E9v3 implements Evaluator3{
 	
 	//evaluation stage flags, used to denote which eval stage is complete
 	private final static int stage1Flag = 1 << 0;
 	private final static int stage2Flag = 1 << 1;
 	private final static int stage3Flag = 1 << 2;
 	private final static int evalCompleteMask = stage1Flag | stage2Flag | stage3Flag;
 	
 	/**
 	 * gives bonus multiplier to the value of sliding pieces
 	 * mobilitiy scores based on how cluttered the board is
 	 * <p>
 	 * sliding pieces with high movement on a clutterd board
 	 * are more valuable
 	 * <p>
 	 * indexed [num-pawn-attacked-squares]
 	 */
 	private final static double[] clutterIndex; 
 	
 	private final static int[] kingDangerTable;
 	
 	private final static int[] materialWeights = new int[7];
 	
 	private final static int tempoWeight = S(14, 5);
 	private final static int bishopPairWeight = S(10, 42);
 
 	private final static int[][] mobilityWeights = new int[7][];
 	
 	private final static int[][] isolatedPawns = new int[][]{{
 		S(-15,-10), S(-18,-15), S(-20,-19), S(-22,-20), S(-22,-20), S(-20,-19), S(-18,-15), S(-15,-10)},
 		{S(-10, -14), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-10, -14)},
 	};
 
 	private final static int[] pawnChain = new int[]{
 		S(13,0), S(15,0), S(18,1), S(22,5), S(22,5), S(18,1), S(15,0), S(13,0)
 	};
 
 	private final static int[][] doubledPawns = new int[][]{
 		{S(-9,-18), S(-12,-19), S(-13,-19), S(-13,-19), S(-13,-19), S(-13,-19), S(-12,-19), S(-12,-18)},
 		{S(-6,-13), S(-8,-16), S(-9,-17), S(-9,-17), S(-9,-17), S(-9,-17), S(-8,-16), S(-6,-13)},
 	};
 
 	private final static int[][] backwardPawns = new int[][]{
 		{S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),},
 		{S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),},
 	};
 	
 	private final static int[][] pawnShelter = new int[][]{ //only need 7 indeces, pawn cant be on last row
 			{0, 30, 20, 8, 2, 0, 0},
 			{0, 75, 38, 20, 5, 0, 0},
 			//{0, 61, 45, 17, 5, 0, 0},
 			//{0, 141, 103, 39, 13, 0, 0},
 	};
 	
 	private final static int[][] pawnStorm = new int[][]{ //indexed [type][distance]
 			{-25, -20, -18, -14, -8, 0}, //no allied pawn
 			{-20, -18, -14, -10, -6, 0}, //has allied pawn, enemy not blocked
 			{-10, -8, -6, -4, -1, 0}, //enemy pawn blocked by allied pawn
 	};
 
 	private final static int[][] kingDangerSquares = {
 			{
 				2,  0,  2,  3,  3,  2,  0,  2,
 				2,  2,  4,  8,  8,  4,  2,  2,
 				7, 10, 12, 12, 12, 12, 10,  7,
 				15, 15, 15, 15, 15, 15, 15, 15,
 				15, 15, 15, 15, 15, 15, 15, 15,
 				15, 15, 15, 15, 15, 15, 15, 15,
 				15, 15, 15, 15, 15, 15, 15, 15,
 				15, 15, 15, 15, 15, 15, 15, 15
 			}, new int[64]
 	};
 
 	private final static int[] zeroi = new int[2];
 	
 	private final int[] materialScore = new int[2];
 	
 	/** margin for scaling scores from midgame to endgame
 	 * <p> calculated by difference between midgame and endgame material*/
 	private final int scaleMargin;
 	private final int endMaterial;
 	
 	/** stores score for non-pawn material*/
 	private final int[] nonPawnMaterial = new int[2];
 	/** stores attack mask for all pieces for each player, indexed [player][piece-type]*/
 	private final long[] attackMask = new long[2];
 	
 	//cached values
 	/** stores total king distance from allied pawns*/
 	private final int[] kingPawnDist = new int[2];
 	private final PawnHash pawnHash;
 	final PawnHashEntry filler = new PawnHashEntry();
 
 	static{
 		//clutterIndex calculated by linear interpolation
 		clutterIndex = new double[64];
 		final double start = .9;
 		final double end = 1.1;
 		final double diff = end-start;
 		for(int a = 0; a < 64; a++){
 			clutterIndex[a] = start + diff*(a/64.);
 		}
 		
 		kingDangerTable = new int[128];
 		final int maxSlope = 30;
 		final int maxDanger = 1280;
 		for(int x = 0, i = 0; i < kingDangerTable.length; i++){
 			x = Math.min(maxDanger, Math.min((int)(i*i*.4), x + maxSlope));
 			kingDangerTable[i] = S(-x, 0);
 		}
 
 		for(int a = 0; a < 64; a++) kingDangerSquares[1][a] = kingDangerSquares[0][63-a];
 		
 		materialWeights[State4.PIECE_TYPE_QUEEN] = 900;
 		materialWeights[State4.PIECE_TYPE_ROOK] = 470;
 		materialWeights[State4.PIECE_TYPE_BISHOP] = 306;
 		materialWeights[State4.PIECE_TYPE_KNIGHT] = 301;
 		materialWeights[State4.PIECE_TYPE_PAWN] = 100;
 
 		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new int[]{
 				S(-19,-49), S(-13,-40), S(-6,-27), S(-1,0), S(7,2),
 				S(12,10), S(14,28), S(16,44), S(17,48)
 		};
 		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new int[]{
 				S(-13,-30), S(-6,-20), S(1,-18), S(7,-10), S(15,-1),
 				S(24,8), S(28,14), S(24,18), S(30,20), S(34,23),
 				S(38,25), S(43,31), S(49,32), S(55,37), S(55,38), S(55,38)
 		};
 		mobilityWeights[State4.PIECE_TYPE_ROOK] = new int[]{
 				S(-10,-69), S(-7,-47), S(-4,-43), S(-1,-10), S(2,13), S(5,26),
 				S(7,35), S(10,43), S(11,50), S(12,56), S(12,60), S(13,63),
 				S(14,66), S(15,69), S(15,74), S(17,74)
 		};
 		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new int[]{
 				S(-6,-69), S(-4,-49), S(-2,-45), S(-2,-28), S(-1,-9), S(0,10),
 				S(1,15), S(2,20), S(4,25), S(5,30), S(6,30), S(7,30), S(8,30),
 				S(8,30), S(9,30), S(10,35), S(12,35), S(14,35), S(15,35), S(15,35),
 				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35),
 				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35)
 		};
 	}
 	
 	public E9v3(){
 		this(16);
 	}
 	
 	public E9v3(int pawnHashSize){
 		int startMaterial = (
 				  materialWeights[State4.PIECE_TYPE_PAWN]*8
 				+ materialWeights[State4.PIECE_TYPE_KNIGHT]*2
 				+ materialWeights[State4.PIECE_TYPE_BISHOP]*2
 				+ materialWeights[State4.PIECE_TYPE_ROOK]*2
 				+ materialWeights[State4.PIECE_TYPE_QUEEN]
 				) * 2;
 		endMaterial = (
 				  materialWeights[State4.PIECE_TYPE_ROOK]
 				+ materialWeights[State4.PIECE_TYPE_QUEEN]
 				) * 2;
 		
 		scaleMargin = scaleMargin(startMaterial, endMaterial);
 		
 		pawnHash = new PawnHash(pawnHashSize, 16);
 	}
 	
 	/** build a weight scaling from passed start,end values*/
 	private static int S(int start, int end){
 		return Weight.encode(start, end);
 	}
 	
 	/** build a constant, non-scaling weight*/
 	private static int S(final int v){
 		return Weight.encode(v);
 	}
 	
 	/**  calculates the scale margin to use in {@link #getScale(int, int, int)}*/
 	private static int scaleMargin(final int startMaterial, final int endMaterial){
 		return endMaterial-startMaterial;
 	}
 	
 	/** gets the interpolatino factor for the weight*/
 	private static double getScale(final int totalMaterialScore, final int endMaterial, final int margin){
 		return min(1-(endMaterial-totalMaterialScore)*1./margin, 1);
 	}
 	
 	@Override
 	public int eval(final int player, final State4 s) {
 		return refine(player, s, -90000, 90000, 0);
 	}
 
 	@Override
 	public int eval(final int player, final State4 s, final int lowerBound, final int upperBound) {
 		return refine(player, s, lowerBound, upperBound, 0);
 	}
 
 	@Override
 	public int refine(final int player, final State4 s, final int lowerBound,
 			final int upperBound, final int scoreEncoding) {
 		
 		int score = ScoreEncoder.getScore(scoreEncoding);
 		int margin = ScoreEncoder.getMargin(scoreEncoding);
 		int flags = ScoreEncoder.getFlags(scoreEncoding);
 		
 		if((flags != 0 && (score+margin <= lowerBound || score+margin >= upperBound)) ||
 				(evalCompleteMask & flags) == evalCompleteMask){
 			return scoreEncoding;
 		}
 
 		final int totalMaterialScore = materialScore[0]+materialScore[1];
 		final double scale = getScale(totalMaterialScore, endMaterial, scaleMargin);
 		
 		final int pawnType = State4.PIECE_TYPE_PAWN;
 		final int pawnWeight = materialWeights[pawnType];
 		nonPawnMaterial[0] = materialScore[0]-s.pieceCounts[0][pawnType]*pawnWeight;
 		nonPawnMaterial[1] = materialScore[1]-s.pieceCounts[1][pawnType]*pawnWeight;
 		
 		final long alliedQueens = s.queens[player];
 		final long enemyQueens = s.queens[1-player];
 		final long queens = alliedQueens | enemyQueens;
 		
 		if(flags == 0){
 			flags |= stage1Flag;
 			
 			//load hashed pawn values, if any
 			final long pawnZkey = s.pawnZkey();
 			final PawnHashEntry phEntry = pawnHash.get(pawnZkey);
 			final PawnHashEntry loader;
 			if(phEntry == null){
 				filler.passedPawns = 0;
 				filler.zkey = 0;
 				loader = filler;
 			} else{
 				loader = phEntry;
 			}
 			
 			//System.out.println("material score = "+(materialScore[player] - materialScore[1-player]));
 			int stage1Score = S(materialScore[player] - materialScore[1-player]);
 			stage1Score += tempoWeight;
 			
 			if(s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2){
 				stage1Score += bishopPairWeight;
 			}
 			if(s.pieceCounts[1-player][State4.PIECE_TYPE_BISHOP] == 2){
 				stage1Score += -bishopPairWeight;
 			}
 			
 			stage1Score += scorePawns(player, s, loader, enemyQueens) - scorePawns(1-player, s, loader, alliedQueens);
 			
 			if(phEntry == null){ //store newly calculated pawn values
 				loader.zkey = pawnZkey;
 				pawnHash.put(pawnZkey, loader);
 			}
 			
 			final int stage1MarginLower; //margin for a lower cutoff
 			final int stage1MarginUpper; //margin for an upper cutoff
 			if(alliedQueens != 0 && enemyQueens != 0){
 				//both sides have queen, apply even margin
 				stage1MarginLower = 120;
 				stage1MarginUpper = -120;
 			} else if(alliedQueens != 0){
 				//score will be higher because allied queen, no enemy queen
 				stage1MarginLower = 140;
 				stage1MarginUpper = -100;
 			} else if(enemyQueens != 0){
 				//score will be lower because enemy queen, no allied queen
 				stage1MarginLower = 100;
 				stage1MarginUpper = -140;
 			} else{
 				stage1MarginLower = 90;
 				stage1MarginUpper = -90;
 			}
 			
 			score = Weight.interpolate(stage1Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage1Score)*.1), 0), scale);
 			
 			if(score+stage1MarginLower <= lowerBound){
 				return ScoreEncoder.encode(score, stage1MarginLower, flags);
 			}
 			if(score+stage1MarginUpper >= upperBound){
 				return ScoreEncoder.encode(score, stage1MarginUpper, flags);
 			}
 		}
 		
 		if((flags & stage2Flag) == 0){
 			flags |= stage2Flag;
 
 			final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
 			final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
 			final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
 			final double clutterMult = clutterIndex[(int)BitUtil.getSetBits(pawnAttacks)];
 			
 			final int stage2Score = scoreMobility(player, s, clutterMult, nonPawnMaterial, attackMask) -
 					scoreMobility(1-player, s, clutterMult, nonPawnMaterial, attackMask);
 			score += Weight.interpolate(stage2Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage2Score)*.1), 0), scale);
 			if(queens == 0){
 				flags |= stage3Flag;
 				return ScoreEncoder.encode(score, 0, flags);
 			} else{
 				//stage 2 margin related to how much we expect the score to change
 				//maximally due to king safety
 				final int stage2MarginLower;
 				final int stage2MarginUpper;
 				if(alliedQueens != 0 && enemyQueens != 0){
 					//both sides have queen, apply even margin
 					stage2MarginLower = 80;
 					stage2MarginUpper = -50;
 				} else if(alliedQueens != 0){
 					//score will be higher because allied queen, no enemy queen
 					stage2MarginLower = 110;
 					stage2MarginUpper = -20;
				}  else if(enemyQueens != 0){
 					//score will be lower because enemy queen, no allied queen
					stage2MarginLower = 20;
					stage2MarginUpper = -110;
				} else{
					//both sides no queen, aplly even margin
 					stage2MarginLower = 50;
					stage2MarginUpper = -50;
 				}
 				
 				if(score+stage2MarginLower <= lowerBound){
 					return ScoreEncoder.encode(score, stage2MarginLower, flags);
 				} if(score+stage2MarginUpper >= upperBound){
 					return ScoreEncoder.encode(score, stage2MarginUpper, flags);
 				} else{
 					flags |= stage3Flag;
 					//margin cutoff failed, calculate king safety scores
 					final int stage3Score = evalKingSafety(player, s, alliedQueens, enemyQueens);
 
 					score += Weight.interpolate(stage3Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage3Score)*.1), 0), scale);
 					
 					return ScoreEncoder.encode(score, 0, flags);
 				}
 			}
 		}
 		
 		if((flags & stage3Flag) == 0){
 			assert queens != 0; //should be caugt by stage 2 eval if queens == 0
 			
 			flags |= stage3Flag;
 
 			final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
 			final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
 			final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
 			final double clutterMult = clutterIndex[(int)BitUtil.getSetBits(pawnAttacks)];
 			
 			//recalculate attack masks
 			scoreMobility(player, s, clutterMult, nonPawnMaterial, attackMask);
 			scoreMobility(1-player, s, clutterMult, nonPawnMaterial, attackMask);
 
 			final int stage3Score = evalKingSafety(player, s, alliedQueens, enemyQueens);
 
 			score += Weight.interpolate(stage3Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage3Score)*.1), 0), scale);
 			return ScoreEncoder.encode(score, 0, flags);
 		}
 		
 		
 		//evaluation should complete in one of the stages above
 		assert false;
 		return 0;
 	}
 	
 	private int evalKingSafety(final int player, final State4 s, final long alliedQueens, final long enemyQueens){
 		int score = 0;
 		if(enemyQueens != 0){
 			final long king = s.kings[player];
 			final int kingIndex = BitUtil.lsbIndex(king);
 			score += evalKingPressure3(kingIndex, player, s, attackMask[player]);
 		}
 		if(alliedQueens != 0){
 			final long king = s.kings[1-player];
 			final int kingIndex = BitUtil.lsbIndex(king);
 			score -= evalKingPressure3(kingIndex, 1-player, s, attackMask[1-player]);
 		}
 		return score;
 	}
 	
 	/** scores pawn structure*/
 	private int scorePawns(final int player, final State4 s, final PawnHashEntry entry, final long enemyQueens){
 		int score = 0;
 		
 		//get pawn scores from hash entry, or recalculate if necessary
 		final long pawnZkey = s.pawnZkey();
 		if(pawnZkey != entry.zkey){
 			score += calculatePawnScore(player, s, entry);
 
 			final long king = s.kings[player];
 			final int kingIndex = BitUtil.lsbIndex(king);
 			int kingDangerScore = 0;
 			if(enemyQueens != 0){
 				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 				//  NOTE: hashing here doesnt actually take being able to castle into account
 				//  however, doesnt seem to affect playing strength
 				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 				
 				//pawn wall, storm calculations
 				final long cmoves = State4.getCastleMoves(player, s);
 				int pawnWallBonus = pawnShelterStormDanger(player, s, kingIndex);
 				if(cmoves != 0){
 					//if we can castle, count the pawn wall/storm weight as best available after castle
 					if((castleOffsets[0][player] & cmoves) != 0){
 						final int leftIndex = castleIndex[0][player];
 						final int leftScore = pawnShelterStormDanger(player, s, leftIndex);
 						pawnWallBonus = leftScore > pawnWallBonus? leftScore: pawnWallBonus;
 					}
 					if((castleOffsets[1][player] & cmoves) != 0){
 						final int rightIndex = castleIndex[1][player];
 						final int rightScore = pawnShelterStormDanger(player, s, rightIndex);
 						pawnWallBonus = rightScore > pawnWallBonus? rightScore: pawnWallBonus;
 					}
 				}
 				kingDangerScore += S(pawnWallBonus, 0);
 			}
 			
 			kingDangerScore += S(-kingDangerSquares[player][kingIndex], 0);
 			kingDangerScore += S(0, centerDanger[kingIndex]);
 			
 			if(player == 0) entry.score1 += kingDangerScore;
 			else entry.score2 += kingDangerScore;
 			score += kingDangerScore;
 			
 		} else{
 			score += player == 0? entry.score1: entry.score2;
 		}
 		
 		//score passed pawns
 		final long alliedPawns = s.pawns[player];
 		final long passedPawns = entry.passedPawns & alliedPawns;
 		if(passedPawns != 0){
 			for(long pp = passedPawns; pp != 0; pp &= pp-1){
 				final long p = pp & -pp;
 				score += analyzePassedPawn(player, p, s, nonPawnMaterial);
 			}
 		}
 		
 		//adjustments for non-pawn disadvantage
 		final boolean nonPawnDisadvantage = nonPawnMaterial[player]-nonPawnMaterial[1-player]+20 < 0;
 		if(nonPawnDisadvantage){
 			final double npDisMult = max(min(nonPawnMaterial[1-player]-nonPawnMaterial[player], 300), 0)/300.;
 			if(player == 0){
 				score += Weight.multWeight(S(-10, -20), npDisMult*entry.isolatedPawns1);
 				score += Weight.multWeight(S(-10, -10), npDisMult*entry.doubledPawns1);
 				score += Weight.multWeight(S(-10, -10), npDisMult*entry.backwardPawns1);
 			} else{
 				score += Weight.multWeight(S(-10, -20), npDisMult*entry.isolatedPawns2);
 				score += Weight.multWeight(S(-10, -10), npDisMult*entry.doubledPawns2);
 				score += Weight.multWeight(S(-10, -10), npDisMult*entry.backwardPawns2);
 			}
 		}
 		
 		//System.out.println(player+" pawn score = ("+mgScore(score)+", "+egScore(score)+")");
 		return score;
 	}
 	
 	private static int max(final int a1, final int a2){
 		return a1 > a2? a1: a2;
 	}
 	
 	private static int analyzePassedPawn(final int player, final long p, final State4 s, final int[] nonPawnMaterial){
 		
 		int passedPawnSore = 0;
 		
 		final int pawnIndex = BitUtil.lsbIndex(p);
 		//agg.add(this.p.passedPawnRowWeight[player][index >>> 3]);s
 		
 		final int row = player == 0? pawnIndex>>>3: 7-(pawnIndex>>>3);
 		final int pawnDist = 7-row; //distance of pawn from promotion square
 
 		assert (Masks.passedPawnMasks[player][pawnIndex] & s.pawns[1-player]) == 0;
 		
 		//calculate king distance to promote square
 		final int kingIndex = BitUtil.lsbIndex(s.kings[1-player]);
 		final int kingXDist = Math.abs(kingIndex%8 - pawnIndex%8);
 		final int promoteRow = 1-player == 0? 7: 0; 
 		final int kingYDist = Math.abs((kingIndex>>>3) - promoteRow);
 		final int enemyKingDist = kingXDist > kingYDist? kingXDist: kingYDist;
 		
 		//our pawn closer to promotion than enemy king
 		if(pawnDist < enemyKingDist){
 			final int diff = enemyKingDist-pawnDist;
 			assert diff < 8;
 			passedPawnSore += S(0, max(diff*diff, 15));
 		}
 		
 		//pawn closer than enemy king and no material remaining
 		if(pawnDist < enemyKingDist && nonPawnMaterial[1-player] == 0){
 			passedPawnSore += S(500);
 		}
 		
 		//checks for support by same color bishop
 		//performs badly by several indications
 		/*final int endIndex = index%8 + (player == 0? 56: 0);
 		final int endColor = PositionMasks.squareColor(endIndex);
 		final int enemyBishopSupport = -25;
 		final long squareMask = PositionMasks.bishopSquareMask[endColor];
 		if((s.bishops[1-player] & squareMask) != 0){ //allied supporting bishop
 			agg.add(0, enemyBishopSupport/(pawnDist*pawnDist));
 		}*/
 
 		final int rr = row*(row-1);
 		final int start = 16*rr/2;
 		final int end = 10*(rr+row+1)/2;
 		passedPawnSore += S(start, end);
 		
 		
 		//checks for pawn advancement blocked
 		final long nextPos = player == 0? p << 8: p >>> 8;
 		final long allPieces = s.pieces[0]|s.pieces[1];
 		if((nextPos & allPieces) != 0){ //pawn adancement blocked
 			passedPawnSore += S(-start/6/pawnDist, -end/6/pawnDist);
 			
 			//slight bonus for causing a piece to keep blocking a pawn,
 			//(w0,w1,d) = (111,134,126) without-with, depth=3
 			if((nextPos & s.queens[1-player]) != 0) passedPawnSore += S(45/pawnDist);
 			else if((nextPos & s.rooks[1-player]) != 0) passedPawnSore += S(35/pawnDist);
 			//else if((nextPos & s.bishops[1-player]) != 0) passedPawnSore += S(25/pawnDist);
 			//else if((nextPos & s.knights[1-player]) != 0) passedPawnSore += S(25/pawnDist);
 		}
 		
 		//checks to see whether we have a non-pawn material disadvantage,
 		//its very hard to keep a passed pawn when behind
 		//final int nonPawnMaterialDiff = nonPawnMaterial[player]-nonPawnMaterial[1-player];
 		final double npDisMult = max(min(nonPawnMaterial[1-player]-nonPawnMaterial[player], 300), 0)/300.;
 		passedPawnSore += Weight.multWeight(S(-start*2/3, -end*2/3), npDisMult);
 		
 		//passed pawn supported by rook bonus
 		if((s.rooks[player] & PositionMasks.opposedPawnMask[1-player][pawnIndex]) != 0){
 			//agg.add(10/pawnDist);
 		}
 		
 		final boolean chain = (PositionMasks.pawnChainMask[player][pawnIndex] & s.pawns[player]) != 0;
 		if(chain){
 			passedPawnSore += S(35/pawnDist);
 		}
 		
 		return passedPawnSore;
 	}
 	
 	private static double min(final double d1, final double d2){
 		return d1 < d2? d1: d2;
 	}
 	
 	private static double max(final double d1, final double d2){
 		return d1 > d2? d1: d2;
 	}
 	
 	/** determines pawn score, except for passed pawns*/
 	private static int calculatePawnScore(final int player, final State4 s, final PawnHashEntry phEntry){
 		int pawnScore = 0;
 		long passedPawns = 0;
 		int isolatedPawnsCount = 0;
 		int doubledPawnsCount = 0;
 		int backwardPawnsCount = 0;
 
 		final long enemyPawns = s.pawns[1-player];
 		final long alliedPawns = s.pawns[player];
 		final long all = alliedPawns | enemyPawns;
 		final int kingIndex = BitUtil.lsbIndex(s.kings[player]);
 
 		int kingDistAgg = 0; //king distance aggregator
 		for(long pawns = alliedPawns; pawns != 0; pawns &= pawns-1){
 			final long p = pawns & -pawns;
 			final int index = BitUtil.lsbIndex(pawns);
 			final int col = index%8;
 
 			final boolean passed = (Masks.passedPawnMasks[player][index] & enemyPawns) == 0;
 			final boolean isolated = (PositionMasks.isolatedPawnMask[col] & alliedPawns) == 0;
 			final boolean opposed = (PositionMasks.opposedPawnMask[player][index] & enemyPawns) != 0;
 			final int opposedFlag = opposed? 1: 0;
 			final boolean chain = (PositionMasks.pawnChainMask[player][index] & alliedPawns) != 0;
 			final boolean doubled = (PositionMasks.opposedPawnMask[player][index] & alliedPawns) != 0;
 
 			if(passed){
 				passedPawns |= p;
 			}
 			if(isolated){
 				pawnScore += isolatedPawns[opposedFlag][col];
 				isolatedPawnsCount++;
 			}
 			if(doubled){
 				pawnScore += doubledPawns[opposedFlag][col];
 				doubledPawnsCount++;
 			}
 			if(chain){
 				pawnScore += pawnChain[col];
 			}
 
 			//backward pawn checking
 			final long attackSpan = PositionMasks.isolatedPawnMask[col] & Masks.passedPawnMasks[player][index];
 			if(!passed && !isolated && !chain &&
 					(attackSpan & enemyPawns) != 0 && //enemy pawns that can attack our pawns
 					(PositionMasks.pawnAttacks[player][index] & enemyPawns) == 0){ //not attacking enemy pawns
 				long b = PositionMasks.pawnAttacks[player][index];
 				while((b & all) == 0){
 					b = player == 0? b << 8: b >>> 8;
 					assert b != 0;
 				}
 
 				final boolean backward = ((b | (player == 0? b << 8: b >>> 8)) & enemyPawns) != 0;
 				if(backward){
 					pawnScore += backwardPawns[opposedFlag][col];
 					backwardPawnsCount++;
 				}
 			}
 
 			//allied king distance, used to encourage king supporting pawns in endgame
 			final int kingXDist = Math.abs(kingIndex%8 - index%8);
 			final int kingYDist = Math.abs((kingIndex>>>3) - (index>>>3));
 			final int alliedKingDist = kingXDist > kingYDist? kingXDist: kingYDist;
 			assert alliedKingDist < 8;
 			kingDistAgg += alliedKingDist-1;
 		}
 
 		//minimize avg king dist from pawns in endgame
 		final double n = s.pieceCounts[player][State4.PIECE_TYPE_PAWN];
 		if(n > 0) pawnScore += S(0, (int)(-kingDistAgg/n*5+.5));
 
 		if(player == 0){
 			phEntry.score1 = pawnScore;
 			phEntry.isolatedPawns1 = isolatedPawnsCount;
 			phEntry.doubledPawns1 = doubledPawnsCount;
 			phEntry.backwardPawns1 = backwardPawnsCount;
 		} else{
 			phEntry.score2 = pawnScore;
 			phEntry.isolatedPawns2 = isolatedPawnsCount;
 			phEntry.doubledPawns2 = doubledPawnsCount;
 			phEntry.backwardPawns2 = backwardPawnsCount;
 		}
 		phEntry.passedPawns |= passedPawns;
 		return pawnScore;
 	}
 	
 	/** calculates danger associated with pawn wall weaknesses or storming enemy pawns*/
 	private static int pawnShelterStormDanger(final int player, final State4 s, final int kingIndex){
 		final int kc = kingIndex%8; //king column
 		final int kr = player == 0? kingIndex >>> 3: 7-(kingIndex>>>3); //king rank
 		final long mask = Masks.passedPawnMasks[player][kingIndex];
 		final long wallPawns = s.pawns[player] & mask; //pawns in front of the king
 		final long stormPawns = s.pawns[1-player] & mask; //pawns in front of the king
 		final int f = kc == 0? 1: kc == 7? 6: kc; //file, eval as if not on edge
 		
 		int pawnWallDanger = 0;
 		
 		for(int a = -1; a <= 1; a++){
 			final long colMask = Masks.colMask[f+a];
 			
 			final long allied = wallPawns & colMask;
 			final int rankAllied;
 			if(allied != 0){
 				rankAllied = player == 0? BitUtil.lsbIndex(allied)>>>3: 7-(BitUtil.msbIndex(allied)>>>3);
 				pawnWallDanger += pawnShelter[f != kc? 0: 1][rankAllied];
 			} else{
 				rankAllied = 0;
 			}
 			
 			final long enemy = stormPawns & colMask;
 			if(enemy != 0){
 				final int rankEnemy = player == 0? BitUtil.lsbIndex(enemy)>>>3: 7-(BitUtil.msbIndex(enemy)>>>3);
 				final int type = allied == 0? 0: rankAllied+1 != rankEnemy? 1: 2;
 				assert rankEnemy > kr;
 				pawnWallDanger += pawnStorm[type][rankEnemy-kr-1];
 			}
 		}
 		
 		return pawnWallDanger;
 	}
 	
 	private static int[] centerDanger = new int[]{
 		-30, -15, -10, -10, -10, -10, -15, -30,
 		-15, -10, -10, -10, -10, -10, -10, -15,
 		-10, -10, -8, -8, -8, -8, -10, -10,
 		-10, -10, -8, -4, -4, -8, -10, -10,
 		-10, -10, -8, -4, -4, -8, -10, -10,
 		-10, -10, -8, -8, -8, -8, -10, -10,
 		-15, -10, -10, -10, -10, -10, -10, -15,
 		-30, -15, -10, -10, -10, -10, -15, -30,
 	};
 	
 	/** stores castling positions indexed [side = left? 0: 1][player]*/
 	private final static long[][] castleOffsets = new long[][]{
 		{1L<<2, 1L<<58}, //castle left mask
 		{1L<<6, 1L<<62}, //castle right mask
 	};
 	
 	/** gives the final index of the king after castling, index [side = left? 0: 1][player]*/
 	private final static int[][] castleIndex = new int[][]{
 		{2, 58}, //castle left index
 		{6, 62}, //castle right index
 	};
 	
 	private static int evalKingPressure3(final int kingIndex, final int player,
 			final State4 s, final long alliedAttackMask){
 		
 		final long king = 1L << kingIndex;
 		final long allied = s.pieces[player];
 		final long enemy = s.pieces[1-player];
 		final long agg = allied | enemy;
 		int index = 0;
 		
 		final long kingRing = Masks.getRawKingMoves(king);
 		final long undefended = kingRing & ~alliedAttackMask;
 		final long rookContactCheckMask = kingRing &
 				~(PositionMasks.pawnAttacks[0][kingIndex] | PositionMasks.pawnAttacks[1][kingIndex]);
 		final long bishopContactCheckMask = kingRing & ~rookContactCheckMask;
 		
 		final long bishops = s.bishops[1-player];
 		final long rooks = s.rooks[1-player];
 		final long queens = s.queens[1-player];
 		final long pawns = s.pawns[1-player];
 		final long knights = s.knights[1-player];
 		
 		//process queen attacks
 		int supportedQueenAttacks = 0;
 		for(long tempQueens = queens; tempQueens != 0; tempQueens &= tempQueens-1){
 			final long q = tempQueens & -tempQueens;
 			final long qAgg = agg & ~q;
 			final long queenMoves = Masks.getRawQueenMoves(agg, q) & ~enemy & undefended;
 			for(long temp = queenMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
 				final long pos = temp & -temp;
 				if((pos & undefended) != 0){
 					final long aggPieces = qAgg | pos;
 					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
 					final long knightAttacks = Masks.getRawKnightMoves(pos);
 					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
 					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);
 					
 					if((bishops & bishopAttacks) != 0 |
 							(rooks & rookAttacks) != 0 |
 							(knights & knightAttacks) != 0 |
 							(pawns & pawnAttacks) != 0 |
 							(queens & ~q & (bishopAttacks|rookAttacks)) != 0){
 						supportedQueenAttacks++;
 					}
 				}
 			}
 		}
 		//index += supportedQueenAttacks*16;
 		index += supportedQueenAttacks*4;
 
 		//process rook attacks
 		int supportedRookAttacks = 0;
 		int supportedRookContactChecks = 0;
 		for(long tempRooks = rooks; tempRooks != 0; tempRooks &= tempRooks-1){
 			final long r = tempRooks & -tempRooks;
 			final long rAgg = agg & ~r;
 			final long rookMoves = Masks.getRawRookMoves(agg, r) & ~enemy & undefended;
 			for(long temp = rookMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
 				final long pos = temp & -temp;
 				if((pos & undefended) != 0){
 					final long aggPieces = rAgg | pos;
 					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
 					final long knightAttacks = Masks.getRawKnightMoves(pos);
 					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
 					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);
 
 					if((bishops & bishopAttacks) != 0 |
 							(rooks & ~r & rookAttacks) != 0 |
 							(knights & knightAttacks) != 0 |
 							(pawns & pawnAttacks) != 0 |
 							(queens & (bishopAttacks|rookAttacks)) != 0){
 						if((pos & rookContactCheckMask) != 0) supportedRookContactChecks++;
 						else supportedRookAttacks++;
 					}
 				}
 			}
 		}
 		index += supportedRookAttacks*1;
 		index += supportedRookContactChecks*2;
 		
 		//process bishop attacks
 		int supportedBishopAttacks = 0;
 		int supportedBishopContactChecks = 0;
 		for(long tempBishops = bishops; tempBishops != 0; tempBishops &= tempBishops-1){
 			final long b = tempBishops & -tempBishops;
 			final long bAgg = agg & ~b;
 			final long bishopMoves = Masks.getRawBishopMoves(agg, b) & ~enemy & undefended;
 			for(long temp = bishopMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
 				final long pos = temp & -temp;
 				if((pos & undefended) != 0){
 					final long aggPieces = bAgg | pos;
 					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
 					final long knightAttacks = Masks.getRawKnightMoves(pos);
 					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
 					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);
 
 					if((bishops & ~b & bishopAttacks) != 0 |
 							(rooks & rookAttacks) != 0 |
 							(knights & knightAttacks) != 0 |
 							(pawns & pawnAttacks) != 0 |
 							(queens & (bishopAttacks|rookAttacks)) != 0){
 						if((pos & bishopContactCheckMask) != 0) supportedBishopContactChecks++;
 						else supportedBishopAttacks++;
 					}
 				}
 			}
 		}
 		index += supportedBishopAttacks*1;
 		index += supportedBishopContactChecks*2;
 		
 		//process knight attacks
 		int supportedKnightAttacks = 0;
 		for(long tempKnights = knights; tempKnights != 0; tempKnights &= tempKnights-1){
 			final long k = tempKnights & -tempKnights;
 			final long kAgg = agg & ~k;
 			final long knightMoves = Masks.getRawKnightMoves(k) & ~enemy & undefended;
 			for(long temp = knightMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
 				final long pos = temp & -temp;
 				if((pos & undefended) != 0){
 					final long aggPieces = kAgg | pos;
 					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
 					final long knightAttacks = Masks.getRawKnightMoves(pos);
 					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
 					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);
 
 					if((bishops & bishopAttacks) != 0 |
 							(rooks & rookAttacks) != 0 |
 							(knights & ~k & knightAttacks) != 0 |
 							(pawns & pawnAttacks) != 0 |
 							(queens & (bishopAttacks|rookAttacks)) != 0){
 						supportedKnightAttacks++;
 					}
 				}
 			}
 		}
 		index += supportedKnightAttacks*1;
 		
 		return kingDangerTable[index < 128? index: 127];
 	}
 	
 	@Override
 	public void makeMove(final long encoding) {
 		update(encoding, 1);
 	}
 
 	@Override
 	public void undoMove(final long encoding) {
 		update(encoding, -1);
 	}
 	
 	/** incrementally updates the score after a move, dir = undo? -1: 1*/
 	private void update(final long encoding, final int dir){
 		final int player = MoveEncoder.getPlayer(encoding);
 		final int taken = MoveEncoder.getTakenType(encoding);
 		if(taken != 0){
 			materialScore[1-player] -= dir*materialWeights[taken];
 		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
 			materialScore[1-player] -= dir*materialWeights[State4.PIECE_TYPE_PAWN];
 		}
 		if(MoveEncoder.isPawnPromotion(encoding)){
 			materialScore[player] += dir*(materialWeights[State4.PIECE_TYPE_QUEEN]-
 					materialWeights[State4.PIECE_TYPE_PAWN]);
 		}
 	}
 
 	@Override
 	public void initialize(State4 s) {
 		System.arraycopy(zeroi, 0, materialScore, 0, 2);
 		System.arraycopy(zeroi, 0, kingPawnDist, 0, 2);
 		
 		for(int a = 0; a < 2; a++){
 			final int b = State4.PIECE_TYPE_BISHOP;
 			materialScore[a] += s.pieceCounts[a][b] * materialWeights[b];
 			
 			final int n = State4.PIECE_TYPE_KNIGHT;
 			materialScore[a] += s.pieceCounts[a][n] * materialWeights[n];
 			
 			final int q = State4.PIECE_TYPE_QUEEN;
 			materialScore[a] += s.pieceCounts[a][q] * materialWeights[q];
 			
 			final int r = State4.PIECE_TYPE_ROOK;
 			materialScore[a] += s.pieceCounts[a][r] * materialWeights[r];
 			
 			final int p = State4.PIECE_TYPE_PAWN;
 			materialScore[a] += s.pieceCounts[a][p] * materialWeights[p];
 		}
 	}
 	
 	@Override
 	public void reset(){}
 	
 
 	/** calculates mobility and danger to enemy king from mobility*/
 	private static int scoreMobility(final int player, final State4 s,
 			final double clutterMult, final int[] nonPawnMaterial, final long[] attackMask){
 		int mobScore = 0;
 		
 		final long alliedPawns = s.pawns[player];
 		final long enemyPawns = s.pawns[1-player];
 		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1-player, enemyPawns);
 		
 		final long allied = s.pieces[player];
 		final long enemy = s.pieces[1-player];
 		final long agg = allied | enemy;
 		final long aggPawns = alliedPawns | enemyPawns;
 		final long blockedAlliedPawns; //allied pawns whose movement is blocked by another pawn
 		final long chainedAlliedPawns; //allied pawns that are supporting other allied pawns
 		
 		if(player == 0){
 			final long movementBlocked = ((alliedPawns << 8) & aggPawns) >>> 8;
 			final long supportPawns = (((alliedPawns << 7) & alliedPawns) >>> 7) |
 					(((alliedPawns << 9) & alliedPawns) >>> 9);
 			blockedAlliedPawns = movementBlocked;
 			chainedAlliedPawns = supportPawns;
 		} else{
 			final long movementBlocked = ((alliedPawns >>> 8) & aggPawns) << 8;
 			final long supportPawns = (((alliedPawns >>> 7) & alliedPawns) << 7) |
 					(((alliedPawns >>> 9) & alliedPawns) << 9);
 			blockedAlliedPawns = movementBlocked;
 			chainedAlliedPawns = supportPawns;
 		}
 		
 		long bishopAttackMask = 0;
 		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
 			final long b = bishops & -bishops;
 			final long rawMoves = Masks.getRawBishopMoves(agg, b);
 			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
 			final int count = (int)BitUtil.getSetBits(moves);
 			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_BISHOP][count], clutterMult);
 			bishopAttackMask |= rawMoves;
 			
 			//penalize bishop for blocking allied pawns on bishop color
 			final long squareMask = (PositionMasks.bishopSquareMask[0] & b) != 0?
 					PositionMasks.bishopSquareMask[0]: PositionMasks.bishopSquareMask[1];
 			final int blockingPawnsCount = (int)BitUtil.getSetBits(blockedAlliedPawns & squareMask);
 			final int supportingPawnsCount = (int)BitUtil.getSetBits(chainedAlliedPawns & squareMask & ~blockedAlliedPawns);
 			mobScore += S(blockingPawnsCount*-5 + supportingPawnsCount*-1, 0);
 		}
 
 		long knightAttackMask = 0;
 		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
 			final long k = knights & -knights;
 			final long rawMoves = Masks.getRawKnightMoves(k);
 			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
 			final int count = (int)BitUtil.getSetBits(moves);
 			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_KNIGHT][count], clutterMult);
 			knightAttackMask |= rawMoves;
 		}
 
 		long rookAttackMask = 0;
 		final long allPieces = s.pieces[0]|s.pieces[1];
 		final int alliedKingIndex = BitUtil.lsbIndex(s.kings[player]);
 		final int alliedKingCol = alliedKingIndex%8;
 		final int alliedKingRow = alliedKingIndex >>> 3;
 		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
 			final long r = rooks&-rooks;
 			final long rawMoves = Masks.getRawRookMoves(agg, r);
 			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
 			final int moveCount = (int)BitUtil.getSetBits(moves);
 			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_ROOK][moveCount], clutterMult);
 			rookAttackMask |= rawMoves;
 			
 			final int rindex = BitUtil.lsbIndex(r);
 			final int col = rindex%8;
 			if(isHalfOpen(col, enemyPawns, allPieces & ~r)){ //tests file half open
 				mobScore += S(6, 15);
 				if(((allPieces & ~r) & Masks.colMask[col]) == 0){ //tests file open
 					mobScore += S(6, 15);
 				}
 			}
 
 			final int row = rindex >>> 3;
 			if(row == alliedKingRow){
 				final int backRank = player == 0? 0: 7;
 				if(alliedKingRow == backRank && moveCount <= 4){
 					if(alliedKingCol >= 4 && col > alliedKingCol){
 						if(!s.kingMoved[player] && !s.rookMoved[player][1]){
 							mobScore += S(-20, -50); //trapped, but can castle right
 						} else{
 							mobScore += S(-50, -120); //trapped, cannot castle
 							//old, 80, 150
 						}
 					}
 					if(alliedKingCol <= 3 && col < alliedKingCol){
 						if(!s.kingMoved[player] && !s.rookMoved[player][0]){
 							mobScore += S(-20, -50); //trapped, but can castle left
 						} else{
 							mobScore += S(-50, -120); //trapped, cannot castle
 						}
 					}
 				}
 			}
 		}
 
 		long queenAttackMask = 0;
 		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
 			final long q = queens&-queens;
 			final long rawMoves = Masks.getRawQueenMoves(agg, q);
 			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
 			final int count = (int)BitUtil.getSetBits(moves);
 			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_QUEEN][count], clutterMult);
 			queenAttackMask |= rawMoves;
 		}
 		
 		final long pawnAttackMask = Masks.getRawPawnAttacks(player, alliedPawns);
 		attackMask[player] = bishopAttackMask | knightAttackMask | rookAttackMask | queenAttackMask | pawnAttackMask;
 		
 
 		//System.out.println(player+" pawn score = ("+mgScore(mobScore)+", "+egScore(mobScore)+")");
 		return mobScore;
 	}
 	
 	/**
 	 * 
 	 * @param col
 	 * @param enemyPawns
 	 * @param pieces pieces excluding the rook to be tested
 	 * @return
 	 */
 	private static boolean isHalfOpen(final int col, final long enemyPawns, final long pieces){
 		final long mask = Masks.colMask[col];
 		if((mask & pieces) == 0) return true; //column is fully open
 		if((mask & (pieces & ~enemyPawns)) == 0){
 			//no pieces except for pawns
 			final long c = enemyPawns & mask;
 			if((c & (c-1)) == 0){
 				return true; //only one pawn in the column
 			}
 		}
 		return false;
 	}
 }
