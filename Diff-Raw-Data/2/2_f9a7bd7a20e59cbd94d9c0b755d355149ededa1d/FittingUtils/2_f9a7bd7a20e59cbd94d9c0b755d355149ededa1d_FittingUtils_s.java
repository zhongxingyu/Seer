 package org.dawnsci.plotting.tools.fitting;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Vector3d;
 
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.preference.FittingConstants;
 import org.dawnsci.plotting.tools.Vector3dutil;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.IAnalysisMonitor;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
 import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.PearsonVII;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.PseudoVoigt;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
 import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class FittingUtils {
 
 	private static Logger logger = LoggerFactory.getLogger(FittingUtils.class);
 	
 	/**
 	 * This method runs a fit on data passed in. It reads preferences to 
 	 * determine which algorithm etc. the user prefers. 
 	 * 
 	 * It can take a while to run. Callers of these utils should protect the
 	 * call by running it from a Job. This method is UI thread safe and may be called
 	 * in any thread.
 	 * 
 	 * The x and y data may have been sliced if the user has completed a selection choice
 	 * which limits the peak fit range.
 	 * 
 	 * The RegionBounds returns are not computed in y. Instead the peak fitter provides the
 	 * y values as zero. So XAXIS regions should be created, then Y data is not required.
 	 * 
 	 * The method also computes x,y abstract data set pairs which are plot fragments for the
 	 * function plotted over the peak region. These can be plotted to show the user the
 	 * fitted function, which they may wish to adjust.
 	 * 
 	 * @return
 	 */
 	public static FittedFunctions getFittedPeaks(final FittedPeaksInfo info) throws Exception {
 		
 		List<CompositeFunction> composites=null;
 		final IOptimizer optimizer = getOptimizer();
 		if (info.getNumPeaks()==1) {
 			double lowOffset = info.getY().min().doubleValue();
 			double highOffset = (Double) info.getY().mean();
 			Offset offset = new Offset(lowOffset, highOffset);
 			double fwhmApprox = info.getX().peakToPeak().doubleValue()/2.0;
 			IdentifiedPeak iniPeak = new IdentifiedPeak(((Number)info.getX().mean()).doubleValue(), info.getX().min().doubleValue(), info.getX().max().doubleValue(), info.getX().peakToPeak().doubleValue()*info.getY().max().doubleValue(), info.getY().max().doubleValue(), 0, info.getX().getSize()-1, Arrays.asList(new Double[] {info.getX().min().doubleValue()+fwhmApprox, info.getX().max().doubleValue()-fwhmApprox}));
 			iniPeak.setFWHM(fwhmApprox);
 			
 			Constructor<? extends APeak> ctor = getPeakType().getClass().getConstructor(IdentifiedPeak.class);
 			APeak localPeak = ctor.newInstance(iniPeak);
 			CompositeFunction comp = new CompositeFunction();
 			comp.addFunction(localPeak);
 			comp.addFunction(offset);
 			optimizer.optimize(new AbstractDataset[] { info.getX() }, info.getY(), comp);
 
 			composites = new ArrayList<CompositeFunction>(1);
 			composites.add(comp);
 		
 		} else {
 			composites =  Generic1DFitter.fitPeakFunctions(info.getIdentifiedPeaks(), info.getX(), info.getY(), getPeakType(), optimizer, getSmoothing(), info.getNumPeaks(), 0.0, false, false, new IAnalysisMonitor() {
 				@Override
 				public boolean hasBeenCancelled() {
 					return info.getMonitor().isCanceled(); // We always use the monitor.isCancelled() the fitting can take a while
 					                             // and should always allow stopping.
 				}
 			});
 		}
 		
 		if (composites==null || composites.isEmpty()) return null;
 				
 		final FittedFunctions bean = new FittedFunctions();
 		for (CompositeFunction function : composites) {
 			
 			final IPeak peak = function.getPeak(0);
 			if (info.getMonitor().isCanceled()) return null;
 			double w = peak.getFWHM();
 			final double position = peak.getPosition();
 			RectangularROI bounds = new RectangularROI(position - w/2, 0, w, 0, 0);
 			
 			final AbstractDataset[] pf = getPeakFunction(info.getX(), info.getY(), function);
 			
 			bean.addFittedPeak(new FittedFunction(function, bounds, pf));
 
 		}
 		
 		bean.setOptimizer(optimizer);
 		if (bean != null && info.getSelectedTrace()!=null)
 			for (FittedFunction p : bean.getFunctionList()) {
 				p.setX((AbstractDataset)info.getSelectedTrace().getXData());
 				p.setY((AbstractDataset)info.getSelectedTrace().getYData());
 				p.setDataTrace(info.getSelectedTrace());
 				p.setQ(getQ(info, p));
 			}
 		return bean;
 	}
 
 	public static Vector3d getQ(FittedPeaksInfo info,FittedFunction p) {
 		LinearROI bounds;
 		try {
 			
 			bounds = (LinearROI)(info.getPlottingSystem().getRegion(info.getSelectedTrace().getName()).getROI());
		} catch (Exception e1) {
 			bounds = new LinearROI();
 			e1.printStackTrace();
 		}
 		double l=bounds.getLength();
 		double [] pt = bounds.getPoint(p.getPosition()/l);
 		
 		IDiffractionMetadata dmeta = null;
 		IDataset set = null;
 		final Collection<ITrace> traces = info.getPlottingSystem().getTraces(IImageTrace.class);
 		final IImageTrace trace = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 		if (trace!=null) {
 			set = trace.getData();
 			final IMetaData      meta = ((AbstractDataset)set).getMetadata();
 			if (meta instanceof IDiffractionMetadata) {
 
 				dmeta = (IDiffractionMetadata)meta;
 			}
 		}
 		QSpace qSpace  = null;
 		Vector3dutil vectorUtil= null;
 		if (dmeta != null) {
 
 			try {
 				DetectorProperties detector2dProperties = dmeta.getDetector2DProperties();
 				DiffractionCrystalEnvironment diffractionCrystalEnvironment = dmeta.getDiffractionCrystalEnvironment();
 				
 				if (!(detector2dProperties == null)){
 					qSpace = new QSpace(detector2dProperties,
 							diffractionCrystalEnvironment,1);
 									
 					vectorUtil = new Vector3dutil(qSpace, pt[0], pt[1]);
 					return new Vector3d(vectorUtil.getQx(),vectorUtil.getQy(),vectorUtil.getQz());
 				}
 			} catch (Exception e) {
 				logger.error("Could not create a detector properties object from metadata", e);
 			}
 		}
 		
 		return new Vector3d(0,0,0);
 	}
 
 	public static List<IdentifiedPeak> getIdentifiedPeaks(final FittedFunctions      fittedPeaks,
 			                                              final AbstractDataset  x,
 								                          final IProgressMonitor monitor) throws Exception {
 		
 		if (fittedPeaks==null) return null;
 		
 		final List<IdentifiedPeak> idpeaks = new ArrayList<IdentifiedPeak>(fittedPeaks.size());
 		for (FittedFunction peak : fittedPeaks.getFunctionList()) {
 			
 			final IPeak       apeak = peak.getPeak();
 			
 			IdentifiedPeak idp = new IdentifiedPeak( apeak.getPosition(), 
 					                                 apeak.getParameter(1).getLowerLimit(), 
 					                                 apeak.getParameter(1).getUpperLimit(), 
 					                                 apeak.getArea(), 
 					                                 apeak.getHeight(), 
 					                                 0, 
 					                                 x.getSize()-1, 
 					                                 null);
 			idp.setFWHM(apeak.getFWHM());
 			idpeaks.add(idp);
 		}
 		return idpeaks;
 	}
 	
 	public static int getPeaksRequired() {
 		return Activator.getDefault().getPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
 	}
 
 	/**
 	 * 
 	 * @param x
 	 * @param peak
 	 * @return
 	 */
 	private static AbstractDataset[] getPeakFunction(AbstractDataset x, final AbstractDataset y, CompositeFunction peak) {
 
 //		double min = peak.getPosition() - (peak.getFWHM()); // Quite wide
 //		double max = peak.getPosition() + (peak.getFWHM());
 //
 //		final AbstractDataset[] a = xintersection(x,y,min,max);
 //		x=a[0];
 //		y=a[1];
 		
 //		CompositeFunction function = new CompositeFunction();
 //		Offset os = new Offset(info.getY().min().doubleValue(), info.getY().max().doubleValue());
 //		function.addFunction(peak);
 //		function.addFunction(os);
 		
 		// We make an x dataset with n times the points of the real data to get a smooth
 		// fitting function
 		final int    factor = Activator.getDefault().getPreferenceStore().getInt(FittingConstants.FIT_SMOOTH_FACTOR);
 		final double xmin = x.min().doubleValue();
 		final double xmax = x.max().doubleValue();
 		final double step = (xmax-xmin)/(x.getSize()*factor);
 		x = AbstractDataset.arange(xmin, xmax, step, AbstractDataset.FLOAT64);
 
 		return new AbstractDataset[]{x,peak.makeDataset(x)};
 		
 	}
 
     /**
      * TODO
      * @return
      */
 	public static int getSmoothing() {
 		return Activator.getDefault().getPreferenceStore().getInt(FittingConstants.SMOOTHING);
 	}
 
 	public static IOptimizer getOptimizer() {
 		return new GeneticAlg(getQuality());
 	}
 
 	/**
 	 * TODO
 	 * @return
 	 */
 	private static double getQuality() {
 		return Activator.getDefault().getPreferenceStore().getDouble(FittingConstants.QUALITY);
 	}
 
 	public static APeak getPeakType() {
 		try {
 			
 			final String peakClass = Activator.getDefault().getPreferenceStore().getString(FittingConstants.PEAK_TYPE);
 			
 			/**
 			 * Could use reflection to save on objects, but there's only 4 of them.
 			 */
 			return getPeakOptions().get(peakClass);
 			
 		} catch (Exception ne) {
 			logger.error("Cannot determine peak type required!", ne);
 			Activator.getDefault().getPreferenceStore().setValue(FittingConstants.PEAK_TYPE, Gaussian.class.getName());
 		    return new Gaussian(1, 1, 1, 1);
 		}
 	}
 
 	/**
 	 * Slices x and y using the x as the reference.
 	 * @param x
 	 * @param y - may be null
 	 * @param startValue
 	 * @param endValue
 	 * @return x and y sliced to the startValue and endValue
 	 */
 	public static AbstractDataset[] xintersection(AbstractDataset x,
 			                                      AbstractDataset y, 
 			                                      final double startValue, 
 			                                      final double endValue) {
 		
 		List<Double> cross = DatasetUtils.crossings(x, startValue);		
 		final int    start = cross==null || cross.isEmpty() 
 				           ? 0
 				           : (int)Math.floor(cross.get(0)); // Lower value
 		
 		cross = DatasetUtils.crossings(x, endValue);		
 		final int    stop  =  cross==null || cross.isEmpty() 
 				           ? x.getSize()-1
 				           : (int)Math.ceil(cross.get(cross.size()-1)); // Upper value
 		
 		x = x.getSlice(new int[] { start }, new int[] { stop }, null);
 		if (y!=null) y = y.getSlice(new int[] { start }, new int[] { stop }, null);		
 		
 		return (y!=null) ? new AbstractDataset[]{x,y} : new AbstractDataset[]{x};
 	}
 	
 	
 	public static Map<String, APeak> getPeakOptions() {
 		final Map<String, APeak> opts = new LinkedHashMap<String, APeak>(4);
 		opts.put(Gaussian.class.getName(),    new Gaussian(1, 1, 1, 1));
 		opts.put(Lorentzian.class.getName(),  new Lorentzian(1, 1, 1, 1));
 		opts.put(PearsonVII.class.getName(),  new PearsonVII(1, 1, 1, 1));
 		opts.put(PseudoVoigt.class.getName(), new PseudoVoigt(1, 1, 1, 1));
 		return opts;
 	}
 	
 	public static int getPolynomialOrderRequired() {
 		return Activator.getDefault().getPreferenceStore().getInt(FittingConstants.POLY_ORDER);
 	}
 	
 	public static FittedFunctions getFittedPolynomial(final FittedPeaksInfo info) throws Exception {
 		
 		Polynomial poly = Fitter.polyFit(new AbstractDataset[] {info.getX()}, info.getY(), 1e-8, getPolynomialOrderRequired());
 		
 		CompositeFunction function = new CompositeFunction();
 		function.addFunction(poly);
 
 		final FittedFunctions bean = new FittedFunctions();
 
 		if (info.getMonitor().isCanceled()) return null;
 		
 		Double max = info.getX().max().doubleValue();
 		Double min = info.getX().min().doubleValue();
 		
 		RectangularROI bounds = new RectangularROI(min,0,max-min,0,0);
 
 		final AbstractDataset[] pf = getPeakFunction(info.getX(), info.getY(), function);
 
 		bean.addFittedPeak(new FittedFunction(function, bounds, pf));
 
 		return bean;
 	}
 }
