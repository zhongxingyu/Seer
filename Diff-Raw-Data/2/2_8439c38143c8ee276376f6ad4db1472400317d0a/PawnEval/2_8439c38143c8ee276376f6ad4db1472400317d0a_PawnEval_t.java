 package chess.eval.e9.pawnEval;
 
 import chess.state4.BitUtil;
 import chess.state4.Masks;
 import chess.state4.State4;
 import chess.eval.PositionMasks;
 import chess.eval.e9.PawnHashEntry;
 import chess.eval.e9.Weight;
 
 /**
  * container class for methods pertaining to pawn evaluation, and king evaluation where relevant
  * (for instance, pawns protecting a king, castling, etc)
  * @author jdc2172
  *
  */
 public final class PawnEval {
 
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
 	
 	static{
 		for(int a = 0; a < 64; a++) kingDangerSquares[1][a] = kingDangerSquares[0][63-a];
 	}
 	
 	/** build a weight scaling from passed start,end values*/
 	private static int S(int start, int end){
 		return Weight.encode(start, end);
 	}
 	
 	/** build a constant, non-scaling weight*/
 	private static int S(final int v){
 		return Weight.encode(v);
 	}
 	
 	/** scores pawn structure*/
 	public static int scorePawns(final int player, final State4 s,
 			final PawnHashEntry entry, final long enemyQueens, final int nonPawnMaterialScore){
 		int score = 0;
 		
 		//get pawn scores from hash entry, or recalculate if necessary
 		final long pawnZkey = s.pawnZkey();
 		if(pawnZkey != entry.zkey){
 			score += analyzePawns(player, s, entry);
 
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
 				score += analyzePassedPawn(player, p, s, nonPawnMaterialScore);
 			}
 		}
 		
 		//adjustments for non-pawn disadvantage
 		//final boolean nonPawnDisadvantage = nonPawnMaterial[player]-nonPawnMaterial[1-player]+20 < 0;
 		final boolean nonPawnDisadvantage = nonPawnMaterialScore+20 < 0;
 		if(nonPawnDisadvantage){
 			//final double npDisMult = Math.max(Math.min(nonPawnMaterial[1-player]-nonPawnMaterial[player], 300), 0)/300.;
 			final double npDisMult = Math.max(Math.min(-nonPawnMaterialScore, 300), 0)/300.;
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
 		
 		return score;
 	}
 	
 	private static int max(final int a1, final int a2){
 		return a1 > a2? a1: a2;
 	}
 	
 	private static int analyzePassedPawn(final int player, final long p, final State4 s, final int nonPawnMaterialScore){
 		
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
		boolean enemyHasNonPawnMaterial = (s.pieces[1-player] & ~s.pawns[1-player] & ~s.kings[1-player]) != 0;
 		if(pawnDist < enemyKingDist && !enemyHasNonPawnMaterial){
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
 		//final double npDisMult = Math.max(Math.min(nonPawnMaterial[1-player]-nonPawnMaterial[player], 300), 0)/300.;
 		final double npDisMult = Math.max(Math.min(-nonPawnMaterialScore, 300), 0)/300.;
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
 	
 
 	
 	/**
 	 * determines pawn score, structure, etc (except for passed pawns)
 	 * <p>
 	 * raw pawn score is aggregated here but analysis of non-hashable features (for instance, pressure on
 	 * weak pawns) is handled elsewhere
 	 * <p>
 	 * results are recorded in pawn hash entry
 	 * @param player
 	 * @param s
 	 * @param phEntry
 	 * @return returns raw score for pawns
 	 */
 	public static int analyzePawns(final int player, final State4 s, final PawnHashEntry phEntry){
 		int pawnScore = 0;
 		long passedPawns = 0;
 		int isolatedPawnsCount = 0;
 		int doubledPawnsCount = 0;
 		int backwardPawnsCount = 0;
 		long weakPawns = 0; //mask aggregating weak pawns
 
 		final long enemyPawns = s.pawns[1-player];
 		final long alliedPawns = s.pawns[player];
 		final long all = alliedPawns | enemyPawns;
 
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
 
 			//backward pawn determination
 			final boolean backward;
 			final long attackSpan = PositionMasks.isolatedPawnMask[col] & Masks.passedPawnMasks[player][index];
 			if(!passed && !isolated && !chain && //preliminary checks for possibility of backward pawn
 					(attackSpan & enemyPawns) != 0 && //enemy pawns that can attack our pawns
 					(PositionMasks.pawnAttacks[player][index] & enemyPawns) == 0){ //not attacking enemy pawns
 				
 				long b = PositionMasks.pawnAttacks[player][index];
 				if(player == 0){
 					while((b & all) == 0){
 						b = b << 8;
 						assert b != 0;
 					}
 				} else{
 					while((b & all) == 0){
 						b = b >>> 8;
 						assert b != 0;
 					}
 				}
 				
 				//if we encountered an enemy pawn, then backwards;
 				//else, if we encounted an allied pawn, must check that at least one
 				//square in front of the allied pawn
 				backward = ((b | (player == 0? b << 8: b >>> 8)) & enemyPawns) != 0;
 				
 			} else{
 				backward = false;
 			}
 			
 			if(backward){
 				pawnScore += backwardPawns[opposedFlag][col];
 				backwardPawnsCount++;
 			}
 			
 			//record weak pawns for use in other parts of chess.eval
 			//should probably also include doubled pawns that are stuck and unsupported
 			if(isolated || backward){
 				weakPawns |= p;
 			}
 		}
 
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
 		phEntry.weakPawns |= weakPawns;
 		return pawnScore;
 	}
 	
 
 	
 	/** calculates danger associated with pawn wall weaknesses or storming enemy pawns*/
 	private static int pawnShelterStormDanger(final int player, final State4 s, final int kingIndex){
 		final int kc = kingIndex%8; //king column
 		final int kr = player == 0? kingIndex >>> 3: 7-(kingIndex>>>3); //king rank
 		final long mask = Masks.passedPawnMasks[player][kingIndex];
 		final long wallPawns = s.pawns[player] & mask; //pawns in front of the king
 		final long stormPawns = s.pawns[1-player] & mask; //pawns in front of the king
 		final int f = kc == 0? 1: kc == 7? 6: kc; //file, chess.eval as if not on edge
 		
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
 }
