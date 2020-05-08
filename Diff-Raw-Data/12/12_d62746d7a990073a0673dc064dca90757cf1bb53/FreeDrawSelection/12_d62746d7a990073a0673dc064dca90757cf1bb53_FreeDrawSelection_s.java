 package org.dawb.workbench.plotting.system.swtxy.selection;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 
 import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 /**
  * Used for masking. This region can be transformed into the masking
  * dataset using MaskCreator (or code similar to).
  * 
  * The region bounds from this selection is a polyline region
  * bounds consisting of the 
  * 
  * @author fcp94556
  *
  */
 class FreeDrawSelection extends AbstractSelectionRegion {
 
 	private PointList points;
 
 	public FreeDrawSelection(String name, Axis xAxis, Axis yAxis) {
 		super(name, xAxis, yAxis);
 		setRegionColor(ColorConstants.orange);
 		setLineWidth(10);
 		setAlpha(160);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		parent.add(this);
 		updateConnectionBounds();
 	}
 	
 	@Override
 	public boolean isMobile() {
 		return false; // You cannot move this figure yet...
 	}
 
 	@Override
 	public void setLineWidth(int width) {
 		super.setLineWidth(width);
 		updateConnectionBounds();
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.FREE_DRAW;
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		
 		if (points==null) return;
 		final Rectangle pntBounds = points.getBounds().getCopy();
 		pntBounds.x     -=getLineWidth(); 
 		pntBounds.y     -=getLineWidth(); 
 		pntBounds.width +=2*getLineWidth(); 
 		pntBounds.height+=2*getLineWidth(); 
 		setBounds(pntBounds);
 	}
 
 	@Override
 	public void paintBeforeAdded(Graphics g, 
 			                     PointList clicks,
 			                     Rectangle parentBounds) {
 		
 		if (points==null) {
 			points = new PointList();
 			points.addPoint(clicks.getFirstPoint());
 		}
 		points.addPoint(clicks.getLastPoint());
 		
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.setLineWidth(getLineWidth());
 		g.drawPolyline(points);
 	}
 	
 	@Override
 	public void paintFigure(Graphics g) {
 		
 		super.paintFigure(g);
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.setLineWidth(getLineWidth());
 		g.drawPolyline(points);
 		
 		g.setAlpha(255);
 		g.setForegroundColor(ColorConstants.black);
 		if (isShowPosition()) {
 			drawPointText(g, points.getFirstPoint());
 			drawPointText(g, points.getLastPoint());
 		}
 		
 		if (isShowLabel()) {
 			g.drawText(getName(), points.getMidpoint());
 		}
 	}
 
 	private void drawPointText(Graphics g, Point pnt) {
 		
 		double[] loc = new double[]{getXAxis().getPositionValue(pnt.x, false), getYAxis().getPositionValue(pnt.y, false)};
         final String text = getLabelPositionText(loc);
         g.drawString(text, pnt);
 
 	}
 	
 	private NumberFormat format = new DecimalFormat("######0.00");
 	
 	protected String getLabelPositionText(double[] p) {
 		
 		if (Double.isNaN(p[0])||Double.isNaN(p[1])) return "";
 		final StringBuilder buf = new StringBuilder();
 		buf.append("(");
 		buf.append(format.format(p[0]));
 		buf.append(", ");
 		buf.append(format.format(p[1]));
 		buf.append(")");
 		return buf.toString();
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, 
 			                   Rectangle parentBounds) {
 		
 		updateConnectionBounds();
 		createROI(true);
 		fireROIChanged(getROI());
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-free.png";
 	}
 
 	@Override
 	protected ROIBase createROI(boolean recordResult) {
 		if (points == null) return getROI();
 		
		final Rectangle rect = getBounds();
		final double[] a1 = new double[]{getXAxis().getPositionValue(rect.x, false), getYAxis().getPositionValue(rect.y, false)};
		double[] a2 = new double[]{getXAxis().getPositionValue(rect.x+rect.width, false), getYAxis().getPositionValue(rect.y+rect.height, false)};

		final PolygonalROI proi = new PolygonalROI(a1);
 		
 		for (int i = 0; i < points.size(); i++) {
 			final Point pnt = points.getPoint(i);
			double[] pd = new double[]{getXAxis().getPositionValue(pnt.x, false), getYAxis().getPositionValue(pnt.y, false)};
			proi.insertPoint(pd);
 		}
 		
 		if (recordResult)
 			roi = proi;
 		
 		return proi;
 	}
 
 	@Override
 	protected void updateROI(ROIBase bounds) {
 		if (bounds instanceof PolygonalROI) {
 			final PolygonalROI proi = (PolygonalROI) bounds;
 			if (points==null) points = new PointList();
 	        points.removeAllPoints();
 	        
 	        for (ROIBase p : proi) {
 				
 	           	final int x = getXAxis().getValuePosition(p.getPointX(), false);
 	           	final int y = getYAxis().getValuePosition(p.getPointY(), false);
 	           	points.addPoint(new Point(x,y));
 			}
 	        updateConnectionBounds();
 		}
 
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 1;
 	}
 }
