 package charland.games.go;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 /**
  * Handles drawing the game.
  * 
  * @author Michael
  * 
  */
 public class PuzzleView extends View {
     private final Game game;
 
     private int selX; // X index of selection
     private int selY; // Y index of selection
     private final Rect selRect = new Rect();
     private float top;
 
     private float left;
 
     private float bottom;
 
     private float right;
 
     private float cellWidth;
 
     private float width;
 
     public PuzzleView(Context context) {
         super(context);
         this.game = (Game) context;
         setFocusable(true);
         setFocusableInTouchMode(true);
     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 
         width = Math.min(w, h);
         cellWidth = width / (float) (Board.SIZE + 1);
 
         left = cellWidth;
         top = cellWidth;
         right = Board.SIZE * cellWidth;
         bottom = Board.SIZE * cellWidth;
 
         getRedrawRect(selX, selY, selRect);
         super.onSizeChanged(w, h, oldw, oldh);
     }
 
     /**
      * This will set the rectangle to redraw.
      * 
      * @param x
      *            The starting x.
      * @param y
      *            The starting y.
      * @param rect
      *            The rectangle to set.
      */
     private void getRedrawRect(int x, int y, Rect rect) {
         int left = (int) (this.left / 2 + x * cellWidth);
         int top = (int) (this.top / 2 + y * cellWidth);
         int right = (int) (this.left / 2 + x * cellWidth + cellWidth);
         int bottom = (int) (this.top / 2 + y * cellWidth + cellWidth);
 
         rect.set(left, top, right, bottom);
     }
 
     /**
      * Draws everything!!
      */
     @Override
     protected void onDraw(Canvas canvas) {
         drawTheBackground(canvas);
         drawTheBoard(canvas);
         drawTheStones(canvas);
         drawTheStats(canvas);
     }
 
     /**
      * Draw the background...
      * 
      * @param canvas
      *            What to draw on.
      */
     private void drawTheBackground(Canvas canvas) {
         Paint background = new Paint();
         background.setColor(getResources().getColor(R.color.puzzle_background));
         canvas.drawRect(0, 0, width, width, background);
     }
 
     /**
      * Draws the stones on the board.
      * 
      * @param canvas
      *            What to draw on.
      */
     private void drawTheStones(Canvas canvas) {
         Paint black = new Paint();
         black.setColor(getResources().getColor(R.color.black_stone));
 
         Paint white = new Paint();
         white.setColor(getResources().getColor(R.color.white_stone));
 
         short[][] board = game.getGameBoard();
         for (int x = 0; x < board.length; x++) {
             for (int y = 0; y < board.length; y++) {
                if (board[x][y] > 0) {
                     Paint colour = white;
                    if (board[x][y] == 1) {
                         colour = black;
                     }
                     canvas.drawCircle(left + x * cellWidth, top + y * cellWidth, cellWidth / 2, colour);
                 }
             }
         }
     }
 
     /**
      * Draw the board!
      * 
      * @param canvas
      *            What to draw on.
      */
     private void drawTheBoard(Canvas canvas) {
         // Draw the board...
         // Define colors for the grid lines
         Paint dark = new Paint();
         dark.setColor(getResources().getColor(R.color.puzzle_dark));
 
         // Draw the grid lines
         // Log.d(TAG, "GAME BOARD: W=" + getWidth() + " H=" + getHeight());
         for (int i = 1; i < Board.SIZE + 1; i++) {
 
             // Horizontal
             float y = i * cellWidth;
             canvas.drawLine(left, y, right, y, dark);
 
             // Log.d(Go.TAG, "Horizontal Line y=" + y + " x from " + left + " to " + right);
 
             // Vertical
             float x = i * cellWidth;
             canvas.drawLine(x, top, x, bottom, dark);
 
             // Draw the 9 circles in the middle of the board.
             if (i > 1 && (i + 1) % 2 == 0 && i < Board.SIZE) {
                 for (int across = 0; across < 3; ++across) {
                     // int across = 0;
                     canvas.drawCircle(left * 3 + across * cellWidth * 2, y, 5, dark);
                 }
             }
         }
     }
 
     /**
      * Draw the statistics.
      * 
      * @param canvas
      *            What to draw on.
      */
     private void drawTheStats(Canvas canvas) {
         
         Paint light = new Paint();
         light.setColor(getResources().getColor(R.color.puzzle_hilite));
         int x = 0;
         int y = (int) ((Board.SIZE + 2) * cellWidth);
         if(getWidth() > getHeight()) {
             x = y;
             y = 50;
         }
         canvas.drawText("Black Stones Captured: " + game.getWhiteStonesCaptured(), x, y, light);
         canvas.drawText("White Stones Captured: " + game.getBlackStonesCaptured(), x, y + 15, light);
 
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_DOWN)
             return super.onTouchEvent(event);
         select((int) ((event.getX() - cellWidth / 2) / cellWidth), (int) ((event.getY() - cellWidth / 2) / cellWidth));
         Log.d(Go.TAG, "onTouchEvent: x " + selX + ", y " + selY);
         if (game.playTurn(selX, selY)) {
             Toast toast = Toast.makeText(game, R.string.no_moves_label, Toast.LENGTH_SHORT);
             toast.setGravity(Gravity.CENTER, 0, 0);
             toast.show();
         }
         return true;
     }
 
     /**
      * Select the spot to redraw.
      * 
      * @param x
      *            The x location.
      * @param y
      *            The y location.
      */
     private void select(int x, int y) {
         invalidate(selRect);
         selX = Math.min(Math.max(x, 0), Board.SIZE - 1);
         selY = Math.min(Math.max(y, 0), Board.SIZE - 1);
         getRedrawRect(selX, selY, selRect);
         invalidate(selRect);
     }
 }
