 /*
  * Original Code developed and contributed by Prime Technology.
  * Subsequent Code Modifications Copyright 2011 ICEsoft Technologies Canada Corp. (c)
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
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: (ICE-6978) Used JSONBuilder to add the functionality of escaping JS output.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  */
 package org.icefaces.ace.component.panel;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.icefaces.ace.component.menu.Menu;
 import org.icefaces.ace.renderkit.CoreRenderer;
 
 import org.icefaces.ace.util.Utils;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="panel", value="org.icefaces.ace.component.panel.Panel")
 public class PanelRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         Panel panel = (Panel) component;
         String clientId = panel.getClientId(context);
         Map<String, String> params = context.getExternalContext().getRequestParameterMap();
 
         //Restore toggle state
         String collapsedParam = params.get(clientId + "_collapsed");
         if(collapsedParam != null) {
             panel.setCollapsed(Boolean.valueOf(collapsedParam));
         }
 
         //Restore visibility state
         String visibleParam = params.get(clientId + "_visible");
         if(visibleParam != null) {
             panel.setVisible(Boolean.valueOf(visibleParam));
         }
 
         decodeBehaviors(context, component);
     }
 
     @Override
     public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
         Panel panel = (Panel) component;
 
         encodeMarkup(facesContext, panel);
         encodeScript(facesContext, panel);
     }
 
     protected void encodeScript(FacesContext context, Panel panel) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = panel.getClientId(context);
 
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
 
         writer.write(this.resolveWidgetVar(panel) + " = new ");
 		JSONBuilder jb = JSONBuilder.create();
 		jb.beginFunction("ice.ace.Panel")
 			.item(clientId)
 			.beginMap()
 			.entry("visible", panel.isVisible());
 
         //Toggle configuration
         if(panel.isToggleable()) {
             jb.entry("toggleable", true);
             jb.entry("toggleSpeed", panel.getToggleSpeed());
             jb.entry("collapsed", panel.isCollapsed());
         }
 
         //Toggle configuration
         if(panel.isClosable()) {
             jb.entry("closable", true);
             jb.entry("closeSpeed", panel.getCloseSpeed());
         }
 
         //Options menu configuration
         if(panel.getOptionsMenu() != null) {
             jb.entry("hasMenu", true);
         }
 
         encodeClientBehaviors(context, panel, jb);
 
         jb.endMap().endFunction();
 		writer.write(jb.toString());
 		
         writer.endElement("script");
     }
 
     protected void encodeMarkup(FacesContext context, Panel panel) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = panel.getClientId(context);
         Menu optionsMenu = panel.getOptionsMenu();
 
         writer.startElement("div", null);
         writer.writeAttribute("id", clientId, null);
 		
         String styleClass = panel.getStyleClass() ;
 		Utils.writeConcatenatedStyleClasses(writer, Panel.PANEL_CLASS, styleClass);
 		String style = panel.getStyle();
         if(style != null) {
             writer.writeAttribute("style", style, "style");
         }
 
         encodeHeader(context, panel);
         encodeContent(context, panel);
         encodeFooter(context, panel);
 
         if(panel.isToggleable()) {
             encodeStateHolder(context, panel, clientId + "_collapsed", String.valueOf(panel.isCollapsed()));
         }
 
         if(panel.isClosable()) {
             encodeStateHolder(context, panel, clientId + "_visible", String.valueOf(panel.isVisible()));
         }
 
         if (optionsMenu != null) {
             optionsMenu.setPosition("dynamic");
             optionsMenu.setTrigger(clientId + "_menu");
             optionsMenu.setMy("left top");
             optionsMenu.setAt("left bottom");
 
             optionsMenu.encodeAll(context);
         }
 
         writer.endElement("div");
     }
 
     protected void encodeHeader(FacesContext context, Panel panel) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String widgetVar = this.resolveWidgetVar(panel);
         UIComponent header = panel.getFacet("header");
         String headerText = panel.getHeader();
         String clientId = panel.getClientId(context);
 
         if(headerText == null && header == null) {
             return;
         }
 
         writer.startElement("div", null);
         writer.writeAttribute("id", clientId + "_header", null);
         writer.writeAttribute("class", Panel.PANEL_TITLEBAR_CLASS, null);
 
         //Title
         writer.startElement("span", null);
         writer.writeAttribute("class", Panel.PANEL_TITLE_CLASS, null);
 
         if(header != null) {
             renderChild(context, header);
         } else if(headerText != null) {
             writer.write(headerText);
         }
 
         writer.endElement("span");
 
         //Options
         if(panel.isClosable()) {
             encodeIcon(context, panel, "ui-icon-closethick", clientId + "_closer");
         }
 
         if(panel.isToggleable()) {
             String icon = panel.isCollapsed() ? "ui-icon-plusthick" : "ui-icon-minusthick";
             encodeIcon(context, panel, icon, clientId + "_toggler");
         }
 
         if(panel.getOptionsMenu() != null) {
             encodeIcon(context, panel, "ui-icon-gear", clientId + "_menu");
         }
 
         writer.endElement("div");
     }
 
     protected void encodeContent(FacesContext facesContext, Panel panel) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
 
         writer.startElement("div", null);
         writer.writeAttribute("id", panel.getClientId() + "_content", null);
         writer.writeAttribute("class", Panel.PANEL_CONTENT_CLASS, null);
         if (panel.isCollapsed()) {
             writer.writeAttribute("style", "display:none", null);
         }
 
         renderChildren(facesContext, panel);
 
         writer.endElement("div");
     }
 
     protected void encodeFooter(FacesContext facesContext, Panel panel) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         UIComponent footer = panel.getFacet("footer");
         String footerText = panel.getFooter();
 
         if (footer != null || footerText != null) {
             writer.startElement("div", null);
             writer.writeAttribute("id", panel.getClientId(facesContext) + "_footer", null);
             writer.writeAttribute("class", Panel.PANEL_FOOTER_CLASS, null);
 
             if (footer != null) {
                 renderChild(facesContext, footer);
             } else if (footerText != null) {
                 writer.write(footerText);
             }
 
             writer.endElement("div");
         }
     }
 
     protected void encodeIcon(FacesContext context, Panel panel, String iconClass, String id) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.startElement("a", null);
        writer.writeAttribute("href", "#", null);
         writer.writeAttribute("class", Panel.PANEL_TITLE_ICON_CLASS, null);
 
         writer.startElement("span", null);
         
         if(id != null) {
             writer.writeAttribute("id", id, null);
         }
 
         writer.writeAttribute("class", "ui-icon " + iconClass, null);
 
         writer.endElement("span");
 
         writer.endElement("a");
     }
 
     protected void encodeStateHolder(FacesContext context, Panel panel, String name, String value) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.startElement("input", null);
         writer.writeAttribute("type", "hidden", null);
         writer.writeAttribute("id", name, null);
         writer.writeAttribute("name", name, null);
         writer.writeAttribute("value", value, null);
         writer.endElement("input");
     }
 
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Do nothing
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
