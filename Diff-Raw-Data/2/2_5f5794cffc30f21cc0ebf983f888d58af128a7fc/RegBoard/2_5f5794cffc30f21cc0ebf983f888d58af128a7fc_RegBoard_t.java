 package com.chess.genesis;
 
 class RegBoard extends RegPosition
 {
 	public static final int[] InitRegBoard = {
 		Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_QUEEN, Piece.BLACK_KING, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK,
 		Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN, Piece.BLACK_PAWN,
 		Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY,
 		Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY,
 		Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY,
 		Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY,
 		Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN, Piece.WHITE_PAWN,
 		Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_QUEEN, Piece.WHITE_KING, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK};
 
 	public static final RegPiece[] InitRegPiece = {
 		new RegPiece(Piece.A7, Piece.BLACK_PAWN), new RegPiece(Piece.B7, Piece.BLACK_PAWN),
 		new RegPiece(Piece.C7, Piece.BLACK_PAWN), new RegPiece(Piece.D7, Piece.BLACK_PAWN),
 		new RegPiece(Piece.E7, Piece.BLACK_PAWN), new RegPiece(Piece.F7, Piece.BLACK_PAWN), new RegPiece(Piece.G7, Piece.BLACK_PAWN), new RegPiece(Piece.H7, Piece.BLACK_PAWN),
 		new RegPiece(Piece.B8, Piece.BLACK_KNIGHT), new RegPiece(Piece.G8, Piece.BLACK_KNIGHT), new RegPiece(Piece.C8, Piece.BLACK_BISHOP), new RegPiece(Piece.F8, Piece.BLACK_BISHOP),
 		new RegPiece(Piece.A8, Piece.BLACK_ROOK), new RegPiece(Piece.H8, Piece.BLACK_ROOK), new RegPiece(Piece.D8, Piece.BLACK_QUEEN), new RegPiece(Piece.E8, Piece.BLACK_KING),
 		new RegPiece(Piece.A2, Piece.WHITE_PAWN), new RegPiece(Piece.B2, Piece.WHITE_PAWN), new RegPiece(Piece.C2, Piece.WHITE_PAWN), new RegPiece(Piece.D2, Piece.WHITE_PAWN),
 		new RegPiece(Piece.E2, Piece.WHITE_PAWN), new RegPiece(Piece.F2, Piece.WHITE_PAWN), new RegPiece(Piece.G2, Piece.WHITE_PAWN), new RegPiece(Piece.H2, Piece.WHITE_PAWN),
 		new RegPiece(Piece.B1, Piece.WHITE_KNIGHT), new RegPiece(Piece.G1, Piece.WHITE_KNIGHT), new RegPiece(Piece.C1, Piece.WHITE_BISHOP), new RegPiece(Piece.F1, Piece.WHITE_BISHOP),
 		new RegPiece(Piece.A1, Piece.WHITE_ROOK), new RegPiece(Piece.H1, Piece.WHITE_ROOK), new RegPiece(Piece.D1, Piece.WHITE_QUEEN), new RegPiece(Piece.E1, Piece.WHITE_KING)	};
 
 	public static final int[][] regLocValue = {
 		{0, 0, 0, 0, 0, 0, 0, 0,
 		0, 0, 0, 0, 0, 0, 0, 0,
 		0, 0, 0, 0, 0, 0, 0, 0,
 		0, 0, 0, 0, 0, 0, 0, 0,
 		0, 0, 0, 0, 0, 0, 0, 0,
 		0, 0, 0, 0, 0, 0, 0, 0,
 		0, 0, 0, 0, 0, 0, 0, 0,
 		0, 0, 0, 0, 0, 0, 0, 0},
 	{	  0,   0,   0,   0,   0,   0,   0,   0,
 /* Pawn */	 10,  10,  10,  10,  10,  10,  10,  10,
 		  5,   5,   5,   5,   5,   5,   5,   5,
 		  0,   0,   5,   5,   5,   5,   0,   0,
 		  0,   0,  -5,  -5,  -5,  -5,   0,   0,
 		 -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,
 		-10, -10, -10, -10, -10, -10, -10, -10,
 		  0,   0,   0,   0,   0,   0,   0,   0},
 	{	-10, -5,  0,  0,  0,  0, -5, -10,
 /* Knight */	 -5,  0, 10, 10, 10, 10,  0,  -5,
 		  0, 10, 20, 20, 20, 20, 10,   0,
 		  0, 10, 20, 20, 20, 20, 10,   0,
 		  0, 10, 20, 20, 20, 20, 10,   0,
 		  0, 10, 20, 20, 20, 20, 10,   0,
 		 -5,  0, 10, 10, 10, 10,  0,  -5,
 		-10, -5,  0,  0,  0,  0, -5, -10},
 	{	-10, -10, -10, -10, -10, -10, -10, -10,
 /* Bishop */	-10,   0,   0,   0,   0,   0,   0, -10,
 		-10,   0,  10,  10,  10,  10,   0, -10,
 		-10,   0,  10,  20,  20,  10,   0, -10,
 		-10,   0,  10,  20,  20,  10,   0, -10,
 		-10,   0,  10,  10,  10,  10,   0, -10,
 		-10,   0,   0,   0,   0,   0,   0, -10,
 		-10, -10, -10, -10, -10, -10, -10, -10},
 	{	-10, -10, -10, -10, -10, -10, -10, -10,
 /* Rook */	-10,   0,   0,   0,   0,   0,   0, -10,
 		-10,   0,  10,  10,  10,  10,   0, -10,
 		-10,   0,  10,  20,  20,  10,   0, -10,
 		-10,   0,  10,  20,  20,  10,   0, -10,
 		-10,   0,  10,  10,  10,  10,   0, -10,
 		-10,   0,   0,   0,   0,   0,   0, -10,
 		-10, -10, -10, -10, -10, -10, -10, -10},
 	{	-10, -10, -10, -10, -10, -10, -10, -10,
 /* Queen */	-10,   0,   0,   0,   0,   0,   0, -10,
 		-10,   0,  10,  10,  10,  10,   0, -10,
 		-10,   0,  10,  20,  20,  10,   0, -10,
 		-10,   0,  10,  20,  20,  10,   0, -10,
 		-10,   0,  10,  10,  10,  10,   0, -10,
 		-10,   0,   0,   0,   0,   0,   0, -10,
 		-10, -10, -10, -10, -10, -10, -10, -10},
 	{	  0, -5, -10, -15, -20, -15, -10, -5,
 /* King */	  0, -5, -10, -15, -15, -15, -10, -5,
 		  0, -5, -10, -10, -10, -10, -10, -5,
 		  0, -5,  -5,  -5,  -5,  -5,  -5, -5,
 		  0,  5,   5,   5,   5,   5,   5,  5,
 		  0,  5,  10,  10,  10,  10,  10,  5,
 		  0,  5,  10,  15,  15,  15,  10,  5,
 		  0,  5,  10,  15,  20,  15,  10,  5}
 	};
 
 	public static final int[] regPieceValue =
 		{0, 224, 336, 560, 896, 1456, 0};
 
 	public static final int NOT_MATE = 1;
 	public static final int CHECK_MATE = 2;
 	public static final int STALE_MATE = 3;
 
 	public static final int MOVE_ALL = 0;
 	public static final int MOVE_CAPTURE = 1;
 	public static final int MOVE_MOVE = 2;
 	public static final int MOVE_PLACE = 3;
 
 	public static final int ZBOX_SIZE = 838;
 	public static final int WTM_HASH = 837;
 	public static final int HOLD_START = 768;
 	public static final int HOLD_END = 781;
 	public static final int ENPASSANT_HASH = 834;
 	public static final int CASTLE_HASH = 834;
 
 	public static final int VALID_MOVE = 0;
 	public static final int INVALID_FORMAT = 1;
 	public static final int NOPIECE_ERROR = 2;
 	public static final int DONT_OWN = 3;
 	public static final int KING_FIRST = 4;
 	public static final int NON_EMPTY_PLACE = 5;
 	public static final int CAPTURE_OWN = 6;
 	public static final int INVALID_MOVEMENT = 7;
 	public static final int IN_CHECK = 8;
 	public static final int IN_CHECK_PLACE = 9;
 	public static final int CANT_CASTLE = 10;
 
 	public static long[] hashBox = new long[ZBOX_SIZE];
 	public static long startHash;
 
 	private long key;
 
 	public RegBoard()
 	{
 		reset();
 	}
 
 	public RegBoard(final RegBoard board)
 	{
 		square = IntArray.clone(board.square);
 		piece = RegPiece.arrayCopy(board.piece);
 
 		flags = new MoveFlags(board.flags);
 		ply = board.ply;
 		stm = board.stm;
 		key = board.key;
 	}
 
 	public void reset()
 	{
 		square = IntArray.clone(InitRegBoard);
 		piece = RegPiece.arrayCopy(InitRegPiece);
 
 		key = startHash;
 		stm = Piece.WHITE;
 		ply = 0;
 		flags.reset();
 	}
 
 	public int getStm()
 	{
 		return stm;
 	}
 
 	public int getPly()
 	{
 		return ply;
 	}
 
 	public long hash()
 	{
 		return key;
 	}
 
 	public int kingIndex(final int color)
 	{
 		return (Piece.WHITE == color)? piece[31].loc : piece[15].loc;
 	}
 
 	public MoveFlags getMoveFlags()
 	{
 		return new MoveFlags(flags);
 	}
 
 	private void rebuildHash()
 	{
 		key = startHash;
 		key ^= (stm == Piece.WHITE)? hashBox[WTM_HASH] : 0;
 
 		for (int i = 0; i < 32; i++) {
 			if (piece[i].loc != Piece.DEAD)
 				key ^= hashBox[12 * piece[i].loc + piece[i].type];
 		}
 		key ^= ((flags.bits & 0x08) != 0)? hashBox[ENPASSANT_HASH] : 0;
 		key ^= ((flags.bits & 0x10) != 0)? hashBox[CASTLE_HASH + Piece.WHITE] : 0;
 		key ^= ((flags.bits & 0x40) != 0)? hashBox[CASTLE_HASH + Piece.BLACK] : 0;
 		key ^= ((flags.bits & 0x20) != 0)? hashBox[CASTLE_HASH + 2 * Piece.WHITE] : 0;
 		key ^= ((flags.bits & 0x80) != 0)? hashBox[CASTLE_HASH + 2 * Piece.BLACK] : 0;
 	}
 
 	private RegPosition getPosition()
 	{
 		final RegPosition pos = new RegPosition();
 
 		pos.square = IntArray.clone(square);
 		pos.piece = RegPiece.arrayCopy(piece);
 
 		pos.flags = new MoveFlags(flags);
 		pos.ply = ply;
 		pos.stm = stm;
 
 		return pos;
 	}
 
 	public int[] getPieceCounts()
 	{
 		final int[] counts = new int[13];
 
 		for (int i = 0; i < 32; i++) {
 			if (piece[i].loc != Piece.DEAD)
 				counts[piece[i].type + 6]++;
 		}
 		return counts;
 	}
 
 	public int[] getBoardArray()
 	{
 		return square;
 	}
 
 	private int pieceIndex(final int loc, final int type)
 	{
 		final int start = (type > 0)? 16:0, end = (type > 0)? 32:16;
 
 		for (int i = start; i < end; i++)
 			if (piece[i].loc == loc && piece[i].type == type)
 				return i;
 		return Piece.NONE;
 	}
 
 	private boolean isPromote(final RegMove move, final int color)
 	{
 		return (color == Piece.WHITE)?
 			(move.to <= Piece.H8 && move.from <= Piece.H7) :
 			(move.to >= Piece.A1 && move.from >= Piece.A2);
 	}
 
 	public void make(final RegMove move)
 	{
 		final boolean isWhite = (move.index > 15);
 		final int color = isWhite? Piece.WHITE : Piece.BLACK;
 
 		key ^= hashBox[13 * move.from + piece[move.index].type + 6];
 
 		if (move.getCastle() != 0) {
 			final boolean left = (move.getCastle() == 0x20);
 			final int castleTo = move.to + (left? 1 : -1);
 			final int castleI = pieceIndex(move.to - (move.to & 0x7) + (left? 0 : 7), color * Piece.ROOK);
 
 			key ^= hashBox[13 * piece[castleI].loc + piece[castleI].type + 6];
 			key ^= hashBox[13 * castleTo + piece[castleI].type + 6];
 			if (flags.canKingCastle(color) != 0)
 				key ^= hashBox[CASTLE_HASH + color];
 			if (flags.canQueenCastle(color) != 0)
 				key ^= hashBox[CASTLE_HASH + color * 2];
 
 			square[castleTo] = piece[castleI].type;
 			square[piece[castleI].loc] = Piece.EMPTY;
 			piece[castleI].loc = castleTo;
 			flags.clearCastle(color);
 		} else if (Math.abs(piece[move.index].type) == Piece.ROOK) {
 			if (move.from == (isWhite? Piece.H1:Piece.H8) && flags.canKingCastle(color) != 0) {
 				flags.clearKingCastle(color);
 				key ^= hashBox[CASTLE_HASH + color];
 			} else if (move.from == (isWhite? Piece.A1:Piece.A8) && flags.canQueenCastle(color) != 0) {
 				flags.clearQueenCastle(color);
 				key ^= hashBox[CASTLE_HASH + color * 2];
 			}
 		} else if (Math.abs(piece[move.index].type) == Piece.KING && flags.canCastle(color) != 0) {
 			if (flags.canKingCastle(color) != 0)
 				key ^= hashBox[CASTLE_HASH + color];
 			if (flags.canQueenCastle(color) != 0)
 				key ^= hashBox[CASTLE_HASH + color * 2];
 			flags.clearCastle(color);
 		} else if (move.getPromote() != 0) {
 			piece[move.index].type = move.getPromote() * color;
 		}
 		key ^= hashBox[13 * move.to + piece[move.index].type + 6];
 
 		if (flags.canEnPassant() != 0) {
 			flags.clearEnPassant();
 			key ^= hashBox[ENPASSANT_HASH];
 		}
 
 		// update board information
 		square[move.to] = piece[move.index].type;
 		square[move.from] = Piece.EMPTY;
 		// update piece information
 		piece[move.index].loc = move.to;
 		if (move.xindex != Piece.NONE) {
 			key ^= hashBox[13 * piece[move.xindex].loc + piece[move.xindex].type + 6];
 			if (move.getEnPassant())
 				square[piece[move.xindex].loc] = Piece.EMPTY;
 			piece[move.xindex].loc = Piece.DEAD;
 		} else if (Math.abs(piece[move.index].type) == Piece.PAWN && Math.abs(move.to - move.from) == 16) {
 			flags.setEnPassant(move.to & 0x7);
 			key ^= hashBox[ENPASSANT_HASH];
 		}
 
 		key ^= hashBox[WTM_HASH];
 		stm ^= -2;
 		ply++;
 	}
 
 	public void unmake(final RegMove move, final MoveFlags undoFlags)
 	{
 		final boolean isWhite = (move.index > 15);
 		final int color = isWhite? Piece.WHITE : Piece.BLACK,
 			bits = flags.bits ^ undoFlags.bits;
 
 		key ^= ((bits & ((color == Piece.WHITE)? 0x10 : 0x40)) != 0)? hashBox[CASTLE_HASH + color] : 0;
 		key ^= ((bits & ((color == Piece.WHITE)? 0x20 : 0x80)) != 0)? hashBox[CASTLE_HASH + 2 * color] : 0;
 		key ^= ((bits & 0x8) != 0)? hashBox[ENPASSANT_HASH] : 0;
 		key ^= hashBox[13 * move.to + piece[move.index].type + 6];
 
 		if (move.getCastle() != 0) {
 			final boolean left = (move.from - move.to > 0);
 			final int castleFrom = move.to - (move.to & 0x7) + (left? 0 : 7);
 			final int castleI = pieceIndex(move.to + (left? 1 : -1), isWhite? Piece.WHITE_ROOK : Piece.BLACK_ROOK);
 
 			key ^= hashBox[13 * piece[castleI].loc + piece[castleI].type + 6];
 			key ^= hashBox[13 * castleFrom + piece[castleI].type + 6];
 
 			square[piece[castleI].loc] = Piece.EMPTY;
 			square[castleFrom] = piece[castleI].type;
 			piece[castleI].loc = castleFrom;
 		} else if (move.getPromote() != 0) {
 			piece[move.index].type = Piece.PAWN * color;
 		}
 
 		key ^= hashBox[13 * move.from + piece[move.index].type + 6];
 
 		piece[move.index].loc = move.from;
 		if (move.xindex == Piece.NONE) {
 			square[move.to] = Piece.EMPTY;
 		} else {
 			if (move.getEnPassant()) {
 				piece[move.xindex].loc = move.to + ((move.from - move.to > 0)? 8 : -8);
 				square[piece[move.xindex].loc] = Piece.PAWN * -color;
 				square[move.to] = Piece.EMPTY;
 			} else {
 				piece[move.xindex].loc = move.to;
 				square[move.to] = piece[move.xindex].type;
 			}
 			key ^= hashBox[13 * piece[move.xindex].loc + piece[move.xindex].type + 6];
 		}
 		square[move.from] = piece[move.index].type;
 
 		key ^= hashBox[WTM_HASH];
 		flags.bits = undoFlags.bits;
 		stm ^= -2;
 		ply--;
 	}
 
 	public int isMate()
 	{
 		if (anyMoves(stm))
 			return NOT_MATE;
 		else if (incheck(stm))
 			return CHECK_MATE;
 		else
 			return STALE_MATE;
 	}
 
 	public boolean validMove(final RegMove moveIn, final RegMove move)
 	{
 		if (move.from == move.to)
 			return false;
 
 		final MoveFlags undoFlags = new MoveFlags(flags);
 		move.set(moveIn);
 
 		move.index = pieceIndex(move.from, square[move.from]);
 		if (move.index == Piece.NONE)
 			return false;
 		else if (square[move.from] * stm < 0)
 			return false;
 		move.xindex = pieceIndex(move.to, square[move.to]);
 		if (move.xindex != Piece.NONE && square[move.to] * stm > 0)
 			return false;
 
 		if (move.getCastle() != 0) {
 			return validCastle(move, stm) == VALID_MOVE;
 		} else if (move.getEnPassant() && flags.canEnPassant() != 0) {
 			return validEnPassant(move, stm) == VALID_MOVE;
 		} else if (isPromote(move, stm) && Math.abs(square[move.from]) == Piece.PAWN) {
 			if (move.getPromote() == 0)
 				move.setPromote(Piece.QUEEN);
 		} else {
 			move.flags = 0;
 		}
 
 		if (!fromto(move.from, move.to))
 			return false;
 
 		boolean ret = true;
 
 		make(move);
 		// stm is opponent after make
 		if (incheck(stm ^ -2))
 			ret = false;
 		unmake(move, undoFlags);
 
 		return ret;
 	}
 
 	public int validCastle(RegMove move, final int color)
 	{
 		// can we castle on that side
		if (flags.canCastle(color) == 0 || move.getCastle() == 0)
 			return CANT_CASTLE;
 		// can't castle while in check
 		if (incheck(color))
 			return CANT_CASTLE;
 
 		final int king = (color == Piece.WHITE)? Piece.E1 : Piece.E8;
 
 		// king side
 		if (move.getCastle() == 0x10 && square[king + 1] == Piece.EMPTY && square[king + 2] == Piece.EMPTY &&
 		!isAttacked(king + 1, color) && !isAttacked(king + 2, color) &&
 		Math.abs(square[((color == Piece.WHITE)? Piece.H1:Piece.H8)]) == Piece.ROOK) {
 			move.index = (color == Piece.WHITE)? 31 : 15;
 			move.xindex = Piece.NONE;
 			move.from = king;
 			move.to = king + 2;
 			return VALID_MOVE;
 		} else if (move.getCastle() == 0x20 && square[king - 1] == Piece.EMPTY && square[king - 2] == Piece.EMPTY &&
 		square[king - 3] == Piece.EMPTY && !isAttacked(king - 1, color) && !isAttacked(king - 2, color) &&
 		Math.abs(square[((color == Piece.WHITE)? Piece.A1:Piece.A8)]) == Piece.ROOK) {
 			move.index = (color == Piece.WHITE)? 31 : 15;
 			move.xindex = Piece.NONE;
 			move.from = king;
 			move.to = king - 2;
 			return VALID_MOVE;
 		}
 		return CANT_CASTLE;
 	}
 
 	public int validEnPassant(RegMove move, final int color)
 	{
 		final MoveFlags undoFlags = new MoveFlags(flags);
 		final int ep = flags.enPassantFile() + ((color == Piece.WHITE)? Piece.A5 : Piece.A4),
 			ep_to = ep + ((color == Piece.WHITE)? -8 : 8);
 
 		if (move.to == ep_to && Math.abs(ep - move.from) == 1) {
 			move.index = pieceIndex(move.from, square[move.from]);
 			move.xindex = pieceIndex(ep, square[ep]);
 			move.setEnPassant();
 
 			int ret = VALID_MOVE;
 
 			make(move);
 			// stm is opponent after make
 			if (incheck(stm ^ -2))
 				ret = IN_CHECK;
 			unmake(move, undoFlags);
 			return ret;
 		}
 		return INVALID_MOVEMENT;
 	}
 
 	public int validMove(final RegMove move)
 	{
 		final MoveFlags undoFlags = new MoveFlags(flags);
 		final int color = getStm();
 
 		// if castle flag is set, move must a castle to be valid
 		if (move.getCastle() != 0)
 			return validCastle(move, color);
 
 		move.index = pieceIndex(move.from, square[move.from]);
 
 		switch (Math.abs(piece[move.index].type)) {
 		case Piece.PAWN:
 			// en passant
 			if (flags.canEnPassant() != 0 && validEnPassant(move, color) == VALID_MOVE)
 				return VALID_MOVE;
 
 			if (!isPromote(move, color)) {
 				move.flags = 0;
 				break;
 			} else if (move.getPromote() == 0) {
 				// manualy set to queen if not specified
 				move.setPromote(Piece.QUEEN);
 			}
 			break;
 		case Piece.KING:
 			// manual castling without proper O-O/O-O-O notation
 			if (Math.abs(move.from - move.to) == 2) {
 				move.setCastle((move.from > move.to)? 0x20 : 0x10);
 				return validCastle(move, color);
 			}
 		default:
 			// move can't be special, so clear flags
 			move.flags = 0;
 		}
 
 		if (move.index == Piece.NONE)
 			return NOPIECE_ERROR;
 		else if (square[move.from] * color < 0)
 			return DONT_OWN;
 		move.xindex = pieceIndex(move.to, square[move.to]);
 		if (move.xindex != Piece.NONE && square[move.to] * color > 0)
 			return CAPTURE_OWN;
 
 		if (!fromto(move.from, move.to))
 			return INVALID_MOVEMENT;
 
 		int ret = VALID_MOVE;
 
 		make(move);
 		// stm is opponent after make
 		if (incheck(stm ^ -2))
 			ret = IN_CHECK;
 		unmake(move, undoFlags);
 
 		return ret;
 	}
 
 	public int eval()
 	{
 		int white = 0, black = 0;
 		for (int b = 0, w = 16; b < 16; b++, w++) {
 			if (piece[b].loc != Piece.DEAD) {
 				int mod = (piece[b].type == Piece.BLACK_PAWN || piece[b].type == Piece.BLACK_KING)? -1:1;
 				black += mod * regLocValue[-piece[b].type][piece[b].loc];
 				black += regPieceValue[-piece[b].type];
 			} else {
 				black -= regPieceValue[-piece[b].type];
 			}
 			if (piece[w].loc != Piece.DEAD) {
 				white += regLocValue[piece[w].type][piece[w].loc];
 				white += regPieceValue[piece[w].type];
 			} else {
 				white -= regPieceValue[piece[w].type];
 			}
 		}
 		white -= black;
 		return (stm == Piece.WHITE)? -white : white;
 	}
 
 	public boolean anyMoves(final int color)
 	{
 		final int start = (color == Piece.WHITE)? 31:15, end = (color == Piece.WHITE)? 16:0;
 		final MoveFlags undoFlags = new MoveFlags(flags);
 		final RegMoveNode item = new RegMoveNode();
 
 		for (int idx = start; idx >= end; idx--) {
 			if (piece[idx].loc == Piece.DEAD)
 				continue;
 
 			final int[] loc = genAll(piece[idx].loc);
 
 			for (int n = 0; loc[n] != -1; n++) {
 				item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
 				item.move.to = loc[n];
 				item.move.from = piece[idx].loc;
 				item.move.index = idx;
 
 				// nothing special for promotion
 				make(item.move);
 				if (!incheck(color)) {
 					unmake(item.move, undoFlags);
 					return true;
 				}
 				unmake(item.move, undoFlags);
 			}
 		}
 
 		// can't castle while in check
 		final boolean inCheck = incheck(color);
 		final int king = (color == Piece.WHITE)? Piece.E1 : Piece.E8;
 
 		// King Side
 		if (!inCheck && flags.canKingCastle(color) != 0 && square[king + 1] == Piece.EMPTY &&
 		square[king + 2] == Piece.EMPTY && !isAttacked(king + 1, color) &&
 		!isAttacked(king + 2, color) && Math.abs(square[((color == Piece.WHITE)? Piece.H1:Piece.H8)]) == Piece.ROOK) {
 			item.move.xindex = Piece.NONE;
 			item.move.to = king + 1;
 			item.move.from = king;
 			item.move.index = (color == Piece.WHITE)? 31:15;
 
 			make(item.move);
 			if (!incheck(color)) {
 				unmake(item.move, undoFlags);
 				return true;
 			}
 			unmake(item.move, undoFlags);
 		}
 		// Queen Side
 		if (!inCheck && flags.canQueenCastle(color) != 0 && square[king - 1] == Piece.EMPTY &&
 		square[king - 2] == Piece.EMPTY && square[king - 3] == Piece.EMPTY &&
 		!isAttacked(king - 1, color) && !isAttacked(king - 2, color) &&
 		Math.abs(square[((color == Piece.WHITE)? Piece.A1:Piece.A8)]) == Piece.ROOK) {
 			item.move.xindex =Piece.NONE;
 			item.move.to = king - 1;
 			item.move.from = king;
 			item.move.index = (color == Piece.WHITE)? 31:15;
 
 			make(item.move);
 			if (!incheck(color)) {
 				unmake(item.move, undoFlags);
 				return true;
 			}
 			unmake(item.move, undoFlags);
 		}
 
 		if (flags.canEnPassant() == 0)
 			return false;
 
 		final int eps_file = flags.enPassantFile(),
 			eps = eps_file + ((color == Piece.WHITE)? Piece.A5 : Piece.A4),
 			your_pawn = (color == Piece.WHITE)? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
 			opp_pawn = your_pawn * -1;
 
 		// en passant to left
 		if (eps_file != 0 && square[eps - 1] == your_pawn) {
 			item.move.xindex = pieceIndex(eps, opp_pawn);
 			item.move.to = eps + 8 * opp_pawn;
 			item.move.from = eps - 1;
 			item.move.index = pieceIndex(eps - 1, your_pawn);
 			item.move.setEnPassant();
 
 			make(item.move);
 			if (!incheck(color)) {
 				unmake(item.move, undoFlags);
 				return true;
 			}
 			unmake(item.move, undoFlags);
 		}
 		// en passant to right
 		if (eps_file != 7 && square[eps + 1] == your_pawn) {
 			item.move.xindex = pieceIndex(eps, opp_pawn);
 			item.move.to = eps + 8 * opp_pawn;
 			item.move.from = eps + 1;
 			item.move.index = pieceIndex(eps + 1, your_pawn);
 			item.move.setEnPassant();
 
 			make(item.move);
 			if (!incheck(color)) {
 				unmake(item.move, undoFlags);
 				return true;
 			}
 			unmake(item.move, undoFlags);
 		}
 		return false;
 	}
 
 	public void getMoveList(final RegMoveList data, final int color, int movetype)
 	{
 		int start = (color == Piece.WHITE)? 31:15, end = (color == Piece.WHITE)? 16:0;
 		MoveFlags undoFlags = new MoveFlags(flags);
 
 		for (int idx = start; idx >= end; idx--) {
 			if (piece[idx].loc == Piece.DEAD)
 				continue;
 
 			final int[] loc;
 			switch (movetype) {
 			case MOVE_ALL:
 			default:
 				loc = genAll(piece[idx].loc);
 				break;
 			case MOVE_CAPTURE:
 				loc = genCapture(piece[idx].loc);
 				break;
 			case MOVE_MOVE:
 				loc = genMove(piece[idx].loc);
 				break;
 			}
 
 			for (int n = 0; loc[n] != -1; n++) {
 				final RegMoveNode item = new RegMoveNode();
 
 				item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
 				item.move.to = loc[n];
 				item.move.from = piece[idx].loc;
 				item.move.index = idx;
 
 				if (Math.abs(piece[idx].type) == Piece.PAWN && isPromote(item.move, color)) {
 					item.move.setPromote(Piece.QUEEN);
 
 					make(item.move);
 					if (incheck(color)) {
 						unmake(item.move, undoFlags);
 						continue;
 					}
 					unmake(item.move, undoFlags);
 
 					data.list[data.size++] = item;
 					for (int i = Piece.ROOK; i > Piece.PAWN; i--) {
 						item.move.setPromote(i);
 
 						make(item.move);
 						item.check = incheck(color ^ -2);
 						item.score = eval();
 						unmake(item.move, undoFlags);
 
 						data.list[data.size++] = new RegMoveNode(item);
 					}
 				} else {
 					make(item.move);
 					if (!incheck(color)) {
 						item.check = incheck(color ^ -2);
 						item.score = eval();
 						data.list[data.size++] = item;
 					}
 					unmake(item.move, undoFlags);
 				}
 			}
 		}
 	}
 
 	public void getCastleMoveList(final RegMoveList data, final int color)
 	{
 		// can't castle while in check
 		if (incheck(color))
 			return;
 
 		final int king = (color == Piece.WHITE)? Piece.E1 : Piece.E8,
 			kindex = (color == Piece.WHITE)? 31 : 15;
 
 		// King Side
 		if (flags.canKingCastle(color) != 0 && square[king + 1] == Piece.EMPTY && square[king + 2] == Piece.EMPTY &&
 		!isAttacked(king + 1, color) && !isAttacked(king + 2, color) &&
 		Math.abs(square[((color == Piece.WHITE)? Piece.H1:Piece.H8)]) == Piece.ROOK) {
 			final RegMoveNode item = new RegMoveNode();
 
 			item.move.xindex = Piece.NONE;
 			item.move.to = king + 2;
 			item.move.from = king;
 			item.move.index = kindex;
 			item.move.setCastle(0x10);
 			item.score = eval();
 			item.check = incheck(color ^ -2);
 
 			data.list[data.size++] = item;
 		}
 		// Queen Side
 		if (flags.canQueenCastle(color) != 0 && square[king - 1] == Piece.EMPTY && square[king - 2] == Piece.EMPTY &&
 		square[king - 3] == Piece.EMPTY && !isAttacked(king - 1, color) && !isAttacked(king - 2, color) &&
 		Math.abs(square[((color == Piece.WHITE)? Piece.A1:Piece.A8)]) == Piece.ROOK) {
 			final RegMoveNode item = new RegMoveNode();
 
 			item.move.xindex = Piece.NONE;
 			item.move.to = king - 2;
 			item.move.from = king;
 			item.move.index = kindex;
 			item.move.setCastle(0x20);
 			item.score = eval();
 			item.check = incheck(color ^ -2);
 
 			data.list[data.size++] = item;
 		}
 	}
 
 	public void getEnPassantMoveList(final RegMoveList data, final int color)
 	{
 		if (flags.canEnPassant() == 0)
 			return;
 
 		final int eps_file = flags.enPassantFile(),
 			eps = eps_file + ((color == Piece.WHITE)? Piece.A5 : Piece.A4),
 			your_pawn = (color == Piece.WHITE)? Piece.WHITE_PAWN :Piece.BLACK_PAWN,
 			opp_pawn = -your_pawn;
 		final MoveFlags undoFlags = new MoveFlags(flags);
 
 		// en passant to left
 		if (eps_file != 0 && square[eps - 1] == your_pawn) {
 			final RegMoveNode item = new RegMoveNode();
 			item.move.xindex = pieceIndex(eps, opp_pawn);
 			item.move.to = eps + 8 * opp_pawn;
 			item.move.from = eps - 1;
 			item.move.index = pieceIndex(eps - 1, your_pawn);
 			item.move.setEnPassant();
 
 			make(item.move);
 			if (!incheck(color)) {
 				item.check = incheck(color ^ -2);
 				item.score = eval();
 				data.list[data.size++] = item;
 			}
 			unmake(item.move, undoFlags);
 		}
 		// en passant to right
 		if (eps_file != 7 && square[eps + 1] == your_pawn) {
 			final RegMoveNode item = new RegMoveNode();
 			item.move.xindex = pieceIndex(eps, opp_pawn);
 			item.move.to = eps + 8 * opp_pawn;
 			item.move.from = eps + 1;
 			item.move.index = pieceIndex(eps + 1, your_pawn);
 			item.move.setEnPassant();
 
 			make(item.move);
 			if (!incheck(color)) {
 				item.check = incheck(color ^ -2);
 				item.score = eval();
 				data.list[data.size++] = item;
 			}
 			unmake(item.move, undoFlags);
 		}
 	}
 
 	public RegMoveList getMoveList(final int color, final int movetype)
 	{
 		final RegMoveList data = new RegMoveList();
 		data.size = 0;
 
 		switch (movetype) {
 		case MOVE_ALL:
 			getMoveList(data, color, movetype);
 			getCastleMoveList(data, color);
 			getEnPassantMoveList(data, color);
 			break;
 		case MOVE_CAPTURE:
 			getMoveList(data, color, movetype);
 			getEnPassantMoveList(data, color);
 			break;
 		case MOVE_MOVE:
 			getMoveList(data, color, movetype);
 			getCastleMoveList(data, color);
 			break;
 		}
 		return data;
 	}
 }
