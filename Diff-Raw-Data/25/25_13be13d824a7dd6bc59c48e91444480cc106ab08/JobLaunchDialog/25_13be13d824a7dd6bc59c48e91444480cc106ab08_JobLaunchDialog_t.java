 package org.iplantc.de.client.views.dialogs;
 
 import org.iplantc.core.client.widgets.BoundedTextField;
 import org.iplantc.core.client.widgets.utils.ComponentValueTable;
 import org.iplantc.core.client.widgets.validator.JobNameValidator;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.JobLaunchedEvent;
 import org.iplantc.de.client.models.DEProperties;
 import org.iplantc.de.client.services.AnalysisServiceFacade;
 import org.iplantc.de.client.services.FolderCreateCallback;
 import org.iplantc.de.client.services.FolderServiceFacade;
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
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Provides the user interface for a user to launch a job within the system.
  * 
  * @author amuir
  * 
  */
 public class JobLaunchDialog extends Dialog {
     private final String tagCaller;
     private final ComponentValueTable tblComponentVals;
     private TextField<String> fieldName;
     private TextArea areaDescription;
     private CheckBox chkDebug;
     private CheckBox chkNotify;
     private final String COLON = ":"; //$NON-NLS-1$
     private FolderSelector folderSelector;
     private static UserInfo uinfo = UserInfo.getInstance();
     private boolean isValidFolder;
 
     /**
      * Constructs an instance of the job launch dialog.
      * 
      * @param tagCaller the identifier, or handle, of the calling window
      * @param tblComponentVals the table modeling component values within a widget or wizard
      */
     public JobLaunchDialog(String tagCaller, final ComponentValueTable tblComponentVals) {
         this.tagCaller = tagCaller;
         this.tblComponentVals = tblComponentVals;
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
         fieldName.setId("idJobName"); //$NON-NLS-1$
         fieldName.setValue(I18N.DISPLAY.defaultJobName());
         fieldName.setSelectOnFocus(true);
         fieldName.setAllowBlank(false);
         fieldName.setValidator(new JobNameValidator());
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
                     doExport();
                     // once you got a enter key, listen no more
                     fieldName.removeKeyListener(this);
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
 
     private void initNotifyField() {
         chkNotify = new CheckBox();
         chkNotify.setId("idChkNotify");
         chkNotify.setValue(true);
     }
 
     private VerticalPanel buildNameEntry() {
         VerticalPanel ret = new VerticalPanel();
         ret.setSpacing(5);
 
         ret.add(new LabelField(I18N.DISPLAY.jobname() + COLON));
 
         initNameField();
 
         ret.add(fieldName);
 
         return ret;
     }
 
     private VerticalPanel buildDestinationFolderEntry() {
         setDefaultOutputFolder();
         VerticalPanel ret = new VerticalPanel();
         ret.setSpacing(5);
         folderSelector = new FolderSelector(new checkPermissions(), null);
         ret.add(new Label(I18N.DISPLAY.selectJobOutputDir("/"
                 + DEProperties.getInstance().getDefaulyOutputFolderName())
                 + COLON));
         ret.add(folderSelector.getWidget());
         return ret;
     }
 
     private void initDescriptionArea() {
        areaDescription = new JobDescriptionTextArea() {
            @Override
            public void onKeyUp(FieldEvent fe) {
                updateOkButton();
            }
        };
         areaDescription.setId("idJobDescription"); //$NON-NLS-1$
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
                     doExport();
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
         String caption = I18N.DISPLAY.launchJob();
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
 
     private void handleJobLaunchSuccess() {
         // notify the world that we have successfully launched a job
         EventBus eventbus = EventBus.getInstance();
 
         JobLaunchedEvent event = new JobLaunchedEvent(tagCaller, fieldName.getValue());
         eventbus.fireEvent(event);
 
         // close dialog now that job has launched
         closeDialog();
 
         MessageBox.info(I18N.DISPLAY.analysisSubmitted(), I18N.DISPLAY.analysisSubmittedMsg(), null);
 
     }
 
     private void handleJobLaunchFailure(Throwable caught) {
         closeDialog();
         ErrorHandler.post(I18N.ERROR.jobFailedToLaunch(fieldName.getValue()), caught);
     }
 
     private void launchJob(String idWorkspace, String json, AsyncCallback<String> callback) {
         AnalysisServiceFacade facade = new AnalysisServiceFacade();
 
         facade.launchAnalysis(idWorkspace, json, callback);
     }
 
     private AsyncCallback<String> buildJobLaunchCallback() {
         return new AsyncCallback<String>() {
             @Override
             public void onSuccess(String result) {
                 if (result != null) {
                     handleJobLaunchSuccess();
                 } else { // service is currently not reporting failure correctly, says successful
                          // when not
                          // TODO: get service to report failure correctly
                     handleJobLaunchFailure(null);
                 }
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 handleJobLaunchFailure(caught);
             }
         };
     }
 
     private void exportLaunch(String type, String json) {
         String idWorkspace = uinfo.getWorkspaceId();
         mask(I18N.DISPLAY.launchingJob());
 
         // create our callback - this is the same regardless of the job type
         AsyncCallback<String> callback = buildJobLaunchCallback();
         launchJob(idWorkspace, json, callback);
 
     }
 
     private void doExport() {
         tblComponentVals.setName(fieldName.getValue());
         tblComponentVals.setDescription(JsonUtil.escapeNewLine(areaDescription.getValue()));
         tblComponentVals.setDebugEnabled(chkDebug.getValue());
         tblComponentVals.setNotifyEnabled(chkNotify.getValue());
 
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
         panelOuter.add(buildNotifyEntry());
         panelOuter.add(buildDebugEntry());
 
         add(panelOuter);
     }
 
     private HorizontalPanel buildNotifyEntry() {
         HorizontalPanel ret = new HorizontalPanel();
         ret.setSpacing(5);
 
         TableData td = new TableData();
         td.setHorizontalAlign(HorizontalAlignment.LEFT);
 
         initNotifyField();
 
         ret.add(chkNotify, td);
         ret.add(new Html(I18N.DISPLAY.notifyemail()), td);
 
         return ret;
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
 
     private class JobDescriptionTextArea extends TextArea {
         /**
          * {@inheritDoc}
          */
         @Override
         protected void afterRender() {
             super.afterRender();
             areaDescription.el().setElementAttribute("spellcheck", "false"); //$NON-NLS-1$ //$NON-NLS-2$
         }
     }
 
     private void setDefaultOutputFolder() {
         FolderServiceFacade facade = new FolderServiceFacade();
         facade.getHomeFolder(new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
 
             }
 
             @Override
             public void onSuccess(String result) {
                 JSONObject root = null;
                 Folder home = null;
                 JSONObject obj = JSONParser.parseStrict(result).isObject();
                 JSONArray items = JsonUtil.getArray(obj, "roots");
                 if (items != null) {
                     for (int i = 0; i < items.size(); i++) {
                         root = JsonUtil.getObject(items.get(i).toString());
                         if (root != null) {
                             home = new Folder(root);
                             createOutputFolderByDefault(home.getId(), DEProperties.getInstance()
                                     .getDefaulyOutputFolderName());
                             break;
 
                         }
                     }
                 }
             }
         });
     }
 
     protected void createOutputFolderByDefault(String idParentFolder, String name) {
         FolderServiceFacade facade = new FolderServiceFacade();
         facade.createFolder(idParentFolder + "/" + name, new OutputFolderCreateCallback(idParentFolder,
                 name));
     }
 
     private class OutputFolderCreateCallback extends FolderCreateCallback {
 
         private String idParentFolder;
         private String name;
 
         public OutputFolderCreateCallback(String idParentFolder, String name) {
             super(idParentFolder, name);
             this.idParentFolder = idParentFolder;
             this.name = name;
         }
 
         @Override
         public void onSuccess(String result) {
             super.onSuccess(result);
             folderSelector.displayFolderName(idParentFolder + "/" + name);
             folderSelector.setDefaultFolderId(idParentFolder + "/" + name);
         }
 
         @Override
         public void onFailure(Throwable caught) {
             JSONObject jsonError = parseJsonError(caught);
             if (jsonError != null) {
                 String errCode = JsonUtil.getString(jsonError, ERROR_CODE);
 
                 ErrorCode code = ErrorCode.valueOf(errCode);
                 if (!code.equals(ErrorCode.ERR_EXISTS)) {
                     super.onFailure(caught);
                 } else {
                     folderSelector.displayFolderName(idParentFolder + "/" + name);
                     folderSelector.setDefaultFolderId(idParentFolder + "/" + name);
                 }
             }
         }
     }
 }
