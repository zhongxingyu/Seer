 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.workbench.ui.editors;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.IVariableManager;
 import org.dawb.common.ui.editors.ICheckableObject;
 import org.dawb.common.ui.editors.IDatasetEditor;
 import org.dawb.common.ui.editors.IPlotUpdateParticipant;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.slicing.SliceComponent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.views.PlotDataView;
 import org.dawb.common.ui.widgets.ActionBarWrapper;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.editors.preference.EditorConstants;
 import org.dawb.workbench.ui.views.PlotDataPage;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IActionBars2;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.IPage;
 import org.eclipse.ui.part.Page;
 import org.eclipse.ui.progress.UIJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 
 /**
  * An editor which combines a plot with a graph of data sets.
  * 
  * 
  */
 public class PlotDataEditor extends EditorPart implements IReusableEditor, IDatasetEditor {
 	
 	private static Logger logger = LoggerFactory.getLogger(PlotDataEditor.class);
 	
 	// This view is a composite of two other views.
 	private AbstractPlottingSystem      plottingSystem;	
 	private Collection<String>          dataNames;
 	private IMetaData                   metaData;
 	private PlotType                    defaultPlotType;
 	private Map<Integer, IAxis>         axisMap;
 	private PlotJob                     plotJob;
 	private ActionBarWrapper            wrapper;
 
 	public PlotDataEditor(final PlotType defaultPlotType) {
 		
 	    this.axisMap = new HashMap<Integer, IAxis>(4);
 	    this.plotJob = new PlotJob();
 		try {
 			this.defaultPlotType= defaultPlotType;
 	        this.plottingSystem = PlottingFactory.createPlottingSystem();
 	        
 		} catch (Exception ne) {
 			logger.error("Cannot locate any plotting systems!", ne);
 		}
  	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);		
 	}
 
 	@Override
 	public Map<String, IDataset> getSelected() {
 		
 		final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
         final Map<String,IDataset> ret = new HashMap<String, IDataset>(3);
 		if (dataSetComponent==null) return ret;
 		
         final List<ICheckableObject> selectedNames = dataSetComponent.getSelections();
         for (Object object : selectedNames) {
         	final AbstractDataset set = getDataSet(object, null);
         	if (set==null) continue;
          	ret.put(set.getName(), set);
         }
 		return ret;
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 	
 
 	public void setToolbarsVisible(boolean isVisible) {
 		wrapper.setVisible(isVisible);
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		
 		final Composite  main       = new Composite(parent, SWT.NONE);
 		final GridLayout gridLayout = new GridLayout(1, false);
 		main.setLayout(gridLayout);
 		GridUtils.removeMargins(main);
 		
 		final IActionBars bars = this.getEditorSite().getActionBars();
 		this.wrapper = ActionBarWrapper.createActionBars(main,(IActionBars2)bars);
 				
 		// NOTE use name of input. This means that although two files of the same
 		// name could be opened, the editor name is clearly visible in the GUI and
 		// is usually short.
 		final String plotName = this.getEditorInput().getName();
 		
 		final Composite plot  = new Composite(main, SWT.NONE);
 		plot.setLayout(new FillLayout());
 		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         plottingSystem.createPlotPart(plot, plotName, wrapper, defaultPlotType, this);
         plottingSystem.addPropertyChangeListener(new IPropertyChangeListener() {				
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				if (IPlottingSystem.RESCALE_ID.equals(event.getProperty())) {
 					Activator.getDefault().getPreferenceStore().setValue(EditorConstants.RESCALE_SETTING, (Boolean)event.getNewValue());
 				} else {
 					setAxisSettings(EditorConstants.XAXIS_PROP_STUB, plottingSystem.getSelectedXAxis());
 					setAxisSettings(EditorConstants.YAXIS_PROP_STUB, plottingSystem.getSelectedYAxis());
 				}
 				
 			}
 		});
 		getAxisSettings(EditorConstants.XAXIS_PROP_STUB, plottingSystem.getSelectedXAxis());
 		getAxisSettings(EditorConstants.YAXIS_PROP_STUB, plottingSystem.getSelectedYAxis());
        
         axisMap.put(1, plottingSystem.getSelectedYAxis());
 		// FIX to http://jira.diamond.ac.uk/browse/DAWNSCI-380 remove axes until they work
         for (int i = 2; i <=2; i++) { //(Y4)
         	final IAxis yAxis = plottingSystem.createAxis("Y"+i, true, i==3||i==4?SWT.RIGHT:SWT.LEFT);
         	yAxis.setVisible(false);
         	yAxis.setTitle("Y"+i);
             axisMap.put(i, yAxis);
 		}        		
 		
 		// Finally
 		if (wrapper!=null)  wrapper.update(true);
 	    createData(getEditorInput());	
 	    
 		// We ensure that the view for choosing data sets is visible
 		if (EclipseUtils.getActivePage()!=null) {
 			try {
 				EclipseUtils.getActivePage().showView(PlotDataView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
 			} catch (PartInitException e) {
 				logger.error("Cannot open "+PlotDataView.ID);
 			}
 		}
 		
 		getEditorSite().setSelectionProvider(plottingSystem.getSelectionProvider());
 
 		plottingSystem.setRescale(Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.RESCALE_SETTING));
  	}
 
 	protected void setAxisSettings(String propertyStub, IAxis axis) {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		boolean isDateTime = axis.isDateFormatEnabled();
 		store.setValue(propertyStub+"isDateTime", isDateTime);
 		if (isDateTime) {
 			String format = axis.getFormatPattern();
 			store.setValue(propertyStub + "dateFormat", format);
 		}
 		boolean isLog = axis.isLog10();
 		store.setValue(propertyStub + "log10", isLog);
 	}
 
 	protected void getAxisSettings(String propertyStub, IAxis axis) {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		boolean isDateTime = false;
 		if (store.contains(propertyStub+"isDateTime")) {
 			isDateTime = store.getBoolean(propertyStub+"isDateTime");
 			axis.setDateFormatEnabled(isDateTime);
 		}
 		if (isDateTime && store.contains(propertyStub+"dateFormat")) {
 			axis.setFormatPattern(store.getString(propertyStub+"dateFormat"));
 		}
 		if (store.contains(propertyStub+"log10")) {
 			axis.setLog10(store.getBoolean(propertyStub+"log10"));
 		}
 	}
 	/**
 	 * Call to change plot default by looking at data
 	 */
 	public void createPlotTypeDefaultFromData() {
 	    // If the data is only 2D we tell the PlottingSystem to switch to 2D mode.
 		boolean is1D = false;
 		
 		final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
  		if (dataSetComponent!=null) {
 			for (ICheckableObject set : dataSetComponent.getData()) {
 				if (dataSetComponent.getActiveDimensions(set, true)==1)	{
 					is1D=true;
 					break;
 				}
 			}
  		}
 		if (!is1D) {
 			getPlottingSystem().setPlotType(PlotType.IMAGE);
 		}
 
 	}
 
 	
 	private boolean doingUpdate = false;
 	private boolean extractingMetaData;
 	
 	/**
 	 * Must be called in UI thread
 	 * @param selections
 	 * @param useTask
 	 */
 	@Override
 	public void updatePlot(final ICheckableObject[]      selections, 
 			               final IPlotUpdateParticipant participant,
 			               final boolean                useTask) {
 		
 		if (doingUpdate) return;
 		
 		if (selections==null || selections.length<1) {
 			if (participant!=null) participant.setSlicerVisible(false);
 		}
 		
 		try {
 			doingUpdate = true;
 			if (selections==null||selections.length<1) {
 				plottingSystem.reset();
 				return;
 			}
 			
 			if (selections.length==1 && participant.getDimensionCount(selections[0])!=1) {
 				
 				participant.setSlicerVisible(true);
 				participant.setSlicerData(selections[0],
 						                  EclipseUtils.getFilePath(getEditorInput()), 
 						                  getShape(selections[0]), 
 						                  plottingSystem);
 
 				return;
 			}
 			
 			participant.setSlicerVisible(false);
 			
 			if (useTask) {
 				plotJob.plot(selections, participant); 
 			} else {
 				createPlot(selections, participant, new NullProgressMonitor());
 			}
 		} finally {
 			doingUpdate = false;
 		}
 	}
 	
 	private int[] getShape(ICheckableObject ob) {
 		
 		if (ob.isExpression()) {
 			return null; 
 		} else {
 			String name = ob.getName();
 			int[] shape=null;
 			try {
 				shape = metaData.getDataShapes().get(name);
 			} catch (Exception allowed) {
 				// It's ok
 			}
 			if (shape==null) {
 				shape = getLazyDataset(name, null).getShape();
 			}
 			return shape;
 		}
 	}
 
 	private class PlotJob extends Job {
 
 		public PlotJob() {
 			super("Plot update");
 			setSystem(true);
 			setUser(false);
 			setPriority(Job.INTERACTIVE);
 		}
 
 		private ICheckableObject[] selections;
 		private IPlotUpdateParticipant participant;
 
 		public void plot(ICheckableObject[] selections,
 				               IPlotUpdateParticipant participant) {
 			
 			cancel();
             this.selections  = selections;
             this.participant = participant;
             schedule();
 			
 		}
 		
 		public IStatus run(IProgressMonitor monitor) {
 			monitor.beginTask("Updating selected DataSets", 100);		
 			createPlot(selections, participant, monitor);
 			return Status.OK_STATUS;
 		}
 	}
 	
 	private void createPlot(final ICheckableObject[] selections, final IPlotUpdateParticipant participant, final IProgressMonitor monitor) {
 		
 
 		if (monitor.isCanceled()) return;
 		plottingSystem.clear();
 		
 		boolean requireFullRefresh = plottingSystem.getPlotType()!=participant.getPlotMode();
 		final IDataset data = getDataSet(selections[0], monitor);
 		
 		if (data==null)             return;
 		try {
 		    if (data.getSize()<0) return;
 		} catch (Exception ne) {
 			return;
 		}
 		if (data.getRank()>2)       return; // Cannot plot more that 2 dims!
 		
 		if (participant.getPlotMode()==PlotType.IMAGE || data.getRank()==2) {
 			
 		    plottingSystem.createPlot2D(data, null, monitor);
 		    
 		} else {
 			List<ICheckableObject> sels = new ArrayList<ICheckableObject>(Arrays.asList(selections));
 
 			final IDataset x;
 			if (plottingSystem.isXfirst() && sels.size()>1) {
 				x  = data;
 				sels.remove(0);
 			} else {
 				x = null;
 			}
 			
 			if (sels.isEmpty() || (!plottingSystem.isXfirst() && sels.size()==1)) {
 				final List<ITrace> traces = plottingSystem.createPlot1D(x, Arrays.asList(data), getEditorInput().getName(), monitor);
 		        sync(sels,traces);
 				return;
 			}
 
             final Map<Integer,List<AbstractDataset>> ys = sels.isEmpty()
                                                        ? null
                                 		               : new HashMap<Integer,List<AbstractDataset>>(4);
 
            // Sort ys by axes (for 2D there is one y)
             if (!sels.isEmpty()) {
         	   for (int i = 1; i <= 4; i++) {
         		   List<AbstractDataset> tmp = getYS(i, sels, monitor);
         		   if (tmp!=null) {
         			   ys.put(i, tmp);
         		   }
         	   }
             }
 
     		final List<ITrace> traces = new ArrayList<ITrace>();
             Display.getDefault().syncExec(new Runnable() {
             	public void run() {
             		traces.addAll(createPlotSeparateAxes(x,ys,monitor));
             	}
             });
 	        sync(sels,traces);
 
 		}
 		
 		if (requireFullRefresh) {
 			plottingSystem.repaint(true);
 		}
 		monitor.done();
 	}
 
 	/**
 	 * Records any expression names so that they can be available in the history tool.
 	 * @param sels
 	 * @param traces
 	 */
 	private void sync(List<ICheckableObject> sels, List<ITrace> traces) {
 		if (sels==null || traces==null) return ;
 		if (sels.size() == traces.size()) {
 			for (int i = 0; i < sels.size(); i++) {
 				if (traces.get(i).getUserObject()==null || traces.get(i).getUserObject() instanceof String) {
 					traces.get(i).setUserObject(sels.get(i).getVariable());
 				}
 			}
 		}
 		
 	}
 
 	protected List<ITrace> createPlotSeparateAxes(final IDataset                    x,
 			                              final Map<Integer,List<AbstractDataset>> ys,
 			                              final IProgressMonitor                   monitor) {
 		
 		List<ITrace> traces = new ArrayList<ITrace>();
 		try {
 			for (int i = 1; i <= 4; i++) {
 				final IAxis axis = axisMap.get(i);
 				if (axis==null) continue;
 				if (ys.get(i)==null) {
 					axis.setVisible(false);
 					continue;
 				}
 				axis.setVisible(true);
 				plottingSystem.setSelectedYAxis(axis);	
 
 				final List<AbstractDataset> ysAbs = ys.get(i);
 				final List<IDataset>       ysReal = new ArrayList<IDataset>();
 				if (ysAbs!=null) for (AbstractDataset a : ysAbs) ysReal.add(a);
 				traces.addAll(plottingSystem.createPlot1D(x, ysReal, getEditorInput().getName(), monitor));
 			} 
 			return traces;
 		} finally {
 			plottingSystem.setSelectedYAxis(axisMap.get(1));
 		}
 	}
 
 	private List<AbstractDataset> getYS(int iyaxis, List<ICheckableObject> selections, IProgressMonitor monitor) {
 		
 		List<AbstractDataset> ys = new ArrayList<AbstractDataset>(3);
 		for (ICheckableObject co : selections) {
 			
 			if (co.getYaxis()!=iyaxis) continue;
 			ys.add(getDataSet(co, monitor));
 			if (monitor.isCanceled()) return null;
 			monitor.worked(1);
 		}
 		return ys.isEmpty()?null:ys;
 	}
 
 	public AbstractDataset getDataset(String name, IMonitor monitor) {
 		try {
 			
 			AbstractDataset set = LoaderFactory.getDataSet(EclipseUtils.getFilePath(getEditorInput()), name, monitor);
 			try {
 			    set = set.squeeze();
 			} catch (Throwable ignored) {
 				// Leave set assigned as read
 			}
 
 			if (set!=null) set.setName(name);
 			return set;
 			
 		} catch (Exception e) {
 			logger.error("Cannot read "+name, e);
 			return null;
 		}
 	}
 	
 	public ILazyDataset getLazyDataset(String name, IMonitor monitor) {
 		try {
 			
 			DataHolder holder = LoaderFactory.getData(EclipseUtils.getFilePath(getEditorInput()), monitor);
 			ILazyDataset set  = holder.getLazyDataset(name);
 
 			if (set!=null) set.setName(name);
 			return set;
 			
 		} catch (Exception e) {
 			logger.error("Cannot read "+name, e);
 			return null;
 		}
 	}
 
 
 	@Override
 	public AbstractDataset getVariableValue(String name, final IMonitor monitor) {
 
 		throw new NullPointerException("Expressions are not supported by "+getClass().getName());
 	}
 	@Override
 	public AbstractDataset getLazyValue(String name, final IMonitor monitor) {
 
 		throw new NullPointerException("Expressions are not supported by "+getClass().getName());
 	}
 	
 	public boolean containsDatasetName(String name, IMonitor monitor) {
 		if (dataNames==null) return false;
 		return dataNames.contains(name);
 	}
 	
 	@Override	
 	public boolean isVariableName(String name, IMonitor monitor) {
 		throw new NullPointerException("Expressions are not supported by "+getClass().getName());
 	}
 	
 	private AbstractDataset getDataSet(final Object input, final IProgressMonitor monitor) {
 		
 	    Object object = input;	
 		if (input instanceof CheckableObject) {
 			ICheckableObject check = (ICheckableObject)input;
 			object = check.isExpression() ? check.getExpression() : check.getName();
 		}
 		if (object instanceof IExpressionObject) {
 			try {
 				return (AbstractDataset)((IExpressionObject)object).getDataSet(null, new ProgressMonitorWrapper(monitor));
 			} catch (Exception e) {
 				// valid, user can enter an invalid expression. In this case
 				// it colours red but does not stop them from using the view.
 				return new DoubleDataset();
 			}
 		}
  		return getDataset((String)object, new ProgressMonitorWrapper(monitor));
 	}
 
 
 	@Override
 	public void setInput(final IEditorInput input) {
 		
 		super.setInput(input);
 		setPartName(input.getName());
 				
 		extractingMetaData = true;
 		// Get the meta data in a thread to avoid things breaking
		final Job getMeta = new Job("Extract Metadata") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				final String    path = EclipseUtils.getFilePath(getEditorInput());
 				try {
 					PlotDataEditor.this.metaData = LoaderFactory.getMetaData(path, null);
 			        if (metaData==null || metaData.getDataNames()==null) {
 						DataHolder dh = LoaderFactory.getData(path, null);
 						if (dh==null) dh= new DataHolder();
 						PlotDataEditor.this.dataNames = dh.getMap().keySet();
 			        } else {
 			        	PlotDataEditor.this.dataNames = metaData.getDataNames();
 			        }
 				} catch (Exception ne) {
 					logger.error("Cannot generate meta data!", ne);
 					return Status.CANCEL_STATUS;
 				} finally {
 			        extractingMetaData = false;
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		getMeta.setSystem(true);
 		getMeta.schedule();
 
 		createData(input);
 	}
 	
     /**
      * Method used to select all 1D plots in the order they are extracted.
      * If overide then current selected plots will be ignored.
      */
 	public void setAll1DSelected(final boolean overide) {
 		
 		final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
 		dataSetComponent.setAll1DSelected(overide);
 	}
 
 	/**
 	 * Reads the data sets from the IEditorInput
 	 */
 	private void createData(final IEditorInput input) {
 
 		final UIJob job = new UIJob("Update data") {
 			
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
 		 		if (dataSetComponent==null) return Status.CANCEL_STATUS;
 				try {
 					
 					// We wait while extracting meta data
 					int waited = 0;
 					while(extractingMetaData) {
 						Thread.sleep(100);
 						waited+=100;
 						if (waited>=240000) { // 4 mins
 							logger.error("Cannot extract meta data from "+EclipseUtils.getFilePath(getEditorInput()));
 						    return Status.CANCEL_STATUS;
 						}
 					}
 					
 					dataSetComponent.setMetaData(metaData);
 					dataSetComponent.refresh();
 					PlotDataEditor.this.plottingSystem.setRootName(dataSetComponent.getRootName());
 					return Status.OK_STATUS;
 				} catch (Exception ne) {
 					logger.error("Cannot open nexus", ne);
 					return Status.CANCEL_STATUS;
 				}
 			}
 		};
 		job.setUser(false);
 		job.schedule(10);
 	}
 
 	
 
 	@Override
 	public void setFocus() {
 		
 		if (plottingSystem!=null) {
 			plottingSystem.setFocus();
 		}
 		
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				final PlotDataComponent pc = (PlotDataComponent)getDataSetComponent();
 				if (pc!=null && (pc.getData()==null || pc.getData().isEmpty())) {
 					createData(getEditorInput());
 				}			
 			}
 		});
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
     @Override
     public void dispose() {
 
      	if (plottingSystem!=null) plottingSystem.dispose();
      	super.dispose();
     }
 
 	public IVariableManager getDataSetComponent() {
 		
 		final IWorkbenchPage wb =EclipseUtils.getActivePage();
 		if (wb==null) return null;
 		
 		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
 		if (view==null) return null;
 		
 		IPage page = view.getCurrentPage();
 		if (!(page instanceof PlotDataPage)) return null;
 		return ((PlotDataPage)page).getDataSetComponent();
 	}
 	public SliceComponent getSliceComponent() {
 		
 		final IWorkbenchPage wb =EclipseUtils.getActivePage();
 		if (wb==null) return null;
 		
 		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
 		if (view==null) return null;
 		
 		IPage page = view.getCurrentPage();
 		if (!(page instanceof PlotDataPage)) return null;
 		return ((PlotDataPage)page).getSliceComponent();
 	}
 
 	public IPlottingSystem getPlotWindow() {
 		return plottingSystem;
 	}
 
 	public AbstractPlottingSystem getPlottingSystem() {
 		return this.plottingSystem;
 	}
 	public PlotDataEditor getDataSetEditor() {
 		return this;
 	}
 
     public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
 		
 		if (clazz == Page.class) {
 			final PlotDataEditor      ed  = getDataSetEditor();
 			return PlotDataPage.getPageFor(ed);
 		} else if (clazz == IToolPageSystem.class) {
 			return getPlottingSystem();
 		}
 		
 		return super.getAdapter(clazz);
 	}
 
 	@Override
 	public void deleteExpression() {
 		getDataSetComponent().deleteExpression();
 	}
 
 	@Override
 	public void addExpression() {
 		getDataSetComponent().addExpression();
 	}
 
 	public String toString(){
 		if (getEditorInput()!=null) return getEditorInput().getName();
 		return super.toString();
 	}
 	
 }
