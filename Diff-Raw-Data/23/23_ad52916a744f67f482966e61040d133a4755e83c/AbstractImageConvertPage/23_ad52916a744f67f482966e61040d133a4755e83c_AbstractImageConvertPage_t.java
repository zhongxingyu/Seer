 package org.dawnsci.conversion.ui.pages;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.ui.slicing.DimsData;
 import org.dawb.common.ui.slicing.DimsDataList;
 import org.dawb.common.ui.slicing.SliceComponent;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.wizard.ResourceChoosePage;
 import org.dawnsci.conversion.ui.Activator;
 import org.dawnsci.conversion.ui.IConversionWizardPage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public abstract class AbstractImageConvertPage extends ResourceChoosePage implements IConversionWizardPage {
 
 	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractImageConvertPage.class);
 
 	private static final String LAST_SET_KEY = "org.dawnsci.conversion.ui.pages.lastDataSet";
 	
 	protected CCombo         nameChoice;
 	protected String         datasetName;
 	protected IConversionContext context;
 	protected SliceComponent sliceComponent;
 	protected Label          multiFileMessage;
 
 	public AbstractImageConvertPage(String pageName, String description, ImageDescriptor icon) {
 		super(pageName, description, icon);
 	}
 
 	/**
 	 * Create the advanced part of the image convert page.
 	 * @param parent
 	 */
 	protected abstract void createAdvanced(final Composite parent);
 	
 
 
 	@Override
 	public void createContentBeforeFileChoose(Composite container) {
 		
 	
 		new Label(container, SWT.NULL);
 		new Label(container, SWT.NULL);
 		new Label(container, SWT.NULL);
 		new Label(container, SWT.NULL);
 
 		
 		Label label = new Label(container, SWT.NULL);
 		label.setLayoutData(new GridData());
 		label.setText("Dataset Name");
 		
 		nameChoice = new CCombo(container, SWT.READ_ONLY|SWT.BORDER);
 		nameChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
 		nameChoice.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				datasetName = nameChoice.getItem(nameChoice.getSelectionIndex());
 				pathChanged();
 				nameChanged();
 				Activator.getDefault().getPreferenceStore().setValue(LAST_SET_KEY, datasetName);
 			}
 		});
 		
 	}
 	
 	@Override
 	protected void createContentAfterFileChoose(Composite container) {
 		
 		this.multiFileMessage = new Label(container, SWT.WRAP);
 		multiFileMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
 		multiFileMessage.setText("(Directory will contain exported files named after the data file.)");
 		GridUtils.setVisible(multiFileMessage, false);
 		
 		createAdvanced(container);
 		
 		Label sep = new Label(container, SWT.HORIZONTAL|SWT.SEPARATOR);
 		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
 		
 		this.sliceComponent = new SliceComponent("org.dawb.workbench.views.h5GalleryView");
		sliceComponent.setAxesVisible(false);

 		final Control slicer = sliceComponent.createPartControl(container);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
 		data.minimumHeight=560;
 		slicer.setLayoutData(data);
 		sliceComponent.setVisible(true);
 		sliceComponent.setRangesAllowed(true);
 		sliceComponent.setToolBarEnabled(false);
 		
 		pathChanged();
 
 	}
 	
 	/**
 	 * Checks the path is ok.
 	 */
 	protected void pathChanged() {
 
 		final String path = getAbsoluteFilePath();
 		if (path == null) {
 			setErrorMessage("Please set an output folder.");
 			return;
 		}
 		final File output = new File(path);
 		try {
 			if (!output.getParentFile().exists()) {
 				setErrorMessage("The directory "+output.getParent()+" does not exist.");
 				return;			
 			}
 		} catch (Exception ne) {
 			setErrorMessage(ne.getMessage()); // Not very friendly...
 			return;			
 		}
 	
 		setErrorMessage(null);
 		return;
 	}
 	
     public boolean isPageComplete() {
     	if (context==null) return false;
         return super.isPageComplete();
     }
 
     
 	protected void nameChanged() {
 		try {
 
             ILazyDataset lz = getLazyExpression();				
 			if (lz==null) {
 				DataHolder dh = LoaderFactory.getData(context.getFilePaths().get(0), new IMonitor.Stub());
 				lz = dh.getLazyDataset(datasetName);
 			}
 			sliceComponent.setData(lz, datasetName, context.getFilePaths().get(0));
 
 		} catch (Exception ne) {
 			setErrorMessage("Cannot read data set '"+datasetName+"'");
 			logger.error("Cannot get data", ne);
 		}
 
 	}
 
 	private ILazyDataset getLazyExpression() {
 		if (datasetName!=null && datasetName.endsWith("[Expression]")) {
 			
 			final IExpressionObject object = getExpression(datasetName);
 			if (object==null) return null;
 			return object.getLazyDataSet(datasetName, new IMonitor.Stub());
 		}
 		return null;
 	}
 
 	@Override
 	public void setContext(IConversionContext context) {
 		
 		if (context!=null && context.equals(this.context)) return;
 		
 		this.context = context;
 		setErrorMessage(null);
 		if (context==null) {
 			// Clear any data
 	        setPageComplete(false);
 			return;
 		}
 		// We populate the names later using a wizard task.
         try {
         	getNamesOfSupportedRank();
 		} catch (Exception e) {
 			logger.error("Cannot extract data sets!", e);
 			return;
 		}
         
         setPageComplete(true);
  	}
 	
 	@Override
 	public IConversionContext getContext() {
 		if (context == null) return null;
 		context.setDatasetName(datasetName);
 		context.setOutputPath(getAbsoluteFilePath());
 	
 		final DimsDataList dims = sliceComponent.getDimsDataList();
 		for (DimsData dd : dims.getDimsData()) {
 			if (dd.isSlice()) {
 				context.addSliceDimension(dd.getDimension(), String.valueOf(dd.getSlice()));
 			} else if (dd.isRange()) {
 				context.addSliceDimension(dd.getDimension(), dd.getSliceRange()!=null ? dd.getSliceRange() : "all");				
 			}
 		}
 		        
         // Set any lazy dataset which can be an expression.
         ILazyDataset set = getLazyExpression();
         context.setLazyDataset(set);
 
 		return context;
 	}
 
  	protected void getNamesOfSupportedRank() throws Exception {
 		
 		getContainer().run(true, true, new IRunnableWithProgress() {
 
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				
 				
 				try {
                     final List<String> names = getActiveDatasets(context, monitor);
                     if (names==null || names.isEmpty()) return;
                     
                     Display.getDefault().asyncExec(new Runnable() {
                     	public void run() {
                     		nameChoice.setItems(names.toArray(new String[names.size()]));
                     		final String lastName = Activator.getDefault().getPreferenceStore().getString(LAST_SET_KEY);
                     		
                     		int index = 0;
                     		if (lastName!=null && names.contains(lastName)) {
                     			index = names.indexOf(lastName);
                     		}
                     		
                     		nameChoice.select(index);
                     		datasetName = names.get(index);
                     		nameChanged();
                     	}
                     });
                     
 				} catch (Exception ne) {
 					throw new InvocationTargetException(ne);
 				}
 
 			}
 
 		});
 	}
 	
 	@Override
 	public IWizardPage getNextPage() {
 		return null;
 	}
 
 }
