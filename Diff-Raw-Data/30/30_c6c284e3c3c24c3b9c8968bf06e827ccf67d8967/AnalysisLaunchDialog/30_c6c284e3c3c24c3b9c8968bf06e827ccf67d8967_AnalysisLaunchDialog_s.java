 package org.iplantc.de.client.views.dialogs;
 
 import org.iplantc.core.client.widgets.BoundedTextField;
 import org.iplantc.core.client.widgets.utils.ComponentValueTable;
 import org.iplantc.core.client.widgets.validator.AnalysisNameValidator;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.AnalysisLaunchedEvent;
 import org.iplantc.de.client.services.AnalysisServiceFacade;
 import org.iplantc.de.client.services.DiskResourceServiceFacade;
 import org.iplantc.de.client.utils.DataUtils;
 import org.iplantc.de.client.utils.WizardExportHelper;
 import org.iplantc.de.client.views.FolderSelector;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Provides the user interface for a user to launch an analysis within the system.
  * 
  * @author amuir
  * 
  */
 public class AnalysisLaunchDialog extends Dialog {
     private final String tagCaller;
     private final ComponentValueTable tblComponentVals;
     private TextField<String> fieldName;
     private TextArea areaDescription;
     private CheckBox chkDebug;
     private final String COLON = ":"; //$NON-NLS-1$
     private FolderSelector folderSelector;
     private static UserInfo uinfo = UserInfo.getInstance();
     private boolean isValidFolder;
     private final String defaultOutputFolder;
 
     /**
      * Constructs an instance of the analysis launch dialog.
      * 
      * @param tagCaller the identifier, or handle, of the calling window
      * @param tblComponentVals the table modeling component values within a widget or wizard
      */
     public AnalysisLaunchDialog(String tagCaller, final ComponentValueTable tblComponentVals,
             String defaultOutputFolder) {
         this.tagCaller = tagCaller;
         this.tblComponentVals = tblComponentVals;
         this.defaultOutputFolder = defaultOutputFolder;
         init();
         compose();
     }
 
     private void updateOkButton() {
         Button btnOk = getButtonById(Dialog.OK);
         btnOk.setEnabled(fieldsValid() && isValidFolder);
     }
 
     private void initNameField() {
         fieldName = new BoundedTextField<String>() {
             @Override
             public void onKeyUp(FieldEvent fe) {
                 updateOkButton();
             }
         };
         fieldName.setMaxLength(75);
         fieldName.setWidth(320);
         fieldName.setId("idAnalysisName"); //$NON-NLS-1$
         fieldName.setValue(I18N.DISPLAY.defaultAnalysisName());
         fieldName.setSelectOnFocus(true);
         fieldName.setAllowBlank(false);
         fieldName.setValidator(new AnalysisNameValidator());
         fieldName.setValidateOnBlur(true);
         fieldName.setFireChangeEventOnSetValue(true);
 
         fieldName.addListener(Events.OnBlur, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(final BaseEvent be) {
                 updateOkButton();
             }
         });
 
        fieldName.addKeyListener(new KeyListener() {
            @Override
            public void componentKeyUp(ComponentEvent event) {
                if (event.getKeyCode() == KeyCodes.KEY_ENTER && fieldsValid()) {
                    // once you got a enter key, listen no more
                    fieldName.removeKeyListener(this);

                    confirmOutputFolder(new Command() {
                        @Override
                        public void execute() {
                            doExport();
                        }
                    });
                }
            }
        });

         setFocusWidget(fieldName);
     }
 
     private void initDebugField() {
         chkDebug = new CheckBox();
         chkDebug.setId("idChkDebug");
         chkDebug.setValue(false);
     }
 
     private VerticalPanel buildNameEntry() {
         VerticalPanel ret = new VerticalPanel();
         ret.setSpacing(5);
 
         ret.add(new LabelField(I18N.DISPLAY.analysisName() + COLON));
 
         initNameField();
 
         ret.add(fieldName);
 
         return ret;
     }
 
     private VerticalPanel buildDestinationFolderEntry() {
         VerticalPanel ret = new VerticalPanel();
         ret.setSpacing(5);
         folderSelector = new FolderSelector(new checkPermissions(), null);
         folderSelector.displayFolderName(defaultOutputFolder);
         folderSelector.setDefaultFolderId(defaultOutputFolder);
         ret.add(new Label(I18N.DISPLAY.selectAnalysisOutputDir("/"
                 + DEProperties.getInstance().getDefaultOutputFolderName())
                 + COLON));
         ret.add(folderSelector.getWidget());
         return ret;
     }
 
     private void initDescriptionArea() {
         areaDescription = new AnalysisDescriptionTextArea() {
             @Override
             public void onKeyUp(FieldEvent fe) {
                 updateOkButton();
             }
         };
         areaDescription.setId("idAnalysisDescription"); //$NON-NLS-1$
         areaDescription.setSize(320, 100);
         areaDescription.setSelectOnFocus(true);
         areaDescription.setMaxLength(255);
     }
 
     private VerticalPanel buildDescriptionEntry() {
         VerticalPanel ret = new VerticalPanel();
         ret.setSpacing(5);
 
         initDescriptionArea();
 
         ret.add(new Label(I18N.DISPLAY.description() + COLON));
         ret.add(areaDescription);
 
         return ret;
     }
 
     private void initButtons() {
         Button btn = getButtonById(Dialog.OK);
 
         btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 if (fieldsValid()) {
                     confirmOutputFolder(new Command() {
                         @Override
                         public void execute() {
                             doExport();
                         }
                     });
                 }
             }
         });
 
         btn = getButtonById(Dialog.CANCEL);
         btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 hide();
             }
         });
     }
 
     /**
      * Warns the user about potentially overwriting data if an output folder other than the default
      * folder is selected. If the user confirms, or if the output folder has been left at the default, a
      * command is run.
      * 
      * @param onConfirm a command to run if the user clicks "yes"
      */
     private void confirmOutputFolder(final Command onConfirm) {
         Folder folder = folderSelector.getSelectedFolder();
         String defaultFolderId = folderSelector.getDefaultFolderId();
 
         // if default folder selected, don't ask
         if (folder == null || defaultFolderId.equals(folder.getId())) {
             onConfirm.execute();
         } else {
             new DiskResourceServiceFacade().getFolderContents(folder.getId(),
                     new AsyncCallback<String>() {
                         @Override
                         public void onSuccess(String result) {
                             // if the folder is empty, don't ask for confirmation
                             if (isFolderEmpty(result)) {
                                 onConfirm.execute();
                             } else {
                                 MessageBox.confirm(I18N.DISPLAY.warning(),
                                         I18N.DISPLAY.confirmOutputFolder(),
                                         new Listener<MessageBoxEvent>() {
                                             @Override
                                             public void handleEvent(MessageBoxEvent be) {
                                                 if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                                     onConfirm.execute();
                                                 }
                                             }
                                         });
                             }
                         }
 
                         @Override
                         public void onFailure(Throwable caught) {
                             ErrorHandler.post(caught);
                         }
                     });
         }
     }
 
     /**
      * Tests if a folder contains files or subfolders.
      * 
      * @param json folder metadata as returned by FolderServiceFacade.getFolderContents()
      * @return true if empty, false if files or subfolders exist
      */
     private boolean isFolderEmpty(String json) {
         JSONObject jsonObj = JsonUtil.getObject(json);
         if (jsonObj == null) {
             return false; // play it safe
         }
 
         if (JsonUtil.getBoolean(jsonObj, "hasSubDirs", false)) { //$NON-NLS-1$
             return false;
         }
 
         JSONArray files = JsonUtil.getArray(jsonObj, "files");
         return (files == null || files.size() <= 0);
     }
 
     private boolean fieldsValid() {
         boolean nameValid = fieldName.isValid();
         // clear the error icon when input changes from invalid to valid
         if (nameValid) {
             fieldName.clearInvalid();
         }
 
         boolean descValid = areaDescription.isValid();
         // clear the error icon when input changes from invalid to valid
         if (descValid) {
             areaDescription.clearInvalid();
         }
 
         return nameValid && descValid;
     }
 
     private void init() {
         String caption = I18N.DISPLAY.launchAnalysis();
         setHeading(caption);
 
         setResizable(false);
         setModal(true);
         setWidth(400);
         setButtons(Dialog.OKCANCEL);
 
         // we begin with default folder
         isValidFolder = true;
 
         initButtons();
     }
 
     private void closeDialog() {
         unmask();
         hide();
     }
 
     private void handleAnalysisLaunchSuccess() {
         // notify the world that we have successfully launched an analysis
         EventBus eventbus = EventBus.getInstance();
 
         AnalysisLaunchedEvent event = new AnalysisLaunchedEvent(tagCaller, fieldName.getValue());
         eventbus.fireEvent(event);
 
         // close dialog now that the analysis has launched
         closeDialog();
 
         MessageBox.info(I18N.DISPLAY.analysisSubmitted(), I18N.DISPLAY.analysisSubmittedMsg(), null);
 
     }
 
     private void handleAnalysisLaunchFailure(Throwable caught) {
         closeDialog();
         ErrorHandler.post(I18N.ERROR.analysisFailedToLaunch(fieldName.getValue()), caught);
     }
 
     private void launchAnalysis(String idWorkspace, String json, AsyncCallback<String> callback) {
         AnalysisServiceFacade facade = new AnalysisServiceFacade();
 
         facade.launchAnalysis(idWorkspace, json, callback);
     }
 
     private AsyncCallback<String> buildAnalysisLaunchCallback() {
         return new AsyncCallback<String>() {
             @Override
             public void onSuccess(String result) {
                 if (result != null) {
                     handleAnalysisLaunchSuccess();
                 } else { // service is currently not reporting failure correctly, says successful
                          // when not
                          // TODO: get service to report failure correctly
                     handleAnalysisLaunchFailure(null);
                 }
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 handleAnalysisLaunchFailure(caught);
             }
         };
     }
 
     private void exportLaunch(String type, String json) {
         String idWorkspace = uinfo.getWorkspaceId();
         mask(I18N.DISPLAY.launchingAnalysis());
 
         // create our callback - this is the same regardless of the analysis type
         AsyncCallback<String> callback = buildAnalysisLaunchCallback();
         launchAnalysis(idWorkspace, json, callback);
 
     }
 
     private void doExport() {
         tblComponentVals.setName(fieldName.getValue());
         tblComponentVals.setDescription(JsonUtil.escapeNewLine(areaDescription.getValue()));
         tblComponentVals.setDebugEnabled(chkDebug.getValue());
         tblComponentVals.setNotifyEnabled(UserSettings.getInstance().isEnableEmailNotification());
 
         String selectedFolderId = (folderSelector.getSelectedFolderId() != null && !folderSelector
                 .getSelectedFolderId().isEmpty()) ? folderSelector.getSelectedFolderId()
                 : folderSelector.getDefaultFolderId();
 
         tblComponentVals.setOutputFolderId(selectedFolderId);
         tblComponentVals
                 .setCreateSubFolder(selectedFolderId.equals(folderSelector.getDefaultFolderId()));
 
         String type = tblComponentVals.getType();
         String json = WizardExportHelper.buildJSON(tblComponentVals, false);
 
         exportLaunch(type, json);
     }
 
     private class checkPermissions implements Command {
         @Override
         public void execute() {
             if (!DataUtils.canUploadToThisFolder(folderSelector.getSelectedFolder())) {
                 MessageBox.alert(I18N.DISPLAY.permissionErrorTitle(),
                         I18N.DISPLAY.permissionErrorMessage(), null);
                 isValidFolder = false;
             } else {
                 isValidFolder = true;
             }
             updateOkButton();
         }
     }
 
     private void compose() {
         VerticalPanel panelOuter = new VerticalPanel();
 
         panelOuter.setSpacing(5);
         panelOuter.add(buildNameEntry());
         panelOuter.add(buildDescriptionEntry());
         panelOuter.add(buildDestinationFolderEntry());
         panelOuter.add(buildDebugEntry());
 
         add(panelOuter);
     }
 
     private HorizontalPanel buildDebugEntry() {
         HorizontalPanel ret = new HorizontalPanel();
         ret.setSpacing(5);
 
         TableData td = new TableData();
         td.setHorizontalAlign(HorizontalAlignment.LEFT);
 
         initDebugField();
 
         ret.add(chkDebug, td);
         ret.add(new Html(I18N.DISPLAY.debug()), td);
 
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
 
         // prevent button bar from receiving focus
         ButtonBar buttonbar = getButtonBar();
         buttonbar.getElement().setTabIndex(-1);
     }
 
     private class AnalysisDescriptionTextArea extends TextArea {
         /**
          * {@inheritDoc}
          */
         @Override
         protected void afterRender() {
             super.afterRender();
             areaDescription.el().setElementAttribute("spellcheck", "false"); //$NON-NLS-1$ //$NON-NLS-2$
         }
     }
 }
