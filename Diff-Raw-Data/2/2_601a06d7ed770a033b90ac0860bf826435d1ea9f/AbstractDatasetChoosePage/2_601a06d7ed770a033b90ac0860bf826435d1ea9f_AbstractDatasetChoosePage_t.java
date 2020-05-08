 /*
  * Copyright (c) 2012 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawnsci.conversion.ui.pages;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.wizard.ResourceChoosePage;
 import org.dawnsci.conversion.ui.Activator;
 import org.dawnsci.conversion.ui.IConversionWizardPage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
 import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
 import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
 import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 public abstract class AbstractDatasetChoosePage extends ResourceChoosePage implements IConversionWizardPage{
 	
 	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractDatasetChoosePage.class);
 
 	protected CheckboxTableViewer checkboxTableViewer;
 	protected String[]            dataSetNames;
 	protected Label               multiFileMessage;
 	protected Button              overwriteButton;
 	protected Button              openButton;
     
 	protected boolean open      = true;
 	protected boolean overwrite = false;
 
 	protected IMetadata          imeta;
 	protected IDataHolder        holder;
 	
 
 	protected AbstractDatasetChoosePage(String pageName, String description, ImageDescriptor icon) {
 		super(pageName, description, icon);
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
 				AbstractDatasetChoosePage.this.open = openButton.getSelection();
 				pathChanged();
 			}
 		});
 		openButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
 
 		
 		final Composite main = new Composite(container, SWT.NONE);
 		main.setLayout(new GridLayout(2, false));
 		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
 		GridUtils.removeMargins(main);
 		
 		final Label chooseData = new Label(main, SWT.LEFT);
 		chooseData.setText("Please tick data to export:");
 		
 		final ToolBarManager toolMan = new ToolBarManager(SWT.RIGHT|SWT.FLAT|SWT.WRAP);
         createActions(toolMan);
         toolMan.createControl(main);
         toolMan.getControl().setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
 		
         
 		final Text searchText = new Text(main, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
 		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		searchText.setToolTipText("Search on data set name." );
 		searchText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 
 		this.checkboxTableViewer = CheckboxTableViewer.newCheckList(main, SWT.BORDER | SWT.FULL_SELECTION);
 		Table table = checkboxTableViewer.getTable();
 		table.setToolTipText(getDataTableTooltipText());
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
 
 		final DatasetTableFilter setFilter = new DatasetTableFilter();
 		checkboxTableViewer.addFilter(setFilter);
 		searchText.addModifyListener(new ModifyListener() {		
 			@Override
 			public void modifyText(ModifyEvent e) {
 				if (main.isDisposed()) return;
 				setFilter.setSearchText(searchText.getText());
 				checkboxTableViewer.refresh();
 			}
 		});
 		
 		setPageComplete(false);
 
 	}
 	
     protected abstract String getDataTableTooltipText();
 
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
 	
 
 	protected abstract String getExtension();
 
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
 					IDataHolder holder = LoaderFactory.getData(source, new ProgressMonitorWrapper(monitor));
 					final List<String> names = SliceUtils.getSlicableNames(holder, getMinimumDataSize());
 					setDataNames(names.toArray(new String[names.size()]), null, holder);
 					return;
 
 				} catch (Exception ne) {
 					throw new InvocationTargetException(ne);
 				}
 
 			}
 
 		});
 	}
 
 	protected abstract int getMinimumDataSize();
 
 	protected void setDataNames(String[] array, final IMetadata imeta, final IDataHolder holder) {
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
				rank = ld!=null ? ld.squeeze(true).getRank() : -1;
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
 	public IWizardPage getNextPage() {
 		return null;
 	}
 
 }
