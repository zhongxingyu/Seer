 package org.dawb.workbench.plotting.system.swtxy;
 
 import java.lang.ref.Reference;
 import java.lang.ref.SoftReference;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.IAxisListener;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.dawb.common.services.HistogramBound;
 import org.dawb.common.services.IImageService;
 import org.dawb.common.services.ImageServiceBean;
 import org.dawb.common.services.ImageServiceBean.HistoType;
 import org.dawb.common.services.ImageServiceBean.ImageOrigin;
 import org.dawb.common.ui.image.PaletteFactory;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IPaletteListener;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceContainer;
 import org.dawb.common.ui.plot.trace.PaletteEvent;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.PlottingConstants;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 /**
  * A trace which draws an image to the plot.
  * 
  * @author fcp94556
  *
  */
 public class ImageTrace extends Figure implements IImageTrace, IAxisListener, ITraceContainer {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ImageTrace.class);
 
 	private String           name;
 	private Axis             xAxis;
 	private Axis             yAxis;
 	private AbstractDataset  image;
 	private DownsampleType   downsampleType=DownsampleType.MEAN;
 	private int              currentDownSampleBin=-1;
 	private List<AbstractDataset> axes;
 	private ImageServiceBean imageServiceBean;
 	private boolean          isMaximumZoom;
 		
 	public ImageTrace(final String name, 
 			          final Axis xAxis, 
 			          final Axis yAxis) {
 		
 		this.name  = name;
 		this.xAxis = xAxis;		
 		this.yAxis = yAxis;
 
 		this.imageServiceBean = new ImageServiceBean();
 		try {
 			imageServiceBean.setPalette(PaletteFactory.getPalette(Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.P_PALETTE), true));
 		} catch (Exception e) {
 			logger.error("Cannot create palette!", e);
 		}	
 		imageServiceBean.setOrigin(ImageOrigin.forLabel(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.ORIGIN_PREF)));
 		imageServiceBean.setHistogramType(HistoType.forLabel(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.HISTO_PREF)));
 		imageServiceBean.setMinimumCutBound(HistogramBound.fromString(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MIN_CUT)));
 		imageServiceBean.setMaximumCutBound(HistogramBound.fromString(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MAX_CUT)));
 		imageServiceBean.setNanBound(HistogramBound.fromString(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.NAN_CUT)));
 		
 		xAxis.addListener(this);
 		yAxis.addListener(this);
 		
 		if (xAxis instanceof AspectAxis && yAxis instanceof AspectAxis) {
 			
 			AspectAxis x = (AspectAxis)xAxis;
 			AspectAxis y = (AspectAxis)yAxis;
 			x.setKeepAspectWith(y);
 			y.setKeepAspectWith(x);		
 		}
 				
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
 	}
 
 	public AspectAxis getYAxis() {
 		return (AspectAxis)yAxis;
 	}
 
 	public void setYAxis(Axis yAxis) {
 		this.yAxis = yAxis;
 	}
 
 	public AbstractDataset getImage() {
 		return image;
 	}
 
 	public PaletteData getPaletteData() {
 		return imageServiceBean.getPalette();
 	}
 
 	public void setPaletteData(PaletteData paletteData) {
 		imageServiceBean.setPalette(paletteData);
 		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
 		repaint();
 		firePaletteDataListeners(paletteData);
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
 				AbstractDataset reducedFullImage = getDownsampled(image);
 
 				final IImageService service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 
 				imageServiceBean.setImage(reducedFullImage);
 				imageServiceBean.setMonitor(monitor);
 				if (fullMask!=null) {
 					imageServiceBean.setMask(getDownsampled(fullMask));
 				} else {
 					imageServiceBean.setMask(null); // Ensure we loose the mask!
 				}
 				
 				if (rescaleType==ImageScaleType.REHISTOGRAM) { // Avoids changing colouring to 
 					// max and min of new selection.
 					AbstractDataset  slice     = slice(getXAxis().getRange(), getYAxis().getRange(), getData());
 					ImageServiceBean histoBean = new ImageServiceBean(slice, getHistoType());
 					if (fullMask!=null) histoBean.setMask(slice(getXAxis().getRange(), getYAxis().getRange(), fullMask));
 					float[] fa = service.getFastStatistics(histoBean);
 					
 					// Not sure how to deal with this better, but if the bean is logged, then you need to modify the values
 					if (imageServiceBean.isLogColorScale()) {
 						setMin(Math.log10(fa[0]));
 						setMax(Math.log10(fa[1]));
 					} else {
 						setMin(fa[0]);
 						setMax(fa[1]);
 					}
 				}
 				
 				this.imageData   = service.getImageData(imageServiceBean);
 				
 			} catch (Exception e) {
 				logger.error("Cannot create image from data!", e);
 			} finally {
 				imageCreationAllowed = true;
 			}
 			
 		}
 		
 		if (monitor!=null && monitor.isCanceled()) return false;
 		
 		try {
 			
 			isMaximumZoom = false;
 			if (imageData!=null && imageData.width==bounds.width && imageData.height==bounds.height) { 
 				// No slice, faster
 				if (monitor!=null && monitor.isCanceled()) return false;
 				if (this.scaledImage!=null &&!scaledImage.isDisposed()) this.scaledImage.dispose(); // IMPORTANT
 				this.scaledImage  = new Image(Display.getDefault(), imageData);
 
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
 				int[] shape = image.getShape();
 				int[] imageRanges = getImageBounds(shape, origin);
 				int[] axisRanges = getAxisBounds(); 
 				RESCALE: if (!Arrays.equals(axisRanges, imageRanges)) {
 
 					final double x1Rat = getXRatio(true, shape, origin);
 					final double y1Rat = getYRatio(true, shape, origin);
 					final double x4Rat = getXRatio(false, shape, origin);
 					final double y4Rat = getYRatio(false, shape, origin);
 
 					// If scales are not requiring a slice, break
 					if (x1Rat==0d && y1Rat==0d && x4Rat==1d && y4Rat==1d) {
 						break RESCALE;
 					}
 					
 					int x1 = (int)Math.round(imageData.width*x1Rat);
 					int y1 = (int)Math.round(imageData.height*y1Rat);
 					int x4 = (int)Math.round(imageData.width*x4Rat);
 					int y4 = (int)Math.round(imageData.height*y4Rat);
 					
 					if((x4-x1)>=2 && (y4-y1)>=2){
 						// Pixel slice on downsampled data = fast!
 						// NOTE Assumes 8-bit images
 						final int size   = (x4-x1)*(y4-y1);
 						final byte[] pixels = new byte[size];
 						final int wid    = (x4-x1);
 						for (int y = 0; y < (y4-y1); y++) {
 							imageData.getPixels(x1, y1+y, wid, pixels, wid*y);
 						}
 						data = new ImageData((x4-x1), (y4-y1), data.depth, getPaletteData(), 1, pixels);
 					} else {
 						isMaximumZoom = true;
 						// minimum zoomed-in image is 4 pixels, TODO: block axis rescaling
 						final byte[] pixels = new byte[4];
 						for (int y = 0; y < 2; y++) {
 							imageData.getPixels(x1, y1+y, 2, pixels, 2*y);
 						}
 						data = new ImageData(2, 2, data.depth, getPaletteData(), 1, pixels);
 					}
 				}
 				data = data!=null ? data.scaledTo(rbounds.width, rbounds.height) : null;
 				if (this.scaledImage!=null &&!scaledImage.isDisposed()) this.scaledImage.dispose(); // IMPORTANT
 				this.scaledImage = data!=null ? new Image(Display.getDefault(), data) : null;
 			}
 			
 			return true;
 			
 		} catch (Throwable ne) {
 			logger.error("Image scale error!", ne);
 			return false;
 		}
 
 	}
 
 	private int[] getAxisBounds() {
 		final Range xr = getXAxis().getRange();
 		final Range yr = getYAxis().getRange();
 		return getBounds(xr, yr);
 	}
 
 	private static final int[] getBounds(Range xr, Range yr) {
 		return new int[] {(int) Math.floor(xr.getLower()), (int) Math.floor(yr.getLower()),
 				(int) Math.ceil(xr.getUpper()), (int) Math.ceil(yr.getUpper())};
 	}
 
 	private double getXRatio(boolean isTopLeft, final int[] shape, ImageOrigin origin) {
 		double xCoord = isTopLeft ? getXAxis().getRange().getLower() : getXAxis().getRange().getUpper();
 		switch (origin) {
 		case TOP_LEFT:
 			return xCoord/shape[1];
 		case TOP_RIGHT:
 			return (shape[0]-xCoord)/shape[0];
 		case BOTTOM_RIGHT:
 			return (shape[1]-xCoord)/shape[1];
 		case BOTTOM_LEFT:
 			return xCoord/shape[0];
 		}
 		return 0d;
 	}
 
 	private double getYRatio(boolean isTopLeft, final int[] shape, ImageOrigin origin) {
 		double yCoord = isTopLeft ? getYAxis().getRange().getUpper() : getYAxis().getRange().getLower();
 		switch (origin) {
 		case TOP_LEFT:
 			return yCoord/shape[0];
 		case TOP_RIGHT:
 			return yCoord/shape[1];
 		case BOTTOM_RIGHT:
 			return (shape[0]-yCoord)/shape[0];
 		case BOTTOM_LEFT:
 			return (shape[1]-yCoord)/shape[1];
 		}
 		return 0d;
 	}
 
 	private Map<Integer, Reference<Object>> mipMap;
 	private Map<Integer, Reference<Object>> maskMap;
 	
 	private AbstractDataset getDownsampled(AbstractDataset image) {
 		
 		// Down sample, no point histogramming the whole thing
         final int bin = getDownsampleBin();
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
 		
 		final Downsample downSampler = new Downsample(getDownsampleTypeDiamond(), new int[]{bin,bin});
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
 	}
 	
 	@Override
 	public AbstractDataset getDownsampled() {
 		return getDownsampled(getImage());
 	}
 	
 	public AbstractDataset getDownsampledMask() {
 		if (getMask()==null) return null;
 		return getDownsampled(getMask());
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
 			if (!imageReady) return;
 			lastAspectRatio = isKeepAspectRatio();
 		}
 
 		graphics.pushState();	
 		final XYRegionGraph graph  = (XYRegionGraph)xAxis.getParent();
 		final Point         loc    = graph.getRegionArea().getLocation();
 		
 		graphics.drawImage(scaledImage, loc.x, loc.y);
   	    
 		graphics.popState();
 	}
 
 	private boolean isKeepAspectRatio() {
 		return getXAxis().isKeepAspect() && getYAxis().isKeepAspect();
 	}
 
 	public void remove() {
 		
 		if (mipMap!=null)           mipMap.clear();
 		if (maskMap!=null)          maskMap.clear();
 		if (scaledImage!=null)      scaledImage.dispose();
 		if (paletteListeners!=null) paletteListeners.clear();
 		paletteListeners = null;
         clearAspect(xAxis);
         clearAspect(yAxis);
 		if (getParent()!=null) getParent().remove(this);
 		xAxis.removeListener(this);
 		yAxis.removeListener(this);
 		axisRedrawActive = false;
 		imageServiceBean.dispose();
 		if (this.scaledImage!=null && !scaledImage.isDisposed()) scaledImage.dispose();
 		imageServiceBean = null;
 	}
 
 	private void clearAspect(Axis axis) {
         if (axis instanceof AspectAxis ) {			
 			AspectAxis aaxis = (AspectAxis)axis;
 			aaxis.setKeepAspectWith(null);
 			aaxis.setMaximumRange(null);
 		}
 	}
 
 	@Override
 	public AbstractDataset getData() {
 		return image;
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
 		//createScaledImage(true, null);
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
 	
 	private void setupAxis(Axis axis, Range bounds, AbstractDataset labels) {
 		((AspectAxis)axis).setMaximumRange(bounds);
 		((AspectAxis)axis).setLabelData(labels);
		if (labels!=null) ((AspectAxis)axis).setTitle(labels.getName());
 	}
 
 	@Override
 	public ImageOrigin getImageOrigin() {
 		return imageServiceBean.getOrigin();
 	}
 	
 	@Override
 	public void setData(final AbstractDataset image, List<AbstractDataset> axes, boolean performAuto) {
 		// The image is drawn low y to the top left but the axes are low y to the bottom right
 		// We do not currently reflect it as it takes too long. Instead in the slice
 		// method, we allow for the fact that the dataset is in a different orientation to 
 		// what is plotted.
 		this.image = image;
 		if (this.mipMap!=null) mipMap.clear();
 		
 		if (imageServiceBean==null) imageServiceBean = new ImageServiceBean();
 		imageServiceBean.setImage(image);
 		
 		final IImageService service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 		final float[] fa = service.getFastStatistics(imageServiceBean);
 		setMin(fa[0]);
 		setMax(fa[1]);
 		this.axes  = axes;
 		
 		createAxisBounds();
 
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
 	
 	@Override
 	public void setAxes(List<AbstractDataset> axes, boolean performAuto) {
 		this.axes  = axes;
 		createAxisBounds();
 		
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
 		imageServiceBean.setMin(min);
 		fireMinDataListeners();
 	}
 
 	public Number getMax() {
 		return imageServiceBean.getMax();
 	}
 
 	public void setMax(Number max) {
 		imageServiceBean.setMax(max);
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
 
 	@Override
 	public DownsampleType getDownsampleType() {
 		return downsampleType;
 	}
 	
 	@Override
 	public void setDownsampleType(DownsampleType type) {
 		if (this.mipMap!=null) mipMap.clear();
 		this.downsampleType = type;
 		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
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
 		imageServiceBean.setMax(null);
 		imageServiceBean.setMin(null);
 		createScaledImage(ImageScaleType.REHISTOGRAM, null);
 		// Max and min changed in all likely-hood
 		fireMaxDataListeners();
 		fireMinDataListeners();
 		repaint();
 	}
 	
 
 	@Override
 	public List<AbstractDataset> getAxes() {
 		return axes;
 	}
 
 	/**
 	 * return the HistoType being used
 	 * @return
 	 */
 	@Override
 	public HistoType getHistoType() {
 		return imageServiceBean.getHistogramType();
 	}
 	
 	/**
 	 * Sets the histo type.
 	 */
 	@Override
 	public void setHistoType(HistoType type) {
 		imageServiceBean.setHistogramType(type);
 		Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.HISTO_PREF, type.getLabel());
 		createScaledImage(ImageScaleType.REHISTOGRAM, null);
 		repaint();
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
 	}
 
 	@Override
 	public HistogramBound getMinCut() {
 		return imageServiceBean.getMinimumCutBound();
 	}
 
 	@Override
 	public void setMinCut(HistogramBound bound) {
 		
 		storeBound(bound, PlottingConstants.MIN_CUT);
 		imageServiceBean.setMinimumCutBound(bound);
 		fireMinCutListeners();
 	}
 
 	private void storeBound(HistogramBound bound, String prop) {
 		if (bound!=null) {
 			Activator.getDefault().getPreferenceStore().setValue(prop, bound.toString());
 		} else {
 			Activator.getDefault().getPreferenceStore().setValue(prop, "");
 		}
 	}
 
 	@Override
 	public HistogramBound getMaxCut() {
 		return imageServiceBean.getMaximumCutBound();
 	}
 
 	@Override
 	public void setMaxCut(HistogramBound bound) {
 		storeBound(bound, PlottingConstants.MAX_CUT);
 		imageServiceBean.setMaximumCutBound(bound);
 		fireMaxCutListeners();
 	}
 
 	@Override
 	public HistogramBound getNanBound() {
 		return imageServiceBean.getNanBound();
 	}
 
 	@Override
 	public void setNanBound(HistogramBound bound) {
 		storeBound(bound, PlottingConstants.NAN_CUT);
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
 	public void setMask(AbstractDataset bd) {
 		if (maskMap!=null) maskMap.clear();
 		fullMask = bd;
 		rehistogram();
 		fireMaskListeners();
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
 	public ROIBase getRegionInAxisCoordinates(final ROIBase roi) throws Exception {
 		
 		if (axes==null)     return roi;
 		if (axes.isEmpty()) return roi;
 		
 		final AbstractDataset xl = axes.get(0); // May be null
 		final AbstractDataset yl = axes.get(1); // May be null
 		
 		if (roi instanceof LinearROI) {
 			double[] sp = ((LinearROI)roi).getPoint();
 			double[] ep = ((LinearROI)roi).getEndPoint();
 			transform(xl,0,sp,ep);
 			transform(yl,1,sp,ep);
 			return new LinearROI(sp, ep);
 			
 		} else if (roi instanceof PolygonalROI) {
 			PolygonalROI proi = (PolygonalROI)roi;
 			final PolygonalROI ret = new PolygonalROI();
 			for (PointROI pointROI : proi) {
 				double[] dp = pointROI.getPoint();
 				transform(xl,0,dp);
 				transform(yl,1,dp);
 				ret.insertPoint(dp);
 			}
 			
 		} else if (roi instanceof PointROI) {
 			double[] dp = ((PointROI)roi).getPoint();
 			transform(xl,0,dp);
 			transform(yl,1,dp);
 			return new PointROI(dp);
 			
 		} else if (roi instanceof RectangularROI) {
 			RectangularROI rroi = (RectangularROI)roi;
 			double[] sp=roi.getPoint();
 			double[] ep=rroi.getEndPoint();
 			transform(xl,0,sp,ep);
 			transform(yl,1,sp,ep);
 				
 			return new RectangularROI(sp[0], sp[1], ep[0]-sp[0], sp[1]-ep[1], rroi.getAngle());
 						
 		} else {
 			throw new Exception("Unsupported roi "+roi.getClass());
 		}
 
 		return roi;
 	}
 
 	
 	@Override
 	public double[] getPointInAxisCoordinates(final double[] point) throws Exception {
 		if (axes==null)     return point;
 		if (axes.isEmpty()) return point;
 		
 		final AbstractDataset xl = axes.get(0); // May be null
 		final AbstractDataset yl = axes.get(1); // May be null
 		transform(xl,0,point);
 		transform(yl,1,point);
         return point;
 	}
 
 	private void transform(AbstractDataset label, int index, double[]... points) {
 		if (label!=null) {
 			for (double[] ds : points) {
 				int dataIndex = (int)ds[index];
 				ds[index] = label.getDouble(dataIndex);
 			}
 		}		
 	}
 	private Object userObject;
 
 	@Override
 	public Object getUserObject() {
 		return userObject;
 	}
 
 	@Override
 	public void setUserObject(Object userObject) {
 		this.userObject = userObject;
 	}
 
 }
