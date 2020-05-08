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
 package org.primefaces.mobile.component.loadcommand;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.PhaseId;
 import org.primefaces.mobile.model.JSONSerializer;
 import org.primefaces.renderkit.CoreRenderer;
 import org.primefaces.util.ComponentUtils;
 
 public class LoadCommandRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         LoadCommand cmd = (LoadCommand) component;
 
         if(context.getExternalContext().getRequestParameterMap().containsKey(cmd.getClientId(context) + "_reload")) {
             ActionEvent event = new ActionEvent(cmd);
             /* Skip the JSF lifecycle so that we don't waste time trying to validate the contents
              * of an empty form ...
              */
             event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
             cmd.queueEvent(event);
         }
     }
     
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         LoadCommand cmd = (LoadCommand) component;
         if (context.getExternalContext().getRequestParameterMap().containsKey(cmd.getClientId(context) + "_reload")) {
             /* This is the result of a partial update to reload the data. Return raw JSON. This will
              * be stored inside of a script tag returned by the partial update.
              */
             ResponseWriter writer = context.getResponseWriter();
             JSONSerializer s = new JSONSerializer();
             try {
                 if (cmd.getError() != null) {
                     writer.write("{ 'error' : '" + cmd.getError() + "'}");
                 } else {
                     writer.write(s.serializeObject(cmd.getValue()));
                 }
             } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                 Logger.getLogger(LoadCommandRenderer.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else {
             this.encodeMarkup(context, cmd);
             this.encodeScript(context, cmd);
         }
     }
     
     protected void encodeMarkup(FacesContext context, LoadCommand cmd) throws IOException {
         
     }
     
     protected void encodeScript(FacesContext context, LoadCommand cmd) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = cmd.getClientId();
         UIComponent form = (UIComponent) ComponentUtils.findParentForm(context, cmd);
         if(form == null) {
             throw new FacesException("LoadCommand '" + cmd.getName() + "'must be inside a form.");
         }
         
         StringBuilder onComplete = new StringBuilder();
         if (cmd.getOncomplete() != null) {
             onComplete.append("function (statusText) {").append(cmd.getOncomplete()).append("}");
         } else {
             onComplete.append("null");
         }
        
         String schema = "null";
         if (cmd.getOfflineSave().equals("true")) {
             JSONSerializer s = new JSONSerializer();
             schema = s.serializeObjectSchema(cmd.getValue().getClass());
         }
         
         // Global variables populated by this load.
         String widgetName = "window." + cmd.resolveWidgetVar();
         String widgetSchemaName = "window." + cmd.resolveWidgetVar() + "_schema";
         
         // Output empty form to POST for this command.
         String formId = form.getClientId(context);
         
         startScript(writer, clientId);
         writer.write(widgetName + " = null;");
    
         // Create DB schema
         writer.write(widgetSchemaName + " = generatePersistenceSchema(");
         writer.write(schema);
         writer.write(", '");
         writer.write(cmd.resolveWidgetVar());
         writer.write("');");
         
        writer.write("function " + cmd.getName() + "(params){ ");
         
         // Setup the widget.
        writer.write(MessageFormat.format("PrimeFaces.Utils.ajaxBeanLoad(''{0}'', ''{1}'', {2}, ''{3}'', {4}, params);",
                 new Object[] {
                     clientId,
                     formId,
                     widgetSchemaName,
                     cmd.resolveWidgetVar(),
                     onComplete.toString()
                 }));
 
         writer.write("}");
         
         endScript(writer);
     }
 }
