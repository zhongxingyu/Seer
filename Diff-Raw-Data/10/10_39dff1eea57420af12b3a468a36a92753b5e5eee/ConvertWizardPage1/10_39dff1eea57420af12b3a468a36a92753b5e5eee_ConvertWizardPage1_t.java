 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.edna.workbench.actions;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 /**
  *   ConvertWizardPage1
  *
  *   @author gerring
  *   @date Aug 31, 2010
  *   @project org.edna.workbench.actions
  **/
 public class ConvertWizardPage1 extends WizardPage {
 
 	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConvertWizardPage1.class);
 	
 	private CheckboxTableViewer checkboxTableViewer;
 	private String[]            dataSetNames;
 
 	private Label      txtLabel;
 	private Text       txtPath;
 	private boolean overwrite = false;
 	private boolean open      = true;
 	private IFile   path;
 	private IFile   source;
 
 	private IMetaData imeta;
 
 	private DataHolder holder;
 
 	/**
 	 * Create the wizard.
 	 */
 	public ConvertWizardPage1() {
 		super("wizardPage");
 		setTitle("Convert Data");
 		setDescription("Convert data from synchrotron formats and compressed files to common simple data formats.");
 		dataSetNames = new String[]{"Loading..."};
     }
 
 	/**
 	 * Create contents of the wizard.
 	 * @param parent
 	 */
 	public void createControl(Composite parent) {
 		
 		ISelection selection = EclipseUtils.getActivePage().getSelection();
 		createSource(selection);
 
 		Composite container = new Composite(parent, SWT.NULL);
 
 		setControl(container);
 		container.setLayout(new GridLayout(1, false));
 		
 		Composite top = new Composite(container, SWT.NONE);
 		top.setLayout(new GridLayout(3, false));
 		top.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
 		
 		Label convertLabel = new Label(top, SWT.NONE);
 		convertLabel.setText("Convert to");
 		
 		Combo combo = new Combo(top, SWT.READ_ONLY);
 		combo.setItems(new String[] {"Comma Separated Values (*.csv)"});
 		combo.setToolTipText("Convert to file type by file extension");
 		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
 		combo.select(0);
 		
 		txtLabel = new Label(top, SWT.NULL);
 		txtLabel.setText("Export &File  ");
 		txtPath = new Text(top, SWT.BORDER);
 		txtPath.setEditable(false);
 		txtPath.setEnabled(false);
 		txtPath.setText(getPath().getFullPath().toOSString());
 		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		txtPath.setLayoutData(gd);
 		txtPath.addModifyListener(new ModifyListener() {			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				pathChanged();
 			}
 		});
 
 		Button button = new Button(top, SWT.PUSH);
 		button.setText("...");
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleBrowse();
 			}
 		});
 		
 		final Button over = new Button(top, SWT.CHECK);
 		over.setText("Overwrite file if it exists.");
 		over.setSelection(overwrite);
 		over.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				overwrite = over.getSelection();
 				pathChanged();
 			}
 		});
 		over.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
 		
 		final Button open = new Button(top, SWT.CHECK);
 		open.setText("Open file after export.");
 		open.setSelection(true);
 		open.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ConvertWizardPage1.this.open = open.getSelection();
 				pathChanged();
 			}
 		});
 		open.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
 
 
 		pathChanged();
 
 		
 		Composite main = new Composite(container, SWT.NONE);
 		main.setLayout(new GridLayout(2, false));
 		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		final Label chooseData = new Label(main, SWT.LEFT);
 		chooseData.setText("Please tick data to export:");
 		
 		final ToolBarManager toolMan = new ToolBarManager(SWT.RIGHT|SWT.FLAT);
         createActions(toolMan);
         toolMan.createControl(main);
         toolMan.getControl().setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
 		
 		this.checkboxTableViewer = CheckboxTableViewer.newCheckList(main, SWT.BORDER | SWT.FULL_SELECTION);
 		Table table = checkboxTableViewer.getTable();
 		table.setToolTipText("Select data to export to the csv.");
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		
 		final MenuManager man = new MenuManager();
         createActions(man);
         Menu menu = man.createContextMenu(checkboxTableViewer.getControl());
         checkboxTableViewer.getControl().setMenu(menu);
 	
 		checkboxTableViewer.setContentProvider(new IStructuredContentProvider() {
 			@Override
 			public void dispose() {}
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 
 			@Override
 			public Object[] getElements(Object inputElement) {
 				return dataSetNames;
 			}
 		});
 		checkboxTableViewer.setInput(new Object());
 		checkboxTableViewer.setAllGrayed(true);
 		
 		// We populate the names later using a wizard task.
         try {
 			getDataSetNames();
 		} catch (Exception e) {
 			logger.error("Cannot extract data sets!", e);
 		}
 
 	}
 	
 	private void createActions(IContributionManager toolMan) {
 		
         final Action tickNone = new Action("Select None", Activator.getImageDescriptor("icons/unticked.gif")) {
         	public void run() {
         		checkboxTableViewer.setAllChecked(false);
         	}
         };
         toolMan.add(tickNone);
         
         final Action tickAll1D = new Action("Select All 1D Data", Activator.getImageDescriptor("icons/ticked.png")) {
         	public void run() {
         		setAll1DChecked();
         	}
         };
         toolMan.add(tickAll1D);
 
 	}
 
 	private void createSource(ISelection selection) {
 		StructuredSelection s = (StructuredSelection)selection;
 		final Object        o = s.getFirstElement();
 		if (o instanceof IFile) source = (IFile)o;
 	}
 	
 	private static IContainer exportFolder = null;
 	IFile getPath() {
 		if (path==null) { // We make one up from the source
 			IFile source = getSource();
 			final String strPath = source.getName().substring(0, source.getName().indexOf("."))+".csv";
 			if (exportFolder == null) {
 				this.path = source.getParent().getFile(new Path(strPath));
 			} else {
 				this.path = exportFolder.getFile(new Path(strPath));
 
 			}		
 		}
 		return path;
 	}
 	
 	public IFile getSource() {
 		return source;
 	}
 
 	/**
 	 * Uses the standard container selection dialog to choose the new value for the container field.
 	 */
 
 	private void handleBrowse() {
 		final IFile p = WorkspaceResourceDialog.openNewFile(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
 				"Export location", "Please choose a location to export the ascii data to. This must be a cvs file.", 
 				getPath().getFullPath(), null);
 		if (p!=null) {
 			this.path = p;
 		    txtPath.setText(this.path.getFullPath().toOSString());
 		    exportFolder = p.getParent();
 		}
 		pathChanged();
 	}
 
 	/**
 	 * Ensures that both text fields are set.
 	 */
 
 	private void pathChanged() {
 
         final String p = txtPath.getText();
         txtLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
 		if (p==null || p.length() == 0) {
 			updateStatus("Please select a file to export to.");
 			return;
 		}
 		IFile path = getPath();
 		if (path.exists() && (!path.isAccessible() || path.isReadOnly())) {
 			updateStatus("Please choose another location to export to; this one is read only.");
 			txtLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
 			return;
 		}
 		if (path.exists() && !overwrite) {
 			updateStatus("Please confirm overwrite of the file.");
 			return;
 		}
 		if (!path.getName().toLowerCase().endsWith(".csv")) {
 			updateStatus("Please set the file name to export as a file with the extension 'csv'.");
 			return;
 		}
 		updateStatus(null);
 	}
 
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 
 	public boolean isOpen() {
 		return open;
 	}
 	
 	protected void getDataSetNames() throws Exception {
 		
 		getContainer().run(true, true, new IRunnableWithProgress() {
 
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				
 				try {
 
 					// Attempt to use meta data, save memory
 					final IMetaData    meta = LoaderFactory.getMetaData(source.getLocation().toOSString(), new ProgressMonitorWrapper(monitor));
 					if (meta != null) {
 						final Collection<String> names = meta.getDataNames();
 						if (names !=null) {
 							setDataNames(names.toArray(new String[names.size()]), meta, null);
 							return;
 						}
 					}
 
 					DataHolder holder = LoaderFactory.getData(source.getLocation().toOSString(), new ProgressMonitorWrapper(monitor));
 					final List<String> names = new ArrayList<String>(holder.getMap().keySet());
 					Collections.sort(names);
 					setDataNames(names.toArray(new String[names.size()]), null, holder);
 					return;
 
 				} catch (Exception ne) {
 					throw new InvocationTargetException(ne);
 				}
 
 			}
 		});
 	}
 
 	protected void setDataNames(String[] array, final IMetaData imeta, final DataHolder holder) {
 		dataSetNames = array;
 		this.imeta   = imeta;
 		this.holder  = holder;
 		getContainer().getShell().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				checkboxTableViewer.getTable().setEnabled(true);
 				checkboxTableViewer.refresh();
 				checkboxTableViewer.setAllChecked(false);
 				checkboxTableViewer.setAllGrayed(false);
 				setAll1DChecked();
 			}
 		});
 	}
 	
 	protected void setAll1DChecked() {
 		for (String name : dataSetNames) {
 			int rank=-1;
 			if (imeta!=null) {
 				rank = imeta.getDataShapes()!=null && imeta.getDataShapes().get(name)!=null
 				     ? imeta.getDataShapes().get(name).length
 				     : -1;
 			}
 			if (rank<0 && holder!=null) {
 				final ILazyDataset ld = holder.getLazyDataset(name);
 				rank = ld!=null ? ld.getRank() : -1;
 			}
 			
 			if (rank==1) {
 				checkboxTableViewer.setChecked(name, true);
 			}
 		}		
 	}
 
 	protected String[] getSelected() {
 		Object[] elements = checkboxTableViewer.getCheckedElements();
 		final String[] ret= new String[elements.length];
 		for (int i = 0; i < elements.length; i++) {
 			ret[i]= elements[i]!=null ? elements[i].toString() : null;
 		}
 		return ret;
 	}
 
 	public boolean isOverwrite() {
 		return overwrite;
 	}
 
 }
