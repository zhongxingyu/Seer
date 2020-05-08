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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.component.growl.Growl;
 import org.primefaces.context.RequestContext;
 import org.primefaces.renderkit.UINotificationRenderer;
 
 public class GrowlRenderer extends UINotificationRenderer {
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         Growl uiGrowl = (Growl) component;
         ResponseWriter writer = context.getResponseWriter();
         String clientId = uiGrowl.getClientId(context);
         Map<String, List<FacesMessage>> messagesMap = new HashMap<String, List<FacesMessage>>();
 
         String _for = uiGrowl.getFor();
         Iterator<FacesMessage> messages;
         if (_for != null) {
             messages = context.getMessages(_for);
         } else {
             messages = uiGrowl.isGlobalOnly() ? context.getMessages(null) : context.getMessages();
         }
 
         while (messages.hasNext()) {
             FacesMessage message = messages.next();
             FacesMessage.Severity severity = message.getSeverity();
 
             if (severity.equals(FacesMessage.SEVERITY_INFO)) {
                 addMessage(uiGrowl, message, messagesMap, "info");
             } else if (severity.equals(FacesMessage.SEVERITY_WARN)) {
                addMessage(uiGrowl, message, messagesMap, "warn");
             } else if (severity.equals(FacesMessage.SEVERITY_ERROR)) {
                addMessage(uiGrowl, message, messagesMap, "error");
             } else if (severity.equals(FacesMessage.SEVERITY_FATAL)) {
                addMessage(uiGrowl, message, messagesMap, "fatal");
             }
         }
 
         writer.startElement("div", uiGrowl);
         writer.writeAttribute("id", clientId, "id");
         writer.startElement("div", null);
         writer.writeAttribute("id", clientId + "_popup", "id");
         writer.writeAttribute("data-role", "popup", null);
         writer.writeAttribute("data-transition", "fade", null);
         writer.writeAttribute("data-theme", "a", null);
 
         Boolean showPopup = false;
         for (String severity : messagesMap.keySet()) {
             List<FacesMessage> severityMessages = messagesMap.get(severity);
 
             if (severityMessages.size() > 0) {
                 encodeSeverityMessages(context, uiGrowl, severity, severityMessages);
                 showPopup = true;
             }
         }
 
         writer.endElement("div");
         writer.endElement("div");
 
         if (showPopup) {
             showGrowl(clientId + "_popup", uiGrowl.getLife(), uiGrowl.isSticky());
         }
 
     }
 
     protected void showGrowl(String id, int time, boolean sticky) {
         RequestContext requestContext = RequestContext.getCurrentInstance();
         String command = "PrimeFaces.showGrowl('" + id + "'," + time + "," + sticky + ")";
         requestContext.execute(command);
     }
 
     protected void addMessage(Growl uiGrowl, FacesMessage message, Map<String, List<FacesMessage>> messagesMap, String severity) {
         if (shouldRender(uiGrowl, message, severity)) {
             List<FacesMessage> severityMessages = messagesMap.get(severity);
 
             if (severityMessages == null) {
                 severityMessages = new ArrayList<FacesMessage>();
                 messagesMap.put(severity, severityMessages);
             }
 
             severityMessages.add(message);
         }
     }
 
     protected void encodeSeverityMessages(FacesContext context, Growl uiGrowl, String severity, List<FacesMessage> messages) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         boolean escape = uiGrowl.isEscape();
 
 
         for (FacesMessage msg : messages) {
             String summary = msg.getSummary() != null ? msg.getSummary() : "";
             String detail = msg.getDetail() != null ? msg.getDetail() : summary;
            String icon = null;
            
            if (severity.equals("info")) icon = "info";
            if (severity.equals("warn")) icon = "alert";            
            if (severity.equals("error")) icon = "delete";            
            if (severity.equals("fatal")) icon = "minus";                     
 
             writer.startElement("p", null);
 
             writer.startElement("span", null);
            writer.writeAttribute("class", "ui-icon ui-icon-" + icon, null);
             writer.writeAttribute("style", "float: left;margin-right: 5px;", null);
             writer.endElement("span");
 
             if (uiGrowl.isShowSummary()) {
                 if (escape) {
                     writer.startElement("b", null);
                     writer.writeText(summary, null);
                     writer.endElement("b");
                 } else {
                     writer.write(summary);
                 }
             }
             
             if (uiGrowl.isShowSummary() && uiGrowl.isShowDetail()){
                 writer.writeText(" ", null);
             }
             
             if (uiGrowl.isShowDetail()) {
                 if (escape) {
                     writer.writeText(detail, null);
                 } else {
                     writer.write(detail);
                 }
             }          
 
             msg.rendered();
             writer.endElement("p");
         }
 
     }
 }
