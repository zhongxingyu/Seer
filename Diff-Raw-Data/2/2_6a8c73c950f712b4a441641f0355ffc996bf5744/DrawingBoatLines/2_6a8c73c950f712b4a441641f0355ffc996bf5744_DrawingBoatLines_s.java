 package com.rullelinjeapp.customviews;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.rullelinjeapp.R;
 
 public class DrawingBoatLines extends View {
 	final static private String TAG = "##### DrawingBoatLines";
 
 	public void logm(String line) {
 		Log.i(TAG, line);
 	}
 
 	Paint green = new Paint();
 	Paint black = new Paint();
 	Paint lightred = new Paint();
 	Paint orange = new Paint();
 	Paint purple = new Paint();
 	Paint[] paints = { green, lightred, orange, purple };
 	int[] drawables = { R.drawable.inclineboat72p_green_0f8000,
 			R.drawable.inclineboat72p_lightred_f03932,
 			R.drawable.inclineboat72p_orange_fc9700,
 			R.drawable.inclineboat72p_purple_8e2abf };
 	int cellWidth;
 	int gridBottom;
 	int selectedAngleIndex;
 	public ArrayList<Double> angles = new ArrayList<Double>();
 
 	public DrawingBoatLines(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		pupulateVariables();
 	}
 
 	public int setAngle(double angle) {
 		angles.add(angle);
 		selectedAngleIndex = angles.size() - 1;
 		this.invalidate();
 		return selectedAngleIndex;
 	}
 
 	public void setSelectedIndex(int index) {
 		selectedAngleIndex = index;
 		this.invalidate();
 	}
 
 	public DrawingBoatLines(Context context) {
 		super(context);
 		pupulateVariables();
 	}
 
 	private void pupulateVariables() {
 		cellWidth = 30;
 		green.setARGB(255, 15, 128, 0);
 		black.setColor(Color.BLACK);
 		lightred.setARGB(255, 240, 57, 50);
 		orange.setARGB(255, 252, 151, 0);
 		purple.setARGB(255, 142, 42, 191);
 		selectedAngleIndex = 0;
 	}
 
 	private int getYAxes() {
 		int numberOfLines = getHeight() / cellWidth;
 		return (int) (numberOfLines * 2.0 / 3.0 * cellWidth);
 	}
 
 	private int getXAxes() {
 		int numberOfLines = getWidth() / cellWidth;
 		return (int) (numberOfLines / 2.0 * cellWidth);
 	}
 
 	private float[] getPointsFromAngle(double angle) {
 		float kat = (float) Math.tan(angle) * getWidth() / 2;
 		float[] points = { 0, Math.max(getYAxes() + kat, (float) getTop()),
 				getWidth(), Math.min(getYAxes() - kat, (float) gridBottom) };
 		return points;
 	}
 
 	public void drawR(Canvas canvas, int angleToDraw) {
 		gridBottom = getBottom() - getHeight() % cellWidth;
 
 		// draw grid
 		for (int i = 0; i <= getWidth(); i += cellWidth) {
 			canvas.drawLine(i, getTop(), i, gridBottom, black);
 		}
 		for (int i = getTop(); i <= getHeight(); i += cellWidth) {
 			canvas.drawLine(0, i, getWidth(), i, black);
 		}
 
 		// draw axes
 		float yAxes = getXAxes();
 		canvas.drawRect(yAxes - 2, getTop(), yAxes + 2, gridBottom, black);
 		float xAxes = getYAxes();
 		canvas.drawRect(0, xAxes - 2, getWidth(), xAxes + 2, black);
 
 		if (angles.size() < 1) {
 			return;
 		}
 		// draw angelLines
 		int lenPaints = paints.length;
 		if (angleToDraw == 999) {
 			paints[selectedAngleIndex % lenPaints].setStrokeWidth(5);
 			for (int i = 0; i < angles.size(); i++) {
 				canvas.drawLines(getPointsFromAngle(angles.get(i)), paints[i
 						% lenPaints]);
 			}
 			paints[selectedAngleIndex % lenPaints].setStrokeWidth(0);
 		} else {
 			paints[angleToDraw % lenPaints].setStrokeWidth(5);
 			canvas.drawLines(getPointsFromAngle(angles.get(angleToDraw)),
 					paints[angleToDraw % lenPaints]);
 			paints[angleToDraw % lenPaints].setStrokeWidth(0);
 		}
 		Bitmap boat = BitmapFactory.decodeResource(getResources(),
 				drawables[selectedAngleIndex]);
 		Matrix matrix = new Matrix();
 		double rad = -angles.get(selectedAngleIndex);
 		float sin = (float) Math.sin(rad);
 		float cos = (float) Math.cos(rad);
 		float[] points = {
 				cos,
 				-sin,
 				getXAxes() - ((boat.getWidth() / 2) * cos)
						- ((boat.getHeight() / 2) * sin),
 				sin,
 				cos,
 				getYAxes() - ((boat.getWidth() / 2) * sin)
 						- ((boat.getHeight() / 2) * cos)-3, 0F, 0F, 1F };
 
 		matrix.setValues(points);
 		// Toast.makeText(getContext(),
 		// matrix.toString(), Toast.LENGTH_LONG).show();
 		canvas.drawBitmap(boat, matrix, null);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		drawR(canvas, 999);
 
 		logm("Views height: " + getHeight() + " Views width: " + getWidth()
 				+ " viewTop: " + getTop() + " viewBottom: " + getBottom()
 				+ " myBottom: " + gridBottom);
 	}
 
 }
