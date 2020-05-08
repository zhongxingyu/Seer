 package org.dawb.common.ui.wizard;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.ui.Activator;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawnsci.slicing.api.SlicingFactory;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.dawnsci.slicing.api.system.DimsDataList;
 import org.dawnsci.slicing.api.system.ISliceSystem;
 import org.dawnsci.slicing.api.system.RangeMode;
 import org.dawnsci.slicing.api.system.SliceSource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public abstract class AbstractSliceConversionPage extends ResourceChoosePage {
 
 	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractSliceConversionPage.class);
 
 	private static final String LAST_SET_KEY = "org.dawnsci.conversion.ui.pages.lastDataSet";
 	
 	protected CCombo         nameChoice;
 	protected String         datasetName;
 	protected IConversionContext context;
 	protected ISliceSystem   sliceComponent;
 	protected Label          multiFileMessage;
 
 
 	public AbstractSliceConversionPage(String pageName, String description, ImageDescriptor icon) {
 		super(pageName, description, icon);
 	}
 
 	/**
 	 * Create the advanced part of the image convert page.
 	 * @param parent
 	 */
 	protected abstract void createAdvanced(final Composite parent);
 	
 
 
 	@Override
 	public void createContentBeforeFileChoose(Composite container) {
 		
 	
 		Label filler = new Label(container, SWT.NULL);
 		filler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
 
 		
 		Label label = new Label(container, SWT.NULL);
 		label.setLayoutData(new GridData());
 		label.setText("Dataset Name");
 		
 		nameChoice = new CCombo(container, SWT.READ_ONLY|SWT.BORDER);
 		nameChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		nameChoice.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				datasetName = nameChoice.getItem(nameChoice.getSelectionIndex());
 				pathChanged();
 				nameChanged();
 				Activator.getDefault().getPreferenceStore().setValue(LAST_SET_KEY, datasetName);
 			}
 		});
 		nameChoice.addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				datasetName = nameChoice.getText();
 				pathChanged();
 				nameChanged();
 				if (getErrorMessage()==null) {
 					int[] shape = sliceComponent.getData().getLazySet().getShape();
 				    sliceComponent.setLabel("Slices matching '"+datasetName+"', based on '"+sliceComponent.getSliceName()+"'.\nWith the shape "+Arrays.toString(shape));
 				}
 			}
 		});
 		
 		final Button editable = new Button(container, SWT.CHECK);
 		editable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		editable.setToolTipText("Click to enter a regular expression for dataset name.");
 		editable.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				nameChoice.setEditable(editable.getSelection());
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
 		
 		try {
 			this.sliceComponent = SlicingFactory.createSliceSystem("org.dawb.workbench.views.h5GalleryView");
 		} catch (Exception e) {
 			logger.error("Cannot create slice system!", e);
 			return;
 		}
 
 		final Control slicer = sliceComponent.createPartControl(container);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
 		data.minimumHeight=560;
 		slicer.setLayoutData(data);
 		sliceComponent.setVisible(true);
 	    sliceComponent.setRangeMode(RangeMode.MULTI_RANGE);
 		sliceComponent.setSliceActionsEnabled(false);
 		
 		pathChanged();
 
 	}
 	
 	/**
 	 * Checks the path is ok.
 	 */
 	protected void pathChanged() {
 		isPageValid();
 		return;
 	}
 	
 	/**
 	 * 
 	 * @return true if page is valid
 	 */
 	public boolean isPageValid() {
 
 		final String path = getAbsoluteFilePath();
 		if (path == null) {
 			setErrorMessage("Please set an output folder.");
 			return false;
 		}
 		final File output = new File(path);
 		try {
 			if (!output.getParentFile().exists()) {
 				setErrorMessage("The directory "+output.getParent()+" does not exist.");
 				return false;			
 			}
 		} catch (Exception ne) {
 			setErrorMessage(ne.getMessage()); // Not very friendly...
 			return false;			
 		}
 		
 		if (!isDirectory() && overwrite!=null) {
 			boolean isOver = overwrite.getSelection();
 			if (!isOver && !output.isDirectory() && output.exists()) {
 				setErrorMessage("The file '"+output.getName()+"' exists. Check overwrite to replace this file."); // Not very friendly...
 				return false;			
 			}
 		}
		
		// Check that two ranges have not been set.
		if (sliceComponent!=null) {
			final DimsDataList dl = sliceComponent.getDimsDataList();
			if (dl!=null && dl.getRangeCount()>1) {
				setErrorMessage("There is a limitation that only one range may be set, curently."); // Not very friendly...
				return false;			
			}
		}
	
 		setErrorMessage(null);
 		return true;
 	}
 
 	public boolean isPageComplete() {
     	if (context==null) return false;
         return super.isPageComplete();
     }
 
 	public boolean isContextSet() {
 		if (context==null) return false;
 		return true;
 	}
 
     private boolean      isExpression;
 	private DimsDataList defaultDimsList;
 
 	protected void nameChanged() {
 		
 		if (datasetName==null || "".equals(datasetName)) {
 			setErrorMessage("Please choose a dataset name");
 			return;
 		}
 		
 		// Probably should check if regular expression a better way
 		if (datasetName.contains("*") || datasetName.contains("+") || datasetName.contains("?")) {
 			try {
 				Pattern.compile(datasetName);
 				setErrorMessage(null);
 				return;
 			} catch (Exception neOther) {
 				setErrorMessage("The regular expression is invalid '"+datasetName+"'");
 				return;
 			}
 		}
 
 		try {
 			isExpression = true;
 			ILazyDataset lz = getLazyExpression();
 			if (lz!=null) {
 				final SliceSource source = new SliceSource(getExpression().getVariableManager(), lz, datasetName, context.getFilePaths().get(0), isExpression);
 				sliceComponent.setData(source);
 			} else {
 				IDataHolder dh = LoaderFactory.getData(context.getFilePaths().get(0), true, true, new IMonitor.Stub());
 				lz = dh.getLazyDataset(datasetName);
 				isExpression = false;
 				
 				final SliceSource source = new SliceSource(dh, lz, datasetName, context.getFilePaths().get(0), isExpression);
 				sliceComponent.setData(source);
 			}
 			if (lz!=null) {
 				setErrorMessage(null);
   		    } else {
   				setErrorMessage("Cannot read data set '"+datasetName+"'");
 			}
 
 		} catch (Exception ne) {
 			
 			setErrorMessage("Cannot read data set '"+datasetName+"'");
 			logger.error("Cannot get data", ne);
 			return;
 		}
 
 	}
 
 	/**
 	 * Must be called before setContext(...) or defaultDimsList will be null!
 	 * @param dimsList
 	 */
 	public void setDefaltSliceDims(DimsDataList dimsList) {
 		this.defaultDimsList = dimsList;
 	}
 
 
 	private ILazyDataset getLazyExpression() {
 		
         final IExpressionObject object = getExpression();
         if (object==null) return null;
         return object.getLazyDataSet(datasetName, new IMonitor.Stub());
 	}
 	
 	private IExpressionObject getExpression() {
 		if (datasetName!=null && datasetName.endsWith("[Expression]")) {
 			
 			final IExpressionObject object = getExpression(datasetName);
 			return object;
 		}
 		return null;
 	}
 
 
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
         if (context.getDatasetNames()!=null && context.getDatasetNames().size()>0) {
         	final List<String> names = Arrays.asList(nameChoice.getItems());
         	if (names.contains(context.getDatasetNames().get(0))) {
             	datasetName = context.getDatasetNames().get(0);
       		    nameChoice.select(names.indexOf(datasetName));
         		nameChanged();
         	}
         }
         // if lazydataset if provided, get the name from it
         if (context.getLazyDataset() != null) {
         	datasetName = context.getLazyDataset().getName();
         	nameChoice.setItems(new String[] {datasetName});
         	nameChoice.select(0);
         }
 		if (defaultDimsList!=null) {
 			try {
 				sliceComponent.setDimsDataList(defaultDimsList);
 			} catch (Throwable ne) {
 				logger.error("Cannot set dimensional data "+defaultDimsList, ne);
 			}
 		}
       
         setPageComplete(true);
  	}
 	
 	public IConversionContext getContext() {
 		if (context == null) return null;
 		context.setDatasetName(datasetName);
 		context.setOutputPath(getAbsoluteFilePath());
 		context.setExpression(isExpression);
 		final DimsDataList dims = sliceComponent.getDimsDataList();
 		for (DimsData dd : dims.iterable()) {
 			if (dd.isSlice()) {
 				context.addSliceDimension(dd.getDimension(), String.valueOf(dd.getSlice()));
 			} else if (dd.isTextRange()) {
 				context.addSliceDimension(dd.getDimension(), dd.getSliceRange()!=null ? dd.getSliceRange() : "all");
 				try {
 					ILazyDataset lazy = sliceComponent.getData().getLazySet();
 					if (lazy == null) {
 						//check the lazy dataset in the context
 						lazy = context.getLazyDataset();
 					}
 				    context.setWorkSize(lazy.getShape()[dd.getDimension()]);
 				} catch (Exception ne) {
 					logger.error("Cannot set work size!", ne);
 				}
 			}
 		}
 		        
         // Set any lazy dataset which can be an expression.
         ILazyDataset set = getLazyExpression();
         if (set != null) context.setLazyDataset(set);
 
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
