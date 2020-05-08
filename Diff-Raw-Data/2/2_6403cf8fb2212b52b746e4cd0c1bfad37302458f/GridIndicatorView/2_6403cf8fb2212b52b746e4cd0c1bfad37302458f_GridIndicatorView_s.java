 package com.mntnorv.wrdl_holo.views;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.FloatMath;
 import android.view.View;
 
 public class GridIndicatorView extends View {
 	/* FIELDS */
 	private Paint indicatorPaint;
 	private ArrayList<RectF> indicatorRectList;
 	private ArrayList<Float> indicatorRotationList;
 	
 	private float tileWidth;
 	private float tileHeight;
 	private int rows;
 	private int columns;
 	
 	private float indicatorHeight;
 	
 	private float[] rotMatrix;
 	
 	/* CONSTRUCOTRS */
 	public GridIndicatorView(Context context) {
 		super(context);
 
 		// Constructor only for edit mode
 		if (!this.isInEditMode()) {
 			throw (new UnsupportedOperationException("GridIndicatorView(Context) is only supported in edit mode."));
 		}
 	}
 	
 	public GridIndicatorView(Context context, float tileWidth, float tileHeight, int columns, int rows) {
 		super(context);
 		this.tileWidth = tileWidth;
 		this.tileHeight = tileHeight;
 		this.rows = rows;
 		this.columns = columns;
 		
 		initGridIndicatorView();
 	}
 
 	public GridIndicatorView(Context context, float tileWidth, float tileHeight, int columns, int rows,
 							 float indicatorHeight, int indicatorColor) {
 		this(context, tileWidth, tileHeight, rows, columns);
 		
 		this.indicatorHeight = indicatorHeight;
 		this.indicatorPaint.setColor(indicatorColor);
 	}
 
 	/* INIT */
 	private void initGridIndicatorView() {
 		indicatorRectList = new ArrayList<RectF>();
 		indicatorRotationList = new ArrayList<Float>();
 		indicatorPaint = new Paint();
 		indicatorPaint.setColor(0xBB63BAF9);
 		indicatorHeight = 16 * getResources().getDisplayMetrics().density;
 		
 		float rotValue = (float)Math.toDegrees(Math.atan(tileWidth/tileHeight));
 		
 		rotMatrix = new float[9];
 		rotMatrix[4] = 0; // center
 		
 		rotMatrix[1] =  90; // top
 		rotMatrix[7] = -90; // bottom
 		rotMatrix[3] =   0; // left
 		rotMatrix[5] = 180; // right
 		
 		rotMatrix[0] =  rotValue;       // top left
 		rotMatrix[2] = -rotValue + 180; // top right
 		rotMatrix[6] = -rotValue;       // bottom left
 		rotMatrix[8] =  rotValue - 180; // bottom right
 	}
 	
 	/* METHODS */
 	/**
 	 * Adds an indicator to the View. 
 	 * Both {@code toCol} and {@code toRow} can't be equal to {@code fromCol} and
 	 * {@code fromRow}.
 	 * @param fromCol - indicator start tile column: {@code 0} to {@code (columns - 1)}
 	 * @param fromRow - indicator start tile row: {@code 0} to {@code (rows - 1)}
 	 * @param toCol - indicator end tile column: {@code fromCol +/- 1} or equal
 	 * @param toRow - indicator end tile row: {@code fromRow +/- 1} or equal
 	 */
 	public void addIndicator (int fromCol, int fromRow, int toCol, int toRow) {
 		int dX = fromCol - toCol;
 		int dY = fromRow - toRow;
 		
 		float x, y, w, h;
 		h = indicatorHeight;
 		
 		if (dX != 0 && dY != 0) {
 			w = FloatMath.sqrt(tileWidth*tileWidth + tileHeight*tileHeight);
 		} else {
 			w = tileWidth;
 		}
 		
 		x = fromCol * tileWidth + tileWidth/2;
 		y = fromRow * tileHeight + tileHeight/2 - h/2;
 		
 		indicatorRectList.add(0, new RectF(x, y, x+w, y+h));
 		indicatorRotationList.add(0, rotMatrix[(dY+1)*3 + dX + 1]);
 		
 		invalidate();
 	}
 	
 	public void removeLastIndicator() {
		if (indicatorRectList.size() > 1) {
 			indicatorRectList.remove(0);
 			indicatorRotationList.remove(0);
 			invalidate();
 		}
 	}
 	
 	public void clearIndicators() {
 		indicatorRectList.clear();
 		indicatorRotationList.clear();
 		invalidate();
 	}
 	
 	/* SETTERS */
 	/**
 	 * Set the height of an indicator. NOTE: all indicators must be
 	 * cleared with {@link #clearIndicators()} and readded with
 	 * {@link #addIndicator(int, int, int, int)} to change the height.
 	 */
 	public void setIndicatorHeight(float indicatorHeight) {
 		this.indicatorHeight = indicatorHeight;
 		invalidate();
 	}
 	
 	public void setIndicatorColor(int color) {
 		indicatorPaint.setColor(color);
 		invalidate();
 	}
 	
 	/* MEASURE */
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
 	}
 
 	private int measureWidth(int measureSpec) {
 		int result = 0;
         int specMode = MeasureSpec.getMode(measureSpec);
         int specSize = MeasureSpec.getSize(measureSpec);
 
         if (specMode == MeasureSpec.EXACTLY) {
             result = specSize;
         } else {
             // Measure the text
             result = (int)tileWidth*columns + getPaddingLeft() + getPaddingRight();
             if (specMode == MeasureSpec.AT_MOST) {
                 result = Math.min(result, specSize);
             }
         }
 
         return result;
 	}
 	
 	private int measureHeight(int measureSpec) {
 		int result = 0;
         int specMode = MeasureSpec.getMode(measureSpec);
         int specSize = MeasureSpec.getSize(measureSpec);
 
         if (specMode == MeasureSpec.EXACTLY) {
             result = specSize;
         } else {
             // Measure the text
             result = (int)tileHeight*rows + getPaddingTop() + getPaddingBottom();
             if (specMode == MeasureSpec.AT_MOST) {
                 result = Math.min(result, specSize);
             }
         }
 
         return result;
 	}
 
 	/* DRAW */
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		for (int i = 0; i < indicatorRectList.size(); i++) {
 			RectF indicator = indicatorRectList.get(i);
 			canvas.save();
 			canvas.rotate(indicatorRotationList.get(i), indicator.left, indicator.top + indicator.height()/2);
 			canvas.drawRect(indicator, indicatorPaint);
 			canvas.restore();
 		}
 	}
 }
