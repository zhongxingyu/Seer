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
 package org.primefaces.mobile.renderkit;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
 import org.primefaces.renderkit.InputRenderer;
 import org.primefaces.util.ComponentUtils;
 
 public class SelectBooleanCheckboxRenderer extends InputRenderer {
 
     @Override
 	public void decode(FacesContext context, UIComponent component) {
 		SelectBooleanCheckbox checkbox = (SelectBooleanCheckbox) component;
 
         if(checkbox.isDisabled()) {
             return;
         }
 
         decodeBehaviors(context, checkbox);
 
 		String clientId = checkbox.getClientId(context);
 		String submittedValue = (String) context.getExternalContext().getRequestParameterMap().get(clientId + "_input");
 
         if(submittedValue != null && submittedValue.equalsIgnoreCase("on")) {
             checkbox.setSubmittedValue("true");
         }
         else {
             checkbox.setSubmittedValue("false");
         }
 	}
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         SelectBooleanCheckbox checkbox = (SelectBooleanCheckbox) component;
 
         encodeMarkup(context, checkbox);
        encodeScript(context, checkbox);
     }
 
     protected void encodeMarkup(FacesContext context, SelectBooleanCheckbox checkbox) throws IOException {        
         ResponseWriter writer = context.getResponseWriter();
         String clientId = checkbox.getClientId(context);
         boolean checked = Boolean.valueOf(ComponentUtils.getValueToRender(context, checkbox));
         boolean disabled = checkbox.isDisabled();
         String label = checkbox.getLabel();
         
         writer.startElement("div", checkbox);
         writer.writeAttribute("id", clientId, "id");
 
         if(checkbox.getStyle() != null) writer.writeAttribute("style", checkbox.getStyle(), "style");
         if(checkbox.getStyleClass() != null) writer.writeAttribute("class", checkbox.getStyleClass(), "styleClass");
         
         if(label == null) {
             encodeInput(context, checkbox, clientId, checked, disabled);
             encodeLabel(context, checkbox, clientId);
         }
         else {
             writer.writeAttribute("data-role", "fieldcontain", null);
             
             writer.startElement("fieldset", null);
             writer.writeAttribute("data-role", "controlgroup", null);
             
             writer.startElement("legend", null);
             writer.writeText(label, "label");
             writer.endElement("legend");
             
             encodeInput(context, checkbox, clientId, checked, disabled);
             encodeLabel(context, checkbox, clientId);
             
             writer.endElement("fieldset");
         }
         
         writer.endElement("div");
     }
 
     protected void encodeInput(FacesContext context, SelectBooleanCheckbox checkbox, String clientId, boolean checked, boolean disabled) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String inputId = clientId + "_input";
                 
         writer.startElement("input", null);
         writer.writeAttribute("id", inputId, "id");
         writer.writeAttribute("name", inputId, null);
         writer.writeAttribute("type", "checkbox", null);
 
         if(checked) writer.writeAttribute("checked", "checked", null);
         if(disabled) writer.writeAttribute("disabled", "disabled", null);
         if(checkbox.getOnchange() != null) writer.writeAttribute("onchange", checkbox.getOnchange(), null);
 
         writer.endElement("input");
     }
 
     protected void encodeLabel(FacesContext context, SelectBooleanCheckbox checkbox, String clientId) throws IOException {
         String itemLabel = checkbox.getItemLabel();
         
         if(itemLabel != null) {
             ResponseWriter writer = context.getResponseWriter();
 
             writer.startElement("label", null);
             writer.writeAttribute("for", clientId + "_input", null);
             writer.writeText(itemLabel, "itemLabel");
             writer.endElement("label");
         }
     }
    
    protected void encodeScript(FacesContext context, SelectBooleanCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = checkbox.getClientId(context);

        startScript(writer, clientId);
        
        writer.write("PrimeFaces.cw('SelectBooleanCheckbox','" + checkbox.resolveWidgetVar() + "',{");
        writer.write("id:'" + clientId + "'");
        
        encodeClientBehaviors(context, checkbox);

        writer.write("});");

        endScript(writer);
    }
 }
