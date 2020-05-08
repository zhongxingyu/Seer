 package edu.mines.csci498.snake;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 /*
  * BoardView.java
  * Description: Handles basic View model for Board-like displays. This incorporates handling
  * 		tile arrays & their respective bitmaps.
  */
 public class BoardView extends View {
 	// Holds bitmaps based off a key (Maps a type of tile to a bitmap)
 	private Bitmap[] mTileBitmaps;
 
 	// Holds the tile array (maps an x and y coord to a tile key)
 	private int[][] mTileKeyArray;
 
 	private int mTileSize;
 	protected int mNumberTilesX;
 	protected int mNumberTilesY;
 	
 	//A default paint tool used to draw the bitmaps
 	private final Paint mPaint = new Paint();
 
 	public BoardView(Context context, AttributeSet attrs, int tileSize) {
 		super(context, attrs);
 		this.mTileSize = tileSize;
 	}
 
 	public void createNewBitmapArray(int size) {
 		mTileBitmaps = new Bitmap[size];
 	}
 
 	/*
 	 * Takes a resource and loads it into our Bitmaps with the given key Only
 	 * will load the dimensions given by our mTileSize. This method of loading 
 	 * was given as a tutorial on the site docs
 	 */
 	public void loadResource(int key, Drawable resource) {
 		Bitmap bm = Bitmap.createBitmap(mTileSize, mTileSize,
 				Bitmap.Config.ARGB_8888); // Needs to be mutable & 8888 gives us
 											// alpha capabilities
 		Canvas canvas = new Canvas(bm);
 
 		resource.setBounds(0, 0, mTileSize, mTileSize);
 		resource.draw(canvas);
 
 		mTileBitmaps[key] = bm;
 
 	}
 
 	/**
 	 * Clears all tiles' key values back to the default(0).
 	 */
 	public void clearTiles() {
 		Log.i("snake", "clearing everything");
 		for (int i = 0; i < mNumberTilesX; ++i)
 			for (int j = 0; j < mNumberTilesY; ++j)
 				mTileKeyArray[i][j] = 0;
 	}
 	
 	/**
 	 * Sets a tile on the board to contain the asset indicated by key
 	 * 
 	 * @param x The x coordinate of the tile
 	 * @param y The y coordinate of the tile
 	 * @param key The asset key
 	 */
 	public void setTile(int x, int y, int key) {
 		mTileKeyArray[x][y] = key;
 	}
 	
 	/**
 	 * Gets the asset key of a given tile
 	 * 
 	 * @param x The x coordinate of the tile
 	 * @param y The y coordinate of the tile
 	 * @return The asset key of the tile located at x and y
 	 */
 	public int getTileAssetKey(int x, int y) {
 		return mTileKeyArray[x][y];
 	}
 
 	// Will be called at the beginning to allow us to create proper board
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		mNumberTilesX = (int) Math.floor(w / mTileSize);
 		mNumberTilesY = (int) Math.floor(h / mTileSize);
 
 		mTileKeyArray = new int[mNumberTilesX][mNumberTilesY];
 		clearTiles();
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
		mPaint.setAlpha(255);
		
 		for (int i = 0; i < mNumberTilesX; ++i) {
 			for (int j = 0; j < mNumberTilesY; ++j) {
 				// load bitmap key
 				int key = mTileKeyArray[i][j]; // 0-default
 				if (key >= 0 && key < mTileBitmaps.length) {
 					// load bitmap
 					Bitmap bm = mTileBitmaps[key];
 					// draw
 					canvas.drawBitmap(bm, (mTileSize * i), (mTileSize * j), mPaint);
 				}
 			}
 		}
 	}
 
 }
