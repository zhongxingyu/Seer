 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
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
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.progressbar;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.el.MethodExpression;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.icefaces.ace.context.RequestContext;
 import org.icefaces.ace.event.ProgressBarChangeEvent;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="progressBar", value="org.icefaces.ace.component.progressbar.ProgressBar")
 public class ProgressBarRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext facesContext, UIComponent component) {
         ProgressBar progressBar = (ProgressBar) component;
         String clientId = progressBar.getClientId(facesContext);
         Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
 
         if(params.containsKey(clientId)) {
             if (params.containsKey(clientId + "_cancel")) {
                progressBar.getValueExpression("value").setValue(facesContext.getELContext(), 0);
 
                 if (progressBar.getCancelListener() != null) {
                     progressBar.getCancelListener().invoke(facesContext.getELContext(), null);
                 }
 
             } else if (params.containsKey(clientId + "_complete")) {
                 if (progressBar.getCompleteListener() != null) {
                     progressBar.getCompleteListener().invoke(facesContext.getELContext(), null);
                 }
 
             } else if (params.containsKey(clientId + "_change")) {
                 MethodExpression changeListener = progressBar.getChangeListener();
                 if (changeListener != null) {
                     ProgressBarChangeEvent event = new ProgressBarChangeEvent(component, params.get(clientId + "_value"), params.get(clientId + "_percentage"));
                     changeListener.invoke(facesContext.getELContext(), new Object[]{event});
                 }
 
             } else {
                 RequestContext.getCurrentInstance().addCallbackParam(progressBar.getClientId(facesContext) + "_value", progressBar.getValue());
 
             }
         }
         decodeBehaviors(facesContext, progressBar);
     }
 
     @Override
     public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
         ProgressBar progressBar = (ProgressBar) component;
 
         encodeMarkup(facesContext, progressBar);
     }
 
     protected void encodeMarkup(FacesContext facesContext, ProgressBar progressBar) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
 
         writer.startElement("div", progressBar);
         writer.writeAttribute("id", progressBar.getClientId(facesContext), "id");
 
         String style = progressBar.getStyle();
         if (style != null) {
             writer.writeAttribute("style", style, "style");
         }
         String styleClass = progressBar.getStyleClass();
         if (styleClass != null) {
             writer.writeAttribute("class", styleClass, "styleClass");
         }
 		
 		encodeScript(facesContext, progressBar);
 
         writer.endElement("div");
     }
 
     protected void encodeScript(FacesContext facesContext, ProgressBar progressBar) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = progressBar.getClientId(facesContext);
         boolean hasChangeListener = progressBar.getChangeListener() != null;
 
         writer.startElement("script", progressBar);
         writer.writeAttribute("type", "text/javascript", null);
 
         writer.write("ice.ace.jq(function() {");
 
         writer.write(this.resolveWidgetVar(progressBar) + " = new ");
         JSONBuilder json = JSONBuilder.create();
         json.beginFunction("ice.ace.ProgressBar").
             item(clientId).
             beginMap().
                 entry("value", progressBar.getValue());
 
                 if(progressBar.isUsePolling()) {
                     json.entry("usePolling", true);
                     json.entry("pollingInterval", progressBar.getPollingInterval());
                 } else {
                     json.entry("usePolling", false);
                 }
 
                 if(progressBar.isDisabled()) {
                     json.entry("disabled", true);
                 }
                 json.entry("hasChangeListener", hasChangeListener);
                 encodeClientBehaviors(facesContext, progressBar, json);
             json.endMap();
         json.endFunction();
         writer.write(json.toString());
 
         writer.write("});");
 
         writer.endElement("script");
     }
 }
