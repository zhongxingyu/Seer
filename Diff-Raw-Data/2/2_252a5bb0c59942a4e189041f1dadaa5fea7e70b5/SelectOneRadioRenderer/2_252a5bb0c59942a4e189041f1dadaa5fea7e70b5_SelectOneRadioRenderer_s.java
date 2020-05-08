 /*
  * Copyright 2009-2012 Prime Teknoloji.
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
 import java.util.List;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UINamingContainer;
 import javax.faces.component.UISelectOne;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.model.SelectItem;
 import org.primefaces.component.selectoneradio.SelectOneRadio;
 import org.primefaces.renderkit.SelectOneRenderer;
 
 public class SelectOneRadioRenderer extends SelectOneRenderer {
 
     @Override
 	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
         return context.getRenderKit().getRenderer("javax.faces.SelectOne", "javax.faces.Radio").getConvertedValue(context, component, submittedValue);
 	}
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         SelectOneRadio radio = (SelectOneRadio) component;
         
         encodeMarkup(context, radio);
         encodeScript(context, radio);
     }
     
     protected void encodeMarkup(FacesContext context, SelectOneRadio radio) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = radio.getClientId(context);
         String style = radio.getStyle();
         String styleClass = radio.getStyleClass();
         
         List<SelectItem> selectItems = getSelectItems(context, radio);
 
         writer.startElement("div", radio);
         writer.writeAttribute("id", clientId, "id");
         writer.writeAttribute("data-role", "fieldcontain", null);
 
         if(style != null) writer.writeAttribute("style", style, "style");
         if(styleClass != null) writer.writeAttribute("class", styleClass, "styleClass");
 
         encodeSelectItems(context, radio, selectItems);
 
        writer.endElement("table");
     }
     
     protected void encodeScript(FacesContext context, SelectOneRadio radio) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = radio.getClientId(context);
 
         startScript(writer, clientId);
         
         writer.write("PrimeFaces.cw('SelectOneRadio','" + radio.resolveWidgetVar() + "',{");
         writer.write("id:'" + clientId + "'");
 
         encodeClientBehaviors(context, radio);
 
         writer.write("});");
 
         endScript(writer);
     }
     
     protected void encodeSelectItems(FacesContext context, SelectOneRadio radio, List<SelectItem> selectItems) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         Converter converter = getConverter(context, radio);
         Object value = radio.getSubmittedValue();
         String name = radio.getClientId(context);
         if(value == null) {
             value = radio.getValue();
         }
         Class type = value == null ? String.class : value.getClass();
         
         String layout = radio.getLayout();
         boolean horizontal = layout != null && layout.equals("lineDirection");
         
         writer.startElement("fieldset", null);
         writer.writeAttribute("data-role", "controlgroup", null);
         if(horizontal) {
             writer.writeAttribute("data-type", "horizontal", null);
         }
         
         if(radio.getLabel() != null) {
             writer.startElement("legend", null);
             writer.writeText(radio.getLabel(), null);
             writer.endElement("legend");
         }
         
         int idx = -1;
         for(SelectItem selectItem : selectItems) {
             idx++;
             boolean disabled = selectItem.isDisabled() || radio.isDisabled();
             String id = name + UINamingContainer.getSeparatorChar(context) + idx;
             Object coercedItemValue = coerceToModelType(context, selectItem.getValue(), type);
             boolean selected = (coercedItemValue != null) && coercedItemValue.equals(value);
             
             encodeOption(context, radio, selectItem, id, name, converter, selected, disabled);
         }
         
         writer.endElement("fieldset");
     }
     
     protected void encodeOption(FacesContext context, SelectOneRadio radio, SelectItem option, String id, String name, Converter converter, boolean selected, boolean disabled) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String itemValueAsString = getOptionAsString(context, radio, converter, option.getValue());
        
         writer.startElement("input", null);
         writer.writeAttribute("id", id, null);
         writer.writeAttribute("name", name, null);
         writer.writeAttribute("type", "radio", null);
         writer.writeAttribute("value", itemValueAsString, null);
 
         if(selected) writer.writeAttribute("checked", "checked", null);
         if(disabled) writer.writeAttribute("disabled", "disabled", null);
         if(radio.getOnchange() != null) writer.writeAttribute("onchange", radio.getOnchange(), null);
 
         writer.endElement("input");
         
         writer.startElement("label", null);
         writer.writeAttribute("for", id, null);
         
         if(option.isEscape())
             writer.writeText(option.getLabel(),null);
         else
             writer.write(option.getLabel());
         
         writer.endElement("label");
     }
     
     @Override
     protected String getSubmitParam(FacesContext context, UISelectOne selectOne) {
         return selectOne.getClientId(context);
     }
     
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Do nothing
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
     
 }
