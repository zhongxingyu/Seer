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
 package org.icefaces.mobi.component.button;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.component.UIViewRoot;
 import javax.faces.component.behavior.ClientBehaviorHolder;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import org.icefaces.mobi.component.contentpane.ContentPane;
 import org.icefaces.mobi.component.contentstack.ContentStack;
 import org.icefaces.mobi.component.panelconfirmation.PanelConfirmation;
 import org.icefaces.mobi.component.submitnotification.SubmitNotification;
 
 import org.icefaces.mobi.renderkit.CoreRenderer;
 import org.icefaces.mobi.renderkit.ResponseWriterWrapper;
 
 import org.icefaces.mobi.utils.JSFUtils;
 import org.icefaces.mobi.utils.MobiJSFUtils;
 import org.icemobile.component.IButton;
 import org.icemobile.component.IButtonGroup;
 import org.icemobile.renderkit.IResponseWriter;
 import org.icemobile.renderkit.ButtonCoreRenderer;
 
 
 public class  CommandButtonRenderer extends CoreRenderer {
     private static Logger logger = Logger.getLogger(CommandButtonRenderer.class.getName());
     List <UIParameter> uiParamChildren;
 
     public void decode(FacesContext facesContext, UIComponent uiComponent) {
         Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
         CommandButton commandButton = (CommandButton) uiComponent;
         String source = String.valueOf(requestParameterMap.get("ice.event.captured"));
         String clientId = commandButton.getClientId();
         if (clientId.equals(source)) {
             /* if child of commandButtonGroup set to selectedId */
             Object parent = uiComponent.getParent();
             if (parent instanceof CommandButtonGroup)  {
                 CommandButtonGroup cbg = (CommandButtonGroup)parent;
                 if (!cbg.isDisabled()){
                    cbg.setSelectedId(clientId);
                 }
             }
             try {
                 if (!commandButton.isDisabled()) {
                     uiComponent.queueEvent(new ActionEvent(uiComponent));
                     decodeBehaviors(facesContext, uiComponent);
                 }
             } catch (Exception e) {
                 logger.warning("Error queuing CommandButton event");
             }
         }
     }
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
         IResponseWriter writer = new ResponseWriterWrapper(facesContext.getResponseWriter());
         IButton button = (IButton)uiComponent;
         ButtonCoreRenderer renderer = new ButtonCoreRenderer();
         /*
           check to see if I am the selectedId if I have parent CommandButtonGroup
          */
         String clientId = button.getClientId();
         Object parent = uiComponent.getParent();
         if (parent instanceof IButtonGroup)  {
             IButtonGroup cbg = (IButtonGroup)parent;
             button.setGroupId(cbg.getClientId());
             if (cbg.isDisabled()){
                 button.setParentDisabled(true);
             }
             String selectedId = cbg.getSelectedId();
             if (null != selectedId){
                 if (selectedId.equals(clientId)) {
                     button.setSelectedButton(true);
                 } else {
                     button.setSelectedButton(false);
                 }
               //  logger.info("button selectedId="+selectedId+" myid ="+clientId+" selected="+button.isSelectedButton());
             }
         }
         /*
          * if using openContentPane, then can't use any other attributes on this component
          * unfortunately, the selected css has already been applied.  Not sure how to refactor
          * that but users will have to made aware that the button group cannot be used with
          * this feature.
          */
         if(button.getOpenContentPane() != null ){
             UIComponent stack = findParentContentStack(uiComponent);
             if (stack!=null){
                 StringBuilder noPanelConf = new StringBuilder("mobi.layoutMenu.showContent('").append(stack.getClientId());
                 noPanelConf.append("', event");
                 noPanelConf.append(",{ selectedId: '").append(button.getOpenContentPane()).append("'");
                 noPanelConf.append(",singleSubmit: ").append(button.isSingleSubmit());
                 noPanelConf.append(", item: '").append(uiComponent.getClientId(facesContext)).append("'");
                 UIComponent pane = stack.findComponent(button.getOpenContentPane());
                 if (pane!=null){
                     String paneId = pane.getClientId(facesContext);
                     noPanelConf.append(",selClientId: '").append(paneId).append("'");
                     ContentPane cp = (ContentPane)pane;
                     noPanelConf.append(",client: ").append(cp.isClient());
                 }
                 noPanelConf.append("});");
                 button.setJsCall(noPanelConf);
                 renderer.encodeEnd(button, writer);
                 return;
             }
         }
         // get the params and set into button
         uiParamChildren = JSFUtils.captureParameters(uiComponent);
         String params = "";
         if (null != uiParamChildren) {
             String paramsAsString = MobiJSFUtils.asParameterString(uiParamChildren);
             button.setParams(paramsAsString);
         }
         //get behaviors and set into button object
         ClientBehaviorHolder cbh = (ClientBehaviorHolder)uiComponent;
         boolean hasBehaviors = !cbh.getClientBehaviors().isEmpty();
         if (hasBehaviors){
             String behaviors = this.encodeClientBehaviors(facesContext, cbh, "click").toString();
             behaviors = behaviors.replace("\"", "\'");
             button.setBehaviors(behaviors);
         }
         // panelConfirmation, submitNotification or both?
         if (null!= button.getPanelConfirmation()){
             String searchId = button.getPanelConfirmation();
             UIComponent comp = findClientId(facesContext, uiComponent, searchId);
             if (null != comp){
                 PanelConfirmation pc = (PanelConfirmation)comp;
                 button.setPanelConfirmationId(pc.getClientId(facesContext));
             }else {
                 logger.log(Level.WARNING,  "PanelConfirmation with id="+searchId+" not found for button id="+button.getClientId());
             }
         } else{
             button.setPanelConfirmationId(null);
         }
         if (null != button.getSubmitNotification()){
             String searchId = button.getSubmitNotification();
             UIComponent comp = findClientId(facesContext, uiComponent, searchId);
             if (null != comp){
                 SubmitNotification submitNot = (SubmitNotification)comp;
                 if (!submitNot.isDisabled()){
                     button.setSubmitNotificationId(submitNot.getClientId(facesContext));
                 } else {
                     button.setSubmitNotificationId(null);
                     logger.log(Level.FINE, "submit notification is found but disabled");
                 }
             } else {
                 logger.log(Level.WARNING,  "SubmitNotification with id="+searchId+" not found for button id="+button.getClientId());
             }
         } else {
             button.setSubmitNotificationId(null);
         }
         renderer.encodeEnd(button, writer);
     }
         
     protected static UIComponent findParentContentStack(UIComponent component) {
         UIComponent parent = component;
         while (parent != null)
             if (parent instanceof ContentStack) break;
             else parent = parent.getParent();
 
         return parent;
     }
 
 
     private UIComponent findClientId(FacesContext facesContext, UIComponent button, String id){
         //the button has to have a form, so look for it and then the panelConfirmation and
         //submitNotification components are in same form.
         UIComponent uiForm = JSFUtils.findParentForm(button);
         UIComponent component;
         if (null != uiForm){
             component = uiForm.findComponent(id);
             if (null!= component){
                return component;
             }
             else { //try UIViewRoot
                 UIViewRoot root = facesContext.getViewRoot();
                 component = root.findComponent(id);
                 if (null!=component){
                     return component;
                 }
             }
         }
         logger.warning("Cannot find component linked to commandButton with id="+id);
         return null;
     }
 
 }
