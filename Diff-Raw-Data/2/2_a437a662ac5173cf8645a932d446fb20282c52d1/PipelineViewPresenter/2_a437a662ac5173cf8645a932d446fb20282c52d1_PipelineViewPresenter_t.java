 package org.iplantc.core.pipelines.client.presenter;
 
 import java.util.List;
 import java.util.Map;
 
 import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
 import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineApp;
 import org.iplantc.core.pipelineBuilder.client.json.autobeans.PipelineAppMapping;
 import org.iplantc.core.pipelines.client.dnd.AppsGridDragHandler;
 import org.iplantc.core.pipelines.client.dnd.PipelineBuilderDNDHandler;
 import org.iplantc.core.pipelines.client.dnd.PipelineBuilderDropHandler;
 import org.iplantc.core.pipelines.client.util.PipelineAutoBeanUtil;
 import org.iplantc.core.pipelines.client.views.AppSelectionDialog;
 import org.iplantc.core.pipelines.client.views.PipelineAppMappingView;
 import org.iplantc.core.pipelines.client.views.PipelineAppOrderView;
 import org.iplantc.core.pipelines.client.views.PipelineView;
 import org.iplantc.core.pipelines.client.views.widgets.PipelineViewToolbar;
 import org.iplantc.core.pipelines.client.views.widgets.PipelineViewToolbarImpl;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uiapps.client.Services;
 import org.iplantc.core.uiapps.client.events.AppGroupCountUpdateEvent;
 import org.iplantc.core.uiapps.client.gin.AppsInjector;
 import org.iplantc.core.uiapps.client.models.autobeans.App;
 import org.iplantc.core.uiapps.client.views.AppsView;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.presenter.Presenter;
 
 import com.google.common.base.Strings;
 import com.google.gwt.editor.client.EditorError;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.dnd.core.client.DND.Operation;
 import com.sencha.gxt.dnd.core.client.DropTarget;
 import com.sencha.gxt.dnd.core.client.GridDragSource;
 import com.sencha.gxt.widget.core.client.container.Container;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 
 /**
  * The Presenter for the Pipeline View.
  *
  * @author psarando
  *
  */
 public class PipelineViewPresenter implements Presenter, PipelineView.Presenter,
         PipelineViewToolbar.Presenter, PipelineBuilderDNDHandler.Presenter,
         PipelineAppOrderView.Presenter, PipelineAppMappingView.Presenter, AppSelectionDialog.Presenter {
 
     private final PipelineView view;
     private final PipelineViewToolbar toolbar;
     private AppsView.Presenter appsPresenter;
     private AppSelectionDialog appSelectView;
     private final Command onPublishCallback;
     private final PipelineAutoBeanUtil utils = new PipelineAutoBeanUtil();
 
     public PipelineViewPresenter(PipelineView view, Command onPublishCallback) {
         this.view = view;
         this.onPublishCallback = onPublishCallback;
 
         toolbar = new PipelineViewToolbarImpl();
 
         view.setPresenter(this);
         view.getAppOrderPanel().setPresenter(this);
         view.getMappingPanel().setPresenter(this);
         toolbar.setPresenter(this);
 
         view.setNorthWidget(toolbar);
 
         initAppsView();
 
         // Set an initial Pipeline to edit.
         Pipeline pipeline = utils.getPipelineFactory().pipeline().as();
         setPipeline(pipeline);
     }
 
     private void initAppsView() {
         appSelectView = new AppSelectionDialog();
         appSelectView.setPresenter(this);
 
         appsPresenter = AppsInjector.INSTANCE.getAppsViewPresenter();
 
         initAppsGridDragHandler(appsPresenter.getAppsGrid());
         initPipelineBuilderDropHandler(view.getBuilderDropContainer());
 
         appsPresenter.builder()
                 .hideToolbarButtonCopy()
                 .hideToolbarButtonCreate()
                 .hideToolbarButtonDelete()
                 .hideToolbarButtonEdit()
                 .hideToolbarButtonRequestTool()
                 .hideToolbarButtonSubmit()
                .go(appSelectView);
     }
 
     private void initAppsGridDragHandler(Grid<App> grid) {
         AppsGridDragHandler handler = new AppsGridDragHandler();
         handler.setPresenter(this);
 
         GridDragSource<App> source = new GridDragSource<App>(grid);
         source.addDragStartHandler(handler);
         source.addDragCancelHandler(handler);
     }
 
     private void initPipelineBuilderDropHandler(Container builderPanel) {
         PipelineBuilderDropHandler handler = new PipelineBuilderDropHandler();
         handler.setPresenter(this);
 
         DropTarget target = new DropTarget(builderPanel);
         target.setOperation(Operation.COPY);
         target.addDragEnterHandler(handler);
         target.addDragLeaveHandler(handler);
         target.addDragCancelHandler(handler);
         target.addDropHandler(handler);
     }
 
     @Override
     public void setPipeline(Pipeline pipeline) {
         if (pipeline == null) {
             pipeline = utils.getPipelineFactory().pipeline().as();
         }
         view.setPipeline(pipeline);
     }
 
     @Override
     public void setPipeline(Splittable serviceWorkflowJson) {
         Pipeline pipeline = null;
         if (serviceWorkflowJson != null) {
             pipeline = utils.serviceJsonToPipeline(serviceWorkflowJson);
         }
         setPipeline(pipeline);
     }
 
     @Override
     public void go(HasOneWidget container) {
         container.setWidget(view);
     }
 
     @Override
     public void onPublishClicked() {
         if (view.isValid()) {
             publishPipeline();
         } else {
             markErrors();
         }
     }
 
     private void publishPipeline() {
         toolbar.setPublishButtonEnabled(false);
         view.markInfoBtnValid();
         view.markAppOrderBtnValid();
         view.markMappingBtnValid();
 
         final Pipeline pipeline = getPipeline();
         String publishJson = utils.getPublishJson(pipeline);
         if (publishJson == null) {
             ErrorHandler.post(I18N.ERROR.workflowPublishError());
             toolbar.setPublishButtonEnabled(true);
             return;
         }
 
         Services.USER_APP_SERVICE.publishWorkflow(publishJson, new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
 
                 String newId = utils.parseServiceSaveResponseId(result);
 
                 if (!Strings.isNullOrEmpty(newId) && !newId.equals(pipeline.getId())) {
                     pipeline.setId(newId);
                     loadPipeline(pipeline);
 
                     AppGroupCountUpdateEvent event = new AppGroupCountUpdateEvent(true, null);
                     EventBus.getInstance().fireEvent(event);
                 }
 
                 toolbar.setPublishButtonEnabled(true);
 
                 if (onPublishCallback != null) {
                     onPublishCallback.execute();
                 }
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.ERROR.workflowPublishError(), caught);
                 toolbar.setPublishButtonEnabled(true);
             }
         });
     }
 
     private void markErrors() {
         view.markInfoBtnValid();
         view.markAppOrderBtnValid();
         view.markMappingBtnValid();
 
         List<EditorError> errors = view.getErrors();
         if (errors != null) {
             for (EditorError err : errors) {
                 String path = err.getAbsolutePath();
                 if ("name".equals(path) || "description".equals(path)) { //$NON-NLS-1$ //$NON-NLS-2$
                     view.markInfoBtnInvalid(err.getMessage());
                 } else if (err.getUserData() == view.getAppOrderPanel()) {
                     view.markAppOrderBtnInvalid(err.getMessage());
                 } else if (err.getUserData() == view.getMappingPanel()) {
                     view.markMappingBtnInvalid(err.getMessage());
                 }
             }
         }
     }
 
     @Override
     public void onSwapViewClicked() {
         view.clearInvalid();
 
         IsWidget activeView = view.getActiveView();
         Pipeline pipeline = getPipeline();
 
         if (activeView == view.getStepEditorPanel()) {
             activeView = view.getBuilderPanel();
             appsPresenter.go(view.getAppsContainer());
         } else {
             activeView = view.getStepEditorPanel();
             appsPresenter.go(appSelectView);
         }
 
         view.setActiveView(activeView);
 
         loadPipeline(pipeline);
     }
 
     private void reconfigurePipelineAppMappingView(int startingStep, List<PipelineApp> apps) {
         if (apps != null) {
             for (PipelineApp app : apps) {
                 if (app.getStep() >= startingStep) {
                     utils.resetAppMappings(app);
                 }
             }
         }
 
         view.getMappingPanel().setValue(apps);
     }
 
     @Override
     public Pipeline getPipeline() {
         if (view.getActiveView() == view.getBuilderPanel()) {
             return view.getPipelineCreator().getPipeline();
         }
 
         return view.getPipeline();
     }
 
     private void loadPipeline(Pipeline pipeline) {
         if (pipeline != null) {
             if (view.getActiveView() == view.getBuilderPanel()) {
                 view.getPipelineCreator().loadPipeline(pipeline);
             } else {
                 view.setPipeline(pipeline);
             }
         }
 
     }
 
     @Override
     public void onInfoClick() {
         view.getStepPanel().setActiveWidget(view.getInfoPanel());
         view.getHelpContainer().setHTML(I18N.DISPLAY.infoPnlTip());
     }
 
     @Override
     public void onAppOrderClick() {
         view.getStepPanel().setActiveWidget(view.getAppOrderPanel());
         view.getHelpContainer().setHTML(I18N.DISPLAY.selectOrderPnlTip());
     }
 
     @Override
     public void onMappingClick() {
         view.getStepPanel().setActiveWidget(view.getMappingPanel());
         view.getHelpContainer().setHTML(I18N.DISPLAY.inputsOutputsPnlTip());
     }
 
     @Override
     public void onAddAppsClicked() {
         appSelectView.show();
     }
 
     @Override
     public void onMoveUpClicked() {
         PipelineApp selectedApp = view.getOrderGridSelectedApp();
         if (selectedApp == null) {
             return;
         }
 
         ListStore<PipelineApp> store = view.getPipelineAppStore();
 
         int selectedStep = selectedApp.getStep();
         if (selectedStep > 1) {
             int stepUp = selectedStep - 1;
             PipelineApp prevApp = store.get(stepUp - 1);
             prevApp.setStep(selectedStep);
             selectedApp.setStep(stepUp);
 
             store.update(selectedApp);
             store.update(prevApp);
 
             store.applySort(false);
 
             reconfigurePipelineAppMappingView(stepUp, store.getAll());
         }
     }
 
     @Override
     public void onMoveDownClicked() {
         PipelineApp selectedApp = view.getOrderGridSelectedApp();
         if (selectedApp == null) {
             return;
         }
 
         ListStore<PipelineApp> store = view.getPipelineAppStore();
 
         int selectedStep = selectedApp.getStep();
         if (selectedStep < store.size()) {
             int stepDown = selectedStep + 1;
             PipelineApp nextApp = store.get(stepDown - 1);
             nextApp.setStep(selectedStep);
             selectedApp.setStep(stepDown);
 
             store.update(selectedApp);
             store.update(nextApp);
 
             store.applySort(false);
 
             reconfigurePipelineAppMappingView(stepDown, store.getAll());
         }
     }
 
     @Override
     public void onRemoveAppClicked() {
         PipelineApp selectedApp = view.getOrderGridSelectedApp();
 
         if (selectedApp != null) {
             ListStore<PipelineApp> store = view.getPipelineAppStore();
 
             store.remove(selectedApp);
 
             for (int step = 1; step <= store.size(); step++) {
                 PipelineApp app = store.get(step - 1);
                 app.setStep(step);
                 store.update(app);
             }
 
             reconfigurePipelineAppMappingView(selectedApp.getStep(), store.getAll());
         }
     }
 
     @Override
     public void onAddAppClick() {
         App selectedApp = appsPresenter.getSelectedApp();
         utils.appToPipelineApp(selectedApp, new AsyncCallback<PipelineApp>() {
 
             @Override
             public void onSuccess(PipelineApp result) {
                 if (result != null) {
                     ListStore<PipelineApp> store = view.getPipelineAppStore();
 
                     result.setStep(store.size() + 1);
                     store.add(result);
 
                     appSelectView.updateStatusBar(store.size(), I18N.DISPLAY.lastApp(result.getName()));
 
                     view.getMappingPanel().setValue(store.getAll());
                 }
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ListStore<PipelineApp> store = view.getPipelineAppStore();
                 appSelectView.updateStatusBar(store.size(), caught.getMessage());
             }
         });
 
     }
 
     @Override
     public void addAppToPipeline(final App app) {
         utils.appToPipelineApp(app, new AsyncCallback<PipelineApp>() {
 
             @Override
             public void onSuccess(PipelineApp result) {
                 if (result != null) {
                     view.getPipelineCreator().appendApp(result);
                     unmaskPipelineBuilder();
                 }
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 unmaskPipelineBuilder();
             }
         });
     }
 
     @Override
     public void maskPipelineBuilder(String message) {
         view.getBuilderDropContainer().mask(message);
     }
 
     @Override
     public void unmaskPipelineBuilder() {
         view.getBuilderDropContainer().unmask();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getStepName(PipelineApp app) {
         return utils.getStepName(app);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setInputOutputMapping(PipelineApp targetStep, String targetInputId,
             PipelineApp sourceStep, String sourceOutputId) {
         utils.setInputOutputMapping(targetStep, targetInputId, sourceStep, sourceOutputId);
     }
 
     @Override
     public boolean isMappingValid(PipelineApp targetStep) {
         if (targetStep == null) {
             return false;
         }
 
         // Each app after the first one should have at least one output-to-input mapping.
         if (targetStep.getStep() > 1) {
             List<PipelineAppMapping> mappings = targetStep.getMappings();
             if (mappings == null || mappings.size() < 1) {
                 return false;
             }
 
             for (PipelineAppMapping mapping : mappings) {
                 Map<String, String> map = mapping.getMap();
 
                 if (map == null || map.keySet().isEmpty()) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 }
