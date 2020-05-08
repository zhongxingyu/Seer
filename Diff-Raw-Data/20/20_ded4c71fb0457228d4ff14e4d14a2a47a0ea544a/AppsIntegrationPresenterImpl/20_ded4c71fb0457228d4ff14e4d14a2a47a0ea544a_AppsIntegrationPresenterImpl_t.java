 package org.iplantc.core.uiapps.integration.client.presenter;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.messages.IplantDisplayStrings;
 import org.iplantc.core.resources.client.uiapps.integration.AppIntegrationErrorMessages;
 import org.iplantc.core.resources.client.uiapps.integration.AppIntegrationMessages;
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
 import org.iplantc.core.uiapps.widgets.client.models.Argument;
 import org.iplantc.core.uiapps.widgets.client.models.ArgumentGroup;
 import org.iplantc.core.uiapps.widgets.client.models.ArgumentType;
 import org.iplantc.core.uiapps.widgets.client.models.metadata.DataObject;
 import org.iplantc.core.uiapps.widgets.client.models.util.AppTemplateUtils;
 import org.iplantc.core.uiapps.widgets.client.services.AppTemplateServices;
 import org.iplantc.core.uiapps.widgets.client.view.AppWizardPreviewView;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.errorHandling.models.SimpleServiceError;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.info.ErrorAnnouncementConfig;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.info.SuccessAnnouncementConfig;
 import org.iplantc.core.uicommons.client.views.IsMinimizable;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.ErrorDialog3;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IplantInfoBox;
 import org.iplantc.de.client.UUIDServiceAsync;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.EditorError;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 import com.sencha.gxt.widget.core.client.Component;
 import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
 import com.sencha.gxt.widget.core.client.box.MessageBox;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.form.TextArea;
 
 /**
  * @author jstroot
  *
  */
 public class AppsIntegrationPresenterImpl implements AppsIntegrationView.Presenter {
 
     private final AppsIntegrationView view;
     private AppTemplate appTemplate;
     private final AppTemplateServices atService;
     private final AppIntegrationErrorMessages errorMessages;
     private final AppIntegrationMessages appIntMessages = GWT.create(AppIntegrationMessages.class);
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
     public AppTemplate getAppTemplate() {
         return appTemplate;
     }
 
     private boolean isViewValid() {
         // The flushed AppTemplate is the same object when editing was first started.
         view.flush();
         boolean hasErrors = view.hasErrors();
 
         if (hasErrors) {
             List<EditorError> errors = view.getErrors();
             String description = "";
             for (EditorError err : errors) {
                 description += err.getMessage();
                 if (errors.indexOf(err) < errors.size() - 1) {
                     description += "\n";
                 }
             }
             ErrorDialog3 errDlg = new ErrorDialog3(SafeHtmlUtils.fromString("App Editor contains errors"), description);
             errDlg.show();
             return false;
         }
 
         return true;
 
     }
 
     @Override
     public void onSaveClicked() {
 
         if (!isViewValid()) {
             return;
         }
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
         Splittable split = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(AppTemplateUtils.removeEmptyGroupArguments(appTemplate)));
         TextArea ta = new TextArea();
         ta.setReadOnly(true);
         ta.setValue(JsonUtil.prettyPrint(split.getPayload(), null, 4));
         IPlantDialog dlg = new IPlantDialog();
         dlg.setHeadingText(appIntMessages.previewJSON());
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
         dlg.setHeadingText(appIntMessages.commandLineOrder());
         dlg.setModal(true);
         dlg.setOkButtonText(messages.done());
         dlg.setAutoHide(false);
     
        CommandLineOrderingPanel clop = new CommandLineOrderingPanel(
                getAllTemplateArguments(view.flushRawApp()), this, appIntMessages);
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
         if ((event.getSource() instanceof IsMinimizable) && ((IsMinimizable)event.getSource()).isMinimized()) {
             return;
         }
 
     
         try {
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
                             if (!isViewValid()) {
                                 hide();
                                 return;
                             }
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
         } catch (IllegalStateException e) {
             /*
              * JDS This is expected to occur when 'flush()' is called when 'edit()' was not called first.
              */
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
 
                 IplantAnnouncer.getInstance().schedule(new SuccessAnnouncementConfig(appIntMessages.saveSuccessful()));
             }
     
             @Override
             public void onFailure(Throwable caught) {
                 SimpleServiceError serviceError = AutoBeanCodex.decode(atService.getAppTemplateFactory(), SimpleServiceError.class, caught.getMessage()).as();
                 IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(errorMessages.unableToSave()));
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
         AppTemplate cleaned = AppTemplateUtils.removeEmptyGroupArguments(at);
         atService.cmdLinePreview(cleaned, new AsyncCallback<String>() {
 
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
