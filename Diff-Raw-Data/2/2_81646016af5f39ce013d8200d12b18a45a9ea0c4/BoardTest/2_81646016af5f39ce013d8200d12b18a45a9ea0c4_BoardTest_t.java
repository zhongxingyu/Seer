 package sf.pnr.base;
 
 import junit.framework.TestCase;
 
 import static sf.pnr.base.StringUtils.*;
 import static sf.pnr.base.Utils.*;
 
 public class BoardTest extends TestCase {
 
 	public void testToFen() {
 		final Board board = new Board();
 		
 		board.restart();
 		final String fenInitial = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
 		assertEquals(fenInitial, StringUtils.toFen(board));
 		
 		testFenRoundTrip(fenInitial);
 		testFenRoundTrip("k7/8/8/4N3/8/8/8/3K4 b - - 13 56");
 		testFenRoundTrip("rnbqkbnr/pp2pppp/8/2ppP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3");
 	}
 
     public void testEnPassantOnA() {
         final Board board = new Board();
         board.restart();
         board.move(StringUtils.fromSimple("a2a4"));
         final String fenInitial = "rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR b KQkq a3 0 1";
         assertEquals(fenInitial, StringUtils.toFen(board));
     }
 
     public void testEnPassantWithCheck() {
         Board board = fromFen("8/2p5/3p4/KP5r/1R2Pp1k/8/6P1/8 b - e3 0 1");
         play(board, fromSimple("f4e3 e.p."));
         final int kingPos = board.getKing(BLACK_TO_MOVE);
         assertTrue(board.isAttacked(kingPos, WHITE_TO_MOVE));
         board = fromFen("8/2p5/3p4/KP5r/1R2Pp1k/8/6P1/8 b - e3 0 1 ");
         playAndUndo(board, fromSimple("f4e3 e.p."));
     }
 
     public void testEnPassant2() {
         final Board board = fromFen("8/8/3p4/KPp4r/1R2PpPk/8/8/8 b - g3 0 2");
         playAndUndo(board, fromSimple("f4g3 e.p."));
     }
 
 	private void testFenRoundTrip(final String fenInitial) {
 		final Board board = StringUtils.fromFen(fenInitial);
 		assertEquals(fenInitial, StringUtils.toFen(board));
 	}
 	
 	public void testMove() {
 		final Board board = new Board();
 		board.restart();
 		
 		final long[] undos = new long[3];
 		
 		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", StringUtils.toFen(board));
 		undos[0] = board.move(StringUtils.fromSimple("e2e4"));
 		assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", StringUtils.toFen(board));
 		undos[1] = board.move(StringUtils.fromSimple("e7e5"));
 		assertEquals("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2", StringUtils.toFen(board));
 		board.takeBack(undos[1]);
 		assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", StringUtils.toFen(board));
 		board.takeBack(undos[0]);
 		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", StringUtils.toFen(board));
 		
 		board.restart();
 		
 		undos[0] = board.move(StringUtils.fromSimple("e2e4"));
 		assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", StringUtils.toFen(board));
 		undos[1] = board.move(StringUtils.fromSimple("c7c5"));
 		assertEquals("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2", StringUtils.toFen(board));
 		undos[2] = board.move(StringUtils.fromSimple("g1f3"));
 		assertEquals("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2", StringUtils.toFen(board));
 		board.takeBack(undos[2]);
 		assertEquals("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2", StringUtils.toFen(board));
 		board.takeBack(undos[1]);
 		assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", StringUtils.toFen(board));
 		board.takeBack(undos[0]);
 		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", StringUtils.toFen(board));
 	}
 	
 	public void testPlayAndUndo() {
 		playAndUndo(StringUtils.fromSimple("f2f4"));
 		playAndUndo(StringUtils.fromSimpleList("f2f4, e7e5"));
 		playAndUndo(StringUtils.fromSimpleList("f2f4, e7e5, f4e5"));
 		playAndUndo(StringUtils.fromSimpleList("f2f4, e7e5, f4e5, d7d6, e5d6, f8d6, g2g3, d8g5, g1f3, g5g3, h2g3, d6g3"));
 		playAndUndo(StringUtils.fromSimpleList("e2e4, e7e5, g1f3, b8c6, f1b5, a7a6, b5c6"));
 
         Board board = fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
         playAndUndo(board, fromSimpleList("f3f6, h8f8"));
 
         board = fromFen("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28");
         playAndUndo(board, fromSimpleList("b1c1, g2g4, h6f8, f1c1, f8g7, c1c4"));
 
         board.restart();
         play(board, fromSimpleList("e2e4, b7b6, e4e5, d7d5"));
         playAndUndo(board, fromSimple("e5d6 e.p."));
 	}
 
     public void testPromotion() {
         final Board board = new Board();
         board.restart();
         board.move(StringUtils.fromSimple("a2a4")); board.move(StringUtils.fromSimple("b7b5"));
         board.move(StringUtils.fromSimple("b2b4")); board.move(StringUtils.fromSimple("b8a6"));
         board.move(StringUtils.fromSimple("a4b5")); board.move(StringUtils.fromSimple("a6b8"));
         board.move(StringUtils.fromSimple("b5b6")); board.move(StringUtils.fromSimple("b8a6"));
         board.move(StringUtils.fromSimple("b6b7")); board.move(StringUtils.fromSimple("a6b4"));
         board.move(MT_PROMOTION_QUEEN | (B[7] << SHIFT_TO) | B[6]);
         final int[] queens = board.getPieces(WHITE_TO_MOVE, QUEEN);
         assertEquals(2, queens[0]);
         assertTrue(containsPosition(D[0], queens));
         assertTrue(containsPosition(B[7], queens));
     }
 
     public void testIsAttackedSimple() {
         final Board board = new Board();
         board.restart();
         assertFalse(board.isAttacked(A[0], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(B[0], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(C[0], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(D[0], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(E[0], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(F[0], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(G[0], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(H[0], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(A[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(B[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(C[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(D[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(E[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(F[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(G[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(H[1], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(A[2], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(B[2], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(C[2], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(D[2], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(E[2], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(F[2], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(G[2], WHITE_TO_MOVE));
         assertTrue(board.isAttacked(H[2], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(A[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(B[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(C[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(D[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(E[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(F[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(G[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(H[3], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(A[0], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(B[0], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(C[0], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(D[0], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(E[0], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(F[0], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(G[0], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(H[0], BLACK_TO_MOVE));
 
         assertFalse(board.isAttacked(A[7], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(B[7], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(C[7], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(D[7], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(E[7], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(F[7], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(G[7], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(H[7], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(A[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(B[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(C[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(D[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(E[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(F[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(G[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(H[6], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(A[5], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(B[5], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(C[5], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(D[5], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(E[5], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(F[5], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(G[5], BLACK_TO_MOVE));
         assertTrue(board.isAttacked(H[5], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(A[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(B[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(C[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(D[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(E[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(F[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(G[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(H[4], BLACK_TO_MOVE));
         assertFalse(board.isAttacked(A[7], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(B[7], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(C[7], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(D[7], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(E[7], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(F[7], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(G[7], WHITE_TO_MOVE));
         assertFalse(board.isAttacked(H[7], WHITE_TO_MOVE));
     }
 
     public void testIsAttacked() {
         final Board board = fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
         assertTrue(board.isAttacked(C[0], WHITE_TO_MOVE));
     }
 
     public void testIsAttackedKnight() {
         final Board board = fromFen("8/7p/p5pB/8/P1pkn3/8/P4KPP/4r3 b - - 1 30");
         assertTrue(board.isAttacked(F[1], BLACK_TO_MOVE));
     }
 
     public void testPromotionWithUndo() {
         final Board board = fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
         playAndUndo(board, fromSimple("a2a1Q"));
     }
 
     public void testKingMove() {
         final Board board = new Board();
         board.restart();
         board.move(fromSimple("d2d3"));
         board.move(fromSimple("b8a6"));
         board.move(fromSimple("e1d2"));
         assertEquals(D[1], board.getKing(WHITE_TO_MOVE));
     }
 
     public void testRestartKingPos() {
         final Board board = new Board();
         board.restart();
         assertEquals(1, board.getPieceArrayPositions()[E[0]]);
     }
 
     public void testCastlingUndoQueenSide() {
         final Board board = fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
         playAndUndo(board, fromSimple("e1c1") | MT_CASTLING_QUEENSIDE);
     }
 
     public void testCastlingUndoKingSide() {
         final Board board = fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
         playAndUndo(board, fromSimple("e1g1") | MT_CASTLING_KINGSIDE);
     }
 
     public void testCapturedRookAndCastling() {
         final Board board = fromFen("k7/8/8/8/8/8/r7/R1B1K2R b KQ - 0 1");
         play(board, fromSimple("a2a1"));
         final int state = board.getState();
         assertEquals(CASTLING_WHITE_KINGSIDE, state & CASTLING_ALL);
     }
 
     public void testPromotionWithCapture() {
         Board board = fromFen("1b3nq1/P1kp4/8/KP6/8/8/8/3R1B2 w - - 0 1");
         playAndUndo(board, fromSimple("a7b8N"));
         playAndUndo(board, fromSimple("a7b8B"));
         playAndUndo(board, fromSimple("a7b8R"));
         playAndUndo(board, fromSimple("a7b8Q"));
         board = fromFen("1rb5/1P3kp1/P7/2p3K1/3b2P1/6PB/4P3/6N1 w - - 0 1");
         playAndUndo(board, fromSimple("b7c8N"));
         playAndUndo(board, fromSimple("b7c8B"));
         playAndUndo(board, fromSimple("b7c8R"));
         playAndUndo(board, fromSimple("b7c8Q"));
     }
 
     public void testPromotionWithRookCapture() {
         final Board board = fromFen("r3qk1r/ppp1n2p/3p1p2/8/4P3/1B1P4/PpP3PP/R1B1K2R b KQ - 1 2");
         play(board, fromSimple("b2a1N"));
         assertEquals(CASTLING_WHITE_KINGSIDE, board.getState() & CASTLING_ALL);
     }
 
     public void testIsDiscoveredCheckKingMove() {
         final Board board = fromFen("8/8/8/8/2b4p/5p1p/7p/4NK1k w - - 2 3");
         assertFalse(board.isDiscoveredCheck(board.getKing(BLACK), board.getKing(WHITE), 1));
     }
 
     public void testIsCheckingMovePawn() {
         final Board board = fromFen("1k6/8/P7/8/8/8/8/4K3 w - - 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("a6a7")));
     }
 
     public void testIsCheckingMoveKingMove() {
         final Board board = fromFen("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1");
         assertFalse(board.isCheckingMove(StringUtils.fromSimple("e1f1")));
     }
 
     public void testIsCheckingMoveRookBehindPawn() {
         final Board board = fromFen("k7/8/8/8/8/P7/R7/K7 w - - 0 1");
         assertFalse(board.isCheckingMove(StringUtils.fromSimple("a3a4")));
     }
 
     public void testIsCheckingMoveRookAttack() {
         final Board board = fromFen("8/5KBk/6p1/6Pb/7R/8/8/4q3 w - - 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("h4h5")));
     }
 
     public void testIsCheckingMoveCastlingQueenSide() {
         final Board board = fromFen("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("e1c1") | MT_CASTLING_QUEENSIDE));
     }
 
     public void testIsCheckingMoveCastlingKingSide() {
         final Board board = fromFen("5k2/8/8/8/8/8/8/4K2R w K - 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("e1g1") | MT_CASTLING_KINGSIDE));
     }
 
     public void testIsCheckingMoveCastling2() {
         final Board board = fromFen("8/8/8/8/8/8/8/R3K2k w Q - 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("e1c1") | MT_CASTLING_QUEENSIDE));
     }
 
     public void testIsCheckingMoveEnPassantBishop() {
        final Board board = fromFen("b7/8/8/8/4Pp2/8/8/4k2K b - e3 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("f4e3") | MT_EN_PASSANT));
     }
 
     public void testIsCheckingMoveEnPassantRook() {
         final Board board = fromFen("8/8/8/8/r3Pp1K/8/8/4k3 b - e3 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("f4e3") | MT_EN_PASSANT));
     }
 
     public void testIsCheckingMovePromotionQueen() {
         final Board board = fromFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("a7a8Q")));
     }
 
     public void testIsCheckingMovePromotionKnight() {
         final Board board = fromFen("8/P1k5/8/8/8/8/8/4K3 w - - 0 1");
         assertTrue(board.isCheckingMove(StringUtils.fromSimple("a7a8N")));
     }
 
     public void testPawnCountAfterOpeningMove() {
         final Board board = new Board();
         board.restart();
         board.move(StringUtils.fromLong(board, "d2d4"));
         assertEquals(8, board.getPieces(WHITE_TO_MOVE, PAWN)[0]);
         assertEquals(8, board.getPieces(BLACK_TO_MOVE, PAWN)[0]);
     }
 
     public void testIsMate() {
         final Board board = fromFen("r3qk1r/ppp1n2p/3p1p2/8/4P3/1BpP4/PPP3PP/R1B1K2R w KQ - 0 1");
         assertFalse(board.isMate());
         board.move(StringUtils.fromSimple("c1h6"));
         assertTrue(board.isMate());
     }
 
 	private static void playAndUndo(final int... moves) {
 		final Board board = new Board();
 		board.restart();
         playAndUndo(board, moves);
 	}
 
     private static void playAndUndo(final Board board, final int... moves) {
         final long[] undos = new long[moves.length];
         final int[] states = new int[moves.length];
         final long[] zobristKeys = new long[moves.length];
         final String[] fenBefore = new String[moves.length];
         for (int i = 0; i < moves.length; i++) {
             fenBefore[i] = StringUtils.toFen(board);
             states[i] = board.getState();
             zobristKeys[i] = board.getZobristKey();
             undos[i] = board.move(moves[i]);
             checkPieceListConsistency(board);
         }
         for (int i = undos.length - 1; i >= 0; i--) {
             board.takeBack(undos[i]);
             assertEquals(states[i], board.getState());
             assertEquals(zobristKeys[i], board.getZobristKey());
             assertEquals(fenBefore[i], StringUtils.toFen(board));
             checkPieceListConsistency(board);
         }
     }
 
     private static void play(final Board board, final int... moves) {
         for (int move : moves) {
             board.move(move);
             checkPieceListConsistency(board);
         }
     }
 
     public static void checkPieceListConsistency(final Board board) {
 		checkPieceListConsistency(board, PAWN);
 		checkPieceListConsistency(board, KNIGHT);
 		checkPieceListConsistency(board, BISHOP);
 		checkPieceListConsistency(board, ROOK);
 		checkPieceListConsistency(board, QUEEN);
 		checkPieceListConsistency(board, KING);
         assertTrue(board.getPieces(WHITE_TO_MOVE, PAWN)[0] <= 8);
         assertTrue(board.getPieces(BLACK_TO_MOVE, PAWN)[0] <= 8);
         final int[] squares = board.getBoard();
         final int[] pieceArrayPos = board.getPieceArrayPositions();
         for (int i = 0; i < squares.length; i++) {
             assertTrue(squares[i] != EMPTY && pieceArrayPos[i] != EMPTY || squares[i] == EMPTY && pieceArrayPos[i] == EMPTY);
         }
 	}
 
 	private static void checkPieceListConsistency(final Board board, final int piece) {
 		checkPieceListConsistency(board, piece, WHITE_TO_MOVE);
 		checkPieceListConsistency(board, piece, BLACK_TO_MOVE);
 	}
 
 	private static void checkPieceListConsistency(final Board board, final int piece, final int toMove) {
 		final int[] pieces = board.getPieces(toMove, piece);
 		final int[] squares = board.getBoard();
 		final int[] pieceArrayPositions = board.getPieceArrayPositions();
 		final int signum = toMove == WHITE_TO_MOVE? 1: -1;
 		for (int i = 1; i <= pieces[0]; i++) {
 			assertEquals(signum * piece, squares[pieces[i]]);
 			assertEquals(i, pieceArrayPositions[pieces[i]]);
 		}
 	}
 
     private boolean containsPosition(final int pos, final int[] pieces) {
         for (int i = pieces[0]; i > 0; i--) {
             if (pieces[i] == pos) {
                 return true;
             }
         }
         return false;
     }
 }
