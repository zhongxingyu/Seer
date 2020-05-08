 /*
  * Copyright (C) 2012 Luca Santarelli
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
 
 package it.sineo.android.tileMapEditor;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URL;
 import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
 
 import org.metalev.multitouch.controller.MultiTouchController;
 import org.metalev.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
 import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
 import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.widget.Toast;
 
 public class TiledMapView extends View implements MultiTouchObjectCanvas<TileMap>, OnSharedPreferenceChangeListener {
 
 	private final static String TAG = TiledMapView.class.getSimpleName();
 
 	private final static float MIN_DISTANCE = 8f;
 	/**
 	 * Used during panning to determine if we need to repaint the map. A literal 0
 	 * will cause lots of extra redraws due to "trembling fingers" and sensor
 	 * precision.
 	 */
 	private final static float MIN_DISTANCE_TO_INVALIDATE = 1.2f;
 
 	private Context context;
 
 	private int tileSize;
 
 	private TileMap tileMap;
 	private PointInfo currentTouchPoint = null;
 	private long fingerDownTime = 0;
 	private PointInfo fingerDownPoint = null;
 
 	/*
 	 * Listeners ========================================
 	 */
 	private OnShortPressListener shortPressListener = null;
 	private OnLongPressListener longPressListener = null;
 
 	/*
 	 * Canvas stuff ========================================
 	 */
 	private Paint gridPaint;
 	private Paint debugPaint;
 	private Paint emptyTilePaint;
 	private RectF viewRect;
 	private RectF mapRect;
 
 	private boolean mustDrawGrid = C.DEFAULT_MAP_SHOW_GRID;
 	private boolean mustExportGrid = C.DEFAULT_EXPORT_SHOW_GRID;
 	private int emptyTileColor = C.DEFAULT_MAP_COLOR_EMPTY_TILE;
 
 	public void init(Context context) {
 		tileSize = context.getResources().getDimensionPixelSize(R.dimen.tiledMap_tile);
 		gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		gridPaint.setColor(Color.WHITE);
 		gridPaint.setStrokeWidth(1.5f);
 		gridPaint.setAlpha(128);
 
 		debugPaint = new Paint(gridPaint);
 		debugPaint.setColor(Color.CYAN);
 		debugPaint.setAlpha(255);
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
 		mustDrawGrid = prefs.getBoolean(C.PREFS_MAP_SHOW_GRID, C.DEFAULT_MAP_SHOW_GRID);
 		mustExportGrid = prefs.getBoolean(C.PREFS_EXPORT_SHOW_GRID, C.DEFAULT_EXPORT_SHOW_GRID);
 		emptyTileColor = prefs.getInt(C.PREFS_MAP_COLOR_EMPTY_TILE, C.DEFAULT_MAP_COLOR_EMPTY_TILE);
 		prefs.registerOnSharedPreferenceChangeListener(this);
 
 		emptyTilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		emptyTilePaint.setColor(emptyTileColor);
 		emptyTilePaint.setStyle(Paint.Style.FILL_AND_STROKE);
 
 		mapRect = new RectF(0, 0, 0, 0);
 
 		currentTouchPoint = new PointInfo();
 		fingerDownPoint = new PointInfo();
 	}
 
 	public void initMap(int rows, int columns) {
 		tileMap = new TileMap(rows, columns);
 	}
 
 	public void renameMap(String name) {
 		tileMap.name = name;
 	}
 
 	public String getMapName() {
 		return tileMap.name;
 	}
 
 	public Bundle toBundle() {
 		Bundle b = tileMap.toBundle();
 		Log.d(TAG, "built bundle: " + b.toString());
 		return b;
 	}
 
 	public String toJSON() {
 		String json = tileMap.toJSON();
 		Log.d(TAG, "built json: " + json);
 		return json;
 	}
 
 	public void restoreFromBundle(Bundle b) {
 		if (b != null) {
 			Log.d(TAG, "received bundle: " + b.toString());
 			tileMap = new TileMap(b);
 
 			for (int idxRow = 0; idxRow < tileMap.rows; idxRow++) {
 				for (int idxCol = 0; idxCol < tileMap.columns; idxCol++) {
 					if (tileMap.tilePaths[idxRow][idxCol] != null) {
 						setTile(idxRow, idxCol, tileMap.tilePaths[idxRow][idxCol], tileMap.tileAngles[idxRow][idxCol]);
 					}
 				}
 			}
 		} else {
 			Log.d(TAG, "received null bundle");
 		}
 	}
 
 	public void restoreFromJSON(String s) {
 		if (s != null) {
 			Log.d(TAG, "received JSON: " + s);
 			tileMap = new TileMap(s);
 
 			for (int idxRow = 0; idxRow < tileMap.rows; idxRow++) {
 				for (int idxCol = 0; idxCol < tileMap.columns; idxCol++) {
 					if (tileMap.tilePaths[idxRow][idxCol] != null) {
 						setTile(idxRow, idxCol, tileMap.tilePaths[idxRow][idxCol], tileMap.tileAngles[idxRow][idxCol]);
 					}
 				}
 			}
 		} else {
 			Log.d(TAG, "received null json");
 		}
 	}
 
 	public byte[] export(Bitmap.CompressFormat format, int quality) {
 		Bitmap fullMap = Bitmap.createBitmap(tileSize * tileMap.columns, tileSize * tileMap.rows, Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(fullMap);
 		canvas.drawPaint(emptyTilePaint);
 		long t0 = System.currentTimeMillis();
 		for (int rowIdx = 0; rowIdx < tileMap.rows; rowIdx++) {
 			for (int colIdx = 0; colIdx < tileMap.columns; colIdx++) {
 				if (tileMap.tilePaths[rowIdx][colIdx] != null) {
 					float left = tileSize * colIdx;
 					float top = tileSize * rowIdx;
 					Matrix m = tileMap.tileMatrices[rowIdx][colIdx];
 					Log.d(TAG, "angle: " + tileMap.tileAngles[rowIdx][colIdx] + " => "
 							+ (90 * tileMap.tileAngles[rowIdx][colIdx]));
 					/*
 					 * The following order and call to scale+rotate is the only one which
 					 * works...
 					 */
 					m.reset();
 					m.postRotate(90 * tileMap.tileAngles[rowIdx][colIdx]);
 					Bitmap scaled = Bitmap.createBitmap(tileMap.tileBitmaps[rowIdx][colIdx], 0, 0, tileSize, tileSize, m, true);
 					canvas.drawBitmap(scaled, left, top, debugPaint);
 					scaled.recycle();
 				}
 			}
 		}
 		long t1 = System.currentTimeMillis();
 		Log.d(TAG, "time to draw tiles: " + (t1 - t0) + " ms");
 		if (mustExportGrid) {
 			t0 = System.currentTimeMillis();
 			/* Grid */
 			for (int rowIdx = 0; rowIdx <= tileMap.rows; rowIdx++) {
 				float x0 = 0;
 				float y0 = (rowIdx * tileSize);
 				float x1 = ((tileMap.columns * tileSize) - 1);
 				float y1 = (rowIdx * tileSize);
 				canvas.drawLine(x0, y0, x1, y1, gridPaint);
 			}
 			for (int colIdx = 0; colIdx <= tileMap.columns; colIdx++) {
 				float x0 = colIdx * tileSize;
 				float y0 = 0;
 				float x1 = colIdx * tileSize;
 				float y1 = ((tileMap.rows * tileSize) - 1);
 				canvas.drawLine(x0, y0, x1, y1, gridPaint);
 			}
 			t1 = System.currentTimeMillis();
 			Log.d(TAG, "time to draw grid: " + (t1 - t0) + " ms");
 		} else {
 			Log.d(TAG, "not drawing grid as per settings");
 		}
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		t0 = System.currentTimeMillis();
 		fullMap.compress(format, quality, baos);
 		t1 = System.currentTimeMillis();
 		Log.d(TAG, "time to convert format: " + (t1 - t0) + " ms");
 		return baos.toByteArray();
 	}
 
 	public byte[] exportThumb(Bitmap.CompressFormat format, int quality) {
 		Bitmap fullMap = Bitmap.createBitmap(tileSize * tileMap.columns, tileSize * tileMap.rows, Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(fullMap);
 		canvas.drawPaint(emptyTilePaint);
 		long t0 = System.currentTimeMillis();
 		for (int rowIdx = 0; rowIdx < tileMap.rows; rowIdx++) {
 			for (int colIdx = 0; colIdx < tileMap.columns; colIdx++) {
 				if (tileMap.tilePaths[rowIdx][colIdx] != null) {
 					float left = tileSize * colIdx;
 					float top = tileSize * rowIdx;
 					Matrix m = tileMap.tileMatrices[rowIdx][colIdx];
 					Log.d(TAG, "angle: " + tileMap.tileAngles[rowIdx][colIdx] + " => "
 							+ (90 * tileMap.tileAngles[rowIdx][colIdx]));
 					/*
 					 * The following order and call to scale+rotate is the only one which
 					 * works...
 					 */
 					m.reset();
 					m.postRotate(90 * tileMap.tileAngles[rowIdx][colIdx]);
 					Bitmap scaled = Bitmap.createBitmap(tileMap.tileBitmaps[rowIdx][colIdx], 0, 0, tileSize, tileSize, m, true);
 					canvas.drawBitmap(scaled, left, top, debugPaint);
 					scaled.recycle();
 				}
 			}
 		}
 		long t1 = System.currentTimeMillis();
 		Log.d(TAG, "time to draw tiles: " + (t1 - t0) + " ms");
 		int width, height;
 		if (tileMap.rows > tileMap.columns) {
 			width = (int) ((tileSize * tileMap.columns) / tileMap.rows);
 			height = tileSize;
 		} else if (tileMap.rows < tileMap.columns) {
 			width = tileSize;
 			height = (int) ((tileSize * tileMap.rows) / tileMap.columns);
 		} else {
 			width = height = tileSize;
 		}
 		Log.d(TAG, "rows=" + tileMap.rows + ", columns=" + tileMap.columns + ", width=" + width + ", height=" + height);
 		fullMap = Bitmap.createScaledBitmap(fullMap, width, height, true);
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		t0 = System.currentTimeMillis();
 		fullMap.compress(format, quality, baos);
 		t1 = System.currentTimeMillis();
 		Log.d(TAG, "time to convert format: " + (t1 - t0) + " ms");
 		return baos.toByteArray();
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		viewRect = new RectF(0, 0, w, h);
 	}
 
 	/*
 	 * onDraw ========================================
 	 */
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		canvas.save();
 
 		long t0 = System.currentTimeMillis();
 
 		mapRect.left = tileMap.xOff;
 		mapRect.top = tileMap.yOff;
 		mapRect.right = tileMap.xOff + tileSize * tileMap.columns * tileMap.scale;
 		mapRect.bottom = tileMap.yOff + tileSize * tileMap.rows * tileMap.scale;
 		if (mapRect.intersect(viewRect)) {
 			canvas.drawRect(mapRect, emptyTilePaint);
 		}
 		long t1 = System.currentTimeMillis();
 		Log.d(TAG, "time to fill map background: " + (t1 - t0) + " ms.");
 		t0 = System.currentTimeMillis();
 		for (int rowIdx = 0; rowIdx < tileMap.rows; rowIdx++) {
 			for (int colIdx = 0; colIdx < tileMap.columns; colIdx++) {
 				if (tileMap.tilePaths[rowIdx][colIdx] != null) {
 					float left = tileMap.xOff + tileSize * colIdx * tileMap.scale;
 					float top = tileMap.yOff + tileSize * rowIdx * tileMap.scale;
 					float width = tileSize * tileMap.scale;
 					float height = width;
 					if (viewRect.intersects(left, top, left + width, top + height)) {
 						Matrix m = tileMap.tileMatrices[rowIdx][colIdx];
 						Log.d(TAG, "angle: " + tileMap.tileAngles[rowIdx][colIdx] + " => "
 								+ (90 * tileMap.tileAngles[rowIdx][colIdx]));
 						/*
 						 * The following order and call to scale+rotate is the only one
 						 * which works...
 						 */
 						m.reset();
 						m.postRotate(90 * tileMap.tileAngles[rowIdx][colIdx]);
 						m.preScale(tileMap.scale, tileMap.scale);
 						Bitmap scaled = Bitmap.createBitmap(tileMap.tileBitmaps[rowIdx][colIdx], 0, 0, tileSize, tileSize, m, true);
 						canvas.drawBitmap(scaled, left, top, debugPaint);
						/*
						 * Commenting out recycle() as some devices find it too
						 * "aggressive". I'll need to learn how to properly display bitmaps.
						 * :(
						 */
						// scaled.recycle();
 					} // end-if: tile does not intersect view area
 				}
 			}
 		}
 		t1 = System.currentTimeMillis();
 		Log.d(TAG, "time to draw tiles: " + (t1 - t0) + " ms");
 		if (mustDrawGrid) {
 			t0 = System.currentTimeMillis();
 			/* Grid */
 			for (int rowIdx = 0; rowIdx <= tileMap.rows; rowIdx++) {
 				float x0 = tileMap.xOff;
 				float y0 = tileMap.yOff + (rowIdx * tileSize * tileMap.scale);
 				float x1 = tileMap.xOff + ((tileMap.columns * tileSize) - 1) * tileMap.scale;
 				float y1 = tileMap.yOff + (rowIdx * tileSize) * tileMap.scale;
 				canvas.drawLine(x0, y0, x1, y1, gridPaint);
 			}
 			for (int colIdx = 0; colIdx <= tileMap.columns; colIdx++) {
 				float x0 = tileMap.xOff + colIdx * tileSize * tileMap.scale;
 				float y0 = tileMap.yOff;
 				float x1 = tileMap.xOff + colIdx * tileSize * tileMap.scale;
 				float y1 = tileMap.yOff + ((tileMap.rows * tileSize) - 1) * tileMap.scale;
 				canvas.drawLine(x0, y0, x1, y1, gridPaint);
 			}
 			t1 = System.currentTimeMillis();
 			Log.d(TAG, "time to draw grid: " + (t1 - t0) + " ms");
 		} else {
 			Log.d(TAG, "not drawing grid as per settings");
 		}
 		canvas.restore();
 	}
 
 	public void setTile(int row, int column, String path) {
 		setTile(row, column, path, (byte) 0);
 	}
 
 	public void setTile(int row, int column, String path, byte angle) {
 		if (path != null) {
 			try {
 				InputStream is = null;
 				if (path.startsWith("assets:")) {
 					// 7 = "assets:".length();
 					is = context.getAssets().open(path.substring(7));
 				} else if (path.startsWith("content:")) {
 					Uri uri = Uri.parse(path);
 					is = context.getContentResolver().openInputStream(uri);
 				}
 				Bitmap source = BitmapFactory.decodeStream(is);
 				Bitmap scaled = Bitmap.createScaledBitmap(source, tileSize, tileSize, true);
 				tileMap.tileBitmaps[row][column] = scaled;
 				tileMap.tilePaths[row][column] = path;
 				tileMap.tileAngles[row][column] = angle;
 				tileMap.tileMatrices[row][column] = new Matrix();
 				source.recycle();
 			} catch (IOException ioex) {
 				Toast.makeText(context, context.getResources().getString(R.string.tiledMap_error_tilenotfound, path),
 						Toast.LENGTH_LONG).show();
 				/* And reset the tile as if it was empty. */
 				tileMap.tileBitmaps[row][column] = null;
 				tileMap.tilePaths[row][column] = null;
 				tileMap.tileAngles[row][column] = 0;
 				tileMap.tileMatrices[row][column] = null;
 			}
 		} else {
 			/* This is an "alias" to remove the tile. */
 			if (tileMap.tileBitmaps[row][column] != null) {
 				tileMap.tileBitmaps[row][column].recycle();
 			}
 			tileMap.tileBitmaps[row][column] = null;
 			tileMap.tilePaths[row][column] = null;
 			tileMap.tileAngles[row][column] = 0;
 			tileMap.tileMatrices[row][column] = null;
 		} // end-if: path is null
 		invalidate();
 	}
 
 	public void removeTile(int row, int column) {
 		setTile(row, column, null);
 	}
 
 	public void rotateTile(int row, int column) {
 		tileMap.tileAngles[row][column] = (byte) ((tileMap.tileAngles[row][column] + 1) % 4);
 		invalidate();
 	}
 
 	/*
 	 * Multi-touch interface ========================================
 	 */
 	private MultiTouchController<TileMap> multiTouchController = new MultiTouchController<TileMap>(this);
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return multiTouchController.onTouchEvent(event);
 	}
 
 	@Override
 	public TileMap getDraggableObjectAtPoint(PointInfo touchPoint) {
 		/*
 		 * There is always a single object: the map.
 		 */
 		return tileMap;
 	}
 
 	@Override
 	public void selectObject(TileMap obj, PointInfo touchPoint) {
 		currentTouchPoint.set(touchPoint);
 		if (obj != null && touchPoint.getNumTouchPoints() == 1) {
 			// First finger pressed
 			fingerDownTime = touchPoint.getEventTime();
 			fingerDownPoint.set(touchPoint);
 			Log.d(TAG, "first finger pressed, fingerDownTime: " + fingerDownTime);
 		} else if (touchPoint.getNumTouchPoints() == 1) {
 			Log.d(TAG, MessageFormat.format(
 					"last finger removed, fingerDownTime: {0}, eventTime: {1}, delta: {2} longPress: {3}", fingerDownTime,
 					touchPoint.getEventTime(), (touchPoint.getEventTime() - fingerDownTime),
 					ViewConfiguration.getLongPressTimeout()));
 			// Last finger removed from screen
 			float dX = (touchPoint.getX() - fingerDownPoint.getX());
 			float dY = (touchPoint.getY() - fingerDownPoint.getY());
 			float distance = FloatMath.sqrt(dX * dX + dY * dY);
 			if (distance < MIN_DISTANCE) {
 				/*
 				 * Calculate row+column. touchPoint(x,y) are relative to the screen.
 				 */
 				int column = (int) FloatMath.floor(((touchPoint.getX() - tileMap.xOff) / tileMap.scale) / tileSize);
 				int row = (int) FloatMath.floor(((touchPoint.getY() - tileMap.yOff) / tileMap.scale) / tileSize);
 				Log.d(TAG, "p=(" + touchPoint.getX() + "," + touchPoint.getY() + ") row=" + row + ", col=" + column);
 				Log.d(TAG,
 						MessageFormat.format("p=({0},{1}) row={2}, col={3}", touchPoint.getX(), touchPoint.getY(), row, column));
 				if (row >= 0 && row < tileMap.rows && column >= 0 && column < tileMap.columns) {
 					boolean acted = false;
 					if (touchPoint.getEventTime() - fingerDownTime > ViewConfiguration.getLongPressTimeout()) {
 						Log.d(TAG, "long press detected");
 						if (longPressListener != null) {
 							acted = longPressListener.onLongPress(row, column, tileMap.tilePaths[row][column] == null);
 						}
 						if (tileMap.tilePaths[row][column] != null) {
 							/* Only if the tile is not empty */
 							super.performLongClick();
 						}
 					} else {
 						Log.d(TAG, "short press detected");
 						if (shortPressListener != null) {
 							acted = shortPressListener.onShortPress(row, column, tileMap.tilePaths[row][column] == null);
 						}
 					}
 					if (acted) {
 						invalidate();
 					}
 				} else {
 					/* Touched outside region of map */
 					Log.d(TAG, "touch input outside of region");
 				}
 			} else {
 				Log.d(TAG, "pointer moved more than min_distance, assuming no-tap event");
 			}
 		}
 	}
 
 	@Override
 	public void getPositionAndScale(TileMap obj, PositionAndScale objPosAndScaleOut) {
 		if (obj != null) {
 			/*
 			 * No rotation, no x/y scaling (we scale both axis the same way).
 			 */
 			objPosAndScaleOut.set(obj.xOff, obj.yOff, true, obj.scale, false, 0, 0, false, 0);
 		}
 	}
 
 	@Override
 	public boolean setPositionAndScale(TileMap obj, PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
 		currentTouchPoint.set(touchPoint);
 		if (obj != null) {
 			boolean invalidated = false;
 			if (Math.abs(obj.scale - newObjPosAndScale.getScale()) > 0) {
 				/*
 				 * First limit scaling:
 				 */
 				float tileScaleFactor = 1.5f;
 				float newTentativeMapWidth = obj.columns * tileSize * newObjPosAndScale.getScale();
 				float newTentativeMapHeight = obj.rows * tileSize * newObjPosAndScale.getScale();
 				if (obj.scale > newObjPosAndScale.getScale() && newTentativeMapHeight < getHeight()
 						&& newTentativeMapWidth < getWidth()) {
 					// Limit scaling down to minimum breakdown scale
 					obj.scale = Math.min(getWidth() / (float) (obj.columns * tileSize), getHeight()
 							/ (float) (obj.rows * tileSize));
 				} else if (obj.scale < newObjPosAndScale.getScale()
 						&& tileScaleFactor * tileSize * newObjPosAndScale.getScale() > getWidth()
 						&& tileScaleFactor * tileSize * newObjPosAndScale.getScale() > getHeight()) {
 					// Limit scaling up to a single tile.
 					obj.scale = Math.max(getWidth() / (float) (tileScaleFactor * tileSize), getHeight()
 							/ (float) (tileScaleFactor * tileSize));
 				} else {
 					obj.scale = newObjPosAndScale.getScale();
 				}
 				invalidated = true;
 			} // end-if: scale changed
 
 			/* Drag/Pan with limit: */
 			float dx = newObjPosAndScale.getXOff() - obj.xOff;
 			float dy = newObjPosAndScale.getYOff() - obj.yOff;
 			if (Math.abs(dx) > 0 || Math.abs(dy) > 0) {
 				RectF vr = new RectF(0, 0, getWidth(), getHeight());
 				RectF dr = new RectF(newObjPosAndScale.getXOff(), newObjPosAndScale.getYOff(), newObjPosAndScale.getXOff()
 						+ obj.columns * tileSize * obj.scale, newObjPosAndScale.getYOff() + obj.rows * tileSize * obj.scale);
 				float diffUp = Math.min(vr.bottom - dr.bottom, 0 - dr.top);
 				float diffDown = Math.max(vr.bottom - dr.bottom, 0 - dr.top);
 				float diffLeft = Math.min(0 - dr.left, vr.right - dr.right);
 				float diffRight = Math.max(0 - dr.left, vr.right - dr.right);
 				if (diffUp > 0) {
 					dy += diffUp;
 				}
 				if (diffDown < 0) {
 					dy += diffDown;
 				}
 				if (diffLeft > 0) {
 					dx += diffLeft;
 				}
 				if (diffRight < 0) {
 					dx += diffRight;
 				}
 				/*
 				 * Does the map need repainting?
 				 */
 				if (FloatMath.sqrt(dx * dx + dy * dy) > MIN_DISTANCE_TO_INVALIDATE) {
 					obj.xOff += dx;
 					obj.yOff += dy;
 					invalidated = true;
 				}
 			} // end-if: x,y offset > 0
 			if (invalidated) {
 				invalidate();
 			}
 		} // end-if: obj !=null
 		return true;
 	}
 
 	/*
 	 * Class constructors ========================================
 	 */
 	public TiledMapView(Context context) {
 		this(context, null);
 	}
 
 	public TiledMapView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	public TiledMapView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context);
 		this.context = context;
 	}
 
 	/*
 	 * SharedPreference listener ========================================
 	 */
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 		/*
 		 * Act on preferences which change drawing behavior:
 		 */
 		if (C.PREFS_MAP_SHOW_GRID.compareTo(key) == 0) {
 			mustDrawGrid = prefs.getBoolean(key, C.DEFAULT_MAP_SHOW_GRID);
 			invalidate();
 		} else if (C.PREFS_EXPORT_SHOW_GRID.compareTo(key) == 0) {
 			mustExportGrid = prefs.getBoolean(key, C.DEFAULT_EXPORT_SHOW_GRID);
 			invalidate();
 		} else if (C.PREFS_MAP_COLOR_EMPTY_TILE.compareTo(key) == 0) {
 			emptyTileColor = prefs.getInt(key, C.DEFAULT_MAP_COLOR_EMPTY_TILE);
 			emptyTilePaint.setColor(emptyTileColor);
 			invalidate();
 		}
 	}
 
 	/*
 	 * Our listeners ========================================
 	 */
 	public void setOnShortPressListener(OnShortPressListener listener) {
 		this.shortPressListener = listener;
 	}
 
 	public void setOnLongPressListener(OnLongPressListener listener) {
 		this.longPressListener = listener;
 	}
 
 	public static interface OnShortPressListener {
 		/**
 		 * 
 		 * @param row
 		 * @param column
 		 * @param isEmpty
 		 * @return true if the event was processed, false otherwise
 		 */
 		public boolean onShortPress(int row, int column, boolean isEmpty);
 	}
 
 	public static interface OnLongPressListener {
 		/**
 		 * 
 		 * @param row
 		 * @param column
 		 * @param isEmpty
 		 * @return true if the event was processed, false otherwise
 		 */
 		public boolean onLongPress(int row, int column, boolean isEmpty);
 	}
 
 }
