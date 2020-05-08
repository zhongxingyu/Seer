 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
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
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.contextmenu;
 
 import java.io.IOException;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.icefaces.ace.component.menu.AbstractMenu;
 import org.icefaces.ace.component.menu.BaseMenuRenderer;
 import org.icefaces.ace.component.submenu.Submenu;
 import org.icefaces.ace.component.multicolumnsubmenu.MultiColumnSubmenu;
 import org.icefaces.ace.component.menuitem.MenuItem;
 import org.icefaces.ace.component.menuseparator.MenuSeparator;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.ace.util.Utils;
 import org.icefaces.render.MandatoryResourceComponent;
 import java.util.Iterator;
 
 @MandatoryResourceComponent(tagName="contextMenu", value="org.icefaces.ace.component.contextmenu.ContextMenu")
 public class ContextMenuRenderer extends BaseMenuRenderer {
 
     @Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 		AbstractMenu menu = (AbstractMenu) component;
 
 		if(menu.shouldBuildFromModel()) {
 			menu.buildMenuFromModel();
 		}
 
 		encodeMarkup(context, menu);
 	}
 	
     protected void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
         ContextMenu menu = (ContextMenu) abstractMenu;
 		String widgetVar = this.resolveWidgetVar(menu);
 		String clientId = menu.getClientId(context);
 		String trigger = findTrigger(context, menu);
 		
 		writer.startElement("script", menu);
 		writer.writeAttribute("type", "text/javascript", null);
 
         writer.write("ice.ace.jq(function() {");
 
         JSONBuilder json = JSONBuilder.create();
         json.initialiseVar(widgetVar)
             .beginFunction("ice.ace.create")
             .item("ContextMenu")
             .beginArray()
             .item(clientId)
             .beginMap()
             .entry("target", trigger, true)
             .entry("zindex", menu.getZindex())
             .entry("direction", menu.getDirection())
 
             .beginMap("animation")
             .entry("animated", menu.getEffect())
             .entry("duration", menu.getEffectDuration())
             .endMap()
 
             .entryNonNullValue("styleClass", menu.getStyleClass())
             .entryNonNullValue("style", menu.getStyle())
             .endMap()
             .endArray()
             .endFunction();
 
         writer.write(json.toString());
         writer.write("});");
 		writer.endElement("script");
 	}
 	
     protected void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException{
 		ResponseWriter writer = context.getResponseWriter();
         ContextMenu menu = (ContextMenu) abstractMenu;
 		String clientId = menu.getClientId(context);
 
         writer.startElement("span", menu);
 		writer.writeAttribute("id", clientId, "id");
 		writer.writeAttribute("style", "display:none;", null);
 
 		writer.startElement("ul", null);
 
 		encodeMenuContent(context, menu);
 
 		writer.endElement("ul");
 		
 		encodeScript(context, menu);
 
         writer.endElement("span");
 	}
 	
     protected void encodeMenuContent(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         for(Iterator<UIComponent> iterator = component.getChildren().iterator(); iterator.hasNext();) {
             UIComponent child = (UIComponent) iterator.next();
 
             if(child.isRendered()) {
 
                 writer.startElement("li", null);
 
                 if(child instanceof MenuItem) {
                     encodeMenuItem(context, (MenuItem) child);
                 } else if(child instanceof Submenu) {
                     encodeSubmenu(context, (Submenu) child);
                 } else if(child instanceof MenuSeparator) {
                     // we just need <li></li>
                 } else if(child instanceof MultiColumnSubmenu) {
 					encodeMultiColumnSubmenu(context, (MultiColumnSubmenu) child);
 				}
 
                 writer.endElement("li");
             }
         }
     }
 	
 	protected void encodeSubmenu(FacesContext context, Submenu submenu) throws IOException{
 		ResponseWriter writer = context.getResponseWriter();
         String icon = submenu.getIcon();
 
 		String label = submenu.getLabel();
 		boolean disabled = submenu.isDisabled();
 
 		writer.startElement("a", null);
 		if (disabled) {
 			writer.writeAttribute("class", "ui-state-disabled", null);
 		} else {
 			writer.writeAttribute("href", "#", null);
 		}
 
 		if(icon != null) {
 			writer.startElement("span", null);
 			writer.writeAttribute("class", icon + " wijmo-wijmenu-icon-left", null);
 			writer.endElement("span");
 		}
 
 		if(label != null) {
 			writer.startElement("span", null);
 			String style = submenu.getStyle();
 			if (style != null && style.trim().length() > 0) {
 				writer.writeAttribute("style", style, "style");
 			}
 			Utils.writeConcatenatedStyleClasses(writer, "wijmo-wijmenu-text", submenu.getStyleClass());
 			writer.write(submenu.getLabel());
 			writer.endElement("span");
 		}
 
 		writer.endElement("a");
 
         //submenus and menuitems
 		if(submenu.getChildCount() > 0 && !disabled) {
 			writer.startElement("ul", null);
 
 			encodeMenuContent(context, submenu);
 
 			writer.endElement("ul");
 		}
 	}
 
     protected String findTrigger(FacesContext context, ContextMenu menu) {
 		String trigger = null;
 		String _for = menu.getFor();
 
		if(_for != null) {
 			UIComponent forComponent = menu.findComponent(_for);
 
 			if(forComponent == null)
 				throw new FacesException("Cannot find component '" + _for + "' in view.");
 			else {
                 return "'" +  forComponent.getClientId(context) + "'";
 			}
 		}
 		else {
 			trigger = "document";
 		}
 
 		return trigger;
 	}
 }
