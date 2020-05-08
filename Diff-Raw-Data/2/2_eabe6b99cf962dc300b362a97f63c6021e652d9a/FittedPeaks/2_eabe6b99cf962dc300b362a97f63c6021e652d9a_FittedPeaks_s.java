 package org.dawb.workbench.plotting.tools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.annotation.IAnnotation;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.RegionBounds;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.eclipse.draw2d.ColorConstants;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
 import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
 
 /**
  * Stores various information about the fit, including the IRegions
  * and other GUI things.
  * 
  * @author fcp94556
  *
  */
 public class FittedPeaks {
 
 	private List<RegionBounds>       peakBounds;
 	private List<? extends IPeak>    peaks;
 	private List<IRegion>            peakAreaRegions;
 	private List<IRegion>            peakLineRegions;
 	private List<ITrace>             peakTraces;
 	private List<IAnnotation>        peakAnnotations;
 	private IOptimizer               optimizer;
 	
 	public FittedPeaks() {
 		this.peakAreaRegions = new ArrayList<IRegion>(7);
 		this.peakLineRegions = new ArrayList<IRegion>(7);
 		this.peakTraces  = new ArrayList<ITrace>(7);
 		this.peakAnnotations  = new ArrayList<IAnnotation>(7);
 	}
 
 	public void dispose() {
 		if (peakBounds!=null) peakBounds.clear();
 		peakBounds = null;
 		
 		if (peaks!=null) peaks.clear();
 		peaks = null;
 		
 		if (peakAreaRegions!=null) peakAreaRegions.clear();
 		peakAreaRegions = null;
 
 		if (peakLineRegions!=null) peakLineRegions.clear();
 		peakLineRegions = null;
 		
 		if (peakTraces!=null) peakTraces.clear();
 		peakTraces = null;
 		
 		if (peakAnnotations!=null) peakAnnotations.clear();
 		peakAnnotations = null;
 
 		optimizer = null;
 	}
 	
 	/**
 	 * Not thread safe, UI call.
 	 */
 	public void activate() {
 		for (IRegion region : peakAreaRegions) region.setVisible(true);
 		for (IRegion region : peakLineRegions) region.setVisible(true);
 		for (ITrace  trace  : peakTraces)  trace.setVisible(true);
 		for (IAnnotation  ann  : peakAnnotations)  ann.setVisible(true);
 	}
 	/**
 	 * Not thread safe, UI call.
 	 */
 	public void deactivate() {
 		for (IRegion region : peakAreaRegions) region.setVisible(false);
 		for (IRegion region : peakLineRegions) region.setVisible(false);
 		for (ITrace  trace  : peakTraces)  trace.setVisible(false);
 		for (IAnnotation  ann  : peakAnnotations)  ann.setVisible(false);
 	}
 
 	public void setAreasVisible(boolean isVis) {
 		for (IRegion region : peakAreaRegions) region.setVisible(isVis);
 	}
 
 	public void setPeaksVisible(boolean isVis) {
 		for (IRegion region : peakLineRegions) region.setVisible(isVis);
 	}
 
 	public void setTracesVisible(boolean isVis) {
 		for (ITrace  trace  : peakTraces)  trace.setVisible(isVis);
 	}
 	public void setAnnotationsVisible(boolean isVis) {
 		for (IAnnotation  ann  : peakAnnotations)  ann.setVisible(isVis);
 	}
 	
 	public void setSelectedPeak(int ipeak) {
 		for (IRegion region : peakAreaRegions) region.setRegionColor(ColorConstants.orange);
 		peakAreaRegions.get(ipeak).setRegionColor(ColorConstants.red);
 		
 		for (ITrace trace : peakTraces) ((ILineTrace)trace).setTraceColor(ColorConstants.black);
 		
 		for (IAnnotation  ann  : peakAnnotations)  ann.setAnnotationColor(ColorConstants.black);
 		
 		final ILineTrace trace = ((ILineTrace)peakTraces.get(ipeak));
 		trace.setTraceColor(ColorConstants.darkGreen);
 		
 		peakAnnotations.get(ipeak).setAnnotationColor(ColorConstants.darkGreen);

		trace.repaint();
 	}
 
 	/**
 	 * x and y pairs for the fitted functions.
 	 */
 	private List<AbstractDataset[]>  functionData;
 
 	
 	public List<RegionBounds> getPeakBounds() {
 		return peakBounds;
 	}
 	public void setPeakBounds(List<RegionBounds> peakBounds) {
 		this.peakBounds = peakBounds;
 	}
 	public List<? extends IPeak> getPeaks() {
 		return peaks;
 	}
 	public void setPeaks(List<? extends IPeak> peakFunctions) {
 		this.peaks = peakFunctions;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((functionData == null) ? 0 : functionData.hashCode());
 		result = prime * result
 				+ ((optimizer == null) ? 0 : optimizer.hashCode());
 		result = prime * result
 				+ ((peakAreaRegions == null) ? 0 : peakAreaRegions.hashCode());
 		result = prime * result
 				+ ((peakBounds == null) ? 0 : peakBounds.hashCode());
 		result = prime * result
 				+ ((peaks == null) ? 0 : peaks.hashCode());
 		result = prime * result
 				+ ((peakLineRegions == null) ? 0 : peakLineRegions.hashCode());
 		result = prime * result
 				+ ((peakTraces == null) ? 0 : peakTraces.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		FittedPeaks other = (FittedPeaks) obj;
 		if (functionData == null) {
 			if (other.functionData != null)
 				return false;
 		} else if (!functionData.equals(other.functionData))
 			return false;
 		if (optimizer == null) {
 			if (other.optimizer != null)
 				return false;
 		} else if (!optimizer.equals(other.optimizer))
 			return false;
 		if (peakAreaRegions == null) {
 			if (other.peakAreaRegions != null)
 				return false;
 		} else if (!peakAreaRegions.equals(other.peakAreaRegions))
 			return false;
 		if (peakBounds == null) {
 			if (other.peakBounds != null)
 				return false;
 		} else if (!peakBounds.equals(other.peakBounds))
 			return false;
 		if (peaks == null) {
 			if (other.peaks != null)
 				return false;
 		} else if (!peaks.equals(other.peaks))
 			return false;
 		if (peakLineRegions == null) {
 			if (other.peakLineRegions != null)
 				return false;
 		} else if (!peakLineRegions.equals(other.peakLineRegions))
 			return false;
 		if (peakTraces == null) {
 			if (other.peakTraces != null)
 				return false;
 		} else if (!peakTraces.equals(other.peakTraces))
 			return false;
 		return true;
 	}
 	public List<AbstractDataset[]> getFunctionData() {
 		return functionData;
 	}
 	public void setFunctionData(List<AbstractDataset[]> functionData) {
 		this.functionData = functionData;
 	}
 	public List<ITrace> getPeakTraces() {
 		return peakTraces;
 	}
 	public void setPeakTraces(ArrayList<ITrace> peakTraces) {
 		this.peakTraces = peakTraces;
 	}
 
 	/**
 	 * Remove stored traces from a plotting system.
 	 * @param sys
 	 */
 	public void removeSelections(IPlottingSystem sys) {
 		for (ITrace  trace   : peakTraces)   sys.removeTrace(trace);
 		for (IRegion region  : peakAreaRegions)  sys.removeRegion(region);
 		for (IRegion region  : peakLineRegions)  sys.removeRegion(region);
 		for (IAnnotation ann  : peakAnnotations)  sys.removeAnnotation(ann);
 		peakAreaRegions.clear();
 		peakLineRegions.clear();
 		peakTraces.clear();
 		peakAnnotations.clear();
 	}
 
 	public void addAreaRegion(IRegion region) {
 		peakAreaRegions.add(region);
 	}
 	public void addLineRegion(IRegion region) {
 		peakLineRegions.add(region);
 	}
 
 	public void addTrace(ILineTrace trace) {
 		peakTraces.add(trace);
 	}
 
 
 	public void addAnnotation(IAnnotation ann) {
 		peakAnnotations.add(ann);
 	}
 
 	public int size() {
 		return peaks.size();
 	}
 
 	public String getPeakName(int peakNumber) {
 		try {
 		    return peakTraces.get(peakNumber).getName();
 		} catch (IndexOutOfBoundsException ne) {
 			return null;
 		}
 	}
 
 	public double getPosition(Integer peakNumber) {
 		try {
 			return ((APeak)this.peaks.get(peakNumber)).getPosition();
 		} catch (IndexOutOfBoundsException ne) {
 			return Double.NaN;
 		}
 	}
 	
 	public double getFWHM(Integer peakNumber) {
 		try {
 			return ((APeak)this.peaks.get(peakNumber)).getFWHM();
 		} catch (IndexOutOfBoundsException ne) {
 			return Double.NaN;
 		}
 	}
 	
 	public double getArea(Integer peakNumber) {
 		try {
 			return ((APeak)this.peaks.get(peakNumber)).getArea();
 		} catch (IndexOutOfBoundsException ne) {
 			return Double.NaN;
 		}
 	}
 	
 	public String getPeakType(Integer peakNumber) {
 		try {
 		    IPeak peak =  peaks.get(peakNumber);
 		    return peak.getClass().getSimpleName();
 		} catch (IndexOutOfBoundsException ne) {
 			return null;
 		}
 	}
 	
 	public String getAlgorithmType(Integer peakNumber) {
 		return getOptimizer().getClass().getSimpleName();
 	}
 
 	public boolean isEmpty() {
 		if (peaks==null) return true;
 		return peaks.isEmpty();
 	}
 
 	public IOptimizer getOptimizer() {
 		return optimizer;
 	}
 
 	public void setOptimizer(IOptimizer optimizer) {
 		this.optimizer = optimizer;
 	}
 
 	public List<IAnnotation> getPeakAnnotations() {
 		return peakAnnotations;
 	}
 
 	public void setPeakAnnotations(List<IAnnotation> peakAnnoations) {
 		this.peakAnnotations = peakAnnoations;
 	}
 
 
 }
