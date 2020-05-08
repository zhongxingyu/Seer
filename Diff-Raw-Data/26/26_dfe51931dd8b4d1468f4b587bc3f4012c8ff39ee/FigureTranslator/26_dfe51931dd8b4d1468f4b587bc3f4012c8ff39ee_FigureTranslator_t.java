 package org.dawnsci.plotting.draw2d.swtxy.translate;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.dawnsci.plotting.draw2d.swtxy.selection.SelectionHandle;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.LayoutManager;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.draw2d.UpdateManager;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 
 /**
  * Class from SWT examples which allows a draw2D figure to move.
  * @author fcp94556
  *
  */
 public class FigureTranslator implements MouseListener, MouseMotionListener {
 
 	public enum LockType {
 		NONE, X, Y;
 	}
 	private LockType lockedDirection = LockType.NONE;
 	
 	private final IFigure redrawFigure;
 	private Rectangle bounds;
 	private XYGraph xyGraph;
 	private Dimension cumulativeOffset;
 	private Point startLocation;
 	private Point location;
 
 	private List<IFigure> translations;
 	private boolean active=true;
 
 	private IFigure listenerFigure;
  
 	public FigureTranslator(XYGraph xyGraph, IFigure figure) {
 		this(xyGraph, figure, figure, Arrays.asList(new IFigure[]{figure}));
 	}
 
 	public FigureTranslator(XYGraph xyGraph, IFigure redrawFigure, IFigure listenerFigure, List<IFigure> moveFigures) {
 		this.redrawFigure = redrawFigure;
 		this.listenerFigure = listenerFigure;
 		listenerFigure.addMouseListener(this);
 		listenerFigure.addMouseMotionListener(this);
 		this.translations = moveFigures;
 		this.xyGraph = xyGraph;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent event) {
 		
 		if (!isActive()) return;
 		
 		try {
 			if (location == null) return;
 			Point newLocation = event.getLocation();
 			if (newLocation == null) return;
 			
 			fireBeforeTranslation(new TranslationEvent(this));
 			this.cumulativeOffset = newLocation.getDifference(startLocation);
 	
 			Dimension offset = newLocation.getDifference(location);
 			if (offset.width == 0 && offset.height == 0) return;

 			UpdateManager updateMgr = redrawFigure.getUpdateManager();
 			LayoutManager layoutMgr = redrawFigure.getParent().getLayoutManager();
 			bounds = redrawFigure.getBounds();
 			updateMgr.addDirtyRegion(redrawFigure.getParent(), bounds);
 			
			this.bounds = translate(bounds.getCopy(), offset.width, offset.height, newLocation);
 			if (layoutMgr!=null) layoutMgr.setConstraint(redrawFigure, bounds);
 			
 			for (int i = 0; i < translations.size(); i++) {
				translate(((IFigure) translations.get(i)), offset.width, offset.height, newLocation);
 			}
 		
 			updateMgr.addDirtyRegion(redrawFigure.getParent(), bounds);
			location = newLocation;
 			
 		} finally {
 			fireAfterTranslation(new TranslationEvent(this));
 			event.consume();
 		}
 	}
 
	private Rectangle translate(Object trans, int raw_width, int raw_height, Point location) {
 		
 		int width = 0; int height = 0;
 		if (lockedDirection == LockType.NONE) {
 			width = raw_width; height = raw_height;
 		} else if (lockedDirection == LockType.X) {
 			width = raw_width;
 		} else if (lockedDirection == LockType.Y) {
 			height = raw_height;
 		}
 		if (trans instanceof Rectangle) {
 			return ((Rectangle)trans).translate(width, height);
		} else if (trans instanceof SelectionHandle) {
			((SelectionHandle) trans).setLocation(location);
 		} else if (trans instanceof IFigure)  {
 			((IFigure)trans).translate(width, height);
 		}
 		return null;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent me) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent me) {
 	}
 
 	@Override
 	public void mouseHover(MouseEvent me) {
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent me) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent event) {
 		if (!isActive()) return;
 
 		location = event.getLocation();
 		startLocation = event.getLocation();
 		fireOnActivate(new TranslationEvent(this));
 
 		event.consume();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent event) {
 		if (!isActive()) return;
 
 		if (location == null) return;
 		fireCompletedTranslation(new TranslationEvent(this));
 		xyGraph.getOperationsManager().addCommand(new TranslateCommand(redrawFigure, cumulativeOffset, translations, this));
 		location = null;
 		
 		event.consume();
 	}
 	@Override
 	public void mouseDoubleClicked(MouseEvent me) {
 	}
 
 	/**
 	 * @return point where mouse was pressed
 	 */
 	public Point getStartLocation() {
 		return startLocation;
 	}
 
 	/**
 	 * @return point where mouse is (or was released)
 	 */
 	public Point getEndLocation() {
 		return location;
 	}
 
 	/**
 	 * @return figure that is to be redrawn
 	 */
 	public IFigure getRedrawFigure() {
 		return redrawFigure;
 	}
 
 	private List<TranslationListener> listeners;
 
 	public void removeTranslationListeners() {
 		if (listeners != null) {
 			listeners.clear();
 		}
 	}
 
 	public void addTranslationListener(TranslationListener translationListener) {
 		if (listeners == null)
 			listeners = new ArrayList<TranslationListener>(7);
 		listeners.add(translationListener);
 	}
 
 	public void removeTranslationListener(TranslationListener translationListener) {
 		if (listeners == null)
 			return;
 		listeners.remove(translationListener);
 	}
 
 	protected void fireBeforeTranslation(TranslationEvent evt) {
 		if (listeners == null)
 			return;
 		for (TranslationListener l : listeners) {
 			l.translateBefore(evt);
 		}
 	}
 
 	protected void fireOnActivate(TranslationEvent evt) {
 		if (listeners == null)
 			return;
 		for (TranslationListener l : listeners) {
 			l.onActivate(evt);
 		}
 	}
 
 	protected void fireAfterTranslation(TranslationEvent evt) {
 		if (listeners == null)
 			return;
 		for (TranslationListener l : listeners) {
 			l.translationAfter(evt);
 		}
 	}
 
 	protected void fireCompletedTranslation(TranslationEvent evt) {
 		if (listeners == null)
 			return;
 		for (TranslationListener l : listeners) {
 			l.translationCompleted(evt);
 		}
 	}
 
 	public void setActive(boolean mobile) {
 		this.active = mobile;
 		if (active) {
 			listenerFigure.addMouseListener(this);
 			listenerFigure.addMouseMotionListener(this);
 		} else {
 			listenerFigure.removeMouseListener(this);
 			listenerFigure.removeMouseMotionListener(this);
 		}
 	}
 	
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setLockedDirection(LockType d) {
 		lockedDirection = d;
 	}
 
 }
