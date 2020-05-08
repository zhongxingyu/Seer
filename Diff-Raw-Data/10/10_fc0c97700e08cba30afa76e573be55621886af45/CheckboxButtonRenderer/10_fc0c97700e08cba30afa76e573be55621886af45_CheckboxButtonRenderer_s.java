 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.checkboxbutton;
 
 
 
 import java.io.IOException;
 import java.lang.String;
 import java.util.*;
 
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.ConverterException;
 import javax.faces.render.Renderer;
 import javax.faces.event.ValueChangeEvent;
 
 import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.ace.util.ScriptWriter;
 import org.icefaces.ace.util.Utils;
 
 import org.icefaces.util.EnvUtils;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.ace.renderkit.CoreRenderer;
 
 @MandatoryResourceComponent(tagName="checkboxButton", value="org.icefaces.ace.component.checkboxbutton.CheckboxButton")
 public class CheckboxButtonRenderer extends CoreRenderer {
     List <UIParameter> uiParamChildren;
 
     private enum EventType {
         HOVER, FOCUS
     }
 
     public void decode(FacesContext facesContext, UIComponent uiComponent) {
         Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
         CheckboxButton checkbox = (CheckboxButton) uiComponent;
         String clientId = uiComponent.getClientId();
         String hiddenValue = String.valueOf(requestParameterMap.get(clientId+"_hidden"));
 
         if (null==hiddenValue || hiddenValue.equals("null")){
             return;
         }else {
             boolean submittedValue = isChecked(hiddenValue);
             checkbox.setSubmittedValue(submittedValue);
         }
 
         decodeBehaviors(facesContext, checkbox);
     }
 
 
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
     throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         CheckboxButton checkbox = (CheckboxButton) uiComponent;
         String clientId = uiComponent.getClientId(facesContext);
         String firstWrapperClass = "yui-button yui-checkboxbutton-button ui-button ui-widget ui-state-default";
         String secondWrapperClass = "first-child";
         boolean ariaEnabled = EnvUtils.isAriaEnabled(facesContext);
         uiParamChildren = Utils.captureParameters(checkbox);
 
         // Root Container
         writer.startElement(HTML.DIV_ELEM, uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, clientId, null);
 
         encodeScript(facesContext, writer, checkbox, clientId, EventType.HOVER);
         encodeRootStyle(writer, checkbox);
 
         // First Wrapper
         writer.startElement(HTML.SPAN_ELEM, uiComponent);
         writer.writeAttribute(HTML.CLASS_ATTR, firstWrapperClass, null);
 
         // Second Wrapper
         writer.startElement(HTML.SPAN_ELEM, uiComponent);
         writer.writeAttribute(HTML.CLASS_ATTR, secondWrapperClass, null);
 
         if (ariaEnabled)
             encodeAriaAttributes(writer, checkbox);
 
         // Button Element
         writer.startElement(HTML.BUTTON_ELEM, uiComponent);
         writer.writeAttribute(HTML.TYPE_ATTR, "button", null);
         writer.writeAttribute(HTML.NAME_ATTR, clientId+"_button", null);
 
         encodeButtonTabIndex(writer, checkbox, ariaEnabled);
         encodeButtonStyle(writer, checkbox);
         encodeScript(facesContext, writer, checkbox, clientId, EventType.FOCUS);
 
         renderPassThruAttributes(facesContext, checkbox, HTML.BUTTON_ATTRS, new String[]{"style"});
 
         // Another Damn Element. Previously added dynamically with JS.
         writer.startElement(HTML.SPAN_ELEM, null);
 
         encodeIconStyle(writer, checkbox);
 
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.BUTTON_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     private void encodeAriaAttributes(ResponseWriter writer, CheckboxButton checkbox) throws IOException {
         writer.writeAttribute(HTML.ROLE_ATTR, "checkbox", null);
         writer.writeAttribute(HTML.ARIA_DESCRIBED_BY_ATTR, checkbox.getLabel(), null);
         writer.writeAttribute(HTML.ARIA_DISABLED_ATTR, checkbox.isDisabled(), null);
     }
 
     private void encodeButtonTabIndex(ResponseWriter writer, CheckboxButton checkbox, boolean ariaEnabled) throws IOException {
         Integer tabindex = checkbox.getTabindex();
 
         if (ariaEnabled && tabindex == null)
             tabindex = 0;
 
         if (tabindex != null)
             writer.writeAttribute(HTML.TABINDEX_ATTR, tabindex, null);
     }
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
     throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         CheckboxButton checkbox = (CheckboxButton) uiComponent;
         Object val = checkbox.getValue();
 
         writer.startElement("input", uiComponent);
         writer.writeAttribute("type", "hidden", null);
         writer.writeAttribute("name",clientId+"_hidden", null);
         writer.writeAttribute("value",val, null);
         writer.endElement("input");
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void encodeScript(FacesContext facesContext, ResponseWriter writer,
                               CheckboxButton checkbox, String clientId, EventType type) throws IOException {
         boolean ariaEnabled = EnvUtils.isAriaEnabled(facesContext);
         JSONBuilder jb = JSONBuilder.create();
         jb.beginFunction("ice.ace.lazy")
           .item("checkboxbutton")
           .beginArray()
           .item(clientId)
           .beginMap()
           .entry("ariaEnabled", ariaEnabled);
 
         if (checkbox.isDisabled())
             jb.entry("disabled", true);
 
         if (uiParamChildren != null) {
             jb.beginMap("uiParams");
             for (UIParameter p : uiParamChildren)
                 jb.entry(p.getName(), (String)p.getValue());
             jb.endMap();
         }
 
         encodeClientBehaviors(facesContext, checkbox, jb);
 
         jb.endMap().endArray().endFunction();
 
         String eventType = "";
         if (EventType.HOVER.equals(type))
             eventType = HTML.ONMOUSEOVER_ATTR;
         else if (EventType.FOCUS.equals(type))
             eventType = HTML.ONFOCUS_ATTR;
 
         writer.writeAttribute(eventType, jb.toString(), null);
     }
 
     private String findCheckboxLabel(CheckboxButton checkbox){
         String label="";
         String checkLabel = checkbox.getLabel();
         if (null!=checkLabel && !checkLabel.equals("")){
             label=checkLabel;
         }
         return label;
     }
 
 
     /**
      * support similar return values as jsf component
      * so can use strings true/false, on/off, yes/no to
      * support older browsers
      * @param hiddenValue
      * @return
      */
     private boolean isChecked(String hiddenValue) {
         return hiddenValue.equalsIgnoreCase("true") ||
                hiddenValue.equalsIgnoreCase("on") ||
                hiddenValue.equalsIgnoreCase("yes");
     }
 
     //forced converter support. It's either a boolean or string.
     @Override
     public Object getConvertedValue(FacesContext facesContext, UIComponent uiComponent,
                                     Object submittedValue) throws ConverterException{
         if (submittedValue instanceof Boolean) {
             return submittedValue;
         }
         else {
             return Boolean.valueOf(submittedValue.toString());
         }
     }
 
     private void encodeButtonStyle(ResponseWriter writer, CheckboxButton checkbox) throws IOException {
         String buttonClasses = "";
         String selectedClass = "ui-state-active";
         String disabledClass = "ui-state-disabled";
         Boolean val = (Boolean)checkbox.getValue();
 
        if (val) {
             buttonClasses += selectedClass + " ";
        } else if (checkbox.isDisabled()) {
             buttonClasses += disabledClass + " ";
         }
 
         if (!buttonClasses.equals("")) {
             writer.writeAttribute(HTML.CLASS_ATTR, buttonClasses.trim(), null);
         }
     }
 
     private void encodeIconStyle(ResponseWriter writer, CheckboxButton checkbox) throws IOException {
         String iconClass = "ui-icon";
         String selectedStyle = "ui-icon-check";
         String unselectedStyle = "ui-icon-unchecked";
         Boolean val = (Boolean)checkbox.getValue();
 
        if (val) {
             iconClass += " " + selectedStyle;
         } else {
             iconClass += " " + unselectedStyle;
         };
 
         writer.writeAttribute(HTML.CLASS_ATTR, iconClass, null);
     }
 
     private void encodeRootStyle(ResponseWriter writer, CheckboxButton checkbox) throws IOException {
         String styleClass = checkbox.getStyleClass();
         String styleClassVal = "ice-checkboxbutton";
         String style = checkbox.getStyle();
 
         if (styleClass != null && styleClass.trim().length() > 0)
             styleClassVal += " " + styleClass;
 
         if (style != null && style.trim().length() > 0)
             writer.writeAttribute(HTML.STYLE_ATTR, style, HTML.STYLE_ATTR);
 
         writer.writeAttribute(HTML.CLASS_ATTR, styleClassVal, null);
     }
 }
