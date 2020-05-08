 /*
  * Copyright (C) 2010 Alex Cojocaru
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.alexalecu.imageUtil;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 
 import javax.imageio.ImageIO;
 import javax.swing.SwingWorker;
 
 import com.alexalecu.imageCrop.exception.InvalidOperationException;
 
 public class AutoSelectTask extends SwingWorker<Object[], AutoSelectStatus> {
 	public final static int MIN_ADJACENT_PIXELS_FOR_SELECT = 5;
 	
 	// disable the disk-based cache to speed up the image processing
 	static {
 		ImageIO.setUseCache(false);
 	}
 	
 	private AutoSelectStatus autoSelectStatus; // the current task status
 	private Object[] result; // the task execution result
 	
 	private BufferedImage image;
 	private Rectangle selectionRect;
 	private Color bgColor;
 	private int bgTolerance;
 	private ImageSelectMethod selectMethod;
 
 
 	/**
 	 * @return the current task status
 	 */
 	public AutoSelectStatus getAutoSelectStatus()
 	{
 		return autoSelectStatus;
 	}
 
 	/**
 	 * set the current task status and trigger a property change event
 	 * @param autoSelectStatus
 	 */
 	private void setAutoSelectStatus(AutoSelectStatus autoSelectStatus)
 	{
 		AutoSelectStatus old = this.autoSelectStatus;
 		this.autoSelectStatus = autoSelectStatus;
 		getPropertyChangeSupport().firePropertyChange("autoSelectStatus", old, autoSelectStatus);
 	}
 
 	/**
 	 * @return the task execution result
 	 */
 	public Object[] getResult()
 	{
 		return result;
 	}
 
 	/**
 	 * set the task execution result and trigger a property change event
 	 * @param result
 	 */
 	private void setResult(Object[] result)
 	{
 		Object[] oldResult = this.result;
 		this.result = result;
 		getPropertyChangeSupport().firePropertyChange("result", oldResult, result);
 	}
 
 	/**
 	 * set the BufferedImage to work on
 	 * @param image
 	 */
 	public void setImage(BufferedImage image) throws InvalidOperationException {
 		assertStateForChangingProperties();
 		this.image = image;
 	}
 
 	/**
 	 * set the selection rectangle to start from
 	 * @param selectionRect
 	 */
 	public void setSelectionRect(Rectangle selectionRect) throws InvalidOperationException {
 		assertStateForChangingProperties();
 		this.selectionRect = selectionRect;
 	}
 
 	/**
 	 * set the background color to look for
 	 * @param bgColor
 	 */
 	public void setBgColor(Color bgColor) throws InvalidOperationException {
 		assertStateForChangingProperties();
 		this.bgColor = bgColor;
 	}
 
 	/**
 	 * set the background tolerance to take into account when matching the background color
 	 * @param bgTolerance
 	 */
 	public void setBgTolerance(int bgTolerance) throws InvalidOperationException {
 		assertStateForChangingProperties();
 		this.bgTolerance = (int)(255 * bgTolerance / 100);
 	}
 
 	/**
 	 * set the select method to use, minimum or maximum
 	 * @param selectMethod
 	 */
 	public void setSelectMethod(ImageSelectMethod selectMethod) throws InvalidOperationException {
 		assertStateForChangingProperties();
 		this.selectMethod = selectMethod;
 	}
 	
 	/**
 	 * Assert that the current task state allows the instance fields to be modified
 	 * @throws InvalidOperationException
 	 */
 	public void assertStateForChangingProperties() throws InvalidOperationException
 	{
 		if (getState() == StateValue.PENDING)
 			return;
 		if (getState() == StateValue.DONE)
 			return;
 		
 		throw new InvalidOperationException("Cannot change instance fields" +
 				" when the AutoSelectTask is in " + getState() + "state");
 	}
 
 	/**
 	 * compute the rectangle which is the optimized solution for cropping the source BufferedImage;
 	 * make sure you set the execution parameters before executing the task
 	 * @return an array containing two Objects; the first one is the resulting Rectangle,
 	 * while the 2nd object is an ArrayList containing the polygon edges
 	 */
 	@Override
 	protected Object[] doInBackground() {
 		// check the input parameters
 		if (image == null || selectionRect == null || bgColor == null || selectMethod == null) {
 			return new Object[] {null, null};
 		}
 
 		// compute the coordinates of the minimum rectangle which encloses the whole image
 		publish(AutoSelectStatus.SelectBoundingRectangle);
 		Rectangle maxRect = getMinBoundingRectangle();
 		if (maxRect == null || isCancelled()) // return if the task has been cancelled
 			return new Object[] {null, null};
 
 		// cut just the section that concerns me
 		BufferedImage biw = ImageConvert.cropImageNew(image, maxRect);
 		if (isCancelled()) // return if the task has been cancelled
 			return new Object[] {null, null};
 		
 		Rectangle imageBoundRect = new Rectangle(0, 0, biw.getWidth(), biw.getHeight());
 		
 		// convert the image to 2 color only:
 		// the background area to background color
 		// the rest to the color opposite to the background one
 		publish(AutoSelectStatus.ReduceImageColors);
 		reduceColors(biw, imageBoundRect);
 		if (isCancelled()) // return if the task has been cancelled
 			return new Object[] {null, null};
 		
 		ConvexHullL polygon = new ConvexHullL();
         Rectangle polygonRect;
 
 		// scan the image to find the hull envelope points
 		publish(AutoSelectStatus.FindEdgePoints);
 		List<GeomPoint> points = getEnvelopePoints(biw, imageBoundRect, 0);
 		if (points == null || isCancelled()) // return if the task has been cancelled
 			return new Object[] {null, null};
 		
 		// compute the polygon vertices and shift their coordinates
 		publish(AutoSelectStatus.FindVertices);
 		List<GeomPoint> vertices = getVertices(biw, points);
 		if (vertices == null || isCancelled()) // return if the task has been cancelled
 			return new Object[] {null, null};
         for (int i = 0; i < vertices.size(); i++) {
         	GeomPoint p = vertices.get(i);
         	p.setX(p.getX() + maxRect.x);
         	p.setY(p.getY() + maxRect.y);
         	polygon.addPoint(p);
         }
 
         if (polygon.size() < 3 || isCancelled()) // return if the task has been cancelled
 			return new Object[] {null, null};
         	
 		// if -1 or if >= the width or height of the maximum rectangle,
 		// then the max rectangle is computed, otherwise the min one
 		int nrMatches = selectMethod == ImageSelectMethod.SelectMinimum
 				? MIN_ADJACENT_PIXELS_FOR_SELECT : -1;
 		
 		// if the minimum rectangle (the maximum rectangle enclosed in the image) is needed,
 		// it has to be calculated
 		if (nrMatches > -1 && maxRect.width > nrMatches && maxRect.height > nrMatches) {
 			publish(AutoSelectStatus.ComputeLargestRectangle);
 			polygon.computeLargestRectangle();
 			polygonRect = new Rectangle(polygon.rectp.getX(), polygon.rectp.getY(),
 					polygon.rectw, polygon.recth);
 		}
 		else {
 			publish(AutoSelectStatus.ComputeEdgeList);
 			polygonRect = new Rectangle(maxRect.x, maxRect.y, maxRect.width, maxRect.height);
 			polygon.computeEdgeList();
 		}
 		if (isCancelled()) // return if the task has been cancelled
 			return new Object[] {null, null};
 		
 		publish(AutoSelectStatus.Finished);
 		return new Object[] {polygonRect, polygon.edgeList};
 	}
 
 	@Override
 	protected void process(List<AutoSelectStatus> statusList) {
 		setAutoSelectStatus(statusList.get(statusList.size() - 1));
 	}
 
 	@Override
 	public void done() {
 		try {
 			setResult(get());
 		}
 		catch (InterruptedException e) {
 			setResult(new Object[] {null, null});
 		}
 		catch (ExecutionException e) {
 			setResult(new Object[] {null, null});
 		}
 		catch (CancellationException e) {
 			setResult(new Object[] {null, null});
 		}
 	}
 
 	
 	/**
 	 * compute the coordinates of the minimum rectangle which accommodates the whole image
 	 * @return the minimum rectangle which contains the whole image
 	 */
 	private Rectangle getMinBoundingRectangle() {
 		// initialize some local variables
 		int left = selectionRect.x;
 		int right = selectionRect.x + selectionRect.width - 1;
 		int top = selectionRect.y;
 		int bottom = selectionRect.y + selectionRect.height - 1;
 		
 		boolean loopL = true, loopR = true, loopT = true, loopB = true;
 		byte directionL = 0, directionR = 0, directionT = 0, directionB = 0;
 		int prevL, prevR, prevT, prevB;
 		
 		// keep processing till no edge can be moved any more
 		while (loopL || loopR || loopT || loopB) {
 			prevL = left;
 			while (loopL) {
 				if (ImageColors.isBgColor(image, left, true, top, bottom, bgColor, bgTolerance)) {
 					// stop if the previous move was backwards or not enough room
 					// and move the left forward only if the right is far enough
 					if (directionL != -1 && left < right - 1) {
 						directionL = 1;
 						left++;
 					}
 					else {
 						if (left < right - 1)
 							left++;
 						loopL = false;
 					}
 				}
 				else {
 					// if the left has not moved forward during this step and
 					// we're on non-bg color, move it backwards and scan again
 					if (directionL != 1 && left > 0) {
 						directionL = -1;
 						left--;
 					}
 					else {
 						loopL = false;
 					}
 				}
 				if (isCancelled()) // check if the task has been cancelled
 					return null;
 			}
 
 			prevR = right;
 			while (loopR) {
 				if (ImageColors.isBgColor(image, right, true, top, bottom, bgColor, bgTolerance)) {
 					if (directionR != 1 && left < right - 1) {
 						directionR = -1;
 						right--;
 					}
 					else {
 						if (left < right - 1)
 							right--;
 						else if (right < image.getWidth() - 1)
 							right++;
 						loopR = false;
 					}
 				}
 				else {
 					if (directionR != -1 && right < image.getWidth() - 1) {
 						directionR = 1;
 						right++;
 					}
 					else {
 						loopR = false;
 					}
 				}
 				if (isCancelled()) // check if the task has been cancelled
 					return null;
 			}
 
 			// if the left or right edge have changed, make sure we process
 			// the top and bottom too
 			if (prevL != left || prevR != right) {
 				if (!loopT) {
 					directionT = 0;
 					loopT = true;
 				}
 				if (!loopB) {
 					directionB = 0;
 					loopB = true;
 				}
 			}
 			
 			prevT = top;
 			while (loopT) {
 				if (ImageColors.isBgColor(image, top, false, left, right, bgColor, bgTolerance)) {
 					if (directionT != -1 && top < bottom - 1) {
 						directionT = 1;
 						top++;
 					}
 					else {
 						if (top < bottom - 1)
 							top++;
 						loopT = false;
 					}
 				}
 				else {
 					if (directionT != 1 && top > 0) {
 						directionT = -1;
 						top--;
 					}
 					else {
 						loopT = false;
 					}
 				}
 				if (isCancelled()) // check if the task has been cancelled
 					return null;
 			}
 			
 			prevB = bottom;
 			while (loopB) {
 				if (ImageColors.isBgColor(image, bottom, false, left, right, bgColor,bgTolerance)) {
 					if (directionB != 1 && top < bottom - 1) {
 						directionB = -1;
 						bottom--;
 					}
 					else {
 						if (top < bottom - 1)
 							bottom--;
 						else if (bottom < image.getHeight() - 1)
 							bottom++;
 						loopB = false;
 					}
 				}
 				else {
 					if (directionB != -1 && bottom < image.getHeight() - 1) {
 						directionB = 1;
 						bottom++;
 					}
 					else {
 						loopB = false;
 					}
 				}
 				if (isCancelled()) // check if the task has been cancelled
 					return null;
 			}
 
 			// if the top or bottom edge have changed, make sure we process
 			// the left and right too
 			if (prevT != top || prevB != bottom) {
 				if (!loopL) {
 					directionL = 0;
 					loopL = true;
 				}
 				if (!loopR) {
 					directionR = 0;
 					loopR = true;
 				}
 			}
 		}
 		
 		return new Rectangle(left, top, right - left + 1, bottom - top + 1);
 	}
 	
 	/**
 	 * all pixels which do not match the bg color are converted to the fg color;
 	 * the conversion is applied only within the bounding rectangle 
 	 * @param bi the BufferedImage to be converted
 	 * @param boundingRect the bounding rectangle where the conversion is applied
 	 */
 	public void reduceColors(BufferedImage bi, Rectangle boundingRect) {
 		// define some easier to use variables
 		int startX = boundingRect.x;
 		int startY = boundingRect.y;
 		int endX = boundingRect.x + boundingRect.width - 1;
 		int endY = boundingRect.y + boundingRect.height - 1;

		Color fgColor = new Color(255 - bgColor.getRed(), 255 - bgColor.getGreen(),
				255 - bgColor.getBlue());
 		
 		// scan the image on the vertical, from the left edge of the bounding rectangle to the right
 		// edge of it, looking for pixels not matching the bg color and converting them to fg color
 		for (int j = startY; j <= endY; j++) {
 			
 			// get the start and the end coordinates on the current horizontal
 			// line where the non-background color zone is located
 			int res[] = ImageColors.getColorMargins(bi, j, false, startX, endX,
 					bgColor, bgTolerance);
 
 			if (isCancelled()) // check if the task has been cancelled
 				return;
 			
 			// if no coordinates have been found, the whole line is bg
 			// color; convert it to bg color
 			if (res[0] == -1 && res[1] == -1) {
 				for (int i = startX; i <= endX; i++)
 					bi.setRGB(i, j, bgColor.getRGB());
 			}
 			else {
 				// both res[0] and res[1] are > -1 in this case
 				// the first and last sections are bg, the middle one is fg 
 				for (int i = startX; i < res[0]; i++)
 					bi.setRGB(i, j, bgColor.getRGB());
 				for (int i = res[0]; i <= res[1]; i++)
 					bi.setRGB(i, j, fgColor.getRGB());
 				for (int i = res[1] + 1; i <= endX; i++)
 					bi.setRGB(i, j, bgColor.getRGB());
 			}
 
 			if (isCancelled()) // check if the task has been cancelled
 				return;
 		}
 	}
 	
 	/**
 	 * Return a list containing all the points located on the hull envelope,
 	 * starting with the top left one and going counter-clockwise on the hull
 	 * @param bi the BufferedImage to scan
 	 * @param boundingRect the bounding rectangle containing the area to scan
 	 * @param bgTol the tolerance used when trying to match the background color
 	 * @return an ArrayList of GeomPoint objects representing the hull vertices
 	 */
 	public List<GeomPoint> getEnvelopePoints(BufferedImage bi, Rectangle boundingRect, int bgTol) {
 		// set up some helper properties
 		int startX = boundingRect.x;
 		int startY = boundingRect.y;
 		int endX = boundingRect.x + boundingRect.width - 1;
 		int endY = boundingRect.y + boundingRect.height - 1;
 		
 		// the list containing the points on the left side of the hull
 		Stack<GeomPoint> pointsL = new Stack<GeomPoint>();
 		// the list containing the points on the top, right and bottom sides
 		Stack<GeomPoint> pointsR = new Stack<GeomPoint>();
 		
 		int[] marginsPrev = null;
 		boolean breakOut = false;
 		
 		for (int y = startY; y <= endY + 1; y++) {
 			// find the limits of the non-bg color
 			int[] margins = ImageColors.getColorMargins(bi, y, false, startX, endX, bgColor, bgTol);
 			
 			// if no limits were found, the whole line is bg color
 			if (margins[0] == -1 || margins[1] == -1) {
 				// continue scanning if lines containing non-bg color were not found already
 				if (!breakOut)
 					continue;
 				
 				// the last found line (which contains non-bg color) represents
 				// the bottom edge of the hull, so let's add the points to the
 				// envelope point list, taking into account the direction
 				if (!pointsR.empty()) {
 					pointsR.pop();
 					for (int x = marginsPrev[0] + 1; x <= marginsPrev[1]; x++)
 						pointsL.push(new GeomPoint(x, y - 1));
 				}
 				
 				// exit the loop as the hull has been scanned successfully
 				break;
 			}
 			
 			// if it's the first line containing non-bg color found, then it is
 			// the top edge of the hull, so add all its points to the list
 			if (!breakOut) {
 				breakOut = true;
 				pointsL.push(new GeomPoint(margins[0], y));
 				for (int x = margins[0] + 1; x <= margins[1]; x++)
 					pointsR.push(new GeomPoint(x, y));
 			}
 			// otherwise add the leftmost pixel to the left list, the rightmost
 			// pixel to the right list
 			else {
 				pointsL.push(new GeomPoint(margins[0], y));
 				pointsR.push(new GeomPoint(margins[1], y));
 			}
 			
 			marginsPrev = margins;
 
 			if (isCancelled()) // check if the task has been cancelled
 				return null;
 		}
 		
 		// merge the lists
 		while (pointsR.size() > 0)
 			pointsL.push(pointsR.pop());
 		
 		return pointsL;
 	}
 	
 	/**
 	 * Scan the hull located on the image and find the hull vertices
 	 * @param bi the BufferedImage containing the hull of color != bgColor
 	 * @param points the list of points on the hull edges
 	 * @return a list of GeomPoint objects representing the hull vertices
 	 */
 	public List<GeomPoint> getVertices(BufferedImage bi, List<GeomPoint> points) {
 		Stack<GeomPoint> vertices = new Stack<GeomPoint>();
 		
 		if (points.isEmpty())
 			return vertices;
 		
 		// convert to a collection more suitable for our purposes
 		points = new ArrayList<GeomPoint>(points);
 		
 		GeomPoint pOrig = points.get(0);
 		int offsetX = pOrig.getX();
 		int offsetY = pOrig.getY();
 		
 		// shift the axis origin to the top left point, update all coordinates
 		for (int i = 0; i < points.size(); i++) {
 			GeomPoint p = points.get(i);
 			p.setX(p.getX() - offsetX);
 			p.setY(p.getY() - offsetY);
 		}
 		
 		// scan the envelope point list and remove those located on the same
 		// edge
 		double tanPrv = 0d;
 		double tanCrt;
 		vertices.push(pOrig);
 		for (int i = 1; i < points.size(); i++) {
 			GeomPoint p = points.get(i);
 			tanCrt = ((double)p.getY() - vertices.peek().getY()) /
 					(p.getX() - vertices.peek().getX());
 			
 			// if the previous tangent is not the same as the current one, we
 			// are on a new edge, so lets update the variables
 			if (tanPrv != tanCrt || i == 1) {
 				tanPrv = tanCrt;
 				vertices.add(p);
 			}
 			else {
 				// same edge here, update the last vertex to the current point
 				vertices.pop();
 				vertices.push(p);
 			}
 			
 			if (isCancelled()) // check if the task has been cancelled
 				return null;
 		}
 		
 		// shift the axis origin back, update the vertex coordinates
 		for (int i = 0; i < vertices.size(); i++) {
 			GeomPoint p = vertices.get(i);
 			p.setX(p.getX() + offsetX);
 			p.setY(p.getY() + offsetY);
 		}
 		
 		return vertices;
 	}
 
 
 	/**
 	 * An inline ConvexHull class definition which allows me to exit the processing
 	 * if the task has been cancelled
 	 * @author alex
 	 */
 	public class ConvexHullL extends ArrayList<GeomPoint> {
 		private static final long serialVersionUID = 1L;
 	    
 	    private int start, stop; //tangents for iterative convex hull
 	    private int xmin, xmax, ymin, ymax;  //position of hull
 	    
 	    /* fixed aspect ratio */
 	    private boolean fixed;
 	    private int fixedX, fixedY;
 	    
 	    /* largest rectangle's attributes */
 	    public GeomPoint rectp;
 	    public int recth, rectw;
 	    
 	    public ArrayList<GeomEdge> edgeList;
 	    
 	    
 	    public ConvexHullL() {
 	        this.fixed = false;
 	        this.fixedX = 1;
 	        this.fixedY = 1;
 	    }
 	    
 	    /* position of point w.r.t. hull edge
 	     * sign of twice the area of triangle abc
 	     */
 	    public boolean onLeft(GeomPoint a, GeomPoint b, GeomPoint c) {
 	        int area = (b.getX() - a.getX()) * (c.getY() - a.getY()) -
 	        		(c.getX() - a.getX()) * (b.getY() - a.getY());
 	        return (area < 0);
 	    }
 	    
 	    /* check if point is outside
 	     * true is point is on right of all vertices
 	     * finds tangents if point is outside
 	     */
 	    public boolean pointOutside(GeomPoint p) {
 	        
 	        boolean ptIn = true, currIn, prevIn = true;
 	        
 	        GeomPoint a = (GeomPoint)this.get(0);
 	        GeomPoint b;
 	        
 	        for (int i = 0; i < this.size(); i++) {
 	            b = (GeomPoint)this.get((i+1)%this.size());
 	            currIn = onLeft(a, b, p);
 	            ptIn = ptIn && currIn;
 	            a = b;
 	            
 	            if (prevIn && !currIn) {
 	            	start = i;
 	            }  /* next point outside, 1st tangent found */
 	            if (!prevIn && currIn) {
 	            	stop = i;
 	            }  /* 2nd tangent */
 	            prevIn = currIn;
 	            
 	        }
 	        return !ptIn;
 	    }
 	    
 	    public void addPoint(GeomPoint p) {
 			if (size() < 2) {
 				add(p);
 			}
 			else if (size() == 2) {
 				GeomPoint ha = (GeomPoint) get(0);
 				GeomPoint hb = (GeomPoint) get(1);
 				if (onLeft(ha, hb, p))
 					add(p);
 				else
 					add(1, p);
 			}
 			else {
 				addPointToHull(p);
 			}
 		}
 	    
 	    /* check if point is outside, insert it, maintaining general position */
 	    private boolean addPointToHull(GeomPoint p) {
 	        
 	        /* index of tangents */
 	        start = 0;
 	        stop = 0;
 	        
 	        if (!pointOutside(p)) {
 	            return false;
 	        }
 	        
 	        /* insert point */
 	        int numRemove;
 	        
 	        if (stop > start) {
 	            numRemove = stop - start - 1;
 	            if (numRemove > 0) {
 	                this.removeRange(start+1, stop);
 	            }
 	            this.add(start+1, p); //insertElmentAt(p, start+1);
 	        }
 	        else{
 	            numRemove = stop + this.size() - start - 1;
 	            if (numRemove > 0) {
 	                if (start+1 < this.size()) {
 	                    this.removeRange(start+1, this.size());
 	                }
 	                if (stop-1 >= 0) {
 	                    this.removeRange(0, stop);
 	                }
 	            }
 	            this.add(p);
 	          
 	        }
 	        return true;
 	    } //addPointToHull
 	    
 	    /* compute edge list
 	     * set xmin, xmax
 	     * used to find largest rectangle by scanning horizontally
 	     */
 	    public void computeEdgeList() {
 	    	edgeList = new ArrayList<GeomEdge>();
 	        GeomPoint a, b;
 	        GeomEdge e;
 	        a = (GeomPoint)this.get(this.size()-1);
 	        for (int i = 0; i < this.size(); i++) {
 	            b = (GeomPoint)this.get(i);
 	            //b = (GeomPoint)this.elementAt(i+1);
 	            
 	            if (i==0) {
 	                this.xmin = a.getX();
 	                this.xmax = a.getX();
 	                this.ymin = a.getY();
 	                this.ymax = a.getY();
 	            }
 	            else {
 	                if (a.getX() < this.xmin) {
 	                    this.xmin = a.getX();
 	                }
 	                if (a.getX() > this.xmax) {
 	                    this.xmax  = a.getX();
 	                    // this.yxmax = a.getY();
 	                }
 	                if (a.getY() < this.ymin) {
 	                    this.ymin = a.getY();
 	                }
 	                if (a.getY() > this.ymax) {
 	                    this.ymax  = a.getY();
 	                }
 	            }
 	            e = new GeomEdge(a,b);
 	            edgeList.add(e);
 	            a = b;
 	            
 	            if (isCancelled())
 	            	return;
 	        } //for
 	        // b = (GeomPoint)this.elementAt(this.size()-1);
 	        // a = (GeomPoint)this.elementAt(0);
 	        // e = new GeomEdge(b,a);
 	        // l.add(e);
 	    }
 	    
 	    /* compute y intersection with an edge
 	     * first pixel completely inside
 	     * ceil function if edge is on top, floor otherwise
 	     * (+y is down)
 	     */
 	    public int yIntersect(int xi, GeomEdge e) {
 	        int y;
 	        double yfirst = (e.m()) * (xi-0.5) + e.b();
 	        double ylast = (e.m()) * (xi+0.5) + e.b();
 	        
 	        if (!e.isTop()) {
 	            y = (int)Math.floor(Math.min(yfirst, ylast));
 	        }
 	        else {
 	            y = (int)Math.ceil(Math.max(yfirst, ylast));
 	        }
 	        return y;
 	    }
 	    
 	    /* find largest pixel completely inside
 	     * look through all edges for intersection
 	     */
 	    public int xIntersect(int y, ArrayList<GeomEdge> l) {
 	        int x = 0;
 	        double x0 = 0, x1 = 0;
 	        for (int i = 0; i < this.size(); i++) {
 	            GeomEdge e = (GeomEdge)l.get(i);
 	            if (e.isRight() && e.ymin() <= y && e.ymax() >= y) {
 	                x0 = (double)(y+0.5 - e.b()) / e.m();
 	                x1 = (double)(y-0.5 - e.b()) / e.m();
 	            }
 	        }
 	        x = (int)Math.floor(Math.min(x0,x1));
 	        return x;
 	    }
 	    
 	    public GeomEdge findEdge(int x, boolean isTop, ArrayList<GeomEdge> l){
 	        GeomEdge e, emax = (GeomEdge)l.get(0);
 	        //int count = 0;
 	        for (int i = 0; i < this.size(); i++) {
 	            e = (GeomEdge)l.get(i);
 	            if (e.xmin() == x) {
 	                //count++;
 	                //if (count == 1){
 	                //    emax = e;
 	                //}
 	                //else{
 	                if (e.xmax() != e.xmin()) {
 	                    if ((e.isTop() && isTop) || (!e.isTop() && !isTop)) {
 	                        emax = e;
 	                    }
 	                }
 	            }
 	            
 	        }
 	        return emax;
 	    }
 	    
 	    /* compute 3 top and bottom 3 corner rectangle for each xi
 	     * find largest 2 corner rectangle
 	     */
 	    public void computeLargestRectangle() {
 	    	
 	    	computeEdgeList();
 
 			if (isCancelled()) // return if the task has been cancelled
 				return;
 	        
 	        GeomEdge top, bottom;
 	        int ymax, ymin, xright, xlo, xhi;
 	        int area, maxArea = 0;
 	        int width, height, maxh = 0, maxw = 0;
 	        
 	        GeomPoint maxp = new GeomPoint(0,0);
 	        
 	        ArrayList<GeomPoint> xint = new ArrayList<GeomPoint>();
 	        
 	        for (int i = 0; i <= this.ymax; i++) {
 	            int x = xIntersect(i, edgeList);
 	            GeomPoint px = new GeomPoint(x, i);
 	            xint.add(px);
 	        }
 	        //find first top and bottom edges
 	        top = findEdge(this.xmin, true, edgeList);
 	        bottom = findEdge(this.xmin, false, edgeList);
 	        
 	        //scan for rectangle left position
 	        for (int xi = this.xmin; xi < this.xmax; xi++) {
 	            
 	            ymin = yIntersect(xi, top);
 	            ymax = yIntersect(xi, bottom);
 	            
 	            for (int ylo = ymax;ylo >= ymin; ylo--) {//ylo from to to bottom
 	                
 	                for (int yhi = ymin; yhi <= ymax; yhi++) {
 	                
 	                    if (yhi > ylo) {
 	                        
 	                        xlo = (int)((GeomPoint)xint.get(ylo)).getX();
 	                        	// xIntersect(ylo,edgeList);
 	                        xhi = (int)((GeomPoint)xint.get(yhi)).getX();
 	                        	// xIntersect(yhi,edgeList);
 	                        
 	                        xright = Math.min(xlo, xhi);
 	                        
 	                        height = yhi-ylo;
 	                        width = xright - xi;
 	                            
 	                        if (!this.fixed) {
 	                        }//!fixed
 	                        else {
 	                          int fixedWidth = (int) Math
 										.ceil(((double) height * this.fixedX)
 												/ ((double) this.fixedY));
 	                          if (fixedWidth <= width) {
 	                              width = fixedWidth;
 	                          }
 	                          else {
 	                              width = 0;
 	                          }
 	                       }
 	                       area = width * height;
 	                        
 	                       if (area > maxArea) {
 	                            maxArea = area;
 	                            maxp = new GeomPoint(xi, ylo);
 	                            maxw = width;
 	                            maxh = height;
 	                       }
 	                    }  // end if yhi > ylo
 
 	        			if (isCancelled()) // return if the task has been cancelled
 	        				return;
 	                }  // end for yhi
 	            }  // end for ylo
 	            if (xi == top.xmax()) {
 	                top = findEdge(xi,  true, edgeList);
 	            }
 	            if (xi == bottom.xmax()) {
 	                bottom = findEdge(xi, false, edgeList);
 	            }
 	        }  // end for xi
 	        this.rectp = maxp;
 	        this.recth = maxh;
 	        this.rectw = maxw;
 	    }
 	    
 	}   
 
 	
 }
