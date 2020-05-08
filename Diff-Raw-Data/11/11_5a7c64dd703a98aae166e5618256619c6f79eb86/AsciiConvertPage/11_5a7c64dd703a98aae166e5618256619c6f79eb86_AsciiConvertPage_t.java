 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawnsci.conversion.ui.pages;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.wizard.ResourceChoosePage;
 import org.dawnsci.conversion.converters.AsciiConvert1D;
 import org.dawnsci.conversion.ui.Activator;
 import org.dawnsci.conversion.ui.IConversionWizardPage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Table;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 /**
  *   AsciiConvertPage used if the context is a 1D ascii one.
  *
  *   @author gerring
  *   @date Aug 31, 2010
  *   @project org.edna.workbench.actions
  **/
 public class AsciiConvertPage extends ResourceChoosePage implements IConversionWizardPage {
 	
 	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AsciiConvertPage.class);
 
 	private CheckboxTableViewer checkboxTableViewer;
 	private String[]            dataSetNames;
 	private int                 conversionSelection;
 	private Label               multiFileMessage;
     private Button              overwriteButton;
     private Button              openButton;
     
 	private boolean open      = true;
 	private boolean overwrite = false;
 
 	private IMetaData          imeta;
 	private DataHolder         holder;
 	
 	private final static String[] CONVERT_OPTIONS = new String[] {"Tab Separated Values (*.dat)", 
 		                                                          "Comma Separated Values (*.csv)"};
 
 	/**
 	 * Create the wizard.
 	 */
 	public AsciiConvertPage() {
 		super("wizardPage", "Convert data from synchrotron formats and compressed files to common simple data formats.", null);
 		setTitle("Convert Data");
 		dataSetNames = new String[]{"Loading..."};
 		setDirectory(false);
 		setOverwriteVisible(false);
 		setNewFile(true);
 		setPathEditable(true);
     	setFileLabel("Output file");
     }
 
 	/**
 	 * Create contents of the wizard.
 	 * @param parent
 	 */
 	public void createContentBeforeFileChoose(Composite container) {
 				
 		Label convertLabel = new Label(container, SWT.NONE);
 		convertLabel.setText("Convert to");
 		
 		final Combo combo = new Combo(container, SWT.READ_ONLY|SWT.BORDER);
 		combo.setItems(CONVERT_OPTIONS);
 		combo.setToolTipText("Convert to file type by file extension");
 		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
 		combo.select(0);
 		
 		conversionSelection = 0;
 		combo.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				conversionSelection = combo.getSelectionIndex();
 			}
 		});
 
 	}
 	
 	public void createContentAfterFileChoose(Composite container) {
 	
 		this.multiFileMessage = new Label(container, SWT.WRAP);
 		multiFileMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
 		multiFileMessage.setText("(Directory will contain exported files named after the data file.)");
 		GridUtils.setVisible(multiFileMessage, false);
 		
 		this.overwriteButton = new Button(container, SWT.CHECK);
 		overwriteButton.setText("Overwrite file if it exists.");
 		overwriteButton.setSelection(overwrite);
 		overwriteButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				overwrite = overwriteButton.getSelection();
 				pathChanged();
 			}
 		});
 		overwriteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
 		
 		this.openButton = new Button(container, SWT.CHECK);
 		openButton.setText("Open file after export.");
 		openButton.setSelection(true);
 		openButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				AsciiConvertPage.this.open = openButton.getSelection();
 				pathChanged();
 			}
 		});
 		openButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
 
 		
 		Composite main = new Composite(container, SWT.NONE);
 		main.setLayout(new GridLayout(2, false));
 		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
 		
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
 
 		
 		setPageComplete(false);
 
 	}
 	
     public boolean isPageComplete() {
     	if (context==null) return false;
         return super.isPageComplete();
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
 	
 	
 	private String getExtension() {
 		return conversionSelection==0?"dat":"csv";
 	}
 
 
 	/**
 	 * Ensures that both text fields are set.
 	 */
 	protected void pathChanged() {
 
         final String p = getAbsoluteFilePath();
 		if (p==null || p.length() == 0) {
 			setErrorMessage("Please select a file to export to.");
			setPageComplete(false);
 			return;
 		}
 		final File path = new File(p);
 		if (path.exists() && !path.canWrite()) {
 			setErrorMessage("Please choose another location to export to; this one is read only.");
			setPageComplete(false);
 			return;
 		}
 		if (context.getFilePaths().size()<2) {
 			if (path.exists() && !overwrite) {
 				setErrorMessage("Please confirm overwrite of the file.");
				setPageComplete(false);
 				return;
 			}
 			if (!path.getName().toLowerCase().endsWith("."+getExtension())) {
 				setErrorMessage("Please set the file name to export as a file with the extension '"+getExtension()+"'.");
				setPageComplete(false);
 				return;
 			}
 		} else {
 			if (!path.exists()) {
 				setErrorMessage("Please choose an existing folder to export to.");
				setPageComplete(false);
 				return;
 			}
 		}
 		setErrorMessage(null);
		setPageComplete(true);
 	}
 
 	public boolean isOpen() {
 		return open;
 	}
 	
 	protected void getDataSetNames() throws Exception {
 		
 		getContainer().run(true, true, new IRunnableWithProgress() {
 
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				
 				try {
 
 					final String source = getSourcePath(context);
 					if (source==null || "".equals(source)) return;
 					// Attempt to use meta data, save memory
 					final IMetaData    meta = LoaderFactory.getMetaData(source, new ProgressMonitorWrapper(monitor));
 					if (meta != null) {
 						final Collection<String> names = meta.getDataNames();
 						if (names !=null) {
 							setDataNames(names.toArray(new String[names.size()]), meta, null);
 							return;
 						}
 					}
 
 					DataHolder holder = LoaderFactory.getData(source, new ProgressMonitorWrapper(monitor));
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
 
 	public String[] getSelected() {
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
 
 	protected IConversionContext context;
 	
 	@Override
 	public void setContext(IConversionContext context) {
 		
 		if (context!=null && context.equals(this.context)) return;
 		
 		this.context = context;
 		setErrorMessage(null);
 		if (context==null) { // new context being prepared.
 			this.imeta  = null;
 			this.holder = null;
 	        setPageComplete(false);
 			return;
 		}
 		// We populate the names later using a wizard task.
         try {
 			getDataSetNames();
 		} catch (Exception e) {
 			logger.error("Cannot extract data sets!", e);
 		}
         
 		final File source = new File(getSourcePath(context));
        
         setPageComplete(true);
         
         if (context.getFilePaths().size()>1 || source.isDirectory()) { // Multi
         	setPath(source.getParent());
         	setDirectory(true);
         	setFileLabel("Output folder");
         	GridUtils.setVisible(multiFileMessage, true);
         	this.overwriteButton.setSelection(true);
         	this.overwrite = true;
         	this.overwriteButton.setEnabled(false);
         	this.openButton.setSelection(false);
         	this.open = false;
         	this.openButton.setEnabled(false);
         } else {
         	if (source.isFile()) {
         	    final String strName = source.getName().substring(0, source.getName().indexOf("."))+"."+getExtension();
 	        	setPath((new File(source.getParentFile(), strName)).getAbsolutePath());
 	        	setDirectory(false);
 	        	setFileLabel("Output file");
 	        	GridUtils.setVisible(multiFileMessage, false);
 	        	this.overwriteButton.setEnabled(true);
 	        	this.openButton.setEnabled(true);
         	} 
         }
 
 	}
 	
 	private static final String getFileNameNoExtension(File file) {
 		final String fileName = file.getName();
 		int posExt = fileName.lastIndexOf(".");
 		// No File Extension
 		return posExt == -1 ? fileName : fileName.substring(0, posExt);
 	}
 	
 	
 	@Override
 	public IConversionContext getContext() {
 		if (context==null) return null;
 		final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
 		bean.setConversionType(getExtension());
 		context.setUserObject(bean);
 		context.setOutputPath(getAbsoluteFilePath()); // cvs or dat file.
 		context.setDatasetNames(Arrays.asList(getSelected()));
 		return context;
 	}
 
 	
 	@Override
 	public IWizardPage getNextPage() {
 		return null;
 	}
 
 }
