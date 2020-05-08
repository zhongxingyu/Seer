 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.client.widgets.validator.AnalysisNameValidator;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.views.panels.IPlantDialogPanel;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.util.DiskResourceUtil;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.AsyncUploadCompleteHandler;
 import org.iplantc.de.client.events.DefaultUploadCompleteHandler;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.services.DiskResouceDuplicatesCheckCallback;
 import org.iplantc.de.client.services.DiskResourceServiceFacade;
 import org.iplantc.de.client.utils.DataUtils;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.FormEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.util.Format;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Status;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FileUploadField;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Panel component for uploading files.
  * 
  * @author lenards
  * 
  */
 public class FileUploadDialogPanel extends IPlantDialogPanel {
     private static final String ID_WRAP = "idWrap"; //$NON-NLS-1$
 
     private static final String ID_FILE_UPLD = "idFileUpld"; //$NON-NLS-1$
 
     private static final String ID_BTN_RESET = "idBtnReset"; //$NON-NLS-1$
 
     private static final String ID_STAT = "idStat"; //$NON-NLS-1$
 
     private static final int FIELD_WIDTH = 425;
 
     private static final String URL_REGEX = "^(?:ftp|FTP|HTTPS?|https?)://[^/]+/.*[^/ ]$"; //$NON-NLS-1$
 
     public static final String HDN_USER_ID_KEY = "user"; //$NON-NLS-1$
     public static final String HDN_PARENT_ID_KEY = "dest"; //$NON-NLS-1$
     public static final String FILE_TYPE = "type"; //$NON-NLS-1$
     public static final String URL_FIELD = "url"; //$NON-NLS-1$
 
     private static final int MAX_UPLOADS = 5;
 
     private final ArrayList<FormPanel> forms;
     private final ContentPanel pnlLayout;
     private final DefaultUploadCompleteHandler hdlrUpload;
     private final List<FileUploadField> fupload;
     private final List<TextArea> urls;
     private final FastMap<Status> fileStatusMap;
     private final String destFolder;
     private final MODE mode;
     private final String servletActionUrl;
 
     private int submitCount;
     private int responseCount;
 
     public static enum MODE {
         URL_ONLY, FILE_ONLY
     };
 
     /**
      * Instantiate from hidden fields, URL, and handler.
      * 
      * @param hiddenFields collection of hidden form fields.
      * @param servletActionUrl servlet URL for the upload action.
      * @param handler handler to be executed on upload completion.
      */
     public FileUploadDialogPanel(FastMap<String> hiddenFields, String servletActionUrl,
             DefaultUploadCompleteHandler handler, MODE mode) {
         hdlrUpload = handler;
         this.mode = mode;
         destFolder = hiddenFields.get(HDN_PARENT_ID_KEY);
         this.servletActionUrl = servletActionUrl;
 
         forms = new ArrayList<FormPanel>();
         fupload = new ArrayList<FileUploadField>();
         urls = new ArrayList<TextArea>();
         fileStatusMap = new FastMap<Status>();
 
         pnlLayout = new ContentPanel();
         pnlLayout.setHeaderVisible(false);
         pnlLayout.setBodyBorder(false);
         pnlLayout.setScrollMode(Scroll.AUTOY);
         pnlLayout.setStyleAttribute("padding", "5px");
 
         pnlLayout.add(new Html("&nbsp;" + I18N.DISPLAY.fileUploadFolder(destFolder))); //$NON-NLS-1$
         buildInternalLayout(hiddenFields);
     }
 
     private FormPanel initForm() {
         FormPanel form = new FormPanel();
         form.setBodyBorder(false);
         form.setBorders(false);
         form.setStyleName("iplantc-form-layout-panel"); //$NON-NLS-1$
 
         form.setHideLabels(true);
         form.setHeaderVisible(false);
         form.setFieldWidth(FIELD_WIDTH);
         form.setScrollMode(Scroll.AUTOY);
 
         return form;
     }
 
     private FormPanel initUploadForm(int index) {
         FormPanel form = initForm();
         form.setAction(servletActionUrl);
         form.setMethod(Method.POST);
         form.setEncoding(Encoding.MULTIPART);
         form.addListener(Events.Submit, new SubmitListener(index));
         return form;
     }
 
     private void buildInternalLayout(FastMap<String> hiddenFields) {
         if (mode == MODE.FILE_ONLY) {
             buildSimpleUploadLayout(hiddenFields);
         }
 
         if (mode == MODE.URL_ONLY) {
             buildUrlImportLayout();
         }
     }
 
     private void buildSimpleUploadLayout(FastMap<String> hiddenFields) {
         pnlLayout.setHeight(270);
         pnlLayout.add(new Html("&nbsp;" + I18N.DISPLAY.fileUploadMaxSizeWarning())); //$NON-NLS-1$
 
         for (int i = 0; i < MAX_UPLOADS; i++) {
             FormPanel fp = initUploadForm(i);
             // add any key/value pairs provided as hidden field
             for (String field : hiddenFields.keySet()) {
                 Hidden hdn = new Hidden(field, hiddenFields.get(field));
                 fp.add(hdn);
             }
             HorizontalPanel uploadFieldPnl = buildFileUpload(i);
             fupload.add((FileUploadField)uploadFieldPnl.getItemByItemId(ID_FILE_UPLD + i));
             fp.add(uploadFieldPnl);
             pnlLayout.add(fp);
             forms.add(fp);
         }
     }
 
     private void buildUrlImportLayout() {
         pnlLayout.setHeight(360);
         pnlLayout.add(new Html("&nbsp;" + I18N.DISPLAY.urlPrompt())); //$NON-NLS-1$
         VerticalPanel vp = new VerticalPanel();
         vp.setSpacing(3);
 
         for (int i = 0; i < MAX_UPLOADS; i++) {
             TextArea url = buildUrlField(URL_FIELD + i);
             urls.add(url);
             vp.add(url);
         }
 
         pnlLayout.add(vp);
     }
 
     private TextArea buildUrlField(String id) {
         TextArea url = new TextArea();
 
         url.setId(id);
         url.setName(URL_FIELD);
         url.setWidth(FIELD_WIDTH);
 
         url.setAllowBlank(true);
         url.setAutoValidate(true);
         url.setRegex(URL_REGEX);
         url.getMessages().setRegexText(I18N.DISPLAY.invalidImportUrl());
 
         url.addKeyListener(new KeyListener() {
             @Override
             public void componentKeyUp(ComponentEvent event) {
                validateForm();

                 if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                     handleOkClick();
                 }
             }
         });
 
         return url;
     }
 
     private HorizontalPanel buildFileUpload(int index) {
         HorizontalPanel wrapper = new HorizontalPanel();
         wrapper.setBorders(false);
         wrapper.setId(ID_WRAP + index);
 
         FileUploadField ret = new FileUploadField();
         ret.setValidator(new AnalysisNameValidator());
         ret.setId(ID_FILE_UPLD + index);
         ret.setName("file"); //$NON-NLS-1$
         ret.addListener(Events.OnChange, new Listener<FieldEvent>() {
             @Override
             public void handleEvent(FieldEvent be) {
                 // KLUDGE remove the C:\fakepath\ string from the filename that Chrome prepends before
                 // validating. Other browsers still reserve the right to prepend their own fake paths
                 // (though I don't know of any others that do), but we don't want to remove all
                 // characters up to a "\" or "/", since an invalid filename my contain those characters,
                 // which would pass validation if all the following characters were valid.
                 FileUploadField fileUploadField = (FileUploadField)be.getBoxComponent();
                 fileUploadField.setValue(fileUploadField.getValue().replace("C:\\fakepath\\", "")); //$NON-NLS-1$ //$NON-NLS-2$
 
                 validateForm();
             }
 
         });
         ret.setAutoValidate(true);
         ret.setWidth(275);
 
         wrapper.add(ret);
         // Reserve some space for the FileUploadField validation icon.
         wrapper.add(new Html("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")); //$NON-NLS-1$
         wrapper.add(buildResetButton(ret));
         wrapper.add(buildStatus(ID_STAT + index));
 
         return wrapper;
     }
 
     private Button buildResetButton(final FileUploadField field) {
         Button reset = new Button();
         reset.setToolTip(I18N.DISPLAY.reset());
         reset.setId(ID_BTN_RESET);
         reset.setIcon(AbstractImagePrototype.create(Resources.ICONS.arrow_undo()));
         reset.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 field.reset();
                 validateForm();
             }
         });
 
         return reset;
     }
 
     private Status buildStatus(String id) {
         Status s = new Status();
         s.setId(id);
         fileStatusMap.put(id, s);
 
         return s;
     }
 
     private void validateForm() {
         getOkButton().setEnabled(isValidUploadForm());
     }
 
     private void initOkButton() {
         Button btnParentOk = getOkButton();
         btnParentOk.setText(I18N.DISPLAY.upload());
         btnParentOk.disable();
     }
 
     private void doUpload() {
         if (isValidUploadForm()) {
             getOkButton().disable();
 
             // check for duplicate files already on the server, excluding any invalid upload fields
             final FastMap<TextField<String>> destResourceMap = new FastMap<TextField<String>>();
             if (mode == MODE.FILE_ONLY) {
                 for (FileUploadField uploadField : fupload) {
                     // Remove any path from the filename.
                     if (isValidUploadField(uploadField)) {
                         destResourceMap.put(buildResourceId(uploadField.getValue()), uploadField);
                     }
                 }
             }
 
             if (mode == MODE.URL_ONLY) {
                 for (TextArea urlField : urls) {
                     boolean validUrl = isValidUploadField(urlField);
 
                     if (validUrl) {
                         String url = urlField.getValue().trim();
                         urlField.setValue(url);
                         destResourceMap.put(buildResourceId(DiskResourceUtil.parseNameFromPath(url)),
                                 urlField);
                     }
                 }
             }
 
             if (!destResourceMap.isEmpty()) {
                 List<String> ids = new ArrayList<String>();
                 ids.addAll(destResourceMap.keySet());
                 DataUtils.checkListForDuplicateFilenames(ids, new CheckDuplicatesCallback(ids,
                         destResourceMap));
             }
         } else {
             ErrorHandler.post(I18N.ERROR.invalidFilenameEntered(), null);
         }
     }
 
     private String buildResourceId(String filename) {
         return destFolder + "/" + filename; //$NON-NLS-1$
     }
 
     private boolean isValidUploadForm() {
         if (mode == MODE.FILE_ONLY) {
             for (FileUploadField uploadField : fupload) {
                 if (isValidUploadField(uploadField)) {
                     return true;
                 }
             }
         }
 
         if (mode == MODE.URL_ONLY) {
             for (TextArea urlField : urls) {
                 if (isValidUploadField(urlField)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Checks if the given TextField is valid, but returns false if its filename is also null or empty.
      * 
      * @param uploadField
      * @return true if the given TextField is valid and has a non-empty filename.
      */
     private boolean isValidUploadField(TextField<String> uploadField) {
         if (uploadField == null) {
             return false;
         }
 
         String filename = uploadField.getValue();
         if (filename == null) {
             return false;
         }
 
         return uploadField.isValid() && !filename.trim().isEmpty() && !filename.equalsIgnoreCase("null"); //$NON-NLS-1$
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
 
     private final class CheckDuplicatesCallback extends DiskResouceDuplicatesCheckCallback {
 
         private final FastMap<TextField<String>> destResourceMap;
 
         public CheckDuplicatesCallback(List<String> diskResourceIds,
                 FastMap<TextField<String>> destResourceMap) {
             super(diskResourceIds);
             this.destResourceMap = destResourceMap;
         }
 
         @Override
         public void markDuplicates(List<String> duplicates) {
             if (destResourceMap != null && duplicates != null && duplicates.size() > 0) {
                 for (String id : duplicates) {
                     @SuppressWarnings("rawtypes")
                     Field f = destResourceMap.get(buildResourceId(id));
                     f.markInvalid(I18N.ERROR.fileExist());
                 }
                 if (mode == MODE.FILE_ONLY) {
                     for (int i = 0; i < MAX_UPLOADS; i++) {
                         Status st = fileStatusMap.get(ID_STAT + i);
                         st.setText(""); //$NON-NLS-1$
                     }
                 }
                 getOkButton().enable();
                 return;
             } else {
                 if (fupload.size() > 0) {
                     getOkButton().disable();
                     for (int i = 0; i < MAX_UPLOADS; i++) {
                         if (isValidUploadField(fupload.get(i))) {
                             FormPanel formPanel = forms.get(i);
                             Status st = fileStatusMap.get(ID_STAT + i);
                             st.setBusy(""); //$NON-NLS-1$
                             formPanel.submit();
                             submitCount++;
                         }
                     }
                 }
 
                 if (urls.size() > 0) {
                     getOkButton().disable();
                     submitUrlImports();
                 }
 
             }
         }
     }
 
     private void submitUrlImports() {
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         for (TextArea urlField : urls) {
             if (isValidUploadField(urlField)) {
                 String value = urlField.getValue();
 
                 facade.importFromUrl(value, destFolder,
                         new AsyncUploadCompleteHandler(destFolder, value) {
                             @Override
                             public void onAfterCompletion() {
                                 if (submitCount == responseCount) {
                                     hdlrUpload.onAfterCompletion();
                                 }
                             }
 
                         });
             }
         }
     }
 
     private String stripXml(String response) {
         String ret = null;
         if (response != null) {
             ret = Format.stripTags(response);
         }
 
         return ret;
     }
 
     private class SubmitListener implements Listener<FormEvent> {
 
         private final int index;
 
         public SubmitListener(int index) {
             this.index = index;
         }
 
         @Override
         public void handleEvent(FormEvent fe) {
             String response = stripXml(fe.getResultHtml());
 
             try {
                 JSONObject jsonResponse = JsonUtil.getObject(JsonUtil.formatString(response));
                 if (jsonResponse == null) {
                     throw new Exception(response);
                 }
 
                 String action = JsonUtil.getString(jsonResponse, "action"); //$NON-NLS-1$
 
                 responseCount++;
 
                 if (action.equals("file-upload")) { //$NON-NLS-1$
                     JSONObject file = JsonUtil.getObject(jsonResponse, "file"); //$NON-NLS-1$
 
                     if (file != null) {
                         hdlrUpload.onCompletion(JsonUtil.getString(file, File.LABEL), file.toString());
 
                         // Clear the busy notification with a Success message
                         Status st = fileStatusMap.get(ID_STAT + index);
                         st.clearStatus(I18N.DISPLAY.success());
                     }
                 }
 
             } catch (Exception caught) {
                 String firstFileName = ""; //$NON-NLS-1$
                 responseCount++;
 
                 if (!fupload.isEmpty()) {
                     firstFileName = fupload.get(0).getValue();
                 } else if (!urls.isEmpty()) {
                     firstFileName = DiskResourceUtil.parseNameFromPath(urls.get(0).getValue());
                 }
 
                 ErrorHandler.post(I18N.ERROR.fileUploadFailed(firstFileName), caught);
 
             } finally {
                 if (submitCount == responseCount) {
                     hdlrUpload.onAfterCompletion();
                 }
             }
         }
     }
 }
