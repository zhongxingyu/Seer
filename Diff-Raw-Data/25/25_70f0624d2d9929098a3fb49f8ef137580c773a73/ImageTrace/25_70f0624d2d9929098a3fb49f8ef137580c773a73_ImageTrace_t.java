 package org.dawb.workbench.plotting.system.swtxy;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.IAxisListener;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.dawb.common.services.IImageService;
 import org.dawb.common.ui.image.PaletteFactory;
 import org.dawb.common.ui.plot.region.RegionBounds;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.PlottingConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
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
 		
 		// TODO x-axis to be told to keep aspect ratio with y
 		
 	}
 	
 	private AbstractDataset getReflection(final AbstractDataset unreflectedImage) {
 		
 		final int[] shape = unreflectedImage.getShape();
 		AbstractDataset tdata = AbstractDataset.zeros(shape, AbstractDataset.getDType(unreflectedImage));		
 		for (int row = 0; row<shape[0]; ++row) {
 			int newRow = shape[0]-row-1;
 			for (int col = 0; col<shape[1]; ++col) {				
 				Object val = unreflectedImage.getObject(row, col);
 				tdata.set(val, newRow, col);
 			}
 		}
 		return tdata;
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
 		createScaledImage(true);
 		repaint();
 	}
 
 	
 	private Image scaledImage;
 	private Image rawImage;
 	private Range xRangeCached, yRangeCached;
 	
 	/**
 	 * Whenever this is called the SWT image is created
 	 * and saved in the swtImage field.
 	 */
 	private void createScaledImage(boolean force) {
 		
 		
 		boolean isRotated = getImageOrigin()==ImageOrigin.BOTTOM_LEFT||getImageOrigin()==ImageOrigin.TOP_RIGHT;
 		
 		final Axis  x      = isRotated ? yAxis : xAxis;
 		final Axis  y      = isRotated ? xAxis : yAxis;
		
 		final Range xRange = x.getRange();
 		final Range yRange = y.getRange();
 				
 		final XYRegionGraph graph  = (XYRegionGraph)x.getParent();
 		final Rectangle     bounds = graph.getRegionArea().getBounds();
 				
 		final boolean isSameRange = (xRangeCached!=null && xRangeCached.equals(xRange) && yRangeCached!=null && yRangeCached.equals(yRange));
 		
 		if (!isSameRange || rawImage==null || force) {
			final RegionBounds regionBounds = new RegionBounds(new double[]{yRange.getLower(), xRange.getLower()}, 
	                                                           new double[]{yRange.getUpper(), xRange.getUpper()});
 			AbstractDataset slice = slice(regionBounds);
 			
 			final IImageService service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 			try {
 				this.rawImage   = service.getImage(slice, getPaletteData(), imageOriginaMap.get(getImageOrigin()));
 			} catch (Exception e) {
 				logger.error("Cannot create image from data!", e);
 			}
 		}
 		
 		
 		ImageData data = rawImage.getImageData().scaledTo(bounds.width, bounds.height);
 		this.scaledImage = new Image(rawImage.getDevice(), data);
 		
 		xRangeCached = xRange;
 		yRangeCached = yRange;
 	}
 	
 	@Override
 	protected void paintFigure(Graphics graphics) {
 		
 		if (scaledImage==null) createScaledImage(false);
 		
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
 		createScaledImage(false);
 	}
 
 	@Override
 	public void axisRevalidated(Axis axis) {
 		if (!axisRedrawActive) return;
 		createScaledImage(false);
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
 			xAxis.setRange(0, image.getShape()[1]);
 			yAxis.setRange(0, image.getShape()[0]);		
 			break;
 
 		case BOTTOM_RIGHT:
 			xAxis.setRange(image.getShape()[0], 0);
 			yAxis.setRange(0, image.getShape()[1]);		
 			break;
 
 		case TOP_RIGHT:
 			xAxis.setRange(image.getShape()[1], 0);
 			yAxis.setRange(image.getShape()[0], 0);		
 			break;
 		
 		}
 	}
 	
 	private RegionBounds getImageBounds() {
 		switch(getImageOrigin()) {
 		case TOP_LEFT:
 			return new RegionBounds(new double[]{0, image.getShape()[1]},
 					                new double[]{image.getShape()[0], 1});	
 						
 		case BOTTOM_LEFT:
 			return new RegionBounds(new double[]{0, 0},
 	                                new double[]{image.getShape()[1], image.getShape()[0]});	
 
 		case BOTTOM_RIGHT:
 			return new RegionBounds(new double[]{image.getShape()[0], 0},
                                     new double[]{0, image.getShape()[1]});	
 
 		case TOP_RIGHT:
 			return new RegionBounds(new double[]{image.getShape()[1], image.getShape()[0]},
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
 
 }
