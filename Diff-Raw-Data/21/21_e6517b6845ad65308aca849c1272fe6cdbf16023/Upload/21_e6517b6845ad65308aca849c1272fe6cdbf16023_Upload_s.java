 /* MyMAM - Open Source Digital Media Asset Management.
  * http://www.mymam.net
  *
  * Copyright 2013, MyMAM contributors as indicated by the @author tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.mymam.ui;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import javax.faces.component.FacesComponent;
 import javax.faces.component.UIInput;
 
 /**
  * Custom component for file uploads.
  *
  * <p/>
  * In the XHTML facelet, the component is used as follows:
  * <pre>
  *     &lt;h:form id="fileupload" enctype="multipart/form-data"&gt;
  *         &lt;mymam:upload value="#{fileUploadBean.uploadedFile}"/&gt;
  *     &lt;/h:form&gt;
  * </pre>
  *
  * <p/>
  * In addition to that, you may configure the content of labels and messages
  * used in the upload component:
  * <pre>
  *     &lt;h:form id="fileupload" enctype="multipart/form-data"&gt;
  *         &lt;mymam:upload value="#{fileUploadBean.uploadedFile}"/&gt;
  *             successMsgTemplate="&lt;strong&gt;Well done!&lt;/strong&gt; $ok of $total files have been uploaded successfully."
  *             warnMsgTemplate="&lt;strong&gt;Warning!&lt;/strong&gt; $failed of $total uploads failed."
  *             errorMsgTemplate="&lt;strong&gt;Error!&lt;/strong&gt; $failed of $total uploads failed."
  *             addFilesLabel="Add Files..."
  *             clearButtonLabel="Clear"
  *             submitButtonLabel="Submit"
  *             removeButtonLabel="Remove"
  *             globalStatusTemplate="$progress% of $size uploaded."
  *             uploadingLabel="Uploading"
  *             doneLabel="Done"
  *             failedLabel="Failed"/>
  *     &lt;/h:form&gt;
  * </pre>
  *
  * <p/>
  * As shown in the example, the messages may include some variables.
  * These variables are substitued by JavaScript in the browser.
  *
  * <p/>
  * Variables for successMsgTemplate, warnMsgTemplate, and errorMsgTemplate are:
  * <ul>
  *     <li>$failed: Number of failed uploads</li>
  *     <li>$ok: Number of successful uploads</li>
  *     <li>$total: Total number of uploads</li>
  * </ul>
  *
  * <p/>
  * Variables for globalStatusTemplate are:
  * <ul>
  *     <li>$progress: percentage of completion, e.g. '5' for 5%.</li>
  *     <li>$size: total upload size, e.g. '5M' for 5 Megabytes.</li>
  * </ul>
  *
  * @author fstab
  */
 @ResourceDependencies({
         // TODO: Add CSS for file upload
        @ResourceDependency(library="js", name="jquery-1.9.0.js", target="body"),
        @ResourceDependency(library="js", name="bootstrap.min.js", target="body"),
        @ResourceDependency(library="js", name="jquery.ui.widget.js", target="body"),
        @ResourceDependency(library="js", name="jquery.iframe-transport.js", target="body"),
        @ResourceDependency(library="js", name="jquery.fileupload.js", target="body"),
        @ResourceDependency(library="js", name="upload.js", target="body"),
        @ResourceDependency(library="css", name="upload.css")
 })
 @FacesComponent(value = "net.mymam.ui.Upload")
 public class Upload extends UIInput {
 
     private enum PropertyKeys {
         addFilesButtonLabel,
         clearButtonLabel,
         submitButtonLabel,
         removeButtonLabel,
         successMsgTemplate,
         warnMsgTemplate,
         errorMsgTemplate,
         globalStatusTemplate,
         uploadingLabel,
         doneLabel,
         failedLabel
     };
 
     public Upload() {
         setRendererType("net.mymam.ui.UploadRenderer");
     }
 
     public String getAddFilesButtonLabel() {
         return (String) getStateHelper().eval(PropertyKeys.addFilesButtonLabel, "Add Files...");
     }
 
     /**
      * Label for the "Add Files..." button.
      *
      * <p/>
      * Default value: <tt>Add Files...</tt>
      *
      * @param addFilesButtonLabel label for the "Add Files..." button.
      */
     public void setAddFilesLabel(String addFilesButtonLabel) {
         getStateHelper().put(PropertyKeys.addFilesButtonLabel, addFilesButtonLabel);
     }
 
     public String getClearButtonLabel() {
         return (String) getStateHelper().eval(PropertyKeys.clearButtonLabel, "Clear");
     }
 
     /**
      * Label for the "Clear" button.
      *
      * <p/>
      * Default value: <tt>Clear</tt>
      *
      * @param clearButtonLabel label for the "Clear" button.
      */
     public void setClearButtonLabel(String clearButtonLabel) {
         getStateHelper().put(PropertyKeys.clearButtonLabel, clearButtonLabel);
     }
 
     public String getSubmitButtonLabel() {
         return (String) getStateHelper().eval(PropertyKeys.submitButtonLabel, "Submit");
     }
 
     /**
      * Label for the "Submit" button.
      *
      * <p/>
      * Default value: <tt>Submit</tt>
      *
      * @param submitButtonLabel label for the "Submit" button.
      */
     public void setSubmitButtonLabel(String submitButtonLabel) {
         getStateHelper().put(PropertyKeys.submitButtonLabel, submitButtonLabel);
     }
 
     public String getRemoveButtonLabel() {
         return (String) getStateHelper().eval(PropertyKeys.removeButtonLabel, "Remove");
     }
 
     /**
      * Label for the "Remove" button.
      *
      * <p/>
      * Default value: <tt>Remove</tt>
      *
      * @param removeButtonLabel label for the "Remove" button.
      */
     public void setRemoveButtonLabel(String removeButtonLabel) {
         getStateHelper().put(PropertyKeys.removeButtonLabel, removeButtonLabel);
     }
 
     public String getSuccessMsgTemplate() {
         return (String) getStateHelper().eval(PropertyKeys.successMsgTemplate, "<strong>Well done!</strong> "+
                 "$ok of $total files have been uploaded successfully.");
     }
 
     /**
      * Content of the <a href="http://twitter.github.com/bootstrap/components.html#alerts">alert</a> box
      * displayed when all uploads have been finished successfully.
      *
      * <p/>
      * The message may use HTML markup, and may include some variables that are processed by JavaScript.
      * See {@link Upload class documentation} for the definition of these variables.
      *
      * <p/>
      * Default value:
      * <tt>&lt;strong&gt;Well done!&lt;/strong&gt; $ok of $total files have been uploaded successfully.</tt>
      *
      * @param successMsgTemplate the message to be displayed upon successful upload.
      */
     public void setSuccessMsgTemplate(String successMsgTemplate) {
         getStateHelper().put(PropertyKeys.successMsgTemplate, successMsgTemplate);
     }
 
     public String getWarnMsgTemplate() {
         return (String) getStateHelper().eval(PropertyKeys.warnMsgTemplate, "<strong>Warning!</strong> " +
                 "$failed of $total uploads failed.");
     }
 
     /**
      * Content of the <a href="http://twitter.github.com/bootstrap/components.html#alerts">alert</a> box
      * displayed when only a part of the files could be uploaded successfully.
      *
      * <p/>
      * The message may use HTML markup, and may include some variables that are processed by JavaScript.
      * See {@link Upload class documentation} for the definition of these variables.
      *
      * <p/>
      * Default value:
      * <tt>&lt;strong&gt;Warning!&lt;/strong&gt; $failed of $total uploads failed.</tt>
      *
      * @param warnMsgTemplate the message to be displayed.
      */
     public void setWarnMsgTemplate(String warnMsgTemplate) {
         getStateHelper().put(PropertyKeys.warnMsgTemplate, warnMsgTemplate);
     }
 
     public String getErrorMsgTemplate() {
         return (String) getStateHelper().eval(PropertyKeys.errorMsgTemplate, "<strong>Error!</strong> " +
                 "$failed of $total uploads failed.");
     }
 
     /**
      * Content of the <a href="http://twitter.github.com/bootstrap/components.html#alerts">alert</a> box
      * displayed when the upload has failed.
      *
      * <p/>
      * The message may use HTML markup, and may include some variables that are processed by JavaScript.
      * See {@link Upload class documentation} for the definition of these variables.
      *
      * <p/>
      * Default value:
      * <tt>&lt;strong&gt;Error!&lt;/strong&gt; $failed of $total uploads failed.</tt>
      *
      * @param errorMsgTemplate the message to be displayed.
      */
     public void setErrorMsgTemplate(String errorMsgTemplate) {
         getStateHelper().put(PropertyKeys.errorMsgTemplate, errorMsgTemplate);
     }
 
     public String getGlobalStatusTemplate() {
         return (String) getStateHelper().eval(PropertyKeys.globalStatusTemplate, "$progress% of $size uploaded.");
     }
 
     /**
      * Content of the status line that is displayed during the upload.
      *
      * <p/>
      * The message may use HTML markup, and may include some variables that are processed by JavaScript.
      * See {@link Upload class documentation} for the definition of these variables.
      *
      * <p/>
      * Default value: <tt>$progress% of $size uploaded.</tt>
      *
      * @param globalStatusTemplate content of the status line
      */
     public void setGlobalStatusTemplate(String globalStatusTemplate) {
         getStateHelper().put(PropertyKeys.globalStatusTemplate, globalStatusTemplate);
     }
 
     public String getUploadingLabel() {
         return (String) getStateHelper().eval(PropertyKeys.uploadingLabel, "Uploading");
     }
 
     /**
      * Label that is displayed during upload in the right column.
      *
      * <p/>
      * Default value: <tt>Uploading</tt>
      *
      * @param uploadingLabel label that is displayed during upload
      */
     public void setUploadingLabel(String uploadingLabel) {
         getStateHelper().put(PropertyKeys.uploadingLabel, uploadingLabel);
     }
 
     public String getDoneLabel() {
         return (String) getStateHelper().eval(PropertyKeys.doneLabel, "Done");
     }
 
     /**
      * Label that is displayed upon successful upload in the right column.
      *
      * <p/>
      * Default value: <tt>Done</tt>
      *
      * @param doneLabel label that is displayed upon successful upload
      */
     public void setDoneLabel(String doneLabel) {
         getStateHelper().put(PropertyKeys.doneLabel, doneLabel);
     }
     public String getFailedLabel() {
         return (String) getStateHelper().eval(PropertyKeys.failedLabel, "Failed");
     }
 
     /**
      * Label that is displayed upon failed upload in the right column.
      *
      * <p/>
      * Default value: <tt>Failed</tt>
      *
      * @param failedLabel label that is displayed upon failed upload
      */
     public void setFailedLabel(String failedLabel) {
         getStateHelper().put(PropertyKeys.failedLabel, failedLabel);
     }
 }
