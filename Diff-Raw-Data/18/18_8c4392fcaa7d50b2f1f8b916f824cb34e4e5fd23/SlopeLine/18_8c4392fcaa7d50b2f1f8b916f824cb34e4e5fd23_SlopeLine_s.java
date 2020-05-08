 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package plotter.xy;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Point2D;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JComponent;
 
 /**
  * A slope line allows the user to click and drag on a plot, drawing a line.
  * Slope lines are allowed to be attached to multiple plots at the same time.
  * This works because the user can only draw a slope line on one plot at a time.
  * @author Adam Crume
  */
 public class SlopeLine extends JComponent implements MouseListener, MouseMotionListener {
 	private static final long serialVersionUID = 1L;
 
 	private static final int CURSOR_BOX_SIZE = 4;
 
 	/** X coordinate of the point where the user started drawing a slope line. */
 	private int startX;
 
 	/** Y coordinate of the point where the user started drawing a slope line. */
 	private int startY;
 
 	/** Plot contents displaying the slope line, or null if the slope line is not displayed. */
 	private XYPlotContents contents;
 
 	// Note that the end points of the slope line are not guaranteed to be in any particular order.
 
 	/** X coordinate of end 1 of the slope line, or -1 to flag that the line is not yet visible. */
 	private int x1;
 
 	/** Y coordinate of end 1 of the slope line. */
 	private int y1;
 
 	/** X coordinate of end 2 of the slope line. */
 	private int x2;
 
 	/** Y coordinate of end 2 of the slope line. */
 	private int y2;
 
 	/** Maps plots to listeners for that plot.  The key null is used for listeners that listen to all plots.  May be null. */
 	private Map<XYPlot, Set<Listener>> listeners;
 
 	private boolean showSlopeLine = false;
 	@Override
 	protected void paintComponent(Graphics g) {
 		if(contents != null && x1 != -1) {
 			Color savedColor = g.getColor();
 			try {
 				g.setColor(getForeground());
 				int b = CURSOR_BOX_SIZE;
 				// Draw a small rectangle where the user originally pressed the mouse button.
 				g.drawRect(startX - b / 2, startY - b / 2, b, b);
 				// Draw the slope line itself.
 				g.drawLine(x1, y1, x2, y2);
 			} finally {
 				g.setColor(savedColor);
 			}
 		}
 	}
 
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// ignore
 	}
 
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// ignore
 	}
 
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// ignore
 	}
 
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		showSlopeLine = false;
	    if ((e.getModifiersEx() & (InputEvent.BUTTON1_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.BUTTON1_DOWN_MASK)
 	    	showSlopeLine = true;
 	    else
 	    	return;
 	    
 		startX = e.getX();
 		startY = e.getY();
 		x1 = -1;
 		contents = (XYPlotContents) e.getSource();
 		contents.add(this);
 		contents.revalidate();
 
 		XYPlot plot = (XYPlot) contents.getParent();
 		Set<Listener> set = getListenersForPlot(plot);
 		if(set != null) {
 			Point2D p = new Point2D.Double(startX, startY);
 			plot.toLogical(p, p);
 			double startLogicalX = p.getX();
 			double startLogicalY = p.getY();
 			for(Listener listener : set) {
 				listener.slopeLineAdded(this, plot, startLogicalX, startLogicalY);
 			}
 		}
 	}
 
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (!showSlopeLine)
 			return;
 		
 		if(contents == null) {
 			return;
 		}
 
 		contents.remove(this);
 
 		int minX = Math.min(x1, x2);
 		int maxX = Math.max(x1, x2);
 		int minY = Math.min(y1, y2);
 		int maxY = Math.max(y1, y2);
 		repaintLine(contents, minX, minY, maxX, maxY);
 
 		XYPlot plot = (XYPlot) contents.getParent();
 		Set<Listener> set = getListenersForPlot(plot);
 		if(set != null) {
 			for(Listener listener : set) {
 				listener.slopeLineRemoved(this, plot);
 			}
 		}
 
 		contents = null;
 	}
 
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if (!showSlopeLine)
 			return;
 		
 		// Old bounding box
 		int minX = Math.min(x1, x2);
 		int maxX = Math.max(x1, x2);
 		int minY = Math.min(y1, y2);
 		int maxY = Math.max(y1, y2);
 
 		// Extend the line to the edges of the contents
 		int dx = e.getX() - startX;
 		int dy = e.getY() - startY;
 		if(dx == 0 && dy == 0) {
 			x1 = -1;
 		} else if(Math.abs(dx) > Math.abs(dy)) {
 			double m = dy / (double) dx;
 			double b = startY - m * startX;
 			x1 = 0;
 			y1 = (int) (b + .5);
 			x2 = getWidth();
 			y2 = (int) (m * x2 + b + .5);
 		} else {
 			double m = dx / (double) dy;
 			double b = startX - m * startY;
 			y1 = 0;
 			x1 = (int) (b + .5);
 			y2 = getHeight();
 			x2 = (int) (m * y2 + b + .5);
 		}
 
 		// Union the old bounding box with the new one
 		minX = Math.min(minX, Math.min(x1, x2));
 		maxX = Math.max(maxX, Math.max(x1, x2));
 		minY = Math.min(minY, Math.min(y1, y2));
 		maxY = Math.max(maxY, Math.max(y1, y2));
 
 		// Repaint the union of the old and new bounding boxes
 		repaintLine(this, minX, minY, maxX, maxY);
 
 
 		XYPlot plot = (XYPlot) contents.getParent();
 		Set<Listener> set = getListenersForPlot(plot);
 		if(set != null) {
 			Point2D p = new Point2D.Double(startX, startY);
 			plot.toLogical(p, p);
 			double startLogicalX = p.getX();
 			double startLogicalY = p.getY();
 			p.setLocation(e.getX(), e.getY());
 			plot.toLogical(p, p);
 			double endLogicalX = p.getX();
 			double endLogicalY = p.getY();
 			for(Listener listener : set) {
 				listener.slopeLineUpdated(this, plot, startLogicalX, startLogicalY, endLogicalX, endLogicalY);
 			}
 		}
 	}
 
 
 	/**
 	 * Returns all listeners that should receive events for the plot.
 	 * A return value of null is equivalent to the empty set.
 	 * @param plot plot with changes
 	 * @return relevant listeners, may be null
 	 */
 	private Set<Listener> getListenersForPlot(XYPlot plot) {
 		if(listeners != null) {
 			Set<Listener> wildcardListeners = listeners.get(null);
 			Set<Listener> specificListeners = listeners.get(plot);
 			if(wildcardListeners == null) {
 				return specificListeners;
 			} else if(specificListeners == null) {
 				return wildcardListeners;
 			} else {
 				Set<Listener> s = new HashSet<Listener>(wildcardListeners);
 				s.addAll(specificListeners);
 				return s;
 			}
 		}
 		return null;
 	}
 
 
 	/**
 	 * Given the slope line's bounding box, repaints an area large enough to contain the bounding box and the cursor rectangle.
 	 * @param c component to repaint
 	 * @param minX minimum X coordinate of the bounding box
 	 * @param minY minimum Y coordinate of the bounding box
 	 * @param maxX maximum X coordinate of the bounding box
 	 * @param maxY maximum Y coordinate of the bounding box
 	 */
 	private void repaintLine(Component c, int minX, int minY, int maxX, int maxY) {
 		int width = maxX - minX + 1;
 		int height = maxY - minY + 1;
 		if(width < CURSOR_BOX_SIZE) {
 			minX = (minX + maxX) / 2 - CURSOR_BOX_SIZE / 2 - 1;
 			width = CURSOR_BOX_SIZE + 2;
 		}
 		if(height < CURSOR_BOX_SIZE) {
 			minY = (minY + maxY) / 2 - CURSOR_BOX_SIZE / 2 - 1;
 			height = CURSOR_BOX_SIZE + 2;
 		}
 		c.repaint(minX, minY, width, height);
 	}
 
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// ignore
 	}
 
 
 	/**
 	 * Attaches this slope line to a plot.
 	 * @param plot plot to attach to
 	 */
 	public void attach(XYPlot plot) {
 		XYPlotContents contents = plot.getContents();
 		if(contents == null) {
 			throw new IllegalArgumentException("Plot does not contain an XYPlotContents component");
 		}
 		contents.addMouseListener(this);
 		contents.addMouseMotionListener(this);
 	}
 
 
 	/**
 	 * Adds a listener for slope changes.
 	 * @param plot plot to listen to, or null for all plots this line is attached to
 	 * @param listener listener to add
 	 */
 	public void addListenerForPlot(XYPlot plot, Listener listener) {
 		if(listeners == null) {
 			listeners = new HashMap<XYPlot, Set<Listener>>();
 		}
 		Set<Listener> set = listeners.get(plot);
 		if(set == null) {
 			set = new HashSet<Listener>();
 			listeners.put(plot, set);
 		}
 		set.add(listener);
 	}
 
 
 	/**
 	 * Listens for slope changes.
 	 * @author Adam Crume
 	 */
 	public interface Listener {
 		/**
 		 * Notifies that a slope line has been started.
 		 * @param line line that was added
 		 * @param plot plot the line is displayed on
 		 * @param startX X coordinate of the start point
 		 * @param startY Y coordinate of the start point
 		 */
 		void slopeLineAdded(SlopeLine line, XYPlot plot, double startX, double startY);
 
 
 		/**
 		 * Notifies that a slope line has been updated.
 		 * @param line line that was updated
 		 * @param plot plot the line is displayed on
 		 * @param startX X coordinate of the start point
 		 * @param startY Y coordinate of the start point
 		 * @param endX X coordinate of the end point
 		 * @param endY Y coordinate of the end point
 		 */
 		void slopeLineUpdated(SlopeLine line, XYPlot plot, double startX, double startY, double endX, double endY);
 
 
 		/**
 		 * Notifies that a slope line has been hidden.
 		 * @param line line that was hidden
 		 * @param plot plot that the line was displayed on
 		 */
 		void slopeLineRemoved(SlopeLine line, XYPlot plot);
 	}
 }
