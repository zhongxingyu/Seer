 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.workbench.ui.data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventListener;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.IExpressionObjectService;
 import org.dawb.common.services.IVariableManager;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.DawbUtils;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
 import org.dawb.common.ui.util.DialogUtils;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.util.io.FileUtils;
 import org.dawb.common.util.io.PropUtils;
 import org.dawb.gda.extensions.util.DatasetTitleUtils;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.data.wizard.PythonFilterWizard;
 import org.dawb.workbench.ui.editors.preference.EditorConstants;
 import org.dawb.workbench.ui.editors.preference.EditorPreferencePage;
 import org.dawb.workbench.ui.transferable.TransferableDataObject;
 import org.dawnsci.io.h5.H5Loader;
 import org.dawnsci.plotting.AbstractPlottingSystem;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.IPlottingSystemSelection;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.tool.IToolChangeListener;
 import org.dawnsci.plotting.api.tool.IToolPage;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.tool.ToolChangeEvent;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.tools.reduction.DataReductionWizard;
 import org.dawnsci.slicing.api.data.ITransferableDataObject;
 import org.dawnsci.slicing.api.data.ITransferableDataService;
 import org.dawnsci.slicing.api.system.DimsDataList;
 import org.dawnsci.slicing.api.system.ISliceSystem;
 import org.dawnsci.slicing.api.util.SliceUtils;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Region;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.progress.IProgressService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IErrorDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 import uk.ac.diamond.scisoft.analysis.utils.OSUtils;
 
 /**
  * This view can view and plot any file. It is most efficient if the Loader that LoaderFactory
  * uses for this file type is an IMetaLoader. 
  */
 public class PlotDataComponent implements IVariableManager, MouseListener, KeyListener, IPlottingSystemSelection, IAdaptable {
 		
 	private static final Logger logger = LoggerFactory.getLogger(PlotDataComponent.class);
 
 	// Use table as it might get extended to do more later.
 	protected TableViewer dataViewer;
 	
 	/**
 	 * data is the objects for the table, either a String or an ExpressionObject
 	 * currently. Probably a better design possible with a single object type.
 	 */
 	protected List<ITransferableDataObject> data;
 	protected String                filePath;
 	protected String                fileName;
 	private   String                rootName;
 	private   boolean               staggerSupported = false;
 
 	private IEditorPart              editor;
 	private IPropertyChangeListener  propListener;
 	private ArrayList<IAction>       dataComponentActions;
 	private Composite                container;
 	private DataTableFilter               dataFilter;
 
 	private IAction                  dataReduction;
 	private ITraceListener           traceListener;
 	private ITraceListener           dataViewRefreshListener;
 	private IToolChangeListener      toolListener;
 
     private IDataHolder dataHolder;
 	private IMetaData   metaData;
 
 	private ITransferableDataService transferableService;
 	private IExpressionObjectService expressionService;
 	
 	public PlotDataComponent(final IEditorPart editor) throws Exception {
 				
 		this.data = new ArrayList<ITransferableDataObject>(7);
 		this.editor   = editor;
 		
 		this.expressionService  = (IExpressionObjectService)ServiceManager.getService(IExpressionObjectService.class);
 		this.transferableService= (ITransferableDataService)ServiceManager.getService(ITransferableDataService.class);
 		
 		this.propListener = new IPropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().equals(EditorConstants.IGNORE_DATASET_FILTERS)) {
 					
 					if (filePath==null) return;
 					IProgressService service = (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);
 					try {
 						// Changed to cancellable as sometimes loading the tree takes ages and you
 						// did not mean such to choose the file.
 						service.run(true, true, new IRunnableWithProgress() {
 							@Override
 							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 								try {
 									final IMetaData  meta = LoaderFactory.getMetaData(filePath, new ProgressMonitorWrapper(monitor));
 									Display.getDefault().syncExec(new Runnable() {
 										public void run() {
 											try {
 												PlotDataComponent.this.setData(LoaderFactory.getData(filePath, true, true, null), meta);
 											} catch (Exception e) {
 												logger.error("Cannot change file path", e);
 											}
 										}
 									});
 								} catch (Exception e) {
 									throw new InvocationTargetException(e);
 								}
 							}
 						});
 					} catch (Exception ne) {
 						logger.error("Unable to refresh data set list", ne);
 					}
 				} else if (event.getProperty().equals(EditorConstants.SHOW_XY_COLUMN)) {
 					setColumnVisible(2, 32, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_DATA_SIZE)) {
 					setColumnVisible(3, 100, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_DIMS)) {
 					setColumnVisible(4, 100, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_SHAPE)) {
 					setColumnVisible(5, 100, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_VARNAME)) {
 					setColumnVisible(6, 100, (Boolean)event.getNewValue());
 				}
 			}
 		};
 		// If they change the ignore filters activity, recompute the available data sets.
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propListener);
 		
 		// Trace listener is used to refresh the table.
 		this.dataViewRefreshListener = new ITraceListener.Stub() {
 			protected void update(TraceEvent evt) {
 				if (dataViewer==null || dataViewer.getControl().isDisposed()) return;
 				dataViewer.refresh();
 			}
 		};
 		if (getPlottingSystem()!=null) getPlottingSystem().addTraceListener(dataViewRefreshListener);
 	}
 
 	public Composite getControl() {
 		return container;
 	}
 
 	protected void setColumnVisible(final int col, final int width, boolean isVis) {
 		if (this.dataViewer==null || this.dataViewer.getControl().isDisposed()) return;
 		dataViewer.getTable().getColumn(col).setWidth(isVis?width:0);
 		dataViewer.getTable().getColumn(col).setResizable(isVis?true:false);
 	}
 	
 	/**
 	 * Create contents of the view part.
 	 * @param parent
 	 */
 	public void createPartControl(final Composite parent, IActionBars bars) throws Exception {
 		
 		this.container = new Composite(parent, SWT.NONE);
 		if (parent.getLayout() instanceof GridLayout) container.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		GridLayout gl_container = new GridLayout(1, false);
 		gl_container.verticalSpacing = 0;
 		gl_container.marginWidth = 0;
 		gl_container.marginHeight = 0;
 		gl_container.horizontalSpacing = 0;
 
 		container.setLayout(gl_container);
 
 		final Text searchText = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
 		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		searchText.setToolTipText("Search on data set name or shape\nFor instance '132, 4096' to find all of that shape." );
 				
 		this.dataViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL);
 		
 		dataViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 		dataViewer.getTable().addMouseListener(this);
 		dataViewer.getTable().addKeyListener(this);
 		dataViewer.getTable().setLinesVisible(true);
 		dataViewer.getTable().setHeaderVisible(true);
 		
 		createColumns();
         dataViewer.setColumnProperties(new String[]{"Data","Length"});
          
 		dataViewer.getTable().setItemCount(data.size());
 		dataViewer.setUseHashlookup(true);
 
 		dataViewer.setContentProvider(new IStructuredContentProvider() {			
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			}
 			
 			@Override
 			public void dispose() {
 			}
 			@Override
 			public Object[] getElements(Object inputElement) {
 				return data.toArray(new Object[data.size()]);
 			}
 		});	
 		
 		// Maybe being the selection provider cause the left mouse problem
         //if (getSite()!=null) getSite().setSelectionProvider(dataViewer);
 		dataViewer.setInput(new String());
 				
 		createActions(bars);
 		
 		setColumnVisible(2, 36,  Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_XY_COLUMN));
 		setColumnVisible(3, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_DATA_SIZE));
 		setColumnVisible(4, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_DIMS));
 		setColumnVisible(5, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_SHAPE));
 		setColumnVisible(6, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_VARNAME));
 	
 		try {
 
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesUpdated(TraceEvent evt) {
 					updateSelection(true);
 					dataViewer.refresh();
 				}
 			};
 			if (getPlottingSystem()!=null) getPlottingSystem().addTraceListener(traceListener);
 			
 			if (dataReduction!=null) {
 				this.toolListener = new IToolChangeListener() {
 
 					@Override
 					public void toolChanged(ToolChangeEvent evt) {
 						if (dataReduction!=null) {
 							dataReduction.setEnabled(isDataReductionToolActive());
 						}
 					}
 				};
 				getAbstractPlottingSystem().addToolChangeListener(toolListener);
 			}
 			
 			getAbstractPlottingSystem().addPropertyChangeListener(new IPropertyChangeListener() {				
 				@Override
 				public void propertyChange(PropertyChangeEvent event) {
 					try {
 						saveAxisSettings(".xAxis", getPlottingSystem().getSelectedXAxis());
 						saveAxisSettings(".yAxis", getPlottingSystem().getSelectedYAxis());
 					} catch (Throwable ne) {
 						logger.error("Cannot save settings for plotting configuration!", ne);
 					}
 				}
 			});	
 			
 			readAxisSettings(".xAxis", getPlottingSystem().getSelectedXAxis());
 			readAxisSettings(".yAxis", getPlottingSystem().getSelectedYAxis());
 			
 			this.dataFilter = new DataTableFilter();
 			dataViewer.addFilter(dataFilter);
 			searchText.addModifyListener(new ModifyListener() {		
 				@Override
 				public void modifyText(ModifyEvent e) {
 					if (parent.isDisposed()) return;
 					dataFilter.setSearchText(searchText.getText());
 					dataViewer.refresh();
 				}
 			});
 		}catch (Exception ne) {
 			logger.error("Cannot add trace listener!", ne);
 		}
 		
 		// Allow the colours to be drawn nicely.
 		final Table table = dataViewer.getTable();
 		if (OSUtils.isWindowsOS()) table.addListener(SWT.EraseItem, new Listener() {
 			public void handleEvent(Event event) {
 				
 				GC gc = event.gc;
 				Color foreground = gc.getForeground();
 				Color background = gc.getBackground();
 
 				try {
 					Rectangle area = table.getClientArea();
 					/*
 					 * If you wish to paint the selection beyond the end of last column,
 					 * you must change the clipping region.
 					 */
 					int columnCount = table.getColumnCount();
 					if (event.index == columnCount - 1 || columnCount == 0) {
 						int width = area.x + area.width - event.x;
 						if (width > 0) {
 							Region region = new Region();
 							gc.getClipping(region);
 							region.add(event.x, event.y, width, event.height);
 							gc.setClipping(region);
 							region.dispose();
 						}
 					}
 	
 					gc.setAdvanced(true);
 					if (gc.getAdvanced()) gc.setAlpha(50);
 	
 					if ((event.detail & SWT.SELECTED) != 0) {
 						gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
 						gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
 					}
 	
 					final TableItem item = table.getItem(new Point(event.x, event.y));
 					// Draw the colour in the Value column
 					if (item!=null && item.getData() instanceof TransferableDataObject) {
 						
 						Rectangle     nameArea = item.getBounds(1); // Name column
 						TransferableDataObject cn = (TransferableDataObject)item.getData();
 						
 						if (cn.isChecked() && !cn.isExpression() && nameArea.contains(event.x, event.y)) {
 							int origAlpha = gc.getAlpha();
 							gc.setAlpha(255);
 							final Color plotColor = get1DPlotColor(cn);
 							if (plotColor!=null) {
 								gc.setForeground(plotColor);
 								int offset = cn.getFilterPath()!=null ? 20 : 0;
 								gc.drawText(cn.getDisplayName(rootName), item.getBounds().x+16+offset, item.getBounds().y+1);
 								event.doit = false;
 							}
 							gc.setAlpha(origAlpha);
 						} 
 						
 						if ((event.detail & SWT.HOT) != 0) {
 							// Draw the colour in the Value column
 							gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
 							Rectangle bounds = event.getBounds();
 						    gc.fillGradientRectangle(0, bounds.y, 500, bounds.height, false);
 						}
 					}
 						
 				} finally {
 					if ((event.detail & SWT.SELECTED) != 0) event.detail &= ~SWT.SELECTED;
 					if ((event.detail & SWT.HOT) != 0) 	    event.detail &= ~SWT.HOT;
 					// restore colors for subsequent drawing
 					gc.setForeground(foreground);
 					gc.setBackground(background);
 				}
 
 			}
 			
 			
 
 		});
 
 	}
 	
 
 	
 	private static final String LOG_PREF   = "org.dawb.workbench.ui.editors.log.axis-";
 	private static final String TIME_PREF  = "org.dawb.workbench.ui.editors.time.axis-";
 	private static final String FORMAT_PREF= "org.dawb.workbench.ui.editors.format.axis-";
 	
 	private void saveAxisSettings(String key, IAxis selectedAxis) {
 		
 		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		
 		if (store.getBoolean(EditorConstants.SAVE_LOG_FORMAT))    store.setValue(LOG_PREF+getExtension()+key,    selectedAxis.isLog10());
 		if (store.getBoolean(EditorConstants.SAVE_TIME_FORMAT))   store.setValue(TIME_PREF+getExtension()+key,   selectedAxis.isDateFormatEnabled());
 		if (store.getBoolean(EditorConstants.SAVE_FORMAT_STRING)) store.setValue(FORMAT_PREF+getExtension()+key, selectedAxis.getFormatPattern());	
 	}
 	
 	private void readAxisSettings(String key, IAxis selectedAxis) {
 		
 		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		if (store.getBoolean(EditorConstants.SAVE_LOG_FORMAT)) if (store.contains(LOG_PREF+getExtension()+key)) {
 			selectedAxis.setLog10(store.getBoolean(LOG_PREF+getExtension()+key));
 		}
 		if (store.getBoolean(EditorConstants.SAVE_TIME_FORMAT)) if (store.contains(TIME_PREF+getExtension()+key)) {
 		    selectedAxis.setDateFormatEnabled(store.getBoolean(TIME_PREF+getExtension()+key));
 		}
 		if (store.getBoolean(EditorConstants.SAVE_FORMAT_STRING)) if (store.contains(FORMAT_PREF+getExtension()+key)) {
 		    selectedAxis.setFormatPattern(store.getString(FORMAT_PREF+getExtension()+key));	
 		}
 	}
 
 	private final String getExtension() {
 		try {
 			return FileUtils.getFileExtension(filePath);
 			
 		} catch (Throwable ne) {
 			return "";
 		}
 	}
 
 	public void setFocus() {
 		if (dataViewer!=null && !dataViewer.getControl().isDisposed()) {
 			dataViewer.getControl().setFocus();
 			
 			if (dataReduction!=null) {
 				dataReduction.setEnabled(isDataReductionToolActive());
 			}
 
 		}
 	}
 
 	private void readExpressions() throws Exception {
 		
 		final String cachePath = DawbUtils.getDawbHome()+getFileName()+".properties";
 		Properties props = PropUtils.loadProperties(cachePath);
 		if (props!=null) {
 			try {
 				for (Object ob : props.keySet()) {
 					final String mementoKey = (String)ob;
 					final String memento    = props.getProperty(mementoKey);
 					if (TransferableDataObject.isMementoKey(mementoKey)) {
 						final String possibleName = TransferableDataObject.getName(memento);
 						ITransferableDataObject o = getCheckableObjectByName(possibleName);
 						if (o!=null) {
 							o.setVariable(TransferableDataObject.getVariable(memento));
 						} else {
 							o = transferableService.createExpression(dataHolder, metaData);
 							o.createExpression(this, mementoKey, props.getProperty(mementoKey));
 							data.add(o);
 						}
 					}
 				}
 			} catch (Exception ne) {
 				throw new PartInitException(ne.getMessage());
 			}
 		}
 	}
 	
 	public void saveExpressions() {
 		try {
 			final Properties props = new Properties();
 			for (ITransferableDataObject check : data) {
 				props.put(check.getMementoKey(), check.getMemento());
 			}
 			// Save properties to workspace.
 			final String cachePath = DawbUtils.getDawbHome()+getFileName()+".properties";
 			PropUtils.storeProperties(props, cachePath);
 			
 		} catch (Exception e) {
 			logger.error("Cannot save expression", e);
 		}
 	}
 	
 	
 	/**
 	 * Puts actions on right click menu and in action bar.
 	 * @param bars
 	 */
 	private void createActions(final IActionBars bars) {	
 		
 		
 	    final MenuManager menuManager = new MenuManager();
 	    menuManager.setRemoveAllWhenShown(true);
 	    dataViewer.getControl().setMenu (menuManager.createContextMenu(dataViewer.getControl()));
 	    
 		final List<Object> rightClickActions = new ArrayList<Object>(11);
 	    createDimensionalActions(rightClickActions, false);
 
 	    PlotDataComponent.this.dataReduction = new Action("Data reduction...", Activator.getImageDescriptor("icons/data-reduction.png")) {
 			@Override
 			public void run() {
 				DataReductionWizard wiz=null;
 				try {
 					wiz = (DataReductionWizard)EclipseUtils.openWizard(DataReductionWizard.ID, false);
 				} catch (Exception e) {
 					logger.error("Cannot open wizard "+DataReductionWizard.ID, e);
 				}
 				wiz.setData(getIFile(true),
 						    getSelectionNames().get(0),
 						    (IDataReductionToolPage)getAbstractPlottingSystem().getActiveTool(),
 						    getSliceSet());
 				wiz.setSlice(getSliceSet(), getSliceData());
 				
 				// TODO Should be non modal, it takes a while.
 				WizardDialog wd = new  WizardDialog(Display.getDefault().getActiveShell(), wiz);
 				wd.setTitle(wiz.getWindowTitle());
 				wd.create();
 				wd.getShell().setSize(650, 800);
 				DialogUtils.centerDialog(Display.getDefault().getActiveShell(), wd.getShell());
 				wd.open();
 			}
 		};
 
 		final Action copy = new Action("Copy selected data (it can then be pasted to another data list.)", Activator.getImageDescriptor("icons/copy.gif")) {
 			public void run() {
 				final ITransferableDataObject sel = (ITransferableDataObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
 				if (sel==null) return;
 				transferableService.setBuffer(sel);
 			}
 		};
 		bars.getToolBarManager().add(copy);
 		copy.setEnabled(false);
 		
 		final Action paste = new Action("Paste", Activator.getImageDescriptor("icons/paste.gif")) {
 			public void run() {
 				ITransferableDataObject checkedObject = getCheckedObject(transferableService.getBuffer());
 				if (checkedObject==null) return;
 				data.add(checkedObject);
 				checkedObject.setChecked(!checkedObject.isChecked());
 				selectionChanged(checkedObject, true);
 				dataViewer.refresh();
 				
 				final ISliceSystem system = (ISliceSystem)editor.getAdapter(ISliceSystem.class);
 				if (system!=null) system.refresh();
 			}
 		};
 		bars.getToolBarManager().add(paste);
 		paste.setEnabled(false);
 	
 		final Action delete = new Action("Delete", Activator.getImageDescriptor("icons/delete.gif")) {
 			public void run() {
 				final Object sel           = ((StructuredSelection)dataViewer.getSelection()).getFirstElement();
 				final ITransferableDataObject ob  = (ITransferableDataObject)sel;
 				if (ob!=null) {
 					boolean ok = data.remove(ob);
 					if (ok) ob.dispose();
 				}
 				dataViewer.refresh();
 			}
 		};
 		bars.getToolBarManager().add(delete);
 		delete.setEnabled(false);
 		
 		bars.getToolBarManager().add(new Separator());
 		final Action createFilter = new Action("Create Filter", Activator.getImageDescriptor("icons/filter.png")) {
 			public void run() {
 				final Object sel           = ((StructuredSelection)dataViewer.getSelection()).getFirstElement();
 				final ITransferableDataObject ob  = (ITransferableDataObject)sel;
 				if (ob==null) return;
 				chooseFilterFile(ob);
 			}
 		};
 		bars.getToolBarManager().add(createFilter);
 		createFilter.setEnabled(false);
 		
 		final Action clearFilter = new Action("Clear filter", Activator.getImageDescriptor("icons/delete_filter.png")) {
 			public void run() {
 				final Object sel           = ((StructuredSelection)dataViewer.getSelection()).getFirstElement();
 				final ITransferableDataObject ob  = (ITransferableDataObject)sel;
 				if (ob==null) return;
 				clearFilterFile(ob);
 			}
 		};
 		bars.getToolBarManager().add(clearFilter);
 		clearFilter.setEnabled(false);
 	
 		
 		dataViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				final Object sel           = ((StructuredSelection)dataViewer.getSelection()).getFirstElement();
 				final ITransferableDataObject ob  = (ITransferableDataObject)sel;
 				updateActions(copy, paste, delete, createFilter, clearFilter, ob, bars);
 			}
 		});
 		
 		menuManager.addMenuListener(new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				
 				if (staggerSupported) {
 			        updatePlotDimenionsSelected((IAction)rightClickActions.get(1), 
 			        		                    (IAction)rightClickActions.get(2), 
 			        		                    (IAction)rightClickActions.get(3), 
 			        		                    getPlottingSystem().getPlotType());
 				}
 
 		        for (Object action : rightClickActions) {
 		        	if (action instanceof IAction) {
 					    menuManager.add((IAction)action);		
 		        	} else if (action instanceof IContributionItem) {
 				        menuManager.add((IContributionItem)action);
 		        	}
 				}
 			    
 				menuManager.add(new Separator(getClass().getName()+"sep1"));
 				menuManager.add(new Action("Clear") {
 					@Override
 					public void run() {
 						for (ITransferableDataObject co : data) {
 							co.setChecked(false);
 						}
 						selections.clear();
 						dataViewer.refresh();
 						fireSelectionListeners(Collections.<ITransferableDataObject> emptyList());
 					}
 				});
 				
 				menuManager.add(new Separator(getClass().getName()+".copyPaste"));
 				
 				final Object sel           = ((StructuredSelection)dataViewer.getSelection()).getFirstElement();
 				final ITransferableDataObject ob  = (ITransferableDataObject)sel;
 				menuManager.add(copy);
 				menuManager.add(paste);
 				menuManager.add(delete);
 				menuManager.add(new Separator(getClass().getName()+".filter"));
 				menuManager.add(createFilter);
 				menuManager.add(clearFilter);
 				
 				updateActions(copy, paste, delete, createFilter, clearFilter, ob, null);
 
 
 				if (H5Loader.isH5(getFileName())) {
 					menuManager.add(new Separator(getClass().getName()+"sep2"));
 					
 					dataReduction.setEnabled(false);
 					menuManager.add(dataReduction);
 				}
 				
 				menuManager.add(new Separator(getClass().getName()+".error"));
 				
 				/**
 				 * What follows is adding some actions for setting errors on other plotted data sets.
 				 * The logic is a bit convoluted at the moment.
 				 */
 				final ILazyDataset currentSelecedData  = ob!=null ? getLazyValue(ob.getVariable(), null) : null;
 
 				if (currentSelecedData!=null) {
 					if (selections!=null && selections.size() > 0) {
 						menuManager.add(new Action("Set '"+ob.getName()+"' as error on other plotted data...") {
 							@Override
 							public void run() {
 								final PlotDataChooseDialog dialog = new PlotDataChooseDialog(Display.getDefault().getActiveShell());
 								dialog.init(selections, ob);
 								final ITransferableDataObject plotD = dialog.choose();
 								if (plotD!=null) {
 									ILazyDataset set = (ILazyDataset)getLazyValue(plotD.getVariable(), null);
 										
 									if (set instanceof IErrorDataset) { // Data was all read in already.
 										IErrorDataset errSet = (IErrorDataset)set;
 										// Read plotted data into memory, so can read error data too.
 										errSet.setError(getVariableValue(ob.getVariable(), null));
 										
 									} else { // Set errors lazily
 										set.setLazyErrors(currentSelecedData);
 									}
 									fireSelectionListeners(selections);
 
 								}
 							}
 						});
 					}
 
 					final boolean isDatasetError = currentSelecedData instanceof IErrorDataset && ((IErrorDataset)currentSelecedData).hasErrors();
 					final boolean isLazyError    = currentSelecedData.getLazyErrors()!=null;
 					if (isDatasetError || isLazyError) {
 						menuManager.add(new Action("Clear error on '"+currentSelecedData.getName()+"'") {
 							@Override
 							public void run() {
 								currentSelecedData.setLazyErrors(null);
 								fireSelectionListeners(selections);
 							}
 						});
 					}
 				}
 
 				
 				menuManager.add(new Separator(getClass().getName()+"sep3"));
 				menuManager.add(new Action("Preferences...") {
 					@Override
 					public void run() {
 						PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.edna.workbench.editors.preferencePage", null, null);
 						if (pref != null) pref.open();
 					}
 				});
 				
 				menuManager.addMenuListener(new IMenuListener() {			
 					@Override
 					public void menuAboutToShow(IMenuManager manager) {
 						if (dataReduction!=null) dataReduction.setEnabled(isDataReductionToolActive());
 					}
 				});
 			}
 		});
 
 		
 	}
 
 	private PlotDataFilterProvider filterProvider;
 	/**
 	 * Choose a jython file to be used as the filter or create a new one.
 	 * 
 	 * @param ob
 	 */
 	private void chooseFilterFile(ITransferableDataObject ob) {
 
         if (ob==null) return;
         
 		PythonFilterWizard wiz=null;
 		try {
 			wiz = (PythonFilterWizard)EclipseUtils.openWizard(PythonFilterWizard.ID, false);
 		} catch (Exception e) {
 			logger.error("Cannot open wizard "+PythonFilterWizard.ID, e);
 		}
 		
 		// TODO Should be non modal, it takes a while.
 		WizardDialog wd = new  WizardDialog(Display.getDefault().getActiveShell(), wiz);
 		wd.setTitle(wiz.getWindowTitle());
 		wd.create();
 		wd.getShell().setSize(650, 800);
 		DialogUtils.centerDialog(Display.getCurrent().getActiveShell(), wd.getShell());
 		wd.open();
 
 		final String filterPath =  wiz.getPythonPath();
 		if (filterPath==null) return;
          
         if (ob.getFilterPath()!=null) {
         	boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Overwrite", 
         			                  "Do you want to replace filter '"+ob.getFilterPath()+"' with '"+filterPath);
         	if (!ok) return; 
         }
 		
         ob.setFilterPath(filterPath);
         
         saveExpressions(); // TODO save filters as well.
         
         // We now create a filter which calls the associated cpython interperter to filter the data.
         if (filterProvider==null) filterProvider = new PlotDataFilterProvider(getPlottingSystem());
         try {
         	filterProvider.createFilter(ob); // TODO Rank from dialog
         } catch (Exception ne) {
         	ob.setFilterPath(null);
         	String message = ne.getMessage()!=null ? ne.getMessage() : "Filter '"+ob.getFilterPath()+"' is not a valid python script!";
         	MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Cannot set filter", message);
         	Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, message));
         }
 
         ob.setChecked(!ob.isChecked());
         selectionChanged(ob, true);
 	}
 	
 	
 	protected void clearFilterFile(ITransferableDataObject ob) {
 
 		if (ob==null) return;
 		if (filterProvider!=null) filterProvider.deleteFilter(ob); 
 		ob.setFilterPath(null);
 		
         ob.setChecked(!ob.isChecked());
         selectionChanged(ob, true);
 		
 	}
 
 
 	protected void updateActions(Action copy, 
 					             Action paste, 
 					             Action delete, 
 					             Action createFilter, 
 					             Action deleteFilter, 
 			                     ITransferableDataObject ob,
 			                     IActionBars bars) {
 		
 		if (ob!=null) {
 		    copy.setText("Copy '"+ob.getName()+"' (can be paste to other data).");
 			copy.setEnabled(true);
 		} else {
 			copy.setEnabled(false);
 		}
 		
 		ITransferableDataObject currentCopiedData = transferableService.getBuffer();
 		if (currentCopiedData!=null) {
 		    paste.setText("Paste '"+currentCopiedData.getName()+"' (from file "+currentCopiedData.getFileName()+") into this data.");
 		    paste.setEnabled(true);
 		} else {
 			paste.setEnabled(false);
 		}	
 		
 		if (ob!=null && ob.isTransientData()) {
 		    delete.setText("Delete '"+ob.getName());
 		    delete.setEnabled(true);
 		} else {
 			delete.setText("Delete");
 			delete.setEnabled(false);
 		}
 		
 		if (ob!=null) {
 			createFilter.setText("Filter plot of '"+ob.getName()+"' using python");
 			createFilter.setEnabled(true);
 		} else {
 			createFilter.setEnabled(false);
 		}
 		
 		if (ob!=null && ob.getFilterPath()!=null) {
 			deleteFilter.setText("Clear filter of '"+ob.getName()+"'");
 			deleteFilter.setEnabled(true);
 		} else {
 			deleteFilter.setEnabled(false);
 		}
 
 		if (bars!=null) {
 			bars.getToolBarManager().update(true);
 			bars.updateActionBars();
 		}
 	}
 
 	/**
 	 * Checks whether the object exists and if it does, asks the user for a new
 	 * name. If a new name is not provided or the user cancels, will return null.
 	 * @param original
 	 * @return cloned object with unique name or null if no paste-able object can be determined.
 	 */
 	protected ITransferableDataObject getCheckedObject(final ITransferableDataObject original) {
 		
 		if (nameExists(original.getName())) {
 			if (original.isExpression()) {
 				MessageDialog.openWarning(Display.getDefault().getActiveShell(), 
 						"Cannot paste expression", 
 						"Cannot paste expression '"+original.getName()+"' as it already exists");
 				return null;
 				
 			} else {
 			
 				final IInputValidator validator = new IInputValidator() {
 					@Override
 					public String isValid(String newText) {
 						if (nameExists(newText)) {
 							return "'"+newText+"' already exists in this data.";
 						} else {
 							return null;
 						}
 					}
 				};
 				
 				InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), 
 						                            "'"+original.getName()+"' already exists",
 													"Please provide a new name for '"+original.getName()+"'", original.getName()+"_1",
 													validator);
 				int rc = dialog.open();
 				if (rc == Window.OK) {
 					ITransferableDataObject clone = original.clone();
 					clone.setName(dialog.getValue());
 					return clone;
 			    } else {
 			    	return null;
 			    }
 			}
 		}
 
 		return original.clone();
 	}
 	
 	private boolean nameExists(String original) {
 		for (ITransferableDataObject existing : data) {
 			if (existing.getName().equals(original)) {
                 return true;
 			}
 		}
 		return false;
 	} 
 
 	public IAction getDataReductionAction() {
 		return dataReduction;
 	}
 	
 	protected DimsDataList getSliceData() {
 		final ISliceSystem system = (ISliceSystem)editor.getAdapter(ISliceSystem.class);
 		if (system!=null) return system.getDimsDataList();
 	
 		return null;
 	}
 	
 	protected ILazyDataset getSliceSet() {
 		final ISliceSystem system = (ISliceSystem)editor.getAdapter(ISliceSystem.class);
 		if (system!=null) return system.getData().getLazySet();
 
 		return null;
 	}
 
 	public IFile getIFile(boolean createNewFile) {
 		IFile file = null;
 		IEditorInput input = editor.getEditorInput();				         
 		try {
 			file = EclipseUtils.getIFile(input);
 		} catch (Throwable ne) {
 			file = null;
 		}
 		
 		if (createNewFile && file==null) {// Might be external file
 			final String name = input.getName();
 			final IProject data = ResourcesPlugin.getWorkspace().getRoot().getProject("data");
 			if (!data.exists()) {
 				try {
 					data.create(new NullProgressMonitor());
 				} catch (CoreException e) {
 					logger.error("Cannot create 'data' project!", e);
 				}
 			}
 			if (!data.isOpen()) {
 				try {
 					data.open(new NullProgressMonitor());
 				} catch (CoreException e) {
 					logger.error("Cannot open 'data' project!", e);
 				}
 			}
 			file = data.getFile(name);
 			if (file.exists()) {
 				file = EclipseUtils.getUniqueFile(file, FileUtils.getFileExtension(name));
 			}
 			try {
 				file.create(new FileInputStream(EclipseUtils.getFile(input)), IResource.FORCE, new NullProgressMonitor());
 				data.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
 			} catch (Exception e) {
 				logger.error("Cannot create file "+file.getName()+"!", e);
 			}
 		}
 		
 		return file;
 	}
 
 	protected List<String> getSelectionNames() {
 		final List<String> names = new ArrayList<String>(3);
 		for (ITransferableDataObject ob : getSelections()) {
 			if (!ob.isExpression())
 				names.add(ob.getPath());
 			else {
 				names.add(ob.getExpression().getExpressionName());
 			}
 		}
 		return names;
 	}
 
 	protected boolean isDataReductionToolActive() {
 		
 		if (H5Loader.isH5(getFileName()) || isSelectionReducible()) {
 			IToolPageSystem toolSystem = (IToolPageSystem)getPlottingSystem().getAdapter(IToolPageSystem.class);
 			IToolPage tool = toolSystem.getActiveTool();
 			return tool!=null && tool instanceof IDataReductionToolPage;
 		}
 		return false;
 	}
 
 	/**
 	 * if the table selection is reducible
 	 */
 	private boolean isSelectionReducible() {
 		final Object sel = ((StructuredSelection)dataViewer.getSelection()).getFirstElement();
 		final ITransferableDataObject ob  = (ITransferableDataObject)sel;
		if (ob==null) return false;
 		ILazyDataset lazy = ob.getLazyData(null);
 		if (lazy.getRank() >= 3)
 			return true;
 		return false;
 	}
 	
 	private ExpressionEditingSupport     expressionEditor;
 	private VariableNameEditingSupport   variableEditor;
 	
 	private void createColumns() throws Exception {
 		
 		ColumnViewerToolTipSupport.enableFor(dataViewer,ToolTip.NO_RECREATE);
 		
 		
 		final TableViewerColumn tick   = new TableViewerColumn(dataViewer, SWT.LEFT, 0);
 		tick.getColumn().setText(" ");
 		tick.getColumn().setWidth(30);
 		tick.setLabelProvider(new DataSetColumnLabelProvider(0, this));
 	
 		final TableViewerColumn name   = new TableViewerColumn(dataViewer, SWT.LEFT, 1);
 		name.getColumn().setText("Name");
 		name.getColumn().setWidth(Math.max(30,Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PLOT_DATA_NAME_WIDTH)));
 		name.getColumn().addListener(SWT.Resize, new Listener() {		
 			@Override
 			public void handleEvent(Event event) {
 				Activator.getDefault().getPreferenceStore().setValue(EditorConstants.PLOT_DATA_NAME_WIDTH, name.getColumn().getWidth());
 			}
 		});
 		name.setLabelProvider(new DelegatingStyledCellLabelProvider(new DataSetColumnLabelProvider(1, this)));
 		
 		expressionEditor = new ExpressionEditingSupport(dataViewer, this);
 		name.setEditingSupport(expressionEditor);
 		
 		final TableViewerColumn axis   = new TableViewerColumn(dataViewer, SWT.LEFT, 2);
 		axis.getColumn().setText(" ");
 		axis.getColumn().setWidth(32);
 		axis.setLabelProvider(new DataSetColumnLabelProvider(2, this));
 		axis.setEditingSupport(new AxisEditingSupport(dataViewer, this));
 
 		final TableViewerColumn size   = new TableViewerColumn(dataViewer, SWT.LEFT, 3);
 		size.getColumn().setText("Size");
 		size.getColumn().setWidth(150);
 		size.getColumn().setResizable(true);
 		size.setLabelProvider(new DataSetColumnLabelProvider(3, this));
 			
 		final TableViewerColumn dims   = new TableViewerColumn(dataViewer, SWT.LEFT, 4);
 		dims.getColumn().setText("Dimensions");
 		dims.getColumn().setWidth(150);
 		dims.getColumn().setResizable(true);
 		dims.setLabelProvider(new DataSetColumnLabelProvider(4, this));
 		
 		final TableViewerColumn shape   = new TableViewerColumn(dataViewer, SWT.LEFT, 5);
 		shape.getColumn().setText("Shape");
 		shape.getColumn().setWidth(150);
 		shape.getColumn().setResizable(true);
 		shape.setLabelProvider(new DataSetColumnLabelProvider(5, this));
 
 		final TableViewerColumn varName   = new TableViewerColumn(dataViewer, SWT.LEFT, 6);
 		varName.getColumn().setText("Variable");
 		varName.getColumn().setWidth(150);
 		varName.getColumn().setResizable(true);
 		varName.setLabelProvider(new DataSetColumnLabelProvider(6, this));
 		
 		variableEditor = new VariableNameEditingSupport(dataViewer, this);
 		varName.setEditingSupport(variableEditor);
 	}
 	
 
 	private void createDimensionalActions(List<Object> rightClickActions, boolean isToolbar) {
 				
 		this.dataComponentActions = new ArrayList<IAction>(11);
 		
 		final Action preferences = new Action("Configure column preferences...", Activator.getImageDescriptor("icons/application_view_columns.png")) {
 		    public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), EditorPreferencePage.ID, null, null);
 				if (pref != null) pref.open();
 		    }
 		
 		};
 		
 		if (staggerSupported) {
 			// Warning this is horrible:
 			final Action xyAction = new Action("XY Plot", SWT.TOGGLE) {
 				@Override
 				public void run() {
 					setPlotMode(PlotType.XY);
 				}
 			};
 			xyAction.setImageDescriptor(Activator.getImageDescriptor("/icons/chart_curve.png"));
 			xyAction.setToolTipText("XY Graph of Data, overlayed for multiple data.");
 			dataComponentActions.add(xyAction);
 			
 			final Action staggeredAction = new Action("XY staggered in Z",  SWT.TOGGLE) {
 				@Override
 				public void run() {
 					setPlotMode(PlotType.XY_STACKED);
 				}
 			};		
 			staggeredAction.setImageDescriptor(Activator.getImageDescriptor("/icons/chart_curve_staggered.png"));
 			staggeredAction.setToolTipText("XY Graph of Data, staggered in Z for multiple data.");
 			dataComponentActions.add(staggeredAction);
 	
 			final Action xyzAction = new Action("XYZ",  SWT.TOGGLE) {
 				@Override
 				public void run() {
 					setPlotMode(PlotType.XY_STACKED_3D);
 				}
 			};		
 			xyzAction.setImageDescriptor(Activator.getImageDescriptor("/icons/chart_curve_3D.png"));
 			xyzAction.setToolTipText("XYZ, X is the first chosen data and Z the last.");
 			dataComponentActions.add(xyzAction);
 	
 			rightClickActions.add(new Separator());
 			rightClickActions.add(xyAction);
 			rightClickActions.add(staggeredAction);
 			rightClickActions.add(xyzAction);
 			rightClickActions.add(new Separator());
 			
 			updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, getPlottingSystem().getPlotType());
 
 			
 			// Removed when part disposed.
 			addPlotModeListener(new PlotModeListener() {			
 				@Override
 				public void plotChangePerformed(PlotType plotMode) {
 					updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, plotMode);
 					updateSelection(true);
 				}
 			});
 			
 			final Display dis = PlatformUI.getWorkbench().getDisplay();
 			if (isToolbar) {
 				dis.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, getPlottingSystem().getPlotType());
 					}
 				});
 			}
 		}
 		
 		final Action setX = new Action("Set selected data as x-axis") {
 			public void run() {
 				
 				final TransferableDataObject sel = (TransferableDataObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
 				if (sel==null) return;
 				
 				setAsX(sel);
 			}
 		};
 		setX.setImageDescriptor(Activator.getImageDescriptor("/icons/to_x.png"));
 		setX.setToolTipText("Changes the plot to use selected data set as the x-axis.");
 		dataComponentActions.add(setX);
 		rightClickActions.add(setX);
 		rightClickActions.add(new Separator());
 
 		final Action addExpression = new Action("Add expression") {
 			public void run() {
 				final ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
 				try {
 					cs.getCommand("org.dawb.workbench.editors.addExpression").executeWithChecks(new ExecutionEvent());
 				} catch (Exception e) {
 					logger.error("Cannot run action", e);
 				} 
 				
 			}
 		};
 		addExpression.setImageDescriptor(Activator.getImageDescriptor("/icons/add_expression.png"));
 		addExpression.setToolTipText("Adds an expression which can be plotted. Must be function of other data sets.");
 		dataComponentActions.add(addExpression);
 		rightClickActions.add(addExpression);
 		
 		final Action deleteExpression = new Action("Delete expression") {
 			public void run() {
 				final ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
 				try {
 				    cs.getCommand("org.dawb.workbench.editors.deleteExpression").executeWithChecks(new ExecutionEvent());
 				} catch (Exception e) {
 					logger.error("Cannot run action", e);
 				} 
 			}
 		};
 		deleteExpression.setImageDescriptor(Activator.getImageDescriptor("/icons/delete_expression.png"));
 		deleteExpression.setToolTipText("Deletes an expression.");
 		dataComponentActions.add(deleteExpression);
 		rightClickActions.add(deleteExpression);
 
 		dataComponentActions.add(preferences);
 
 	}
 
 	public void clearExpressionCache(String... variableNames) {
 		for (ITransferableDataObject ob : data) {
 			if (ob.isExpression() && (variableNames==null || variableNames.length<1 || ob.getExpression().containsVariable(variableNames)))  {
 				ob.getExpression().clear();
 			}
 		}
 	}
 
 	protected void setAsX(TransferableDataObject sel) {
 		
 		int [] shape = sel.getShape(true);
 		if (shape.length!=1) return; 
 		sel.setChecked(true);
 		
 		if (selections.contains(sel)) selections.remove(sel);
 	    selections.add(0, sel);
 	    getAbstractPlottingSystem().setXFirst(true);
 		updateSelection(true);
 		dataViewer.refresh();
 	}
 
 	public List<IAction> getDimensionalActions() {
 		
 		return dataComponentActions;
 		
 	}
 	
 	protected void updatePlotDimenionsSelected(IAction xyAction, IAction staggeredAction, IAction xyzAction, PlotType plotMode) {
 
 		if (staggerSupported) return;
 		xyAction.setChecked(PlotType.XY.equals(plotMode));
 		staggeredAction.setChecked(PlotType.XY_STACKED.equals(plotMode));
 		xyzAction.setChecked(PlotType.XY_STACKED_3D.equals(plotMode));
 	}
 	
 
 	private List<ITransferableDataObject> selections = new ArrayList<ITransferableDataObject>(7);
 		
 	/**
 	 * @return Returns the selections.
 	 */
 	public List<ITransferableDataObject> getSelections() {
 		return selections;
 	}
 	
 	public void mouseDoubleClick(MouseEvent e){ 
 		
 		if (e.button==1) {
 			final Point           pnt     = new Point(e.x, e.y);
 			final TableItem       item    = this.dataViewer.getTable().getItem(pnt);
 			if (item==null) return;
 			
             Rectangle rect1 = item.getBounds(1);
             Rectangle rect6 = item.getBounds(6);
             if (!rect1.contains(pnt) && !rect6.contains(pnt)) return;
 			try {
 				ITransferableDataObject data = (ITransferableDataObject)item.getData();
 				expressionEditor.setExpressionActive(true);
 				variableEditor.setVariableNameActive(true);
 				
 				dataViewer.editElement(data, rect1.contains(pnt)?1:6);
 			} finally {
 				expressionEditor.setExpressionActive(false);
 				variableEditor.setVariableNameActive(false);
 			}
           
 		}
 	}
 	
 
 	/**
 	 * Sent when a mouse button is pressed.
 	 *
 	 * @param e an event containing information about the mouse button press
 	 */
 	public void mouseDown(MouseEvent e) {
 		
 		expressionEditor.setExpressionActive(false);
 		variableEditor.setVariableNameActive(false);
 		if (e.button==1) {
 			
 			final Point           pnt     = new Point(e.x, e.y);
 			final TableItem       item    = this.dataViewer.getTable().getItem(pnt);
 			if (item==null) return;
 			
             Rectangle rect = item.getBounds(0); // First column (tick and name)
             if (!rect.contains(pnt)) return;
 		
             dataViewer.cancelEditing();
 			final TransferableDataObject clicked = (TransferableDataObject)item.getData();
 			
 			if (e.stateMask==131072) { // Shift is pressed
 				try {
 				    final ITransferableDataObject from = selections.get(selections.size()-1);
 				    // TODO Table may be filtered - then we cannot loop over the data
 				    // only the visible data.
 				    final int fromIndex = data.indexOf(from);
 				    final int toIndex   = data.indexOf(clicked);
 				    final int inc       = (fromIndex<toIndex) ? 1 : -1;
 				    
 				    for (int i = fromIndex+inc; inc==1?i<toIndex:i>toIndex; i+=inc) {
 				    	selectionChanged(data.get(i), false);
 					}
 			    	selectionChanged(clicked, true);
 			    	
 				} catch (Throwable t) {
 					selectionChanged(clicked, true);
 				}
 			} else{
  			    selectionChanged(clicked, true);
 			}
 	        dataViewer.cancelEditing();
 		}
 	}
 
 
 	
 	public void keyPressed(KeyEvent e) {
 		if (e.keyCode==13) {
 			selectionChanged((TransferableDataObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement(), true);
 		}
 	}
 
 	/**
 	 * Sent when a key is released on the system keyboard.
 	 *
 	 * @param e an event containing information about the key release
 	 */
 	public void keyReleased(KeyEvent e) {
 	}
 	/**
 	 * Sent when a mouse button is released.
 	 *
 	 * @param e an event containing information about the mouse button release
 	 */
 	public void mouseUp(MouseEvent e) {
 		
 	}
 	
 	protected void selectionChanged(final ITransferableDataObject check, boolean fireListeners) {
 		
 		if (selections==null) selections = new ArrayList<ITransferableDataObject>(7);
 
 		if (check!=null) {
 			
 			if (isInvalidExpression(check)) return;
 			
 			check.setChecked(!check.isChecked());
 
 			if (!check.isChecked()) {
 				selections.remove(check);
 			} else {
 				// We only allow selection of one set not 1D
 				final int[] shape = check.getShape(true);
 				final int    dims = shape.length;
 				if (dims!=1) { // Nothing else gets selected
 					setAllChecked(false);
 					check.setChecked(true);
 					this.selections.clear();
 				}
 				if (!selections.contains(check)) {
 					selections.add(check);
 				}
 
 			}
 
 			// 1D takes precidence
 			boolean is1D = false;
 			// We check selections to ensure that only n*1D or 1*2D+ are selected
 			for (ITransferableDataObject set : selections) {
 				final int[] shape = set.getShape(true);
 				if (shape.length==1) is1D = true;
 			}
 
 			if (is1D) for (Iterator<ITransferableDataObject> it = selections.iterator(); it.hasNext();) {
 				ITransferableDataObject set = it.next();
 				final int[] shape = set.getShape(true);
 
 				if (shape.length!=1) {
 					set.setChecked(false);
 					it.remove();
 				}
 			}
 
 		} else {
 			selections.clear();
 		}
 
 		updateSelection(fireListeners); // Results in job being done for the plotting.
 		// Trace listener is used to refresh the table, added in construnctor
 	}
 
 	private boolean isInvalidExpression(ITransferableDataObject check) {
 		
 		if (!check.isExpression()) return false;
 
 		boolean isExprOk = check.getExpression().isValid(new IMonitor.Stub());
 		if (!isExprOk) {
 			List<String> names = check.getExpression().getInvalidVariables(new IMonitor.Stub());
 			if (names!=null && names.size()>0) {
 				MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Expression '"+check.getName()+"' is not valid.",
 						                      "Expression '"+check.getName()+"' is not valid.\n\n"+
 						                       names+" cannot be resolved.");
 			} else {
 				MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Expression '"+check.getName()+"' is not valid.",
 	                      "Expression '"+check.getName()+"' is not valid.");
 			}
 			return true;
 		}
 		
 		return false;
 
 	}
 
 	private synchronized void updateSelection(boolean fireListeners) {
 
 		if (selections==null) return;
 
 		// Record selections
 		if (Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SAVE_SEL_DATA)) try {
 			StringBuilder buf = new StringBuilder();
 			for (ITransferableDataObject ob : selections) {
 				buf.append(ob.getName());
 				buf.append(",");
 			}
 			
 			Activator.getDefault().getPreferenceStore().setValue(DATA_SEL, buf.toString());
 		} catch (Throwable ne) {
 			logger.error("Cannot save last selections!", ne);
 		}
 		
 		if (fireListeners) fireSelectionListeners(selections);
 	}
 
     @Override
 	public void setAll1DSelected(final boolean overide) {
 		
  		if (!overide && getSelections()!=null && getSelections().size()>0 ) {
         	return; // Something selected, leave it alone.
         }
 		
 		selections.clear();
 		for (ITransferableDataObject sel : data) {
 			final int[] shape = sel.getShape(true);
 			if (shape.length==1) {
 				selections.add(sel);
 				sel.setChecked(true);
 			}
 		}
 		updateSelection(true);
 		dataViewer.refresh();
 	}
 
 
 	
 	private List<ISelectionChangedListener> listeners;
 	
 	/**
 	 * Call to be notified of data set collections being made.
 	 * The selections returned are a StructuredSelection with a list
 	 * of objects some are Strings for the data set name and
 	 * others are ExpressionObject if the user created expressions.
 	 * 
 	 * NOTE: The listener is NOT called on the GUI thread.
 	 * 
 	 * @param l
 	 */
 	public void addSelectionListener(final ISelectionChangedListener l){
 		if (listeners==null) listeners = new ArrayList<ISelectionChangedListener>(7);
 		listeners.add(l);
 	}
 	
 	public void removeSelectionListener(final ISelectionChangedListener l){
 		if (listeners==null) return;
 		listeners.remove(l);
 	}
 	
 	protected void fireSelectionListeners(List<ITransferableDataObject> selections) {
 		if (listeners==null) return;
 		final SelectionChangedEvent event = new SelectionChangedEvent(this.dataViewer, new StructuredSelection(selections));
 		for (ISelectionChangedListener l : listeners) l.selectionChanged(event);
 	}
 
 	public String getRootName() {
 		return rootName;
 	}
 
 	/**
 	 * @param pm The plotMode to set.
 	 */
 	public void setPlotMode(PlotType pm) {
 		if (plotModeListeners!=null) {
 			for (PlotModeListener l : plotModeListeners) {
 				l.plotChangePerformed(pm);
 			}
 		}
 	}
 	
 	private List<PlotModeListener> plotModeListeners;
 
 
 	protected void addPlotModeListener(PlotModeListener l) {
 		if (plotModeListeners==null) plotModeListeners = new ArrayList<PlotModeListener>(7);
 		plotModeListeners.add(l);
 	}
 	
 	protected void removePlotModeListener(PlotModeListener l) {
 		if (plotModeListeners==null) return;
 		plotModeListeners.remove(l);
 	}
 	
 	protected interface PlotModeListener extends EventListener {
 		void plotChangePerformed(PlotType plotMode);
 	}
 
 	private List<String> getStringSelections(List<ITransferableDataObject> selections) {
 		
 		final List<String> ret = new ArrayList<String>(selections.size());
 		for (ITransferableDataObject sel : selections) {
 			if (!sel.isExpression()) ret.add(sel.getName());
 		}
 		return ret;
 	}
 
 	
 	@Override
 	public IDataset getVariableValue(String variableName, final IMonitor monitor) {
 
 		final ITransferableDataObject ob = getCheckableObjectByVariable(variableName);
 		if (ob==null) return null;
 		if (!ob.isExpression()) {
 			return ob.getData(monitor);
 		} else {
 			try {
 				return ob.getExpression().getDataSet(null, new IMonitor.Stub());
 			} catch (Exception e) {
 				return null;
 			}
 		}		
 	}
 	
 	@Override
 	public ILazyDataset getLazyValue(String variableName, final IMonitor monitor) {
 		final ITransferableDataObject ob = getCheckableObjectByVariable(variableName);
 		return ob.getLazyData(monitor);
 	}
 
 	/**
 	 * Tries to get the lazy dataset for the name
 	 * @param dataName
 	 * @param monitor
 	 * @return
 	 */
 	public ILazyDataset getDataValue(String dataName, final IMonitor monitor) {
 		for (ITransferableDataObject ob : data) {
 			if (ob.getName().equals(dataName)) {
 				return ob.getLazyData(monitor);
 			}
 		}
         return null;
 	}
 	@Override
 	public boolean isDataName(String dataName, IMonitor monitor) {
 		for (ITransferableDataObject ob : data) {
 			if (ob.getName().equals(dataName)) {
 				return true;
 			}
 		}
         return false;
 	}	
 
 
 	@Override
 	public List<String> getVariableNames() {
 		List<String> ret = new ArrayList<String>(data.size());
 		for (ITransferableDataObject ob : data) {
 			ret.add(ob.getVariable());
 		}
 		return ret;
 	}
 	
 	@Override
 	public List<String> getDataNames() {
 		List<String> ret = new ArrayList<String>(data.size());
 		for (ITransferableDataObject ob : data) {
 			ret.add(ob.getName());
 		}
 		return ret;
 	}
 
 
 	
 	@Override
 	public boolean isVariableName(String variableName, IMonitor monitor) {
 		final ITransferableDataObject ob = getCheckableObjectByVariable(variableName);
 		return ob!=null;
 	}
 	
 	private ITransferableDataObject getCheckableObjectByVariable(String variableName) {
 		for (ITransferableDataObject ob : data) {
 			if (ob.getVariable().equals(variableName)) return ob;
 		}
 		return null;
 	}
 	private ITransferableDataObject getCheckableObjectByName(String name) {
 		for (ITransferableDataObject ob : data) {
 			if (ob.getName().equals(name)) return ob;
 		}
 		return null;
 	}
 
 	public boolean isDataSetName(String name, IMonitor monitor) {
 		final List<String> allNames = getStringSelections(data);
 		return allNames.contains(name);
 	}
 	
 	Color get1DPlotColor(ITransferableDataObject element) {
 		final String axis = element.getAxis(selections, getPlottingSystem().is2D(), getAbstractPlottingSystem().isXFirst());
 		if ("X".equals(axis)) return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
 		final String name = element.toString();
 		if (getPlottingSystem()!=null) {
 			final Color col = getAbstractPlottingSystem().get1DPlotColor(name);
 			return col;
 		}
 		return null;
 	}
 
 	
 	public void dispose() {
 		
 		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propListener);
 		datasetSelection = null;
 		
 		this.metaData   = null;
 		this.dataHolder = null;
 		this.filePath   = null;
 		
 		if (listeners!=null) listeners.clear();
 		if (data != null){
 			for (ITransferableDataObject ob : data) ob.dispose();
 			this.data.clear();
 		}
 		if (plotModeListeners!=null) plotModeListeners.clear();
 		if (getPlottingSystem()!=null&&traceListener!=null) {
 			getPlottingSystem().removeTraceListener(this.traceListener);
 		}
 		if (getPlottingSystem()!=null&&dataViewRefreshListener!=null) {
 			getPlottingSystem().removeTraceListener(this.dataViewRefreshListener);
 		}
 		
 		if (getPlottingSystem()!=null&&toolListener!=null) {
 			getAbstractPlottingSystem().removeToolChangeListener(this.toolListener);
 		}
 		if (dataViewer!=null && !dataViewer.getControl().isDisposed()) {
 			dataViewer.getTable().removeMouseListener(this);
 			dataViewer.getTable().removeKeyListener(this);
 		}
 		if (filterProvider!=null) {
 			filterProvider.dispose();
 			filterProvider = null;
 		}
 	}
 
 	public void addExpression() {
 		
 		if (!Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_VARNAME)) {
 		    Activator.getDefault().getPreferenceStore().setValue(EditorConstants.SHOW_VARNAME, true);
 		} 
 		final ITransferableDataObject newItem = transferableService.createExpression(dataHolder, metaData, expressionService.createExpressionObject(this, null, null));
 		data.add(newItem);
 		dataViewer.refresh();
 		try {
 			expressionEditor.setExpressionActive(true);
 			dataViewer.editElement(newItem, 1);	
 		} finally {
 			expressionEditor.setExpressionActive(false);
 		}
 	}
 
 	protected void addExpression(IExpressionObject expressionObject) {
 		data.add(transferableService.createExpression(dataHolder, metaData, expressionObject));
 		dataViewer.refresh();
 	}
 
 	public void deleteExpression() {
 		
 		final Object sel = ((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
 		if (sel==null || !(sel instanceof TransferableDataObject)) return;
 		
 		ITransferableDataObject ob = (ITransferableDataObject)sel;
 		if (!ob.isExpression()) {
 			if (ob.isTransientData()) { // We delete it
 				boolean ok = data.remove(ob);
 				if (ok) ob.dispose();
 				dataViewer.refresh();
 				return;
 			} else { // We tell them that we cannot.
 				String name = ob.getName();
 				MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Cannot delete", "The data '"+name+"' is not an expression.");
 				return;
 			}
 		}
 		
 		// We have to process this in a job incase expressions now have to be reevaluated
 	    if (selections!=null) selections.remove(sel);
 		data.remove(sel);
 		clearExpressionCache(ob.getVariable());
 		dataViewer.refresh();
 		saveExpressions();
 		fireSelectionListeners(selections);
 	}
 	
 	
 	public static List<Pattern> getIgnored() {
 		
 		if (Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.IGNORE_DATASET_FILTERS)) return null;
 		
 		final List<Pattern> patterns    = new ArrayList<Pattern>(5);
 
 		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.data.set.filter");
 		
 		for (IConfigurationElement e : config) {
 			final String pattern     = e.getAttribute("regularExpression");
 			patterns.add(Pattern.compile(pattern));
 		}
 		
 		return patterns;
 	}
 	
 	private static final String DATA_SEL = "org.dawb.workbench.ui.editors.plotdata.selected";
 	/**
 	 * Used when the view is being controlled from a Dialog.
 	 * 
 	 * We actually clone the data holder because the user can paste in datasets
 	 * from outside this file in order to do expressions on them.
 	 * 
 	 * @param meta
 	 * @throws Exception 
 	 */
 	public void setData(final IDataHolder dh, IMetaData meta) {
 		
 		this.data.clear();
 		this.dataHolder=dh.clone();
 		this.metaData = meta;
 		this.filePath = dh.getFilePath();
 		
 		if (metaData==null) metaData = dataHolder.getMetadata();
 		
 		final Collection<String> names = SliceUtils.getSlicableNames(dataHolder);
 		for (String name : names) this.data.add(transferableService.createData(dataHolder, metaData, name));
 		
 		// Search names to see if they all have a common root, we do not show this.
 		this.rootName = DatasetTitleUtils.getRootName(names);
 		
 		if (dataFilter!=null) dataFilter.setMetaData(meta);
 
 		try {
 		    readExpressions();
 		} catch (Exception ne ) {
 			logger.error("Cannot read expressions for file.", ne);
 		}
 		
 		// Some of the meta data
 		if (Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SAVE_SEL_DATA)) {
 			try {
 
 				final String prop = Activator.getDefault().getPreferenceStore().getString(DATA_SEL);
 				if (prop!=null) {
 					final Collection<String> saveSelections = Arrays.asList(prop.split(","));
 					if (data!=null && !data.isEmpty()) {
 						boolean foundData = false;
 						for (ITransferableDataObject checker : data) {
 							if (saveSelections.contains(checker.getName())) {
 								if (!foundData) selections.clear();
 								checker.setChecked(true);
 								this.selections.add(checker);
 								foundData = true;
 							}
 						}
 
 						if (foundData) {
 							fireSelectionListeners(selections);
 						}
 					}
 				}
 			} catch (Throwable ne) {
 				logger.error("Cannot save data previously selected!", ne);
 			}
 		}
 		
 		// If we are an image or a single data set, plot it.
 		final List<String> namesNoStack = new ArrayList<String>(names);
 		namesNoStack.remove("Image Stack");
 		if (namesNoStack!=null && namesNoStack.size()==1) {
 			ITransferableDataObject check = data.get(0);
 			check.setChecked(false); // selectionChanged flips this
 			selectionChanged(check, true);
 			dataViewer.refresh();
 		}
 	}
 
 
 	public void refresh() {
 		this.dataViewer.refresh();
 	}
 
 
 	public String getFileName() {
 		if (filePath!=null) return (new File(filePath)).getName();
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	private IDataset datasetSelection = null;
 	/**
 	 * Thread safe
 	 * @param name
 	 */
 	public IDataset setDatasetSelected(final String name, final boolean clearOthers) {
 		
 		datasetSelection = null;
 		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				if (clearOthers) {
 					PlotDataComponent.this.setAllChecked(false);
 					PlotDataComponent.this.selectionChanged(null, true);
 				}
 				
 				final ITransferableDataObject check = PlotDataComponent.this.getObjectByName(name);
 				check.setChecked(false);
 				PlotDataComponent.this.selectionChanged(check, true);
 				datasetSelection = check.getData((IMonitor)null);
 			}
 		});
 		
 		return datasetSelection;
 	}
 
 	protected void setAllChecked(boolean isChecked) {
 		for (ITransferableDataObject check : data) {
 			check.setChecked(isChecked);
 		}
 	}
 
 	/**
 	 * Does a loop, may be bad...
 	 * @param name
 	 * @return
 	 */
 	protected ITransferableDataObject getObjectByName(final String name) {
 		for (ITransferableDataObject check : data) {
 			if (name.equals(check.getName())) return check;
 		}
 		return null;
 	}
 
 
 	public IPlottingSystem getPlottingSystem() {
 		if (editor==null) return null;
 		return (IPlottingSystem)editor.getAdapter(IPlottingSystem.class);
 	}
 
 	private AbstractPlottingSystem getAbstractPlottingSystem() {
 		return (AbstractPlottingSystem)getPlottingSystem();
 	}
 
 	public boolean isStaggerSupported() {
 		return staggerSupported;
 	}
 
 
 	public void setStaggerSupported(boolean staggerSupported) {
 		this.staggerSupported = staggerSupported;
 	}
 
 
 	public List<ITransferableDataObject> getData() {
 		return data;
 	}
 
 
 	public ISelectionProvider getViewer() {
 		return dataViewer;
 	}
 	
 	
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		if (adapter==IPlottingSystem.class) {
 			return getPlottingSystem();
 		}
 		return null;
 	}
 
 	IMetaData getMetaData() {
 		return metaData;
 	}
 
 }
