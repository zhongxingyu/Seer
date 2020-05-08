 package org.dawnsci.plotting.draw2d.swtxy;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.widgets.figureparts.ColorMapRamp;
 import org.csstudio.swt.xygraph.figures.Annotation;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.PlotArea;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.undo.ZoomType;
 import org.dawb.common.services.ImageServiceBean.ImageOrigin;
 import org.dawb.common.ui.image.CursorUtils;
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.dawb.common.ui.plot.axis.ICoordinateSystem;
 import org.dawb.common.ui.plot.axis.IPositionListener;
 import org.dawb.common.ui.plot.axis.PositionEvent;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
 import org.dawnsci.plotting.draw2d.swtxy.selection.SelectionRegionFactory;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.PaletteData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RegionArea extends PlotArea {
 
 	private static final Logger logger = LoggerFactory.getLogger(RegionArea.class);
 	
 	protected ISelectionProvider selectionProvider;
 	private final Map<String,AbstractSelectionRegion>     regions;
 	private final Map<String,ImageTrace> imageTraces;
 	
 	private Collection<IRegionListener>     regionListeners;
 	private Collection<ITraceListener>      imageTraceListeners;
 	private boolean                         containsMouse=false;
 
 	public RegionArea(XYRegionGraph xyGraph) {
 		super(xyGraph);
 		this.regions     = new LinkedHashMap<String,AbstractSelectionRegion>();
 		this.imageTraces = new LinkedHashMap<String,ImageTrace>();	
 		
 		addMouseMotionListener(new MouseMotionListener.Stub() {
 			@Override
 			public void mouseMoved(MouseEvent me) {
 				firePositionListeners(new PositionEvent(RegionArea.this, 
 						                               (AspectAxis)getRegionGraph().primaryXAxis,
 						                               (AspectAxis)getRegionGraph().primaryYAxis,
 													    me.x, 
 													    me.y));
 				createPositionCursor(me);
 			}
 			/**
 			 * @see org.eclipse.draw2d.MouseMotionListener#mouseEntered(MouseEvent)
 			 */
 			public void mouseEntered(MouseEvent me) {
 				containsMouse = true;
 			}
 
 			/**
 			 * @see org.eclipse.draw2d.MouseMotionListener#mouseExited(MouseEvent)
 			 */
 			public void mouseExited(MouseEvent me) {
 				containsMouse = false;
 			}
 			
 		});
 
 	}
 
 	protected void firePositionListeners(PositionEvent positionEvent) {
 		if (positionListeners==null) return;
 		for (IPositionListener l : positionListeners) {
 			l.positionChanged(positionEvent);
 		}
 	}
 	
 	private Collection<IPositionListener> positionListeners;
 	public void addPositionListener(IPositionListener l) {
 		if (positionListeners==null) positionListeners = new HashSet<IPositionListener>();
 		positionListeners.add(l);
 	}
 	public void removePositionListener(IPositionListener l) {
 		if (positionListeners==null) return;
 		positionListeners.remove(l);
 	}
 
 	@Override
 	protected void paintClientArea(final Graphics graphics) {
 		super.paintClientArea(graphics);
 		
 		
 	}
 
 	public void setStatusLineManager(final IStatusLineManager statusLine) {
 		
 		if (statusLine==null) return;
 		
 		final NumberFormat format = new DecimalFormat("#0.0000#");
 		addPositionListener(new IPositionListener() {
 
 			@Override
 			public void positionChanged(PositionEvent me) {
 				final IImageTrace trace = getImageTrace();
 				if (trace!=null) {
 					try {
 						double[] da = trace.getPointInAxisCoordinates(new double[]{me.x,me.y});
 						statusLine.setMessage(format.format(da[0])+", "+format.format(da[1]));
 						return;
 					} catch (Throwable ignored) {
                         // Normal position
 					}
 				}
 
 				statusLine.setMessage(format.format(me.x)+", "+format.format(me.y));
 			}
 		});
 	}
 	
     private Cursor positionCursor;
 	/**
 	 * Whenever cursor is NONE we show intensity info.
 	 * @param me
 	 */
 	protected void createPositionCursor(MouseEvent me) {
 		
 		if (!containsMouse)  {
 			setCursor(null);
 			return;
 		}
 		if (getSelectedCursor()!=null) return;
 		if (zoomType!=ZoomType.NONE)   return;
 		
 
		if (positionCursor==null) positionCursor.dispose();
 		positionCursor = CursorUtils.getPositionCursor(me, (AspectAxis)getRegionGraph().primaryXAxis, (AspectAxis)getRegionGraph().primaryYAxis, getImageTrace());
 		setCursor(positionCursor);
 	}
 
 
 	public void addRegion(final AbstractSelectionRegion region) {
 		addRegion(region, true);
 	}
 
 	void addRegion(final AbstractSelectionRegion region, boolean fireListeners) {
 		regions.put(region.getName(), region);
 		region.setXyGraph(xyGraph);
 		region.createContents(this);
 		region.setSelectionProvider(selectionProvider);
 		if (fireListeners) fireRegionAdded(new RegionEvent(region));
 		clearRegionTool();
 		revalidate();
 	}
 
 	public boolean removeRegion(final AbstractSelectionRegion region) {
 		if (region==null) return false;
 	    final AbstractSelectionRegion gone = regions.remove(region.getName());
 		if (gone!=null){
 			gone.remove(); // Clears up children (you can live without this
 			fireRegionRemoved(new RegionEvent(gone));
 			revalidate();
 		}
 		return gone!=null;
 	}
 	
 	public void renameRegion(final AbstractSelectionRegion region, String name) {
 	    regions.remove(region.getName());
 	    region.setName(name);
 	    regions.put(name, region);
 	}
 	
 	public void clearRegions() {
 		clearRegionsInternal();
 		revalidate();
 	}
 	
     protected void clearRegionsInternal() {
 		clearRegionTool();
 		if (regions==null) return;
 		
 		final Collection<String> deleted = new HashSet<String>(5);
 		for (AbstractSelectionRegion region : regions.values()) {
 			if (!region.isUserRegion()) continue;
 			deleted.add(region.getName());
 			region.remove();
 		}
 		regions.keySet().removeAll(deleted);
 		fireRegionsRemoved(new RegionEvent(this));
 
 	}
 	
 
 	public ImageTrace createImageTrace(String name, Axis xAxis, Axis yAxis, ColorMapRamp intensity) {
 
         if (imageTraces.containsKey(name)) throw new RuntimeException("There is an image called '"+name+"' already plotted!");
         
 		final ImageTrace trace = new ImageTrace(name, xAxis, yAxis, intensity);
 		
 		fireImageTraceCreated(new TraceEvent(trace));
 		
 		return trace;
 	}
 
 	/**Add a trace to the plot area.
 	 * @param trace the trace to be added.
 	 */
 	public void addImageTrace(final ImageTrace trace){
 		imageTraces.put(trace.getName(), trace);
 		add(trace);
 		
         toFront();		
 		revalidate();
 		
 		fireImageTraceAdded(new TraceEvent(trace));
 	}
 	
 	void toFront() {
 		for (Annotation a : getAnnotationList()) {
 			a.toFront();
 		}
 		// Move all regions to front again
 		if (getRegionMap()!=null) for (String name : getRegionMap().keySet()) {
 			try {
 				getRegionMap().get(name).toFront();
 			} catch (Exception ne) {
 				continue;
 			}
 		}
 	}
 	
 	public boolean removeImageTrace(final ImageTrace trace){
 	    final ImageTrace gone = imageTraces.remove(trace.getName());
 		if (gone!=null){
 			trace.remove();
 			fireImageTraceRemoved(new TraceEvent(trace));
 			revalidate();
 		}
 		return gone!=null;
 	}
 
 	public void clearImageTraces() {
 		if (imageTraces==null) return;
 		for (ImageTrace trace : imageTraces.values()) {
 			trace.remove();
 			fireImageTraceRemoved(new TraceEvent(trace));
 		}
 		imageTraces.clear();
 		revalidate();
 	}
 
 	
 	@Override
 	protected void layout() {
 	    final Rectangle clientArea = getClientArea();
 		for(ImageTrace trace : imageTraces.values()){
 			if(trace != null && trace.isVisible())
 				//Shrink will make the trace has no intersection with axes,
 				//which will make it only repaints the trace area.
 				trace.setBounds(clientArea);//.getCopy().shrink(1, 1));				
 		}		
         super.layout();
 	}
 		
     RegionMouseListener regionListener;
     
     /**
      * Has to be set when plotting system is created.
      */
 	RegionCreationLayer               regionLayer;
 
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
 	public AbstractSelectionRegion createRegion(String name, IAxis x, IAxis y, RegionType regionType, boolean startingWithMouseEvent) throws Exception {
 
 		if (getRegionMap()!=null) {
 			if (getRegionMap().containsKey(name)) throw new Exception("The region '"+name+"' already exists.");
 		}
 		
 		ICoordinateSystem       coords  = new RegionCoordinateSystem(getImageTrace(), x, y);
 		AbstractSelectionRegion region  = SelectionRegionFactory.createSelectionRegion(name, coords, regionType);
 		if (startingWithMouseEvent) {
 			xyGraph.setZoomType(ZoomType.NONE);
 		    
 		    // Mouse listener for region bounds
 		    regionListener = new RegionMouseListener(regionLayer, this, region, region.getMinimumMousePresses(), region.getMaximumMousePresses());
 		    regionLayer.setMouseListenerActive(regionListener, true);
 		}
 
 		fireRegionCreated(new RegionEvent(region));
         return region;
 	}
 
 	public void setRegionLayer(RegionCreationLayer regionLayer) {
 		this.regionLayer = regionLayer;
 	}
 
 	public void disposeRegion(AbstractSelectionRegion region) {
 		removeRegion(region);
 		setCursor(null);
 		clearRegionTool();
 	}
 	
 	
 	protected void clearRegionTool() {
 		if (regionListener!=null) {
 		    regionLayer.setMouseListenerActive(regionListener, false);
 			IRegion wasBeingAdded = regionListener.getRegionBeingAdded();
 		    regionListener = null;
 		    setCursor(null);
 		    
 			if (wasBeingAdded!=null) {
 				fireRegionCancelled(new RegionEvent(wasBeingAdded));
 			}
 
 		}
 	}
 	
 	private Cursor specialCursor;
 	
 	public void setSelectedCursor(Cursor cursor) {
 		setZoomType(ZoomType.NONE);
 		setCursor(cursor);
 		specialCursor = cursor;
 	}
 	/**
 	 * Custom cursor if one set, or null
 	 * @return
 	 */
 	public Cursor getSelectedCursor() {
 		return specialCursor;
 	}
 	
 	private Cursor internalCursor;
 	public void setCursor(Cursor cursor) {
 		
 		try {
 			if (cursor!=null&&cursor.isDisposed()) cursor = null;
 			if (cursor!=null && this.internalCursor == cursor) return;
 			if (specialCursor!=null && !specialCursor.isDisposed()) {
 				cursor = specialCursor;
 			}
 
 			internalCursor = cursor;
 			if (cursor!=null&&cursor.isDisposed()) cursor = null;
 		    super.setCursor(cursor);
 		} catch (Throwable ignored) {
 			// Intentionally ignore bad cursors.
 		}
 	}
 
 	public void setZoomType(final ZoomType zoomType) {
 		specialCursor = null;
 		clearRegionTool();
 		super.setZoomType(zoomType);
 	}
 
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean addRegionListener(final IRegionListener l) {
 		if (regionListeners == null) regionListeners = new HashSet<IRegionListener>(7);
 		return regionListeners.add(l);
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean removeRegionListener(final IRegionListener l) {
 		if (regionListeners == null) return true;
 		return regionListeners.remove(l);
 	}
 
 	protected void fireRegionCreated(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionCreated(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region creation", ne);
 				continue;
 			}
 		}
 	}
 	
 	protected void fireRegionCancelled(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionCancelled(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region add being cancelled", ne);
 				continue;
 			}
 		}
 	}
 
 	protected void fireRegionAdded(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionAdded(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region add", ne);
 				continue;
 			}
 		}
 	}
 	
 	protected void fireRegionRemoved(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionRemoved(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region removal", ne);
 				continue;
 			}
 		}
 	}
 	protected void fireRegionsRemoved(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 			    l.regionsRemoved(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region removal", ne);
 				continue;
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean addImageTraceListener(final ITraceListener l) {
 		if (imageTraceListeners == null) imageTraceListeners = new HashSet<ITraceListener>(7);
 		return imageTraceListeners.add(l);
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean removeImageTraceListener(final ITraceListener l) {
 		if (imageTraceListeners == null) return true;
 		return imageTraceListeners.remove(l);
 	}
 
 	
 	protected void fireImageTraceCreated(TraceEvent evt) {
 		if (imageTraceListeners==null) return;
 		for (ITraceListener l : imageTraceListeners) l.traceCreated(evt);
 	}
 	
 
 	protected void fireImageTraceAdded(TraceEvent evt) {
 		if (imageTraceListeners==null) return;
 		for (ITraceListener l : imageTraceListeners) l.traceAdded(evt);
 	}
 	
 	protected void fireImageTraceRemoved(TraceEvent evt) {
 		if (imageTraceListeners==null) return;
 		for (ITraceListener l : imageTraceListeners) l.traceRemoved(evt);
 	}
 
 	
 	public Map<String, AbstractSelectionRegion> getRegionMap() {
 		return regions;
 	}
 	public List<AbstractSelectionRegion> getRegions() {
 		final Collection<AbstractSelectionRegion> vals = regions.values();
 		return new ArrayList<AbstractSelectionRegion>(vals);
 	}
 	
 //	private Image rawImage;
 	
 //	@Override
 //	protected void paintClientArea(final Graphics graphics) {
 	
 // TODO
 //		if (rawImage==null) {
 //			rawImage = new Image(Display.getCurrent(), "C:/tmp/ESRF_Pilatus_Data.png");
 //		}
 //		
 //		final Rectangle bounds = getBounds();
 //		final Image scaled = new Image(Display.getCurrent(),
 //				rawImage.getImageData().scaledTo(bounds.width,bounds.height));
 //		graphics.drawImage(scaled, new Point(0,0));
 //
 //		super.paintClientArea(graphics);
 //
 //	}
 
 	public Collection<String> getRegionNames() {
 		return regions.keySet();
 	}
 
 
 	public void setSelectionProvider(ISelectionProvider provider) {
 		this.selectionProvider = provider;
 	}
 
 
 	public AbstractSelectionRegion getRegion(String name) {
 		if (regions==null) return null;
 		return regions.get(name);
 	}
 
 	public Map<String,ImageTrace> getImageTraces() {
 		return this.imageTraces;
 	}
 
   
 	/**
 	 * Must call in UI thread safe way.
 	 */
 	public void clearTraces() {
 		
 		final List<Trace> traceList = getTraceList();
 		if (traceList!=null) {
 			for (Trace trace : traceList) {
 				remove(trace);
 				if (trace instanceof LineTrace) ((LineTrace)trace).dispose();
 			}
 			traceList.clear();
 	    }
 		
 		if (imageTraces!=null) {
 			final Collection<ImageTrace> its = new HashSet<ImageTrace>(imageTraces.values());
 			for (ImageTrace trace : its) {
 				final ImageTrace gone = imageTraces.remove(trace.getName());
 				if (gone!=null){
 					trace.remove();
 					fireImageTraceRemoved(new TraceEvent(trace));
 				}
 			}
 
 			imageTraces.clear();
 
 		}
 
 	}
 
 
 	public void setPaletteData(PaletteData data) {
 		if (imageTraces!=null) for (ImageTrace trace : imageTraces.values()) {
 			trace.setPaletteData(data);
 		}
 	}
 
 
 	public void setImageOrigin(ImageOrigin origin) {
 		if (imageTraces!=null) for (ImageTrace trace : imageTraces.values()) {
 			trace.setImageOrigin(origin);
 		}
 	}
 
 
 	public ImageTrace getImageTrace() {
 		if (imageTraces!=null && imageTraces.size()>0) return imageTraces.values().iterator().next();
 		return null;
 	}
 
 
 	public void dispose() {
 		
 		clearTraces();
 		clearRegionsInternal();
 		if (regionListeners!=null)     regionListeners.clear();
 		if (imageTraceListeners!=null) imageTraceListeners.clear();
 		if (regions!=null)             regions.clear();
 		if (imageTraces!=null)         imageTraces.clear();
 	}
 
 	/**
 	 * Call to find out of any of the current regions are user editable.
 	 * @return
 	 */
 	public boolean hasUserRegions() {
 		if (getRegionMap()==null || getRegionMap().isEmpty()) return false;
 		for (String regionName : getRegionMap().keySet()) {
 			if (getRegionMap().get(regionName).isUserRegion()) return true;
 		}
 		return false;
 	}
 
 	XYRegionGraph getRegionGraph() {
 		return (XYRegionGraph)xyGraph;
 	}
 
 }
