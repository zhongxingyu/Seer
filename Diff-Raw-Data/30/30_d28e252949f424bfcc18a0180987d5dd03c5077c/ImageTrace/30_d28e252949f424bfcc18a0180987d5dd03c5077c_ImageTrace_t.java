 package org.dawb.workbench.plotting.system.swtxy;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.IAxisListener;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.dawb.common.services.IImageService;
 import org.dawb.common.services.IImageService.ImageServiceBean;
 import org.dawb.common.ui.image.PaletteFactory;
 import org.dawb.common.ui.plot.region.RegionBounds;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.PlottingConstants;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
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
 
 /**
  * A trace which draws an image to the plot.
  * 
  * @author fcp94556
  *
  */
 public class ImageTrace extends Figure implements IImageTrace, IAxisListener {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ImageTrace.class);
 
 	private String name;
 	private Axis   xAxis;
 	private Axis   yAxis;
 	private AbstractDataset  image;
 	private PaletteData paletteData;
 	private ImageOrigin imageOrigin;
    private Number min, max;

 
 	private Job imageScaleJob; // Needed for large images on slow systems.
 	
 	private static Map<IImageTrace.ImageOrigin, IImageService.ImageOrigin> imageOriginaMap;
 	static {
 		imageOriginaMap = new HashMap<IImageTrace.ImageOrigin, IImageService.ImageOrigin>();
 		imageOriginaMap.put(IImageTrace.ImageOrigin.TOP_LEFT,     IImageService.ImageOrigin.TOP_LEFT);
 		imageOriginaMap.put(IImageTrace.ImageOrigin.TOP_RIGHT,    IImageService.ImageOrigin.TOP_RIGHT);
 		imageOriginaMap.put(IImageTrace.ImageOrigin.BOTTOM_LEFT,  IImageService.ImageOrigin.BOTTOM_LEFT);
 		imageOriginaMap.put(IImageTrace.ImageOrigin.BOTTOM_RIGHT, IImageService.ImageOrigin.BOTTOM_RIGHT);
 	}
 	
 	public ImageTrace(final String name, 
 			          final Axis xAxis, 
 			          final Axis yAxis) {
 		
 		this.name  = name;
 		this.xAxis = xAxis;		
 		this.yAxis = yAxis;
 
 		this.paletteData = PaletteFactory.getPalette(Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.P_PALETTE));	
 		this.imageOrigin = IImageTrace.ImageOrigin.forLabel(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.ORIGIN_PREF));
 				
 		xAxis.addListener(this);
 		yAxis.addListener(this);
 		
 		if (xAxis instanceof AspectAxis && yAxis instanceof AspectAxis) {
 			
 			AspectAxis x = (AspectAxis)xAxis;
 			AspectAxis y = (AspectAxis)yAxis;
 			x.setKeepAspectWith(y);
 			y.setKeepAspectWith(x);
 		}
 		
 		this.imageScaleJob = new Job("Create scaled image") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				createScaledImage(false, monitor);
 				return Status.OK_STATUS;
 			}
 		};
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Axis getxAxis() {
 		return xAxis;
 	}
 
 	public void setxAxis(Axis xAxis) {
 		this.xAxis = xAxis;
 	}
 
 	public Axis getyAxis() {
 		return yAxis;
 	}
 
 	public void setyAxis(Axis yAxis) {
 		this.yAxis = yAxis;
 	}
 
 	public AbstractDataset getImage() {
 		return image;
 	}
 
 	public void setImage(AbstractDataset image) {
 		this.image = image;
 	}
 
 	public PaletteData getPaletteData() {
 		return paletteData;
 	}
 
 	public void setPaletteData(PaletteData paletteData) {
 		this.paletteData = paletteData;
 		createScaledImage(true, null);
 		repaint();
 	}
 
 	
 	private Image     scaledImage;
 	private ImageData rawData;
 	private Range     xRangeCached, yRangeCached;
 	
 	/**
 	 * Whenever this is called the SWT image is created
 	 * and saved in the swtImage field.
 	 */
 	private void createScaledImage(boolean force, final IProgressMonitor monitor) {
 		
 		
 		boolean isRotated = getImageOrigin()==ImageOrigin.TOP_LEFT||getImageOrigin()==ImageOrigin.BOTTOM_RIGHT;
 		
 		final Axis  x      = isRotated ? yAxis : xAxis;
 		final Axis  y      = isRotated ? xAxis : yAxis;
 		
 		final Range xRange = x.getRange();
 		final Range yRange = y.getRange();
 		
 		if (monitor!=null && monitor.isCanceled()) return;
 		
 		final boolean isSameRange = (xRangeCached!=null && xRangeCached.equals(xRange) && yRangeCached!=null && yRangeCached.equals(yRange));
 		
 		if (!isSameRange || rawData==null || force) {
 			if (monitor!=null && monitor.isCanceled()) return;
 			final RegionBounds regionBounds = new RegionBounds(new double[]{xRange.getLower(), yRange.getLower()}, 
 	                                                           new double[]{xRange.getUpper(), yRange.getUpper()});
 			AbstractDataset slice = slice(regionBounds);
 			
 			final IImageService service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 			if (monitor!=null && monitor.isCanceled()) return;
 			try {
 				final ImageServiceBean bean =  new IImageService.ImageServiceBean();
 				bean.setImage(slice);
 				bean.setOrigin(imageOriginaMap.get(getImageOrigin()));
 				bean.setPalette(getPaletteData());
 				bean.setMonitor(monitor);
				if (getMin()!=null) bean.setMin(min);
				if (getMax()!=null) bean.setMax(max);
 				if (monitor!=null && monitor.isCanceled()) return;
 				this.rawData   = service.getImageData(bean);
 			} catch (Exception e) {
 				logger.error("Cannot create image from data!", e);
 			}
 		}
 		
 		final XYRegionGraph graph  = (XYRegionGraph)x.getParent();
 		final Rectangle     bounds = graph.getRegionArea().getBounds();
 		
 		if (monitor!=null && monitor.isCanceled()) return;
 		ImageData data = rawData.scaledTo(bounds.width, bounds.height);
 		if (monitor!=null && monitor.isCanceled()) return;
 		this.scaledImage = new Image(Display.getDefault(), data);
 		
 		xRangeCached = xRange;
 		yRangeCached = yRange;
 		
 		if (monitor!=null && Display.getDefault()!=null) Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				repaint();
 			}
 		});
 	}
 	
 	@Override
 	protected void paintFigure(Graphics graphics) {
 		
 		if (scaledImage==null) createScaledImage(false, null);
 		
 		super.paintFigure(graphics);
 		
 		graphics.pushState();	
 		final XYRegionGraph graph  = (XYRegionGraph)xAxis.getParent();
 		final Point         loc    = graph.getRegionArea().getLocation();
 		graphics.drawImage(scaledImage, loc);
 		graphics.popState();
 	}
 
 	public void remove() {
 		if (scaledImage!=null) scaledImage.dispose();
 		getParent().remove(this);
 		xAxis.removeListenr(this);
 		yAxis.removeListenr(this);
 		axisRedrawActive = false;
 	}
 
 	@Override
 	public AbstractDataset getData() {
 		return image;
 	}
 
 	@Override
 	public AbstractDataset slice(RegionBounds bounds) {
 		
 		final AbstractDataset data = getData();
 		
 		// Check that a slice needed, this speeds up the initial show of the image.
 		final RegionBounds imageBounds = getImageBounds(); 
 		if (imageBounds!=null && imageBounds.equals(bounds))  {
 			return image;
 		}
 		
 		int[] xRange = getRange(bounds, 0, false);
 		int[] yRange = getRange(bounds, 1, false);		
 				
 		try {
 			return data.getSlice(new int[]{xRange[0],yRange[0]}, 
 					             new int[]{xRange[1],yRange[1]}, 
 					             new int[]{1,     1});
 			
 		}catch (IllegalArgumentException iae) {
 			logger.error("Cannot slice image", iae);
 			return getData();
 		}
 	}
 
 	private int[] getRange(RegionBounds bounds, int index, boolean inverted) {
 		
 		final int side = image.getShape()[index];
 		int start = (int)Math.round(bounds.getP1()[index]);
 		if (inverted) start = side-start;
 		
 		int stop  = (int)Math.round(bounds.getP2()[index]);
 		if (inverted) stop = side-stop;
 
 		if (start>stop) {
 			start = (int)Math.round(bounds.getP2()[index]);
 			if (inverted) start = side-start;
 			
 			stop  = (int)Math.round(bounds.getP1()[index]);
 			if (inverted) stop = side-stop;
 		}
 		
 		return new int[]{start, stop};
 	}
 
 	private boolean axisRedrawActive = true;
 	@Override
 	public void axisRangeChanged(Axis axis, Range old_range, Range new_range) {
 		if (!axisRedrawActive) return;
 		imageScaleJob.cancel();
 		imageScaleJob.schedule();
 	}
 
 	@Override
 	public void axisRevalidated(Axis axis) {
 		if (!axisRedrawActive) return;
 		imageScaleJob.cancel();
 		imageScaleJob.schedule();
 	}
 	private void setAxisRedrawActive(boolean b) {
 		this.axisRedrawActive = b;
 	}
 
 
 	public void performAutoscale() {
 		switch(getImageOrigin()) {
 		case TOP_LEFT:
 			xAxis.setRange(0, image.getShape()[1]);
 			yAxis.setRange(image.getShape()[0], 0);	
 			break;
 			
 		case BOTTOM_LEFT:
 			xAxis.setRange(0, image.getShape()[0]);
 			yAxis.setRange(0, image.getShape()[1]);		
 			break;
 
 		case BOTTOM_RIGHT:
 			xAxis.setRange(image.getShape()[1], 0);
 			yAxis.setRange(0, image.getShape()[0]);		
 			break;
 
 		case TOP_RIGHT:
 			xAxis.setRange(image.getShape()[0], 0);
 			yAxis.setRange(image.getShape()[1], 0);		
 			break;
 		
 		}
 	}
 	
 	private RegionBounds getImageBounds() {
 		switch(getImageOrigin()) {
 		case TOP_LEFT:
 			return new RegionBounds(new double[]{0, image.getShape()[0]},
 					                new double[]{image.getShape()[1], 1});	
 						
 		case BOTTOM_LEFT:
 			return new RegionBounds(new double[]{0, 0},
 	                                new double[]{image.getShape()[0], image.getShape()[1]});	
 
 		case BOTTOM_RIGHT:
 			return new RegionBounds(new double[]{image.getShape()[1], 0},
                                     new double[]{0, image.getShape()[0]});	
 
 		case TOP_RIGHT:
 			return new RegionBounds(new double[]{image.getShape()[0], image.getShape()[1]},
                                     new double[]{0,0});	
 		
 		}
 		return null;
 	}
 
 	public void setImageOrigin(ImageOrigin imageOrigin) {
 		this.imageOrigin = imageOrigin;
 		performAutoscale();
 		repaint();
 	}
 
 	@Override
 	public ImageOrigin getImageOrigin() {
 		return imageOrigin;
 	}
 	
 	@Override
 	public void setData(final AbstractDataset image, List<AbstractDataset> axes) {
 		// The image is drawn low y to the top left but the axes are low y to the bottom right
 		// We do not currently reflect it as it takes too long. Instead in the slice
 		// method, we allow for the fact that the dataset is in a different orientation to 
 		// what is plotted.
 		this.image = image;
 		try {
 			setAxisRedrawActive(false);
 			performAutoscale();
 		} finally {
 			setAxisRedrawActive(true);
 		}
        
 	}
	public Number getMin() {
		return min;
	}

	public void setMin(Number min) {
		this.min = min;
	}

	public Number getMax() {
		return max;
	}

	public void setMax(Number max) {
		this.max = max;
	}
 
 }
