 package com.group2.finger_occ_demo;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.group2.finger_occ_demo.data.Movie;
 
 import android.graphics.Canvas;
import android.graphics.Color;
 import android.graphics.Paint;
import android.graphics.Rect;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.View;
 
 /**
  * Implements view for scatter plot.
  */
 public class ScatterPlotView {
 	public Points points;
 	private float sizeX;
 	private float sizeY;
 	private float marginX;
 	private float marginY;
 	
 	private int[] xRange = {0, 110};// currently year produced
 	private int[] yRange = {0, 10};// currently rating
 	
 	private int xOffset;
 	private int yOffset;
 	
 	final private int TICK_LINES_Y = 10;
 	final private int TICK_LINES_X = 11;
 	final private int TICK_SIZE = 15;//in px
 	
 	final private int POINT_OFFSET = 0;
 	
 	public ScatterPlotView(int screenWidth, int screenHeight){
 		//scatter plot is within 5% of each screen side
 		marginX = (float)(screenWidth * 0.1);
 		marginY = (float)(screenHeight * 0.1);
 		sizeX = (float) (screenWidth - (marginX * 2));
 		sizeY = (float) (screenHeight - ((marginY * 0.5) * 7));//*7 is to account for bottom bar
 		
 		xOffset = 0;
 		yOffset = 0;
 		
 		points = new Points((float)(marginX * 0.5), (float)(marginY * 0.5) - POINT_OFFSET, sizeX, sizeY, xRange, yRange);
 	}
 	
 	/**
 	 * Whenever the canvas is redrawn, these objects are drawn.
 	 */
 	public void onDraw(Canvas canvas) {
 		//shapes can obscure lines and graph background, so this is why graph is first.
 		this.drawGraph(canvas);
 		points.drawShapes(canvas);
 	}
 	
 	/**
 	 * Handles touching on the scatter plot. Make sure the event is sent in.
 	 * Returns any alert message text.
 	 */
 	public ArrayList<Movie> onTouch(MotionEvent event, View view) {
 		ArrayList<Movie> movies = null;
 		
 		// check if finger is in radius to resize any objects (want to make objects bigger on touch and drag)
 		points.checkRadius((int)event.getX(), (int)event.getY(), view);
 		    	
     	if(event.getAction() == MotionEvent.ACTION_UP ){
     		// See if finger is in any of the objects
     		movies = points.inShape((int)event.getX(), (int)event.getY());
     			
     		view.invalidate();
     	}
     	    	
     	return movies;
 	}
 	
 	/**
      * Detect drags on the scatterplot.
      */
 	public void onDrag(DragEvent event, View view) {
 		points.checkRadius((int)event.getX(), (int)event.getY(), view);
 	}
 	
 	/*
 	 * Graph translation and zooming
 	 */
 	
 	/**
 	 * Resets scatter plot. Currently only resets panning.
 	 */
 	public void resetGraph(){
 		this.xOffset = 0;
 		this.yOffset = 0;
 		
 		points.resetPosition();
 	}
 	
 	/**
 	 * Resets and restores all points in the scatterplot
 	 */
 	public void resetPoints(List<Movie> movies){
 		points.init_from_data(movies);
 	}
 	
 	/*
 	 * Non generic draw functions
 	 */
 	
 	/**
 	 * Draws graph lines (no ticks yet).
 	 */
 	private void drawGraph(Canvas canvas){
 		final int TEXT_OFFSET = -15;//tick label offset away from tick line, 0 means starts at tick line
 		final int FROM_YEAR = 1900;
 		Paint black = new Paint();
 		black.setStrokeWidth(3);
 		
 		//left line
 		canvas.drawLine((float)(marginX * 0.5) + xOffset, (float)(marginY * 0.5) + yOffset, (float)(marginX * 0.5) + xOffset, (float)(marginY * 0.5) + sizeY + yOffset, black);
 		
 		//draw left tick lines (with numbers)
 		float yPos;
 		float xPosStart;
 		float xPosEnd;
 		for (int i = 0;i <= TICK_LINES_Y; i += 1){
 			yPos = (float)( (marginY * 0.5) + yOffset + ((( ((marginY * 0.5) + sizeY) /(TICK_LINES_Y + 1)) * i)) );
 			xPosStart = (float)(marginX * 0.5) + xOffset - TICK_SIZE/2;
 			xPosEnd = (float)(marginX * 0.5) + xOffset + TICK_SIZE/2;
 			
 			if (i != TICK_LINES_Y)
 				canvas.drawLine(xPosStart, yPos, xPosEnd, yPos, black);
 			canvas.drawText((TICK_LINES_Y - i) + "", xPosStart - TICK_SIZE/2 - 5, yPos + 4, black);
 		}
 			
 		//bottom line
 		canvas.drawLine((float)(marginX * 0.5) + xOffset, (float)(marginY * 0.5) + sizeY + yOffset, (float) ((marginX * 0.5) + sizeX + xOffset), (float)(marginY * 0.5) +  sizeY + yOffset, black);
 		
 		//draw bottom tick lines
 		float xPos;
 		float yPosStart;
 		float yPosEnd;
 		for (int i = 0;i < TICK_LINES_X; i += 1){
 			xPos = (float) ((marginX * 0.5) + xOffset + ((sizeX/TICK_LINES_X) * (i + 1)));
 			yPosStart = (float) ((marginY * 0.5) + sizeY + yOffset - TICK_SIZE/2);
 			yPosEnd = (float) ((marginY * 0.5) + sizeY + yOffset + TICK_SIZE/2);
 			
 			canvas.drawLine(xPos, yPosStart, xPos, yPosEnd, black);
 			canvas.drawText((FROM_YEAR + (i*10)) + "", xPos - ((sizeX/TICK_LINES_X) * 1) + TEXT_OFFSET, yPosEnd + TICK_SIZE/2 + 5, black);
 			if (i == TICK_LINES_X - 1){
 				canvas.drawText((FROM_YEAR + ((i+1)*10)) + "", xPos + TEXT_OFFSET, yPosEnd + TICK_SIZE/2 + 5, black);
 			}
 		}
 	}
 }
