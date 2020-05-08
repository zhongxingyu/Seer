 package chessGUI;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Vector;
 
 import javax.swing.*;
 
 import chessBackend.*;
 import chessIO.ChessImages;
 import chessPieces.Piece;
 import chessPieces.PieceID;
 
 public class BoardPanel extends JPanel implements MouseListener, ActionListener {
 
 	private BoardGUI boardGUI;
 
 	private int gameHeight;
 	private int gameWidth;
 	private int sidebarWidth;
 	private int imageWidth;
 	private int imageHeight;
 
 	private JPanel squaresGUIPanel;
 	private JPanel lostBlackPiecesPanel;
 	private JPanel lostWhitePiecesPanel;
 
 	private SquarePanel[][] chessSquares;
 	private PieceGUI selectedComponent;
 	private SquarePanel lastMovedSquare;
 
 	private boolean flipBoard;
 	private boolean makeMove;
 
 	private boolean freelyMove;
 	private Board freelyMoveBoard;
 
 	private int highLightCount;
 	private Move highLightMove;
 	private Timer highLightTimer;
 
 	private Adjudicator adjudicator;
 
 	private boolean debug;
 
 	public BoardPanel(BoardGUI boardGUI, boolean debug) {
 		this.debug = debug;
 		this.boardGUI = boardGUI;
 		this.flipBoard = false;
 
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
 		gameHeight = (int) ((double) screenSize.height * 0.8);
 		imageHeight = (int) ((double) gameHeight * 0.10);
 		imageWidth = (int) ((double) imageHeight * 0.6);
 		sidebarWidth = 2 * (int) ((double) imageWidth * 1.15);
 		gameWidth = 2 * sidebarWidth + gameHeight;
 
 		lostBlackPiecesPanel = new JPanel(new FlowLayout());
 		lostBlackPiecesPanel.setBackground(Color.GRAY);
 		lostBlackPiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
 		lostBlackPiecesPanel.addMouseListener(this);
 		this.add(lostBlackPiecesPanel, BorderLayout.WEST);
 
 		squaresGUIPanel = new JPanel(new GridLayout(8, 8));
 		squaresGUIPanel.setBackground(Color.GRAY);
 		squaresGUIPanel.setPreferredSize(new Dimension(gameHeight, gameHeight));
 		buildSquaresGUI();
 
 		this.add(squaresGUIPanel, BorderLayout.CENTER);
 
 		lostWhitePiecesPanel = new JPanel(new FlowLayout());
 		lostWhitePiecesPanel.setBackground(Color.GRAY);
 		lostWhitePiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
 		lostWhitePiecesPanel.addMouseListener(this);
 		this.add(lostWhitePiecesPanel, BorderLayout.EAST);
 
 		this.setBackground(Color.GRAY);
 
 		highLightTimer = new Timer(200, this);
 
 	}
 
 	public void newGame(Board board) {
 
 		adjudicator = new Adjudicator(board);
 
 		refreshBoard();
 
 		colorSquaresDefault();
 		clearPiecesTaken();
 		attachValidMoves();
 
 		updateLastMovedSquare();
 
 	}
 
 	private void setFlipBoard(boolean flipBoard) {
 
 		if (this.flipBoard != flipBoard) {
 			this.flipBoard = flipBoard;
 			refreshBoard();
 
 			updateLastMovedSquare();
 
 			attachValidMoves();
 		}
 
 	}
 
 	public void highlightMove(Move move) {
 		highLightCount = 5;
 		highLightMove = move;
 		highLightTimer.start();
 
 	}
 
 	public void setFreelyMove(boolean freelyMove) {
 
 		if (this.freelyMove && !freelyMove) {
 			// new game with freely moved setup
 			refreshBoard();
 
 			attachValidMoves();
 		}
 
 		this.freelyMove = freelyMove;
 
 		if (freelyMove) {
 			colorSquaresDefault();
 		}
 	}
 
 	public boolean isFreelyMove() {
 		return freelyMove;
 	}
 
 	private void freeMove(PieceGUI fromComponent, Component toComponent) {
 
 		// normal square to square move
 		if (fromComponent instanceof SquarePanel && toComponent instanceof SquarePanel) {
 
 			SquarePanel fromSqr = (SquarePanel) fromComponent;
 			SquarePanel toSqr = (SquarePanel) toComponent;
 
 			if (toSqr.getPieceID() != null) {
 				takePiece(toSqr.getPieceID(), toSqr.getPlayer());
 			}
 
 			toSqr.showChessPiece(fromSqr.getPieceID(), fromSqr.getPlayer());
 
 			fromSqr.clearChessPiece();
 
 			Piece piece = adjudicator.getPiece(fromSqr.getRow(), fromSqr.getCol());
 			adjudicator.placePiece(piece, toSqr.getRow(), toSqr.getCol());
 		}
 
 		// moving piece from board to side lines
 		if (fromComponent instanceof SquarePanel && !(toComponent instanceof SquarePanel)) {
 			SquarePanel fromSqr = (SquarePanel) fromComponent;
 
 			takePiece(fromSqr.getPieceID(), fromSqr.getPlayer());
 
 			fromSqr.clearChessPiece();
 
 			Piece piece = adjudicator.getPiece(fromSqr.getRow(), fromSqr.getCol());
 			adjudicator.placePiece(piece, -1, -1);
 		}
 
 		// moving piece from side lines to board
 		if (fromComponent instanceof JPieceTakenLabel && toComponent instanceof SquarePanel) {
 			SquarePanel toSqr = (SquarePanel) toComponent;
 			JPieceTakenLabel fromLbl = (JPieceTakenLabel) fromComponent;
 
 			if (toSqr.getPieceID() != null) {
 				takePiece(toSqr.getPieceID(), toSqr.getPlayer());
 			}
 
 			toSqr.showChessPiece(fromLbl.getPieceID(), fromLbl.getPlayer());
 
 			fromLbl.getParent().remove(fromLbl);
 			lostWhitePiecesPanel.updateUI();
 			lostBlackPiecesPanel.updateUI();
 
 			Piece piece = new Piece(fromLbl.getPieceID(), fromLbl.getPlayer(), -1, -1, false);
 			adjudicator.placePiece(piece, toSqr.getRow(), toSqr.getCol());
 		}
 
 	}
 
 	private void buildSquaresGUI() {
 		chessSquares = new SquarePanel[8][8];
 
 		SquarePanel square;
 		for (int row = 0; row < 8; row++) {
 			for (int col = 0; col < 8; col++) {
 				square = new SquarePanel(col % 2 == row % 2, row, col, debug);
 				square.addMouseListener(this);
 
 				chessSquares[row][col] = square;
 				squaresGUIPanel.add(chessSquares[row][col]);
 			}
 		}
 
 	}
 
 	public boolean moveMade(Move move) {
 
 		if (adjudicator.move(move)) {
 			refreshBoard();
 
 			if (move.hasPieceTaken()) {
				takePiece(move.getPieceTakenID(), adjudicator.getTurn());
 			}
 
 			updateLastMovedSquare();
 
 			attachValidMoves();
 
 			setGameSatus(adjudicator.getGameStatus(), adjudicator.getTurn());
 		} else {
 			return false;
 		}
 
 		return true;
 
 	}
 
 	public void makeMove() {
 		this.makeMove = true;
 		// setFlipBoard(adjudicator.getTurn() == Side.BLACK);
 	}
 
 	public void setGameSatus(GameStatus status, Side playerTurn) {
 
 		if (status == GameStatus.CHECK) {
 			System.out.println("Check!");
 		}
 
 		if (status == GameStatus.CHECKMATE) {
 		}
 
 		if (status == GameStatus.STALEMATE) {
 
 		}
 	}
 
 	public Move undoMove() {
 		Move undoneMove = null;
 
 		if (adjudicator.canUndo()) {
 
 			undoneMove = adjudicator.undo();
 			// full refresh of board info
 			refreshBoard();
 
 			// reset all squares to default border color
 			colorSquaresDefault();
 
 			// attach the new valid moves to appropriate squares
 			attachValidMoves();
 
 			// update side line view of pieces that have been taken
 			if (undoneMove.hasPieceTaken()) {
 				refreshPiecesTaken();
 			}
 
 			// show what the last move made was before last two moves were made
 			updateLastMovedSquare();
 
 		} else {
 			System.out.println("Cannot undo move");
 		}
 
 		return undoneMove;
 
 	}
 
 	public boolean canUndo() {
 		return adjudicator.canUndo();
 	}
 
 	public Move redoMove() {
 		Move redoneMove = null;
 
 		if (adjudicator.canRedo()) {
 			redoneMove = adjudicator.redo();
 
 			refreshBoard();
 
 			if (redoneMove.hasPieceTaken()) {
				takePiece(redoneMove.getPieceTakenID(), adjudicator.getTurn());
 			}
 
 			updateLastMovedSquare();
 
 			attachValidMoves();
 
 			setGameSatus(adjudicator.getGameStatus(), adjudicator.getTurn());
 		}
 
 		return redoneMove;
 	}
 
 	public boolean canRedo() {
 		return adjudicator.canRedo();
 	}
 
 	public void refreshBoard() {
 
 		highLightCount = 0;
 
 		for (int row = 0; row < 8; row++) {
 			for (int col = 0; col < 8; col++) {
 
 				if (adjudicator.getPieceID(row, col) == null && getChessSquare(row, col).getPieceID() != null) {
 					getChessSquare(row, col).clearChessPiece();
 				}
 
 				if (adjudicator.getPieceID(row, col) != null) {
 					getChessSquare(row, col).showChessPiece(adjudicator.getPieceID(row, col), adjudicator.getPiecePlayer(row, col));
 				}
 
 				if (debug) {
 					chessSquares[row][col].updateDebugInfo("");
 				}
 			}
 		}
 
 		this.selectedComponent = null;
 	}
 
 	private void attachValidMoves() {
 
 		clearValidMoves();
 
 		Vector<Move> validMoves = adjudicator.getValidMoves();
 
 		Move move;
 		for (int m = 0; m < validMoves.size(); m++) {
 			move = validMoves.elementAt(m);
 			getChessSquare(move.getFromRow(), move.getFromCol()).addValidMove(move);
 		}
 
 		if (selectedComponent instanceof SquarePanel) {
 			colorValidMoveSquares((SquarePanel) selectedComponent, true);
 		}
 	}
 
 	private void clearValidMoves() {
 		for (int r = 0; r < 8; r++) {
 			for (int c = 0; c < 8; c++) {
 				chessSquares[r][c].removeAllValidMoves();
 			}
 		}
 	}
 
 	private void colorValidMoveSquares(SquarePanel square, boolean valid) {
 		Vector<Move> validMoves = square.getValidMoves();
 		Move move;
 
 		for (int i = 0; i < validMoves.size(); i++) {
 			move = validMoves.elementAt(i);
 
 			getChessSquare(move.getToRow(), move.getToCol()).showAsValidMove(valid);
 
 			if (debug) {
 				// if (valid && move != null) {
 				// getChessSquare(move.getToRow(),
 				// move.getToCol()).updateDebugInfo(game.getMoveChosenPathValue(move)
 				// + "");
 				// } else {
 				// getChessSquare(move.getToRow(),
 				// move.getToCol()).updateDebugInfo("");
 				// }
 			}
 
 		}
 	}
 
 	private void colorSquaresDefault() {
 
 		selectedComponent = null;
 
 		for (int r = 0; r < 8; r++) {
 			for (int c = 0; c < 8; c++) {
 				chessSquares[r][c].showAsDefault();
 			}
 		}
 	}
 
 	private void takePiece(PieceID pieceTakenID, Side pieceTakenSide) {
 		JPieceTakenLabel picLabel = new JPieceTakenLabel(pieceTakenID, pieceTakenSide);
 		picLabel.addMouseListener(this);
 
 		if (pieceTakenSide == Side.WHITE) {
 			lostWhitePiecesPanel.add(picLabel);
 		} else {
 			lostBlackPiecesPanel.add(picLabel);
 		}
 	}
 
 	private void refreshPiecesTaken() {
 		JPieceTakenLabel picLabel;
 
 		clearPiecesTaken();
 
 		Vector<Piece> takenPieces = adjudicator.getPiecesTaken(Side.WHITE);
 
 		for (int i = 0; i < takenPieces.size(); i++) {
 			picLabel = new JPieceTakenLabel(takenPieces.elementAt(i).getPieceID(), takenPieces.elementAt(i).getSide());
 			picLabel.addMouseListener(this);
 			lostWhitePiecesPanel.add(picLabel);
 		}
 
 		takenPieces = adjudicator.getPiecesTaken(Side.BLACK);
 
 		for (int i = 0; i < takenPieces.size(); i++) {
 			picLabel = new JPieceTakenLabel(takenPieces.elementAt(i).getPieceID(), takenPieces.elementAt(i).getSide());
 			picLabel.addMouseListener(this);
 			lostBlackPiecesPanel.add(picLabel);
 		}
 
 	}
 
 	private void clearPiecesTaken() {
 		lostBlackPiecesPanel.removeAll();
 		lostBlackPiecesPanel.updateUI();
 		lostWhitePiecesPanel.removeAll();
 		lostWhitePiecesPanel.updateUI();
 	}
 
 	private void updateLastMovedSquare() {
 		if (lastMovedSquare != null) {
 			lastMovedSquare.showAsLastMoved(false);
 			lastMovedSquare = null;
 		}
 
 		Move lastMove = adjudicator.getLastMoveMade();
 		if (lastMove != null) {
 			lastMovedSquare = getChessSquare(lastMove.getToRow(), lastMove.getToCol());
 			lastMovedSquare.showAsLastMoved(true);
 		}
 
 	}
 
 	private SquarePanel getChessSquare(int row, int col) {
 		int getRow;
 		int getCol;
 
 		if (flipBoard) {
 			getRow = 7 - row;
 			getCol = 7 - col;
 		} else {
 			getRow = row;
 			getCol = col;
 		}
 
 		return chessSquares[getRow][getCol];
 	}
 
 	public Board getBoard() {
 		return adjudicator.getBoard();
 	}
 
 	private Move getOrientedMove(int fromRow, int fromCol, int toRow, int toCol) {
 		int orienFromRow;
 		int orienFromCol;
 		int orienToRow;
 		int orienToCol;
 
 		if (flipBoard) {
 			orienFromRow = 7 - fromRow;
 			orienFromCol = 7 - fromCol;
 			orienToRow = 7 - toRow;
 			orienToCol = 7 - toCol;
 		} else {
 			orienFromRow = fromRow;
 			orienFromCol = fromCol;
 			orienToRow = toRow;
 			orienToCol = toCol;
 		}
 
 		return new Move(orienFromRow, orienFromCol, orienToRow, orienToCol);
 
 	}
 
 	// Listener Methods
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		SquarePanel clickedSquare = null;
 		JPieceTakenLabel takenClicked = null;
 		Component c = arg0.getComponent();
 
 		if (c instanceof SquarePanel) {
 			clickedSquare = (SquarePanel) c;
 		} else {
 			if (c instanceof JPieceTakenLabel) {
 				takenClicked = (JPieceTakenLabel) c;
 			}
 		}
 
 		// lock board when it's not the users turn
 		if (!makeMove) {
 			System.out.println("Not your turn!");
 			return;
 		}
 
 		if (clickedSquare != null) {
 
 			if (selectedComponent == null) {
 				if (clickedSquare.hasPiece()) {
 
 					if (arg0.isControlDown() && freelyMove) {
 						freeMove(clickedSquare, null);
 					} else {
 						selectedComponent = clickedSquare;
 						selectedComponent.showAsSelected(true);
 
 						if (!freelyMove) {
 							colorValidMoveSquares(clickedSquare, true);
 						}
 					}
 				}
 
 			} else {
 
 				if (selectedComponent != clickedSquare && selectedComponent instanceof SquarePanel) {
 					// square selected and square clicked
 
 					SquarePanel selectedSquare = (SquarePanel) selectedComponent;
 
 					if (!freelyMove) {
 						Move m = getOrientedMove(selectedSquare.getRow(), selectedSquare.getCol(), clickedSquare.getRow(), clickedSquare.getCol());
 
 						Move validMove = selectedSquare.checkIfValidMove(m);
 						if (validMove != null) {
 							selectedSquare.showAsSelected(false);
 							colorValidMoveSquares(selectedSquare, false);
 
 							selectedComponent = null;
 
 							// makeMove(validMove);
 							makeMove = false;
 
 							boardGUI.makeMove(validMove);
 
 						} else {
 							if (clickedSquare.hasPiece()) {
 
 								colorValidMoveSquares((SquarePanel) selectedComponent, false);
 								colorValidMoveSquares(clickedSquare, true);
 
 								((SquarePanel) selectedComponent).showAsSelected(false);
 								selectedComponent = clickedSquare;
 								((SquarePanel) selectedComponent).showAsSelected(true);
 
 							} else {
 								System.out.println("Invalid move");
 							}
 						}
 					} else {
 						// freely moving from square to square
 						freeMove(selectedSquare, clickedSquare);
 						selectedComponent.showAsSelected(false);
 						selectedComponent = null;
 					}
 
 				}
 
 			}
 
 			// moving piece from side lines to board
 			if (selectedComponent instanceof JPieceTakenLabel) {
 				freeMove(selectedComponent, clickedSquare);
 				selectedComponent.showAsSelected(false);
 				selectedComponent = null;
 			}
 		}
 
 		if (freelyMove) {
 			// selecting side line piece for moving to board
 			if (takenClicked != null) {
 
 				if (selectedComponent != null) {
 					selectedComponent.showAsSelected(false);
 				}
 
 				selectedComponent = takenClicked;
 				selectedComponent.showAsSelected(true);
 			}
 
 			// moving from board to side lines
 			if (takenClicked == null && clickedSquare == null && selectedComponent instanceof SquarePanel) {
 				freeMove(selectedComponent, c);
 				selectedComponent.showAsSelected(false);
 				selectedComponent = null;
 			}
 		}
 
 	}
 
 	// unused listener methods
 
 	public void mouseClicked(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		if (e.getSource() == highLightTimer) {
 			if (highLightCount > 0) {
 				if (highLightCount % 2 == 0) {
 					getChessSquare(highLightMove.getToRow(), highLightMove.getToCol()).showAsHighLighted(false);
 					getChessSquare(highLightMove.getFromRow(), highLightMove.getFromCol()).showAsHighLighted(true);
 				} else {
 					getChessSquare(highLightMove.getToRow(), highLightMove.getToCol()).showAsHighLighted(true);
 					getChessSquare(highLightMove.getFromRow(), highLightMove.getFromCol()).showAsHighLighted(false);
 				}
 			} else {
 				highLightTimer.stop();
 				getChessSquare(highLightMove.getToRow(), highLightMove.getToCol()).showAsHighLighted(false);
 				getChessSquare(highLightMove.getFromRow(), highLightMove.getFromCol()).showAsHighLighted(false);
 			}
 
 			highLightCount--;
 		}
 
 	}
 }
