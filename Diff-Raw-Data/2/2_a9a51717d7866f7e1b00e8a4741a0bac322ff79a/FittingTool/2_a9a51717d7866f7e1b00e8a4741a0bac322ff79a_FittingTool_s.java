 package org.dawb.workbench.plotting.tools;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.ui.image.IconUtils;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.annotation.AnnotationUtils;
 import org.dawb.common.ui.plot.annotation.IAnnotation;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.common.ui.plot.trace.TraceUtils;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.FittingConstants;
 import org.dawb.workbench.plotting.preference.FittingPreferencePage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.IGuiInfoManager;
 import uk.ac.diamond.scisoft.analysis.rcp.views.PlotServerConnection;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class FittingTool extends AbstractToolPage implements IRegionListener {
 
 	private static final Logger logger = LoggerFactory.getLogger(FittingTool.class);
 	
 	private Composite     composite;
 	private TableViewer   viewer;
 	private IRegion       fitRegion;
 	private FittingJob    fittingJob;
 	private FittedPeaks   fittedPeaks;
 	
 	private ISelectionChangedListener viewUpdateListener;
 	private MenuAction tracesMenu;
 	private ITraceListener traceListener;
 	protected ILineTrace selectedTrace;
 
 	public FittingTool() {
 		super();
 		this.fittingJob = new FittingJob();
 		
 		this.traceListener = new ITraceListener.Stub() {
 			
 			@Override
 			public void tracesPlotted(TraceEvent evt) {
 				
 				@SuppressWarnings("unchecked")
 				final List<ITrace> traces = evt.getSource() instanceof List
 				                          ? (List<ITrace>)evt.getSource()
 				                          : null;
 				if (traces!=null && fittedPeaks!=null && !fittedPeaks.isEmpty()) {
 					traces.removeAll(fittedPeaks.getFittedPeakTraces());
 				}
 				if (traces!=null && !traces.isEmpty()) {
 					updateTracesChoice(traces.get(traces.size()-1));
 				}
 				
 			}
 			
 			@Override
 			public void tracesCleared(TraceEvent evet) {
 				if (tracesMenu!=null) tracesMenu.clear();
 				if (getSite()!=null) getSite().getActionBars().updateActionBars();
 			}
 		};
 
 	}
 	
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_1D;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		
 		this.composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new FillLayout());
 
 		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
         createColumns(viewer);
 		viewer.getTable().setLinesVisible(true);
 		viewer.getTable().setHeaderVisible(true);
 		viewer.setContentProvider(createContentProvider());
 		createActions();
 				
 		getSite().setSelectionProvider(viewer);
 		
 		this.viewUpdateListener = new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				final StructuredSelection sel = (StructuredSelection)event.getSelection();
 				if (fittedPeaks!=null && sel!=null && sel.getFirstElement()!=null) {
 					fittedPeaks.setSelectedPeak((FittedPeak)sel.getFirstElement());
 					viewer.refresh();
 				}
 			}
 		};
 		viewer.addSelectionChangedListener(viewUpdateListener);
 		
 		activate();
 	}
 
 	private void createColumns(final TableViewer viewer) {
 		
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 
         TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(150);
 		var.setLabelProvider(new FittingLabelProvider(0));
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
 		var.getColumn().setText("Position");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new FittingLabelProvider(1));
 
         var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
 		var.getColumn().setText("FWHM");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new FittingLabelProvider(2));
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
 		var.getColumn().setText("Area");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new FittingLabelProvider(3));
 
         var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
 		var.getColumn().setText("Type");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new FittingLabelProvider(4));
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 5);
 		var.getColumn().setText("Algorithm");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new FittingLabelProvider(5));
 
 	}
 	
 	private IContentProvider createContentProvider() {
 		return new IStructuredContentProvider() {
 			@Override
 			public void dispose() {
 			}
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 
 			@Override
 			public Object[] getElements(Object inputElement) {
 
 				if (fittedPeaks==null)    return new IPeak[]{new NullPeak()};
 				if (fittedPeaks.size()<1) return new IPeak[]{new NullPeak()};
 				
 				return fittedPeaks.toArray();
 			}
 		};
 	}
 	
 
 	@Override
 	public void activate() {
 		
 		if (getPlottingSystem()==null) return;
 		if (isDisposed()) return;
 		
 		super.activate();
 		if (viewer!=null && viewer.getControl().isDisposed()) return;
 		
 		if (viewUpdateListener!=null) viewer.addSelectionChangedListener(viewUpdateListener);
 		if (this.traceListener!=null) getPlottingSystem().addTraceListener(traceListener);
 		updateTracesChoice(null);
 		
 		try {
 			if (fittedPeaks!=null) fittedPeaks.activate();
 			getPlottingSystem().addRegionListener(this);
 			this.fitRegion = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Fit selection", getPlottingSystem()), 
 					getPlottingSystem().is2D() ? IRegion.RegionType.BOX : IRegion.RegionType.XAXIS);
 			fitRegion.setRegionColor(ColorConstants.green);
 
 			if (viewer!=null) {
 				viewer.refresh();
 			}
 
 		} catch (Exception e) {
 			logger.error("Cannot put the selection into fitting region mode!", e);
 		}
 	}
 	@Override
 	public void deactivate() {
 		
 		super.deactivate();
 		if (viewer!=null && !viewer.getControl().isDisposed()) {		
 		    if (viewUpdateListener!=null) viewer.removeSelectionChangedListener(viewUpdateListener);
 		}
 		
 		if (getPlottingSystem()!=null) {
 			if (this.traceListener!=null) getPlottingSystem().removeTraceListener(traceListener);
 	
 			try {
 				getPlottingSystem().removeRegionListener(this);
 				if (fittedPeaks!=null) fittedPeaks.deactivate();
 				
 			} catch (Exception e) {
 				logger.error("Cannot put the selection into fitting region mode!", e);
 			}		
 		}
 	}
 
 	@Override
 	public void setFocus() {
         if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
 	}
 	
 	public void dispose() {
 		deactivate();
 		viewUpdateListener = null;
 		
         if (viewer!=null) viewer.getControl().dispose();
        
         // Using clear and setting to null helps the garbage collector.
         if (fittedPeaks!=null) fittedPeaks.dispose();
         fittedPeaks = null;
         
 		super.dispose();
 	}
 
 
 	@Override
 	public Control getControl() {
 		return composite;
 	}
 
 	@Override
 	public void regionCreated(RegionEvent evt) {
 		
 		
 	}
 
 	@Override
 	public void regionAdded(RegionEvent evt) {
 		if (evt==null || evt.getRegion()==null) {
 			getPlottingSystem().clearRegions();
 			return;
 		}
 		if (evt.getRegion()==fitRegion) {
 			fittingJob.fit();
 		}
 	}
 
 	@Override
 	public void regionRemoved(RegionEvent evt) {
 		
 		
 	}
 
 	@Override
 	public void regionsRemoved(RegionEvent evt) {
 		
 	}
 
 	private final class FittingJob extends Job {
 		
 		public FittingJob() {
 			super("Fit peaks");
 			setPriority(Job.INTERACTIVE);
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 
 			if (composite==null)        return Status.CANCEL_STATUS;
 			if (composite.isDisposed()) return Status.CANCEL_STATUS;
 
 			final RectangularROI bounds = (RectangularROI) fitRegion.getROI();
 			if (fitRegion==null || bounds==null) return Status.CANCEL_STATUS;
 
 			getPlottingSystem().removeRegionListener(FittingTool.this);
 
 			composite.getDisplay().syncExec(new Runnable() {
 				public void run() {
 					getPlottingSystem().removeRegion(fitRegion);
 					if (fittedPeaks!=null) {
 						fittedPeaks.removeSelections(getPlottingSystem(), false);
 					}
 				}
 			});
 			if (selectedTrace==null)    return Status.CANCEL_STATUS;
 
 
 			// We chop x and y by the region bounds. We assume the
 			// plot is an XAXIS selection therefore the indices in
 			// y = indices chosen in x.
 			final double[] p1 = bounds.getPointRef();
 			final double[] p2 = bounds.getEndPoint();
 
 			// We peak fit only the first of the data sets plotted for now.
 			AbstractDataset x  = selectedTrace.getXData();
 			AbstractDataset y  = selectedTrace.getYData();
 
 			AbstractDataset[] a= FittingUtils.xintersection(x,y,p1[0],p2[0]);
 			x = a[0]; y=a[1];
 
 			final FittedPeaks bean = FittingUtils.getFittedPeaks(x, y, monitor);
     		// Add saved peaks if any.
    		if (fittedPeaks!=null && !fittedPeaks.isEmpty()) {
     			bean.addFittedPeaks(fittedPeaks.getPeakList());
     		}
 			createFittedPeaks(bean);
 
 			return Status.OK_STATUS;
 		}
 
 		public void fit() {
 			cancel();
 			schedule();
 		}
 	};
 
 	/**
 	 * Thread safe
 	 * @param peaks
 	 */
 	protected synchronized void createFittedPeaks(final FittedPeaks newBean) {
 		
 		if (newBean==null) {
 			fittedPeaks = null;
 			logger.error("Cannot find peaks in the given selection.");
 			return;
 		}
 		composite.getDisplay().syncExec(new Runnable() {
 			
 		    public void run() {
 		    	try {
 		    		
 		    		
 		    		boolean requireFWHMSelections = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FWHM_SELECTIONS);
 		    		boolean requirePeakSelections = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_PEAK_SELECTIONS);
 		    		boolean requireTrace = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FITTING_TRACE);
 		    		boolean requireAnnot = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_ANNOTATION_AT_PEAK);
 
 		    		int ipeak = 1;
 					// Draw the regions
 					for (FittedPeak fp : newBean.getPeakList()) {
 						
 						if (fp.isSaved()) continue;
 						
 						RectangularROI rb = fp.getRoi();
 						final IRegion area = RegionUtils.replaceCreateRegion(getPlottingSystem(), "Peak Area "+ipeak, RegionType.XAXIS);
 						area.setRegionColor(ColorConstants.orange);
 						area.setROI(rb);
 						area.setMobile(false);
 						getPlottingSystem().addRegion(area);
 						fp.setFwhm(area);
 						if (!requireFWHMSelections) area.setVisible(false);
 												
 						final AbstractDataset[] pair = fp.getPeakFunctions();
 						final ILineTrace trace = TraceUtils.replaceCreateLineTrace(getPlottingSystem(), "Peak "+ipeak);
 						trace.setData(pair[0], pair[1]);
 						trace.setLineWidth(1);
 						trace.setTraceColor(ColorConstants.black);
 						trace.setUserTrace(false);
 						getPlottingSystem().addTrace(trace);
 						fp.setTrace(trace);
 						if (!requireTrace) trace.setVisible(false);
 
 	                   	final IAnnotation ann = AnnotationUtils.replaceCreateAnnotation(getPlottingSystem(), "Peak "+ipeak);
                     	ann.setLocation(fp.getPosition(), fp.getPeakValue());                  	
                     	getPlottingSystem().addAnnotation(ann);                   	
                     	fp.setAnnotation(ann);
                     	if (!requireAnnot) ann.setVisible(false);
                     	
 						final IRegion line = RegionUtils.replaceCreateRegion(getPlottingSystem(), "Peak Line "+ipeak, RegionType.XAXIS_LINE);
 						line.setRegionColor(ColorConstants.black);
 						line.setAlpha(150);
 						line.setLineWidth(1);
 						getPlottingSystem().addRegion(line);
 						line.setROI(new LinearROI(rb.getMidPoint(), rb.getMidPoint()));
 						line.setMobile(false);
 						fp.setCenter(line);
 						if (!requirePeakSelections) line.setVisible(false);
 
 
 					    ++ipeak;
 					}
 				
 					FittingTool.this.fittedPeaks = newBean;
 					viewer.setInput(newBean);
                     viewer.refresh();
                     
                     updatePlotServerConnection(newBean);
                     
 		    	} catch (Exception ne) {
 		    		logger.error("Cannot create fitted peaks!", ne);
 		    	}
 		    } 
 		});
 	}
 
 	private IGuiInfoManager plotServerConnection;
 	
 	protected void updatePlotServerConnection(FittedPeaks bean) {
 		
 		if (bean==null) return;
 		
 		if (plotServerConnection==null && getPart() instanceof IEditorPart) {
 			this.plotServerConnection = new PlotServerConnection(((IEditorPart)getPart()).getEditorInput().getName());
 		}
 		
 		if (plotServerConnection!=null) {
 			final Serializable peaks = (Serializable)bean.getPeakFunctions();
 			if (peaks!=null && !bean.isEmpty()) {
 				
 				// For some reason this causes NPEs if you create more than one for a given file.
 				
 				//plotServerConnection.putGUIInfo(GuiParameters.FITTEDPEAKS, peaks);
 			}
 		}
 	}
 	
 
 	protected void updateTracesChoice(ITrace selected) {
 		
 		if (tracesMenu==null) return;
 		
 		tracesMenu.clear();
 		
 		final Collection<ITrace> traces = getPlottingSystem().getTraces();
 		if (traces==null || traces.size()<0) return;
 		if (fittedPeaks!=null) traces.removeAll(fittedPeaks.getFittedPeakTraces());
 		
 		final CheckableActionGroup group = new CheckableActionGroup();
 		FittingTool.this.selectedTrace = null;
 		int index = 0;
 		int selectionIndex=0;
 		for (final ITrace iTrace : traces) {
 			
 			if (iTrace==selected) selectionIndex= index;
 			
 			if (FittingTool.this.selectedTrace==null && iTrace instanceof ILineTrace) {
 				FittingTool.this.selectedTrace = (ILineTrace)iTrace;
 			}
 			
 			final Action action = new Action(iTrace.getName(), IAction.AS_CHECK_BOX) {
 				public void run() {
 					if (iTrace instanceof ILineTrace) FittingTool.this.selectedTrace = (ILineTrace)iTrace;
 					tracesMenu.setSelectedAction(this);
 					if (fittingJob!=null&&isActive()) fittingJob.fit();
 					setChecked(true);
 				}
 			};
 			tracesMenu.add(action);
 			group.add(action);
 			
 			index++;
 		}
 		
 		if (tracesMenu!=null && !tracesMenu.isEmpty()) {
 			tracesMenu.setSelectedAction(selectionIndex);
 			tracesMenu.getAction(selectionIndex).setChecked(true);
 		}
 				
 		getSite().getActionBars().updateActionBars();
 	}
 
 	
 	/**
 	 * We use the old actions here for simplicity of configuration.
 	 * 
 	 * TODO consider moving to commands.
 	 */
 	private void createActions() {
 		
 		
 		final Action showAnns = new Action("Show annotations at the peak position.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_ANNOTATION_AT_PEAK, isChecked);
 				if (fittedPeaks!=null) fittedPeaks.setAnnotationsVisible(isChecked);
 			}
 		};
 		showAnns.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showAnnotation.png"));
 		getSite().getActionBars().getToolBarManager().add(showAnns);
 		
 		showAnns.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_ANNOTATION_AT_PEAK));
 
 		final Action showTrace = new Action("Show fitting traces.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_FITTING_TRACE, isChecked);
 				if (fittedPeaks!=null) fittedPeaks.setTracesVisible(isChecked);
 			}
 		};
 		showTrace.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFittingTrace.png"));
 		getSite().getActionBars().getToolBarManager().add(showTrace);
 
 		showTrace.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FITTING_TRACE));
 
 		
 		final Action showPeak = new Action("Show peak lines.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_PEAK_SELECTIONS, isChecked);
 				if (fittedPeaks!=null) fittedPeaks.setPeaksVisible(isChecked);
 			}
 		};
 		showPeak.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showPeakLine.png"));
 		getSite().getActionBars().getToolBarManager().add(showPeak);
 		
 		showPeak.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_PEAK_SELECTIONS));
 
 		final Action showFWHM = new Action("Show selection regions for full width, half max.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_FWHM_SELECTIONS, isChecked);
 				if (fittedPeaks!=null) fittedPeaks.setAreasVisible(isChecked);
 			}
 		};
 		showFWHM.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFWHM.png"));
 		getSite().getActionBars().getToolBarManager().add(showFWHM);
 		
 		showFWHM.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FWHM_SELECTIONS));
 		
 		final Separator sep = new Separator(getClass().getName()+".separator1");	
 		getSite().getActionBars().getToolBarManager().add(sep);
 		
 		final Action savePeak = new Action("Save peak.", IAction.AS_PUSH_BUTTON) {
 			public void run() {
 				try {
 					fittedPeaks.saveSelectedPeak(getPlottingSystem());
 				} catch (Exception e) {
 					logger.error("Cannot rename saved peak ", e);
 				}
 				viewer.refresh();
 			}
 		};
 		savePeak.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-savePeak.png"));
 		getSite().getActionBars().getToolBarManager().add(savePeak);
 		
 		final Separator sep3 = new Separator(getClass().getName()+".separator3");	
 		getSite().getActionBars().getToolBarManager().add(sep3);
 
 		final MenuAction  peakType = new MenuAction("Peak type to fit");
 		peakType.setToolTipText("Peak type to fit");
 		peakType.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-peak-type.png"));
 		
 		CheckableActionGroup group = new CheckableActionGroup();
 		
 		Action selectedPeakAction = null;
 		for (final IPeak peak : FittingUtils.getPeakOptions().values()) {
 			
 			final Action action = new Action(peak.getClass().getSimpleName(), IAction.AS_CHECK_BOX) {
 				public void run() {
 					Activator.getDefault().getPreferenceStore().setValue(FittingConstants.PEAK_TYPE, peak.getClass().getName());
 					setChecked(true);
 					if (fittingJob!=null&&isActive()) fittingJob.fit();
 					peakType.setSelectedAction(this);
 				}
 			};
 			peakType.add(action);
 			group.add(action);
 			if (peak.getClass().getName().equals(Activator.getDefault().getPreferenceStore().getString(FittingConstants.PEAK_TYPE))) {
 				selectedPeakAction = action;
 			}
 		}
 		
 		if (selectedPeakAction!=null) {
 			peakType.setSelectedAction(selectedPeakAction);
 			selectedPeakAction.setChecked(true);
 		}
 		getSite().getActionBars().getToolBarManager().add(peakType);
 		getSite().getActionBars().getMenuManager().add(peakType);
 
 		
 		final Separator sep2 = new Separator(getClass().getName()+".separator2");	
 		getSite().getActionBars().getToolBarManager().add(sep2);
 
 		this.tracesMenu = new MenuAction("Traces");
 		tracesMenu.setToolTipText("Choose trace for fit.");
 		tracesMenu.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-trace-choice.png"));
 		
 		getSite().getActionBars().getToolBarManager().add(tracesMenu);
 		getSite().getActionBars().getMenuManager().add(tracesMenu);
 				
 		final MenuAction numberPeaks = new MenuAction("Number peaks to fit");
 		numberPeaks.setToolTipText("Number peaks to fit");
 				
 		group = new CheckableActionGroup();
 		
 		final int npeak = Activator.getDefault().getPreferenceStore().getDefaultInt(FittingConstants.PEAK_NUMBER_CHOICES);
 		for (int ipeak = 1; ipeak <= npeak; ipeak++) {
 			
 			final int peak = ipeak;
 			final Action action = new Action("Fit "+String.valueOf(ipeak)+" Peaks", IAction.AS_CHECK_BOX) {
 				public void run() {
 					Activator.getDefault().getPreferenceStore().setValue(FittingConstants.PEAK_NUMBER, peak);
 					numberPeaks.setSelectedAction(this);
 					setChecked(true);
 					if (isActive()) fittingJob.fit();
 				}
 			};
 			
 			action.setImageDescriptor(IconUtils.createIconDescriptor(String.valueOf(ipeak)));
 			numberPeaks.add(action);
 			group.add(action);
 			action.setChecked(false);
 			action.setToolTipText("Fit "+ipeak+" peak(s)");
 			
 		}
 
 		final int ipeak = Activator.getDefault().getPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
 		numberPeaks.setSelectedAction(ipeak-1);
 		numberPeaks.setCheckedAction(ipeak-1, true);
 		
 		getSite().getActionBars().getToolBarManager().add(numberPeaks);
 		//getSite().getActionBars().getMenuManager().add(numberPeaks);
 		
 		
 		final Action clear = new Action("Clear all", Activator.getImageDescriptor("icons/plot-tool-peak-fit-clear.png")) {
 			public void run() {
 				if (!isActive()) return;
 				if (fittedPeaks!=null) {
 					fittedPeaks.removeSelections(getPlottingSystem(), true);
 					fittedPeaks.dispose();
 					fittedPeaks = null;
 				}
 				viewer.refresh();
 			}
 		};
 		clear.setToolTipText("Clear all regions found in the fitting");
 		
 		getSite().getActionBars().getToolBarManager().add(clear);
 		getSite().getActionBars().getMenuManager().add(clear);
 		
 		final Action delete = new Action("Delete peak selected", Activator.getImageDescriptor("icons/delete.gif")) {
 			public void run() {
 				if (!isActive()) return;
 				if (fittedPeaks!=null) fittedPeaks.deleteSelectedPeak(getPlottingSystem());
 				viewer.refresh();
 			}
 		};
 		delete.setToolTipText("Delete peak selected, if any");
 		
 		getSite().getActionBars().getToolBarManager().add(delete);
 
 		final Action preferences = new Action("Preferences...") {
 			public void run() {
 				if (!isActive()) return;
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), FittingPreferencePage.ID, null, null);
 				if (pref != null) pref.open();
 			}
 		};
 
 		getSite().getActionBars().getMenuManager().add(preferences);
 
 	    final MenuManager menuManager = new MenuManager();
 	    menuManager.add(clear);
 	    menuManager.add(delete);
 	    menuManager.add(savePeak);
 	    menuManager.add(new Separator());
 	    menuManager.add(showAnns);
 	    menuManager.add(showTrace);
 	    menuManager.add(showPeak);
 	    menuManager.add(showFWHM);
 		
 	    viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
 
 	}
 	
 	public void setFittedPeaks(FittedPeaks fittedPeaks) {
 		this.fittedPeaks = fittedPeaks;
 	}
 
 	
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
 		if (key == IPeak.class) {
 			return fittedPeaks!=null && !fittedPeaks.isEmpty() ? fittedPeaks.getPeakFunctions() : null;
 		}
 		return super.getAdapter(key);
 	}
 
 }
