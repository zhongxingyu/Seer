 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 
 import uk.ac.diamond.scisoft.analysis.roi.GridROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 
 /**
  *     A BoxSelection with grid or dots
  * 
  *     p1------------p2
  *     |  o o o o o   |
  *     |  o o o o o   |
  *     p3------------p4
  *     
  *     This class is public so that it can be cast and the various
  *     color settings accessed.
  *     
  *     Normally concrete class of IRegion should not be used
  *     
  * @author fcp94556
  */
 public class GridSelection extends BoxSelection {
 		
 	private Color pointColor = ColorConstants.white;
 	private Color gridColor  = ColorConstants.lightGray;
 	
 	/**
 	 * Non public, it can be used from outside but not created there.
 	 * @param name
 	 * @param coords
 	 */
 	GridSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(IRegion.RegionType.GRID.getDefaultColor());	
 		setAlpha(80);
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-grid.png";
 	}
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.GRID;
 	}	
 	protected Figure createRegionFillFigure() {
 		return new RegionFillFigure(this) {
 			@Override
 			public void paintFigure(Graphics gc) {
 				
 				super.paintFigure(gc);
 				final Rectangle size = getRectangleFromVertices();				
 				this.bounds = size.getCopy().expand(5, 5);
 				gc.setAlpha(getAlpha());
 				gc.fillRectangle(size);
 				
 				GridSelection.this.drawLabel(gc, size);
 				
 				if (getROI()!=null && getROI() instanceof GridROI) {
 				    GridROI groi = (GridROI)createROI(false);
 					if (groi.isMidPointOn()) {
 						double[][] points = getGridPoints(groi);
 						if (points!=null) {
 							double[] xpoints = points[0];
 							double[] ypoints = points[1];
 	                        for (int i = 0; i < Math.min(xpoints.length, ypoints.length); i++) {
 	                        	drawMidPoint(xpoints[i], ypoints[i], gc);
 	 						}
 						}
 					}
 					
 					if (groi.isGridLineOn()) {
 						drawGridLines(groi, gc);
 					}
 
 				}
 			}
 		};
 	}
 
 	protected void drawMidPoint(double x, double y, Graphics gc) {
 		
 		int[] pnt = coords.getValuePosition(x, y);
 		gc.pushState();
 		gc.setAlpha(255);
 		gc.setForegroundColor(pointColor);
 		gc.setBackgroundColor(pointColor);
 		gc.fillOval(pnt[0], pnt[1], 5, 5);
 		gc.popState();
 	}
 	
 	/**
 	 * 
 	 * @param groi
 	 * @return [xpoints][ypoints]
 	 */
 	protected double[][] getGridPoints(GridROI groi) {
 
 		double[][] gridPoints = groi.getGridPoints();
 		int xGrids = gridPoints[0].length;
 		int yGrids = gridPoints[1].length;
 		if (xGrids > 0 && yGrids > 0) {
 			int numPoints = xGrids * yGrids;
 			double[] xPoints = new double[numPoints];
 			double[] yPoints = new double[numPoints];
 
 			int cnt = 0;
 			for (int i = 0; i < xGrids; i++) {
 				for (int j = 0; j < yGrids; j++) {
 					xPoints[cnt] = gridPoints[0][i];
 					yPoints[cnt] = gridPoints[1][j];
 					cnt++;
 				}
 			}
 			return new double[][]{xPoints, yPoints};
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param groi
 	 * @return [xpoints][ypoints]
 	 */
 	protected void drawGridLines(GridROI groi, Graphics gc) {
 
 		gc.pushState();
 		gc.setAlpha(255);
 		gc.setForegroundColor(gridColor);
 		gc.setBackgroundColor(gridColor);
 		gc.setLineWidth(1);
 		gc.setLineStyle(SWT.LINE_SOLID);
 		double[]   spt       = groi.getPoint();
 		double[]   len       = groi.getLengths();
 		double[][] gridLines = groi.getGridLines();
 		int xGrids = gridLines[0].length;
 		int yGrids = gridLines[1].length;
 		if (xGrids != 0 && yGrids != 0) {
 			for (int i = 0; i < xGrids; i++) {
 				int[] pnt1 = coords.getValuePosition(gridLines[0][i], spt[1]);
 				int[] pnt2 = coords.getValuePosition(gridLines[0][i], spt[1] + len[1]);
 				gc.drawLine(pnt1[0], pnt1[1], pnt2[0], pnt2[1]);
 			}
 			
 			for (int i = 0; i < yGrids; i++) {
 
 				int[] pnt1 = coords.getValuePosition(spt[0], gridLines[1][i]);
 				int[] pnt2 = coords.getValuePosition(spt[0] + len[0], gridLines[1][i]);
 				gc.drawLine(pnt1[0], pnt1[1], pnt2[0], pnt2[1]);
 			}
 		}
 		
 		gc.popState();
 
 	}
 
 
 	@Override
 	public IROI createROI(boolean recordResult) {
 		if (p1!=null) {
 			final Rectangle  rect = getRectangleFromVertices();			
 			final GridROI    groi = (GridROI)getRoiFromRectangle(rect);
 			
 			if (getROI() != null && getROI() instanceof GridROI) {
 				GridROI oldRoi = (GridROI)getROI();
 				// Copy grid, preferences, etc from existing GridROI
 				// This maintains spacing etc. until it is changed in setROI(...)
 				// These things are determined externally by the user of the ROI
 				// and we passively draw them here. TODO Consider about how to edit
 				// these in the RegionComposite...
 				groi.setxySpacing(oldRoi.getxSpacing(), oldRoi.getySpacing());
 				groi.setGridPreferences(oldRoi.getGridPreferences());
 			    groi.setGridLineOn(oldRoi.isGridLineOn());
 			    groi.setMidPointOn(oldRoi.isMidPointOn());
 			    
 			}
			groi.setName(getName());
 			if (recordResult) roi = groi;
 			return groi;
 		}
 		return super.getROI();
 	}
 	
 	@Override
 	protected RectangularROI createROI(double ptx, double pty, double width, double height, double angle) {
 		GridROI groi = new GridROI(ptx, pty, width, height, angle);
 		if (roi!=null) groi.setPlot(roi.isPlot());
 		// set the Region isActive flag
 		this.setActive(this.isActive());
 		return groi;
 	}
 
 	@Override
 	protected void updateROI(IROI roi) {
 		if (roi instanceof RectangularROI) {
 			RectangularROI groi = (RectangularROI) roi;
 			if (p1!=null) p1.setPosition(groi.getPointRef());
 			if (p4!=null) p4.setPosition(groi.getEndPoint());
 			updateConnectionBounds();
 		}
 	}
 
 	public Color getPointColor() {
 		return pointColor;
 	}
 
 	public void setPointColor(Color pointColor) {
 		this.pointColor = pointColor;
 	}
 	
 	public Color getGridColor() {
 		return gridColor;
 	}
 
 	public void setGridColor(Color gridColor) {
 		this.gridColor = gridColor;
 	}
 	
 	@Override
 	protected void updateConnectionBounds() {
 		if (connection==null) return;
 		final Rectangle size = getRectangleFromVertices();				
 		size.expand(5, 5);
 		connection.setBounds(size);
 	}
 	
 }
