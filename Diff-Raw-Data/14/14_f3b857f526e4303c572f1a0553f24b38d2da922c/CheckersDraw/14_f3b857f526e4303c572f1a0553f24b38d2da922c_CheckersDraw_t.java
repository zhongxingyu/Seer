 package edu.ycp.cs481.ycpgames;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 import java.util.List;
 
 /**
  * Created by michaelcoombs on 12/2/13.
  */
 public class CheckersDraw extends SurfaceView implements SurfaceHolder.Callback {
     private static final String TAG = "CheckersDraw";
     private Paint paintOne, paintTwo, circlePaint, selectPaint;
     private Toast invalidToast;
     private CheckersGame game;
     private CheckersGrid grid;
     private GameThread thread;
     private CheckersGridCell selectedPiece;
     private boolean selected = false;
     private AlertDialog gameOverAlert;
     private AlertDialog.Builder alertDialogBuilder;
 
 
     public CheckersDraw(Context context, int[] screen){
         super(context);
 
         getHolder().addCallback(this);
 
         game = new CheckersGame();
         grid = new CheckersGrid(screen);
         invalidToast = Toast.makeText(context,"Invalid move, please select another!",Toast.LENGTH_SHORT);
 
         paintOne = new Paint();
         paintOne.setColor(Color.parseColor("#108642"));
         paintOne.setStyle(Paint.Style.FILL);
         paintOne.setAntiAlias(true);
         paintOne.setTextSize(48);
         paintOne.setTypeface(Typeface.DEFAULT_BOLD);
        paintOne.setShadowLayer(3.2f, 3, 3, Color.DKGRAY);
         paintOne.setStrokeWidth(12);
 
         paintTwo = new Paint();
         paintTwo.setColor(Color.parseColor("#c0c0c0"));
         paintTwo.setAntiAlias(true);
         paintTwo.setTextSize(48);
         paintTwo.setTypeface(Typeface.DEFAULT_BOLD);
        paintTwo.setShadowLayer(3.2f,3,3,Color.BLACK);
         paintTwo.setStyle(Paint.Style.FILL);
         paintTwo.setStrokeWidth(12);
 
         circlePaint = new Paint();
         circlePaint.setColor(Color.YELLOW);
         circlePaint.setStyle(Paint.Style.FILL);
         circlePaint.setStrokeWidth(3);
         circlePaint.setAntiAlias(true);
 
         selectPaint = new Paint();
         selectPaint.setARGB(128, 218, 165, 32);
         selectPaint.setStyle(Paint.Style.FILL);
         selectPaint.setStrokeWidth(3);
 
         alertDialogBuilder = new AlertDialog.Builder(context);
         alertDialogBuilder.setTitle(R.string.game_over);
         alertDialogBuilder
                 .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         game.reset();
                         dialog.cancel();
                     }
                 })
                 .setNegativeButton("No", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         ((Activity)getContext()).finish();
                     }
                 });
 
 
     }
     @Override
     public void onDraw(Canvas canvas){
         canvas.drawRGB(255, 255, 255);
         Paint tempPaint;
         CheckersBoard tempBoard = game.getBoard();
         List<int[]> valid;
 
         for(int i = 0; i <grid.getGridWidth();i++){
             for (int j = 1; j < grid.getGridLength()-1;j++){
                 if((i%2 == 0)&&(j%2 !=0)||(j%2 == 0)&&(i%2 != 0)){
                     paintOne.setColor(Color.parseColor("#108642"));
                     tempPaint = paintOne;
                 }else{
                     paintTwo.setColor(Color.parseColor("#c0c0c0"));
                     tempPaint = paintTwo;
                 }
                 canvas.drawRect(grid.getCell(i,j).getX(),grid.getCell(i,j).getY(),grid.getCell(i,j).getX1(),grid.getCell(i,j).getY1(),tempPaint);
 
                 if(tempBoard.getPieceAt(i,j-1).getPlayer() == CheckersVal.PLAYER_ONE){
                     paintOne.setColor(Color.BLACK);
                     canvas.drawCircle(grid.getCell(i,j).getCenter()[0],grid.getCell(i,j).getCenter()[1],(int)(grid.getCell(i,j).getWidth()/2.1),paintOne);
                     if(tempBoard.getPieceAt(i,j-1).getRank() == CheckersVal.KING){
                         canvas.drawCircle(grid.getCell(i,j).getCenter()[0],grid.getCell(i,j).getCenter()[1],(int)(grid.getCell(i,j).getWidth()/3.1),circlePaint);
                     }
                 }else if (tempBoard.getPieceAt(i,j-1).getPlayer() == CheckersVal.PLAYER_TWO){
                     //paintTwo.setColor(Color.WHITE);
                     canvas.drawCircle(grid.getCell(i,j).getCenter()[0],grid.getCell(i,j).getCenter()[1],(int)(grid.getCell(i,j).getWidth()/2.1),paintTwo);
                     if(tempBoard.getPieceAt(i,j-1).getRank() == CheckersVal.KING){
                         canvas.drawCircle(grid.getCell(i,j).getCenter()[0],grid.getCell(i,j).getCenter()[1],(int)(grid.getCell(i,j).getWidth()/3.1),circlePaint);
                     }
                 }
             }
         }
         for(int i = 0; i < (12 - game.getBoard().getPlayerTwoPieces());i++){
             canvas.drawCircle(grid.getCell(0,0).getCenter()[0]+((int)(grid.getCell(0,0).getWidth()/1.1)*i),
                     grid.getCell(0,0).getCenter()[1],
                     (int)(grid.getCell(0,0).getWidth()/3.1),
                     paintTwo);
         }
         for(int i = 0; i < (12 - game.getBoard().getPlayerOnePieces());i++){
            paintOne.setColor(Color.BLACK);
             canvas.drawCircle(grid.getCell(0,9).getCenter()[0]+((int)(grid.getCell(0,9).getWidth()/1.1)*i),
                     grid.getCell(0,9).getCenter()[1],
                     (int)(grid.getCell(0,9).getWidth()/3.1),
                     paintOne);
         }
         if(selected){
             valid = mSelectPiece(selectedPiece);
             if(valid != null){
                 for (int[] temp : valid) {
                         canvas.drawCircle(grid.getCell(temp[0], temp[1] + 1).getCenter()[0],
                                 grid.getCell(temp[0], temp[1] + 1).getCenter()[1],
                                 (int) (grid.getCell(temp[0], temp[1]).getWidth() / 2.1),
                                 selectPaint);
                 }
             }
         }
     }
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         thread = new GameThread(getHolder(), this);
         thread.setRunning(true);
         thread.start();
     }
 
     public void setRunning(boolean run){
         thread.setRunning(run);
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 
     }
     private List<int[]> mSelectPiece(CheckersGridCell piece){
         List<int[]> valid = null;
         if(game.selectPiece(piece.getArrX(),piece.getArrY()-1)){
             Log.d(TAG,"Piece selected");
             valid = game.getValidMoves();
         }else{
             Log.d(TAG,"Piece selected not valid");
             selected = false;
         }
         return valid;
     }
     public CheckersGridCell mGetNearestPiece(int x, int y){
         CheckersGridCell temp = null;
         for(int i = 0; i < grid.getGridWidth();i++){
             for (int j = 1; j < grid.getGridLength()-1; j++) {
                 if (x > grid.getCell(i,j).getX() && x <= grid.getCell(i,j).getX1()){
                     if (y > grid.getCell(i,j).getY() && y <= grid.getCell(i,j).getY1()){
                         temp = grid.getCell(i,j);
                     }
                 }
             }
         }
         return temp;
     }
 
     public void mUpdateGrid(CheckersGridCell move){
         game.makeMove(move.getArrX(),move.getArrY()-1);
         selected = false;
         if(game.getBoard().isGameOver()>0){
             switch (game.getBoard().isGameOver()){
                 case 1:
                     if(Settings.getInstance().isSinglePlayer())
                         alertDialogBuilder.setMessage(R.string.player_win);
                     else
                         alertDialogBuilder.setMessage("Player 1 has won! Would you like to play again?");
                     break;
                 case 2:
                     if(Settings.getInstance().isSinglePlayer())
                         alertDialogBuilder.setMessage(R.string.comp_win);
                     else
                         alertDialogBuilder.setMessage("Player 2 has won! Would you like to play again?");
                     break;
             }
             gameOverAlert = alertDialogBuilder.create();
             this.showAlert();
         }
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         boolean retry = true;
         setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
             } catch (InterruptedException e) {}
         }
     }
     public void showAlert(){
         gameOverAlert.show();
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event){
        // if (game.settings.isSinglePlayer() && game.playerOne.isPlayersTurn()){
 
         boolean touchEvent = false;
         int action = event.getAction();
         //Log.d(TAG,""+selected);
         switch (action){
             case MotionEvent.ACTION_DOWN:
                 touchEvent = true;
                 if(!selected){
                     final float x = event.getX();
                     final float y = event.getY();
                     selectedPiece = mGetNearestPiece((int)x,(int)y);
                     if (selectedPiece == null){
                         invalidToast.show();
                         touchEvent = false;
                         break;
                     }
                     selected = true;
                     break;
                 }else{
                     final float x = event.getX();
                     final float y = event.getY();
                     selectedPiece = mGetNearestPiece((int)x,(int)y);
                     if (selectedPiece == null){
                         invalidToast.show();
                         touchEvent = false;
                         selected = false;
                         break;
                     }
                     mUpdateGrid(selectedPiece);
                     selected = false;
                     break;
                 }
             case MotionEvent.ACTION_UP:
                 break;
             default:
                 touchEvent = false;
         }
         return touchEvent;
     }
 
     class GameThread extends Thread{
         private SurfaceHolder surfaceHolder;
         private CheckersDraw gameView;
         private boolean run = false;
 
         public GameThread(SurfaceHolder surfaceHolder, CheckersDraw gameView) {
             this.surfaceHolder = surfaceHolder;
             this.gameView = gameView;
         }
 
         public void setRunning(boolean run) {
             this.run = run;
         }
 
 
         @Override
         public void run() {
             while (run) {
                 Canvas c = null;
                 try{
                     c = surfaceHolder.lockCanvas();
                     synchronized (surfaceHolder) {
                         if(c!= null){
                             gameView.onDraw(c);
 
                         }
 
                     }
                 }finally {
                     if (c != null) {
                         surfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
             }
         }
     }
 }
