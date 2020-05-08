 package sf.pnr.base;
 
 import sf.pnr.alg.EvalHashTable;
 import sf.pnr.alg.PawnHashTable;
 
 import static sf.pnr.base.BitBoard.*;
 import static sf.pnr.base.Utils.*;
 
 /**
  */
 public final class Evaluation {
 
     public static final int VAL_DRAW = 0;
     public static final int VAL_MATE = 20000;
     public static final int VAL_MATE_THRESHOLD = VAL_MATE - 200;
     public static final int VAL_MIN = -30000;
 
     public static final int VAL_PAWN = 100;
     public static final int VAL_KNIGHT = 325;
     public static final int VAL_BISHOP = 333;
     public static final int VAL_ROOK = 515;
     public static final int VAL_QUEEN = 935;
     public static final int VAL_KING = VAL_MATE;
 
     public static final int[] VAL_PIECES;
 
     public static final int[][] VAL_PIECE_COUNTS;
     public static final int[][] VAL_PIECE_INCREMENTS;
 
     @Configurable(Configurable.Key.EVAL_POSITION_PAWN_OPENING)
     public static int[] VAL_POSITION_BONUS_PAWN;
     @Configurable(Configurable.Key.EVAL_POSITION_PAWN_ENDGAME)
     public static int[] VAL_POSITION_BONUS_PAWN_ENDGAME;
     @Configurable(Configurable.Key.EVAL_POSITION_KNIGHT_OPENING)
     public static int[] VAL_POSITION_BONUS_KNIGHT;
     @Configurable(Configurable.Key.EVAL_POSITION_KNIGHT_ENDGAME)
     public static int[] VAL_POSITION_BONUS_KNIGHT_ENDGAME;
     @Configurable(Configurable.Key.EVAL_POSITION_BISHOP_OPENING)
     public static int[] VAL_POSITION_BONUS_BISHOP;
     @Configurable(Configurable.Key.EVAL_POSITION_BISHOP_ENDGAME)
     public static int[] VAL_POSITION_BONUS_BISHOP_ENDGAME;
     @Configurable(Configurable.Key.EVAL_POSITION_ROOK_OPENING)
     public static int[] VAL_POSITION_BONUS_ROOK;
     @Configurable(Configurable.Key.EVAL_POSITION_ROOK_ENDGAME)
     public static int[] VAL_POSITION_BONUS_ROOK_ENDGAME;
     @Configurable(Configurable.Key.EVAL_POSITION_QUEEN_OPENING)
     public static int[] VAL_POSITION_BONUS_QUEEN;
     @Configurable(Configurable.Key.EVAL_POSITION_QUEEN_ENDGAME)
     public static int[] VAL_POSITION_BONUS_QUEEN_ENDGAME;
     @Configurable(Configurable.Key.EVAL_POSITION_KING_OPENING)
     public static int[] VAL_POSITION_BONUS_KING;
     @Configurable(Configurable.Key.EVAL_POSITION_KING_ENDGAME)
     public static int[] VAL_POSITION_BONUS_KING_ENDGAME;
 
     public static final int[][] VAL_POSITION_BONUS_OPENING = new int[7][128];
     public static final int[][] VAL_POSITION_BONUS_ENDGAME = new int[7][128];
 
     public static final int[] SHIFT_POSITION_BONUS = new int[]{8, 0};
 
     @Configurable(Configurable.Key.EVAL_PENALTY_DOUBLE_PAWN)
     public static int PENALTY_DOUBLE_PAWN = -30;
     @Configurable(Configurable.Key.EVAL_PENALTY_TRIPLE_PAWN)
     public static int PENALTY_TRIPLE_PAWN = -20;
     @Configurable(Configurable.Key.EVAL_PENALTY_ISOLATED_PAWN)
     public static int PENALTY_ISOLATED_PAWN = -20;
     @Configurable(Configurable.Key.EVAL_PENALTY_WEAK_PAWN)
     public static int PENALTY_WEAK_PAWN = -10;
 
     @Configurable(Configurable.Key.EVAL_BONUS_PAWN_SHIELD)
     public static int BONUS_PAWN_SHIELD = 30;
     @Configurable(Configurable.Key.EVAL_BONUS_PAWN_STORM_MAX)
     public static int BONUS_PAWN_STORM_MAX = 30;
     @Configurable(Configurable.Key.EVAL_PENALTY_PAWN_STORM_MAX_MAIN)
     public static int BONUS_PAWN_STORM_DEDUCTION_MAIN_FILE = 6;
     @Configurable(Configurable.Key.EVAL_PENALTY_PAWN_STORM_MAX_SIDE)
     public static int BONUS_PAWN_STORM_DEDUCTION_SIDE_FILE = 5;
     public static final int BONUS_PASSED_PAWN_PER_SQUARE = 5;
     @Configurable(Configurable.Key.EVAL_BONUS_DEFENSE)
     public static int BONUS_DEFENSE = 2;
     @Configurable(Configurable.Key.EVAL_BONUS_ATTACK)
     public static int BONUS_ATTACK = 1;
     @Configurable(Configurable.Key.EVAL_BONUS_HUNG_PIECE)
     public static int BONUS_HUNG_PIECE = 50;
     @Configurable(Configurable.Key.EVAL_BONUS_MOBILITY)
     public static int BONUS_MOBILITY = 2;
     @Configurable(Configurable.Key.EVAL_BONUS_DISTANCE_KNIGHT)
     public static int[] BONUS_DISTANCE_KNIGHT = new int[] {0, 10, 3, 2, 2, 1, 1, 0};
     @Configurable(Configurable.Key.EVAL_BONUS_DISTANCE_BISHOP)
     public static int[] BONUS_DISTANCE_BISHOP = new int[] {0, 3, 1, 0, 0, 0, 0, 0};
     @Configurable(Configurable.Key.EVAL_BONUS_DISTANCE_ROOK)
     public static int[] BONUS_DISTANCE_ROOK = new int[] {0, 3, 1, 0, 0, 0, 0, 0};
     @Configurable(Configurable.Key.EVAL_BONUS_DISTANCE_QUEEN)
     public static int[] BONUS_DISTANCE_QUEEN = new int[] {0, 2, 1, 0, 0, 0, 0, 0};
     @Configurable(Configurable.Key.EVAL_BONUS_MOBILITY_KNIGHT)
     public static int[] BONUS_MOBILITY_KNIGHT = new int[] {-10, -6, -3, 0, 3, 6, 8, 9, 10};
     @Configurable(Configurable.Key.EVAL_BONUS_MOBILITY_BISHOP)
     public static int[] BONUS_MOBILITY_BISHOP = new int[] {-10, -8, -4, -2, 0, 2, 4, 8, 9, 10, 11, 12, 13, 14};
     @Configurable(Configurable.Key.EVAL_BONUS_MOBILITY_ROOK)
     public static int[] BONUS_MOBILITY_ROOK = new int[] {-10, -9, -7, -3, 0, 3, 7, 9, 10, 11, 12, 13, 14, 15, 16};
     @Configurable(Configurable.Key.EVAL_BONUS_MOBILITY_QUEEN)
     public static int[] BONUS_MOBILITY_QUEEN = new int[] {-15, -11, -8, -5, -2, 0, 2, 5, 7, 8, 9, 10, 11, 12, 13, 14,
                                                           15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15};
     @Configurable(Configurable.Key.EVAL_BONUS_MOBILITY_KING)
     public static int[] BONUS_MOBILITY_KING = new int[] {-10, -6, -2, 0, 2, 4, 5, 5, 5};
     public static final int BONUS_KING_IN_SIGHT_NON_SLIDING = 5;
     public static final int BONUS_KING_IN_SIGHT_SLIDING = 3;
     @Configurable(Configurable.Key.EVAL_BONUS_UNSTOPPABLE_PAWN)
     public static int BONUS_UNSTOPPABLE_PAWN = VAL_QUEEN - VAL_PAWN - 125;
     @Configurable(Configurable.Key.EVAL_PENALTY_CASTLING_MISSED)
     public static int PENALTY_CASTLING_MISSED = -50;
     @Configurable(Configurable.Key.EVAL_PENALTY_CASTLING_PENDING)
     public static int PENALTY_CASTLING_PENDING = -30;
     @Configurable(Configurable.Key.EVAL_PENALTY_CASTLING_PENDING_BOTH)
     public static int PENALTY_CASTLING_PENDING_BOTH = -20;
 
     @Configurable(Configurable.Key.EVAL_BONUS_ROOK_SAMEFILE)
     public static int BONUS_ROOKS_ON_SAME_FILE = 12;
     @Configurable(Configurable.Key.EVAL_BONUS_ROOK_SAMERANK)
     public static int BONUS_ROOKS_ON_SAME_RANK = 12;
     @Configurable(Configurable.Key.EVAL_BONUS_ROOK_SEMIOPENFILE)
     public static int BONUS_ROOKS_ON_SEMI_OPEN_FILE = 10;
     @Configurable(Configurable.Key.EVAL_BONUS_ROOK_OPENFILE)
     public static int BONUS_ROOKS_ON_OPEN_FILE = 10;
     @Configurable(Configurable.Key.EVAL_PENALTY_ATTACKS_AROUND_KING)
     public static int[] PENALTY_ATTACKS_AROUND_KING = new int[]{0, -3, -6, -10, -20, -35, -50, -100, -200};
     public static final double DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE = 0.2;
 
     public static final int INITIAL_MATERIAL_VALUE;
 
     private static final int PENALTY_TRAPPED_KNIGHT_A7 = -50;
     private static final int PENALTY_TRAPPED_KNIGHT_A8 = -50;
     private static final int PENALTY_TRAPPED_BISHOP_A6 = -50;
     private static final int PENALTY_TRAPPED_BISHOP_A7 = -150;
     private static final int PENALTY_TRAPPED_ROOK_BY_KING = -40;
 
     static {
         VAL_PIECES = new int[7];
         VAL_PIECES[PAWN] = VAL_PAWN;
         VAL_PIECES[KNIGHT] = VAL_KNIGHT;
         VAL_PIECES[BISHOP] = VAL_BISHOP;
         VAL_PIECES[ROOK] = VAL_ROOK;
         VAL_PIECES[QUEEN] = VAL_QUEEN;
         VAL_PIECES[KING] = VAL_KING;
 
         VAL_PIECE_COUNTS = new int[7][11];
         VAL_PIECE_COUNTS[PAWN] = new int[] {-35, VAL_PAWN, 2 * VAL_PAWN, 3 * VAL_PAWN, 4 * VAL_PAWN, 5 * VAL_PAWN,
             6 * VAL_PAWN, 7 * VAL_PAWN, 8 * VAL_PAWN};
         VAL_PIECE_COUNTS[KNIGHT] = new int[] {0, VAL_KNIGHT, 2 * VAL_KNIGHT - 30, 3 * VAL_KNIGHT - 60,
             4 * VAL_KNIGHT - 150, 5 * VAL_KNIGHT - 300, 6 * VAL_KNIGHT - 450, 7 * VAL_KNIGHT - 600, 8 * VAL_KNIGHT - 750};
         VAL_PIECE_COUNTS[BISHOP] = new int[] {0, VAL_BISHOP, 2 * VAL_BISHOP + 30, 3 * VAL_BISHOP + 15,
             4 * VAL_BISHOP - 50, 5 * VAL_BISHOP - 150, 6 * VAL_BISHOP - 300, 7 * VAL_BISHOP - 450, 8 * VAL_BISHOP - 600};
         VAL_PIECE_COUNTS[ROOK] = new int[] {0, VAL_ROOK, 2 * VAL_ROOK, 3 * VAL_ROOK - 30, 4 * ROOK - 60,
             5 * VAL_ROOK - 90, 6 * VAL_ROOK - 200, 7 * VAL_ROOK - 350, 8 * VAL_ROOK - 500};
         VAL_PIECE_COUNTS[QUEEN] = new int[] {0, VAL_QUEEN, 2 * VAL_QUEEN, 3 * VAL_QUEEN, 4 * VAL_QUEEN, 5 * VAL_QUEEN,
             6 * VAL_QUEEN, 7 * VAL_QUEEN, 8 * VAL_QUEEN, 9 * VAL_QUEEN, 10 * VAL_QUEEN};
         VAL_PIECE_INCREMENTS = new int[7][11];
         for (int i = 0; i < VAL_PIECE_COUNTS.length; i++) {
             for (int j = 1; j < VAL_PIECE_COUNTS[i].length; j++) {
                 VAL_PIECE_INCREMENTS[i][j] = VAL_PIECE_COUNTS[i][j] - VAL_PIECE_COUNTS[i][j - 1];
             }
         }
         INITIAL_MATERIAL_VALUE = VAL_PIECE_COUNTS[PAWN][8] + VAL_PIECE_COUNTS[KNIGHT][2] + VAL_PIECE_COUNTS[BISHOP][2] +
             VAL_PIECE_COUNTS[ROOK][2] + VAL_PIECE_COUNTS[QUEEN][1];
 
         VAL_POSITION_BONUS_PAWN = new int[]
             {
                 // pnr-pawn-opening3
                   0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0,
                   6,  8,  8,-10,-10,  8,  8,  6,     15, 15, 15, 15, 15, 15, 15, 15,
                   5,  3, -2,  4,  4, -2,  3,  5,      4,  4,  5,  6,  6,  5,  4,  4,
                   0,  0,  0, 12, 12,  0,  0,  0,      1,  1,  2,  8,  8,  2,  1,  1,
                   1,  1,  2,  8,  8,  2,  1,  1,      0,  0,  0, 12, 12,  0,  0,  0,
                   4,  4,  5,  6,  6,  5,  4,  4,      5,  3, -2,  4,  4, -2,  3,  5,
                  15, 15, 15, 15, 15, 15, 15, 15,      6,  8,  8,-10,-10,  8,  8,  6,
                   0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
                 // pnr-pawn-opening3
                 //  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0,
                 //  5, 10, 10,-20,-20, 10, 10,  5,     50, 50, 50, 50, 50, 50, 50, 50,
                 //  5, -5,-10,  0,  0,-10, -5,  5,     10, 10, 20, 30, 30, 20, 10, 10,
                 //  0,  0,  0, 20, 20,  0,  0,  0,      5,  5, 10, 25, 25, 10,  5,  5,
                 //  5,  5, 10, 25, 25, 10,  5,  5,      0,  0,  0, 20, 20,  0,  0,  0,
                 // 10, 10, 20, 30, 30, 20, 10, 10,      5, -5,-10,  0,  0,-10, -5,  5,
                 // 50, 50, 50, 50, 50, 50, 50, 50,      5, 10, 10,-20,-20, 10, 10,  5,
                 //  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
             };
         VAL_POSITION_BONUS_PAWN_ENDGAME = new int[]
             {
                 // pnr-pawn-endgame5
                   0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0,
                  10, 10, 10, 10, 10, 10, 10, 10,     50, 50, 50, 50, 50, 50, 50, 50,
                  18, 18, 16, 14, 14, 16, 18, 18,     40, 40, 40, 40, 40, 40, 40, 40,
                  26, 26, 26, 28, 28, 26, 26, 26,     34, 34, 34, 34, 34, 34, 34, 34,
                  34, 34, 34, 34, 34, 34, 34, 34,     26, 26, 26, 28, 28, 26, 26, 26,
                  40, 40, 40, 40, 40, 40, 40, 40,     18, 18, 16, 14, 14, 16, 18, 18,
                  50, 50, 50, 50, 50, 50, 50, 50,     10, 10, 10, 10, 10, 10, 10, 10,
                   0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
                 // pnr-pawn-endgame3
                 //  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0,
                 //  0,  0,  0,  0,  0,  0,  0,  0,     10, 10, 10, 11, 11, 10, 10, 10,
                 //  2,  2,  2,  3,  3,  2,  2,  2,      8,  8,  9,  9,  9,  9,  8,  8,
                 //  4,  4,  5,  6,  6,  5,  4,  4,      6,  6,  7,  8,  8,  7,  6,  6,
                 //  6,  6,  7,  8,  8,  7,  6,  6,      4,  4,  5,  6,  6,  5,  4,  4,
                 //  8,  8,  9,  9,  9,  9,  8,  8,      2,  2,  2,  3,  3,  2,  2,  2,
                 // 10, 10, 10, 11, 11, 10, 10, 10,      0,  0,  0,  0,  0,  0,  0,  0,
                 //  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
             };
         VAL_POSITION_BONUS_KNIGHT = new int[]
             {
                 // pnr-knight-opening3
                 -35,-20,-15,-10,-10,-15,-20,-35,    -35,-20,-15,-10,-10,-15,-20,-35,
                 -20,-10,  0,  3,  3,  0,-10,-20,    -20, -5,  0,  5,  5,  0, -5,-20,
                 -15,  5, 10, 15, 15, 10,  5,-15,    -15,  5, 10, 15, 15, 10,  5,-15,
                 -10,  3, 15, 20, 20, 15,  3,-10,    -10,  3, 15, 20, 20, 15,  3,-10,
                 -10,  3, 15, 20, 20, 15,  3,-10,    -10,  3, 15, 20, 20, 15,  3,-10,
                 -15,  5, 10, 15, 15, 10,  5,-15,    -15,  5, 10, 15, 15, 10,  5,-15,
                 -20, -5,  0,  5,  5,  0, -5,-20,    -20,-10,  0,  3,  3,  0,-10,-20,
                 -35,-20,-15,-10,-10,-15,-20,-35,    -35,-20,-15,-10,-10,-15,-20,-35
             };
         VAL_POSITION_BONUS_KNIGHT_ENDGAME = new int[]
             {
                 // pnr-knight-endgame3
                 -15, -8, -6, -4, -4, -6, -8,-15,    -10, -6, -4, -2, -2, -4, -6,-10,
                  -8, -5,  0,  2,  2,  0, -5, -8,     -6,  0,  2,  5,  5,  2,  0, -6,
                  -6,  0,  4,  6,  6,  4,  0, -6,     -4,  5,  8,  9,  9,  8,  5, -4,
                  -4,  3,  6,  8,  8,  6,  3, -4,     -2,  3,  7, 10, 10,  7,  3, -2,
                  -2,  3,  7, 10, 10,  7,  3, -2,     -4,  3,  6,  8,  8,  6,  3, -4,
                  -4,  5,  8,  9,  9,  8,  5, -4,     -6,  0,  4,  6,  6,  4,  0, -6,
                  -6,  0,  2,  5,  5,  2,  0, -6,     -8, -5,  0,  2,  2,  0, -5, -8,
                 -10, -6, -4, -2, -2, -4, -6,-10,    -15, -8, -6, -4, -4, -6, -8,-15
             };
         VAL_POSITION_BONUS_BISHOP = new int[]
             {
                 // pnr-bishop-opening3
                 -20,-16,-13,-10,-10,-13,-16,-20,    -10, -8, -6, -4, -4, -6, -8,-10,
                 -16,  5,  0, -2, -2,  0,  5,-16,     -8,  5,  3,  6,  6,  3,  5, -8,
                 -13,  0,  3,  5,  5,  3,  0,-13,     -6,  7,  9, 12, 12,  9,  7, -6,
                 -10,  5,  7, 10, 10,  7,  5,-10,     -8,  3, 10, 10, 10, 10,  3, -8,
                  -8,  3, 10, 10, 10, 10,  3, -8,    -10,  5,  7, 10, 10,  7,  5,-10,
                  -6,  7,  9, 12, 12,  9,  7, -6,    -13,  0,  3,  5,  5,  3,  0,-13,
                  -8,  5,  3,  6,  6,  3,  5, -8,    -16,  5,  0, -2, -2,  0,  5,-16,
                 -10, -8, -6, -4, -4, -6, -8,-10,    -20,-16,-13,-10,-10,-13,-16,-20
             };
         VAL_POSITION_BONUS_BISHOP_ENDGAME = new int[]
             {
                 // pnr-bishop-endgame2
                 -20,-16,-13,-10,-10,-13,-16,-20,    -14,-12,-10,-10,-10,-10,-12,-14,
                 -16,  3,  0,  0,  0,  0,  3,-16,    -12,  5,  0,  3,  3,  0,  5,-12,
                 -13,  0,  5,  7,  7,  5,  0,-13,    -10,  7, 10,  8,  8, 10,  7,-10,
                 -10,  5,  7, 10, 10,  7,  5,-10,     -8,  3, 10, 10, 10, 10,  3, -8,
                  -8,  3, 10, 10, 10, 10,  3, -8,    -10,  5,  7, 10, 10,  7,  5,-10,
                 -10,  7, 10,  8,  8, 10,  7,-10,    -13,  0,  5,  7,  7,  5,  0,-13,
                 -12,  5,  0,  3,  3,  0,  5,-12,    -16,  3,  0,  0,  0,  0,  3,-16,
                 -14,-12,-10,-10,-10,-10,-12,-14,    -20,-16,-13,-10,-10,-13,-16,-20
             };
         VAL_POSITION_BONUS_ROOK = new int[]
             {
                 // pnr-rook-opening1
                   0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  5,  5,  0,  0,  0,
                   5, 10, 10, 10, 10, 10, 10,  5,     -5,  0,  0,  0,  0,  0,  0, -5,
                  -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                  -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                  -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                  -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                  -5,  0,  0,  0,  0,  0,  0, -5,      5, 10, 10, 10, 10, 10, 10,  5,
                   0,  0,  0,  5,  5,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
             };
         VAL_POSITION_BONUS_ROOK_ENDGAME = new int[]
             {
                 // pnr-rook-endgame2
                  -8,  -5, -1,  1,  1, -1, -5, -8,     -2,  -1,  3,  5,  5,  3, -1, -2,
                  -7,  -4,  0,  2,  2,  0, -4, -7,     -4,  -2,  2,  4,  4,  2, -2, -4,
                  -6,  -3,  1,  3,  3,  1, -3, -6,     -6,  -3,  1,  3,  3,  1, -3, -6,
                  -6,  -3,  1,  3,  3,  1, -3, -6,     -6,  -3,  1,  3,  3,  1, -3, -6,
                  -6,  -3,  1,  3,  3,  1, -3, -6,     -6,  -3,  1,  3,  3,  1, -3, -6,
                  -6,  -3,  1,  3,  3,  1, -3, -6,     -6,  -3,  1,  3,  3,  1, -3, -6,
                  -4,  -2,  2,  4,  4,  2, -2, -4,     -7,  -4,  0,  2,  2,  0, -4, -7,
                  -2,  -1,  3,  5,  5,  3, -1, -2,     -8,  -5, -1,  1,  1, -1, -5, -8
             };
         VAL_POSITION_BONUS_QUEEN = new int[]
             {
                 // pnr-queen-opening4
                 -20,-10,-10, -5, -5,-10,-10,-20,    -20,-10,-10, -5, -5,-10,-10,-20,
                 -10,  0,  0,  0,  0,  0,  0,-10,    -10,  0,  0,  0,  0,  0,  0,-10,
                 -10,  0,  5,  5,  5,  5,  0,-10,    -10,  0,  5,  5,  5,  5,  0,-10,
                  -5,  0,  5,  5,  5,  5,  0, -5,      0,  0,  5,  5,  5,  5,  0,  0,
                   0,  0,  5,  5,  5,  5,  0,  0,     -5,  0,  5,  5,  5,  5,  0, -5,
                 -10,  0,  5,  5,  5,  5,  0,-10,    -10,  0,  5,  5,  5,  5,  0,-10,
                 -10,  0,  0,  0,  0,  0,  0,-10,    -10,  0,  0,  0,  0,  0,  0,-10,
                 -20,-10,-10, -5, -5,-10,-10,-20,    -20,-10,-10, -5, -5,-10,-10,-20
             };
         VAL_POSITION_BONUS_QUEEN_ENDGAME = new int[]
             {
                 // pnr-queen-endgame5
                 -20,-10,-10, -5, -5,-10,-10,-20,    -20,-10,-10, -5, -5,-10,-10,-20,
                 -10,  0,  0,  0,  0,  0,  0,-10,    -10,  0,  0,  0,  0,  0,  0,-10,
                 -10,  0,  5,  5,  5,  5,  0,-10,    -10,  0,  5,  5,  5,  5,  0,-10,
                  -5,  0,  5,  5,  5,  5,  0, -5,      0,  0,  5,  5,  5,  5,  0,  0,
                   0,  0,  5,  5,  5,  5,  0,  0,     -5,  0,  5,  5,  5,  5,  0, -5,
                 -10,  0,  5,  5,  5,  5,  0,-10,    -10,  0,  5,  5,  5,  5,  0,-10,
                 -10,  0,  0,  0,  0,  0,  0,-10,    -10,  0,  0,  0,  0,  0,  0,-10,
                 -20,-10,-10, -5, -5,-10,-10,-20,    -20,-10,-10, -5, -5,-10,-10,-20
             };
         VAL_POSITION_BONUS_KING = new int[]
             {
                 // pnr-king-opening1
                  20, 30, 10,  0,  0, 10, 30, 20,    -30,-40,-40,-50,-50,-40,-40,-30,
                  20, 20,  0,  0,  0,  0, 20, 20,    -30,-40,-40,-50,-50,-40,-40,-30,
                 -10,-20,-20,-20,-20,-20,-20,-10,    -30,-40,-40,-50,-50,-40,-40,-30,
                 -20,-30,-30,-40,-40,-30,-30,-20,    -30,-40,-40,-50,-50,-40,-40,-30,
                 -30,-40,-40,-50,-50,-40,-40,-30,    -20,-30,-30,-40,-40,-30,-30,-20,
                 -30,-40,-40,-50,-50,-40,-40,-30,    -10,-20,-20,-20,-20,-20,-20,-10,
                 -30,-40,-40,-50,-50,-40,-40,-30,     20, 20,  0,  0,  0,  0, 20, 20,
                 -30,-40,-40,-50,-50,-40,-40,-30,     20, 30, 10,  0,  0, 10, 30, 20
             };
         VAL_POSITION_BONUS_KING_ENDGAME = new int[]
             {
                 // pnr-king-endgame2
                 -20,-15,-12, -7, -7,-12,-15,-20,   -20,-15,-13,-12,-12,-13,-15,-20,
                 -14, -8, -3,  0,  0, -3, -8,-14,   -14, -8,  0,  0,  0,  0, -8,-14,
                 -12, -5,  4,  8,  8,  4, -5,-12,   -12, -5,  4,  8,  8,  4, -5,-12,
                 -10, -3,  8, 12, 12,  8, -3,-10,   -10, -3,  8, 12, 12,  8, -3,-10,
                 -10, -3,  8, 12, 12,  8, -3,-10,   -10, -3,  8, 12, 12,  8, -3,-10,
                 -12, -5,  4,  8,  8,  4, -5,-12,   -12, -5,  4,  8,  8,  4, -5,-12,
                 -14, -8,  0,  0,  0,  0, -8,-14,   -14, -8, -3,  0,  0, -3, -8,-14,
                 -20,-15,-13,-12,-12,-13,-15,-20,   -20,-15,-12, -7, -7,-12,-15,-20
             };
 
         VAL_POSITION_BONUS_OPENING[PAWN] = VAL_POSITION_BONUS_PAWN;
         VAL_POSITION_BONUS_OPENING[KNIGHT] = VAL_POSITION_BONUS_KNIGHT;
         VAL_POSITION_BONUS_OPENING[BISHOP] = VAL_POSITION_BONUS_BISHOP;
         VAL_POSITION_BONUS_OPENING[ROOK] = VAL_POSITION_BONUS_ROOK;
         VAL_POSITION_BONUS_OPENING[QUEEN] = VAL_POSITION_BONUS_QUEEN;
         VAL_POSITION_BONUS_OPENING[KING] = VAL_POSITION_BONUS_KING;
 
         VAL_POSITION_BONUS_ENDGAME[PAWN] = VAL_POSITION_BONUS_PAWN_ENDGAME;
         VAL_POSITION_BONUS_ENDGAME[KNIGHT] = VAL_POSITION_BONUS_KNIGHT_ENDGAME;
         VAL_POSITION_BONUS_ENDGAME[BISHOP] = VAL_POSITION_BONUS_BISHOP_ENDGAME;
         VAL_POSITION_BONUS_ENDGAME[ROOK] = VAL_POSITION_BONUS_ROOK_ENDGAME;
         VAL_POSITION_BONUS_ENDGAME[QUEEN] = VAL_POSITION_BONUS_QUEEN_ENDGAME;
         VAL_POSITION_BONUS_ENDGAME[KING] = VAL_POSITION_BONUS_KING_ENDGAME;
     }
 
     private final PawnHashTable pawnHashTable = new PawnHashTable();
     private final EvalHashTable evalHashTable = new EvalHashTable();
 
     public int evaluate(final Board board) {
         final int state = board.getState();
         final int halfMoves = (state & HALF_MOVES) >> SHIFT_HALF_MOVES;
         if (halfMoves >= 100) {
             return VAL_DRAW;
         }
         final int toMove = state & WHITE_TO_MOVE;
         final long zobrist = board.getZobristKey() ^ ZOBRIST_TO_MOVE[toMove];
         final int value = evalHashTable.read(zobrist);
         final int signum = (toMove << 1) - 1;
         if (value != 0) {
             return (value + VAL_MIN) * signum;
         }
         if (drawByInsufficientMaterial(board)) {
             return VAL_DRAW;
         }
 
         final int state2 = board.getState2();
         final int stage = board.getStage();
         final long whites64 = board.getBitboard(WHITE);
         final long blacks64 = board.getBitboard(BLACK);
         final long allPieces64 = whites64 | blacks64;
         final int shiftPositionBonusWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftPositionBonusBlack = SHIFT_POSITION_BONUS[BLACK];
         int scorePositionalOpening = 0;
         int scorePositionalEndgame = 0;
         long whitePawns64 = 0L;
         long blackPawns64 = 0L;
         long whitePawnAttacks64 = 0L;
         long blackPawnAttacks64 = 0L;
         int attackCount = 0;
         int defenseCount = 0;
         int mobilityCount = 0;
 
         final int[] whitePawns = board.getPieces(WHITE, PAWN);
         final long[] whitePawnAttacks = PAWN_ATTACK[WHITE];
         final long[] whitePawnMoves = PAWN_MOVES[WHITE];
         for (int i = whitePawns[0]; i > 0; i--) {
             final int pawn = whitePawns[i];
             final int pawn64 = convert0x88To64(pawn);
             whitePawns64 |= 1L << pawn64;
 
             final long pawnAttack64 = whitePawnAttacks[pawn64];
             final long attacked64 = pawnAttack64 & blacks64;
             whitePawnAttacks64 |= pawnAttack64;
             attackCount += Long.bitCount(attacked64);
             final long defended64 = pawnAttack64 & whites64;
             defenseCount += Long.bitCount(defended64);
             final long pawnMoves64 = whitePawnMoves[pawn64];
             final long doubleBlocker64 = pawnMoves64 & (pawnMoves64 >> 8) & allPieces64;
             final long mobility64 = pawnMoves64 & (~allPieces64);
             mobilityCount += Long.bitCount(mobility64 & ~(doubleBlocker64 << 8));
         }
 
         final int[] blackPawns = board.getPieces(BLACK, PAWN);
         final long[] blackPawnAttacks = PAWN_ATTACK[BLACK];
         final long[] blackPawnMoves = PAWN_MOVES[BLACK];
         for (int i = blackPawns[0]; i > 0; i--) {
             final int pawn = blackPawns[i];
             final int pawn64 = convert0x88To64(pawn);
             blackPawns64 |= 1L << pawn64;
 
             final long pawnAttack64 = blackPawnAttacks[pawn64];
             final long attacked64 = pawnAttack64 & whites64;
             blackPawnAttacks64 |= pawnAttack64;
             attackCount -= Long.bitCount(attacked64);
             final long defended64 = pawnAttack64 & blacks64;
             defenseCount -= Long.bitCount(defended64);
             final long pawnMoves64 = blackPawnMoves[pawn64];
             final long doubleBlocker64 = pawnMoves64 & (pawnMoves64 << 8) & allPieces64;
             final long mobility64 = pawnMoves64 & (~allPieces64);
             mobilityCount -= Long.bitCount(mobility64 & ~(doubleBlocker64 >> 8));
         }
 
         // en passant
         final int enPassant = (state & EN_PASSANT) >> SHIFT_EN_PASSANT;
         if (enPassant != 0) {
             final long enPassantBitBoard = BitBoard.EN_PASSANT_SQUARES[toMove][enPassant - 1];
             if (toMove == WHITE_TO_MOVE) {
                 attackCount += Long.bitCount(whitePawns64 & enPassantBitBoard);
             } else {
                 attackCount -= Long.bitCount(blackPawns64 & enPassantBitBoard);
             }
         }
         int scoreAttack = attackCount * BONUS_ATTACK;
         int scoreDefense = defenseCount * BONUS_DEFENSE;
         int scoreMobility = mobilityCount * BONUS_MOBILITY;
 
         final int hungPieceCountBlack = Long.bitCount(whitePawnAttacks64 & blacks64 & ~blackPawns64);
         final int hungPieceCountWhite = Long.bitCount(blackPawnAttacks64 & whites64 & ~whitePawns64);
         int scoreHungPiece = 0;
         if (toMove == WHITE_TO_MOVE) {
             scoreHungPiece += (hungPieceCountBlack * BONUS_HUNG_PIECE);
             if (hungPieceCountWhite > 1) {
                 scoreHungPiece -= BONUS_HUNG_PIECE >> 2;
             }
         } else {
             scoreHungPiece -= (hungPieceCountWhite * BONUS_HUNG_PIECE);
             if (hungPieceCountBlack > 1) {
                 scoreHungPiece += BONUS_HUNG_PIECE >> 2;
             }
         }
 
         // knights
         int scoreDistance = 0;
         int materialValueNoPawnWhite = 0;
         int materialValueNoPawnBlack = 0;
         final long piecesMaskWhite = board.getBitboard(WHITE);
         final long piecesMaskBlack = board.getBitboard(BLACK);
         final int blackKing = board.getKing(BLACK);
         final int[] whiteKnights = board.getPieces(WHITE, KNIGHT);
         int[] positionalBonusOpening = VAL_POSITION_BONUS_OPENING[KNIGHT];
         int[] positionalBonusEndGame = VAL_POSITION_BONUS_ENDGAME[KNIGHT];
         materialValueNoPawnWhite += VAL_PIECE_COUNTS[KNIGHT][whiteKnights[0]];
         long whiteAttacks64 = whitePawnAttacks64;
         for (int i = whiteKnights[0]; i > 0; i--) {
             final int knight = whiteKnights[i];
             final int knight64 = convert0x88To64(knight);
             final long knightMask = KNIGHT_MOVES[knight64];
             whiteAttacks64 |= knightMask;
             final long defended64 = knightMask & piecesMaskWhite;
             scoreDefense += Long.bitCount(defended64) * BONUS_DEFENSE;
             final long attacked64 = knightMask & piecesMaskBlack;
             scoreAttack += Long.bitCount(attacked64) * BONUS_ATTACK;
             scoreMobility += BONUS_MOBILITY_KNIGHT[Long.bitCount(knightMask ^ defended64 ^ attacked64)];
             scoreDistance += BONUS_DISTANCE_KNIGHT[
                 distance(knight, blackKing, ATTACK_DISTANCE_KNIGHT, SHIFT_ATTACK_DISTANCE_KNIGHT)];
             scorePositionalOpening += positionalBonusOpening[knight + shiftPositionBonusWhite];
             scorePositionalEndgame += positionalBonusEndGame[knight + shiftPositionBonusWhite];
         }
         final int whiteKing = board.getKing(WHITE);
         final int[] blackKnights = board.getPieces(BLACK, KNIGHT);
         materialValueNoPawnBlack += VAL_PIECE_COUNTS[KNIGHT][blackKnights[0]];
         long blackAttacks64 = blackPawnAttacks64;
         for (int i = blackKnights[0]; i > 0; i--) {
             final int knight = blackKnights[i];
             final int knight64 = convert0x88To64(knight);
             final long knightMask = KNIGHT_MOVES[knight64];
             blackAttacks64 |= knightMask;
             final long defended64 = knightMask & piecesMaskBlack;
             scoreDefense -= Long.bitCount(defended64) * BONUS_DEFENSE;
             final long attacked64 = knightMask & piecesMaskWhite;
             scoreAttack -= Long.bitCount(attacked64) * BONUS_ATTACK;
             scoreMobility -= BONUS_MOBILITY_KNIGHT[Long.bitCount(knightMask ^ defended64 ^ attacked64)];
             scoreDistance -= BONUS_DISTANCE_KNIGHT[
                 distance(knight, whiteKing, ATTACK_DISTANCE_KNIGHT, SHIFT_ATTACK_DISTANCE_KNIGHT)];
             scorePositionalOpening -= positionalBonusOpening[knight + shiftPositionBonusBlack];
             scorePositionalEndgame -= positionalBonusEndGame[knight + shiftPositionBonusBlack];
         }
 
         // bishops
         final int[] squares = board.getBoard();
         final int[] whiteBishops = board.getPieces(WHITE, BISHOP);
         positionalBonusOpening = VAL_POSITION_BONUS_OPENING[BISHOP];
         positionalBonusEndGame = VAL_POSITION_BONUS_ENDGAME[BISHOP];
         materialValueNoPawnWhite += VAL_PIECE_COUNTS[BISHOP][whiteBishops[0]];
         for (int i = whiteBishops[0]; i > 0; i--) {
             final int bishop = whiteBishops[i];
             int mobility = 0;
             for (int delta: DELTA[BISHOP]) {
                 for (int pos = bishop + delta; (pos & 0x88) == 0; pos += delta) {
                     whiteAttacks64 |= 1L << convert0x88To64(pos);
                     if (squares[pos] == EMPTY) {
                         mobility++;
                     } else if (WHITE == side(squares[pos])) {
                         scoreDefense += BONUS_DEFENSE;
                         break;
                     } else {
                         scoreAttack += BONUS_ATTACK;
                         break;
                     }
                 }
             }
             scoreMobility += BONUS_MOBILITY_BISHOP[mobility];
             scoreDistance += BONUS_DISTANCE_BISHOP[distance(bishop, blackKing, ATTACK_DISTANCE_MASKS[BISHOP],
                 SHIFT_ATTACK_DISTANCES[BISHOP])];
             scorePositionalOpening += positionalBonusOpening[bishop + shiftPositionBonusWhite];
             scorePositionalEndgame += positionalBonusEndGame[bishop + shiftPositionBonusWhite];
         }
         final int[] blackBishops = board.getPieces(BLACK, BISHOP);
         materialValueNoPawnBlack += VAL_PIECE_COUNTS[BISHOP][blackBishops[0]];
         for (int i = blackBishops[0]; i > 0; i--) {
             final int bishop = blackBishops[i];
             int mobility = 0;
             for (int delta: DELTA[BISHOP]) {
                 for (int pos = bishop + delta; (pos & 0x88) == 0; pos += delta) {
                     blackAttacks64 |= 1L << convert0x88To64(pos);
                     if (squares[pos] == EMPTY) {
                         mobility++;
                     } else if (BLACK == side(squares[pos])) {
                         scoreDefense -= BONUS_DEFENSE;
                         break;
                     } else {
                         scoreAttack -= BONUS_ATTACK;
                         break;
                     }
                 }
             }
             scoreMobility -= BONUS_MOBILITY_BISHOP[mobility];
             scoreDistance -= BONUS_DISTANCE_BISHOP[distance(bishop, whiteKing, ATTACK_DISTANCE_MASKS[BISHOP],
                 SHIFT_ATTACK_DISTANCES[BISHOP])];
             scorePositionalOpening -= positionalBonusOpening[bishop + shiftPositionBonusBlack];
             scorePositionalEndgame -= positionalBonusEndGame[bishop + shiftPositionBonusBlack];
         }
 
         // rooks
         final int[] whiteRooks = board.getPieces(WHITE, ROOK);
         positionalBonusOpening = VAL_POSITION_BONUS_OPENING[ROOK];
         positionalBonusEndGame = VAL_POSITION_BONUS_ENDGAME[ROOK];
         final int whiteRookCount = whiteRooks[0];
         materialValueNoPawnWhite += VAL_PIECE_COUNTS[ROOK][whiteRookCount];
         int whiteRookFiles = 0;
         int whiteRookRanks = 0;
         int scoreRooksOnOpenFiles = 0;
         for (int i = whiteRookCount; i > 0; i--) {
             final int rook = whiteRooks[i];
             int mobility = 0;
             for (int delta: DELTA[ROOK]) {
                 for (int pos = rook + delta; (pos & 0x88) == 0; pos += delta) {
                     whiteAttacks64 |= 1L << convert0x88To64(pos);
                     if (squares[pos] == EMPTY) {
                         mobility++;
                     } else if (WHITE == side(squares[pos])) {
                         scoreDefense += BONUS_DEFENSE;
                         break;
                     } else {
                         scoreAttack += BONUS_ATTACK;
                         break;
                     }
                 }
             }
             final int file = getFile(rook);
             final int rank = getRank(rook);
             whiteRookFiles |= FILE_RANK_BITS[file];
             whiteRookRanks |= FILE_RANK_BITS[rank];
             scoreMobility += BONUS_MOBILITY_ROOK[mobility];
             scoreDistance += BONUS_DISTANCE_ROOK[distance(rook, blackKing, ATTACK_DISTANCE_MASKS[ROOK],
                 SHIFT_ATTACK_DISTANCES[ROOK])];
             scorePositionalOpening += positionalBonusOpening[rook + shiftPositionBonusWhite];
             scorePositionalEndgame += positionalBonusEndGame[rook + shiftPositionBonusWhite];
             final long file64 = BitBoard.BITBOARD_FILE[file];
             final long ranksAbove64 = BitBoard.BITBOARD_RANKS_ABOVE[rank];
             scoreRooksOnOpenFiles += (ranksAbove64 & (whitePawns64 | blackPawns64) & file64) == 0? BONUS_ROOKS_ON_SEMI_OPEN_FILE: 0;
             scoreRooksOnOpenFiles += (ranksAbove64 & allPieces64 & file64) == 0? BONUS_ROOKS_ON_OPEN_FILE: 0;
         }
         final int[] blackRooks = board.getPieces(BLACK, ROOK);
         final int blackRookCount = blackRooks[0];
         materialValueNoPawnBlack += VAL_PIECE_COUNTS[ROOK][blackRookCount];
         int blackRookFiles = 0;
         int blackRookRanks = 0;
         for (int i = blackRookCount; i > 0; i--) {
             final int rook = blackRooks[i];
             int mobility = 0;
             for (int delta: DELTA[ROOK]) {
                 for (int pos = rook + delta; (pos & 0x88) == 0; pos += delta) {
                     blackAttacks64 |= 1L << convert0x88To64(pos);
                     if (squares[pos] == EMPTY) {
                         mobility++;
                     } else if (BLACK == side(squares[pos])) {
                         scoreDefense -= BONUS_DEFENSE;
                         break;
                     } else {
                         scoreAttack -= BONUS_ATTACK;
                         break;
                     }
                 }
             }
             final int file = getFile(rook);
             final int rank = getRank(rook);
             blackRookFiles |= FILE_RANK_BITS[file];
             blackRookRanks |= FILE_RANK_BITS[rank];
             scoreMobility -= BONUS_MOBILITY_ROOK[mobility];
             scoreDistance -= BONUS_DISTANCE_ROOK[distance(rook, whiteKing, ATTACK_DISTANCE_MASKS[ROOK],
                 SHIFT_ATTACK_DISTANCES[ROOK])];
             scorePositionalOpening -= positionalBonusOpening[rook + shiftPositionBonusBlack];
             scorePositionalEndgame -= positionalBonusEndGame[rook + shiftPositionBonusBlack];
             final long file64 = BitBoard.BITBOARD_FILE[file];
             final long ranksBelow64 = BitBoard.BITBOARD_RANKS_BELOW[rank];
             scoreRooksOnOpenFiles -= (ranksBelow64 & (whitePawns64 | blackPawns64) & file64) == 0? BONUS_ROOKS_ON_SEMI_OPEN_FILE: 0;
             scoreRooksOnOpenFiles -= (ranksBelow64 & allPieces64 & file64) == 0? BONUS_ROOKS_ON_OPEN_FILE: 0;
         }
 
         // queens
         final int[] whiteQueens = board.getPieces(WHITE, QUEEN);
         positionalBonusOpening = VAL_POSITION_BONUS_OPENING[QUEEN];
         positionalBonusEndGame = VAL_POSITION_BONUS_ENDGAME[QUEEN];
         materialValueNoPawnWhite += VAL_PIECE_COUNTS[QUEEN][whiteQueens[0]];
         for (int i = whiteQueens[0]; i > 0; i--) {
             final int queen = whiteQueens[i];
             int mobility = 0;
             for (int delta: DELTA[QUEEN]) {
                 for (int pos = queen + delta; (pos & 0x88) == 0; pos += delta) {
                     whiteAttacks64 |= 1L << convert0x88To64(pos);
                     if (squares[pos] == EMPTY) {
                         mobility++;
                     } else if (WHITE == side(squares[pos])) {
                         scoreDefense += BONUS_DEFENSE;
                         break;
                     } else {
                         scoreAttack += BONUS_ATTACK;
                         break;
                     }
                 }
             }
             scoreMobility += BONUS_MOBILITY_QUEEN[mobility];
             scoreDistance += BONUS_DISTANCE_QUEEN[distance(queen, blackKing, ATTACK_DISTANCE_MASKS[QUEEN],
                 SHIFT_ATTACK_DISTANCES[QUEEN])];
             scorePositionalOpening += positionalBonusOpening[queen + shiftPositionBonusWhite];
             scorePositionalEndgame += positionalBonusEndGame[queen + shiftPositionBonusWhite];
         }
         final int[] blackQueens = board.getPieces(BLACK, QUEEN);
         materialValueNoPawnBlack += VAL_PIECE_COUNTS[QUEEN][blackQueens[0]];
         for (int i = blackQueens[0]; i > 0; i--) {
             final int queen = blackQueens[i];
             int mobility = 0;
             for (int delta: DELTA[QUEEN]) {
                 for (int pos = queen + delta; (pos & 0x88) == 0; pos += delta) {
                     blackAttacks64 |= 1L << convert0x88To64(pos);
                     if (squares[pos] == EMPTY) {
                         mobility++;
                     } else if (BLACK == side(squares[pos])) {
                         scoreDefense -= BONUS_DEFENSE;
                         break;
                     } else {
                         scoreAttack -= BONUS_ATTACK;
                         break;
                     }
                 }
             }
             scoreMobility -= BONUS_MOBILITY_QUEEN[mobility];
             scoreDistance -= BONUS_DISTANCE_QUEEN[distance(queen, whiteKing, ATTACK_DISTANCE_MASKS[QUEEN],
                 SHIFT_ATTACK_DISTANCES[QUEEN])];
             scorePositionalOpening -= positionalBonusOpening[queen + shiftPositionBonusBlack];
             scorePositionalEndgame -= positionalBonusEndGame[queen + shiftPositionBonusBlack];
         }
 
         // kings
         final int whiteKing64 = convert0x88To64(whiteKing);
         final long whiteKingAttacks64 = KING_MOVES[whiteKing64];
         scoreDefense += Long.bitCount(whiteKingAttacks64 & whites64) * BONUS_DEFENSE;
         scoreAttack += Long.bitCount(whiteKingAttacks64 & blacks64) * BONUS_ATTACK;
         scoreMobility += BONUS_MOBILITY_KING[Long.bitCount(whiteKingAttacks64 & ~whites64 & ~blacks64)];
        whiteAttacks64 |= whiteKingAttacks64;
         final int blackKing64 = convert0x88To64(blackKing);
         final long blackKingAttacks64 = KING_MOVES[blackKing64];
         scoreDefense -= Long.bitCount(blackKingAttacks64 & blacks64) * BONUS_DEFENSE;
         scoreAttack -= Long.bitCount(blackKingAttacks64 & whites64) * BONUS_ATTACK;
         scoreMobility -= BONUS_MOBILITY_KING[Long.bitCount(blackKingAttacks64 & ~whites64 & ~blacks64)];
        blackAttacks64 |= blackKingAttacks64;
 
         final int scoreAttacksAroundKingPenalty = PENALTY_ATTACKS_AROUND_KING[Long.bitCount(whiteKingAttacks64 & blackAttacks64 & ~whites64)] -
             PENALTY_ATTACKS_AROUND_KING[Long.bitCount(blackKingAttacks64 & whiteAttacks64 & ~blacks64)];
 
         final int scoreCastlingPenalty = getCastlingPenaltyAsWhite(state, state2);
         final int scoreRookBonus = BONUS_ROOKS_ON_SAME_FILE * (whiteRookCount - Integer.bitCount(whiteRookFiles)) +
             BONUS_ROOKS_ON_SAME_RANK * (whiteRookCount - Integer.bitCount(whiteRookRanks)) -
             BONUS_ROOKS_ON_SAME_FILE * (blackRookCount - Integer.bitCount(blackRookFiles)) -
             BONUS_ROOKS_ON_SAME_RANK * (blackRookCount - Integer.bitCount(blackRookRanks));
 
         final int scoreMaterialValue = materialValueNoPawnWhite - materialValueNoPawnBlack;
         final int scoreTrappedPieces = getTrappedPiecesPenaltyAsWhite(board);
         final int pawnHashValue = pawnEval(board);
         int scorePawn = PawnHashTable.getValueFromPawnHashValue(pawnHashValue);
         if (materialValueNoPawnWhite == 0 && materialValueNoPawnBlack == 0) {
             final int unstoppablePawnDistWhite = PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, toMove);
             final int unstoppablePawnDistBlack = PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, toMove);
             if (unstoppablePawnDistWhite < unstoppablePawnDistBlack) {
                 scorePawn += BONUS_UNSTOPPABLE_PAWN;
             } else if (unstoppablePawnDistWhite > unstoppablePawnDistBlack) {
                 scorePawn -= BONUS_UNSTOPPABLE_PAWN;
             } else if (unstoppablePawnDistWhite == unstoppablePawnDistBlack && unstoppablePawnDistWhite != 7) {
                 scorePawn += signum * BONUS_UNSTOPPABLE_PAWN;
             }
         }
 
         int score = scoreAttack + scoreDefense + scoreMobility + scoreHungPiece + scoreMaterialValue +
             scorePawn + scoreRookBonus + scoreTrappedPieces + scoreAttacksAroundKingPenalty  +
             scoreRooksOnOpenFiles;
         score += ((scorePositionalOpening + scoreCastlingPenalty) * (STAGE_MAX - stage) +
             (scorePositionalEndgame + scoreDistance) * stage) / STAGE_MAX;
 
         //System.out.printf("Attack:              %4d\r\n", scoreAttack);
         //System.out.printf("Defense:             %4d\r\n", scoreDefense);
         //System.out.printf("Mobility:            %4d\r\n", scoreMobility);
         //System.out.printf("Hung Piece:          %4d\r\n", scoreHungPiece);
         //System.out.printf("Material Value:      %4d\r\n", scoreMaterialValue);
         //System.out.printf("Pawn:                %4d\r\n", scorePawn);
         //System.out.printf("Rook Bonus:          %4d\r\n", scoreRookBonus);
         //System.out.printf("Trapped Pieces:      %4d\r\n", scoreTrappedPieces);
         //System.out.printf("Attacks Around King: %4d (%d / %d)\r\n", scoreAttacksAroundKingPenalty,
         //    Long.bitCount(whiteKingAttacks64 & blackAttacks64 & ~whites64),
         //    Long.bitCount(blackKingAttacks64 & whiteAttacks64 & ~blacks64));
         //System.out.printf("Positional Opening:  %4d\r\n", scorePositionalOpening);
         //System.out.printf("Positional Endgame:  %4d\r\n", scorePositionalEndgame);
         //System.out.printf("Castling Penalty:    %4d\r\n", scoreCastlingPenalty);
         //System.out.printf("Distance:            %4d\r\n", scoreDistance);
         //System.out.printf("Rooks on Open Files: %4d\r\n", scoreRooksOnOpenFiles);
         //System.out.printf("Before Draw Prob.:   %4d\r\n", score);
 
         final int materialValueNoPawnToMove = toMove == BLACK? materialValueNoPawnBlack: materialValueNoPawnWhite;
         if (score * signum > 0 && materialValueNoPawnToMove <= VAL_PIECE_COUNTS[BISHOP][2]) {
             score *= (1.0 - drawProbability(board));
             //System.out.printf("After Draw Prob.   : %4d\r\n", score);
         }
 
         evalHashTable.set(zobrist, score - VAL_MIN);
 
         return score * signum;
     }
 
     public static int computeMaterialValueOneSide(final Board board, final int side) {
         int score = VAL_PIECE_COUNTS[PAWN][board.getPieces(side, PAWN)[0]];
         score += VAL_PIECE_COUNTS[KNIGHT][board.getPieces(side, KNIGHT)[0]];
         score += VAL_PIECE_COUNTS[BISHOP][board.getPieces(side, BISHOP)[0]];
         score += VAL_PIECE_COUNTS[ROOK][board.getPieces(side, ROOK)[0]];
         score += VAL_PIECE_COUNTS[QUEEN][board.getPieces(side, QUEEN)[0]];
         return score;
     }
 
     public static int computePositionalGain(final int absPiece, final int fromPos, final int toPos,
                                             final int stage, final int shift) {
         final int[] typeBonusOpening = VAL_POSITION_BONUS_OPENING[absPiece];
         final int[] typeBonusEndGame = VAL_POSITION_BONUS_ENDGAME[absPiece];
         return ((typeBonusOpening[toPos + shift] - typeBonusOpening[fromPos + shift]) * (STAGE_MAX - stage) +
             (typeBonusEndGame[toPos + shift] - typeBonusEndGame[fromPos + shift]) * stage) / STAGE_MAX;
     }
 
     public static int distance(final int from0x88, final int to0x88, final int distanceMask, final int distanceShift) {
         return (ATTACK_ARRAY[from0x88 - to0x88 + 120] & distanceMask) >> distanceShift;
     }
 
     public static int getCastlingPenaltyAsWhite(final int state, final int state2) {
         int castlingPenalty = 0;
         final int castlingWhite = state & CASTLING_WHITE;
         if (castlingWhite == CASTLING_WHITE) {
             castlingPenalty += PENALTY_CASTLING_PENDING_BOTH;
         } else if (castlingWhite != 0) {
             castlingPenalty += PENALTY_CASTLING_PENDING;
         } else if ((state2 & CASTLED_WHITE) == 0) {
             castlingPenalty += PENALTY_CASTLING_MISSED;
         }
         final int castlingBlack = state & CASTLING_BLACK;
         if (castlingBlack == CASTLING_BLACK) {
             castlingPenalty -= PENALTY_CASTLING_PENDING_BOTH;
         } else if (castlingBlack != 0) {
             castlingPenalty -= PENALTY_CASTLING_PENDING;
         } else if ((state2 & CASTLED_BLACK) == 0) {
             castlingPenalty -= PENALTY_CASTLING_MISSED;
         }
         return castlingPenalty;
     }
 
     public static int getTrappedPiecesPenaltyAsWhite(final Board board) {
         // http://chessprogramming.wikispaces.com/Trapped+pieces
 
         final int[] squares = board.getBoard();
         int score = 0;
         if (squares[A[6]] == BISHOP && squares[C[6]] == -PAWN && squares[B[5]] == -PAWN ||
                 squares[H[6]] == BISHOP && squares[F[6]] == -PAWN && squares[G[5]] == -PAWN) {
             score += PENALTY_TRAPPED_BISHOP_A7;
         }
         if (squares[A[1]] == -BISHOP && squares[C[1]] == PAWN && squares[B[2]] == PAWN ||
                 squares[H[1]] == -BISHOP && squares[F[1]] == PAWN && squares[G[2]] == PAWN) {
             score += -PENALTY_TRAPPED_BISHOP_A7;
         }
         if (squares[A[7]] == KNIGHT && (squares[C[6]] == -PAWN || squares[A[6]] == -PAWN) ||
                 squares[H[7]] == KNIGHT && (squares[F[6]] == -PAWN || squares[H[6]] == -PAWN)) {
             score += PENALTY_TRAPPED_KNIGHT_A8;
         }
         if (squares[A[0]] == -KNIGHT && (squares[C[1]] == PAWN || squares[A[1]] == PAWN) ||
                 squares[H[0]] == -KNIGHT && (squares[F[1]] == PAWN || squares[H[1]] == PAWN)) {
             score += -PENALTY_TRAPPED_KNIGHT_A8;
         }
         if (squares[A[5]] == BISHOP && squares[C[5]] == -PAWN && squares[B[4]] == -PAWN ||
                 squares[H[5]] == BISHOP && squares[F[5]] == -PAWN && squares[G[4]] == -PAWN) {
             score += PENALTY_TRAPPED_BISHOP_A6;
         }
         if (squares[A[2]] == -BISHOP && squares[C[2]] == PAWN && squares[B[3]] == PAWN ||
                 squares[H[2]] == -BISHOP && squares[F[2]] == PAWN && squares[G[3]] == PAWN) {
             score += -PENALTY_TRAPPED_BISHOP_A6;
         }
         if (squares[A[6]] == KNIGHT && squares[B[6]] == -PAWN && squares[A[5]] == -PAWN ||
                 squares[H[6]] == KNIGHT && squares[G[6]] == -PAWN && squares[H[5]] == -PAWN) {
             score += PENALTY_TRAPPED_KNIGHT_A7;
         }
         if (squares[A[1]] == -KNIGHT && squares[B[1]] == PAWN && squares[A[2]] == PAWN ||
                 squares[H[1]] == -KNIGHT && squares[G[1]] == PAWN && squares[H[2]] == PAWN) {
             score += -PENALTY_TRAPPED_KNIGHT_A7;
         }
         if ((squares[A[0]] == ROOK || squares[B[0]] == ROOK || squares[A[1]] == ROOK || squares[B[1]] == ROOK) &&
                 (squares[B[0]] == KING || squares[C[0]] == KING || squares[D[0]] == KING) ||
             (squares[G[0]] == ROOK || squares[H[0]] == ROOK || squares[G[1]] == ROOK || squares[H[1]] == ROOK) &&
                 (squares[F[0]] == KING || squares[G[0]] == KING)) {
             score += PENALTY_TRAPPED_ROOK_BY_KING;
         }
         if ((squares[A[7]] == -ROOK || squares[B[7]] == -ROOK || squares[A[6]] == -ROOK || squares[B[6]] == -ROOK) &&
                 (squares[B[7]] == -KING || squares[C[7]] == -KING || squares[D[7]] == -KING) ||
             (squares[G[7]] == -ROOK || squares[H[7]] == -ROOK || squares[G[6]] == -ROOK || squares[H[6]] == -ROOK) &&
                 (squares[F[7]] == -KING || squares[G[7]] == -KING)) {
             score += -PENALTY_TRAPPED_ROOK_BY_KING;
         }
         return score;
     }
 
     public static boolean drawByInsufficientMaterial(final Board board) {
         if (board.getPieces(WHITE_TO_MOVE, PAWN)[0] > 0 || board.getPieces(BLACK_TO_MOVE, PAWN)[0] > 0 ||
             board.getPieces(WHITE_TO_MOVE, ROOK)[0] > 0 || board.getPieces(BLACK_TO_MOVE, ROOK)[0] > 0 ||
             board.getPieces(WHITE_TO_MOVE, QUEEN)[0] > 0 || board.getPieces(BLACK_TO_MOVE, QUEEN)[0] > 0) {
             return false;
         }
 
         // FIDE
         // KK
         // KBK
         // KNK
         // KNNK
         // KBKB (bishops are on the same colour
 
         // likely draw
         // - both sides have a king and a minor piece each
         // - the weaker side has a minor piece against two knights
         // - two bishops draw against a bishop
         // - two minor pieces against one draw, except when the stronger side has a bishop pair
 
         final int whiteKnightCount = board.getPieces(WHITE_TO_MOVE, KNIGHT)[0];
         final int blackKnightCount = board.getPieces(BLACK_TO_MOVE, KNIGHT)[0];
         final int[] whiteBishops = board.getPieces(WHITE_TO_MOVE, BISHOP);
         final int whiteBishopCount = whiteBishops[0];
         final int[] blackBishops = board.getPieces(BLACK_TO_MOVE, BISHOP);
         final int blackBishopCount = blackBishops[0];
         if (whiteKnightCount + whiteBishopCount <= 2 && whiteBishopCount < 2 &&
             blackKnightCount + blackBishopCount <= 2 && blackBishopCount < 2) {
             return true;
         }
         if (whiteKnightCount + blackKnightCount > 0) {
             return false;
         }
         if ((whiteBishopCount ^ blackBishopCount) == 3) {
             return true;
         }
         boolean bishopOnWhite = false;
         boolean bishopOnBlack = false;
         for (int i = 1; i <= whiteBishopCount; i++) {
             final int pos = whiteBishops[i];
             final int color = (getRank(pos) + getFile(pos)) & 0x01;
             bishopOnWhite |= color == 1;
             bishopOnBlack |= color == 0;
         }
         for (int i = 1; i <= blackBishopCount; i++) {
             final int pos = blackBishops[i];
             final int color = (getRank(pos) + getFile(pos)) & 0x01;
             bishopOnWhite |= color == 1;
             bishopOnBlack |= color == 0;
         }
         return !(bishopOnWhite && bishopOnBlack);
     }
 
     public static double drawProbability(final Board board) {
         final int toMove = board.getState() & WHITE_TO_MOVE;
         final double probability;
         if (board.getPieces(toMove, PAWN)[0] <= 0 && board.getPieces(toMove, ROOK)[0] <= 0 &&
             board.getPieces(toMove, QUEEN)[0] <= 0) {
 
             final int knightCount = board.getPieces(toMove, KNIGHT)[0];
             final int[] bishops = board.getPieces(toMove, BISHOP);
             final int bishopCount = bishops[0];
             if (knightCount + bishopCount <= 2 && bishopCount < 2) {
                 if (bishopCount == 1 && knightCount == 1) {
                     return 0.75;
                 } else {
                     return 1.0;
                 }
             }
             if (knightCount == 0) {
                 boolean bishopOnWhite = false;
                 boolean bishopOnBlack = false;
                 for (int i = 1; i <= bishopCount; i++) {
                     final int pos = bishops[i];
                     final int color = (getRank(pos) + getFile(pos)) & 0x01;
                     bishopOnWhite |= color == 1;
                     bishopOnBlack |= color == 0;
                 }
                 if (!(bishopOnWhite && bishopOnBlack)) {
                     return 1.0;
                 } else {
                     probability = DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE;
                 }
             } else {
                 probability = 0.0;
             }
         } else {
             probability = 0.0;
         }
         final int halfMoves = (board.getState() & HALF_MOVES) >> SHIFT_HALF_MOVES;
         if (halfMoves > 80) {
             return probability + (1 - probability) * ((double) (Math.min(halfMoves, 100) - 80)) / 20;
         }
         return probability;
     }
 
     public int pawnEval(final Board board) {
         final int stage = board.getStage();
         final long zobristPawn = board.getZobristPawn();
         int pawnHashValue = pawnHashTable.get(zobristPawn, stage);
         if (pawnHashValue == 0) {
             pawnHashValue = pawnEval(board, stage);
             pawnHashTable.set(zobristPawn, pawnHashValue);
         }
         return pawnHashValue;
     }
 
     public static int pawnEval(final Board board, final int stage) {
         int score = 0;
 
         // pawn storm
         final int whiteKing = board.getKing(WHITE);
         final int whiteKingFile = getFile(whiteKing);
         final int whiteKingRank = getRank(whiteKing);
         final int whiteKing64 = convert0x88To64(whiteKing);
         final long whiteKingMask = 1L << whiteKing64;
         final int blackKing = board.getKing(BLACK);
         final int blackKingFile = getFile(blackKing);
         final int blackKingRank = getRank(blackKing);
         final int blackKing64 = convert0x88To64(blackKing);
         final long blackKingMask = 1L << blackKing64;
         final boolean potentialPawnStorm = (whiteKingFile <= FILE_D && blackKingFile >= FILE_E) ||
             (whiteKingFile >= FILE_E && blackKingFile <= FILE_D);
 
         final int shiftPositionBonusWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftPositionBonusBlack = SHIFT_POSITION_BONUS[BLACK];
         int typeBonusOpening = VAL_POSITION_BONUS_KING[board.getKing(WHITE) + shiftPositionBonusWhite] -
             VAL_POSITION_BONUS_KING[board.getKing(BLACK) + shiftPositionBonusBlack];
         int typeBonusEndGame = VAL_POSITION_BONUS_KING_ENDGAME[board.getKing(WHITE) + shiftPositionBonusWhite] -
             VAL_POSITION_BONUS_KING_ENDGAME[board.getKing(BLACK) + shiftPositionBonusBlack];
         final int[] positionalBonusOpening = VAL_POSITION_BONUS_OPENING[PAWN];
         final int[] positionalBonusEndGame = VAL_POSITION_BONUS_ENDGAME[PAWN];
 
         long[] pawnMask = new long[2];
         long[] pawnAttackMask = new long[2];
         final int[] whitePawns = board.getPieces(WHITE, PAWN);
         int pawnStormBonus = 0;
         final int whitePawnCount = whitePawns[0];
         for (int i = whitePawnCount; i > 0; i--) {
             final int pawn = whitePawns[i];
             final int pawn64 = convert0x88To64(pawn);
             pawnMask[WHITE] |= 1L << pawn64;
             final long pawnAttack = PAWN_ATTACK[WHITE][pawn64];
             pawnAttackMask[WHITE] |= pawnAttack;
             score += (blackKingMask & pawnAttack) > 0? BONUS_KING_IN_SIGHT_NON_SLIDING: 0;
             if (potentialPawnStorm) {
                 final int pawnRank = getRank(pawn);
                 if (pawnRank < blackKingRank) {
                     final int pawnFile = getFile(pawn);
                     int pawnStormBonusDeduction = BONUS_PAWN_STORM_MAX;
                     if (pawnFile == blackKingFile) {
                         pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_MAIN_FILE * (blackKingRank - pawnRank);
                     } else if (pawnFile == blackKingFile + 1 || pawnFile == blackKingFile - 1) {
                         pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_SIDE_FILE * (blackKingRank - pawnRank);
                     }
                     if (pawnStormBonusDeduction < BONUS_PAWN_STORM_MAX) {
                         pawnStormBonus += BONUS_PAWN_STORM_MAX - pawnStormBonusDeduction;
                     }
                 }
             }
             typeBonusOpening += positionalBonusOpening[pawn + shiftPositionBonusWhite];
             typeBonusEndGame += positionalBonusEndGame[pawn + shiftPositionBonusWhite];
         }
         final int[] blackPawns = board.getPieces(BLACK, PAWN);
         final int blackPawnCount = blackPawns[0];
         for (int i = blackPawnCount; i > 0; i--) {
             final int pawn = blackPawns[i];
             final int pawn64 = convert0x88To64(pawn);
             pawnMask[BLACK] |= 1L << pawn64;
             final long pawnAttack = PAWN_ATTACK[BLACK][pawn64];
             pawnAttackMask[BLACK] |= pawnAttack;
             score -= (whiteKingMask & pawnAttack) > 0? BONUS_KING_IN_SIGHT_NON_SLIDING: 0;
             if (potentialPawnStorm) {
                 final int pawnRank = getRank(pawn);
                 if (pawnRank > whiteKingRank) {
                     final int pawnFile = getFile(pawn);
                     int pawnStormBonusDeduction = BONUS_PAWN_STORM_MAX;
                     if (pawnFile == whiteKingFile) {
                         pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_MAIN_FILE * (pawnRank - whiteKingRank);
                     } else if (pawnFile == whiteKingFile + 1 || pawnFile == whiteKingFile - 1) {
                         pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_SIDE_FILE * (pawnRank - whiteKingRank);
                     }
                     if (pawnStormBonusDeduction < BONUS_PAWN_STORM_MAX) {
                         pawnStormBonus -= BONUS_PAWN_STORM_MAX - pawnStormBonusDeduction;
                     }
                 }
             }
             typeBonusOpening -= positionalBonusOpening[pawn + shiftPositionBonusBlack];
             typeBonusEndGame -= positionalBonusEndGame[pawn + shiftPositionBonusBlack];
         }
         score += (typeBonusOpening * (STAGE_MAX - stage) + (typeBonusEndGame + pawnStormBonus) * stage) / STAGE_MAX;
 
         score += (Long.bitCount(pawnMask[WHITE] & pawnAttackMask[BLACK] & ~pawnAttackMask[WHITE]) -
             Long.bitCount(pawnMask[BLACK] & pawnAttackMask[WHITE] & ~pawnAttackMask[BLACK])) * PENALTY_WEAK_PAWN;
 
         int unstoppablePawnWhite = 7; // TODO: handle case where the opponent king is between two passed pawns
         int unstoppablePawnIfNextWhite = 7;
         int unstoppablePawnBlack = 7;
         int unstoppablePawnIfNextBlack = 7;
         long prevFileWhite = 0L;
         long prevFileBlack = 0L;
         for (int i = 0; i < 8; i++) {
             final long fileMask = BITBOARD_FILE[i];
             final long midFileWhite = pawnMask[WHITE] & fileMask;
             final int whiteCount = Long.bitCount(midFileWhite);
             final long midFileBlack = pawnMask[BLACK] & fileMask;
             final int blackCount = Long.bitCount(midFileBlack);
             if (blackCount + whiteCount == 0) {
                 prevFileWhite = 0L;
                 prevFileBlack = 0L;
                 continue;
             }
             if (whiteCount >= 2) {
                 if (whiteCount == 2) {
                     score += PENALTY_DOUBLE_PAWN;
                 } else {
                     score += PENALTY_TRIPLE_PAWN;
                 }
             }
             if (blackCount >= 2) {
                 if (blackCount == 2) {
                     score -= PENALTY_DOUBLE_PAWN;
                 } else {
                     score -= PENALTY_TRIPLE_PAWN;
                 }
             }
 
             final long nextFileWhite;
             final long nextFileBlack;
             if (i < 7) {
                 nextFileWhite = pawnMask[WHITE] & BITBOARD_FILE[i + 1];
                 nextFileBlack = pawnMask[BLACK] & BITBOARD_FILE[i + 1];
             } else {
                 nextFileWhite = 0L;
                 nextFileBlack = 0L;
             }
 
             if (prevFileWhite == 0L && midFileWhite > 0L && nextFileWhite == 0L) {
                 score += PENALTY_ISOLATED_PAWN;
             }
             if (prevFileBlack == 0L && midFileBlack > 0L && nextFileBlack == 0L) {
                 score -= PENALTY_ISOLATED_PAWN;
             }
 
             if (midFileWhite > 0) {
                 final int promotionDistance = Long.numberOfLeadingZeros(midFileWhite) / 8;
                 final long blackPawnsOnAdjacentOrSameFile = pawnMask[BLACK] & BITBOARD_FILE_WITH_NEIGHBOURS[i];
                 if ((blackPawnsOnAdjacentOrSameFile & BITBOARD_RANKS_ABOVE[7 - promotionDistance + 1]) == 0) {
                     int realDist = promotionDistance;
                     if (promotionDistance == 6) {
                         realDist--;
                     }
                     if (whiteKingFile == i && promotionDistance > 7 - whiteKingRank) {
                         realDist++;
                     }
                     final int blackKingDist = Math.max(Math.abs(blackKingFile - i), 7 - blackKingRank);
                     final boolean noBlackPawnsAhead =
                         (blackPawnsOnAdjacentOrSameFile & BITBOARD_RANK[7 - promotionDistance + 1]) == 0;
                     if (noBlackPawnsAhead) {
                         if (realDist + 1 < blackKingDist) {
                             if (realDist < unstoppablePawnWhite) {
                                 unstoppablePawnWhite = realDist;
                             }
                             if (realDist < unstoppablePawnIfNextWhite) {
                                 unstoppablePawnIfNextWhite = realDist;
                             }
                         }
                         score += BONUS_PASSED_PAWN_PER_SQUARE * (6 - promotionDistance);
                     }
                     if (realDist < blackKingDist) {
                         if (realDist < unstoppablePawnIfNextWhite) {
                             unstoppablePawnIfNextWhite = realDist;
                         }
                     }
                 }
             }
 
             if (midFileBlack > 0) {
                 final int promotionDistance = Long.numberOfTrailingZeros(midFileBlack) / 8;
                 final long whitePawnsOnAdjacentOrSameFile = pawnMask[WHITE] & BITBOARD_FILE_WITH_NEIGHBOURS[i];
                 if ((whitePawnsOnAdjacentOrSameFile & BITBOARD_RANKS_BELOW[promotionDistance - 1]) == 0) {
                     int realDist = promotionDistance;
                     if (promotionDistance == 6) {
                         realDist--;
                     }
                     if (blackKingFile == i && promotionDistance > blackKingRank) {
                         realDist++;
                     }
                     final int whiteKingDist = Math.max(Math.abs(whiteKingFile - i), whiteKingRank);
                     final boolean noWhitePawnsAhead =
                         (whitePawnsOnAdjacentOrSameFile & BITBOARD_RANK[promotionDistance - 1]) == 0;
                     if (noWhitePawnsAhead) {
                         if (realDist + 1 < whiteKingDist) {
                             if (realDist < unstoppablePawnBlack) {
                                 unstoppablePawnBlack = realDist;
                             }
                             if (realDist < unstoppablePawnIfNextBlack) {
                                 unstoppablePawnIfNextBlack = realDist;
                             }
                         }
                         score -= BONUS_PASSED_PAWN_PER_SQUARE * (6 - promotionDistance);
                     }
                     if (realDist < whiteKingDist) {
                         if (realDist < unstoppablePawnIfNextBlack) {
                             unstoppablePawnIfNextBlack = realDist;
                         }
                     }
                 }
             }
 
             prevFileWhite = midFileWhite;
             prevFileBlack = midFileBlack;
         }
 
         int pawnShield = 0;
         if ((PAWN_SHIELD_KING_SIDE_KING[WHITE] & whiteKingMask) != 0) {
             for (long shieldMask : PAWN_SHIELD_KING_SIDE[WHITE]) {
                 if ((pawnMask[WHITE] & shieldMask) == shieldMask) {
                     pawnShield = BONUS_PAWN_SHIELD;
                     break;
                 }
             }
         } else if ((PAWN_SHIELD_QUEEN_SIDE_KING[WHITE] & whiteKingMask) != 0) {
             for (long shieldMask : PAWN_SHIELD_QUEEN_SIDE[WHITE]) {
                 if ((pawnMask[WHITE] & shieldMask) == shieldMask) {
                     pawnShield = BONUS_PAWN_SHIELD;
                     break;
                 }
             }
         }
         if ((PAWN_SHIELD_KING_SIDE_KING[BLACK] & blackKingMask) != 0) {
             for (long shieldMask: PAWN_SHIELD_KING_SIDE[BLACK]) {
                 if ((pawnMask[BLACK] & shieldMask) == shieldMask) {
                     pawnShield -= BONUS_PAWN_SHIELD;
                     break;
                 }
             }
         } else if ((PAWN_SHIELD_QUEEN_SIDE_KING[BLACK] & blackKingMask) != 0) {
             for (long shieldMask: PAWN_SHIELD_QUEEN_SIDE[BLACK]) {
                 if ((pawnMask[BLACK] & shieldMask) == shieldMask) {
                     pawnShield -= BONUS_PAWN_SHIELD;
                     break;
                 }
             }
         }
         score += pawnShield * (STAGE_MAX - stage) / STAGE_MAX;
         score += VAL_PIECE_COUNTS[PAWN][whitePawnCount] - VAL_PIECE_COUNTS[PAWN][blackPawnCount];
 
         return PawnHashTable.getPawnHashValue(score, stage, unstoppablePawnWhite, unstoppablePawnIfNextWhite,
             unstoppablePawnBlack, unstoppablePawnIfNextBlack);
     }
 
     public EvalHashTable getEvalHashTable() {
         return evalHashTable;
     }
 
     public PawnHashTable getPawnHashTable() {
         return pawnHashTable;
     }
 
     public void clear() {
         evalHashTable.clear();
         pawnHashTable.clear();
     }
 }
