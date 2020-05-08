 package sf.pnr.base;
 
 import java.util.Arrays;
 
 import static sf.pnr.base.Evaluation.*;
 
 public class Utils {
 	public static final int FULL_INT = 0xFFFFFFFF;
     public static final int MAX_SEARCH_DEPTH = 150;
 
     // pieces
 	public static final int EMPTY = 0;
 	public static final int KING = 1;
 	public static final int QUEEN = 2;
 	public static final int ROOK = 3;
 	public static final int BISHOP = 4;
 	public static final int KNIGHT = 5;
 	public static final int PAWN = 6;
     public static final int[] TYPES = new int[] {KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN};
     public static final int[] TYPES_NOPAWN = new int[] {KING, QUEEN, ROOK, BISHOP, KNIGHT};
 
 	// state
 	public static final int BLACK_TO_MOVE = 0x00000000;
     public static final int BLACK = BLACK_TO_MOVE;
     public static final int WHITE_TO_MOVE = 0x00000001;
 	public static final int WHITE = WHITE_TO_MOVE;
 	public static final int CASTLING_WHITE_KINGSIDE =  0x00000002;
 	public static final int CASTLING_WHITE_QUEENSIDE = 0x00000004;
 	public static final int CASTLING_BLACK_KINGSIDE =  0x00000008;
 	public static final int CASTLING_BLACK_QUEENSIDE = 0x00000010;
 	public static final int CASTLING_WHITE = CASTLING_WHITE_KINGSIDE | CASTLING_WHITE_QUEENSIDE;
 	public static final int CASTLING_BLACK = CASTLING_BLACK_KINGSIDE | CASTLING_BLACK_QUEENSIDE;
 	public static final int CASTLING_ALL = CASTLING_WHITE | CASTLING_BLACK;
 	public static final int EN_PASSANT = 0x000001E0;
 	public static final int HALF_MOVES = 0x000FFE00;
 	public static final int FULL_MOVES = 0xFFF00000;
 	public static final int SHIFT_CASTLING = 1;
 	public static final int SHIFT_EN_PASSANT = 5;
 	public static final int SHIFT_HALF_MOVES = 9;
 	public static final int SHIFT_FULL_MOVES = 20;
 	public static final int CLEAR_CASTLING_WHITE_KINGSIDE = FULL_INT ^ CASTLING_WHITE_KINGSIDE;
 	public static final int CLEAR_CASTLING_WHITE_QUEENSIDE = FULL_INT ^ CASTLING_WHITE_QUEENSIDE;
 	public static final int CLEAR_CASTLING_BLACK_KINGSIDE = FULL_INT ^ CASTLING_BLACK_KINGSIDE;
 	public static final int CLEAR_CASTLING_BLACK_QUEENSIDE = FULL_INT ^ CASTLING_BLACK_QUEENSIDE;
 	public static final int[] CLEAR_CASTLING = new int[128];
 	public static final int CLEAR_EN_PASSANT = FULL_INT ^ EN_PASSANT;
 	public static final int CLEAR_HALF_MOVES = FULL_INT ^ HALF_MOVES;
 	public static final int CLEAR_FULL_MOVES = FULL_INT ^ FULL_MOVES;
 	public static final int UNIT_HALF_MOVES = 0x00000200;
 	public static final int UNIT_FULL_MOVES = 0x00100000;
 
 	public static final int FILE = 0x07;
 	public static final int RANK = 0x70;
 	public static final int SHIFT_RANK = 4;
 
 	// move
 	public static final int FROM       = 0x0000007F;
 	public static final int TO         = 0x00003F80;
 	public static final int MOVE_TYPE  = 0x0001C000;
     public static final int CHECKING   = 0x00020000;
     public static final int CAPTURED   = 0x000E0000;
     public static final int MOVE_VALUE = 0x7FFC0000;
     public static final int FROM_TO  = FROM | TO;
     public static final int BASE_INFO  = FROM_TO | MOVE_TYPE;
     public static final int SHIFT_TO = 7;
 	public static final int SHIFT_MOVE_TYPE = 14;
     public static final int SHIFT_CHECKING = 17;
     public static final int SHIFT_CAPTURED = 17;
 	public static final int SHIFT_MOVE_VALUE = 18;
 	public static final int MT_NORMAL = 0 << SHIFT_MOVE_TYPE;
 	public static final int MT_CASTLING_KINGSIDE = 1 << SHIFT_MOVE_TYPE;
 	public static final int MT_CASTLING_QUEENSIDE = 2 << SHIFT_MOVE_TYPE;
 	public static final int MT_CASTLING = MT_CASTLING_KINGSIDE | MT_CASTLING_QUEENSIDE;
 	public static final int MT_EN_PASSANT = 3 << SHIFT_MOVE_TYPE;
 	public static final int MT_PROMOTION_QUEEN = 4 << SHIFT_MOVE_TYPE;
 	public static final int MT_PROMOTION_ROOK = 5 << SHIFT_MOVE_TYPE;
 	public static final int MT_PROMOTION_BISHOP = 6 << SHIFT_MOVE_TYPE;
 	public static final int MT_PROMOTION_KNIGHT = 7 << SHIFT_MOVE_TYPE;
 	public static final int MT_PROMOTION = 4 << SHIFT_MOVE_TYPE;
     public static final int PLY = 16;
     public static final int SHIFT_PLY = 4;
     public static final int STAGE_MAX = 63;
     public static final int[] PROMOTION_TO_PIECE = new int[] {EMPTY, EMPTY, EMPTY, EMPTY, QUEEN, ROOK, BISHOP, KNIGHT}; 
     public static final int[] CASTLING_TO_ROOK_FROM_DELTA = new int[] {0, 1, -2};
     public static final int[] CASTLING_TO_ROOK_TO_DELTA = new int[] {0, -1, 1}; 
 
 	// deltas
 	public static final int[] DELTA_KING = {-0x0F, -0x11, 0x0F, 0x11, -0x01, -0x10, 0x01, 0x10};
 	public static final int[] DELTA_QUEEN = {-0x0F, -0x11, 0x0F, 0x11, -0x01, -0x10, 0x01, 0x10};
 	public static final int[] DELTA_ROOK = {-0x01, -0x10, 0x01, 0x10};
 	public static final int[] DELTA_BISHOP = {-0x0F, -0x11, 0x0F, 0x11};
 	public static final int[] DELTA_KNIGHT = {-0x21, -0x12, 0x0E, 0x1F, 0x21, 0x12, -0x0E, -0x1F};
 	public static final int[][] DELTA_PAWN_ATTACK = {{-0x11, -0x0F}, {0x11, 0x0F}};
 	public static final int[][] DELTA = new int[7][8];
 
     // attack
     public static final int ATTACK_K = 1;
     public static final int ATTACK_R = 2;
     public static final int ATTACK_B = 4;
     public static final int ATTACK_N = 8;
     public static final int ATTACK_Q = ATTACK_R | ATTACK_B;
     public static final int SHIFT_ATTACK_DELTA = 4;
     public static final int ATTACK_DELTA = 0xFF << SHIFT_ATTACK_DELTA;
     public static final int SHIFT_ATTACK_DISTANCE = 12;
     public static final int ATTACK_DISTANCE = 0x07 << SHIFT_ATTACK_DISTANCE;
     public static final int[] ATTACK_BITS;
 
     public static final int[][] ATTACK_VALUE = new int[7][7];
 
     public static final int[] A = new int[8];
 	public static final int[] B = new int[8];
 	public static final int[] C = new int[8];
 	public static final int[] D = new int[8];
 	public static final int[] E = new int[8];
 	public static final int[] F = new int[8];
 	public static final int[] G = new int[8];
 	public static final int[] H = new int[8];
 	public static final int FILE_A = 0;
 	public static final int FILE_B = 1;
 	public static final int FILE_C = 2;
 	public static final int FILE_D = 3;
 	public static final int FILE_E = 4;
 	public static final int FILE_F = 5;
 	public static final int FILE_G = 6;
 	public static final int FILE_H = 7;
     public static final int DL = -0x11;
     public static final int UL = 0x0F;
     public static final int DR = -0x0F;
     public static final int UR = 0x11;
     public static final int UP = 0x10;
     public static final int DN = -0x10;
 
     public static final int[] INITIAL_BOARD = new int[]
 	   {   ROOK,  KNIGHT,  BISHOP,   QUEEN,    KING,  BISHOP,  KNIGHT,    ROOK,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
 		   PAWN,    PAWN,    PAWN,    PAWN,    PAWN,    PAWN,    PAWN,    PAWN,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
 		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
 		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
 		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
 		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
 		  -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
 		  -ROOK, -KNIGHT, -BISHOP,  -QUEEN,   -KING, -BISHOP, -KNIGHT,   -ROOK,
                EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY};
 
 	public static final int INITIAL_STATE = WHITE_TO_MOVE |
 		CASTLING_WHITE_KINGSIDE | CASTLING_WHITE_QUEENSIDE | CASTLING_BLACK_KINGSIDE | CASTLING_BLACK_QUEENSIDE |
 		(1 << SHIFT_FULL_MOVES);
 	public static final int[] INITIAL_PIECE_ARRAY_POS = new int[]
    	   {1, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0,
    		1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0,
    		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    		1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0,
    		1, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0};
 
     public static final int[] ATTACK_ARRAY = new int[]{
 		0x0400, 0x72F4, 0x7400, 0x7400, 0x7400, 0x7400, 0x7400, 0x7400, 0x7302, 0x7400, 0x7400, 0x7400,
 		0x7400, 0x7400, 0x7400, 0x7314, 0x0400, 0x7400, 0x62F4, 0x6400, 0x6400, 0x6400, 0x6400, 0x6400,
 		0x6302, 0x6400, 0x6400, 0x6400, 0x6400, 0x6400, 0x6314, 0x7400, 0x0400, 0x7400, 0x6400, 0x52F4,
 		0x5400, 0x5400, 0x5400, 0x5400, 0x5302, 0x5400, 0x5400, 0x5400, 0x5400, 0x5314, 0x6400, 0x7400,
 		0x0400, 0x7400, 0x6400, 0x5400, 0x42F4, 0x4400, 0x4400, 0x4400, 0x4302, 0x4400, 0x4400, 0x4400,
 		0x4314, 0x5400, 0x6400, 0x7400, 0x0400, 0x7400, 0x6400, 0x5400, 0x4400, 0x32F4, 0x3400, 0x3400,
 		0x3302, 0x3400, 0x3400, 0x3314, 0x4400, 0x5400, 0x6400, 0x7400, 0x0400, 0x7400, 0x6400, 0x5400,
 		0x4400, 0x3400, 0x22F4, 0x21F8, 0x2302, 0x2218, 0x2314, 0x3400, 0x4400, 0x5400, 0x6400, 0x7400,
 		0x0400, 0x7400, 0x6400, 0x5400, 0x4400, 0x3400, 0x22E8, 0x12F5, 0x1303, 0x1315, 0x2328, 0x3400,
 		0x4400, 0x5400, 0x6400, 0x7400, 0x0400, 0x73F2, 0x63F2, 0x53F2, 0x43F2, 0x33F2, 0x23F2, 0x13F3,
 		0x0400, 0x1413, 0x2412, 0x3412, 0x4412, 0x5412, 0x6412, 0x7412, 0x0400, 0x7400, 0x6400, 0x5400,
 		0x4400, 0x3400, 0x24E8, 0x14F5, 0x1503, 0x1515, 0x2528, 0x3400, 0x4400, 0x5400, 0x6400, 0x7400,
 		0x0400, 0x7400, 0x6400, 0x5400, 0x4400, 0x3400, 0x24F4, 0x25F8, 0x2502, 0x2618, 0x2514, 0x3400,
 		0x4400, 0x5400, 0x6400, 0x7400, 0x0400, 0x7400, 0x6400, 0x5400, 0x4400, 0x34F4, 0x3400, 0x3400,
 		0x3502, 0x3400, 0x3400, 0x3514, 0x4400, 0x5400, 0x6400, 0x7400, 0x0400, 0x7400, 0x6400, 0x5400,
 		0x44F4, 0x4400, 0x4400, 0x4400, 0x4502, 0x4400, 0x4400, 0x4400, 0x4514, 0x5400, 0x6400, 0x7400,
 		0x0400, 0x7400, 0x6400, 0x54F4, 0x5400, 0x5400, 0x5400, 0x5400, 0x5502, 0x5400, 0x5400, 0x5400,
 		0x5400, 0x5514, 0x6400, 0x7400, 0x0400, 0x7400, 0x64F4, 0x6400, 0x6400, 0x6400, 0x6400, 0x6400,
 		0x6502, 0x6400, 0x6400, 0x6400, 0x6400, 0x6400, 0x6514, 0x7400, 0x0400, 0x74F4, 0x7400, 0x7400,
 		0x7400, 0x7400, 0x7400, 0x7400, 0x7502, 0x7400, 0x7400, 0x7400, 0x7400, 0x7400, 0x7400, 0x7514};
     
     public static final long[][][] ZOBRIST_PIECES = new long[7][2][64];
     public static final long[] ZOBRIST_CASTLING = new long[16];
     public static final long[] ZOBRIST_EN_PASSANT = new long[9];
     public static final long ZOBRIST_WHITE_TO_MOVE;
     public static final long[] ZOBRIST_TO_MOVE = new long[2];
 
     public static final boolean[] SLIDING = new boolean[7];
 
     static {
         ATTACK_BITS = new int[6];
         ATTACK_BITS[KING] = ATTACK_K; ATTACK_BITS[QUEEN] = ATTACK_Q; ATTACK_BITS[ROOK] = ATTACK_R;
         ATTACK_BITS[BISHOP] = ATTACK_B; ATTACK_BITS[KNIGHT] = ATTACK_N;
 
         ATTACK_VALUE[PAWN][PAWN] = VAL_PAWN - 1; ATTACK_VALUE[PAWN][KNIGHT] = VAL_KNIGHT - 1;
         ATTACK_VALUE[PAWN][BISHOP] = VAL_BISHOP - 1; ATTACK_VALUE[PAWN][ROOK] = VAL_ROOK - 1;
         ATTACK_VALUE[PAWN][QUEEN] = VAL_QUEEN - 1;
         ATTACK_VALUE[KNIGHT][PAWN] = VAL_PAWN - 3; ATTACK_VALUE[KNIGHT][KNIGHT] = VAL_KNIGHT - 3;
         ATTACK_VALUE[KNIGHT][BISHOP] = VAL_BISHOP - 3; ATTACK_VALUE[KNIGHT][ROOK] = VAL_ROOK - 3;
         ATTACK_VALUE[KNIGHT][QUEEN] = VAL_QUEEN - 3;
         ATTACK_VALUE[BISHOP][PAWN] = VAL_PAWN - 3; ATTACK_VALUE[BISHOP][KNIGHT] = VAL_KNIGHT - 3;
         ATTACK_VALUE[BISHOP][BISHOP] = VAL_BISHOP - 3; ATTACK_VALUE[BISHOP][ROOK] = VAL_ROOK - 3;
         ATTACK_VALUE[BISHOP][QUEEN] = VAL_QUEEN - 3;
         ATTACK_VALUE[ROOK][PAWN] = VAL_PAWN - 5; ATTACK_VALUE[ROOK][KNIGHT] = VAL_KNIGHT - 5;
         ATTACK_VALUE[ROOK][BISHOP] = VAL_BISHOP - 5; ATTACK_VALUE[ROOK][ROOK] = VAL_ROOK - 5;
         ATTACK_VALUE[ROOK][QUEEN] = VAL_QUEEN - 5;
         ATTACK_VALUE[QUEEN][PAWN] = VAL_PAWN - 9; ATTACK_VALUE[QUEEN][KNIGHT] = VAL_KNIGHT - 9;
         ATTACK_VALUE[QUEEN][BISHOP] = VAL_BISHOP - 9; ATTACK_VALUE[QUEEN][ROOK] = VAL_ROOK - 9;
         ATTACK_VALUE[QUEEN][QUEEN] = VAL_QUEEN - 9;
         ATTACK_VALUE[KING][PAWN] = VAL_PAWN / 10; ATTACK_VALUE[KING][KNIGHT] = VAL_KNIGHT / 10;
         ATTACK_VALUE[KING][BISHOP] = VAL_BISHOP / 10; ATTACK_VALUE[KING][ROOK] = VAL_ROOK / 10;
         ATTACK_VALUE[KING][QUEEN] = VAL_QUEEN / 10;
 
         DELTA[KNIGHT] = DELTA_KNIGHT;
         DELTA[BISHOP] = DELTA_BISHOP;
         DELTA[ROOK] = DELTA_ROOK;
         DELTA[QUEEN] = DELTA_QUEEN;
         DELTA[KING] = DELTA_KING;
 
 		for (int i = 0; i < 8; i++) {
 			A[i] = i << 4;
 			B[i] = A[i] + 1;
 			C[i] = A[i] + 2;
 			D[i] = A[i] + 3;
 			E[i] = A[i] + 4;
 			F[i] = A[i] + 5;
 			G[i] = A[i] + 6;
 			H[i] = A[i] + 7;
 		}
 		Arrays.fill(CLEAR_CASTLING, FULL_INT);
 		CLEAR_CASTLING[A[0]] = CLEAR_CASTLING_WHITE_QUEENSIDE;
 		CLEAR_CASTLING[E[0]] = CLEAR_CASTLING_WHITE_QUEENSIDE & CLEAR_CASTLING_WHITE_KINGSIDE;
 		CLEAR_CASTLING[H[0]] = CLEAR_CASTLING_WHITE_KINGSIDE;
 		CLEAR_CASTLING[A[7]] = CLEAR_CASTLING_BLACK_QUEENSIDE;
 		CLEAR_CASTLING[E[7]] = CLEAR_CASTLING_BLACK_QUEENSIDE & CLEAR_CASTLING_BLACK_KINGSIDE;
 		CLEAR_CASTLING[H[7]] = CLEAR_CASTLING_BLACK_KINGSIDE;
 
         System.arraycopy(Polyglot.ZOBRIST, 0 * 64, ZOBRIST_PIECES[PAWN][BLACK_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 1 * 64, ZOBRIST_PIECES[PAWN][WHITE_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 2 * 64, ZOBRIST_PIECES[KNIGHT][BLACK_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 3 * 64, ZOBRIST_PIECES[KNIGHT][WHITE_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 4 * 64, ZOBRIST_PIECES[BISHOP][BLACK_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 5 * 64, ZOBRIST_PIECES[BISHOP][WHITE_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 6 * 64, ZOBRIST_PIECES[ROOK][BLACK_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 7 * 64, ZOBRIST_PIECES[ROOK][WHITE_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 8 * 64, ZOBRIST_PIECES[QUEEN][BLACK_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 9 * 64, ZOBRIST_PIECES[QUEEN][WHITE_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 10 * 64, ZOBRIST_PIECES[KING][BLACK_TO_MOVE], 0, 64);
         System.arraycopy(Polyglot.ZOBRIST, 11 * 64, ZOBRIST_PIECES[KING][WHITE_TO_MOVE], 0, 64);
 
         for (int i = 0; i < 16; i++) {
             long zobrist = 0;
             final int castling = i << SHIFT_CASTLING;
             if ((castling & CASTLING_WHITE_KINGSIDE) > 0) {
                 zobrist ^= Polyglot.ZOBRIST[768];
             }
             if ((castling & CASTLING_WHITE_QUEENSIDE) > 0) {
                 zobrist ^= Polyglot.ZOBRIST[769];
             }
             if ((castling & CASTLING_BLACK_KINGSIDE) > 0) {
                 zobrist ^= Polyglot.ZOBRIST[770];
             }
             if ((castling & CASTLING_BLACK_QUEENSIDE) > 0) {
                 zobrist ^= Polyglot.ZOBRIST[771];
             }
             ZOBRIST_CASTLING[i] = zobrist;
         }
 
         System.arraycopy(Polyglot.ZOBRIST, 772, ZOBRIST_EN_PASSANT, 1, 8);
         ZOBRIST_WHITE_TO_MOVE = Polyglot.ZOBRIST[780];
         ZOBRIST_TO_MOVE[BLACK_TO_MOVE] = 0;
         ZOBRIST_TO_MOVE[WHITE_TO_MOVE] = ZOBRIST_WHITE_TO_MOVE;
 
         Arrays.fill(SLIDING, true);
         SLIDING[PAWN] = false;
         SLIDING[KNIGHT] = false;
         SLIDING[KING] = false;
     }
 	
 	private Utils() {
 		// hide
 	}
 
     public static int getMoveFromIndex(final int move) {
 		return move & FROM;
 	}
 
 	public static int getMoveToIndex(final int move) {
 		return (move & TO) >> SHIFT_TO;
 	}
 	
 	public static int getFile(final int index) {
 		return index & 0x0F;
 	}
 
 	public static int getRank(final int index) {
 		return index >> SHIFT_RANK;
 	}
 
     public static int getIndex(final int file, final int rank) {
         return (rank << 4) + file;
     }
 
     public static boolean isSliding(final int piece) {
 		return piece == QUEEN || piece == ROOK || piece == BISHOP;
 	}
 
     public static long computeZobristIncremental(final Board board) {
         final int state = board.getState();
         long zobrist = ZOBRIST_TO_MOVE[state & WHITE_TO_MOVE];
         zobrist ^= computeZobrist(board, KING);
         zobrist ^= computeZobrist(board, QUEEN);
         zobrist ^= computeZobrist(board, ROOK);
         zobrist ^= computeZobrist(board, BISHOP);
         zobrist ^= computeZobrist(board, KNIGHT);
         zobrist ^= computeZobrist(board, PAWN);
         return zobrist;
     }
 
     public static long computeZobristNonIncremental(final int state) {
         long zobrist = ZOBRIST_CASTLING[(state & CASTLING_ALL) >> SHIFT_CASTLING];
         zobrist ^= ZOBRIST_EN_PASSANT[(state & EN_PASSANT) >> SHIFT_EN_PASSANT];
         return zobrist;
     }
 
     public static long computeZobrist(final Board board, final int piece) {
         return computeZobrist(board, WHITE_TO_MOVE, piece) ^ computeZobrist(board, BLACK_TO_MOVE, piece);
     }
 
     private static long computeZobrist(final Board board, final int toMove, final int piece) {
         final int[] pieces = board.getPieces(toMove, piece);
         long zobrist = 0;
         for (int i = pieces[0]; i > 0; i--) {
             final int pos0x88 = pieces[i];
             final int pos64 = convert0x88To64(pos0x88);
             zobrist ^= ZOBRIST_PIECES[piece][toMove][pos64];
         }
         return zobrist;
     }
 
     public static int convert0x88To64(final int pos0x88) {
         return ((pos0x88 & RANK) >> 1) | (pos0x88 & FILE);
     }
 
     public static int side(final int piece) {
         return 1 - (piece >>> 31);
     }

    public static boolean isCastling(final int move) {
        final int moveType = move & MOVE_TYPE;
        return moveType == MT_CASTLING_KINGSIDE || moveType == MT_CASTLING_QUEENSIDE;
    }
 }
