 package org.cotrix.web.importwizard.client.step.upload;
 
 import org.cotrix.web.importwizard.client.ImportServiceAsync;
 import org.cotrix.web.importwizard.client.event.FileUploadedEvent;
 import org.cotrix.web.importwizard.client.event.ImportBus;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent.ResetWizardHandler;
import org.cotrix.web.importwizard.client.resources.ImportConstants;
 import org.cotrix.web.importwizard.client.step.AbstractWizardStep;
 import org.cotrix.web.importwizard.client.wizard.NavigationButtonConfiguration;
 import org.cotrix.web.importwizard.shared.FileUploadProgress;
 import org.vectomatic.file.File;
 import org.vectomatic.file.FileList;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class UploadStepPresenterImpl extends AbstractWizardStep implements UploadStepPresenter, ResetWizardHandler {
 
 	protected static final int POLLING_TIME = 500;
 	protected static final int POLLING_ERROR_TRESHOLD = 3;
 	
 	@Inject
 	protected ImportServiceAsync importService;
 	
 	protected EventBus importEventBus;
 	
 	protected Timer progressPolling;
 	protected int pollingErrors = 0;
 	
 	protected boolean complete = false;
 	
 	protected UploadStepView view;
 	protected String fileName;
 
 
 	@Inject
 	public UploadStepPresenterImpl(UploadStepView view, @ImportBus EventBus importEventBus) {
 		super("upload", "Acquire", "Upload it", "Choose a CSV or SDMX file on you computer.", NavigationButtonConfiguration.DEFAULT_BACKWARD, NavigationButtonConfiguration.DEFAULT_FORWARD);
 		this.view = view;
 		this.view.setPresenter(this);
 		
 		progressPolling = new Timer() {
 			
 			@Override
 			public void run() {
 				getUploadProgress();
 			}
 		};
 		
 		this.importEventBus = importEventBus;
 		importEventBus.addHandler(ResetWizardEvent.TYPE, this);
 	}
 		
 	/** 
 	 * {@inheritDoc}
 	 */
 	public void go(HasWidgets container) {
 		container.add(view.asWidget());
 	}
 	
 
 	public void onUploadFileChanged(FileList fileList, String filename) {
 		Log.trace("UploadFileChanged");
 		this.processFiles(fileList);
 	}
 	
 	private void processFiles(FileList files) {
 		Log.trace("processing selected files "+files.getLength());
 		if (files.getLength() == 0)	return;
 
 		File file = files.getItem(0);
 		Log.trace("File name: "+file.getName()+" size: "+file.getSize()+" type: "+file.getType());
 		
 		boolean valid = isValid(file);
 		if (valid) startUpload(file);
 	}
 	
 	protected boolean isValid(File file)
 	{
 		if (file.getSize() == 0) {
 			onError("The file looks empty");
 			return false;
 		}
 		
 		String type = file.getType();
 		if (type == null) {
 			//we will check the type on server side
 			return true;
 		}
 		
 		
		if (contains(ImportConstants.INSTANCE.csvMimeTypes(), type)) return true;
		if (contains(ImportConstants.INSTANCE.xmlMimeTypes(), type)) return true;
 		
 		onError("Only CSV or SDMX for now");
 		return false;
 	}
 	
	protected boolean contains(String[] array, String value)
	{
		for (String arrayValue:array) if (arrayValue.equals(value)) return true;
		return false;
	}
 	protected void startUpload(File file)
 	{
 		complete = false;
 		
 		fileName = file.getName();
 		view.setupUpload(fileName, file.getSize());
 		view.submitForm();
 		
 		//start polling
 		pollingErrors = 0;
 		progressPolling.scheduleRepeating(POLLING_TIME);
 	}
 	
 	protected void getUploadProgress()
 	{
 		importService.getUploadProgress(new AsyncCallback<FileUploadProgress>() {
 			
 			@Override
 			public void onSuccess(FileUploadProgress result) {
 				updateProgress(result);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				pollingErrors++;
 				Log.error("Failed getting upload progress", caught);
 				if (pollingErrors>POLLING_ERROR_TRESHOLD) uploadFailed();
 			}
 		});
 	}
 	
 	protected void updateProgress(FileUploadProgress progress) {
 		Log.trace("updateProgress "+progress);
 		switch (progress.getStatus()) {
 			case ONGOING: uploadOngoing(progress.getProgress()); break;
 			case DONE: uploadDone(progress); break;
 			case FAILED: uploadFailed(); break;
 		}
 	}
 	
 	protected void uploadOngoing(int progress)
 	{
 		view.setUploadProgress(progress);
 	}
 	
 	protected void uploadDone(FileUploadProgress progress)
 	{
 		progressPolling.cancel();
 		view.setUploadProgress(progress.getProgress());
 		complete = true;
 		importEventBus.fireEvent(new FileUploadedEvent(fileName));
 		view.setUploadComplete(progress.getCodeListType().toString());
 	}
 	
 	protected void uploadFailed()
 	{
 		view.setUploadFailed();
 		progressPolling.cancel();
 	}
 	
 	public void onDeleteButtonClicked() {
 		reset();
 	}
 
 	public boolean isComplete() {
 		return complete;
 	}
 	
 	public void onError(String message) {
 		view.alert(message);
 	}
 
 	public void onSubmitComplete(SubmitCompleteEvent event) {
 		Log.trace("Submit complete");
 	}
 
 	@Override
 	public void onRetryButtonClicked() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onResetWizard(ResetWizardEvent event) {
 		reset();
 	}
 	
 	protected void reset()
 	{
 		progressPolling.cancel();
 		view.reset();
 		complete = false;
 		fileName = null;
 	}
 
 }
