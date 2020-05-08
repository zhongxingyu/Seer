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
 
 package org.icefaces.ace.component.dataexporter;
 
 import java.io.IOException;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import org.icefaces.ace.component.datatable.DataTable;
 
 public class DataExporter extends DataExporterBase {
 
 	private transient String path = null;
 	private transient String source = "";
 	
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         super.broadcast(event);
 
         if (event != null) {
 			try {
				FacesContext facesContext = getFacesContext();
 				Object customExporter = getCustomExporter();
 				Exporter exporter;
 				if (customExporter == null) {
 					exporter = ExporterFactory.getExporterForType(getType());
 				} else {
 					if (customExporter instanceof Exporter) {
 						exporter = (Exporter) customExporter;
 					} else {
 						throw new FacesException("Object specified as custom exporter does not extend  org.icefaces.ace.component.dataexporter.Exporter.");
 					}
 				}
 				
 				UIComponent targetComponent = event.getComponent().findComponent(getTarget());
 				if (targetComponent == null) targetComponent = findComponentCustom(facesContext.getViewRoot(), getTarget());
 				if (targetComponent == null) throw new FacesException("Cannot find component \"" + getTarget() + "\" in view.");
 				if (!(targetComponent instanceof DataTable)) throw new FacesException("Unsupported datasource target:\"" + targetComponent.getClass().getName() + "\", exporter must target a ACE DataTable.");
 				
				DataTable table = (DataTable) targetComponent;
 				this.path = exporter.export(facesContext, this, table);
 			} catch (IOException e) { 
 				throw new FacesException(e); 
 			}
         }
 	}
 
 	private UIComponent findComponentCustom(UIComponent base, String id) {
 
 		if (base.getId().equals(id)) return base;
 		java.util.List<UIComponent> children = base.getChildren();
 		UIComponent result = null;
 		for (UIComponent child : children) {
 			result = findComponentCustom(child, id);
 			if (result != null) break;
 		}
 		return result;
 	}
 	
 	public String getPath(String clientId) {
 		if (this.source.equals(clientId)) {
 			return this.path;
 		} else {
 			return null;
 		}
 	}
 	
 	public void setSource(String clientId) {
 		this.source = clientId;
 	}
 	
 	protected FacesContext getFacesContext() {
 		return FacesContext.getCurrentInstance();
 	}
 }
