 package org.cotrix.web.importwizard.client.step.summary;
 
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.TrackerLabels;
 import org.cotrix.web.importwizard.client.event.CodeListSelectedEvent;
 import org.cotrix.web.importwizard.client.event.FileUploadedEvent;
 import org.cotrix.web.importwizard.client.event.ImportBus;
 import org.cotrix.web.importwizard.client.event.MappingLoadedEvent;
 import org.cotrix.web.importwizard.client.event.MappingModeUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.MappingLoadedEvent.MappingLoadedHandler;
 import org.cotrix.web.importwizard.client.event.MappingsUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.CodeListSelectedEvent.CodeListSelectedHandler;
 import org.cotrix.web.importwizard.client.event.FileUploadedEvent.FileUploadedHandler;
 import org.cotrix.web.importwizard.client.event.MappingsUpdatedEvent.MappingsUpdatedHandler;
 import org.cotrix.web.importwizard.client.event.MetadataUpdatedEvent;
 import org.cotrix.web.importwizard.client.event.MetadataUpdatedEvent.MetadataUpdatedHandler;
import org.cotrix.web.importwizard.client.event.ResetWizardEvent;
import org.cotrix.web.importwizard.client.event.ResetWizardEvent.ResetWizardHandler;
 import org.cotrix.web.importwizard.client.step.AbstractVisualWizardStep;
 import org.cotrix.web.importwizard.client.wizard.NavigationButtonConfiguration;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 import org.cotrix.web.importwizard.shared.ImportMetadata;
 import org.cotrix.web.importwizard.shared.MappingMode;
 
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
public class SummaryStepPresenterImpl extends AbstractVisualWizardStep implements SummaryStepPresenter, MetadataUpdatedHandler, MappingLoadedHandler, MappingsUpdatedHandler, FileUploadedHandler, CodeListSelectedHandler, ResetWizardHandler {
 
 	protected SummaryStepView view;
 	protected EventBus importEventBus;
 
 	@Inject
 	public SummaryStepPresenterImpl(SummaryStepView view, @ImportBus EventBus importEventBus) {
 		super("summary", TrackerLabels.SUMMARY, "Recap", "Here's the plan of action, let's do it.", NavigationButtonConfiguration.BACKWARD, NavigationButtonConfiguration.IMPORT);
 		this.view = view;
 		
 		this.importEventBus = importEventBus;
 		importEventBus.addHandler(MetadataUpdatedEvent.TYPE, this);
 		importEventBus.addHandler(MappingsUpdatedEvent.TYPE, this);
 		importEventBus.addHandler(FileUploadedEvent.TYPE, this);
 		importEventBus.addHandler(CodeListSelectedEvent.TYPE, this);
		importEventBus.addHandler(ResetWizardEvent.TYPE, this);
 	}
 	
 	public void go(HasWidgets container) {
 		container.add(view.asWidget());
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean leave() {
 		MappingMode mappingMode = view.getMappingMode();
 		importEventBus.fireEvent(new MappingModeUpdatedEvent(mappingMode));
 		return true;
 	}
 	
 	@Override
 	public void onMappingLoaded(MappingLoadedEvent event) {
 		List<AttributeMapping> attributesMappings = event.getMappings();
 		view.setMapping(attributesMappings);
 	}
 
 	@Override
 	public void onMappingUpdated(MappingsUpdatedEvent event) {
 		List<AttributeMapping> attributesMappings = event.getMappings();
 		view.setMapping(attributesMappings);
 		
 		/*
 		if (attributesMappings.getMappingMode()==null) view.setMappingModeVisible(false);
 		else {
 			view.setMappingMode(attributesMappings.getMappingMode());
 			view.setMappingModeVisible(true);
 		}*/
 	}
 
 	@Override
 	public void onMetadataUpdated(MetadataUpdatedEvent event) {
 		
 		ImportMetadata metadata = event.getMetadata();
 		if (metadata.getOriginalName()==null || metadata.getOriginalName().equals(metadata.getName())) view.setCodelistName(metadata.getName());
 		else view.setCodelistName(metadata.getOriginalName()+" as "+metadata.getName());
 		
 		this.view.setMetadataAttributes(metadata.getAttributes());		
 	}
 
 	@Override
 	public void onFileUploaded(FileUploadedEvent event) {
 		view.setFileName(event.getFileName());
 		view.setFileNameVisible(true);
 	}
 
 	@Override
 	public void onCodeListSelected(CodeListSelectedEvent event) {
 		view.setFileNameVisible(false);
 	}
 
	@Override
	public void onResetWizard(ResetWizardEvent event) {
		view.setMappingMode(MappingMode.STRICT);
	}

 
 }
