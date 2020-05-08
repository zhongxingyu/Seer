 package org.dawb.workbench.plotting.system.swtxy.selection;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.dawb.common.ui.plot.region.RegionBounds;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 
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
 			                     Point firstClick,
 			                     Point dragLocation, 
 			                     Rectangle parentBounds) {
 		
 		if (points==null) {
 			points = new PointList();
 			points.addPoint(firstClick);
 		}
 		points.addPoint(dragLocation);
 		
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
 		
 		double[] loc = new double[]{getxAxis().getPositionValue(pnt.x, false), getyAxis().getPositionValue(pnt.y, false)};
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
 	public void setLocalBounds(Point firstClick, 
 			                   Point dragLocation,
 			                   Rectangle parentBounds) {
 		
 		updateConnectionBounds();
 		createRegionBounds(true);
 		fireRegionBoundsChanged(getRegionBounds());
 	}
 
 	@Override
 	protected void fireRoiSelection() {
 // TODO FIXME Is there a useful ROI one can fire here?
 
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-free.png";
 	}
 
 	@Override
 	protected RegionBounds createRegionBounds(boolean recordResult) {
 		if (points == null) return getRegionBounds();
 		
 		final Rectangle rect = getBounds();
 		double[] a1 = new double[]{getxAxis().getPositionValue(rect.x, false), getyAxis().getPositionValue(rect.y, false)};
 		double[] a2 = new double[]{getxAxis().getPositionValue(rect.x+rect.width, false), getyAxis().getPositionValue(rect.y+rect.height, false)};
 		
 		final RegionBounds bounds = new RegionBounds(a1, a2);
 		
 		for (int i = 0; i < points.size(); i++) {
 			final Point pnt = points.getPoint(i);
 			double[] pd = new double[]{getxAxis().getPositionValue(pnt.x, false), getyAxis().getPositionValue(pnt.y, false)};
 			bounds.addPoint(pd);
 		}
 		
 		if (recordResult) this.regionBounds = bounds;
 		
 		return bounds;
 	}
 
 	@Override
 	protected void updateRegionBounds(RegionBounds bounds) {
 
 		if (!bounds.isPoints()) throw new RuntimeException("Expected points bounds for free draw!");
 
 		if (points==null) points = new PointList();
         points.removeAllPoints();
         
         for (double[] pnt : bounds.getPoints()) {
 			
            	final int x = getxAxis().getValuePosition(pnt[0], false);
            	final int y = getyAxis().getValuePosition(pnt[1], false);
            	points.addPoint(new Point(x,y));
 		}
         
         updateConnectionBounds();
 	}
 
 }
