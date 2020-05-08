 package org.dawb.workbench.plotting.system.swtxy.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
 import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
 import org.dawb.workbench.plotting.system.swtxy.util.RotatablePolylineShape;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class PolylineSelection extends AbstractSelectionRegion {
 
 	DecoratedPolyline pline;
 
 	public PolylineSelection(String name, Axis xAxis, Axis yAxis) {
 		super(name, xAxis, yAxis);
 		setRegionColor(ColorConstants.cyan);
 		setAlpha(80);
 		setLineWidth(2);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		pline = new DecoratedPolyline(parent);
 		pline.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(pline);
 		sync(getBean());
 		updateROI();
 		if (roi == null)
 			createROI(true);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.POLYLINE;
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		if (pline != null) {
 			Rectangle b = pline.getUpdatedBounds();
 			pline.setBounds(b);
 		}
 	}
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		g.setLineStyle(SWT.LINE_DOT);
 		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.drawPolyline(clicks);
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		if (pline != null) {
 			pline.setPoints(clicks);
 			setRegionColor(getRegionColor());
 			setOpaque(false);
 			setAlpha(getAlpha());
 			updateConnectionBounds();
 			createROI(true);
 			fireROIChanged(getROI());
 		}
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-polyline.png";
 	}
 
 	@Override
 	protected ROIBase createROI(boolean recordResult) {
 		final Axis xa = getXAxis();
 		final Axis ya = getYAxis();
 		final PointList pl = pline.getPoints();
 		final PolygonalROI proi = new PolygonalROI();
 		for (int i = 0, imax = pl.size(); i < imax; i++) {
 			Point p = pl.getPoint(i);
 			proi.insertPoint(i, xa.getPositionValue(p.x(), false), ya.getPositionValue(p.y(), false));
 		}
 		if (recordResult)
 			roi = proi;
 
 		return proi;
 	}
 
 	@Override
 	protected void updateROI(ROIBase bounds) {
 		if (bounds instanceof PolygonalROI) {
 			if (pline == null)
 				return;
 
 			pline.updateROI((PolygonalROI) bounds);
 
 			updateConnectionBounds();
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 0; // signifies unlimited presses
 	}
 
 	class DecoratedPolyline extends RotatablePolylineShape {
 		List<IFigure> handles;
 		private Figure parent;
 		private static final int SIDE = 8;
 
 		public DecoratedPolyline(Figure parent) {
 			super();
 			handles = new ArrayList<IFigure>();
 			this.parent = parent;
 		}
 
 		@Override
 		public void setPoints(PointList points) {
 			super.setPoints(points);
 			FigureListener listener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					parent.repaint();
 				}
 			};
 
 			FigureTranslator mover;
 			for (int i = 0, imax = points.size(); i < imax; i++) {
 				Point p = points.getPoint(i);
 				RectangularHandle h = new RectangularHandle(getXAxis(), getYAxis(), getRegionColor(), this, SIDE, p.preciseX(), p.preciseY());
 				parent.add(h);
 				mover = new FigureTranslator(getXyGraph(), h);
 				mover.addTranslationListener(createRegionNotifier());
 				h.addFigureListener(listener);
 				handles.add(h);
 			}
 
 			addFigureListener(listener);
 			mover = new FigureTranslator(getXyGraph(), parent, this, handles);
 			mover.addTranslationListener(createRegionNotifier());
 			setRegionObjects(this, handles);
 		}
 
 		private Rectangle getUpdatedBounds() {
 			int i = 0;
 			Rectangle b = null;
 			for (IFigure f : handles) { // this is called first so update points
 				if (f instanceof SelectionHandle) {
 					SelectionHandle h = (SelectionHandle) f;
 					setPoint(h.getSelectionPoint(), i++);
 					if (b == null) {
 						b = new Rectangle(h.getBounds());
 					} else {
 						b.union(h.getBounds());
 					}
 				}
 			}
 			return b;
 		}
 
 		public void updateROI(PolygonalROI proi) {
 			final PointList pl = getPoints();
 			final int imax = handles.size();
 			if (imax != proi.getSides())
 				return;
 
 			final Axis xa = getXAxis();
 			final Axis ya = getYAxis();
 			for (int i = 0; i < imax; i++) {
 				PointROI p = proi.getPoint(i);
 				Point np = new Point(xa.getValuePosition(p.getPointX(), false), ya.getValuePosition(p.getPointY(), false));
 				pl.setPoint(np, i);
 				SelectionHandle h = (SelectionHandle) handles.get(i);
 				h.setSelectionPoint(np);
 			}
 		}
 
 		@Override
 		protected void fillShape(Graphics graphics) {
 			super.fillShape(graphics);
 		}
 
 		@Override
 		protected void outlineShape(Graphics graphics) {
 			super.outlineShape(graphics);
 			for (IFigure f : handles) {
 				f.paint(graphics);
 			}
 		}
 	}
 }
