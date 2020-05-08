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
 import org.dawb.common.ui.DawbUtils;
 import org.dawb.common.ui.editors.ICheckableObject;
 import org.dawb.common.ui.editors.IDatasetEditor;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
 import org.dawb.common.ui.slicing.DimsDataList;
 import org.dawb.common.ui.slicing.ISlicablePlottingPart;
 import org.dawb.common.ui.util.DialogUtils;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.util.io.FileUtils;
 import org.dawb.common.util.io.PropUtils;
 import org.dawb.common.util.io.SortingUtils;
 import org.dawb.common.util.list.SortNatural;
 import org.dawb.gda.extensions.util.DatasetTitleUtils;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.editors.preference.EditorConstants;
 import org.dawb.workbench.ui.editors.preference.EditorPreferencePage;
 import org.dawb.workbench.ui.expressions.ExpressionFunctionProposalProvider;
 import org.dawb.workbench.ui.expressions.TextCellEditorWithContentProposal;
 import org.dawnsci.io.h5.H5Loader;
 import org.dawnsci.plotting.AbstractPlottingSystem;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.IPlottingSystemSelection;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.tool.IToolChangeListener;
 import org.dawnsci.plotting.api.tool.IToolPage;
 import org.dawnsci.plotting.api.tool.ToolChangeEvent;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.tools.reduction.DataReductionWizard;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
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
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
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
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.progress.IProgressService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IErrorDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
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
 	protected List<CheckableObject> data;
 	protected String                filePath;
 	protected String                fileName;
 	private   String                rootName;
 	private   boolean               staggerSupported = false;
 
 	protected IMetaData             metaData;
 	protected final IDatasetEditor  providerDeligate;
 	private IPropertyChangeListener propListener;
 	private ArrayList<IAction>      dataComponentActions;
 	private Composite               container;
 	private DataFilter              dataFilter;
 
 	private IAction                  dataReduction;
 	private ITraceListener           traceListener;
 	private ITraceListener           dataViewRefreshListener;
 	private IToolChangeListener      toolListener;
 	private IExpressionObjectService service;
 
 	
 	public PlotDataComponent(final IDatasetEditor providerDeligate) {
 				
 		this.data = new ArrayList<CheckableObject>(7);
 		this.providerDeligate   = providerDeligate;
 		
 		this.service  = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
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
 									setFile(filePath, monitor);
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
		getPlottingSystem().addTraceListener(dataViewRefreshListener);
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
 	public void createPartControl(final Composite parent) {
 		
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
 				
 		createRightClickMenu();
 		
 		setColumnVisible(2, 36,  Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_XY_COLUMN));
 		setColumnVisible(3, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_DATA_SIZE));
 		setColumnVisible(4, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_DIMS));
 		setColumnVisible(5, 80, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_SHAPE));
 		setColumnVisible(6, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_VARNAME));
 	
 		try {
 
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesUpdated(TraceEvent evt) {
 					updateSelection(true);
 					dataViewer.refresh();
 				}
 			};
 			getPlottingSystem().addTraceListener(traceListener);
 			
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
 			
 			this.dataFilter = new DataFilter();
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
 					if (item!=null && item.getData() instanceof CheckableObject) {
 						
 						Rectangle     nameArea = item.getBounds(1); // Name column
 						CheckableObject cn = (CheckableObject)item.getData();
 						
 						if (cn.isChecked() && !cn.isExpression() && nameArea.contains(event.x, event.y)) {
 							int origAlpha = gc.getAlpha();
 							gc.setAlpha(255);
 							final Color plotColor = get1DPlotColor(cn);
 							if (plotColor!=null) {
 								gc.setForeground(plotColor);									
 								gc.drawText(cn.getName(), item.getBounds().x+16, item.getBounds().y+1);
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
 			if (filePath==null) {
 				if (this.providerDeligate  instanceof IEditorPart) {
 					return FileUtils.getFileExtension(((IEditorPart)providerDeligate).getEditorInput().getName());
 				}
 			}
 
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
 					if (CheckableObject.isMementoKey(mementoKey)) {
 						final String possibleName = CheckableObject.getName(memento);
 						CheckableObject o = getCheckableObjectByName(possibleName);
 						if (o!=null) {
 							o.setVariable(CheckableObject.getVariable(memento));
 						} else {
 							o = new CheckableObject();
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
 	
 	private void saveExpressions() {
 		try {
 			final Properties props = new Properties();
 			for (ICheckableObject check : data) {
 				props.put(check.getMementoKey(), check.getMemento());
 			}
 			// Save properties to workspace.
 			final String cachePath = DawbUtils.getDawbHome()+getFileName()+".properties";
 			PropUtils.storeProperties(props, cachePath);
 			
 		} catch (Exception e) {
 			logger.error("Cannot save expression", e);
 		}
 	}
 	
 	private void createRightClickMenu() {	
 		
 		
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
 						    (IDataReductionToolPage)getAbstractPlottingSystem().getActiveTool());
 				wiz.setSlice(getSliceSet(), getSliceData());
 				
 				// TODO Should be non modal, it takes a while.
 				WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
 				wd.setTitle(wiz.getWindowTitle());
 				wd.create();
 				wd.getShell().setSize(650, 800);
 				DialogUtils.centerDialog(Display.getCurrent().getActiveShell(), wd.getShell());
 				wd.open();
 			}
 		};
 
 		
 		menuManager.addMenuListener(new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				
 				if (staggerSupported) {
 			        updatePlotDimenionsSelected((IAction)rightClickActions.get(1), 
 			        		                    (IAction)rightClickActions.get(2), 
 			        		                    (IAction)rightClickActions.get(3), 
 			        		                    getPlotMode());
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
 						for (ICheckableObject co : data) {
 							co.setChecked(false);
 						}
 						selections.clear();
 						dataViewer.refresh();
 						fireSelectionListeners(Collections.<ICheckableObject> emptyList());
 					}
 				});
 				
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
 				final Object sel           = ((StructuredSelection)dataViewer.getSelection()).getFirstElement();
 				final ICheckableObject ob  = (ICheckableObject)sel;
 				final ILazyDataset currentSelecedData  = getLazyValue(ob.getVariable(), null);
 
 				if (currentSelecedData!=null) {
 					if (selections!=null && selections.size() > 0) {
 						menuManager.add(new Action("Set '"+ob.getName()+"' as error on other plotted data...") {
 							@Override
 							public void run() {
 								final PlotDataChooseDialog dialog = new PlotDataChooseDialog(Display.getDefault().getActiveShell());
 								dialog.init(selections, ob);
 								final ICheckableObject plotD = dialog.choose();
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
 	
 	public IAction getDataReductionAction() {
 		return dataReduction;
 	}
 	
 	protected DimsDataList getSliceData() {
 		if (providerDeligate instanceof ISlicablePlottingPart) {
 			return ((ISlicablePlottingPart)providerDeligate).getSliceComponent().getDimsDataList();
 		}
 		return null;
 	}
 	
 	protected ILazyDataset getSliceSet() {
 		if (providerDeligate instanceof ISlicablePlottingPart) {
 			return ((ISlicablePlottingPart)providerDeligate).getSliceComponent().getLazyDataset();
 		}
 		return null;
 	}
 
 	public IFile getIFile(boolean createNewFile) {
 		IFile file = null;
 		IEditorInput input = (providerDeligate instanceof IEditorPart) 
 				           ? (IEditorInput)((IEditorPart)providerDeligate).getEditorInput()
 				           : null;
 				         
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
 		for (ICheckableObject ob : getSelections()) {
 			if (!ob.isExpression()) names.add(ob.getPath());
 		}
 		return names;
 	}
 
 	protected boolean isDataReductionToolActive() {
 		
 		if (H5Loader.isH5(getFileName())) {
 			IToolPage tool = getAbstractPlottingSystem().getActiveTool();
 			return tool!=null && tool instanceof IDataReductionToolPage;
 		}
 		return false;
 	}
 	
 	private void createColumns() {
 		
 		ColumnViewerToolTipSupport.enableFor(dataViewer,ToolTip.NO_RECREATE);
 		
 		
 		final TableViewerColumn tick   = new TableViewerColumn(dataViewer, SWT.LEFT, 0);
 		tick.getColumn().setText(" ");
 		tick.getColumn().setWidth(30);
 		tick.setLabelProvider(new DataSetColumnLabelProvider(0));
 	
 		final TableViewerColumn name   = new TableViewerColumn(dataViewer, SWT.LEFT, 1);
 		name.getColumn().setText("Name");
 		name.getColumn().setWidth(Math.max(30,Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PLOT_DATA_NAME_WIDTH)));
 		name.getColumn().addListener(SWT.Resize, new Listener() {		
 			@Override
 			public void handleEvent(Event event) {
 				Activator.getDefault().getPreferenceStore().setValue(EditorConstants.PLOT_DATA_NAME_WIDTH, name.getColumn().getWidth());
 			}
 		});
 		name.setLabelProvider(new DataSetColumnLabelProvider(1));
 		name.setEditingSupport(new ExpressionEditingSupport(dataViewer));
 		
 		final TableViewerColumn axis   = new TableViewerColumn(dataViewer, SWT.LEFT, 2);
 		axis.getColumn().setText(" ");
 		axis.getColumn().setWidth(32);
 		axis.setLabelProvider(new DataSetColumnLabelProvider(2));
 		axis.setEditingSupport(new AxisEditingSupport(dataViewer));
 
 		final TableViewerColumn size   = new TableViewerColumn(dataViewer, SWT.LEFT, 3);
 		size.getColumn().setText("Size");
 		size.getColumn().setWidth(150);
 		size.getColumn().setResizable(true);
 		size.setLabelProvider(new DataSetColumnLabelProvider(3));
 			
 		final TableViewerColumn dims   = new TableViewerColumn(dataViewer, SWT.LEFT, 4);
 		dims.getColumn().setText("Dimensions");
 		dims.getColumn().setWidth(150);
 		dims.getColumn().setResizable(true);
 		dims.setLabelProvider(new DataSetColumnLabelProvider(4));
 		
 		final TableViewerColumn shape   = new TableViewerColumn(dataViewer, SWT.LEFT, 5);
 		shape.getColumn().setText("Shape");
 		shape.getColumn().setWidth(80);
 		shape.getColumn().setResizable(true);
 		shape.setLabelProvider(new DataSetColumnLabelProvider(5));
 
 		final TableViewerColumn varName   = new TableViewerColumn(dataViewer, SWT.LEFT, 6);
 		varName.getColumn().setText("Variable");
 		varName.getColumn().setWidth(150);
 		varName.getColumn().setResizable(true);
 		varName.setLabelProvider(new DataSetColumnLabelProvider(6));
 		varName.setEditingSupport(new VariableNameEditingSupport(dataViewer));
 	}
 	
 	private boolean isExpressionActive   = false;
 	
 	private class ExpressionEditingSupport extends EditingSupport {
 
 		private TextCellEditor cellEditor;
 		
 		public ExpressionEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 			
 			IExpressionObject exObj = service.createExpressionObject(null,null,"");
 			
 			if (exObj != null) {
 				IContentProposalProvider contentProposalProvider = new ExpressionFunctionProposalProvider(exObj.getFunctions());
 				cellEditor = new TextCellEditorWithContentProposal((Composite)getViewer().getControl(), contentProposalProvider, null, new char[]{':'});
 			} else {
 				cellEditor = new TextCellEditor((Composite)getViewer().getControl());
 				logger.error("Expression Object service returned null");
 			}
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			return cellEditor;
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			if (!isExpressionActive) return false;
 			return (element instanceof CheckableObject) && ((ICheckableObject)element).isExpression();
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			ICheckableObject check = (ICheckableObject)element;
 			String text = check.getExpression().getExpressionString();
 			if (text==null) return "";
 			return text;
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			final CheckableObject check = (CheckableObject)element;
 			try {
 				String         expression   = (String)value;
 				final IExpressionObject ob   = check.getExpression();
 				if (expression!=null) expression = expression.trim();
 				if (value==null || "".equals(expression))  return;
 				if (value.equals(ob.getExpressionString()))      return;
 				
 				ob.setExpressionString(expression);
 				check.setChecked(false); // selectionChanged(...) puts it to true
 				selectionChanged(check, true);
 				saveExpressions();
 
 			} catch (Exception e) {
 				logger.error("Cannot set expression "+check.getName(), e);
 
 			} 
 			getViewer().refresh();
 		}
 
 	}
 
 	
 	private boolean isVariableNameActive = false;
 	
 	private class VariableNameEditingSupport extends EditingSupport {
 
 		public VariableNameEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			return new TextCellEditor((Composite)getViewer().getControl());
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			return isVariableNameActive;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return ((ICheckableObject)element).getVariable();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			ICheckableObject data  = (ICheckableObject)element;
 			try {
         		if (data.getVariable()!=null && data.getVariable().equals(value)) return;
         		if (data.getVariable()!=null && value!=null && data.getVariable().equals(((String)value).trim())) return;
                 String variableName = service.validate(PlotDataComponent.this, (String)value);
  
 				clearExpressionCache();
 				data.setVariable(variableName);
 				saveExpressions();
 				
 			} catch (Exception e) {
 				final String message = "The name '"+value+"' is not valid.";
 				final Status status  = new Status(Status.WARNING, "org.dawb.workbench.ui", message, e);
 				ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot rename data", message, status);
 			    return;
 			}
 			getViewer().refresh();
 		}
 
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
 			
 			updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, plotMode);
 
 			
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
 						updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, getPlotMode());
 					}
 				});
 			}
 		}
 		
 		final Action setX = new Action("Set selected data as x-axis") {
 			public void run() {
 				
 				final CheckableObject sel = (CheckableObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
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
 
 	private void clearExpressionCache() {
 		for (ICheckableObject ob : data) {
 			if (ob.isExpression())  ob.getExpression().clear();
 		}
 	}
 
 	protected void setAsX(CheckableObject sel) {
 		if (getActiveDimensions(sel, true)!=1) return; 
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
 	
 	/**
 	 * Call to load a data file and display it.
 	 * @param path
 	 */
 	public void setFile(final String path,final IProgressMonitor monitor) throws Exception {
 		
 		monitor.beginTask("Opening file " + path, 10);
 		monitor.worked(1);
 		if (monitor.isCanceled()) return;
 
 		final IMetaData          meta = LoaderFactory.getMetaData(path, new ProgressMonitorWrapper(monitor));
 		final List<String>       sets = new ArrayList<String>(meta.getDataNames()); // Will be small list			 
 		SortingUtils.removeIgnoredNames(sets, getIgnored());
 		Collections.sort(sets, new SortNatural<String>(true));
 		
 		this.metaData = meta;
 		if (dataFilter!=null) dataFilter.setMetaData(metaData);
 		if (monitor.isCanceled()) return;
 		
 		for (Iterator<CheckableObject> it = data.iterator(); it.hasNext();) {
 			final ICheckableObject ob = it.next();
 			if (!ob.isExpression()) {
 				it.remove();
 			} else {
 				ob.getExpression().clear();
 			}
 		}
 		
 		int pos = 0;
 		for (String name : sets) {
 			data.add(pos, new CheckableObject(name));
 			pos++;
 		}
 		this.filePath = path;
 		try {
 		    readExpressions();
 		} catch (Exception ne ) {
 			logger.error("Cannot read expressions for file.", ne);
 		}
 		
 		final Display dis = PlatformUI.getWorkbench().getDisplay();
 		dis.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				dataViewer.refresh();
 			}
 		});
 	}
 
 	private List<ICheckableObject> selections = new ArrayList<ICheckableObject>(7);
 		
 	/**
 	 * @return Returns the selections.
 	 */
 	public List<ICheckableObject> getSelections() {
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
 				ICheckableObject data = (ICheckableObject)item.getData();
 				if (rect1.contains(pnt)) {
 					isExpressionActive  = true;
 				} else {
 					isVariableNameActive= true;
 				}
 				dataViewer.editElement(data, rect1.contains(pnt)?1:6);
 			} finally {
 				isExpressionActive  = false;
 				isVariableNameActive= false;
 			}
           
 		}
 	}
 	
 
 	/**
 	 * Sent when a mouse button is pressed.
 	 *
 	 * @param e an event containing information about the mouse button press
 	 */
 	public void mouseDown(MouseEvent e) {
 		
 		isVariableNameActive = false;
 		isExpressionActive   = false;
 		if (e.button==1) {
 			
 			final Point           pnt     = new Point(e.x, e.y);
 			final TableItem       item    = this.dataViewer.getTable().getItem(pnt);
 			if (item==null) return;
 			
             Rectangle rect = item.getBounds(0); // First column (tick and name)
             if (!rect.contains(pnt)) return;
 		
             dataViewer.cancelEditing();
 			final CheckableObject clicked = (CheckableObject)item.getData();
 			
 			if (e.stateMask==131072) { // Shift is pressed
 				try {
 				    final ICheckableObject from = selections.get(selections.size()-1);
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
 			selectionChanged((CheckableObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement(), true);
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
 	
 	protected void selectionChanged(final CheckableObject check, boolean fireListeners) {
 		
 		if (selections==null) selections = new ArrayList<ICheckableObject>(7);
 
 		if (check!=null) {
 
 			check.setChecked(!check.isChecked());
 
 			if (!check.isChecked()) {
 				selections.remove(check);
 			} else {
 				// We only allow selection of one set not 1D
 				final int    dims = getActiveDimensions(check, true);
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
 			for (ICheckableObject set : selections) if (getActiveDimensions(set, true)==1)	is1D=true;
 
 			if (is1D) for (Iterator<ICheckableObject> it = selections.iterator(); it.hasNext();) {
 				ICheckableObject set = it.next();
 
 				if (getActiveDimensions(set, true)!=1) {
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
 
 	private synchronized void updateSelection(boolean fireListeners) {
 
 		if (selections==null) return;
 
 		// Record selections
 		if (Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SAVE_SEL_DATA)) try {
 			StringBuilder buf = new StringBuilder();
 			for (ICheckableObject ob : selections) {
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
 		for (CheckableObject sel : data) {
 			if (getActiveDimensions(sel, true)==1) {
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
 	
 	private void fireSelectionListeners(List<ICheckableObject> selections) {
 		if (listeners==null) return;
 		final SelectionChangedEvent event = new SelectionChangedEvent(this.dataViewer, new StructuredSelection(selections));
 		for (ISelectionChangedListener l : listeners) l.selectionChanged(event);
 	}
 
 	
 	public String getRootName() {
 		return rootName;
 	}
 
 	private PlotType plotMode = PlotType.XY;
 
 	public PlotType getPlotMode() {
 		return plotMode;
 	}
 
 	/**
 	 * @param pm The plotMode to set.
 	 */
 	public void setPlotMode(PlotType pm) {
 		plotMode = pm;
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
 
 	private List<String> getStringSelections(List<CheckableObject> selections) {
 		
 		final List<String> ret = new ArrayList<String>(selections.size());
 		for (ICheckableObject sel : selections) {
 			if (!sel.isExpression()) ret.add(sel.getName());
 		}
 		return ret;
 	}
 
 	
 	public AbstractDataset getDataSet(String name, final IMonitor monitor) {
 		
 		try {
 			if (providerDeligate!=null) {
 				return (AbstractDataset)providerDeligate.getDataset(name, monitor);
 			}
 			if (this.filePath==null) return null;
 			
 			AbstractDataset set = LoaderFactory.getDataSet(this.filePath, name, monitor);
 			try {
 			    set = set.squeeze();
 			} catch (Throwable ignored) {
 				// Leave set assigned as read
 			}
 			return set;
 			
 		} catch (IllegalArgumentException ie) {
 			return null;
 		} catch (Exception e) {
 			logger.error("Cannot get data set "+name+" from "+filePath+". Currently expressions can only contain existing Data Sets.", e);
 			return null;
 		}
 	}
 	
 	public ILazyDataset getLazyDataSet(String name, final IMonitor monitor) {
 		
 		try {
 			if (providerDeligate!=null) {
 				return providerDeligate.getLazyDataset(name, monitor);
 			}
 			if (this.filePath==null) return null;
 			
 			DataHolder holder = LoaderFactory.getData(filePath, monitor);
 			ILazyDataset set  = holder.getLazyDataset(name);
 			return set;
 			
 		} catch (IllegalArgumentException ie) {
 			return null;
 		} catch (Exception e) {
 			logger.error("Cannot get data set "+name+" from "+filePath+". Currently expressions can only contain existing Data Sets.", e);
 			return null;
 		}
 	}
 
 	
 	@Override
 	public AbstractDataset getVariableValue(String variableName, final IMonitor monitor) {
 
 		final ICheckableObject ob = getCheckableObjectByVariable(variableName);
 		if (!ob.isExpression()) {
 			return getDataSet(ob.getName(), monitor);
 		} else {
 			try {
 				return (AbstractDataset)ob.getExpression().getDataSet(null, new IMonitor.Stub());
 			} catch (Exception e) {
 				return null;
 			}
 		}		
 	}
 	
 	@Override
 	public ILazyDataset getLazyValue(String name, final IMonitor monitor) {
 		final ICheckableObject ob = getCheckableObjectByVariable(name);
 		if (!ob.isExpression()) {
 			return getLazyDataSet(ob.getName(), monitor);
 		} else {
 			try {
 				return ob.getExpression().getLazyDataSet(name, new IMonitor.Stub());
 			} catch (Exception e) {
 				return null;
 			}
 		}		
 	}
 
 	
 	@Override
 	public boolean isVariableName(String variableName, IMonitor monitor) {
 		final ICheckableObject ob = getCheckableObjectByVariable(variableName);
 		return ob!=null;
 	}
 	
 	private ICheckableObject getCheckableObjectByVariable(String variableName) {
 		for (ICheckableObject ob : data) {
 			if (ob.getVariable().equals(variableName)) return ob;
 		}
 		return null;
 	}
 	private CheckableObject getCheckableObjectByName(String name) {
 		for (CheckableObject ob : data) {
 			if (ob.getName().equals(name)) return ob;
 		}
 		return null;
 	}
 
 	public boolean isDataSetName(String name, IMonitor monitor) {
 		final List<String> allNames = getStringSelections(data);
 		return allNames.contains(name);
 	}
 	
 	private Color get1DPlotColor(ICheckableObject element) {
 		final String axis = element.getAxis(selections, getPlottingSystem().is2D(), getAbstractPlottingSystem().isXFirst());
 		if ("X".equals(axis)) return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
 		final String name = element.toString();
 		if (getPlottingSystem()!=null) {
 			final Color col = getAbstractPlottingSystem().get1DPlotColor(name);
 			return col;
 		}
 		return null;
 	}
 
 	private class DataSetColumnLabelProvider extends ColumnLabelProvider {
 		
 		private Color RED   = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
 		private Color BLUE  = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE);
 		private Color BLACK = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
 		private Image checkedIcon;
 		private Image uncheckedIcon;
 		
 		private int columnIndex;
 		DataSetColumnLabelProvider(int columnIndex) {
 			this.columnIndex = columnIndex;
 			if (columnIndex == 0) {
 				ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
 				checkedIcon   = id.createImage();
 				id = Activator.getImageDescriptor("icons/unticked.gif");
 				uncheckedIcon =  id.createImage();
 			}
 		}
 		
 		public Image getImage(Object ob) {
 			
 			if (columnIndex!=0) return null;
 			final ICheckableObject element = (ICheckableObject)ob;
 			return element.isChecked() ? checkedIcon : uncheckedIcon;
 		}
 		@Override
 		public String getText(Object ob) {
 			
 			final ICheckableObject element = (ICheckableObject)ob;
 			final String          name    = element.toString();
 			
 			switch (columnIndex) {
 			case 0:
 				return null;
 			case 1:
 				String setName = element.toString();
 				if (!element.isExpression() && rootName!=null && setName.startsWith(rootName)) {
 					setName = setName.substring(rootName.length());
 				}
 				return setName;
 			case 2:
 				return element.getAxis(selections, getPlottingSystem().is2D(), getAbstractPlottingSystem().isXFirst());
 
 			case 3:
 				if (!element.isExpression()) {
 					if (metaData.getDataSizes()==null) {
 						final ILazyDataset set = getLazyDataSet(name, (IMonitor)null);
 						if (set!=null) {
 							return set.getSize()+"";
 						}
 					    return "Unknown";
 						
 					}
 					return metaData.getDataSizes().get(name)+"";
 				} else {
 					final ILazyDataset set = element.getExpression().getLazyDataSet(name, new IMonitor.Stub());
 					if (set!=null) {
 						return set.getSize()+"";
 					}
 				    return "Unknown";
 				}
 			case 4:
 				return getActiveDimensions(element, false)+"";
 			case 5:
 				if (!element.isExpression()) {
 					if (metaData.getDataShapes()==null || metaData.getDataShapes().get(name)==null) {
 						final ILazyDataset set = getLazyDataSet(name, null);
 						if (set!=null) {
 							return Arrays.toString(set.getShape());
 						}
 					    return "Unknown";
 						
 					}
 					return Arrays.toString(metaData.getDataShapes().get(name));
 				}  else {
 					final ILazyDataset set = element.getExpression().getLazyDataSet(name, new IMonitor.Stub());
 					if (set!=null) {
 						return Arrays.toString(set.getShape());
 					}
 				    return "Unknown";
 				}
 
 			case 6:
 				return element.getVariable();
 			default:
 				return element.toString();
 			}
 		}
 	    
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
 		 */
 		public Color getForeground(Object ob) {
 			
 			final ICheckableObject element = (ICheckableObject)ob;
 	    		    	
 			switch (columnIndex) {
 			case 1:
 				if (element.isExpression()) {
 					final IExpressionObject o = element.getExpression();
 					return o.isValid(new IMonitor.Stub()) ? BLUE : RED;
 				} else if (element.isChecked()) {
 					return get1DPlotColor(element);
 				}
 				return BLACK;
 			case 6:
 				return BLUE;
 			default:
 				return BLACK;
 			}
 	    }
 	}
 	
 	public void dispose() {
 		
 		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propListener);
 		datasetSelection = null;
 		if (listeners!=null) listeners.clear();
 		if (data != null)       this.data.clear();
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
 	}
 
 	public int getActiveDimensions(ICheckableObject element, boolean squeeze) {
 		
 		if (element.isExpression()) {
 		    try {
 				return element.getExpression().getLazyDataSet(element.getVariable(), new IMonitor.Stub()).getRank();
 			} catch (Exception e) {
 				logger.error("Could not get shape of "+element.getVariable());
 				return 1;
 			}
 		}
 		
 		final String name = element.getName();
 		if (metaData.getDataShapes()==null || metaData.getDataShapes().get(name)==null) {
 			final ILazyDataset set = getLazyDataSet(name, (IMonitor)null);
 			// Assuming it has been squeezed already
 			if (set!=null) {
 				return set.getShape().length;
 			}
 			return 1;
 
 		}
 		if (metaData.getDataShapes().get(name)!=null) {
 			final int[] shape = metaData.getDataShapes().get(name);
 			if (squeeze) {
 				int count = 0;
 				for (int i : shape) if (i>1) ++count;
 				if (count<1) count=1;
 				return count;
 			} else {
 				return shape.length;
 			}
 		}
 		return 1;
 	}
 
 	public void addExpression() {
 		
 		if (!Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_VARNAME)) {
 		    Activator.getDefault().getPreferenceStore().setValue(EditorConstants.SHOW_VARNAME, true);
 		} 
 		final CheckableObject newItem = new CheckableObject(service.createExpressionObject(this, null, null));
 		data.add(newItem);
 		dataViewer.refresh();
 		try {
 			isExpressionActive = true;
 			dataViewer.editElement(newItem, 1);	
 		} finally {
 			isExpressionActive = false;
 		}
 	}
 
 	protected void addExpression(IExpressionObject expressionObject) {
 		data.add(new CheckableObject(expressionObject));
 		dataViewer.refresh();
 	}
 
 	public void deleteExpression() {
 		final Object sel = ((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
 		if (sel==null || !(sel instanceof CheckableObject)) return;
 		
 		if (!((ICheckableObject)sel).isExpression()) return;
 	    if (selections!=null) selections.remove(sel);
 		data.remove(sel);
 		clearExpressionCache();
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
 	 * @param meta
 	 */
 	public void setMetaData(final IMetaData meta) {
 		
 		if (meta==null) return;
 		this.data.clear();
 		final Collection<String> names = meta.getDataNames();
 		for (String name : names) this.data.add(new CheckableObject(name));
 		
 		// Search names to see if they all have a common root, we do not show this.
 		this.rootName = DatasetTitleUtils.getRootName(names);
 		
 		if (meta.getUserObjects()!=null) {
 			// TODO is this needed
 			//this.data.addAll(meta.getUserObjects());
 		}
 		this.metaData = meta;
 		if (dataFilter!=null) dataFilter.setMetaData(this.metaData);
 
 		try {
 		    readExpressions();
 		} catch (Exception ne ) {
 			logger.error("Cannot read expressions for file.", ne);
 		}
 		
 		// Some of the meta data
 		if (Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SAVE_SEL_DATA)) try {
 			final String prop = Activator.getDefault().getPreferenceStore().getString(DATA_SEL);
 			if (prop!=null) {
 				final Collection<String> saveSelections = Arrays.asList(prop.split(","));
 				if (data!=null && !data.isEmpty()) {
 					boolean foundData = false;
 					for (CheckableObject checker : data) {
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
 
 	private AbstractDataset datasetSelection = null;
 	/**
 	 * Thread safe
 	 * @param name
 	 */
 	public AbstractDataset setDatasetSelected(final String name, final boolean clearOthers) {
 		
 		datasetSelection = null;
 		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				if (clearOthers) {
 					PlotDataComponent.this.setAllChecked(false);
 					PlotDataComponent.this.selectionChanged(null, true);
 				}
 				
 				final CheckableObject check = PlotDataComponent.this.getObjectByName(name);
 				check.setChecked(false);
 				PlotDataComponent.this.selectionChanged(check, true);
 				datasetSelection = PlotDataComponent.this.getDataSet(name, (IMonitor)null);
 			}
 		});
 		
 		return datasetSelection;
 	}
 
 	protected void setAllChecked(boolean isChecked) {
 		for (ICheckableObject check : data) {
 			check.setChecked(isChecked);
 		}
 	}
 
 	/**
 	 * Does a loop, may be bad...
 	 * @param name
 	 * @return
 	 */
 	protected CheckableObject getObjectByName(final String name) {
 		for (CheckableObject check : data) {
 			if (name.equals(check.getName())) return check;
 		}
 		return null;
 	}
 
 	public IMetaData getMetaData() {
 		return metaData;
 	}
 
 	public IPlottingSystem getPlottingSystem() {
 		return providerDeligate!=null ? providerDeligate.getPlottingSystem() : null;
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
 
 
 	public List<CheckableObject> getData() {
 		return data;
 	}
 
 
 	public ISelectionProvider getViewer() {
 		return dataViewer;
 	}
 	
 	
 	private class AxisEditingSupport extends EditingSupport {
 
 		public AxisEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 		}
 		
 		@Override
 		protected CellEditor getCellEditor(final Object element) {
 			// FIX to http://jira.diamond.ac.uk/browse/DAWNSCI-380 remove axes until they work
 			ComboBoxCellEditor ce = new ComboBoxCellEditor((Composite)getViewer().getControl(), new String[]{"X","Y1","Y2" /**,"Y3","Y4" **/} , SWT.READ_ONLY);
 			final CCombo ccombo = (CCombo)ce.getControl();
 			ccombo.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					setValue(element, ccombo.getSelectionIndex());
 				}
 			});
 			return ce;
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			ICheckableObject co = (ICheckableObject)element;
 			return co.isChecked();
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return ((ICheckableObject)element).getAxisIndex(selections, getAbstractPlottingSystem().isXFirst());
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			getViewer().cancelEditing();
 			CheckableObject co = (CheckableObject)element;
 			if (value instanceof Integer) {
 				int isel = ((Integer)value).intValue();
 				if (isel==0) {
 					setAsX(co);
 				} else {
 					
 					if (getAbstractPlottingSystem().isXFirst() && "X".equals(co.getAxis(selections, getPlottingSystem().is2D(), true))) {
 						// We lost an x
 						getAbstractPlottingSystem().setXFirst(false);
 					}
 					co.setYaxis(isel);
                     
 					getPlottingSystem().clear();
 					fireSelectionListeners(selections);
 					getViewer().refresh();
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		if (adapter==IPlottingSystem.class) {
 			return getPlottingSystem();
 		}
 		return null;
 	}
 
 
 }
