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
 
 import net.mymam.upload.UploadMultipartRequestWrapper;
 import org.apache.commons.fileupload.FileItem;
 
 import javax.faces.component.EditableValueHolder;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.FacesRenderer;
 import javax.faces.render.Renderer;
 import javax.servlet.ServletRequestWrapper;
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 /**
  * Renderer for the {@link Upload} component.
  *
  * <p/>
  * Most of the HTML rendered here is initially invisible, using <tt>style="display: none"</tt>.
 * The invisible tags are templates that are used by <tt>mymam-upload.js</tt>.
  *
  * @see Upload
  * @author fstab
  */
 @FacesRenderer(componentFamily = UIInput.COMPONENT_FAMILY, rendererType = "net.mymam.ui.UploadRenderer")
 public class UploadRenderer extends Renderer {
 
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         if ( component.isRendered() ) {
             ResponseWriter writer = context.getResponseWriter();
             String clientId = component.getClientId(context);
             renderMainButtons(clientId, writer, (Upload) component);
             renderStatusMessageTemplates(writer, (Upload) component);
             renderGlobalStatusLineTemplate(writer, (Upload) component);
             renderUploadInfoTable(writer, (Upload) component);
         }
     }
 
     private void renderMainButtons(String clientId, ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("div", component);
         writer.writeAttribute("class", "main-buttons", null);
         renderAddFilesButton(clientId, writer, component);
         renderClearButton(writer, component);
         renderSubmitButton(writer, component);
         writer.endElement("div");
     }
 
     private void renderAddFilesButton(String clientId, ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("span", component);
         writer.writeAttribute("class", "btn fileinput-button btn-primary", null);
         writer.startElement("i", component);
         writer.writeAttribute("class", "icon-plus", null);
         writer.endElement("i");
         writer.startElement("span", component);
         writer.write(component.getAddFilesButtonLabel());
         writer.endElement("span");
         writer.startElement("input", component);
         writer.writeAttribute("type", "file", null);
         writer.writeAttribute("name", clientId, null);
         writer.writeAttribute("multiple", "multiple", null);
         writer.endElement("input");
         writer.endElement("span");
     }
 
     private void renderClearButton(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("input", component);
         writer.writeAttribute("type", "button", null);
         writer.writeAttribute("value", component.getClearButtonLabel(), null);
         writer.writeAttribute("class", "btn clear-button", null);
         writer.endElement("input");
     }
 
     private void renderSubmitButton(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("input", component);
         writer.writeAttribute("type", "button", null);
         writer.writeAttribute("value", component.getSubmitButtonLabel(), null);
         writer.writeAttribute("class", "btn submit-button", null);
         writer.endElement("input");
     }
 
     private void renderStatusMessageTemplates(ResponseWriter writer, Upload component) throws IOException {
         renderStatusMessageTemplate(writer, component, "alert alert-success upload-info-all-done", component.getSuccessMsgTemplate());
         renderStatusMessageTemplate(writer, component, "alert alert-block upload-info-all-warn", component.getWarnMsgTemplate());
         renderStatusMessageTemplate(writer, component, "alert alert-error upload-info-all-error", component.getErrorMsgTemplate());
     }
 
     private void renderStatusMessageTemplate(ResponseWriter writer, Upload component, String styleClass, String msg) throws IOException {
         writer.startElement("div", component);
         writer.writeAttribute("class", styleClass, null);
         writer.writeAttribute("style", "display: none", null);
         writer.startElement("button", component);
         writer.writeAttribute("type", "button", null);
         writer.writeAttribute("class", "close", null);
         writer.writeAttribute("data-dismiss", "alert", null);
         writer.write("&times;");
         writer.endElement("button");
         writer.write(msg);
         writer.endElement("div");
     }
 
     private void renderGlobalStatusLineTemplate(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("p", component);
         writer.writeAttribute("class", "upload-info-status lead", null);
         writer.startElement("span", component);
         writer.writeAttribute("style", "display: none;", null);
         writer.write(component.getGlobalStatusTemplate());
         writer.endElement("span");
         writer.endElement("p");
     }
 
     private void renderUploadInfoTable(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("table", component);
         writer.writeAttribute("class", "table table-striped", null);
         writer.startElement("tbody", component);
         writer.startElement("tr", component);
         writer.writeAttribute("style", "display: none", null);
         renderColumnOne(writer, component);
         renderColumnTwo(writer, component);
         renderColumnThree(writer, component);
         renderColumnFour(writer, component);
         writer.endElement("tr");
         writer.endElement("tbody");
         writer.endElement("table");
     }
 
     private void renderColumnOne(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("td", component);
         writer.writeAttribute("class", "upload-info-line-number", null);
         writer.write("1");
         writer.endElement("td");
     }
 
     private void renderColumnTwo(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("td", component);
         writer.writeAttribute("class", "upload-info-file-name", null);
         writer.startElement("div", component);
         writer.write("video.mov");
         writer.endElement("div");
         writer.startElement("div", component);
         writer.writeAttribute("class", "progress progress-striped", null);
         writer.startElement("div", component);
         writer.writeAttribute("class", "bar", null);
         writer.writeAttribute("style", "width: 0%;", null);
         writer.endElement("div");
         writer.endElement("div");
         writer.endElement("td");
     }
 
     private void renderColumnThree(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("td", component);
         writer.writeAttribute("class", "upload-info-file-size", null);
         writer.write("1.5M");
         writer.endElement("td");
     }
 
     private void renderColumnFour(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("td", component);
         writer.writeAttribute("class", "upload-info-status-column", null);
         renderRemoveButton(writer, component);
         renderLabel(writer, component, "label label-info upload-info-status-uploading", "icon-arrow-up", component.getUploadingLabel());
         renderLabel(writer, component, "label label-success upload-info-status-done", "icon-ok-sign", component.getDoneLabel());
         renderLabel(writer, component, "label label-important upload-info-status-failed", "icon-warning-sign", component.getFailedLabel());
         writer.endElement("td");
     }
 
     private void renderRemoveButton(ResponseWriter writer, Upload component) throws IOException {
         writer.startElement("button", component);
         writer.writeAttribute("class", "btn upload-info-status-remove", null);
         writer.startElement("i", component);
         writer.writeAttribute("class", "icon-remove-circle", null);
         writer.endElement("i");
         writer.startElement("span", component);
         writer.write("&nbsp;" + component.getRemoveButtonLabel());
         writer.endElement("span");
         writer.endElement("button");
     }
 
     private void renderLabel(ResponseWriter writer, Upload component, String styleClass, String icon, String msg) throws IOException {
         writer.startElement("span", component);
         writer.writeAttribute("class", styleClass, null);
         writer.writeAttribute("style", "display: none;", null);
         writer.startElement("i", component);
         writer.writeAttribute("class", icon, null);
         writer.endElement("i");
         writer.startElement("span", component);
         writer.write("&nbsp;" + msg);
         writer.endElement("span");
         writer.endElement("span");
     }
 
     /**
      * Handle the postback of a form containing an {@link Upload} component.
      *
      * <p/>
      * If the request contains a {@link FileItem}, set the corresponding
      * value in the backing bean.
      *
      * @param context {@link FacesContext} for the request we are processing.
      * @param component {@link UIComponent} to be decoded.
      */
     @Override
     public void decode(FacesContext context, UIComponent component) {
         UploadMultipartRequestWrapper requestWrapper = getMultiPartRequestInChain(context);
         if (requestWrapper != null) { // if we are processing a multipart request
             String clientId = component.getClientId(context);
             FileItem fileItem = requestWrapper.getFileItem(clientId);
             if (fileItem != null) { // if the multipart request has a file item
                 String encoding = findEncoding(requestWrapper);
                 UploadedFile uploadedFile = new UploadedFile(fileItem, encoding);
                 ((EditableValueHolder) component).setSubmittedValue(uploadedFile);
                 ((EditableValueHolder) component).setValid(true);
             }
         }
     }
 
     // Either the encoding is sent in the request, or we take the JVM's default encoding.
     private String findEncoding(UploadMultipartRequestWrapper request) {
         String encoding = request.getCharacterEncoding();
         if ( encoding == null ) {
             encoding = System.getProperty("file.encoding");
         }
         if ( encoding == null ) {
             encoding = Charset.defaultCharset().toString();
         }
         return encoding;
     }
 
     // Finds our MultipartRequestServletWrapper in case application contains other RequestWrappers.
     // This method is taken from PrimeFaces' implementation.
     private UploadMultipartRequestWrapper getMultiPartRequestInChain(FacesContext facesContext) {
         Object request = facesContext.getExternalContext().getRequest();
         while (request instanceof ServletRequestWrapper) {
             if (request instanceof UploadMultipartRequestWrapper) {
                 return (UploadMultipartRequestWrapper) request;
             } else {
                 request = ((ServletRequestWrapper) request).getRequest();
             }
         }
         return null;
     }
 
 }
