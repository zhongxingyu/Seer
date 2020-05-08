 package org.iplantc.core.uidiskresource.client.views.dialogs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.info.ErrorAnnouncementConfig;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.widgets.IPCFileUploadField;
 import org.iplantc.core.uidiskresource.client.events.FileUploadedEvent;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.services.DiskResourceServiceFacade;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DuplicateDiskResourceCallback;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.safehtml.shared.SafeUri;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 import com.sencha.gxt.core.client.util.Format;
 import com.sencha.gxt.core.shared.FastMap;
 import com.sencha.gxt.widget.core.client.Status;
 import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.event.InvalidEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent;
 import com.sencha.gxt.widget.core.client.event.SubmitEvent;
 import com.sencha.gxt.widget.core.client.event.SubmitEvent.SubmitHandler;
 import com.sencha.gxt.widget.core.client.event.ValidEvent;
 import com.sencha.gxt.widget.core.client.form.FormPanel;
 import com.sencha.gxt.widget.core.client.form.FormPanel.Encoding;
 import com.sencha.gxt.widget.core.client.form.FormPanel.Method;
 import com.sencha.gxt.widget.core.client.form.FormPanelHelper;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.tips.ToolTip;
 import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
 
 public class SimpleFileUploadDialog extends IPlantDialog {
 
     private static final String FORM_WIDTH = "475";
     private static final String FORM_HEIGHT = "28";
     public static final String HDN_PARENT_ID_KEY = "dest";
     public static final String HDN_USER_ID_KEY = "user";
     public static final String FILE_TYPE = "type";
     public static final String URL_FIELD = "url";
     private static SimpleFileUploadPanelUiBinder BINDER = GWT
             .create(SimpleFileUploadPanelUiBinder.class);
 
     @UiTemplate("SimpleFileUploadPanel.ui.xml")
     interface SimpleFileUploadPanelUiBinder extends UiBinder<Widget, SimpleFileUploadDialog> {
     }
 
     @UiField
     HTML htmlDestText;
 
     @UiField
     FormPanel form0, form1, form2, form3, form4;
 
     @UiField
     IPCFileUploadField fuf0, fuf1, fuf2, fuf3, fuf4;
 
     @UiField
     TextButton btn0, btn1, btn2, btn3, btn4;
 
     @UiField
     Status status0, status1, status2, status3, status4;
 
     private final List<FormPanel> formList;
     private final List<IPCFileUploadField> fufList;
     private final List<TextButton> tbList;
     private final List<Status> statList;
     private final Folder uploadDest;
     private final DiskResourceServiceFacade drService;
     private final List<FormPanel> submittedForms = Lists.newArrayList();
     private final SafeUri fileUploadServlet;
     private final String userName;
     private final EventBus eventBus;
 
     public SimpleFileUploadDialog(Folder uploadDest, DiskResourceServiceFacade drService,
             EventBus eventBus, SafeUri fileUploadServlet, String userName) {
         this.uploadDest = uploadDest;
         this.drService = drService;
         this.eventBus = eventBus;
         this.fileUploadServlet = fileUploadServlet;
         this.userName = userName;
         setAutoHide(false);
         setHideOnButtonClick(false);
         // Reset the "OK" button text
         getOkButton().setText(I18N.DISPLAY.upload());
         getOkButton().setEnabled(false);
         setHeadingText(I18N.DISPLAY.upload());
         addCancelButtonSelectHandler(new HideSelectHandler(this));
 
         add(BINDER.createAndBindUi(this));
 
         formList = Lists.newArrayList(form0, form1, form2, form3, form4);
         fufList = Lists.newArrayList(fuf0, fuf1, fuf2, fuf3, fuf4);
         tbList = Lists.newArrayList(btn0, btn1, btn2, btn3, btn4);
         statList = Lists.newArrayList(status0, status1, status2, status3, status4);
 
         initDestPathLabel();
     }
 
     private void initDestPathLabel() {
         String destPath = uploadDest.getId();
 
         htmlDestText.setHTML(Format.ellipse(I18N.DISPLAY.uploadingToFolder(destPath), 80));
         new ToolTip(htmlDestText, new ToolTipConfig(destPath));
     }
 
     @UiFactory
     FormPanel createFormPanel() {
         FormPanel form = new FormPanel();
         form.setAction(fileUploadServlet);
         form.setMethod(Method.POST);
         form.setEncoding(Encoding.MULTIPART);
         form.setSize(FORM_WIDTH, FORM_HEIGHT);
         return form;
     }
 
     @UiFactory
     HorizontalLayoutContainer createHLC() {
         HorizontalLayoutContainer hlc = new HorizontalLayoutContainer();
         hlc.add(new Hidden(HDN_PARENT_ID_KEY, uploadDest.getId()));
         hlc.add(new Hidden(HDN_USER_ID_KEY, userName));
         return hlc;
     }
 
     @UiFactory
     Status createFormStatus() {
         Status status = new Status();
         status.setWidth(15);
         return status;
     }
 
     @UiHandler({"btn0", "btn1", "btn2", "btn3", "btn4"})
     void onResetClicked(SelectEvent event) {
         IPCFileUploadField uField = fufList.get(tbList.indexOf(event.getSource()));
         uField.reset();
         uField.validate(true);
     }
 
     @UiHandler({"fuf0", "fuf1", "fuf2", "fuf3", "fuf4"})
     void onFieldChanged(ChangeEvent event) {
         getOkButton().setEnabled(FormPanelHelper.isValid(this, true) && isValidForm());
     }
 
     @UiHandler({"fuf0", "fuf1", "fuf2", "fuf3", "fuf4"})
     void onFieldValid(ValidEvent event) {
         getOkButton().setEnabled(FormPanelHelper.isValid(this, true) && isValidForm());
     }
 
     private boolean isValidForm() {
         for (IPCFileUploadField f : fufList) {
             if (!Strings.isNullOrEmpty(f.getValue())
                     && !f.getValue().equalsIgnoreCase(uploadDest.getId())) {
                 return true;
             }
         }
         return false;
 
     }
 
     @UiHandler({"fuf0", "fuf1", "fuf2", "fuf3", "fuf4"})
     void onFieldInvalid(InvalidEvent event) {
         getOkButton().setEnabled(false);
     }
 
     @Override
     public void hide() {
         if (submittedForms.size() > 0) {
             final ConfirmMessageBox cmb = new ConfirmMessageBox(
                     I18N.DISPLAY.confirmAction(),
                     I18N.DISPLAY.idropLiteCloseConfirmMessage());
 
             cmb.addHideHandler(new HideHandler() {
 
                 @Override
                 public void onHide(HideEvent event) {
                     if (cmb.getHideButton().getText().equalsIgnoreCase("yes")) {
                         SimpleFileUploadDialog.super.hide();
                     }
                 }
             });
 
             cmb.show();
         } else {
             super.hide();
         }
     }
 
     @UiHandler({"form0", "form1", "form2", "form3", "form4"})
     void onSubmitComplete(SubmitCompleteEvent event) {
         if (submittedForms.contains(event.getSource())) {
             submittedForms.remove(event.getSource());
             statList.get(formList.indexOf(event.getSource())).clearStatus("");
         }
 
         String results2 = event.getResults();
         String results = Format.stripTags(results2);
         Splittable split = StringQuoter.split(results);
         IPCFileUploadField field = fufList.get(formList.indexOf(event.getSource()));
        if ((split.get("file") == null)) {
             field.markInvalid(I18N.ERROR.fileUploadFailed(field.getValue()));
             IplantAnnouncer.getInstance().schedule(I18N.ERROR.fileUploadFailed(field.getValue()),
                     new ErrorAnnouncementConfig());
         } else {
             IplantAnnouncer.getInstance().schedule(I18N.DISPLAY.fileUploadSuccess(field.getValue()));
         }
 
         if (submittedForms.size() == 0) {
             eventBus.fireEvent(new FileUploadedEvent(uploadDest));
             hide();
         }
 
     }
 
     @UiHandler({"fuf0", "fuf1", "fuf2", "fuf3", "fuf4"})
     void onFormKeyUp(KeyUpEvent event) {
         if ((event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
                 || (event.getNativeKeyCode() == KeyCodes.KEY_DELETE)) {
             TextField tf = (TextField)event.getSource();
             for (IPCFileUploadField fuf : fufList) {
                 String value = fuf.getValue();
                 String currentValue = tf.getCurrentValue();
                 if (value.equalsIgnoreCase(currentValue)) {
                     fuf.clear();
                     fuf.validate(true);
                     break;
                 }
             }
         }
     }
 
     @Override
     protected void onOkButtonClicked() {
         doUpload();
     }
 
     private void doUpload() {
         FastMap<IPCFileUploadField> destResourceMap = new FastMap<IPCFileUploadField>();
         for (IPCFileUploadField field : fufList) {
             String fileName = field.getValue().replaceAll(".*[\\\\/]", "");
             field.setEnabled(!Strings.isNullOrEmpty(fileName) && !fileName.equalsIgnoreCase("null"));
             if (field.isEnabled()) {
                 destResourceMap.put(uploadDest.getId() + "/" + fileName, field);
             } else {
                 field.setEnabled(false);
             }
         }
 
         if (!destResourceMap.isEmpty()) {
             ArrayList<String> ids = Lists.newArrayList(destResourceMap.keySet());
             drService.diskResourcesExist(ids, new CheckDuplicatesCallback(ids, destResourceMap,
                     statList, fufList, submittedForms, formList));
         }
     }
 
     private final class CheckDuplicatesCallback extends DuplicateDiskResourceCallback {
         private final FastMap<IPCFileUploadField> destResourceMap;
         private final List<Status> statList;
         private final List<IPCFileUploadField> fufList;
         private final List<FormPanel> submittedForms;
         private final List<FormPanel> formList;
 
         public CheckDuplicatesCallback(List<String> ids, FastMap<IPCFileUploadField> destResourceMap,
                 List<Status> statList, List<IPCFileUploadField> fufList, List<FormPanel> submittedForms,
                 List<FormPanel> formList) {
             super(ids, null);
             this.destResourceMap = destResourceMap;
             this.statList = statList;
             this.fufList = fufList;
             this.submittedForms = submittedForms;
             this.formList = formList;
         }
 
         @Override
         public void markDuplicates(Collection<String> duplicates) {
             if ((duplicates != null) && !duplicates.isEmpty()) {
                 for (String id : duplicates) {
                     destResourceMap.get(id).markInvalid(I18N.ERROR.fileExist());
                 }
             } else {
                 for (IPCFileUploadField field : destResourceMap.values()) {
                     int index = fufList.indexOf(field);
                     statList.get(index).setBusy("");
                     FormPanel form = formList.get(index);
                     form.addSubmitHandler(new SubmitHandler() {
 
                         @Override
                         public void onSubmit(SubmitEvent event) {
                             getOkButton().disable();
                         }
                     });
                     form.submit();
                     submittedForms.add(form);
                 }
             }
 
         }
     }
 }
