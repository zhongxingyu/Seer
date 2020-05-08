 package org.dawb.workbench.plotting.tools.history;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.gpu.IOperation;
 import org.dawb.common.gpu.OperationFactory;
 import org.dawb.common.gpu.Operator;
 import org.dawb.common.ui.components.cell.ScaleCellEditor;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.common.ui.plot.trace.TraceWillPlotEvent;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.PlottingConstants;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Scale;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 
 /**
  * Tool whereby images may be added to a static list which has various
  * operations. TODO Create a static method for adding images, for instance
  * from the Project Explorer.
  * 
  * @author fcp94556
  *
  */
 public class ImageHistoryTool extends AbstractHistoryTool implements MouseListener {
 
 	private static Logger logger = LoggerFactory.getLogger(ImageHistoryTool.class);
 	
 	/**
 	 * We simply keep the history in a static map of traces.
 	 */
 	private static final Map<String, HistoryBean> imageHistory;
     static {
         imageHistory = new LinkedHashMap<String, HistoryBean>(17);   		
     }
     
     protected Map<String, HistoryBean> getHistoryCache() {
     	return imageHistory;
     }
     
     /**
      * Assigned on the first activate and then kept to avoid
      * the wrong data being used as the base because the plot
      * will be changing.
      */
     private AbstractDataset originalData;
 	private MathsJob        updateJob;
 
 	private IOperation operation;
 	
 	private enum ImageHistoryMarker { MARKER }
     
     public ImageHistoryTool() {
     	super(false);
     	this.updateJob = new MathsJob();
     	
     	// Use CPU it is *not* slower for the maths this tool does
     	// To try GPU change to getBasicGpuOperation()
     	this.operation = OperationFactory.getBasicCpuOperation();
     	
 		this.traceListener = new ITraceListener.Stub() {
 			
 			@Override
 			public void traceAdded(TraceEvent evt) {
 				if (!isActive())          return;
 				if (updatingPlotsAlready) return;
				if (((IImageTrace)evt.getSource()).getUserObject()==ImageHistoryMarker.MARKER) return;
 				updatePlots(false);
 			}
 			
 			@Override
 			public void traceRemoved(TraceEvent evt) {
 				if (!isActive())          return;
 				if (updatingPlotsAlready) return;
				if (((IImageTrace)evt.getSource()).getUserObject()==ImageHistoryMarker.MARKER) return;
 				updatePlots(false);
 			}
 			
 			@Override
 			public void traceWillPlot(final TraceWillPlotEvent evt) {
 				if (!isActive())               return;
 				if (updatingPlotsAlready)      return;
 				if (evt.getImageTrace()==null) return;
 				if (evt.getImageTrace().getUserObject()==ImageHistoryMarker.MARKER) return;
 				originalData = evt.getImage();
 				if (getImageTrace()==null) return;
 				if (!isActiveSelections()) return;
 		
 				AbstractDataset set = getCombinedData(null);
 				if (set==null) return;
 				if (set.getSize()==1) {
 					evt.doit = false;
 					return;
 				}
 				if (!set.isCompatibleWith(originalData)) return;
 				
 				evt.getImageTrace().setUserObject(ImageHistoryMarker.MARKER);
 				evt.setImageData(set, evt.getImageTrace().getAxes());
 			}
 
 		};
 
     }
 	
 	@Override
 	public void activate() {
 		
         if (getPlottingSystem()!=null && originalData==null) { 
         	
         	final IImageTrace imageTrace = getImageTrace();
         	if (imageTrace!=null && imageTrace.getUserObject()!=ImageHistoryMarker.MARKER)  {
         	    this.originalData = imageTrace!=null ? imageTrace.getData() : null;
         	}
 		}
 		super.activate();
         refresh();
 	}
 	
 	public void deactivate() {
 		super.deactivate();
 		operation.deactivate(); // It can still be used
 	}
 	
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	protected IAction createAddAction() {
 		return new Action("Add image to compare table", Activator.getImageDescriptor("icons/add.png")) {
 			public void run() {
 				final Collection<ITrace> traces = getPlottingSystem().getTraces(IImageTrace.class);
 				if (traces==null||traces.isEmpty()) return;
 				
 				// TODO Check if one of our history traces.
 				for (ITrace iTrace : traces) {
 					
 					if (iTrace.getUserObject()==HistoryType.HISTORY_PLOT) continue;
 					final IImageTrace imageTrace = (IImageTrace)iTrace;
 					String plotName =  ((AbstractPlottingSystem)getPlottingSystem()).getTitle();
 					if (plotName==null || "".equals(plotName)) {
 						plotName = imageTrace.getName();
 					}
 					addImageToHistory(imageTrace.getData(),plotName);
 				}
 				refresh();
 			}
 		};	
 	}
 	
 	protected void addImageToHistory(final AbstractDataset data, String name) {
 		
 		if (name==null) name = data.getName();
 		final HistoryBean bean = new HistoryBean();
 		bean.setData(data);
 		final List<AbstractDataset> axes = getImageTrace()!=null ? getImageTrace().getAxes() : null;
 		bean.setAxes(axes);
 		bean.setTraceName(name);
 		bean.setPlotName(getPlottingSystem().getPlotName());
 		bean.setOperator(Operator.ADD);
 		imageHistory.put(bean.getTraceKey(), bean);
 	}
 	
 	protected MenuManager createActions(MenuManager manager) {
 		
 
 		final IAction include = new Action("Include current plot", Activator.getImageDescriptor("icons/include-current-image.png")) {
 			public void run() {
 				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.INCLUDE_ORIGINAL, isChecked());
 				updatePlots(false);
 				refresh();
 			}
 		};
 		include.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.INCLUDE_ORIGINAL));
 		
 		final IAction revert = new Action("Revert plot", Activator.getImageDescriptor("icons/reset.gif")) {
 			public void run() {
 				AbstractDataset plot = getOriginalData();
 				if (plot==null) return;
 				
 				for (String key : imageHistory.keySet()) {
 					imageHistory.get(key).setSelected(false);
 				}
 				setPlotImage(plot);
 				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.INCLUDE_ORIGINAL, true);
 				include.setChecked(true);
 				refresh();
 			}
 		};
 		
 		final IAction up = new Action("Move up", Activator.getImageDescriptor("icons/arrow_up.png")) {
 			public void run() {
 				moveBean(-1);
 			}
 		};
 		
 		final IAction down = new Action("Move down", Activator.getImageDescriptor("icons/arrow_down.png")) {
 			public void run() {
 				moveBean(1);
 			}
 		};
 		
 		getSite().getActionBars().getToolBarManager().add(include);
 		getSite().getActionBars().getToolBarManager().add(revert);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 
 		getSite().getActionBars().getToolBarManager().add(up);
 		getSite().getActionBars().getToolBarManager().add(down);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		
 		manager.add(include);
 		manager.add(revert);
 		manager.add(new Separator());
 		manager.add(up);
 		manager.add(down);
 		manager.add(new Separator());
 		
 		super.createActions(manager);
 		
 		return manager;
 	}
 
 	protected AbstractDataset getOriginalData() {
 		
 		// Try and read the original data from the editor.
 		return originalData; // We attempt to cache it otherwise.
 	}
 
 	protected void moveBean(int i) {
 		
 		final HistoryBean  bean   = getSelectedPlot();
 		if (bean==null) return;
 		
 		final List<String>            keys = new ArrayList<String>(imageHistory.keySet());
 		final Map<String, HistoryBean> tmp = new HashMap<String, HistoryBean>(imageHistory);
 		final int index = keys.indexOf(bean.getTraceKey());
 		if (index<0) return;
 		
 		if (index+1>keys.size() || index+1<0) return;
 		
 		keys.remove(index);
 		keys.add(index+i, bean.getTraceKey());
 		
 		imageHistory.clear();
 		for (String key : keys) {
 			imageHistory.put(key, tmp.get(key));
 		}
 		refresh();
 		updatePlots(false);
 	}
 
 	@Override
 	protected void updatePlots(boolean force) {
 		if (!getPlottingSystem().is2D()) {
 			logger.error("Plotting system is not plotting 2D data, image history should not be active.");
 			return;
 		}
 		if (updatingPlotsAlready&&!force) return;
 		updatingPlotsAlready = true;
 		updateJob.cancel();
 		updateJob.schedule();
 	}
 	
 	private class MathsJob extends Job {
 		
 		public MathsJob() {
 			super("Process images");
 			setUser(false);
 			setPriority(Job.INTERACTIVE);
 		}
 		
 		public IStatus run(IProgressMonitor monitor) {
 
 			try {
 				// Do nothing if 1D data plotted
 				if (!getPlottingSystem().is2D()) return Status.CANCEL_STATUS;
 				if (!isActive()) return Status.CANCEL_STATUS;
 				if (!getPlottingSystem().getPlotType().is2D()) return Status.CANCEL_STATUS;
 				
 				final long start = System.currentTimeMillis();
 				AbstractDataset a = getCombinedData(monitor);
 				if (a==null) return Status.CANCEL_STATUS;
 				if (a.getSize()==1) {
 					getPlottingSystem().clear();
 					return Status.CANCEL_STATUS;
 				}
 				
 				final long end = System.currentTimeMillis();
 				logger.trace("Processed image maths in "+(end-start));
 				
 				setPlotImage(a);
 
 				return Status.OK_STATUS;
 				
 			} finally {
 				updatingPlotsAlready = false;
 			}
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @return Data to plot, size 1 dataset to do clear, null to do nothing
 	 */
 	private AbstractDataset getCombinedData(IProgressMonitor monitor) {
 		
 		boolean includeCurrentPlot = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.INCLUDE_ORIGINAL);
 		
 		// Loop over history and reprocess maths.
 		AbstractDataset od = getOriginalData();
 		if (!isActiveSelections()) {
 			if (includeCurrentPlot) {
 			    return od;
 			} else {
 				return new IntegerDataset(1); // Clear
 			}
 		}
 		
 		AbstractDataset a  = od!=null&&includeCurrentPlot
 				           ? od 
 				           : null;
 		if (od!=null && od.getRank()!=2) return null; // This is image compare!
 		
 		for (String key : imageHistory.keySet()) {
 			
 			if (monitor!=null && monitor.isCanceled()) return null;
 			if (!isActive()) return null;
 			
 			final HistoryBean bean = imageHistory.get(key);
 			if (bean==null)         continue;
 			if (!bean.isSelected()) continue;
 			if (bean.getWeighting()<1) continue;
 			
 			if (a==null) { 
 				if (bean.getData()==null) continue;
 				a = bean.getData();
 				continue;
 			}
 			
 			if (!a.isCompatibleWith(bean.getData())) {
 				bean.setSelected(false);
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						viewer.refresh(bean);
 					}
 				});
 				continue;
 			}
 			
 			AbstractDataset data = bean.getData();
 			if (bean.getWeighting()<100) { // Reduce its intensity
 				data = operation.process(data, bean.getWeighting()/100d, Operator.MULTIPLY);
 			}
 			a = operation.process(a, data, bean.getOperator());
 		}
 
 		return a;
 	}
 	
 	public void setPlotImage(final AbstractDataset plot) {
 		
 		Display.getDefault().syncExec(new Runnable() {
 			public void run () {
 				if (!getPlottingSystem().is2D()) return;
 				if (!isActive())                 return;
 				if (!getPlottingSystem().getPlotType().is2D()) return;
 				
 				getPlottingSystem().removeTraceListener(traceListener);
 				try {
 					IImageTrace imageTrace = getImageTrace();
 					if (imageTrace==null) {
 						imageTrace = getPlottingSystem().createImageTrace(plot.getName()!=null?plot.getName():"");
 						imageTrace.setData(plot, null, false);
 						getPlottingSystem().addTrace(imageTrace);
 						getPlottingSystem().autoscaleAxes();	
 						return;
 					}
 					
 					boolean current = updatingPlotsAlready;
 					try {
 						updatingPlotsAlready = true;
 						getPlottingSystem().clear();
 			     	} finally {
 			     		updatingPlotsAlready = current;
 			     	}
 					final IImageTrace image = getPlottingSystem().createImageTrace(imageTrace!=null?imageTrace.getName():"Image");
 					if (image==null) return;
 					image.setData(plot, imageTrace!=null?imageTrace.getAxes():null, false);
 					image.setUserObject(ImageHistoryMarker.MARKER);
 					getPlottingSystem().addTrace(image);
 					getPlottingSystem().repaint();
 				} catch (Throwable ne ) {
 					logger.error(ImageHistoryTool.class.getSimpleName()+" unable to process image. This might not be a fatal error because an image might not be plotted.");
 				} finally {
 					getPlottingSystem().addTraceListener(traceListener);
 				}
 			}
 		});
 	}
 
 	protected boolean isActiveSelections() {
 		final Map<String,HistoryBean> history = getHistoryCache();
 		if (history==null || history.size()<1) return false;
 		for (HistoryBean historyBean : history.values()) {
 			if (historyBean.isSelected()) return true;
 		}
 		return false;
 	}
 	protected boolean isEmpty() {
 		final Map<String,HistoryBean> history = getHistoryCache();
 		if (history==null || history.size()<1) return true;
 		return false;
 	}
 
 	@Override
 	protected void updatePlot(HistoryBean bean, boolean force) {
 		updatePlots(force); // We update everything when one changes.
 	}
 
 	@Override
 	protected void createColumns(TableViewer viewer) {
 		
 		viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
 			public void handleEvent(Event event) {
 				// height cannot be per row so simply set
 				event.height = 40;
 			}
 		});
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 		viewer.setColumnProperties(new String[] { "Selected", "Name", "Original Plot", "Operator" });
 		
 		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Plot"); // Selected
 		var.getColumn().setWidth(50);
 		var.setLabelProvider(new ImageCompareLabelProvider());
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(200);
 		var.setLabelProvider(new ImageCompareLabelProvider());
 		var.setEditingSupport(new ImageNameEditingSupport(viewer));
 		
 		var = new TableViewerColumn(viewer, SWT.CENTER, 2);
 		var.getColumn().setText("Original File");
 		var.getColumn().setWidth(0);
 		var.getColumn().setMoveable(false);
 		var.getColumn().setResizable(false);		
 		var.setLabelProvider(new ImageCompareLabelProvider());
 		
 		var = new TableViewerColumn(viewer, SWT.CENTER, 3);
 		var.getColumn().setText("Operator");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new ImageCompareLabelProvider());
 		var.setEditingSupport(new ImageOperatorEditingSupport(viewer));
 		
 		var = new TableViewerColumn(viewer, SWT.CENTER, 4);
 		var.getColumn().setText("Shape");
 		var.getColumn().setWidth(150);
 		var.setLabelProvider(new ImageCompareLabelProvider());
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, 5);
 		var.getColumn().setText("Weight");
 		var.getColumn().setWidth(150);
 		var.setLabelProvider(new ImageCompareLabelProvider());
 		var.setEditingSupport(new ImageWeightingEditingSupport(viewer));
 	}
 	
 	private class ImageCompareLabelProvider extends ColumnLabelProvider {
 		
 		private Image checkedIcon;
 		private Image uncheckedIcon;
 		
 		public ImageCompareLabelProvider() {
 			
 			ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
 			checkedIcon   = id.createImage();
 			id = Activator.getImageDescriptor("icons/unticked.gif");
 			uncheckedIcon =  id.createImage();
 		}
 		
 		private int columnIndex;
 		public void update(ViewerCell cell) {
 			columnIndex = cell.getColumnIndex();
 			super.update(cell);
 		}
 		
 		public Image getImage(Object element) {
 			
 			if (!(element instanceof HistoryBean)) return null;
 
 			if (columnIndex==0) {
 				final HistoryBean bean = (HistoryBean)element;
 				return bean.isSelected() ? checkedIcon : uncheckedIcon;
 			}
 			
 			return null;
 		}
 		
 		public String getText(Object element) {
 			
 			if (element instanceof String) return "";
 			
 			final HistoryBean bean = (HistoryBean)element;
 			if (columnIndex==1) {
 			     return bean.getTraceName();
 			}
 			if (columnIndex==2) {
 			     return bean.getPlotName();
 			}
 			if (columnIndex==3) {
 				boolean includeCurrentPlot = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.INCLUDE_ORIGINAL);
 				if (getIndex(bean)==0 && !includeCurrentPlot) return "";
 			    return bean.getOperator().getName();
 			}
 			if (columnIndex==4) {
 				try {
 			        return Arrays.toString(bean.getData().getShape());
 				} catch (Throwable ne) {
 					return "";
 				}
 			}
 			if (columnIndex==5) {
 				return bean.getWeighting()+" %";
 			}
 			return "";
 		}
 		
 		private int getIndex(HistoryBean bean) {
 			final List<String> keys = new ArrayList<String>(imageHistory.keySet());
 			return keys.indexOf(bean.getTraceKey());
 		}
 		
 		public Color getForeground(Object element) {
 			if (!(element instanceof HistoryBean)) return null;
 			HistoryBean bean = (HistoryBean)element;
 			
 			if (columnIndex==4&&!isShapeCompatible(element)) {
 				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
 			}
 			
 			return bean.isSelected() 
 				   ? Display.getDefault().getSystemColor(SWT.COLOR_BLACK)
 				   : Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
 		}
 		
 		public String getToolTipText(Object element) {
 			if (!isShapeCompatible(element)) return "Shape of compare image is not the same as the plot.";
 			return super.getToolTipText(element);
 		}
 
 		private boolean isShapeCompatible(Object element) {
 			final AbstractDataset od = getOriginalData();
 			if (od==null) return true;
 			if (!(element instanceof HistoryBean)) return true;
 			HistoryBean bean = (HistoryBean)element;
 			return od.isCompatibleWith(bean.getData());
 		}
 
 		public void dispose() {
 			super.dispose();
 			checkedIcon.dispose();
 			uncheckedIcon.dispose();
 		}
 	}
 	
 	private class ImageNameEditingSupport extends EditingSupport {
 
 		public ImageNameEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			return new TextCellEditor((Composite)getViewer().getControl());
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return ((HistoryBean)element).getTraceName();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			((HistoryBean)element).setTraceName((String)value);
 			viewer.refresh(element);
 		}
 
 	}
 
 	private class ImageOperatorEditingSupport extends EditingSupport {
 
 		public ImageOperatorEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 		}
 
 		@Override
 		protected CellEditor getCellEditor(final Object element) {
 			ComboBoxCellEditor ed = new ComboBoxCellEditor((Composite)getViewer().getControl(), Operator.getOperators(), SWT.READ_ONLY);
 		
 			((CCombo)ed.getControl()).addSelectionListener(new SelectionAdapter() {			
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					ImageOperatorEditingSupport.this.setValue(element, ((CCombo)e.getSource()).getSelectionIndex());
 				}
 			});
 			return ed;
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return ((HistoryBean)element).getOperator().getIndex();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			((HistoryBean)element).setOperator(Operator.getOperator((Integer)value));
 			((HistoryBean)element).setSelected(true);
 			viewer.refresh(element);
 			updatePlots(false);
 		}
 
 	}
 
 	private class ImageWeightingEditingSupport extends EditingSupport {
 
 		public ImageWeightingEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 		}
 
 		@Override
 		protected CellEditor getCellEditor(final Object element) {
 			final ScaleCellEditor ed = new ScaleCellEditor((Composite)getViewer().getControl());
 			ed.setMinimum(0);
 			ed.setMaximum(100);
 			ed.addSelectionListener(new SelectionAdapter() {		
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					int value = ((Scale)e.getSource()).getSelection();
 					HistoryBean bean = (HistoryBean)element;
 					if (value==0) {
 						bean.setWeighting(0);
 						bean.setSelected(false);
 					} else if (value>100) {
 						bean.setWeighting(100);
 						bean.setSelected(true);
 					} else {
 						bean.setWeighting(value);
 						bean.setSelected(true);
 					}
 					ed.getControl().setToolTipText(String.valueOf(value));
 					viewer.update(bean, new String[]{"Selected"});
 					updateJob.cancel();
 					updateJob.schedule();
 				}
 			});
 			return ed;
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return ((HistoryBean)element).getWeighting();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			((HistoryBean)element).setWeighting((Integer)value);
 			viewer.refresh(element);
 			updatePlots(false);
 		}
 
 	}
 
 	@Override
 	public void setData(Object obj){
 		if(obj instanceof List<?>){
 			List<?> images = (List<?>)obj;
 			clearCache();
 			for (Object image : images) {
 				if(image instanceof AbstractDataset)
 					addImageToHistory((AbstractDataset)image, ((AbstractDataset)image).getName());
 			}
 		}
 	}
 }
