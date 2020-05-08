 package org.dawnsci.plotting.draw2d.swtxy;
 
 import java.lang.ref.Reference;
 import java.lang.ref.SoftReference;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.widgets.figureparts.ColorMapRamp;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.IAxisListener;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.histogram.HistogramBound;
 import org.dawnsci.plotting.api.histogram.IImageService;
 import org.dawnsci.plotting.api.histogram.IPaletteService;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
 import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
 import org.dawnsci.plotting.api.preferences.PlottingConstants;
 import org.dawnsci.plotting.api.trace.DownSampleEvent;
 import org.dawnsci.plotting.api.trace.IDownSampleListener;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.IPaletteListener;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceContainer;
 import org.dawnsci.plotting.api.trace.PaletteEvent;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.api.trace.TraceUtils;
 import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * A trace which draws an image to the plot.
  * 
  * @author fcp94556
  *
  */
 public class ImageTrace extends Figure implements IImageTrace, IAxisListener, ITraceContainer {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ImageTrace.class);
 	
 	private static final int MINIMUM_ZOOM_SIZE  = 4;
 	private static final int MINIMUM_LABEL_SIZE = 10;
 
 	private String           name;
 	private String           dataName;
 	private String           paletteName;
 	private Axis             xAxis;
 	private Axis             yAxis;
 	private ColorMapRamp     intensityScale;
 	private AbstractDataset  image;
 	private DownsampleType   downsampleType=DownsampleType.MAXIMUM;
 	private int              currentDownSampleBin=-1;
 	private List<IDataset>    axes;
 	private ImageServiceBean imageServiceBean;
 	/**
 	 * Used to define if the zoom is at its maximum possible extend
 	 */
 	private boolean          isMaximumZoom;
 	/**
 	 * Used to define if the zoom is at an extent large enough to show a 
 	 * label grid for the intensity.
 	 */
 	private boolean          isLabelZoom;
 	
 	/**
 	 * The parent plotting system for this image.
 	 */
 	private IPlottingSystem plottingSystem;
 
 	private IImageService service;
 
 	private boolean xTicksAtEnd, yTicksAtEnd;
 		
 	public ImageTrace(final String name, 
 			          final Axis xAxis, 
 			          final Axis yAxis,
 			          final ColorMapRamp intensityScale) {
 		
 		this.name  = name;
 		this.xAxis = xAxis;
 		this.yAxis = yAxis;
 		this.intensityScale = intensityScale;
 
 		this.imageServiceBean = new ImageServiceBean();
 		try {
 			final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
 			final String scheme = getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME);
 			imageServiceBean.setPalette(pservice.getPaletteData(scheme));
 			setPaletteName(scheme);
 		} catch (Exception e) {
 			logger.error("Cannot create palette!", e);
 		}	
 		imageServiceBean.setOrigin(ImageOrigin.forLabel(getPreferenceStore().getString(BasePlottingConstants.ORIGIN_PREF)));
 		imageServiceBean.setHistogramType(HistoType.forLabel(getPreferenceStore().getString(BasePlottingConstants.HISTO_PREF)));
 		imageServiceBean.setMinimumCutBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.MIN_CUT)));
 		imageServiceBean.setMaximumCutBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.MAX_CUT)));
 		imageServiceBean.setNanBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.NAN_CUT)));
 		imageServiceBean.setLo(getPreferenceStore().getDouble(BasePlottingConstants.HISTO_LO));
 		imageServiceBean.setHi(getPreferenceStore().getDouble(BasePlottingConstants.HISTO_HI));		
 		
 		this.service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 		downsampleType = DownsampleType.forLabel(getPreferenceStore().getString(BasePlottingConstants.DOWNSAMPLE_PREF));
 
 		xAxis.addListener(this);
 		yAxis.addListener(this);
 
 		xTicksAtEnd = xAxis.hasTicksAtEnds();
 		xAxis.setTicksAtEnds(false);
 		yTicksAtEnd = yAxis.hasTicksAtEnds();
 		yAxis.setTicksAtEnds(false);
 		xAxis.setTicksIndexBased(true);
 		yAxis.setTicksIndexBased(true);
 
 		if (xAxis instanceof AspectAxis && yAxis instanceof AspectAxis) {
 			
 			AspectAxis x = (AspectAxis)xAxis;
 			AspectAxis y = (AspectAxis)yAxis;
 			x.setKeepAspectWith(y);
 			y.setKeepAspectWith(x);		
 		}
 				
 	}
 	
 	private IPreferenceStore store;
 	private IPreferenceStore getPreferenceStore() {
 		if (store!=null) return store;
 		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
 		return store;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public AspectAxis getXAxis() {
 		return (AspectAxis)xAxis;
 	}
 
 	public void setXAxis(Axis xAxis) {
 		this.xAxis = xAxis;
 		xAxis.setTicksIndexBased(true);
 	}
 
 	public AspectAxis getYAxis() {
 		return (AspectAxis)yAxis;
 	}
 
 	public void setYAxis(Axis yAxis) {
 		this.yAxis = yAxis;
 		yAxis.setTicksIndexBased(true);
 	}
 
 	public AbstractDataset getImage() {
 		return image;
 	}
 
 	public PaletteData getPaletteData() {
 		if (imageServiceBean==null) return null;
 		return imageServiceBean.getPalette();
 	}
 
 	public void setPaletteData(PaletteData paletteData) {
 		if (imageServiceBean==null) return;
 		imageServiceBean.setPalette(paletteData);
 		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
 		intensityScale.repaint();
 		repaint();
 		firePaletteDataListeners(paletteData);
 	}
 
 	@Override
 	public String getPaletteName() {
 		return paletteName;
 	}
 
 	@Override
 	public void setPaletteName(String paletteName) {
 		this.paletteName = paletteName;
 	}
 
 	private enum ImageScaleType {
 		// Going up in order of work done
 		NO_REIMAGE,
 		REIMAGE_ALLOWED,
 		FORCE_REIMAGE, 
 		REHISTOGRAM;
 	}
 	private Image            scaledImage;
 	private ImageData        imageData;
 	private boolean          imageCreationAllowed = true;
 	/**
 	 * When this is called the SWT image is created
 	 * and saved in the swtImage field. The image is downsampled. If rescaleAllowed
 	 * is set to false, the current bin is not checked and the last scaled image
 	 * is always used.
 	 *  
 	 * Do not synchronized this method - it can cause a race condition on linux only.
 	 * 
 	 * @return true if scaledImage created.
 	 */
 
 	private double xOffset;
 	private double yOffset;
 	private org.eclipse.swt.graphics.Rectangle  screenRectangle;
 	
 	private boolean createScaledImage(ImageScaleType rescaleType, final IProgressMonitor monitor) {
 			
 		if (!imageCreationAllowed) return false;
 
 		boolean requireImageGeneration = imageData==null || 
 				                         rescaleType==ImageScaleType.FORCE_REIMAGE || 
 				                         rescaleType==ImageScaleType.REHISTOGRAM; // We know that it is needed
 		
 		// If we just changed downsample scale, we force the update.
 	    // This allows user resizes of the plot area to be picked up
 		// and the larger data size used if it fits.
         if (!requireImageGeneration && rescaleType==ImageScaleType.REIMAGE_ALLOWED && currentDownSampleBin>0) {
         	if (getDownsampleBin()!=currentDownSampleBin) {
         		requireImageGeneration = true;
         	}
         }
 
 		final XYRegionGraph graph  = (XYRegionGraph)getXAxis().getParent();
 		final Rectangle     rbounds = graph.getRegionArea().getBounds();
 		if (rbounds.width<1 || rbounds.height<1) return false;
 
 		if (!imageCreationAllowed) return false;
 		if (monitor!=null && monitor.isCanceled()) return false;
 
 		if (requireImageGeneration) {
 			try {
 				imageCreationAllowed = false;
 				if (image==null) return false;
 				AbstractDataset reducedFullImage = getDownsampled(image);
 
 				imageServiceBean.setImage(reducedFullImage);
 				imageServiceBean.setMonitor(monitor);
 				if (fullMask!=null) {
 					// For masks, we preserve the min (the falses) to avoid loosing fine lines
 					// which are masked.
 					imageServiceBean.setMask(getDownsampled(fullMask, DownsampleMode.MINIMUM));
 				} else {
 					imageServiceBean.setMask(null); // Ensure we lose the mask!
 				}
 				
 				if (rescaleType==ImageScaleType.REHISTOGRAM) { // Avoids changing colouring to 
 					                                           // max and min of new selection.
 					AbstractDataset  slice     = slice(getYAxis().getRange(), getXAxis().getRange(), (AbstractDataset)getData());
 					ImageServiceBean histoBean = imageServiceBean.clone();
 					histoBean.setImage(slice);
 					if (fullMask!=null) histoBean.setMask(slice(getYAxis().getRange(), getXAxis().getRange(), fullMask));
 					float[] fa = service.getFastStatistics(histoBean);
 					setMin(fa[0]);
 					setMax(fa[1]);
 
 				}
 								
 				this.imageData   = service.getImageData(imageServiceBean);
 				
 				try {
 					ImageServiceBean intensityScaleBean = imageServiceBean.clone();
 					intensityScaleBean.setOrigin(ImageOrigin.TOP_LEFT);
 					// We send the image drawn with the same palette to the 
 					// intensityScale
 					// TODO FIXME This will not work in log mode
 					final DoubleDataset dds = new DoubleDataset(256,1);
 					double max = getMax().doubleValue();
 					double inc = (max - getMin().doubleValue())/256d;
 					for (int i = 0; i < 256; i++) {
 						dds.set(max - (i*inc), i, 0);
 					}
 					intensityScaleBean.setImage(dds);
 					intensityScaleBean.setMask(null);
 					intensityScale.setImageData(service.getImageData(intensityScaleBean));
 					intensityScale.setLog10(getImageServiceBean().isLogColorScale());
 				} catch (Throwable ne) {
 					logger.warn("Cannot update intensity!");
 				}
 
 			} catch (Exception e) {
 				logger.error("Cannot create image from data!", e);
 			} finally {
 				imageCreationAllowed = true;
 			}
 			
 		}
 		
 		if (monitor!=null && monitor.isCanceled()) return false;
 		
 		try {
 			
 			isMaximumZoom = false;
 			isLabelZoom   = false;
 			if (imageData!=null && imageData.width==bounds.width && imageData.height==bounds.height) { 
 				// No slice, faster
 				if (monitor!=null && monitor.isCanceled()) return false;
 				if (scaledImage!=null &&!scaledImage.isDisposed()) scaledImage.dispose(); // IMPORTANT
 				scaledImage  = new Image(Display.getDefault(), imageData);
 			} else {
 				// slice data to get current zoom area
 				/**     
 				 *      x1,y1--------------x2,y2
 				 *        |                  |
 				 *        |                  |
 				 *        |                  |
 				 *      x3,y3--------------x4,y4
 				 */
 				ImageData data = imageData;
 				ImageOrigin origin = getImageOrigin();
 				
 				Range xRange = xAxis.getRange();
 				Range yRange = yAxis.getRange();
 				
 				double minX = xRange.getLower()/currentDownSampleBin;
 				double minY = yRange.getLower()/currentDownSampleBin;
 				double maxX = xRange.getUpper()/currentDownSampleBin;
 				double maxY = yRange.getUpper()/currentDownSampleBin;
 				int xSize = imageData.width;
 				int ySize = imageData.height;
 				
 				// check as getLower and getUpper don't work as expected
 				if(maxX < minX){
 					double temp = maxX;
 					maxX = minX;
 					minX = temp;
 				}
 				if(maxY < minY){
 					double temp = maxY;
 					maxY = minY;
 					minY = temp;
 				}
 				
 				double xSpread = maxX - minX;
 				double ySpread = maxY - minY;
 				
 				double xScale = rbounds.width / xSpread;
 				double yScale = rbounds.height / ySpread;
 //				System.err.println("Area is " + rbounds + " with scale (x,y) " + xScale + ", " + yScale);
 				
 				// Deliberately get the over-sized dimensions so that the edge pixels can be smoothly panned through.
 				int minXI = (int) Math.floor(minX);
 				int minYI = (int) Math.floor(minY);
 				
 				int maxXI = (int) Math.ceil(maxX);
 				int maxYI = (int) Math.ceil(maxY);
 				
 				int fullWidth = (int) (maxXI-minXI);
 				int fullHeight = (int) (maxYI-minYI);
 				
 				// Force a minimum size on the system
 				if (fullWidth <= MINIMUM_ZOOM_SIZE) {
 					if (fullWidth > imageData.width) fullWidth = MINIMUM_ZOOM_SIZE;
 					isMaximumZoom = true;
 				}
 				if (fullHeight <= MINIMUM_ZOOM_SIZE) {
 					if (fullHeight > imageData.height) fullHeight = MINIMUM_ZOOM_SIZE;
 					isMaximumZoom = true;
 				}
 				if (fullWidth <= MINIMUM_LABEL_SIZE && fullHeight <= MINIMUM_LABEL_SIZE) {
 					isLabelZoom = true;
 				}
 				
 				int scaleWidth = (int) (fullWidth*xScale);
 				int scaleHeight = (int) (fullHeight*yScale);
 //				System.err.println("Scaling to " + scaleWidth + "x" + scaleHeight);
 				int xPix = (int)minX;
 				int yPix = (int)minY;
 				
 				double xPixD = 0;
 				double yPixD = 0;
 				
 				// These offsets are used when the scaled images is drawn to the screen.
 				xOffset = (minX - Math.floor(minX))*xScale;
 				yOffset = (minY - Math.floor(minY))*yScale;
 				// Deal with the origin orientations correctly.
 				switch (origin) {
 				case TOP_LEFT:
 					break;
 				case TOP_RIGHT:
 					xPixD = xSize-maxX;
 					xPix = (int) Math.floor(xPixD);
 					xOffset = (xPixD - xPix)*xScale;
 					break;
 				case BOTTOM_RIGHT:
 					xPixD = xSize-maxX;
 					xPix = (int) Math.floor(xPixD);
 					xOffset = (xPixD - xPix)*xScale;
 					yPixD = ySize-maxY;
 					yPix = (int) Math.floor(yPixD);
 					yOffset = (yPixD - yPix)*yScale;
 					break;
 				case BOTTOM_LEFT:
 					yPixD = ySize-maxY;
 					yPix = (int) Math.floor(yPixD);
 					yOffset = (yPixD - yPix)*yScale;
 					break;
 				}
 				if (yPix+fullHeight > ySize) {
 					return false; // prevent IAE in calling getPixel
 				}
 				if (xPix+fullWidth > xSize) {
 					return false;
 				}
 				// Slice the data.
 				// Pixel slice on downsampled data = fast!
 				if (imageData.depth <= 8) {
 					// NOTE Assumes 8-bit images
 					final int size   = fullWidth*fullHeight;
 					final byte[] pixels = new byte[size];
 					for (int y = 0; y < fullHeight; y++) {
 						imageData.getPixels(xPix, yPix+y, fullWidth, pixels, fullWidth*y);
 					}
 					data = new ImageData(fullWidth, fullHeight, data.depth, getPaletteData(), 1, pixels);
 				} else {
 					// NOTE Assumes 24 Bit Images
 					final int[] pixels = new int[fullWidth];
 					
 					data = new ImageData(fullWidth, fullHeight, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));
 					for (int y = 0; y < fullHeight; y++) {					
 						imageData.getPixels(xPix, yPix+y, fullWidth, pixels, 0);
 						data.setPixels(0, y, fullWidth, pixels, 0);
 					}
 				}
 				// create the scaled image
 				// We are suspicious if the algorithm wants to create an image
 				// bigger than the screen size and in that case do not scale
 				// Fix to http://jira.diamond.ac.uk/browse/SCI-926
 				boolean proceedWithScale = true;
 				try {
 					if (screenRectangle == null) {
 						screenRectangle = Display.getCurrent().getPrimaryMonitor().getClientArea();
 					}
 					if (scaleWidth>screenRectangle.width*2      || 
 						scaleHeight>screenRectangle.height*2) {
 						
 						logger.error("Image scaling algorithm has malfunctioned and asked for an image bigger than the screen!");
 						logger.debug("scaleWidth="+scaleWidth);
 						logger.debug("scaleHeight="+scaleHeight);
 						proceedWithScale = false;
 					}
 				} catch (Throwable ne) {
 					proceedWithScale = true;
 				}
 				
 				if (proceedWithScale) {
 				    data = data!=null ? data.scaledTo(scaleWidth, scaleHeight) : null;
 					if (scaledImage!=null &&!scaledImage.isDisposed()) scaledImage.dispose(); // IMPORTANT
 					scaledImage = data!=null ? new Image(Display.getDefault(), data) : null;
 				} else if (scaledImage==null) {
 					scaledImage = data!=null ? new Image(Display.getDefault(), data) : null;
 				}
 				
 			}
 
 			return true;
 		} catch (IllegalArgumentException ie) {
 			logger.error(ie.toString());
 			return false;
 		} catch (java.lang.NegativeArraySizeException allowed) {
 			return false;
 			
 		} catch (NullPointerException ne) {
 			throw ne;
 		} catch (Throwable ne) {
 			logger.error("Image scale error!", ne);
 			return false;
 		}
 	}
 
 	private static final int[] getBounds(Range xr, Range yr) {
 		return new int[] {(int) Math.floor(xr.getLower()), (int) Math.floor(yr.getLower()),
 				(int) Math.ceil(xr.getUpper()), (int) Math.ceil(yr.getUpper())};
 	}
 
 	private Map<Integer, Reference<Object>> mipMap;
 	private Map<Integer, Reference<Object>> maskMap;
 	private Collection<IDownSampleListener> downsampleListeners;
 	
 	private AbstractDataset getDownsampled(AbstractDataset image) {
 	
 		return getDownsampled(image, getDownsampleTypeDiamond());
  	}
 	
 	/**
 	 * Uses caches based on bin, not DownsampleMode.
 	 * @param image
 	 * @param mode
 	 * @return
 	 */
 	private AbstractDataset getDownsampled(AbstractDataset image, DownsampleMode mode) {
 		
 		// Down sample, no point histogramming the whole thing
         final int bin = getDownsampleBin();
         
         boolean newBin = false;
         if (currentDownSampleBin!=bin) newBin = true;
         
         try {
 	        this.currentDownSampleBin = bin;
 			if (bin==1) {
 		        logger.trace("No downsample bin (or bin=1)");
 				return image; // nothing to downsample
 			}
 			
 			if (image.getDtype()!=AbstractDataset.BOOL) {
 				if (mipMap!=null && mipMap.containsKey(bin) && mipMap.get(bin).get()!=null) {
 			        logger.trace("Downsample bin used, "+bin);
 					return (AbstractDataset)mipMap.get(bin).get();
 				}
 			} else {
 				if (maskMap!=null && maskMap.containsKey(bin) && maskMap.get(bin).get()!=null) {
 			        logger.trace("Downsample mask bin used, "+bin);
 					return (AbstractDataset)maskMap.get(bin).get();
 				}
 			}
 			
 			final Downsample downSampler = new Downsample(mode, new int[]{bin,bin});
 			List<AbstractDataset>   sets = downSampler.value(image);
 			final AbstractDataset set = sets.get(0);
 			
 			if (image.getDtype()!=AbstractDataset.BOOL) {
 				if (mipMap==null) mipMap = new HashMap<Integer,Reference<Object>>(3);
 				mipMap.put(bin, new SoftReference<Object>(set));
 		        logger.trace("Downsample bin created, "+bin);
 			} else {
 				if (maskMap==null) maskMap = new HashMap<Integer,Reference<Object>>(3);
 				maskMap.put(bin, new SoftReference<Object>(set));
 		        logger.trace("Downsample mask bin created, "+bin);
 			}
 	      
 			return set;
 			
         } finally {
         	if (newBin) { // We fire a downsample event.
         		fireDownsampleListeners(new DownSampleEvent(this, bin));
         	}
         }
 	}
 	
 	protected void fireDownsampleListeners(DownSampleEvent evt) {
 		if (downsampleListeners==null) return;
 		for (IDownSampleListener l : downsampleListeners) l.downSampleChanged(evt);
 	}
 
 	@Override
 	public int getBin() {
 		return currentDownSampleBin;
 	}
 	
 	/**
 	 * Add listener to be notifed if the dawnsampling changes.
 	 * @param l
 	 */
 	@Override
 	public void addDownsampleListener(IDownSampleListener l) {
 		if (downsampleListeners==null) downsampleListeners = new HashSet<IDownSampleListener>(7);
 		downsampleListeners.add(l);
 	}
 	
 	/**
 	 * Remove listener so that it is not notified.
 	 * @param l
 	 */
 	@Override
 	public void removeDownsampleListener(IDownSampleListener l) {
 		if (downsampleListeners==null) return;
 		downsampleListeners.remove(l);
 	}
 	
 	@Override
 	public AbstractDataset getDownsampled() {
 		return getDownsampled(getImage());
 	}
 	
 	public AbstractDataset getDownsampledMask() {
 		if (getMask()==null) return null;
 		return getDownsampled(getMask(), DownsampleMode.MINIMUM);
 	}
 
 	/**
 	 * Returns the bin for downsampling, either 1,2,4 or 8 currently.
 	 * This gives a pixel count of 1,4,16 or 64 for the bin. If 1 no
 	 * binning at all is done and no downsampling is being done, getDownsampled()
 	 * will return the AbstractDataset ok even if bin is one (no downsampling).
 	 * 
 	 * @param slice
 	 * @param bounds
 	 * @return
 	 */
 	public int getDownsampleBin() {
 		
 		final XYRegionGraph graph      = (XYRegionGraph)getXAxis().getParent();
 		final Rectangle     realBounds = graph.getRegionArea().getBounds();
 		
 		double rwidth  = getSpan(getXAxis());
 		double rheight = getSpan(getYAxis());
  
 		int iwidth  = realBounds.width;
 		int iheight = realBounds.height;
 
 		if (iwidth>(rwidth/2d) || iheight>(rheight/2d)) {
 			return 1;
 		}
 
 		if (iwidth>(rwidth/4d) || iheight>(rheight/4d)) {
 			return 2;
 		}
 
 		if (iwidth>(rwidth/8d) || iheight>(rheight/8d)) {
 			return 4;
 		}
 		return 8;
 	}
 
 	private double getSpan(Axis axis) {
 		final Range range = axis.getRange();
 		return Math.max(range.getUpper(),range.getLower()) - Math.min(range.getUpper(), range.getLower());
 	}
 
 	private boolean lastAspectRatio = true;
 	private IntensityLabelPainter intensityLabelPainter;
 	@Override
 	protected void paintFigure(Graphics graphics) {
 		
 		super.paintFigure(graphics);
 
 		/**
 		 * This is not actually needed except that when there
 		 * are a number of opens of an image, e.g. when moving
 		 * around an h5 gallery with arrow keys, it looks smooth 
 		 * with this in.
 		 */
 		if (scaledImage==null || !isKeepAspectRatio() || lastAspectRatio!=isKeepAspectRatio()) {
 			boolean imageReady = createScaledImage(ImageScaleType.NO_REIMAGE, null);
 			if (!imageReady) {
 				return;
 			}
 			lastAspectRatio = isKeepAspectRatio();
 		}
 
 		graphics.pushState();	
 		final XYRegionGraph graph  = (XYRegionGraph)xAxis.getParent();
 		final Point         loc    = graph.getRegionArea().getLocation();
 		
 		// Offsets and scaled image are calculated in the createScaledImage method.
 		graphics.drawImage(scaledImage, loc.x-((int)xOffset), loc.y-((int)yOffset));
 		
 		if (isLabelZoom && scaledImage!=null) {
 			if (intensityLabelPainter==null) intensityLabelPainter = new IntensityLabelPainter(plottingSystem, this);
 			intensityLabelPainter.paintIntensityLabels(graphics);
 		}
 
 		graphics.popState();
 	}
 
 
 	private boolean isKeepAspectRatio() {
 		return getXAxis().isKeepAspect() && getYAxis().isKeepAspect();
 	}
 	
 //	public void removeNotify() {
 //        super.removeNotify();
 //        remove();
 //	}
 	
 	public void remove() {
 		
 		if (mipMap!=null)           mipMap.clear();
 		if (maskMap!=null)          maskMap.clear();
 		if (scaledImage!=null)      scaledImage.dispose();
 		
 		if (paletteListeners!=null) paletteListeners.clear();
 		paletteListeners = null;
 		if (downsampleListeners!=null) downsampleListeners.clear();
 		downsampleListeners = null;
 		
         clearAspect(xAxis);
         clearAspect(yAxis);
         
 		if (getParent()!=null) getParent().remove(this);
 		xAxis.removeListener(this);
 		yAxis.removeListener(this);
 		xAxis.setTicksAtEnds(xTicksAtEnd);
 		yAxis.setTicksAtEnds(yTicksAtEnd);
 		xAxis.setTicksIndexBased(false);
 		yAxis.setTicksIndexBased(false);
 		axisRedrawActive = false;
 		if (imageServiceBean!=null) imageServiceBean.dispose();
 		
 		this.imageServiceBean = null;
 		this.service          = null;
 		this.intensityScale   = null;
 		this.image            = null;
 		this.rgbDataset       = null;
 	}
 	
 	public void dispose() {
 		remove();
 	}
 
 	private void clearAspect(Axis axis) {
         if (axis instanceof AspectAxis ) {			
 			AspectAxis aaxis = (AspectAxis)axis;
 			aaxis.setKeepAspectWith(null);
 			aaxis.setMaximumRange(null);
 		}
 	}
 
 	@Override
 	public IDataset getData() {
 		return image;
 	}
 	
 	@Override
 	public IDataset getRGBData() {
 		return rgbDataset;
 	}
 
 
 	/**
 	 * Create a slice of data from given ranges
 	 * @param xr
 	 * @param yr
 	 * @return
 	 */
 	private final AbstractDataset slice(Range xr, Range yr, final AbstractDataset data) {
 		
 		// Check that a slice needed, this speeds up the initial show of the image.
 		final int[] shape = data.getShape();
 		final int[] imageRanges = getImageBounds(shape, getImageOrigin());
 		final int[] bounds = getBounds(xr, yr);
 		if (imageRanges!=null && Arrays.equals(imageRanges, bounds)) {
 			return data;
 		}
 		
 		int[] xRange = getRange(bounds, shape[0], 0, false);
 		int[] yRange = getRange(bounds, shape[1], 1, false);		
 
 		try {
 			return data.getSlice(new int[]{xRange[0],yRange[0]}, new int[]{xRange[1],yRange[1]}, null);
 			
 		} catch (IllegalArgumentException iae) {
 			logger.error("Cannot slice image", iae);
 			return data;
 		}
 	}
 
 	private static final int[] getRange(int[] bounds, int side, int index, boolean inverted) {
 		int start = bounds[index];
 		if (inverted) start = side-start;
 		
 		int stop  = bounds[2+index];
 		if (inverted) stop = side-stop;
 
 		if (start>stop) {
 			start = bounds[2+index];
 			if (inverted) start = side-start;
 			
 			stop  = bounds[index];
 			if (inverted) stop = side-stop;
 		}
 		
 		return new int[]{start, stop};
 	}
 
 	private boolean axisRedrawActive = true;
 
 	@Override
 	public void axisRangeChanged(Axis axis, Range old_range, Range new_range) {
 		createScaledImage(ImageScaleType.REIMAGE_ALLOWED, null);
 	}
 
 	/**
 	 * We do a bit here to ensure that 
 	 * not too many calls to createScaledImage(...) are made.
 	 */
 	@Override
 	public void axisRevalidated(Axis axis) {
 		if (axis.isYAxis()) updateAxisRange(axis);
 	}
 	
 	private void updateAxisRange(Axis axis) {
 		if (!axisRedrawActive) return;				
 		createScaledImage(ImageScaleType.REIMAGE_ALLOWED, null);
 	}
 
 
 	
 	private void setAxisRedrawActive(boolean b) {
 		this.axisRedrawActive = b;
 	}
 
 
 	public void performAutoscale() {
 		final int[] shape = image.getShape();
 		switch(getImageOrigin()) {
 		case TOP_LEFT:
 			xAxis.setRange(0, shape[1]);
 			yAxis.setRange(shape[0], 0);	
 			break;
 			
 		case BOTTOM_LEFT:
 			xAxis.setRange(0, shape[0]);
 			yAxis.setRange(0, shape[1]);		
 			break;
 
 		case BOTTOM_RIGHT:
 			xAxis.setRange(shape[1], 0);
 			yAxis.setRange(0, shape[0]);		
 			break;
 
 		case TOP_RIGHT:
 			xAxis.setRange(shape[0], 0);
 			yAxis.setRange(shape[1], 0);		
 			break;
 		
 		}
 	}
 	
 	private static final int[] getImageBounds(int[] shape, ImageOrigin origin) {
 		if (origin==null) origin = ImageOrigin.TOP_LEFT; 
 		switch (origin) {
 		case TOP_LEFT:
 			return new int[] {0, shape[0], shape[1], 0};
 		case BOTTOM_LEFT:
 			return new int[] {0, 0, shape[0], shape[1]};
 		case BOTTOM_RIGHT:
 			return new int[] {shape[1], 0, 0, shape[0]};
 		case TOP_RIGHT:
 			return new int[] {shape[0], shape[1], 0, 0};
 		}
 		return null;
 	}
 
 	public void setImageOrigin(ImageOrigin imageOrigin) {
 		if (this.mipMap!=null) mipMap.clear();
 		imageServiceBean.setOrigin(imageOrigin);
 		createAxisBounds();
 		performAutoscale();
 		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
 		repaint();
 		fireImageOriginListeners();
 	}
 
 
 	/**
 	 * Creates new axis bounds, updates the label data set
 	 */
 	private void createAxisBounds() {
 		final int[] shape = image.getShape();
 		if (getImageOrigin()==ImageOrigin.TOP_LEFT || getImageOrigin()==ImageOrigin.BOTTOM_RIGHT) {
 			setupAxis(getXAxis(), new Range(0,shape[1]), axes!=null&&axes.size()>0 ? axes.get(0) : null);
 			setupAxis(getYAxis(), new Range(0,shape[0]), axes!=null&&axes.size()>1 ? axes.get(1) : null);
 		} else {
 			setupAxis(getXAxis(), new Range(0,shape[0]), axes!=null&&axes.size()>1 ? axes.get(1) : null);
 			setupAxis(getYAxis(), new Range(0,shape[1]), axes!=null&&axes.size()>0 ? axes.get(0) : null);
 		}
 	}
 	
 	private void setupAxis(Axis axis, Range bounds, IDataset labels) {
 		((AspectAxis)axis).setMaximumRange(bounds);
 		((AspectAxis)axis).setLabelDataAndTitle(labels);
 	}
 
 	@Override
 	public ImageOrigin getImageOrigin() {
 		if (imageServiceBean==null) return ImageOrigin.TOP_LEFT;
 		return imageServiceBean.getOrigin();
 	}
 	
 	
 	private boolean rescaleHistogram = true;
 	
 	public boolean isRescaleHistogram() {
 		return rescaleHistogram;
 	}
 
 	@Override
 	public void setRescaleHistogram(boolean rescaleHistogram) {
 		this.rescaleHistogram = rescaleHistogram;
 	}
 
 	private RGBDataset rgbDataset;
 	@Override
 	public boolean setData(IDataset image, List<? extends IDataset> axes, boolean performAuto) {
 		
 		if (getPreferenceStore().getBoolean(PlottingConstants.IGNORE_RGB) && image instanceof RGBDataset) {
 			RGBDataset rgb = (RGBDataset)image;
 			image = getSum(rgb);
 			rgbDataset = rgb;
 		} else {
 			rgbDataset = null;
 		}
 		if (plottingSystem!=null) try {
 			final TraceWillPlotEvent evt = new TraceWillPlotEvent(this, false);
 			evt.setImageData(image, axes);
 			evt.setNewImageDataSet(false);
 			plottingSystem.fireWillPlot(evt);
 			if (!evt.doit) return false;
 			if (evt.isNewImageDataSet()) {
 				image = evt.getImage();
 				axes  = evt.getAxes();
 			}
 		} catch (Throwable ignored) {
 			// We allow things to proceed without a warning.
 		}
 
 		// The image is drawn low y to the top left but the axes are low y to the bottom right
 		// We do not currently reflect it as it takes too long. Instead in the slice
 		// method, we allow for the fact that the dataset is in a different orientation to 
 		// what is plotted.
 		this.image = (AbstractDataset)image;
 		if (this.mipMap!=null)  mipMap.clear();
		if (scaledImage!=null && !scaledImage.isDisposed()) scaledImage.dispose();
 		scaledImage = null;
 		imageData   = null;
 		
 		if (imageServiceBean==null) imageServiceBean = new ImageServiceBean();
 		imageServiceBean.setImage(image);
 		
 		if (service==null) service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 		if (rescaleHistogram) {
 			final float[] fa = service.getFastStatistics(imageServiceBean);
 			setMin(fa[0]);
 			setMax(fa[1]);
 		}
 		
 		setAxes(axes, performAuto);
        
 		if (plottingSystem!=null) try {
 			if (plottingSystem.getTraces().contains(this)) {
 				plottingSystem.fireTraceUpdated(new TraceEvent(this));
 			}
 		} catch (Throwable ignored) {
 			// We allow things to proceed without a warning.
 		}
 
 		return true;
 	}
 	
 	private IDataset getSum(RGBDataset rgb) {
 		
 		final int[]       shape = rgb.getShape();
 		final DoubleDataset sum = new DoubleDataset(shape);
 		
 		// Important to test this algorithm use 
 		// \\dls-science\science\groups\das\ExampleData\large test files\Galaxy.tif
 		// Then switch on and off RGB rendering. Previously, position iterator was
 		// use but now only the absolute values seem to loop properly and give the
 		// correct image in the sum.
 		for (int i = 0; i < rgb.getSize(); i++) {
 			try {
 				sum.setAbs(i, rgb.getRedAbs(i)+rgb.getBlueAbs(i)+rgb.getGreenAbs(i));
 			} catch (ArrayIndexOutOfBoundsException ignored) {
 				break;
 			}
 		}
 		return sum;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setAxes(List<? extends IDataset> axes, boolean performAuto) {
 		this.axes  = (List<IDataset>) axes;
 		createAxisBounds();
 		
 		if (axes==null) {
 			getXAxis().setTitle("");
 			getYAxis().setTitle("");
 		} else if (axes.get(0)==null) {
 			getXAxis().setTitle("");
 		} else if (axes.get(1)==null) {
 			getYAxis().setTitle("");
 		}
 		if (performAuto) {
 	 		try {
 				setAxisRedrawActive(false);
 				performAutoscale();
 			} finally {
 				setAxisRedrawActive(true);
 			}
 		} else {
 			createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
 			repaint();
 		}
 	}
 
 
 	public Number getMin() {
 		return imageServiceBean.getMin();
 	}
 
 	public void setMin(Number min) {
 		if (imageServiceBean==null) return;
 		imageServiceBean.setMin(min);
 		try {
 			intensityScale.setMin(min.doubleValue());
 		} catch (Exception e) {
 			logger.error("Cannot set scale of intensity!",e);
 		}
 		fireMinDataListeners();
 	}
 
 	public Number getMax() {
 		return imageServiceBean.getMax();
 	}
 	
 	public void setMax(Number max) {
 		if (imageServiceBean==null) return;
 		imageServiceBean.setMax(max);
 		try {
 			intensityScale.setMax(max.doubleValue());
 		} catch (Exception e) {
 			logger.error("Cannot set scale of intensity!",e);
 		}
 		fireMaxDataListeners();
 	}
 
 	@Override
 	public ImageServiceBean getImageServiceBean() {
 		return imageServiceBean;
 	}
 
 	private Collection<IPaletteListener> paletteListeners;
 
 
 	@Override
 	public void addPaletteListener(IPaletteListener pl) {
 		if (paletteListeners==null) paletteListeners = new HashSet<IPaletteListener>(11);
 		paletteListeners.add(pl);
 	}
 
 	@Override
 	public void removePaletteListener(IPaletteListener pl) {
 		if (paletteListeners==null) return;
 		paletteListeners.remove(pl);
 	}
 	
 	
 	private void firePaletteDataListeners(PaletteData paletteData) {
 		if (paletteListeners==null) return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData()); // Important do not let Mark get at it :)
 		for (IPaletteListener pl : paletteListeners) pl.paletteChanged(evt);
 	}
 	private void fireMinDataListeners() {
 		if (paletteListeners==null) return;
 		if (!imageCreationAllowed)  return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
 		for (IPaletteListener pl : paletteListeners) pl.minChanged(evt);
 	}
 	private void fireMaxDataListeners() {
 		if (paletteListeners==null) return;
 		if (!imageCreationAllowed)  return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
 		for (IPaletteListener pl : paletteListeners) pl.maxChanged(evt);
 	}
 	private void fireMaxCutListeners() {
 		if (paletteListeners==null) return;
 		if (!imageCreationAllowed)  return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
 		for (IPaletteListener pl : paletteListeners) pl.maxCutChanged(evt);
 	}
 	private void fireMinCutListeners() {
 		if (paletteListeners==null) return;
 		if (!imageCreationAllowed)  return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
 		for (IPaletteListener pl : paletteListeners) pl.minCutChanged(evt);
 	}
 	private void fireNanBoundsListeners() {
 		if (paletteListeners==null) return;
 		if (!imageCreationAllowed)  return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
 		for (IPaletteListener pl : paletteListeners) pl.nanBoundsChanged(evt);
 	}
 	private void fireMaskListeners() {
 		if (paletteListeners==null) return;
 		if (!imageCreationAllowed)  return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
 		for (IPaletteListener pl : paletteListeners) pl.maskChanged(evt);
 	}
 	private void fireImageOriginListeners() {
 		if (paletteListeners==null) return;
 		if (!imageCreationAllowed)  return;
 		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
 		for (IPaletteListener pl : paletteListeners) pl.imageOriginChanged(evt);
 	}
 	
 	@Override
 	public DownsampleType getDownsampleType() {
 		return downsampleType;
 	}
 	
 	@Override
 	public void setDownsampleType(DownsampleType type) {
 		if (this.mipMap!=null)  mipMap.clear();
 		if (this.maskMap!=null) maskMap.clear();
 		this.downsampleType = type;
 		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
 		getPreferenceStore().setValue(BasePlottingConstants.DOWNSAMPLE_PREF, type.getLabel());
 		repaint();
 	}
 
 	private DownsampleMode getDownsampleTypeDiamond() {
 		switch(getDownsampleType()) {
 		case MEAN:
 			return DownsampleMode.MEAN;
 		case MAXIMUM:
 			return DownsampleMode.MAXIMUM;
 		case MINIMUM:
 			return DownsampleMode.MINIMUM;
 		case POINT:
 			return DownsampleMode.POINT;
 		}
 		return DownsampleMode.MEAN;
 	}
 
 	@Override
 	public void rehistogram() {
 		if (imageServiceBean==null) return;
 		imageServiceBean.setMax(null);
 		imageServiceBean.setMin(null);
 		createScaledImage(ImageScaleType.REHISTOGRAM, null);
 		// Max and min changed in all likely-hood
 		fireMaxDataListeners();
 		fireMinDataListeners();
 		repaint();
 	}
 	
 	protected void remask() {
 		if (imageServiceBean==null) return;
 		
 		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
 
 		// Max and min changed in all likely-hood
 		fireMaskListeners();
 		repaint();
 	}
 
 	
 	@Override
 	public List<IDataset> getAxes() {
 		return (List<IDataset>) axes;
 	}
 
 	/**
 	 * return the HistoType being used
 	 * @return
 	 */
 	@Override
 	public HistoType getHistoType() {
 		if (imageServiceBean==null) return null;
 		return imageServiceBean.getHistogramType();
 	}
 	
 	/**
 	 * Sets the histo type.
 	 */
 	@Override
 	public boolean setHistoType(HistoType type) {
 		if (imageServiceBean==null) return false;
 		imageServiceBean.setHistogramType(type);
 		getPreferenceStore().setValue(BasePlottingConstants.HISTO_PREF, type.getLabel());
 		boolean histoOk = createScaledImage(ImageScaleType.REHISTOGRAM, null);
 		repaint();
 		return histoOk;
 	}
 
 	@Override
 	public ITrace getTrace() {
 		return this;
 	}
 
 	@Override
 	public void setTrace(ITrace trace) {
 		// Does nothing, you cannot change the trace, this is the trace.
 	}
 	
 	public void setImageUpdateActive(boolean active) {
 		this.imageCreationAllowed = active;
 		if (active) {
 			createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
 			repaint();
 		}
 		firePaletteDataListeners(getPaletteData());
 	}
 
 	@Override
 	public HistogramBound getMinCut() {
 		return imageServiceBean.getMinimumCutBound();
 	}
 
 	@Override
 	public void setMinCut(HistogramBound bound) {
 		
 		storeBound(bound, BasePlottingConstants.MIN_CUT);
 		if (imageServiceBean==null) return;
 		imageServiceBean.setMinimumCutBound(bound);
 		fireMinCutListeners();
 	}
 
 	private void storeBound(HistogramBound bound, String prop) {
 		if (bound!=null) {
 			getPreferenceStore().setValue(prop, bound.toString());
 		} else {
 			getPreferenceStore().setValue(prop, "");
 		}
 	}
 
 	@Override
 	public HistogramBound getMaxCut() {
 		return imageServiceBean.getMaximumCutBound();
 	}
 
 	@Override
 	public void setMaxCut(HistogramBound bound) {
 		storeBound(bound, BasePlottingConstants.MAX_CUT);
 		if (imageServiceBean==null) return;
 		imageServiceBean.setMaximumCutBound(bound);
 		fireMaxCutListeners();
 	}
 
 	@Override
 	public HistogramBound getNanBound() {
 		return imageServiceBean.getNanBound();
 	}
 
 	@Override
 	public void setNanBound(HistogramBound bound) {
 		storeBound(bound, BasePlottingConstants.NAN_CUT);
 		if (imageServiceBean==null) return;
 		imageServiceBean.setNanBound(bound);
 		fireNanBoundsListeners();
 	}
 	
     private AbstractDataset fullMask;
 	/**
 	 * The masking dataset of there is one, normally null.
 	 * @return
 	 */
 	public AbstractDataset getMask() {
 		return fullMask;
 	}
 	
 	/**
 	 * 
 	 * @param bd
 	 */
 	public void setMask(IDataset mask) {
 		
 		if (mask!=null && image!=null && !image.isCompatibleWith(mask)) {
 			
 			BooleanDataset maskDataset = new BooleanDataset(image.getShape());
 			maskDataset.setName("mask");
 			maskDataset.fill(true);
 
 			final int[] shape = mask.getShape();
 			for (int y = 0; y<shape[0]; ++y) {
 				for (int x = 0; x<shape[1]; ++x) {
 			        try {
 			        	// We only add the falses 
 			        	if (!mask.getBoolean(y,x)) {
 			        		maskDataset.set(Boolean.FALSE, y,x);
 			        	}
 			        } catch (Throwable ignored) {
 			        	continue;
 			        }
 				}
 			}
 
 			mask = maskDataset;
 		}
 		if (maskMap!=null) maskMap.clear();
 		fullMask = (AbstractDataset)mask;
 		remask();
 	}
 
 	private boolean userTrace = true;
 	@Override
 	public boolean isUserTrace() {
 		return userTrace;
 	}
 
 	@Override
 	public void setUserTrace(boolean isUserTrace) {
 		this.userTrace = isUserTrace;
 	}
 
 	public boolean isMaximumZoom() {
 		return isMaximumZoom;
 	}
 	
 	private Object userObject;
 
 	public Object getUserObject() {
 		return userObject;
 	}
 
 	public void setUserObject(Object userObject) {
 		this.userObject = userObject;
 	}
 	
 	/**
 	 * If the axis data set has been set, this method will return 
 	 * a selection region in the coordinates of the axes labels rather
 	 * than the indices.
 	 * 
 	 * Ellipse and Sector rois are not currently supported.
 	 * 
 	 * @return ROI in label coordinates. This roi is not that useful after it
 	 *         is created. The data processing needs rois with indices.
 	 */
 	@Override
 	public IROI getRegionInAxisCoordinates(final IROI roi) throws Exception {
 		
 		if (!TraceUtils.isCustomAxes(this)) return roi;
 		
 		final IDataset xl = axes.get(0); // May be null
 		final IDataset yl = axes.get(1); // May be null
 		
 		if (roi instanceof LinearROI) {
 			double[] sp = ((LinearROI)roi).getPoint();
 			double[] ep = ((LinearROI)roi).getEndPoint();
 			TraceUtils.transform(xl,0,sp,ep);
 			TraceUtils.transform(yl,1,sp,ep);
 			return new LinearROI(sp, ep);
 			
 		} else if (roi instanceof PolylineROI) {
 			PolylineROI proi = (PolylineROI)roi;
 			final PolylineROI ret = (proi instanceof PolygonalROI) ? new PolygonalROI() : new PolylineROI();
 			for (PointROI pointROI : proi) {
 				double[] dp = pointROI.getPoint();
 				TraceUtils.transform(xl,0,dp);
 				TraceUtils.transform(yl,1,dp);
 				ret.insertPoint(dp);
 			}
 			
 		} else if (roi instanceof PointROI) {
 			double[] dp = ((PointROI)roi).getPoint();
 			TraceUtils.transform(xl,0,dp);
 			TraceUtils.transform(yl,1,dp);
 			return new PointROI(dp);
 			
 		} else if (roi instanceof RectangularROI) {
 			RectangularROI rroi = (RectangularROI)roi;
 			double[] sp=roi.getPoint();
 			double[] ep=rroi.getEndPoint();
 			TraceUtils.transform(xl,0,sp,ep);
 			TraceUtils.transform(yl,1,sp,ep);
 				
 			return new RectangularROI(sp[0], sp[1], ep[0]-sp[0], sp[1]-ep[1], rroi.getAngle());
 						
 		} else {
 			throw new Exception("Unsupported roi "+roi.getClass());
 		}
 
 		return roi;
 	}
 	
 	@Override
 	public double[] getPointInAxisCoordinates(final double[] point) throws Exception {
 		if (!TraceUtils.isCustomAxes(this)) return point;
 		
 		final AbstractDataset xl = (AbstractDataset)axes.get(0); // May be null
 		final AbstractDataset yl = (AbstractDataset)axes.get(1); // May be null
 		
 		final double[] ret = point.clone();
 		if (xl!=null && xl.getDtype()==AbstractDataset.INT && xl.getSize()==image.getShape()[1] && xl.getInt(0)==0) {
 			// Axis is index.
 		} else {
 			TraceUtils.transform(xl,0,ret);
 		}
 		
 		if (yl!=null && yl.getDtype()==AbstractDataset.INT && yl.getSize()==image.getShape()[0] && yl.getInt(0)==0) {
 			// Axis is index.
 		} else {
 			TraceUtils.transform(yl,1,ret);
 		}
         return ret;
 	}
 	
 	@Override
 	public double[] getPointInImageCoordinates(final double[] axisLocation) throws Exception {
 		if (!TraceUtils.isCustomAxes(this)) return axisLocation;
 		
 		final AbstractDataset xl = (AbstractDataset)axes.get(0); // May be null
 		final AbstractDataset yl = (AbstractDataset)axes.get(1); // May be null
 		final double xIndex = Double.isNaN(axisLocation[0])
 				            ? Double.NaN
 				            : DatasetUtils.crossings(xl, axisLocation[0]).get(0);
 		final double yIndex = Double.isNaN(axisLocation[1])
 	                        ? Double.NaN
 	            		    : DatasetUtils.crossings(yl, axisLocation[1]).get(0);
         return new double[]{xIndex, yIndex};
 	}
 
 	public IPlottingSystem getPlottingSystem() {
 		return plottingSystem;
 	}
 
 	public void setPlottingSystem(IPlottingSystem plottingSystem) {
 		this.plottingSystem = plottingSystem;
 	}
 	
 	@Override
 	public boolean isActive() {
 		return getParent()!=null;
 	}
 
 	@Override
 	public List<String> getAxesNames() {
         return Arrays.asList(xAxis.getTitle(), yAxis.getTitle());
 	}
 
 	@Override
 	public boolean is3DTrace() {
 		return false;
 	}
 
 	@Override
 	public int getRank() {
 		return 2;
 	}
 
 	public String getDataName() {
 		return dataName;
 	}
 
 	public void setDataName(String dataName) {
 		this.dataName = dataName;
 	}
 	
 }
