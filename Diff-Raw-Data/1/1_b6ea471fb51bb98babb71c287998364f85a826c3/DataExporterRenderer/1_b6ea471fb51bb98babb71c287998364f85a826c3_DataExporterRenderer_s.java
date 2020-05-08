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
  
 package org.icefaces.ace.component.dataexporter;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import javax.faces.event.ActionEvent;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.faces.component.behavior.ClientBehavior;
 import javax.faces.component.behavior.ClientBehaviorContext;
 import javax.faces.component.behavior.ClientBehaviorHolder;
 import javax.faces.event.PhaseId;
 import java.util.*;
 
 @MandatoryResourceComponent(tagName="dataExporter", value="org.icefaces.ace.component.dataexporter.DataExporter")
 public class DataExporterRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext facesContext, UIComponent component) {
 		Map<String,String> requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
 		DataExporter exporter = (DataExporter) component;
 		String clientId = exporter.getClientId(facesContext);
 
 		if (requestParameterMap.containsKey("ice.event.captured")) {
 			String source = String.valueOf(requestParameterMap.get("ice.event.captured"));
 			if (clientId.equals(source)) {
 				exporter.setSource(clientId);
                 // Generate resources in invoke application. After all decoding is finished.
                 exporter.queueEvent(new ActionEvent(exporter) {{ setPhaseId(PhaseId.INVOKE_APPLICATION); }});
 			}
 		}
 		
 		decodeBehaviors(facesContext, exporter);
     }
 	
 	@Override
 	public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
 		DataExporter exporter = (DataExporter) component;
 		String clientId = exporter.getClientId(facesContext);
 		
 		writer.startElement("button", null);
 		writer.writeAttribute("id", clientId, null);
 		
 		StringBuilder onclick = new StringBuilder("new ice.ace.DataExporter('" + clientId + "',");
 		onclick.append(" function() { ");
         // ClientBehaviors
         Map<String,List<ClientBehavior>> behaviorEvents = exporter.getClientBehaviors();
         if(!behaviorEvents.isEmpty()) {
             List<ClientBehaviorContext.Parameter> params = Collections.emptyList();
 			for(Iterator<ClientBehavior> behaviorIter = behaviorEvents.get("activate").iterator(); behaviorIter.hasNext();) {
 				ClientBehavior behavior = behaviorIter.next();
 				ClientBehaviorContext cbc = ClientBehaviorContext.createClientBehaviorContext(facesContext, exporter, "activate", clientId, params);
 				String script = behavior.getScript(cbc);    //could be null if disabled
 
 				if(script != null) {
 					onclick.append("ice.ace.ab(ice.ace.extendAjaxArguments(");
                     onclick.append(script);
 					onclick.append(", {'event':event, node:this}));");
 				}
 			}
             onclick.append(" });");
 		} else {
 		    onclick.append("ice.s(event,this);");
         }
         onclick.append("return false;");
 		writer.writeAttribute("onclick", onclick.toString(), null);
 
 		String styleClass = exporter.getStyleClass();
 		if (styleClass != null) writer.writeAttribute("class", styleClass, null);
 		String style = exporter.getStyle();
 		if (style != null) writer.writeAttribute("style", style, null);
 		boolean hasChildren = exporter.getChildren().size() > 0;
 		String label = exporter.getLabel();
 		if (!hasChildren) {
 			label = label == null ? "Export" : label;
 			writer.startElement("span", null);
 			writer.write(label);
 			writer.endElement("span");
 		} else if (label != null) {
 			writer.startElement("span", null);
 			writer.write(label);
 			writer.endElement("span");
 		}	
 	}
 
     @Override
 	public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
 		DataExporter exporter = (DataExporter) component;
 		String clientId = exporter.getClientId(facesContext);
 		
 		writer.endElement("button");
 		
 		writer.startElement("span", null);
 		writer.writeAttribute("id", clientId + "_script", null);
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 		
 		// themeroller support
 		writer.write("ice.ace.jq(ice.ace.escapeClientId('" + clientId + "')).button();");
 		
 		// load file
 		String path = exporter.getPath(clientId);
 		if (path != null) {
 			writer.write("ice.ace.DataExporters['" + clientId + "'].url('" + path + "');");
 		}
 		
 		writer.endElement("script");
 		writer.endElement("span");
 	}
 }
