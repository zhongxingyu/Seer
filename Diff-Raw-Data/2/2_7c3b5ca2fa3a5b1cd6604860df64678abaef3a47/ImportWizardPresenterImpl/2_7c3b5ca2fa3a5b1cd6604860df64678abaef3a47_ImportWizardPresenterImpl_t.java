 package org.cotrix.web.importwizard.client;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.cotrix.web.importwizard.client.ImportWizardView.WizardButton;
 import org.cotrix.web.importwizard.client.event.ImportBus;
 import org.cotrix.web.importwizard.client.event.NewImportEvent;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent.ResetWizardHandler;
 import org.cotrix.web.importwizard.client.flow.FlowManager;
 import org.cotrix.web.importwizard.client.flow.FlowUpdatedEvent;
 import org.cotrix.web.importwizard.client.flow.FlowUpdatedEvent.FlowUpdatedHandler;
 import org.cotrix.web.importwizard.client.flow.builder.FlowManagerBuilder;
 import org.cotrix.web.importwizard.client.flow.builder.NodeBuilder.RootNodeBuilder;
 import org.cotrix.web.importwizard.client.flow.builder.NodeBuilder.SingleNodeBuilder;
 import org.cotrix.web.importwizard.client.flow.builder.NodeBuilder.SwitchNodeBuilder;
 import org.cotrix.web.importwizard.client.progresstracker.ProgressTracker.ProgressStep;
 import org.cotrix.web.importwizard.client.step.TaskWizardStep;
 import org.cotrix.web.importwizard.client.step.VisualWizardStep;
 import org.cotrix.web.importwizard.client.step.WizardStep;
 import org.cotrix.web.importwizard.client.step.codelistdetails.CodelistDetailsStepPresenter;
 import org.cotrix.web.importwizard.client.step.csvmapping.CsvMappingStepPresenter;
 import org.cotrix.web.importwizard.client.step.csvpreview.CsvPreviewStepPresenter;
 import org.cotrix.web.importwizard.client.step.done.DoneStepPresenter;
 import org.cotrix.web.importwizard.client.step.repositorydetails.RepositoryDetailsStepPresenter;
 import org.cotrix.web.importwizard.client.step.sdmxmapping.SdmxMappingStepPresenter;
 import org.cotrix.web.importwizard.client.step.selection.SelectionStepPresenter;
 import org.cotrix.web.importwizard.client.step.sourceselection.SourceSelectionStepPresenter;
 import org.cotrix.web.importwizard.client.step.summary.SummaryStepPresenter;
 import org.cotrix.web.importwizard.client.step.upload.UploadStepPresenter;
 import org.cotrix.web.importwizard.client.task.ImportTask;
 import org.cotrix.web.importwizard.client.task.RetrieveAssetTask;
 import org.cotrix.web.importwizard.client.wizard.WizardAction;
 import org.cotrix.web.importwizard.client.wizard.NavigationButtonConfiguration;
 import org.cotrix.web.importwizard.client.wizard.WizardStepConfiguration;
 import org.cotrix.web.importwizard.client.wizard.event.NavigationEvent;
 import org.cotrix.web.importwizard.client.wizard.event.NavigationEvent.NavigationHandler;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.event.shared.LegacyHandlerWrapper;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ImportWizardPresenterImpl implements ImportWizardPresenter, NavigationHandler, FlowUpdatedHandler, ResetWizardHandler, HasValueChangeHandlers<WizardStep> {
 
 	protected FlowManager<WizardStep> flow;
 
 	protected ImportWizardView view;
 
 	protected EventBus importEventBus;
 	
 	protected EnumMap<WizardButton, WizardAction> buttonsActions = new EnumMap<WizardButton, WizardAction>(WizardButton.class);
 	
 	protected VisualWizardStep currentVisualStep;
 	protected WizardAction backwardAction;
 	protected WizardAction forwardAction;
 
 	@Inject
 	public ImportWizardPresenterImpl(@ImportBus final EventBus importEventBus, ImportWizardView view,  
 			SourceSelectionStepPresenter sourceStep,
 			UploadStepPresenter uploadStep,
 			CsvPreviewStepPresenter csvPreviewStep,
 
 			DetailsNodeSelector detailsNodeSelector,
 			SelectionStepPresenter selectionStep,
 			CodelistDetailsStepPresenter codelistDetailsStep,
 			RepositoryDetailsStepPresenter repositoryDetailsStep,
 			
 			RetrieveAssetTask retrieveAssetTask,
 			MappingNodeSelector mappingNodeSelector,
 
 			CsvMappingStepPresenter csvMappingStep,
 			SdmxMappingStepPresenter sdmxMappingStep, 
 			
 			SummaryStepPresenter summaryStep,
 			
 
 			
 			DoneStepPresenter doneStep,
 			SourceNodeSelector selector,
 			
 			//FIXME to register the handler later :(
 			ImportTask importTask
 			) {
 
 		this.importEventBus = importEventBus;
 		this.view = view;
 		this.view.setPresenter(this);
 
 		RootNodeBuilder<WizardStep> root = FlowManagerBuilder.<WizardStep>startFlow(sourceStep);
 		SwitchNodeBuilder<WizardStep> source = root.hasAlternatives(selector);
 
 		SwitchNodeBuilder<WizardStep> upload = source.alternative(uploadStep).hasAlternatives(new TypeNodeSelector(importEventBus, csvPreviewStep, sdmxMappingStep));
 		SingleNodeBuilder<WizardStep> csvPreview = upload.alternative(csvPreviewStep);
 		SingleNodeBuilder<WizardStep> csvMapping = csvPreview.next(csvMappingStep);
 		SingleNodeBuilder<WizardStep> sdmxMapping = upload.alternative(sdmxMappingStep);
 
 		SwitchNodeBuilder<WizardStep> selection = source.alternative(selectionStep).hasAlternatives(detailsNodeSelector);
 		SingleNodeBuilder<WizardStep> codelistDetails = selection.alternative(codelistDetailsStep);
 		SingleNodeBuilder<WizardStep> repositoryDetails = selection.alternative(repositoryDetailsStep);
 		codelistDetails.next(repositoryDetails);
 		
 		SwitchNodeBuilder<WizardStep> retrieveAsset = selection.alternative(retrieveAssetTask).hasAlternatives(mappingNodeSelector);
 		retrieveAsset.alternative(sdmxMapping);
 		retrieveAsset.alternative(csvMapping);
 		
 		SingleNodeBuilder<WizardStep> summary = csvMapping.next(summaryStep);
 		sdmxMapping.next(summary);
 
 		summary.next(importTask).next(doneStep);
 
 		flow = root.build();
 		flow.addFlowUpdatedHandler(this);
 
 		//only for debug
 		/*if (Log.isTraceEnabled()) {
 			String dot = flow.toDot(new LabelProvider<WizardStep>() {
 
 				@Override
 				public String getLabel(WizardStep item) {
 					return item.getId();
 				}
 			});
 			Log.trace("dot: "+dot);
 		}*/
 
 		Log.trace("Adding steps");
 		registerStep(sourceStep);
 		registerStep(uploadStep);
 		registerStep(selectionStep);
 		registerStep(codelistDetailsStep);
 		registerStep(repositoryDetailsStep);
 		registerStep(csvPreviewStep);
 		registerStep(csvMappingStep);
 		registerStep(sdmxMappingStep);
 		registerStep(summaryStep);
 		registerStep(doneStep);
 		Log.trace("done");
 
 		bind();
 	}
 
 	@Override
 	public void onResetWizard(ResetWizardEvent event) {
 		flow.reset();
 		updateTrackerLabels();
 		updateCurrentStep();
 	}
 
 	protected void registerStep(VisualWizardStep step){
 		view.addStep(step);
 	}
 
 	public void bind()
 	{
 		importEventBus.addHandler(NavigationEvent.TYPE, this);
 		importEventBus.addHandler(ResetWizardEvent.TYPE, this);
 	}
 
 	public void go(HasWidgets container) {
 		container.add(view.asWidget());
 		init();
 	}
 
 	protected void init()
 	{
 		Log.trace("Initializing wizard");
 		updateTrackerLabels();
 		updateCurrentStep();
 	}
 
 	protected void updateTrackerLabels()
 	{
 		List<WizardStep> steps = flow.getCurrentFlow();
 		Log.trace("New FLOW: "+steps);
 		
 		List<ProgressStep> psteps = new ArrayList<ProgressStep>();
 		Set<String> saw = new HashSet<String>();
 		for (WizardStep step:steps) {
 			if (step instanceof VisualWizardStep) {
 			ProgressStep pstep = ((VisualWizardStep)step).getConfiguration().getLabel();
 			if (saw.contains(pstep.getId())) continue;
 			psteps.add(pstep);
 			saw.add(pstep.getId());
 			}
 		}
 		Log.trace("Progress steps: "+psteps);
 		
 		view.setLabels(psteps);
 		
 		if (currentVisualStep!=null) view.showLabel(currentVisualStep.getConfiguration().getLabel());
 	}
 
 	protected void updateCurrentStep()
 	{
 		WizardStep currentStep = flow.getCurrentItem();
 		Log.trace("current step "+currentStep.getId());
 		if (currentStep instanceof VisualWizardStep) showStep((VisualWizardStep)currentStep);
 		if (currentStep instanceof TaskWizardStep) runStep((TaskWizardStep)currentStep);
 		ValueChangeEvent.fire(this, currentStep);
 	}
 	
 	protected void showStep(VisualWizardStep step)
 	{
 		currentVisualStep = step;
 		view.showStep(step);
 		view.showLabel(step.getConfiguration().getLabel());
 		WizardStepConfiguration configuration = step.getConfiguration();
 		applyStepConfiguration(configuration);
 	}
 	
 	protected void runStep(final TaskWizardStep step) {
 		showProgress();
 		step.run(new AsyncCallback<WizardAction>() {
 			
 			@Override
 			public void onSuccess(WizardAction result) {
 				doAction(result);
 				hideProgress();
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.trace("TaskWizardStep "+step.getId()+" failed", caught);
 				hideProgress();
 			}
 		});
 	}
 	
 	protected void showProgress()
 	{
 		view.showProgress();
 	}
 	
 	protected void hideProgress()
 	{
 		view.hideProgress();
 	}
 
 	protected void applyStepConfiguration(WizardStepConfiguration configuration)
 	{
 		String title = configuration.getTitle();
 		view.setStepTitle(title);
 		view.setStepSubtitle(configuration.getSubtitle());
 
 		configureButtons(configuration.getButtons());
 	}
 	
 	protected void configureButtons(NavigationButtonConfiguration ... buttons)
 	{
 		view.hideAllButtons();
 		buttonsActions.clear();
 		
 		if (buttons!=null) for (NavigationButtonConfiguration button:buttons) configureButton(button);
 		
 	}
 
 	protected void configureButton(NavigationButtonConfiguration button)
 	{
 		WizardButton wizardButton = button.getWizardButton();
 		view.showButton(wizardButton);
 		buttonsActions.put(wizardButton, button.getAction());
 	}
 
 	protected void goForward()
 	{
 		boolean isComplete = flow.getCurrentItem().leave();
 		if (!isComplete) return;
 
 		if (flow.isLast())
 			throw new IllegalStateException("There are no more steps");
 
 		flow.goNext();
 		updateCurrentStep();
 	}
 
 	protected void goBack()
 	{
 		if (flow.isFirst())
 			throw new IllegalStateException("We are already in the first step");
 
 		goBackToFirstVisual();
 		updateCurrentStep();
 	}
 	
 	protected void goBackToFirstVisual()
 	{
 		do {
 			flow.goBack();
 		} while (!flow.isFirst() && !(flow.getCurrentItem() instanceof VisualWizardStep));
 	}
 	
 	protected void doAction(WizardAction action)
 	{
 		switch (action) {
 			case BACK: goBack(); break;
 			case NEXT: goForward(); break;
 			case MANAGE: {
 				//TODO
 			} break;
 			case NEW_IMPORT: {
 				importEventBus.fireEvent(new NewImportEvent());
 			} break;
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * @param event
 	 */
 	@Override
 	public void onNavigation(NavigationEvent event) {
 		Log.trace("onNavigation "+event.getNavigationType());
 		switch (event.getNavigationType()) {
 			case BACKWARD: goBack(); break;
 			case FORWARD: goForward(); break;
 		}		
 	}
 
 	@Override
 	public void onFlowUpdated(FlowUpdatedEvent event) {
 		updateTrackerLabels();
 	}
 
 	@Override
 	public void fireEvent(GwtEvent<?> event) {
 		importEventBus.fireEvent(event);		
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<WizardStep> handler) {
 		return new LegacyHandlerWrapper(importEventBus.addHandler(ValueChangeEvent.getType(), handler));
 	}
 
 	@Override
 	public void onButtonClicked(WizardButton button) {
 		WizardAction action = buttonsActions.get(button);
 		if (action == null) {
 			Log.fatal("Action not found for clicked button "+button);
 			throw new IllegalArgumentException("Action not found for clicked button "+button);
 		}
 		doAction(action);
 	}
 }
