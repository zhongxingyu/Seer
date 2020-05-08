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
 package gov.nasa.arc.mct.fastplot.bridge;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import plotter.xy.XYAxis;
 import plotter.xy.XYPlot;
 
 @SuppressWarnings("serial")
 public final class MarqueeZoomListener extends MouseAdapter {
 	
 	private static final Color END_POINT_FOREGROUND = new Color(100, 0, 0);
 	private static final Color START_POINT_FOREGROUND = new Color(0, 100, 0);
 	private static final Color TRANSLUCENT_WHITE = new Color(255, 255, 255, 200);
 
 	private int clickX, clickY;
 	private Rectangle marqueeRect = null;
 	private MarqueeCanvas canvas;
 	private XYPlot xyPlot;
     private int mouseStartX, mouseStartY, mouseEndX, mouseEndY;
     private SimpleDateFormat dateFormat;
     private PlotLocalControlsManager plotLocalControlsManager;
     private Font labelFont;
     private static Cursor ZOOM_IN_PLOT_CURSOR;
 
     static {
     	Toolkit toolkit = Toolkit.getDefaultToolkit();
     	try {
 			Image image = ImageIO.read(MarqueeZoomListener.class.getResourceAsStream("/images/zoomCursor.png"));
 	    	Point cursorHotSpot = new Point(0,0);
 	    	ZOOM_IN_PLOT_CURSOR = toolkit.createCustomCursor(image, cursorHotSpot, "Zoom In Plot"); 
 		} catch (IOException e) {
 			ZOOM_IN_PLOT_CURSOR = Cursor.getDefaultCursor();
 		}
     }
 	
 	public MarqueeZoomListener(PlotLocalControlsManager plotLocalControlsManager, XYPlot xyPlot, SimpleDateFormat dateFormat, Font labelFont) {
 		this.xyPlot = xyPlot;
 		this.dateFormat = dateFormat;
 		this.plotLocalControlsManager = plotLocalControlsManager;
 		this.labelFont = labelFont;
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		
         if (marqueeRect == null) {
             // this method is invoked during each drag event, so dragStart tracks the start of the drag event
             if (canvas != null) {
                 // check the cursor to see if the user is currently in a move or resize mode
                 // also verify whether the selected point is directly in the top level panel. This
                 // should prevent nested selections from triggering selections at the top level. 
                 marqueeRect = new Rectangle();
                 clickX = mouseEndX = e.getX();
                 clickY = mouseEndY = e.getY();
 				marqueeRect.setBounds(e.getX(), e.getY(), 1, 1);
 				canvas.repaint();
             }
         } else {
             mouseEndX = e.getX();
             mouseEndY = e.getY();
 
             if (mouseEndX >= clickX) {
                 if (mouseEndY >= clickY) {
                     // existing upper left point is unchanged, so just adjust
                     // the height, width
                 	marqueeRect.setBounds(clickX, clickY, mouseEndX - clickX, mouseEndY - clickY);
                 } else {
                     // original click is now the lower left point
                     int width = mouseEndX - clickX;
                     marqueeRect.setBounds(mouseEndX - width, mouseEndY, width, clickY - mouseEndY);
                 }
             } else {
                 if (mouseEndY >= clickY) {
                     // original click is now the upper right point
                 	marqueeRect.setBounds(mouseEndX, clickY, clickX - mouseEndX, mouseEndY - clickY);
                 } else {
                     // original click is now the lower right point
                 	marqueeRect.setBounds(mouseEndX, mouseEndY, clickX - mouseEndX, clickY - mouseEndY);
                 }
             }
             canvas.repaint();
         }
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (canvas != null) {
 			xyPlot.getContents().remove(canvas);
 			Point2D startCoord = convertPointToLogicalCoord(mouseStartX, mouseStartY);
 			Point2D endCoord = convertPointToLogicalCoord(mouseEndX, mouseEndY);
 			
 			XYAxis xAxis = xyPlot.getXAxis();
 			if (endCoord.getX() > startCoord.getX()) {				
 				xAxis.setStart(startCoord.getX());
 				xAxis.setEnd(endCoord.getX());
 			} else {
 				xAxis.setStart(endCoord.getX());
 				xAxis.setEnd(startCoord.getX());				
 			}
 			
 			XYAxis  yAxis = xyPlot.getYAxis();
 			if (endCoord.getY() > startCoord.getY()) {
 				yAxis.setStart(startCoord.getY());
 				yAxis.setEnd(endCoord.getY());
 			} else {
 				yAxis.setStart(endCoord.getY());
 				yAxis.setEnd(startCoord.getY());				
 			}
 			
			plotLocalControlsManager.pinXYAxesAfterZoomedIn();
 			marqueeRect = null;
 			canvas = null;
 			xyPlot.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 		}
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		int onmask = InputEvent.ALT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
 	    int offmask = InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK;
 	    if ((e.getModifiersEx() & (onmask | offmask)) == onmask) {
 			canvas = new MarqueeCanvas();
 			xyPlot.getContents().add(canvas);
 			xyPlot.getContents().setComponentZOrder(canvas, 0);
 			mouseStartX = e.getX();
 			mouseStartY = e.getY();
 			xyPlot.setCursor(ZOOM_IN_PLOT_CURSOR);
 	    }			
 	}
 	
 	private Point2D convertPointToLogicalCoord(int x, int y) {
 		Point2D p = new Point2D.Double();	        
 		xyPlot.getContents().toLogical(p, new Point(x, y));	        
         return p;
 	}
 		
 	private class MarqueeCanvas extends JPanel {
 		private MessageFormat messageFormat = new MessageFormat(" (X: {0} Y: {1}) ");
 		
 		public MarqueeCanvas() {
 			setOpaque(false);
 			setBackground(null);
 		}
 				
 		@Override
 		public void paint(Graphics g) {
 			if (marqueeRect == null) {
 				super.paint(g);
 				return;
 			}
 			
 	        Graphics2D g2 = (Graphics2D) g;
 	        RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
 	                RenderingHints.VALUE_ANTIALIAS_ON);
 	        renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 			g2.setFont(labelFont);
 	        FontMetrics fontMetrics = g2.getFontMetrics();
 	        g2.setRenderingHints(renderHints);
 	        g2.setStroke(new BasicStroke(1f,
 	        							 BasicStroke.CAP_SQUARE,
 	        							 BasicStroke.JOIN_MITER,
 	        							 1f,
 	        							 new float[]{2f},
 	        							 0.0f));
 	        g2.setColor(Color.white);
 	        g2.drawRect(marqueeRect.x, marqueeRect.y, marqueeRect.width, marqueeRect.height);
 
 			Point2D p = convertPointToLogicalCoord(mouseStartX, mouseStartY);	        
 	        String startPoint = messageFormat.format(new Object[]{p.getX(), p.getY()});
 			messageFormat.setFormatByArgumentIndex(0, dateFormat);			
 			messageFormat.setFormatByArgumentIndex(1, PlotConstants.DECIMAL_FORMAT);
 			g2.setColor(TRANSLUCENT_WHITE);
 			g2.fillRect(mouseStartX, mouseStartY - fontMetrics.getHeight() - 2, fontMetrics.stringWidth(startPoint), fontMetrics.getHeight());
 			g2.setColor(START_POINT_FOREGROUND);
 	        g2.drawChars(startPoint.toCharArray(), 0, startPoint.length(), mouseStartX, mouseStartY - 4);
 
 			p = convertPointToLogicalCoord(mouseEndX, mouseEndY);	        
 	        String endPoint = messageFormat.format(new Object[]{p.getX(), p.getY()});
 			messageFormat.setFormatByArgumentIndex(0, dateFormat);			
 			messageFormat.setFormatByArgumentIndex(1, PlotConstants.DECIMAL_FORMAT);
 			g2.setColor(TRANSLUCENT_WHITE);
 			g2.fillRect(mouseEndX, mouseEndY - fontMetrics.getHeight() - 2, fontMetrics.stringWidth(endPoint), fontMetrics.getHeight());
 			g2.setColor(END_POINT_FOREGROUND);
 	        g2.drawChars(endPoint.toCharArray(), 0, endPoint.length(), mouseEndX, mouseEndY - 4);
 		}
 
 	}
 
 }
