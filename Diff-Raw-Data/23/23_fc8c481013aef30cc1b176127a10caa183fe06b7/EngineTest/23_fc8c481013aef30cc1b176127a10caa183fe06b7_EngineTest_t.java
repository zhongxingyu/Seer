 package sf.pnr.base;
 
 import junit.framework.TestCase;
 import sf.pnr.io.UCI;
 
 import static sf.pnr.base.Engine.*;
 import static sf.pnr.base.Evaluation.*;
 import static sf.pnr.base.StringUtils.*;
 import static sf.pnr.base.Utils.*;
 
 /**
  */
 public class EngineTest extends TestCase {
 
     private final Engine engine = new Engine();
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         engine.clear();
         engine.setSearchEndTime(Long.MAX_VALUE);
     }
 
     public void testPositions() {
         final Board board = new Board();
         board.restart();
         int score = Engine.getValueFromSearchResult(engine.search(board, 5, 0));
         assertTrue(Integer.toString(score), Math.abs(score) < VAL_PAWN);
 
         // ROOK
         score = Engine.getValueFromSearchResult(engine.search(fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1"), 5, 0));
         assertTrue(Integer.toString(score), score > VAL_ROOK * 0.9 && score < VAL_ROOK * 1.1);
         // MATE
         score = Engine.getValueFromSearchResult(engine.search(fromFen("5rk1/5Npp/8/3Q4/8/8/8/7K w - - 0 1"), 6, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         // QUEEN
         score = Engine.getValueFromSearchResult(engine.search(fromFen("2k5/8/8/8/p7/8/8/4K3 b - - 0 1"), 5, 0));
         assertTrue(Integer.toString(score), score > VAL_QUEEN * 0.7 && score < VAL_QUEEN * 1.3);
         // MATE
         score = Engine.getValueFromSearchResult(engine.search(fromFen("8/8/8/8/8/8/R7/R3K2k w Q - 0 1"), 5, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         // -QUEEN
         score = Engine.getValueFromSearchResult(engine.search(fromFen("7k/8/8/8/R2K3q/8/8/8 w - - 0 1"), 5, 0));
         assertTrue(Integer.toString(score), score < -VAL_QUEEN * 0.7 && score > -VAL_QUEEN * 1.3);
     }
 
     public void testTranspTable() {
         final Board board = fromFen("5rk1/5Npp/8/3Q4/8/8/8/7K w - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 6, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testTranspTable2() {
         final Board board = fromFen("1b1k4/2NP4/1P1K4/4P3/8/8/8/8 w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals(fromSimple("e5e6"), bestLine[0]);
         assertEquals(fromSimple("e5e6"), Engine.getMoveFromSearchResult(result));
     }
 
     public void testNodeCountCheck() {
         final Board board = fromFen("3rk2r/pp1n1pb1/q1pBpn1p/8/2PP4/3Q1N2/PP3PPP/R3R1K1 w - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 5, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         assertTrue("Too many nodes processed: " + engine.getNodeCount(), engine.getNodeCount() < 100000);
     }
 
     public void testMateIn1Ply() {
         final Board board = fromFen("r3qk1r/ppp1n2p/3p1p2/8/4P3/1BpP4/PPP3PP/R1B1K2R w KQ - 0 1");
         final int searchDepth = 4 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testDiscoveredCheck() {
         final Board board = fromFen("1k2r1nr/p1p2ppp/PpQ5/4b3/8/3P1P2/1PPN2PP/4K1NR b - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 4 + 1, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("e5g3")));
     }
 
     public void testIsDiscoveredCheck() {
         final Board board = fromFen("1k2r1nr/p1p2ppp/PpQ5/4b3/8/3P1P2/1PPN2PP/4K1NR b - - 0 1");
         board.move(StringUtils.fromSimple("e5b2"));
         assertTrue(board.isDiscoveredCheck(E[0], E[4], -1));
     }
 
     public void testMateIn5Ply() {
         final Board board = fromFen("k1K5/1p6/1P6/8/8/p7/N7/8 w - - 0 1");
         final int searchDepth = (5 + 1) << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateIn5Ply2() {
         final Board board = fromFen("5k2/p4r2/Ppp3r1/8/7p/7K/1P1Qn2P/8 b - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 6, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateIn11() {
         final Board board = fromFen("7k/8/6P1/6K1/8/8/8/8 w - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 11 + 1, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateIn5() {
         final Board board = fromFen("r1b4k/ppqp3p/2n1pPp1/7Q/8/8/PPP2PPP/R4RK1 w - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 5 + 1, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateIn8() {
         final Board board = fromFen("8/8/8/3B4/4b2p/3N1p1p/5K1p/7k w - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 8 + 1, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateWithPromotion() {
         final Board board = fromFen("6k1/3P2pp/1p6/6r1/P7/1P2q3/5RPP/6K1 w - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 3 + 1, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("d7d8Q")) ||
             MoveGeneratorTest.containsMove(bestLine, fromSimple("d7d8R")));
     }
 
     public void testMateWithPromotion2() {
         final Board board = fromFen("k2r4/3N4/K1P5/8/7b/8/8/8 w - - 0 1");
         final long result = engine.search(board, 3, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateWithOpponentPromotion() {
         final Board board = fromFen("k1K5/1p6/1P6/8/1N6/8/p7/8 w - - 0 1");
         final int score = Engine.getValueFromSearchResult(engine.search(board, 4 + 1, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("b4d5")));
     }
 
     public void testPromotion() {
         final Board board = fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
         final int searchDepth = 6 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_QUEEN * 0.9 && score < VAL_QUEEN * 1.1);
     }
 
     public void testPromotionAt1Ply() {
         final Board board = fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
         final int searchDepth = 1 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_QUEEN * 0.9 && score < VAL_QUEEN * 1.1);
         final int[] bestLine = engine.getBestLine(board);
         assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("a2a1Q")));
     }
 
     public void testBestLineWithMateIn1() {
         final Board board = fromFen("1bkr4/1b2R3/8/8/2qp4/8/6BP/7K w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals(1, bestLine.length);
         assertEquals("Bxb7#", toShort(board, bestLine[0]));
         assertEquals("Bxb7#", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testBestLineMatchesEngineResult() {
         final Board board = fromFen("1B1b4/8/p3Pk2/1ppr2p1/4R1P1/1P3P2/P1K5/8 w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int[] bestLine = engine.getBestLine(board);
 //        assertEquals("f4", toShort(board, bestLine[0]));
         assertEquals(Engine.getMoveFromSearchResult(result), bestLine[0]);
     }
 
     public void testBestLineWithMateIn1V2() {
         final Board board = fromFen("1n2kb1r/p4ppp/4q3/4p1B1/4P3/8/PPP2PPP/2KR4 w k - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals(1, bestLine.length);
         assertEquals("Rd8#", toShort(board, bestLine[0]));
         assertEquals("Rd8#", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testBestMove1() {
         final Board board = fromFen("1r2r1k1/2pq1pb1/4b1pp/nPQnp3/P2pP3/1p1P1NPP/1B3PBK/1R2RN2 b - e3 0 1");
         final long result = engine.search(board, 0, 50);
         final int[] bestLine = engine.getBestLine(board, Engine.getMoveFromSearchResult(result));
         assertEquals("Nb7", toShort(board, bestLine[0]));
         assertEquals("Nb7", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testBestMove2() {
         final Board board = fromFen("2rqnrk1/p5pp/bp2pp2/n1ppP3/2PP4/P1PBBP1N/4Q1PP/2R2RK1 w - d6 0 1");
         final long result = engine.search(board, 0, 50);
         final int[] bestLine = engine.getBestLine(board, Engine.getMoveFromSearchResult(result));
         assertEquals("Nf4", toShort(board, bestLine[0]));
         assertEquals("Nf4", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testDiscoveredMate() {
         final Board board = fromFen("1r1rb1k1/2p3p1/p2q1pN1/3PpP2/Pp1bP3/1B5R/1P4PP/2B4K w - - 0 1");
         final int searchDepth = 6 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals("Rh8+", toShort(board, bestLine[0]));
     }
 
     public void testRookSacWithPinnedQueen() {
         final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
         final int searchDepth = 3 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_ROOK * 0.9 && score < VAL_ROOK * 1.1);
     }
 
     public void testMateWithoutCapture() {
         final Board board = fromFen("1b1k4/2NP4/1P1K4/4P3/8/8/8/8 w - - 0 1");
         final int searchDepth = 5 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testQueenMate() {
         final int score = getValueFromSearchResult(engine.search(fromFen("8/8/8/8/7p/5Qbk/8/5K2 w - - 0 1"), 5, 0));
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testRookSacWithPinnedQueenTimeLimit() {
         final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
         final long start = System.currentTimeMillis();
         final int score = Engine.getValueFromSearchResult(engine.search(board, 15, 300));
         final long elapsed = System.currentTimeMillis() - start;
         // we shouldn't exceed the time by more than 0.5 seconds
         assertTrue(elapsed + "ms", elapsed < 800);
         assertTrue(Integer.toString(score), score > VAL_ROOK * 0.9 && score < VAL_ROOK * 1.1);
     }
 
     public void testMateWithQueenSac1() {
         final Board board = fromFen("5rk1/5Npp/8/3Q4/8/8/8/7K w - - 0 1");
         final int searchDepth = 6 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateWithQueenSac2() {
         final Board board = fromFen("8/p7/8/q2p4/6B1/4KN2/6k1/2Q2n2 w - - 0 1");
         final int searchDepth = 3 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int move = Engine.getMoveFromSearchResult(result);
         assertEquals("Qxf1+", StringUtils.toShort(board, move));
     }
     
     public void testMateWithQueenSac3() {
         final Board board = fromFen("4Qrk1/pp2nppp/8/7q/8/8/PP4PP/3R3K w - - 0 1");
         final int searchDepth = 3 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testMateWithRookSac() {
         final Board board = fromFen("8/5KBk/6p1/6Pb/7R/8/8/4q3 w - - 0 1");
         final int searchDepth = 3 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testTranspTableQuiescence() {
         final Board board = fromFen("8/p7/8/q2p4/6B1/4KN1p/6k1/2Q2n2 w - - 0 1");
         final int searchDepth = 1 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int move = Engine.getMoveFromSearchResult(result);
         assertEquals("Qxf1+", StringUtils.toShort(board, move));
     }
 
     public void testMateIn2WithTwoRooks() {
         final Board board = fromFen("8/8/8/8/8/8/R7/R3K2k w Q - 0 1");
         final int searchDepth = 4 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
     }
 
     public void testQuickestMate1() {
         final Board board = fromFen("8/8/8/8/6p1/5PK1/8/3q1Qbk w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals("Qg2#", toShort(board, bestLine[0]));
         assertEquals("Qg2#", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testQuickestMate2() {
         final Board board = fromFen("6rk/4rpp1/p2qpN1p/1p4Q1/4pP2/P5R1/1PP3PP/5R1K w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals("Qxh6+", toShort(board, bestLine[0]));
         assertEquals("Qxh6+", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testQuickestMate3() {
         final Board board = fromFen("4b1k1/8/5PP1/4B3/8/1pp5/2q5/K6R w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals("Rh8+", toShort(board, bestLine[0]));
         assertEquals("Rh8+", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testQuickestMate4() {
         final Board board = fromFen("8/8/8/8/8/7K/2p4p/2R2Rrk w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertTrue("Rce1,Rfe1".contains(toShort(board, bestLine[0])));
         assertTrue("Rce1,Rfe1".contains(toShort(board, Engine.getMoveFromSearchResult(result))));
     }
 
     public void testQuickestMate5() {
         final Board board = fromFen("3K1k2/5P2/5RPb/8/8/8/8/5Q2 w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals("Qh3", toShort(board, bestLine[0]));
         assertEquals("Qh3", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testQuickestMate6() {
         final Board board = fromFen("8/8/8/8/N1K5/k1N5/p7/R2q4 w - - 0 1");
         final long result = engine.search(board, 5, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         final int[] bestLine = engine.getBestLine(board);
         assertEquals("Rxa2#", toShort(board, bestLine[0]));
         assertEquals("Rxa2#", toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testStalemate() {
         final Board board1 = fromFen("1R2bk2/7Q/P4P2/N6p/4p2P/4K3/p7/B7 b - - 0 1");
         int score = Engine.getValueFromSearchResult(engine.search(board1, 0, 100));
         assertTrue(Integer.toString(score), (score < VAL_PAWN * 0.2) && (score > -VAL_PAWN * 0.2));
         final Board board2 = fromFen("1R3bk1/8/P1P3P1/8/4KpP1/2B2P2/8/8 b - - 0 1");
         score = Engine.getValueFromSearchResult(engine.search(board2, 0, 100));
         assertTrue(Integer.toString(score), (score < VAL_PAWN * 0.2) && (score > -VAL_PAWN * 0.2));
     }
 
     public void testKingInCheck() {
         final Board board = fromFen("7k/8/8/8/R2K3q/8/8/8 w - - 0 1");
         final int searchDepth = 3 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score < -VAL_QUEEN * 0.7 && score > -VAL_QUEEN * 1.3);
     }
 
     public void testMateWithKnightSac() {
         final Board board = fromFen("8/6p1/5pPk/5P2/6K1/2pN4/2Pn4/2B5 w - - 0 1");
         final long result = engine.search(board, 4, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
         assertEquals("Nb2", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
    public void testPawnMate() {
        final Board board = fromFen("8/5N1p/6p1/4Bn1k/8/7K/6P1/2r1q3 w - - 0 1");
        final long result = engine.search(board, 2, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        assertEquals("Nb2", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testIsValidKillerMove() {
        final Board board = fromFen("8/5N1p/6p1/4Bn1k/8/7K/6P1/2r1q3 w - - 0 1");
        assertTrue(engine.isValidKillerMove(board, G[1], G[3]));
    }

     public void testAttackedKnight() {
         final Board board = fromFen("1r3rk1/3bppbp/1q1p1np1/1N6/P3P3/3Q2PP/1P1B1PB1/2R2RK1 b - - 0 1");
         final long result = engine.search(board, 6, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score < VAL_PAWN);
         assertTrue(Integer.toString(score), score > -VAL_PAWN);
         assertTrue("Bxb5, Qa6".contains(StringUtils.toShort(board, Engine.getMoveFromSearchResult(result))));
     }
 
     public void testBishopMove() {
         final Board board = fromFen("r1bqk2r/2ppbppp/p7/1p2P3/4n3/1B6/PPP2PPP/RNBQ1RK1 b kq - 0 1");
         final long result = engine.search(board, 6, 0);
         final int score = Engine.getValueFromSearchResult(result);
         assertTrue(Integer.toString(score), score < VAL_PAWN);
         assertTrue(Integer.toString(score), score > -VAL_PAWN);
         assertEquals("Bb7", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
     }
 
     public void testConsistency() {
         final Board board = fromFen("6qk/7p/p1pp1Q2/4p3/4P1b1/8/2P3PP/6NK b - - 6 32");
         final long result1 = engine.search(board, 8, 0);
         final int score1 = Engine.getValueFromSearchResult(result1);
         final int move = Engine.getMoveFromSearchResult(result1);
         assertEquals(StringUtils.fromSimple("g8g7"), move);
         board.move(move);
         final int score2 = Engine.getValueFromSearchResult(engine.search(board, 7, 0));
         assertTrue(score1 + " vs " + score2, Math.abs(Math.abs(score2) - Math.abs(score1)) < 50);
     }
 
     public void no_testDebugCheckedMoves() {
         final Board board = fromFen("1b5k/7P/p1p2np1/2P2p2/PP3P2/4RQ1R/q2r3P/6K1 w - - 0 1");
         final int searchDepth = 4 << SHIFT_PLY;
         final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
         final int move = Engine.getMoveFromSearchResult(result);
         assertEquals("Re8+", StringUtils.toShort(board, move));
     }
 
     public void no_testScore() {
         final Board board = fromFen("r1b1k2r/ppp1qppp/2npp3/3P4/2P1n3/2B2NP1/PP2PP1P/2RQKB1R b Kkq - 0 9");
         engine.setBestMoveListener(new BestMoveListener() {
             @Override
             public void bestMoveChanged(final int depth, final int bestMove, final int value, final long time, final int[] bestLine, final long nodes) {
                 final String message = String.format("info depth %d currmove %s score cp %d time %d pv %s nodes %d",
                     depth, StringUtils.toLong(bestMove), value, time, StringUtils.toLong(bestLine, " "), nodes);
                 System.out.println(message);
             }
         });
         engine.search(board, 8, 0);
     }
 
     public void no_testScore2() {
         final Board board = fromFen("2rn1rk1/2p1qppp/pn1p1bb1/1P6/1P1PPB2/2NQ1N1P/2B2PP1/R3R2K b - - 0 25");
 //        final Board board = fromFen("r4rk1/p1pn1pp1/1p1qp2p/1B6/3Pb3/4PN2/PP3PPP/2RQ1RK1 w - - 2 15");
         engine.setBestMoveListener(new BestMoveListener() {
             @Override
             public void bestMoveChanged(final int depth, final int bestMove, final int value, final long time, final int[] bestLine, final long nodes) {
                 final String message = String.format("info depth %d currmove %s score cp %d time %d pv %s nodes %d",
                     depth, StringUtils.toLong(bestMove), value, time, StringUtils.toLong(bestLine, " "), nodes);
                 System.out.println(message);
             }
         });
         engine.search(board, 11, 0);
 //        System.out.println(new Evaluation().evaluate(board));
 //        board.move(fromLong(board, "b5d7"));
 //        System.out.println(new Evaluation().evaluate(board));
 //        board.move(fromLong(board, "e4f3"));
 //        System.out.println(new Evaluation().evaluate(board));
     }
 }
