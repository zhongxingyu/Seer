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
 package org.primefaces.mobile.component.inputrange;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.mobile.util.MobileUtils;
 import org.primefaces.renderkit.InputRenderer;
 import org.primefaces.util.ComponentUtils;
 
 public class InputRangeRenderer extends InputRenderer {
     
     @Override
 	public void decode(FacesContext context, UIComponent component) {
 		InputRange inputRange = (InputRange) component;
         String clientId = inputRange.getClientId(context);
         String inputId = inputRange.getLabel() == null ? clientId : clientId + "_input";
 
 		String submittedValue = (String) context.getExternalContext().getRequestParameterMap().get(inputId);
 
         if(submittedValue != null) {
             inputRange.setSubmittedValue(submittedValue);
         }
 	}
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         InputRange range = (InputRange) component;
         String clientId = range.getClientId(context);
         String label = range.getLabel();
         
         String inputId = label == null ? clientId : clientId + "_input";
 
         if(label == null) {
             encodeInput(context, range, inputId);
         } 
         else {
             writer.startElement("div", range);
             writer.writeAttribute("id", clientId, null);
             writer.writeAttribute("data-role", "fieldcontain", null);
                         
             writer.startElement("label", null);
             writer.writeAttribute("for", inputId, null);
             writer.writeText(label, "label");
             writer.endElement("label");
             
             encodeInput(context, range, inputId);
             
             writer.endElement("div");
         }
         
     }
     
     protected void encodeInput(FacesContext context, InputRange range, String inputId) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String valueToRender = ComponentUtils.getValueToRender(context, range);
         
         writer.startElement("input", null);
         writer.writeAttribute("id", inputId, null);
         writer.writeAttribute("name", inputId, null);
        writer.writeAttribute("type", "number", null);
        writer.writeAttribute("data-type", "range", null);
         writer.writeAttribute("min", range.getMinValue(), null);
         writer.writeAttribute("max", range.getMaxValue(), null);
         writer.writeAttribute("step", range.getStep(), null);
         if(MobileUtils.isMini(context)) {
             writer.writeAttribute("data-mini", "true", null);
         }
         
         if(range.isDisabled()) {
             writer.writeAttribute("disabled", "disabled", "disabled");
         }
 
         if(valueToRender != null) {
 			writer.writeAttribute("value", valueToRender , null);
 		}
 
         writer.endElement("input");
     }
 }
