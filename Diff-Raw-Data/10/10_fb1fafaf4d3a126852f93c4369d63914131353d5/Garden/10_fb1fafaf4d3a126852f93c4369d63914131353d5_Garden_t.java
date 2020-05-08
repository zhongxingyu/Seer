 package edu.berkeley.cs160.smartnature;
 
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.graphics.RectF;
 
 import java.util.ArrayList;
 
 public class Garden {
 	
 	private String name;
 	private int previewId;
 	private ArrayList<Plot> plots;
 	private Rect bounds;
 	private Rect padding = new Rect(30, 30, 30, 30);
 	private Rect paddingLand = new Rect(20, 30, 20, 10);
 	private Rect paddingPort = new Rect(30, 20, 10, 20);
 	
 	Garden(String gardenName) {
 		name = gardenName;
 		plots = new ArrayList<Plot>();
 		bounds = new Rect();
 	}
 	
 	Garden(int resId, String gardenName) {
 		this(gardenName);
 		previewId = resId;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getPreviewId() {
 		return previewId;
 	}
 
 	public void setPreviewId(int previewId) {
 		this.previewId = previewId;
 	}
 	
 	public ArrayList<Plot> getPlots() {
 		return plots;
 	}
 	
 	public Plot getPlot(int id) {
 		return plots.get(id);
 	}
 	
 	public int getPlotId(Plot plot) {
 		return plots.indexOf(plot);
 	}
 	
 	public void setPlot(int id, Plot plot) {
 		plots.add(id, plot);
 	}
 	
 	public void addPlot(Plot plot) {
 		Rect pBounds = plot.getShape().getBounds();
 		if (plots.isEmpty()) {
 			bounds = new Rect(pBounds);
 		}
 		else {
 			bounds.left = Math.min(bounds.left, pBounds.left);
 			bounds.top = Math.min(bounds.top, pBounds.top);
 			bounds.right = Math.max(bounds.right, pBounds.right);
 			bounds.bottom = Math.max(bounds.bottom, pBounds.bottom);
 		}
 		
 		plot.setID(plots.size());
 		plots.add(plot);
 	}
 	
 	/** add a polygonal plot */
 	public void addPlot(String plotName, Rect plotBounds, float rotation, float[] points) {
 		addPlot(new Plot(plotName, plotBounds, rotation, points)); 
 	}
 	
 	/** add a rectangular or elliptical plot */
 	public void addPlot(String plotName, Rect plotBounds, float rotation, int shapeType) {
 		addPlot(new Plot(plotName, plotBounds, rotation, shapeType)); 
 	}
 	
 	public Rect getRawBounds() {
 		return bounds;
 	}
 	
 	/** Used for full screen mode */
 	public RectF getBounds() {
 		RectF padded = new RectF(bounds);
 		padded.left -= padding.left;
 		padded.top -= padding.top;
 		padded.right += padding.right;
 		padded.bottom += padding.bottom;
 		return padded;
 	}
 	
 	/** Used for portrait/landscape mode */
 	public RectF getBounds(boolean portraitMode) {
 		Rect offset = portraitMode ? paddingPort : paddingLand;
 		RectF padded = new RectF(bounds);
 		padded.left -= offset.left;
 		padded.top -= offset.top;
 		padded.right += offset.right;
 		padded.bottom += offset.bottom;
 		return padded;
 	}
 	
 	public void refreshBounds() {
 		if (plots.isEmpty())
 			bounds = new Rect();
 		else {
 			bounds = plots.get(0).getShape().copyBounds();
 			for (Plot p : plots) {
 				Rect pBounds = p.getShape().getBounds();
 				bounds.left = Math.min(bounds.left, pBounds.left);
 				bounds.top = Math.min(bounds.top, pBounds.top);
 				bounds.right = Math.max(bounds.right, pBounds.right);
 				bounds.bottom = Math.max(bounds.bottom, pBounds.bottom);
 			}
		}
 	}
 	
 	
 	/** finds a plot which contains (x, y) after being transformed by the matrix */ 
 	public Plot plotAt(float x, float y, Matrix matrix) {
 		Matrix inverse = new Matrix();
 		matrix.invert(inverse);
 		for (Plot p : plots) {
			float[] point = { x, y };
			Matrix tmp = new Matrix(inverse);
			tmp.postRotate(-p.getAngle(), p.getShape().getBounds().centerX(), p.getShape().getBounds().centerY());
			tmp.mapPoints(point);
 			if (p.contains(point[0], point[1]))
 				return p;
 		}
 		return null;
 	}
 }
