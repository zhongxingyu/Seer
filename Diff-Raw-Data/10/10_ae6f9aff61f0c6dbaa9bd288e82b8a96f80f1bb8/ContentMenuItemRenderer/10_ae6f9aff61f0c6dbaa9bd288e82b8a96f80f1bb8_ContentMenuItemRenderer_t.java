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
 package org.icefaces.mobi.component.contentmenuitem;
 
 
 import org.icefaces.mobi.component.accordion.Accordion;
 import org.icefaces.mobi.component.contentpane.ContentPane;
 import org.icefaces.mobi.component.contentstack.ContentStack;
 import org.icefaces.mobi.component.contentnavbar.ContentNavBar;
 import org.icefaces.mobi.component.contentstackmenu.*;
 import org.icefaces.mobi.renderkit.BaseLayoutRenderer;
 import org.icefaces.mobi.utils.HTML;
 import org.icefaces.mobi.utils.Utils;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIForm;
 import javax.faces.component.UIViewRoot;
 import javax.faces.component.visit.VisitCallback;
 import javax.faces.component.visit.VisitContext;
 import javax.faces.component.visit.VisitResult;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 import java.io.IOException;

 import java.util.Arrays;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 public class ContentMenuItemRenderer extends BaseLayoutRenderer {
        private static Logger logger = Logger.getLogger(ContentMenuItemRenderer.class.getName());
 
        public void decode(FacesContext facesContext, UIComponent uiComponent) {
         Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
         ContentMenuItem item = (ContentMenuItem) uiComponent;
         String source = String.valueOf(requestParameterMap.get("ice.event.captured"));
         String clientId = item.getClientId();
         if (clientId.equals(source) ) {
             try {
                 if (!item.isDisabled()) {
                     uiComponent.queueEvent(new ActionEvent(uiComponent));
                     UIComponent parent = item.getParent();
                     ContentStack stack = null;
                     if (parent instanceof ContentStackMenu) {
                         ContentStackMenu menu = (ContentStackMenu)parent;
                         String stackClientId = menu.getStackClientId();
                         UIComponent compStack= facesContext.getViewRoot().findComponent(stackClientId);
                         stack = (ContentStack)compStack;
                     } else if (parent instanceof ContentNavBar){
                         ContentNavBar navBar = (ContentNavBar)parent;
                         UIComponent compStack = findParentContentStack(uiComponent);
                         stack = (ContentStack)compStack;
                     }
                     if (null != stack){
                         String newVal = String.valueOf(item.getValue());
                         stack.setCurrentId(newVal);
                     }
                 }
             } catch (Exception e) {
                 logger.warning("Error queuing CommandButton event");
             }
         }
     }
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
              throws IOException {
          UIComponent parent = uiComponent.getParent();
          if (!(parent instanceof ContentStackMenu) && !(parent instanceof ContentNavBar)){
              logger.warning("ContentMenuItem must have parent of ContentStackMenu or ContentNavBa");
              return;
          }
          if (parent instanceof ContentStackMenu){
              renderItemAsList(parent, facesContext, uiComponent);
          }
          if (parent instanceof ContentNavBar){
              renderItemAsButton(parent, facesContext, uiComponent);
          }
     }
 
     private void renderItemAsButton(UIComponent parent, FacesContext facesContext, UIComponent uiComponent)
         throws IOException {
         ContentMenuItem item = (ContentMenuItem)uiComponent;
         ContentNavBar navBar = (ContentNavBar)uiComponent.getParent();
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId=item.getClientId(facesContext);
         String stackClientId = null;
         StringBuilder menubuttonClass = new StringBuilder(ContentNavBar.CONTENTNAVBAR_BUTTON_MENU_CLASS);
         StringBuilder buttonClass = new StringBuilder (ContentNavBar.CONTENTNAVBAR_BUTTON_CLASS);
         UIComponent stack = findParentContentStack(uiComponent);
         // user specified style class
         String userDefinedClass = item.getStyleClass();
         if (userDefinedClass != null && userDefinedClass.length() > 0){
             buttonClass.append(" ").append(userDefinedClass);
             menubuttonClass.append(" ").append(userDefinedClass);
         }
         if (item.isDisabled()){
             writer.writeAttribute("disabled", "disabled", null);
         }
         if (item.getUrl() != null){
             writer.writeAttribute("href", getResourceURL(facesContext,item.getUrl()), null);
         } else {
             if (stack!=null){
                 if (item.getValue() !=null){
                     stackClientId = stack.getClientId(facesContext);
                     String valId = String.valueOf(item.getValue());
                     UIComponent pane = stack.findComponent(valId);
                     writer.startElement(HTML.DIV_ELEM, uiComponent);
                     writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
                     writer.startElement(HTML.ANCHOR_ELEM, uiComponent);
                     writer.writeAttribute("class",menubuttonClass , "class");
                     if (item.getStyle() !=null){
                      writer.writeAttribute(HTML.STYLE_ATTR, item.getStyle(), HTML.STYLE_ATTR);
                     }
                     StringBuilder sb = new StringBuilder("mobi.layoutMenu.showContent('").append(stackClientId);
                     sb.append("', event");
                     sb.append(",{ selectedId: '").append(item.getValue()).append("'");
                     sb.append(",singleSubmit: ").append(item.isSingleSubmit());
                     sb.append(", parent: '").append(navBar.getClientId(facesContext)).append("'");
                     sb.append(", item: '").append(item.getClientId(facesContext)).append("'");
                     if (pane!=null){
                         String paneId = pane.getClientId(facesContext);
                         sb.append(",selClientId: '").append(paneId).append("'");
                         ContentPane cp = (ContentPane)pane;
                         sb.append(",client: ").append(cp.isClient());
                     }
                     sb.append("});");
                     if ( !item.isDisabled()){
                           writer.writeAttribute("onclick", sb.toString(), null);
                     }
                     writer.write(item.getLabel());
                     writer.endElement(HTML.ANCHOR_ELEM);
                     writer.endElement(HTML.DIV_ELEM);
                 }
             }
         }
     }
 
     /**
      * option to render an item with value of null as either a heading or accordion handle
      * @param parent
      * @param facesContext
      * @param uiComponent
      * @throws IOException
      */
     private void renderItemAsList(UIComponent parent, FacesContext facesContext, UIComponent uiComponent)
                     throws IOException{
          ContentStackMenu parentMenu = (ContentStackMenu)parent;
          ContentMenuItem lmi = (ContentMenuItem)uiComponent;
          ResponseWriter writer = facesContext.getResponseWriter();
          String clientId = lmi.getClientId(facesContext);
          String contentStackId = parentMenu.getContentStackId();
          String stackClientId = null;
          boolean client = false;
          boolean accordion = parentMenu.isAccordion();
          String selId = null;
          if (null!=lmi.getValue()){
              selId= (String)lmi.getValue();
          }
          UIViewRoot root = facesContext.getViewRoot();
          String selClientId = null;
          if (null == ((ContentStackMenu) parent).getStackClientId()){
              //look from root first for id
              UIComponent stack = root.findComponent(contentStackId);
              if (stack !=null){
                  stackClientId = stack.getClientId(facesContext);
              }else {
              //assume menu and stack in same form as siblings
                  UIComponent form = Utils.findParentForm(uiComponent);
                  stackClientId = this.findCompIntree(facesContext, form, contentStackId);
              }
              if (null != stackClientId){
                  ((ContentStackMenu) parent).setStackClientId(stackClientId);
              }
              else{
                  logger.warning("ERROR unable to find stack id="+contentStackId);
                  // SOME FACESMESSAGE  ??
              }
          }
          stackClientId = ((ContentStackMenu) parent).getStackClientId();
          UIComponent stack = root.findComponent(stackClientId);
          //find the clientId of the selected Pane
          if (selId !=null){
              selClientId = findCompIntree(facesContext, stack,  selId);
              if (selClientId!=null ){
                  UIComponent pane = root.findComponent(selClientId);
                  ContentPane cp = (ContentPane)pane;
                  client = cp.isClient();
              }
              else {
                  logger.warning("Unable to find contentPane with id="+selId);
              }
          }
          String icon = lmi.getIcon();
          String label = lmi.getLabel();
          if (null==selId) {
              if (accordion){
                  this.writeTitleAsAccordionHandle(facesContext, uiComponent, parentMenu, label);
              }  else {
                  writeItemListStart(uiComponent, writer, clientId);
                  writer.writeAttribute("class", ContentMenuItem.OUTPUTLISTITEMGROUP_CLASS, "class");
                  if (lmi.getStyle() !=null){
                      writer.writeAttribute(HTML.STYLE_ATTR, lmi.getStyle(), HTML.STYLE_ATTR);
                  }
                  writer.startElement(HTML.DIV_ELEM, uiComponent);
                  writer.writeAttribute("class", ContentMenuItem.OUTPUTLISTITEMDEFAULT_CLASS, "class");
                  writer.write(label);
                  writer.endElement(HTML.LI_ELEM);
              }
          }else {
              writeItemListStart(uiComponent, writer, clientId);
              writer.writeAttribute("class", ContentMenuItem.OUTPUTLISTITEM_CLASS, "class");
              if (lmi.getStyle() !=null){
                      writer.writeAttribute(HTML.STYLE_ATTR, lmi.getStyle(), HTML.STYLE_ATTR);
              }
              writer.startElement(HTML.DIV_ELEM, uiComponent);
              writer.writeAttribute("class", ContentMenuItem.OUTPUTLISTITEMDEFAULT_CLASS, "class");
              writer.startElement(HTML.ANCHOR_ELEM, uiComponent);
              //verify location of panel and get proper id of the contentPane for onclick
              // if url or target then put that in the onclick  otherwise use the value lmi.getValue()
              if (lmi.isDisabled()){
                 writer.writeAttribute("disabled", "disabled", null);
              }
              if (lmi.getUrl() != null){
                  writer.writeAttribute("href", getResourceURL(facesContext,lmi.getUrl()), null);
              }
              else {
                  StringBuilder sb = new StringBuilder("mobi.layoutMenu.showContent('").append(stackClientId).append("', event");
                  sb.append(",{ selectedId: '").append(lmi.getValue()).append("'");
                  sb.append(",singleSubmit: ").append(lmi.isSingleSubmit());
                  if (selClientId!=null){
                       sb.append(",selClientId: '").append(selClientId).append("'");
                  }
                  sb.append(",client: ").append(client);
                  sb.append(", item: '").append(uiComponent.getClientId(facesContext)).append("'");
                  sb.append("});");
                  if (stackClientId != null && !lmi.isDisabled()){
                     writer.writeAttribute("onclick", sb.toString(), null);
                  }
              }
              if (icon !=null){
                 //TODO implementation of icon needs to be defined  --base64?
              }
              writer.write(lmi.getLabel());
              writer.endElement(HTML.ANCHOR_ELEM);
              writer.endElement(HTML.DIV_ELEM);
              writer.endElement(HTML.LI_ELEM);
          }
     }
 
     private void writeItemListStart(UIComponent uiComponent, ResponseWriter writer, String clientId) throws IOException {
         writer.startElement(HTML.LI_ELEM, uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
         writer.writeAttribute(HTML.NAME_ATTR, clientId, HTML.NAME_ATTR);
     }
 
 
     private void writeTitleAsAccordionHandle(FacesContext context, UIComponent childComp,
         ContentStackMenu menu, String title) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         if (menu.isOpenAccordionHandle()){
             writer.endElement(HTML.UL_ELEM);
             writer.endElement(HTML.DIV_ELEM);
             writer.endElement(HTML.SECTION_ELEM);
             menu.setOpenAccordionHandle(false);
         }
         StringBuilder handleClass = new StringBuilder("handle");
         StringBuilder listClass = new StringBuilder(ContentStackMenu.LAYOUTMENU_LIST_CLASS);
         StringBuilder baseClass = new StringBuilder(ContentStackMenu.LAYOUTMENU_CLASS);
         StringBuilder pointerClass = new StringBuilder("pointer");
         String closeClass= "closed";
         StringBuilder cfg = new StringBuilder("{autoheight:true");
         String menuId = menu.getClientId();
         String userDefinedClass = menu.getStyleClass();
         if (userDefinedClass != null && userDefinedClass.length() > 0){
               handleClass.append(" ").append(userDefinedClass);
               pointerClass.append(" ").append(userDefinedClass);
               listClass.append(" ").append(userDefinedClass);
               baseClass.append(" ").append(userDefinedClass);
         }
         writer.startElement(HTML.SECTION_ELEM, childComp);
         writer.writeAttribute(HTML.ID_ATTR, childComp.getClientId(), HTML.ID_ATTR);
         writer.writeAttribute("class", closeClass, "class");
         writer.startElement(HTML.DIV_ELEM, childComp);
         writer.writeAttribute("class", handleClass, "class");
         writer.writeAttribute("onclick", "mobi.accordionController.toggleMenu('" + menuId + "',this);", "onclick");
         writer.startElement(HTML.DIV_ELEM, childComp);
         writer.writeAttribute("class", pointerClass, "class");
         writer.write(Accordion.ACCORDION_RIGHT_POINTING_POINTER);
         writer.endElement(HTML.DIV_ELEM);
         writer.write(title);
         writer.endElement(HTML.DIV_ELEM);
         writer.startElement(HTML.DIV_ELEM, childComp);
         writer.writeAttribute("class", baseClass, "class");
         writer.startElement(HTML.UL_ELEM, childComp);
         writer.writeAttribute("class", listClass, "class");
         if (menu.getStyle() !=null) {
            writer.writeAttribute(HTML.STYLE_ATTR, menu.getStyle(), HTML.STYLE_ATTR);
         }
         menu.setOpenAccordionHandle(true);
     }
 
     public String findCompIntree(FacesContext context, UIComponent compRoot, String id){
          UIComponent searchComp = compRoot.findComponent(id);
          if (null!=searchComp){
              return searchComp.getClientId();
          }
         final String searchId =null;
         compRoot.visitTree(
                  VisitContext.createVisitContext(context, Arrays.asList(new String[] {id}), null),
                          new GetClientId(searchId));
           return searchId;
 
      }
 
       private static class GetClientId implements VisitCallback {
           private String _clientId=null;
           String searchId;
 
           private GetClientId(String searchId){
               this.searchId = searchId;
           }
           public String getClientId(){
               return _clientId;
           }
           public VisitResult visit( VisitContext visitContext, UIComponent uiComponent){
               if (uiComponent instanceof ContentStack || uiComponent instanceof ContentPane){
                   _clientId  = uiComponent.getId();
                       return VisitResult.COMPLETE;
 
               }
               return VisitResult.ACCEPT;
           }
       }
 }
