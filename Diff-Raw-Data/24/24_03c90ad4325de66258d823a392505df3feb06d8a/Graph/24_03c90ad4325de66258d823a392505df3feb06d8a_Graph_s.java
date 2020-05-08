 package com.finalhack.totalelevation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 
 public class Graph extends View {
 	
 	private static final String TAG_GRAPH = "graph";
 
 	// Style information
 	private int outlineColor = 0xffdddddd;
 	private int lineColor = 0xff0000ff;
 	private int height = 0;
 	private int textSize = 0;
 	private int lastY = Integer.MIN_VALUE;
 	private int minElevation = Integer.MAX_VALUE;
 	private int maxElevation = Integer.MIN_VALUE;
 	private float lineWidth = 2;
 	private int borderSize = 2;
 	public List<Integer> elevations = new ArrayList<Integer>();
 	public List<SatSpecificData> satSpecificData = new ArrayList<SatSpecificData>();
 	private Bitmap radarBitmap = null;
 	private int fadeCounter = 0;
	private int graphicalSignalStrengthMultiplier = 3;
 	private int maxSignalStrength = 40;
 
 	private static final int MAX_POSSIBLE_VISIBLE_SATS = 16;
 	
 	private String[] satColors = {
 			"7D8A2E",
 			"C9D787",
 			"7E8AA2",
 			"D8CAA8",
 			"284907",
 			"382513",
 			"468966",
 			"5C832F",
 			"FFB03B",
 			"363942",
 			"B64926",
 			"8E2800",
 			"263248",
 			"FF9800",
 			"FFC0A9"
 		};
 	
 	// Some stats
 	private int screenWidth;
 
 	// Paint for onDraw
 	private Paint outlinePaint = new Paint();
 	private Paint linePaint = new Paint();
 	private Paint textPaint = new Paint();
 	private Paint satPaint = new Paint();
 	private Paint whitePaint = new Paint();
 	private Rect rect = new Rect(0, 0, 0, 0);
 	private Rect satRect = new Rect(0, 0, 0, 0);
 	private int satTextHeight = 0;
 	private int formPadding = 0;
 
 	// Standard constructor
 	@SuppressWarnings("deprecation")
 	public Graph(Context context, AttributeSet attrSet) {
 		super(context, attrSet);
 
 		height = (int)getContext().getResources().getDimension(R.dimen.graph_height);
 		textSize = (int)getContext().getResources().getDimension(R.dimen.graph_sat_text_size);
 		satTextHeight = (int)getContext().getResources().getDimension(R.dimen.graph_sat_text_y);
 		formPadding = (int)getContext().getResources().getDimension(R.dimen.form_padding);
 		
 		radarBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.radar);
 		
 		// Added for preview view in IDE
 		if (isInEditMode()) return;
 
 		// Save off some screen data
 		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
 		screenWidth = (int) display.getWidth();
 
 		// Setup all the paint colors
 		outlinePaint.setColor(outlineColor);
 		linePaint.setColor(lineColor);
 		linePaint.setAntiAlias(true);
 		linePaint.setStrokeWidth(lineWidth);
 		whitePaint.setColor(Color.WHITE);
 		textPaint.setColor(Color.WHITE);
 		textPaint.setTextSize(textSize);
 
 		// Setup the view's background
 		rect = new Rect(0, 0, screenWidth, height);
 	}
 
 	// Allow elevations to be added from the external environment
 	public void updateElevation(int elevation) {
 		elevations.add(elevation);
 		if (elevation < minElevation) minElevation = elevation;
 		if (elevation > maxElevation) maxElevation = elevation;
 		if (BuildConfig.DEBUG) Log.d("", "added: " + elevation);
 		this.invalidate();
 	}
 
 	public void setSatSpecificData(List<SatSpecificData> satSpecificData) {
 		this.satSpecificData = satSpecificData;
 		this.invalidate();
 	}
 
 	// The real drawing happens here
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		// Draw the background
 		canvas.drawRect(rect, outlinePaint);
 
 		// Draw each satellite box
 		int left = formPadding;
 		int satWidth = screenWidth / MAX_POSSIBLE_VISIBLE_SATS;
 		for (int i=0;i<satSpecificData.size();i++) {
 			String fadedColorAlpha = "" + fadeCounter;
 			if (fadedColorAlpha.length()<2) fadedColorAlpha = "0" + fadedColorAlpha;
 			String fadedColor = "#" + fadedColorAlpha + satColors[i];
 			satPaint.setColor(Color.parseColor(fadedColor));
 			// First draw the box background & border
 			// Its height (top) is lowered/raised proportionately to signal strength
 			int startHeight = maxSignalStrength - Integer.parseInt(satSpecificData.get(i).signalStrength);
 			startHeight *= graphicalSignalStrengthMultiplier;
 			satRect.set(left, startHeight, left + satWidth, height);
 			canvas.drawRect(satRect, outlinePaint);
 			// Then draw the box (background shrunken by 1)
 			satRect.set(left+borderSize, borderSize+startHeight, left + satWidth - borderSize, height - borderSize);
 			canvas.drawRect(satRect, satPaint);
 			// Now, label the box
 			canvas.drawText("#" + satSpecificData.get(i).prnNumber, (float)left + formPadding, satTextHeight * 2, textPaint);
 			canvas.drawBitmap(radarBitmap, left, satTextHeight * 2, whitePaint);
 			left += satWidth;
 		}
 		
 		fadeCounter +=5;
 		if (fadeCounter >= 100) {
 			fadeCounter = 0;
 		} else {
 			invalidate();
 		}
 
 		if (BuildConfig.DEBUG) Log.d("", elevations.toString());
 
 		int lineLength = 0;
 
 		// Start plotting on the left
 		int startX = 0;
 
 		// As long as we have some elevations to plot...
 		if (elevations.size() > 0) {
 			// Give each elevation equal space on the screen
 			lineLength = screenWidth / elevations.size();
 
 			// Plot each elevation line
 			for (int elevation : elevations) {
 				// Normalize all elevations to be zero-based
 				int y = elevation - minElevation;
 				// Make the total height range proportional to the height of the view
 				int addOneToAvoidDivByZero = 1;
 				y = y * (height - borderSize) / (maxElevation - minElevation + addOneToAvoidDivByZero);
 				// Turn it upside down becuase 0,0 is at the top instead of at the bottom
 				y = height - y;
 
 				// If this is the first line, just make it horizontal
 				if (lastY == Integer.MIN_VALUE) lastY = y;
 
 				// Draw the elevation line
 				canvas.drawLine(startX, lastY, startX + lineLength, y, linePaint);
 				if (BuildConfig.DEBUG) Log.d("", "" + startX + ", " + lastY + ", " + (startX + lineLength) + ", " + y);
 
 				// Update the next line's starting position
 				lastY = y;
 				startX += lineLength;
 			}
 
 			// Reset the initial y coordinate for the first elevation plot
 			// This will make it ready to plot when this method is called again
 			lastY = Integer.MIN_VALUE;
 
 			if (BuildConfig.DEBUG) Log.d(TAG_GRAPH, "Min: " + minElevation + ", Max: " + maxElevation);
 		}
 	}
 
 	// Standard view override for requesting the needed space on a screen
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		setMeasuredDimension(screenWidth, height + borderSize);
 	}
 	
 }
