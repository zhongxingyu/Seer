 /**
  *    Copyright 2012-2013 Trento RISE
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package eu.trentorise.smartcampus.jp.custom;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 /**
  * @author raman
  *
  */
 public abstract class CustomGridView<T> extends View {
 
 	private int numRows;
 	private int numCols;
 	private int rowHeight;
 	private int colWidth;
 	
 	private List<T> data;
 
 	/**
 	 * @param context
 	 */
 	public CustomGridView(Context context) {
 		super(context);
 		init();
 	}
 
 	/**
 	 * @param context
 	 * @param attrs
 	 * @param defStyle
 	 */
 	public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	/**
 	 * @param context
 	 * @param attrs
 	 */
 	public CustomGridView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 //		super.onDraw(canvas);	
 		if (getHLinePaint() != null) {
 			canvas.drawLine(0, 0, getNumCols()*getColWidth()-1, 0, getHLinePaint());
 			for (int i = 0; i < getNumRows(); i++) {
 				int y = (getRowHeight())*(i)-1;
 				canvas.drawLine(0, y, getNumCols()*getColWidth()-1, y, getHLinePaint());
 			}
 		}
 		if (getVLinePaint() != null) {
 			canvas.drawLine(0, 0, 0, getNumRows() * getRowHeight(), getVLinePaint());
 			for (int i = 0; i < getNumCols(); i++) {
 				int x = (getColWidth()) * (i)-1;
 				canvas.drawLine(x, 0, x, getNumRows() * getRowHeight(), getVLinePaint());
 			}
 		}
 		for (int i = 0; i < getNumRows(); i++) {
 			for (int j = 0; j < getNumCols(); j++) {
 				drawCell(canvas, data.get(i*getNumCols()+j), i, j, (getColWidth())*j, getRowHeight()*i);
 			}
 		}
 	}
 
 	/**
 	 * @param canvas
 	 * @param object
 	 * @param i
 	 * @param j
 	 * @param k
 	 * @param l
 	 */
 	protected abstract void drawCell(Canvas canvas, T item, int row, int col, int x, int y);
 
 
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			return true;
 		}
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			float x = event.getX();
 			float y = event.getY();
			int i = (int)(x / getColWidth());
			int j = (int)(y / getRowHeight());
			handleClick(data.get(j*getNumCols()+i));
 		} 
 		return super.onTouchEvent(event);
 	}
 	
 	/**
 	 * @param object
 	 */
 	protected void handleClick(T object) {
 	}
 
 	/**
 	 * @return
 	 */
 	protected abstract Paint getVLinePaint();
 
 	/**
 	 * @return
 	 */
 	protected abstract Paint getHLinePaint();
 	protected void init() {
 		setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 			}
 		});
 	}
 
 	/**
 	 * @return the numRows
 	 */
 	public int getNumRows() {
 		return numRows;
 	}
 
 	/**
 	 * @param numRows the numRows to set
 	 */
 	public void setNumRows(int numRows) {
 		this.numRows = numRows;
 //		getLayoutParams().height = getRowHeight()*numRows;
 		setMinimumHeight(getRowHeight()*numRows);
 	}
 
 	/**
 	 * @return the numCols
 	 */
 	public int getNumCols() {
 		return numCols;
 	}
 
 	/**
 	 * @param numCols the numCols to set
 	 */
 	public void setNumCols(int numCols) {
 		this.numCols = numCols;
 		setMinimumWidth(numCols*getColWidth());
 //		getLayoutParams().width = numCols*getColWidth();
 	}
 
 	/**
 	 * @return the rowHeight
 	 */
 	public int getRowHeight() {
 		return rowHeight;
 	}
 
 	/**
 	 * @param rowHeight the rowHeight to set
 	 */
 	public void setRowHeight(int rowHeight) {
 		this.rowHeight = rowHeight;
 	}
 
 	/**
 	 * @return the colWidth
 	 */
 	public int getColWidth() {
 		return colWidth;
 	}
 
 	/**
 	 * @param colWidth the colWidth to set
 	 */
 	public void setColWidth(int colWidth) {
 		this.colWidth = colWidth;
 	}
 
 	/**
 	 * @return the data
 	 */
 	public List<T> getData() {
 		return data;
 	}
 
 	/**
 	 * @param data the data to set
 	 */
 	public void setData(List<T> data) {
 		this.data = data;
 	}
 }
