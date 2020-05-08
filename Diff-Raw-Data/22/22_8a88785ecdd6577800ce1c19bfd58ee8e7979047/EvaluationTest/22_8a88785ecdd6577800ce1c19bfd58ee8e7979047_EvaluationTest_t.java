 package sf.pnr.base;
 
 import junit.framework.TestCase;
 import sf.pnr.alg.PawnHashTable;
 
 import static sf.pnr.base.Utils.*;
 import static sf.pnr.base.Evaluation.*;
 
 /**
  */
 public class EvaluationTest extends TestCase {
 
     private Evaluation eval;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         eval = new Evaluation();
     }
 
     @Override
     protected void tearDown() throws Exception {
         eval = null;
         super.tearDown();
     }
 
     public void testDrawByInsufficientMaterialKK() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNK() {
         final Board board = StringUtils.fromFen("4k3/8/8/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKBK() {
         final Board board = StringUtils.fromFen("4k3/8/8/1B6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNKB() {
         final Board board = StringUtils.fromFen("4k3/8/4b3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNKN() {
         final Board board = StringUtils.fromFen("4k3/8/4n3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKBKB() {
         final Board board = StringUtils.fromFen("4k3/8/4b3/8/3K4/8/2B5/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNNK() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNNKN() {
         final Board board = StringUtils.fromFen("4k3/4n3/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNNKB() {
         final Board board = StringUtils.fromFen("4k3/4b3/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKBNKN() {
         final Board board = StringUtils.fromFen("4k3/4n3/4B3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKBNKB() {
         final Board board = StringUtils.fromFen("4k3/4b3/4B3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
    public void testDrawByInsufficientMaterialKBNK() {
        final Board board = StringUtils.fromFen("1k6/4K3/8/1B1N4/8/8/8/8 w - - 0 1");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

     public void testDrawByInsufficientMaterialKBBKN() {
             final Board board = StringUtils.fromFen("7k/5n2/7K/8/4B2B/8/8/8 w - - 0 1");
         assertFalse(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKBBKB() {
             final Board board = StringUtils.fromFen("7k/2b5/7K/1B6/7B/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNNKNN() {
         final Board board = StringUtils.fromFen("4k3/3nn3/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialKNNBK() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 0 1");
         assertFalse(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialBishopsOnSameColors() {
             final Board board = StringUtils.fromFen("7k/7B/7K/7B/8/8/8/8 w - - 0 1");
         assertTrue(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawByInsufficientMaterialBishopsOnOppositeColors() {
             final Board board = StringUtils.fromFen("7k/7B/7K/8/7B/8/8/8 w - - 0 1");
         assertFalse(Evaluation.drawByInsufficientMaterial(board));
     }
 
     public void testDrawProbabilityKK() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNK() {
         final Board board = StringUtils.fromFen("4k3/8/8/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKBK() {
         final Board board = StringUtils.fromFen("4k3/8/8/1B6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNKB() {
         final Board board = StringUtils.fromFen("4k3/8/4b3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNKN() {
         final Board board = StringUtils.fromFen("4k3/8/4n3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKBKB() {
         final Board board = StringUtils.fromFen("4k3/8/4b3/8/3K4/8/2B5/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNNK() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNNKN() {
         final Board board = StringUtils.fromFen("4k3/4n3/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNNKB() {
         final Board board = StringUtils.fromFen("4k3/4b3/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKBNKN() {
         final Board board = StringUtils.fromFen("4k3/4n3/4B3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(0.75, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKBNKB() {
         final Board board = StringUtils.fromFen("4k3/4b3/4B3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(0.75, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKBBKN() {
             final Board board = StringUtils.fromFen("7k/5n2/7K/8/4B2B/8/8/8 w - - 0 1");
         assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKBBKB() {
             final Board board = StringUtils.fromFen("7k/2b5/7K/1B6/7B/8/8/8 w - - 0 1");
         assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNNKNN() {
         final Board board = StringUtils.fromFen("4k3/3nn3/4N3/1N6/3K4/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityKNNBK() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 0 1");
         assertEquals(0.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityBishopsOnSameColors() {
             final Board board = StringUtils.fromFen("7k/7B/7K/7B/8/8/8/8 w - - 0 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityBishopsOnOppositeColors() {
             final Board board = StringUtils.fromFen("7k/7B/7K/8/7B/8/8/8 w - - 0 1");
         assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityBishopsOnOppositeColors2() {
         final Board board = StringUtils.fromFen("4bb2/4k3/4B3/3K4/8/8/8/4B3 w - - 0 2");
         assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityFiftyMoves85() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 85 1");
         assertEquals(0.25, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityFiftyMoves90() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 90 1");
         assertEquals(0.50, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityFiftyMoves95() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 95 1");
         assertEquals(0.75, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityFiftyMoves100() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 100 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityFiftyMoves105() {
         final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 105 1");
         assertEquals(1.0, Evaluation.drawProbability(board));
     }
 
     public void testDrawProbabilityFiftyMoves95OppositeBishops() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/1B1KB3/8/8/8 w - - 95 1");
         assertEquals(0.80, Evaluation.drawProbability(board));
     }
 
     public void testEvalScoreStoredInHash() {
         final Board board = StringUtils.fromFen("7k/5n2/7K/8/4B2B/8/8/8 w - - 0 1");
         final int score = eval.evaluate(board);
         assertEquals(score, eval.evaluate(board));
     }
 
     public void testUnstoppablePawn() {
         final Board board = StringUtils.fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
         final int score = eval.evaluate(board);
         assertTrue("Score: " + score, score > VAL_QUEEN - 200);
         assertTrue("Score: " + score, score < VAL_QUEEN + 200);
     }
 
     public void testPawnEvalBasic() {
         final Board board = new Board();
         board.clear();
         assertEquals(0, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
 
         eval = new Evaluation();
         board.restart();
         assertEquals(0, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPawnEvalDoublePawns() {
         final Board board = StringUtils.fromFen("8/p7/K7/p3p2p/k3p2P/4N3/PP4P1/8 w - - 0 1");
         final int stage = board.getStage();
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int positionalBonusWhite = getPositionalBonusPawn(A[1], shiftWhite, stage) +
             getPositionalBonusPawn(B[1], shiftWhite, stage) + getPositionalBonusPawn(G[1], shiftWhite, stage) +
             getPositionalBonusPawn(H[3], shiftWhite, stage);
         final int positionalBonusBlack = getPositionalBonusPawn(A[4], shiftBlack, stage) +
             getPositionalBonusPawn(A[6], shiftBlack, stage) + getPositionalBonusPawn(E[3], shiftBlack, stage) +
             getPositionalBonusPawn(E[4], shiftBlack, stage) + getPositionalBonusPawn(H[4], shiftBlack, stage) +
             BONUS_PASSED_PAWN_PER_SQUARE * 3;
         assertEquals(positionalBonusWhite - positionalBonusBlack - 2 * PENALTY_DOUBLE_PAWN - 3 * PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPawnEvalBackwardsPawns() {
         final Board board = StringUtils.fromFen("8/Qp4pk/p5b1/5p1p/3B2nP/1P4PK/P1P1r1B1/3r4 b - - 0 1");
         assertEquals(30, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPositionalValuePawn() {
         final Board board = new Board();
         board.restart();
         assertEquals(0, computePositionalBonusNoPawnAsWhite(board));
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         assertEquals(VAL_POSITION_BONUS_PAWN[E[3] + shiftWhite] - VAL_POSITION_BONUS_PAWN[E[1] + shiftWhite],
             computePositionalGain(PAWN, E[1], E[3], board.getStage(), shiftWhite));
         board.move(StringUtils.fromSimple("e2e4"));
         assertEquals(0, computePositionalBonusNoPawnAsWhite(board));
         assertEquals(-(VAL_POSITION_BONUS_PAWN[E[6] + shiftBlack] - VAL_POSITION_BONUS_PAWN[E[4] + shiftBlack]),
             computePositionalGain(PAWN, E[6], E[4], board.getStage(), shiftBlack));
         board.move(StringUtils.fromSimple("e7e5"));
         assertEquals(0, computePositionalBonusNoPawnAsWhite(board));
     }
 
     public void testIsolatedPawnOnFileA() {
         final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/P7/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         final int positionalBonusWhite = getPositionalBonusPawn(A[2], SHIFT_POSITION_BONUS[WHITE], stage);
         int positionalBonusBlack = 0;
         for (int i = 0; i < 8; i++) {
             positionalBonusBlack += getPositionalBonusPawn(getPosition(i, 6), SHIFT_POSITION_BONUS[BLACK], stage);
         }
         assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testIsolatedPawnOnFileB() {
         final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/1P6/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         final int positionalBonusWhite = getPositionalBonusPawn(B[2], SHIFT_POSITION_BONUS[WHITE], stage);
         int positionalBonusBlack = 0;
         for (int i = 0; i < 8; i++) {
             positionalBonusBlack += getPositionalBonusPawn(getPosition(i, 6), SHIFT_POSITION_BONUS[BLACK], stage);
         }
         assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testIsolatedPawnOnFileH() {
         final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/7P/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         final int positionalBonusWhite = getPositionalBonusPawn(H[2], SHIFT_POSITION_BONUS[WHITE], stage);
         int positionalBonusBlack = 0;
         for (int i = 0; i < 8; i++) {
             positionalBonusBlack += getPositionalBonusPawn(getPosition(i, 6), SHIFT_POSITION_BONUS[BLACK], stage);
         }
         assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnFileA() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/P7/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(A[2], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnFileB() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/1P6/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(B[2], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnB4() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/1P6/8/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(B[3], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE * 2 +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPassedPawnOnFileH() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/7P/8/4K3 w - - 0 1");
         final int stage = board.getStage();
         assertEquals(getPositionalBonusPawn(H[2], SHIFT_POSITION_BONUS[WHITE], stage) + BONUS_PASSED_PAWN_PER_SQUARE +
             PENALTY_ISOLATED_PAWN,
             PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
     }
 
     public void testPromotionDistanceStoppableWhite() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1P6/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceStoppable2White() {
         final Board board = StringUtils.fromFen("4k3/8/8/1P6/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppableWhite() {
         final Board board = StringUtils.fromFen("4k3/8/1P6/8/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(2, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppable2White() {
         final Board board = StringUtils.fromFen("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceStoppableBlack() {
         final Board board = StringUtils.fromFen("4k3/8/1p6/8/8/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceStoppable2Black() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/1p6/8/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppableBlack() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/1p6/8/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(2, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPromotionDistanceUnstoppable2Black() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1p6/4K3 w - - 0 1");
         final int pawnHashValue = eval.pawnEval(board);
         assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
         assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
     }
 
     public void testPositionalBonus() {
         final Board board = StringUtils.fromFen("8/5p2/3p1kp1/PR2b2p/r7/8/1P6/1K5B w - - 2 45");
         final int stage = board.getStage();
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int openingScore =
             VAL_POSITION_BONUS_ROOK[B[4] + shiftWhite] - VAL_POSITION_BONUS_ROOK[A[3] + shiftBlack] +
             VAL_POSITION_BONUS_BISHOP[H[0] + shiftWhite] - VAL_POSITION_BONUS_BISHOP[E[4] + shiftBlack] +
             VAL_POSITION_BONUS_KING[B[0] + shiftWhite] - VAL_POSITION_BONUS_KING[F[5] + shiftBlack];
         final int endGameScore =
             VAL_POSITION_BONUS_ROOK_ENDGAME[B[4] + shiftWhite] - VAL_POSITION_BONUS_ROOK_ENDGAME[A[3] + shiftBlack] +
             VAL_POSITION_BONUS_BISHOP[H[0] + shiftWhite] - VAL_POSITION_BONUS_BISHOP[E[4] + shiftBlack] +
             VAL_POSITION_BONUS_KING_ENDGAME[B[0] + shiftWhite] - VAL_POSITION_BONUS_KING_ENDGAME[F[5] + shiftBlack];
         final int bonus = (openingScore * (STAGE_MAX - stage) + endGameScore * stage) / STAGE_MAX;
         assertEquals(bonus, computePositionalBonusNoPawnAsWhite(board));
     }
 
     public void testPositionalBonusQueens() {
         final Board board = StringUtils.fromFen("1q4k1/8/8/2Q5/8/8/8/2K5 w - - 0 1");
         final int stage = board.getStage();
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int openingScore =
             VAL_POSITION_BONUS_QUEEN[C[4] + shiftWhite] - VAL_POSITION_BONUS_QUEEN[B[7] + shiftBlack] +
             VAL_POSITION_BONUS_KING[C[0] + shiftWhite] - VAL_POSITION_BONUS_KING[G[7] + shiftBlack];
         final int endGameScore =
             VAL_POSITION_BONUS_QUEEN_ENDGAME[C[4] + shiftWhite] - VAL_POSITION_BONUS_QUEEN_ENDGAME[B[7] + shiftBlack] +
             VAL_POSITION_BONUS_KING_ENDGAME[C[0] + shiftWhite] - VAL_POSITION_BONUS_KING_ENDGAME[G[7] + shiftBlack];
         final int bonus = (openingScore * (STAGE_MAX - stage) + endGameScore * stage) / STAGE_MAX;
         assertEquals(bonus, computePositionalBonusNoPawnAsWhite(board));
     }
 
     public void testPositionalBonus2() {
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         final int[] pawnEndGame = Evaluation.VAL_POSITION_BONUS_PAWN_ENDGAME;
         assertTrue(pawnEndGame[A[6] + shiftWhite] >= pawnEndGame[A[1] + shiftWhite]);
         assertTrue(pawnEndGame[A[6] + shiftBlack] <= pawnEndGame[A[1] + shiftBlack]);
     }
 
     public void testPositionalBonusSymmetry() throws NoSuchFieldException, IllegalAccessException {
         final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
         final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
         for (int[] arr: Evaluation.VAL_POSITION_BONUS_OPENING) {
             assertEquals(128, arr.length);
             for (int rank = 0; rank < 8; rank++) {
                 for (int file = 0; file < 8; file++) {
                     final int whitePos = Utils.getPosition(file, rank);
                     final int blackPos = Utils.getPosition(file, 7 - rank);
                     assertEquals(StringUtils.toString0x88(whitePos), arr[whitePos + shiftWhite], arr[blackPos + shiftBlack]);
                 }
             }
         }
         for (int[] arr: Evaluation.VAL_POSITION_BONUS_ENDGAME) {
             assertEquals(128, arr.length);
             for (int rank = 0; rank < 8; rank++) {
                 for (int file = 0; file < 8; file++) {
                     final int whitePos = Utils.getPosition(file, rank);
                     final int blackPos = Utils.getPosition(file, 7 - rank);
                     assertEquals(StringUtils.toString0x88(whitePos), arr[whitePos + shiftWhite], arr[blackPos + shiftBlack]);
                 }
             }
         }
     }
 
     public void testPawnMirrorEvalPawnStorm() {
         final Board boardWhite = StringUtils.fromFen("1k6/8/1P6/P1P5/8/8/8/6K1 w - - 0 1");
         final int pawnHashValueWhite = eval.pawnEval(boardWhite);
         final int pawnValueWhite = PawnHashTable.getValueFromPawnHashValue(pawnHashValueWhite);
         final int valueWhite = eval.evaluate(boardWhite);
 
         final Board boardBlack = StringUtils.fromFen("1k6/8/8/8/5p1p/6p1/8/6K1 b - - 0 1");
         final int pawnHashValueBlack = eval.pawnEval(boardBlack);
         final int pawnValueBlack = PawnHashTable.getValueFromPawnHashValue(pawnHashValueBlack);
         final int valueBlack = eval.evaluate(boardBlack);
 
         assertEquals(pawnValueWhite, -pawnValueBlack);
         assertEquals(valueWhite, valueBlack);
     }
 
     public void testPawnMirrorEvalHalfInitial() {
         final Board boardWhite = StringUtils.fromFen("5k2/8/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ - 0 1");
         final int pawnHashValueWhite = eval.pawnEval(boardWhite);
         final int pawnValueWhite = PawnHashTable.getValueFromPawnHashValue(pawnHashValueWhite);
         final int valueWhite = eval.evaluate(boardWhite);
 
         final Board boardBlack = StringUtils.fromFen("rnbqkbnr/pppppppp/8/8/8/8/8/2K5 b kq - 0 1");
         final int pawnHashValueBlack = eval.pawnEval(boardBlack);
         final int pawnValueBlack = PawnHashTable.getValueFromPawnHashValue(pawnHashValueBlack);
         final int valueBlack = eval.evaluate(boardBlack);
 
         assertEquals(pawnValueWhite, -pawnValueBlack);
         assertEquals(valueWhite, valueBlack);
     }
 
     private static int getPositionalBonusPawn(final int position, final int shift, final int stage) {
         final int openingBonus = VAL_POSITION_BONUS_PAWN[position + shift] * (STAGE_MAX - stage);
         final int endgameBonus = VAL_POSITION_BONUS_PAWN_ENDGAME[position + shift] * stage;
         return (openingBonus + endgameBonus) / STAGE_MAX;
     }
 
     public void testCastlingPenaltyCastling() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/8/4K2R w K - 1 1");
         assertEquals(PENALTY_CASTLING_PENDING - PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
         board.move(StringUtils.fromSimple("e1g1") | MT_CASTLING_KINGSIDE);
         assertEquals(-PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
     }
 
     public void testCastlingPenaltyRookMoves() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
         assertEquals(PENALTY_CASTLING_PENDING_BOTH - PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
         board.move(StringUtils.fromSimple("a1a2"));
         assertEquals(PENALTY_CASTLING_PENDING - PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
         board.move(StringUtils.fromSimple("e8e7"));
         board.move(StringUtils.fromSimple("h1h2"));
         assertEquals(PENALTY_CASTLING_MISSED - PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
     }
 
     public void testCastlingPenaltyKingMove() {
         final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/8/4K2R w K - 1 1");
         assertEquals(PENALTY_CASTLING_PENDING - PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
         board.move(StringUtils.fromSimple("e1e2"));
         assertEquals(PENALTY_CASTLING_MISSED-PENALTY_CASTLING_MISSED,
             getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
     }
 
     public void testMobility() {
         final Board board = StringUtils.fromFen("8/1R2K2k/8/8/8/8/8/8 w - - 0 1");
         final int score = Evaluation.computeMobilityBonusAsWhite(board);
         //assertEquals((10 + 8 - 5) * BONUS_MOBILITY + BONUS_DEFENSE + BONUS_DISTANCE_ROOK[1], score);
         assertEquals(BONUS_MOBILITY_ROOK[10] + BONUS_MOBILITY_KING[8] - BONUS_MOBILITY_KING[5] +
             BONUS_DEFENSE + BONUS_DISTANCE_ROOK[1], score);
     }
 
     public void testMobilityBonusArraySizes() {
         assertEquals(8 + 1, BONUS_MOBILITY_KNIGHT.length);
         assertEquals(13 + 1, BONUS_MOBILITY_BISHOP.length);
         assertEquals(14 + 1, BONUS_MOBILITY_ROOK.length);
         assertEquals(27 + 1, BONUS_MOBILITY_QUEEN.length);
         assertEquals(8 + 1, BONUS_MOBILITY_KING.length);
     }
 
     public void testMobilityKnight() {
         final Board board = StringUtils.fromFen("8/p3pk2/2N5/8/1P6/1K6/8/8 w - - 0 1");
         final int[] distance = new int[1];
         final int score = Evaluation.computeMobilityBonusKnight(board, WHITE, distance);
         assertEquals(BONUS_MOBILITY_KNIGHT[5] + BONUS_DEFENSE + 2 * BONUS_ATTACK, score);
         assertEquals(BONUS_DISTANCE_KNIGHT[2], distance[0]);
     }
 
     public void testMobilityPawn() {
         final Board board = StringUtils.fromFen("8/p3p3/2N1kB2/1P6/8/1K6/8/8 w - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals((BONUS_MOBILITY + BONUS_DEFENSE) - (2 * BONUS_MOBILITY + BONUS_ATTACK) - BONUS_HUNG_PIECE, score);
     }
 
     public void testMobilityPawn2() {
         final Board board = StringUtils.fromFen("4k3/8/8/3R1r2/2P1P1P1/8/8/4K3 w - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals(3 * BONUS_MOBILITY + 2 * BONUS_DEFENSE + 2 * BONUS_ATTACK + BONUS_HUNG_PIECE, score);
     }
 
     public void testMobilityPawnBlockedDoubleWhite() {
         final Board board = StringUtils.fromFen("4k3/8/8/5Pp1/3P4/2P5/1PPP4/4K3 w - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals(6 * BONUS_MOBILITY + 3 * BONUS_DEFENSE - BONUS_MOBILITY, score);
     }
 
     public void testMobilityPawnBlockedDoubleBlack() {
         final Board board = StringUtils.fromFen("4k3/1ppp4/2p5/3p1Pp1/8/8/8/4K3 b - - 1 1");
         final int score = Evaluation.computeMobilityBonusPawnAsWhite(board);
         assertEquals(BONUS_MOBILITY - (6 * BONUS_MOBILITY + 3 * BONUS_DEFENSE), score);
     }
 
     public void testUnstoppablePawnWithCapture() {
         final Board board = StringUtils.fromFen("8/8/1p1K4/P7/2k5/8/8/8 w - - 0 1");
         final int score = eval.evaluate(board);
         assertTrue(score > 700);
     }
 }
