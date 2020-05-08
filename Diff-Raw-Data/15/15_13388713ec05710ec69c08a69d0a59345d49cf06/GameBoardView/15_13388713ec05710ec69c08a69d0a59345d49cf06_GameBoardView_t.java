 package org.game.advwars;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Shader.TileMode;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class GameBoardView extends View
 {
 	protected static int mTileSize;
     protected static int mXTileCount;
     protected static int mYTileCount;
     private static int mXOffset;
     private static int mYOffset;
     private Bitmap[] mTileArray;
     private int[][] mTileGrid;
     private float mX;
     private float mY;
     private final Paint mPaint = new Paint();
     private Canvas canvas;
 	
     public GameBoardView(Context context, AttributeSet attrs, int defStyle)
     {
         super(context, attrs, defStyle);
 
         TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GameBoardView);
         mTileSize = a.getInt(R.styleable.GameBoardView_tileSize, 30);
         initGameBoardView();
         a.recycle();
     }
 
     public GameBoardView(Context context, AttributeSet attrs)
     {
         super(context, attrs);
 
         TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GameBoardView);
         mTileSize = a.getInt(R.styleable.GameBoardView_tileSize, 30);
         initGameBoardView();
         a.recycle();
     }
     
     private void initGameBoardView()
     {
         setFocusable(true);
 
         Resources r = this.getContext().getResources();
         
         resetTiles(4);
         loadTile(0, r.getDrawable(R.drawable.grass));
         loadTile(1, r.getDrawable(R.drawable.selected));
     }
 
     public void resetTiles(int tilecount)
     {
     	mTileArray = new Bitmap[tilecount];
     }
     
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh)
     {
         mXTileCount = (int) Math.floor(w / mTileSize);
         mYTileCount = (int) Math.floor(h / mTileSize);
 
         mXOffset = ((w - (mTileSize * mXTileCount)) / 2);
         mYOffset = ((h - (mTileSize * mYTileCount)) / 2);
//
//        mXOffset = 0;
//        mYOffset = 0;
         
         mTileGrid = new int[mXTileCount][mYTileCount];
         clearTiles();
     }
     
     public void loadTile(int key, Drawable tile)
     {
         Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
         canvas = new Canvas(bitmap);
         tile.setBounds(0, 0, mTileSize, mTileSize);
         tile.draw(canvas);
         
         mTileArray[key] = bitmap;
     }
     
     public void clearTiles()
     {
         for (int x = 0; x < mXTileCount; x++) {
             for (int y = 0; y < mYTileCount; y++) {
                 setTile(0, x, y);
             }
         }
     }
     
     public void setTile(int tileindex, int x, int y)
     {
         mTileGrid[x][y] = tileindex;
     }
     
     @Override
     public void onDraw(Canvas canvas)
     {
         super.onDraw(canvas);
         for (int x = 0; x < mXTileCount; x += 1)
         {
             for (int y = 0; y < mYTileCount; y += 1)
             {
                 if (mTileGrid[x][y] > -1)
                 {
                     canvas.drawBitmap(mTileArray[mTileGrid[x][y]], 
                     		mXOffset + x * mTileSize,
                     		mYOffset + y * mTileSize,
                     		mPaint);
                 }
             }
         }
         
         /*mPaint.setColor(Color.RED);
         canvas.drawLine(0, 0, 10, 0, mPaint);
         
         canvas.drawLine(0, 0, 0, 10, mPaint);
         
         mPaint.setColor(Color.BLUE);
         canvas.drawLine(0, 10, 0, 100, mPaint);*/
 
     }
     
     public void update()
     {
           clearTiles();
           //updateWalls();
     }
     
     /*private void updateWalls()
     {
         for (int x = 0; x < mXTileCount; x++) {
             setTile(0, x, 0);
             setTile(0, x, mYTileCount - 1);
         }
         for (int y = 1; y < mYTileCount - 1; y++) {
             setTile(0, 0, y);
             setTile(0, mXTileCount - 1, y);
         }
     }*/
     
     public void redrawBoard()
     {
     	onDraw(this.canvas);
     }
     
     public int getXTileCount()
     {
     	return mXTileCount;
     }
     
     public int getYTileCount()
     {
     	return mYTileCount;
     }
 
 	public void selectPoint(float x, float y) {
		x = x - mXOffset;
		y = y - mYOffset;
 		int tileX = (int) x/mTileSize;
 		int tileY = (int) y/mTileSize;
		
		if(tileX < mXTileCount && tileY < mYTileCount &&
				tileX >= 0 && tileY >= 0)
			setTile(1, tileX, tileY);
 	}
 
 	public void translateBoard(float f, float g) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void scaleBoard(float scale, float x, float y) {
 		mTileSize = (int) (mTileSize * scale);
 		
 		if(mTileSize < 10)
 			mTileSize = 10;
 		if(mTileSize > 50)
 			mTileSize = 50;
 		
 		initGameBoardView();
 		onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
 		invalidate();
 	}
 }
