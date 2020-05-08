 package edu.touro.cooptetris;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Color;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import edu.touro.cooptetris.pieces.LinePiece;
 import edu.touro.cooptetris.pieces.Square;
 
 public class BoardTest {
 
 	private Board board;
 	private Square[] fullRow;
 
 	private void givenBoard() {
 		board = new Board();
 	}
 
 	private void givenFullRow() {
 		fullRow = new Square[Board.NUM_COLUMNS];
 
 		for (int i = 0; i < fullRow.length; i++) {
 			fullRow[i] = new Square(0, 0, Color.BLACK);
 		}
 	}
 
 	private LinePiece givenLinePiece() {
		LinePiece linePiece = new LinePiece(15, 16 * Square.SIDE);
 		return linePiece;
 	}
 
 	@Test
 	public void testCollidesWithFloorLeft() {
 		givenBoard();
 		givenFullRow();
 
 		LinePiece linePiece = givenLinePiece();
 
 		assertFalse(board.willCollideWithFloorLeft(linePiece));
 
		board.setSquareFull(new Square(0, 16*Square.SIDE, Color.BLACK));
 		assertTrue(board.willCollideWithFloorLeft(linePiece));
 	}
 
 	@Test
 	public void testCollidesWithFloorRight() {
 		givenBoard();
 		givenFullRow();
 
 		LinePiece linePiece = givenLinePiece();
 		assertFalse(board.willCollideWithFloorRight(linePiece));
 
		board.setSquareFull(new Square(2*Square.SIDE, 16*Square.SIDE, Color.BLACK));
 
 		assertTrue(board.willCollideWithFloorRight(linePiece));
 	}
 
 	@Test
 	public void testCollidesWithFloorVertical() {
 		givenBoard();
 		givenFullRow();
 
 		LinePiece linePiece = givenLinePiece();
		linePiece.moveDown();
 		assertTrue(board.willCollideWithFloorVertical(linePiece));
 
 	}
 
 	@Test
 	public void testIsRowFull() {
 		givenBoard();
 		whenRowIsFull(2);
 		whenRowIsFull(3);
 
 		thenRowIsFull(2);
 		thenRowIsFull(3);
 	}
 
 	@Test
 	public void testLandPiece() {
 		givenBoard();
 		givenFullRow();
 
 		givenLinePiece();
 
 		LinePiece SecondLinePiece = new LinePiece(30, 10);
 		board.landPiece(SecondLinePiece);
 
 		// assertTrue(board.willCollideWithFloorRight(linePiece));
 	}
 
 	@Test
 	public void testRemoveRow() {
 		givenBoard();
 
 		whenRowIsFull(2);
 		whenRowIsFull(3);
 
 		whenRowIsRemoved(2);
 
 		thenRowIsNotFull(3);
 		thenRowIsFull(2);
 	}
 
 	private void thenRowIsFull(int rowNumber) {
 		assertTrue(board.isRowFull(rowNumber));
 	}
 
 	private void thenRowIsNotFull(int rowNumber) {
 		assertFalse(board.isRowFull(rowNumber));
 	}
 
 	private void whenRowIsFull(int rowNumber) {
 		givenFullRow();
 		board.setSquaresArray(fullRow, rowNumber);
 	}
 
 	private void whenRowIsRemoved(int rowNumber) {
 		board.removeRow(rowNumber);
 	}
 
 	@Test
 	public void testRemoveNoRowsWhenBoardIsEmpty() {
 		givenBoard();
 		whenFullRowsAreRemoved();
 		thenBoardHasCorrectNumberOfRows();
 	}
 
 	@Test
 	public void testRemoveFullRows() {
 		givenBoard();
 		whenRowIsFull(2);
 		whenRowIsFull(3);
 		whenFullRowsAreRemoved();
 		thenRowIsNotFull(2);
 		thenRowIsNotFull(3);
 		thenBoardHasCorrectNumberOfRows();
 	}
 
 	private void whenFullRowsAreRemoved() {
 		givenBoard();
 		board.removeFullRows();
 	}
 
 	private void thenBoardHasCorrectNumberOfRows() {
 		Assert.assertEquals(Board.NUM_ROWS, board.getNumRows());
 	}
 
 }
