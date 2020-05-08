 package edu.ycp.cs481.ycpgames;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.graphics.Canvas;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Created by michaelcoombs on 11/5/13.
  */
 public class DotsDraw extends SurfaceView implements SurfaceHolder.Callback {
 
     //private static final String TAG = "DotsDraw";
     private Bitmap mBackground;
     SurfaceHolder surfaceHolder;
     private GameThread thread;
     private DotsGrid grid;
     private DotsGame game;
     private Paint paintOne, paintTwo;
     private DotsGridCell start,end;
     private boolean touched;
     private float pointerX, pointerY;
     private Toast invalidToast;
 
     public DotsDraw(Context context, DotsGrid grid){
         super(context);
         touched = false;
         this.grid = grid;
         surfaceHolder = getHolder();
         getHolder().addCallback(this);
         game = new DotsGame();
         invalidToast = Toast.makeText(context,"Invalid move, please select another!",Toast.LENGTH_SHORT);
         mBackground = BitmapFactory.decodeResource(getResources(),R.drawable.dot);
 
         paintOne = new Paint();
         paintOne.setColor(Color.GREEN);
         paintOne.setStyle(Paint.Style.FILL);
         paintOne.setAntiAlias(true);
         paintOne.setTextSize(36);
         paintOne.setStrokeWidth(12);
         start = null;
         end = null;
 
         paintTwo = new Paint();
         paintTwo.setColor(Color.GRAY);
         paintTwo.setAntiAlias(true);
         paintTwo.setTextSize(36);
         paintTwo.setStyle(Paint.Style.FILL);
         paintTwo.setStrokeWidth(12);
     }
 
 
     public void setRunning(boolean run){
         thread.setRunning(run);
     }
 
     @Override
     public void onDraw(Canvas canvas){
         canvas.drawRGB(255, 255, 255);
         Paint tempPaint;
         if (game.whosTurn() == GameVal.PLAYER_ONE){
             tempPaint = paintOne;
         }else {
             tempPaint = paintTwo;
         }
 
         while (touched){
             //canvas.drawBitmap(mBackground,start.getCenter()[0] - (mBackground.getScaledWidth(canvas)/2), start.getCenter()[1] - (mBackground.getScaledHeight(canvas)/2),selectPaint);
             //canvas.drawCircle(start.getCenter()[0], start.getCenter()[1],300,selectPaint);
         }
         DotsNode[][] temp = game.board.getDotsGrid();
         for(int i = 0; i < grid.getGridWidth();i++){
             for (int j = 0; j < grid.getGridLength(); j++) {
                int[] center = grid.getCell(i,j).getCenter();
 
                if ((game.board.getLineAt(i, j, Direction.UP) != GameVal.EMPTY) && (j != 0)){
                    canvas.drawLine(center[0],center[1], grid.getCell(i,j-1).getCenter()[0],grid.getCell(i,j-1).getCenter()[1],tempPaint);
                }
                if ((game.board.getLineAt(i,j,Direction.LEFT) != GameVal.EMPTY) && (i !=0)){
                    canvas.drawLine(center[0],center[1], grid.getCell(i-1,j).getCenter()[0],grid.getCell(i-1,j).getCenter()[1],tempPaint);
                }
                if ((game.board.getLineAt(i,j,Direction.RIGHT) != GameVal.EMPTY) && (i != grid.getGridWidth()-1)){
                    canvas.drawLine(center[0],center[1], grid.getCell(i+1,j).getCenter()[0],grid.getCell(i+1,j).getCenter()[1],tempPaint);
                }
                if ((game.board.getLineAt(i,j,Direction.DOWN) != GameVal.EMPTY) && (j != grid.getGridLength()-1)){
                     canvas.drawLine(center[0],center[1], grid.getCell(i,j+1).getCenter()[0],grid.getCell(i,j+1).getCenter()[1],tempPaint);
                }
 
                canvas.drawBitmap(mBackground,center[0] - (mBackground.getScaledWidth(canvas)/2),center[1] - (mBackground.getScaledHeight(canvas)/2),null);
                if(temp[i][j].isNodeFilled() == GameVal.PLAYER_ONE){
                    canvas.drawBitmap(mBackground,grid.getCell(i,j).getCenter()[0]+grid.getCell(i,j).getWidth()/2,grid.getCell(i,j).getCenter()[1]+grid.getCell(i,j).getLength()/2,paintOne);
                }else if(temp[i][j].isNodeFilled() == GameVal.PLAYER_TWO){
                    canvas.drawBitmap(mBackground,grid.getCell(i,j).getCenter()[0],grid.getCell(i,j).getCenter()[1],paintTwo);
                }
             }
         }
        canvas.drawText("Player 1: " + game.getPlayerOneScore(), 20, 1200, paintOne);
        canvas.drawText("Player 2: "+game.getPlayerTwoScore(),520,1200,paintTwo);
     }
 
     public DotsGridCell mFindNearestDot(int x, int y){
        DotsGridCell temp = null;
 
         for(int i = 0; i < grid.getGridWidth();i++){
             for (int j = 0; j < grid.getGridLength(); j++) {
                 if (x > grid.getCell(i,j).getX() && x <= grid.getCell(i,j).getX1()){
                     if (y > grid.getCell(i,j).getY() && y <= grid.getCell(i,j).getY1()){
                         temp = grid.getCell(i,j);
                     }
                 }
             }
         }
         return temp;
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         thread = new GameThread(getHolder(), this);
         thread.setRunning(true);
         thread.start();
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         boolean retry = true;
         thread.setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
             } catch (InterruptedException e) {
 
             }
 
         }
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event){
 
         int action = event.getAction();
 
         switch (action){
             case MotionEvent.ACTION_DOWN:
                 final float x = event.getX();
                 final float y = event.getY();
                 start = mFindNearestDot((int)x,(int)y);
                 if(start == null)
                     break;
                 touched = true;
                 break;
             case MotionEvent.ACTION_MOVE:
                 pointerX = event.getX();
                 pointerY = event.getY();
                 touched = true;
                 break;
             case MotionEvent.ACTION_UP:
                 final float endX = event.getX();
                 final float endY = event.getY();
                 end = mFindNearestDot((int)endX, (int)endY);
                 pointerX = -1;
                 pointerY = -1;
                 touched = true;
                 if (end == null || end.equals(start))
                     break;
                 else{
                     mUpdateGrid(start,end);
                     break;
                 }
             default:
                 touched = false;
         }
 
         touched = false;
         return true;
     }
 
     private void mUpdateGrid(DotsGridCell start, DotsGridCell end) {
         int isMoveValid;
 
         float dx = end.getX()-start.getX();
         float dy = end.getY()-start.getY();
         if (Math.abs(dx) > Math.abs(dy)){
            if (dx > 0){
                 isMoveValid =  game.makeMove(start.getArrX(),start.getArrY(),Direction.RIGHT);
            }else
                 isMoveValid =  game.makeMove(start.getArrX(),start.getArrY(),Direction.LEFT);
         }else{
             if (dy > 0){
                 isMoveValid = game.makeMove(start.getArrX(),start.getArrY(),Direction.DOWN);
             }else
                 isMoveValid = game.makeMove(start.getArrX(),start.getArrY(),Direction.UP);
         }
         if (isMoveValid == -2){
             invalidToast.show();
         }
     }
     //public
 
 class GameThread extends Thread{
     private SurfaceHolder surfaceHolder;
     private DotsDraw gameView;
     private boolean run = false;
 
     public GameThread(SurfaceHolder surfaceHolder, DotsDraw gameView) {
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
                     gameView.onDraw(c);
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
 
 
 
