 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegionContainer;
 import org.dawnsci.plotting.api.region.MouseListener;
 import org.dawnsci.plotting.api.region.MouseMotionListener;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 /**
  * This is a Figure, disabled for mouse events. 
  * 
  * @author fcp94556
  *
  */
 public abstract class AbstractRegion extends Figure implements IRegion, IRegionContainer {
 
 	private Collection<IROIListener> roiListeners;
 	private boolean regionEventsActive = true;
 	private boolean maskRegion         = false;
 	protected String label = null;
 	protected Color labelColour = null;
 	protected Font labelFont = new Font(Display.getCurrent(), "Dialog", 10, SWT.NORMAL);
 	protected Dimension labeldim;
 
 	@Override
 	public boolean addROIListener(final IROIListener l) {
 		if (roiListeners==null) roiListeners = new HashSet<IROIListener>(11);
 		if (!roiListeners.contains(l)) return roiListeners.add(l);
 		return false;
 	}
 	
 	@Override
 	public boolean removeROIListener(final IROIListener l) {
 		if (roiListeners==null) return false;
 		return roiListeners.remove(l);
 	}
 	
 	protected void clearListeners() {
 		if (roiListeners==null) return;
 		roiListeners.clear();
 	}
 	
 	protected void fireROIDragged(IROI roi, ROIEvent.DRAG_TYPE type) {
 		if (roiListeners==null) return;
 		if (!regionEventsActive) return;
 		
 		final ROIEvent evt = new ROIEvent(this, roi);
 		evt.setDragType(type);
 		for (IROIListener l : roiListeners) {
 			try {
 			    l.roiDragged(evt);
 			} catch (Throwable ne) {
				logger.error("Unexpected exception in drawing!", ne);
 			}
 		}
 	}
 	
 	private static final Logger logger = LoggerFactory.getLogger(AbstractRegion.class);
 	
 	protected void fireROIChanged(IROI roi) {
 		if (roiListeners==null)  return;
 		if (!regionEventsActive) return;
 		
 		final ROIEvent evt = new ROIEvent(this, roi);
 		for (IROIListener l : roiListeners.toArray(new IROIListener[0])) {
 			try {
 			    l.roiChanged(evt);
 			} catch (Throwable ne) {
 				logger.error("Unexpected exception in drawning!", ne);
 			}
 		}
 	}
 	protected void fireROISelected(IROI roi) {
 		if (roiListeners==null)  return;
 		if (!regionEventsActive) return;
 		
 		final ROIEvent evt = new ROIEvent(this, roi);
 		for (IROIListener l : roiListeners) {
 			try {
 			 l.roiSelected(evt);
 			} catch (Throwable ne) {
 				logger.error("Unexpected exception in drawning!", ne);
 			}
 		}
 	}
 
 	protected IROI roi;
 
 	@Override
 	public IROI getROI() {
 		return roi;
 	}
 
 	@Override
 	public void setROI(IROI roi) {
 		// Required fix after someone thought it would be a laugh to send
 		// null ROIs over.
 		if (roi == null) throw new NullPointerException("Cannot have a null region position!");
 		this.roi = roi;
 		updateROI();
 		fireROIChanged(this.roi);
 	}
 
 	/**
 	 * Implement to return the region of interest
 	 * @param recordResult if true this calculation changes the recorded absolute position
 	 */
 	protected abstract IROI createROI(boolean recordResult);
 
 	/**
 	 * Updates the region, usually called when items have been created and the position of the
 	 * region should be updated. Does not fire events.
 	 */
 	protected void updateROI() {
 		if (roi != null) {
 			try {
 				this.regionEventsActive = false;
 				updateROI(roi);
 			} finally {
 				this.regionEventsActive = true;
 			}
 		}
 	}
 	
 	/**
 	 * Implement this method to redraw the figure to the axis coordinates (only).
 	 * 
 	 * @param roi
 	 */
 	protected abstract void updateROI(IROI roi);
 
 	public String toString() {
 		if (getName()!=null) return getName();
 		return super.toString();
 	}
 	
 	protected boolean trackMouse;
 
 	@Override
 	public boolean isTrackMouse() {
 		return trackMouse;
 	}
 
 	@Override
 	public void setTrackMouse(boolean trackMouse) {
 		this.trackMouse = trackMouse;
 	}
 	
 	private boolean userRegion = true; // Normally a user region.
 
 	@Override
 	public boolean isUserRegion() {
 		return userRegion;
 	}
 
 	@Override
 	public void setUserRegion(boolean userRegion) {
 		this.userRegion = userRegion;
 	}
 	
 	public IRegion getRegion() {
 		return this;
 	}
 
 	public void setRegion(IRegion region) {
 		// Does nothing
 	}
 
 	public boolean isMaskRegion() {
 		return maskRegion;
 	}
 
 	public void setMaskRegion(boolean maskRegion) {
 		this.maskRegion = maskRegion;
 	}
 	
 	public String getLabel() {
 		if (label==null) return getName();
 		return label;
 	}
 	
 	public void setLabel(String label) {
 		this.label = label ;
 		this.labeldim = FigureUtilities.getTextExtents(label, labelFont);
 	}
 	
 	private Object userObject;
 	/**
 	 * 
 	 * @return last object
 	 */
 	public Object setUserObject(Object object) {
 		Object tmp = userObject;
 		userObject = object;
 		return tmp;
 	}
 	
 	
 	/**
 	 * Call to remove unused resources. Do not forget to use
 	 * super.dispose() in your override.
 	 */
 	public void dispose() {
 		if (labelFont!=null) labelFont.dispose();
 		labelFont   = null;
 		if (labelColour!=null) labelColour.dispose();
 		labelColour = null;
 		labeldim    = null;
 	}
 
 	
 	/**
 	 * 
 	 * @return object
 	 */
 	public Object getUserObject() {
 		return userObject;
 	}
 
 	private boolean isActive = false;
 
 	/**
 	 * Returns whether the region is active or not
 	 */
 	@Override
 	public boolean isActive(){
 		return isActive;
 	}
 
 	/**
 	 * Set whether the region is active or not
 	 * @param b
 	 */
 	@Override
 	public void setActive(boolean b){
 		this.isActive = b;
 	}
 	
 	@Override
 	public void addMouseListener(MouseListener l) {
 		super.addMouseListener(new MouseListenerAdapter(l));
 	}	
 	
 	@Override
 	public void removeMouseListener(MouseListener l){
 		super.removeMouseListener(new MouseListenerAdapter(l));
 	}
 
 	@Override
 	public void addMouseMotionListener(MouseMotionListener l){
 		super.addMouseMotionListener(new MouseMotionAdapter(l));
 	}
 
 	@Override
 	public void removeMouseMotionListener(MouseMotionListener l){
 		super.removeMouseMotionListener(new MouseMotionAdapter(l));
 	}
 	/**
 	 * 
 	 * @return true if the selection region only draws an outline.
 	 */
 	public boolean isOutlineOnly() {
 		return false;
 	}
 	
 	/**
 	 * Set if the region should draw in outline only mode. If
 	 * outline only is not available for this selection region, this
 	 * method will throw a RuntimeException.
 	 */
 	public void setOutlineOnly(boolean outlineOnly) {
 		throw new RuntimeException("setOutlineOnly is not currently implemented by "+getClass().getSimpleName());
 	}
 
 }
