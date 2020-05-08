 package org.iplantc.core.uiapps.integration.client.presenter;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.resources.client.messages.IplantDisplayStrings;
 import org.iplantc.core.resources.client.messages.IplantErrorStrings;
 import org.iplantc.core.resources.client.uiapps.integration.AppIntegrationErrorMessages;
 import org.iplantc.core.resources.client.uiapps.integration.AppIntegrationMessages;
 import org.iplantc.core.uiapps.client.events.AppPublishedEvent;
 import org.iplantc.core.uiapps.client.events.AppPublishedEvent.AppPublishedEventHandler;
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
 import org.iplantc.core.uiapps.widgets.client.view.AppWizardView.RenameWindowHeaderCommand;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.info.ErrorAnnouncementConfig;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.info.SuccessAnnouncementConfig;
 import org.iplantc.core.uicommons.client.views.IsMinimizable;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IplantInfoBox;
 import org.iplantc.de.client.UUIDServiceAsync;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.shared.HandlerRegistration;
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
 public class AppsIntegrationPresenterImpl implements AppsIntegrationView.Presenter, AppPublishedEventHandler {
 
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
     private RenameWindowHeaderCommand renameCmd;
 
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
         eventBus.addHandler(AppPublishedEvent.TYPE, this);
     }
 
     @Override
     public void go(final HasOneWidget container, final AppTemplate appTemplate) {
         go(container, appTemplate, null);
     }
     @Override
     public void go(final HasOneWidget container, final AppTemplate appTemplate, final RenameWindowHeaderCommand renameCmd) {
         this.renameCmd = renameCmd;
         // If we are editing a new AppTemplate, and the current the current AppTemplate has unsaved changes
         if ((appTemplate != null) && (lastSave != null) && isDirty() && !Strings.nullToEmpty(appTemplate.getId()).equals(Strings.nullToEmpty(lastSave.getId()))) {
 
             // JDS ScheduleDeferred to ensure that the dialog's show() method is called after any parent container's show() method.
             Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
                 @Override
                 public void execute() {
                    if (!isViewValid(false, null)) {
                         // JDS View has changes, but contains errors.
                         new ContainsErrorsOnSwitchDialog(messages, I18N.ERROR, container, appTemplate, renameCmd).show();
                     } else {
                         // JDS There are differences and form is valid, so prompt user to save.
                         new PromptForSaveThenSwitchDialog(messages, container, appTemplate, renameCmd).show();
                     }
 
                 }
             });
         } else {
             this.appTemplate = appTemplate;
             go(container);
             if (renameCmd != null) {
                 renameCmd.execute();                
             }
         }
     }
 
     @Override
     public void go(HasOneWidget container) {
         setOnlyLabelEditMode(appTemplate.isPublic());
         /*
          * JDS Make a copy so we can check for differences on exit.
          */
         this.lastSave = AppTemplateUtils.copyAppTemplate(appTemplate);
 
         view.edit(appTemplate);
         updateCommandLinePreview(lastSave);
         if (container.getWidget() == null) {
             // JDS Only set widget if container has no widget.
             container.setWidget(view);
         }
     }
 
     @Override
     public AppTemplate getAppTemplate() {
         return appTemplate;
     }
 
     private boolean isViewValid() {
        return isViewValid(true, null);
    }

    private boolean isViewValid(boolean showDialog, Component container) {
         view.flush();
        boolean hasErrors = view.hasErrors();

        if (hasErrors) {
            if (showDialog) {
                new ContainsErrorsOnHideDialog(messages, I18N.ERROR, container, beforeHideHandlerRegistration).show();
            }
            return false;
        }

        return true;
     }
 
     @Override
     public void onSaveClicked() {
         if (isViewValid()) {
             doOnSaveClicked(null);
         }
     }
 
     private void doOnSaveClicked(AsyncCallback<Void> onSaveCallback) {
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
             uuidService.getUUIDs(argNeedUuid.size(), new GetUuidThenDoSaveCallback(argNeedUuid, toBeSaved, onSaveCallback));
         } else {
             doSave(toBeSaved, onSaveCallback);
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
         boolean isOutput = ArgumentType.FileOutput.equals(type)
                 || ArgumentType.FolderOutput.equals(type) || ArgumentType.MultiFileOutput.equals(type);
         if (isOutput && (dataObject != null) && dataObject.isImplicit()) {
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
 
     private boolean isDirty() {
         try {
             // Determine if there are any changes, variables are broken out for readability
             AutoBean<AppTemplate> lastSaveAb = AutoBeanUtils.getAutoBean(lastSave);
             AutoBean<AppTemplate> currentAb = AutoBeanUtils.getAutoBean(AppTemplateUtils.copyAppTemplate(view.flush()));
             String lastSavePayload = AutoBeanCodex.encode(lastSaveAb).getPayload();
             String currentPayload = AutoBeanCodex.encode(currentAb).getPayload();
             boolean areEqual = lastSavePayload.equals(currentPayload);
             return !areEqual;
         } catch (IllegalStateException e) {
             /*
              * JDS This is expected to occur when 'flush()' is called when 'edit()' was not called first.
              */
             e.printStackTrace();
             return false;
         }
     }
 
     @Override
     public void onBeforeHide(final BeforeHideEvent event) {
         if ((event.getSource() instanceof IsMinimizable) && ((IsMinimizable)event.getSource()).isMinimized()) {
             return;
         }
         if (isDirty()) {
             event.setCancelled(true);
             final Component component = event.getSource();
            if (isViewValid(true, component)) {
                 // JDS There are differences and form is valid, so prompt user to save.
                 new PromptForSaveDialog(messages, component, beforeHideHandlerRegistration).show();
             }
         }
     }
 
     private void doSave(AppTemplate toBeSaved, final AsyncCallback<Void> onSaveCallback) {
         // JDS Make a copy so we can check for differences on exit
         lastSave = AppTemplateUtils.copyAppTemplate(toBeSaved);
 
         AsyncCallback<String> saveCallback = new AsyncCallback<String>() {
     
             @Override
             public void onSuccess(String result) {
                 view.updateAppTemplateId(result);
                 lastSave = AppTemplateUtils.copyAppTemplate(view.flush());
                 if (renameCmd != null) {
                     renameCmd.setAppTemplate(lastSave);
                     renameCmd.execute();
                 }
                 eventBus.fireEvent(new AppUpdatedEvent(lastSave));
 
                 IplantAnnouncer.getInstance().schedule(new SuccessAnnouncementConfig(appIntMessages.saveSuccessful()));
                 if (onSaveCallback != null) {
                     onSaveCallback.onSuccess(null);
                 }
             }
     
             @Override
             public void onFailure(Throwable caught) {
                 IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(errorMessages.unableToSave()));
                 if (onSaveCallback != null) {
                     onSaveCallback.onFailure(caught);
                 }
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
                 ErrorHandler.post(caught);
             }
         });
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
 
     @Override
     public void onAppPublished(AppPublishedEvent appPublishedEvent) {
         final String appTemplateId = Strings.emptyToNull(appTemplate.getId());
         if (appPublishedEvent.getPublishedApp().getId().equalsIgnoreCase(appTemplateId)) {
             setOnlyLabelEditMode(true);
             view.onAppTemplateChanged();
             if (renameCmd != null) {
                 // KLUDGE: Should not have to manually set public flag.
                 appTemplate.setPublic(true);
                 renameCmd.setAppTemplate(appTemplate);
                 renameCmd.execute();
             }
         }
     }
 
     /**
      * This dialog is used when the user attempts to close the view when the current AppTemplate contains
      * errors
      * 
      * @author jstroot
      * 
      */
     private final class ContainsErrorsOnHideDialog extends IplantInfoBox {
         private final Component component;
         private final HandlerRegistration beforeHideHndlrReg;
 
         private ContainsErrorsOnHideDialog(IplantDisplayStrings messages, IplantErrorStrings errorMessages, Component component, HandlerRegistration beforeHideHndlrReg) {
             super(messages.warning(), errorMessages.appContainsErrorsPromptToContinue());
             this.component = component;
             this.beforeHideHndlrReg = beforeHideHndlrReg;
             setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
             setIcon(MessageBox.ICONS.error());
         }
 
         @Override
         protected void onButtonPressed(TextButton button) {
             if (button == getButtonBar().getItemByItemId(PredefinedButton.YES.name())) {
                 // JDS Abort current AppTemplate and hide.
                 beforeHideHndlrReg.removeHandler();
                 component.hide();
             } else if (button == getButtonBar().getItemByItemId(PredefinedButton.NO.name())) {
                 // Do nothing
             }
             hide();
         }
 
     }
 
     /**
      * This dialog is used when the user is attempting to edit a new AppTemplate, but the existing
      * AppTemplate contains errors.
      * 
      * @author jstroot
      * 
      */
     private final class ContainsErrorsOnSwitchDialog extends IplantInfoBox {
         private final HasOneWidget container;
         private final AppTemplate appTemplate;
         private final RenameWindowHeaderCommand renameCmd;
 
         private ContainsErrorsOnSwitchDialog(IplantDisplayStrings messages, IplantErrorStrings errorMessages, HasOneWidget container, AppTemplate appTemplate, RenameWindowHeaderCommand renameCmd) {
             super(messages.warning(), errorMessages.appContainsErrorsPromptToContinue());
             this.container = container;
             this.appTemplate = appTemplate;
             this.renameCmd = renameCmd;
             setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
             setIcon(MessageBox.ICONS.error());
         }
 
         @Override
         protected void onButtonPressed(TextButton button) {
             if (button == getButtonBar().getItemByItemId(PredefinedButton.YES.name())) {
                 // JDS Abort current AppTemplate and switch.
                 AppsIntegrationPresenterImpl.this.appTemplate = appTemplate;
                 AppsIntegrationPresenterImpl.this.go(container);
                 if (renameCmd != null) {
                     renameCmd.execute();
                 }
             } else if (button == getButtonBar().getItemByItemId(PredefinedButton.NO.name())) {
                 // Do nothing
             }
             hide();
         }
     }
 
     /**
      * This dialog is used when the user is attempting to edit a new AppTemplate, but the current
      * AppTemplate has unsaved changes.
      * 
      * @author jstroot
      * 
      */
     private final class PromptForSaveThenSwitchDialog extends IplantInfoBox {
         private final HasOneWidget container;
         private final AppTemplate appTemplate;
         private final RenameWindowHeaderCommand renameCmd;
 
         private PromptForSaveThenSwitchDialog(IplantDisplayStrings messages, HasOneWidget container, AppTemplate appTemplate, RenameWindowHeaderCommand renameCmd) {
             super(messages.save(), messages.unsavedChanges());
             setIcon(MessageBox.ICONS.question());
             setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CANCEL);
             this.container = container;
             this.appTemplate = appTemplate;
             this.renameCmd = renameCmd;
         }
 
         @Override
         protected void onButtonPressed(TextButton button) {
             if (button == getButtonBar().getItemByItemId(PredefinedButton.YES.name())) {
                 // Perform save, then switch.
                 doOnSaveClicked(new AsyncCallback<Void>() {
 
                     @Override
                     public void onSuccess(Void result) {
                         AppsIntegrationPresenterImpl.this.appTemplate = appTemplate;
                         AppsIntegrationPresenterImpl.this.go(container);
                         if (renameCmd != null) {
                             renameCmd.execute();
                         }
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {/* Do Nothing */}
                 });
             } else if (button == getButtonBar().getItemByItemId(PredefinedButton.NO.name())) {
                 // JDS Abort current changes to AppTemplate and switch.
                 AppsIntegrationPresenterImpl.this.appTemplate = appTemplate;
                 AppsIntegrationPresenterImpl.this.go(container);
                 if (renameCmd != null) {
                     renameCmd.execute();
                 }
             } else if (button == getButtonBar().getItemByItemId(PredefinedButton.CANCEL.name())) {
                 // JDS Keep the current AppTemplate
             }
             hide();
         }
     }
 
     /**
      * This dialog is used when the user attempts to close the view or click "Save" when the current
      * AppTemplate contains unsaved changes.
      * 
      * @author jstroot
      * 
      */
     private final class PromptForSaveDialog extends IplantInfoBox {
         private final Component component;
         private final HandlerRegistration beforeHideHndlrReg;
 
         private PromptForSaveDialog(IplantDisplayStrings messages, Component component, HandlerRegistration beforeHideHndlrReg) {
             super(messages.save(), messages.unsavedChanges());
             setIcon(MessageBox.ICONS.question());
             setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CANCEL);
             this.component = component;
             this.beforeHideHndlrReg = beforeHideHndlrReg;
         }
 
         @Override
         protected void onButtonPressed(TextButton button) {
             if (button == getButtonBar().getItemByItemId(PredefinedButton.YES.name())) {
                 // JDS Do save and let window close
                 beforeHideHndlrReg.removeHandler();
                 doOnSaveClicked(null);
                 component.hide();
             } else if (button == getButtonBar().getItemByItemId(PredefinedButton.NO.name())) {
                 // JDS Just let window close
                 beforeHideHndlrReg.removeHandler();
                 component.hide();
             } else if (button == getButtonBar().getItemByItemId(PredefinedButton.CANCEL.name())) {
                 // JDS Do not hide the window
 
             }
             hide();
         }
     }
 
     /**
      * This callback is used to apply any necessary UUIDs to an AppTemplate's Arguments before
      * updating/saving the AppTemplate.
      * 
      * @author jstroot
      * 
      */
     private final class GetUuidThenDoSaveCallback implements AsyncCallback<List<String>> {
         private final List<Argument> argNeedUuid;
         private final AppTemplate toBeSaved;
         private final AsyncCallback<Void> onSaveCallback;
 
         private GetUuidThenDoSaveCallback(List<Argument> argNeedUuid, AppTemplate toBeSaved, AsyncCallback<Void> onSaveCallback) {
             this.argNeedUuid = argNeedUuid;
             this.toBeSaved = toBeSaved;
             this.onSaveCallback = onSaveCallback;
         }
 
         @Override
         public void onSuccess(List<String> result) {
             if ((result == null) || (result.size() != argNeedUuid.size())) {
                 return;
             }
             // Apply UUIDs
             for (Argument arg : argNeedUuid) {
                 arg.setId(result.remove(0));
             }
             doSave(toBeSaved, onSaveCallback);
         }
 
         @Override
         public void onFailure(Throwable caught) {
             ErrorHandler.post(caught);
         }
     }
 
     /**
      * Handles all selection events from this presenter's view.
      * 
      * @author jstroot
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
             view.setEastWidget(appTemplatePropertyEditor);
         }
     }
 
 }
