 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.web2;
 
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.view.MotionEvent;
 import android.view.View;
 
 
 /**
  * TileView: a View-variant designed for handling arrays of "icons" or other
  * drawables.
  * 
  */
 public class TileView extends View {
 
     /**
      * Parameters controlling the size of the tiles and their range within view.
      * Width/Height are in pixels, and Drawables will be scaled to fit to these
      * dimensions. X/Y Tile Counts are the number of tiles that will be drawn.
      */
 
     protected static int mTileSize = 40;
     int w; int h;
 
     protected static int mXTileCount;
     protected static int mYTileCount;
 
     private static int mXOffset;
     private static int mYOffset;
     
     private static final int VIDE = 0;
     private static final int ROUTE = 1;
     private static final int TOUR = 2;
 
 
     private Matrix transform;
 	private Matrix intransform;
 
     /**
      * A hash that maps integer handles specified by the subclasser to the
      * drawable that will be used for that reference
      */
     private Bitmap[] mTileArray; 
     
     /**
      * A two-dimensional array of integers in which the number represents the
      * index of the tile that should be drawn at that locations
      */
     private int[][] mTileGrid;
     
     private void initTileView() {
         setFocusable(true);
         Resources r = this.getContext().getResources();
         
         resetTiles(3);
         loadTile(VIDE, r.getDrawable(R.drawable.herbe));
         loadTile(TOUR, r.getDrawable(R.drawable.tour));
         loadTile(ROUTE, r.getDrawable(R.drawable.chemin));
     	
     }
     
     private final Paint mPaint = new Paint();
     
     void init(){
     	initTileView();
         mTileGrid = new int[][]{
         		{1,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
         		{0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
         		{0,0,2,1,0,0,0,0,0,0,0,0,0,0,0},
         		{0,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
         		{0,1,0,0,0,0,1,1,1,1,0,0,0,0,0},
         		{0,1,0,0,0,0,1,0,0,1,2,0,0,0,0},
         		{0,1,2,0,0,2,1,0,0,1,1,1,1,0,0},
         		{0,1,1,1,1,1,1,0,0,0,0,2,1,0,0},
         		{0,0,0,0,0,0,0,0,0,0,0,0,1,2,0},
         		{0,0,0,0,0,0,0,0,0,0,0,0,1,1,1}
         		};
         mYTileCount=mTileGrid.length;
         mXTileCount=mTileGrid[0].length;
     }
 
     public void ajout(int x, int y){
     	if (mTileGrid[y][x]==VIDE){
     	mTileGrid[y][x] = TOUR;
     	}
     }
     
     public void suppression(int x, int y){
     	if (mTileGrid[x][y] == TOUR){
     		mTileGrid[x][y] = VIDE;
     		invalidate();
     	}
     }
     
     public TileView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         init();
         };    
 
     
         
         
 /*       
   		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
 
         mTileSize = a.getInt(R.styleable.TileView_tileSize, 12);
         
         a.recycle();*/
     
 
     public TileView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         init();
     }
 
 
 
     public TileView(Context context) {
         super(context);
         init();
 
     }
 
     
     
     /**
      * Rests the internal array of Bitmaps used for drawing tiles, and
      * sets the maximum index of tiles to be inserted
      * 
      * @param tilecount
      */
     
     public void resetTiles(int tilecount) {
     	mTileArray = new Bitmap[tilecount];
     }
 
 
     /**
      * Function to set the specified Drawable as the tile for a particular
      * integer key.
      * 
      * @param key
      * @param tile
      */
     public void loadTile(int key, Drawable tile) {
         Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(bitmap);
         tile.setBounds(0, 0, mTileSize, mTileSize);
         tile.draw(canvas);
         
         mTileArray[key] = bitmap;
     }
 
     /**
      * Resets all tiles to 0 (empty)
      * 
      */
 /*    public void clearTiles() {
         for (int x = 0; x < mXTileCount; x++) {
             for (int y = 0; y < mYTileCount; y++) {
                 setTile(0, x, y);
             }
         }
     }*/
 
     /**
      * Used to indicate that a particular tile (set with loadTile and referenced
      * by an integer) should be drawn at the given x/y coordinates during the
      * next invalidate/draw cycle.
      * 
      * @param tileindex
      * @param x
      * @param y
      */
     public void setTile(int tileindex, int x, int y) {
         mTileGrid[x][y] = tileindex;
     }
 
 
     @Override
     public void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         for (int j = 0; j < mXTileCount; j += 1) {
             for (int i = 0; i < mYTileCount; i += 1) {
                if (mTileGrid[i][j] > 0) {
                     canvas.drawBitmap(mTileArray[mTileGrid[i][j]], 
                     		mXOffset +  j * mTileSize,
                     		mYOffset +  i * mTileSize,
                     		mPaint);
                 }
             }
         }
     }
     
     public int getI(float x){
     	return (int) FloatMath.floor((x - mXOffset) / mTileSize);
     }
     
     public int getJ(float y){
     	return (int) FloatMath.floor((y - mXOffset) / mTileSize);
     }
     
     public float getX(int i){
     	return (float) (mXOffset +  i * mTileSize);
     }
     
     public float getY(int j){
     	return (float) (mYOffset +  j * mTileSize);
     }
      
     /** @author remi.rischebe **/
     // Evenement du clic souris pour ajout des tours
     @Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(event.getAction() == MotionEvent.ACTION_UP){
 	    	// Conversion des event.x et event.y
 	    	int i = getI(event.getX());
 	    	int j = getJ(event.getY());
 			if(mTileGrid[i][j] == VIDE){	
 				ajout(i, j); // mthode ajout d'une tour
 				
 				}
 			else if(mTileGrid[i][j] == TOUR){	
 				suppression(i, j); // mthode suppresion d'une tour
 				
 				}
 		}
 		return true;
 	}
     
     //Gestion taille ecran 
     @Override
     protected void onSizeChanged(int largeur, int hauteur, int ancien_largeur, int ancien_hauteur) {
     	super.onSizeChanged(largeur, hauteur, ancien_largeur, ancien_hauteur);
 
         mXOffset = ((largeur - (mTileSize * mXTileCount)) / 2);
         mYOffset = ((hauteur - (mTileSize * mYTileCount)) / 2);
         
 		transform = new Matrix();
 		intransform = new Matrix();
 		RectF rectVoulu = new RectF(0, 0, largeur, hauteur);
 		RectF rectReel = new RectF(0, 0, ancien_largeur, ancien_hauteur);
 		transform.setRectToRect(rectVoulu, rectReel, Matrix.ScaleToFit.CENTER);	
 		transform.invert(intransform);
 	}
     
 }
