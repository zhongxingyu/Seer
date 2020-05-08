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
 
 package org.icefaces.ace.component.tabset;
 
 import java.io.IOException;
 import java.util.*;
 
import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ValueChangeEvent;
 
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.ARIA;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.ace.util.ScriptWriter;
 import org.icefaces.ace.util.Utils;
 import org.icefaces.impl.util.DOMUtils;
 import org.icefaces.util.EnvUtils;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="tabSet", value="org.icefaces.ace.component.tabset.TabSet")
 public class TabSetRenderer extends CoreRenderer {
     private static String YUI_TABSET_INDEX = "yti";
     public boolean getRendersChildren() {
         return true;
     }
 
     public void decode(FacesContext facesContext, UIComponent uiComponent) {
         Integer index = decodeSelectedIndex(facesContext, uiComponent);
         if (index != null) {
             TabSet tabSet = (TabSet) uiComponent;
             int old = tabSet.getSelectedIndex();
             if (old != index.intValue()) {
                 uiComponent.queueEvent(new ValueChangeEvent (uiComponent,
                         new Integer(old), index));
             }
         }
         decodeBehaviors(facesContext, uiComponent);
     }
 
     /**
      * @return Integer if TabSet's selectedIndex has been sent, otherwise null
      */
     protected Integer decodeSelectedIndex(FacesContext facesContext,
             UIComponent uiComponent) {
         Integer index = null;
         Map requestParameterMap = facesContext.getExternalContext().
                 getRequestParameterMap();
         //one field per form will be use to send tabindex info
         if (requestParameterMap.containsKey(YUI_TABSET_INDEX)) {
         	//the value of yti should look something like this "clientId=tabindex"
             String param = String.valueOf(requestParameterMap.get(
                     YUI_TABSET_INDEX));
             String[] info = param.split("=");
             String clientId = uiComponent.getClientId(facesContext);
             //info[0] is containing a clientId of a tabset
             if (clientId.equals(info[0])) {
                 try {
                 	//info[1] is containing a tabindex
                     index = Integer.parseInt(info[1]);
                 } catch(NumberFormatException e) {}
             }
         }
         return index;
     }
     
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
     throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         TabSet tabSet = (TabSet) uiComponent;
         String clientId = uiComponent.getClientId(facesContext);
         //tabset's toot div
         writer.startElement(HTML.DIV_ELEM, uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
         String style = tabSet.getStyle();
         if(style != null){
         	writer.writeAttribute(HTML.STYLE_ATTR, style, HTML.STYLE_ATTR);
         }        
     }
     
     public void encodeChildren(FacesContext facesContext, UIComponent uiComponent)
     throws IOException {
         String clientId = uiComponent.getClientId(facesContext);
         ResponseWriter writer = facesContext.getResponseWriter();
         TabSet tabSet = (TabSet) uiComponent;   
         boolean isBottom = "bottom".equals(tabSet.getOrientation());
         
         //As per YUI's contract if the orientation is set to bottom, the contents of the tab
         //should ger rendered first, and then tabs
         if (isBottom) {
             writer.startElement(HTML.DIV_ELEM, uiComponent);
                 writer.writeAttribute(HTML.TABINDEX_ATTR, 0, HTML.TABINDEX_ATTR);
                 writer.writeAttribute(HTML.ID_ATTR, clientId+"cnt", HTML.ID_ATTR);
                 writer.writeAttribute(HTML.CLASS_ATTR, "yui-content ui-tabs-panel ui-widget-content ui-corner-top", HTML.CLASS_ATTR);
             writer.endElement(HTML.DIV_ELEM);
         
             writer.startElement(HTML.UL_ELEM, uiComponent);
                 writer.writeAttribute(HTML.ID_ATTR, clientId+"_nav", HTML.ID_ATTR);
                 writer.writeAttribute(HTML.CLASS_ATTR, "yui-nav ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all", HTML.CLASS_ATTR);
                 if (EnvUtils.isAriaEnabled(facesContext)) {
                     writer.writeAttribute(ARIA.ROLE_ATTR, ARIA.TABLIST_ROLE, ARIA.ROLE_ATTR);  
                 }
                 doTabs(facesContext, uiComponent, Do.RENDER_LABEL, null, null, null);
             writer.endElement(HTML.UL_ELEM);
               
         } else if ("top".equals(tabSet.getOrientation())) {
             writer.startElement(HTML.UL_ELEM, uiComponent);
                 writer.writeAttribute(HTML.ID_ATTR, clientId+"_nav", HTML.ID_ATTR);
                 writer.writeAttribute(HTML.CLASS_ATTR, "yui-nav ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all", HTML.CLASS_ATTR);
                 if (EnvUtils.isAriaEnabled(facesContext)) {
                     writer.writeAttribute(ARIA.ROLE_ATTR, ARIA.TABLIST_ROLE, ARIA.ROLE_ATTR);  
                 }                
                 doTabs(facesContext, uiComponent, Do.RENDER_LABEL, null, null, null);
             writer.endElement(HTML.UL_ELEM);
             
             
             writer.startElement(HTML.DIV_ELEM, uiComponent);
                 writer.writeAttribute(HTML.ID_ATTR, clientId+"cnt", HTML.ID_ATTR);
                 writer.writeAttribute(HTML.CLASS_ATTR, "yui-content ui-tabs-panel ui-widget-content ui-corner-bottom", HTML.CLASS_ATTR);
             writer.endElement(HTML.DIV_ELEM);			  
         } else {
             writer.startElement(HTML.UL_ELEM, uiComponent);
                 writer.writeAttribute(HTML.ID_ATTR, clientId+"_nav", HTML.ID_ATTR);
                 writer.writeAttribute(HTML.CLASS_ATTR, "yui-nav ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all ui-tabs-vertical", HTML.CLASS_ATTR);
                 if (EnvUtils.isAriaEnabled(facesContext)) {
                     writer.writeAttribute(ARIA.ROLE_ATTR, ARIA.TABLIST_ROLE, ARIA.ROLE_ATTR);  
                 }                
                 doTabs(facesContext, uiComponent, Do.RENDER_LABEL, null, null, null);
             writer.endElement(HTML.UL_ELEM);
             
             
             writer.startElement(HTML.DIV_ELEM, uiComponent);
                 writer.writeAttribute(HTML.ID_ATTR, clientId+"cnt", HTML.ID_ATTR);
                 writer.writeAttribute(HTML.CLASS_ATTR, "yui-content ui-tabs-panel ui-widget-content ui-corner-bottom ui-tabs-content-vertical", HTML.CLASS_ATTR);
             writer.endElement(HTML.DIV_ELEM);
         }
     }
     
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
     throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         final TabSet tabSet = (TabSet) uiComponent;        
         String clientId = uiComponent.getClientId(facesContext);
 //System.out.println("TabSetRenderer.encodeEnd  clientId: " + clientId);
         // ICE-6703: default style classes should be for the top orientation of tabs. (Plus space at the end.)
         String styleClass = "yui-navset yui-navset-top ui-tabset ui-widget ui-widget-content ui-corner-all ";
         
         String orientation = tabSet.getOrientation();
         // ICE-6703: top, invalid or unspecified orientation should all use default style classes defined above.
         if ("left".equalsIgnoreCase(orientation)) {
             styleClass= "yui-navset yui-navset-left ui-tabset-vertical ui-widget ui-widget-content ui-corner-all ";
         } else if ("right".equalsIgnoreCase(orientation)) {
             styleClass= "yui-navset yui-navset-right ui-tabset-vertical ui-widget ui-widget-content ui-corner-all ";
         } else if ("bottom".equalsIgnoreCase(orientation)) {
             styleClass= "yui-navset yui-navset-bottom ui-tabset ui-widget ui-widget-content ui-corner-all ";
         } 
         Object userDefinedClass = tabSet.getAttributes().get("styleClass"); 
         if (userDefinedClass != null ) 
         		styleClass+= userDefinedClass.toString() ;
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, HTML.CLASS_ATTR);
         boolean isClientSide = tabSet.isClientSide();
 
 		String showEffect = tabSet.getShowEffect();
 		showEffect = showEffect == null || showEffect.trim().equals("") ? "" : showEffect;
 		int showEffectLength = tabSet.getShowEffectLength();
 
         int selectedIndex = tabSet.getSelectedIndex();
         //see what the selectedIndex is
         if (selectedIndex >= getRenderedChildCount(tabSet)) {
         	selectedIndex = 0;
         }
 
         // The tabs that are depicted as clickable by the user
         List<String> clickableTabs = new ArrayList<String>();
         boolean tabSetDisabled = tabSet.isDisabled();
         ArrayList<Integer> disabledTabs = tabSetDisabled ? null : new ArrayList<Integer>();
         Map<String, TabPaneCache> tabPaneClientId2Cache =
                 new HashMap<String, TabPaneCache>();
         doTabs(facesContext, uiComponent, Do.GET_CLIENT_IDS, clickableTabs,
                 tabPaneClientId2Cache, disabledTabs);
         if (tabSetDisabled) {
             final int num = clickableTabs.size();
             disabledTabs = new ArrayList<Integer>(num);
             for (int i = 0; i < num; i++) {
                 disabledTabs.add(i);
             }
         }
 
         // The tabs whose contents we need to render. Subset of clickableTabs,
         // where the order has a different meaning: [safeIndex] -> tabClientId
         List<String> visitedTabClientIds = tabSet.getVisitedTabClientIdsAsList();
 
         // Used to detect changes from last lifecycle
         List<String> toRender = new ArrayList<String>();
         Set<String> renderWithoutUpdate = new HashSet<String>();
         if (isClientSide) {
             toRender.addAll(clickableTabs);
         }
         else {
             for (int i = 0; i < clickableTabs.size(); i++) {
                 String tabClientId = clickableTabs.get(i);
                 TabPaneCache cache = tabPaneClientId2Cache.get(tabClientId);
                 if (cache == null) {
                     cache = TabPaneCache.get(TabPaneCache.DEFAULT);
                 }
                 if (cache.isCached() && visitedTabClientIds.contains(tabClientId)) {
 //System.out.println("toRender  cached prev  tabClientId: " + tabClientId);
                     toRender.add(tabClientId);
                     if (cache.isCachedStatically()) {
 //System.out.println("toRender  cached  statically");
                         renderWithoutUpdate.add(tabClientId);
                     }
                 } else if(selectedIndex == i) {
 //System.out.println("toRender  selectedIndex="+selectedIndex+"  tabClientId: " + tabClientId);
                     toRender.add(tabClientId);
                 }
             }
         }
 
         for (int i = 0; i < visitedTabClientIds.size(); i++) {
             String tabClientId = (String) visitedTabClientIds.get(i);
             if (tabClientId != null) {
                 if (!toRender.contains(tabClientId)) {
                     visitedTabClientIds.set(i, null);
                 }
             }
         }
         for (String tabClientId : toRender) {
             if (!visitedTabClientIds.contains(tabClientId)) {
                 visitedTabClientIds.add(tabClientId);
             }
         }
         tabSet.setVisitedTabClientIdsFromList(visitedTabClientIds);
 
         final String safeIdPrefix = clientId+"_safe_";
         int clickableLen = clickableTabs.size();
         String[] safeIds = new String[clickableLen];
         for (int i = 0; i < clickableLen; i++) {
             //System.out.println("Clickable + " + i + " of " + clickableLen + " : " + clickableTabs.get(i));
             int safeIndex = visitedTabClientIds.indexOf(clickableTabs.get(i));
             //System.out.println("  safeIndex: " + safeIndex);
             if (safeIndex >= 0) {
                 safeIds[i] = safeIdPrefix + safeIndex;
             }
             // null for clickable tabs we're not rendering
         }
         
         // Write out the safe
         writer.startElement(HTML.DIV_ELEM, uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, clientId+"_safe", HTML.ID_ATTR);
         writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", HTML.STYLE_ATTR);
         recursivelyRenderSafe(facesContext, writer, tabSet, safeIdPrefix,
                 visitedTabClientIds, renderWithoutUpdate, 0);
         writer.endElement(HTML.DIV_ELEM);
 
         // If the server is trumping the browser's selectedIndex, by reverting
         // it to the previous value, then the dom diff won't know to tell the
         // browser, so we need to induce a script update
         Integer decSelIdx = decodeSelectedIndex(facesContext, uiComponent);
         boolean unexpected = (decSelIdx != null) &&
                 (decSelIdx.intValue() != selectedIndex);
 
         JSONBuilder jb = JSONBuilder.create();
         jb.beginFunction("ice.ace.tabset.updateProperties").
         item(clientId).
         beginMap().
             entry("orientation", orientation).
            entry("showEffect", showEffect).
            entry("showEffectLength", showEffectLength).
        endMap().
         beginMap().
            entry("devMode", facesContext.isProjectStage(ProjectStage.Development)).
            entry("isClientSide", isClientSide).
            entry("aria", EnvUtils.isAriaEnabled(facesContext)).
            entry("selectedIndex", selectedIndex).
             entry("safeIds", safeIds).
             beginArray("disabledTabs");
 
             for (Integer i : disabledTabs)
                 jb.item(i);
 
             jb.endArray().
             entry("overrideSelectedIndex",
                     (unexpected ? System.currentTimeMillis() : 0));
             encodeClientBehaviors(facesContext, tabSet, jb);
         jb.endMap().
         endFunction();
 
         ScriptWriter.insertScript(facesContext, uiComponent, jb.toString());
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void recursivelyRenderSafe(FacesContext facesContext,
             ResponseWriter writer, TabSet tabSet, String idPrefix,
             List visitedTabClientIds, Set<String> renderWithoutUpdate,
             int index) throws IOException {
         if (index >= visitedTabClientIds.size()) {
             return;
         }
 
         writer.startElement(HTML.DIV_ELEM, tabSet);
         writer.writeAttribute(HTML.ID_ATTR, idPrefix+index, HTML.ID_ATTR);
         String tabClientId = (String) visitedTabClientIds.get(index);
         if (tabClientId != null) {
             if (renderWithoutUpdate.contains(tabClientId)) {
 //System.out.println("TabSetRenderer  RENDER  suppressed : " + tabClientId);
                 // Statically cached, so render nothing, and have the DOM diff
                 // check nothing and update nothing
                 doTabs(facesContext, tabSet, Do.RENDER_CONTENT_DIV_BY_CLIENT_ID,
                         visitedTabClientIds.subList(index, index+1), null, null);
             } else {
 //System.out.println("TabSetRenderer  RENDER  contents   : " + tabClientId);
                 // Dynamically cached, or not cached but rendered regardless
                 doTabs(facesContext, tabSet, Do.RENDER_CONTENTS_BY_CLIENT_ID,
                         visitedTabClientIds.subList(index, index+1), null, null);
             }
         }
         writer.endElement(HTML.DIV_ELEM);
 
         writer.startElement(HTML.DIV_ELEM, tabSet);
         writer.writeAttribute(HTML.ID_ATTR, idPrefix+index+"_nxt", HTML.ID_ATTR);
         recursivelyRenderSafe(facesContext, writer, tabSet, idPrefix,
                 visitedTabClientIds, renderWithoutUpdate, index+1);
         writer.endElement(HTML.DIV_ELEM);
 
         /*
 <div id="tabSetForm:tabSetExample_safe" style="display:none;">
 	<div id="tabSetForm:tabSetExample_safe_1"/>
 	<div id="tabSetForm:tabSetExample_safe_1_nxt">
 		<div id="tabSetForm:tabSetExample_safe_2">
 			<div id="tabSetForm:paneTwo" role="tabpanel" tabindex="0">
 				Tab contents 2 TWO
             			<iframe src="http://www.icefaces.org"/>
 			</div>
 		</div>
 		<div id="tabSetForm:tabSetExample_safe_2_nxt">
 			<div id="tabSetForm:tabSetExample_safe_3">
 				<div id="tabSetForm:paneThree" role="tabpanel" tabindex="0">
 					Tab contents 3 THREE
 				</div>
 			</div>
 			<div id="tabSetForm:tabSetExample_safe_3_nxt"/>
 		</div>
 	</div>
 </div>
         */
     }
 
    /*
     * renders tab headers 
     */
     private static void renderTabNav(FacesContext facesContext, TabSet tabSet, UIComponent tab, int index) throws IOException {
 
     	String clientId = tab.getClientId(facesContext);
         ResponseWriter writer = facesContext.getResponseWriter();
         writer.startElement(HTML.LI_ELEM, tab);
         if (EnvUtils.isAriaEnabled(facesContext)) {
             writer.writeAttribute(ARIA.ROLE_ATTR, ARIA.PRESENTATION_ROLE, ARIA.ROLE_ATTR);  
         }
         writer.writeAttribute(HTML.ID_ATTR, clientId+ "li"+ index, HTML.ID_ATTR);
         UIComponent labelFacet = ((TabPane)tab).getLabelFacet();
 		String styleClass = "ui-state-default";
 		if ("top".equals(tabSet.getOrientation())) {
 			styleClass += " ui-corner-top";
 		} else if ("bottom".equals(tabSet.getOrientation())) {
 			styleClass += " ui-corner-bottom";
 		} else {
 			styleClass += " ui-corner-all";
 		}
 		writer.writeAttribute(HTML.CLASS_ATTR, styleClass, HTML.CLASS_ATTR);
 
         writer.startElement(HTML.DIV_ELEM, tab);  
         if (EnvUtils.isAriaEnabled(facesContext)) {
             writer.writeAttribute(ARIA.ROLE_ATTR, ARIA.TAB_ROLE, ARIA.ROLE_ATTR);  
         }
         writer.writeAttribute(HTML.ID_ATTR, clientId+ "tab"+ index, HTML.ID_ATTR);
         writer.writeAttribute(HTML.TABINDEX_ATTR, "0", HTML.TABINDEX_ATTR);
         writer.writeAttribute(HTML.CLASS_ATTR, "yui-navdiv", HTML.CLASS_ATTR);           
         writer.startElement("em", tab);
         writer.writeAttribute(HTML.ID_ATTR, clientId+ "Lbl", HTML.ID_ATTR); 
         //tab header can have input elements, we don't want tab to consume any event that was initiated by an input 
         writer.writeAttribute(HTML.ONCLICK_ATTR, "if(ice.ace.util.isEventSourceInputElement(event)) event.cancelBubble = true;", HTML.ONCLICK_ATTR);
         
         if (labelFacet!= null)
             Utils.renderChild(facesContext, ((TabPane)tab).getLabelFacet());
         else
             writer.write(String.valueOf(tab.getAttributes().get("label")));
         writer.endElement("em");
         writer.endElement(HTML.DIV_ELEM);        
    
         //this is to making a tab focusable.
         writer.startElement(HTML.ANCHOR_ELEM, tab);
         writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", HTML.STYLE_ATTR); 
         writer.endElement(HTML.ANCHOR_ELEM);               
         
         writer.endElement(HTML.LI_ELEM);
     }    
     
     /*
      * Renders tab contents/body
      */
     private static void renderTabBody(FacesContext facesContext,
             TabSet tabSet, UIComponent tab, int index, Do d) throws IOException {
         String clientId = tab.getClientId(facesContext);
         ResponseWriter writer = facesContext.getResponseWriter();
         writer.startElement(HTML.DIV_ELEM, tab);
         writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
         writer.writeAttribute(HTML.TABINDEX_ATTR, 0, HTML.TABINDEX_ATTR);
 		writer.writeAttribute(HTML.CLASS_ATTR, "ui-tabs-panel ui-widget-content ui-corner-bottom", HTML.CLASS_ATTR);
         if (EnvUtils.isAriaEnabled(facesContext)) {
             writer.writeAttribute(ARIA.ROLE_ATTR, ARIA.TABPANEL_ROLE, ARIA.ROLE_ATTR);  
         }
         if (Do.RENDER_CONTENT_DIV_BY_CLIENT_ID.equals(d)) {
             writer.writeAttribute(DOMUtils.DIFF_SUPPRESS, DOMUtils.DIFF_TRUE, null);
         }
         if (!Do.RENDER_CONTENT_DIV_BY_CLIENT_ID.equals(d)) {
             Utils.renderChild(facesContext, tab);
         }
         writer.endElement(HTML.DIV_ELEM);
     }
     
     /*
      * Render children of tabset component
      */
     static void doTabs(FacesContext facesContext, UIComponent uiComponent,
             Do d, List<String> clickableTabs,
             Map<String, TabPaneCache> tabPaneClientId2Cache,
             ArrayList<Integer> disabledTabs) throws IOException{
     	TabSet tabSet = (TabSet) uiComponent;
         Iterator children = tabSet.getChildren().iterator();
         int index = -1;
         while (children.hasNext()) {
             UIComponent child = (UIComponent)children.next();
             //render tabpane component
             if (child instanceof TabPane) {
                 if (child.isRendered()) {
                     index++;
                     doTab(facesContext, tabSet, (TabPane) child, index, d,
                             clickableTabs, tabPaneClientId2Cache, disabledTabs);
                 }
             //if tabs component found, iterate through its modal and render all child.
             } else if (child instanceof Tabs) {
                 Tabs uiList = (Tabs) child;
                 int rowIndex = uiList.getFirst();
                 int numberOfRowsToDisplay = uiList.getRows();
                 int countOfRowsDisplayed = 0;
                 while (  ( numberOfRowsToDisplay == 0 ) ||
                          ( (numberOfRowsToDisplay > 0) &&
                            (countOfRowsDisplayed < numberOfRowsToDisplay) )  )
                 {
                      uiList.setRowIndex(rowIndex);
                      if(!uiList.isRowAvailable()){
                         break;
                     }
                     Iterator childs;
                     if (uiList.getChildCount() > 0) {
                         childs = uiList.getChildren().iterator();
                         while (childs.hasNext()) {
                             UIComponent nextChild = (UIComponent) childs.next();
                             if (nextChild instanceof TabPane && nextChild.isRendered()) {
                                 index++;
                                 doTab(facesContext, tabSet, (TabPane) nextChild, index, d,
                                         clickableTabs, tabPaneClientId2Cache, disabledTabs);
                             }
                         }
                     }
                     rowIndex++;
                     countOfRowsDisplayed++;
                 }
                 uiList.setRowIndex(-1);
             }
         }
     }
 
     private static void doTab(FacesContext facesContext, TabSet tabSet, TabPane tab,
             int index, Do d, List<String> clickableTabs,
             Map<String, TabPaneCache> tabPaneClientId2Cache,
             ArrayList<Integer> disabledTabs) throws IOException {
         if(Do.RENDER_LABEL.equals(d)) {
             renderTabNav(facesContext, tabSet, tab, index);
         } else if(Do.RENDER_CONTENTS.equals(d)) {
             renderTabBody(facesContext, tabSet, tab, index, d);
         } else if(Do.GET_CLIENT_IDS.equals(d)) {
             String clientId = tab.getClientId(facesContext);
             clickableTabs.add(clientId);
             TabPaneCache orig = TabPaneCache.get(tab.getCache());
             TabPaneCache cache = orig.resolve(facesContext, tab);
             tabPaneClientId2Cache.put(clientId, cache);
             TabPaneCache revert = cache.getRevertTo();
             if (disabledTabs != null && !disabledTabs.contains(index) && tab.isDisabled()) {
                 disabledTabs.add(index);
             }
             if (revert != null && revert != orig) {
                 tab.setCache(revert.getNamed());
             }
         } else if(Do.RENDER_CONTENTS_BY_CLIENT_ID.equals(d)) {
             if (clickableTabs.contains(tab.getClientId(facesContext))) {
                 renderTabBody(facesContext, tabSet, tab, index, d);
             }
         } else if(Do.RENDER_CONTENT_DIV_BY_CLIENT_ID.equals(d)) {
             if (clickableTabs.contains(tab.getClientId(facesContext))) {
                 renderTabBody(facesContext, tabSet, tab, index, d);
             }
         } else if(Do.GET_CLIENT_IDS_ONLY.equals(d)) {
             String clientId = tab.getClientId(facesContext);
             clickableTabs.add(clientId);
         }
     }
 
     private int getRenderedChildCount(UIComponent uiComponent) {
     	int count = 0;
     	for (UIComponent component: uiComponent.getChildren()) {
     		if (component instanceof TabPane && component.isRendered()) {
     			count++;
     		}
     	}
     	return count;
     }
 
 
     static enum Do {
         RENDER_LABEL,
         RENDER_CONTENTS,
         RENDER_CONTENTS_BY_CLIENT_ID,
         RENDER_CONTENT_DIV_BY_CLIENT_ID,
         GET_CLIENT_IDS,
         GET_CLIENT_IDS_ONLY
     }
 }
