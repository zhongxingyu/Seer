 package org.dawb.workbench.plotting.system.swtxy;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.dawb.common.ui.plot.axis.AxisEvent;
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.dawb.common.ui.plot.axis.IAxisListener;
 import org.dawb.common.util.text.NumberUtils;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.PlottingConstants;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Rectangle;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  * An axis which can keep aspect with another and have a maximum possible extend which cannot
  * be altered.
  * 
  * @author fcp94556
  *
  */
 public class AspectAxis extends Axis implements IAxis {
 
 	private AspectAxis relativeTo;
 	private Range      maximumRange;
     private boolean    keepAspect; // This is so that the user may have images with and without aspect in the same application.
 	private AbstractDataset labelData;
 	public AspectAxis(String title, boolean yAxis) {
 		super(title, yAxis);
 		keepAspect = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.ASPECT);
 	}
 
 	public void setKeepAspectWith(final AspectAxis axis) {
 		this.relativeTo = axis;
 	}
 	
 	public void checkBounds() {
 	   checkBounds(false);	
 	}
 	protected void checkBounds(final boolean force) {
 		
 		Rectangle calcBounds = getBounds().getCopy();
 		if (relativeTo == null) return;
 		if (!keepAspect)        return;
 		
 		// We keep aspect if the other axis has a larger range than this axis.
 		final double  thisRange     = getInterval(getRange());
 		final double  relRange      = getInterval(relativeTo.getRange());
 		final boolean equal         = NumberUtils.equalsPercent(thisRange, relRange, 0.001);
 		final boolean isOtherReallyLonger = isLonger(calcBounds, getGraph().getPlotArea().getBounds());
 		final boolean isRelative    = equal && !isOtherReallyLonger; // The parent layouts ys second so x is the right size.
 		final boolean isOtherLarger = relRange>thisRange;
 		
 		if (isRelative || isOtherLarger || force) {
 			boolean otherAxisInvalid = setRelativeAxisBounds(calcBounds, thisRange, relRange);
 			setBounds(calcBounds);
 			if (otherAxisInvalid&&!force) { // force is not recursive
 				relativeTo.checkBounds(true);
 			}
 		}		
 		
 		// y correction for companion axis
 		if (!isHorizontal() && getTickLablesSide() == LabelSide.Primary) { 
 			
 			// We have to ensure that our own ticks have been layed out
 			// because we use their size to set the location of the
 			// relative to field.
 			super.layoutTicks();
 			
 			// Make relativeTo appear near to bottom y axis
 			IFigure yTicks = (IFigure)getChildren().get(1);
 			Dimension yAxisSize = yTicks.getSize();
 			final Rectangle relBounds = relativeTo.getBounds().getCopy();
 			relBounds.y = getBounds().y + yAxisSize.height - 10;
 			relativeTo.setBounds(relBounds);
 		}
 
 	}
 
 	private XYGraph getGraph() {
 		return (XYGraph)getParent();
 	}
 
 	/**
 	 * true if with is longer in its direction in pixels than this axis. 
 	 * @param aspectAxis
 	 * @param relativeTo2
 	 * @return
 	 */
 	private boolean isLonger(Rectangle compare, Rectangle otherBounds) {
 		final int len1 = isYAxis() ? compare.height : compare.width;
 		final int len2 = relativeTo.isYAxis() ? otherBounds.height : otherBounds.width;
 		if (len1==len2) return true;
 		return len2>=len1;
 	}
 
 	private boolean setRelativeAxisBounds (final Rectangle origBounds, 
 										   final double    thisRange, 
 										   final double    relRange) {
 		
 		final Rectangle relBounds = relativeTo.getBounds();
 		int      realPixels = relativeTo.isYAxis() ? relBounds.height : relBounds.width;
 		realPixels-= 2*relativeTo.getMargin();
 		
 		final double    pixRatio  = realPixels/relRange;   // pix / unit
 		int       range     = (int)Math.round(thisRange*pixRatio);    // span for thisRange of them
 		range+=2*getMargin();
 		
 		boolean otherAxisInvalid = false;
 		if (isYAxis()  && range>getGraph().getPlotArea().getBounds().height) otherAxisInvalid = true;
 		if (!isYAxis() && range>getGraph().getPlotArea().getBounds().width)  otherAxisInvalid = true;
 
 		if (isYAxis()) origBounds.height = Math.min(range, getGraph().getPlotArea().getBounds().height); 
 		else           origBounds.width  = Math.min(range, getGraph().getPlotArea().getBounds().width);
 		
 		return otherAxisInvalid;
 	}
 
 	/**
 	 * Should be a method on Range really but 
 	 * @param range
 	 * @return
 	 */
 	private double getInterval(Range range) {
 		return Math.max(range.getLower(), range.getUpper()) - Math.min(range.getLower(), range.getUpper());
 	}
 
 	public boolean isKeepAspect() {
 		return keepAspect;
 	}
 
 	public void setKeepAspect(boolean keepAspect) {
 		this.keepAspect = keepAspect;
 	}
 
 	public Range getMaximumRange() {
 		return maximumRange;
 	}
 
 	/**
 	 * Set with lower<upper, the class will check for if the axis is in reversed mode.
 	 * @param maximumRange
 	 */
 	public void setMaximumRange(Range maximumRange) {
 		if (maximumRange==null) {
 			this.maximumRange = null;
 			return;
 		}
 		if (maximumRange.isMinBigger()) throw new RuntimeException("Maximum range must have lower less than upper. AspectAxis allows for reversed real axes in internally!");
 		this.maximumRange = maximumRange;
 	}
 	
 	@Override
 	public void setRange(double lower, double upper) {
 		final Range norm = normalize(new Range(lower, upper));
 		super.setRange(norm.getLower(), norm.getUpper());
 	}
 	@Override
 	public void setRange(Range range) {
 		normalize(range);
 		super.setRange(range);
 	}
 
 	/**
 	 * 
 	 * @param range
 	 * @return true if range not outside maximum.
 	 */
 	private Range normalize(Range range) {
 		if (maximumRange==null) return range;
 		//if (true) return new Range(range.getLower(), range.getUpper());
 		double lower=range.getLower(), upper=range.getUpper();
 		if (!maximumRange.inRange(lower, true)) lower = range.isMinBigger() ? maximumRange.getUpper() : maximumRange.getLower();
 		if (!maximumRange.inRange(upper, true)) upper = range.isMinBigger() ? maximumRange.getLower() : maximumRange.getUpper();
 		return new Range(lower, upper);
 	}
 
 	public void setLabelData(AbstractDataset labels) {
 		if (labels!=null && labels.getShape().length!=1) throw new RuntimeException("You must only label image data with one dimensional axes!");
 		this.labelData = labels;
 	}
 	
 	public Range getScaleRange() {
 		
 		final Range realRange = getRange();
 		if (labelData==null) return realRange;
 
 		final double lower = labelData.getSize()>(int)Math.round(realRange.getLower())
 				           ? labelData.getElementDoubleAbs((int)Math.round(realRange.getLower()))
 				           : labelData.getElementDoubleAbs(labelData.getSize()-1);
 				        			   
 		final double upper = labelData.getSize()>(int)Math.round(realRange.getUpper())
 				           ? labelData.getElementDoubleAbs((int)Math.round(realRange.getUpper()))
 				           : labelData.getElementDoubleAbs(labelData.getSize()-1);
 		return new Range(lower, upper);
 	}
 
 	@Override
 	public boolean isLog10() {
 		return super.isLogScaleEnabled();
 	}
 
 	@Override
 	public void setLog10(boolean isLog10) {
 		super.setLogScale(isLog10);
 	}
 
 	@Override
 	public double getUpper() {
 		return getRange().getUpper();
 	}
 
 	@Override
 	public double getLower() {
 		return getRange().getLower();
 	}
 
 	@Override
 	public int getValuePosition(double value) {
 		return getValuePosition(value, false);
 	}
 
 	@Override
 	public double getPositionValue(int position) {
 		return getPositionValue(position, false);
 	}
 
 	protected Collection<IAxisListener> axisListeners;
 
 	@Override
 	protected void fireRevalidated(){
 		super.fireRevalidated();
 		
 		final AxisEvent evt = new AxisEvent(this);
 		if (axisListeners!=null) for(IAxisListener listener : axisListeners)
 			listener.revalidated(evt);
 	}
 
 	@Override
 	protected void fireAxisRangeChanged(final Range old_range, final Range new_range){
 		super.fireAxisRangeChanged(old_range, new_range);
 		
 		final AxisEvent evt = new AxisEvent(this, old_range.getLower(), old_range.getUpper(),
 				                                  new_range.getLower(), new_range.getUpper());
 		if (axisListeners!=null)  for(IAxisListener listener : axisListeners)
 			listener.rangeChanged(evt);
 
 	}
 
 	@Override
 	public void addAxisListener(IAxisListener listener) {
 		if (axisListeners==null) axisListeners = new ArrayList<IAxisListener>(3);
 		axisListeners.add(listener);
 	}
 
 	@Override
 	public void removeAxisListener(IAxisListener listener) {
 		if (axisListeners==null) return;
 		axisListeners.remove(listener);
 	}
 
 }
