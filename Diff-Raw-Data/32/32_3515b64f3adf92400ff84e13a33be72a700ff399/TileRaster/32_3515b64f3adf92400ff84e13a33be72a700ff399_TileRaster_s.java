 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2009 Prodevelop.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *   Prodevelop, S.L.
  *   Pza. Don Juan de Villarrasa, 14 - 5
  *   46001 Valencia
  *   Spain
  *
  *   +34 963 510 612
  *   +34 963 510 968
  *   prode@prodevelop.es
  *   http://www.prodevelop.es
  *
  *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
  *   Mediana Empresa de la Comunidad Valenciana) &
  *   European Union FEDER funds.
  *   
  *   2009.
  *   author Rub�n Blanco rblanco@prodevelop.es
  *
  *
  * Original version of the code made by Nicolas Gramlich.
  * No header license code found on the original source file.
  * 
  * Original source code downloaded from http://code.google.com/p/osmdroid/
  * package org.andnav.osm.views.overlay;
  * package org.andnav.osm.views;
  * 
  * License stated in that website is 
  * http://www.gnu.org/licenses/lgpl.html
  * As no specific LGPL version was specified, LGPL license version is LGPL v2.1
  * is assumed.
  *  
  * 
  */
 
 package es.prodevelop.gvsig.mini.views.overlay;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.microlog.core.Logger;
 import net.sf.microlog.core.LoggerFactory;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.os.Message;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Interpolator;
 import android.view.animation.LinearInterpolator;
 import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.activities.Map;
 import es.prodevelop.gvsig.mini.geom.Extent;
 import es.prodevelop.gvsig.mini.geom.Feature;
 import es.prodevelop.gvsig.mini.geom.Pixel;
 import es.prodevelop.gvsig.mini.geom.Point;
 import es.prodevelop.gvsig.mini.map.Downloader;
 import es.prodevelop.gvsig.mini.map.GPSPoint;
 import es.prodevelop.gvsig.mini.map.GeoMath;
 import es.prodevelop.gvsig.mini.map.GeoUtils;
 import es.prodevelop.gvsig.mini.map.LayerChangedListener;
 import es.prodevelop.gvsig.mini.map.Layers;
 import es.prodevelop.gvsig.mini.map.Tile;
 import es.prodevelop.gvsig.mini.map.TileFilesystemProvider;
 import es.prodevelop.gvsig.mini.map.TileProvider;
 import es.prodevelop.gvsig.mini.map.ViewPort;
 import es.prodevelop.gvsig.mini.map.renderer.MapRenderer;
 import es.prodevelop.gvsig.mini.map.renderer.OSMRenderer;
 import es.prodevelop.gvsig.mini.map.renderer.WMSRenderer;
 import es.prodevelop.gvsig.mini.namefinder.NamedMultiPoint;
 import es.prodevelop.gvsig.mini.tasks.WorkQueue;
 import es.prodevelop.gvsig.mini.util.ResourceLoader;
 import es.prodevelop.gvsig.mini.util.Utils;
 import es.prodevelop.gvsig.mini.yours.Route;
 import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
 
 /**
  * The main view to show tiles. This view is contained in a RelativeLayout in
  * the Map Activity.
  * 
  * This class contains a MapRenderer that knows how to convert the current pixel
  * coordinates to coordinates and get the URL string of the tile server
  * 
  * The TileProvider manages the logic of download/load from server/filesystem
  * the tiles
  * 
  * @author aromeu 
  * @author rblanco
  * 
  */
 public class TileRaster extends View implements GeoUtils, OnClickListener,
 		OnLongClickListener, LayerChangedListener {
 
 	AcetateOverlay acetate;
 	boolean panMode = true;
 
 	private boolean zoomflag = false;
 	public int mBearing = 0;
 
 	protected MapRenderer mRendererInfo;
 	protected MapRenderer mRendererInfoAlpha;
 	
 	private final static Logger log = LoggerFactory.getLogger(TileRaster.class);
 
 	public MapRenderer getMRendererInfo() {
 		return mRendererInfo;
 	}
 
 	protected TileProvider mTileProvider;
 
 	protected final GestureDetector mGestureDetector = new GestureDetector(
 			new OpenStreetMapViewGestureDetectorListener());
 
 	protected final List<MapOverlay> mOverlays = new ArrayList<MapOverlay>();
 
 	private int previousRotation = 999;
 	public int pixelX;
 	public int pixelY;
 	public Paint mPaint = new Paint();
 	public Paint rotatePaint = new Paint();
 	int alpha;
 	int mTouchDownX;
 	int mTouchDownY;
 	public static int mTouchMapOffsetX;
 	public static int mTouchMapOffsetY;
 	String datalog = null;
 	Map map;
 	private boolean invalidateActionDown = false;
 	private int prevX = 0;
 	private int prevY = 0;
 	int centerPixelX = 0;
 	int centerPixelY = 0;
 	AndroidGeometryDrawer geomDrawer;
 	public static int mapWidth = 0;
 	public static int mapHeight = 0;
 	public static boolean CLEAR_ROUTE = false;
 	private Feature selectedFeature = null;
 
 	// ZoomRefreshTask zoomTask = new ZoomRefreshTask();
 	// Timer t;
 
 	// private Canvas bufferCanvas = new Canvas();
 	// private Bitmap bufferBitmap;
 	// Paint recPaint;
 	// boolean zoomed = false;
 
 	// public TileRaster(Context context, AttributeSet attrs) {
 	// super(context, attrs);
 	//
 	// this.map = (Map) context;
 	//
 	// this.mTileProvider = new TileProvider(context,
 	// new SimpleInvalidationHandler());
 	// geomDrawer = new AndroidGeometryDrawer(this, context);
 	// this.CIRCLE = BitmapFactory.decodeResource(context.getResources(),
 	// R.drawable.cross);
 	//
 	// }
 	Scaler mScaler;
 //	Canvas bufferCanvas = new Canvas();
 //	Bitmap bufferBitmap;
 //	Canvas frontCanvas = new Canvas();
 //	Bitmap frontBitmap;
 
 	/**
 	 * The Constructor.
 	 * 
 	 * @param context
 	 *            The context (Usually the Map activity)
 	 * @param aRendererInfo
 	 *            A MapRenderer
 	 * @param width
 	 *            The width of the view in pixels
 	 * @param height
 	 *            The height of the view in pixels
 	 */
 	public TileRaster(final Context context, final MapRenderer aRendererInfo,
 			final MapRenderer aRendererInfoAlpha, int width, int height) {
 		super(context);
 		try {			
 			TileRaster.this.rotatePaint.setFlags(Paint.FILTER_BITMAP_FLAG);
 			log.setLevel(Utils.LOG_LEVEL);
 			log.setClientID(this.toString());
 			this.mScaler = new Scaler(context, new LinearInterpolator());
 			this.mTileProvider = new TileProvider(context,
 					new SimpleInvalidationHandler(), width, height, 256);
 			this.map = (Map) context;
 			this.setRenderer(aRendererInfo, aRendererInfoAlpha);
 			geomDrawer = new AndroidGeometryDrawer(this, context);
 			acetate = new AcetateOverlay(context, this);
 			this.mCurrentAnimationRunner = new LinearAnimationRunner(0, 0,
 					false);
 			this.setFocusable(true);
 			this.setClickable(true);
 			this.setLongClickable(true);
 			this.setOnClickListener(this);
 			this.setOnLongClickListener(this);
 			this.alpha = 255;
 		} catch (Exception e) {
 			log.error("onCreate:", e);
 		} catch (OutOfMemoryError e) {
 			System.gc();
 			log.error("onCreate:", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @return A List of the current visible MapOverlays
 	 */
 	public List<MapOverlay> getOverlays() {
 		return this.mOverlays;
 	}
 
 	/**
 	 * Sets the center of the Map from a GPSPoint and postinvalidates the view
 	 * 
 	 * @param aCenter
 	 *            A GPSPoint
 	 */
 	public void setMapCenter(final GPSPoint aCenter) {
 		try {
 			this.setMapCenterFromLonLat(aCenter.getLongitudeE6() / 1E6, aCenter
 					.getLatitudeE6() / 1E6);
 
 		} catch (Exception e) {
 			log.error("setMapCenter:", e);
 		}
 	}
 
 	/**
 	 * Checks if the max extent of the MapRenderer contains the xy, sets the
 	 * center and postinvalidates the view. The coordinates x,y need to be in
 	 * the same SRS as the MapRenderer
 	 * 
 	 * @param x
 	 *            the x coordinate
 	 * @param y
 	 *            the y coordinate
 	 */
 	public void setMapCenter(final double x, final double y) {
 		try {
 			if (this.mRendererInfo.getExtent().contains(x, y)) {
 				this.mRendererInfo.setCenter(x, y);
 				this.mRendererInfoAlpha.setCenter(x, y);
 				postInvalidate();
 			}
 		} catch (Exception e) {
 			log.error("setMapCenter:", e);
 		}
 	}
 
 	/**
 	 * Sets the current MapRenderer of the view and centers the view on the
 	 * center of the max extent of the MapRenderer
 	 * 
 	 * @param aRenderer
 	 *            A MapRenderer
 	 */
 	public void setRenderer(final MapRenderer aRenderer, final MapRenderer aRendererAlpha) {
 		try {
 			log.debug("set MapRenderer: " + aRenderer.toString());
 			this.mRendererInfo = aRenderer;
 			this.mRendererInfoAlpha = aRendererAlpha;
 			this.map.vp = new ViewPort();
 			this.mRendererInfo.setCenter(aRenderer.getExtent().getCenter()
 					.getX(), aRenderer.getExtent().getCenter().getY());
 			this.mRendererInfoAlpha.setCenter(aRendererAlpha.getExtent().getCenter()
 					.getX(), aRendererAlpha.getExtent().getCenter().getY());
 			this.map.vp.resolutions = aRenderer.resolutions;
 			this.map.vp.setDist1Pixel(this.map.vp.resolutions[0]);
 			this.map.vp.origin = new es.prodevelop.gvsig.mini.geom.Point(
 					aRenderer.getOriginX(), aRenderer.getOriginY());
 			return;
 
 		} catch (Exception e) {
 			log.error("setRenderer:", e);
 		}
 	}
 
 	/**
 	 * Sets a zoom level that fits a resolution. This method is useful for
 	 * zoomRectangle, zooming to a route, to a multipoint, etc.
 	 * 
 	 * @param resolution
 	 *            The resolution in the units of the projection of the
 	 *            MapRenderer
 	 * @see CRSFactory
 	 */
 	public void setZoomLevelFromResolution(double resolution) {
 		try {
 			final double[] resolutions = this.getMRendererInfo().resolutions;
 			final int size = resolutions.length;
 
 			double current = 0;
 			double moreAccurate = -1;
 			int zoomLevel = -1;
 			for (int i = 0; i < size; i++) {
 				current = resolutions[i];
 				double diff = current - resolution;
 				if (diff > 0) {
 					double moreAccurateDiff = current - moreAccurate;
 					if (diff < moreAccurateDiff) {
 						moreAccurateDiff = diff;
 						zoomLevel = i;
 					}
 				}
 			}
 			if (zoomLevel != -1) {
 				this.setZoomLevel(zoomLevel);
 			}
 		} catch (Exception e) {
 			log.error("setZoomLevelFromResolution", e);
 		}
 	}
 
 	/**
 	 * Sets the zoom level, updates the zoom controls and postInvalidates the
 	 * view
 	 * 
 	 * @param aZoomLevel
 	 *            The zoom level
 	 */
 	public void setZoomLevel(final int aZoomLevel) {
 		final int zoomLevel = Math.max(0, Math.min(this.mRendererInfo
 				.getZOOM_MAXLEVEL(), aZoomLevel));
 		tempZoomLevel = zoomLevel;
 
 		try {
 			this.mRendererInfo.setZoomLevel(zoomLevel);
 			this.mRendererInfoAlpha.setZoomLevel(zoomLevel);
 			map.vp.setDist1Pixel(map.vp.resolutions[zoomLevel]);
 		} catch (Exception e) {
 			log.error("setZoomLevel:", e);
 		}
 		map.updateZoomControl();
 		cleanZoomRectangle();
 		this.postInvalidate();
 	}
 
 	/**
 	 * Sets the zoom level and applies a LinearInterpolation Scaler to the view
 	 * 
 	 * @param aZoomLevel
 	 *            The zoom level
 	 * @param several
 	 *            If changes several zooms level applies the Scaler
 	 */
 	public void setZoomLevel(final int aZoomLevel, boolean several) {
 		try {
 			int levels = aZoomLevel - this.getMRendererInfo().getZoomLevel();
 			tempZoomLevel = aZoomLevel;
 			if (several) {
 				// Zoom in
 				if (levels > 0) {
 					if (mScaler.isFinished()) {
 						mScaler.startScale(1.0f, (float) (levels * 2.0f),
 								Scaler.DURATION_SHORT);
 						postInvalidate();
 					} else {
 						mScaler.extendDuration(Scaler.DURATION_SHORT);
 						mScaler.setFinalScale(mScaler.getFinalScale() * 2.0f);
 					}
 				} else {
 					levels = Math.abs(1 / levels);
 					if (mScaler.isFinished()) {
 						mScaler.startScale(1.0f, (float) (levels / 2.0f),
 								Scaler.DURATION_SHORT);
 						postInvalidate();
 					} else {
 						mScaler.extendDuration(Scaler.DURATION_SHORT);
 						mScaler.setFinalScale(mScaler.getFinalScale() * 0.5f);
 					}
 				}
 			} else {
 				this.setZoomLevel(aZoomLevel);
 			}
 		} catch (Exception e) {
 			log.error("setZoomLevel", e);
 		}
 	}
 
 	void onScalingFinished() {
 //		frontCanvas.drawBitmap(bufferBitmap, 0, 0, normalPaint);
 		setZoomLevel(tempZoomLevel);
 	}
 
 	protected void computeScale() {
 		if (mScaler.computeScale()) {
 			if (mScaler.isFinished())
 				onScalingFinished();
 			else
 				postInvalidate();
 		}
 	}
 
 	private int tempZoomLevel = 0;
 
 	/**
 	 * 
 	 * @return The final zoom level after the Scale interpolation ends
 	 */
 	public int getTempZoomLevel() {
 		return tempZoomLevel;
 	}
 
 	/**
 	 * Increase the zoom of the view and applies the Scaler
 	 */
 	public void zoomIn() {
 		try {
 			this.mTileProvider.clearPendingQueue();
 			tempZoomLevel += 1;
 			// this.setZoomLevel(this.getMRendererInfo().getZoomLevel() + 1);
 
 			if (mScaler.isFinished()) {
 				mScaler.startScale(1.0f, 2.0f, Scaler.DURATION_SHORT);
 				postInvalidate();
 			} else {
 				mScaler.extendDuration(Scaler.DURATION_SHORT);
 				mScaler.setFinalScale(mScaler.getFinalScale() * 2.0f);
 			}
 
 		} catch (Exception e) {
 			log.error("zoomIn:", e);
 		}
 	}
 
 	/**
 	 * Decreases the zoom level of the view applying a Scaler
 	 */
 	public void zoomOut() {
 		try {
 			this.mTileProvider.clearPendingQueue();
 			if (this.getMRendererInfo().getZoomLevel() != 0) {
 				this.tempZoomLevel -= 1;
 				if (mScaler.isFinished()) {
 					mScaler.startScale(1.0f, 0.5f, Scaler.DURATION_SHORT);
 					postInvalidate();
 				} else {
 					mScaler.extendDuration(Scaler.DURATION_SHORT);
 					mScaler.setFinalScale(mScaler.getFinalScale() * 0.5f);
 				}
 			}
 		} catch (Exception e) {
 			log.error("zoomOut:", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @return The current zoom level of the view
 	 */
 	public int getZoomLevel() {
 		return this.getMRendererInfo().getZoomLevel();
 	}
 
 	public int i = 0;
 
 	public boolean onLongPress(MotionEvent e) {
 		if (!panMode || map.navigation) {
 			log.debug("longpress on pan mode or navigation does not work");
 			return false;
 		}
 
 		try {
 			if (TileRaster.this.mTouchMapOffsetX < ResourceLoader.MIN_PAN
 					&& TileRaster.this.mTouchMapOffsetY < ResourceLoader.MIN_PAN
 					&& TileRaster.this.mTouchMapOffsetX > -ResourceLoader.MIN_PAN
 					&& TileRaster.this.mTouchMapOffsetY > -ResourceLoader.MIN_PAN) {
 
 				for (MapOverlay osmvo : this.mOverlays)
 					if (osmvo.onLongPress(e, this)) {
 						map.showContext(osmvo.getItemContext());
 						return true;
 					}
 
 				double[] coords = TileRaster.this.getMRendererInfo()
 						.fromPixels(
 								new int[] { (int) e.getX(), (int) e.getY() });
 				TileRaster.this.getMRendererInfo().getZOOM_MAXLEVEL();
 
 				this.animateTo(coords[0], coords[1]);
 				map.showContext(map.getItemContext());
 
 				pixelX = (int) e.getX();
 				pixelY = (int) e.getY();
 			}
 		} catch (Exception ex) {
 			log.error("onLongPress:", ex);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		try {
 			for (MapOverlay osmvo : this.mOverlays)
 				if (osmvo.onKeyDown(keyCode, event, this))
 					return true;
 
 		} catch (Exception e) {
 			log.error("onkeydown:", e);
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		try {
 			for (MapOverlay osmvo : this.mOverlays)
 				if (osmvo.onKeyUp(keyCode, event, this))
 					return true;
 		} catch (Exception e) {
 			log.error("onkeyUp:", e);
 		}
 
 		return super.onKeyUp(keyCode, event);
 	}
 
 	@Override
 	public boolean onTrackballEvent(MotionEvent event) {
 		try {
 			int x = (int) Math.ceil(event.getX() * 10);
 			int y = (int) Math.ceil(event.getY() * 10);
 			double[] coords = TileRaster.this.getMRendererInfo()
 					.fromPixels(
 							new int[] { (int) centerPixelX + x,
 									(int) centerPixelY + y });
 			this.setMapCenter(coords[0], coords[1]);
 			for (MapOverlay osmvo : this.mOverlays)
 				if (osmvo.onTrackballEvent(event, this))
 					return true;
 		} catch (Exception e) {
 			log.error("onTrackBallEvent", e);
 		}
 		return super.onTrackballEvent(event);
 	}
 
 	@Override
 	public boolean onTouchEvent(final MotionEvent event) {
 		try {
 			if (!map.navigation) {
 				for (MapOverlay osmvo : this.mOverlays)
 					if (osmvo.onTouchEvent(event, this))
 						return true;
 
 				this.mGestureDetector.onTouchEvent(event);
 				acetate.onTouchEvent(event);
 			}
 
 		} catch (Exception e) {
 			log.error("onTouchEvent:", e);
 		}
 
 		return super.onTouchEvent(event);
 
 	}
 	
 	private void drawLayer(final Canvas c, MapRenderer renderer, int alpha) {
 		Paint paint = new Paint();
 		paint.setAlpha(alpha);
 		
 		mapWidth = getWidth();
 		mapHeight = getHeight();
 
		if (!mScaler.isFinished()) {
			Matrix m = c.getMatrix();
			m.preScale(mScaler.mCurrScale, mScaler.mCurrScale,
					c.getWidth() / 2, c.getHeight() / 2);						
			
			c.setMatrix(m);
		}

 		if (map.navigation) {				
 			paint = rotatePaint;
 			int rotationToDraw = -mBearing
 					- map.getmMyLocationOverlay().getOffsetOrientation();
 			if (Math.abs(Math.abs(rotationToDraw)
 					- Math.abs(previousRotation)) < Utils.MIN_ROTATION) {
 				rotationToDraw = previousRotation;
 			} else {
 				previousRotation = rotationToDraw;
 			}
 			c.rotate(rotationToDraw, mapWidth/2, mapHeight/ 2);
 		}
 
 		final int zoomLevel = renderer.getZoomLevel();
 		final int viewWidth = this.getWidth();
 		final int viewHeight = this.getHeight();
 		this.centerPixelX = this.getWidth() / 2;
 		this.centerPixelY = this.getHeight() / 2;
 		final int tileSizePx = renderer.getMAPTILE_SIZEPX();
 
 		int[] centerMapTileCoords = renderer.getMapTileFromCenter();
 
 		final int additionalTilesNeededToLeftOfCenter;
 		final int additionalTilesNeededToRightOfCenter;
 		final int additionalTilesNeededToTopOfCenter;
 		final int additionalTilesNeededToBottomOfCenter;
 
 		Pixel upperLeftCornerOfCenterMapTile = renderer
 				.getUpperLeftCornerOfCenterMapTileInScreen(null);
 
 		final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile
 				.getX();
 		final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile
 				.getY();
 
 		final int centerMapTileScreenRight = centerMapTileScreenLeft
 				+ tileSizePx;
 		final int centerMapTileScreenBottom = centerMapTileScreenTop
 				+ tileSizePx;
 		if (!map.navigation) {
 
 			additionalTilesNeededToLeftOfCenter = (int) Math
 					.ceil((float) centerMapTileScreenLeft / tileSizePx);
 			additionalTilesNeededToRightOfCenter = (int) Math
 					.ceil((float) (viewWidth - centerMapTileScreenRight)
 							/ tileSizePx);
 			additionalTilesNeededToTopOfCenter = (int) Math
 					.ceil((float) centerMapTileScreenTop / tileSizePx);
 			additionalTilesNeededToBottomOfCenter = (int) Math
 					.ceil((float) (viewHeight - centerMapTileScreenBottom)
 							/ tileSizePx);
 		} else {
 
 			additionalTilesNeededToLeftOfCenter = (int) Math
 					.ceil((float) (viewWidth + centerMapTileScreenLeft)
 							/ tileSizePx);
 			additionalTilesNeededToRightOfCenter = (int) Math
 					.ceil((float) (Utils.ROTATE_BUFFER_SIZE * viewWidth - centerMapTileScreenRight)
 							/ tileSizePx);
 			additionalTilesNeededToTopOfCenter = (int) Math
 					.ceil((float) (viewHeight + centerMapTileScreenTop)
 							/ tileSizePx);
 			additionalTilesNeededToBottomOfCenter = (int) Math
 					.ceil((float) (Utils.ROTATE_BUFFER_SIZE * viewHeight - centerMapTileScreenBottom)
 							/ tileSizePx);
 
 		}
 		final int mapTileUpperBound = (int) Math.pow(2, zoomLevel + 1) + 1;
 		final int[] mapTileCoords = new int[] {
 				centerMapTileCoords[MAPTILE_LATITUDE_INDEX],
 				centerMapTileCoords[MAPTILE_LONGITUDE_INDEX] };
 
 		final int size = (additionalTilesNeededToBottomOfCenter
 				+ additionalTilesNeededToTopOfCenter + 1)
 				* (additionalTilesNeededToRightOfCenter
 						+ additionalTilesNeededToLeftOfCenter + 1);
 		Tile[] tiles = new Tile[size];
 		int cont = 0;
 
 		final Extent maxExtent = renderer.getExtent();
 		final Extent viewExtent = map.vp.calculateExtent(mapWidth,
 				mapHeight, renderer.getCenter());
 
 		boolean process = true;
 
 		for (int y = -additionalTilesNeededToTopOfCenter; y <= additionalTilesNeededToBottomOfCenter; y++) {
 			for (int x = -additionalTilesNeededToLeftOfCenter; x <= additionalTilesNeededToRightOfCenter; x++) {
 				process = true;
 				if (viewExtent.intersect(maxExtent)) {
 					final int tileLeft = this.mTouchMapOffsetX
 							+ centerMapTileScreenLeft + (x * tileSizePx);
 					final int tileTop = this.mTouchMapOffsetY
 							+ centerMapTileScreenTop + (y * tileSizePx);
 
 					double[] coords = new double[] {
 							maxExtent.getLefBottomCoordinate().getX(),
 							maxExtent.getLefBottomCoordinate().getY() };
 
 					final int[] leftBottom = renderer.toPixels(coords);
 					coords = new double[] {
 							maxExtent.getRightTopCoordinate().getX(),
 							maxExtent.getRightTopCoordinate().getY() };
 					final int[] rightTop = renderer.toPixels(coords);
 
 					final Rect r = new Rect();
 					r.bottom = leftBottom[1];
 					r.left = leftBottom[0];
 					r.right = rightTop[0];
 					r.top = rightTop[1];
 
 					process = r.intersects(tileLeft, tileTop, tileLeft
 							+ tileSizePx / 2, tileTop + tileSizePx / 2);
 
 				}
 
 				if (process) {
 					mapTileCoords[MAPTILE_LATITUDE_INDEX] = GeoMath.mod(
 							centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
 									+ renderer.isTMS() * y,
 							mapTileUpperBound);
 
 					mapTileCoords[MAPTILE_LONGITUDE_INDEX] = GeoMath.mod(
 							centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
 									+ x, mapTileUpperBound);
 
 					final String tileURLString = renderer
 							.getTileURLString(mapTileCoords, zoomLevel);
 
 					final int[] tile = new int[] { mapTileCoords[0],
 							mapTileCoords[1] };
 
 					final int tileLeft = this.mTouchMapOffsetX
 							+ centerMapTileScreenLeft + (x * tileSizePx);
 					final int tileTop = this.mTouchMapOffsetY
 							+ centerMapTileScreenTop + (y * tileSizePx);
 					
 					final Tile t = new Tile(tileURLString, tile, new Pixel(
 							tileLeft, tileTop));
 					
 					if (cont < tiles.length)
 						tiles[cont] = t;
 
 					if (DEBUGMODE) {
 						c
 								.drawLine(tileLeft, tileTop, tileLeft
 										+ tileSizePx, tileTop, paint);
 						c.drawLine(tileLeft, tileTop, tileLeft, tileTop
 								+ tileSizePx, paint);
 					}
 				}
 				cont++;
 			}
 		}
 
 		this.sortTiles(tiles);
 		final int length = tiles.length;
 		Tile temp;
 		for (int j = 0; j < length; j++) {
 			temp = tiles[j];
 			if (temp != null) {
 				final Bitmap currentMapTile = this.mTileProvider
 					.getMapTile(temp.mURL, temp.tile,
 						renderer.getNAME(), this
 							.getZoomLevel());
 				
 				if (currentMapTile != null) {
 					c.drawBitmap(currentMapTile, temp.distanceFromCenter
 								.getX(), temp.distanceFromCenter.getY(),
 								paint);
 					
 					temp.destroy();
 					temp = null;
 				}
 			}
 		}
 
 		tiles = null;
 	}
 
 	@Override
 	public void onDraw(final Canvas c) {
 		try {
 			drawLayer(c, mRendererInfo, 255);
 			drawLayer(c, mRendererInfoAlpha, this.alpha);
 
 			/* Draw all Overlays. */
 			for (MapOverlay osmvo : this.mOverlays)
 				osmvo.onManagedDraw(c, this);
 
 			acetate.onManagedDraw(c, this);
 
 			computeScale();		
 		} catch (Exception e) {
 			log.error("onDraw", e);
 		}
 
 	}
 
 	/**
 	 * Sorts the tiles from the center of the screen to outside to load the
 	 * tiles in spiral.
 	 * 
 	 * @param array2
 	 *            the array of tiles
 	 */
 	protected void sortTiles(final Tile[] array2) {
 		try {
 			final Pixel center = new Pixel(centerPixelX, centerPixelY);
 			double e1, e2;
 			final int length = array2.length;
 			Tile t;
 			Tile t1;
 			final Pixel temp = new Pixel((256 / 2),
 					(256 / 2));
 			for (int pass = 1; pass < length; pass++) {
 				for (int element = 0; element < length - 1; element++) {
 					t = array2[element];
 					t1 = array2[element + 1];
 					if (t != null && t1 != null) {
 						e1 = (t.distanceFromCenter.add(temp)).distance(center);
 						e2 = (t1.distanceFromCenter.add(temp)).distance(center);
 						if (e1 > e2) {
 							swap(array2, element, element + 1);
 						}
 					}
 				}
 			}
 		} catch (final Exception e) {
 			log.error("sortTiles:", e); 
 		}
 	}
 
 	private void swap(final Tile[] array3, final int first, final int second) {
 		try {
 			final Tile hold = array3[first];
 
 			array3[first] = array3[second];
 			array3[second] = hold;
 		} catch (final Exception e) {
 			log.error("swap", e);
 		}
 	}
 
 	private class SimpleInvalidationHandler extends Handler {
 
 		@Override
 		public void handleMessage(final Message msg) {
 			try {
 				switch (msg.what) {
 				case Downloader.MAPTILEDOWNLOADER_SUCCESS_ID:
 					// if (!Utils.isSDMounted()) {
 					TileRaster.this.invalidate();
 					// }
 					break;
 				case TileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
 					TileRaster.this.invalidate();
 					break;
 				}
 			} catch (Exception e) {
 				log.error("SimpleInvalidationHandler", e);
 			}
 		}
 	}
 
 	private class OpenStreetMapViewGestureDetectorListener implements
 			OnGestureListener, OnDoubleTapListener {
 
 		@Override
 		public boolean onDown(MotionEvent e) {
 			return false;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			return false;
 		}
 
 		@Override
 		public void onLongPress(MotionEvent e) {
 			try {
 				TileRaster.this.onLongPress(e);
 			} catch (Exception est) {
 				log.error(est);
 			}
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			return false;
 		}
 
 		@Override
 		public void onShowPress(MotionEvent e) {
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			try {
 				log.debug("double tap");
 				double[] coords = TileRaster.this.getMRendererInfo()
 						.fromPixels(
 								new int[] { (int) e.getX(), (int) e.getY() });
 				TileRaster.this.setMapCenter(coords[0], coords[1]);
 				TileRaster.this.zoomIn();
 			} catch (Exception ex) {
 				log.error("doubletap", ex);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onDoubleTapEvent(MotionEvent arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 
 			try {
 				for (MapOverlay osmvo : TileRaster.this.mOverlays)
 					if (osmvo.onSingleTapUp(e, TileRaster.this))
 						return true;
 				
 			} catch (Exception ex) {
 				log.error("singletapconfirmed", ex);
 
 			}
 			return false;
 		}
 
 		@Override
 		public boolean onSingleTapUp(MotionEvent arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 	}
 
 	@Override
 	public void onClick(View view) {
 		try {
 			// boolean handled =
 			// this.onSingleTapUpConfirmed(MotionEvent.obtain(0, 0, 0,
 			// centerPixelX, centerPixelY, 0));
 
 			// if (!handled) {
 			// this.zoomIn();
 			// }
 		} catch (Exception e) {
 			log.error(e);
 		}
 	}
 
 	@Override
 	public boolean onLongClick(View view) {
 		try {
 			// boolean handled = this.onLongPress(MotionEvent.obtain(0, 0, 0,
 			// centerPixelX, centerPixelY, 0));
 
 			// if (!handled) {
 			// zoomOut();
 			// }
 		} catch (Exception e) {
 			log.error(e);
 		}
 		return true;
 	}
 
 	@Override
 	public void onLayerChanged(String layerName) {
 		try {
 			log.debug("on layer changed");
 			this.clearCache();
 			MapRenderer previous = this.getMRendererInfo();
 			MapRenderer renderer = Layers.getInstance().getRenderer(layerName);
 
 			log.debug("from: " + previous.toString());
 			log.debug("to: " + renderer.toString());
 
 			if (renderer instanceof WMSRenderer && !map.navigation) {
 				log.debug("Decreasing size of memory cache");
 				Utils.ROTATE_BUFFER_SIZE = 1;
 			} else {
 				Utils.ROTATE_BUFFER_SIZE = 1;
 			}
 
 			mTileProvider.destroy();
 
 			Utils.BUFFER_SIZE = 2;
 			mTileProvider = new TileProvider(map,
 					new SimpleInvalidationHandler(), mapWidth, mapHeight, 256);
 			Extent previousExtent = map.vp.calculateExtent(mapWidth, mapHeight,
 					previous.getCenter());
 			if (renderer != null)
 				this.setRenderer(renderer, this.mRendererInfoAlpha);
 
 			try {
 				final Route route = map.route;
 				if (route != null)
 					renderer.reprojectGeometryCoordinates(route.getRoute()
 							.getFeatureAt(0).getGeometry(), previous.getSRS());
 			} catch (Exception e) {
 				log.error("reprojecting route:", e);
 			}
 
 			try {
 				final NamedMultiPoint nm = map.nameds;
 				if (nm != null)
 					renderer
 							.reprojectGeometryCoordinates(nm, previous.getSRS());
 			} catch (Exception e) {
 				log.error("reprojecting namefinder:", e);
 			}
 
 			double[] newCenter = previous.transformCenter(renderer.getSRS());
 			boolean contains = renderer.getExtent().contains(newCenter);
 
 			double[] minXY = ConversionCoords.reproject(previousExtent
 					.getMinX(), previousExtent.getMinY(), CRSFactory
 					.getCRS(previous.getSRS()), CRSFactory.getCRS(renderer
 					.getSRS()));
 			double[] maxXY = ConversionCoords.reproject(previousExtent
 					.getMaxX(), previousExtent.getMaxY(), CRSFactory
 					.getCRS(previous.getSRS()), CRSFactory.getCRS(renderer
 					.getSRS()));
 			Extent currentExtent = new Extent(minXY[0], minXY[1], maxXY[0],
 					maxXY[1]);
 			if (contains) {
 				this.setMapCenter(newCenter[0], newCenter[1]);
 				if (previous instanceof OSMRenderer
 						&& renderer instanceof OSMRenderer) {
 					setZoomLevel(previous.getZoomLevel(), false);
 				} else {
 					this.zoomToExtent(currentExtent, true);
 				}
 			} else {
 				Point p = renderer.getExtent().getCenter();
 				this.setMapCenter(p.getX(), p.getY());
 			}
 
 			map.persist();
 		} catch (Exception e) {
 			log.error("onlayerchanged:", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @return The center of the map expressed in SRS:4326
 	 */
 	public double[] getCenterLonLat() {
 		double[] res = null;
 		try {
 			final MapRenderer renderer = this.mRendererInfo;
 			res = ConversionCoords.reproject(renderer.getCenter().getX(),
 					renderer.getCenter().getY(), CRSFactory.getCRS(renderer
 							.getSRS()), CRSFactory.getCRS("EPSG:4326"));
 		} catch (Exception e) {
 			log.error("getCenterLonLat:", e);
 		}
 		return res;
 	}
 
 	/**
 	 * Sets the center of the map from a longitude, latitude. Also makes the
 	 * coordinates conversion to the SRS of the MapRenderer
 	 * 
 	 * @param lon
 	 *            The longitude
 	 * @param lat
 	 *            The latitude
 	 */
 	public void setMapCenterFromLonLat(double lon, double lat) {
 		try {
 			if (lon == 0.0 && lat == 0.0)
 				return;
 			double[] coords = ConversionCoords.reproject(lon, lat, CRSFactory
 					.getCRS("EPSG:4326"), CRSFactory.getCRS(this.mRendererInfo
 					.getSRS()));
 			this.setMapCenter(coords[0], coords[1]);
 
 		} catch (Exception e) {
 			log.error("setMapCenterFromLonLat", e);
 		}
 	}
 
 	/**
 	 * Zooms the view to fit an Extent
 	 * 
 	 * @param extent
 	 *            The extent to fit
 	 * @param layerChanged
 	 *            If true then the Scaler is not applied when zooming
 	 */
 	public void zoomToExtent(final Extent extent, boolean layerChanged) {
 		zoomToSpan(extent.getWidth(), extent.getHeight(), layerChanged);
 	}
 
 	/**
 	 * Zooms the view to a width, height
 	 * 
 	 * @param width
 	 *            The width to fit the zoom
 	 * @param height
 	 *            The height to fit the zoom
 	 * @param layerChanged
 	 *            If true then the Scaler is not applied when zooming
 	 */
 	public void zoomToSpan(final double width, final double height,
 			boolean layerChanged) {
 		if (width <= 0 || height <= 0) {
 			return;
 		}
 
 		int zoomLevel = getZoomLevelFitsExtent(width, height);
 		if (zoomLevel != -1) {
 			if (!layerChanged && zoomLevel <= this.getZoomLevel())
 				zoomLevel++;
 			setZoomLevel(zoomLevel, !layerChanged);
 		}
 	}
 
 	/**
 	 * Calculates a zoom level that fits an extent
 	 * 
 	 * @param width
 	 *            The width of the extent
 	 * @param height
 	 *            The height of the extent
 	 * @return The zoom level that fits the extent
 	 */
 	public int getZoomLevelFitsExtent(final double width, final double height) {
 		final Extent mapExtent = map.vp.calculateExtent(mapWidth, mapHeight,
 				this.getMRendererInfo().getCenter());
 		final int curZoomLevel = getZoomLevel();
 
 		final double curWidth = mapExtent.getWidth();
 		final double curHeight = mapExtent.getHeight();
 
 		final double diffNeededX = width / curWidth;
 		final double diffNeededY = height / curHeight;
 
 		if (Double.isInfinite(diffNeededX) || Double.isInfinite(diffNeededY)) {
 			return -1;
 		}
 
 		final double diffNeeded = Math.max(diffNeededX, diffNeededY);
 
 		if (diffNeeded > 1) {
 			return curZoomLevel - Utils.getNextSquareNumberAbove(diffNeeded);
 		} else if (diffNeeded < 0.5) {
 			return curZoomLevel
 					+ Utils.getNextSquareNumberAbove(1 / diffNeeded) - 1;
 		} else {
 			return curZoomLevel;
 		}
 	}
 
 	// @Override
 	// public void postInvalidate() {
 	// if (refresh)
 	// super.postInvalidate();
 	// }
 	//
 	// @Override
 	// public void invalidate() {
 	// if (refresh)
 	// super.invalidate();
 	// }
 
 	// public boolean refresh = true;
 	//
 	// private class ZoomRefreshTask extends TimerTask {
 	//
 	// @Override
 	// public void run() {
 	// refresh = true;
 	// postInvalidate();
 	// try {
 	// this.finalize();
 	// } catch (Throwable e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	// }
 	//
 	// }
 
 	AnimationRunner mCurrentAnimationRunner;
 
 	/**
 	 * Applies a LinearAnimation to center the view to a xy coordinates
 	 * 
 	 * @param x
 	 *            The x coordinate
 	 * @param y
 	 *            The y coordinate
 	 */
 	public void animateTo(final double x, final double y) {
 		// this.stopAnimation(false);
 		// log.debug("animate TO");
 		mCurrentAnimationRunner = new LinearAnimationRunner(x, y, true);
 		WorkQueue.getExclusiveInstance().execute(mCurrentAnimationRunner);
 
 	}
 
 	/**
 	 * Stops a running animation.
 	 * 
 	 * @param jumpToTarget
 	 */
 	public void stopAnimation(final boolean jumpToTarget) {
 		final AnimationRunner currentAnimationRunner = this.mCurrentAnimationRunner;
 
 		if (currentAnimationRunner != null && !currentAnimationRunner.isDone()) {
 			// currentAnimationRunner.interrupt();
 			if (jumpToTarget) {
 				this.setMapCenter(currentAnimationRunner.mTargetX,
 						currentAnimationRunner.mTargetY);
 			}
 		}
 	}
 
 	// protected Object lock = new Object();
 
 	private abstract class AnimationRunner implements Runnable {
 
 		protected final int mSmoothness, mDuration;
 		protected final double mTargetX, mTargetY;
 		protected boolean mDone = false;
 
 		protected final int mStepDuration;
 
 		protected final double mPanTotalX, mPanTotalY;
 
 		public AnimationRunner(final double aTargetX, final double aTargetY) {
 			this(aTargetX, aTargetY, 10, 1000);
 		}
 
 		public abstract void setTarget(final double x, final double y);
 
 		public AnimationRunner(final double aTargetX, final double aTargetY,
 				final int aSmoothness, final int aDuration) {
 			this.mTargetX = aTargetX;
 			this.mTargetY = aTargetY;
 			this.mSmoothness = aSmoothness;
 			this.mDuration = aDuration;
 			this.mStepDuration = aDuration / aSmoothness;
 
 			Point center = TileRaster.this.getMRendererInfo().getCenter();
 
 			final double mapCenterX = center.getX();
 			final double mapCenterY = center.getY();
 
 			this.mPanTotalX = (mapCenterX - aTargetX);
 			this.mPanTotalY = (mapCenterY - aTargetY);
 		}
 
 		@Override
 		public void run() {
 			onRunAnimation();
 			this.mDone = true;
 		}
 
 		public boolean isDone() {
 			return this.mDone;
 		}
 
 		public abstract void onRunAnimation();
 	}
 
 	private class LinearAnimationRunner extends AnimationRunner {
 
 		protected double mPanPerStepX, mPanPerStepY;
 
 		public LinearAnimationRunner(final double aTargetX,
 				final double aTargetY, final boolean animate) {
 			this(aTargetX, aTargetY, 10, 1000, animate);
 			// start();
 		}
 
 		public LinearAnimationRunner(final double aTargetX,
 				final double aTargetY, final int aSmoothness,
 				final int aDuration, final boolean animate) {
 			super(aTargetX, aTargetY, aSmoothness, aDuration);
 			if (animate)
 				setTarget(aTargetX, aTargetY, aSmoothness);
 		}
 
 		public void setTarget(final double x, final double y,
 				final int aSmoothness) {
 			Point center = TileRaster.this.getMRendererInfo().getCenter();
 			final double mapCenterX = center.getX();
 			final double mapCenterY = center.getY();
 
 			this.mPanPerStepX = (mapCenterX - x) / aSmoothness;
 			this.mPanPerStepY = (mapCenterY - y) / aSmoothness;
 
 			// this.setName("LinearAnimationRunner");
 		}
 
 		public void setTarget(final double x, final double y) {
 			setTarget(x, y, 10);
 		}
 
 		@Override
 		public void onRunAnimation() {
 			final double panPerStepX = this.mPanPerStepX;
 			final double panPerStepY = this.mPanPerStepY;
 			final int stepDuration = this.mStepDuration;
 			try {
 				// while (!mDone) {
 				// synchronized (lock) {
 				// try {
 				// lock.wait();
 				// } catch (InterruptedException ignored) {
 				//
 				// }
 				// }
 
 				double newMapCenterX;
 				double newMapCenterY;
 
 				Point center;
 
 				for (int i = this.mSmoothness; i > 0; i--) {
 					center = TileRaster.this.getMRendererInfo().getCenter();
 
 					newMapCenterX = center.getX() - panPerStepX;
 					newMapCenterY = center.getY() - panPerStepY;
 					setMapCenter(newMapCenterX, newMapCenterY);
 
 					Thread.sleep(stepDuration);
 				}
 				setMapCenter(super.mTargetX, super.mTargetY);
 				// }
 			} catch (final Exception e) {
 				// this.interrupt();
 			}
 		}
 	}
 
 	/**
 	 * Switch from panMode to rectangleMode and viceversa
 	 */
 	public void switchPanMode() {
 		panMode = !panMode;
 	}
 
 	/**
 	 * Calculates the size of the rectangle to mark the zoom level to apply when
 	 * zooming in, and tells the AcetateOverlay to draw it
 	 * 
 	 * @param zoom
 	 */
 	public void drawZoomRectangle(int zoom) {
 		try {
 			double res = this.getMRendererInfo().resolutions[zoom];
 			final Point center = this.getMRendererInfo().getCenter();
 			Extent extent = map.vp.calculateExtent(center, res, mapWidth,
 					mapHeight);
 			Extent currentExtent = map.vp.calculateExtent(mapWidth, mapHeight,
 					center);
 			double[] minXY = new double[] { extent.getMinX(), extent.getMinY() };
 			double[] maxXY = new double[] { extent.getMaxX(), extent.getMaxY() };
 			int[] leftBottom = map.vp.fromMapPoint(minXY, currentExtent
 					.getMinX(), currentExtent.getMaxY());
 			int[] rightTop = map.vp.fromMapPoint(maxXY,
 					currentExtent.getMinX(), currentExtent.getMaxY());
 			Rect r = new Rect();
 			r.set(leftBottom[0], rightTop[1], rightTop[0], leftBottom[1]);
 			if (leftBottom[0] <= 0 || leftBottom[1] >= mapHeight
 					|| rightTop[1] <= 0 || rightTop[0] >= mapWidth)
 				return;
 			acetate.drawZoomRectangle(r);
 		} catch (Exception e) {
 			log.error("drawZoomRectangle", e);
 		}
 	}
 
 	/**
 	 * Cleans the ZoomRectangle of the AcetateOverlay
 	 */
 	public void cleanZoomRectangle() {
 		try {
 			acetate.cleanZoomRectangle();
 		} catch (Exception e) {
 			log.error("cleanZoomRectangle", e);
 		}
 	}
 
 	/**
 	 * Sets the Feature selected by the user when onLongPress and animates the
 	 * view to it
 	 * 
 	 * @param f
 	 *            The Feature selected
 	 */
 	public void setSelectedFeature(Feature f) {
 		try {
 			this.selectedFeature = f;
 			if (f != null) {
 				Point geom = (Point) f.getGeometry();
 				log.debug("selectedFeature: " + geom.toString());
 				this.animateTo(geom.getX(), geom.getY());
 			}
 		} catch (Exception e) {
 			log.error("setSelectedFeature", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @return True if PanMode is active, False if zoomRectangle is active
 	 */
 	public boolean isPanMode() {
 		return panMode;
 	}
 
 	class Scaler {
 
 		public static final int DURATION_SHORT = 500;
 		public static final int DURATION_LONG = 1000;
 
 		private float mStartScale;
 		private float mFinalScale;
 		private float mCurrScale;
 
 		private long mStartTime;
 		private int mDuration;
 		private float mDurationReciprocal;
 		private float mDeltaScale;
 		private boolean mFinished;
 		private Interpolator mInterpolator;
 
 		/**
 		 * Create a Scaler with the specified interpolator.
 		 */
 		public Scaler(Context context, Interpolator interpolator) {
 			mFinished = true;
 			mInterpolator = interpolator;
 		}
 
 		/**
 		 * 
 		 * Returns whether the scaler has finished scaling.
 		 * 
 		 * @return True if the scaler has finished scaling, false otherwise.
 		 */
 		public final boolean isFinished() {
 			return mFinished;
 		}
 
 		/**
 		 * Force the finished field to a particular value.
 		 * 
 		 * @param finished
 		 *            The new finished value.
 		 */
 		public final void forceFinished(boolean finished) {
 			mFinished = finished;
 		}
 
 		/**
 		 * Returns how long the scale event will take, in milliseconds.
 		 * 
 		 * @return The duration of the scale in milliseconds.
 		 */
 		public final int getDuration() {
 			return mDuration;
 		}
 
 		/**
 		 * Returns the current scale factor.
 		 * 
 		 * @return The new scale factor.
 		 */
 		public final float getCurrScale() {
 			return mCurrScale;
 		}
 
 		/**
 		 * Returns the start scale factor.
 		 * 
 		 * @return The start scale factor.
 		 */
 		public final float getStartScale() {
 			return mStartScale;
 		}
 
 		/**
 		 * Returns where the scale will end.
 		 * 
 		 * @return The final scale factor.
 		 */
 		public final float getFinalScale() {
 			return mFinalScale;
 		}
 
 		/**
 		 * Sets the final scale for this scaler.
 		 * 
 		 * @param newScale
 		 *            The new scale factor.
 		 */
 		public void setFinalScale(float newScale) {
 			mFinalScale = newScale;
 			mDeltaScale = mFinalScale - mStartScale;
 			mFinished = false;
 		}
 
 		/**
 		 * Call this when you want to know the new scale. If it returns true,
 		 * the animation is not yet finished.
 		 */
 		public boolean computeScale() {
 			if (mFinished) {
 				TileRaster.this.mPaint = new Paint();
 				mCurrScale = 1.0f;
 				return false;
 			}
 
 			int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
 
 			if (timePassed < mDuration) {
 				float x = (float) timePassed * mDurationReciprocal;
 
 				x = mInterpolator.getInterpolation(x);
 
 				mCurrScale = mStartScale + x * mDeltaScale;
 				if (mCurrScale == mFinalScale)
 					mFinished = true;
 
 			} else {
 				mCurrScale = mFinalScale;
 				mFinished = true;
 			}
 			return true;
 		}
 
 		/**
 		 * Start scaling by providing the starting scale and the final scale.
 		 * 
 		 * @param startX
 		 *            Starting horizontal scroll offset in pixels. Positive
 		 *            numbers will scroll the content to the left.
 		 * @param startY
 		 *            Starting vertical scroll offset in pixels. Positive
 		 *            numbers will scroll the content up.
 		 * @param dx
 		 *            Horizontal distance to travel. Positive numbers will
 		 *            scroll the content to the left.
 		 * @param dy
 		 *            Vertical distance to travel. Positive numbers will scroll
 		 *            the content up.
 		 * @param duration
 		 *            Duration of the scroll in milliseconds.
 		 */
 		public void startScale(float startScale, float finalScale, int duration) {
 			TileRaster.this.mPaint.setFlags(Paint.FILTER_BITMAP_FLAG);
 
 			mFinished = false;
 			mDuration = duration;
 			mStartTime = AnimationUtils.currentAnimationTimeMillis();
 			mStartScale = startScale;
 			mFinalScale = finalScale;
 			mDeltaScale = finalScale - startScale;
 			mDurationReciprocal = 1.0f / (float) mDuration;
 //			bufferCanvas.drawBitmap(frontBitmap, TileRaster.this.mTouchMapOffsetX, TileRaster.this.mTouchMapOffsetY, rotatePaint);
 		}
 
 		/**
 		 * Extend the scale animation. This allows a running animation to scale
 		 * further and longer, when used with {@link #setFinalScale(float)}.
 		 * 
 		 * @param extend
 		 *            Additional time to scale in milliseconds.
 		 * @see #setFinalScale(float)
 		 */
 		public void extendDuration(int extend) {
 			TileRaster.this.mPaint.setFlags(Paint.FILTER_BITMAP_FLAG);
 
 			int passed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
 			mDuration = passed + extend;
 			mDurationReciprocal = 1.0f / (float) mDuration;
 			mFinished = false;
 		}
 	}
 
 	/**
 	 * 
 	 * @return The TileProvider
 	 */
 	public TileProvider getMTileProvider() {
 		return mTileProvider;
 	}
 
 	/**
 	 * Clears the pending queues of the TileProvider
 	 */
 	public void clearCache() {
 		try {
 			log.debug("clearCache");
 			this.mTileProvider.clearPendingQueue();
 		} catch (Exception e) {
 			log.error("clearCache", e);
 		}
 	}
 
 	/**
 	 * Frees memory
 	 */
 	public void destroy() {
 		try {
 			log.debug("destroy tileraster");
 			acetate.destroy();
 			mTileProvider.destroy();
 
 			for (MapOverlay m : mOverlays) {
 				m.destroy();
 			}
 
 			geomDrawer = null;
 			// this.mCurrentAnimationRunner.interrupt();
 			// this.mCurrentAnimationRunner = null;
 		} catch (Exception e) {
 			log.error("destroy", e);
 		}
 	}
 
 	public void setAlpha(int aAlpha) {
 		alpha = aAlpha;
 	}
 }
