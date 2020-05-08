 package org.kvj.quebec4.service;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.view.View;
 
 public class DrawingController {
 
 	public enum PageDirection {
 		PageUp, PageDown, PageLeft, PageRight
 	};
 
 	private static final String TAG = "DrawingController";
 	int width = 0;
 	int height = 0;
 	List<List<Bitmap>> rows = new ArrayList<List<Bitmap>>();
 	int columns = 0;
 	int selectedColumn = -1;
 	int selectedRow = -1;
 
 	public DrawingController(Context context) {
 		width = context.getResources().getDisplayMetrics().widthPixels;
 		height = context.getResources().getDisplayMetrics().heightPixels;
 		if (isLandscape(context)) {
 			int t = width;
 			width = height;
 			height = t;
 		}
 		// Log.i(TAG, "New drawing!: " + width + ", " + height);
 		columns = 1;
 		rows.add(createRow());
 		selectedColumn = 0;
 		selectedRow = 0;
 	}
 
 	private List<Bitmap> createRow() {
 		List<Bitmap> result = new ArrayList<Bitmap>();
 		for (int i = 0; i < columns; i++) {
 			result.add(createPage());
 		}
 		return result;
 	}
 
 	public Bitmap getPage() {
 		return rows.get(selectedRow).get(selectedColumn);
 	}
 
 	private Bitmap createPage() {
 		return Bitmap.createBitmap(width, height, Config.ARGB_4444);
 	}
 
 	private boolean isLandscape(Context context) {
 		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
 	}
 
 	public Matrix getMatrix(View view) {
 		Matrix matrix = new Matrix();
 		if (isLandscape(view.getContext())) {
 			matrix.setRotate(-90);
 			matrix.postTranslate(0, view.getHeight());
 		}
 		return matrix;
 	}
 
 	public int getRowCount(Context context) {
 		return isLandscape(context) ? columns : rows.size();
 	}
 
 	public int getColCount(Context context) {
 		return isLandscape(context) ? rows.size() : columns;
 	}
 
 	public int getRow(Context context) {
 		return isLandscape(context) ? selectedColumn : selectedRow;
 	}
 
 	public int getCol(Context context) {
 		return isLandscape(context) ? selectedRow : selectedColumn;
 	}
 
 	private PageDirection translateDirection(Context context,
 			PageDirection direction) {
 		if (isLandscape(context)) {
 			switch (direction) {
 			case PageUp:
 				return PageDirection.PageRight;
 			case PageRight:
 				return PageDirection.PageDown;
 			case PageDown:
 				return PageDirection.PageLeft;
 			case PageLeft:
 				return PageDirection.PageUp;
 			}
 		}
 		return direction;
 	}
 
 	public void addPage(Context context, PageDirection direction) {
 		PageDirection dir = translateDirection(context, direction);
 		switch (dir) {
 		case PageUp:
 			rows.add(0, createRow());
 			selectedRow = 0;
 			break;
 		case PageDown:
 			rows.add(createRow());
 			selectedRow = rows.size() - 1;
 			break;
 		case PageLeft:
 			createCol(false);
 			selectedColumn = 0;
 			break;
 		case PageRight:
 			createCol(true);
 			selectedColumn = columns - 1;
 			break;
 		}
 	}
 
 	private void removeCol() {
 		if (columns > 1) {
 			removeCol(selectedColumn);
 			selectedColumn--;
 			if (selectedColumn < 0) {
 				selectedColumn = 0;
 			}
 		}
 	}
 
 	private void removeRow() {
 		if (rows.size() > 1) {
 			rows.remove(selectedRow);
 			selectedRow--;
 			if (selectedRow < 0) {
 				selectedRow = 0;
 			}
 		}
 	}
 
 	public void removeColumn(Context context) {
 		if (isLandscape(context)) {
 			removeRow();
 		} else {
 			removeCol();
 		}
 	}
 
 	public void removeRow(Context context) {
 		if (isLandscape(context)) {
 			removeCol();
 		} else {
 			removeRow();
 		}
 	}
 
 	private void createCol(boolean last) {
 		for (List<Bitmap> page : rows) {
 			if (last) {
 				page.add(createPage());
 			} else {
 				page.add(0, createPage());
 			}
 		}
 		columns++;
 	}
 
 	private void removeCol(int pos) {
 		for (List<Bitmap> page : rows) {
 			page.remove(pos);
 		}
 		columns--;
 	}
 
 	public void selectRow(Context context, int pos) {
 		if (isLandscape(context)) {
 			selectedColumn = pos;
 		} else {
 			selectedRow = pos;
 		}
 	}
 
 	public void selectColumn(Context context, int pos) {
 		if (isLandscape(context)) {
 			selectedRow = pos;
 		} else {
 			selectedColumn = pos;
 		}
 	}
 
 	public static float dp2px(Context context, float dp) {
 		return dp * context.getResources().getDisplayMetrics().density;
 	}
 
 	public void clearPage() {
 		rows.get(selectedRow).set(selectedColumn, createPage());
 	}
 
 	public boolean saveToFile(Context context, File file) {
 		try {
 			int bwidth = width * columns;
 			int bheight = height * rows.size();
 			Bitmap bitmap = null;
 			if (isLandscape(context)) {
 				bitmap = Bitmap.createBitmap(bheight, bwidth, Config.ARGB_4444);
 			} else {
 				bitmap = Bitmap.createBitmap(bwidth, bheight, Config.ARGB_4444);
 			}
 			Canvas canvas = new Canvas(bitmap);
 			// Paint bgPaint = new Paint();
 			// bgPaint.setStyle(Style.FILL);
 			// bgPaint.setARGB(0xff, 0, 0, 0);
 			// canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(),
 			// bgPaint);
 			Paint paint = new Paint();
 			for (int i = 0; i < rows.size(); i++) {
 				List<Bitmap> row = rows.get(i);
 				for (int j = 0; j < row.size(); j++) {
 					Matrix matrix = new Matrix();
 					matrix.setTranslate(j * width, i * height);
 					if (isLandscape(context)) {
 						matrix.postRotate(-90);
 						matrix.postTranslate(0, bwidth);
 					}
 					canvas.drawBitmap(row.get(j), matrix, paint);
 				}
 			}
 			FileOutputStream stream = new FileOutputStream(file, false);
 			bitmap.compress(CompressFormat.PNG, 100, stream);
 			stream.close();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
