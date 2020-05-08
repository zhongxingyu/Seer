 package com.sixteen.tetrisbattleandroid;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class TetrisTileView extends TileView {
 
 		public static final int scoreIncrement = 20;
 		public static int score= 0;
 		//public static TextView scoreText;
 		
         private static final int BACK_IMG = 0;
         private static final int MAGENTA_IMG = 1;
         private static final int YELLOW_IMG = 2;
         private static final int GREEN_IMG = 3;
         private static final int BLUE_IMG = 4;
         private static final int ORANGE_IMG = 5;
         private static final int CYAN_IMG = 6;
         private static final int RED_IMG  = 7;
         private static final int ALPHA_MAGENTA_IMG = 8;
         private static final int ALPHA_YELLOW_IMG = 9;
         private static final int ALPHA_GREEN_IMG = 10;
         private static final int ALPHA_BLUE_IMG = 11;
         private static final int ALPHA_ORANGE_IMG = 12;
         private static final int ALPHA_CYAN_IMG = 13;
         private static final int ALPHA_RED_IMG  = 14;   
         private static final int IMG_COUNT = 15;
 
         private TetrisPiece[] mAllTetrisPieces;
         
         
 
         private class TetrisPiece {                             
 
                 /** mImage name of the piece block **/
                 public int mImage;
 
                 //Alpha version
                 public int mAlphaImage;
 
                 /** array of piece mRotations **/
                 public TetrisPieceRotation[] mRotations;
 
                 //constructor
                 public TetrisPiece(int image, int alphaImage, TetrisPieceRotation[] rotations)
                 {
                         mImage = image;
                         mAlphaImage = alphaImage;
                         mRotations = rotations;
                 }
                 
                 
         }
 
         /*Rotation */
         private class TetrisPieceRotation {
                 public int mWidth;      
                 public int mHeight;
                 public int mCenterX;
                 public int mCenterY;
                 public String mMap;
 
                 //Constructor
                 public TetrisPieceRotation(int width, int height, int centerX, int centerY, String map)
                 {
                         mWidth = width;
                         mHeight = height;
                         mCenterX = centerX;
                         mCenterY = centerY;
                         mMap = map;
                 }
         }
 
         private void initTetrisPieces() {
                 mAllTetrisPieces = new TetrisPiece[7];
 
                 //J-tetri
                 TetrisPieceRotation[] tmpRotation = new TetrisPieceRotation[4];
                 tmpRotation[0] = new TetrisPieceRotation(3, 2, 2, 2, "100111");
                 tmpRotation[1] = new TetrisPieceRotation(2, 3, 2, 2, "010111");
                 tmpRotation[2] = new TetrisPieceRotation(3, 2, 2, 1, "111001");
                 tmpRotation[3] = new TetrisPieceRotation(2, 3, 1, 2, "111010");
                 mAllTetrisPieces[0] = new TetrisPiece(MAGENTA_IMG, ALPHA_MAGENTA_IMG, tmpRotation);
 
                 //L-tetri
                 tmpRotation = new TetrisPieceRotation[4];
                 tmpRotation[0] = new TetrisPieceRotation(3, 2, 2, 2, "001111");
                 tmpRotation[1] = new TetrisPieceRotation(2, 3, 2, 2, "110101");
                 tmpRotation[2] = new TetrisPieceRotation(3, 2, 2, 1, "111100");
                 tmpRotation[3] = new TetrisPieceRotation(2, 3, 1, 2, "101011");
                 mAllTetrisPieces[1] = new TetrisPiece(YELLOW_IMG, ALPHA_YELLOW_IMG, tmpRotation);
 
                 //Z-tetri
                 tmpRotation = new TetrisPieceRotation[2];
                 tmpRotation[0] = new TetrisPieceRotation(3, 2, 2, 1, "110011");
                 tmpRotation[1] = new TetrisPieceRotation(2, 3, 2, 2, "011110");
                 mAllTetrisPieces[2] = new TetrisPiece(GREEN_IMG, ALPHA_GREEN_IMG, tmpRotation);         
 
                 //S-tetri
                 tmpRotation = new TetrisPieceRotation[2];
                 tmpRotation[0] = new TetrisPieceRotation(3, 2, 2, 1, "011110");
                 tmpRotation[1] = new TetrisPieceRotation(2, 3, 2, 2, "101101");
                 mAllTetrisPieces[3] = new TetrisPiece(BLUE_IMG, ALPHA_BLUE_IMG, tmpRotation);           
 
                 //U-tetri
                 tmpRotation = new TetrisPieceRotation[4];
                 tmpRotation[0] = new TetrisPieceRotation(2, 2, 1, 1, "1101");
                 tmpRotation[1] = new TetrisPieceRotation(2, 2, 2, 1, "0111");
                 tmpRotation[2] = new TetrisPieceRotation(2, 2, 1, 2, "1011");
                 tmpRotation[3] = new TetrisPieceRotation(2, 2, 1, 2, "1101");
                 mAllTetrisPieces[4] = new TetrisPiece(ORANGE_IMG, ALPHA_ORANGE_IMG, tmpRotation);
 
                 //O-tetri
                 tmpRotation = new TetrisPieceRotation[1];
                 tmpRotation[0] = new TetrisPieceRotation(2, 2, 1, 1, "1111");
                 mAllTetrisPieces[5] = new TetrisPiece(CYAN_IMG, ALPHA_CYAN_IMG, tmpRotation);
 
                 //I-tetri
                 tmpRotation = new TetrisPieceRotation[2];
                 tmpRotation[0] = new TetrisPieceRotation(1, 4, 1, 2, "1111");
                 tmpRotation[1] = new TetrisPieceRotation(4, 1, 2, 1, "1111");
                 mAllTetrisPieces[6] = new TetrisPiece(RED_IMG, ALPHA_RED_IMG, tmpRotation);             
 
 
                 
         }
 
         void clearTetrisPiece(int centerX, int centerY, TetrisPiece piece, int rotationIndex) {
                 TetrisPieceRotation rotation = piece.mRotations[rotationIndex];
 
                 int xStart;
                 int y=0;
                 xStart=centerX-rotation.mCenterX;
                 y=centerY-rotation.mCenterY;
                 
                 int x=xStart;
                 int dataPos = 0;
 
                 //for rows
                 for(int r=0; r<rotation.mHeight; r++) {
                         //for columns
                         for(int c=0; c<rotation.mWidth; c++) {
                                 char flag = rotation.mMap.charAt(dataPos);
 
                                 if(flag=='1') {
                                         setTile(BACK_IMG, x, y);
                                 }
                                 x++;
                                 dataPos++;
                         }
                         x=xStart;
                         y++;
                 }
         }
 
         void drawTetrisPiece(int centerX, int centerY, TetrisPiece piece, int rotationIndex) {
                 
                 if(piece==null)
                         return;
                 
                 TetrisPieceRotation rotation = piece.mRotations[rotationIndex];
 
                 int xStart;
                 int y=0;
                xStart=centerX-rotation.mCenterX;
                y=centerY-rotation.mCenterY;
                 
                 int x=xStart;
                 int dataPos=0;
 
 
                 //for rows
                 for(int r=0; r<rotation.mHeight; r++) {
                         //for columns
                         for(int c=0; c<rotation.mWidth; c++) {
                                 char flag = rotation.mMap.charAt(dataPos);
                                 if(flag=='1') {
                                         setTile(piece.mImage, x, y);
                                 }
                                 x++;
                                 dataPos++;
                         }
                         x=xStart;
                         y++;
                 }
         } //drawPiece
 
         boolean canExists(TetrisPieceRotation rotation, int centerX, int centerY) {
 
                 int xStart=centerX-rotation.mCenterX;
                 int x=xStart;
                 int y=centerY-rotation.mCenterY;
                 int dataPos=0;
 
                 //for rows
                 for(int r=0; r<rotation.mHeight; r++) {
                         //for columns
                         for(int c=0; c<rotation.mWidth; c++) {
                                 if(x>=0 && x<mXTileCount && y>=0 && y<mYTileCount) {
                                         char flag = rotation.mMap.charAt(dataPos);
                                         if(flag=='1') {
                                                 if(mBoard[x][y]!=BACK_IMG) return false;
                                         }
                                 }
                                 else //out of board boundary
                                         return false;
                                 x++;
                                 dataPos++;
                         }
                         x=xStart;
                         y++;
                 }
                 return true;
         }
 
         boolean moveDown() {
                 if(mFallingPiece==null) return false;
 
                 if(canExists(mFallingPiece.mRotations[mFallingRotation], mFallingCenterX, mFallingCenterY+1)) {
                         mFallingCenterY++;
                         return true;
                 }
                 return false;
         }
 
         boolean moveLeft() {
                 if(mFallingPiece==null) return false;
 
                 if(canExists(mFallingPiece.mRotations[mFallingRotation], mFallingCenterX-1, mFallingCenterY)) {
                         mFallingCenterX--;
                         return true;
                 }
                 return false;
         }  
 
         boolean moveRight() {
                 if(mFallingPiece==null) return false;
 
                 if(canExists(mFallingPiece.mRotations[mFallingRotation], mFallingCenterX+1, mFallingCenterY)) {
                         mFallingCenterX++;
                         return true;
                 }
                 return false;
         }
 
         boolean rotate() {
                 if(mFallingPiece==null) return false;
         
                 int rotation=mFallingRotation-1;
                 if(rotation == -1) 
                         rotation=mFallingPiece.mRotations.length-1;
                 if(canExists(mFallingPiece.mRotations[rotation], mFallingCenterX, mFallingCenterY)) {
                         mFallingRotation=rotation;
                         return true;
                 }
                 return false;
         }
 
         void merge(TetrisPiece piece, int rotationIndex, int centerX, int centerY) {
 
                 int img=piece.mImage;
                 TetrisPieceRotation rotation = piece.mRotations[rotationIndex];
 
                 int xStart=(centerX-rotation.mCenterX);
                 int x=xStart;
                 int y=(centerY-rotation.mCenterY);
                 int dataPos=0;
 
                 //for rows
                 for(int r=0; r<rotation.mHeight; r++) {
                 //for columns
                         for(int c=0; c<rotation.mWidth; c++) {
                                 char flag = rotation.mMap.charAt(dataPos);
                                 if(flag=='1') {
                                         mBoard[x][y]=img;
                                 }
                                 x++;
                                 dataPos++;
                         }
                         x=xStart;
                         y++;
                 }
         }
 
         private static final long MERGE_ANIMATION_TIME = 150;
 
         private class MergeLine {
                 public int mLine;
                 public int[] mLineTile;
         }
         
         private ArrayList<MergeLine> mMergeLines = new ArrayList<MergeLine>();
 
         private int mMergeTimes;
 
         private MergeHandler mMergeHandler = new MergeHandler();
 
         public class MergeHandler extends Handler {
 
                 @Override
                 public void handleMessage(Message msg) {
                         mergeAnimations();
                         //scoreText.setText(score);
                 }
 
                 public void sleep(long delayMillis) {
                         this.removeMessages(0);
                         sendMessageDelayed(obtainMessage(0), delayMillis);
                 }
         };
 
         private void mergeAnimations()
         {
                 if(mMode == RUNNING) {
                         switch(mMergeTimes) {
                                 case 0: case 2: case 4: //Disappear
                                         for(MergeLine tmpLine : mMergeLines) {
                                                 for(int x=0; x<mXTileCount; x++) {
                                                         mBoard[x][tmpLine.mLine] = BACK_IMG;
                                                 }
                                        }                               
                                         break;
                                 case 1: case 3://Re-appear
                                         for(MergeLine tmpLine : mMergeLines) {
                                                 for(int x=0; x<mXTileCount; x++) {
                                                         mBoard[x][tmpLine.mLine] = tmpLine.mLineTile[x];
                                                 }
                                         }                               
                                         break;
                                 case 5: //Remove merging lines
                                         for(MergeLine tmpLine: mMergeLines) {
                                                 for(int y=tmpLine.mLine; y>=0; y--) {
                                                         for(int x=0; x<mXTileCount; x++) {
                                                                 mBoard[x][y] = (y==0 ? BACK_IMG : mBoard[x][y-1]);
                                                         }
                                                 }                                       
                                         }
                                         break;
                                 case 6: //Start running again
                                         update();
                                         return;
                                         
                         }
                       // score += scoreIncrement;
                         mMergeTimes++;
                         drawBoard();
                         invalidate();
                         mMergeHandler.sleep(MERGE_ANIMATION_TIME);
                 }
                 else if (mMode ==PAUSE) {
                         mMergeHandler.sleep(PAUSE_REFRESH_DELAY);
                         return;
                 }
                 else //Not Running or Pause
                         return;
         }
         
         private boolean findMergingLines() {
                 mMergeLines.clear();
                 
                 
                 
                 for(int y=0; y<mYTileCount; y++) {
                         boolean all=true;
                         for(int x=0; x<mXTileCount; x++) {
                                 if(mBoard[x][y]== BACK_IMG) {
                                         all=false;
                                         break;
                                 }
                         }
                         
                         if(all) {
                                 MergeLine tmpLine = new MergeLine();
                                 tmpLine.mLine = y;
                                 tmpLine.mLineTile = new int[mXTileCount];
                                 for(int x=0; x<mXTileCount; x++)
                                         tmpLine.mLineTile[x] = mBoard[x][y];
                                 mMergeLines.add(tmpLine);
                                 
                                 
                         }
                 }
 
                 if(mMergeLines.isEmpty()==false) {
                         mMergeTimes = 0;
                         mergeAnimations();
                         return true;
                 }
                 else
                         return false;
         }
 
         
         /* Constructor */
         
         public TetrisTileView(Context context, AttributeSet attris) {
                 super(context, attris);
                 initTetrisView();
         }
         
         public TetrisTileView(Context context, AttributeSet attris, int defStyle) {
                 super(context, attris, defStyle);
                 initTetrisView();
         }
 
         private int[][] mBoard;
 
         private void clearBoard()
         {
                 for(int x=0; x<mXTileCount; x++) {
                 	
                 		
                         for(int y=0; y<mYTileCount; y++) {
              
                         			mBoard[x][y]=BACK_IMG;
                         }
                 }
         }
 
         private void drawBoard()
         {
                 if(mBoard==null)
                         return;
                 for(int x=0; x<mXTileCount; x++) {
                         for(int y=0; y<mYTileCount; y++) {
                                 setTile(mBoard[x][y], x, y);
                         }
                 }
         }
 
         private void loadTetrisImage()
         {
                 resetTiles(IMG_COUNT);
                 
                 Resources r = this.getContext().getResources();
         
                 loadTile(BACK_IMG, r.getDrawable(R.drawable.bg));
                 loadTile(MAGENTA_IMG, r.getDrawable(R.drawable.brownblock));
                 loadTile(YELLOW_IMG, r.getDrawable(R.drawable.yellowblock));
                 loadTile(GREEN_IMG, r.getDrawable(R.drawable.greenblock));
                 loadTile(BLUE_IMG, r.getDrawable(R.drawable.blueblock));
                 loadTile(ORANGE_IMG, r.getDrawable(R.drawable.orangeblock));
                 loadTile(CYAN_IMG, r.getDrawable(R.drawable.violetblock));
                 loadTile(RED_IMG, r.getDrawable(R.drawable.redblock));
              
                 
         }
         
         private void initTetrisView()
         {
                 int w = getWidth();
                 int h = getHeight();
                 
                 if(w>0 && h>0 )
                 {
 //                      setFocusable(true);
 
                         initTetrisPieces();
 
                         loadTetrisImage();
 
                         mBoard = new int[mXTileCount][mYTileCount];
                         clearBoard();
                 }
                 
                 return;
         }
 
         @Override
         protected void onSizeChanged(int w, int h, int oldw, int oldh) {        
                 super.onSizeChanged(w, h, oldw, oldh);
 
                 if(mMode != READY) { //Not Initialization
                         loadTetrisImage();
                         drawBoard();
                         drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                         return;
                 }
                 else
                         initTetrisView();
         }
 
         public static final int CONTROL_TOUCH = 0;
         public static final int CONTROL_KEYBOARD = 1;
         private int mControlMethod;
 
         public void setControlMethod(int method) {
                 mControlMethod = method;
                 if(method == CONTROL_KEYBOARD) {
                         setFocusable(true);
                         setFocusableInTouchMode(true);
                 }
                 else {
                         setFocusable(false);
                         setFocusableInTouchMode(false);
                 }
         }
         
         private boolean mIsTouchMove = false;
         private long mTouchDownTime;
         private float mTouchDownX, mTouchDownY;
         private static final long TOUCH_UP_THRESHOLD = 500;
         
         @Override
         public boolean onTouchEvent(MotionEvent event) {
 
                 if(mMode!=RUNNING || mControlMethod!=CONTROL_TOUCH)
                         return true;
                 
                 float x = event.getX();
                 float y = event.getY();
 
                 switch (event.getAction()) {
                         case MotionEvent.ACTION_DOWN:
                                 mIsTouchMove = false;
                                 mTouchDownTime = System.currentTimeMillis();
                                 mTouchDownX = x;
                                 mTouchDownY = y;
                         break;
                         case MotionEvent.ACTION_MOVE:
                                 float dx = x-mTouchDownX;
                                 float dy = y-mTouchDownY;
 
                                 //A valid move
                                 if(Math.abs(dx) >= mTileSize || Math.abs(dy) >= mTileSize) {
                                         mIsTouchMove = true;
                                         mTouchDownX = x;
                                         mTouchDownY = y;
 
                                         //Move in X direction
                                         if(Math.abs(dx) >= Math.abs(dy)) {
                                                 int tetrisMoveX = (int) (dx  / (float)(mTileSize));
                                                 //move right
                                                 if (tetrisMoveX >0)
                                                 {
                                                         for(int moveX = 0; moveX<tetrisMoveX; moveX++) {
                                                                 if(moveRight()==true)
                                                                 {
                                                                         clearTetrisPiece(mFallingCenterX-1, mFallingCenterY, mFallingPiece, mFallingRotation);
                                                                         drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                                                         invalidate();
                                                                 }
                                                                 else
                                                                         break;
                                                         }
                                                 }
                                                 //move left
                                                 else if (tetrisMoveX <0)
                                                 {
                                                         for(int moveX = 0; moveX>tetrisMoveX; moveX--) {
                                                                 if(moveLeft()==true)
                                                                 {
                                                                         clearTetrisPiece(mFallingCenterX+1, mFallingCenterY, mFallingPiece, mFallingRotation);
                                                                         drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                                                         invalidate();
                                                                 }
                                                                 else
                                                                         break;
                                                         }
                                                 }
 
                                         }
                                         //Move in Y direction (Only move down, no up)
                                         else if (dy > 0) {
                                                 int tetrisMoveY = (int) (dy / (float)mTileSize);
                                                 if (tetrisMoveY !=0)
                                                 {
                                                         for(int moveY = 0; moveY<tetrisMoveY; moveY++) {
                                                                 if(moveDown()==true)
                                                                 {
                                                                         clearTetrisPiece(mFallingCenterX, mFallingCenterY-1, mFallingPiece, mFallingRotation);
                                                                         drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                                                         invalidate();
                                                                 }
                                                                 else
                                                                         break;
                                                         }
                                                 }
                                         }
                                 }
                                 break;
                         case MotionEvent.ACTION_UP:
                                 long now = System.currentTimeMillis();
 
                                 //A valid rotation
                                 if (mIsTouchMove == false && 
                                         now-mTouchDownTime < TOUCH_UP_THRESHOLD) {
                                         int oldRotation = mFallingRotation;
                                         if(rotate()==true)
                                         {
                                                 clearTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, oldRotation);
                                                 drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                                 invalidate();
                                         }
                                 }
                                 break;
                 }
                 return true;
         }
 
 
         @Override
         public boolean onKeyDown(int keyCode, KeyEvent event) {
                 if(keyCode == KeyEvent.KEYCODE_BACK) //ESC
                         setFocusable(false);
                 if(mMode!=RUNNING || mControlMethod != CONTROL_KEYBOARD)
                         return true;
                         
                 if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                         int oldRotation = mFallingRotation;
                         if(rotate()==true)
                         {
                                 clearTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, oldRotation);
                                 drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 invalidate();
                         }
                 }
                 else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                         if(moveLeft()==true)
                         {
                                 clearTetrisPiece(mFallingCenterX+1, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 invalidate();
                         }
                 }
                 else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                         if(moveRight()==true)
                         {
                                 clearTetrisPiece(mFallingCenterX-1, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 invalidate();
                         }
                 }
                 else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                         if(moveDown()==true)
                         {
                                 clearTetrisPiece(mFallingCenterX, mFallingCenterY-1, mFallingPiece, mFallingRotation);
                                 drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 invalidate();
                         }
                 }
                 
                 return true;
         }
 
 
         private RefreshHandler mRedrawHandler = new RefreshHandler();
 
     class RefreshHandler extends Handler {
 
         @Override
         public void handleMessage(Message msg) {
             TetrisTileView.this.update();
         }
 
         public void sleep(long delayMillis) {
                 this.removeMessages(0);
             sendMessageDelayed(obtainMessage(0), delayMillis);
         }
     };
     
   //      private static Button mResetButton;
     //    private static Button mPauseButton;
         
      //   public static void setButtons(Button resetButton, Button pauseButton)
        // {
          //       mResetButton = resetButton;
            //     mPauseButton = pauseButton;
         //}
     	//public static void setTextView(TextView mscore){
     		//scoreText = mscore;
     		
     //	}
     	
         
         private int mMode = READY;
         public static int mPlayerCount = 0;
         public static int mFinishedPlayerCount = 0;
         public static final int PAUSE = 0;
         public static final int READY = 1;
         public static final int RUNNING = 2;
         public static final int LOSE = 3;
         
         private long mMoveDelay = 600;
         private long PAUSE_REFRESH_DELAY = 10;
 
         private boolean mFalling = false;
 
         private TetrisPiece mNextPiece = null;
         private TetrisPiece mFallingPiece = null;
         private int mFallingRotation;
         private int mFallingCenterX;
         private int mFallingCenterY;
 
         private static final Random RND = new Random(System.currentTimeMillis());
 
         private void newGame() {
                 mPlayerCount++;
                 clearBoard();
                 drawBoard();
                 mFalling = false;
                 mFallingPiece = null;
                 mNextPiece = null;
                 invalidate();
         }
 
         public void update() {
 
                 if(mMode == PAUSE)
                 {
                         mRedrawHandler.sleep(PAUSE_REFRESH_DELAY);
                         return;
                 }
                 else if(mMode !=RUNNING)
                 {
                         return;
                 }
                 else {
                         if(mFalling == false) {
                                 mFalling = true;
 
                                 //select random piece and its random rotation
                                 if(mNextPiece==null) {
                                         mFallingPiece = mAllTetrisPieces[RND.nextInt(mAllTetrisPieces.length)];
                                         mNextPiece= mAllTetrisPieces[RND.nextInt(mAllTetrisPieces.length)];
                                 }
                                 else
                                 {
                                         mFallingPiece = mNextPiece;
                                         mNextPiece= mAllTetrisPieces[RND.nextInt(mAllTetrisPieces.length)];
                                 }
 
                                 mFallingRotation = RND.nextInt(mFallingPiece.mRotations.length);
                                 mFallingCenterX = RND.nextInt(mXTileCount-mFallingPiece.mRotations[mFallingRotation].mWidth+1)
                                                                         +mFallingPiece.mRotations[mFallingRotation].mCenterX;
                                 mFallingCenterY = mFallingPiece.mRotations[mFallingRotation].mCenterY;
 
                                 if(!canExists(mFallingPiece.mRotations[mFallingRotation],mFallingCenterX,mFallingCenterY))
                                 {
                                         setMode(LOSE);
                                         return;
                                 }
                                 else {
                                         drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 }
                 }
                         else { //Falling
                                 mFalling = moveDown();
                                 if(mFalling == true) {
                                         clearTetrisPiece(mFallingCenterX, mFallingCenterY-1, mFallingPiece, mFallingRotation);
                                         drawTetrisPiece(mFallingCenterX, mFallingCenterY, mFallingPiece, mFallingRotation);
                                 }
                                 //start to merge
                                 else
                                 {
                                         merge(mFallingPiece, mFallingRotation, mFallingCenterX, mFallingCenterY);
                                         mFallingPiece = null;
                                         if(findMergingLines()==true) //There is merging, drawing will be handled elsewhere
                                                 return;
                                         else
                                                 drawBoard();                                    
                                 }
                         }
                 invalidate();    
                         mRedrawHandler.sleep(mMoveDelay);
               }
         }
 
         
 
     public void setMode(int newMode) {
 
         //Only can set mode to READY after LOSE
         if(mMode == LOSE && newMode !=READY)
                 return;
 
         int oldMode = mMode;
         mMode = newMode;
 
 
         //Start running a new game
         if (newMode == RUNNING & oldMode == READY) {
                 update();
                 return;
         }
 
         if (newMode == PAUSE) {
 //            str = res.getText(R.string.mode_pause);
         }
         if (newMode == READY) {
                 newGame();
         }
         if (newMode == LOSE) {
                 mFinishedPlayerCount++;
                 if(mFinishedPlayerCount==mPlayerCount) {
                      //   mResetButton.setVisibility(View.VISIBLE);
                        // mPauseButton.setVisibility(View.INVISIBLE);
                 }
 //            str = res.getString(R.string.mode_lose_prefix) + mScore
 //                  + res.getString(R.string.mode_lose_suffix);
         }
 
 //        mStatusText.setText(str);
 //        mStatusText.setVisibility(View.VISIBLE);
     }
 }
