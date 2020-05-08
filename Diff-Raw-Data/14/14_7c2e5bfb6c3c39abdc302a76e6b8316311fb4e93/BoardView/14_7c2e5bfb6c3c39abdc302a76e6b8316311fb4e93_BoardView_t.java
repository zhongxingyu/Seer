 /**
  * 
  */
 
 package phoenix.game.battleship.views;
 
 import phoenix.game.battleship.actors.Board;
 import phoenix.game.battleship.actors.EntityConstants;
 import phoenix.game.battleship.actors.Game;
 import phoenix.game.battleship.actors.ships.Ship;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.LinearLayout.LayoutParams;
 
 /**
  * @author pushpan
  */
 public class BoardView extends View implements OnTouchListener {
 
     private Game game;
 
     private Ship selectedShip;
 
     private boolean inHanger;
 
     private boolean owner;
 
     private OnShipPlacedListener onShipPlacedListener;
 
     public Game getGame() {
         return game;
     }
 
     public void setGame(Game game) {
         this.game = game;
     }
 
     private Board board;
 
     public BoardView(Context context) {
         super(context);
     }
 
     public BoardView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     public void initGame(Game game) {
         setGame(game);
         setOnTouchListener(this);
         updateWidth();
        setEnabled(false);
     }
 
     public void updateWidth() {
         LayoutParams params = new LayoutParams(getGame().getTileSize() * EntityConstants.SIZE,
                 getGame().getTileSize() * EntityConstants.SIZE);
         setLayoutParams(params);
     }
 
     public void onResume() {
         refresh();
     }
 
     public void onPause() {
         refresh();
 
     }
 
     public void refresh() {
         invalidate();
 
     }
 
     public void onDraw(Canvas canvas) {
         if (getBoard() != null) {
             getBoard().paint(canvas);
         }
     }
 
     @Override
     public boolean onTouch(View view, MotionEvent event) {
         if (isEnabled()) {
             if (event.getAction() == MotionEvent.ACTION_DOWN) {
                 Ship onBoardSelectedShip = hasTouchedAShip(event.getX(), event.getY());
                 int row = getGame().getColumn(event.getY());
                 int col = getGame().getColumn(event.getX());
                 if (isOwner()) {
                     // for the owner the board will only execute touch event if
                     // it is on setup level
                     if (onBoardSelectedShip != null) {
                         setSelectedShip(onBoardSelectedShip);
                         setInHanger(false);
                     } else if (isInSelectionMode()) {
                         if (getBoard().checkIfShipFitsInBoard(getSelectedShip(), row, col)) {
                             // setSelectedShip(null);
                             getBoard().setPositionForSelectedShip(getSelectedShip(), row, col);
                             getBoard().updateOrAddSelectedShip(getSelectedShip());
                             if (isInHanger()) {
                                 setInHanger(false);
                                 if (getOnShipPlacedListener() != null) {
                                     getOnShipPlacedListener().onShipPlaced();
                                 }
                             }
                         }
                     }
                 } else {
                     // for the non-owner the board will only execute touch
                     // event if it is on playing level
                     boolean shouldRepeatChance = false;
                     if (onBoardSelectedShip != null) {
                         shouldRepeatChance = onBoardSelectedShip.updateIfHit(row, col);
                     }
                     getBoard().markDamage(row, col);
                     if (getBoard().checkIfAllShipsHasSank()) {
                         getGame().showGameOver();
                         updateGame();
                         return false;
                     }
                     getGame().nextTurn(shouldRepeatChance);
                     return false;
                 }
                 updateGame();
             }
         }
         return false;
     }
 
     private Ship hasTouchedAShip(float x, float y) {
         return getBoard().checkIfSelected(x, y);
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         refresh();
         return super.onKeyUp(keyCode, event);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         refresh();
         return super.onKeyDown(keyCode, event);
     }
 
     public Board getBoard() {
         return board;
     }
 
     public void setBoard(Board board, boolean owner) {
         this.board = board;
         getBoard().setOwner(owner);
         this.owner = owner;
     }
 
     public boolean isInSelectionMode() {
         return (getSelectedShip() != null);
     }
 
     public Ship getSelectedShip() {
         return selectedShip;
     }
 
     public void setSelectedShip(Ship selectedShip) {
         this.selectedShip = selectedShip;
     }
 
     public OnShipPlacedListener getOnShipPlacedListener() {
         return onShipPlacedListener;
     }
 
     public void setOnShipPlacedListener(OnShipPlacedListener onShipPlacedListener) {
         this.onShipPlacedListener = onShipPlacedListener;
     }
 
     public boolean isInHanger() {
         return inHanger;
     }
 
     public void setInHanger(boolean inHanger) {
         this.inHanger = inHanger;
     }
 
     public void attemptToRotateShipIfPossible() {
         if (getSelectedShip() != null) {
             getBoard().attemptToRotateShipIfPossible(getSelectedShip());
             updateGame();
         }
     }
 
     public boolean isOwner() {
         return owner;
     }
 
     public void updateGame() {
         getGame().updateGame();
     }
 }
