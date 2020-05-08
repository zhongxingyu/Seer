 package org.dawnsci.conversion.ui.pages;
 
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.wizard.ResourceChoosePage;
 import org.dawnsci.conversion.ui.Activator;
 import org.dawnsci.conversion.ui.IConversionWizardPage;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 
 
 public class NCDConvertPage extends ResourceChoosePage implements
 		IConversionWizardPage {
 	
 	private static final Logger logger = LoggerFactory.getLogger(NCDConvertPage.class);
 	
 	private CCombo         nameChoice;
 	private String         datasetName;
 	private IConversionContext context;
 	private Label          multiFileMessage;
 	private Label          axisMessage;
 	private Button         axisButton;
 	
 	private static final String QAXISNAME = "/q";
 	private static final String DATANAME = "/data";
 	private static final String LAST_SET_KEY = "org.dawnsci.conversion.ui.pages.lastDataSetNCD";
 
 	public NCDConvertPage(){
 		super("wizardPage", "Page for slicing NCD nxs results into ascii.", null);
 		setTitle("Convert Tomography to Images");
 		setDirectory(true);
 	}
 	
 	
 	@Override
 	public void createContentBeforeFileChoose(Composite container) {
 		
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
 		
 		final File source = new File(getSourcePath(context));
 		setPath(source.getParent()+File.separator+"output");
 		
 		this.multiFileMessage = new Label(container, SWT.WRAP);
 		multiFileMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
 		multiFileMessage.setText("(Directory will contain exported files named after the data file.)");
 		GridUtils.setVisible(multiFileMessage, false);
 		Label sep = new Label(container, SWT.HORIZONTAL|SWT.SEPARATOR);
 		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
 		
 		axisButton = new Button(container, SWT.CHECK);
 		axisButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
 		axisButton.setText("Save axis with data");
 		axisButton.setEnabled(false);
 		
 		axisMessage = new Label(container, SWT.NULL);
 		axisMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
 		axisMessage.setText("");
 		axisMessage.setEnabled(false);
 		
 		pathChanged();
 	}
 	
 
 	@Override
 	public boolean isOpen() {
 		return false;
 	}
 
 	@Override
 	public IConversionContext getContext() {
 		if (context == null) {
 			return null;
 		}
 		context.setDatasetName(datasetName);
 		context.setOutputPath(getAbsoluteFilePath());
 		context.addSliceDimension(0, "all");
 		if (axisButton.getEnabled() && axisButton.getSelection()) {
 			context.setAxisDatasetName(axisMessage.getText());
 		}
 		
 		return context;
 	}
 	
 	private void nameChanged() {
 		String qAxisName = datasetName.replaceAll(DATANAME, QAXISNAME);
 		
 		if (hasDataset(qAxisName)) {
 			axisButton.setEnabled(true);
 			axisButton.setSelection(true);
 			axisMessage.setText(qAxisName);
 			axisMessage.setEnabled(true);
 			axisMessage.getShell().layout(true,true);
 		} else {
 			axisButton.setEnabled(true);
 			axisButton.setSelection(false);
 			axisMessage.setText("");
 			axisMessage.setEnabled(true);
 		}
 	}
 	
 	/**
 	 * Checks the path is ok.
 	 */
 	@Override
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
         	getSupportedNames();
 		} catch (Exception e) {
 			logger.error("Cannot extract data sets!", e);
 			return;
 		}
         
         setPageComplete(true);
 		
 		// We either are directories if we are choosing multiple files or
 		// we are single file output and specifying a single output file.
         if (context.getFilePaths().size() > 1) { // Multi
      		GridUtils.setVisible(multiFileMessage, true);
         } else {
     		GridUtils.setVisible(multiFileMessage, false);
         }
         multiFileMessage.getParent().layout();	
  	}
 	
 	private boolean hasDataset(String datasetName) {
 		
 		final String source = getSourcePath(context);
 		if (source == null || "".equals(source)) {
 			return false;
 		}
 		
 		IDataset ds = null;
 		try {
			ds = LoaderFactory.getDataSet(source, "add", null);
 		} catch (Exception e) {
 			logger.error("Failed to read dataset {}", datasetName, e);
 		}
 		
 		return (ds == null) ? false : true;
 		
 	}
 
 	private void getSupportedNames() throws Exception {
 
 		getContainer().run(true, true, new IRunnableWithProgress() {
 
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 
 
 				try {
 					final List<String> names = getActiveDatasets(context, monitor);
                     if (names==null || names.isEmpty()) {
                     	return;
                     }
                     
                     Iterator<String> iter = names.iterator();
                     
                     while (iter.hasNext()) {
                     	String str = iter.next();
                     	if (!(str.endsWith("/data") && (str.contains("_result") || str.contains("_processing")))) {
                     		iter.remove();
                     	}
                     }
                     
                     if (names.isEmpty()) {
                     	return;
                     }
                     
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
 }
