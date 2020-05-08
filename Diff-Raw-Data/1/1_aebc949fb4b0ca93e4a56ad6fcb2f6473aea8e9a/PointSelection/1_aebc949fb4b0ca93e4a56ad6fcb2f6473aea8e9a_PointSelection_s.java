 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegionContainer;
 import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
 import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
 import org.dawnsci.plotting.draw2d.swtxy.util.RotatablePolygonShape;
 import org.dawnsci.plotting.draw2d.swtxy.util.RotatableRectangle;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.graphics.Color;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 
 public class PointSelection extends AbstractSelectionRegion {
 
 	private final class RegionContainerRectangularHandle extends RectangularHandle  {
 
 		public RegionContainerRectangularHandle(ICoordinateSystem coords,
 												Color regionColor, 
 												Figure parent, 
 												int lineWidth, 
 												double d,
 												double e) {
 			
 			super(coords, regionColor, parent, lineWidth, d, e);
 			setPreciseLocation(true);
 		}
 
 		@Override
 		public Shape createHandleShape(Figure parent, int side, double[] params) {
 			double angle;
 			if (parent instanceof RotatablePolygonShape) {
 				RotatablePolygonShape pg = (RotatablePolygonShape) parent;
 				angle = pg.getAngleDegrees();
 			} else {
 				angle = 0;
 			}
 			location = new PrecisionPoint(params[0], params[1]);
 			return new RegionContainerRotatableRectangle(location.x(), location.y(), side, side, angle);
 		}
 
 	}
 	
 	private class RegionContainerRotatableRectangle extends RotatableRectangle implements IRegionContainer {
 
 		public RegionContainerRotatableRectangle(int x, int y, int width,
 				int height, double angle) {
 			super(x, y, width, height, angle);
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return PointSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 			// Cannot change this selection.
 		}
 		
 	}
 
 	private SelectionHandle  point;
 	private FigureTranslator mover;
 	
 	public PointSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(RegionType.POINT.getDefaultColor());
 		setLineWidth(7);
 		setAlpha(120);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.POINT;
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		
 	}
 	@Override
 	public boolean containsPoint(double x, double y) {
 		
 		final int[] pix = coords.getValuePosition(x,y);
 		final Point pnt = point.getSelectionPoint();
 		return pnt.x == pix[0] && pnt.y == pix[1];
 	}
 	
 	@Override
 	public void paintBeforeAdded(Graphics g, 
 			                     PointList clicks,
 			                     Rectangle parentBounds) {
 		
 		if (clicks.size()<1) return;
 		final Point pnt    = clicks.getLastPoint();
 		final int   offset = getLineWidth()/2; // int maths ok here
         g.setForegroundColor(getRegionColor());
         g.fillRectangle(pnt.x-offset, pnt.y-offset, getLineWidth(), getLineWidth());
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		this.point = new RegionContainerRectangularHandle(coords, getRegionColor(), parent, getLineWidth(), 100d, 100d);
 		parent.add(point);
 		mover = new FigureTranslator(getXyGraph(), point);	
 		mover.addTranslationListener(createRegionNotifier());
 		setMobile(isMobile());
 		point.setShowPosition(false);
 		setRegionObjects(point);
 	}
 	
 	@Override
 	public void setMobile(final boolean mobile) {
 		getBean().setMobile(mobile);
 		if (mover!=null && point!=null) {
 			mover.setActive(mobile);
 			if (mobile) point.setCursor(Draw2DUtils.getRoiControlPointCursor()) ;
 			else 	    point.setCursor(null) ; 
 		}
 	}
 	
 	@Override
 	public void setVisible(boolean visible) {
 		if (point!=null) point.setVisible(visible);
 		getBean().setVisible(visible);
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		if (clicks.size()<1) return;
 		final Point last = clicks.getLastPoint();
 		point.setSelectionPoint(last);
 		roi = new PointROI(point.getPosition());
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-point.png";
 	}
 
 	@Override
 	protected IROI createROI(boolean recordResult) {
 		if (point == null) return getROI();
 		final PointROI proi = new PointROI(point.getPosition());
 		if (roi!=null) {
 			proi.setPlot(roi.isPlot());
 			// set the Region isActive flag
 			this.setActive(roi.isPlot());
 		}
 		if (recordResult) roi = proi;
 
 		return proi;
 	}
 
 	@Override
 	protected void updateROI(IROI roi) {
 		if (roi instanceof PointROI) {
 			if (point==null) return;
 
 	        point.setPosition(roi.getPointRef());
 	        updateConnectionBounds();
 		}
     }
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 1;
 	}
 }
