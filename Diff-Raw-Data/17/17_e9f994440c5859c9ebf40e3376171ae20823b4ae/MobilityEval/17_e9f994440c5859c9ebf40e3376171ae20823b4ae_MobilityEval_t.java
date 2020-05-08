 package eval.e9.mobilityEval;
 
 import state4.BitUtil;
 import state4.Masks;
 import state4.State4;
 import eval.PositionMasks;
 import eval.e9.Weight;
 
 /** container class for eval pertaining to mobility*/
 public final class MobilityEval {
 
 	private static final int[] knightMobilityWeights;
 	private static final int[] bishopMobilityWeights;
 	private static final int[] rookMobilityWeights;
 	private static final int[] queenMobilityWeights;
 	
 	/** compute logistic (sigmoid) function centered around 0*/
 	private static double logistic(double x){
		return 1./(1+Math.exp(-x))-.5;
 	}
 	
 	/**
 	 * fill passed result store with values from parameterized centered logistic function
 	 * @param center x offset
 	 * @param yoffset y offset
 	 * @param range range of the logistic function, scales the results linearly
 	 * @param store result store
 	 * @see #logistic(double)
 	 */
 	private static void interpolate(int[] center, double[] yoffset, double[] range, int[] store){
 		for(int a = 0; a < store.length; a++){
 			store[a] = Weight.encode(
 					(int)(range[0]*logistic(a+center[0])+yoffset[0]+.5),
 					(int)(range[1]*logistic(a+center[1])+yoffset[1]+.5));
 		}
 	}
 	
 	static{
 		knightMobilityWeights = new int[9];
 		interpolate(new int[]{-2,-2}, new double[]{-2,-2}, new double[]{20,50}, knightMobilityWeights);
 		/*knightMobilityWeights = new int[]{
 				S(-19,-49), S(-13,-40), S(-6,-27), S(-1,0), S(7,2),
 				S(12,10), S(14,28), S(16,44), S(17,48)
 		}*/;
 		
 		bishopMobilityWeights = new int[16];
 		interpolate(new int[]{0,-3}, new double[]{-4,-2}, new double[]{25,45}, bishopMobilityWeights);
 		/*bishopMobilityWeights = new int[]{
 				S(-13,-30), S(-6,-20), S(1,-18), S(7,-10), S(15,-1),
 				S(24,8), S(28,14), S(24,18), S(30,20), S(34,23),
 				S(38,25), S(43,31), S(49,32), S(55,37), S(55,38), S(55,38)
 		};*/
 		
 
 		rookMobilityWeights = new int[16];
 		interpolate(new int[]{-3,-3}, new double[]{-4,-7}, new double[]{30,60}, rookMobilityWeights);
 		/*rookMobilityWeights = new int[]{
 				S(-10,-69), S(-7,-47), S(-4,-43), S(-1,-10), S(2,13), S(5,26),
 				S(7,35), S(10,43), S(11,50), S(12,56), S(12,60), S(13,63),
 				S(14,66), S(15,69), S(15,74), S(17,74)
 		};*/
 		
 		queenMobilityWeights = new int[32];
 		interpolate(new int[]{-3,-2}, new double[]{-4,-20}, new double[]{20,55}, queenMobilityWeights);
 		/*queenMobilityWeights = new int[]{
 				S(-6,-69), S(-4,-49), S(-2,-45), S(-2,-28), S(-1,-9), S(0,10),
 				S(1,15), S(2,20), S(4,25), S(5,30), S(6,30), S(7,30), S(8,30),
 				S(8,30), S(9,30), S(10,35), S(12,35), S(14,35), S(15,35), S(15,35),
 				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35),
 				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35)
 		};*/
 		
		int[][] temp = new int[][]{
				knightMobilityWeights,
				bishopMobilityWeights,
				rookMobilityWeights,
				queenMobilityWeights
		};
		
		for(int a = 0; a < temp.length; a++){
			for(int q = 0; q < temp[a].length; q++){
				System.out.print(Weight.mgScore(temp[a][q])+", ");
			}
			System.out.println();
		}
		
 	}
 	
 	/** build a weight scaling from passed start,end values*/
 	private static int S(int start, int end){
 		return Weight.encode(start, end);
 	}
 
 	/** calculates mobility and danger to enemy king from mobility*/
 	public static int scoreMobility(final int player, final State4 s,
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
 			mobScore += Weight.multWeight(bishopMobilityWeights[count], clutterMult);
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
 			mobScore += Weight.multWeight(knightMobilityWeights[count], clutterMult);
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
 			mobScore += Weight.multWeight(rookMobilityWeights[moveCount], clutterMult);
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
 			mobScore += Weight.multWeight(queenMobilityWeights[count], clutterMult);
 			queenAttackMask |= rawMoves;
 		}
 		
 		final long pawnAttackMask = Masks.getRawPawnAttacks(player, alliedPawns);
 		attackMask[player] = bishopAttackMask | knightAttackMask | rookAttackMask | queenAttackMask | pawnAttackMask;
 		
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
