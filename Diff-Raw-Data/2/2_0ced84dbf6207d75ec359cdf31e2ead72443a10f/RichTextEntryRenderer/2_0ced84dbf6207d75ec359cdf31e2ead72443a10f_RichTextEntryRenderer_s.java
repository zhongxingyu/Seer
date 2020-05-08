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
 
 package org.icefaces.ace.component.richtextentry;
 
 import org.icefaces.ace.renderkit.InputRenderer;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.ace.util.JSONBuilder;
 import org.w3c.dom.Element;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 
@MandatoryResourceComponent(tagName="richTextEntry", value="com.icesoft.faces.component.richtextentry.RichTextEntry")
 public class RichTextEntryRenderer extends InputRenderer {
 
     public void decode(FacesContext context, UIComponent component) {
 		decodeBehaviors(context, component);
 	}
 	
 	public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         RichTextEntry richTextEntry = (RichTextEntry) uiComponent;
 
         writer.startElement("div", null);
         writer.writeAttribute("id", clientId + "container", null);
 		writer.writeAttribute("class", richTextEntry.getStyleClass(), null);
 		if (richTextEntry.getStyle() != null) {
 			writer.writeAttribute("style", richTextEntry.getStyle(), null);
 		}
 
 		writer.startElement("textarea", null);
 		writer.writeAttribute("name", clientId, null);
 		writer.writeAttribute("id", clientId, null);
 		writer.writeAttribute("style", "display:none;", null);
 		Object value = richTextEntry.getValue();
 		if (value != null) {
 			writer.writeText(value, null);
 		}
 		
 		writer.endElement("textarea");
 		writer.endElement("div");
 
 		writer.startElement("span", null);
 		writer.writeAttribute("id", clientId + "scrpt", null);
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 		String customConfig =  richTextEntry.getCustomConfigPath();
 		customConfig = customConfig == null ? "" : resolveResourceURL(facesContext, customConfig);
 		
 		JSONBuilder jb = JSONBuilder.create();
 		jb.beginFunction("ice.ace.richtextentry.renderEditor")
 			.item(clientId)
 			.item(richTextEntry.getToolbar())
 			.item(richTextEntry.getLanguage())
 			.item(richTextEntry.getSkin().toLowerCase())
 			.item(richTextEntry.getHeight())
 			.item(richTextEntry.getWidth())
 			.item(customConfig)
 			.item(richTextEntry.isSaveOnSubmit())
 			.beginMap()
 			.entry("p", ""); // dummy property
 			encodeClientBehaviors(facesContext, richTextEntry, jb);
         jb.endMap().endFunction();
 		writer.write(jb.toString());
 		
 		writer.endElement("script");
 		writer.endElement("span");
     }
 	
     // taken from com.icesoft.faces.util.CoreUtils
 	public static String resolveResourceURL(FacesContext facesContext, String path) {
         ExternalContext ec = facesContext.getExternalContext();
         String ctxtPath = ec.getRequestContextPath();
 
         if (path.length() > 0 && path.charAt(0) == '/' && path.startsWith(ctxtPath)) {
             path = path.substring(ctxtPath.length());
         }
 
         return facesContext.getApplication().getViewHandler().getResourceURL(facesContext, path);
     }
 }
