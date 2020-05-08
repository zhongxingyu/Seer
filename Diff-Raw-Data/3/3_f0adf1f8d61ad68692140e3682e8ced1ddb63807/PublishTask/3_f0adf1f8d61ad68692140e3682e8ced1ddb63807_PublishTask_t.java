 /**
  * 
  */
 package org.cotrix.web.publish.client.wizard.task;
 
 import java.util.List;
 
 import org.cotrix.web.common.client.error.ManagedFailureCallback;
 import org.cotrix.web.common.shared.CsvConfiguration;
 import org.cotrix.web.common.shared.Progress;
 import org.cotrix.web.common.shared.Progress.Status;
 import org.cotrix.web.common.shared.codelist.UICodelist;
 import org.cotrix.web.common.shared.codelist.UIQName;
 import org.cotrix.web.publish.client.PublishServiceAsync;
 import org.cotrix.web.publish.client.event.ItemSelectedEvent;
 import org.cotrix.web.publish.client.event.ItemUpdatedEvent;
 import org.cotrix.web.publish.client.event.MappingsUpdatedEvent;
 import org.cotrix.web.publish.client.event.PublishBus;
 import org.cotrix.web.publish.client.event.PublishCompleteEvent;
 import org.cotrix.web.publish.client.wizard.PublishWizardAction;
 import org.cotrix.web.publish.shared.AttributeMapping;
 import org.cotrix.web.publish.shared.Destination;
 import org.cotrix.web.publish.shared.DownloadType;
 import org.cotrix.web.publish.shared.Format;
 import org.cotrix.web.publish.shared.MappingMode;
 import org.cotrix.web.publish.shared.PublishDirectives;
 import org.cotrix.web.publish.shared.PublishMetadata;
 import org.cotrix.web.publish.shared.UIRepository;
 import org.cotrix.web.wizard.client.WizardAction;
 import org.cotrix.web.wizard.client.step.TaskWizardStep;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class PublishTask implements TaskWizardStep {
 	
 	protected static final String DOWNLOAD_URL = GWT.getModuleBaseURL()+"service/publishDownload?"+DownloadType.PARAMETER_NAME+"="+DownloadType.RESULT;
 	
 	@Inject
 	protected PublishServiceAsync service;
 	protected EventBus publishBus;
 	protected TaskCallBack callback;
 	
 	protected UICodelist codelist;
 	protected Destination destination;
 	protected Format format;
 	protected List<AttributeMapping> mappings;
 	protected CsvConfiguration csvConfiguration;
 	protected UIQName repositoryId;
 	protected MappingMode mappingMode;
 	protected PublishMetadata metadata;
 	
 	protected Timer publishProgressPolling;
 	
 	@Inject
 	public PublishTask(@PublishBus EventBus publishBus)
 	{
 		this.publishBus = publishBus;
 		
 		publishProgressPolling = new Timer() {
 
 			@Override
 			public void run() {
 				getPublishProgress();
 			}
 		};
 		
 		bind();
 	}
 	
 	protected void bind()
 	{
 		publishBus.addHandler(ItemSelectedEvent.getType(UICodelist.class), new ItemSelectedEvent.ItemSelectedHandler<UICodelist>() {
 
 			@Override
 			public void onItemSelected(ItemSelectedEvent<UICodelist> event) {
 				codelist = event.getItem();
 			}
 		});
 		
 		publishBus.addHandler(ItemUpdatedEvent.getType(Destination.class), new ItemUpdatedEvent.ItemUpdatedHandler<Destination>() {
 
 			@Override
 			public void onItemUpdated(ItemUpdatedEvent<Destination> event) {
 				destination = event.getItem();
 			}
 		});
 		
 		publishBus.addHandler(ItemUpdatedEvent.getType(Format.class), new ItemUpdatedEvent.ItemUpdatedHandler<Format>() {
 
 			@Override
 			public void onItemUpdated(ItemUpdatedEvent<Format> event) {
 				format = event.getItem();
 			}
 		});
 		
 		publishBus.addHandler(ItemUpdatedEvent.getType(CsvConfiguration.class), new ItemUpdatedEvent.ItemUpdatedHandler<CsvConfiguration>() {
 
 			@Override
 			public void onItemUpdated(ItemUpdatedEvent<CsvConfiguration> event) {
 				csvConfiguration = event.getItem();
 			}
 		});
 		
 		publishBus.addHandler(ItemSelectedEvent.getType(UIRepository.class), new ItemSelectedEvent.ItemSelectedHandler<UIRepository>() {
 
 			@Override
 			public void onItemSelected(ItemSelectedEvent<UIRepository> event) {
 				repositoryId = event.getItem().getId();
 			}
 		});
 		
 		publishBus.addHandler(ItemUpdatedEvent.getType(PublishMetadata.class), new ItemUpdatedEvent.ItemUpdatedHandler<PublishMetadata>() {
 
 			@Override
 			public void onItemUpdated(ItemUpdatedEvent<PublishMetadata> event) {
 				metadata = event.getItem();
 			}
 		});
 		
 		publishBus.addHandler(ItemUpdatedEvent.getType(MappingMode.class), new ItemUpdatedEvent.ItemUpdatedHandler<MappingMode>() {
 
 			@Override
 			public void onItemUpdated(ItemUpdatedEvent<MappingMode> event) {
 				mappingMode = event.getItem();
 			}
 		});
 		
 		publishBus.addHandler(MappingsUpdatedEvent.TYPE, new MappingsUpdatedEvent.MappingsUpdatedHandler() {
 			
 			@Override
 			public void onMappingUpdated(MappingsUpdatedEvent event) {
 				mappings = event.getMappings();
 			}
 		});
 	}
 	
 
 	@Override
 	public String getId() {
 		return "PublishTask";
 	}
 
 	@Override
 	public boolean leave() {
 		return true;
 	}
 
 	@Override
 	public void run(TaskCallBack callback) {
 		this.callback = callback;
 		PublishDirectives directives = new PublishDirectives();
 		directives.setCodelistId(codelist.getId());
 		directives.setFormat(format);
 		directives.setDestination(destination);
 		directives.setCsvConfiguration(csvConfiguration);
 		directives.setRepositoryId(repositoryId);
 		directives.setMappingMode(mappingMode);
 		directives.setMetadata(metadata);
 		directives.setMappings(mappings);
 		
 		Log.trace("PublishDirectives: "+directives);
 		
 		service.startPublish(directives, new ManagedFailureCallback<Void>() {
 
 			@Override
 			public void onSuccess(Void result) {
 				publishProgressPolling.scheduleRepeating(1000);;
 				
 			}
 		});
 	}
 	
 	protected void getPublishProgress()
 	{
 		service.getPublishProgress(new ManagedFailureCallback<Progress>() {
 
 			@Override
 			public void onSuccess(Progress result) {
 				Log.trace("Import progress: "+result);
 				if (result.isComplete()) publishComplete(result);
 				if (destination==Destination.FILE && result.getStatus()==Status.DONE) startDownload();
 			}
 		});
 	}
 
 	protected void reset() {
 		callback = null;
 		codelist = null;
 		format = null;
 		destination = null;
 		mappings = null;
 	}
 	
 	protected void publishComplete(Progress progress) {
 		publishProgressPolling.cancel();
 		publishBus.fireEvent(new PublishCompleteEvent(progress));
		if (progress.getStatus() == Status.DONE) callback.onSuccess(PublishWizardAction.NEXT);
		else callback.onFailure(progress.getFailureCause());
 	}
 	
 	protected void startDownload() {
 		String url = DOWNLOAD_URL;
 		if (format!=null) url += "&" + Format.PARAMETER_NAME+"="+format;
 		
 		Window.open(url, "myWindow", "");
 	}
 
 	@Override
 	public boolean isComplete() {
 		return false;
 	}
 
 	@Override
 	public WizardAction getAction() {
 		return null;
 	}
 
 }
