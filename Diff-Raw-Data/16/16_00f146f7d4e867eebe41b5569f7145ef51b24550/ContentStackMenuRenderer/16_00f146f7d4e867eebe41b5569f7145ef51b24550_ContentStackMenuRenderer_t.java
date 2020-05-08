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
 
 package org.icefaces.mobi.component.contentstackmenu;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.icefaces.mobi.component.contentstack.ContentStack;
 import org.icefaces.mobi.renderkit.BaseLayoutRenderer;
 import org.icefaces.mobi.utils.HTML;
 import org.icefaces.mobi.utils.JSFUtils;
 import org.icefaces.mobi.utils.Utils;
 
 public class  ContentStackMenuRenderer extends BaseLayoutRenderer {
 
     private static final Logger logger = Logger.getLogger(ContentStackMenuRenderer.class.getName());
     private static final String JS_LIBRARY = "org.icefaces.component.layoutmenu";
     // may also need the js files for accordion if accordion attribute is true
     private static final String JS_ACC_NAME= "accordion.js";
     private static final String JS_ACC_MIN_NAME = "accordion-min.js";
     private static final String JS_ACC_LIB = "org.icefaces.component.accordion";
 
     @Override
     public void decode(FacesContext facesContext, UIComponent uiComponent) {
         ContentStackMenu menu = (ContentStackMenu) uiComponent;
         String clientId = menu.getClientId(facesContext);
         Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
         // ajax behavior comes from ContentStackMenu which sends the currently selected value
         String cStackId = menu.getContentStackId();
         UIViewRoot root = facesContext.getViewRoot();
         UIComponent stackComp = JSFUtils.findChildComponent(root, cStackId) ;
         if (stackComp !=null && stackComp instanceof ContentStack){
             String stackId = stackComp.getClientId(facesContext);
             ContentStack stack = (ContentStack)stackComp;
             String indexStr = params.get(clientId + "_hidden");
             String newStr = params.get(stackId);
         /*    if (newStr !=null){
                 logger.info("submitted "+newStr+" from request");
             }  */
             String oldIndex = stack.getCurrentId();
             if( null != indexStr) {
                 //find the activeIndex and set it
                 if (!oldIndex.equals(indexStr)){
                     stack.setCurrentId(indexStr);
                     /* do we want to queue an event for panel change in stack? */
                    // component.queueEvent(new ValueChangeEvent(component, oldIndex, indexStr)) ;
                 }
             }
         }
     }
 
      @Override
      public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
              throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         ContentStackMenu menu = (ContentStackMenu) uiComponent;
       	UIComponent form = JSFUtils.findParentForm(uiComponent);
         boolean accordion = menu.isAccordion();
    		if(form == null) {
 			throw new FacesException("ContentStackMenu : \"" + clientId + "\" must be inside a form element");
 		}
          // root element
          String userDefinedClass = menu.getStyleClass();
          writer.startElement(HTML.DIV_ELEM, uiComponent);
          writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
          writer.writeAttribute(HTML.NAME_ATTR, clientId, HTML.NAME_ATTR);
          // apply button type style classes
          StringBuilder baseClass = new StringBuilder(ContentStackMenu.LAYOUTMENU_CLASS);
          StringBuilder listClass = new StringBuilder(ContentStackMenu.LAYOUTMENU_LIST_CLASS);
          StringBuilder baseAccordionClass = new StringBuilder(ContentStackMenu.LAYOUTMENU_CLASS+"mobi-accordion");
          if (null != userDefinedClass) {
              baseClass.append(userDefinedClass);
              listClass.append(userDefinedClass);
              baseAccordionClass.append(" ").append(userDefinedClass);
          }
          if (menu.getStyle() !=null){
              writer.writeAttribute("style", menu.getStyle(), "style");
          }
          if (accordion){
              writer.startElement(HTML.DIV_ELEM, uiComponent);
              writer.writeAttribute(HTML.ID_ATTR, clientId+"_acc", HTML.ID_ATTR);
              writer.writeAttribute("class",baseAccordionClass.toString(), null);
          }   else {
              writer.writeAttribute(HTML.CLASS_ATTR, baseClass.toString(), null);
              writer.startElement(HTML.UL_ELEM, uiComponent);
              writer.writeAttribute(HTML.CLASS_ATTR, listClass.toString(), HTML.CLASS_ATTR);
          }
          if (menu.getVar() != null) {
             menu.setRowIndex(-1);
             for (int i = 0; i < menu.getRowCount(); i++) {
                 //assume that if it's a list of items then it's grouped
                 menu.setRowIndex(i);
                 // option can't have children tags but can be disabled ...not too sure what to do about that
                 /* check to see that only child can be ContentMenuItem?  */
                 renderChildren(facesContext, menu);
             }
             menu.setRowIndex(-1);
         }  else {
              //doing it with indiv ContentMenuItem tag's
              renderChildren(facesContext, menu);
         }
 
     }
     @Override
     public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         ContentStackMenu menu = (ContentStackMenu) component;
         writer.endElement(HTML.UL_ELEM);
         if (menu.isAccordion()){
             writer.endElement(HTML.DIV_ELEM);
             writer.endElement(HTML.SECTION_ELEM);
             writer.endElement(HTML.DIV_ELEM);
         }
         this.encodeHidden(facesContext, component);
         writer.endElement(HTML.DIV_ELEM);
     }
 
     @Override
     public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
          //Rendering happens on encodeEnd
     }
 
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 
 
 }
