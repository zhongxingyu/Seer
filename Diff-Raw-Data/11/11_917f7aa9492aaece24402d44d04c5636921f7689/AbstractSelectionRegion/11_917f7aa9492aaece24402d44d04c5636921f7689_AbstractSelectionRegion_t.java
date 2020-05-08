 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import java.util.List;
 
 import org.csstudio.swt.xygraph.figures.Grid;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.dawb.common.ui.plot.axis.CoordinateSystemEvent;
 import org.dawb.common.ui.plot.axis.ICoordinateSystem;
 import org.dawb.common.ui.plot.axis.ICoordinateSystemListener;
 import org.dawb.common.ui.plot.region.AbstractRegion;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawnsci.plotting.draw2d.Activator;
 import org.dawnsci.plotting.draw2d.swtxy.IMobileFigure;
 import org.dawnsci.plotting.draw2d.swtxy.ImageTrace;
 import org.dawnsci.plotting.draw2d.swtxy.RegionBean;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * An AbstractSelectionRegion has two purposes:
  * 1. To draw the 2d shapes for selection in the diagram.
  * 2. To return the selection for the region in the real world coordinates.
  * 
  * Shape used for ROIs which has bounds fixed to the graph area.
  * Links regions to the specifics of the XY Plotting system.
  * 
  * NOTE you may implement this class two ways:
  * WAY1. by creating contents in the createContents(...) method and adding them to the parent. 
  *       This is the recommended and default method.
  * WAY2. by adding contents to this figure.
  * 
  * If doing method 2. remember to add this figure to the parent. If doing this the children can be
  * drawn in the local coordinates for the figure. However note in that case when firing selection
  * events in the axis coordinates parent figure location will also need to be used. If using 1.,
  * the contents of the figure are directly added to the graph figure and therefore their location
  * can be used directly also there are no bounds of this figure to deal with.
  */
 public abstract class AbstractSelectionRegion extends AbstractRegion implements ICoordinateSystemListener {
 
 	private RegionBean bean;
     private ISelectionProvider selectionProvider;
     private IFigure[] regionObjects;
     private int lineWidth=0;
 
     protected ICoordinateSystem coords;
 
 	public AbstractSelectionRegion(String name, ICoordinateSystem co) {
 		super();
 		setEnabled(false); // No mouse events.
 		setOpaque(false);
 		setCursor(null);
 		this.bean = new RegionBean();
 		setName(name);
 		co.addCoordinateSystemListener(this);
 		bean.setCoordinateSystem(co);
 		this.coords = co;
 	}
 
 	/**
 	 * Creates the contents of the selection, i.e. the figure(s) which make up the selection. You
 	 * may add the children directly to the parent here. Otherwise add children to this figure, set
 	 * its bounds and add this figure to the parent. Note this is called after the user interaction
 	 * to create the parameters for figure(s).
 	 * 
 	 * @param parent
 	 */
 	public abstract void createContents(final Figure parent);
 
 	/**
 	 * Return the type of region which this provides UI for.
 	 * @return
 	 */
 	public abstract RegionType getRegionType();
 
 	/**
 	 * If there is a fill figure, this method may be called to 
 	 * refill the fill - typically, this occurs after a translation event
 	 */
 	protected abstract void updateConnectionBounds();
 
 	/**
 	 * Paint the regions before it is finished during the clicks and drag of the user.
 	 * 
 	 * @param g
 	 * @param clicks
 	 * @param parentBounds
 	 */
 	public abstract void paintBeforeAdded(final Graphics g, PointList clicks, Rectangle parentBounds);
 
 	/**
 	 * Sets the local of the region in local coordinates not axis ones.
 	 * @param clicks
 	 * @param parentBounds
 	 */
 	public abstract void setLocalBounds(PointList clicks, Rectangle parentBounds);
 
 	/**
 	 * This method should be implemented to fire a StructuredSelection
 	 * whose first object is an object extending ROIBase. It will be called
 	 * when the user has finished clicking and dragging a selection.
 	 * 
 	 * To implement live updates the user should add an IRegionBoundsListener
 	 * which will be notified on drag.
 	 * 
 	 */
 	protected void fireROISelection() {
 		if (getSelectionProvider() != null && roi != null)
 			getSelectionProvider().setSelection(new StructuredSelection(roi));
 	}
 
 
 	/**
 	 * This cursor is used after the region is created and
 	 * before the user has clicked to create it. This is the
 	 * path to the cursor, for instance "icons/Cursor-box.png"
 	 * @return
 	 */
 	protected abstract String getCursorPath();
 
 	/**
 	 * A selection region can operate with any number of mouse button presses
 	 * @return maximum number of presses, use 0 for "unlimited" presses
 	 */
 	public abstract int getMaximumMousePresses();
 
 	/**
 	 * A selection region can operate with any number of mouse button presses. Override this if
 	 * minimum needs to be different from maximum
 	 * @return minimum number of presses
 	 */
 	public int getMinimumMousePresses() {
 		return getMaximumMousePresses();
 	}
 
 	public void sync(RegionBean bean) {
 		setName(bean.getName());
 		setShowPosition(bean.isShowPosition());
 		setXyGraph(bean.getXyGraph());
 		setRegionColor(bean.getRegionColor());
 		setAlpha(bean.getAlpha());
 		setVisible(bean.isVisible());
 		setMobile(bean.isMobile());
 		setShowLabel(bean.isShowLabel());
 	}
 	
 	private Cursor cursor;
 	/**
 	 * A new cursor is created on each call.
 	 */
 	public Cursor getRegionCursor() {
 		if (cursor==null && getCursorPath()!=null)  {
 			Image image = Activator.getImage(getCursorPath());
 			cursor = new Cursor(Display.getDefault(), image.getImageData(), 8, 8);
 		}
 		return cursor;
 	}
 
 	protected void drawLabel(Graphics gc, Rectangle size) {
		if (isShowLabel() && getLabel()!=null) {
 			gc.setAlpha(255);
 			gc.setForegroundColor(ColorConstants.black);
			gc.drawText(getLabel(), size.getCenter());
 		}
 	}
 
 	@Override
 	public void coordinatesChanged(CoordinateSystemEvent evt) {
 		updateROI();
 	}
 
 	protected void setRegionObjects(IFigure... objects) {
 		this.regionObjects = objects;
 		createSelectionListener(regionObjects);
 	}
 	
 	protected void setRegionObjects(IFigure first, List<IFigure> objects) {
 		regionObjects = new IFigure[objects.size() + 1];
 		int i = 0;
 		regionObjects[i++] = first;
 		for (IFigure f : objects) {
 			regionObjects[i++] = f;
 		}
 		createSelectionListener(regionObjects);
 	}
 
 	private MouseListener selectionListener;
 	private void createSelectionListener(IFigure... ro) {
 		if (selectionListener==null) selectionListener = new MouseListener.Stub() {			
 			@Override
 			public void mousePressed(MouseEvent me) {
 				fireROISelected(getROI()); 
 			}
 		};
 		for (IFigure iFigure : ro) {
 			try {
 				iFigure.addMouseListener(selectionListener);
 			} catch (Throwable ne) {
 				continue;// Probably will not happen
 			}
 		}
 	}
 
 	public String getName() {
 		return bean.getName();
 	}
 	
 	/**
 	 * Remove from graph and remove all RegionBoundsListeners.
 	 * 
 	 */
 	public final void remove() {
 		clearListeners();
 		if (coords!=null) coords.dispose();
 		if (getParent()!=null) getParent().remove(this);
 		if (regionObjects!=null)for (IFigure ob : regionObjects) {
 			if (ob.getParent()!=null) {
 				ob.getParent().remove(ob);
 				ob.removeMouseListener(selectionListener);
 			}
 		}
 		if (cursor!=null) cursor.dispose();
 		cursor = null;
 		//dispose();
 	}
 
 	protected void clearListeners() {
 		try {
             coords.removeCoordinateSystemListener(this);
 		} catch (Exception ignored) {
 			// Do nothing
 		}
  		super.clearListeners();
 	}
 	
 	public void setName(String name) {
 		bean.setName(name);
 		if (label==null)
 			setLabel(name);
 	}
 
 
 	public XYGraph getXyGraph() {
 		return bean.getXyGraph();
 	}
 
 
 	public void setXyGraph(XYGraph xyGraph) {
 		bean.setXyGraph(xyGraph);
 	}
 
 	public Color getRegionColor() {
 		return bean.getRegionColor();
 	}
 
 
 	public void setRegionColor(Color regionColor) {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob!=null) {
 				ob.setForegroundColor(regionColor);
 				ob.setBackgroundColor(regionColor);
 			}
 		}
 		bean.setRegionColor(regionColor);
 	}
 
 
 	public boolean isShowPosition() {
 		return bean.isShowPosition();
 	}
 
 
 	public void setShowPosition(boolean showPosition) {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob instanceof SelectionHandle) {
 				((SelectionHandle)ob).setShowPosition(showPosition);
 			}
 		}
 		bean.setShowPosition(showPosition);
 	}
 
 	public void setAlpha(int alpha) {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob instanceof SelectionHandle) {
 				((SelectionHandle)ob).setAlpha(alpha);
 			} else if (ob instanceof Shape) {
 				((Shape)ob).setAlpha(alpha);
 			}
 		}
 		bean.setAlpha(alpha);
 	}
 	public int getAlpha() {
 		return bean.getAlpha();
 	}
 
 	public RegionBean getBean() {
 		return bean;
 	}
 
 	public ISelectionProvider getSelectionProvider() {
 		return selectionProvider;
 	}
 
 	public void setSelectionProvider(ISelectionProvider selectionProvider) {
 		this.selectionProvider = selectionProvider;
 	}
 
 	public boolean isVisible() {
 		return bean.isVisible();
 	}
 
 	public void setVisible(boolean visible) {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob instanceof IMobileFigure) {
 				((IMobileFigure)ob).setVisible(visible&&(isMobile()||isTrackMouse()));
 			} else {
 			    if (ob!=null) ob.setVisible(visible);
 			}
 		}
 		bean.setVisible(visible);
 	}
 
 	public boolean isMobile() {
 		return bean.isMobile();
 	}
 
 	@Override
 	public void setMobile(boolean mobile) {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob instanceof IMobileFigure) {
 				((IMobileFigure)ob).setVisible(mobile);
 			} else if (ob instanceof RegionFillFigure) {
 				((RegionFillFigure)ob).setMobile(mobile);
 			}
 		}
 		bean.setMobile(mobile);
 	}
 
 
 	public void setHandlesVisible(boolean mobile) {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob instanceof IMobileFigure) {
 				((IMobileFigure)ob).setVisible(mobile);
 			}
 		}
 	}
 
 	public void repaint() {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob!=null) ob.repaint();
 		}
 	}
 
 	public ICoordinateSystem getCoordinateSystem() {
 		return coords;
 	}
 
 	public void setCoordinateSystem(ICoordinateSystem co) {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			if (ob instanceof SelectionHandle) {
 				((SelectionHandle)ob).setCoordinateSystem(co);
 			}
 		}
 		this.coords.removeCoordinateSystemListener(this);
 		co.addCoordinateSystemListener(this);
 		this.coords = co;
 		bean.setCoordinateSystem(co);
 	}
 
 	public TranslationListener createRegionNotifier() {
 		return new TranslationListener() {
 			@Override
 			public void translateBefore(TranslationEvent evt) {
 			}
 
 			@Override
 			public void translationAfter(TranslationEvent evt) {
 				updateConnectionBounds();
 				fireROIDragged(createROI(false), ROIEvent.DRAG_TYPE.RESIZE);
 			}
 
 			@Override
 			public void translationCompleted(TranslationEvent evt) {
 				fireROIChanged(createROI(true));
 				fireROISelection();
 			}
 
 			@Override
 			public void onActivate(TranslationEvent evt) {
 			}
 		};
 	}
 
 	public boolean isShowLabel() {
 		return bean.isShowLabel();
 	}
 
 	public void setShowLabel(boolean showLabel) {
 		bean.setShowLabel(showLabel);
 	}
 
 	public int getLineWidth() {
 		return lineWidth;
 	}
 
 	public void setLineWidth(int lineWidth) {
 		this.lineWidth = lineWidth;
 	}
 	
 	/**
 	 * This will work for WAY1 only. If using WAY2 you will need to override.
 	 * See comment at top of this class.
 	 */
 	@Override
 	public void toBack() {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			final IFigure par = ob.getParent();
 			if (par!=null) {
 				par.remove(ob);
 				final int index = getLowestPositionAboveTraces(par);
 				par.add(ob, index);
 			}
 		}		
 	}
 
 	/**
 	 * This will work for WAY1 only. If using WAY2 you will need to override.
 	 * @param par
 	 * @return
 	 */
 	protected int getLowestPositionAboveTraces(IFigure par) {
 	    int index = 0;
 		for (Object ob : par.getChildren()) {
 			// Do not send regions below traces.
 			if (ob instanceof Trace || ob instanceof ImageTrace || ob instanceof Grid) {
 				index++;
 				continue;
 			}
 			break;
 		}
 		return index;
 	}
 
 	/**
 	 * This will work for WAY1 only. If using WAY2 you will need to override.
 	 * See comment at top of this class.
 	 */
 	@Override
 	public void toFront() {
 		if (regionObjects!=null) for (IFigure ob : regionObjects) {
 			final IFigure par = ob.getParent();
 			if (par!=null) {
 				par.remove(ob);
 				final int end = par.getChildren()!=null 
 						      ? par.getChildren().size()
 						      : 0;
 				par.add(ob, end);
 			}
 		}		
 		
 	}
 	
 	/**
 	 * It is a good idea to override this, the default simply checks that the real world
 	 * value is in the selections bounds.
 	 */
 	@Override
 	public boolean containsPoint(double x, double y) {
 		
 		final int[] pix = coords.getValuePosition(new double[]{x,y});
 		
 		if (regionObjects!=null) {
 			for (IFigure ob : regionObjects) {
 				if (ob.containsPoint(pix[0], pix[1])) return true;
 		    }
 			return false;
 
 		} else {
 			return containsPoint(pix[0], pix[1]);
 		}
 		
 	}
 
 
 	protected RectangularROI getRoiFromRectangle(final Rectangle rect) {
 
 		double[] a1 = coords.getPositionValue(rect.x, rect.y);
 		double[] a2 = coords.getPositionValue(rect.x+rect.width, rect.y+rect.height);
 		if (coords.isXReversed()) reverse(a1,a2,0);
 		if (coords.isYReversed()) reverse(a1,a2,1);
 
 		double x = a1[0]; double y = a1[1];
 		double w = a2[0] - a1[0]; double h = a2[1] - a1[1];
 
 		if (w<0) {
 			w = Math.abs(w);
 			x-= w;
 		}
 		if (h<0) {
 			h = Math.abs(h);
 			y-= h;
 		}
 
 		return createROI(x, y, w, h, 0);
 	}
 	
 	/**
 	 * Used from getRoiFromRectangle to get a ROI from the rectangle.
 	 * 
 	 * @param ptx
 	 * @param pty
 	 * @param width
 	 * @param height
 	 * @param angle
 	 * @return
 	 */
 	protected RectangularROI createROI(double ptx, double pty, double width, double height, double angle) {
 		return new RectangularROI(ptx, pty, width, height, angle);
 	}
 	
 	protected void reverse(double[] a1, double[] a2, int i) {
 		double tmp = a1[i];
 		a1[i] = a2[i];
 		a2[i] = tmp;
 	}
 
 }
