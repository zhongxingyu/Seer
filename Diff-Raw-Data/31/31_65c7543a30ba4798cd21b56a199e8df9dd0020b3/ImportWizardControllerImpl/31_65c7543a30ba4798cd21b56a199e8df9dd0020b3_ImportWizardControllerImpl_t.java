 package org.cotrix.web.importwizard.client;
 
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.event.CodeListSelectedEvent;
 import org.cotrix.web.importwizard.client.event.CodeListSelectedEvent.CodeListSelectedHandler;
 import org.cotrix.web.importwizard.client.event.CodeListTypeUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.CsvParserConfigurationEditedEvent;
 import org.cotrix.web.importwizard.client.event.CsvParserConfigurationEditedEvent.CsvParserConfigurationEditedHandler;
 import org.cotrix.web.importwizard.client.event.CsvParserConfigurationUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.FileUploadedEvent;
 import org.cotrix.web.importwizard.client.event.ImportProgressEvent;
 import org.cotrix.web.importwizard.client.event.ImportStartedEvent;
 import org.cotrix.web.importwizard.client.event.MappingUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.NewImportEvent;
 import org.cotrix.web.importwizard.client.event.NewImportEvent.NewImportHandler;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent;
 import org.cotrix.web.importwizard.client.event.MappingUpdatedEvent.MappingUpdatedHandler;
 import org.cotrix.web.importwizard.client.event.MetadataUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.MetadataUpdatedEvent.MetadataUpdatedHandler;
 import org.cotrix.web.importwizard.client.event.PreviewDataUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.FileUploadedEvent.FileUploadedHandler;
 import org.cotrix.web.importwizard.client.event.ImportBus;
 import org.cotrix.web.importwizard.client.event.SaveEvent;
 import org.cotrix.web.importwizard.client.event.SaveEvent.SaveHandler;
 import org.cotrix.web.importwizard.client.session.ImportSession;
 import org.cotrix.web.importwizard.client.wizard.event.NavigationEvent;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 import org.cotrix.web.importwizard.shared.CsvParserConfiguration;
 import org.cotrix.web.importwizard.shared.CodeListPreviewData;
 import org.cotrix.web.importwizard.shared.CodeListType;
 import org.cotrix.web.importwizard.shared.ImportMetadata;
 import org.cotrix.web.importwizard.shared.ImportProgress;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ImportWizardControllerImpl implements ImportWizardController {
 	
 	protected EventBus importEventBus;
 	
 	@Inject
 	protected ImportSession session;
 	
 	@Inject
 	protected ImportServiceAsync importService;
 	
 	@Inject
 	protected ImportWizardPresenter importWizardPresenter;
 	
 	protected ImportMetadata metadata;
 	protected List<AttributeMapping> mapping;
 	
 	protected Timer importProgressPolling;
 	
 	@Inject
 	public ImportWizardControllerImpl(@ImportBus EventBus importEventBus)
 	{
 		this.importEventBus = importEventBus;
 		
 		importProgressPolling = new Timer() {
 			
 			@Override
 			public void run() {
 				getImportProgress();
 			}
 		};
 		
 		bind();
 	}
 	
 	protected void bind()
 	{
 		importEventBus.addHandler(FileUploadedEvent.TYPE, new FileUploadedHandler(){
 
 			@Override
 			public void onFileUploaded(FileUploadedEvent event) {
 				importedItemUpdated();
 			}});
 		importEventBus.addHandler(CodeListSelectedEvent.TYPE, new CodeListSelectedHandler(){
 
 			@Override
 			public void onCodeListSelected(CodeListSelectedEvent event) {
 				importedItemUpdated();
 			}});
 		importEventBus.addHandler(CsvParserConfigurationEditedEvent.TYPE, new CsvParserConfigurationEditedHandler(){
 
 			@Override
 			public void onCsvParserConfigurationEdited(CsvParserConfigurationEditedEvent event) {
 				saveCsvParserConfiguration(event.getConfiguration());
 			}});
 		importEventBus.addHandler(MetadataUpdatedEvent.TYPE, new MetadataUpdatedHandler(){
 
 			@Override
 			public void onMetadataUpdated(MetadataUpdatedEvent event) {
 				if (event.isUserEdited()) metadata = event.getMetadata();				
 			}
 		});
 		importEventBus.addHandler(MappingUpdatedEvent.TYPE, new MappingUpdatedHandler() {
 			
 			@Override
 			public void onMappingUpdated(MappingUpdatedEvent event) {
 				if (event.isUserEdit()) mapping = event.getMapping();
 			}
 		});
 		importEventBus.addHandler(SaveEvent.TYPE, new SaveHandler() {
 			
 			@Override
 			public void onSave(SaveEvent event) {
 				startImport();
 			}
 		});
 		importEventBus.addHandler(NewImportEvent.TYPE, new NewImportHandler(){
 
 			@Override
 			public void onNewImport(NewImportEvent event) {
 				newImportRequested();
 			}});
 	}
 	
 	protected void importedItemUpdated()
 	{
 		Log.trace("importedItemUpdated");
 		
 		Log.trace("getting preview data");
 		getPreviewData();
 		
 		Log.trace("getting codelist type");
 		getCodeListType(new Callback<CodeListType, Void>() {
 
 			@Override
 			public void onFailure(Void reason) {
 			}
 
 			@Override
 			public void onSuccess(CodeListType result) {
 				switch (result) {
 					case CSV: getCSVParserConfiguration(); break;
 					default: break;
 				}				
 			}
 		});
 		
 		Log.trace("getting metadata");
 		getMetadata();
 		
 		Log.trace("getting columns");
		getMapping();
 		
 		Log.trace("done importedItemUpdated");
 	}
 	
 	protected void getPreviewData()
 	{
 		importService.getPreviewData(new AsyncCallback<CodeListPreviewData>() {
 			
 			@Override
 			public void onSuccess(CodeListPreviewData previewData) {
 				importEventBus.fireEvent(new PreviewDataUpdatedEvent(previewData));
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Failed retrieving preview data", caught);
 			}
 		});
 	}
 	
 	protected void getCodeListType(final Callback<CodeListType, Void> callaback)
 	{
 		importService.getCodeListType(new AsyncCallback<CodeListType>() {
 			
 			@Override
 			public void onSuccess(CodeListType type) {
 				importEventBus.fireEvent(new CodeListTypeUpdatedEvent(type));
 				callaback.onSuccess(type);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Failed retrieving preview data", caught);
 			}
 		});
 	}
 	
 	protected void getCSVParserConfiguration()
 	{
 		importService.getCsvParserConfiguration(new AsyncCallback<CsvParserConfiguration>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Error retrieving CSV Parser configuration", caught);
 			}
 
 			@Override
 			public void onSuccess(CsvParserConfiguration result) {
 				importEventBus.fireEvent(new CsvParserConfigurationUpdatedEvent(result));				
 			}
 		});
 	}
 	
 	protected void saveCsvParserConfiguration(CsvParserConfiguration configuration) {
 		importService.updateCsvParserConfiguration(configuration, new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Error updating the CSV Parser configuration", caught);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				getCSVParserConfiguration();
 			}
 		});
 	}
 	
 	protected void getMetadata()
 	{
 		importService.getMetadata(new AsyncCallback<ImportMetadata>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Error getting the Metadata", caught);
 				
 			}
 
 			@Override
 			public void onSuccess(ImportMetadata result) {
 				importEventBus.fireEvent(new MetadataUpdatedEvent(result, false));
 				metadata = result;
 			}
 		});
 	}
 	
	protected void getMapping()
 	{
 		importService.getMapping(new AsyncCallback<List<AttributeMapping>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Error getting the columns", caught);
 			}
 
 			@Override
 			public void onSuccess(List<AttributeMapping> result) {
				importEventBus.fireEvent(new MappingUpdatedEvent(result, false));
				mapping = result;
 			}
 		});
 	}
 	
 	protected void startImport()
 	{
 		Log.trace("starting import");
 		importService.startImport(metadata, mapping, new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Error starting the import", caught);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				importEventBus.fireEvent(new ImportStartedEvent());
 				importProgressPolling.scheduleRepeating(1000);
 			}
 			
 		});
 	}
 	
 	protected void getImportProgress()
 	{
 		importService.getImportProgress(new AsyncCallback<ImportProgress>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Error getting the import progress", caught);
 			}
 
 			@Override
 			public void onSuccess(ImportProgress result) {
 				Log.trace("Import progress: "+result);
 				updateImportProgress(result);			
 			}
 		});
 	}
 	
 	protected void updateImportProgress(ImportProgress progress)
 	{
 		if (progress.isComplete()) importProgressPolling.cancel();
 		importEventBus.fireEvent(new ImportProgressEvent(progress));
 		if (progress.isComplete()) importEventBus.fireEvent(NavigationEvent.FORWARD);
 	}
 	
 	protected void newImportRequested()
 	{
 		metadata = null;
 		mapping = null;
 		importProgressPolling.cancel();
 		importEventBus.fireEvent(new ResetWizardEvent());
 	}
 
 	public void go(HasWidgets container) {
 		importWizardPresenter.go(container);
 	}
 }
