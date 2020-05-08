 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 package com.icesoft.faces.application;
 
 import com.icesoft.faces.context.effects.CurrentStyle;
 import com.icesoft.faces.renderkit.dom_html_basic.FormRenderer;
 import com.icesoft.faces.util.CoreUtils;
 import org.icefaces.impl.event.BridgeSetup;
 import org.icefaces.impl.event.ResourceOutputUtil;
 import org.icefaces.impl.event.UIOutputWriter;
 import org.icefaces.impl.util.FormEndRenderer;
 import org.icefaces.impl.util.FormEndRendering;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.SystemEvent;
 import javax.faces.event.SystemEventListener;
 import java.io.IOException;
 import java.util.*;
 
 public class ExtrasSetup implements SystemEventListener {
     private static final ResourceBundle defaultBridgeMessages = new ListResourceBundle() {
         protected Object[][] getContents() {
             return new Object[][]{
                     {"session-expired", "User Session Expired"},
                     {"connection-lost", "Network Connection Interrupted"},
                     {"server-error", "Server Internal Error"},
                     {"description", "To reconnect click the Reload button on the browser or click the button below"},
                     {"button-text", "Reload"}
             };
         }
     };
     private static final FormEndRenderer FormHiddenInputFields = new FormHiddenInputFieldsRenderer();
     private boolean fastBusyIndicator;
     private String compatResourceName;
     private String extraCompatResourceName;
     private boolean includeScrollOffsets;
 
     private static final String ICE_COMPAT_LIB = "ice.compat";
 
     public ExtrasSetup() {
         FacesContext context = FacesContext.getCurrentInstance();
 
         fastBusyIndicator = EnvUtils.isFastBusyIndicator(context);
         includeScrollOffsets = EnvUtils.isIncludeScrollOffsets(context);
 
         boolean developmentStage = context.isProjectStage(ProjectStage.Development);
         compatResourceName = developmentStage ? "compat.uncompressed.js" : "compat.js";
         extraCompatResourceName = developmentStage ? "icefaces-compat.uncompressed.js" : "icefaces-compat.js";
     }
 
     public boolean isListenerForSource(Object source) {
         return source instanceof UIViewRoot;
     }
 
     public void processEvent(SystemEvent event) throws AbortProcessingException {
         final FacesContext context = FacesContext.getCurrentInstance();
 
         if (EnvUtils.isICEfacesView(context)) {
             UIViewRoot root = context.getViewRoot();
 
             root.addComponentResource(context, ResourceOutputUtil.createTransientScriptResourceComponent(compatResourceName, ICE_COMPAT_LIB), "head");
             root.addComponentResource(context, ResourceOutputUtil.createTransientScriptResourceComponent(extraCompatResourceName, ICE_COMPAT_LIB), "head");
 
             UIOutput output = new UIOutputWriter() {
                 public void encode(ResponseWriter writer, FacesContext context) throws IOException {
 
                     ResourceBundle localizedBundle = defaultBridgeMessages;
                     try {
                         localizedBundle = ResourceBundle.getBundle("bridge-messages", context.getViewRoot().getLocale());
                     } catch (MissingResourceException e) {
                         localizedBundle = defaultBridgeMessages;
                     }
 
                     String connectionLostRedirectURI;
                     String uri = EnvUtils.getConnectionLostRedirectURI(context);
                     if (uri == null) {
                         connectionLostRedirectURI = "null";
                     } else {
                         connectionLostRedirectURI = "'" + CoreUtils.resolveResourceURL(context, uri.replaceAll("'", "")) + "'";
                     }
 
                     String sessionExpiredRedirectURI;
                     uri = EnvUtils.getSessionExpiredRedirectURI(context);
                     if (uri == null) {
                         sessionExpiredRedirectURI = "null";
                     } else {
                         sessionExpiredRedirectURI = "'" + CoreUtils.resolveResourceURL(context, uri.replaceAll("'", "")) + "'";
                     }
 
                     final String contextPath = CoreUtils.resolveResourceURL(context, "/");
 
                     writer.startElement("span", this);
                     writer.writeAttribute("id", getClientId(context), null);
                     writer.startElement("script", this);
                     writer.writeAttribute("type", "text/javascript", null);
                     writer.write("ice.includeScrollOffsets=" + includeScrollOffsets + ";");
                     writer.write("ice.DefaultIndicators({");
                     writer.write("fastBusyIndicator: ");
                     writer.write(Boolean.toString(fastBusyIndicator));
                     writer.write(",");
                     writer.write("connectionLostRedirectURI: ");
                     writer.write(connectionLostRedirectURI);
                     writer.write(",");
                     writer.write("sessionExpiredRedirectURI: ");
                     writer.write(sessionExpiredRedirectURI);
                     writer.write(",");
                     writer.write("connection: { context: '");
                     writer.write(contextPath);
                     writer.write("'},");
                     writer.write("messages: {");
                     writer.write("sessionExpired: '");
                     writer.write(localizedBundle.getString("session-expired"));
                     writer.write("',");
                     writer.write("connectionLost: '");
                     writer.write(localizedBundle.getString("connection-lost"));
                     writer.write("',");
                     writer.write("serverError: '");
                     writer.write(localizedBundle.getString("server-error"));
                     writer.write("',");
                     writer.write("description: '");
                     writer.write(localizedBundle.getString("description"));
                     writer.write("',");
                     writer.write("buttonText: '");
                     writer.write(localizedBundle.getString("button-text"));
                     writer.write("'");
                     writer.write("}},'");
                     writer.write(getClientId(context));
                     writer.write("');");
                     writer.endElement("script");
                     writer.endElement("span");
                 }
             };
            Map attributes = output.getAttributes();
            attributes.put("name", "ICEfacesCompatSetup");
            attributes.put("library", "ice.compat");
             output.setTransient(true);
             output.setId(BridgeSetup.getViewID(context.getExternalContext()) + "_icefaces_compat_config");
             root.addComponentResource(context, output, "body");
 
             FormEndRendering.addRenderer(context, FormHiddenInputFields);
         }
     }
 
     private static class FormHiddenInputFieldsRenderer implements FormEndRenderer {
         public void encode(FacesContext context, UIComponent component) throws IOException {
             ResponseWriter writer = context.getResponseWriter();
             String formClientID = component.getClientId(context);
             writer.startElement("span", component);
             writer.writeAttribute("id", formClientID + "hdnFldsDiv", null);
 
             //todo: replace this with FormRenderer.addHiddenField calls in the renderers that need "icefacesCssUpdates" field           
             //css input field is required by some renderers (such as DnD)
             writer.startElement("input", component);
             writer.writeAttribute("type", "hidden", null);
             writer.writeAttribute("name", CurrentStyle.CSS_UPDATE_FIELD, null);
             writer.writeAttribute("value", "", null);
             writer.endElement("input");
 
             //Render any required hidden fields. There is a list
             //(on the request map of the external context) of
             //'required hidden fields'. Hidden fields can be
             //contributed by the CommandLinkRenderer. Contribution
             //is made during rendering of this form's commandLink
             //children so we have to wait for the child renderers
             //to complete their work before we render the hidden
             //fields. Therefore, this method should be called from
             //the form's encodeEnd method. We can assume that the
             //hidden fields are the last fields in the form because
             //they are rendered in the FormRenderer's encodeEnd
             //method. Note that the CommandLinkRenderer adds one
             //hidden field that indicates the id of the link that
             //was clicked to submit the form ( in case there are
             //multiple commandLinks on a page) and one hidden field
             //for each of its UIParameter children.
             Map requestMap = context.getExternalContext().getRequestMap();
             Map map = (Map) requestMap.get(FormRenderer.COMMAND_LINK_HIDDEN_FIELDS_KEY);
             if (map != null) {
                 Iterator fields = map.entrySet().iterator();
                 while (fields.hasNext()) {
                     Map.Entry nextField = (Map.Entry) fields.next();
                     if (FormRenderer.COMMAND_LINK_HIDDEN_FIELD.equals(nextField.getValue())) {
                         writer.startElement("input", component);
                         writer.writeAttribute("type", "hidden", null);
                         writer.writeAttribute("name", nextField.getKey().toString(), null);
                         writer.endElement("input");
                     }
                 }
                 //remove map to avoid being used by the next rendered form
                 requestMap.remove(FormRenderer.COMMAND_LINK_HIDDEN_FIELDS_KEY);
             }
             writer.endElement("span");
         }
     }
 }
