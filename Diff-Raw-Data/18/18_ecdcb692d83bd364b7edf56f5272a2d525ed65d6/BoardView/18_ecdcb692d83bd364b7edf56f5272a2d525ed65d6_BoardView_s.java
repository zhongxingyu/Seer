 package se.lannstrom.chesssensei;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import se.lannstrom.chesssensei.SelectionManager.SelectionState;
 import se.lannstrom.chesssensei.model.Board;
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.Board.ChessPiece;
 import se.lannstrom.chesssensei.model.rules.ChessRuleStrategy;
 import se.lannstrom.chesssensei.model.BoardPosition;
 import se.lannstrom.chesssensei.model.ChessMove;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class BoardView extends View {
 	private class GuestureDect extends GestureDetector.SimpleOnGestureListener {
 	   @Override
 	   public boolean onDown(MotionEvent e) {
 		   tapped(e);
 		   return true;
 	   }
 
 	}
 	private GestureDetector detector =
 			new GestureDetector(BoardView.this.getContext(), new GuestureDect());
 
 	private Board board;
 	private Paint darkPaint;
 	private Paint lightPaint;
 	private Rect rect;
 	private int squareDiag;
 
 	/* Set if the user has flipped the screen */
 	private boolean inverted;
 
 	private SelectionManager selectionManager;
 	private Paint fromSelectedPaint;
 	private Paint movesPaint;
 
 	private HashMap<ChessPiece, Bitmap> scaledBitmaps = new HashMap<Board.ChessPiece, Bitmap>();
 
 	private ChessRuleStrategy chessRuleStrategy;
 
 	/**
 	 * Listeners to inform when the current player changes
 	 */
 	private ArrayList<ActiveColorChangeListener> activeColorListener =
 			new ArrayList<ActiveColorChangeListener>();
 
 	Context context;
 
 	public BoardView(Context context, AttributeSet as) {
 		super(context, as);
 
 		this.context = context;
 
 		darkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		darkPaint.setColor(Color.GREEN);
 		lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		lightPaint.setColor(Color.GRAY);
 		fromSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		fromSelectedPaint.setColor(Color.RED);
 		movesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		movesPaint.setColor(Color.BLUE);
 
 		rect = new Rect();
 		board = Board.createStartingBoard();
 
 		selectionManager = new SelectionManager(this, ChessColor.WHITE, board);
 		selectionManager.setSelectionState(SelectionState.FROM);
 		chessRuleStrategy = new ChessRuleStrategy();
 		informActiveColorListeners(board.getActive());
 	}
 	
 	public SelectionManager getSelectionManager() {
 		return selectionManager;
 	}
 
 	public void addActiveColorChangeListener(ActiveColorChangeListener l) {
 		activeColorListener.add(l);
 	}
 
 	private void informActiveColorListeners(ChessColor c) {
 		for (ActiveColorChangeListener l : activeColorListener) {
 			l.activeColor(c);
 		}
 	}
 
 	public void doMove() {
 		ChessMove move = selectionManager.buildMove();
 		chessRuleStrategy.doMove(board, move);
 		selectionManager.reset();
 
 		ChessColor active = board.getActive();
 		selectionManager.setColor(active);
 		informActiveColorListeners(active);
 
 		invalidate();
 	}
 
 	public double getRelativeX(MotionEvent e) {
		return e.getX() - getLeft();
 	}
 
 	public double getRelativeY(MotionEvent e) {
		return e.getY() - getTop();
 	}
 
 	public void tapped(MotionEvent e) {
 	   int selectedX = (int) (getRelativeX(e) / squareDiag);
 	   int selectedY = (int) (getRelativeY(e) / squareDiag);
 	   /* Make sure we stay inside the boundaries of the board */
 	   if (selectedX > 7) selectedX = 7;
 	   if (selectedY > 7) selectedY = 7;
 	   if (selectedX < 0) selectedX = 0;
 	   if (selectedY < 0) selectedY = 0;
 
 	   if (inverted) {
 		   selectedY = 7 - selectedY;
 	   }
 
 	   selectionManager.select(selectedX, selectedY);
 
 	   invalidate();
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public void setBoard(Board b) {
 		board = b;
 		invalidate();
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 
 		int squareWidth = w / 8;
 		int squareHeight = h / 8;
 
 		squareDiag = Math.min(squareWidth, squareHeight);
 		scaleBitmaps(squareDiag);
 	}
 
 	/**
 	 * Scale the images of the chess pieces so they fit in the new square size
 	 *
 	 * @param dstDiag new square diagonal
 	 */
 	private void scaleBitmaps(int dstDiag) {
 		for (ChessPiece cp : ChessPiece.values()) {
 			Bitmap scaled =  ChessPieceImages.getInstance(context).
 					getScaledChessPiece(cp, dstDiag);
 			scaledBitmaps.put(cp, scaled);
 		}
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j ++) {
 				drawSquare(canvas, i, j);
 			}
 		}
 	}
 
 	/**
 	 * Draw the chess square at (i, j)
 	 *
 	 * @param canvas
 	 * @param i
 	 * @param j
 	 */
 	private void drawSquare(Canvas canvas, int i, int j) {
		rect.left = getLeft() + i * squareDiag;
 		rect.right = rect.left + squareDiag;
		rect.top = getTop() + j * squareDiag;
 		rect.bottom = rect.top + squareDiag;
 
 		/* Logic for handling flipping the screen */
 		if (inverted) {
 			j = 7 - j;
 		}
 
 		/* Draw square background */
 		Paint p = null;
 		BoardPosition from = selectionManager.getFrom();
 		if (from != null &&	from.getX() == i &&
 							from.getY() == j) {
 			p = fromSelectedPaint;
 		} else if (selectionManager.isValidMove(i, j)) {
 			p = movesPaint;
 		} else if ((j + i) % 2 == 0) {
 			p = darkPaint;
 		} else {
 			p = lightPaint;
 		}
 
 		canvas.drawRect(rect, p);
 
 		/* Draw chess piece image */
 		ChessPiece cp = null;
 		cp = board.getPieceAt(i, j);
 		if (cp != null) {
 			Bitmap map = scaledBitmaps.get(cp);
 			if (map != null) {
 				canvas.drawBitmap(map, null, rect, p);
 			}
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		boolean result = detector.onTouchEvent(event);
 		return result;
 	}
 
 	/**
 	 * Allow the user to flip the screen from white at the top to white at the
 	 * bottom
 	 */
 	public void flip() {
 		if (inverted) {
 			inverted = false;
 		} else {
 			inverted = true;
 		}
 		invalidate();
 	}
 	
 	/**
 	 * @return a GameResult if the game is over, null otherwise.
 	 */
 	public Board.GameResult getResult() {
 		return chessRuleStrategy.getResults(board);
 	}
 
 	/**
 	 * Don't allow selections anymore
 	 */
 	public void disableSelection() {
 		selectionManager.disableSelection();
 	}
 
 }
