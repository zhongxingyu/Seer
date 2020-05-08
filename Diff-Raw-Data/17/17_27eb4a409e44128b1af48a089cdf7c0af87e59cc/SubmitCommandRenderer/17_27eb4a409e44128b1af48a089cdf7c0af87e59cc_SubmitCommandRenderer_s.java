 /*
  * Copyright 2009-2011 Prime Technology.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.helix.mobile.component.submitcommand;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class SubmitCommandRenderer extends CoreRenderer {
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         SubmitCommand cmd = (SubmitCommand) component;
         this.encodeScript(context, cmd);
     }
     
     protected void encodeScript(FacesContext context, SubmitCommand cmd) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = cmd.getClientId();
         StringBuilder actions = new StringBuilder();
         
         actions.append("{");
         if (cmd.getSuccessAction() != null) {
             actions.append("success: function(data, textStatus, jqXHR){ ").append(cmd.getSuccessAction()).append("}");
         }
        if (cmd.getSuccessAction() != null && cmd.getErrorAction() != null) {
             actions.append(",");
         }
         if (cmd.getErrorAction() != null) {
             actions.append("error: function(jqXHR,textStatus,errorThrown){ ").append(cmd.getErrorAction()).append("}");
         }
         actions.append("}");
         
         startScript(writer, clientId);
 
         writer.write("function " + cmd.getName() + "(){ ");
         
         // Setup the widget.
         writer.write(MessageFormat.format("Helix.Ajax.ajaxFormSubmit(''{0}'', ''{1}'', ''{2}'', ''{3}'', ''{4}'', ''{5}'', {6});",
                 new Object[] {
                     cmd.getUrl(),
                     cmd.getForm(),
                     cmd.getStatusTitle() != null ? cmd.getStatusTitle() : "",
                     cmd.getSuccessMessage() != null ? cmd.getSuccessMessage() : "",
                     cmd.getPendingMessage() != null ? cmd.getPendingMessage() : "",
                     cmd.getErrorMessage() != null ? cmd.getErrorMessage() : "",
                     actions.toString()
                 }));
 
         writer.write("}");
         
         endScript(writer);
     }
 }
