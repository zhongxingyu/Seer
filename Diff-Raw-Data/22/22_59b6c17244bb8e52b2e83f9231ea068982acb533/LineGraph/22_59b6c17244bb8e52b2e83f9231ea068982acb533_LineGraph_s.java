 package com.echo.holographlibrary;
 
 import java.util.ArrayList;
 
 import com.echo.holographlibrary.BarGraph.OnBarClickedListener;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.AvoidXfermode;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.Path.Direction;
 import android.graphics.PixelFormat;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Region;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class LineGraph extends SurfaceView implements SurfaceHolder.Callback {
 	
 	private ArrayList<Line> lines = new ArrayList<Line>();
 	private float minY = 0, minX = 0;
 	private float maxY = 0, maxX = 0;
 	private boolean isMaxYUserSet = false;
 	private int lineToFill = -1;
 	private int indexSelected = -1;
 	private OnPointClickedListener listener;
 	private Bitmap fullImage;
 	private boolean shouldUpdate = false;
 	
 	public LineGraph(Context context){
 		super(context);
 		this.setZOrderOnTop(true); //necessary                
 	    getHolder().setFormat(PixelFormat.TRANSPARENT); 
 	    getHolder().addCallback(this); 
 	}
 	
 	public LineGraph(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.setZOrderOnTop(true); //necessary                
 	    getHolder().setFormat(PixelFormat.TRANSPARENT); 
 	    getHolder().addCallback(this); 
 	}
 	public void setMinY(float minY){
 		
 	}
 	public void addLine(Line line) {
 		lines.add(line);
 	}
 	public ArrayList<Line> getLines() {
 		return lines;
 	}
 	public void setLineToFill(int indexOfLine) {
 		this.lineToFill = indexOfLine;
 	}
 	public int getLineToFill(){
 		return lineToFill;
 	}
 	public void setLines(ArrayList<Line> lines) {
 		this.lines = lines;
 	}
 	public Line getLine(int index) {
 		return lines.get(index);
 	}
 	public int getSize(){
 		return lines.size();
 	}
 	
 	public void setRangeY(float min, float max) {
 		minY = min;
 		maxY = max;
 		isMaxYUserSet = true;
 	}
 	public float getMaxY(){
 		if (isMaxYUserSet){
 			return maxY;
 		} else {
 			maxY = lines.get(0).getPoint(0).getY();
 			for (Line line : lines){
 				for (LinePoint point : line.getPoints()){
 					if (point.getY() > maxY){
 						maxY = point.getY();
 					}
 				}
 			}
 			return maxY;
 		}
 		
 	}
 	public float getMinY(){
 		if (isMaxYUserSet){
 			return minY;
 		} else {
 			float min = lines.get(0).getPoint(0).getY();
 			for (Line line : lines){
 				for (LinePoint point : line.getPoints()){
 					if (point.getY() < min) min = point.getY();
 				}
 			}
 			minY = min;
 			return minY;
 		}
 	}
 	public float getMaxX(){
 		float max = lines.get(0).getPoint(0).getX();
 		for (Line line : lines){
 			for (LinePoint point : line.getPoints()){
 				if (point.getX() > max) max = point.getX();
 			}
 		}
 		maxX = max;
 		return maxX;
 		
 	}
 	public float getMinX(){
 		float max = lines.get(0).getPoint(0).getX();
 		for (Line line : lines){
 			for (LinePoint point : line.getPoints()){
 				if (point.getX() < max) max = point.getX();
 			}
 		}
 		maxX = max;
 		return maxX;
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		setWillNotDraw(false); 					//Allows us to use invalidate() to call onDraw()
 		postInvalidate();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {}
 	
 	public void onDraw(Canvas ca) {
 		if (fullImage == null || shouldUpdate) {
 			fullImage = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
 			Canvas canvas = new Canvas(fullImage);
 			Paint paint = new Paint();
 			Path path = new Path();
 			float bottomPadding = 10, topPadding = 10;
 			float sidePadding = 10;
 			float usableHeight = getHeight() - bottomPadding - topPadding;
 			float usableWidth = getWidth() - 2*sidePadding;
 			
 			int lineCount = 0;
 			for (Line line : lines){
 				int count = 0;
 				float firstXPixels = 0, lastXPixels = 0, newYPixels = 0;
 				float lastYPixels = 0, newXPixels = 0;
 				float maxY = getMaxY();
 				float minY = getMinY();
 				float maxX = getMaxX();
 				float minX = getMinX();
 				
 				if (lineCount == lineToFill){
 					paint.setColor(Color.BLACK);
 					paint.setAlpha(30);
 					paint.setStrokeWidth(2);
 					for (int i = 10; i-getWidth() < getHeight(); i = i+20){
 						canvas.drawLine(i, getHeight()-bottomPadding, 0, getHeight()-bottomPadding-i, paint);
 					}
 					
 					paint = new Paint();
 					paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
 					for (LinePoint p : line.getPoints()){
 						float yPercent = (p.getY()-minY)/(maxY - minY);
 						float xPercent = (p.getX()-minX)/(maxX - minX);
 						if (count == 0){
 							lastXPixels = sidePadding + (xPercent*usableWidth);
 							lastYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
 							firstXPixels = lastXPixels;
 							path.moveTo(lastXPixels, lastYPixels);
 						} else {
 							newXPixels = sidePadding + (xPercent*usableWidth);
 							newYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
 							path.lineTo(newXPixels, newYPixels);
 							Path pa = new Path();
 							pa.moveTo(lastXPixels, lastYPixels);
 							pa.lineTo(newXPixels, newYPixels);
 							pa.lineTo(newXPixels, 0);
 							pa.lineTo(lastXPixels, 0);
 							pa.close();
 							canvas.drawPath(pa, paint);
 							lastXPixels = newXPixels;
 							lastYPixels = newYPixels;
 						}
 						count++;
 					}
 					
 					Path pa = new Path();
 					pa.moveTo(0, getHeight()-bottomPadding);
 					pa.lineTo(sidePadding, getHeight()-bottomPadding);
 					pa.lineTo(sidePadding, 0);
 					pa.lineTo(0, 0);
 					pa.close();
 					canvas.drawPath(pa, paint);
 					
 					pa = new Path();
 					pa.moveTo(getWidth(), getHeight()-bottomPadding);
 					pa.lineTo(getWidth()-sidePadding, getHeight()-bottomPadding);
 					pa.lineTo(getWidth()-sidePadding, 0);
 					pa.lineTo(getWidth(), 0);
 					pa.close();
 					canvas.drawPath(pa, paint);
 					
 				}
 				
 				lineCount++;
 			}
 			
 			paint = new Paint();
 			
 			paint.setColor(Color.BLACK);
 			paint.setAlpha(50);
 			paint.setAntiAlias(true);
 			canvas.drawLine(sidePadding, getHeight() - bottomPadding, getWidth()-sidePadding, getHeight()-bottomPadding, paint);
 			paint.setAlpha(255);
 			
 			
 			for (Line line : lines){
 				int count = 0;
 				float lastXPixels = 0, newYPixels = 0;
 				float lastYPixels = 0, newXPixels = 0;
 				float maxY = getMaxY();
 				float minY = getMinY();
 				float maxX = getMaxX();
 				float minX = getMinX();
 				
 				paint.setColor(line.getColor());
 				paint.setStrokeWidth(6);
 				
 				for (LinePoint p : line.getPoints()){
 					float yPercent = (p.getY()-minY)/(maxY - minY);
 					float xPercent = (p.getX()-minX)/(maxX - minX);
 					if (count == 0){
 						lastXPixels = sidePadding + (xPercent*usableWidth);
 						lastYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
 					} else {
 						newXPixels = sidePadding + (xPercent*usableWidth);
 						newYPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
 						canvas.drawLine(lastXPixels, lastYPixels, newXPixels, newYPixels, paint);
 						lastXPixels = newXPixels;
 						lastYPixels = newYPixels;
 					}
 					count++;
 				}
 			}
 			
 			
 			int pointCount = 0;
 			
 			for (Line line : lines){
 				float maxY = getMaxY();
 				float minY = getMinY();
 				float maxX = getMaxX();
 				float minX = getMinX();
 				
 				paint.setColor(line.getColor());
 				paint.setStrokeWidth(6);
 				paint.setStrokeCap(Paint.Cap.ROUND);
 				
 				if (line.isShowingPoints()){
 					for (LinePoint p : line.getPoints()){
 						float yPercent = (p.getY()-minY)/(maxY - minY);
 						float xPercent = (p.getX()-minX)/(maxX - minX);
 						float xPixels = sidePadding + (xPercent*usableWidth);
 						float yPixels = getHeight() - bottomPadding - (usableHeight*yPercent);
 						
 						paint.setColor(Color.GRAY);
 						canvas.drawCircle(xPixels, yPixels, 10, paint);
 						paint.setColor(Color.WHITE);
 						canvas.drawCircle(xPixels, yPixels, 5, paint);
 						
 						Path path2 = new Path();
 						path2.addCircle(xPixels, yPixels, 30, Direction.CW);
 						p.setPath(path2);
 						p.setRegion(new Region((int)(xPixels-30), (int)(yPixels-30), (int)(xPixels+30), (int)(yPixels+30)));
 						
 						if (indexSelected == pointCount && listener != null){
 							paint.setColor(Color.parseColor("#33B5E5"));
 							paint.setAlpha(100);
 							canvas.drawPath(p.getPath(), paint);
 							paint.setAlpha(255);
 						}
 						
 						pointCount++;
 					}
 				}
 			}
 			
 			shouldUpdate = false;
 		}
 		
 		ca.drawBitmap(fullImage, 0, 0, null);
 		
 		
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 	    Point point = new Point();
 	    point.x = (int) event.getX();
 	    point.y = (int) event.getY();
 	    
 	    int count = 0;
 	    int lineCount = 0;
 	    int pointCount = 0;
 	    
 	    Region r = new Region();
 	    for (Line line : lines){
 	    	pointCount = 0;
 	    	for (LinePoint p : line.getPoints()){
		    	r.setPath(p.getPath(), p.getRegion());
		    	if (r.contains((int)point.x,(int) point.y) && event.getAction() == MotionEvent.ACTION_DOWN){
		    		indexSelected = count;
		    	} else if (event.getAction() == MotionEvent.ACTION_UP){
		    		if (r.contains((int)point.x,(int) point.y) && listener != null){
		    			listener.onClick(lineCount, pointCount);
		    		}
		    		indexSelected = -1;
		    	}
 		    	pointCount++;
 			    count++;
 	    	}
 	    	lineCount++;
 	    	
 	    }
 	    
 	    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP){
 	    	shouldUpdate = true;
 	    	postInvalidate();
 	    }
 	    
 	    
 
 	    return true;
 	}
 	
 	public void setOnPointClickedListener(OnPointClickedListener listener) {
 		this.listener = listener;
 	}
 	
 	public abstract class OnPointClickedListener {
 		abstract void onClick(int lineIndex, int pointIndex);
 	}
 }
