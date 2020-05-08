 /*
  * Copyright 2004-2011 ICEsoft Technologies Canada Corp. (c)
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
  * See the License for the specific language governing permissions an
  * limitations under the License.
  */
 
 package org.icefaces.component.list;
 
 
 import org.icefaces.component.utils.BaseLayoutRenderer;
 import org.icefaces.component.utils.HTML;
 
 import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.Renderer;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class OutputListRenderer extends BaseLayoutRenderer {
     private static Logger logger = Logger.getLogger(OutputListRenderer.class.getName());
 
 
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         OutputList list = (OutputList) uiComponent;
         //check to ensure children are all of type OutputListItem
         writer.startElement(HTML.UL_ELEM, uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
 
         // apply component style classes.
         String userDefinedClass = list.getStyleClass();
         StringBuilder styleClasses = new StringBuilder(OutputList.OUTPUTLIST_CLASS);
         if (list.isInset()) {
             styleClasses.append(" ").append(OutputList.OUTPUTLISTINSET_CLASS);
         }
         if (userDefinedClass != null) {
             styleClasses.append(" ").append(userDefinedClass);
         }
         writer.writeAttribute("class", styleClasses.toString(), "styleClass");
         if (list.getVar() != null) {
             list.setRowIndex(-1);
             for (int i = 0; i < list.getRowCount(); i++) {
                 //assume that if it's a list of items then it's grouped
                 list.setRowIndex(i);
                 writer.startElement(HTML.LI_ELEM, null);
                 writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
                 //do we want to allow them to overwrite the styling with a list??
                 String itemDefinedClass = list.getItemStyleClass();
                 String styleClass = OutputListItem.OUTPUTLISTITEM_CLASS;
                 if (userDefinedClass != null) {
                     styleClass += " " + itemDefinedClass;
                 }
                 writer.writeAttribute("class", styleClass, "styleClass");
                 writer.startElement(HTML.DIV_ELEM, uiComponent);
                 if (list.getItemType().equals("thumb")) {
                     writer.writeAttribute("class", OutputListItem.OUTPUTLISTITEMTHUMB_CLASS, null);
                 } else {
                     writer.writeAttribute("class", OutputListItem.OUTPUTLISTITEMDEFAULT_CLASS, null);
                 }
                 renderChildren(facesContext, list);
                 writer.endElement(HTML.DIV_ELEM);
                 writer.endElement(HTML.LI_ELEM);
             }
         } else  if (facesContext.isProjectStage(ProjectStage.Development) ||
                 logger.isLoggable(Level.FINER)) {
         //check for value of the var and if not null then iterate over list otherwise,
         //xlook for appropirate children and
         // verify the children are OutputListItem only
             List<UIComponent> children = uiComponent.getChildren();
             for (UIComponent child : children) {
                 if (!(child instanceof OutputListItem)) {
                     logger.finer("The OutputList component allows only children of type OutputListItem");
                 }
             }
         }
     }
 
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         writer.endElement(HTML.UL_ELEM);
     }
 
     public boolean getRendersChildren() {
         return true;
     }
     public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
        //Rendering happens on encodeEnd
     }
 }
