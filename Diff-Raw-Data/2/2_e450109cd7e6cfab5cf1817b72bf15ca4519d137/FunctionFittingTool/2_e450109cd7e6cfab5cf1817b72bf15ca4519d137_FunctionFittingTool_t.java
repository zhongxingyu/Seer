 package org.dawnsci.plotting.tools.fitting;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.workbench.jmx.UserPlotBean;
 import org.dawnsci.common.widgets.gda.function.FunctionFittingWidget;
 import org.dawnsci.common.widgets.gda.function.IFittedFunctionInvalidatedEvent;
 import org.dawnsci.common.widgets.gda.function.ModelModifiedAdapter;
 import org.dawnsci.common.widgets.gda.function.descriptors.DefaultFunctionDescriptorProvider;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.trace.ILineTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
 import org.dawnsci.plotting.tools.Activator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
 import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
 import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants.FIT_ALGORITHMS;
 import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunctionService;
 import uk.ac.diamond.scisoft.analysis.optimize.ApacheNelderMead;
 import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
 import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class FunctionFittingTool extends AbstractToolPage implements
 		IFunctionService {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(FunctionFittingTool.class);
 
 	private static final Image FIT = Activator
 			.getImage("icons/chart_curve_go.png");
 	private static final Image UPDATE = Activator
 			.getImage("icons/arrow_refresh_small.png");
 
 	private Control control;
 	private boolean autoRefit;
 
 	protected IROIListener roiListener = new FunctionFittingROIListener();
 	protected IRegion region = null;
 	private CompositeFunction compFunction = null;
 	protected ILineTrace estimate;
 	private ILineTrace fitTrace;
 	private CompositeFunction resultFunction;
 
 	private UpdateFitPlotJob updateFittedPlotJob;
 	private ITraceListener traceListener = new FunctionFittingTraceListener();
 
 	private Text chiSquaredValueText;
 	private FunctionFittingWidget functionWidget;
 
 	private Button updateAllButton;
 
 	private IPreferenceStore prefs = Activator.getPlottingPreferenceStore();
 
 	private boolean connectLater;
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_1D;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(1, false));
 		GridUtils.removeMargins(composite);
 		// composite is our top level control
 		control = composite;
 
 		Composite infoComposite = new Composite(composite, SWT.NONE);
 		infoComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1));
 		infoComposite.setLayout(new GridLayout(2, true));
 
 		Composite actionComposite = new Composite(infoComposite, SWT.NONE);
 		actionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 		actionComposite.setLayout(new GridLayout(2, true));
 
 		final Button autoRefitButton = new Button(actionComposite, SWT.TOGGLE);
 		autoRefitButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 		autoRefitButton.setText("Auto Refit");
 		autoRefitButton.setImage(FIT);
 		autoRefitButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				autoRefit = autoRefitButton.getSelection();
 				updateFunctionPlot(false);
 			}
 		});
 
 		updateAllButton = new Button(actionComposite, SWT.PUSH);
 		updateAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 		updateAllButton.setText("Update All");
 		updateAllButton.setImage(UPDATE);
 		updateAllButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				updateAllParameters();
 			}
 		});
 
 		fitOnceButton = new Button(actionComposite, SWT.PUSH);
 		fitOnceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1));
 		fitOnceButton.setText("Fit Once");
		fitOnceButton.setEnabled(true);
 		fitOnceButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				updateFunctionPlot(true);
 			}
 		});
 
 		Composite resultsComposite = new Composite(infoComposite, SWT.BORDER);
 		resultsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1));
 		resultsComposite.setLayout(new GridLayout(1, false));
 
 		Label chiSquaredInfoLabel = new Label(resultsComposite, SWT.NONE);
 		chiSquaredInfoLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
 				true, false, 1, 1));
 		chiSquaredInfoLabel.setText("Normalised goodness of fit:");
 
 		chiSquaredValueText = new Text(resultsComposite, SWT.READ_ONLY
 				| SWT.CENTER);
 		chiSquaredValueText.setBackground(resultsComposite.getBackground());
 		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		chiSquaredValueText.setLayoutData(gd);
 		chiSquaredValueText.setText("Not Calculated");
 
 		functionWidget = new FunctionFittingWidget(composite,
 				new DefaultFunctionDescriptorProvider(), getSite());
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(functionWidget);
 
 		// Initialise with a simple function.
 		if (compFunction == null) compFunction = new CompositeFunction();
 		functionWidget.setInput(compFunction);
 		functionWidget.expandAll();
 
 		functionWidget.addModelModifiedListener(new ModelModifiedAdapter() {
 			@Override
 			protected void modelModified() {
 				compFunctionModified();
 			}
 
 			@Override
 			public void fittedFunctionInvalidated(
 					IFittedFunctionInvalidatedEvent event) {
 				resultFunction = null;
 				chiSquaredValueText.setText("Not Calculated");
 				updateAllButton.setEnabled(false);
 				// TODO remove fitted trace
 			}
 		});
 
 		getSite().setSelectionProvider(functionWidget.getFunctionViewer());
 		fillActionBar(getSite().getActionBars());
 
 		if (connectLater) {
 			connectPlotSystemListeners();
 			compFunctionModified();
 		}
 	}
 
 	@Override
 	public Control getControl() {
 		return control;
 	}
 
 	@Override
 	public void setFocus() {
 		functionWidget.setFocus();
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 		if (functionWidget != null) {
 			// XXX because activate can be called before the controls are
 			// created, defer connecting the listeners in that case.
 			connectPlotSystemListeners();
 		} else {
 			connectLater = true;
 		}
 	}
 
 	private void compFunctionModified() {
 		updateFunctionPlot(false);
 		fitOnceButton
 				.setEnabled(functionWidget.isValid() && compFunction != null
 						&& compFunction.getNoOfFunctions() != 0);
 	}
 
 	private void connectPlotSystemListeners() {
 		try {
 			getPlottingSystem().addTraceListener(traceListener);
 
 			region = getPlottingSystem().getRegion("fit_region");
 			if (region == null) {
 				region = getPlottingSystem().createRegion("fit_region",
 						RegionType.XAXIS);
 
 				region.setROI(new RectangularROI(getPlottingSystem()
 						.getSelectedXAxis().getLower(), 0, getPlottingSystem()
 						.getSelectedXAxis().getUpper()
 						- getPlottingSystem().getSelectedXAxis().getLower(),
 						100, 0));
 				getPlottingSystem().addRegion(region);
 			} else {
 				region.setVisible(true);
 			}
 			region.addROIListener(roiListener);
 			updateFunctionPlot(false);
 
 		} catch (Exception e) {
 			logger.error("Failed to activate function fitting tool", e);
 		}
 	}
 
 	@Override
 	public void deactivate() {
 		if (region != null) {
 			region.removeROIListener(roiListener);
 			region.setVisible(false);
 		}
 		Collection<ITrace> traces = getPlottingSystem().getTraces();
 		if (traces.contains(estimate))
 			getPlottingSystem().removeTrace(estimate);
 		if (traces.contains(fitTrace))
 			getPlottingSystem().removeTrace(fitTrace);
 
 		getPlottingSystem().removeTraceListener(traceListener);
 
 		super.deactivate();
 	}
 
 	private void setChiSquaredValue(double value) {
 		chiSquaredValueText.setText(Double.toString(value));
 	}
 
 	private void fillActionBar(IActionBars actionBars) {
 		IToolBarManager manager = actionBars.getToolBarManager();
 		manager.add(new ExportFittingDataAction());
 		manager.add(new ImportFittingDataAction());
 
 		IMenuManager menuManager = actionBars.getMenuManager();
 		menuManager.add(new Separator());
 		menuManager.add(new OpenFittingToolPreferencesAction());
 		menuManager.add(new Separator());
 	}
 
 	private void updateAllParameters() {
 		if (resultFunction != null) {
 			double[] parameterValues = resultFunction.getParameterValues();
 			compFunction.setParameterValues(parameterValues);
 			functionWidget.refresh();
 			compFunctionModified();
 			updateFunctionPlot(false);
 		}
 	}
 
 	private void updateFunctionPlot(boolean force) {
 		if (!functionWidget.isValid()) {
 			return;
 		}
 		getPlottingSystem().removeTraceListener(traceListener);
 		boolean firstTrace = true;
 		for (ITrace selectedTrace : getPlottingSystem().getTraces()) {
 			if (selectedTrace instanceof ILineTrace) {
 				ILineTrace trace = (ILineTrace) selectedTrace;
 				if (trace.isUserTrace() && firstTrace) {
 					firstTrace = false;
 					// We chop x and y by the region bounds. We assume the
 					// plot is an XAXIS selection therefore the indices in
 					// y = indices chosen in x.
 					RectangularROI roi = (RectangularROI) region.getROI();
 
 					final double[] p1 = roi.getPointRef();
 					final double[] p2 = roi.getEndPoint();
 
 					// We peak fit only the first of the data sets plotted
 					// for now.
 					AbstractDataset x = (AbstractDataset) trace.getXData();
 					AbstractDataset y = (AbstractDataset) trace.getYData();
 
 					try {
 						AbstractDataset[] a = Generic1DFitter.xintersection(x,
 								y, p1[0], p2[0]);
 						x = a[0];
 						y = a[1];
 					} catch (Throwable npe) {
 						continue;
 					}
 
 					estimate = (ILineTrace) getPlottingSystem().getTrace(
 							"Estimate");
 					if (estimate == null) {
 						estimate = getPlottingSystem().createLineTrace(
 								"Estimate");
 						estimate.setUserTrace(false);
 						estimate.setTraceType(ILineTrace.TraceType.DASH_LINE);
 						getPlottingSystem().addTrace(estimate);
 					}
 
 					if (compFunction != null) {
 						DoubleDataset functionData = compFunction
 								.calculateValues(x);
 						estimate.setData(x, functionData);
 					}
 
 					// System.out.println(x);
 					// System.out.println(y);
 
 					getPlottingSystem().repaint();
 
 					updateFittedPlot(force, x, y);
 				}
 			}
 		}
 		refreshViewer();
 		getPlottingSystem().addTraceListener(traceListener);
 	}
 
 	private void updateFittedPlot(boolean force, final AbstractDataset x,
 			final AbstractDataset y) {
 
 		if (force || autoRefit) {
 
 			if (updateFittedPlotJob == null) {
 				updateFittedPlotJob = new UpdateFitPlotJob("Update Fitted Plot");
 			}
 			updateFittedPlotJob.setData(x, y);
 			updateFittedPlotJob.schedule();
 		}
 
 	}
 
 	// TODO this job is sometimes unstopped at shutdown, add to dispose
 	private class UpdateFitPlotJob extends Job {
 
 		public UpdateFitPlotJob(String name) {
 			super(name);
 		}
 
 		private AbstractDataset x;
 		private AbstractDataset y;
 
 		public void setData(AbstractDataset x, AbstractDataset y) {
 			this.x = x.clone();
 			this.y = y.clone();
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					if (fitTrace != null)
 						fitTrace.setVisible(false);
 					getPlottingSystem().repaint();
 				}
 			});
 
 			try {
 				double accuracy = prefs.getDouble(FittingConstants.FIT_QUALITY);
 				logger.debug("Accuracy is set to {}", accuracy);
 				int algoId = prefs.getInt(FittingConstants.FIT_ALGORITHM);
 				FIT_ALGORITHMS algorithm = FIT_ALGORITHMS.fromId(algoId);
 
 				IOptimizer fitMethod = null;
 				if (algorithm == null) {
 					fitMethod = new ApacheNelderMead();
 				} else {
 					switch (algorithm) {
 					default:
 					case APACHENELDERMEAD:
 						fitMethod = new ApacheNelderMead();
 						break;
 					case GENETIC:
 						fitMethod = new GeneticAlg(accuracy);
 						break;
 					}
 				}
 
 				// TODO (review race condition) this copy of compFunction
 				// appears to happen "late" if the job is not scheduled for a
 				// "while" then the compFunction can change (by GUI interaction)
 				// between when the estimate was plotted and the fit is started.
 				// TODO There is no way of cancelling this fit. If an errant fit
 				// is attempted (e.g. Add(Gaussian(0,0,0), Box(0,0,0,0,0))) the
 				// fitter appears to run forever. This is (one of?) the reasons
 				// that "Job found still running after platform shutdown. Jobs
 				// should be canceled by the plugin that scheduled them during
 				// shutdown:
 				// org.dawnsci.plotting.tools.fitting.FunctionFittingTool$UpdateFitPlotJob"
 				// error is observed.
 
 				resultFunction = Fitter.fit(x, y, fitMethod, compFunction
 						.copy().getFunctions());
 			} catch (Exception e) {
 				return Status.CANCEL_STATUS;
 			}
 
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					getPlottingSystem().removeTraceListener(traceListener);
 					setChiSquaredValue(resultFunction.residual(true, y, null,
 							new IDataset[] { x }) / x.count());
 
 					fitTrace = (ILineTrace) getPlottingSystem().getTrace("Fit");
 					if (fitTrace == null) {
 						fitTrace = getPlottingSystem().createLineTrace("Fit");
 						fitTrace.setUserTrace(false);
 						fitTrace.setLineWidth(2);
 						getPlottingSystem().addTrace(fitTrace);
 					}
 
 					System.out.println("Plotting");
 					System.out.println(resultFunction);
 					DoubleDataset resultData = resultFunction
 							.calculateValues(x);
 					fitTrace.setData(x, resultData);
 					fitTrace.setVisible(true);
 
 					getPlottingSystem().repaint();
 					refreshViewer();
 					getPlottingSystem().addTraceListener(traceListener);
 
 					functionWidget.setFittedInput(resultFunction);
 					updateAllButton.setEnabled(true);
 				}
 			});
 
 			return Status.OK_STATUS;
 		}
 
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Class key) {
 		if (key == IFunctionService.class)
 			return this;
 		return super.getAdapter(key);
 	}
 
 	@Override
 	public Map<String, IFunction> getFunctions() {
 
 		HashMap<String, IFunction> functions = new HashMap<String, IFunction>();
 
 		if (compFunction != null) {
 			for (int i = 0; i < compFunction.getNoOfFunctions(); i++) {
 				String key = String.format("%03d_initial_%s", i, compFunction
 						.getFunction(i).getName());
 				functions.put(key, compFunction.getFunction(i));
 			}
 		}
 
 		if (resultFunction != null) {
 			for (int i = 0; i < resultFunction.getNoOfFunctions(); i++) {
 				String key = String.format("%03d_result_%s", i, resultFunction
 						.getFunction(i).getName());
 				functions.put(key, resultFunction.getFunction(i));
 			}
 		}
 
 		return functions;
 	}
 
 	@Override
 	public void setFunctions(Map<String, IFunction> functions) {
 		// clear the composite function
 		compFunction = new CompositeFunction();
 		for (String key : functions.keySet()) {
 			if (key.contains("_initial_")) {
 				compFunction.addFunction((AFunction) functions.get(key));
 			}
 		}
 
 		resultFunction = new CompositeFunction();
 		updateAllButton.setEnabled(true);
 		for (String key : functions.keySet()) {
 			if (key.contains("_result_")) {
 				resultFunction.addFunction((AFunction) functions.get(key));
 			}
 		}
 
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				functionWidget.setInput(compFunction);
 				functionWidget.setFittedInput(resultFunction);
 				compFunctionModified();
 
 				getPlottingSystem().repaint();
 				refreshViewer();
 			}
 		});
 	}
 
 	/**
 	 * Set the list of functions available to the user to select from
 	 *
 	 * @param functions
 	 *            list of functions
 	 */
 	public void setFunctionList(IFunction[] functions) {
 
 	}
 
 	private void refreshViewer() {
 		// TODO what is the condition that this can be null???
 		if (functionWidget != null)
 			functionWidget.refresh();
 	}
 
 	/*
 	 * Update function plot if region of interest changes
 	 */
 	private class FunctionFittingROIListener implements IROIListener {
 		@Override
 		public void roiDragged(ROIEvent evt) {
 			return;
 		}
 
 		@Override
 		public void roiChanged(ROIEvent evt) {
 			updateFunctionPlot(false);
 		}
 
 		@Override
 		public void roiSelected(ROIEvent evt) {
 		}
 	}
 
 	private class FunctionFittingTraceListener implements ITraceListener {
 		boolean updating = false;
 
 		private void update() {
 			if (!updating) {
 				try {
 					updating = true;
 					updateFunctionPlot(false);
 				} finally {
 					updating = false;
 				}
 			}
 		}
 
 		@Override
 		public void tracesUpdated(TraceEvent evt) {
 		}
 
 		@Override
 		public void tracesRemoved(TraceEvent evet) {
 		}
 
 		@Override
 		public void tracesAdded(TraceEvent evt) {
 			update();
 		}
 
 		@Override
 		public void traceWillPlot(TraceWillPlotEvent evt) {
 		}
 
 		@Override
 		public void traceUpdated(TraceEvent evt) {
 			update();
 		}
 
 		@Override
 		public void traceRemoved(TraceEvent evt) {
 		}
 
 		@Override
 		public void traceCreated(TraceEvent evt) {
 		}
 
 		@Override
 		public void traceAdded(TraceEvent evt) {
 			update();
 		}
 	}
 
 	/**
 	 * TODO review setToolData / setToolData here, it seems not possibly
 	 * correct. If getToolData is called before setToolData a NPE happens. Seems
 	 * illogical. Perhaps this is a remnant from an unimplemented or old
 	 * thing????
 	 */
 	private Map<String, Serializable> functions = null;
 
 	private Button fitOnceButton;
 
 	/**
 	 * Override to set the tool data to something specific
 	 *
 	 * @param toolData
 	 */
 	@Override
 	public void setToolData(Serializable toolData) {
 
 		final UserPlotBean bean = (UserPlotBean) toolData;
 		functions = bean.getFunctions();
 
 		compFunction = new CompositeFunction();
 		for (String key : functions.keySet()) {
 			if (functions.get(key) instanceof AFunction) {
 				AFunction function = (AFunction) functions.get(key);
 				compFunction.addFunction(function);
 
 			}
 		}
 
 		if (functionWidget != null) {
 			functionWidget.setInput(compFunction);
 			functionWidget.expandAll();
 			compFunctionModified();
 		}
 	}
 
 	@Override
 	public Serializable getToolData() {
 
 		UserPlotBean bean = new UserPlotBean();
 
 		int count = 0;
 		for (String key : functions.keySet()) {
 			functions.put(key, compFunction.getFunction(count));
 			count++;
 		}
 
 		bean.setFunctions(functions); // We only set functions because it does a
 										// replace merge.
 
 		return bean;
 	}
 
 }
