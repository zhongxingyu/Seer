 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import java.util.Arrays;
 
 import org.dawb.common.ui.plot.axis.ICoordinateSystem;
 import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
 import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator.LockType;
 import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.UpdateManager;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Display;
 
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 
 /**
 
                       ,,ggddY""""Ybbgg,,
                  ,agd""'    |         `""bg,
               ,gdP"         | 3           "Ybg,
             ,dP"            |                "Yb,
           ,dP"         _,,dd|"""Ybb,,_         "Yb,
          ,8"         ,dP"'  | |    `"Yb,         "8,
         ,8'        ,d"      | |2       "b,        `8,
        ,8'        d"        | |          "b        `8,
        d'        d'        ,gPPRg,        `b        `b
        8         8        dP'   `Yb        8         8
        8         8        8)  1  (8        8         8
        8         8        Yb     dP        8         8
        8         Y,        "8ggg8"        ,P         8
        Y,         Ya                     aP         ,P
        `8,         "Ya                 aP"         ,8'
         `8,          "Yb,_         _,dP"          ,8'
          `8a           `""YbbgggddP""'           a8'
           `Yba                                 adP'
             "Yba                             adY"
               `"Yba,                     ,adP"'
                  `"Y8ba,             ,ad8P"'
                       ``""YYbaaadPP""''
  *         
  *     
  *    1. Center 
  *    2. Inner Radius
  *    3. Outer Radius
  *    
  *    Currently the inner radius and outer radius can only be modified programmatically.
  *     
  * @author fcp94556
  */
 class RingSelection extends AbstractSelectionRegion {
 		
 	private static final int SIDE      = 8;
 	
 	private SelectionHandle center;
 
 	private Figure             connection;
 	private SelectionHandle innerControl, outerControl;
 	
 	public RingSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(ColorConstants.yellow);	
 		setAlpha(80);
 		labelColour = ColorConstants.black; 
 		labelFont = new Font(Display.getCurrent(), "Dialog", 10, SWT.BOLD);
 	}
 
 	@Override
 	public void createContents(final Figure parent) {
 		
 		this.center = new RectangularHandle(coords, getRegionColor(), connection, SIDE, 100, 100);
 		center.setCursor(null);
 
 		this.innerControl = createSelectionHandle();
 		this.outerControl = createSelectionHandle();
 
 		this.connection = new RegionFillFigure(this) {
 
 			@Override
 			public void paintFigure(Graphics gc) {
 				//super.paintFigure(gc);
 
 				// We get a bound half the size of the 
 				// two rectangles and then draw a ring
 				gc.setAlpha(getAlpha());
 				
 				final Point    cen = center.getSelectionPoint();
 				final int outerRad = outerControl.getSelectionPoint().y-cen.y;
 				final int innerRad = innerControl.getSelectionPoint().y-cen.y;
 
 				gc.setLineWidth(outerRad-innerRad);
 				gc.setForegroundColor(getRegionColor());
 				
 				final Rectangle out = new Rectangle(new Point(cen.x-outerRad, cen.y-outerRad), new Point(cen.x+outerRad, cen.y+outerRad));
 				final Rectangle in  = new Rectangle(new Point(cen.x-innerRad, cen.y-innerRad), new Point(cen.x+innerRad, cen.y+innerRad));
 				final Point     tl  = (new Rectangle(out.getTopLeft(),     in.getTopLeft())).getCenter();
 				final Point     br  = (new Rectangle(out.getBottomRight(), in.getBottomRight())).getCenter();
 				final Rectangle mid = new Rectangle(tl, br);
 				gc.drawOval(mid); 
 				if (label != null && isShowLabel()) {
 					gc.setAlpha(255);
 					gc.setForegroundColor(labelColour);
 					gc.setFont(labelFont);
 					gc.drawText(label, new Point(cen.x + outerRad - innerRad, cen.y-outerRad));
 				}
 			}
 
 			@Override
 			public boolean containsPoint(int x, int y) {
 				if (!super.containsPoint(x, y)) return false;
 
 				final Point    cen = center.getSelectionPoint();
 				final int outerRad = outerControl.getSelectionPoint().y-cen.y;
 				final int innerRad = innerControl.getSelectionPoint().y-cen.y;
 				double rad = cen.getDistance(Point.SINGLETON.setLocation(x, y)); 
 				return rad <= outerRad && rad > innerRad;
 			}
 		};
 
 		
 		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
 		connection.setBackgroundColor(getRegionColor());
 		connection.setBounds(new Rectangle(0,0,100,100));
 		connection.setOpaque(false);
 
 		parent.add(connection);
 		parent.add(center);
 		parent.add(innerControl);
 		parent.add(outerControl);
 		
 		FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{center, connection, innerControl, outerControl}));
 		// Add a translation listener to be notified when the mover will translate so that
 		// we do not recompute point locations during the move.
 		mover.addTranslationListener(createRegionNotifier());
 		
 		setRegionObjects(connection, center, innerControl, outerControl);
 		sync(getBean());
 		updateROI();
 		if (roi == null)
 			createROI(true);
 
 		outerControl.setForegroundColor(ColorConstants.blue);
 		innerControl.setForegroundColor(ColorConstants.red);
 	}
 	
 	@Override
 	public boolean containsPoint(double x, double y) {
 		
 		final int[] pix = coords.getValuePosition(x,y);
 		return connection.containsPoint(pix[0], pix[1]);
 	}
 	
 	private RectangularHandle createSelectionHandle() {
 		
 		RectangularHandle ret = new RectangularHandle(coords, getRegionColor(), connection, SIDE, 100, 100);
 		FigureTranslator trans = new FigureTranslator(getXyGraph(), ret);
 		trans.setLockedDirection(LockType.Y);
 		trans.addTranslationListener(createRegionNotifier());
 
 		return ret;
 	}
 
 	@Override
 	public void paintBeforeAdded(final Graphics gc, PointList clicks, Rectangle parentBounds) {
 		gc.setLineStyle(Graphics.LINE_DOT);
 
 		Point cen = clicks.getFirstPoint();
 		int diff = (int)Math.round(cen.getDistance(clicks.getLastPoint()));
 		Rectangle bounds = new Rectangle(new Point(cen.x-diff, cen.y-diff), new Point(cen.x+diff, cen.y+diff));
 		gc.drawOval(bounds);
 
 		gc.setLineWidth(diff/4);
 		diff = Math.round(0.875f*diff);
 		bounds = new Rectangle(new Point(cen.x-diff, cen.y-diff), new Point(cen.x+diff, cen.y+diff));
 		gc.setLineStyle(Graphics.LINE_SOLID);
 		gc.setForegroundColor(getRegionColor());
 		gc.setAlpha(getAlpha());
 		gc.drawOval(bounds);
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-circle.png";
 	}
 
 	@Override
 	public ROIBase createROI(boolean recordResult) {
 		if (center!=null) {
 			final Point     cen = center.getSelectionPoint();
 			final int outerRad = outerControl.getSelectionPoint().y-cen.y;
 			final int innerRad = innerControl.getSelectionPoint().y-cen.y;
 
 			final Rectangle out = new Rectangle(new Point(cen.x+outerRad, cen.y+outerRad), new Point(cen.x-outerRad, cen.y-outerRad));
 			final Rectangle in  = new Rectangle(new Point(cen.x+innerRad, cen.y+innerRad), new Point(cen.x-innerRad, cen.y-innerRad));
 			
 			double[] rcen = coords.getPositionValue(cen.x,cen.y);
			double cenY   = rcen[1];
 			double inRad  = coords.getPositionValue(0,in.getTop().y)[1]-cenY;
 			double outRad = coords.getPositionValue(0,out.getTop().y)[1]-cenY;
 			if (inRad < 0)
 				inRad = -inRad;
 			if (outRad < 0)
 				outRad = -outRad;
 
 			final SectorROI sroi = new SectorROI(inRad, outRad);
 			sroi.setPoint(rcen);
 
 			if (recordResult)
 				roi = sroi;
 			return sroi;
 		}
 		return super.getROI();
 	}
 
 	@Override
 	protected void updateROI(ROIBase roi) {
 		if (roi instanceof SectorROI) {
 			SectorROI sroi = (SectorROI) roi;
 			if (center!=null) {
 				center.setPosition(sroi.getPointRef());
 				double y = sroi.getPointY();
 				int[] cen = coords.getValuePosition(sroi.getPoint());
 				
 				int innerRad = coords.getValuePosition(0,y+sroi.getRadius(0))[1]-cen[1];
 				int outerRad = coords.getValuePosition(0,y+sroi.getRadius(1))[1]-cen[1];
 				setControlPositions(innerRad, outerRad);
 				updateConnectionBounds();
 			}
 		}
 	}
 
 	private void setControlPositions(int innerRad, int outerRad) {
 		final Point cen = center.getSelectionPoint();
 		innerControl.setSelectionPoint(new Point(cen.x, cen.y+innerRad));
 		outerControl.setSelectionPoint(new Point(cen.x, cen.y+outerRad));
 	}
 
 	/**
 	 * Sets the local in local coordinates
 	 * @param bounds
 	 */
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		Point cen = clicks.getFirstPoint();
 
 		int diff = (int)Math.round(cen.getDistance(clicks.getLastPoint()));
 		if (center!=null)   {
 			center.setSelectionPoint(cen);
 			
 			int outerRad = diff;
 			int innerRad = Math.round(0.75f * diff);
 			setControlPositions(innerRad, outerRad);
 		}
 		
 		updateConnectionBounds();
 		createROI(true);
 		fireROIChanged(getROI());
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		if (connection==null) return;
 		final Point     cen = center.getSelectionPoint();
 		final int outerRad  = outerControl.getSelectionPoint().y;
 		final int innerRad  = innerControl.getSelectionPoint().y;
 		
 		// this maths looks wrong but it passes the unit test and human usage:
 		int diff      = Math.round(1.1f*((Math.max(outerRad, innerRad)-cen.y)));
 		Rectangle out = new Rectangle(new Point(cen.x-diff, cen.y-diff), new Point(cen.x+diff, cen.y+diff));
 		diff          = Math.round(1.1f*((Math.min(outerRad, innerRad)-cen.y)));
 		out.union(new Rectangle(new Point(cen.x-diff, cen.y-diff), new Point(cen.x+diff, cen.y+diff)));
 
 		UpdateManager updateMgr = null;
 		try {
 			updateMgr = connection.getParent()!=null
 	                  ? connection.getParent().getUpdateManager()
 	                  : connection.getUpdateManager();
 		} catch (Throwable ignored) {
 			// We intentionally allow the code to continue without the UpdateManager
 		}
 
 		connection.setBounds(out);
 		if (updateMgr!=null) updateMgr.addDirtyRegion(connection, out);
 	}
 	
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.RING;
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 2;
 	}
 
 	@Override
 	public void setRegionColor(Color regionColor) {
 		super.setRegionColor(regionColor);
 		labelColour = regionColor;
 	}
 }
