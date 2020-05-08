 package com.example.tetris;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 
 import com.example.ccweibo.R;
 
 /**
  * Tetris will be represented by a 18 * 10 color
  * 
  * @author flamearrow
  * 
  */
 public class TetrisView extends SurfaceView implements Callback {
 	private Random rand = new Random();
 	private static final int MATRIX_HEIGHT = 24;
 	private static final int MATRIX_WIDTH = 12;
 	private static final int PREVIEW_EDGE = 4;
 	private static final int SQUARE_EDGE_WIDTH = 2;
 	private static final int SQUARE_EDGE_COLOR = Color.YELLOW;
 	private static final int SEPARATOR_COLOR = Color.GRAY;
 	private static final double GOLDEN_RATIO = 0.618;
 	private static final int INITIAL_BLOCK_COLOR = Color.DKGRAY;
 
 	private TetrisThread _thread;
 
 	// Android Color are all int!
 	private int[][] _gameMatrix;
 	private int[][] _previewMatrix;
 
 	private int _screenWidth;
 	private int _screenHeight;
 
 	// Paint object si used to draw stuff
 	private Paint _backgroundPaint;
 	private Paint _gameMatrixPaint;
 	private Paint _previewMatrixPaint;
 	private Paint _separatorPaint;
 	private Paint _staticPaint;
 
 	private int _level;
 	private int _score;
 	private int _currentHeight;
 
 	private boolean _justStart;
 
 	private Block _currentBlock;
 	private Block _nextBlock;
	private volatile Set<Point> _currentBlockPoints;
 
 	private int getRandomColor() {
 		switch (rand.nextInt(7)) {
 		case 0:
 			return Color.BLACK;
 		case 1:
 			return Color.RED;
 		case 2:
 			return Color.GREEN;
 		case 3:
 			return Color.BLUE;
 		case 4:
 			return Color.YELLOW;
 		case 5:
 			return Color.CYAN;
 		case 6:
 			return Color.MAGENTA;
 		default:
 			return Color.WHITE;
 		}
 	}
 
 	public Block.Direction getRandomDirection() {
 		switch (rand.nextInt(4)) {
 		case 0:
 			return Block.Direction.Down;
 		case 1:
 			return Block.Direction.Up;
 		case 2:
 			return Block.Direction.Left;
 		case 3:
 			return Block.Direction.Right;
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * @return a block with random value and color
 	 */
 	private Block getRandomBlock() {
 		switch (rand.nextInt(7)) {
 		case 0:
 			return new Block(Block.Value.I, getRandomColor(),
 					getRandomDirection());
 		case 1:
 			return new Block(Block.Value.L, getRandomColor(),
 					getRandomDirection());
 		case 2:
 			return new Block(Block.Value.O, getRandomColor(),
 					getRandomDirection());
 		case 3:
 			return new Block(Block.Value.rL, getRandomColor(),
 					getRandomDirection());
 		case 4:
 			return new Block(Block.Value.rS, getRandomColor(),
 					getRandomDirection());
 		case 5:
 			return new Block(Block.Value.S, getRandomColor(),
 					getRandomDirection());
 		case 6:
 			return new Block(Block.Value.T, getRandomColor(),
 					getRandomDirection());
 		default:
 			return null;
 		}
 	}
 
 	// Note: customized View needs to implement this two param constructor
 	public TetrisView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		// this call is ensuring surfaceCreated() method will be called
 		getHolder().addCallback(this);
 
 		_gameMatrix = new int[MATRIX_HEIGHT][MATRIX_WIDTH];
 		_previewMatrix = new int[PREVIEW_EDGE][PREVIEW_EDGE];
 		_backgroundPaint = new Paint();
 		_gameMatrixPaint = new Paint();
 		_previewMatrixPaint = new Paint();
 		_separatorPaint = new Paint();
 		_staticPaint = new Paint();
 		_currentBlockPoints = new HashSet<Point>();
 	}
 
 	// pause the game, we want to save game state after returning to game
 	public void pause() {
 		surfaceDestroyed(null);
 	}
 
 	public void releaseResources() {
 
 	}
 
 	// rotate the current active block
 	public void rotate() {
 
 	}
 
 	private void updateComponents() {
 		// if it's just started, we don't want to update matrixs as they are
 		// already updated
 		if (_justStart) {
 			_justStart = false;
 			return;
 		}
 
 		boolean addAnotherBlock = updateGameMatrix();
 		if (addAnotherBlock) {
 			addBlockToMatrix(_gameMatrix, MATRIX_HEIGHT - 1, 4, _nextBlock);
 			updatePreviewMatrix();
 		}
 		updateTimer();
 		updateScoreBoard();
 	}
 
 	/**
 	 * First dropping the floating block one line if applicable
 	 * 
 	 * then remove all full lines
 	 * 
 	 * @return whether we need to add another block to the game
 	 */
 	private boolean updateGameMatrix() {
 		// first move the floating block one line down
 		boolean canDropOneLine = true;
 		Point tmpPoint = new Point();
 		for (Point p : _currentBlockPoints) {
 			tmpPoint.set(p.x - 1, p.y);
			// TODO: _currentBlockPoints isn't working correctly, contention?
 			if (_currentBlockPoints.contains(tmpPoint)) {
 				continue;
 			}
 			// if we hit bottom or the lower block is already set then we can't
 			// continue dropping
			if ((p.x < 0)
 					|| (_gameMatrix[tmpPoint.x][tmpPoint.y] != INITIAL_BLOCK_COLOR)) {
 				canDropOneLine = false;
 				break;
 			}
 		}
 
 		if (canDropOneLine) {
 			for (Point p : _currentBlockPoints) {
 				_gameMatrix[p.x][p.y] = INITIAL_BLOCK_COLOR;
 				p.offset(-1, 0);
 			}
 			for (Point p : _currentBlockPoints) {
 				_gameMatrix[p.x][p.y] = _currentBlock.color;
 			}
 			// TODO: update _currentHeight
 		}
 
 		// then check if we need to remove lines from bottom to _currentHeight
 		int currentRow = 0;
 		int currentBottom = Integer.MAX_VALUE;
 		while (currentRow < _currentHeight) {
 			for (int i = 0; i < MATRIX_WIDTH; i++) {
 				if (_gameMatrix[currentRow][i] == INITIAL_BLOCK_COLOR) {
 					// if this line can't be removed, we either skip it or drop
 					// it to currentBottom
 
 					// currentBottom is not empty, need to drop
 					if (currentBottom != Integer.MAX_VALUE) {
 						moveRow(_gameMatrix, currentRow, currentBottom);
 						currentBottom = currentRow;
 					}
 					currentRow++;
 					break;
 				}
 			}
 			// if we can reach here then currentRow needs to be removed
 			if (currentBottom > currentRow)
 				currentBottom = currentRow;
 			// clear the row
 			clearRow(_gameMatrix, currentRow);
 			currentRow++;
 		}
 
 		return !canDropOneLine;
 	}
 
 	// move the currentRow to the currentBottom row of the matrix, clear
 	// currentRow
 	private void moveRow(int[][] matrix, int currentRow, int currentBottom) {
 		for (int i = 0; i < MATRIX_WIDTH; i++) {
 			matrix[currentBottom][i] = matrix[currentRow][i];
 			matrix[currentRow][i] = INITIAL_BLOCK_COLOR;
 		}
 	}
 
 	private void clearRow(int[][] matrix, int rowToClear) {
 		for (int i = 0; i < MATRIX_WIDTH; i++) {
 			matrix[rowToClear][i] = INITIAL_BLOCK_COLOR;
 		}
 	}
 
 	private void updatePreviewMatrix() {
 		for (int i = 0; i < PREVIEW_EDGE; i++)
 			for (int j = 0; j < PREVIEW_EDGE; j++)
 				_previewMatrix[i][j] = INITIAL_BLOCK_COLOR;
 		_nextBlock = getRandomBlock();
 		addBlockToMatrix(_previewMatrix, PREVIEW_EDGE - 1, 0, _nextBlock);
 	}
 
 	/**
 	 * add a new block to specified color matrix, if there's no space for the
 	 * given block, return false
 	 * 
 	 * @param colorMatrix
 	 * @param x
 	 * @param y
 	 * @param newBlock
 	 * @return whether the new block successfully added
 	 */
 	private boolean addBlockToMatrix(int[][] colorMatrix, int x, int y,
 			Block newBlock) {
 		// with hex value defined, just apply it to a 4*4 matrix
 		int count = 0;
 		int value = newBlock.value.gethexV();
 		int color = newBlock.color;
 		int[][] tmpMatrix = new int[4][4];
 		for (int i = 0; i < 4; i++) {
 			for (int j = 0; j < 4; j++) {
 				if (((value >> (count++)) & 1) > 0) {
 					tmpMatrix[i][j] = color;
 				}
 			}
 		}
 		// rotate to the correct direction
 		switch (newBlock.direction) {
 		case Right:
 			rotateMatrix(tmpMatrix, 1);
 			break;
 		case Down:
 			rotateMatrix(tmpMatrix, 2);
 			break;
 		case Left:
 			rotateMatrix(tmpMatrix, 3);
 			break;
 		case Up:
 		default:
 			break;
 		}
 
 		// apply the tmp matrix to colorMatrix
 		for (int i = 0; i < 4; i++) {
 			for (int j = 0; j < 4; j++) {
 				// we find a collision, game over
 				if (colorMatrix[x - i][y + j] != INITIAL_BLOCK_COLOR) {
 					return false;
 				} else if (tmpMatrix[i][j] != 0) {
 					colorMatrix[x - i][y + j] = tmpMatrix[i][j];
 				}
 
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Rotate a 4x4 matrix Rotate sequence clockwisely for n times
 	 * 
 	 * @param matrix
 	 * @param times
 	 */
 	private void rotateMatrix(int[][] matrix, int times) {
 		for (int loop = 0; loop <= times; loop++) {
 			int tmp = -1;
 			for (int i = 0; i < 4; i++) {
 				for (int j = i; j < 4; j++) {
 					tmp = matrix[i][j];
 					matrix[i][j] = matrix[j][i];
 					matrix[j][i] = tmp;
 				}
 			}
 			for (int i = 0; i < 2; i++) {
 				for (int j = 0; j < 4; j++) {
 					tmp = matrix[i][j];
 					matrix[i][j] = matrix[3 - i][j];
 					matrix[3 - i][j] = tmp;
 				}
 			}
 		}
 	}
 
 	private void updateTimer() {
 
 	}
 
 	private void updateScoreBoard() {
 
 	}
 
 	private void drawComponents(Canvas canvas) {
 		_backgroundPaint.setColor(Color.WHITE);
 		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
 				_backgroundPaint);
 
 		// a squre's edge length, should accommodate with width
 		// the total width is shared by gameSection width, previewSection with
 		// and a separator
 		int blockEdge = _screenWidth / (MATRIX_WIDTH + PREVIEW_EDGE + 1);
 
 		// first draw the game secion
 
 		// starting from bottom left, draw a MATRIX_HEIGHT x MATRIX_WIDTH matrix
 		// each block has a black frame and filled with color at
 		// _colorMatrix[i][j]
 		Point currentPoint = new Point(0, _screenHeight);
 		// for (int i = MATRIX_HEIGHT - 1; i >= 0; i--) {
 		for (int i = 0; i < MATRIX_HEIGHT - 1; i++) {
 			for (int j = 0; j < MATRIX_WIDTH; j++) {
 				// draw edge
 				_gameMatrixPaint.setColor(SQUARE_EDGE_COLOR);
 				canvas.drawRect(currentPoint.x, currentPoint.y - blockEdge,
 						currentPoint.x + blockEdge, currentPoint.y,
 						_gameMatrixPaint);
 				// draw squre
 				_gameMatrixPaint.setColor(_gameMatrix[i][j]);
 				canvas.drawRect(currentPoint.x + SQUARE_EDGE_WIDTH,
 						currentPoint.y - blockEdge + SQUARE_EDGE_WIDTH,
 						currentPoint.x + blockEdge - SQUARE_EDGE_WIDTH,
 						currentPoint.y - SQUARE_EDGE_WIDTH, _gameMatrixPaint);
 				currentPoint.offset(blockEdge, 0);
 			}
 			// move to the start of next line
 			currentPoint.offset(-MATRIX_WIDTH * blockEdge, -blockEdge);
 		}
 
 		// then draw the left-right separator
 		currentPoint.set(MATRIX_WIDTH * blockEdge + blockEdge / 2, 0);
 		_separatorPaint.setColor(SEPARATOR_COLOR);
 		_separatorPaint.setStrokeWidth(blockEdge / 2);
 		canvas.drawLine(currentPoint.x, 0, currentPoint.x, _screenHeight,
 				_separatorPaint);
 
 		// then draw the top-bottom separator, should separate it in golden
 		// ratio
 		currentPoint.offset(0, (int) (_screenHeight * (1 - GOLDEN_RATIO)));
 		canvas.drawLine(currentPoint.x, currentPoint.y, _screenWidth,
 				currentPoint.y, _separatorPaint);
 
 		// then draw the preview section, should be in center of top part
 		currentPoint.set(currentPoint.x + blockEdge / 2, currentPoint.y / 2);
 		currentPoint.offset(0, -PREVIEW_EDGE / 2 * blockEdge);
 		for (int i = 0; i < PREVIEW_EDGE; i++) {
 			for (int j = 0; j < PREVIEW_EDGE; j++) {
 				// draw edge
 				_previewMatrixPaint.setColor(SQUARE_EDGE_COLOR);
 				canvas.drawRect(currentPoint.x, currentPoint.y - blockEdge,
 						currentPoint.x + blockEdge, currentPoint.y,
 						_previewMatrixPaint);
 				// draw squre
 				_previewMatrixPaint.setColor(_previewMatrix[i][j]);
 				canvas.drawRect(currentPoint.x + SQUARE_EDGE_WIDTH,
 						currentPoint.y - blockEdge + SQUARE_EDGE_WIDTH,
 						currentPoint.x + blockEdge - SQUARE_EDGE_WIDTH,
 						currentPoint.y - SQUARE_EDGE_WIDTH, _previewMatrixPaint);
 				currentPoint.offset(blockEdge, 0);
 			}
 			// move to the start of next line
 			currentPoint.offset(-PREVIEW_EDGE * blockEdge, blockEdge);
 		}
 
 		// then draw Level and score
 		// this needs to be exposed to update separately
 		currentPoint.set(currentPoint.x,
 				(int) (_screenHeight * (1 - GOLDEN_RATIO / 2)));
 		currentPoint.offset(blockEdge, -2 * blockEdge);
 		canvas.drawText(getResources().getString(R.string.level) + _level,
 				currentPoint.x, currentPoint.y, _staticPaint);
 		currentPoint.offset(0, 4 * blockEdge);
 		canvas.drawText(getResources().getString(R.string.score) + _score,
 				currentPoint.x, currentPoint.y, _staticPaint);
 	}
 
 	// called when first added to the View, used to record screen width and
 	// height
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		_screenHeight = h;
 		_screenWidth = w;
 
 	}
 
 	// move the current active block to left/right if according whether this tap
 	// happens at left/right half of the screen
 	public void moveLR(MotionEvent e) {
 
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		newGame(holder);
 	}
 
 	private void newGame(SurfaceHolder holder) {
 		initializeParams();
 		initializeMatrix();
 		_thread = new TetrisThread(holder);
 		_thread.setRunning(true);
 		_thread.start();
 	}
 
 	private void initializeParams() {
 		_level = 1;
 		_score = 0;
 		_currentHeight = 0;
 		_justStart = true;
 	}
 
 	// this is called when a new game is started, add a random block in
 	// _gameMatrix and a random block in _previewMatrix
 	private void initializeMatrix() {
 		for (int i = 0; i < MATRIX_HEIGHT; i++)
 			for (int j = 0; j < MATRIX_WIDTH; j++)
 				_gameMatrix[i][j] = INITIAL_BLOCK_COLOR;
 		for (int i = 0; i < PREVIEW_EDGE; i++)
 			for (int j = 0; j < PREVIEW_EDGE; j++)
 				_previewMatrix[i][j] = INITIAL_BLOCK_COLOR;
 
 		_currentBlock = getRandomBlock();
 		addBlockToMatrix(_gameMatrix, MATRIX_HEIGHT - 1, 4, _currentBlock);
 
 		// add four points to the current Blocks
 		for (int i = MATRIX_HEIGHT - 1; i > MATRIX_HEIGHT - 5; i--) {
 			for (int j = 0; j < MATRIX_WIDTH; j++) {
 				if (_gameMatrix[i][j] != INITIAL_BLOCK_COLOR) {
 					_currentBlockPoints.add(new Point(i, j));
 				}
 			}
 		}
 
 		_nextBlock = getRandomBlock();
 		addBlockToMatrix(_previewMatrix, PREVIEW_EDGE - 1, 0, _nextBlock);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 
 	}
 
 	// This is called when you exit the game, should stop the thread. Otherwise
 	// it will continue trying to draw on a Null canvas and cause NPE
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		_thread.setRunning(false);
 		boolean retry = true;
 		while (retry) {
 			try {
 				_thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private class TetrisThread extends Thread {
 		// _holder is used to retrieve Canvas object of the current SurfaceView
 		private SurfaceHolder _holder;
 		private boolean _running;
 
 		public TetrisThread(SurfaceHolder holder) {
 			_holder = holder;
 		}
 
 		public void setRunning(boolean running) {
 			_running = running;
 		}
 
 		@Override
 		public void run() {
 			Canvas canvas = null;
 			long previousFrameTime = System.currentTimeMillis();
 			while (_running) {
 				long currentFrameTime = System.currentTimeMillis();
 				long elapsed = currentFrameTime - previousFrameTime;
 
 				// we want to update the canvas every 100 milis
 				// just a simple speed controller
 				// to implement 'falling immediately' we need a fancier one
 				if (elapsed < 1000 - _level * 50) {
 					continue;
 				}
 				try {
 					canvas = _holder.lockCanvas(null);
 					synchronized (_holder) {
 						previousFrameTime = currentFrameTime;
 
 						updateComponents();
 						drawComponents(canvas);
 					}
 				} finally {
 					if (canvas != null) {
 						_holder.unlockCanvasAndPost(canvas);
 					}
 				}
 			}
 		}
 	}
 }
