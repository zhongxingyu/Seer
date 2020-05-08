 package org.dawnsci.plotting.tools.masking;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveAction;
 
 import org.dawb.common.ui.image.ShapeType;
 import org.dawnsci.plotting.AbstractPlottingSystem;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.preferences.PlottingConstants;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.tools.Activator;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.DefaultOperationHistory;
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.IRectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 
 /**
  * This class is taken directly out of the class of the same name in SDA
  * 
  * The intention is to make the maths available separate to the side plot system.
  * 
  * @author fcp94556
  *
  */
 public class MaskObject {
 
 	private static final Logger logger = LoggerFactory.getLogger(MaskObject.class);
 	
 	enum MaskRegionType {
 		REGION_FROM_MASKING;
 	}
 	
 	public enum MaskMode {
 		/**
 		 * Draw the mask, ie do the mask
 		 */
 		DRAW, 
 		
 		/**
 		 * Toggle the masked state
 		 */
 		TOGGLE, 
 		
 		/**
 		 * Remove the mask at the location.
 		 */
 		ERASE
 	}
 
 	private MaskMode        maskMode;
     private boolean         squarePen           = false;
     private boolean         ignoreAlreadyMasked = false;
    /**
      * The booleans are false to mask and
      * true to leave, that way multiply will work.
      */
     private BooleanDataset  maskDataset;
     private AbstractDataset imageDataset;
     
     /**
      * Used for undoable masking operations.
      */
 	private DefaultOperationHistory operationManager;
 	private ForkJoinPool pool;
     
 	MaskObject() {
 		this.operationManager = new DefaultOperationHistory();
 		operationManager.setLimit(MaskOperation.MASK_CONTEXT, 20);	
 		this.pool = new ForkJoinPool();
 	}
 	
     /**
      * Designed to copy data in an incoming mask onto this one as best as possible.
      * @param savedMask
      */
 	public void process(BooleanDataset savedMask) {
 		createMaskIfNeeded();
 		
         MaskOperation op = new MaskOperation(maskDataset, savedMask.getSize()/16);
 		final int[] shape = savedMask.getShape();
 		for (int y = 0; y<shape[0]; ++y) {
 			for (int x = 0; x<shape[1]; ++x) {
 		        try {
 		        	// We only add the falses 
 		        	if (!savedMask.getBoolean(y,x)) {
 		        		toggleMask(op, Boolean.FALSE, y,x);
 		        	}
 		        } catch (Throwable ignored) {
 		        	continue;
 		        }
 			}
 		}
 		
         try {
         	if (op.getSize()>0) operationManager.execute(op, null, null);
 		} catch (ExecutionException e) {
 			logger.error("Internal error processing external mask.", e);
 		}
 
 	}
 	
 	/**
 	 * Attempts to process a pen shape at a given draw2d location.
 	 * 
 	 * Translate from click location using 'system' parameter.
 	 * 
 	 * @param loc
 	 * @param system
 	 */
 	public void process(final Point finishLocation, final IPlottingSystem system, IProgressMonitor monitor) {
 				
 		ShapeType penShape = ShapeType.valueOf(Activator.getPlottingPreferenceStore().getString(PlottingConstants.MASK_PEN_SHAPE));
         if (penShape==ShapeType.NONE) return;
         
 		createMaskIfNeeded();
        
 		boolean maskOut        = Activator.getPlottingPreferenceStore().getBoolean(PlottingConstants.MASK_PEN_MASKOUT);
         final int     penSize  = Activator.getPlottingPreferenceStore().getInt(PlottingConstants.MASK_PEN_SIZE);
         final int     rad1     = (int)Math.ceil(penSize/2f);
         final int     rad2     = (int)Math.floor(penSize/2f);
 
         final IAxis xAxis = system.getSelectedXAxis();
         final IAxis yAxis = system.getSelectedYAxis();
 
         final Point startLocation = ((AbstractPlottingSystem)system).getShiftPoint();
         final Collection<Point> locations;
         if (startLocation==null) {
         	locations = new HashSet<Point>(7);
         	locations.add(finishLocation);
         } else {
         	locations = lineBresenham(startLocation, finishLocation);
         }
         
         MaskOperation op = new MaskOperation(maskDataset, 300);
         
         for (final Point loc : locations) {
         	
 	        monitor.worked(1);
 	        
 	        final List<int[]> span = new ArrayList<int[]>(3);
 	        Display.getDefault().syncExec(new Runnable() {
 	        	public void run() {
 	        		span.add(new int[]{(int)xAxis.getPositionValue(loc.x-rad2), (int)yAxis.getPositionValue(loc.y-rad2)});
 	        		span.add(new int[]{(int)xAxis.getPositionValue(loc.x+rad1), (int)yAxis.getPositionValue(loc.y+rad1)});
 	        		span.add(new int[]{(int)xAxis.getPositionValue(loc.x),      (int)yAxis.getPositionValue(loc.y)});
 	        	}
 	        });
 	        int[]  start = clip(span.get(0));
 	        int[]  end   = clip(span.get(1));
 	        int[]  cen   = span.get(2);
 	        int[]  b     = new int[]{cen[0], start[1]};
 	        int radius   = end[1]-cen[1];
 	
 	        boolean mv = maskOut ? Boolean.FALSE : Boolean.TRUE;
 	        
 	        for (int y = start[1]; y<=end[1]; ++y) {
 	        	for (int x = start[0]; x<=end[0]; ++x) {
 	        		       		
 	        		if (penShape==ShapeType.SQUARE) {
 	        			toggleMask(op, mv, y, x);
 	
 	        		} else if (penShape==ShapeType.CIRCLE || penSize<3) {
 	        			
 	        			double r = Math.hypot(x - cen[0], y - cen[1]);
 	                    if (r<=radius) {
 	                    	toggleMask(op, mv, y, x);
 	                    }
 	                    
 	        		} else if (penShape==ShapeType.TRIANGLE) {
 	
 	           			if (x <= b[0] ) { // px<=bx
 	           				double real = y-start[1];
 	           				double dash = 2*(x-start[0]);
 	        				if (real>dash) {
 	        					toggleMask(op, mv, y, 2*cen[0]-x-(cen[0]-start[0]));
 	        				}
 	        			} else { // px>bx
 	        				double real = y-start[1];
 	           				double dash = 2*(x-cen[0]);
 	        				if (real>dash) {
 	        					toggleMask(op, mv, y, x);
 	        				}
 	        			}
 	       			
 	        		}
 	        	}
 	        }
        }
         
         try {
         	if (op.getSize()>0) operationManager.execute(op, null, null);
 		} catch (ExecutionException e) {
 			logger.error("Problem processing mask draw.", e);
 		}
 	}
 	
 	private final static Collection<Point> lineBresenham(Point from, Point to) {
 		
 		
 		int x0 =from.x, x1 = to.x, y0 = from.y, y1 = to.y;
 		int dy = y1 - y0;
 		int dx = x1 - x0;
 		int stepx, stepy;
 
 		if (dy < 0) {
 			dy = -dy;
 			stepy = -1;
 		} else {
 			stepy = 1;
 		}
 		if (dx < 0) {
 			dx = -dx;
 			stepx = -1;
 		} else {
 			stepx = 1;
 		}
 		dy <<= 1; // dy is now 2*dy
 		dx <<= 1; // dx is now 2*dx
 
 		final List<Point> ret = new ArrayList<Point>(31);
 		ret.add(new Point(x0, y0));
 		if (dx > dy) {
 			int fraction = dy - (dx >> 1); // same as 2*dy - dx
 			while (x0 != x1) {
 				if (fraction >= 0) {
 					y0 += stepy;
 					fraction -= dx; // same as fraction -= 2*dx
 				}
 				x0 += stepx;
 				fraction += dy; // same as fraction -= 2*dy
 				ret.add(new Point(x0, y0));
 			}
 		} else {
 			int fraction = dx - (dy >> 1);
 			while (y0 != y1) {
 				if (fraction >= 0) {
 					x0 += stepx;
 					fraction -= dy;
 				}
 				y0 += stepy;
 				fraction += dx;
 				ret.add(new Point(x0, y0));
 			}
 		}
 		
 		return ret;
 	}
 
 
 	/**
 	 * Toggles a pixel and adds the pixels toggled state to the
 	 * AbstractOperation to allow undo/redo to work.
 	 * @param mv
 	 * @param y
 	 * @param x
 	 */
 	private void toggleMask(MaskOperation op, boolean mv, int y, int x) {
 		if (maskDataset.getBoolean(y,x)!=mv) {
 			op.addVertex(mv, y, x);		
 		}
 	}
 	
 	public void dispose() {
 		if (operationManager!=null) {
 			operationManager.dispose(MaskOperation.MASK_CONTEXT, true, true, true);
 		}
 		if (pool!=null) {
 			pool.shutdownNow();
 		}
 	}
 
 	/**
 	 * Clip to span of image dataset (not 1)
 	 * @param is
 	 * @return
 	 */
 	private int[] clip(int[] is) {
 		int maxX = imageDataset.getShape()[1]-1;
 		if (is[0]>maxX) is[0] = maxX;
 		if (is[0]<0)    is[0] = 0;
 		
 		int maxY = imageDataset.getShape()[0]-1;
 		if (is[1]>maxY) is[1] = maxY;
 		if (is[1]<0)    is[1] = 0;
         return is;
 	}
 
 	/**
 	 * Designed to be called after processBounds(...) has been called at least once.
 	 * Deals with fact that that may leave us with no mask and will create one if needed.
 	 * 
 	 * @param region
 	 * @return
 	 */
 	public boolean process(IRegion region, IProgressMonitor monitor) {
         return process(null, null, Arrays.asList(new IRegion[]{region}), monitor);
 	}
 
 	/**
 	 * Processes the while of the dataset and sets those values in bounds 
 	 * to be false and those outside to be true in the mask.
 	 * 
 	 * Nullifies the mask if max and min are null.
 	 * 
 	 * @param min
 	 * @param max
 	 * @return
 	 */
 	public boolean process(final Number              minNumber, 
 			               final Number              maxNumber, 
 			               final Collection<IRegion> regions, 
 			               final IProgressMonitor    monitor) {
 		
 		createMaskIfNeeded();
 		monitor.worked(1);
 	
         // Slightly wrong AbstractDataset loop, but it is faster...
 		if (minNumber!=null || maxNumber!=null) {
 			final int           as  = imageDataset.getElementsPerItem();
 			if (as!=1) throw new RuntimeException("Cannot deal with mulitple elements in mask processing!");
 			
 			double              lo  = minNumber!=null ? minNumber.doubleValue() : Double.NaN;
 			double              hi  = maxNumber!=null ? maxNumber.doubleValue() : Double.NaN;
 			
 			final int size = imageDataset.getSize();
 			for (int i = 0; i < size; i++) {
 				
 				double x = imageDataset.getElementDoubleAbs(i);
 				boolean isValid = isValid(x, lo, hi);
 				if (ignoreAlreadyMasked && isValid && !maskDataset.getAbs(i)) continue;
 				maskDataset.setAbs(i, isValid);
 				
 			}
 		}
 
 		if (regions != null) {
 			// Remove invalid regions first to make processing faster.
 			final List<IRegion> validRegions = new ArrayList<IRegion>(regions.size());
 			for (IRegion region : regions) {
 				if (region == null)             continue;
 				if (!isSupportedRegion(region)) continue;
 				if (region.getUserObject()!=MaskRegionType.REGION_FROM_MASKING)     continue;
 				validRegions.add(region);
 			}
 
 			if (validRegions.isEmpty()) return true;
 
 			final MaskOperation op  = new MaskOperation(maskDataset, getMaskDataset().getSize()/16);
 			final int[]      shape  = imageDataset.getShape();
 			
 			if (Boolean.getBoolean("org.dawnsci.plotting.tools.masking.no.thread.pool")) {
 				MAIN_LOOP: for (int y = 0; y<shape[0]; ++y) {
 					for (int x = 0; x<shape[1]; ++x) {
 						for (IRegion region : validRegions) {
 							monitor.worked(1);
 							try {
 								if (region.getCoordinateSystem().isDisposed()) break MAIN_LOOP;
 								if (region.getROI().containsPoint(x, y)) {
 									toggleMask(op, !region.isMaskRegion(), y, x);
 								}
 							} catch (Throwable ne) {
 								logger.trace("Cannot process point "+(new Point(x,y)), ne);
 								continue;
 							}
 						}
 					}
 				}
 			} else {
 				// NORMALLY
 				pool.invoke(new MaskRegionsAction(op, shape, regions, monitor));
 			}
 
 			try {
 				if (op.getSize()>0) operationManager.execute(op, null, null);
 			} catch (ExecutionException e) {
 				logger.error("Internal error processing region mask.", e);
 			}   
 		}
 		
 
 		return true;
 	}
 	
 	private static int INC = 100; // The amount of columns to do for each task.
 	
 		
 	private class MaskRegionsAction extends RecursiveAction {
 		
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -4810609286048487303L;
 
 		protected MaskOperation    op;
 		protected   int[]          shape;
 		protected IProgressMonitor monitor;
 		private final Collection<IRegion> regions;
 		
 		MaskRegionsAction(final MaskOperation op, final int[] shape, 
 			                final Collection<IRegion> regions, 
 			                final IProgressMonitor monitor) {
 		
 			this.op      = op;
 			this.shape   = shape;
 			this.regions = regions;
 			this.monitor = monitor;
 		}
 
 		@Override
 		protected void compute() {
 
 			final Collection<RegionAction> actions = new ArrayList<RegionAction>(regions.size());
 
 			for (IRegion region : regions) {
 
 				final IROI    roi       = region.getROI();
 				final boolean isMasking = region.isMaskRegion();
 				actions.add(new RegionAction(op, shape, roi, getScreenPixelWidth(region), isMasking, monitor));
 			}
 			invokeAll(actions);
 		}
 		
         /**
          * Get pixel width in data coordinates.
          * @param region
          * @return
          */
 		public double getScreenPixelWidth(IRegion region) {
 			final int widPix = region.getLineWidth();
 			double[] s = region.getCoordinateSystem().getPositionValue(new int[]{0, 0});
 			double[] e = region.getCoordinateSystem().getPositionValue(new int[]{widPix, widPix});
 			//return Math.pow((Math.pow(e[0]-s[0], 2)+Math.pow(e[1]-s[1], 2)), 0.5);
 			// FIXME This is not right but works for many images that we have. 
 			// Those with significantly different axis scales, it will not.
 			return Math.min(e[0]-s[0], e[1]-s[1]);
 		}
 
 	}
 
 	private class RegionAction extends MaskRegionsAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -7438635693096574242L;
 		
 		protected boolean          isMasking;
 		protected IROI             roi;
 		protected double           lineWidth;
 
 		/**
 		 * 
 		 * @param op
 		 * @param shape
 		 * @param roi
 		 * @param lineWidth in data coordinates (ROI)
 		 * @param isMasking
 		 * @param monitor
 		 */
 		public RegionAction(MaskOperation op, int[] shape, IROI roi, double lineWidth,
 				            boolean isMasking, IProgressMonitor monitor) {
 			super(op, shape, null, monitor);
 			this.isMasking = isMasking;
 			this.roi = roi;
 			this.lineWidth = lineWidth;
 		}
 
 		@Override
 		protected void compute() {
 			
 			
 			// We use the bounding box of the region.
 			final IRectangularROI bounds = roi.getBounds();
			final double[] beg = bounds.getPoint();
 			final double[] end = bounds.getEndPoint();
 
 			if (roi instanceof LinearROI) { // special case where isNearOutline is used for mask
 				double distance = Math.max(0.5, lineWidth/2.);
 				beg[0] -= distance;
 				beg[1] -= distance;
 				end[0] += distance;
 				end[1] += distance;
 			}
 
 			int xStart = Math.max(0, (int) Math.round(beg[0]));
 			int xEnd   = Math.min(shape[1] - 1, (int) Math.round(end[0]));
 			
 			int yStart = Math.max(0, (int) Math.round(beg[1]));
 			int yEnd   = Math.min(shape[0] - 1, (int) Math.round(end[1]));
 
 			final Collection<RegionAction> actions = new ArrayList<RegionAction>(yEnd/INC);
 			// We loop all pixels here because looping bounds boxes of rois did not work yet.
 
 			for (int y=yStart; y<yEnd; y+=INC) { 
 				
 				final int yMax = Math.min(yStart+INC, yEnd);
 				actions.add(new PixelAction(op, xStart, xEnd, yStart, yMax, isMasking, roi, lineWidth, monitor));
 				yStart+=INC;
 				if (monitor.isCanceled()) return;
 			}
 			invokeAll(actions);
 		}
 	}
 	
 	/**
 	 * Each PixelAction does around 100,000 of the pixels.
 	 * 
 	 * @author fcp94556
 	 */
 	private class PixelAction extends RegionAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -6539202270251672297L;
 		
 		private int xStart, xEnd;
 		private int yStart, yEnd;
 
 		public PixelAction(MaskOperation op, 
 				           int xStart, int xEnd,
 				           int yStart, int yEnd,
 				           boolean isMasking, IROI roi, double lineWidth, 
 				           IProgressMonitor monitor) {
 			
 			super(op, null, roi, lineWidth, isMasking, monitor);
 			this.xStart  = xStart;
 			this.xEnd    = xEnd;
 			this.yStart  = yStart;
 			this.yEnd    = yEnd;
 		}
 
 		@Override
 		protected void compute() {
 			if (roi instanceof LinearROI) {
 				double distance = Math.max(0.5, lineWidth/2.);
 				for (int y = yStart; y < yEnd; ++y) {
 					if (monitor.isCanceled()) return;
 					monitor.worked(1);
 
 					for (int x = xStart; x < xEnd; ++x) {
 
 						if (maskDataset.getBoolean(y, x) != isMasking)
 							continue;
 						try {
 							if (roi.isNearOutline(x, y, distance)) {
 								toggleMask(op, !isMasking, y, x);
 							}
 						} catch (Throwable ne) {
 							logger.trace("Cannot process point " + (new Point(x, y)), ne);
 							return;
 						}
 					}
 				}
 			} else {
 				for (int y = yStart; y < yEnd; ++y) {
 					if (monitor.isCanceled())
 						return;
 					monitor.worked(1);
 
 					for (int x = xStart; x < xEnd; ++x) {
 
 						if (maskDataset.getBoolean(y, x) != isMasking)
 							continue;
 						try {
 							if (roi.containsPoint(x, y)) {
 								toggleMask(op, !isMasking, y, x);
 							}
 						} catch (Throwable ne) {
 							logger.trace("Cannot process point " + (new Point(x, y)), ne);
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void createMaskIfNeeded() {
 		if (maskDataset == null || !maskDataset.isCompatibleWith(imageDataset)) {
 			maskDataset = BooleanDataset.ones(imageDataset.getShape());
 			maskDataset.setName("mask");
 		}	
 		if (operationManager ==null)  {
 			operationManager = new DefaultOperationHistory();
 			operationManager.setLimit(MaskOperation.MASK_CONTEXT, 20);
 		}
 	}
 
 	private static final boolean isValid(double val, double min, double max) {
 		if (!Double.isNaN(min) && val<=min) return false;
 		if (!Double.isNaN(max) && val>=max) return false;
 		return true;
 	}
 
 	/**
 	 * TODO Add more than just line, free and box - ring would be useful!
 	 * @param region
 	 * @return
 	 */
 	public boolean isSupportedRegion(IRegion region) {
 		
 		if (!region.isVisible())    return false;
 		if (!region.isUserRegion()) return false;
 		if (region.getROI()==null)  return false;
 		
 		return true;
 	}
 
 	public MaskMode getMaskMode() {
 		return maskMode;
 	}
 
 	public void setMaskMode(MaskMode paintMode) {
 		this.maskMode = paintMode;
 	}
 	public boolean isSquarePen() {
 		return squarePen;
 	}
 
 	public void setSquarePen(boolean squarePen) {
 		this.squarePen = squarePen;
 	}
 
 	public BooleanDataset getMaskDataset() {
 		return maskDataset;
 	}
 
 	/**
 	 * The booleans get filled true when this is set.
 	 * @param maskDataset
 	 */
 	public void setMaskDataset(BooleanDataset maskDataset, boolean requireFill) {
 		this.maskDataset = maskDataset;
 		if (maskDataset!=null && requireFill) maskDataset.fill(true);
 	}
 
 	public AbstractDataset getImageDataset() {
 		return imageDataset;
 	}
 
 	public void setImageDataset(AbstractDataset imageDataset) {
 		this.imageDataset = imageDataset;
 	}
 
 	public void reset() {
 		this.maskDataset = null;
 		if (operationManager!=null) {
 			operationManager.dispose(MaskOperation.MASK_CONTEXT, true, true, true);
 		}
 	}
 
 	public IOperationHistory getOperationManager() {
 		return operationManager;
 	}
 
 	public void undo() {
 		try {
 			operationManager.undo(MaskOperation.MASK_CONTEXT, null, null);
 		} catch (ExecutionException e) {
 			logger.error("Internal error - unable to undo!", e);
 		}
 	}
 	public void redo() {
 		try {
 			operationManager.redo(MaskOperation.MASK_CONTEXT, null, null);
 		} catch (ExecutionException e) {
 			logger.error("Internal error - unable to redo!", e);
 		}
 	}
 
 	public void invert() {
 		if (maskDataset!=null) {
 			IndexIterator it1 = maskDataset.getIterator();
 			while (it1.hasNext()) {
 				boolean b = maskDataset.getAbs(it1.index);
 				maskDataset.setAbs(it1.index, !b);
 			}
 		}
 		
 	}
 
 	public boolean isIgnoreAlreadyMasked() {
 		return ignoreAlreadyMasked;
 	}
 
 	public void setIgnoreAlreadyMasked(boolean ignoreAlreadyMasked) {
 		this.ignoreAlreadyMasked = ignoreAlreadyMasked;
 	}
 }
