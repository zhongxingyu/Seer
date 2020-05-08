 package gui.gameEntities.piecesBoard;
 
 import gameEngine.gameMath.Point;
 import gameLogic.Game;
 import gameLogic.TurnChangeListener;
 import gameLogic.board.InvalidPlayException;
 import gameLogic.board.Play;
 import gameLogic.board.ValidPlay;
 import gameLogic.board.piece.Piece;
 import gameLogic.gameFlow.gameStates.GameStateGameEnded;
 import gameLogic.gameFlow.gameStates.GameStateMovingStrongs;
 import gameLogic.gameFlow.gameStates.GameStatePuttingStrongs;
 import gameLogic.gameFlow.gameStates.GameStatePuttingWeaks;
 import gameLogic.gameFlow.gameStates.StateVisitor;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BoardPlayInputDecoder implements StateVisitor,TurnChangeListener,MouseListener {
 
 	private final Point startDrag = new Point();
 	private Point endDrag = new Point();
 	private final Game game;
 	private final PiecesBoard piecesBoard;
 	private final List<ErrorListener> errorListeners = new ArrayList<ErrorListener>();
 	private final List<PlayListener> playListeners = new ArrayList<PlayListener>();
 	private final boolean processForTop;
 	private final boolean processForBottom;
 
 	public BoardPlayInputDecoder(final boolean processForTop,final boolean processForBottom, final Game game,final PiecesBoard piecesBoard) {
 		this.processForTop = processForTop;
 		this.processForBottom = processForBottom;
 		this.game = game;
 		game.addTurnListener(this);
 		this.piecesBoard = piecesBoard;
 	}
 
 	public void addErrorListener(final ErrorListener errorListener){
 		errorListeners.add(errorListener);
 	}
 
 	public void addPlayListener(final PlayListener playListener){
 		playListeners.add(playListener);
 	}
 
 	@Override
 	public void changedTurn(final Game game) {
 		refreshBoard();
 	}
 
 	@Override
 	public void mouseClicked(final MouseEvent e) {}
 
 	@Override
 	public void mouseEntered(final MouseEvent e) {}
 
 	@Override
 	public void mouseExited(final MouseEvent e) {}
 
 	@Override
 	public void mousePressed(final MouseEvent e) {
 		if(!shouldProcessThis()){
 			return;
 		}
 		final int mouseX = e.getX();
 		final int mouseY = e.getY();
 		final double boardX1 = piecesBoard.getX();
 		final double boardX2 = boardX1+piecesBoard.getBoardWidth();
 		final double boardY1 = piecesBoard.getY();
 		final double boardY2 = boardY1+piecesBoard.getBoardHeight();
 		if(mouseX<boardX1){return;}
 		if(mouseX>boardX2){return;}
 		if(mouseY<boardY1){return;}
 		if(mouseY>boardY2){return;}
 
 		startDrag.setX((int) ((mouseX - boardX1)/ piecesBoard.getGridWidth()));
 		startDrag.setY((int) ((mouseY - boardY1)/ piecesBoard.getGridHeight()));
 	}
 
 	@Override
 	public void mouseReleased(final MouseEvent e) {
 		if(!shouldProcessThis()){
 			return;
 		}
 		final int mouseX = e.getX();
 		final int mouseY = e.getY();
 		final double boardX1 = piecesBoard.getX();
 		final double boardX2 = boardX1+piecesBoard.getBoardWidth();
 		final double boardY1 = piecesBoard.getY();
 		final double boardY2 = boardY1+piecesBoard.getBoardHeight();
 		if(mouseX<boardX1) {return;}
 		if(mouseX>boardX2) {return;}
 		if(mouseY<boardY1) {return;}
 		if(mouseY>boardY2) {return;}
 		final int pieceColumn = (int) ((mouseX - boardX1)/ piecesBoard.getGridWidth());
 		final int pieceLine = (int) ((mouseY - boardY1)/ piecesBoard.getGridHeight());
 		endDrag = new Point(pieceColumn, pieceLine);
 		visitToKnowHowToPlayAndPlay();
 	}
 
 	@Override
 	public void onGameEnded(final GameStateGameEnded gameStateGameEnded) {
 		//Nothing to do
 	}
 
 	@Override
 	public void onMovingStrongs(final GameStateMovingStrongs gameStateMovingStrongs) {
 		if(startDrag.getX() != endDrag.getX()&&startDrag.getY() != endDrag.getY() ) {
 			return;
 		}
 		final int line = (int)startDrag.getY();
 		final int column = (int)startDrag.getX();
 		final java.awt.Point strongToMovePosition = new java.awt.Point(line,column);
 		final Piece strongToMove = game.getBoard().getPieceAt(strongToMovePosition);
 		if(game.isTopPlayerTurn() && !strongToMove.isTopPlayerStrongPiece()){
 			return;
 		}
 		if(game.isBottomPlayerTurn() && !strongToMove.isBottomPlayerStrongPiece()){
 			return;
 		}
 
 		char direction = 'x';
 		if(startDrag.getX() > endDrag.getX()){
 			direction = Play.LEFT;
 		}
 		if(startDrag.getX() < endDrag.getX()){
 			direction = Play.RIGHT;
 		}
 		if(startDrag.getY() > endDrag.getY()){
 			direction = Play.UP;
 		}
 		if(startDrag.getY() < endDrag.getY()){
 			direction = Play.DOWN;
 		}
 		if(direction != 'x'){
 			tryToPlay(new Play(strongToMove.getId(), direction));
 		}
 	}
 
 	@Override
 	public void onPuttingStrongs(final GameStatePuttingStrongs gameStatePuttingStrongs) {
 		tryToPlay(new Play((int)endDrag.getX()));
 	}
 
 	@Override
 	public void onPuttingWeaks(final GameStatePuttingWeaks gameStatePuttingWeaks) {
 		tryToPlay(new Play((int)endDrag.getX()));
 	}
 
 	private void refreshBoard() {
		if(piecesBoard!=null) {
			piecesBoard.refreshBoard(game.getBoard(),game.getCurrentState().getAlreadyMovedOrEmptySet());
		}
 	}
 
 	public void restartGame(){
 		game.restartGame();
 		refreshBoard();
 	}
 
 	private boolean shouldProcessThis() {
 		return processForTop && game.isTopPlayerTurn()||processForBottom && game.isBottomPlayerTurn();
 	}
 
 	private void shoutToErrorListeners(final String errorMessage){
 		for (final ErrorListener errorListener : errorListeners) {
 			errorListener.error(errorMessage);
 		}
 	}
 
 	private void shoutToPlayListeners(){
 		for (final PlayListener playListener : playListeners) {
 			playListener.played();
 		}
 	}
 
 	public void tryToPlay(final Play play){
 		if(play == null){
 			shoutToErrorListeners("Invalid play");
 			return;
 		}
 		try {
 			final ValidPlay validPlay = game.validatePlay(play, game.isTopPlayerTurn());
 			game.play(validPlay);
 		} catch (final InvalidPlayException invalidPlayException) {
 			shoutToErrorListeners(invalidPlayException.getMessage());
 			return;
 		}
 		refreshBoard();
 		shoutToPlayListeners();
 	}
 
 	private void visitToKnowHowToPlayAndPlay(){
 		game.getCurrentState().accept(this);
 	}
 }
