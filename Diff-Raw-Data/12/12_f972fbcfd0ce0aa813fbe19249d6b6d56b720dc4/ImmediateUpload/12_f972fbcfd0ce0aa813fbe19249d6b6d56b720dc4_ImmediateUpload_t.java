 /**
  * Copyright (C) 2011 PROCESSBASE Ltd.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.processbase.ui.core.template;
 
 import com.vaadin.terminal.StreamResource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.Upload;
 import com.vaadin.ui.Upload.FailedEvent;
 import com.vaadin.ui.Upload.FinishedEvent;
 import com.vaadin.ui.Upload.StartedEvent;
 import com.vaadin.ui.Upload.SucceededEvent;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.themes.Reindeer;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.ResourceBundle;
 
 import org.ow2.bonita.facade.runtime.AttachmentInstance;
 import org.ow2.bonita.facade.runtime.Document;
 import org.processbase.ui.core.BPMModule;
 import org.processbase.ui.core.ProcessbaseApplication;
 
 /**
  *
  * @author marat
  */
 public class ImmediateUpload extends VerticalLayout
         implements Button.ClickListener,
         Upload.SucceededListener,
         Upload.FailedListener,
         Upload.StartedListener,
         Upload.ProgressListener,
         Upload.FinishedListener,
         Upload.Receiver {
 
     private Label status = new Label("");
     private ProgressIndicator pi = new ProgressIndicator();
     private HorizontalLayout progressLayout = new HorizontalLayout();
     private HorizontalLayout statusLayout = new HorizontalLayout();
     private Upload upload = new Upload("", (Upload.Receiver) this);
     private Button deleteBtn = new Button();
     private Button downloadBtn = new Button();
     private Button cancelBtn = new Button();
     private String fileName;
     private String attachmentName;
     private String mtype;
     private ByteArrayOutputStream baos = new ByteArrayOutputStream();
     private String processUUID;
     private ResourceBundle messages;
     private boolean needToSave = false;
 
 
     public ImmediateUpload(String processUUID, String label, String attachmentName, String fileName, boolean hasFile, boolean readOnly, ResourceBundle messages) {
         System.out.println(processUUID + " " + attachmentName + " " + fileName + " " + hasFile);
         this.processUUID = processUUID;
         this.messages = messages;
         this.fileName = fileName;
         this.attachmentName = attachmentName;
         setSpacing(true);
 
         addComponent(statusLayout);
         if (!hasFile) {
             addComponent(upload);
         } else {
             downloadBtn.setCaption(label);
             downloadBtn.setStyleName(Reindeer.BUTTON_LINK);
             downloadBtn.addListener((Button.ClickListener) this);
             addComponent(downloadBtn);
         }
 
         addComponent(progressLayout);
 
         // Make uploading start immediately when file is selected
         upload.setImmediate(true);
         upload.setButtonCaption(label);
         upload.setStyleName(Reindeer.BUTTON_LINK);
 
         progressLayout.setSpacing(true);
         progressLayout.setVisible(false);
         progressLayout.addComponent(pi);
         progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_CENTER);
         cancelBtn = new Button(messages.getString("btnCancel"), this);
         cancelBtn.setStyleName("small");
         progressLayout.addComponent(cancelBtn);
 
         deleteBtn.setStyleName(Reindeer.BUTTON_LINK);
         deleteBtn.setDescription(messages.getString("btnDelete"));
         deleteBtn.setIcon(new ThemeResource("icons/cancel.png"));
         deleteBtn.addListener((Button.ClickListener) this);
         deleteBtn.setVisible(false);
         statusLayout.addComponent(deleteBtn);
         statusLayout.addComponent(status);
 
         /**
          * =========== Add needed listener for the upload component: start,
          * progress, finish, success, fail ===========
          */
         upload.addListener((Upload.StartedListener) this);
         upload.addListener((Upload.ProgressListener) this);
         upload.addListener((Upload.SucceededListener) this);
         upload.addListener((Upload.FailedListener) this);
         upload.addListener((Upload.FinishedListener) this);
 
     }
 
     public void buttonClick(ClickEvent event) {
         try {
             if (event.getButton().equals(cancelBtn)) {
                 upload.interruptUpload();
             } else if (event.getButton().equals(deleteBtn)) {
                 baos = new ByteArrayOutputStream();
                 upload.setVisible(true);
                 status.setValue("");
                 deleteBtn.setVisible(false);
             } else if (event.getButton().equals(downloadBtn)) {
             	BPMModule bpmModule = ProcessbaseApplication.getCurrent().getBpmModule();
 				AttachmentInstance attachment = bpmModule.getAttachment(processUUID, attachmentName);
 				
 				//Document document = bpmModule.getDocument(attachment.getUUID());
                 ByteArraySource bas = new ByteArraySource(ProcessbaseApplication.getCurrent().getFileBody(processUUID, attachmentName));
                if(bas!=null && bas.byteArray.length!=0){
	                StreamResource streamResource = new StreamResource(bas, attachment.getFileName(), getApplication());
	                streamResource.setCacheTime(50000); // no cache (<=0) does not work with IE8
	                getWindow().getWindow().open(streamResource, "_blank");
                }
                else
                	getWindow().showNotification("File " + attachmentName + "is empty");
             }
         } catch (Exception ex) {
             ex.printStackTrace();
            getWindow().showNotification("File loading error");
         }
     }
 
     public void uploadStarted(StartedEvent event) {
         // This method gets called immediatedly after upload is started
         upload.setVisible(false);
         progressLayout.setVisible(true);
         pi.setValue(0f);
         pi.setPollingInterval(500);
         status.setValue(messages.getString("labelUploading") + " \"" + event.getFilename() + "\"");
     }
 
     public void updateProgress(long readBytes, long contentLength) {
         // This method gets called several times during the update
         pi.setValue(new Float(readBytes / (float) contentLength));
     }
 
     public void uploadSucceeded(SucceededEvent event) {
         // This method gets called when the upload finished successfully
         status.setValue("\"" + event.getFilename() + "\" " + messages.getString("labelIsUploaded"));
         setNeedToSave(true);
     }
 
     public void uploadFailed(FailedEvent event) {
         // This method gets called when the upload failed
         status.setValue(messages.getString("labelUploadingInterrupted"));
     }
 
     public void uploadFinished(FinishedEvent event) {
         // This method gets called always when the upload finished,
         // either succeeding or failing
         progressLayout.setVisible(false);
 //                upload.setVisible(true);
 //                upload.setCaption("Select another file");
         deleteBtn.setVisible(true);
     }
 
     public OutputStream receiveUpload(String filename, String mimetype) {
         fileName = filename;
         mtype = mimetype;
 
         return baos;
     }
 
     public String getFileName() {
         return fileName;
     }
 
     public String getMimeType() {
         return mtype;
     }
 
     public byte[] getFileBody() {
         return baos.toByteArray();
     }
 
     public boolean isNeedToSave() {
         return needToSave;
     }
 
     public void setNeedToSave(boolean needToSave) {
         this.needToSave = needToSave;
     }
 
 	public Upload getUploadComponent() {
 		return upload;
 	}
 
 
 }
