 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.views.panels.IPlantDialogPanel;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.AsyncUploadCompleteHandler;
 import org.iplantc.de.client.utils.DataUtils;
 
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.FormEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.Status;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Panel component for uploading files.
  * 
  * @author lenards
  * 
  */
 public class FileUploadDialogPanel extends IPlantDialogPanel {
     private static final int FIELD_WIDTH = 475;
 
     private static final String URL_REGEX = "^(?:ftp|FTP|HTTPS?|https?)://[^/]+/.*[^/ ]$"; //$NON-NLS-1$
 
     public static final String HDN_USER_ID_KEY = "user"; //$NON-NLS-1$
     public static final String HDN_PARENT_ID_KEY = "parentfolderid"; //$NON-NLS-1$
     public static final String FILE_TYPE = "type"; //$NON-NLS-1$
     public static final String URL_FIELD = "url"; //$NON-NLS-1$
 
     private static final int MAX_UPLOADS = 5;
 
     private final FormPanel form;
     private final VerticalPanel pnlLayout;
     private final AsyncUploadCompleteHandler hdlrUpload;
     private final Status fileStatus;
     private final List<FileUpload> fupload;
     private final List<TextArea> urls;
     private final String destFolder;
     private final MODE mode;
 
     public static enum MODE {
         URL_ONLY, FILE_AND_URL
     };
 
     /**
      * Instantiate from hidden fields, URL, and handler.
      * 
      * @param hiddenFields collection of hidden form fields.
      * @param servletActionUrl servlet URL for the upload action.
      * @param handler handler to be executed on upload completion.
      */
     public FileUploadDialogPanel(FastMap<String> hiddenFields, String servletActionUrl,
             AsyncUploadCompleteHandler handler, MODE mode) {
         hdlrUpload = handler;
         this.mode = mode;
         destFolder = hiddenFields.get(HDN_PARENT_ID_KEY);
 
         form = new FormPanel();
         fupload = new ArrayList<FileUpload>();
         urls = new ArrayList<TextArea>();
 
         fileStatus = buildFileStatus();
 
         initForm(servletActionUrl);
 
         VerticalPanel pnlInternalLayout = buildInternalLayout(hiddenFields);
 
         VerticalPanel vpnlWidget = new VerticalPanel();
         vpnlWidget.setSpacing(5);
 
         if (mode.equals(MODE.FILE_AND_URL)) {
             vpnlWidget.add(new LabelField(I18N.DISPLAY.fileUploadMaxSizeWarning()));
         }
         vpnlWidget.add(pnlInternalLayout);
 
         form.add(vpnlWidget);
 
         pnlLayout = new VerticalPanel();
         pnlLayout.setLayoutData(new FitLayout());
         pnlLayout.add(form);
     }
 
     private void initForm(String servletActionUrl) {
         form.setStyleName("iplantc-form-layout-panel"); //$NON-NLS-1$
 
         form.setHideLabels(true);
         form.setHeaderVisible(false);
         form.setFieldWidth(FIELD_WIDTH);
 
         form.setAction(servletActionUrl);
         form.setMethod(Method.POST);
         form.setEncoding(Encoding.MULTIPART);
 
         form.addListener(Events.Submit, new SubmitListener());
     }
 
     private VerticalPanel buildInternalLayout(FastMap<String> hiddenFields) {
         VerticalPanel ret = new VerticalPanel();
         ret.setSpacing(4);
         ret.setStyleName("iplantc-form-internal-layout-panel"); //$NON-NLS-1$
 
         // add any key/value pairs provided as hidden field
         for (String field : hiddenFields.keySet()) {
             Hidden hdn = new Hidden(field, hiddenFields.get(field));
             ret.add(hdn);
         }
 
         if (mode.equals(MODE.FILE_AND_URL)) {
             // then add the visual widgets
             for (int i = 0; i < MAX_UPLOADS; i++) {
                 FileUpload uploadField = buildFileUpload();
                 fupload.add(uploadField);
                 ret.add(uploadField);
             }
         }
 
         ret.add(new HTML(I18N.DISPLAY.urlPrompt()));
 
         for (int i = 0; i < MAX_UPLOADS; i++) {
             TextArea url = buildUrlField();
 
             urls.add(url);
             ret.add(url);
         }
 
         ret.add(fileStatus);
 
         return ret;
     }
 
     private TextArea buildUrlField() {
         TextArea url = new TextArea();
 
         url.setName(URL_FIELD);
         url.setWidth(FIELD_WIDTH);
 
         url.setAllowBlank(true);
         url.setAutoValidate(true);
         url.setRegex(URL_REGEX);
         url.getMessages().setRegexText(I18N.DISPLAY.invalidImportUrl());
 
         url.addListener(Events.Change, new Listener<FieldEvent>() {
             @Override
             public void handleEvent(FieldEvent be) {
                 validateForm();
             }
         });
 
         url.addKeyListener(new KeyListener() {
             @Override
             public void componentKeyUp(ComponentEvent event) {
                 if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                     handleOkClick();
                 }
             }
         });
 
         return url;
     }
 
     private Status buildFileStatus() {
         Status ret = new Status();
 
         ret.setStyleName("iplantc-file-status"); //$NON-NLS-1$
 
         return ret;
     }
 
     private FileUpload buildFileUpload() {
         FileUpload ret = new FileUpload();
 
         ret.setStyleName("iplantc-file-upload"); //$NON-NLS-1$
         ret.setName("file"); //$NON-NLS-1$
 
         ret.addChangeHandler(new ChangeHandler() {
             /**
              * When the file upload has changed, enable the upload button.
              * 
              * This is only fired when an actual file is selected, not merely when the browse button is
              * clicked.
              */
             @Override
             public void onChange(ChangeEvent event) {
                 validateForm();
             }
         });
 
         return ret;
     }
 
     private void validateForm() {
        boolean fileStatusIsBusy = fileStatus.getIconStyle().equals("x-status-busy"); //$NON-NLS-1$
 
         getOkButton().setEnabled(isValidUploadForm() && !fileStatusIsBusy);
     }
 
     private void initOkButton() {
         Button btnParentOk = getOkButton();
         btnParentOk.setText(I18N.DISPLAY.upload());
         btnParentOk.disable();
     }
 
     private void doUpload() {
         if (isValidUploadForm()) {
             fileStatus.setBusy(""); //$NON-NLS-1$
             fileStatus.show();
 
             getOkButton().disable();
 
             // check for duplicate files already on the server, excluding any invalid upload fields
             final List<String> destResourceIds = new ArrayList<String>();
             if (mode.equals(MODE.FILE_AND_URL)) {
                 for (FileUpload uploadField : fupload) {
                     // Remove any path from the filename.
                     String filename = uploadField.getFilename().replaceAll(".*[\\\\/]", ""); //$NON-NLS-1$//$NON-NLS-2$
                     boolean validFilename = isValidFilename(filename);
 
                     uploadField.setEnabled(validFilename);
 
                     if (validFilename) {
                         destResourceIds.add(buildResourceId(filename));
                     }
                 }
             }
 
             for (TextArea urlField : urls) {
                 String url = urlField.getValue();
                 boolean validUrl = isValidFilename(url);
 
                 urlField.setEnabled(validUrl);
 
                 if (validUrl) {
                     urlField.setValue(url.trim());
                     destResourceIds.add(buildResourceId(DataUtils.parseNameFromPath(url)));
                 }
             }
 
             if (!destResourceIds.isEmpty()) {
                 DataUtils.checkListForDuplicateFilenames(destResourceIds, new AsyncCallback<String>() {
 
                     @Override
                     public void onSuccess(String response) {
                         form.submit();
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(caught.getMessage(), caught);
                         hdlrUpload.onAfterCompletion();
                     }
                 });
             }
         } else {
             ErrorHandler.post(I18N.ERROR.invalidFilenameEntered(), null);
         }
     }
 
     private String buildResourceId(String filename) {
         return destFolder + "/" + filename; //$NON-NLS-1$
     }
 
     private boolean isValidUploadForm() {
         if (mode.equals(MODE.FILE_AND_URL)) {
             for (FileUpload uploadField : fupload) {
                 String filename = uploadField.getFilename();
                 if (isValidFilename(filename)) {
                     return true;
                 }
             }
         }
 
         for (TextArea urlField : urls) {
             if (urlField.isValid() && isValidFilename(urlField.getValue())) {
                 return true;
             }
         }
 
         return false;
     }
 
     private boolean isValidFilename(String filename) {
         return filename != null && !filename.trim().isEmpty() && !filename.equalsIgnoreCase("null"); //$NON-NLS-1$
     }
 
     private Button getOkButton() {
         return (Button)parentButtons.getItemByItemId(Dialog.OK);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Widget getDisplayWidget() {
         return pnlLayout;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void handleOkClick() {
         fileStatus.show();
         doUpload();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setButtonBar(ButtonBar buttons) {
         super.setButtonBar(buttons);
         initOkButton();
     }
 
     private class SubmitListener implements Listener<FormEvent> {
         @Override
         public void handleEvent(FormEvent fe) {
             String response = fe.getResultHtml();
 
             try {
                 JSONObject jsonResponse = JsonUtil.getObject(JsonUtil.formatString(response));
                 JSONArray results = JsonUtil.getArray(jsonResponse, "results"); //$NON-NLS-1$
                 if (results == null) {
                     throw new Exception(response);
                 }
 
                 for (int i = 0; i < results.size(); i++) {
                     JSONObject jsonFileUploadStatus = JsonUtil.getObjectAt(results, i);
                     if (jsonFileUploadStatus != null) {
                         String action = JsonUtil.getString(jsonFileUploadStatus, "action"); //$NON-NLS-1$
 
                         if (action.equals("file-upload")) { //$NON-NLS-1$
                             JSONObject file = JsonUtil.getObject(jsonFileUploadStatus, "file"); //$NON-NLS-1$
 
                             if (file != null) {
                                 hdlrUpload.onCompletion(JsonUtil.getString(file, File.LABEL),
                                         file.toString());
                             }
                         } else if (action.equals("url-upload")) { //$NON-NLS-1$
                             String sourceUrl = JsonUtil.getString(jsonFileUploadStatus, "url"); //$NON-NLS-1$
 
                             hdlrUpload.onImportSuccess(sourceUrl, jsonFileUploadStatus.toString());
                         }
                     }
                 }
             } catch (Exception caught) {
                 String firstFileName = ""; //$NON-NLS-1$
 
                 if (!fupload.isEmpty()) {
                     firstFileName = fupload.get(0).getFilename();
                 } else if (!urls.isEmpty()) {
                     firstFileName = DataUtils.parseNameFromPath(urls.get(0).getValue());
                 }
 
                 ErrorHandler.post(I18N.ERROR.fileUploadFailed(firstFileName), caught);
                 hdlrUpload.onAfterCompletion();
             }
 
             // we're done, so clear the busy notification
             fileStatus.clearStatus(""); //$NON-NLS-1$
         }
     }
 }
