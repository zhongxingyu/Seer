 package org.iplantc.core.uiapps.integration.client.presenter;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.messages.IplantDisplayStrings;
 import org.iplantc.core.resources.client.uiapps.integration.AppIntegrationErrorMessages;
 import org.iplantc.core.uiapps.client.events.AppUpdatedEvent;
 import org.iplantc.core.uiapps.integration.client.dialogs.CommandLineOrderingPanel;
 import org.iplantc.core.uiapps.integration.client.view.AppsIntegrationView;
 import org.iplantc.core.uiapps.widgets.client.events.AppTemplateSelectedEvent;
 import org.iplantc.core.uiapps.widgets.client.events.AppTemplateSelectedEvent.AppTemplateSelectedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.events.AppTemplateUpdatedEvent;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentGroupSelectedEvent;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentGroupSelectedEvent.ArgumentGroupSelectedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentSelectedEvent;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentSelectedEvent.ArgumentSelectedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplate;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplateAutoBeanFactory;
 import org.iplantc.core.uiapps.widgets.client.models.Argument;
 import org.iplantc.core.uiapps.widgets.client.models.ArgumentGroup;
 import org.iplantc.core.uiapps.widgets.client.models.ArgumentType;
 import org.iplantc.core.uiapps.widgets.client.models.metadata.DataObject;
 import org.iplantc.core.uiapps.widgets.client.models.util.AppTemplateUtils;
 import org.iplantc.core.uiapps.widgets.client.presenter.AppWizardPresenterJsonAdapter;
 import org.iplantc.core.uiapps.widgets.client.services.AppTemplateServices;
 import org.iplantc.core.uiapps.widgets.client.view.AppWizardPreviewView;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IplantInfoBox;
 import org.iplantc.de.client.UUIDServiceAsync;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 import com.sencha.gxt.core.client.util.Point;
 import com.sencha.gxt.widget.core.client.Component;
 import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
 import com.sencha.gxt.widget.core.client.box.MessageBox;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.form.TextArea;
 import com.sencha.gxt.widget.core.client.info.DefaultInfoConfig;
 import com.sencha.gxt.widget.core.client.info.Info;
 
 /**
  * @author jstroot
  *
  */
 public class AppsIntegrationPresenterImpl implements AppsIntegrationView.Presenter {
 
     private final AppsIntegrationView view;
     private AppTemplate appTemplate;
     private final AppTemplateServices atService;
     private final AppIntegrationErrorMessages errorMessages;
     private final EventBus eventBus;
     private final IplantDisplayStrings messages;
     private AppTemplate lastSave;
     private HandlerRegistration beforeHideHandlerRegistration;
     private final UUIDServiceAsync uuidService;
     private boolean onlyLabelEditMode = false;
 
     public AppsIntegrationPresenterImpl(final AppsIntegrationView view, final EventBus eventBus, final AppTemplateServices atService, final AppIntegrationErrorMessages errorMessages,
             final IplantDisplayStrings messages, final UUIDServiceAsync uuidService) {
         this.view = view;
         this.eventBus = eventBus;
         this.atService = atService;
         this.errorMessages = errorMessages;
         this.messages = messages;
         this.uuidService = uuidService;
         view.setPresenter(this);
         SelectionHandler handler = new SelectionHandler(view);
         view.addArgumentSelectedEventHandler(handler);
         view.addArgumentGroupSelectedEventHandler(handler);
         view.addAppTemplateSelectedEventHandler(handler);
         view.addAppTemplateUpdatedEventHandler(this);
     }
 
     @Override
     public void goLegacy(HasOneWidget container, Splittable legacyJson) {
         setAppTemplateFromLegacyJson(legacyJson);
         go(container);
     }
 
     @Override
     public void go(HasOneWidget container, AppTemplate appTemplate) {
         this.appTemplate = appTemplate;
         go(container);
     }
 
     @Override
     public void go(HasOneWidget container) {
         view.edit(appTemplate);
         /*
          * JDS Make a copy so we can check for differences on exit.
          * Flushing is necessary before copying because some values don't exist in the JSON when the
          * template is fetched. This will result in a false detection of changes in the BeforeHide
          * handler method
          */
         this.lastSave = AppTemplateUtils.copyAppTemplate(appTemplate);
         updateCommandLinePreview(lastSave);
         container.setWidget(view);
     }
 
     @Override
     public void setAppTemplateFromLegacyJson(Splittable legacyJson) {
         Splittable appTemplateSplit = AppWizardPresenterJsonAdapter.adaptAppTemplateJsonString(legacyJson);
 
         AppTemplateAutoBeanFactory factory = GWT.create(AppTemplateAutoBeanFactory.class);
         AutoBean<AppTemplate> appTemplateAb = AutoBeanCodex.decode(factory, AppTemplate.class, appTemplateSplit);
 
         this.appTemplate = appTemplateAb.as();
     }
 
     @Override
     public AppTemplate getAppTemplate() {
         return appTemplate;
     }
 
     @Override
     public void onSaveClicked() {
         // The flushed AppTemplate is the same object when editing was first started.
         AppTemplate toBeSaved = view.flush();
 
         // Update the AppTemplate's edited and published date.
         Date currentTime = new Date();
         toBeSaved.setEditedDate(currentTime);
         toBeSaved.setPublishedDate(currentTime);
 
         final List<Argument> argNeedUuid = Lists.newArrayList();
         // First loop over AppTemplate and look for UUIDs which need to be applied
         for (ArgumentGroup ag : toBeSaved.getArgumentGroups()) {
             for (Argument arg : ag.getArguments()) {
                 if (Strings.isNullOrEmpty(arg.getId())) {
                     argNeedUuid.add(arg);
                 }
             }
         }
 
         // Check if we have anything which needs a UUID
         if (argNeedUuid.size() > 0) {
             uuidService.getUUIDs(argNeedUuid.size(), new GetUuidCallback(argNeedUuid, toBeSaved));
         } else {
             doSave(toBeSaved);
         }
     }
 
     @Override
     public void onPreviewUiClicked() {
         AppWizardPreviewView preview = new AppWizardPreviewView(view.flush());
         preview.show();
     }
 
     @Override
     public void onPreviewJsonClicked() {
         Splittable split = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(appTemplate));
         TextArea ta = new TextArea();
         ta.setReadOnly(true);
         ta.setValue(JsonUtil.prettyPrint(split.getPayload(), null, 4));
         IPlantDialog dlg = new IPlantDialog();
         dlg.setHeadingText(messages.previewJSON());
         dlg.setPredefinedButtons(PredefinedButton.OK);
         dlg.add(ta);
         dlg.setSize("500", "350");
         dlg.setResizable(true);
 
         dlg.show();
     }
 
     @Override
     public void onArgumentOrderClicked() {
         IPlantDialog dlg = new IPlantDialog();
         dlg.setPredefinedButtons(PredefinedButton.OK);
         dlg.setHeadingText(messages.commandLineOrder());
         dlg.setModal(true);
         dlg.setOkButtonText(messages.done());
         dlg.setAutoHide(false);
     
         CommandLineOrderingPanel clop = new CommandLineOrderingPanel(getAllTemplateArguments(view.flush()), this, messages);
         clop.setSize("640", "480");
         dlg.add(clop);
         dlg.addHideHandler(new HideHandler() {
             @Override
             public void onHide(HideEvent event) {
                 // JDS Tell view to refresh since we may have changed values.
                 view.onAppTemplateChanged();
             }
         });
         dlg.show();
     }
 
     @Override
     public boolean orderingRequired(Argument arg) {
         if (arg == null) {
             return false;
         }
         ArgumentType type = arg.getType();
         if (type.equals(ArgumentType.Info) || type.equals(ArgumentType.EnvironmentVariable)) {
             return false;
         }
     
         DataObject dataObject = arg.getDataObject();
         if ((dataObject != null) && dataObject.isImplicit() && type.equals(ArgumentType.Output)) {
             return false;
         }
         return true;
     }
 
     @Override
     public void onAppTemplateChanged() {
         view.onAppTemplateChanged();
     }
 
     @Override
     public void onAppTemplateUpdate(AppTemplateUpdatedEvent event) {
         updateCommandLinePreview(event.getUpdatedAppTemplate());
     }
 
     @Override
     public void onBeforeHide(final BeforeHideEvent event) {
         // Determine if there are any changes, variables are broken out for readability
         AutoBean<AppTemplate> lastSaveAb = AutoBeanUtils.getAutoBean(lastSave);
         AutoBean<AppTemplate> currentAb = AutoBeanUtils.getAutoBean(AppTemplateUtils.copyAppTemplate(view.flush()));
         String lastSavePayload = AutoBeanCodex.encode(lastSaveAb).getPayload();
         String currentPayload = AutoBeanCodex.encode(currentAb).getPayload();
         boolean areEqual = lastSavePayload.equals(currentPayload);
     
         if (!areEqual) {
             event.setCancelled(true);
             final Component component = event.getSource();
             // JDS There are differences, so prompt user to save.
             final IplantInfoBox dlg = new IplantInfoBox(messages.save(), messages.unsavedChanges()) {
                 @Override
                 protected void onButtonPressed(TextButton button) {
                     if (button == getButtonBar().getItemByItemId(PredefinedButton.YES.name())) {
                         // JDS Do save and let window close
                         beforeHideHandlerRegistration.removeHandler();
                         onSaveClicked();
                         component.hide();
                     } else if (button == getButtonBar().getItemByItemId(PredefinedButton.NO.name())) {
                         // JDS Just let window close
                         beforeHideHandlerRegistration.removeHandler();
                         component.hide();
                     } else if (button == getButtonBar().getItemByItemId(PredefinedButton.CANCEL.name())) {
                         // JDS Do not hide the window
 
                     }
                     hide();
                 }
             };
             dlg.setIcon(MessageBox.ICONS.question());
             dlg.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CANCEL);
             dlg.show();
         }
     }
 
     private void doSave(AppTemplate toBeSaved) {
         // JDS Make a copy so we can check for differences on exit
         lastSave = AppTemplateUtils.copyAppTemplate(toBeSaved);
 
        AsyncCallback<String> saveCallback = new AsyncCallback<String>() {
     
             @Override
             public void onSuccess(String result) {
                 // eventBus.fireEvent(new AppGroupCountUpdateEvent(true, null));
                 view.updateAppTemplateId(result);
                 lastSave = AppTemplateUtils.copyAppTemplate(view.flush());
 
                 eventBus.fireEvent(new AppUpdatedEvent(lastSave));
                 // TODO JDS The user feedback provided by the TempInfoWidget needs to be replaced pending completion of new notification widget (waiting on CORE-4126, CORE-4170)
                 Info infoThing = new TempInfoWidget();
                 infoThing.show(new DefaultInfoConfig("Success", "App Sucessfully Saved"));
             }
     
             @Override
             public void onFailure(Throwable caught) {
                 String failureMsg = errorMessages.publishFailureDefaultMessage();
                 // TODO JDS Notify user of failure via new notification widget (waiting on CORE-4126, CORE-4170)
                 Info infoThing = new TempInfoWidget();
                 infoThing.show(new DefaultInfoConfig("Oops!", failureMsg + "\n" + caught.getMessage()));
                GWT.log(caught.getMessage());
             }
        };

        if (isOnlyLabelEditMode()) {
            atService.updateAppLabels(lastSave, saveCallback);
        } else {
            atService.saveAndPublishAppTemplate(lastSave, saveCallback);
        }
     
     }
 
     private List<Argument> getAllTemplateArguments(AppTemplate at) {
         if (at == null) {
             return Collections.emptyList();
         }
         List<Argument> args = Lists.newArrayList();
         for (ArgumentGroup ag : at.getArgumentGroups()) {
              args.addAll(ag.getArguments());
         }
         return args;
     }
 
     private void updateCommandLinePreview(final AppTemplate at) {
         atService.cmdLinePreview(at, new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 Splittable split = StringQuoter.split(result);
                 String cmdLinePrev = split.get("params").asString();
 
                 /*
                  * JDS If the given AppTemplate has a valid DeployedComponent, prepend the
                  * DeployedComponent name to the command line preview
                  */
                 if ((at.getDeployedComponent() != null) && !Strings.isNullOrEmpty(at.getDeployedComponent().getName())) {
                     cmdLinePrev = at.getDeployedComponent().getName() + " " + cmdLinePrev;
                 }
                 view.setCmdLinePreview(cmdLinePrev);
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 GWT.log("FAILS!!!!");
                 ErrorHandler.post(caught);
             }
         });
     }
 
     private final class TempInfoWidget extends Info {
         @Override
         protected Point position() {
             int left = view.asWidget().getAbsoluteLeft() + view.asWidget().getOffsetWidth() - config.getWidth();
 
             int top = view.asWidget().getAbsoluteTop() + 10;
 
             return new Point(left, top);
         }
     }
 
     private final class GetUuidCallback implements AsyncCallback<List<String>> {
         private final List<Argument> argNeedUuid;
         private final AppTemplate toBeSaved;
 
         private GetUuidCallback(List<Argument> argNeedUuid, AppTemplate toBeSaved) {
             this.argNeedUuid = argNeedUuid;
             this.toBeSaved = toBeSaved;
         }
 
         @Override
         public void onSuccess(List<String> result) {
             if ((result == null) || (result.size() != argNeedUuid.size())) {
                 return;
             }
             // JDS Apply UUIDs
             for (Argument arg : argNeedUuid) {
                 arg.setId(result.remove(0));
             }
             doSave(toBeSaved);
         }
 
         @Override
         public void onFailure(Throwable caught) {
             // TODO JDS Notify user of failure via new notification widget (waiting on CORE-4126, CORE-4170)
             ErrorHandler.post(caught);
         }
     }
 
     /**
      * 
      * @author jstroot
      * 
      */
     private final class SelectionHandler implements ArgumentSelectedEventHandler, ArgumentGroupSelectedEventHandler, AppTemplateSelectedEventHandler {
         private final AppsIntegrationView view;
     
         private SelectionHandler(AppsIntegrationView view) {
             this.view = view;
         }
     
         @Override
         public void onArgumentSelected(ArgumentSelectedEvent event) {
             IsWidget argumentPropertyEditor = event.getArgumentPropertyEditor();
             view.setEastWidget(argumentPropertyEditor);
         }
     
         @Override
         public void onArgumentGroupSelected(ArgumentGroupSelectedEvent event) {
             IsWidget argumentGrpPropertyEditor = event.getArgumentGroupPropertyEditor();
             view.setEastWidget(argumentGrpPropertyEditor);
         }
     
         @Override
         public void onAppTemplateSelected(AppTemplateSelectedEvent event) {
             IsWidget appTemplatePropertyEditor = event.getAppTemplatePropertyEditor();
             if (appTemplatePropertyEditor != null) {
                 view.setEastWidget(appTemplatePropertyEditor);
             }
         }
     }
 
     @Override
     public void setBeforeHideHandlerRegistration(HandlerRegistration hr) {
         this.beforeHideHandlerRegistration = hr;
     }
 
     @Override
     public boolean isOnlyLabelEditMode() {
         return onlyLabelEditMode;
     }
 
     @Override
     public void setOnlyLabelEditMode(boolean onlyLabelEditMode) {
         this.onlyLabelEditMode = onlyLabelEditMode;
         view.setOnlyLabelEditMode(onlyLabelEditMode);
     }
 
 }
