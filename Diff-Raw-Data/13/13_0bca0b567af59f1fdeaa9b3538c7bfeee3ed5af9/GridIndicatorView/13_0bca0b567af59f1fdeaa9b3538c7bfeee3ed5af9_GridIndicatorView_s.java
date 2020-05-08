 package com.mntnorv.wrdl_holo;
 
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
 	
 	private float[] rotMatrix;
 	
 	/* CONSTRUCOTRS */
 	public GridIndicatorView(Context context, float tileWidth, float tileHeight, int rows, int columns) {
 		super(context);
 		this.tileWidth = tileWidth;
 		this.tileHeight = tileHeight;
 		this.rows = rows;
 		this.columns = columns;
 		
 		initGridIndicatorView();
 	}
 
 	/* INIT */
 	private void initGridIndicatorView() {
 		indicatorRectList = new ArrayList<RectF>();
 		indicatorRotationList = new ArrayList<Float>();
 		indicatorPaint = new Paint();
 		indicatorPaint.setColor(0xFF63BAF9);
 		
 		float rotValue = (float)Math.toDegrees(Math.atan(tileWidth/tileHeight));
 		
 		rotMatrix = new float[9];
 		rotMatrix[4] = 0; // center
 		
 		rotMatrix[1] =  90; // top
 		rotMatrix[7] = -90; // bottom
		rotMatrix[3] = 180; // left
		rotMatrix[5] =   0; // right
 		
		rotMatrix[0] =  rotValue - 180; // top left
		rotMatrix[2] = -rotValue;       // top right
		rotMatrix[6] = -rotValue + 180; // bottom left
		rotMatrix[8] =  rotValue;       // botton right
 	}
 	
 	/* METHODS */
 	public void addIndicator (int fromCol, int fromRow, int toCol, int toRow) {
 		int dX = fromCol - toCol;
 		int dY = fromRow - toRow;
 		
 		float x, y, w, h;
 		h = 16;
 		
 		if (dX != 0 && dY != 0) {
 			w = FloatMath.sqrt(tileWidth*tileWidth + tileHeight*tileHeight);
 		} else {
 			w = tileWidth;
 		}
 		
 		x = fromCol * tileWidth + tileWidth/2;
 		y = fromRow * tileHeight + tileHeight/2 - h/2;
 		
 		indicatorRectList.add(new RectF(x, y, x+w, y+h));
 		indicatorRotationList.add(rotMatrix[(dY+1)*3 + dX + 1]);
 	}
 	
 	public void removeLastIndicator() {
 		if (indicatorRectList.size() > 1) {
 			indicatorRectList.remove(0);
 			indicatorRotationList.remove(0);
 		}
 	}
 	
 	public void clearIndicators() {
 		indicatorRectList.clear();
 		indicatorRotationList.clear();
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
