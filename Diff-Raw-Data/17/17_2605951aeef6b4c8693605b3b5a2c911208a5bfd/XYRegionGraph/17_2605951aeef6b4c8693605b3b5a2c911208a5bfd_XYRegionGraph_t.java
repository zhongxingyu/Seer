 package org.dawnsci.plotting.draw2d.swtxy;
 
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.csstudio.swt.widgets.figureparts.ColorMapRamp;
 import org.csstudio.swt.xygraph.figures.Annotation;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.IAnnotationLabelProvider;
 import org.csstudio.swt.xygraph.figures.Legend;
 import org.csstudio.swt.xygraph.figures.PlotArea;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
 import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
 import org.dawnsci.plotting.api.region.IRegionListener;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 
 /**
  * This is an XYGraph which supports regions of interest.
  * @author fcp94556
  *
  */
 public class XYRegionGraph extends XYGraph {
 	
 	public XYRegionGraph() {
 		super();
 		removeAxis(primaryXAxis);
 		removeAxis(primaryYAxis);
 		
		primaryYAxis = new AspectAxis(Y_AXIS, true);
 		primaryYAxis.setTickLabelSide(LabelSide.Primary);
 		primaryYAxis.setAutoScaleThreshold(0.1);
 		addAxis(primaryYAxis);
 
		primaryXAxis = new AspectAxis(X_AXIS, false);
 		primaryXAxis.setTickLabelSide(LabelSide.Primary);
 		addAxis(primaryXAxis);
 
 		try {
 		    this.showLegend = getPreferenceStore().getBoolean(BasePlottingConstants.XY_SHOWLEGEND);
 		} catch (NullPointerException ne) {
 			this.showLegend = true;
 		}
 	}
 	
 	private IPreferenceStore store;
 	private IPreferenceStore getPreferenceStore() {
 		if (store!=null) return store;
 		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
 		return store;
 	}
 	
 	@Override
 	protected PlotArea createPlotArea(XYGraph xyGraph) {
         return new RegionArea(this);
 	}
 
 	public void addRegion(final AbstractSelectionRegion region) {
 		getRegionArea().addRegion(region);
 	}
 	public void removeRegion(final AbstractSelectionRegion region) {
 		getRegionArea().removeRegion(region);
 	}
 	public void renameRegion(final AbstractSelectionRegion region, String name) {
 		getRegionArea().renameRegion(region, name);
 	}
 	public void setSelectionProvider(final ISelectionProvider provider) {
 		getRegionArea().setSelectionProvider(provider);
 	}
 
 	/**
 	 * Create region of interest
 	 * @param name
 	 * @param xAxis
 	 * @param yAxis
 	 * @param regionType
 	 * @param startingWithMouseEvent
 	 * @return region
 	 * @throws Exception
 	 */
 	public AbstractSelectionRegion createRegion(String name, IAxis xAxis, IAxis yAxis, RegionType regionType, boolean startingWithMouseEvent) throws Exception {
 		return getRegionArea().createRegion(name, xAxis, yAxis, regionType, startingWithMouseEvent);
 	}
 	public void disposeRegion(final AbstractSelectionRegion region) {
 		getRegionArea().disposeRegion(region);
 	}
 
 
 	public ImageTrace createImageTrace(String name, Axis xAxis, Axis yAxis, ColorMapRamp intensity) {
 		RegionArea ra = (RegionArea)getPlotArea();
 		return ra.createImageTrace(name, xAxis, yAxis, intensity);
 	}
 	public ImageStackTrace createImageStackTrace(String name, Axis xAxis, Axis yAxis, ColorMapRamp intensity) {
 		RegionArea ra = (RegionArea)getPlotArea();
 		return ra.createImageStackTrace(name, xAxis, yAxis, intensity);
 	}
 	
 	public void addImageTrace(final ImageTrace trace) {
 		getRegionArea().addImageTrace(trace);
 	}
 	public void removeImageTrace(final ImageTrace trace) {
 		getRegionArea().removeImageTrace(trace);
 	}
 
 	public boolean addRegionListener(IRegionListener l) {
 		return getRegionArea().addRegionListener(l);
 	}
 	
 	public boolean removeRegionListener(IRegionListener l) {
 		return getRegionArea().removeRegionListener(l);
 	}
 
 	public AbstractSelectionRegion getRegion(String name) {
 		return getRegionArea().getRegion(name);
 	}
 
 	public void clearRegions() {
 		getRegionArea().clearRegions();
 	}
 	public List<AbstractSelectionRegion> getRegions() {
 		return getRegionArea().getRegions();
 	}
 
 	public void clearRegionTool() {
 		getRegionArea().clearRegionTool();
 	}
 
 	public void clearImageTraces() {
 		getRegionArea().clearImageTraces();
 	}
 	
 	public RegionArea getRegionArea() {
 		return (RegionArea)getPlotArea();
 	}
 
 	public void performAutoScale(){
 
 		if (getRegionArea().getImageTraces()!=null && getRegionArea().getImageTraces().size()>0) {
 			
 			for (ImageTrace trace : getRegionArea().getImageTraces().values()) {
 				trace.performAutoscale();
 			}
 			
 			
 		} else {
 			super.performAutoScale();
 		}
 		
 	}
 	
 	public void setDefaultShowLegend(boolean showLeg) {
 		this.showLegend = showLeg;
 	}
 	
 	public void setShowLegend(boolean showLeg) {
 		super.setShowLegend(showLeg);
 		getPreferenceStore().setValue(BasePlottingConstants.XY_SHOWLEGEND, showLeg);
 	}
 	
 	/**
 	 * @return the showLegend
 	 */
 	public boolean isShowLegend() {
 		return showLegend;
 	}
 
 
 	/**
 	 * Call from UI thread only!
 	 */
 	public void clearTraces() {
 		
 		if (super.getLegendMap()!=null) {
 			final Collection<Legend> legends = super.getLegendMap().values();
 			for (Legend legend : legends) {
 				legend.getTraceList().clear();
 			}
 			//super.getLegendMap().clear();
 		}
 		
 		for(Axis axis : getXAxisList()){
 			axis.clear();
 		}
 		for(Axis axis : getYAxisList()){
 			axis.clear();
 		}
 		getRegionArea().clearTraces();
 		
 		revalidate();
 
 	}
 
 	public void setPaletteData(PaletteData data) {
 		getRegionArea().setPaletteData(data);
 	}
 
 	public void setImageOrigin(ImageOrigin origin) {
 		getRegionArea().setImageOrigin(origin);
 	}
 	
 	public void layout() {
 		
 		super.layout();
 		
 		for (Axis axis : getXAxisList()) {
 			if (axis instanceof AspectAxis) {
 				((AspectAxis)axis).checkBounds();
 			}
 		}
 		for (Axis axis : getYAxisList()) {
 			if (axis instanceof AspectAxis) {
 				((AspectAxis)axis).checkBounds();
 			}
 		}
 		
 		if(getPlotArea() != null && getPlotArea().isVisible()){
 
 			Rectangle plotAreaBound = new Rectangle(
 					primaryXAxis.getBounds().x + primaryXAxis.getMargin(),
 					primaryYAxis.getBounds().y + primaryYAxis.getMargin(),
 					primaryXAxis.getTickLength(),
 					primaryYAxis.getTickLength()
 					);
 			getPlotArea().setBounds(plotAreaBound);
 
 		}
 		
 	}
 
 	public void setKeepAspect(boolean checked) {
 		for (Axis axis : getXAxisList()) {
 			if (axis instanceof AspectAxis) ((AspectAxis)axis).setKeepAspect(checked);
 		}
 		for (Axis axis : getYAxisList()) {
 			if (axis instanceof AspectAxis) ((AspectAxis)axis).setKeepAspect(checked);
 		}
 	}
 	
 	/**
 	 * Zooms about the central point a factor (-ve for out) usually +-0.1
 	 */
 	public void setZoomLevel(MouseEvent evt, double delta) {
 		
 		int primX = primaryXAxis.getTickLength();
 		int primY = primaryYAxis.getTickLength();
 		double xScale = delta;
 		double yScale = delta;	
 
 		// Allow for axis size
 		if (primX>(primY*1.333)) {
 			double ratio = (Double.valueOf(primX)/Double.valueOf(primY));
 			yScale = delta / ratio;
 		} else if (primY>(primX*1.333)) {
 			xScale = delta / (Double.valueOf(primY)/Double.valueOf(primX));
 		}
 		
 		// Allow for available size
 		if (getRegionArea().getImageTrace()!=null && Boolean.getBoolean("org.dawb.workbench.plotting.system.do.zoom.fudging")) {
 			
 			// Fudged scaling algorithm
 			// TODO make a less jerky one
 			Rectangle fullSize  = getBounds();
 			int w  = fullSize.width; 
 			int h  = fullSize.height;
 			int xg = w-primX; 
 			int yg = h-primY; 
 			if ((xg-yg)>100) {
 				double scale = 2d;
 				yScale = delta>0 ? yScale*scale : yScale/scale;
 			} else if ((yg-xg)>10) {
 				double scale = 6d;
 				yScale = delta>0 ? yScale/scale : yScale*scale;
 			}
 		}
 		
 		for (Axis axis : getXAxisList()) {
 			final double cenX = axis.getPositionValue(evt.x, false);
 			axis.zoomInOut(cenX, xScale);
 		}
 		for (Axis axis : getYAxisList()) {
 			final double cenY = axis.getPositionValue(evt.y, false);
 			axis.zoomInOut(cenY, yScale);
 		}
 	}
 	
 	public void dispose() {
 		removeAll();
 		getRegionArea().dispose();
 		clearPropertyListeners();
 	}
 	
 	private final NumberFormat integerFormat   = NumberFormat.getIntegerInstance();
 	private final NumberFormat intensityFormat = NumberFormat.getNumberInstance();
 	
 	public void addAnnotation(final Annotation annotation){
 		
 		annotation.setLabelProvider(new IAnnotationLabelProvider() {		
 			@Override
 			public String getInfoText(double xValue, double yValue) {
 				if (getRegionArea().getImageTrace()==null) return null;
 				if (annotation.getTrace()!=null) return null;
 				
 				final ImageTrace im = getRegionArea().getImageTrace();
 				final StringBuilder buf = new StringBuilder();
 				buf.append(annotation.getName());
 				try {
 					if (annotation.getXAxis().getRange().inRange(xValue) &&
 						annotation.getYAxis().getRange().inRange(yValue)) {
 						
 						final float value = im.getData().getFloat((int)yValue, (int)xValue);
 						buf.append("\nIntensity ");
 						buf.append(intensityFormat.format(value));
 					}
 				} catch (Exception ignored) {
 					// We carry on if the pixel locations are invalid.
 				}
 				buf.append("\nLocation ");
 				buf.append("(");
 				buf.append(integerFormat.format(xValue));
 				buf.append(" : ");
 				buf.append(integerFormat.format(yValue));
 				buf.append(")");
 				return buf.toString();
 			}
 		});
 		super.addAnnotation(annotation);
 	}
 
 	private Collection<IPropertyChangeListener> propertyListeners;
 	
 	/**
 	 * NOTE This listener is *not* notified once for each configuration setting made on 
 	 * the configuration but once whenever the form is applied by the user (and many things
 	 * are changed) 
 	 * 
 	 * You then have to read the property you require from the object (for instance the axis
 	 * format) in case it has changed. This is not ideal, later there may be more events fired and
 	 * it will be possible to check property name, for now it is always set to "Graph Configuration".
 	 * 
 	 * @param listener
 	 */
 	public void addPropertyChangeListener(IPropertyChangeListener listener) {
 		if (propertyListeners==null) propertyListeners = new HashSet<IPropertyChangeListener>(3);
 		propertyListeners.add(listener);
 	}
 	
 	public void removePropertyChangeListener(IPropertyChangeListener listener) {
 		if (propertyListeners==null) return;
 		propertyListeners.remove(listener);
 	}
 	
 	protected void clearPropertyListeners() {
 		if (propertyListeners!=null) propertyListeners.clear();
 	}
 	
 	public void fireConfigurationPropertyChangeListeners() {
 		if (propertyListeners==null) return;
 		final PropertyChangeEvent evt = new PropertyChangeEvent(this, "Graph Configuration", "Various", "Various (some new)");
 		for (IPropertyChangeListener l : propertyListeners) {
 			l.propertyChange(evt);
 		}
 	}
 
 	public void setShowAxes(final boolean checked) {
 		this.primaryXAxis.setVisible(checked);
 		this.primaryYAxis.setVisible(checked);
 	}
 
 	/**
 	 * 
 	 * @param trace
 	 * @param toFront - if true, move regions to front
 	 */
 	public void addTrace(Trace trace, Axis xAsxis, Axis yAxis, boolean toFront) {
 		super.addTrace(trace, xAsxis, yAxis);
 		getRegionArea().toFront();
 	}
 
 	
 	private IAxis selectedXAxis;
 	private IAxis selectedYAxis;
 
 	public IAxis getSelectedXAxis() {
 		if (selectedXAxis==null) {
 			return (AspectAxis)primaryXAxis;
 		}
 		return selectedXAxis;
 	}
 
 	public void setSelectedXAxis(IAxis selectedXAxis) {
 		this.selectedXAxis = selectedXAxis;
 	}
 
 	public IAxis getSelectedYAxis() {
 		if (selectedYAxis==null) {
 			return (AspectAxis)primaryYAxis;
 		}
 		return selectedYAxis;
 	}
 
 	public void setSelectedYAxis(IAxis selectedYAxis) {
 		this.selectedYAxis = selectedYAxis;
 	}

	@Override
	public boolean removeAxis(Axis axis) {
		// need to be done to stop inconsistency(!)
		if (axis == selectedXAxis) {
			selectedXAxis = null;
		} else if (axis == selectedYAxis) {
			selectedYAxis = null;
		}
		return super.removeAxis(axis);
	}
 }
