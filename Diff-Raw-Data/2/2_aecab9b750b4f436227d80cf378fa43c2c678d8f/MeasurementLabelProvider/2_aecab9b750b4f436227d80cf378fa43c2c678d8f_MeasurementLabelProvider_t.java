 package org.dawnsci.plotting.tools.region;
 
 import org.dawb.common.util.number.DoubleUtils;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.swt.graphics.Image;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public class MeasurementLabelProvider extends ColumnLabelProvider {
 	
 	public enum LabelType {
 		ROINAME, STARTX, STARTY, ENDX, ENDY, MAX, SUM, ROITYPE, DX, DY, LENGTH, INNERRAD, OUTERRAD, ROISTRING, ACTIVE
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(MeasurementLabelProvider.class);
 	
 	private LabelType column;
 	private AbstractRegionTableTool tool;
 	private Image checkedIcon;
 	private Image uncheckedIcon;
 	private int precision = 3;
 
 	public MeasurementLabelProvider(AbstractRegionTableTool tool, LabelType i) {
 		this.column = i;
 		this.tool   = tool;
 		ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
 		checkedIcon   = id.createImage();
 		id = Activator.getImageDescriptor("icons/unticked.gif");
 		uncheckedIcon =  id.createImage();
 	}
 
 	private static final String NA = "-";
 
 	@Override
 	public Image getImage(Object element){
 		
 		if (!(element instanceof IRegion)) return null;
 		if (column==LabelType.ACTIVE){
 			final IRegion region = (IRegion)element;
 			return region.isActive() && tool.getControl().isEnabled() ? checkedIcon : uncheckedIcon;
 		}
 		return null;
 	}
 
 	@Override
 	public String getText(Object element) {
 		
 		if (!(element instanceof IRegion)) return null;
 		final IRegion    region = (IRegion)element;
 
 		IROI roi = tool.getROI(region);
 
 		try {
 			Object fobj = null;
 			if (element instanceof String) return "";
 			ICoordinateSystem coords = region.getCoordinateSystem();
			if(roi == null) return "";
 			double[] startPoint = getAxisPoint(coords, roi.getPoint());
 			double[] endPoint = {0, 0};
 			if(roi instanceof RectangularROI){
 				endPoint = getAxisPoint(coords, ((RectangularROI)roi).getEndPoint());
 			} else if (roi instanceof LinearROI){
 				endPoint = getAxisPoint(coords, ((LinearROI)roi).getEndPoint());
 			}
 			switch(column) {
 			case ROINAME:
 				return region.getLabel();
 			case STARTX:
 				fobj = startPoint[0];
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case STARTY: // dx
 				fobj = startPoint[1];
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case ENDX: // dy
 				fobj = endPoint[0];
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case ENDY: // length
 				fobj = endPoint[1];
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case MAX: // max
 				final double max = tool.getMaxIntensity(region);
 			    if (Double.isNaN(max)) return NA;
 				return DoubleUtils.formatDouble(max, 5);
 			case SUM: // sum
 				final double sum = tool.getSum(region);
 				if(Double.isNaN(sum)) return NA;
 				return DoubleUtils.formatDouble(sum, 5);
 			case ROITYPE: //ROI type
 				return region.getRegionType().getName();
 			case DX: // dx
 				if (roi instanceof LinearROI) {
 					LinearROI lroi = (LinearROI) roi;
 					fobj = lroi.getEndPoint()[0] - lroi.getPointX();
 				} else if (roi instanceof RectangularROI) {
 					RectangularROI rroi = (RectangularROI) roi;
 					fobj = rroi.getEndPoint()[0] - rroi.getPointX();
 				}
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case DY: // dy
 				if (roi instanceof LinearROI) {
 					LinearROI lroi = (LinearROI) roi;
 					fobj = lroi.getEndPoint()[1] - lroi.getPointY();
 				} else if (roi instanceof RectangularROI) {
 					RectangularROI rroi = (RectangularROI) roi;
 					fobj = rroi.getEndPoint()[1] - rroi.getPointY();
 				}
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case LENGTH: // length
 				if (roi instanceof LinearROI) {
 					LinearROI lroi = (LinearROI) roi;
 					fobj = lroi.getLength();
 				} else if (roi instanceof RectangularROI) {
 					RectangularROI rroi = (RectangularROI) roi;
 					double[] lens = rroi.getLengths();
 					fobj = Math.hypot(lens[0], lens[1]);
 				}
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case INNERRAD: // in rad
 				if (roi instanceof SectorROI) {
 					SectorROI sroi = (SectorROI) roi;
 					fobj = sroi.getRadius(0);
 				}
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case OUTERRAD: // out rad
 				if (roi instanceof SectorROI) {
 					SectorROI sroi = (SectorROI) roi;
 					fobj = sroi.getRadius(1);
 				}
 				return fobj == null ? NA : String.valueOf(DoubleUtils.roundDouble((Double)fobj, precision));
 			case ROISTRING: // region
 				return tool.getROI(region).toString();
 			default:
 				return "";
 			}
 			
 			
 		} catch (Throwable ne) {
 			// One must not throw RuntimeExceptions like null pointers from this
 			// method because the user gets an eclipse dialog confusing them with 
 			// the error
 			logger.error("Cannot get value in info table", ne);
 			return "";
 		}
 	}
 
 	public String getToolTipText(Object element) {
 		return "Any selection region can be used in measurement tool. Try box and axis selections as well as line...";
 	}
 
 	@Override
 	public void dispose(){
 		super.dispose();
 		checkedIcon.dispose();
 		uncheckedIcon.dispose();
 	}
 
 	/**
 	 * get point in axis coords
 	 * @param coords
 	 * @return
 	 */
 	private double[] getAxisPoint(ICoordinateSystem coords, double... vals) {
 		if (coords==null) return vals;
 		try {
 			return coords.getValueAxisLocation(vals);
 		} catch (Exception e) {
 			return vals;
 		}
 	}
 }
