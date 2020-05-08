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
 
 import org.dawb.common.ui.DawbUtils;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystemData;
 import org.dawb.common.ui.plot.IPlottingSystemSelection;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.common.ui.widgets.DoubleClickModifier;
 import org.dawb.common.util.io.PropUtils;
 import org.dawb.common.util.io.SortingUtils;
 import org.dawb.common.util.list.SortNatural;
 import org.dawb.gda.extensions.util.DatasetTitleUtils;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.editors.preference.EditorConstants;
 import org.dawb.workbench.ui.editors.preference.EditorPreferencePage;
 import org.dawb.workbench.ui.editors.slicing.ExpressionObject;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ICellModifier;
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
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.progress.IProgressService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.gda.monitor.IMonitor;
 
 /**
  * This view can view and plot any file. It is most efficient if the Loader that LoaderFactory
  * uses for this file type is an IMetaLoader. 
  */
 public class PlotDataComponent implements IPlottingSystemData, MouseListener, KeyListener, IPlottingSystemSelection {
 		
 	private static final Logger logger = LoggerFactory.getLogger(PlotDataComponent.class);
 
 	// NOTE Old ID before this class was convert to display files without knowing the 
 	// underlying file type.
 	public static final String ID = "uk.ac.gda.views.nexus.NexusPlotView"; //$NON-NLS-1$
 
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
 
 	protected IMetaData           metaData;
 
 	protected final IPlottingSystemData providerDeligate;
 
 	private IPropertyChangeListener propListener;
 
 	private ArrayList<IAction> dataComponentActions;
 
 	private Composite container;
 
 	private DataFilter dataFilter;
 	
 	public PlotDataComponent(final IPlottingSystemData providerDeligate) {
 				
 		this.data = new ArrayList<CheckableObject>(7);
 		this.providerDeligate   = providerDeligate;
 		
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
 					setColumnVisible(1, 24, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_DATA_SIZE)) {
 					setColumnVisible(2, 100, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_DIMS)) {
 					setColumnVisible(3, 100, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_SHAPE)) {
 					setColumnVisible(4, 100, (Boolean)event.getNewValue());
 				} else if (event.getProperty().equals(EditorConstants.SHOW_VARNAME)) {
 					setColumnVisible(5, 100, (Boolean)event.getNewValue());
 				}
 			}
 		};
 		// If they change the ignore filters activity, recompute the available data sets.
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propListener);
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
 		searchText.setToolTipText("Search on data set name or expression value." );
 				
 		this.dataViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL);
 		
 		dataViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 		dataViewer.getTable().addMouseListener(this);
 		dataViewer.getTable().addKeyListener(this);
 		dataViewer.getTable().setLinesVisible(true);
 		dataViewer.getTable().setHeaderVisible(true);
 		
 		createColumns();
         dataViewer.setColumnProperties(new String[]{"Data","Length"});
         
         dataViewer.setCellEditors(createCellEditors(dataViewer));
         dataViewer.setCellModifier(createModifier(dataViewer));
         
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
 		
 		setColumnVisible(1, 24,  Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_XY_COLUMN));
 		setColumnVisible(2, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_DATA_SIZE));
 		setColumnVisible(3, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_DIMS));
 		setColumnVisible(4, 180, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_SHAPE));
 		setColumnVisible(5, 150, Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_VARNAME));
 	
 		try {
 			/**
 			 * No need to remove this one, the listeners are cleared on a dispose
 			 */
 			getPlottingSystem().addTraceListener(new ITraceListener.Stub() {
 				@Override
 				public void tracesAltered(TraceEvent evt) {
 					updateSelection();
 				}
 			});
 			
 			
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
 	}
 	
 
 	public void setFocus() {
 		if (dataViewer!=null && !dataViewer.getControl().isDisposed()) {
 			dataViewer.getControl().setFocus();
 		}
 	}
 
 	private void saveExpressions() {
 		try {
 			final Properties props = new Properties();
 			for (CheckableObject check : data) {
 				if (check.isExpression()) {
 					ExpressionObject o = check.getExpression();
 					props.setProperty(o.getMementoKey(), o.getExpression());
 				}
 			}
 			
 			// Save properties to workspace.
 			final String cachePath = DawbUtils.getDawbHome()+getFileName()+".properties";
 			PropUtils.storeProperties(props, cachePath);
 			
 		} catch (Exception e) {
 			logger.error("Cannot save expression", e);
 		}
 	}
 	
 	private void readExpressions() throws Exception {
 		
 		final String cachePath = DawbUtils.getDawbHome()+getFileName()+".properties";
 		Properties props = PropUtils.loadProperties(cachePath);
 		if (props!=null) {
 			try {
 				for (Object name : props.keySet()) {
 					final String key = name.toString();
 					if (ExpressionObject.isExpressionKey(key)) {
 						final CheckableObject o = new CheckableObject();
 						o.setExpression(new ExpressionObject(this, props.getProperty(key), key));
 						data.add(o);
 					}
 				}
 			} catch (Exception ne) {
 				throw new PartInitException(ne.getMessage());
 			}
 		}
 	}
 
 	private void createRightClickMenu() {	
 	    final MenuManager menuManager = new MenuManager();
 	    dataViewer.getControl().setMenu (menuManager.createContextMenu(dataViewer.getControl()));
 		createDimensionalActions(menuManager, false);
 		menuManager.add(new Separator(getClass().getName()+"sep1"));
 		menuManager.add(new Action("Preferences...") {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.edna.workbench.editors.preferencePage", null, null);
 				if (pref != null) pref.open();
 			}
 		});
 	}
 	
 	private void createColumns() {
 		
 		ColumnViewerToolTipSupport.enableFor(dataViewer,ToolTip.NO_RECREATE);
 		
 		final TableViewerColumn name   = new TableViewerColumn(dataViewer, SWT.LEFT, 0);
 		name.getColumn().setText("Name");
 		name.getColumn().setWidth(180);
 		name.setLabelProvider(new DataSetColumnLabelProvider(0));
 		
 		final TableViewerColumn axis   = new TableViewerColumn(dataViewer, SWT.LEFT, 1);
 		axis.getColumn().setText(" ");
 		axis.getColumn().setWidth(24);
 		axis.setLabelProvider(new DataSetColumnLabelProvider(1));
 
 		final TableViewerColumn size   = new TableViewerColumn(dataViewer, SWT.LEFT, 2);
 		size.getColumn().setText("Size");
 		size.getColumn().setWidth(150);
 		size.getColumn().setResizable(true);
 		size.setLabelProvider(new DataSetColumnLabelProvider(2));
 			
 		final TableViewerColumn dims   = new TableViewerColumn(dataViewer, SWT.LEFT, 3);
 		dims.getColumn().setText("Dimensions");
 		dims.getColumn().setWidth(150);
 		dims.getColumn().setResizable(true);
 		dims.setLabelProvider(new DataSetColumnLabelProvider(3));
 		
 		final TableViewerColumn shape   = new TableViewerColumn(dataViewer, SWT.LEFT, 4);
 		shape.getColumn().setText("Shape");
 		shape.getColumn().setWidth(200);
 		shape.getColumn().setResizable(true);
 		shape.setLabelProvider(new DataSetColumnLabelProvider(4));
 
 		final TableViewerColumn varName   = new TableViewerColumn(dataViewer, SWT.LEFT, 5);
 		varName.getColumn().setText("Variable");
 		varName.getColumn().setWidth(150);
 		varName.getColumn().setResizable(true);
 		varName.setLabelProvider(new DataSetColumnLabelProvider(5));
 	}
 	
 	private CellEditor[] createCellEditors(final TableViewer tableViewer) {
 		CellEditor[] editors  = new CellEditor[1];
 		TextCellEditor nameEd = new TextCellEditor(tableViewer.getTable());
 		((Text)nameEd.getControl()).setTextLimit(60);
 		// NOTE Must not add verify listener - it breaks things.
 		editors[0] = nameEd;
 		
 		return editors;
 	}
 	
 	private ICellModifier createModifier(final TableViewer tableViewer) {
 		return new DoubleClickModifier(tableViewer) {
 			@Override
 			public boolean canModify(Object element, String property) {
 				if (!enabled) return false;
 				return (element instanceof CheckableObject) && ((CheckableObject)element).isExpression() && "Data".equalsIgnoreCase(property);
 			}
 
 			@Override
 			public Object getValue(Object element, String property) {
 				// NOTE: Only works for scannables right now which have one name
 				final String expr = ((CheckableObject)element).getExpression().getExpression();
 				return expr!=null ? expr : "";
 			}
 			@Override
 			public void modify(Object item, String property, Object value) {
 				
 				try {
 				    final CheckableObject check = (CheckableObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
 				    final ExpressionObject ob   = check.getExpression();
 					ob.setExpression((String)value);
 					check.setChecked(true);
 					dataViewer.refresh();
 					
 					saveExpressions();
 					
 				} catch (Exception e) {
 					logger.error("Cannot set "+property, e);
 	
 				} finally {
 					setEnabled(false);
 				}
 			}
 	    };
 	}
 
 	private void createDimensionalActions(IContributionManager manager, boolean isToolbar) {
 				
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
 					setPlotMode(PlotType.PT1D);
 				}
 			};
 			xyAction.setImageDescriptor(Activator.getImageDescriptor("/icons/chart_curve.png"));
 			xyAction.setToolTipText("XY Graph of Data, overlayed for multiple data.");
 			dataComponentActions.add(xyAction);
 			
 			final Action staggeredAction = new Action("XY Staggered in Z",  SWT.TOGGLE) {
 				@Override
 				public void run() {
 					setPlotMode(PlotType.PT1D_STACKED);
 				}
 			};		
 			staggeredAction.setImageDescriptor(Activator.getImageDescriptor("/icons/chart_curve_staggered.png"));
 			staggeredAction.setToolTipText("XY Graph of Data, staggered in Z for multiple data.");
 			dataComponentActions.add(staggeredAction);
 	
 			final Action xyzAction = new Action("XYZ",  SWT.TOGGLE) {
 				@Override
 				public void run() {
 					setPlotMode(PlotType.PT1D_3D);
 				}
 			};		
 			xyzAction.setImageDescriptor(Activator.getImageDescriptor("/icons/chart_curve_3D.png"));
 			xyzAction.setToolTipText("XYZ, X is the first chosen data and Z the last.");
 			dataComponentActions.add(xyzAction);
 	
 			manager.add(new Separator());
 			manager.add(xyAction);
 			manager.add(staggeredAction);
 			manager.add(xyzAction);
 			manager.add(new Separator());
 			
 
 			
 			// Removed when part disposed.
 			addPlotModeListener(new PlotModeListener() {			
 				@Override
 				public void plotChangePerformed(PlotType plotMode) {
 					updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, plotMode);
 					updateSelection();
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
 			} else {
 				final MenuManager man = (MenuManager)manager;
 				man.addMenuListener(new IMenuListener() {
 					@Override
 					public void menuAboutToShow(IMenuManager manager) {
 						dis.asyncExec(new Runnable() {
 							@Override
 							public void run() {
 						        updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, getPlotMode());
 							}
 						});
 					}
 				});
 			}
 		}
 		
 		final Action setX = new Action("Set Selected Set as X-Axis") {
 			public void run() {
 				
 				final CheckableObject sel = (CheckableObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
 				if (sel==null) return;
 				
 				if (getDimensionCount(sel)!=1) return; 
 				sel.setChecked(true);
 				
 				if (selections.contains(sel)) selections.remove(sel);
 			    selections.add(0, sel);
 				getPlottingSystem().setXfirst(true);
 				updateSelection();
 				dataViewer.refresh(sel);
 			}
 		};
 		setX.setImageDescriptor(Activator.getImageDescriptor("/icons/to_x.png"));
 		setX.setToolTipText("Changes the plot to use selected data set as the x-axis.");
 		dataComponentActions.add(setX);
 		manager.add(setX);
 		manager.add(new Separator());
 
 		final Action addExpression = new Action("Add Expression") {
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
 		manager.add(addExpression);
 		
 		final Action deleteExpression = new Action("Delete Expression") {
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
 		manager.add(deleteExpression);
 
 		dataComponentActions.add(preferences);
 
 	}
 
 	public List<IAction> getDimensionalActions() {
 		
 		return dataComponentActions;
 		
 	}
 	
 	protected void updatePlotDimenionsSelected(Action xyAction, Action staggeredAction, Action xyzAction, PlotType plotMode) {
 
 		xyAction.setChecked(PlotType.PT1D.equals(plotMode));
 		staggeredAction.setChecked(PlotType.PT1D_STACKED.equals(plotMode));
 		xyzAction.setChecked(PlotType.PT1D_3D.equals(plotMode));
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
 			final CheckableObject ob = it.next();
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
 
 	private List<CheckableObject> selections = new ArrayList<CheckableObject>(7);
 		
 	/**
 	 * @return Returns the selections.
 	 */
 	public List<CheckableObject> getSelections() {
 		return selections;
 	}
 	
 	public void mouseDoubleClick(MouseEvent e){ 
 		
 	}
 	
 	public void keyPressed(KeyEvent e) {
 		if (e.keyCode==13) {
 			selectionChanged((CheckableObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement());
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
 	 * Sent when a mouse button is pressed.
 	 *
 	 * @param e an event containing information about the mouse button press
 	 */
 	public void mouseDown(MouseEvent e) {
 		if (e.button==1) {
 			final TableItem item = this.dataViewer.getTable().getItem(new Point(e.x, e.y));
 			if (item!=null) selectionChanged((CheckableObject)item.getData());
 		}
 	}
 
 	/**
 	 * Sent when a mouse button is released.
 	 *
 	 * @param e an event containing information about the mouse button release
 	 */
 	public void mouseUp(MouseEvent e) {
 		
 	}
 	
 	protected void selectionChanged(final CheckableObject check) {
 		
 		if (selections==null) selections = new ArrayList<CheckableObject>(7);
 
 		if (check!=null) {
 
 			check.setChecked(!check.isChecked());
 
 			if (!check.isChecked()) {
 				selections.remove(check);
 			} else {
 				// We only allow selection of one set not 1D
 				final int    dims = getDimensionCount(check);
 				if (dims!=1) { // Nothing else gets selected
 					PlotDataComponent.this.setAllChecked(false);
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
 			for (CheckableObject set : selections) if (getDimensionCount(set)==1)	is1D=true;
 
 			if (is1D) for (Iterator<CheckableObject> it = selections.iterator(); it.hasNext();) {
 				CheckableObject set = it.next();
 
 				if (getDimensionCount(set)!=1) {
 					set.setChecked(false);
 					it.remove();
 				}
 			}
 
 		} else {
 			selections.clear();
 		}
 
 		updateSelection();
 		this.dataViewer.refresh();
 	}
 
 	private synchronized void updateSelection() {
 
 		if (selections==null) return;
 
 		// Record selections
 		try {
 			StringBuilder buf = new StringBuilder();
 			for (CheckableObject ob : selections) {
 				buf.append(ob.getName());
 				buf.append(",");
 			}
 			
 			Activator.getDefault().getPreferenceStore().setValue(DATA_SEL, buf.toString());
 		} catch (Throwable ne) {
 			logger.error("Cannot save last selections!", ne);
 		}
 		
 		fireSelectionListeners(selections);
 	}
 
     @Override
 	public void setAll1DSelected(final boolean overide) {
 		
  		if (!overide && getSelections()!=null && getSelections().size()>0 ) {
         	return; // Something selected, leave it alone.
         }
 		
 		selections.clear();
 		for (CheckableObject sel : data) {
 			if (getDimensionCount(sel)==1) {
 				selections.add(sel);
 				sel.setChecked(true);
 			}
 		}
 		updateSelection();
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
 	
 	private void fireSelectionListeners(List<CheckableObject> selections) {
 		if (listeners==null) return;
 		final SelectionChangedEvent event = new SelectionChangedEvent(this.dataViewer, new StructuredSelection(selections));
 		for (ISelectionChangedListener l : listeners) l.selectionChanged(event);
 	}
 
 	
 	public String getRootName() {
 		return rootName;
 	}
 
 	private PlotType plotMode = PlotType.PT1D;
 
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
 		for (CheckableObject sel : selections) {
 			if (!sel.isExpression()) ret.add(sel.getName());
 		}
 		return ret;
 	}
 
 	
 	@Override
 	public AbstractDataset getDataSet(String name, final IMonitor monitor) {
 		
 		try {
 			if (providerDeligate!=null) {
 				return providerDeligate.getDataSet(name, monitor);
 			}
 			if (this.filePath==null) return null;
 			
 			return LoaderFactory.getDataSet(this.filePath, name, monitor);
 			
 		} catch (IllegalArgumentException ie) {
 			return null;
 		} catch (Exception e) {
 			logger.error("Cannot get data set "+name+" from "+filePath+". Currently expressions can only contain existing Data Sets.", e);
 			return null;
 		}
 	}
 	
 	@Override
 	public AbstractDataset getExpressionSet(String name, final IMonitor monitor) {
 
 		AbstractDataset set = getDataSet(name, monitor);
 		if (set==null) {
 			final List<String> names = getStringSelections(data);
 			for (String n : names) {
 				if (ExpressionObject.getSafeName(n).equals(name)) {
 					set = getDataSet(n, monitor);
 				}
 			}
 		}
 		
 		return set;
 	}
 	
 	@Override
 	public boolean isDataSetName(String name, IMonitor monitor) {
 		final List<String> allNames = getStringSelections(data);
 		return allNames.contains(name);
 	}
 	
 	@Override
 	public boolean isExpressionSetName(String name, IMonitor monitor) {
 		if (isDataSetName(name, monitor)) return true;
 		final List<String> allNames = getStringSelections(data);
 		for (String n : allNames) {
 			if (ExpressionObject.getSafeName(n).equals(name)) return true;
 		}
 		return false;
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
 			final CheckableObject element = (CheckableObject)ob;
 			return element.isChecked() ? checkedIcon : uncheckedIcon;
 		}
 		@Override
 		public String getText(Object ob) {
 			
 			final CheckableObject element = (CheckableObject)ob;
 			switch (columnIndex) {
 			case 0:
 				String setName = element.toString();
 				if (!element.isExpression() && rootName!=null) {
 					setName = setName.substring(rootName.length());
 				}
 				return setName;
 			case 1:
 				if (selections!=null&&!selections.isEmpty()) {
 					if (selections.size()>1) {
 						if (selections.contains(element)) {
 							if (selections.indexOf(element)==0) {
 								return "X";
 							}
 							if (selections.size()>2) {
 								return "Y"+selections.indexOf(element);
 							}
 							return "Y";
 						}
 					} if (selections.size()==1 && selections.contains(element)) {
 						return "Y";
 					}
 				}
                 return "";
 			case 2:
 				if (!element.isExpression()) {
 					final String name = element.toString();
 					if (metaData.getDataSizes()==null) {
 						final IDataset set = getDataSet(name, (IMonitor)null);
 						if (set!=null) {
 							return set.getSize()+"";
 						}
 					    return "Unknown";
 						
 					}
 					return metaData.getDataSizes().get(name)+"";
 				} 
 				return element.getExpression().getSize(new NullProgressMonitor())+"";
 			case 3:
 				return getDimensionCount(element)+"";
 			case 4:
 				if (!element.isExpression()) {
 					final String name = element.toString();
 					if (metaData.getDataShapes()==null) {
 						final IDataset set = getDataSet(name, (IMonitor)null);
 						if (set!=null) {
 							return Arrays.toString(set.getShape());
 						}
 					    return "Unknown";
 						
 					}
 					return Arrays.toString(metaData.getDataShapes().get(name));
 				} 
 				return "["+element.getExpression().getSize(new NullProgressMonitor())+"]";
 			case 5:
 				if (!element.isExpression()) {
 				    return ExpressionObject.getSafeName(element.getName());
 				} else {
 					return "";
 				}
 			default:
 				return element.toString();
 			}
 		}
 	    
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
 		 */
 		public Color getForeground(Object ob) {
 			
 			final CheckableObject element = (CheckableObject)ob;
 	    		    	
 			switch (columnIndex) {
 			case 0:
 				if (!element.isExpression()) {
 					final String         name = element.toString();
 					if (getPlottingSystem()!=null) {
 						final Color col = getPlottingSystem().get1DPlotColor(name);
 						return col;
 					}
 				} else {
 					final ExpressionObject o = element.getExpression();
 					return o.isValid(new NullProgressMonitor()) ? BLUE : RED;
 				}
 				return BLACK;
 			case 5:
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
 		if (dataViewer!=null && !dataViewer.getControl().isDisposed()) {
 			dataViewer.getTable().removeMouseListener(this);
 			dataViewer.getTable().removeKeyListener(this);
 		}
 	}
 
 	public int getDimensionCount(CheckableObject element) {
 		
 		if (!element.isExpression()) {
 			final String name = element.getName();
 			if (metaData.getDataShapes()==null) {
 				final IDataset set = getDataSet(name, (IMonitor)null);
 				if (set!=null) {
 					return set.getShape().length;
 				}
 			    return 1;
 				
 			}
 			if (metaData.getDataShapes().get(name)!=null) {
 			    return metaData.getDataShapes().get(name).length;
 			}
 		} 
 		return 1;
 	}
 
 	public void addExpression() {
 		final CheckableObject newItem = new CheckableObject(new ExpressionObject(this));
 		data.add(newItem);
 		dataViewer.refresh();
 		
 		((DoubleClickModifier)dataViewer.getCellModifier()).setEnabled(true);		
 		dataViewer.editElement(newItem, 0);
 		
 	}
 
 	protected void addExpression(ExpressionObject expressionObject) {
 		data.add(new CheckableObject(expressionObject));
 		dataViewer.refresh();
 	}
 
 	public void deleteExpression() {
 		final Object sel = ((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
 		if (sel==null || !(sel instanceof CheckableObject)) return;
 		
 		if (!((CheckableObject)sel).isExpression()) return;
 		data.remove(sel);
 		dataViewer.refresh();
 		saveExpressions();
 
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
 		try {
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
 
 	/**
 	 * @return exprs can be null or empty
 	 */
 	public List<Object> getExpressions(IPlottingSystemData prov) {
 		if (data == null) return null;
 		final List<Object> exprs = new ArrayList<Object>(3);
 		for (Object o : data) {
 			if (o instanceof ExpressionObject) {
 				ExpressionObject e = (ExpressionObject)o;
 				e = new ExpressionObject(prov, e.getExpression(), e.getMementoKey());
 				exprs.add(e);
 			}
 		}
 		return exprs;
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
 					PlotDataComponent.this.selectionChanged(null);
 				}
 				
 				final CheckableObject check = PlotDataComponent.this.getObjectByName(name);
 				check.setChecked(false);
 				PlotDataComponent.this.selectionChanged(check);
 				datasetSelection = PlotDataComponent.this.getDataSet(name, (IMonitor)null);
 			}
 		});
 		
 		return datasetSelection;
 	}
 
 	protected void setAllChecked(boolean isChecked) {
 		for (CheckableObject check : data) {
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
 
 
 	@Override
 	public AbstractPlottingSystem getPlottingSystem() {
 		return providerDeligate!=null ? providerDeligate.getPlottingSystem() : null;
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
 
 }
