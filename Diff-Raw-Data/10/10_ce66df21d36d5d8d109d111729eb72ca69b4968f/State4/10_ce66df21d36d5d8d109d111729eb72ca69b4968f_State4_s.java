 package chess.state4;
 
 import chess.eval.PositionMasks;
 
 public final class State4 {
 	
 	public static final int WHITE = 0;
 	public static final int BLACK = 1;
 	
 	//NOTE: CHANGING THESE VALUES WILL ALMOST CERTAINLY CAUSE MANY ERRORS
 	public static final int PIECE_TYPE_EMPTY = 0; //definitely must stay 0
 	public static final int PIECE_TYPE_KING = 1;
 	public static final int PIECE_TYPE_QUEEN = 2;
 	public static final int PIECE_TYPE_ROOK = 3;
 	public static final int PIECE_TYPE_BISHOP = 4;
 	public static final int PIECE_TYPE_KNIGHT = 5;
 	public static final int PIECE_TYPE_PAWN = 6;
 	public static final int PROMOTE_QUEEN = 0;
 	public static final int PROMOTE_ROOK = 1;
 	public static final int PROMOTE_BISHOP = 2;
 	public static final int PROMOTE_KNIGHT = 3;
 	
 	/** piece masks, indexed [piece-type][player]*/
 	public final long[][] pieceMasks = new long[7][2];
 	
 	/** stores piece counts for each player, total pieces
 	 * stored in {@link #PIECE_TYPE_EMPTY} index
 	 * <p> indexed [player][piece-type]*/
 	public final int[][] pieceCounts = new int[2][7];
 	
 	/** aggregate piece mask*/
 	public final long[] pieces = pieceMasks[PIECE_TYPE_EMPTY];
 	/** stores the piece type on each board index*/
 	public final int[] mailbox = new int[64];
 	/** stores possible enPassante square*/
 	public long enPassante = 0;
 	
 	public final long[] pawns = pieceMasks[PIECE_TYPE_PAWN];
 	public final long[] knights = pieceMasks[PIECE_TYPE_KNIGHT];
 	public final long[] kings = pieceMasks[PIECE_TYPE_KING];
 	public final long[] queens = pieceMasks[PIECE_TYPE_QUEEN];
 	public final long[] rooks = pieceMasks[PIECE_TYPE_ROOK];
 	public final long[] bishops = pieceMasks[PIECE_TYPE_BISHOP];
 	
 	private final long[] history = new long[128];
 	/** index in {@link #history} of first unused record*/
 	private int hindex = 0;
 	
 	/** zobrist hash key*/
 	private long zkey = 0;
 	/** zobrist hash for pawns and king, undifferentiated for side to move*/
 	private long pawnZkey = 0;
 	private final ZHash zhash;
 	private final HistoryMap2 hm = new HistoryMap2(10);
 	/** appearance hash, applied to zkey to denote how many times a positions hash appeared*/
 	private final long[] appHashs;
 	/** seed used to generate zkey hashes*/
 	private final long zkeySeed;
 	
 	/** records if king has moved to determine castling props, indexed [player]*/
 	public final boolean[] kingMoved = new boolean[2];
 	/** records if rook has moved to determine castline props, indexed [player][rook] where rook==left-rook? 0: 1*/
 	public final boolean[][] rookMoved = new boolean[2][2];
 	/** count since last pawn move or take (for 50-move draw)*/
 	public int drawCount = 0;
 	private final int maxDrawCount;
 	
 	public State4(final long zkeySeed, final int maxDrawCount){
 		this.maxDrawCount = maxDrawCount;
 		zhash = new ZHash(zkeySeed);
 		appHashs = new long[]{0, 0, zhash.appeared2, zhash.appeared3};
 		this.zkeySeed = zkeySeed;
 	}
 	
 	public State4(final long zkeySeed){
 		this(zkeySeed, 100);
 	}
 	
 	public State4(){
 		this(47388L, 100);
 	}
 	
 	public static long getCastleMoves(int player, State4 s){
 		if(!s.kingMoved[player] && (!s.rookMoved[player][0] || !s.rookMoved[player][1])){
 			long moves = 0;
 			final long agg = s.pieces[0]|s.pieces[1];
 			if(!s.rookMoved[player][0] && (Masks.castleBlockedMask[player][0] & agg) == 0 &&
 					!maskIsAttacked(Masks.castleThroughCheck[player][0], 1 - player, s)){
 				moves |= Masks.castleMask[player][0];
 			}
 			if(!s.rookMoved[player][1] && (Masks.castleBlockedMask[player][1] & agg) == 0 &&
 					!maskIsAttacked(Masks.castleThroughCheck[player][1], 1 - player, s)){
 				moves |= Masks.castleMask[player][1];
 			}
 			return moves;
 		}
 		return 0;
 	}
 	
 	/** clears stored history, should be called just before beginning a new chess.search*/
 	public void resetHistory(){
 		hindex = 0;
 	}
 	
 	/** if true, player to move has the option to draw*/
 	public boolean isDrawable(){
 		return hm.get(zkey) >= 3;
 	}
 	
 	/** if true, game is a forced draw (via 50 move draw, piece combinations, etc)*/
 	public boolean isForcedDraw(){
 		final long pieces1 = pieces[0];
 		final long pieces2 = pieces[1];
 		final long king1 = kings[0];
 		final long king2 = kings[1];
 		final long bishops1 = bishops[0];
 		final long bishops2 = bishops[1];
 		final long knights1 = knights[0];
 		final long knights2 = knights[1];
 		
 		//only king and bishop (of one square)
 		final boolean kingBishop1 = pieces1 == (king1 | bishops1) && pieces2 == king2 && (
 				(((PositionMasks.bishopSquareMask[0] & bishops1) == 0 && (PositionMasks.bishopSquareMask[1] & bishops1) != 0)) ||
 				(((PositionMasks.bishopSquareMask[0] & bishops1) != 0 && (PositionMasks.bishopSquareMask[1] & bishops1) == 0)));
 		final boolean kingBishop2 = pieces2 == (king2 | bishops2) && pieces1 == king1 && (
 				(((PositionMasks.bishopSquareMask[0] & bishops2) == 0 && (PositionMasks.bishopSquareMask[1] & bishops2) != 0)) ||
 				(((PositionMasks.bishopSquareMask[0] & bishops2) != 0 && (PositionMasks.bishopSquareMask[1] & bishops2) == 0)));
 		final boolean kingBishop = kingBishop1 | kingBishop2;
 		//final boolean kingBishop = false;
 		
 		//king and only one knight
 		final boolean kingKnight1 = pieces1 == (king1 | knights1) && pieces2 == king2;
 		final boolean kingKnight2 = pieces2 == (king2 | knights2) && pieces1 == king1;
 		final boolean kingKnight = kingKnight1 | kingKnight2;
 		
 		final boolean onlyKings = pieces1 == king1 && pieces2 == king2;
 		return drawCount >= maxDrawCount || onlyKings || kingBishop || kingKnight;
 	}
 
 	/**
 	 * quicker check to see if a single position index is attacked.
 	 * use {@link #maskIsAttacked(long, int, State4)} to handle entire position masks
 	 * @param posMask mask of the position to test
 	 * @param player player doing the attacking attacking
 	 * @param s board state to check for attacks
 	 * @return returns true of passed position is attacked, false otherwise
 	 */
 	public static boolean posIsAttacked(final long posMask, final int player, final State4 s){
 		//assert posMask != 0 && (posMask & (posMask-1)) == 0; //assert exactly one position marked in the mask
 
 		final long pawnAttacks = Masks.getRawPawnAttacks(player, s.pawns[player]) & posMask;
 
 		final long agg = s.pieces[0] | s.pieces[1] | posMask;
 		
 		return pawnAttacks != 0 ||
 				(Masks.getRawBishopMoves(agg, posMask) & (s.queens[player]|s.bishops[player])) != 0 ||
 				(Masks.getRawRookMoves(agg, posMask) & (s.queens[player]|s.rooks[player])) != 0 ||
 				(Masks.getRawKnightMoves(posMask) & s.knights[player]) != 0 ||
 				(Masks.getRawKingMoves(posMask) & s.kings[player]) != 0;
 	}
 
 	/**
 	 * quicker check to see if a single position index is attacked.
 	 * use {@link #maskIsAttacked(long, int, State4)} to handle entire position masks
 	 * @param posIndex index (0-63) of the position to test
 	 * @param player player doing the attacking attacking
 	 * @param s board state to check for attacks
 	 * @return returns true of passed position is attacked, false otherwise
 	 */
 	public static boolean posIsAttacked(final int posIndex, final int player, final State4 s){
 		return posIsAttacked(1L << posIndex, player, s);
 	}
 	
 	/**
 	 * checks to see if the passed position mask is attacked by passed player
 	 * @param posMask mask containing positions to check for attacks
 	 * @param player player doing the attacking
 	 * @param s board state to check for attacks
 	 * @return returns true if attacked, false otherwise
 	 */
 	public static boolean maskIsAttacked(long posMask, final int player, final State4 s){
 		for(; posMask != 0; posMask &= posMask-1){
 			if(posIsAttacked(BitUtil.lsb(posMask), player, s)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * executs passed move
 	 * @param player player moving
 	 * @param pieceMask mask for starting position of the piece to move
 	 * @param moveMask mask for final location of the piece
 	 * @return returns move encoding
 	 */
 	public long executeMove(final int player, final long pieceMask, final long moveMask, final int pawnPromoteType){
 		final int pos1 = BitUtil.lsbIndex(pieceMask);
 		final int pos2 = BitUtil.lsbIndex(moveMask);
 		
 		assert pieceMask != 0;
 		assert moveMask != 0;
 		assert player == 0 || player == 1;
 		assert (pieces[player] & pieces[1-player]) == 0;
 		assert (pieceMask & pieces[1-player]) == 0;
 		assert (moveMask & pieces[player]) == 0;
 		
 		zkey ^= zhash.turnChange;
 		final int movingType = mailbox[pos1];
 		final int takenType = mailbox[pos2];
 		
 		assert movingType != 0;
 		
 		final long z = zhash.getZHash(player, movingType, pos1) ^ zhash.getZHash(player, movingType, pos2);
 		zkey ^= z;
 		if(movingType == PIECE_TYPE_PAWN || movingType == PIECE_TYPE_KING){
 			pawnZkey ^= z;
 		}
 
 		long encoding = MoveEncoder.encode(pos1, pos2, movingType, takenType, player);
 		
 		if(movingType == PIECE_TYPE_KING && !kingMoved[player]){
 			encoding = MoveEncoder.setFirstKingMove(encoding);
 			kingMoved[player] = true;
 			if(player == 0){
 				if(pos2 == 2){
 					//castle left
 					rooks[0] &= ~1L;
 					rooks[0] |= 0x8L;
 					mailbox[0] = PIECE_TYPE_EMPTY;
 					mailbox[3] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(0, PIECE_TYPE_ROOK, 0) ^
 							zhash.getZHash(0, PIECE_TYPE_ROOK, 3); //should probably be a stored constant
 					encoding = MoveEncoder.setCastle(encoding);
 				} else if(pos2 == 6){
 					//castle right
 					rooks[0] &= ~0x80L;
 					rooks[0] |= 0x20L;
 					mailbox[7] = PIECE_TYPE_EMPTY;
 					mailbox[5] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(0, PIECE_TYPE_ROOK, 7) ^
 							zhash.getZHash(0, PIECE_TYPE_ROOK, 5);
 					encoding = MoveEncoder.setCastle(encoding);
 				}
 			} else { //player 1
 				if(pos2 == 58){
 					//castle left
 					rooks[1] &= ~0x100000000000000L;
 					rooks[1] |= 0x800000000000000L;
 					mailbox[56] = PIECE_TYPE_EMPTY;
 					mailbox[59] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(1, PIECE_TYPE_ROOK, 56) ^
 							zhash.getZHash(1, PIECE_TYPE_ROOK, 59);
 					encoding = MoveEncoder.setCastle(encoding);
 				} else if(pos2 == 62){
 					//castle right
 					rooks[1] &= ~0x8000000000000000L;
 					rooks[1] |= 0x2000000000000000L;
 					mailbox[63] = PIECE_TYPE_EMPTY;
 					mailbox[61] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(1, PIECE_TYPE_ROOK, 63) ^
 							zhash.getZHash(1, PIECE_TYPE_ROOK, 61);
 					encoding = MoveEncoder.setCastle(encoding);
 				}
 			} 
 		}
 		
 		if(movingType == PIECE_TYPE_ROOK && !kingMoved[player]){
 			if(!rookMoved[player][0] && (pieceMask & Masks.rookStartingPos[player][0]) != 0){ //rook on left starting pos moved
 				encoding = MoveEncoder.setFirstRookMove(player, 0, encoding);
 				rookMoved[player][0] = true;
 			} else if(!rookMoved[player][1] && (pieceMask & Masks.rookStartingPos[player][1]) != 0){ //rook on right starting pos moved
 				encoding = MoveEncoder.setFirstRookMove(player, 1, encoding);
 				rookMoved[player][1] = true;
 			}
 		}
 		if(takenType == PIECE_TYPE_ROOK && !kingMoved[1-player]){
 			if(!rookMoved[1-player][0] && (moveMask & Masks.rookStartingPos[1-player][0]) != 0){ //rook on left starting pos taken
 				encoding = MoveEncoder.setFirstRookMove(1-player, 0, encoding);
 				rookMoved[1-player][0] = true;
 			} else if(!rookMoved[1-player][1] && (moveMask & Masks.rookStartingPos[1-player][1]) != 0){ //rook on right starting pos taken
 				encoding = MoveEncoder.setFirstRookMove(1-player, 1, encoding);
 				rookMoved[1-player][1] = true;
 			}
 		}
 		
 		//move the first piece
 		final long[] b1 = pieceMasks[movingType];
 		b1[player] = (b1[player] & ~pieceMask) | moveMask;
 
 		//remove the second piece if move was a take
 		if(takenType != PIECE_TYPE_EMPTY){ //handle piece take
 			pieceMasks[takenType][1-player] &= ~moveMask;
 			pieceCounts[1-player][takenType]--;
 			pieceCounts[1-player][PIECE_TYPE_EMPTY]--;
 			
 			final long z2 = zhash.getZHash(1-player, takenType, pos2);
 			zkey ^= z2;
 			
 			if(takenType == PIECE_TYPE_PAWN){
 				pawnZkey ^= z2;
 			}
 		}
 		
 		
 		assert pos1 != pos2;
 		
 		mailbox[pos2] = mailbox[pos1];
 		mailbox[pos1] = PIECE_TYPE_EMPTY;
 
 		long prevEnPassante = enPassante; //make new copy to clear old
 		if(prevEnPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(prevEnPassante)];
 			encoding = MoveEncoder.setPrevEnPassantePos(prevEnPassante, encoding);
 		}
 		
 		//special pawn code for promotion, en passant
 		enPassante = 0;
 		if(movingType == PIECE_TYPE_PAWN){
 			if((moveMask & Masks.pawnPromotionMask[player]) != 0){
 				//pawn promotion
 				final int ptype = pawnPromoteType+2; //piece type for promotion
 				mailbox[pos2] = ptype;
 				pawns[player] &= ~moveMask;
 				pieceMasks[ptype][player] |= moveMask;
 				pieceCounts[player][PIECE_TYPE_PAWN]--;
 				pieceCounts[player][ptype]++;
 				encoding = MoveEncoder.setPawnPromotion(encoding, pawnPromoteType);
 				
 				final long pkey = zhash.getZHash(player, PIECE_TYPE_PAWN, pos2); 
 				zkey ^= pkey ^ zhash.getZHash(player, ptype, pos2);
 				pawnZkey ^= pkey;
 			} else if((player == 0 && (pieceMask & 0xFF00L) != 0 && (moveMask & 0xFF000000L) != 0) ||
 					(player == 1 && (pieceMask & 0xFF000000000000L) != 0 && (moveMask & 0xFF00000000L) != 0)){
 				//pawn moved 2 squares
 				long enPassantMask = player == 0? moveMask >>> 8: moveMask << 8;
 				if((enPassantMask & Masks.getRawPawnAttacks(1-player, pawns[1-player])) != 0){
 					//en passant matters (enemy pawn can attack), set en passant
 					enPassante = enPassantMask;
 					zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 				}
 			} else if(moveMask == prevEnPassante){
 				//making an en passante take move
 				final long takePos = player == 0? moveMask >>> 8: moveMask << 8;
 				pawns[1-player] &= ~takePos;
 				final int pos3 = BitUtil.lsbIndex(takePos);
 				mailbox[pos3] = PIECE_TYPE_EMPTY;
 				pieceCounts[1-player][PIECE_TYPE_PAWN]--;
 				encoding = MoveEncoder.setEnPassanteTake(encoding);
 				pieceCounts[1-player][PIECE_TYPE_EMPTY]--;
 				
 				final long pkey = zhash.getZHash(1-player, PIECE_TYPE_PAWN, pos3);
 				zkey ^= pkey;
 				pawnZkey ^= pkey;
 			}
 		}
 		
 
 		encoding = MoveEncoder.setPrevDrawCount(encoding, drawCount);
 		drawCount++;
 		if(movingType == PIECE_TYPE_PAWN || takenType != PIECE_TYPE_EMPTY){
 			drawCount = 0;
 		}
 		
 		collect();
 		
 		history[hindex++] = encoding;
 		hm.put(zkey);
 		
 		return encoding;
 	}
 	
 	public void nullMove(){
 		zkey ^= zhash.turnChange;
 		
 		long encoding = 0;
 		if(enPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 			encoding = MoveEncoder.setPrevEnPassantePos(enPassante, encoding);
 		}
 		enPassante = 0;
 		encoding = MoveEncoder.setPrevDrawCount(encoding, drawCount);
 		
 		history[hindex++] = encoding;
 	}
 	
 	public void undoNullMove(){
 		zkey ^= zhash.turnChange;
 		final long encoding = history[--hindex];
 		
 		final long prevEnPassantPos = MoveEncoder.getPrevEnPassantePos(encoding);
 		final long hasPrevEnPassant = BitUtil.isDef(prevEnPassantPos);
 		enPassante = (1L<<prevEnPassantPos)*hasPrevEnPassant;
 		zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)]*hasPrevEnPassant;
 		drawCount = MoveEncoder.getPrevDrawCount(encoding);
 	}
 	
 	public void undoMove(){
 		final long encoding = history[--hindex];
 		
 		hm.remove(zkey);
 		
 		assert encoding != 0;
 		assert (pieces[0] & pieces[1]) == 0;
 		
 		final long q = 1;
 		final int pos1 = MoveEncoder.getPos1(encoding);
 		final int pos2 = MoveEncoder.getPos2(encoding);
 		final int takenType = MoveEncoder.getTakenType(encoding);
 		final int moveType = MoveEncoder.getMovePieceType(encoding);
 		final int player = MoveEncoder.getPlayer(encoding);
 		assert player == 0 || player == 1;
 		
 		zkey ^= zhash.turnChange;
 		int type = mailbox[pos2];
 
 		assert type != 0;
 		
 		final long z = zhash.getZHash(player, type, pos2) ^ zhash.getZHash(player, type, pos1);
 		zkey ^= z;
 		if(type == PIECE_TYPE_PAWN || type == PIECE_TYPE_KING){
 			pawnZkey ^= z;
 		}
 
 		final long[] b1 = pieceMasks[mailbox[pos2]];
 		
 		if(mailbox[pos2] == PIECE_TYPE_KING && MoveEncoder.isFirstKingMove(encoding)){
 			kingMoved[player] = false;
 			
 			//check for castle, undo if necessary
 			if(player == 0){
 				if(pos2 == 2){
 					//castle left
 					rooks[0] &= ~0x8L;
 					rooks[0] |= 1L;
 					mailbox[3] = PIECE_TYPE_EMPTY;
 					mailbox[0] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(0, PIECE_TYPE_ROOK, 0) ^
 							zhash.getZHash(0, PIECE_TYPE_ROOK, 3);
 				} else if(pos2 == 6){
 					//castle right
 					rooks[0] &= ~0x20L;
 					rooks[0] |= 0x80L;
 					mailbox[5] = PIECE_TYPE_EMPTY;
 					mailbox[7] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(0, PIECE_TYPE_ROOK, 5) ^
 							zhash.getZHash(0, PIECE_TYPE_ROOK, 7);
 				}
 			} else { //player 1
 				if(pos2 == 58){
 					//castle left
 					rooks[1] &= ~0x800000000000000L;
 					rooks[1] |= 0x100000000000000L;
 					mailbox[59] = PIECE_TYPE_EMPTY;
 					mailbox[56] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(1, PIECE_TYPE_ROOK, 59) ^
 							zhash.getZHash(1, PIECE_TYPE_ROOK, 56);
 				} else if(pos2 == 62){
 					//castle right
 					rooks[1] &= ~0x2000000000000000L;
 					rooks[1] |= 0x8000000000000000L;
 					mailbox[61] = PIECE_TYPE_EMPTY;
 					mailbox[63] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.getZHash(1, PIECE_TYPE_ROOK, 61) ^
 							zhash.getZHash(1, PIECE_TYPE_ROOK, 63);
 				}
 			} 
 		}
 		
 		if(moveType == PIECE_TYPE_ROOK && MoveEncoder.isFirstRookMove(player, encoding)){
 			final int rookTakenSide = MoveEncoder.getFirstRookMoveSide(player, encoding);
 			rookMoved[player][rookTakenSide] = false;
 		}
 		if(takenType == PIECE_TYPE_ROOK && MoveEncoder.isFirstRookMove(1-player, encoding)){
 			final int rookTakenSide = MoveEncoder.getFirstRookMoveSide(1-player, encoding);
 			rookMoved[1-player][rookTakenSide] = false;
 		}
 		
 		//undo move of piece
 		b1[player] = (b1[player] & ~(q<<pos2)) | q<<pos1;
 		mailbox[pos1] = mailbox[pos2];
 		mailbox[pos2] = takenType;
 
 		//add back take piece
 		if(takenType != PIECE_TYPE_EMPTY){
 			pieceMasks[takenType][1-player] |= 1L<<pos2;
 			pieceCounts[1-player][takenType]++;
 			pieceCounts[1-player][PIECE_TYPE_EMPTY]++;
 			
 			final long z2 = zhash.getZHash(1-player, takenType, pos2); 
 			zkey ^= z2;
 			
 			if(takenType == PIECE_TYPE_PAWN){
 				pawnZkey ^= z2;
 			}
 		}
 		
 		
 		//undo pawn promotion
 		if(MoveEncoder.isPawnPromotion(encoding)){
 			final int ptype = MoveEncoder.getPawnPromotionType(encoding)+2;
 			long moveMask = 1L<<pos1;
 			mailbox[pos1] = PIECE_TYPE_PAWN;
 			pieceMasks[ptype][player] &= ~moveMask;
 			pawns[player] |= moveMask;
 			pieceCounts[player][PIECE_TYPE_PAWN]++;
 			pieceCounts[player][ptype]--;
 			
 			final long pzkey = zhash.getZHash(player, PIECE_TYPE_PAWN, pos1);
 			zkey ^= pzkey ^ zhash.getZHash(player, ptype, pos1);
 			pawnZkey ^= pzkey;
 		}
 
 		//clear any current en passant square
 		if(enPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 		}
 		
 		enPassante = 0;
 		
 		//re-apply previous en passant square
 		final long prevEnPassantPos = MoveEncoder.getPrevEnPassantePos(encoding);
 		final long hasPrevEnPassant = BitUtil.isDef(prevEnPassantPos);
 		enPassante = (1L<<prevEnPassantPos)*hasPrevEnPassant;
 		zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)]*hasPrevEnPassant;
 
 		//undo an en passante take
 		if(MoveEncoder.isEnPassanteTake(encoding) != 0){
 			final long takePos = player == 0? (1L<<pos2) >>> 8: (1L<<pos2) << 8;
 			pawns[1-player] |= takePos;
 			int pos3 = BitUtil.lsbIndex(takePos);
 			mailbox[pos3] = PIECE_TYPE_PAWN;
 			pieceCounts[1-player][PIECE_TYPE_PAWN]++;
 			//enPassante = takePos;
 			pieceCounts[1-player][PIECE_TYPE_EMPTY]++;
 			
 			final long pkey = zhash.getZHash(1-player, PIECE_TYPE_PAWN, pos3);
 			zkey ^= pkey;
 			pawnZkey ^= pkey;
 		}
 
 		collect();
 		drawCount = MoveEncoder.getPrevDrawCount(encoding);
 	}
 	
 	/** collects all pieces onto the player piece aggregators*/
 	private void collect(){
 		pieces[0] = pawns[0] | knights[0] | kings[0] | queens[0] | rooks[0] | bishops[0];
 		pieces[1] = pawns[1] | knights[1] | kings[1] | queens[1] | rooks[1] | bishops[1];
 		assert (pieces[0] & pieces[1]) == 0;
 	}
 	
 	public String toString(){
 		char[] c = new char[64];
 		for(int i = 0; i < 64; i++){
 			c[i] = '-';
 		}
 		
 		long[][] l = new long[][]{pawns,kings,queens,rooks,bishops,knights};
 		char[] rep = new char[]{'p','k','q','r','b','n'};
 		for(int i = 0; i < l.length; i++){
 			for(int a = 0; a <= 1; a++){
 				long p = l[i][a];
 				while(p != 0){
 					int index = BitUtil.lsbIndex(p);
 					c[index] = a == 0? rep[i]: Character.toUpperCase(rep[i]);
 					p = p&(p-1);
 				}
 			}
 		}
 		
 		String s = "";
 		String temp = "";
 		for(int i = 63; i >= 0; i--){
 			temp = c[i]+temp;
 			if(i % 8 == 0){
 				s += temp;
 				if(i != 0){
 					s += "\n";
 				}
 				temp = "";
 			}
 		}
 		return s;
 	}
 	
 	/** initializes all pieces to starting locations and clears history map and list*/
 	public void initialize(){
 		long p = 0xFFL;
 		long q = 0x8L;
 		long k = 0x10L;
 		long n = 0x42L;
 		long b = 0x24L;
 		long r = 0x81L;
 		
 		pawns[0] = p<<8;
 		pawns[1] = p<<8*6;
 		queens[0] = q;
 		queens[1] = q<<8*7;
 		kings[0] = k;
 		kings[1] = k<<8*7;
 		knights[0] = n;
 		knights[1] = n<<8*7;
 		bishops[0] = b;
 		bishops[1] = b<<8*7;
 		rooks[0] = r;
 		rooks[1] = r<<8*7;
 		
 		update(State4.WHITE);
 	}
 	
 	public long zkey(){
 		final int count = hm.get(zkey);
 		assert count >= 0;
 		
 		long castleKey = 0;
 		if(!kingMoved[0] && !rookMoved[0][0]) castleKey ^= zhash.canCastle[0][0];
 		if(!kingMoved[0] && !rookMoved[0][1]) castleKey ^= zhash.canCastle[0][1];
 		if(!kingMoved[1] && !rookMoved[1][0]) castleKey ^= zhash.canCastle[1][0];
 		if(!kingMoved[1] && !rookMoved[1][1]) castleKey ^= zhash.canCastle[1][1];
 		
 		return zkey ^ appHashs[count < 4? count: 3] ^ castleKey;
 	}
 	
 	public long pawnZkey(){
 		return pawnZkey;
 	}
 
 	/**
 	 * convenience method for setting mailbox and zkey information
 	 * for all pieces of a certain type
 	 * @param pieceType type to set mailbox index
 	 * @param pieces mask containing piece locations to set
 	 */
 	private void setPieceMetaInfo(final int player, final int pieceType, final long pieces){
 		long piece = pieces;
 		while(piece != 0){
 			int index = BitUtil.lsbIndex(piece);
 			mailbox[index] = pieceType;
 
 			final long z = zhash.getZHash(player, pieceType, index);
 			zkey ^= z;
 			if(pieceType == PIECE_TYPE_PAWN){
 				pawnZkey ^= z;
 			}
 
			piece = piece&(piece-1);
 		}
 
 		int count = (int)BitUtil.getSetBits(pieces);
 		pieceCounts[player][PIECE_TYPE_EMPTY] += count;
 		pieceCounts[player][pieceType] = count;
 	}
 
 	/** 
 	 * (1) updates mailbox, zkey, to reflect board position
 	 * (2) clears history
 	 * (3) properly collects pieces into piece aggregrates
 	 * <p> this should only be called once after the pieces have been set up
 	 * on a new board. Afterwards, everything will be maintained incrementally
 	 */
 	public void update(int sideToMove){
 		//build zkey, mailbox, and piece counts
 		zkey = 0;
 		pawnZkey = 0;
 		pieceCounts[0][PIECE_TYPE_EMPTY] = 0;
 		pieceCounts[1][PIECE_TYPE_EMPTY] = 0;
 
 		for(int a = 0; a < 64; a++) mailbox[a] = PIECE_TYPE_EMPTY;
 
 		for(int p = 0; p < 2; p++){
 			setPieceMetaInfo(p, PIECE_TYPE_PAWN, pawns[p]);
 			setPieceMetaInfo(p, PIECE_TYPE_BISHOP, bishops[p]);
 			setPieceMetaInfo(p, PIECE_TYPE_KNIGHT, knights[p]);
 			setPieceMetaInfo(p, PIECE_TYPE_ROOK, rooks[p]);
 			setPieceMetaInfo(p, PIECE_TYPE_QUEEN, queens[p]);
 			setPieceMetaInfo(p, PIECE_TYPE_KING, kings[p]);
 		}
 
 		zkey ^= zhash.turn[sideToMove];
 		if(enPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 		}
 
 		hm.clear();
 		hm.put(zkey);
 		resetHistory();
 		collect();
 	}
 }
