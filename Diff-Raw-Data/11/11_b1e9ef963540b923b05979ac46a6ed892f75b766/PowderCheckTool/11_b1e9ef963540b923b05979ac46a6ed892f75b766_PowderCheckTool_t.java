 package org.dawnsci.plotting.tools.powdercheck;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.PlottingFactory;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.part.IPageSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
 import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FFT;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public class PowderCheckTool extends AbstractToolPage {
 	
 	
 	private final static Logger logger = LoggerFactory.getLogger(PowderCheckTool.class);
 
 	private static final double REL_TOL = 1e-10;
 	private static final double ABS_TOL = 1e-10;
 	private static final int MAX_EVAL = 100000;
 	
 	IPlottingSystem system;
 	UpdatePlotJob updatePlotJob;
 
 	private ITraceListener            traceListener;
 	private CalibrantSelectedListener calListener;
 	
 	public PowderCheckTool() {
 		try {
 			system = PlottingFactory.createPlottingSystem();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		this.calListener = new CalibrantSelectedListener() {		
 			@Override
 			public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
 				updateCalibrantLines();
 			}
 		};
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		
 		final IPageSite site = getSite();
 		IActionBars actionbars = site!=null?site.getActionBars():null;
 
 		system.createPlotPart(parent, 
 				getTitle(), 
 				actionbars, 
 				PlotType.XY,
 				this.getViewPart());
 
 		system.getSelectedYAxis().setAxisAutoscaleTight(true);
 		
 		// TODO Listen to other things.
 		this.traceListener = new ITraceListener.Stub() {
 			protected void update(TraceEvent evt) {
 				PowderCheckTool.this.update();
 			}
 		};
 		getPlottingSystem().addTraceListener(traceListener);
 	}
 	
 	
 	private void update() {
 
 		IImageTrace im = getImageTrace();
 		if (im == null) {
 			logger.error("No trace in plotting system");
 			return;
 		}
 		
 		final AbstractDataset ds = (AbstractDataset)im.getData();
 		if (ds==null) return;
 			
 		final IMetaData       m  = ds.getMetadata();
 
 		if (m == null || !(m instanceof IDiffractionMetadata)) {
 			//TODO nicer error
 			logger.error("No Diffraction Metadata");
 			return;
 		}
 		
 		if (updatePlotJob == null) {
 			updatePlotJob= new UpdatePlotJob();
 		}
 		
 		updatePlotJob.cancel();
 		updatePlotJob.setData(ds, (IDiffractionMetadata)m);
 		updatePlotJob.schedule();
 		
 	}
 	
 	private void cleanPlottingSystem(){
 		if (system != null) {		
 			system.reset();
 		}
 	}
 	
 	
 	@Override
 	public void activate() {
 		CalibrationFactory.addCalibrantSelectionListener(calListener);
 		getPlottingSystem().addTraceListener(traceListener);
 		boolean wasActive = isActive();
 		super.activate();
 		if (!wasActive) update();		
 	}
 
 	@Override
 	public void deactivate() {
 		CalibrationFactory.removeCalibrantSelectionListener(calListener);
 		getPlottingSystem().removeTraceListener(traceListener);
 		super.deactivate();
 		cleanPlottingSystem();
 	}
 	
 	@Override
 	public void dispose() {
 		deactivate();
 		if (system!=null) system.dispose();
 		system = null;
 		super.dispose();
 	}
 	
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return system;
 		} else if (clazz == IPlottingSystem.class) {
 		    return system;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 	
 	private void integrateFullSector(AbstractDataset data, IDiffractionMetadata md) {
 		QSpace qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
 		double[] bc = md.getDetector2DProperties().getBeamCentreCoords();
 		int[] shape = data.getShape();
 		double[] farCorner = new double[]{0,0};
 		double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
 		if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
 		if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];
 		double maxDistance = Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
 		SectorROI sroi = new SectorROI(bc[0], bc[1], 0, maxDistance, 0, 2*Math.PI, 1, true, SectorROI.NONE);
 		AbstractDataset[] profile = ROIProfile.sector(data, null, sroi, true, false, false, qSpace, XAxis.Q, false);
 		
 		ArrayList<IDataset> y = new ArrayList<IDataset> ();
 		profile[0].setName("Full image integration");
 		y.add(profile[0]);
 		
 		system.updatePlot1D(profile[4], y, null);
 		
 		//fitPeaksToTrace(profile[4], profile[0]);
 	}
 	
 	
 
 	
 	private void fitPeaksToTrace(final AbstractDataset x, final AbstractDataset y) {
 		
 		
 		AbstractDataset yfft = FFT.fft(y);
 		
 		for (int i = 20; i < yfft.getSize(); i++) {
 			yfft.set(0, i);
 		}
 //		
 		yfft = FFT.ifft(yfft).real();
 		
 		system.updatePlot1D(x, Arrays.asList(new AbstractDataset[]{yfft}), null);
 		
 		
 //		List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
 //		final double[] qVals = new double[spacings.size()];
 //		
 //		for (int i = 0 ; i < spacings.size(); i++) {
 //			qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
 //		}
 //		
 //		final CompositeFunction cf = new CompositeFunction();
 //		
 //		double qMax = x.max().doubleValue();
 //		double qMin = x.min().doubleValue();
 //		
 //		for (double q : qVals) {
 //			if (q > qMax || q < qMin) continue;
 //			Gaussian g = new Gaussian(q,0.02,1000);
 //			g.getParameter(0).setFixed(true);
 //			cf.addFunction(g);
 //		}
 //		
 //		double[] initParam = new double[cf.getFunctions().length*2];
 //		
 //		int i = 0;
 //		for (IFunction func : cf.getFunctions()) {
 //			initParam[i++] = func.getParameter(1).getValue();
 //			initParam[i++] = func.getParameter(2).getValue();
 //		}
 //		
 //		final AbstractDataset yfit = AbstractDataset.zeros(x, AbstractDataset.FLOAT64);
 //		
 //		 MultivariateOptimizer opt = new SimplexOptimizer(REL_TOL,ABS_TOL);
 //		    
 //		    MultivariateFunction fun = new MultivariateFunction() {
 //				
 //				@Override
 //				public double value(double[] arg0) {
 //					
 //					int j = 0;
 //					for (IFunction func : cf.getFunctions()) {
 //						
 //						double[] p = func.getParameterValues();
 //						p[1] = arg0[j++];
 //						p[2] = arg0[j++];
 //						func.setParameterValues(p);
 ////						func.getParameter(1).setValue(arg0[j++]);
 ////						func.getParameter(2).setValue(arg0[j++]);
 //					}
 //					
 //					for (int i = 0 ; i < yfit.getSize() ; i++) {
 //						yfit.set(cf.val(x.getDouble(i)), i);
 //					}
 //					
 //					double test = y.residual(yfit);
 //					
 //					return y.residual(yfit);
 //
 //				}
 //			};
 //		    
 //			PointValuePair result = opt.optimize(new InitialGuess(initParam), GoalType.MINIMIZE,
 //					new ObjectiveFunction(fun), new MaxEval(MAX_EVAL),
 //					new NelderMeadSimplex(initParam.length));	
 			
 			//return result.getPointRef();
 		
 //		try {
 //			Fitter.fit(x, y,new ApacheNelderMead(), cf);
 //		} catch (Exception e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //			return;
 //		}
 		
 //		AbstractDataset yfit = AbstractDataset.zeros(x, AbstractDataset.FLOAT64);
 //		
 //		for (int j = 0 ; j < yfit.getSize() ; j++) {
 //			yfit.set(cf.val(x.getDouble(j)), j);
 //		}
 //		
 //		yfit.setName("Fit");
 //		
 //		system.updatePlot1D(x, Arrays.asList(new AbstractDataset[]{yfit}), null);
 	}
 	
 	private void updateCalibrantLines() {
 		
 		List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
 		final double[] qVals = new double[spacings.size()];
 		
 		for (int i = 0 ; i < spacings.size(); i++) {
 			qVals[i] = (Math.PI*2)/(spacings.get(i).getDNano()*10);
 		}
 
 		Display.getDefault().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 
 				if (system.getPlotComposite() == null) return;
 				
 				IAxis ax = system.getSelectedXAxis();
 
 				double low = ax.getLower();
 				double up = ax.getUpper();
 
 				for (IRegion r : system.getRegions()) system.removeRegion(r);
 				for (int i = 0; i < qVals.length; i++) {
 					if (qVals[i] < low || qVals[i] > up) continue;
 
 					try {
 						RectangularROI roi = new RectangularROI(qVals[i], 0, 1, 1, 0);
 						IRegion reg = system.getRegion("Q value: " + qVals[i]);
 						if (reg!=null) system.removeRegion(reg);
 						
 						final IRegion area = system.createRegion("Q value: " + qVals[i], RegionType.XAXIS_LINE);
 						area.setROI(roi);
 						area.setRegionColor(ColorConstants.gray);
 						area.setUserRegion(false);
 						system.addRegion(area);
 						area.setMobile(false);
 					} catch (Exception e) {
 						logger.error("Region is already there", e);
 					}
 
 				}
 			}
 		});
 	}
 	
 	@Override
 	public Control getControl() {
 		if (system != null) return system.getPlotComposite();
 		return null;
 	}
 
 	@Override
 	public void setFocus() {
 		if (system != null) system.setFocus();
 
 	}
 
 	private class UpdatePlotJob extends Job {
 		
 		AbstractDataset dataset;
 		IDiffractionMetadata metadata;
 
 		public UpdatePlotJob() {
 			super("Integrate image and plot");
 			// TODO Auto-generated constructor stub
 		}
 		
 		private void setData(AbstractDataset ds, IDiffractionMetadata md) {
 			dataset = ds;
 			metadata = md;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			if (system.getPlotComposite()==null) return Status.CANCEL_STATUS;
 			cleanPlottingSystem();
 			return integrateQuadrants(dataset,metadata, monitor);
 
 		}
 		
 		private IStatus integrateQuadrants(AbstractDataset data, IDiffractionMetadata md, IProgressMonitor monitor) {
 			QSpace qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
 			double[] bc = md.getDetector2DProperties().getBeamCentreCoords();
 			int[] shape = data.getShape();
 			
 			double[] farCorner = new double[]{0,0};
 			double[] centre = md.getDetector2DProperties().getBeamCentreCoords();
 			if (centre[0] < shape[0]/2.0) farCorner[0] = shape[0];
 			if (centre[1] < shape[1]/2.0) farCorner[1] = shape[1];
 			double maxDistance = Math.sqrt(Math.pow(centre[0]-farCorner[0],2)+Math.pow(centre[1]-farCorner[1],2));
 			SectorROI sroi = new SectorROI(bc[0], bc[1], 0, maxDistance, Math.PI/4 - Math.PI/8, Math.PI/4 + Math.PI/8, 1, true, SectorROI.INVERT);
 			AbstractDataset[] profile = ROIProfile.sector(data, null, sroi, true, false, false, qSpace, XAxis.Q, false);
 			
 			ArrayList<IDataset> y = new ArrayList<IDataset> ();
 			profile[0].setName("Top right");
 			y.add(profile[0]);
 			if (system == null) {
 				logger.error("Plotting system is null");
 				return Status.CANCEL_STATUS;
 			}
 				
 			List<ITrace> traces = system.updatePlot1D(profile[4], y, null);
 			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.darkBlue);
 			y.remove(0);
 			
 			final AbstractDataset reflection = profile[2];
 			final AbstractDataset axref = profile[6];
 			reflection.setName("Bottom left");
 			y.add(reflection);
 			traces = system.updatePlot1D(axref, y, null);
 			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.lightBlue);
 			y.remove(0);
 			
 			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
 			
 			sroi = new SectorROI(bc[0], bc[1], 0, maxDistance, 3*Math.PI/4 - Math.PI/8, 3*Math.PI/4 + Math.PI/8, 1, true, SectorROI.INVERT);
 			profile = ROIProfile.sector(data, null, sroi, true, false, false, qSpace, XAxis.Q, false);
 			
 			profile[0].setName("Bottom right");
 			y.add(profile[0]);
 			traces = system.updatePlot1D(profile[4], y, null);
 			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.darkGreen);
 			y.remove(0);
 			
 			final AbstractDataset reflection2 = profile[2];
 			final AbstractDataset axref2 = profile[6];
 			reflection2.setName("Top left");
 			y.add(reflection2);
 			traces = system.updatePlot1D(axref2, y, null);
 			//((ILineTrace)traces.get(0)).setTraceColor(ColorConstants.lightGreen);
 			updateCalibrantLines();
 			
 			return Status.OK_STATUS;
 		}
 
 	}
 	
 }
