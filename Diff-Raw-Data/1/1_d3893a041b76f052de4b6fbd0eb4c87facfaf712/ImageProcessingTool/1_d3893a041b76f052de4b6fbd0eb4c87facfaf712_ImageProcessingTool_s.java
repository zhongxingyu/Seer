 package org.dawnsci.plotting.tools.processing;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.widgets.ActionBarWrapper;
 import org.dawb.workbench.jmx.UserPlotBean;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.PlottingFactory;
 import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.IRegionListener;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.region.RegionEvent;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.tool.IAuxiliaryToolDataset;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.XAxisBoxROI;
 
 /**
  * Abstract Image Processing Tool
  * This tool has two profile plotting systems and a composite of controls
  * @author wqk87977
  *
  */
 public abstract class ImageProcessingTool extends AbstractToolPage  implements IROIListener, IAuxiliaryToolDataset {
 	
 	private static Logger logger = LoggerFactory.getLogger(ImageProcessingTool.class);
 
 	protected IPlottingSystem selectionPlottingSystem;
 	protected IPlottingSystem reviewPlottingSystem;
 
 	private IRegionListener regionListener;
 	private Composite profileContentComposite;
 	private NormaliseProcessJob updateNormaliseProcess;
 	private ITraceListener traceListener;
 
 	protected HashMap<String, IDataset> auxiliaryDatasets = new HashMap<String, IDataset>();
 
 	protected IRegion region;
 
 	protected IDataset originalData;
 	protected List<IDataset> originalAxes;
 	protected IDataset auxiliaryData;
 	
 	protected UserPlotBean userPlotBean = new UserPlotBean();
 	/**
 	 * flag set to true when update occurs through the tool
 	 */
 	protected boolean isUpdated = false;
 
 	public ImageProcessingTool(){
 
 		try {
 			selectionPlottingSystem = PlottingFactory.createPlottingSystem();
 			reviewPlottingSystem = PlottingFactory.createPlottingSystem();
 
 			updateNormaliseProcess = new NormaliseProcessJob();
 		
 			this.regionListener = new IRegionListener.Stub() {
 				@Override
 				public void regionRemoved(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().removeROIListener(ImageProcessingTool.this);
 					}
 				}
 				@Override
 				public void regionsRemoved(RegionEvent evt) {
 					//clears traces if all regions removed
 //					final Collection<IRegion> regions = getPlottingSystem().getRegions();
 //					if (regions == null || regions.isEmpty()) {
 //						registeredTraces.clear();
 //						normProfilePlotSystem.clear();
 //						preNormProfilePlotSystem.clear();
 //					}
 				}
 				@Override
 				public void regionAdded(RegionEvent evt) {}
 				@Override
 				public void regionCreated(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().addROIListener(ImageProcessingTool.this);
 					}
 				}
 				protected void update(RegionEvent evt) {}
 			};
 
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesAdded(TraceEvent evt) {
 					if (!(evt.getSource() instanceof List<?>)) {
 						return;
 					}
 				}
 				@Override
 				protected void update(TraceEvent evt) {
 					if(!isUpdated){
 						if(getPlottingSystem()!= null && getPlottingSystem().getTraces().size()>0){
 							originalData = getPlottingSystem().getTraces().iterator().next().getData().clone();
 							selectionPlottingSystem.updatePlot2D(originalData, originalAxes, null);
 						}
 					}
 				}
 			};
 		} catch (Exception e) {
 			logger.error("Cannot get plotting system!", e);
 		}
 
 	}
 
 	protected String getRegionName() {
 		return "Processed Region";
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		profileContentComposite = new Composite(parent, SWT.NONE);
 		profileContentComposite.setLayout(new GridLayout(1, true));
 		GridUtils.removeMargins(profileContentComposite);
 		final Action reselect = new Action("Create new region to process", getImageDescriptor()) {
 			public void run() {
 				createNewRegion();
 			}
 		};
 		if (getSite().getActionBars() != null){
 			getSite().getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
 			getSite().getActionBars().getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
 			getSite().getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroupAfter"));
 		}
 
 		SashForm mainSashForm = new SashForm(profileContentComposite, SWT.VERTICAL);
 		mainSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
 		mainSashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
 		// top part
 		Composite profilePlotComposite = new Composite(mainSashForm, SWT.BORDER);
 		profilePlotComposite.setLayout(new FillLayout());
 		
 		selectionPlottingSystem.createPlotPart(profilePlotComposite, 
 				 getTitle(), 
 				 getSite().getActionBars(), 
 				 PlotType.XY,
 				 null);
 		configureSelectionPlottingSystem(selectionPlottingSystem);
 		// Unused actions removed for tool
 		selectionPlottingSystem.getPlotActionSystem().remove(BasePlottingConstants.RESCALE);
 		selectionPlottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_INDEX);
 		selectionPlottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_X_AXIS);
 		selectionPlottingSystem.setXFirst(true);
 		selectionPlottingSystem.setRescale(true);
 		selectionPlottingSystem.addRegionListener(regionListener);
 		createNewRegion();
 		
 		//bottom part
 		SashForm bottomSashForm = new SashForm(mainSashForm, SWT.HORIZONTAL);
 		bottomSashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
 
 		Composite displayComp = new Composite(bottomSashForm, SWT.NONE);
 		displayComp.setLayout(new GridLayout(1, false));
 		GridUtils.removeMargins(displayComp);
 		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(displayComp, null);
 		Composite displayPlotComp  = new Composite(displayComp, SWT.BORDER);
 		displayPlotComp.setLayout(new FillLayout());
 		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		reviewPlottingSystem.createPlotPart(displayPlotComp, 
 												 "User display", 
 												 actionBarWrapper, 
 												 PlotType.XY, 
 												 null);
 		configureReviewPlottingSystem(reviewPlottingSystem);
 		Composite controlComp = new Composite(bottomSashForm, SWT.NONE);
 		controlComp.setLayout(new GridLayout(1, false));
 		controlComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		createControlComposite(controlComp);
 		bottomSashForm.setWeights(new int[] { 2, 1});
 		mainSashForm.setWeights(new int[]{1, 1});
 		parent.layout();
 
 		originalData = getPlottingSystem().getTraces().iterator().next().getData().clone();
 		originalAxes = ((IImageTrace)getPlottingSystem().getTraces().iterator().next()).getAxes();
 	}
 
 	/**
 	 * Composite to create the controls
 	 * @param parent
 	 */
 	protected abstract void createControlComposite(Composite parent);
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return profileContentComposite;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	@Override
 	public final ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void setFocus() {
 		if (getControl()!=null && !getControl().isDisposed()) {
 			getControl().setFocus();
 		}
 	}
 
 	@Override
 	public void addDataset(IDataset data) {
 		if(data == null) return;
 		auxiliaryDatasets.put(data.getName(), data);
 	}
 
 	@Override
 	public void removeDataset(IDataset data) {
 		if(data == null) return;
 		auxiliaryDatasets.remove(data.getName());
 	}
 
 	protected final void createNewRegion() {
 		// Start with a selection of the right type
 		try {
 			IRegion region = selectionPlottingSystem.createRegion(RegionUtils.getUniqueName(getRegionName(), getPlottingSystem()), getCreateRegionType());
 			double width = getImageTrace().getData().getShape()[0];
 			region.setROI(new XAxisBoxROI(width, 0));
 			selectionPlottingSystem.addRegion(region);
 		} catch (Exception e) {
 			logger.error("Cannot create region for profile tool!");
 		}
 	}
 
 	/**
 	 * The object used to mark this profile as being part of this tool.
 	 * By default just uses package string.
 	 * @return
 	 */
 	private Object getMarker() {
 		return getToolPageRole().getClass().getName().intern();
 	}
 
 	public boolean isRegionTypeSupported(RegionType type) {
 		return (type==RegionType.BOX)||(type==RegionType.PERIMETERBOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
 	}
 
 	protected RegionType getCreateRegionType() {
 		return RegionType.BOX;
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 		if(getPlottingSystem() != null){
 			getPlottingSystem().addTraceListener(traceListener);
 			if(getPlottingSystem().getTraces().size()>0){
 				originalData = getPlottingSystem().getTraces().iterator().next().getData().clone();
 				originalAxes = ((IImageTrace)getPlottingSystem().getTraces().iterator().next()).getAxes();
 			}
 		}
 		setRegionsActive(true);
 		updateProfiles(true);
 	}
 
 	@Override
 	public void deactivate() {
 		super.deactivate();
 		setRegionsActive(false);
 
 		if(getPlottingSystem() != null){
 			getPlottingSystem().removeTraceListener(traceListener);
 		}
 		if(selectionPlottingSystem != null){
 			selectionPlottingSystem.removeRegionListener(regionListener);
 			selectionPlottingSystem.clear();
 		}
 		if(reviewPlottingSystem != null){
 			reviewPlottingSystem.clear();
 		}
 	}
 	
 	private void setRegionsActive(boolean active) {
 		if (selectionPlottingSystem!=null) {
 			final Collection<IRegion> regions = selectionPlottingSystem.getRegions();
 			if (regions!=null) for (IRegion iRegion : regions) {
 				if (active) {
 					iRegion.addROIListener(this);
 				} else {
 					iRegion.removeROIListener(this);
 				}
 				if (iRegion.getUserObject()==getMarker()) {
 					if (active) {
 						iRegion.setVisible(active);
 					} else {
 						// If the plotting system has changed dimensionality
 						// to something not compatible with us, remove the region.
 						// TODO Change to having getRank() == rank 
 						if (getToolPageRole().is2D() && !selectionPlottingSystem.is2D()) {
 							iRegion.setVisible(active);
 						} else if (selectionPlottingSystem.is2D() && !getToolPageRole().is2D()) {
 							iRegion.setVisible(active);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public Control getControl() {
 		return profileContentComposite;
 	}
 
 	@Override
 	public void dispose() {
 		deactivate();
 		if (selectionPlottingSystem!=null) selectionPlottingSystem.dispose();
 		selectionPlottingSystem = null;
 		if (reviewPlottingSystem!=null) reviewPlottingSystem.dispose();
 		reviewPlottingSystem = null;
 		super.dispose();
 	}
 
 	@Override
 	public IPlottingSystem getToolPlottingSystem() {
 		return selectionPlottingSystem;
 	}
 	@Override
 	public void roiSelected(ROIEvent evt) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isStaticTool() {
 		return true;
 	}
 
 	@Override
 	public void roiDragged(ROIEvent evt) {
 		region = (IRegion)evt.getSource();
 		updateProfiles(region, evt.getROI(), true);
 	}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		region = (IRegion)evt.getSource();
 		updateProfiles(region, region.getROI(), true);
 	}
 
 	@Override
 	public Serializable getToolData() {
 		// TODO Auto-generated method stub
 		return userPlotBean;
 	}
 	
 	/**
 	 * Updates the normalisation profiles
 	 * @param isFullProcess
 	 *         set to true for the full process, false for the review process only
 	 */
 	protected void updateProfiles(boolean isFullProcess){
 		updateProfiles(getRegion(), getRegion().getROI(), isFullProcess);
 	}
 
 	/**
 	 * Update the normalisation profiles
 	 * @param r 
 	 *         the region
 	 * @param rb 
 	 *         the roi
 	 * @param isFullProcess
 	 *         set to true for the full process, false for the review process only
 	 */
 	protected synchronized void updateProfiles(IRegion r, IROI rb, boolean isFullProcess) {
 		if (!isActive()) return;
 		if (r!=null) {
 			if(!isRegionTypeSupported(r.getRegionType())) return;
 			if (!r.isUserRegion()) return;
 		}
 		updateNormaliseProcess.profile(r, rb, isFullProcess);
 	}
 
 	/**
 	 * 
 	 * @param plotter
 	 */
 	protected abstract void configureSelectionPlottingSystem(IPlottingSystem plotter);
 
 	/**
 	 * 
 	 * @param plotter
 	 */
 	protected abstract void configureReviewPlottingSystem(IPlottingSystem plotter);
 
 	/**
 	 * Creates selection profile
 	 * @param image
 	 * @param region
 	 * @param roi - may be null
 	 * @param monitor
 	 */
 	protected abstract void createSelectionProfile(IImageTrace image, 
 													  IROI roi, 
 													  IProgressMonitor monitor);
 
 	/**
 	 * Creates the review profile
 	 * @param image
 	 * @param region
 	 * @param roi - may be null
 	 * @param monitor
 	 */
 	protected abstract void createReviewProfile(IProgressMonitor monitor);
 
 	protected IRegion getRegion(){
 		return region;
 	}
 
 	private final class NormaliseProcessJob extends Job {
 		private IRegion currentRegion;
 		private IROI currentROI;
 		private boolean isFullProcess;
 
 		NormaliseProcessJob() {
 			super(getRegionName()+" update");
 			setSystem(true);
 			setUser(false);
 			setPriority(Job.INTERACTIVE);
 		}
 
 		/**
 		 * 
 		 * @param region can be null
 		 * @param roi can be null
 		 * @param isFullProcess
 		 */
 		public void profile(IRegion region, IROI roi, boolean isFullProcess) {
 			this.currentRegion = region;
 			this.currentROI    = roi;
 			this.isFullProcess = isFullProcess;
 			schedule();
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			try {
 				isUpdated = true;
 				if (!isActive()) return Status.CANCEL_STATUS;
 				Collection<ITrace> traces= selectionPlottingSystem.getTraces(IImageTrace.class);
 				if(traces!=null && traces.size() == 0){
 					traces = getPlottingSystem().getTraces(IImageTrace.class);
 				}
 				IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 
 				if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
 				if (image==null) {
 					getPlottingSystem().clear();
 					reviewPlottingSystem.clear();
 					return Status.OK_STATUS;
 				}
 				if(isFullProcess){
 					if (currentRegion==null) {
 						final Collection<IRegion> regions = selectionPlottingSystem.getRegions();
 						if (regions!=null) {
 						for (IRegion iRegion : regions) {
 							if (!iRegion.isUserRegion()) continue;
 							if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
 								createSelectionProfile(image, iRegion.getROI(), monitor);
 							}
 						} else {
 							getPlottingSystem().clear();
 						}
 					} else {
 						if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
 						createSelectionProfile(image, currentROI!=null?currentROI:currentRegion.getROI(), monitor);
 					}
 					if (monitor.isCanceled()) return Status.CANCEL_STATUS;
 					getPlottingSystem().repaint();
 				} 
 				
 				createReviewProfile(monitor);
 
 				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
 				reviewPlottingSystem.repaint();
 
 				isUpdated = false;
 				return Status.OK_STATUS;
 			} catch (Throwable ne) {
 				logger.error("Internal error processing profile! ", ne);
 				return Status.CANCEL_STATUS;
 			}
 		}
 	}
 }
