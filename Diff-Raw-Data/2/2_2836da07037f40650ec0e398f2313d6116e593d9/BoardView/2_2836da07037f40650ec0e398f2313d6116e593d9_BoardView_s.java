 package com.stevekb.connectfour;
 
 import com.stevekb.connectfour.Board.Player;
 import com.stevekb.connectfour.Board.WinType;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class BoardView extends SurfaceView implements SurfaceHolder.Callback {
 
 	// ID Elements
 	private TextView gameOverText;
 	private LinearLayout gameOverLayout;
 
 	// Drawing info
 	private float BOARD_MARGIN = 25;
 	private RectF boardRect;
 	private int previewSpot = -1;
 	private int maxX, maxY;
 
 	// Game Logic
 	private boolean gameInProgress = true;
 	private Player onPlayer = Player.RED;
 	private WinType win;
 	private int winX = -1, winY = -1;
 
 	// Paint Settings
 	private static final int BOARD_COLOR = Color.YELLOW;
 	private static final int BG_COLOR = Color.rgb(232, 232, 232);
 	private static final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private static final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private static final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private static final Paint playerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 
 	// Board Storage
 	private Board myBoard = new Board();
 
 	public BoardView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		// Set colors
 		this.setBackgroundColor(BG_COLOR);
 		boardPaint.setColor(BOARD_COLOR);
 		bgPaint.setColor(BG_COLOR);
 		borderPaint.setColor(Color.BLACK);
 
 		getHolder().addCallback(this);
 		setFocusable(true);
 	}
 
 	@Override
 	protected void onDraw(Canvas c) {
 		super.onDraw(c);
 
 		DrawBoard(c);
 	}
 
 	private void DrawBoard(Canvas c) {
 
 		boardRect = new RectF(maxX * 0.05f, maxY * 0.1f, maxX * 0.95f,
 				maxY * 0.95f);
 		RectF borderRect = new RectF(maxX * 0.05f - 1, maxY * 0.1f - 1,
 				maxX * 0.95f + 1, maxY * 0.95f + 1);
 
 		if (previewSpot != -1)
 			ShowPreviewSpot(c);
 
 		// Draw board
 		c.drawRoundRect(borderRect, 20f, 20f, borderPaint);
 		c.drawRoundRect(boardRect, 20f, 20f, boardPaint);
 
 		DrillHoles(c);
 
 		if (winX != -1)
 			ShowWinLine(c);
 	}
 
 	private void ShowPreviewSpot(Canvas c) {
 		float deltaX = (boardRect.right - boardRect.left - BOARD_MARGIN) / 7;
 		float initial = boardRect.left + ((deltaX + BOARD_MARGIN) / 2);
 		float pieceRadius = deltaX * 0.425f;
 
 		if (onPlayer == Player.RED)
 			playerPaint.setColor(Color.RED);
 		else
 			playerPaint.setColor(Color.BLACK);
 
 		c.drawCircle(initial + deltaX * previewSpot, boardRect.top
 				- pieceRadius / 2, pieceRadius + 1, borderPaint);
 		c.drawCircle(initial + deltaX * previewSpot, boardRect.top
 				- pieceRadius / 2, pieceRadius, playerPaint);
 	}
 
 	private void DrillHoles(Canvas c) {
 
 		float deltaX = (boardRect.right - boardRect.left - BOARD_MARGIN) / 7;
 		float deltaY = (boardRect.bottom - boardRect.top - BOARD_MARGIN) / 7;
 
 		int xSpot = 0, ySpot = 6;
 		for (float yPos = boardRect.top + ((deltaY + BOARD_MARGIN) / 2); ySpot >= 0; yPos += deltaY, ySpot--) {
 			for (float xPos = boardRect.left + ((deltaX + BOARD_MARGIN) / 2); xSpot < 7; xPos += deltaX, xSpot++) {
 
 				if (myBoard.spots[ySpot][xSpot] == Player.RED)
 					playerPaint.setColor(Color.RED);
 				else if (myBoard.spots[ySpot][xSpot] == Player.BLACK)
 					playerPaint.setColor(Color.BLACK);
 				else
 					playerPaint.setColor(BG_COLOR);
 
 				float pieceRadius = deltaX * 0.425f;
 
 				c.drawCircle(xPos, yPos, pieceRadius + 1, borderPaint);
 				c.drawCircle(xPos, yPos, pieceRadius, playerPaint);
 			}
 
 			xSpot = 0;
 		}
 	}
 
 	private void ShowWinLine(Canvas c) {
 
 		float deltaX = (boardRect.right - boardRect.left - BOARD_MARGIN) / 7;
 		float deltaY = (boardRect.bottom - boardRect.top - BOARD_MARGIN) / 7;
 		float winXStart = boardRect.left + ((deltaX + BOARD_MARGIN) / 2)
 				+ deltaX * winX;
 		float winYStart = boardRect.top + ((deltaY + BOARD_MARGIN) / 2)
 				+ deltaY * (6 - winY);
 		float winXEnd;
 		float winYEnd;
 
 		switch (win) {
 		case HORIZONTAL:
 			winXEnd = winXStart + deltaX * 3;
 			winYEnd = winYStart;
 			break;
 		case VERTICAL:
 			winXEnd = winXStart;
 			winYEnd = winYStart - deltaY * 3;
 			break;
 		case DIAG_POS:
 			winXEnd = winXStart + deltaX * 3;
 			winYEnd = winYStart - deltaY * 3;
 			break;
 		default:
 			winXEnd = winXStart - deltaX * 3;
 			winYEnd = winYStart - deltaY * 3;
 			break;
 		}
 
 		Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 
 		linePaint.setColor(Color.BLACK);
 		linePaint.setStrokeWidth(21f);
 		c.drawLine(winXStart, winYStart, winXEnd, winYEnd, linePaint);
 
 		linePaint.setColor(Color.GREEN);
 		linePaint.setStrokeWidth(20f);
 		c.drawLine(winXStart, winYStart, winXEnd, winYEnd, linePaint);
 
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (gameInProgress) {
 			if (event.getAction() == MotionEvent.ACTION_DOWN
 					|| event.getAction() == MotionEvent.ACTION_MOVE) {
 
 				previewSpot = touchToSpot(event.getX());
 
 				invalidate();
 
 				return true;
 			} else if (event.getAction() == MotionEvent.ACTION_UP) {
 
 				previewSpot = -1;
 				int toDrop = touchToSpot(event.getX());
 
 				for (int i = 0; i < 7; i++) {
 					if (myBoard.spots[i][toDrop] == Player.BLANK) {
 						myBoard.spots[i][toDrop] = onPlayer;
 
 						onPlayer = (onPlayer == Player.BLACK) ? Player.RED
 								: Player.BLACK;
 
 						break;
 					}
 				}
 
 				invalidate();
 
 				Player gameResult = CheckGameState();
 				if (gameResult != Player.BLANK)
 					GameOver(gameResult);
 
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private Player CheckGameState() {
 
 		// Check horizontal
 		for (int i = 0; i < 7; i++)
 			for (int j = 0; j < 4; j++)
 				if (myBoard.spots[i][j] != Player.BLANK)
 					if (myBoard.spots[i][j] == myBoard.spots[i][j + 1]
 							&& myBoard.spots[i][j] == myBoard.spots[i][j + 2]
 							&& myBoard.spots[i][j] == myBoard.spots[i][j + 3]
 							&& myBoard.spots[i][j] != Player.BLANK) {
 
 						winX = j;
 						winY = i;
 						win = WinType.HORIZONTAL;
 						gameInProgress = false;
 						return myBoard.spots[i][j];
 					}
 
 		// Check Vertical
 		for (int i = 0; i < 4; i++)
 			for (int j = 0; j < 7; j++)
 				if (myBoard.spots[i][j] != Player.BLANK)
 					if (myBoard.spots[i][j] == myBoard.spots[i + 1][j]
 							&& myBoard.spots[i][j] == myBoard.spots[i + 2][j]
 							&& myBoard.spots[i][j] == myBoard.spots[i + 3][j]
 							&& myBoard.spots[i][j] != Player.BLANK) {
 
 						winX = j;
 						winY = i;
 						win = WinType.VERTICAL;
 						gameInProgress = false;
 						return myBoard.spots[i][j];
 					}
 
 		// Check Positive Slope
 		for (int i = 0; i < 4; i++)
 			for (int j = 0; j < 4; j++)
 				if (myBoard.spots[i][j] != Player.BLANK)
 					if (myBoard.spots[i][j] == myBoard.spots[i + 1][j + 1]
 							&& myBoard.spots[i][j] == myBoard.spots[i + 2][j + 2]
 							&& myBoard.spots[i][j] == myBoard.spots[i + 3][j + 3]
 							&& myBoard.spots[i][j] != Player.BLANK) {
 
 						winX = j;
 						winY = i;
 						win = WinType.DIAG_POS;
 						gameInProgress = false;
 						return myBoard.spots[i][j];
 					}
 
 		// Check Negative Slope
 		for (int i = 0; i < 4; i++)
 			for (int j = 3; j < 7; j++)
 				if (myBoard.spots[i][j] != Player.BLANK)
 					if (myBoard.spots[i][j] == myBoard.spots[i + 1][j - 1]
 							&& myBoard.spots[i][j] == myBoard.spots[i + 2][j - 2]
 							&& myBoard.spots[i][j] == myBoard.spots[i + 3][j - 3]) {
 
 						winX = j;
 						winY = i;
 						win = WinType.DIAG_NEG;
 						gameInProgress = false;
 						return myBoard.spots[i][j];
 					}
 
 		// Check for Full Board
 		boolean oneBlank = false;
 		for (int i = 0; i < 7; i++)
 			for (int j = 0; j < 7; j++)
 				if (myBoard.spots[i][j] == Player.BLANK) {
 					oneBlank = true;
 				}
 		if (!oneBlank) {
 			gameInProgress = false;
 			return Player.TIE;
 		}
 
 		return Player.BLANK;
 	}
 
 	private void GameOver(Player p) {
 		if (p == Player.RED)
 			gameOverText.setText("Red Wins!");
 		else if (p == Player.BLACK)
 			gameOverText.setText("Black Wins!");
 		else
 			gameOverText.setText("It's a Tie!");
 
 		gameOverLayout.setVisibility(View.VISIBLE);
 	}
 
 	public void setGameOverText(TextView t) {
 		gameOverText = t;
 	}
 
 	public void setGameOverLayout(LinearLayout l) {
 		gameOverLayout = l;
 	}
 
 	private int touchToSpot(float x) {
 		float deltaX = (boardRect.right - boardRect.left - BOARD_MARGIN) / 7;
 		float initial = boardRect.left + BOARD_MARGIN / 2;
 
 		if (x < initial + (deltaX * 1))
 			return 0;
 		else if (x < initial + (deltaX * 2))
 			return 1;
 		else if (x < initial + (deltaX * 3))
 			return 2;
 		else if (x < initial + (deltaX * 4))
 			return 3;
 		else if (x < initial + (deltaX * 5))
 			return 4;
 		else if (x < initial + (deltaX * 6))
 			return 5;
 		else
 			return 6;
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		maxX = width;
 		maxY = height;
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		maxX = getWidth();
 		maxY = getHeight();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 
 	}
 }
